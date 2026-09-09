import { StrictMode } from "react";
import { createRoot } from "react-dom/client";

import "./index.css";

import App from "./App.tsx";

import { AuthProvider } from "./context/AuthContext";

import AppErrorBoundary from "./components/AppErrorBoundary";
import ConnectionGuard from "./components/ConnectionGuard";


createRoot(
  document.getElementById("root")!
).render(
  <StrictMode>
    <AuthProvider>

      <AppErrorBoundary>

        <ConnectionGuard>

          <App />

        </ConnectionGuard>

      </AppErrorBoundary>

    </AuthProvider>
  </StrictMode>
);