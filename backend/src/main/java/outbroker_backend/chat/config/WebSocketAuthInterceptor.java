package outbroker_backend.chat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import outbroker_backend.auth.service.JwtService;
import outbroker_backend.chat.entity.ChatRoom;
import outbroker_backend.chat.repository.ChatRoomRepository;
import outbroker_backend.user.entity.User;
import outbroker_backend.user.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel
    ) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(
                        message,
                        StompHeaderAccessor.class
                );

        if (accessor == null) {
            throw new IllegalArgumentException(
                    "Invalid WebSocket message"
            );
        }

        StompCommand command = accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {
            authenticate(accessor);
        }

        if (StompCommand.SEND.equals(command)) {
            authorizeSend(accessor);
        }

        if (StompCommand.SUBSCRIBE.equals(command)) {
            authorizeSubscribe(accessor);
        }

        return message;
    }

    private void authenticate(
            StompHeaderAccessor accessor
    ) {

        String authorization =
                accessor.getFirstNativeHeader("Authorization");

        if (authorization == null
                || !authorization.startsWith("Bearer ")) {

            throw new IllegalArgumentException(
                    "WebSocket authentication required"
            );
        }

        String jwt = authorization.substring(7);

        try {

            String phoneNumber =
                    jwtService.extractPhoneNumber(jwt);

            if (phoneNumber == null) {
                throw new IllegalArgumentException(
                        "Invalid WebSocket token"
                );
            }

            User user = userRepository
                    .findByPhoneNumber(phoneNumber)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "User not found"
                            ));

            if (!jwtService.isTokenValid(jwt, user)) {
                throw new IllegalArgumentException(
                        "Invalid or expired WebSocket token"
                );
            }

            List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority(
                            user.getRole().name()
                    ),
                    new SimpleGrantedAuthority(
                            "ROLE_" + user.getRole().name()
                    )
            );

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            authorities
                    );

            accessor.setUser(authentication);

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "WebSocket authentication failed",
                    e
            );
        }
    }

    private void authorizeSend(
            StompHeaderAccessor accessor
    ) {

        Authentication authentication =
        (Authentication) SimpMessageHeaderAccessor.getUser(
                accessor.getMessageHeaders()
        );

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new IllegalArgumentException(
                    "WebSocket authentication required"
            );
        }

        String destination =
                accessor.getDestination();

        if (!"/app/chat.sendMessage".equals(destination)) {

            throw new IllegalArgumentException(
                    "Unauthorized WebSocket destination"
            );
        }
    }

    private void authorizeSubscribe(
            StompHeaderAccessor accessor
    ) {

        Authentication authentication =
        (Authentication) SimpMessageHeaderAccessor.getUser(
                accessor.getMessageHeaders()
        );

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new IllegalArgumentException(
                    "WebSocket authentication required"
            );
        }

        String destination =
                accessor.getDestination();

        if (destination == null) {
            throw new IllegalArgumentException(
                    "WebSocket subscription destination is required"
            );
        }

        // Private user queue
        if ("/user/queue/messages".equals(destination)) {
            return;
        }

        // Room subscription
        if (destination.startsWith("/topic/room.")) {

            String roomIdText =
                    destination.substring(
                            "/topic/room.".length()
                    );

            UUID roomId;

            try {
                roomId = UUID.fromString(roomIdText);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Invalid chat room ID"
                );
            }

            UUID userId = extractUserId(authentication);

            ChatRoom room =
                    chatRoomRepository.findById(roomId)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Chat room not found"
                                    ));

            boolean participant =
                    room.getTenantId().equals(userId)
                            || room.getLandlordId().equals(userId);

            if (!participant) {
                throw new IllegalArgumentException(
                        "You are not a participant in this chat room"
                );
            }

            return;
        }

        throw new IllegalArgumentException(
                "Unauthorized WebSocket subscription"
        );
    }

    private UUID extractUserId(
            Authentication authentication
    ) {

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof User user)) {
            throw new IllegalArgumentException(
                    "Invalid WebSocket user"
            );
        }

        return user.getId();
    }
}