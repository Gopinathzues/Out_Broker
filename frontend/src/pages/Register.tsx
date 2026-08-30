import {
  ArrowLeft,
  Check,
  ChevronRight,
  LoaderCircle,
  MapPin,
  ShieldCheck,
  UserRound,
  Building2,
} from "lucide-react";
import { FormEvent, useRef, useState } from "react";
import {
  useLocation,
  useNavigate,
} from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import "./Register.css";

type Role = "TENANT" | "LANDLORD";

interface RegisterLocationState {
  from?: string;
}

function Register() {
  const navigate = useNavigate();
  const location = useLocation();

  const { login } = useAuth();

  /*
   * Preserve the page the user originally wanted.
   *
   * Example:
   *
   * Home
   *   ↓
   * Add Property
   *   ↓
   * Register
   *
   * In that case, `from` will be "/add-property".
   */
  const locationState =
    location.state as RegisterLocationState | null;

  const intendedDestination =
    locationState?.from || "/home";

  const [name, setName] = useState("");
  const [phone, setPhone] = useState("");
  const [role, setRole] =
    useState<Role | null>(null);
  const [agreed, setAgreed] =
    useState(false);

  const [otpSent, setOtpSent] =
    useState(false);

  const [otp, setOtp] = useState([
    "",
    "",
    "",
    "",
    "",
    "",
  ]);

  const [loading, setLoading] =
    useState(false);

  const [otpError, setOtpError] =
    useState("");

  const [formError, setFormError] =
    useState("");

  const otpRefs =
    useRef<Array<HTMLInputElement | null>>(
      []
    );

  const phoneValid =
    phone.length === 10;

  const nameValid =
    name.trim().length >= 2;

  const canRegister =
    nameValid &&
    phoneValid &&
    role !== null &&
    agreed;

  const otpValue = otp.join("");

  /*
   * STEP 1
   *
   * Registration form → Send OTP
   *
   * Temporary UI behaviour.
   * Backend will replace this with the
   * actual registration API.
   */
  const handleRegister = (
    event: FormEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    if (loading) return;

    if (!nameValid) {
      setFormError(
        "Please enter your name."
      );
      return;
    }

    if (!phoneValid) {
      setFormError(
        "Please enter a valid 10-digit mobile number."
      );
      return;
    }

    if (!role) {
      setFormError(
        "Please select how you want to use OutBroker."
      );
      return;
    }

    if (!agreed) {
      setFormError(
        "Please agree to the Terms & Conditions and Privacy Policy."
      );
      return;
    }

    setFormError("");
    setLoading(true);

    /*
     * Temporary UI-only OTP behaviour.
     *
     * Backend team will replace this
     * with the real registration API.
     */
    setTimeout(() => {
      setLoading(false);
      setOtpSent(true);

      setOtp([
        "",
        "",
        "",
        "",
        "",
        "",
      ]);

      setTimeout(() => {
        otpRefs.current[0]?.focus();
      }, 50);
    }, 900);
  };

  /*
   * OTP input
   */
  const handleOtpChange = (
    index: number,
    value: string
  ) => {
    const digit = value
      .replace(/\D/g, "")
      .slice(-1);

    const updatedOtp = [...otp];

    updatedOtp[index] = digit;

    setOtp(updatedOtp);
    setOtpError("");

    if (
      digit &&
      index < 5
    ) {
      otpRefs.current[
        index + 1
      ]?.focus();
    }
  };

  /*
   * OTP keyboard navigation
   */
  const handleOtpKeyDown = (
    index: number,
    event: React.KeyboardEvent<HTMLInputElement>
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
  };

  /*
   * STEP 2
   *
   * Verify OTP
   *
   * OTP verification does NOT mean
   * that the landlord is fully verified.
   *
   * It only establishes that the
   * mobile number was verified.
   */
  const handleVerifyOtp = () => {
    if (loading) return;

    if (otpValue.length !== 6) {
      setOtpError(
        "Please enter the 6-digit verification code."
      );
      return;
    }

    /*
     * Temporary demo OTP.
     *
     * Backend OTP verification will
     * replace this.
     */
    if (otpValue !== "123456") {
      setOtpError(
        "Invalid OTP. Please try again."
      );
      return;
    }

    setOtpError("");
    setLoading(true);

    setTimeout(() => {
      setLoading(false);

      /*
       * Temporary authentication state.
       *
       * Backend will eventually return:
       * - real token
       * - user ID
       * - role
       * - verification status
       */
      login(
        "demo-registration-token",
        {
          name: name.trim(),
          role,
          verificationStatus:
            "OTP_VERIFIED",
        }
      );

      /*
       * PROPERTY SEEKER
       *
       * A tenant/property seeker only
       * needs mobile verification at
       * this stage, so send them to Home.
       */
      if (role === "TENANT") {
        navigate("/home", {
          replace: true,
        });

        return;
      }

      /*
       * PROPERTY OWNER
       *
       * OTP is only the first verification
       * stage.
       *
       * Continue into the owner verification
       * workflow.
       *
       * IMPORTANT:
       * Preserve the original destination.
       *
       * If the journey was:
       *
       * Home → Add Property → Register
       *
       * then Verification knows that the
       * eventual destination is Add Property.
       */
      navigate("/verification", {
        replace: true,
        state: {
          from: intendedDestination,
        },
      });
    }, 900);
  };

  /*
   * Change mobile number
   */
  const handleChangeNumber = () => {
    setOtpSent(false);

    setOtp([
      "",
      "",
      "",
      "",
      "",
      "",
    ]);

    setOtpError("");
    setFormError("");
  };

  return (
    <main className="register-page">
      {/* Decorative background */}
      <div
        className="register-background"
        aria-hidden="true"
      >
        <span className="register-orb register-orb-one" />

        <span className="register-orb register-orb-two" />

        <span className="register-orb register-orb-three" />
      </div>

      {/* Back */}
      <button
        className="register-back-button"
        type="button"
        aria-label="Go back"
        onClick={() =>
          navigate(-1)
        }
      >
        <ArrowLeft
          size={21}
          strokeWidth={2.2}
        />
      </button>

      <section className="register-shell">
        {/* Brand */}
        <header className="register-brand">
          <div className="register-brand-mark">
            <div className="register-house">
              <span className="register-roof" />

              <span className="register-house-body" />

              <span className="register-door" />
            </div>
          </div>

          <div className="register-brand-name">
            <span className="register-red">
              Out
            </span>

            <span className="register-blue">
              Broker
            </span>
          </div>

          <p className="register-tagline">
            Chennai Properties
          </p>

          <div
            className="register-accent"
            aria-hidden="true"
          >
            <span className="accent-red" />

            <span className="accent-blue" />

            <span className="accent-red-small" />
          </div>
        </header>

        {/* Main card */}
        <section className="register-card">
          <div className="register-trust-badge">
            <ShieldCheck
              size={14}
              strokeWidth={2.3}
            />

            <span>
              Trusted property platform
            </span>
          </div>

          {!otpSent ? (
            <>
              {/* Registration heading */}
              <div className="register-heading">
                <h1>
                  Create your account
                </h1>

                <p>
                  Join OutBroker to
                  discover or list
                  properties directly.
                </p>
              </div>

              <form
                className="register-form"
                onSubmit={
                  handleRegister
                }
              >
                {/* Full name */}
                <div className="register-field">
                  <label htmlFor="register-name">
                    Full name
                  </label>

                  <div
                    className={`register-input-wrapper ${
                      name
                        ? "has-value"
                        : ""
                    }`}
                  >
                    <UserRound
                      className="register-input-icon"
                      size={19}
                      strokeWidth={1.9}
                    />

                    <input
                      id="register-name"
                      type="text"
                      value={name}
                      onChange={(
                        event
                      ) => {
                        setName(
                          event.target
                            .value
                        );

                        setFormError(
                          ""
                        );
                      }}
                      placeholder="Enter your full name"
                      autoComplete="name"
                    />
                  </div>
                </div>

                {/* Mobile */}
                <div className="register-field">
                  <label htmlFor="register-phone">
                    Mobile number
                  </label>

                  <div
                    className={`register-phone-wrapper ${
                      phoneValid
                        ? "valid"
                        : ""
                    }`}
                  >
                    <div className="register-country-code">
                      +91
                    </div>

                    <div className="register-phone-divider" />

                    <input
                      id="register-phone"
                      type="tel"
                      value={phone}
                      onChange={(
                        event
                      ) => {
                        setPhone(
                          event.target.value
                            .replace(
                              /\D/g,
                              ""
                            )
                            .slice(
                              0,
                              10
                            )
                        );

                        setFormError(
                          ""
                        );
                      }}
                      placeholder="98765 43210"
                      inputMode="numeric"
                      autoComplete="tel"
                    />

                    {phoneValid && (
                      <div
                        className="register-valid-mark"
                        aria-label="Valid mobile number"
                      >
                        <Check
                          size={16}
                          strokeWidth={3}
                        />
                      </div>
                    )}
                  </div>

                  <p className="register-field-help">
                    We'll send a
                    verification code
                    to this number.
                  </p>
                </div>

                {/* Role */}
                <div className="register-field">
                  <div className="register-label-row">
                    <label>
                      How will you use
                      OutBroker?
                    </label>
                  </div>

                  <div className="role-options">
                    {/* Property seeker */}
                    <button
                      type="button"
                      className={`role-card ${
                        role ===
                        "TENANT"
                          ? "selected"
                          : ""
                      }`}
                      onClick={() => {
                        setRole(
                          "TENANT"
                        );

                        setFormError(
                          ""
                        );
                      }}
                    >
                      <div className="role-icon">
                        <MapPin
                          size={19}
                          strokeWidth={
                            1.9
                          }
                        />
                      </div>

                      <div className="role-content">
                        <strong>
                          Property
                          Seeker
                        </strong>

                        <span>
                          Find a home
                          or property
                          to rent
                        </span>
                      </div>

                      <span className="role-radio">
                        {role ===
                          "TENANT" && (
                          <span />
                        )}
                      </span>
                    </button>

                    {/* Property owner */}
                    <button
                      type="button"
                      className={`role-card ${
                        role ===
                        "LANDLORD"
                          ? "selected"
                          : ""
                      }`}
                      onClick={() => {
                        setRole(
                          "LANDLORD"
                        );

                        setFormError(
                          ""
                        );
                      }}
                    >
                      <div className="role-icon">
                        <Building2
                          size={19}
                          strokeWidth={
                            1.9
                          }
                        />
                      </div>

                      <div className="role-content">
                        <strong>
                          Property
                          Owner
                        </strong>

                        <span>
                          List and
                          manage your
                          properties
                        </span>
                      </div>

                      <span className="role-radio">
                        {role ===
                          "LANDLORD" && (
                          <span />
                        )}
                      </span>
                    </button>
                  </div>
                </div>

                {/* Terms */}
                <label
                  className={`register-terms ${
                    agreed
                      ? "checked"
                      : ""
                  }`}
                >
                  <input
                    type="checkbox"
                    checked={
                      agreed
                    }
                    onChange={(
                      event
                    ) => {
                      setAgreed(
                        event.target
                          .checked
                      );

                      setFormError(
                        ""
                      );
                    }}
                  />

                  <span className="register-checkbox">
                    {agreed && (
                      <Check
                        size={12}
                        strokeWidth={
                          3
                        }
                      />
                    )}
                  </span>

                  <span className="register-terms-text">
                    I agree to the{" "}
                    <button
                      type="button"
                      onClick={(
                        event
                      ) =>
                        event.stopPropagation()
                      }
                    >
                      Terms &
                      Conditions
                    </button>{" "}
                    and{" "}
                    <button
                      type="button"
                      onClick={(
                        event
                      ) =>
                        event.stopPropagation()
                      }
                    >
                      Privacy Policy
                    </button>
                  </span>
                </label>

                {/* Form error */}
                {formError && (
                  <p className="register-error">
                    {formError}
                  </p>
                )}

                {/* Send OTP */}
                <button
                  className={`register-primary-button ${
                    loading
                      ? "loading"
                      : ""
                  }`}
                  type="submit"
                  disabled={
                    loading ||
                    !canRegister
                  }
                >
                  {loading ? (
                    <>
                      <LoaderCircle
                        className="register-spinner"
                        size={20}
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
                        size={21}
                        strokeWidth={
                          2.4
                        }
                      />
                    </>
                  )}
                </button>
              </form>

              {/* Login */}
              <div className="register-login-link">
                <span>
                  Already have an
                  account?
                </span>

                <button
                  type="button"
                  onClick={() =>
                    navigate(
                      "/login"
                    )
                  }
                >
                  Login
                </button>
              </div>
            </>
          ) : (
            <>
              {/* OTP screen */}
              <div className="register-heading otp-heading">
                <h1>
                  Verify your
                  number
                </h1>

                <p>
                  Enter the 6-digit
                  code sent to
                </p>

                <div className="otp-phone">
                  +91 {phone}
                </div>
              </div>

              <div className="otp-section">
                <label className="otp-label">
                  Verification code
                </label>

                <div className="otp-inputs">
                  {otp.map(
                    (
                      digit,
                      index
                    ) => (
                      <input
                        key={
                          index
                        }
                        ref={(
                          element
                        ) => {
                          otpRefs.current[
                            index
                          ] =
                            element;
                        }}
                        className="otp-input"
                        type="text"
                        inputMode="numeric"
                        maxLength={1}
                        value={digit}
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
                        aria-label={`OTP digit ${
                          index + 1
                        }`}
                      />
                    )
                  )}
                </div>

                {otpError && (
                  <p className="register-error otp-error">
                    {otpError}
                  </p>
                )}

                {/* Verify */}
                <button
                  className={`register-primary-button otp-button ${
                    loading
                      ? "loading"
                      : ""
                  }`}
                  type="button"
                  onClick={
                    handleVerifyOtp
                  }
                  disabled={
                    loading ||
                    otpValue.length !==
                      6
                  }
                >
                  {loading ? (
                    <>
                      <LoaderCircle
                        className="register-spinner"
                        size={20}
                      />

                      <span>
                        Verifying...
                      </span>
                    </>
                  ) : (
                    <>
                      <span>
                        Verify &amp;
                        Continue
                      </span>

                      <ChevronRight
                        size={21}
                        strokeWidth={
                          2.4
                        }
                      />
                    </>
                  )}
                </button>

                {/* Resend */}
                <div className="otp-actions">
                  <span>
                    Didn't receive
                    the code?
                  </span>

                  <button
                    type="button"
                    onClick={() => {
                      setOtp([
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                      ]);

                      setOtpError(
                        ""
                      );

                      otpRefs.current[
                        0
                      ]?.focus();
                    }}
                  >
                    Resend OTP
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
                  Change mobile
                  number
                </button>

                {/* Demo */}
                <div className="demo-otp-note">
                  <ShieldCheck
                    size={14}
                    strokeWidth={
                      1.8
                    }
                  />

                  <span>
                    Demo OTP:{" "}
                    <strong>
                      123456
                    </strong>
                  </span>
                </div>
              </div>
            </>
          )}
        </section>

        {/* Footer */}
        <div className="register-safe-footer">
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

export default Register;