import {
  useEffect,
  useRef,
  useState,
} from "react";
import type {
  FormEvent,
  KeyboardEvent,
} from "react";

import {
  useLocation,
  useNavigate,
} from "react-router-dom";

import {
  ArrowLeft,
  Check,
  ChevronRight,
  LoaderCircle,
  MapPin,
  MessageCircle,
  Phone,
  ShieldCheck,
} from "lucide-react";

import "../App.css";
import { useAuth, type User } from "../context/AuthContext";

type AuthStep = "phone" | "otp";

/*
 * ------------------------------------------------------------
 * DEV-ONLY HELPER
 * ------------------------------------------------------------
 *
 * Reads whatever user object AuthContext last persisted for
 * this browser, purely so the temporary "loginForDevelopment"
 * flow below can simulate an existing account instead of
 * always resetting verification progress on every login.
 *
 * The backend team can delete this helper entirely once real
 * authentication is wired up — the OTP verify response will
 * already carry the account's verificationStatus.
 */
function readPersistedDevUser(): User | null {
  try {
    const stored = localStorage.getItem("user");
    return stored ? (JSON.parse(stored) as User) : null;
  } catch {
    return null;
  }
}

function Login() {
  const navigate = useNavigate();
  const location = useLocation();

  const { loginForDevelopment } = useAuth();

  const [authStep, setAuthStep] =
    useState<AuthStep>("phone");

  const [phoneNumber, setPhoneNumber] =
    useState("");

  const [otp, setOtp] = useState([
    "",
    "",
    "",
    "",
    "",
    "",
  ]);

  const [acceptedTerms, setAcceptedTerms] =
    useState(false);

  const [isSendingOtp, setIsSendingOtp] =
    useState(false);

  const [isVerifying, setIsVerifying] =
    useState(false);

  const [countdown, setCountdown] =
    useState(30);

  const [errorMessage, setErrorMessage] =
    useState("");

  const otpRefs =
    useRef<Array<HTMLInputElement | null>>([]);

  /*
   * ------------------------------------------------------------
   * PHONE NUMBER
   * ------------------------------------------------------------
   */

  const handlePhoneChange = (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    const value = event.target.value
      .replace(/\D/g, "")
      .slice(0, 10);

    setPhoneNumber(value);
    setErrorMessage("");
  };

  /*
   * ------------------------------------------------------------
   * SEND OTP
   * ------------------------------------------------------------
   */

  const handleSendOtp = (
    event: FormEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    if (isSendingOtp) return;

    if (phoneNumber.length !== 10) {
      setErrorMessage(
        "Enter a valid 10-digit mobile number."
      );
      return;
    }

    if (!acceptedTerms) {
      setErrorMessage(
        "Please accept the Terms & Conditions and Privacy Policy."
      );
      return;
    }

    setErrorMessage("");
    setIsSendingOtp(true);

    /*
     * TEMPORARY FRONTEND BEHAVIOUR
     *
     * Backend replacement:
     * POST /auth/send-otp
     */

    setTimeout(() => {
      setIsSendingOtp(false);
      setAuthStep("otp");
      setCountdown(30);

      setTimeout(() => {
        otpRefs.current[0]?.focus();
      }, 100);
    }, 900);
  };

  /*
   * ------------------------------------------------------------
   * OTP
   * ------------------------------------------------------------
   */

  const handleOtpChange = (
    index: number,
    value: string
  ) => {
    const numericValue = value
      .replace(/\D/g, "")
      .slice(-1);

    const newOtp = [...otp];

    newOtp[index] = numericValue;

    setOtp(newOtp);
    setErrorMessage("");

    if (
      numericValue &&
      index < 5
    ) {
      otpRefs.current[
        index + 1
      ]?.focus();
    }
  };

  const handleOtpKeyDown = (
    index: number,
    event: KeyboardEvent<HTMLInputElement>
  ) => {
    if (
      event.key === "Backspace" &&
      !otp[index] &&
      index > 0
    ) {
      otpRefs.current[
        index - 1
      ]?.focus();
    }

    if (
      event.key === "ArrowLeft" &&
      index > 0
    ) {
      otpRefs.current[
        index - 1
      ]?.focus();
    }

    if (
      event.key === "ArrowRight" &&
      index < 5
    ) {
      otpRefs.current[
        index + 1
      ]?.focus();
    }
  };

  const handleOtpPaste = (
    event: React.ClipboardEvent<HTMLInputElement>
  ) => {
    event.preventDefault();

    const pastedValue =
      event.clipboardData
        .getData("text")
        .replace(/\D/g, "")
        .slice(0, 6);

    if (!pastedValue) return;

    const newOtp = [
      "",
      "",
      "",
      "",
      "",
      "",
    ];

    pastedValue
      .split("")
      .forEach((digit, index) => {
        newOtp[index] = digit;
      });

    setOtp(newOtp);

    const nextIndex = Math.min(
      pastedValue.length,
      5
    );

    setTimeout(() => {
      otpRefs.current[
        nextIndex
      ]?.focus();
    }, 50);
  };

  /*
   * ------------------------------------------------------------
   * VERIFY OTP
   * ------------------------------------------------------------
   */

  const handleVerifyOtp = (
    event: FormEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    if (isVerifying) return;

    const enteredOtp =
      otp.join("");

    if (enteredOtp.length !== 6) {
      setErrorMessage(
        "Enter the complete 6-digit OTP."
      );
      return;
    }

    setErrorMessage("");
    setIsVerifying(true);

    /*
     * TEMPORARY FRONTEND BEHAVIOUR
     *
     * Backend replacement:
     * POST /auth/verify-otp
     *
     * The backend should return something similar to:
     *
     * {
     *   token: "...",
     *   user: {
     *     id: ...,
     *     phone: "...",
     *     role: "TENANT" | "LANDLORD",
     *     verificationStatus: "..."
     *   }
     * }
     */

    setTimeout(() => {
      /*
       * --------------------------------------------------------
       * TEMPORARY DEVELOPMENT AUTHENTICATION
       * --------------------------------------------------------
       *
       * We need a real AuthContext session during frontend
       * development so ProtectedRoute can actually be tested.
       *
       * This user is temporarily treated as an existing
       * LANDLORD account.
       *
       * IMPORTANT (verification state):
       *
       * We must NOT hard-code "FULLY_VERIFIED" here. Doing so
       * previously made it impossible to test the "existing
       * owner who still needs to verify" path (Scenario C/D),
       * and it also hid the real problem: verification status
       * belongs to the ACCOUNT, not to the login action.
       *
       * So this reuses whatever verification status is already
       * on this device for this dev account (i.e. if the user
       * already completed verification earlier in this browser,
       * logging in again correctly keeps them verified). A brand
       * new dev session starts as "PENDING", exactly like a real
       * existing owner who registered but hasn't finished
       * verification yet — so /add-property will correctly send
       * them to /verification first.
       *
       * The backend will replace this entire block with the
       * real /auth/verify-otp response, which will already
       * include the account's true verificationStatus.
       *
       * DO NOT consider this production authentication.
       */
      const previousDevUser = readPersistedDevUser();

      loginForDevelopment({
        id: 1,
        phone: phoneNumber,
        role: previousDevUser?.role ?? "LANDLORD",
        verificationStatus:
          previousDevUser?.verificationStatus ?? "PENDING",
      });

      setIsVerifying(false);

      /*
       * If the user was originally trying to access a
       * protected page, return them there.
       *
       * Otherwise go to Home.
       */
      const from =
        location.state?.from;

      if (
        typeof from === "string" &&
        from.length > 0
      ) {
        navigate(from, {
          replace: true,
        });
      } else {
        navigate("/home", {
          replace: true,
        });
      }
    }, 1000);
  };

  /*
   * ------------------------------------------------------------
   * RESEND OTP
   * ------------------------------------------------------------
   */

  const handleResendOtp = () => {
    if (
      countdown > 0 ||
      isSendingOtp
    ) {
      return;
    }

    setOtp([
      "",
      "",
      "",
      "",
      "",
      "",
    ]);

    setCountdown(30);
    setErrorMessage("");

    /*
     * Backend replacement:
     * POST /auth/resend-otp
     */

    setTimeout(() => {
      otpRefs.current[0]?.focus();
    }, 50);
  };

  /*
   * ------------------------------------------------------------
   * CHANGE NUMBER
   * ------------------------------------------------------------
   */

  const handleChangeNumber = () => {
    setAuthStep("phone");

    setOtp([
      "",
      "",
      "",
      "",
      "",
      "",
    ]);

    setErrorMessage("");
  };

  /*
   * ------------------------------------------------------------
   * GUEST
   * ------------------------------------------------------------
   */

  const handleGuestLogin = () => {
    /*
     * Guest users intentionally do NOT receive
     * an authentication token.
     *
     * They can browse public pages such as:
     * - Home
     * - Property Details
     *
     * Protected actions will still require authentication.
     */

    navigate("/home", {
      replace: true,
    });
  };

  /*
   * ------------------------------------------------------------
   * CREATE PROFILE
   * ------------------------------------------------------------
   */

  const handleCreateProfile = () => {
    navigate("/register");
  };

  /*
   * ------------------------------------------------------------
   * BACK BUTTON
   * ------------------------------------------------------------
   */

  const handleBack = () => {
    if (authStep === "otp") {
      handleChangeNumber();
      return;
    }

    if (window.history.length > 1) {
      navigate(-1);
    } else {
      navigate("/home", {
        replace: true,
      });
    }
  };

  /*
   * ------------------------------------------------------------
   * OTP COUNTDOWN
   * ------------------------------------------------------------
   */

  useEffect(() => {
    if (
      authStep !== "otp" ||
      countdown <= 0
    ) {
      return;
    }

    const timer =
      window.setInterval(() => {
        setCountdown(
          (current) =>
            current > 0
              ? current - 1
              : 0
        );
      }, 1000);

    return () => {
      window.clearInterval(timer);
    };
  }, [
    authStep,
    countdown,
  ]);

  /*
   * ------------------------------------------------------------
   * RENDER
   * ------------------------------------------------------------
   */

  return (
    <main className="login-page">
      {/* Animated background */}
      <div
        className="background-decoration"
        aria-hidden="true"
      >
        <span className="orb orb-one" />
        <span className="orb orb-two" />
        <span className="orb orb-three" />
        <span className="orb orb-four" />

        <span className="floating-dot dot-one" />
        <span className="floating-dot dot-two" />
        <span className="floating-dot dot-three" />
      </div>

      {/* Back button */}
      <button
        className="back-button"
        type="button"
        aria-label={
          authStep === "otp"
            ? "Go back to phone number"
            : "Go back"
        }
        onClick={handleBack}
      >
        <ArrowLeft
          size={21}
          strokeWidth={2.2}
        />
      </button>

      <section className="login-shell">
        {/* =====================================================
            BRAND
        ====================================================== */}

        <header className="brand-section">
          <div className="brand-mark">
            <div className="brand-mark-inner">
              <span className="brand-house-roof" />
              <span className="brand-house-body" />
              <span className="brand-house-door" />
            </div>
          </div>

          <div className="brand-name">
            <span className="brand-red">
              Out
            </span>

            <span className="brand-blue">
              Broker
            </span>
          </div>

          <p className="brand-tagline">
            Chennai Properties
          </p>

          <div
            className="brand-accent"
            aria-hidden="true"
          >
            <span className="accent-red" />
            <span className="accent-blue" />
            <span className="accent-red-small" />
          </div>
        </header>

        {/* =====================================================
            AUTH CARD
        ====================================================== */}

        <section
          className={`login-card ${
            authStep === "otp"
              ? "otp-card"
              : ""
          }`}
        >
          {/* Trust badge */}
          <div className="trust-badge">
            <ShieldCheck
              size={14}
              strokeWidth={2.3}
            />

            <span>
              Trusted property platform
            </span>
          </div>

          {/* ===================================================
              PHONE STEP
          ==================================================== */}

          {authStep === "phone" && (
            <div className="auth-step auth-step-phone">
              <div className="login-heading">
                <h1>
                  Welcome back!
                </h1>

                <p>
                  Enter your mobile number
                  to continue your property
                  journey
                </p>
              </div>

              <form
                className="login-form"
                onSubmit={
                  handleSendOtp
                }
              >
                {/* Mobile number */}
                <div className="field-group">
                  <label htmlFor="phone">
                    Mobile number
                  </label>

                  <div
                    className={`phone-input-wrapper ${
                      phoneNumber
                        ? "has-value"
                        : ""
                    }`}
                  >
                    <div className="country-code">
                      <span>
                        +91
                      </span>
                    </div>

                    <div className="phone-icon-wrapper">
                      <Phone
                        size={18}
                        strokeWidth={1.9}
                      />
                    </div>

                    <input
                      id="phone"
                      type="tel"
                      inputMode="numeric"
                      value={
                        phoneNumber
                      }
                      onChange={
                        handlePhoneChange
                      }
                      placeholder="98765 43210"
                      autoComplete="tel"
                      maxLength={10}
                    />
                  </div>

                  <div className="field-hint">
                    <span>
                      We'll send a
                      verification code
                      to this number.
                    </span>
                  </div>
                </div>

                {/* Terms */}
                <label className="terms-checkbox-row">
                  <input
                    type="checkbox"
                    checked={
                      acceptedTerms
                    }
                    onChange={(event) =>
                      setAcceptedTerms(
                        event.target
                          .checked
                      )
                    }
                  />

                  <span className="custom-checkbox">
                    {acceptedTerms && (
                      <Check
                        size={12}
                        strokeWidth={3}
                      />
                    )}
                  </span>

                  <span className="terms-checkbox-text">
                    I agree to the{" "}
                    <button
                      type="button"
                      onClick={(event) =>
                        event.preventDefault()
                      }
                    >
                      Terms &
                      Conditions
                    </button>{" "}
                    and{" "}
                    <button
                      type="button"
                      onClick={(event) =>
                        event.preventDefault()
                      }
                    >
                      Privacy Policy
                    </button>
                  </span>
                </label>

                {/* Error */}
                {errorMessage && (
                  <div className="error-message">
                    {
                      errorMessage
                    }
                  </div>
                )}

                {/* Send OTP */}
                <button
                  className={`primary-button ${
                    isSendingOtp
                      ? "loading"
                      : ""
                  }`}
                  type="submit"
                  disabled={
                    isSendingOtp
                  }
                >
                  {isSendingOtp ? (
                    <>
                      <LoaderCircle
                        className="button-spinner"
                        size={19}
                      />

                      <span>
                        Sending OTP...
                      </span>
                    </>
                  ) : (
                    <>
                      <span>
                        Send OTP
                      </span>

                      <ChevronRight
                        size={19}
                        strokeWidth={2.3}
                      />
                    </>
                  )}
                </button>
              </form>

              {/* Guest divider */}
              <div className="divider">
                <span />
                <p>
                  or continue as
                </p>
                <span />
              </div>

              {/* Guest */}
              <button
                className="guest-button"
                type="button"
                onClick={
                  handleGuestLogin
                }
              >
                <MapPin
                  size={18}
                  strokeWidth={1.9}
                />

                <span>
                  Continue as Guest
                </span>
              </button>

              {/* Create account */}
              <div className="account-message">
                <span>
                  New here?
                </span>

                <button
                  type="button"
                  onClick={
                    handleCreateProfile
                  }
                >
                  Create your profile
                </button>
              </div>
            </div>
          )}

          {/* ===================================================
              OTP STEP
          ==================================================== */}

          {authStep === "otp" && (
            <div className="auth-step auth-step-otp">
              <div className="otp-icon">
                <MessageCircle
                  size={24}
                  strokeWidth={1.8}
                />
              </div>

              <div className="login-heading otp-heading">
                <h1>
                  Verify your number
                </h1>

                <p>
                  Enter the 6-digit code
                  sent to
                </p>

                <div className="otp-phone">
                  +91{" "}
                  {phoneNumber}
                </div>
              </div>

              <form
                className="otp-form"
                onSubmit={
                  handleVerifyOtp
                }
              >
                {/* OTP boxes */}
                <div
                  className="otp-inputs"
                  aria-label="One time password"
                >
                  {otp.map(
                    (
                      digit,
                      index
                    ) => (
                      <input
                        key={index}
                        ref={(element) => {
                          otpRefs.current[
                            index
                          ] =
                            element;
                        }}
                        className={`otp-input ${
                          digit
                            ? "filled"
                            : ""
                        }`}
                        type="text"
                        inputMode="numeric"
                        maxLength={1}
                        value={digit}
                        aria-label={`OTP digit ${
                          index +
                          1
                        }`}
                        onChange={(
                          event
                        ) =>
                          handleOtpChange(
                            index,
                            event
                              .target
                              .value
                          )
                        }
                        onKeyDown={(
                          event
                        ) =>
                          handleOtpKeyDown(
                            index,
                            event
                          )
                        }
                        onPaste={
                          handleOtpPaste
                        }
                      />
                    )
                  )}
                </div>

                {errorMessage && (
                  <div className="error-message">
                    {
                      errorMessage
                    }
                  </div>
                )}

                {/* Verify */}
                <button
                  className={`primary-button ${
                    isVerifying
                      ? "loading"
                      : ""
                  }`}
                  type="submit"
                  disabled={
                    isVerifying
                  }
                >
                  {isVerifying ? (
                    <>
                      <LoaderCircle
                        className="button-spinner"
                        size={19}
                      />

                      <span>
                        Verifying...
                      </span>
                    </>
                  ) : (
                    <>
                      <span>
                        Verify &
                        Continue
                      </span>

                      <ChevronRight
                        size={19}
                        strokeWidth={2.3}
                      />
                    </>
                  )}
                </button>
              </form>

              {/* Resend */}
              <div className="resend-section">
                <span>
                  Didn't receive
                  the code?
                </span>

                <button
                  type="button"
                  className={
                    countdown >
                    0
                      ? "disabled"
                      : ""
                  }
                  disabled={
                    countdown >
                    0
                  }
                  onClick={
                    handleResendOtp
                  }
                >
                  {countdown >
                  0
                    ? `Resend in ${countdown}s`
                    : "Resend OTP"}
                </button>
              </div>

              {/* Change number */}
              <button
                className="change-number-button"
                type="button"
                onClick={
                  handleChangeNumber
                }
              >
                Change mobile number
              </button>
            </div>
          )}

          {/* ===================================================
              TERMS FOOTER
          ==================================================== */}

          <p className="terms-footer">
            By continuing, you agree
            to our{" "}
            <button type="button">
              Terms & Conditions
            </button>{" "}
            and{" "}
            <button type="button">
              Privacy Policy
            </button>
            .
          </p>
        </section>

        {/* Safe footer */}
        <div className="safe-footer">
          <ShieldCheck
            size={14}
            strokeWidth={1.8}
          />

          <span>
            Safe. Simple. Direct.
          </span>
        </div>
      </section>
    </main>
  );
}

export default Login;