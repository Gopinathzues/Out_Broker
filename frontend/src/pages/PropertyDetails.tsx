import { useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  ArrowLeft,
  ArrowUpDown,
  BedDouble,
  Bookmark,
  Check,
  ChevronLeft,
  ChevronRight,
  Heart,
  MapPin,
  MessageCircle,
  Phone,
  Ruler,
  ShieldCheck,
  Share2,
  Sofa,
  Users,
} from "lucide-react";
import "./PropertyDetails.css";

type VerificationLevel = "fully" | "document" | "basic";

interface Property {
  id: number;
  title: string;
  category: "House" | "Office Room" | "Shop" | "Hostel/PG";
  type: "Rent" | "Sale";
  area: string;
  address: string;
  price: string;
  priceValue: number;
  bhk?: string;
  sqft?: string;
  floor?: string;
  furnish?: string;
  tenant?: string;
  amenities: string[];
  verification: VerificationLevel;
  description: string;
  ownerName: string;
  ownerSince: string;
  photos: string[];
}

const PROPERTIES: Property[] = [
  {
    id: 1,
    title: "3BHK Premium Flat",
    category: "House",
    type: "Rent",
    area: "Adyar",
    address: "42, Gandhi Nagar, Adyar, Chennai – 600020",
    price: "₹35,000",
    priceValue: 35000,
    bhk: "3 BHK",
    sqft: "1450 sq.ft",
    floor: "4th Floor",
    furnish: "Semi-furnished",
    tenant: "Family / Professionals",
    amenities: [
      "Covered Parking",
      "24×7 Water",
      "Power Backup",
      "Lift",
      "Security",
      "Balcony",
    ],
    verification: "fully",
    description:
      "Spacious 3BHK home in Gandhi Nagar, Adyar with good connectivity to major roads, schools, offices and daily essentials.",
    ownerName: "Verified Property Owner",
    ownerSince: "Member since 2024",
    photos: [
      "https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?auto=format&fit=crop&w=1200&q=85",
      "https://images.unsplash.com/photo-1600210492486-724fe5c67fb0?auto=format&fit=crop&w=1200&q=85",
      "https://images.unsplash.com/photo-1600566753086-00f18fb6b3ea?auto=format&fit=crop&w=1200&q=85",
      "https://images.unsplash.com/photo-1600607687920-4e2a09cf159d?auto=format&fit=crop&w=1200&q=85",
    ],
  },
  {
    id: 2,
    title: "2BHK Modern Apartment",
    category: "House",
    type: "Rent",
    area: "Velachery",
    address: "100 Feet Road, Velachery, Chennai – 600042",
    price: "₹22,000",
    priceValue: 22000,
    bhk: "2 BHK",
    sqft: "1100 sq.ft",
    floor: "3rd Floor",
    furnish: "Fully furnished",
    tenant: "Family / Professionals",
    amenities: [
      "Parking",
      "Lift",
      "Security",
      "Water Supply",
      "Balcony",
    ],
    verification: "fully",
    description:
      "Well-maintained 2BHK apartment in a convenient residential area of Velachery.",
    ownerName: "Verified Property Owner",
    ownerSince: "Member since 2024",
    photos: [
      "https://images.unsplash.com/photo-1600607688969-a5bfcd646154?auto=format&fit=crop&w=1200&q=85",
      "https://images.unsplash.com/photo-1600607687920-4e2a09cf159d?auto=format&fit=crop&w=1200&q=85",
      "https://images.unsplash.com/photo-1600566753190-17f0baa2a6c3?auto=format&fit=crop&w=1200&q=85",
    ],
  },
  {
    id: 3,
    title: "Premium Office Space",
    category: "Office Room",
    type: "Rent",
    area: "Anna Nagar",
    address: "2nd Avenue, Anna Nagar, Chennai – 600040",
    price: "₹55,000",
    priceValue: 55000,
    sqft: "1800 sq.ft",
    floor: "2nd Floor",
    furnish: "Furnished",
    tenant: "Business / Startup",
    amenities: [
      "Power Backup",
      "Lift",
      "Parking",
      "Reception Area",
      "Security",
    ],
    verification: "document",
    description:
      "Professional office space suitable for startups, agencies and small businesses.",
    ownerName: "Property Owner",
    ownerSince: "Member since 2025",
    photos: [
      "https://images.unsplash.com/photo-1497366811353-6870744d04b2?auto=format&fit=crop&w=1200&q=85",
      "https://images.unsplash.com/photo-1497366754035-f200968a6e72?auto=format&fit=crop&w=1200&q=85",
      "https://images.unsplash.com/photo-1497366216548-37526070297c?auto=format&fit=crop&w=1200&q=85",
    ],
  },
  {
    id: 4,
    title: "Commercial Shop Space",
    category: "Shop",
    type: "Rent",
    area: "T. Nagar",
    address: "North Usman Road, T. Nagar, Chennai – 600017",
    price: "₹45,000",
    priceValue: 45000,
    sqft: "900 sq.ft",
    floor: "Ground Floor",
    furnish: "Unfurnished",
    tenant: "Commercial",
    amenities: [
      "Road Facing",
      "Parking",
      "Power Backup",
      "Water Supply",
    ],
    verification: "fully",
    description:
      "Road-facing commercial space with excellent visibility and accessibility.",
    ownerName: "Verified Property Owner",
    ownerSince: "Member since 2023",
    photos: [
      "https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?auto=format&fit=crop&w=1200&q=85",
      "https://images.unsplash.com/photo-1555529669-e69e7aa0ba9a?auto=format&fit=crop&w=1200&q=85",
    ],
  },
  {
    id: 5,
    title: "Comfortable Boys Hostel",
    category: "Hostel/PG",
    type: "Rent",
    area: "OMR",
    address: "Sholinganallur, OMR, Chennai – 600119",
    price: "₹5,500",
    priceValue: 5500,
    sqft: "Shared accommodation",
    floor: "2nd Floor",
    furnish: "Fully furnished",
    tenant: "Students / Professionals",
    amenities: [
      "Food Available",
      "Wi-Fi",
      "Laundry",
      "24×7 Water",
      "Security",
    ],
    verification: "fully",
    description:
      "Clean and convenient hostel accommodation close to major IT offices along OMR.",
    ownerName: "Verified Property Owner",
    ownerSince: "Member since 2024",
    photos: [
      "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?auto=format&fit=crop&w=1200&q=85",
      "https://images.unsplash.com/photo-1560185008-b033106af5c3?auto=format&fit=crop&w=1200&q=85",
    ],
  },
  {
    id: 6,
    title: "2BHK Family Home",
    category: "House",
    type: "Rent",
    area: "ECR",
    address: "ECR Main Road, Chennai – 600041",
    price: "₹28,000",
    priceValue: 28000,
    bhk: "2 BHK",
    sqft: "1250 sq.ft",
    floor: "1st Floor",
    furnish: "Semi-furnished",
    tenant: "Family",
    amenities: [
      "Parking",
      "Garden",
      "Water Supply",
      "Security",
      "Balcony",
    ],
    verification: "basic",
    description:
      "Quiet residential property with convenient access to ECR and surrounding neighbourhoods.",
    ownerName: "Property Owner",
    ownerSince: "Member since 2025",
    photos: [
      "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=1200&q=85",
      "https://images.unsplash.com/photo-1600047509807-ba8f99d2cdde?auto=format&fit=crop&w=1200&q=85",
    ],
  },
];

const FALLBACK_PROPERTY = PROPERTIES[0];

function getVerification(level: VerificationLevel) {
  switch (level) {
    case "fully":
      return {
        label: "Fully Verified",
        description: "Owner and property verification completed",
        className: "verification-full",
      };

    case "document":
      return {
        label: "Document Verified",
        description: "Owner documents verified",
        className: "verification-document",
      };

    default:
      return {
        label: "Basic Verified",
        description: "Mobile number verified",
        className: "verification-basic",
      };
  }
}

function PropertyDetails() {
  const { id } = useParams();
  const navigate = useNavigate();

  const property = useMemo(() => {
    const propertyId = Number(id);
    return (
      PROPERTIES.find((item) => item.id === propertyId) ??
      FALLBACK_PROPERTY
    );
  }, [id]);

  const [activePhoto, setActivePhoto] = useState(0);
  const [isSaved, setIsSaved] = useState(() => {
    const saved = localStorage.getItem("outbroker_favorites");

    if (!saved) return false;

    try {
      const favorites: number[] = JSON.parse(saved);
      return favorites.includes(property.id);
    } catch {
      return false;
    }
  });

  const [showAllAmenities, setShowAllAmenities] = useState(false);
  const [showContactMessage, setShowContactMessage] = useState(false);

  const verification = getVerification(property.verification);

  const visibleAmenities = showAllAmenities
    ? property.amenities
    : property.amenities.slice(0, 4);

  const updateSavedState = () => {
    const stored = localStorage.getItem("outbroker_favorites");

    let favorites: number[] = [];

    if (stored) {
      try {
        favorites = JSON.parse(stored);
      } catch {
        favorites = [];
      }
    }

    if (favorites.includes(property.id)) {
      favorites = favorites.filter((item) => item !== property.id);
    } else {
      favorites.push(property.id);
    }

    localStorage.setItem(
      "outbroker_favorites",
      JSON.stringify(favorites)
    );

    setIsSaved(favorites.includes(property.id));
  };

  const previousPhoto = () => {
    setActivePhoto((current) =>
      current === 0 ? property.photos.length - 1 : current - 1
    );
  };

  const nextPhoto = () => {
    setActivePhoto((current) =>
      current === property.photos.length - 1 ? 0 : current + 1
    );
  };

  const handleContact = () => {
    setShowContactMessage(true);
  };

  return (
    <main className="property-details-page">
      {/* Header */}
      <header className="details-header">
        <button
          className="details-icon-button"
          type="button"
          aria-label="Go back"
          onClick={() => navigate(-1)}
        >
          <ArrowLeft size={21} strokeWidth={2.2} />
        </button>

        <div className="details-header-title">
          <span>Property Details</span>
          <small>{property.area}, Chennai</small>
        </div>

        <div className="details-header-actions">
          <button
            className="details-icon-button"
            type="button"
            aria-label="Share property"
            onClick={() => {
              if (navigator.share) {
                navigator
                  .share({
                    title: property.title,
                    text: `${property.title} in ${property.area}`,
                  })
                  .catch(() => undefined);
              }
            }}
          >
            <Share2 size={19} strokeWidth={2} />
          </button>

          <button
            className={`details-icon-button ${
              isSaved ? "saved-active" : ""
            }`}
            type="button"
            aria-label={
              isSaved ? "Remove from saved" : "Save property"
            }
            onClick={updateSavedState}
          >
            {isSaved ? (
              <Heart
                size={19}
                strokeWidth={2.1}
                fill="currentColor"
              />
            ) : (
              <Bookmark size={19} strokeWidth={2} />
            )}
          </button>
        </div>
      </header>

      <div className="details-content">
        {/* Photo gallery */}
        <section className="photo-gallery">
          <div className="main-photo-container">
            <img
              src={property.photos[activePhoto]}
              alt={`${property.title} photo ${activePhoto + 1}`}
              className="main-property-photo"
            />

            <div className="photo-overlay" />

            <div className="verification-photo-badge">
              <ShieldCheck size={15} strokeWidth={2.4} />
              <span>{verification.label}</span>
            </div>

            <div className="photo-counter">
              {activePhoto + 1} / {property.photos.length}
            </div>

            {property.photos.length > 1 && (
              <>
                <button
                  className="gallery-arrow gallery-arrow-left"
                  type="button"
                  aria-label="Previous photo"
                  onClick={previousPhoto}
                >
                  <ChevronLeft size={22} />
                </button>

                <button
                  className="gallery-arrow gallery-arrow-right"
                  type="button"
                  aria-label="Next photo"
                  onClick={nextPhoto}
                >
                  <ChevronRight size={22} />
                </button>
              </>
            )}
          </div>

          {property.photos.length > 1 && (
            <div className="photo-thumbnails">
              {property.photos.map((photo, index) => (
                <button
                  key={photo}
                  type="button"
                  className={`photo-thumbnail ${
                    activePhoto === index ? "active" : ""
                  }`}
                  onClick={() => setActivePhoto(index)}
                  aria-label={`View photo ${index + 1}`}
                >
                  <img
                    src={photo}
                    alt=""
                    aria-hidden="true"
                  />
                </button>
              ))}
            </div>
          )}
        </section>

        {/* Main property information */}
        <section className="property-intro">
          <div className="property-type-row">
            <span className="property-category">
              {property.category}
            </span>

            <span className="property-listing-type">
              For {property.type}
            </span>
          </div>

          <h1>{property.title}</h1>

          <div className="property-location">
            <MapPin size={17} strokeWidth={2} />
            <span>
              {property.area}, Chennai
            </span>
          </div>

          <div className="price-row">
            <div>
              <strong>{property.price}</strong>
              <span>/month</span>
            </div>

            <button
              type="button"
              className={`save-inline-button ${
                isSaved ? "saved" : ""
              }`}
              onClick={updateSavedState}
            >
              <Heart
                size={17}
                fill={isSaved ? "currentColor" : "none"}
              />
              {isSaved ? "Saved" : "Save"}
            </button>
          </div>
        </section>

        {/* Verification */}
        <section
          className={`verification-card ${verification.className}`}
        >
          <div className="verification-icon">
            <ShieldCheck size={22} strokeWidth={2.2} />
          </div>

          <div className="verification-copy">
            <strong>{verification.label}</strong>
            <span>{verification.description}</span>
          </div>

          <Check size={20} strokeWidth={2.5} />
        </section>

        {/* Specifications */}
        <section className="details-section">
          <div className="section-heading">
            <div>
              <span className="section-eyebrow">
                PROPERTY INFORMATION
              </span>
              <h2>Specifications</h2>
            </div>
          </div>

          <div className="spec-grid">
            {property.bhk && (
              <div className="spec-item">
                <span className="spec-icon"><BedDouble size={17} strokeWidth={2.1} /></span>
                <div>
                  <small>Bedrooms</small>
                  <strong>{property.bhk}</strong>
                </div>
              </div>
            )}

            {property.sqft && (
              <div className="spec-item">
                <span className="spec-icon"><Ruler size={17} strokeWidth={2.1} /></span>
                <div>
                  <small>Area</small>
                  <strong>{property.sqft}</strong>
                </div>
              </div>
            )}

            {property.floor && (
              <div className="spec-item">
                <span className="spec-icon"><ArrowUpDown size={17} strokeWidth={2.1} /></span>
                <div>
                  <small>Floor</small>
                  <strong>{property.floor}</strong>
                </div>
              </div>
            )}

            {property.furnish && (
              <div className="spec-item">
                <span className="spec-icon"><Sofa size={17} strokeWidth={2.1} /></span>
                <div>
                  <small>Furnishing</small>
                  <strong>{property.furnish}</strong>
                </div>
              </div>
            )}

            {property.tenant && (
              <div className="spec-item spec-item-wide">
                <span className="spec-icon"><Users size={17} strokeWidth={2.1} /></span>
                <div>
                  <small>Suitable for</small>
                  <strong>{property.tenant}</strong>
                </div>
              </div>
            )}
          </div>
        </section>

        {/* Amenities */}
        <section className="details-section">
          <div className="section-heading">
            <div>
              <span className="section-eyebrow">FEATURES</span>
              <h2>Amenities</h2>
            </div>
          </div>

          <div className="amenities-grid">
            {visibleAmenities.map((amenity) => (
              <div className="amenity-chip" key={amenity}>
                <Check size={14} strokeWidth={2.8} />
                <span>{amenity}</span>
              </div>
            ))}
          </div>

          {property.amenities.length > 4 && (
            <button
              className="show-more-button"
              type="button"
              onClick={() =>
                setShowAllAmenities((value) => !value)
              }
            >
              {showAllAmenities
                ? "Show less"
                : `Show all ${property.amenities.length} amenities`}
            </button>
          )}
        </section>

        {/* Location */}
        <section className="details-section">
          <div className="section-heading">
            <div>
              <span className="section-eyebrow">LOCATION</span>
              <h2>Property location</h2>
            </div>
          </div>

          <div className="location-card">
            <div className="location-map-placeholder">
              <div className="map-grid" />

              <div className="map-pin">
                <MapPin size={25} fill="currentColor" />
              </div>

              <div className="map-location-label">
                <strong>{property.area}</strong>
                <span>Chennai</span>
              </div>
            </div>

            <div className="location-address">
              <MapPin size={18} />
              <span>{property.address}</span>
            </div>
          </div>
        </section>

        {/* Description */}
        <section className="details-section">
          <div className="section-heading">
            <div>
              <span className="section-eyebrow">ABOUT</span>
              <h2>About this property</h2>
            </div>
          </div>

          <p className="property-description">
            {property.description}
          </p>
        </section>

        {/* Owner */}
        <section className="details-section owner-section">
          <div className="section-heading">
            <div>
              <span className="section-eyebrow">LISTED BY</span>
              <h2>Property owner</h2>
            </div>
          </div>

          <div className="owner-card">
            <div className="owner-avatar">
              {property.ownerName.charAt(0)}
            </div>

            <div className="owner-info">
              <div className="owner-name-row">
                <strong>{property.ownerName}</strong>

                {property.verification === "fully" && (
                  <ShieldCheck
                    size={16}
                    className="owner-verified-icon"
                  />
                )}
              </div>

              <span>{property.ownerSince}</span>
            </div>

            <div className="owner-safe">
              <ShieldCheck size={16} />
              <span>Verified</span>
            </div>
          </div>
        </section>

        {/* Bottom spacing for sticky CTA */}
        <div className="details-bottom-space" />
      </div>

      {/* Contact CTA */}
      <div className="contact-bar">
        <button
          className="contact-secondary"
          type="button"
          onClick={() => setShowContactMessage(true)}
        >
          <MessageCircle size={20} strokeWidth={2} />
          <span>Chat</span>
        </button>

        <button
          className="contact-primary"
          type="button"
          onClick={handleContact}
        >
          <Phone size={19} strokeWidth={2.2} />
          <span>Contact Owner</span>
        </button>
      </div>

      {/* Temporary contact feedback */}
      {showContactMessage && (
        <div className="contact-toast">
          <div className="contact-toast-icon">
            <MessageCircle size={18} />
          </div>

          <div>
            <strong>Contact feature ready</strong>
            <span>
              Owner contact/chat will be connected to the backend.
            </span>
          </div>

          <button
            type="button"
            onClick={() => setShowContactMessage(false)}
            aria-label="Close message"
          >
            ×
          </button>
        </div>
      )}
    </main>
  );
}

export default PropertyDetails;