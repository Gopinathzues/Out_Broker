import {
  ArrowLeft,
  Heart,
  Home as HomeIcon,
  MapPin,
  UserRound,
  X,
} from "lucide-react";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import { useAuth } from "../context/AuthContext";

import {
  getSavedProperties,
  toggleSavedProperty,
} from "../data/propertyStore";

import "./Saved.css";

function Saved() {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  /*
   * Use the SAME propertyStore used by Home.
   *
   * This is important because we don't want another
   * independent favorites system.
   */
  const [savedProperties, setSavedProperties] =
    useState(() => getSavedProperties());

  /*
   * Reload saved properties whenever this page is opened.
   */
  useEffect(() => {
    setSavedProperties(getSavedProperties());
  }, []);

  /*
   * Remove a property from Saved.
   *
   * We intentionally use toggleSavedProperty()
   * instead of directly modifying localStorage.
   */
  const handleRemove = (
    property: (typeof savedProperties)[number],
  ) => {
    const updated = toggleSavedProperty(property);

    setSavedProperties(updated);
  };

  /*
   * Open the existing Property Details page.
   */
  const handlePropertyClick = (id: number | string) => {
    navigate(`/properties/${id}`);
  };

  return (
    <main className="saved-page">
      {/* =====================================================
          HEADER
      ====================================================== */}

      <header className="saved-header">
        <button
          type="button"
          className="saved-back-button"
          onClick={() => navigate("/home")}
          aria-label="Back to home"
        >
          <ArrowLeft size={20} />
        </button>

        <div className="saved-header-title">
          <span>SAVED</span>

          <h1>Saved Properties</h1>

          <p>
            {savedProperties.length}{" "}
            {savedProperties.length === 1
              ? "property"
              : "properties"}{" "}
            saved
          </p>
        </div>
      </header>

      {/* =====================================================
          CONTENT
      ====================================================== */}

      <section className="saved-content">
        {savedProperties.length === 0 ? (
          /* =================================================
             EMPTY STATE
          ================================================== */

          <div className="saved-empty-state">
            <div className="saved-empty-icon">
              <Heart size={30} />
            </div>

            <h2>No saved properties yet</h2>

            <p>
              Properties you save will appear here.
              Tap the heart on any property to keep
              it for later.
            </p>

            <button
              type="button"
              className="saved-browse-button"
              onClick={() => navigate("/home")}
            >
              Browse properties
            </button>
          </div>
        ) : (
          /* =================================================
             SAVED PROPERTY LIST
          ================================================== */

          <div className="saved-list">
            {savedProperties.map((property) => (
              <article
                className="saved-property-card"
                key={property.id}
              >
                {/* Property image */}

                <button
                  type="button"
                  className="saved-property-image-button"
                  onClick={() =>
                    handlePropertyClick(
                      property.id,
                    )
                  }
                  aria-label={`View ${property.title}`}
                >
                  <img
                    src={property.image}
                    alt={property.title}
                    className="saved-property-image"
                  />

                  <span className="saved-category">
                    {property.category}
                  </span>

                  {property.verified && (
                    <span className="saved-verified">
                      ✓ Verified
                    </span>
                  )}
                </button>

                {/* Property content */}

                <div className="saved-property-content">
                  <div className="saved-property-top">
                    <button
                      type="button"
                      className="saved-property-title"
                      onClick={() =>
                        handlePropertyClick(
                          property.id,
                        )
                      }
                    >
                      <h2>
                        {property.title}
                      </h2>
                    </button>

                    <button
                      type="button"
                      className="saved-remove-button"
                      onClick={() =>
                        handleRemove(property)
                      }
                      aria-label={`Remove ${property.title} from saved properties`}
                    >
                      <Heart
                        size={19}
                        fill="currentColor"
                      />
                    </button>
                  </div>

                  {/* Location */}

                  <div className="saved-location">
                    <MapPin size={14} />

                    <span>
                      {property.location}
                    </span>
                  </div>

                  {/* Property specifications */}

                  <div className="saved-specs">
                    {property.bhk && (
                      <span>
                        {property.bhk}
                      </span>
                    )}

                    {property.area && (
                      <span>
                        {property.area}
                      </span>
                    )}

                    {property.furnishing && (
                      <span>
                        {property.furnishing}
                      </span>
                    )}
                  </div>

                  {/* Footer */}

                  <div className="saved-property-footer">
                    <strong>
                      {property.priceLabel}
                    </strong>

                    <button
                      type="button"
                      onClick={() =>
                        handlePropertyClick(
                          property.id,
                        )
                      }
                    >
                      View property
                    </button>
                  </div>
                </div>

                {/* Remove shortcut */}

                <button
                  type="button"
                  className="saved-card-close"
                  onClick={() =>
                    handleRemove(property)
                  }
                  aria-label="Remove saved property"
                >
                  <X size={15} />
                </button>
              </article>
            ))}
          </div>
        )}
      </section>

      {/* =====================================================
          BOTTOM NAVIGATION
      ====================================================== */}

      <nav
        className="saved-bottom-navigation"
        aria-label="Main navigation"
      >
        <button
          type="button"
          onClick={() => navigate("/home")}
        >
          <HomeIcon size={20} />
          <span>Home</span>
        </button>

        <button
          type="button"
          onClick={() => navigate("/map")}
        >
          <MapPin size={20} />
          <span>Map</span>
        </button>

        <button
          type="button"
          className="active"
        >
          <Heart
            size={20}
            fill="currentColor"
          />
          <span>Saved</span>
        </button>

        <button
          type="button"
          onClick={() =>
            navigate(isAuthenticated ? "/account" : "/login")
          }
        >
          <UserRound size={20} />
          <span>Profile</span>
        </button>
      </nav>
    </main>
  );
}

export default Saved;