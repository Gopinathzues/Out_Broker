import {
  ArrowLeft,
  Crosshair,
  Heart,
  Home,
  MapPin,
  Search,
  SlidersHorizontal,
  UserRound,
  X,
} from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";

import { useAuth } from "../context/AuthContext";

import {
  getSavedProperties,
  toggleSavedProperty,
} from "../data/propertyStore";

import "./Map.css";

type Property = {
  id: number;
  title: string;
  category: "House" | "Office Room" | "Shop" | "Hostel/PG";
  location: string;
  priceLabel: string;
  image: string;
  lat: number;
  lng: number;
  verified: boolean;
};

const PROPERTIES: Property[] = [
  {
    id: 1,
    title: "3 BHK Premium Flat",
    category: "House",
    location: "Adyar, Chennai",
    priceLabel: "₹35,000 / month",
    image:
      "https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?auto=format&fit=crop&w=900&q=80",
    lat: 13.0068,
    lng: 80.257,
    verified: true,
  },
  {
    id: 2,
    title: "Modern 2 BHK Apartment",
    category: "House",
    location: "Velachery, Chennai",
    priceLabel: "₹24,000 / month",
    image:
      "https://images.unsplash.com/photo-1600566753190-17f0baa2a6c3?auto=format&fit=crop&w=900&q=80",
    lat: 12.975,
    lng: 80.221,
    verified: true,
  },
  {
    id: 3,
    title: "Premium Office Space",
    category: "Office Room",
    location: "Guindy, Chennai",
    priceLabel: "₹28,000 / month",
    image:
      "https://images.unsplash.com/photo-1497366754035-f200968a6e72?auto=format&fit=crop&w=900&q=80",
    lat: 13.0067,
    lng: 80.2206,
    verified: true,
  },
  {
    id: 4,
    title: "Roadside Commercial Shop",
    category: "Shop",
    location: "T. Nagar, Chennai",
    priceLabel: "₹45,000 / month",
    image:
      "https://images.unsplash.com/photo-1556761175-b413da4baf72?auto=format&fit=crop&w=900&q=80",
    lat: 13.0418,
    lng: 80.2341,
    verified: true,
  },
  {
    id: 5,
    title: "Comfortable PG for Students",
    category: "Hostel/PG",
    location: "Tambaram, Chennai",
    priceLabel: "₹8,500 / month",
    image:
      "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?auto=format&fit=crop&w=900&q=80",
    lat: 12.9249,
    lng: 80.100,
    verified: true,
  },
];

const categories = [
  "All",
  "House",
  "Office Room",
  "Shop",
  "Hostel/PG",
] as const;

function Map() {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  const [activeCategory, setActiveCategory] =
    useState<(typeof categories)[number]>("All");

  const [search, setSearch] = useState("");
  const [selectedProperty, setSelectedProperty] =
    useState<Property | null>(null);

  const [locationLoading, setLocationLoading] =
    useState(false);

  /*
   * Connected to the shared propertyStore so a property
   * saved from Home, Map or Saved always shows the same
   * saved state everywhere.
   */
  const [savedIds, setSavedIds] = useState<string[]>([]);

  useEffect(() => {
    const saved = getSavedProperties();
    setSavedIds(saved.map((property) => String(property.id)));
  }, []);

  const toggleFavorite = (property: Property) => {
    /*
     * Guests can browse the map freely, but saving a
     * property requires an account.
     */
    if (!isAuthenticated) {
      navigate("/login");
      return;
    }

    const updated = toggleSavedProperty({
      id: property.id,
      title: property.title,
      category: property.category,
      location: property.location,
      priceLabel: property.priceLabel,
      area: "",
      verified: property.verified,
      image: property.image,
    });

    setSavedIds(updated.map((item) => String(item.id)));
  };

  const filteredProperties = useMemo(() => {
    const value = search.trim().toLowerCase();

    return PROPERTIES.filter((property) => {
      const categoryMatch =
        activeCategory === "All" ||
        property.category === activeCategory;

      const searchMatch =
        !value ||
        property.title.toLowerCase().includes(value) ||
        property.location.toLowerCase().includes(value) ||
        property.category.toLowerCase().includes(value);

      return categoryMatch && searchMatch;
    });
  }, [activeCategory, search]);

  /*
   * Temporary frontend-only location behaviour.
   *
   * Later this can be replaced with the backend/map SDK
   * location service without changing the UI contract.
   */
  const handleNearMe = () => {
    if (!navigator.geolocation) {
      return;
    }

    setLocationLoading(true);

    navigator.geolocation.getCurrentPosition(
      () => {
        setLocationLoading(false);

        /*
         * The real map provider will later:
         *
         * 1. receive latitude/longitude
         * 2. center the map
         * 3. query nearby properties
         */
      },
      () => {
        setLocationLoading(false);
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0,
      },
    );
  };

  /*
   * Convert Chennai coordinates into positions
   * inside our temporary visual map.
   *
   * This is deliberately isolated so that it can be
   * replaced by Leaflet / Google Maps / Mapbox later.
   */
  const getPinPosition = (
    lat: number,
    lng: number,
  ) => {
    const minLat = 12.88;
    const maxLat = 13.15;
    const minLng = 80.08;
    const maxLng = 80.32;

    const left =
      ((lng - minLng) /
        (maxLng - minLng)) *
      100;

    const top =
      (1 -
        (lat - minLat) /
          (maxLat - minLat)) *
      100;

    return {
      left: `${Math.max(5, Math.min(95, left))}%`,
      top: `${Math.max(8, Math.min(92, top))}%`,
    };
  };

  return (
    <main className="map-page">
      {/* =====================================================
          HEADER
      ====================================================== */}

      <header className="map-header">
        <button
          type="button"
          className="map-back-button"
          onClick={() => navigate("/home")}
          aria-label="Back to home"
        >
          <ArrowLeft size={21} />
        </button>

        <div className="map-header-title">
          <span>EXPLORE</span>
          <h1>Properties near you</h1>
        </div>

        <button
          type="button"
          className="map-filter-button"
          aria-label="Map filters"
        >
          <SlidersHorizontal size={19} />
        </button>
      </header>

      {/* =====================================================
          SEARCH
      ====================================================== */}

      <section className="map-search-section">
        <div className="map-search">
          <Search size={19} />

          <input
            type="text"
            placeholder="Search area or property"
            value={search}
            onChange={(event) =>
              setSearch(event.target.value)
            }
          />

          {search && (
            <button
              type="button"
              onClick={() => setSearch("")}
              aria-label="Clear search"
            >
              <X size={17} />
            </button>
          )}
        </div>

        <div className="map-category-scroll">
          {categories.map((category) => (
            <button
              type="button"
              key={category}
              className={
                activeCategory === category
                  ? "map-category active"
                  : "map-category"
              }
              onClick={() =>
                setActiveCategory(category)
              }
            >
              {category}
            </button>
          ))}
        </div>
      </section>

      {/* =====================================================
          MAP
      ====================================================== */}

      <section className="map-canvas">
        <div className="map-road road-one" />
        <div className="map-road road-two" />
        <div className="map-road road-three" />
        <div className="map-road road-four" />
        <div className="map-road road-five" />

        <div className="map-water" />

        <div className="map-label label-adyar">
          Adyar
        </div>

        <div className="map-label label-guindy">
          Guindy
        </div>

        <div className="map-label label-velachery">
          Velachery
        </div>

        <div className="map-label label-tnagar">
          T. Nagar
        </div>

        <div className="map-label label-tambaram">
          Tambaram
        </div>

        {filteredProperties.map((property) => (
          <button
            type="button"
            key={property.id}
            className={
              selectedProperty?.id === property.id
                ? "map-pin selected"
                : "map-pin"
            }
            style={getPinPosition(
              property.lat,
              property.lng,
            )}
            onClick={() =>
              setSelectedProperty(property)
            }
            aria-label={`View ${property.title}`}
          >
            <span>₹</span>
          </button>
        ))}

        {/* Current location */}

        <button
          type="button"
          className="map-location-button"
          onClick={handleNearMe}
          aria-label="Find properties near me"
        >
          <Crosshair
            size={20}
            className={
              locationLoading
                ? "location-loading"
                : ""
            }
          />
        </button>

        <div className="map-location-label">
          Chennai
        </div>
      </section>

      {/* =====================================================
          RESULTS
      ====================================================== */}

      <section className="map-results">
        <div className="map-results-header">
          <div>
            <span>AVAILABLE NEARBY</span>
            <h2>
              {filteredProperties.length} properties
            </h2>
          </div>

          <button
            type="button"
            className="map-list-button"
          >
            List view
          </button>
        </div>

        {selectedProperty ? (
          <article className="map-selected-card">
            <img
              src={selectedProperty.image}
              alt={selectedProperty.title}
            />

            <div className="map-selected-info">
              <div className="map-selected-top">
                <span className="map-property-category">
                  {selectedProperty.category}
                </span>

                <button
                  type="button"
                  className={
                    savedIds.includes(String(selectedProperty.id))
                      ? "saved-active"
                      : ""
                  }
                  aria-label={
                    savedIds.includes(String(selectedProperty.id))
                      ? "Remove from saved"
                      : "Save property"
                  }
                  onClick={() => toggleFavorite(selectedProperty)}
                >
                  <Heart
                    size={18}
                    fill={
                      savedIds.includes(String(selectedProperty.id))
                        ? "currentColor"
                        : "none"
                    }
                  />
                </button>
              </div>

              <h3>{selectedProperty.title}</h3>

              <div className="map-property-location">
                <MapPin size={14} />
                <span>
                  {selectedProperty.location}
                </span>
              </div>

              <div className="map-property-footer">
                <strong>
                  {selectedProperty.priceLabel}
                </strong>

                <button
                  type="button"
                  onClick={() =>
                    navigate(
                      `/properties/${selectedProperty.id}`,
                    )
                  }
                >
                  View
                </button>
              </div>
            </div>

            <button
              type="button"
              className="map-card-close"
              onClick={() =>
                setSelectedProperty(null)
              }
              aria-label="Close property preview"
            >
              <X size={16} />
            </button>
          </article>
        ) : (
          <div className="map-hint-card">
            <div className="map-hint-icon">
              <MapPin size={20} />
            </div>

            <div>
              <strong>
                Explore properties around Chennai
              </strong>

              <p>
                Tap a property pin to preview its
                details.
              </p>
            </div>
          </div>
        )}
      </section>

      {/* =====================================================
          BOTTOM NAVIGATION
      ====================================================== */}

      <nav
        className="map-bottom-navigation"
        aria-label="Main navigation"
      >
        <button
          type="button"
          onClick={() => navigate("/home")}
        >
          <Home size={20} />
          <span>Home</span>
        </button>

        <button
          type="button"
          className="active"
        >
          <MapPin size={20} />
          <span>Map</span>
        </button>

        <button
          type="button"
          onClick={() =>
            navigate(isAuthenticated ? "/saved" : "/login")
          }
        >
          <Heart size={20} />
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

export default Map;