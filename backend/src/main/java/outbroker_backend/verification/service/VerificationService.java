package outbroker_backend.verification.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import outbroker_backend.common.enums.VerificationStatus;
import outbroker_backend.common.exception.ResourceNotFoundException;
import outbroker_backend.user.entity.User;
import outbroker_backend.user.repository.UserRepository;
import outbroker_backend.verification.dto.DocumentSubmissionRequest;
import outbroker_backend.verification.dto.ProofSubmissionRequest;
import outbroker_backend.verification.dto.VerificationStatusResponse;
import outbroker_backend.verification.entity.VerificationSubmission;
import outbroker_backend.verification.repository.VerificationSubmissionRepository;

import java.util.Optional;
import java.util.UUID;

@Service
public class VerificationService {

    private final VerificationSubmissionRepository verificationRepository;
    private final UserRepository userRepository;

    public VerificationService(
            VerificationSubmissionRepository verificationRepository,
            UserRepository userRepository) {

        this.verificationRepository = verificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public VerificationStatusResponse submitDocument(
            UUID userId,
            DocumentSubmissionRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + userId
                        ));

        if (user.getVerificationStatus() == VerificationStatus.FULLY_VERIFIED) {
            throw new IllegalStateException(
                    "User is already fully verified"
            );
        }

        if (request.getDocumentUrl() == null ||
                request.getDocumentUrl().isBlank()) {

            throw new IllegalArgumentException(
                    "Document URL is required"
            );
        }

        VerificationSubmission submission =
                verificationRepository.findLatestByUserId(userId)
                        .orElseGet(() -> {
                            VerificationSubmission sub =
                                    new VerificationSubmission();
                            sub.setUser(user);
                            return sub;
                        });

        submission.setDocumentType(request.getDocumentType());
        submission.setDocumentUrl(request.getDocumentUrl().trim());
        submission.setStatus("PENDING");

        /*
         * A document has been submitted, but this service does not
         * perform document verification. Therefore we do not mark
         * the user as DOCUMENT_VERIFIED here.
         */
        verificationRepository.save(submission);

        return getVerificationStatus(userId);
    }

    @Transactional
    public VerificationStatusResponse submitProof(
            UUID userId,
            ProofSubmissionRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + userId
                        ));

        if (user.getVerificationStatus() == VerificationStatus.FULLY_VERIFIED) {
            throw new IllegalStateException(
                    "User is already fully verified"
            );
        }

        VerificationSubmission submission =
                verificationRepository.findLatestByUserId(userId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Must upload document verification prior to submitting physical proof"
                                ));

        if (submission.getDocumentUrl() == null ||
                submission.getDocumentUrl().isBlank()) {

            throw new IllegalArgumentException(
                    "A verification document must be submitted before physical proof"
            );
        }

        if (request.getSelfieUrl() == null ||
                request.getSelfieUrl().isBlank()) {

            throw new IllegalArgumentException(
                    "Selfie URL is required"
            );
        }

        if (request.getPhysicalProofUrl() == null ||
                request.getPhysicalProofUrl().isBlank()) {

            throw new IllegalArgumentException(
                    "Physical proof URL is required"
            );
        }

        if (request.getLatitude() == null ||
                request.getLongitude() == null) {

            throw new IllegalArgumentException(
                    "Location coordinates are required"
            );
        }

        submission.setSelfieUrl(request.getSelfieUrl().trim());
        submission.setPhysicalProofUrl(
                request.getPhysicalProofUrl().trim()
        );
        submission.setLatitude(request.getLatitude());
        submission.setLongitude(request.getLongitude());

        /*
         * Keep the submission pending until an actual verification
         * decision is performed by the appropriate verification
         * workflow.
         */
        submission.setStatus("PENDING");

        verificationRepository.save(submission);

        return getVerificationStatus(userId);
    }

    @Transactional(readOnly = true)
    public VerificationStatusResponse getVerificationStatus(
            UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + userId
                        ));

        Optional<VerificationSubmission> submission =
                verificationRepository.findLatestByUserId(userId);

        String docUrl = submission
                .map(VerificationSubmission::getDocumentUrl)
                .orElse(null);

        String docType = submission
                .map(VerificationSubmission::getDocumentType)
                .orElse(null);

        boolean hasProof = submission
                .map(s ->
                        s.getSelfieUrl() != null &&
                        !s.getSelfieUrl().isBlank() &&
                        s.getPhysicalProofUrl() != null &&
                        !s.getPhysicalProofUrl().isBlank()
                )
                .orElse(false);

        return new VerificationStatusResponse(
                userId,
                user.getVerificationStatus(),
                docUrl,
                docType,
                hasProof
        );
    }
}