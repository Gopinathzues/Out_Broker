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

    public VerificationService(VerificationSubmissionRepository verificationRepository, UserRepository userRepository) {
        this.verificationRepository = verificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
public VerificationStatusResponse submitDocument(UUID userId, DocumentSubmissionRequest request) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

    VerificationSubmission submission = verificationRepository.findLatestByUserId(userId)
        .orElseGet(() -> {
            VerificationSubmission sub = new VerificationSubmission();
            sub.setUser(user);
            return sub;
        });

    submission.setDocumentType(request.getDocumentType());
    submission.setDocumentUrl(request.getDocumentUrl());
    submission.setStatus("PENDING");
    
    verificationRepository.save(submission);

    user.setVerificationStatus(VerificationStatus.DOCUMENT_VERIFIED);
    userRepository.save(user);

    return getVerificationStatus(userId);
}

    @Transactional
    public VerificationStatusResponse submitProof(UUID userId, ProofSubmissionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

       VerificationSubmission submission = verificationRepository.findLatestByUserId(userId)
        .orElseThrow(() -> new IllegalArgumentException("Must upload document verification prior to submitting physical proof"));

        submission.setSelfieUrl(request.getSelfieUrl());
        submission.setPhysicalProofUrl(request.getPhysicalProofUrl());
        submission.setLatitude(request.getLatitude());
        submission.setLongitude(request.getLongitude());
        submission.setStatus("APPROVED");
        verificationRepository.save(submission);

        user.setVerificationStatus(VerificationStatus.FULLY_VERIFIED);
        userRepository.save(user);

        return getVerificationStatus(userId);
    }

    @Transactional(readOnly = true)
    public VerificationStatusResponse getVerificationStatus(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Optional<VerificationSubmission> submission = verificationRepository.findLatestByUserId(userId);
        String docUrl = submission.map(VerificationSubmission::getDocumentUrl).orElse(null);
        String docType = submission.map(VerificationSubmission::getDocumentType).orElse(null);
        boolean hasProof = submission.map(s -> s.getSelfieUrl() != null && s.getPhysicalProofUrl() != null).orElse(false);

        return new VerificationStatusResponse(userId, user.getVerificationStatus(), docUrl, docType, hasProof);
    }
}