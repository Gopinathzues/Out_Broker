import axiosClient from "./axiosClient";

export interface SendOtpRequest {
  phone: string;
}

export interface VerifyOtpRequest {
  phone: string;
  otp: string;
}

export type UserRole = "TENANT" | "LANDLORD" | "ADMIN";

export interface AuthUser {
  id?: number;
  name?: string;
  email?: string;
  phone?: string;
  role?: UserRole;
  verificationStatus?: string;
}

export interface SendOtpResponse {
  message?: string;
  success?: boolean;
}

export interface VerifyOtpResponse {
  token?: string;
  accessToken?: string;
  user?: AuthUser;
  message?: string;
}

export const authApi = {
  /**
   * Send OTP to mobile number.
   */
  sendOtp: (phone: string) =>
    axiosClient.post<SendOtpResponse>(
      "/auth/send-otp",
      {
        phone,
      } satisfies SendOtpRequest
    ),

  /**
   * Verify OTP.
   *
   * Backend may return either:
   * - token
   * - accessToken
   *
   * Both are supported by the frontend.
   */
  verifyOtp: (phone: string, otp: string) =>
    axiosClient.post<VerifyOtpResponse>(
      "/auth/verify-otp",
      {
        phone,
        otp,
      } satisfies VerifyOtpRequest
    ),

  /**
   * Get currently authenticated user.
   */
  getCurrentUser: () =>
    axiosClient.get<AuthUser>("/users/me"),
};