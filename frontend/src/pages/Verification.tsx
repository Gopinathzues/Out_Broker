import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import {
  AlertTriangle,
  ArrowLeft,
  ArrowRight,
  Camera,
  Check,
  CheckCircle2,
  FileText,
  Home as HomeIcon,
  Lock,
  MapPin,
  ShieldCheck,
} from "lucide-react";
import { useAuth } from "../context/AuthContext";
import CameraCapture from "../components/CameraCapture";
import "./Verification.css";

type VerificationStep = 1 | 2 | 3 | 4;

interface VerificationLocationState {
  from?: string;
}

const steps = [
  {
    number: 1,
    title: "Identity document",
    shortTitle: "Document",
  },
  {
    number: 2,
    title: "Selfie verification",
    shortTitle: "Selfie",
  },
  {
    number: 3,
    title: "Property proof",
    shortTitle: "Proof",
  },
  {
    number: 4,
    title: "Location verification",
    shortTitle: "Location",
  },
];

function Verification() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, updateUser } = useAuth();

  /*
   * Preserve where the user actually wanted to go.
   *
   * Example:
   *
   * Add Property (not verified)
   *   ↓
   * Verification            (from: "/add-property")
   *   ↓
   * Add Property
   *
   * Falls back to "/add-property" since that is the only
   * real destination verification currently unlocks.
   */
  const locationState =
    location.state as VerificationLocationState | null;

  const intendedDestination =
    locationState?.from || "/add-property";

  const alreadyFullyVerified =
    user?.verificationStatus?.toUpperCase() ===
    "FULLY_VERIFIED";

  /*
   * FRONTEND-ONLY DEV ESCAPE HATCH
   *
   * This lets a developer re-open the wizard after already
   * being marked FULLY_VERIFIED, purely to keep testing the
   * document/selfie/proof/location steps without clearing
   * localStorage by hand. It is clearly labeled "(dev)" in
   * the UI and does nothing a real backend integration needs
   * to keep — safe to delete once real verification data
   * exists.
   */
  const [devRetakeVerification, setDevRetakeVerification] =
    useState(false);

  const [currentStep, setCurrentStep] =
    useState<VerificationStep>(1);

  const [documentPhoto, setDocumentPhoto] =
    useState<string | null>(null);

  const [selfiePhoto, setSelfiePhoto] =
    useState<string | null>(null);

  const [proofPhoto, setProofPhoto] =
    useState<string | null>(null);

  const [locationConfirmed, setLocationConfirmed] =
    useState(false);

  const [error, setError] = useState("");

  const goBack = () => {
    if (currentStep > 1) {
      setCurrentStep(
        (currentStep - 1) as VerificationStep
      );
      setError("");
      return;
    }

    /*
     * At step 1, return to wherever the user actually came
     * from (Registration, or the owner flow that sent them
     * here) instead of always forcing them back to Home.
     */
    if (window.history.length > 1) {
      navigate(-1);
    } else {
      navigate("/home");
    }
  };

  const continueStep = () => {
    setError("");

    if (currentStep === 1 && !documentPhoto) {
      setError("Please capture a photo of your identity document.");
      return;
    }

    if (currentStep === 2 && !selfiePhoto) {
      setError("Please capture your verification selfie.");
      return;
    }

    if (currentStep === 3 && !proofPhoto) {
      setError("Please capture your property ownership proof.");
      return;
    }

    if (currentStep === 4 && !locationConfirmed) {
      setError("Please confirm your property location.");
      return;
    }

    if (currentStep < 4) {
      setCurrentStep(
        (currentStep + 1) as VerificationStep
      );
      return;
    }

    /*
     * FRONTEND PROTOTYPE ONLY
     *
     * The backend will eventually return the real
     * verification status.
     *
     * Keeping this update isolated here makes it
     * easy for the backend team to replace later.
     */
    updateUser({
      verificationStatus: "FULLY_VERIFIED",
    });

    navigate(intendedDestination);
  };

  const renderStepContent = () => {
    switch (currentStep) {
      case 1:
        return (
          <>
            <div className="verification-icon">
              <FileText size={24} strokeWidth={2} />
            </div>

            <span className="step-label">
              STEP 1 OF 4
            </span>

            <h2>Upload an identity document</h2>

            <p className="step-description">
              Upload a clear copy of a government-issued
              identity document for verification.
            </p>

            <div className="security-box">
              <strong><Lock size={13} strokeWidth={2.4} /> Your document is secure</strong>
              <span>
                It will only be used for verification
                purposes.
              </span>
            </div>

            <CameraCapture
              value={documentPhoto}
              onCapture={(dataUrl) => {
                setDocumentPhoto(dataUrl);
                setError("");
              }}
              facingMode="environment"
              triggerLabel="Capture document"
              description="Use your back camera to photograph it"
            />

            <p className="upload-note">
              Make sure the name on the document matches
              your account.
            </p>
          </>
        );

      case 2:
        return (
          <>
            <div className="verification-icon">
              <Camera size={24} strokeWidth={2} />
            </div>

            <span className="step-label">
              STEP 2 OF 4
            </span>

            <h2>Verify your identity</h2>

            <p className="step-description">
              Upload a clear selfie so your identity can
              be compared with your submitted document.
            </p>

            <div className="security-box">
              <strong><Lock size={13} strokeWidth={2.4} /> Secure verification</strong>
              <span>
                Your selfie is used only for identity
                verification.
              </span>
            </div>

            <CameraCapture
              value={selfiePhoto}
              onCapture={(dataUrl) => {
                setSelfiePhoto(dataUrl);
                setError("");
              }}
              facingMode="user"
              triggerLabel="Capture selfie"
              description="Use your front camera, face centered"
            />
          </>
        );

      case 3:
        return (
          <>
            <div className="verification-icon">
              <HomeIcon size={24} strokeWidth={2} />
            </div>

            <span className="step-label">
              STEP 3 OF 4
            </span>

            <h2>Provide ownership proof</h2>

            <p className="step-description">
              Upload a document that helps establish
              your ownership or legal right to list this
              property.
            </p>

            <div className="security-box">
              <strong><Lock size={13} strokeWidth={2.4} /> Your information is secure</strong>
              <span>
                Documents are used for verification
                purposes only.
              </span>
            </div>

            <CameraCapture
              value={proofPhoto}
              onCapture={(dataUrl) => {
                setProofPhoto(dataUrl);
                setError("");
              }}
              facingMode="environment"
              triggerLabel="Capture ownership proof"
              description="Use your back camera to photograph it"
            />
          </>
        );

      case 4:
        return (
          <>
            <div className="verification-icon">
              <MapPin size={24} strokeWidth={2} />
            </div>

            <span className="step-label">
              STEP 4 OF 4
            </span>

            <h2>Confirm your property location</h2>

            <p className="step-description">
              Confirm that the property location you
              provide is accurate.
            </p>

            <div className="location-card">
              <div className="location-symbol">
                <MapPin size={19} strokeWidth={2.2} />
              </div>

              <div>
                <strong>
                  Location confirmation
                </strong>

                <span>
                  Your location information will be used
                  to verify the property listing.
                </span>
              </div>
            </div>

            <label className="checkbox-row">
              <input
                type="checkbox"
                checked={locationConfirmed}
                onChange={(event) => {
                  setLocationConfirmed(
                    event.target.checked
                  );
                  setError("");
                }}
              />

              <span>
                I confirm that the property location
                information is accurate.
              </span>
            </label>
          </>
        );
    }
  };

  return (
    <main className="verification-page">
      <div className="verification-background-circle circle-one" />
      <div className="verification-background-circle circle-two" />

      <div className="verification-shell">
        <button
          type="button"
          className="back-button"
          onClick={goBack}
          aria-label="Go back"
        >
          <ArrowLeft size={20} strokeWidth={2.2} />
        </button>

        <header className="verification-brand">
          <div className="brand-logo">
            <HomeIcon
              className="brand-roof"
              size={28}
              strokeWidth={2.2}
            />
          </div>

          <div className="brand-name">
            <span>Out</span>
            <strong>Broker</strong>
          </div>

          <p>Chennai Properties</p>

          <div className="brand-line">
            <span />
            <span />
            <span />
          </div>
        </header>

        <section className="verification-card">
          <div className="verification-intro">
            <span className="owner-badge">
              <ShieldCheck size={14} strokeWidth={2.3} />
              Secure owner verification
            </span>

            <h1>Verify your ownership</h1>

            <p>
              Complete these steps to become a verified
              property owner on OutBroker.
            </p>
          </div>

          {alreadyFullyVerified && !devRetakeVerification ? (
            /*
             * The account is already FULLY_VERIFIED (either
             * from a previous session, or because the backend
             * will eventually report it that way). Re-opening
             * /verification should never force this person
             * through the wizard again — it should simply
             * reflect their real status.
             */
            <div className="verification-content">
              <div className="verification-icon success">
                <CheckCircle2 size={26} strokeWidth={2} />
              </div>

              <span className="step-label">
                VERIFICATION COMPLETE
              </span>

              <h2>You're a verified owner</h2>

              <p className="step-description">
                Your identity, selfie, ownership proof and
                location have all been verified. You can list
                properties on OutBroker.
              </p>

              <div className="security-box success">
                <strong><Lock size={13} strokeWidth={2.4} /> Verified account</strong>
                <span>
                  This status comes from your account, not
                  from this page — it will stay accurate even
                  if you reopen Verification later.
                </span>
              </div>

              <div className="verification-actions">
                <button
                  type="button"
                  className="secondary-button"
                  onClick={() => {
                    setDevRetakeVerification(true);
                    setCurrentStep(1);
                    setError("");
                  }}
                >
                  Redo (dev)
                </button>

                <button
                  type="button"
                  className="primary-button"
                  onClick={() =>
                    navigate(intendedDestination)
                  }
                >
                  <span>Continue to Add Property</span>
                  <ArrowRight size={16} strokeWidth={2.4} />
                </button>
              </div>
            </div>
          ) : (
            <>
              <div className="progress-wrapper">
                {steps.map((step, index) => {
                  const isActive =
                    step.number === currentStep;

                  const isCompleted =
                    step.number < currentStep;

                  return (
                    <div
                      className="progress-item"
                      key={step.number}
                    >
                      <div
                        className={`progress-number ${
                          isActive
                            ? "active"
                            : isCompleted
                            ? "completed"
                            : ""
                        }`}
                      >
                        {isCompleted ? (
                          <Check size={14} strokeWidth={3} />
                        ) : (
                          step.number
                        )}
                      </div>

                      <span>
                        {step.shortTitle}
                      </span>

                      {index < steps.length - 1 && (
                        <div
                          className={`progress-line ${
                            isCompleted
                              ? "completed"
                              : ""
                          }`}
                        />
                      )}
                    </div>
                  );
                })}
              </div>

              <div className="verification-divider" />

              <div
                className="verification-content"
                key={currentStep}
              >
                {renderStepContent()}

                {error && (
                  <div className="verification-error">
                    <AlertTriangle
                      size={16}
                      strokeWidth={2.2}
                      aria-hidden="true"
                    />
                    <span>{error}</span>
                  </div>
                )}

                <div className="verification-actions">
                  {currentStep > 1 && (
                    <button
                      type="button"
                      className="secondary-button"
                      onClick={() => {
                        setCurrentStep(
                          (currentStep -
                            1) as VerificationStep
                        );
                        setError("");
                      }}
                    >
                      Back
                    </button>
                  )}

                  <button
                    type="button"
                    className="primary-button"
                    onClick={continueStep}
                  >
                    <span>
                      {currentStep === 4
                        ? "Complete verification"
                        : "Continue"}
                    </span>

                    <ArrowRight
                      size={16}
                      strokeWidth={2.4}
                      aria-hidden="true"
                    />
                  </button>
                </div>
              </div>
            </>
          )}

          <div className="verification-trust-line">
            <span
              className="trust-line-icon"
              aria-hidden="true"
            >
              <Check size={12} strokeWidth={3} />
            </span>

            <span>
              Your verification information is handled
              securely and used only for owner
              verification.
            </span>
          </div>
        </section>

        <footer className="verification-footer">
          <ShieldCheck size={14} strokeWidth={2.2} />
          Safe. Simple. Direct.
        </footer>
      </div>
    </main>
  );
}

export default Verification;