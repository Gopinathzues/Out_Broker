import { useState } from "react";

import {
  BrowserRouter,
  Navigate,
  Route,
  Routes,
} from "react-router-dom";


/* =========================================================
   COMPONENTS
========================================================= */

import SplashScreen from "./components/SplashScreen";
import ProtectedRoute from "./components/ProtectedRoute";


/* =========================================================
   PAGES
========================================================= */

import Home from "./pages/Home";
import Login from "./pages/login";
import Map from "./pages/Map";
import Register from "./pages/Register";
import Saved from "./pages/Saved";
import Verification from "./pages/Verification";
import PropertyDetails from "./pages/PropertyDetails";
import AddProperty from "./pages/AddProperty";
import MyAccount from "./pages/MyAccount";
import NotFound from "./pages/NotFound";


/* =========================================================
   AUTH
========================================================= */

import { useAuth } from "./context/AuthContext";


function App() {

  const [showSplash, setShowSplash] =
    useState(true);

  const { isAuthenticated } =
    useAuth();


  /* =======================================================
     SPLASH SCREEN
  ======================================================== */

  if (showSplash) {
    return (
      <SplashScreen
        onComplete={() =>
          setShowSplash(false)
        }
      />
    );
  }


  /* =======================================================
     APPLICATION ROUTES
  ======================================================== */

  return (
    <BrowserRouter>

      <Routes>


        {/* =================================================
            APPLICATION ENTRY
        ================================================== */}

        <Route
          path="/"
          element={
            <Navigate
              to={
                isAuthenticated
                  ? "/home"
                  : "/register"
              }
              replace
            />
          }
        />


        {/* =================================================
            PUBLIC AUTHENTICATION
        ================================================== */}

        <Route
          path="/register"
          element={
            <Register />
          }
        />

        <Route
          path="/login"
          element={
            <Login />
          }
        />


        {/* =================================================
            HOME

            Home remains publicly accessible.

            Guests can browse properties.
        ================================================== */}

        <Route
          path="/home"
          element={
            <Home />
          }
        />


        {/* =================================================
            MAP

            Map remains publicly accessible.
        ================================================== */}

        <Route
          path="/map"
          element={
            <Map />
          }
        />


        {/* =================================================
            SAVED

            Authentication required.
        ================================================== */}

        <Route
          path="/saved"
          element={
            isAuthenticated ? (
              <Saved />
            ) : (
              <Navigate
                to="/login"
                replace
              />
            )
          }
        />


        {/* =================================================
            MY ACCOUNT

            Authentication required.
        ================================================== */}

        <Route
          path="/account"
          element={
            isAuthenticated ? (
              <MyAccount />
            ) : (
              <Navigate
                to="/login"
                replace
              />
            )
          }
        />


        {/* =================================================
            PROPERTY DETAILS

            Publicly accessible.
        ================================================== */}

        <Route
          path="/properties/:id"
          element={
            <PropertyDetails />
          }
        />


        {/* =================================================
            OWNER VERIFICATION

            LANDLORD only.
        ================================================== */}

        <Route
          path="/verification"
          element={
            <ProtectedRoute
              allowedRoles={[
                "LANDLORD",
              ]}
            >
              <Verification />
            </ProtectedRoute>
          }
        />


        {/* =================================================
            ADD PROPERTY

            LANDLORD + FULLY VERIFIED.
        ================================================== */}

        <Route
          path="/add-property"
          element={
            <ProtectedRoute
              allowedRoles={[
                "LANDLORD",
              ]}
              requireFullyVerified
            >
              <AddProperty />
            </ProtectedRoute>
          }
        />


        {/* =================================================
            LEGACY VERIFICATION URL

            Supports both:

            /verification
            /Verification

            without maintaining two pages.
        ================================================== */}

        <Route
          path="/Verification"
          element={
            <Navigate
              to="/verification"
              replace
            />
          }
        />


        {/* =================================================
            404 / PAGE NOT FOUND

            Instead of silently redirecting to Home,
            show the proper frontend error state.
        ================================================== */}

        <Route
          path="*"
          element={
            <NotFound />
          }
        />


      </Routes>

    </BrowserRouter>
  );
}


export default App;