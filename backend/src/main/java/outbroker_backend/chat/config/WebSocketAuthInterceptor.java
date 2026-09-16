package outbroker_backend.chat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import outbroker_backend.auth.service.JwtService;
import outbroker_backend.chat.entity.ChatRoom;
import outbroker_backend.chat.repository.ChatRoomRepository;
import outbroker_backend.user.entity.User;
import outbroker_backend.user.repository.UserRepository;

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
            MessageChannel channel) {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(message);

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

    // =========================================================
    // AUTHENTICATE STOMP CONNECTION
    // =========================================================

    private void authenticate(StompHeaderAccessor accessor) {

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

            User user =
                    userRepository.findByPhoneNumber(phoneNumber)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "User not found"
                                    )
                            );

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

            /*
             * IMPORTANT:
             *
             * The principal name is explicitly set to the
             * authenticated user's UUID.
             *
             * This matches the value used by:
             *
             * convertAndSendToUser(
             *     recipientId.toString(),
             *     "/queue/messages",
             *     response
             * )
             */
            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(
                            user.getId().toString(),
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

    // =========================================================
    // AUTHORIZE SEND
    // =========================================================

    private void authorizeSend(
            StompHeaderAccessor accessor) {

        String destination =
                accessor.getDestination();

        if (!"/app/chat.sendMessage".equals(destination)) {

            throw new IllegalArgumentException(
                    "Unauthorized WebSocket destination"
            );
        }

        /*
         * SEND must come from an authenticated STOMP connection.
         */
        Authentication authentication =
                getAuthentication(accessor);

        if (authentication == null) {

            throw new IllegalArgumentException(
                    "WebSocket authentication required"
            );
        }
    }

    // =========================================================
    // AUTHORIZE SUBSCRIBE
    // =========================================================

    private void authorizeSubscribe(
            StompHeaderAccessor accessor) {

        String destination =
                accessor.getDestination();

        if (destination == null) {

            throw new IllegalArgumentException(
                    "WebSocket subscription destination is required"
            );
        }

        Authentication authentication =
                getAuthentication(accessor);

        if (authentication == null) {

            throw new IllegalArgumentException(
                    "WebSocket authentication required"
            );
        }

        // ---------------------------------------------------------
        // PRIVATE USER QUEUE
        // ---------------------------------------------------------

        if ("/user/queue/messages".equals(destination)) {
            return;
        }

        // ---------------------------------------------------------
        // CHAT ROOM TOPIC
        // ---------------------------------------------------------

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

            UUID userId =
                    extractUserId(authentication);

            ChatRoom room =
                    chatRoomRepository.findById(roomId)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Chat room not found"
                                    )
                            );

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

        // ---------------------------------------------------------
        // UNKNOWN DESTINATION
        // ---------------------------------------------------------

        throw new IllegalArgumentException(
                "Unauthorized WebSocket subscription"
        );
    }

    // =========================================================
    // AUTHENTICATION HELPER
    // =========================================================

    private Authentication getAuthentication(
            StompHeaderAccessor accessor) {

        Object user =
                accessor.getUser();

        if (user instanceof Authentication authentication) {
            return authentication;
        }

        return null;
    }

    // =========================================================
    // USER ID HELPER
    // =========================================================

    private UUID extractUserId(
            Authentication authentication) {

        if (authentication == null) {

            throw new IllegalArgumentException(
                    "WebSocket authentication required"
            );
        }

        Object principal =
                authentication.getPrincipal();

        if (principal instanceof String principalId) {

            try {

                return UUID.fromString(principalId);

            } catch (IllegalArgumentException e) {

                throw new IllegalArgumentException(
                        "Invalid authenticated user ID"
                );
            }
        }

        throw new IllegalArgumentException(
                "Invalid WebSocket principal"
        );
    }
}