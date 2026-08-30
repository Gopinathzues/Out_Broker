import {
  BrowserRouter,
  Navigate,
  Route,
  Routes,
} from "react-router-dom";

import Home from "./pages/Home";
import Login from "./pages/login";
import Register from "./pages/Register";
import Verification from "./pages/Verification";
import PropertyDetails from "./pages/PropertyDetails";
import AddProperty from "./pages/AddProperty";

import ProtectedRoute from "./components/ProtectedRoute";

function App() {
  return (
    <BrowserRouter>
      <Routes>

        {/* =====================================================
            APPLICATION ENTRY
        ===================================================== */}

        <Route
          path="/"
          element={
            <Navigate
              to="/home"
              replace
            />
          }
        />

        {/* =====================================================
            PUBLIC AUTHENTICATION
        ===================================================== */}

        <Route
          path="/login"
          element={<Login />}
        />

        <Route
          path="/register"
          element={<Register />}
        />

        {/* =====================================================
            PUBLIC PROPERTY DISCOVERY
        ===================================================== */}

        <Route
          path="/home"
          element={<Home />}
        />

        <Route
          path="/properties/:id"
          element={
            <PropertyDetails />
          }
        />

        {/* =====================================================
            OWNER VERIFICATION
        =====================================================

            Requirements:

            - Authenticated
            - LANDLORD

            IMPORTANT:

            FULLY_VERIFIED is NOT required here.

            An already verified owner can still open
            this page and view their verification state.
        ===================================================== */}

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

        {/* =====================================================
            ADD PROPERTY
        =====================================================

            Requirements:

            1. Authenticated
            2. LANDLORD
            3. FULLY_VERIFIED
        ===================================================== */}

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

        {/* =====================================================
            LEGACY / MISTYPED CASING COMPATIBILITY
        =====================================================

            /verification is the ONLY canonical route.

            This redirect exists purely so that an old bookmark
            or a mistyped URL does not 404 or render a second,
            duplicate verification page. There is only ONE
            Verification component/page in this app.
        ===================================================== */}

        <Route
          path="/Verification"
          element={
            <Navigate
              to="/verification"
              replace
            />
          }
        />

        {/* =====================================================
            UNKNOWN ROUTES
        ===================================================== */}

        <Route
          path="*"
          element={
            <Navigate
              to="/home"
              replace
            />
          }
        />

      </Routes>
    </BrowserRouter>
  );
}

export default App;