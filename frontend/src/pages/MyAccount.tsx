import {
  ArrowLeft,
  BadgeCheck,
  Heart,
  Home as HomeIcon,
  LogOut,
  Mail,
  MapPin,
  Phone,
  ShieldCheck,
  UserRound,
} from "lucide-react";

import { useNavigate } from "react-router-dom";

import { useAuth } from "../context/AuthContext";

import "./MyAccount.css";

/* ============================================================
   HELPERS
============================================================ */

function formatRole(role?: string) {
  if (!role) return "Guest";

  if (role === "TENANT") return "Tenant";
  if (role === "LANDLORD") return "Property Owner";

  return role;
}

function formatVerification(status?: string) {
  if (!status) return "Not verified";

  const normalized = status.toUpperCase();

  if (normalized === "FULLY_VERIFIED") return "Fully verified";
  if (normalized === "DOCUMENT_VERIFIED") return "Document verified";
  if (normalized === "OTP_VERIFIED") return "Phone verified";
  if (normalized === "PENDING") return "Verification pending";
  if (normalized === "REJECTED") return "Verification rejected";

  return status;
}

function getInitials(name?: string, phone?: string) {
  if (name && name.trim().length > 0) {
    return name
      .trim()
      .split(/\s+/)
      .slice(0, 2)
      .map((part) => part[0]?.toUpperCase())
      .join("");
  }

  if (phone) {
    return phone.slice(-2);
  }

  return "OB";
}

/* ============================================================
   MY ACCOUNT
============================================================ */

function MyAccount() {
  const navigate = useNavigate();

  const { user, logout } = useAuth();

  const isFullyVerified =
    user?.verificationStatus?.toUpperCase() === "FULLY_VERIFIED";

  /* ==========================================================
     LOGOUT
  ========================================================== */

  const handleLogout = () => {
    logout();

    navigate("/login", {
      replace: true,
    });
  };

  return (
    <main className="account-page">
      {/* =====================================================
          HEADER
      ====================================================== */}

      <header className="account-header">
        <button
          type="button"
          className="account-back-button"
          onClick={() => navigate("/home")}
          aria-label="Back to home"
        >
          <ArrowLeft size={20} />
        </button>

        <div className="account-header-title">
          <span>ACCOUNT</span>

          <h1>My Account</h1>
        </div>
      </header>

      {/* =====================================================
          CONTENT
      ====================================================== */}

      <section className="account-content">
        {/* =================================================
            PROFILE CARD
        ================================================== */}

        <div className="account-profile-card">
          <div className="account-avatar">
            {getInitials(user?.name, user?.phone)}
          </div>

          <div className="account-profile-info">
            <h2>{user?.name && user.name.trim().length > 0 ? user.name : "Your Profile"}</h2>

            <span className="account-role-chip">
              <UserRound size={13} strokeWidth={2.4} />
              {formatRole(user?.role)}
            </span>
          </div>

          <span
            className={`account-verified-chip ${
              isFullyVerified ? "verified" : "pending"
            }`}
          >
            <ShieldCheck size={13} strokeWidth={2.4} />
            {formatVerification(user?.verificationStatus)}
          </span>
        </div>

        {/* =================================================
            ACCOUNT DETAILS
        ================================================== */}

        <div className="account-details-card">
          <div className="account-details-heading">
            <span className="section-kicker">ACCOUNT DETAILS</span>
          </div>

          <div className="account-detail-row">
            <span className="account-detail-icon">
              <Phone size={17} strokeWidth={2} />
            </span>

            <div className="account-detail-text">
              <small>Mobile number</small>

              <strong>
                {user?.phone ? `+91 ${user.phone}` : "Not available"}
              </strong>
            </div>
          </div>

          <div className="account-detail-row">
            <span className="account-detail-icon">
              <Mail size={17} strokeWidth={2} />
            </span>

            <div className="account-detail-text">
              <small>Email address</small>

              <strong>{user?.email || "Not added yet"}</strong>
            </div>
          </div>

          <div className="account-detail-row">
            <span className="account-detail-icon">
              <BadgeCheck size={17} strokeWidth={2} />
            </span>

            <div className="account-detail-text">
              <small>Account type</small>

              <strong>{formatRole(user?.role)}</strong>
            </div>
          </div>
        </div>

        {/* =================================================
            ACCOUNT ACTIONS
        ================================================== */}

        <div className="account-actions-card">
          {user?.role === "LANDLORD" && (
            <button
              type="button"
              className="account-action-row"
              onClick={() => navigate("/verification")}
            >
              <span className="account-detail-icon">
                <ShieldCheck size={17} strokeWidth={2} />
              </span>

              <span className="account-action-label">
                Verification status
              </span>

              <span className="account-action-chevron">›</span>
            </button>
          )}

          <button
            type="button"
            className="account-action-row"
            onClick={() => navigate("/saved")}
          >
            <span className="account-detail-icon">
              <Heart size={17} strokeWidth={2} />
            </span>

            <span className="account-action-label">
              Saved properties
            </span>

            <span className="account-action-chevron">›</span>
          </button>
        </div>

        {/* =================================================
            LOGOUT
        ================================================== */}

        <button
          type="button"
          className="account-logout-button"
          onClick={handleLogout}
        >
          <LogOut size={18} strokeWidth={2.2} />
          <span>Log out</span>
        </button>
      </section>

      {/* =====================================================
          BOTTOM NAVIGATION
      ====================================================== */}

      <nav
        className="account-bottom-navigation"
        aria-label="Main navigation"
      >
        <button type="button" onClick={() => navigate("/home")}>
          <HomeIcon size={20} />
          <span>Home</span>
        </button>

        <button type="button" onClick={() => navigate("/map")}>
          <MapPin size={20} />
          <span>Map</span>
        </button>

        <button type="button" onClick={() => navigate("/saved")}>
          <Heart size={20} />
          <span>Saved</span>
        </button>

        <button type="button" className="active">
          <UserRound size={20} />
          <span>Profile</span>
        </button>
      </nav>
    </main>
  );
}

export default MyAccount;
