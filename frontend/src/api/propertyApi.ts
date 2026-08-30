import axiosClient from "./axiosClient";

export interface Property {
  id?: number;
  title?: string;
  description?: string;

  propertyType?: string;
  category?: string;

  city?: string;
  location?: string;
  address?: string;

  price?: number;
  rent?: number;
  monthlyRent?: number;

  bedrooms?: number;
  bathrooms?: number;
  area?: number;

  images?: string[];

  verified?: boolean;
  verificationStatus?: string;

  ownerId?: number;
  ownerName?: string;

  [key: string]: unknown;
}

export interface CreatePropertyRequest {
  [key: string]: unknown;
}

export interface UpdatePropertyRequest {
  [key: string]: unknown;
}

export const propertyApi = {
  /**
   * Public property discovery.
   */
  getAllAvailableProperties: () =>
    axiosClient.get<Property[]>("/properties"),

  /**
   * Get one property by ID.
   */
  getPropertyById: (id: number | string) =>
    axiosClient.get<Property>(`/properties/${id}`),

  /**
   * Search properties by city.
   */
  searchByCity: (city: string) =>
    axiosClient.get<Property[]>("/properties/search", {
      params: {
        city,
      },
    }),

  /**
   * Create a property.
   *
   * Intended for authenticated landlords.
   */
  createProperty: (data: CreatePropertyRequest) =>
    axiosClient.post<Property>("/properties", data),

  /**
   * Update an existing property.
   */
  updateProperty: (
    id: number | string,
    data: UpdatePropertyRequest
  ) =>
    axiosClient.put<Property>(
      `/properties/${id}`,
      data
    ),

  /**
   * Delete an existing property.
   */
  deleteProperty: (id: number | string) =>
    axiosClient.delete(
      `/properties/${id}`
    ),
};