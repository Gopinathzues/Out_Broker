package outbroker_backend.verification.dto;

import jakarta.validation.constraints.NotBlank;

public class DocumentSubmissionRequest {

    @NotBlank(message = "Document type is required")
    private String documentType;

    @NotBlank(message = "Document URL is required")
    private String documentUrl;

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public String getDocumentUrl() { return documentUrl; }
    public void setDocumentUrl(String documentUrl) { this.documentUrl = documentUrl; }
}