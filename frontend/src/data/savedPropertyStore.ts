/*
 * ============================================================
 * SAVED PROPERTY STORE
 * ============================================================
 *
 * Frontend-only saved/favorite implementation.
 *
 * IMPORTANT:
 * This is intentionally isolated from the main property store.
 *
 * Later, when the backend is connected, these functions can be
 * replaced with API calls without changing the Home or Saved UI.
 */

export interface SavedProperty {
  id: string | number;

  title: string;

  category: string;

  location: string;

  price?: number;

  priceLabel: string;

  bhk?: string;

  area: string;

  furnishing?: string;

  verified?: boolean;

  featured?: boolean;

  image: string;
}


const SAVED_PROPERTIES_KEY =
  "outbroker_saved_properties";


/*
 * ============================================================
 * GET SAVED PROPERTIES
 * ============================================================
 */

export function getSavedProperties(): SavedProperty[] {
  if (typeof window === "undefined") {
    return [];
  }

  try {
    const stored =
      localStorage.getItem(
        SAVED_PROPERTIES_KEY
      );

    if (!stored) {
      return [];
    }

    const parsed =
      JSON.parse(stored);

    if (!Array.isArray(parsed)) {
      return [];
    }

    return parsed as SavedProperty[];

  } catch (error) {
    console.error(
      "Unable to read saved properties:",
      error
    );

    return [];
  }
}


/*
 * ============================================================
 * CHECK SAVED STATE
 * ============================================================
 */

export function isPropertySaved(
  propertyId: string | number
): boolean {
  return getSavedProperties().some(
    (property) =>
      String(property.id) ===
      String(propertyId)
  );
}


/*
 * ============================================================
 * SAVE PROPERTY
 * ============================================================
 */

export function saveProperty(
  property: SavedProperty
): SavedProperty[] {

  const current =
    getSavedProperties();

  const alreadySaved =
    current.some(
      (item) =>
        String(item.id) ===
        String(property.id)
    );

  if (alreadySaved) {
    return current;
  }

  const updated = [
    property,
    ...current,
  ];

  localStorage.setItem(
    SAVED_PROPERTIES_KEY,
    JSON.stringify(updated)
  );

  return updated;
}


/*
 * ============================================================
 * REMOVE PROPERTY
 * ============================================================
 */

export function removeSavedProperty(
  propertyId: string | number
): SavedProperty[] {

  const current =
    getSavedProperties();

  const updated =
    current.filter(
      (property) =>
        String(property.id) !==
        String(propertyId)
    );

  localStorage.setItem(
    SAVED_PROPERTIES_KEY,
    JSON.stringify(updated)
  );

  return updated;
}


/*
 * ============================================================
 * TOGGLE PROPERTY
 * ============================================================
 */

export function toggleSavedProperty(
  property: SavedProperty
): SavedProperty[] {

  if (
    isPropertySaved(property.id)
  ) {
    return removeSavedProperty(
      property.id
    );
  }

  return saveProperty(property);
}


/*
 * ============================================================
 * CLEAR SAVED PROPERTIES
 * ============================================================
 *
 * Development/testing only.
 */

export function clearSavedProperties(): void {
  if (typeof window === "undefined") {
    return;
  }

  localStorage.removeItem(
    SAVED_PROPERTIES_KEY
  );
}