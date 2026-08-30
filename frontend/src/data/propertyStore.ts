// frontend/src/data/propertyStore.ts

export type PropertyCategory =
  | "PG"
  | "HOSTEL"
  | "ROOM"
  | "APARTMENT"
  | "HOUSE"
  | "VILLA";

export type ListingType =
  | "RENT"
  | "SALE"
  | "PG"
  | "HOSTEL";

export type PropertyStatus =
  | "PENDING_VERIFICATION"
  | "VERIFIED"
  | "REJECTED"
  | "DRAFT";

export type FurnishingType =
  | "FULLY_FURNISHED"
  | "SEMI_FURNISHED"
  | "UNFURNISHED";

export interface PropertyLocation {
  address?: string;
  area?: string;
  city?: string;
  pincode?: string;
  latitude?: number;
  longitude?: number;
}

export interface PropertyPricing {
  amount: number;
  securityDeposit?: number;
  maintenance?: number;
  priceLabel?: string;
}

export interface PropertyDetails {
  bedrooms?: number;
  bathrooms?: number;
  areaSqFt?: number;
  furnishing?: FurnishingType;
  floor?: number;
  totalFloors?: number;
  propertyAge?: number;
}

export interface Property {
  id: string;

  /*
   * Owner information.
   *
   * The backend can later replace this with
   * its own authenticated owner ID.
   */
  ownerId?: number | string;

  category: PropertyCategory;
  listingType: ListingType;

  title: string;
  description?: string;

  location: PropertyLocation;

  pricing: PropertyPricing;

  details: PropertyDetails;

  tenantTypes?: string[];

  amenities?: string[];

  /*
   * Temporary frontend representation.
   *
   * Later these can become uploaded image URLs
   * returned by the backend/cloud storage.
   */
  images: string[];

  /*
   * Verification / listing state.
   */
  status: PropertyStatus;

  verified: boolean;

  createdAt: string;
  updatedAt: string;
}

/*
 * ------------------------------------------------------------
 * STORAGE KEY
 * ------------------------------------------------------------
 *
 * Keep this key in one place.
 *
 * If the backend is connected later, this entire storage
 * implementation can be replaced without changing the
 * property shape used by the pages.
 */
const STORAGE_KEY =
  "outbroker_properties";

/*
 * ------------------------------------------------------------
 * DEMO PROPERTIES
 * ------------------------------------------------------------
 *
 * These allow Home.tsx to continue showing properties before
 * the backend is connected.
 *
 * Do NOT modify these from individual pages.
 */
const DEMO_PROPERTIES: Property[] = [
  {
    id: "demo-1",

    ownerId: "demo-owner-1",

    category: "APARTMENT",
    listingType: "RENT",

    title: "Modern 2 BHK Apartment",

    description:
      "A comfortable apartment in a convenient residential location.",

    location: {
      address:
        "Anna Nagar, Chennai",
      area: "Anna Nagar",
      city: "Chennai",
      pincode: "600040",
      latitude: 13.085,
      longitude: 80.210,
    },

    pricing: {
      amount: 28000,
      priceLabel:
        "₹28,000 / month",
    },

    details: {
      bedrooms: 2,
      bathrooms: 2,
      areaSqFt: 1100,
      furnishing:
        "SEMI_FURNISHED",
    },

    tenantTypes: [
      "FAMILY",
      "BACHELORS",
    ],

    amenities: [
      "Parking",
      "Lift",
      "Power Backup",
    ],

    images: [],

    status: "VERIFIED",

    verified: true,

    createdAt:
      new Date().toISOString(),

    updatedAt:
      new Date().toISOString(),
  },

  {
    id: "demo-2",

    ownerId: "demo-owner-2",

    category: "PG",
    listingType: "PG",

    title: "Comfortable PG for Working Professionals",

    description:
      "Well-connected PG accommodation with essential amenities.",

    location: {
      address:
        "T Nagar, Chennai",
      area: "T Nagar",
      city: "Chennai",
      pincode: "600017",
      latitude: 13.0418,
      longitude: 80.2341,
    },

    pricing: {
      amount: 9000,
      priceLabel:
        "₹9,000 / month",
    },

    details: {
      areaSqFt: 300,
      furnishing:
        "FULLY_FURNISHED",
    },

    tenantTypes: [
      "STUDENTS",
      "WORKING_PROFESSIONALS",
    ],

    amenities: [
      "Wi-Fi",
      "Food",
      "Laundry",
    ],

    images: [],

    status: "VERIFIED",

    verified: true,

    createdAt:
      new Date().toISOString(),

    updatedAt:
      new Date().toISOString(),
  },
];

/*
 * ------------------------------------------------------------
 * ID GENERATOR
 * ------------------------------------------------------------
 */

function generatePropertyId(): string {
  return `property-${Date.now()}-${Math.random()
    .toString(36)
    .slice(2, 8)}`;
}

/*
 * ------------------------------------------------------------
 * READ PROPERTIES
 * ------------------------------------------------------------
 */

export function getProperties(): Property[] {
  if (typeof window === "undefined") {
    return [...DEMO_PROPERTIES];
  }

  const stored =
    localStorage.getItem(
      STORAGE_KEY
    );

  /*
   * First run:
   *
   * Put the demo properties into storage.
   */
  if (!stored) {
    const initialProperties =
      [...DEMO_PROPERTIES];

    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify(
        initialProperties
      )
    );

    return initialProperties;
  }

  try {
    const parsed =
      JSON.parse(stored);

    if (!Array.isArray(parsed)) {
      return [...DEMO_PROPERTIES];
    }

    return parsed as Property[];
  } catch {
    /*
     * If localStorage somehow becomes
     * corrupted, don't crash the application.
     */
    return [...DEMO_PROPERTIES];
  }
}

/*
 * ------------------------------------------------------------
 * GET PROPERTY BY ID
 * ------------------------------------------------------------
 */

export function getPropertyById(
  id: string
): Property | null {
  const properties =
    getProperties();

  return (
    properties.find(
      (property) =>
        property.id === id
    ) ?? null
  );
}

/*
 * ------------------------------------------------------------
 * CREATE PROPERTY
 * ------------------------------------------------------------
 */

export function createProperty(
  propertyData: Omit<
    Property,
    | "id"
    | "createdAt"
    | "updatedAt"
  >
): Property {
  const now =
    new Date().toISOString();

  const newProperty: Property = {
    ...propertyData,

    id: generatePropertyId(),

    createdAt: now,

    updatedAt: now,
  };

  const properties =
    getProperties();

  const updatedProperties = [
    newProperty,
    ...properties,
  ];

  localStorage.setItem(
    STORAGE_KEY,
    JSON.stringify(
      updatedProperties
    )
  );

  return newProperty;
}

/*
 * ------------------------------------------------------------
 * UPDATE PROPERTY
 * ------------------------------------------------------------
 */

export function updateProperty(
  id: string,
  updates: Partial<
    Omit<
      Property,
      | "id"
      | "createdAt"
      | "updatedAt"
    >
  >
): Property | null {
  const properties =
    getProperties();

  const index =
    properties.findIndex(
      (property) =>
        property.id === id
    );

  if (index === -1) {
    return null;
  }

  const updatedProperty: Property =
    {
      ...properties[index],

      ...updates,

      updatedAt:
        new Date().toISOString(),
    };

  const updatedProperties =
    [...properties];

  updatedProperties[index] =
    updatedProperty;

  localStorage.setItem(
    STORAGE_KEY,
    JSON.stringify(
      updatedProperties
    )
  );

  return updatedProperty;
}

/*
 * ------------------------------------------------------------
 * DELETE PROPERTY
 * ------------------------------------------------------------
 */

export function deleteProperty(
  id: string
): boolean {
  const properties =
    getProperties();

  const filtered =
    properties.filter(
      (property) =>
        property.id !== id
    );

  if (
    filtered.length ===
    properties.length
  ) {
    return false;
  }

  localStorage.setItem(
    STORAGE_KEY,
    JSON.stringify(
      filtered
    )
  );

  return true;
}

/*
 * ------------------------------------------------------------
 * GET OWNER PROPERTIES
 * ------------------------------------------------------------
 */

export function getPropertiesByOwner(
  ownerId: number | string
): Property[] {
  return getProperties().filter(
    (property) =>
      String(
        property.ownerId
      ) === String(ownerId)
  );
}

/*
 * ------------------------------------------------------------
 * CLEAR PROTOTYPE DATA
 * ------------------------------------------------------------
 *
 * Useful during development/testing.
 *
 * This does NOT touch authentication.
 */
export function clearPropertyStore(): void {
  localStorage.removeItem(
    STORAGE_KEY
  );
} 