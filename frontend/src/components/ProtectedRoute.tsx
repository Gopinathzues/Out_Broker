import { ReactNode } from "react";
import {
  Navigate,
  useLocation,
} from "react-router-dom";
import { useAuth } from "../context/AuthContext";

interface ProtectedRouteProps {
  children: ReactNode;

  allowedRoles?: Array<
    "TENANT" | "LANDLORD"
  >;

  requireFullyVerified?: boolean;
}

function ProtectedRoute({
  children,
  allowedRoles,
  requireFullyVerified = false,
}: ProtectedRouteProps) {
  const {
    user,
    isAuthenticated,
  } = useAuth();

  const location =
    useLocation();

  /*
   * ---------------------------------------------------------
   * 1. NOT AUTHENTICATED
   * ---------------------------------------------------------
   *
   * Protected actions begin at registration.
   *
   * Example:
   *
   * Home
   *   ↓
   * Add Property
   *   ↓
   * Register
   */
  if (!isAuthenticated) {
    return (
      <Navigate
        to="/register"
        replace
        state={{
          from:
            location.pathname +
            location.search,
        }}
      />
    );
  }

  /*
   * ---------------------------------------------------------
   * 2. ROLE CHECK
   * ---------------------------------------------------------
   */
  if (
    allowedRoles &&
    (
      !user?.role ||
      !allowedRoles.includes(
        user.role
      )
    )
  ) {
    return (
      <Navigate
        to="/home"
        replace
      />
    );
  }

  /*
   * ---------------------------------------------------------
   * 3. VERIFICATION CHECK
   * ---------------------------------------------------------
   *
   * This protects the ACTION, not the verification page.
   *
   * Therefore:
   *
   * /verification
   *     → accessible to the owner
   *
   * /add-property
   *     → requires FULLY_VERIFIED
   */
  if (requireFullyVerified) {
    const status =
      user?.verificationStatus
        ?.toUpperCase();

    if (
      status !== "FULLY_VERIFIED"
    ) {
      return (
        <Navigate
          to="/verification"
          replace
          state={{
            from:
              location.pathname +
              location.search,
          }}
        />
      );
    }
  }

  return <>{children}</>;
}

export default ProtectedRoute;