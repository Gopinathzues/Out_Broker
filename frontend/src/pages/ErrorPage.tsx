import {
  Home,
  RefreshCw,
  TriangleAlert,
} from "lucide-react";
import { useNavigate } from "react-router-dom";
import "./SystemState.css";

interface ErrorPageProps {
  onRetry?: () => void;
}

function ErrorPage({
  onRetry,
}: ErrorPageProps) {
  const navigate = useNavigate();

  const handleRetry = () => {
    if (onRetry) {
      onRetry();
      return;
    }

    window.location.reload();
  };

  return (
    <main className="system-state-page">
      <div className="system-state-orb system-state-orb-left" />
      <div className="system-state-orb system-state-orb-right" />

      <section className="system-state-card">

        <div className="system-state-icon error">
          <TriangleAlert
            size={34}
            strokeWidth={1.8}
          />
        </div>

        <span className="system-state-label">
          SOMETHING WENT WRONG
        </span>

        <h1>
          We couldn't load this page
        </h1>

        <p>
          Something unexpected happened.
          Please try again. If the problem
          continues, return to the home page.
        </p>

        <div className="system-state-actions">

          <button
            type="button"
            className="system-state-primary"
            onClick={handleRetry}
          >
            <RefreshCw size={17} />
            Try again
          </button>

          <button
            type="button"
            className="system-state-secondary"
            onClick={() =>
              navigate("/home")
            }
          >
            <Home size={17} />
            Go to Home
          </button>

        </div>

      </section>
    </main>
  );
}

export default ErrorPage;