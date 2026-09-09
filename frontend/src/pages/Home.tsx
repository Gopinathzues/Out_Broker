import {
  useEffect,
  useMemo,
  useState,
} from "react";

import {
  Bell,
  ChevronDown,
  Filter,
  Heart,
  Home as HomeIcon,
  MapPin,
  Plus,
  Search,
  SlidersHorizontal,
  UserRound,
  X,
} from "lucide-react";

import { useNavigate } from "react-router-dom";

import { useAuth } from "../context/AuthContext";

import {
  getSavedProperties,
  toggleSavedProperty,
} from "../data/propertyStore";

import "./Home.css";


/* ============================================================
   PROPERTY TYPE
============================================================ */

type Property = {
  id: number;

  title: string;

  category:
    | "House"
    | "Office Room"
    | "Shop"
    | "Hostel/PG";

  location: string;

  price: number;

  priceLabel: string;

  bhk?: string;

  area: string;

  furnishing?: string;

  verified: boolean;

  featured?: boolean;

  image: string;
};


/* ============================================================
   DEMO PROPERTIES
============================================================ */

const PROPERTIES: Property[] = [
  {
    id: 1,

    title: "3 BHK Premium Flat",

    category: "House",

    location: "Adyar, Chennai",

    price: 35000,

    priceLabel: "₹35,000 / month",

    bhk: "3 BHK",

    area: "1,450 sq.ft",

    furnishing: "Semi-furnished",

    verified: true,

    featured: true,

    image:
      "https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?auto=format&fit=crop&w=900&q=80",
  },

  {
    id: 2,

    title: "Modern 2 BHK Apartment",

    category: "House",

    location: "Velachery, Chennai",

    price: 24000,

    priceLabel: "₹24,000 / month",

    bhk: "2 BHK",

    area: "1,100 sq.ft",

    furnishing: "Fully furnished",

    verified: true,

    image:
      "https://images.unsplash.com/photo-1600566753190-17f0baa2a6c3?auto=format&fit=crop&w=900&q=80",
  },

  {
    id: 3,

    title: "Premium Office Space",

    category: "Office Room",

    location: "Guindy, Chennai",

    price: 28000,

    priceLabel: "₹28,000 / month",

    area: "850 sq.ft",

    furnishing: "Fully furnished",

    verified: true,

    featured: true,

    image:
      "https://images.unsplash.com/photo-1497366754035-f200968a6e72?auto=format&fit=crop&w=900&q=80",
  },

  {
    id: 4,

    title: "Roadside Commercial Shop",

    category: "Shop",

    location: "T. Nagar, Chennai",

    price: 45000,

    priceLabel: "₹45,000 / month",

    area: "620 sq.ft",

    furnishing: "Unfurnished",

    verified: true,

    image:
      "https://images.unsplash.com/photo-1556761175-b413da4baf72?auto=format&fit=crop&w=900&q=80",
  },

  {
    id: 5,

    title: "Comfortable PG for Students",

    category: "Hostel/PG",

    location: "Tambaram, Chennai",

    price: 8500,

    priceLabel: "₹8,500 / month",

    area: "Shared accommodation",

    furnishing: "Fully furnished",

    verified: true,

    image:
      "https://images.unsplash.com/photo-1555854877-bab0e564b8d5?auto=format&fit=crop&w=900&q=80",
  },

  {
    id: 6,

    title: "Spacious 1 BHK House",

    category: "House",

    location: "Anna Nagar, Chennai",

    price: 18000,

    priceLabel: "₹18,000 / month",

    bhk: "1 BHK",

    area: "750 sq.ft",

    furnishing: "Semi-furnished",

    verified: true,

    image:
      "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=900&q=80",
  },
];


/* ============================================================
   CATEGORIES
============================================================ */

const CATEGORIES = [
  {
    label: "All",
    icon: "⌂",
  },

  {
    label: "House",
    icon: "⌂",
  },

  {
    label: "Office Room",
    icon: "▦",
  },

  {
    label: "Shop",
    icon: "▣",
  },

  {
    label: "Hostel/PG",
    icon: "▤",
  },
];


/* ============================================================
   HOME
============================================================ */

function Home() {
  const navigate = useNavigate();

  const {
    user,
    isAuthenticated,
  } = useAuth();


  /* ==========================================================
     UI STATE
  ========================================================== */

  const [search, setSearch] =
    useState("");

  const [activeCategory, setActiveCategory] =
    useState("All");

  const [showFilters, setShowFilters] =
    useState(false);

  const [showSort, setShowSort] =
    useState(false);

  const [sortBy, setSortBy] =
    useState("Recommended");


  /* ==========================================================
     SAVED PROPERTY STATE
     
     IMPORTANT:
     This is connected to the shared propertyStore.
     
     Home, Map and Saved page will all use the same
     localStorage-based saved-property system.
  ========================================================== */

  const [savedIds, setSavedIds] =
    useState<string[]>([]);


  /* ==========================================================
     LOAD SAVED PROPERTIES
     
     Runs when Home is opened.
  ========================================================== */

  useEffect(() => {
    const saved =
      getSavedProperties();

    setSavedIds(
      saved.map((property) =>
        String(property.id)
      )
    );
  }, []);


  /* ==========================================================
     FILTER + SEARCH + SORT
  ========================================================== */

  const filteredProperties =
    useMemo(() => {
      let result = [
        ...PROPERTIES,
      ];


      /* CATEGORY */

      if (
        activeCategory !==
        "All"
      ) {
        result =
          result.filter(
            (property) =>
              property.category ===
              activeCategory
          );
      }


      /* SEARCH */

      const searchValue =
        search
          .trim()
          .toLowerCase();


      if (searchValue) {
        result =
          result.filter(
            (property) => {
              return (
                property.title
                  .toLowerCase()
                  .includes(
                    searchValue
                  ) ||

                property.location
                  .toLowerCase()
                  .includes(
                    searchValue
                  ) ||

                property.category
                  .toLowerCase()
                  .includes(
                    searchValue
                  )
              );
            }
          );
      }


      /* SORT */

      if (
        sortBy ===
        "Price: Low to High"
      ) {
        result.sort(
          (a, b) =>
            a.price -
            b.price
        );
      }


      if (
        sortBy ===
        "Price: High to Low"
      ) {
        result.sort(
          (a, b) =>
            b.price -
            a.price
        );
      }


      return result;
    }, [
      activeCategory,
      search,
      sortBy,
    ]);


  /* ==========================================================
     PROPERTY NAVIGATION
  ========================================================== */

  const handlePropertyClick = (
    id: number
  ) => {
    navigate(
      `/properties/${id}`
    );
  };


  /* ==========================================================
     SAVE / UNSAVE PROPERTY
     
     Converts Home's property format into the common
     SavedProperty format used by propertyStore.
  ========================================================== */

  const toggleFavorite = (
    property: Property
  ) => {

    /*
     * Guests can browse freely, but saving a
     * property requires an account so their
     * favorites persist against a real profile.
     */

    if (!isAuthenticated) {
      navigate("/login");
      return;
    }

    const updated =
      toggleSavedProperty({
        id: property.id,

        title: property.title,

        category:
          property.category,

        location:
          property.location,

        price:
          property.price,

        priceLabel:
          property.priceLabel,

        bhk:
          property.bhk,

        area:
          property.area,

        furnishing:
          property.furnishing,

        verified:
          property.verified,

        featured:
          property.featured,

        image:
          property.image,
      });


    /* Update Home immediately */

    setSavedIds(
      updated.map((item) =>
        String(item.id)
      )
    );
  };


  /* ==========================================================
     ROLE-BASED ACCESS
     
     Property seekers (TENANT) can browse, search and save
     properties, but listing a property is an owner-only
     action. Guests and landlords still see the CTA — guests
     get routed into registration, landlords get routed into
     the real add-property flow.
  ========================================================== */

  const canListProperty =
    !isAuthenticated ||
    user?.role !== "TENANT";


  /* ==========================================================
     ADD PROPERTY FLOW
     
     Guest:
     Home → Register
     
     Authenticated:
     Home → Add Property
     
     ProtectedRoute will handle final authorization.
  ========================================================== */

  const handleAddProperty = () => {

    if (!isAuthenticated) {
      navigate(
        "/register",
        {
          state: {
            from:
              "/add-property",
          },
        }
      );

      return;
    }


    const isLandlord =
      user?.role ===
      "LANDLORD";


    const isFullyVerified =
      user?.verificationStatus
        ?.toUpperCase() ===
      "FULLY_VERIFIED";


    if (
      isLandlord &&
      isFullyVerified
    ) {
      navigate(
        "/add-property"
      );

      return;
    }


    /*
     * Let ProtectedRoute handle
     * authenticated users who are
     * not currently eligible.
     */

    navigate(
      "/add-property"
    );
  };


  /* ==========================================================
     RENDER
  ========================================================== */

  return (
    <main className="home-page">

      {/* =====================================================
          BACKGROUND DECORATION
      ====================================================== */}

      <div
        className="home-background"
        aria-hidden="true"
      >
        <span className="home-orb home-orb-one" />

        <span className="home-orb home-orb-two" />
      </div>


      <div className="home-container">


        {/* ===================================================
            TOP NAVIGATION
        ==================================================== */}

        <header className="home-header">

          <button
            className="home-brand"
            type="button"
            onClick={() =>
              navigate("/home")
            }
            aria-label="OutBroker home"
          >

            <span className="home-brand-mark">

              <HomeIcon
                size={19}
                strokeWidth={2.5}
              />

            </span>


            <span className="home-brand-name">

              <span>
                Out
              </span>

              <strong>
                Broker
              </strong>

            </span>

          </button>


          <div className="home-header-actions">


            {/* LOCATION */}

            <button
              className="location-button"
              type="button"
              aria-label="Current location"
            >

              <MapPin
                size={17}
                strokeWidth={2}
              />


              <span>

                <small>
                  Location
                </small>

                <strong>
                  Chennai
                </strong>

              </span>


              <ChevronDown
                size={15}
              />

            </button>


            {/* ADD PROPERTY
                (hidden for property seekers — listing
                a property is an owner-only action) */}

            {canListProperty && (

              <button
                className="add-property-header-button"
                type="button"
                onClick={
                  handleAddProperty
                }
                aria-label="List your property"
              >

                <span>

                  <Plus
                    size={17}
                    strokeWidth={2.6}
                  />

                </span>


                <span>
                  List your property
                </span>

              </button>

            )}


            {/* NOTIFICATIONS */}

            <button
              className="header-icon-button"
              type="button"
              aria-label="Notifications"
            >

              <Bell
                size={19}
                strokeWidth={2}
              />

              <span className="notification-dot" />

            </button>


            {/* PROFILE */}

            <button
              className="profile-button"
              type="button"
              onClick={() =>
                navigate(
                  isAuthenticated
                    ? "/account"
                    : "/login"
                )
              }
              aria-label="Profile"
            >

              <UserRound
                size={19}
                strokeWidth={2}
              />

            </button>

          </div>

        </header>


        {/* ===================================================
            HERO
        ==================================================== */}

        <section className="home-hero">

          <div className="hero-copy">

            <span className="hero-eyebrow">
              PROPERTY DISCOVERY
            </span>


            <h1>
              Find a place
              <br />

              <span>
                that feels right.
              </span>
            </h1>


            <p>
              Discover verified
              properties directly from
              owners. No unnecessary
              middlemen.
            </p>

          </div>


          <div
            className="hero-decoration"
            aria-hidden="true"
          >

            <div className="hero-house">

              <HomeIcon
                size={48}
                strokeWidth={1.4}
              />

            </div>

          </div>

        </section>


        {/* ===================================================
            SEARCH
        ==================================================== */}

        <section className="search-section">

          <div className="main-search">

            <Search
              className="search-icon"
              size={21}
              strokeWidth={2}
            />


            <input
              type="search"
              value={search}
              onChange={(event) =>
                setSearch(
                  event.target.value
                )
              }
              placeholder="Search by area, property or landmark"
              aria-label="Search properties"
            />


            {search && (
              <button
                className="clear-search"
                type="button"
                onClick={() =>
                  setSearch("")
                }
                aria-label="Clear search"
              >
                <X size={16} />
              </button>
            )}


            <button
              className="filter-trigger"
              type="button"
              onClick={() =>
                setShowFilters(
                  (value) => !value
                )
              }
              aria-label="Open filters"
            >

              <SlidersHorizontal
                size={19}
              />

              <span>
                Filters
              </span>

            </button>

          </div>

        </section>


        {/* ===================================================
            CATEGORIES
        ==================================================== */}

        <section className="category-section">

          <div className="section-heading-row">

            <div>

              <span className="section-kicker">
                BROWSE BY TYPE
              </span>

              <h2>
                What are you looking
                for?
              </h2>

            </div>

          </div>


          <div className="category-list">

            {CATEGORIES.map(
              (category) => (

                <button
                  key={
                    category.label
                  }
                  type="button"
                  className={`category-chip ${
                    activeCategory ===
                    category.label
                      ? "active"
                      : ""
                  }`}
                  onClick={() =>
                    setActiveCategory(
                      category.label
                    )
                  }
                >

                  <span className="category-icon">
                    {
                      category.icon
                    }
                  </span>


                  <span>
                    {
                      category.label
                    }
                  </span>

                </button>

              )
            )}

          </div>

        </section>


        {/* ===================================================
            FILTER PANEL
        ==================================================== */}

        {showFilters && (

          <section className="filter-panel">

            <div className="filter-panel-header">

              <div>

                <span className="section-kicker">
                  REFINE RESULTS
                </span>

                <h3>
                  Property filters
                </h3>

              </div>


              <button
                type="button"
                className="filter-close"
                onClick={() =>
                  setShowFilters(
                    false
                  )
                }
                aria-label="Close filters"
              >

                <X size={18} />

              </button>

            </div>


            <div className="filter-grid">

              <button
                type="button"
                className="filter-select"
              >

                <span>
                  Property type
                </span>

                <ChevronDown
                  size={16}
                />

              </button>


              <button
                type="button"
                className="filter-select"
              >

                <span>
                  Price range
                </span>

                <ChevronDown
                  size={16}
                />

              </button>


              <button
                type="button"
                className="filter-select"
              >

                <span>
                  BHK
                </span>

                <ChevronDown
                  size={16}
                />

              </button>


              <button
                type="button"
                className="filter-select"
              >

                <span>
                  Area
                </span>

                <ChevronDown
                  size={16}
                />

              </button>


              <button
                type="button"
                className="filter-select"
              >

                <span>
                  Furnishing
                </span>

                <ChevronDown
                  size={16}
                />

              </button>


              <button
                type="button"
                className="filter-select"
              >

                <span>
                  Amenities
                </span>

                <ChevronDown
                  size={16}
                />

              </button>

            </div>


            <div className="filter-note">
              More detailed filters
              will be connected to
              the property API.
            </div>

          </section>

        )}


        {/* ===================================================
            RESULTS HEADER
        ==================================================== */}

        <section className="results-header">

          <div>

            <span className="section-kicker">
              AVAILABLE NOW
            </span>

            <h2>
              {
                filteredProperties.length
              }{" "}
              properties
            </h2>

          </div>


          <div className="sort-wrapper">

            <button
              type="button"
              className="sort-button"
              onClick={() =>
                setShowSort(
                  (value) => !value
                )
              }
            >

              <Filter size={16} />

              <span>
                {sortBy}
              </span>

              <ChevronDown
                size={15}
              />

            </button>


            {showSort && (

              <div className="sort-menu">

                {[
                  "Recommended",
                  "Price: Low to High",
                  "Price: High to Low",
                ].map(
                  (option) => (

                    <button
                      key={option}
                      type="button"
                      className={
                        sortBy ===
                        option
                          ? "selected"
                          : ""
                      }
                      onClick={() => {

                        setSortBy(
                          option
                        );

                        setShowSort(
                          false
                        );

                      }}
                    >
                      {option}
                    </button>

                  )
                )}

              </div>

            )}

          </div>

        </section>


        {/* ===================================================
            PROPERTY GRID
        ==================================================== */}

        {filteredProperties.length >
        0 ? (

          <section className="property-grid">

            {filteredProperties.map(
              (property) => {

                /*
                 * Check shared Saved Store.
                 */

                const isFavorite =
                  savedIds.includes(
                    String(
                      property.id
                    )
                  );


                return (

                  <article
                    className="property-card"
                    key={
                      property.id
                    }
                  >


                    {/* PROPERTY IMAGE */}

                    <button
                      className="property-image-button"
                      type="button"
                      onClick={() =>
                        handlePropertyClick(
                          property.id
                        )
                      }
                    >

                      <div className="property-image-wrapper">

                        <img
                          src={
                            property.image
                          }
                          alt={
                            property.title
                          }
                          className="property-image"
                        />


                        <div className="image-overlay" />


                        {property.featured && (

                          <span className="featured-badge">
                            FEATURED
                          </span>

                        )}


                        <span className="category-badge">
                          {
                            property.category
                          }
                        </span>

                      </div>

                    </button>


                    {/* =================================================
                        SAVE / HEART BUTTON
                    ================================================== */}

                    <button
                      type="button"
                      className={`favorite-button ${
                        isFavorite
                          ? "active"
                          : ""
                      }`}
                      onClick={() =>
                        toggleFavorite(
                          property
                        )
                      }
                      aria-label={
                        isFavorite
                          ? "Remove from favorites"
                          : "Add to favorites"
                      }
                    >

                      <Heart
                        size={18}
                        strokeWidth={2}
                        fill={
                          isFavorite
                            ? "currentColor"
                            : "none"
                        }
                      />

                    </button>


                    {/* PROPERTY CONTENT */}

                    <button
                      type="button"
                      className="property-content"
                      onClick={() =>
                        handlePropertyClick(
                          property.id
                        )
                      }
                    >

                      <div className="property-title-row">

                        <h3>
                          {
                            property.title
                          }
                        </h3>


                        {property.verified && (

                          <span
                            className="verified-mini"
                            title="Verified property"
                          >
                            ✓
                          </span>

                        )}

                      </div>


                      <div className="property-location">

                        <MapPin
                          size={15}
                        />

                        <span>
                          {
                            property.location
                          }
                        </span>

                      </div>


                      <div className="property-specs">

                        {property.bhk && (

                          <span>
                            {
                              property.bhk
                            }
                          </span>

                        )}


                        <span>
                          {
                            property.area
                          }
                        </span>


                        {property.furnishing && (

                          <span>
                            {
                              property.furnishing
                            }
                          </span>

                        )}

                      </div>


                      <div className="property-footer">

                        <strong>
                          {
                            property.priceLabel
                          }
                        </strong>


                        <span className="view-property">

                          View

                          <span>
                            →
                          </span>

                        </span>

                      </div>

                    </button>

                  </article>

                );
              }
            )}

          </section>

        ) : (

          /* =================================================
             EMPTY STATE
          ================================================== */

          <section className="empty-state">

            <div className="empty-icon">

              <Search size={27} />

            </div>


            <h3>
              No properties
              found
            </h3>


            <p>
              Try a different
              area, property type
              or search term.
            </p>


            <button
              type="button"
              onClick={() => {

                setSearch("");

                setActiveCategory(
                  "All"
                );

              }}
            >
              Clear search
            </button>

          </section>

        )}


        {/* ===================================================
            BOTTOM NAVIGATION
        ==================================================== */}

        <nav
          className="bottom-navigation"
          aria-label="Main navigation"
        >


          {/* HOME */}

          <button
            type="button"
            className="bottom-nav-item active"
            onClick={() =>
              navigate("/home")
            }
          >

            <HomeIcon size={20} />

            <span>
              Home
            </span>

          </button>


          {/* MAP */}

          <button
            type="button"
            className="bottom-nav-item"
            onClick={() =>
              navigate("/map")
            }
          >

            <MapPin size={20} />

            <span>
              Map
            </span>

          </button>


          {/* SAVED */}

          <button
            type="button"
            className="bottom-nav-item"
            onClick={() =>
              navigate(
                isAuthenticated
                  ? "/saved"
                  : "/login"
              )
            }
          >

            <Heart
              size={20}
              fill="none"
            />

            <span>
              Saved
            </span>

          </button>


          {/* PROFILE */}

          <button
            type="button"
            className="bottom-nav-item"
            onClick={() =>
              navigate(
                isAuthenticated
                  ? "/account"
                  : "/login"
              )
            }
          >

            <UserRound size={20} />

            <span>
              Profile
            </span>

          </button>

        </nav>

      </div>

    </main>
  );
}


export default Home;