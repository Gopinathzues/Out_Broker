import {
  ReactNode,
  useEffect,
  useState,
} from "react";

import Offline from "../pages/Offline";

interface ConnectionGuardProps {
  children: ReactNode;
}

function ConnectionGuard({
  children,
}: ConnectionGuardProps) {
  const [isOffline, setIsOffline] =
    useState(() => !navigator.onLine);

  useEffect(() => {
    const handleOffline = () => {
      setIsOffline(true);
    };

    const handleOnline = () => {
      setIsOffline(false);
    };

    window.addEventListener(
      "offline",
      handleOffline,
    );

    window.addEventListener(
      "online",
      handleOnline,
    );

    return () => {
      window.removeEventListener(
        "offline",
        handleOffline,
      );

      window.removeEventListener(
        "online",
        handleOnline,
      );
    };
  }, []);

  if (isOffline) {
    return <Offline />;
  }

  return <>{children}</>;
}

export default ConnectionGuard;