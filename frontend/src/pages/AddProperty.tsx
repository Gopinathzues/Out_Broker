import { useEffect, useRef, useState } from "react";
import type { ChangeEvent } from "react";
import {
  ArrowLeft,
  Camera,
  Check,
  ChevronLeft,
  ChevronRight,
  CircleCheck,
  Clock3,
  Crosshair,
  Home,
  ImagePlus,
  Info,
  MapPin,
  ShieldCheck,
  Store,
  Upload,
  Building2,
  Users,
} from "lucide-react";
import { useNavigate } from "react-router-dom";
import "./AddProperty.css";

type Category = "House" | "Office Room" | "Shop" | "Hostel/PG" | "";

type FormData = {
  category: Category;
  listingType: string;
  title: string;
  description: string;

  houseType: string;
  bhk: string;
  floor: string;
  furnishing: string;

  area: string;
  price: string;
  securityDeposit: string;

  tenantTypes: string[];

  hostelType: string;
  roomType: string;
  totalBeds: string;
  stayOptions: string[];
  dayPrice: string;
  monthlyPrice: string;
  yearlyPrice: string;
  monthlyWithFood: string;
  monthlyWithoutFood: string;
  yearlyWithFood: string;
  yearlyWithoutFood: string;
  withACPrice: string;
  withoutACPrice: string;
  curfew: string;

  amenities: string[];

  address: string;
  nearbyPlace: string;
  immediateMoveIn: boolean;
};

type CapturedPhoto = {
  id: string;
  dataUrl: string;
};

const STEPS = [
  { title: "Basic", short: "Basic" },
  { title: "Details", short: "Details" },
  { title: "Pricing", short: "Pricing" },
  { title: "Photos", short: "Photos" },
  { title: "Verify", short: "Verify" },
];

const CATEGORIES = [
  {
    label: "House" as Category,
    description: "Apartment, independent house or villa",
    icon: Home,
  },
  {
    label: "Office Room" as Category,
    description: "Office and workspace listings",
    icon: Building2,
  },
  {
    label: "Shop" as Category,
    description: "Commercial shop or retail space",
    icon: Store,
  },
  {
    label: "Hostel/PG" as Category,
    description: "Hostel, PG and co-living",
    icon: Users,
  },
];

const AMENITIES = [
  "Parking",
  "Lift",
  "Power Backup",
  "Security",
  "Water Supply",
  "Wi-Fi",
  "Air Conditioning",
  "CCTV",
  "Gym",
  "Swimming Pool",
];

const TENANT_TYPES = [
  "Family",
  "Bachelor for Boys",
  "Bachelor for Girls",
  "Family & Bachelor",
  "Any",
];

function AddProperty() {
  const navigate = useNavigate();

  const [step, setStep] = useState(0);

  const [form, setForm] = useState<FormData>({
    category: "",
    listingType: "",
    title: "",
    description: "",

    houseType: "",
    bhk: "",
    floor: "",
    furnishing: "",

    area: "",
    price: "",
    securityDeposit: "",

    tenantTypes: [],

    hostelType: "",
    roomType: "",
    totalBeds: "",
    stayOptions: [],
    dayPrice: "",
    monthlyPrice: "",
    yearlyPrice: "",
    monthlyWithFood: "",
    monthlyWithoutFood: "",
    yearlyWithFood: "",
    yearlyWithoutFood: "",
    withACPrice: "",
    withoutACPrice: "",
    curfew: "",

    amenities: [],

    address: "",
    nearbyPlace: "",
    immediateMoveIn: false,
  });

  const [photos, setPhotos] = useState<CapturedPhoto[]>([]);
  const [cameraOpen, setCameraOpen] = useState(false);
  const [cameraError, setCameraError] = useState("");
  const [locationLoading, setLocationLoading] = useState(false);
  const [locationError, setLocationError] = useState("");
  const [locationGranted, setLocationGranted] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  const videoRef = useRef<HTMLVideoElement | null>(null);
  const streamRef = useRef<MediaStream | null>(null);

  useEffect(() => {
    return () => {
      stopCamera();
    };
  }, []);

  const updateForm = <K extends keyof FormData>(
    field: K,
    value: FormData[K]
  ) => {
    setForm((current) => ({
      ...current,
      [field]: value,
    }));
  };

  const toggleArrayValue = (
    field: "tenantTypes" | "stayOptions" | "amenities",
    value: string
  ) => {
    setForm((current) => {
      const values = current[field];

      return {
        ...current,
        [field]: values.includes(value)
          ? values.filter((item) => item !== value)
          : [...values, value],
      };
    });
  };

  const startCamera = async () => {
    setCameraError("");

    if (!navigator.mediaDevices?.getUserMedia) {
      setCameraError(
        "Live camera capture is not supported by this browser."
      );
      return;
    }

    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: {
          facingMode: { ideal: "environment" },
        },
        audio: false,
      });

      streamRef.current = stream;
      setCameraOpen(true);

      requestAnimationFrame(() => {
        if (videoRef.current) {
          videoRef.current.srcObject = stream;
          videoRef.current.play().catch(() => {});
        }
      });
    } catch {
      setCameraError(
        "Camera permission was denied or the camera is unavailable."
      );
    }
  };

  const stopCamera = () => {
    if (streamRef.current) {
      streamRef.current.getTracks().forEach((track) => track.stop());
      streamRef.current = null;
    }

    if (videoRef.current) {
      videoRef.current.srcObject = null;
    }

    setCameraOpen(false);
  };

  const capturePhoto = () => {
    const video = videoRef.current;

    if (!video || video.videoWidth === 0 || video.videoHeight === 0) {
      setCameraError("Camera is not ready yet. Please try again.");
      return;
    }

    const canvas = document.createElement("canvas");

    const maxWidth = 1280;
    const scale = Math.min(1, maxWidth / video.videoWidth);

    canvas.width = Math.round(video.videoWidth * scale);
    canvas.height = Math.round(video.videoHeight * scale);

    const context = canvas.getContext("2d");

    if (!context) {
      setCameraError("Unable to capture the image.");
      return;
    }

    context.drawImage(
      video,
      0,
      0,
      canvas.width,
      canvas.height
    );

    const dataUrl = canvas.toDataURL("image/jpeg", 0.82);

    setPhotos((current) => [
      ...current,
      {
        id: `${Date.now()}-${current.length}`,
        dataUrl,
      },
    ]);

    stopCamera();
  };

  const removePhoto = (id: string) => {
    setPhotos((current) =>
      current.filter((photo) => photo.id !== id)
    );
  };

  const requestGPS = () => {
    if (!navigator.geolocation) {
      setLocationError("GPS is not supported by this browser.");
      return;
    }

    setLocationLoading(true);
    setLocationError("");

    navigator.geolocation.getCurrentPosition(
      (position) => {
        const latitude = position.coords.latitude.toFixed(5);
        const longitude = position.coords.longitude.toFixed(5);

        setLocationGranted(true);
        setLocationLoading(false);

        setForm((current) => ({
          ...current,
          address:
            current.address ||
            `GPS: ${latitude}°N, ${longitude}°E`,
        }));
      },
      (error) => {
        setLocationLoading(false);

        if (error.code === 1) {
          setLocationError(
            "Location permission was denied. Please allow location access in your browser."
          );
        } else {
          setLocationError(
            "Unable to determine your location. Please try again."
          );
        }
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0,
      }
    );
  };

  const validateStep = () => {
    if (step === 0) {
      if (!form.category) {
        alert("Please select a property category.");
        return false;
      }

      if (!form.listingType) {
        alert("Please select a listing type.");
        return false;
      }

      if (!form.title.trim()) {
        alert("Please enter a property title.");
        return false;
      }

      return true;
    }

    if (step === 1) {
      if (!form.area.trim()) {
        alert("Please enter the property area.");
        return false;
      }

      if (!form.address.trim()) {
        alert("Please enter or detect the property location.");
        return false;
      }

      return true;
    }

    if (step === 2) {
      if (form.category === "Hostel/PG") {
        if (
          !form.monthlyPrice &&
          !form.dayPrice &&
          !form.yearlyPrice
        ) {
          alert("Please enter at least one Hostel/PG price.");
          return false;
        }
      } else if (!form.price) {
        alert("Please enter the property price.");
        return false;
      }

      return true;
    }

    if (step === 3) {
      if (photos.length === 0) {
        alert("Please capture at least one property photo.");
        return false;
      }

      return true;
    }

    return true;
  };

  const handleNext = () => {
    if (!validateStep()) return;

    setStep((current) =>
      Math.min(current + 1, STEPS.length - 1)
    );
  };

  const handleBack = () => {
    if (step === 0) {
      navigate(-1);
      return;
    }

    setStep((current) => Math.max(current - 1, 0));
  };

  const handleSubmit = async () => {
    if (!validateStep()) return;

    setIsSubmitting(true);

    /*
     * Temporary frontend behaviour.
     *
     * Backend team will replace this section with:
     * POST /api/v1/properties
     *
     * Property photos should be uploaded to the backend/storage
     * rather than being persisted as base64 data.
     */
    await new Promise((resolve) =>
      setTimeout(resolve, 900)
    );

    setIsSubmitting(false);
    setSubmitted(true);
  };

  const handleInputChange = (
    event: ChangeEvent<
      HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement
    >
  ) => {
    const { name, value } = event.target;

    setForm((current) => ({
      ...current,
      [name]: value,
    }));
  };

  if (submitted) {
    return (
      <main className="add-property-page">
        <div className="add-property-background" />

        <section className="submission-success">
          <div className="success-icon">
            <CircleCheck size={46} strokeWidth={1.8} />
          </div>

          <span className="success-badge">
            <ShieldCheck size={15} />
            Listing submitted
          </span>

          <h1>Property submitted successfully</h1>

          <p>
            Your property listing has been submitted for review.
            Once approved, it can become available to property
            seekers.
          </p>

          <div className="success-info">
            <div className="success-info-icon">
              <Clock3 size={20} />
            </div>

            <div>
              <strong>Verification review</strong>
              <span>
                The submitted listing will go through the required
                verification process.
              </span>
            </div>
          </div>

          <button
            className="primary-action"
            type="button"
            onClick={() => navigate("/home")}
          >
            Back to Home
          </button>
        </section>
      </main>
    );
  }

  return (
    <main className="add-property-page">
      <div
        className="add-property-background"
        aria-hidden="true"
      >
        <span className="property-orb property-orb-one" />
        <span className="property-orb property-orb-two" />
      </div>

      <header className="add-property-header">
        <button
          className="header-back-button"
          type="button"
          aria-label="Go back"
          onClick={handleBack}
        >
          <ArrowLeft size={21} strokeWidth={2.2} />
        </button>

        <div>
          <span className="header-eyebrow">
            OWNER LISTING
          </span>
          <h1>Add Property</h1>
        </div>

        <div className="verified-header-badge">
          <ShieldCheck size={15} />
          Verified
        </div>
      </header>

      <section className="add-property-container">
        <div className="page-intro">
          <div>
            <span className="section-kicker">
              CREATE A LISTING
            </span>

            <h2>List your property directly</h2>

            <p>
              Provide accurate property details so seekers can find
              the right place without unnecessary middlemen.
            </p>
          </div>

          <div className="step-counter">
            <strong>{step + 1}</strong>
            <span>/ {STEPS.length}</span>
          </div>
        </div>

        <div className="progress-card">
          <div className="progress-line">
            <span
              style={{
                width: `${
                  (step / (STEPS.length - 1)) * 100
                }%`,
              }}
            />
          </div>

          <div className="step-list">
            {STEPS.map((item, index) => {
              const completed = index < step;
              const active = index === step;

              return (
                <button
                  key={item.title}
                  className={`step-item ${
                    active ? "active" : ""
                  } ${completed ? "completed" : ""}`}
                  type="button"
                  onClick={() => {
                    if (index <= step) {
                      setStep(index);
                    }
                  }}
                >
                  <span className="step-circle">
                    {completed ? (
                      <Check size={14} strokeWidth={3} />
                    ) : (
                      index + 1
                    )}
                  </span>

                  <span className="step-title">
                    {item.title}
                  </span>
                </button>
              );
            })}
          </div>
        </div>

        <section className="form-card">
          {step === 0 && (
            <>
              <div className="form-heading">
                <span className="form-icon">
                  <Home size={19} />
                </span>

                <div>
                  <h3>Basic information</h3>
                  <p>
                    Start by telling us what kind of property you
                    are listing.
                  </p>
                </div>
              </div>

              <div className="field-section">
                <label className="field-label">
                  Property category
                  <span>*</span>
                </label>

                <div className="category-grid">
                  {CATEGORIES.map((category) => {
                    const Icon = category.icon;
                    const selected =
                      form.category === category.label;

                    return (
                      <button
                        key={category.label}
                        type="button"
                        className={`category-option ${
                          selected ? "selected" : ""
                        }`}
                        onClick={() =>
                          updateForm(
                            "category",
                            category.label
                          )
                        }
                      >
                        <span className="category-icon">
                          <Icon size={20} />
                        </span>

                        <span className="category-content">
                          <strong>{category.label}</strong>
                          <small>
                            {category.description}
                          </small>
                        </span>

                        <span className="selection-dot">
                          {selected && (
                            <Check
                              size={12}
                              strokeWidth={3}
                            />
                          )}
                        </span>
                      </button>
                    );
                  })}
                </div>
              </div>

              <div className="field-section">
                <label className="field-label">
                  Listing type
                  <span>*</span>
                </label>

                <div className="choice-row">
                  {["Rent", "Lease", "Sale"].map((type) => (
                    <button
                      key={type}
                      type="button"
                      className={`choice-button ${
                        form.listingType === type
                          ? "selected"
                          : ""
                      }`}
                      onClick={() =>
                        updateForm("listingType", type)
                      }
                    >
                      {form.listingType === type && (
                        <Check size={15} />
                      )}
                      {type}
                    </button>
                  ))}
                </div>
              </div>

              <div className="field-grid">
                <div className="field-section">
                  <label
                    className="field-label"
                    htmlFor="title"
                  >
                    Property title
                    <span>*</span>
                  </label>

                  <input
                    id="title"
                    name="title"
                    value={form.title}
                    onChange={handleInputChange}
                    placeholder="e.g. Spacious 2 BHK Apartment"
                    className="text-input"
                  />
                </div>

                <div className="field-section">
                  <label
                    className="field-label"
                    htmlFor="nearbyPlace"
                  >
                    Nearby landmark
                  </label>

                  <input
                    id="nearbyPlace"
                    name="nearbyPlace"
                    value={form.nearbyPlace}
                    onChange={handleInputChange}
                    placeholder="e.g. Near Adyar Depot"
                    className="text-input"
                  />
                </div>
              </div>

              <div className="field-section">
                <label
                  className="field-label"
                  htmlFor="description"
                >
                  Description
                </label>

                <textarea
                  id="description"
                  name="description"
                  value={form.description}
                  onChange={handleInputChange}
                  placeholder="Describe the property, surroundings and anything important for seekers..."
                  className="text-area"
                  rows={4}
                />
              </div>
            </>
          )}

          {step === 1 && (
            <>
              <div className="form-heading">
                <span className="form-icon">
                  <Building2 size={19} />
                </span>

                <div>
                  <h3>Property details</h3>
                  <p>
                    Add the specifications that help seekers
                    understand the property.
                  </p>
                </div>
              </div>

              {form.category === "House" && (
                <>
                  <div className="field-grid">
                    <div className="field-section">
                      <label className="field-label">
                        House type
                      </label>

                      <select
                        name="houseType"
                        value={form.houseType}
                        onChange={handleInputChange}
                        className="text-input"
                      >
                        <option value="">
                          Select house type
                        </option>
                        <option value="Apartment">
                          Apartment
                        </option>
                        <option value="Independent House">
                          Independent House
                        </option>
                        <option value="Villa">Villa</option>
                      </select>
                    </div>

                    <div className="field-section">
                      <label className="field-label">
                        BHK
                      </label>

                      <select
                        name="bhk"
                        value={form.bhk}
                        onChange={handleInputChange}
                        className="text-input"
                      >
                        <option value="">Select BHK</option>
                        {[
                          "1 BHK",
                          "2 BHK",
                          "3 BHK",
                          "4 BHK",
                          "5 BHK",
                          "6 BHK",
                        ].map((item) => (
                          <option key={item} value={item}>
                            {item}
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>

                  <div className="field-grid">
                    <div className="field-section">
                      <label className="field-label">
                        Floor
                      </label>

                      <input
                        name="floor"
                        value={form.floor}
                        onChange={handleInputChange}
                        placeholder="e.g. 4th Floor"
                        className="text-input"
                      />
                    </div>

                    <div className="field-section">
                      <label className="field-label">
                        Furnishing
                      </label>

                      <select
                        name="furnishing"
                        value={form.furnishing}
                        onChange={handleInputChange}
                        className="text-input"
                      >
                        <option value="">
                          Select furnishing
                        </option>
                        <option value="Fully furnished">
                          Fully furnished
                        </option>
                        <option value="Semi-furnished">
                          Semi-furnished
                        </option>
                        <option value="Unfurnished">
                          Unfurnished
                        </option>
                      </select>
                    </div>
                  </div>
                </>
              )}

              {form.category === "Office Room" && (
                <div className="field-grid">
                  <div className="field-section">
                    <label className="field-label">
                      Office type
                    </label>

                    <select
                      name="houseType"
                      value={form.houseType}
                      onChange={handleInputChange}
                      className="text-input"
                    >
                      <option value="">
                        Select office type
                      </option>
                      <option value="Private Office">
                        Private Office
                      </option>
                      <option value="Shared Office">
                        Shared Office
                      </option>
                      <option value="Coworking Space">
                        Coworking Space
                      </option>
                    </select>
                  </div>

                  <div className="field-section">
                    <label className="field-label">
                      Furnishing
                    </label>

                    <select
                      name="furnishing"
                      value={form.furnishing}
                      onChange={handleInputChange}
                      className="text-input"
                    >
                      <option value="">
                        Select furnishing
                      </option>
                      <option value="Fully furnished">
                        Fully furnished
                      </option>
                      <option value="Semi-furnished">
                        Semi-furnished
                      </option>
                      <option value="Unfurnished">
                        Unfurnished
                      </option>
                    </select>
                  </div>
                </div>
              )}

              {form.category === "Shop" && (
                <div className="field-grid">
                  <div className="field-section">
                    <label className="field-label">
                      Shop type
                    </label>

                    <select
                      name="houseType"
                      value={form.houseType}
                      onChange={handleInputChange}
                      className="text-input"
                    >
                      <option value="">
                        Select shop type
                      </option>
                      <option value="Retail Shop">
                        Retail Shop
                      </option>
                      <option value="Commercial Space">
                        Commercial Space
                      </option>
                      <option value="Showroom">
                        Showroom
                      </option>
                    </select>
                  </div>

                  <div className="field-section">
                    <label className="field-label">
                      Furnishing
                    </label>

                    <select
                      name="furnishing"
                      value={form.furnishing}
                      onChange={handleInputChange}
                      className="text-input"
                    >
                      <option value="">
                        Select furnishing
                      </option>
                      <option value="Fully furnished">
                        Fully furnished
                      </option>
                      <option value="Semi-furnished">
                        Semi-furnished
                      </option>
                      <option value="Unfurnished">
                        Unfurnished
                      </option>
                    </select>
                  </div>
                </div>
              )}

              {form.category === "Hostel/PG" && (
                <>
                  <div className="field-grid">
                    <div className="field-section">
                      <label className="field-label">
                        Hostel type
                      </label>

                      <select
                        name="hostelType"
                        value={form.hostelType}
                        onChange={handleInputChange}
                        className="text-input"
                      >
                        <option value="">
                          Select hostel type
                        </option>
                        <option value="Boys Hostel">
                          Boys Hostel
                        </option>
                        <option value="Girls Hostel">
                          Girls Hostel
                        </option>
                        <option value="PG">PG</option>
                        <option value="Co-living">
                          Co-living
                        </option>
                      </select>
                    </div>

                    <div className="field-section">
                      <label className="field-label">
                        Room type
                      </label>

                      <select
                        name="roomType"
                        value={form.roomType}
                        onChange={handleInputChange}
                        className="text-input"
                      >
                        <option value="">
                          Select room type
                        </option>
                        <option value="Single">
                          Single
                        </option>
                        <option value="Double Sharing">
                          Double Sharing
                        </option>
                        <option value="Triple Sharing">
                          Triple Sharing
                        </option>
                        <option value="Dormitory">
                          Dormitory
                        </option>
                      </select>
                    </div>
                  </div>

                  <div className="field-grid">
                    <div className="field-section">
                      <label className="field-label">
                        Total beds
                      </label>

                      <input
                        name="totalBeds"
                        type="number"
                        min="1"
                        value={form.totalBeds}
                        onChange={handleInputChange}
                        placeholder="e.g. 20"
                        className="text-input"
                      />
                    </div>

                    <div className="field-section">
                      <label className="field-label">
                        Curfew
                      </label>

                      <input
                        name="curfew"
                        value={form.curfew}
                        onChange={handleInputChange}
                        placeholder="e.g. 10:30 PM"
                        className="text-input"
                      />
                    </div>
                  </div>

                  <div className="field-section">
                    <label className="field-label">
                      Available stay duration
                    </label>

                    <div className="choice-row wrap">
                      {[
                        "Day Stay",
                        "Monthly Stay",
                        "Yearly Stay",
                      ].map((option) => (
                        <button
                          key={option}
                          type="button"
                          className={`choice-button ${
                            form.stayOptions.includes(option)
                              ? "selected"
                              : ""
                          }`}
                          onClick={() =>
                            toggleArrayValue(
                              "stayOptions",
                              option
                            )
                          }
                        >
                          {form.stayOptions.includes(option) && (
                            <Check size={15} />
                          )}
                          {option}
                        </button>
                      ))}
                    </div>
                  </div>
                </>
              )}

              <div className="field-grid">
                <div className="field-section">
                  <label className="field-label">
                    Area
                    <span>*</span>
                  </label>

                  <div className="input-with-unit">
                    <input
                      name="area"
                      type="number"
                      min="0"
                      value={form.area}
                      onChange={handleInputChange}
                      placeholder="e.g. 1450"
                      className="text-input"
                    />
                    <span>sq.ft</span>
                  </div>
                </div>

                <div className="field-section">
                  <label className="field-label">
                    Nearby place
                  </label>

                  <input
                    name="nearbyPlace"
                    value={form.nearbyPlace}
                    onChange={handleInputChange}
                    placeholder="e.g. Adyar"
                    className="text-input"
                  />
                </div>
              </div>

              <div className="field-section">
                <label className="field-label">
                  Preferred tenant
                </label>

                <div className="choice-row wrap">
                  {TENANT_TYPES.map((tenant) => (
                    <button
                      key={tenant}
                      type="button"
                      className={`choice-button ${
                        form.tenantTypes.includes(tenant)
                          ? "selected"
                          : ""
                      }`}
                      onClick={() =>
                        toggleArrayValue(
                          "tenantTypes",
                          tenant
                        )
                      }
                    >
                      {form.tenantTypes.includes(tenant) && (
                        <Check size={15} />
                      )}
                      {tenant}
                    </button>
                  ))}
                </div>
              </div>

              <div className="field-section">
                <label className="field-label">
                  Amenities
                </label>

                <div className="amenities-grid">
                  {AMENITIES.map((amenity) => (
                    <button
                      key={amenity}
                      type="button"
                      className={`amenity-chip ${
                        form.amenities.includes(amenity)
                          ? "selected"
                          : ""
                      }`}
                      onClick={() =>
                        toggleArrayValue(
                          "amenities",
                          amenity
                        )
                      }
                    >
                      {form.amenities.includes(amenity) && (
                        <Check size={13} />
                      )}
                      {amenity}
                    </button>
                  ))}
                </div>
              </div>

              <div className="location-section">
                <div className="location-heading">
                  <span className="location-icon">
                    <MapPin size={19} />
                  </span>

                  <div>
                    <strong>Property location</strong>
                    <p>
                      GPS auto-tagging helps keep the listing
                      location accurate.
                    </p>
                  </div>
                </div>

                <div className="location-actions">
                  <button
                    type="button"
                    className="location-button"
                    onClick={requestGPS}
                    disabled={locationLoading}
                  >
                    <Crosshair size={18} />
                    {locationLoading
                      ? "Detecting location..."
                      : "Use my current location"}
                  </button>

                  {locationGranted && (
                    <span className="location-success">
                      <Check size={14} />
                      Location detected
                    </span>
                  )}
                </div>

                {locationError && (
                  <p className="location-error">
                    {locationError}
                  </p>
                )}

                <input
                  name="address"
                  value={form.address}
                  onChange={handleInputChange}
                  placeholder="Enter full property address"
                  className="text-input location-input"
                />
              </div>

              <label className="toggle-row">
                <input
                  type="checkbox"
                  checked={form.immediateMoveIn}
                  onChange={(event) =>
                    updateForm(
                      "immediateMoveIn",
                      event.target.checked
                    )
                  }
                />

                <span className="toggle-box">
                  {form.immediateMoveIn && (
                    <Check size={13} strokeWidth={3} />
                  )}
                </span>

                <span>
                  <strong>Available for immediate move-in</strong>
                  <small>
                    Let seekers know the property is currently
                    available.
                  </small>
                </span>
              </label>
            </>
          )}

          {step === 2 && (
            <>
              <div className="form-heading">
                <span className="form-icon">
                  ₹
                </span>

                <div>
                  <h3>Pricing</h3>
                  <p>
                    Enter the applicable pricing information for
                    this property.
                  </p>
                </div>
              </div>

              {form.category !== "Hostel/PG" ? (
                <>
                  <div className="pricing-highlight">
                    <div>
                      <span>Listing type</span>
                      <strong>
                        {form.listingType || "Not selected"}
                      </strong>
                    </div>

                    <div className="pricing-highlight-divider" />

                    <div>
                      <span>Category</span>
                      <strong>
                        {form.category || "Not selected"}
                      </strong>
                    </div>
                  </div>

                  <div className="field-grid">
                    <div className="field-section">
                      <label className="field-label">
                        {form.listingType === "Sale"
                          ? "Sale price"
                          : "Monthly price"}
                        <span>*</span>
                      </label>

                      <div className="input-with-prefix">
                        <span>₹</span>
                        <input
                          name="price"
                          type="number"
                          min="0"
                          value={form.price}
                          onChange={handleInputChange}
                          placeholder="35,000"
                          className="text-input"
                        />
                      </div>
                    </div>

                    <div className="field-section">
                      <label className="field-label">
                        Security deposit
                      </label>

                      <div className="input-with-prefix">
                        <span>₹</span>
                        <input
                          name="securityDeposit"
                          type="number"
                          min="0"
                          value={form.securityDeposit}
                          onChange={handleInputChange}
                          placeholder="70,000"
                          className="text-input"
                        />
                      </div>
                    </div>
                  </div>
                </>
              ) : (
                <>
                  <div className="info-banner">
                    <Info size={18} />
                    <span>
                      Hostel/PG listings can have different pricing
                      for day, monthly and yearly stays, including
                      food and AC options.
                    </span>
                  </div>

                  <div className="pricing-block">
                    <div className="pricing-block-header">
                      <strong>Stay pricing</strong>
                      <span>Optional fields based on availability</span>
                    </div>

                    <div className="field-grid three">
                      <div className="field-section">
                        <label className="field-label">
                          Day stay
                        </label>

                        <div className="input-with-prefix">
                          <span>₹</span>
                          <input
                            name="dayPrice"
                            type="number"
                            min="0"
                            value={form.dayPrice}
                            onChange={handleInputChange}
                            placeholder="1,000"
                            className="text-input"
                          />
                        </div>
                      </div>

                      <div className="field-section">
                        <label className="field-label">
                          Monthly
                        </label>

                        <div className="input-with-prefix">
                          <span>₹</span>
                          <input
                            name="monthlyPrice"
                            type="number"
                            min="0"
                            value={form.monthlyPrice}
                            onChange={handleInputChange}
                            placeholder="8,500"
                            className="text-input"
                          />
                        </div>
                      </div>

                      <div className="field-section">
                        <label className="field-label">
                          Yearly
                        </label>

                        <div className="input-with-prefix">
                          <span>₹</span>
                          <input
                            name="yearlyPrice"
                            type="number"
                            min="0"
                            value={form.yearlyPrice}
                            onChange={handleInputChange}
                            placeholder="90,000"
                            className="text-input"
                          />
                        </div>
                      </div>
                    </div>
                  </div>

                  <div className="pricing-block">
                    <div className="pricing-block-header">
                      <strong>Food-inclusive pricing</strong>
                      <span>
                        Enter prices only where applicable
                      </span>
                    </div>

                    <div className="field-grid">
                      <div className="field-section">
                        <label className="field-label">
                          Monthly with food
                        </label>

                        <div className="input-with-prefix">
                          <span>₹</span>
                          <input
                            name="monthlyWithFood"
                            type="number"
                            min="0"
                            value={form.monthlyWithFood}
                            onChange={handleInputChange}
                            placeholder="10,000"
                            className="text-input"
                          />
                        </div>
                      </div>

                      <div className="field-section">
                        <label className="field-label">
                          Monthly without food
                        </label>

                        <div className="input-with-prefix">
                          <span>₹</span>
                          <input
                            name="monthlyWithoutFood"
                            type="number"
                            min="0"
                            value={form.monthlyWithoutFood}
                            onChange={handleInputChange}
                            placeholder="8,500"
                            className="text-input"
                          />
                        </div>
                      </div>

                      <div className="field-section">
                        <label className="field-label">
                          Yearly with food
                        </label>

                        <div className="input-with-prefix">
                          <span>₹</span>
                          <input
                            name="yearlyWithFood"
                            type="number"
                            min="0"
                            value={form.yearlyWithFood}
                            onChange={handleInputChange}
                            placeholder="1,10,000"
                            className="text-input"
                          />
                        </div>
                      </div>

                      <div className="field-section">
                        <label className="field-label">
                          Yearly without food
                        </label>

                        <div className="input-with-prefix">
                          <span>₹</span>
                          <input
                            name="yearlyWithoutFood"
                            type="number"
                            min="0"
                            value={form.yearlyWithoutFood}
                            onChange={handleInputChange}
                            placeholder="90,000"
                            className="text-input"
                          />
                        </div>
                      </div>
                    </div>
                  </div>

                  <div className="pricing-block">
                    <div className="pricing-block-header">
                      <strong>AC pricing</strong>
                      <span>
                        Use this when AC/non-AC pricing differs
                      </span>
                    </div>

                    <div className="field-grid">
                      <div className="field-section">
                        <label className="field-label">
                          With AC
                        </label>

                        <div className="input-with-prefix">
                          <span>₹</span>
                          <input
                            name="withACPrice"
                            type="number"
                            min="0"
                            value={form.withACPrice}
                            onChange={handleInputChange}
                            placeholder="10,000"
                            className="text-input"
                          />
                        </div>
                      </div>

                      <div className="field-section">
                        <label className="field-label">
                          Without AC
                        </label>

                        <div className="input-with-prefix">
                          <span>₹</span>
                          <input
                            name="withoutACPrice"
                            type="number"
                            min="0"
                            value={form.withoutACPrice}
                            onChange={handleInputChange}
                            placeholder="8,500"
                            className="text-input"
                          />
                        </div>
                      </div>
                    </div>
                  </div>
                </>
              )}
            </>
          )}

          {step === 3 && (
            <>
              <div className="form-heading">
                <span className="form-icon">
                  <Camera size={19} />
                </span>

                <div>
                  <h3>Property photos</h3>
                  <p>
                    Capture clear photos of the property using the
                    live camera.
                  </p>
                </div>
              </div>

              <div className="camera-notice">
                <div className="notice-icon">
                  <ShieldCheck size={19} />
                </div>

                <div>
                  <strong>Live capture only</strong>
                  <p>
                    To follow OutBroker's verification approach,
                    property photos are captured directly through
                    the camera rather than selected from the
                    gallery.
                  </p>
                </div>
              </div>

              {cameraError && (
                <div className="camera-error">
                  <Info size={17} />
                  <span>{cameraError}</span>
                </div>
              )}

              {!cameraOpen ? (
                <button
                  type="button"
                  className="camera-launch"
                  onClick={startCamera}
                >
                  <span className="camera-launch-icon">
                    <Camera size={25} />
                  </span>

                  <span>
                    <strong>Open camera</strong>
                    <small>
                      Capture a new property photo
                    </small>
                  </span>

                  <ChevronRight size={20} />
                </button>
              ) : (
                <div className="camera-panel">
                  <video
                    ref={videoRef}
                    className="camera-video"
                    autoPlay
                    muted
                    playsInline
                  />

                  <div className="camera-overlay">
                    <div className="camera-frame" />
                    <span>
                      Position the property clearly inside the
                      frame
                    </span>
                  </div>

                  <div className="camera-controls">
                    <button
                      type="button"
                      className="camera-cancel"
                      onClick={stopCamera}
                    >
                      Cancel
                    </button>

                    <button
                      type="button"
                      className="capture-button"
                      onClick={capturePhoto}
                      aria-label="Capture property photo"
                    >
                      <span />
                    </button>

                    <div className="camera-control-spacer" />
                  </div>
                </div>
              )}

              {photos.length > 0 && (
                <div className="photo-section">
                  <div className="photo-section-header">
                    <div>
                      <strong>Captured photos</strong>
                      <span>
                        {photos.length} photo
                        {photos.length !== 1 ? "s" : ""}
                      </span>
                    </div>

                    <span className="photo-count">
                      {photos.length}
                    </span>
                  </div>

                  <div className="photo-grid">
                    {photos.map((photo, index) => (
                      <div
                        className="captured-photo"
                        key={photo.id}
                      >
                        <img
                          src={photo.dataUrl}
                          alt={`Property capture ${index + 1}`}
                        />

                        <span className="photo-number">
                          {index + 1}
                        </span>

                        <button
                          type="button"
                          className="remove-photo"
                          onClick={() =>
                            removePhoto(photo.id)
                          }
                          aria-label={`Remove photo ${
                            index + 1
                          }`}
                        >
                          ×
                        </button>
                      </div>
                    ))}

                    <button
                      type="button"
                      className="add-photo-tile"
                      onClick={startCamera}
                    >
                      <ImagePlus size={22} />
                      <span>Add photo</span>
                    </button>
                  </div>
                </div>
              )}

              {photos.length === 0 && (
                <div className="empty-photo-state">
                  <Upload size={24} />
                  <strong>No photos captured yet</strong>
                  <span>
                    Add clear photos showing the property.
                  </span>
                </div>
              )}
            </>
          )}

          {step === 4 && (
            <>
              <div className="form-heading">
                <span className="form-icon">
                  <ShieldCheck size={19} />
                </span>

                <div>
                  <h3>Review &amp; verification</h3>
                  <p>
                    Review your listing before submitting it for
                    verification.
                  </p>
                </div>
              </div>

              <div className="review-banner">
                <div className="review-banner-icon">
                  <ShieldCheck size={22} />
                </div>

                <div>
                  <strong>Fully Verified owner</strong>
                  <p>
                    Your owner verification is required before a
                    property can be posted.
                  </p>
                </div>
              </div>

              <div className="review-section">
                <div className="review-section-header">
                  <strong>Listing summary</strong>

                  <button
                    type="button"
                    onClick={() => setStep(0)}
                  >
                    Edit
                  </button>
                </div>

                <div className="review-grid">
                  <div>
                    <span>Category</span>
                    <strong>
                      {form.category || "Not provided"}
                    </strong>
                  </div>

                  <div>
                    <span>Listing type</span>
                    <strong>
                      {form.listingType || "Not provided"}
                    </strong>
                  </div>

                  <div>
                    <span>Title</span>
                    <strong>
                      {form.title || "Not provided"}
                    </strong>
                  </div>

                  <div>
                    <span>Area</span>
                    <strong>
                      {form.area
                        ? `${form.area} sq.ft`
                        : "Not provided"}
                    </strong>
                  </div>

                  <div className="review-full">
                    <span>Address</span>
                    <strong>
                      {form.address || "Not provided"}
                    </strong>
                  </div>

                  <div className="review-full">
                    <span>Photos</span>
                    <strong>
                      {photos.length} captured photo
                      {photos.length !== 1 ? "s" : ""}
                    </strong>
                  </div>
                </div>
              </div>

              <div className="verification-checklist">
                <div className="verification-checklist-header">
                  <strong>Submission checklist</strong>
                </div>

                <div className="verification-item">
                  <span className="verification-check">
                    <Check size={14} />
                  </span>

                  <div>
                    <strong>Owner verification</strong>
                    <span>
                      Fully Verified status is required.
                    </span>
                  </div>
                </div>

                <div className="verification-item">
                  <span className="verification-check">
                    <Check size={14} />
                  </span>

                  <div>
                    <strong>Property information</strong>
                    <span>
                      Basic details and location have been
                      provided.
                    </span>
                  </div>
                </div>

                <div className="verification-item">
                  <span className="verification-check">
                    <Check size={14} />
                  </span>

                  <div>
                    <strong>Live property photos</strong>
                    <span>
                      Photos were captured through the camera.
                    </span>
                  </div>
                </div>

                <div className="verification-item">
                  <span className="verification-check">
                    <Check size={14} />
                  </span>

                  <div>
                    <strong>GPS location</strong>
                    <span>
                      Property location can be auto-tagged using
                      GPS.
                    </span>
                  </div>
                </div>
              </div>

              <div className="submission-note">
                <Info size={17} />

                <p>
                  By submitting, you confirm that the information
                  provided is accurate and truthful. The backend
                  verification process will determine when the
                  listing becomes available.
                </p>
              </div>
            </>
          )}

          <div className="form-navigation">
            <button
              type="button"
              className="secondary-navigation"
              onClick={handleBack}
            >
              <ChevronLeft size={18} />
              {step === 0 ? "Cancel" : "Back"}
            </button>

            {step < STEPS.length - 1 ? (
              <button
                type="button"
                className="primary-navigation"
                onClick={handleNext}
              >
                Continue
                <ChevronRight size={18} />
              </button>
            ) : (
              <button
                type="button"
                className="submit-navigation"
                onClick={handleSubmit}
                disabled={isSubmitting}
              >
                {isSubmitting ? (
                  <>
                    <span className="button-spinner" />
                    Submitting...
                  </>
                ) : (
                  <>
                    <Check size={18} strokeWidth={2.8} />
                    Submit Listing
                  </>
                )}
              </button>
            )}
          </div>
        </section>

        <footer className="add-property-footer">
          <ShieldCheck size={15} />
          <span>
            Property listings are subject to verification before
            publication.
          </span>
        </footer>
      </section>
    </main>
  );
}

export default AddProperty;