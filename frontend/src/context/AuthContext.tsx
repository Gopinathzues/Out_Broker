import {
  createContext,
  useContext,
  useEffect,
  useState,
} from "react";
import type { ReactNode } from "react";

export type UserRole =
  | "TENANT"
  | "LANDLORD";

export type VerificationStatus =
  | "OTP_VERIFIED"
  | "DOCUMENT_VERIFIED"
  | "FULLY_VERIFIED"
  | "PENDING"
  | "REJECTED"
  | string;

export interface User {
  id?: number | string;
  name?: string;
  email?: string;
  phone?: string;
  role?: UserRole;
  verificationStatus?: VerificationStatus;
}

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;

  login: (
    token: string,
    user?: User
  ) => void;

  loginForDevelopment: (
    user: User
  ) => void;

  updateUser: (
    updates: Partial<User>
  ) => void;

  logout: () => void;
}

const AuthContext =
  createContext<AuthContextType | undefined>(
    undefined
  );

interface AuthProviderProps {
  children: ReactNode;
}

const TOKEN_KEY = "token";
const USER_KEY = "user";

export function AuthProvider({
  children,
}: AuthProviderProps) {
  const [token, setToken] =
    useState<string | null>(() =>
      localStorage.getItem(TOKEN_KEY)
    );

  const [user, setUser] =
    useState<User | null>(() => {
      const storedUser =
        localStorage.getItem(USER_KEY);

      if (!storedUser) {
        return null;
      }

      try {
        return JSON.parse(
          storedUser
        ) as User;
      } catch {
        localStorage.removeItem(USER_KEY);
        return null;
      }
    });

  const isAuthenticated =
    Boolean(token);

  /*
   * Keep authentication token synchronized
   * with localStorage.
   */
  useEffect(() => {
    if (token) {
      localStorage.setItem(
        TOKEN_KEY,
        token
      );
    } else {
      localStorage.removeItem(
        TOKEN_KEY
      );
    }
  }, [token]);

  /*
   * Keep user synchronized with localStorage.
   *
   * In production, the backend response will
   * become the source of truth for this object.
   */
  useEffect(() => {
    if (user) {
      localStorage.setItem(
        USER_KEY,
        JSON.stringify(user)
      );
    } else {
      localStorage.removeItem(
        USER_KEY
      );
    }
  }, [user]);

  /*
   * REAL LOGIN
   *
   * Backend will eventually provide:
   *
   * token
   * user
   * role
   * verificationStatus
   */
  const login = (
    newToken: string,
    newUser?: User
  ) => {
    setToken(newToken);
    setUser(newUser ?? null);
  };

  /*
   * DEVELOPMENT-ONLY LOGIN
   *
   * This allows us to test the frontend before
   * the real backend authentication is connected.
   *
   * This function is intentionally separate from
   * `login()` so the backend team can remove this
   * without changing the real authentication flow.
   */
  const loginForDevelopment = (
    developmentUser: User
  ) => {
    const developmentToken =
      `dev-token-${Date.now()}`;

    setToken(developmentToken);
    setUser(developmentUser);
  };

  /*
   * Update only the properties that changed.
   *
   * The verification flow uses this during
   * frontend-only development.
   *
   * Later the backend response can replace this.
   */
  const updateUser = (
    updates: Partial<User>
  ) => {
    setUser((currentUser) => {
      if (!currentUser) {
        return null;
      }

      return {
        ...currentUser,
        ...updates,
      };
    });
  };

  /*
   * Logout.
   */
  const logout = () => {
    setToken(null);
    setUser(null);

    localStorage.removeItem(
      TOKEN_KEY
    );

    localStorage.removeItem(
      USER_KEY
    );
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated,
        login,
        loginForDevelopment,
        updateUser,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextType {
  const context =
    useContext(AuthContext);

  if (!context) {
    throw new Error(
      "useAuth must be used inside AuthProvider"
    );
  }

  return context;
}