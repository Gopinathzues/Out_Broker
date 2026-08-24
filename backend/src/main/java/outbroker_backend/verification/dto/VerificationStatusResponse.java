package outbroker_backend.verification.dto;

import outbroker_backend.common.enums.VerificationStatus;

import java.util.UUID;

public class VerificationStatusResponse {

    private UUID userId;
    private VerificationStatus currentStatus;
    private String latestDocumentUrl;
    private String latestDocumentType;
    private boolean hasProofSubmitted;

    public VerificationStatusResponse(UUID userId, VerificationStatus currentStatus, String latestDocumentUrl, String latestDocumentType, boolean hasProofSubmitted) {
        this.userId = userId;
        this.currentStatus = currentStatus;
        this.latestDocumentUrl = latestDocumentUrl;
        this.latestDocumentType = latestDocumentType;
        this.hasProofSubmitted = hasProofSubmitted;
    }

    public UUID getUserId() { return userId; }
    public VerificationStatus getCurrentStatus() { return currentStatus; }
    public String getLatestDocumentUrl() { return latestDocumentUrl; }
    public String getLatestDocumentType() { return latestDocumentType; }
    public boolean isHasProofSubmitted() { return hasProofSubmitted; }
}