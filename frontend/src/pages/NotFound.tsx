import {
  ArrowLeft,
  Home,
  SearchX,
} from "lucide-react";
import { useNavigate } from "react-router-dom";
import "./SystemState.css";

function NotFound() {
  const navigate = useNavigate();

  return (
    <main className="system-state-page">
      <div className="system-state-orb system-state-orb-left" />
      <div className="system-state-orb system-state-orb-right" />

      <section className="system-state-card">

        <div className="system-state-icon not-found">
          <SearchX
            size={34}
            strokeWidth={1.8}
          />
        </div>

        <span className="system-state-label">
          404
        </span>

        <h1>
          Page not found
        </h1>

        <p>
          The page you're looking for
          doesn't exist or may have
          been moved.
        </p>

        <div className="system-state-actions">

          <button
            type="button"
            className="system-state-primary"
            onClick={() =>
              navigate("/home")
            }
          >
            <Home size={17} />
            Go to Home
          </button>

          <button
            type="button"
            className="system-state-secondary"
            onClick={() =>
              navigate(-1)
            }
          >
            <ArrowLeft size={17} />
            Go Back
          </button>

        </div>

      </section>
    </main>
  );
}

export default NotFound;