import {
  Home,
  RefreshCw,
  WifiOff,
} from "lucide-react";
import { useNavigate } from "react-router-dom";
import "./SystemState.css";

function Offline() {
  const navigate = useNavigate();

  const handleRetry = () => {
    window.location.reload();
  };

  return (
    <main className="system-state-page">
      <div className="system-state-orb system-state-orb-left" />
      <div className="system-state-orb system-state-orb-right" />

      <section className="system-state-card">

        <div className="system-state-icon offline">
          <WifiOff size={34} strokeWidth={1.8} />
        </div>

        <span className="system-state-label">
          CONNECTION LOST
        </span>

        <h1>
          No internet connection
        </h1>

        <p>
          It looks like you're offline.
          Check your internet connection
          and try again.
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

        <div className="system-state-status">
          <span className="system-state-dot" />
          Waiting for connection...
        </div>

      </section>
    </main>
  );
}

export default Offline;