package outbroker_backend.verification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProofSubmissionRequest {

    @NotBlank(message = "Selfie URL is required")
    private String selfieUrl;

    @NotBlank(message = "Physical proof URL is required")
    private String physicalProofUrl;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    public String getSelfieUrl() { return selfieUrl; }
    public void setSelfieUrl(String selfieUrl) { this.selfieUrl = selfieUrl; }

    public String getPhysicalProofUrl() { return physicalProofUrl; }
    public void setPhysicalProofUrl(String physicalProofUrl) { this.physicalProofUrl = physicalProofUrl; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}