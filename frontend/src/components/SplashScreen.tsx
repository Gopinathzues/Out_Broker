import { useEffect } from "react";
import {
  Home as HomeIcon,
  ShieldCheck,
  MapPin,
  Users,
} from "lucide-react";
import "./SplashScreen.css";

interface SplashScreenProps {
  onComplete: () => void;
}

const SPLASH_DURATION_MS = 2400;

function SplashScreen({ onComplete }: SplashScreenProps) {
  useEffect(() => {
    const timer = window.setTimeout(() => {
      onComplete();
    }, SPLASH_DURATION_MS);

    return () => {
      window.clearTimeout(timer);
    };
  }, [onComplete]);

  return (
    <main
      className="splash-screen"
      role="status"
      aria-label="Loading OutBroker"
    >
      {/* Decorative background */}
      <div className="splash-orb splash-orb-one" aria-hidden="true" />
      <div className="splash-orb splash-orb-two" aria-hidden="true" />
      <div className="splash-grid" aria-hidden="true" />

      <div className="splash-content">
        {/* Brand mark — same mark used in the app header,
            with a verified badge to lead with trust. */}
        <div className="splash-mark-wrap">
          <div className="splash-mark">
            <HomeIcon size={34} strokeWidth={2.4} />
          </div>

          <div className="splash-mark-badge">
            <ShieldCheck size={15} strokeWidth={2.6} />
          </div>
        </div>

        {/* Wordmark */}
        <h1 className="splash-brand">
          <span className="splash-brand-out">Out</span>
          <span className="splash-brand-broker">Broker</span>
        </h1>

        <p className="splash-subtitle">
          Chennai&apos;s trusted property marketplace
        </p>

        {/* Trust strip — the product's core differentiator */}
        <ul className="splash-trust-strip">
          <li>
            <ShieldCheck size={14} strokeWidth={2.4} />
            <span>Verified owners</span>
          </li>

          <li>
            <Users size={14} strokeWidth={2.4} />
            <span>No brokerage</span>
          </li>

          <li>
            <MapPin size={14} strokeWidth={2.4} />
            <span>Local listings</span>
          </li>
        </ul>

        {/* Loading */}
        <div className="splash-progress">
          <span className="splash-progress-fill" />
        </div>

        <p className="splash-loading-text">Setting things up…</p>
      </div>

      <p className="splash-tagline">Safe. Simple. Direct.</p>
    </main>
  );
}

export default SplashScreen;
