import {
  useEffect,
  useRef,
  useState,
} from "react";

import {
  AlertTriangle,
  Camera,
  RotateCcw,
  X,
} from "lucide-react";

import "./CameraCapture.css";

interface CameraCaptureProps {
  /** Data URL of the already-captured photo, if any. */
  value: string | null;

  /** Called with a JPEG data URL once the person confirms a shot. */
  onCapture: (dataUrl: string) => void;

  /** Which physical camera to prefer. */
  facingMode?: "user" | "environment";

  /** Short label shown on the trigger button, e.g. "Capture document". */
  triggerLabel: string;

  /** Accessible description of what's being captured. */
  description?: string;
}

/*
 * ============================================================
 * CAMERA CAPTURE
 * ============================================================
 *
 * Verification photos (identity document, selfie, ownership
 * proof) must come from a live camera shot taken inside the
 * app — never from the gallery or an existing file — so a
 * submitted document can't be a pre-edited or reused image.
 *
 * This intentionally never renders an <input type="file">.
 * It only ever talks to navigator.mediaDevices.getUserMedia,
 * captures a single frame to a canvas, and hands the resulting
 * data URL back to the caller.
 * ============================================================
 */

function CameraCapture({
  value,
  onCapture,
  facingMode = "environment",
  triggerLabel,
  description,
}: CameraCaptureProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [error, setError] = useState("");
  const [isStarting, setIsStarting] = useState(false);

  const videoRef = useRef<HTMLVideoElement | null>(null);
  const streamRef = useRef<MediaStream | null>(null);

  const stopStream = () => {
    streamRef.current?.getTracks().forEach((track) => track.stop());
    streamRef.current = null;
  };

  const openCamera = async () => {
    setError("");
    setIsStarting(true);

    try {
      if (
        !navigator.mediaDevices ||
        !navigator.mediaDevices.getUserMedia
      ) {
        throw new Error("unsupported");
      }

      const stream =
        await navigator.mediaDevices.getUserMedia({
          video: { facingMode },
          audio: false,
        });

      streamRef.current = stream;
      setIsOpen(true);

      /*
       * The <video> element only exists once isOpen is true,
       * so attach the stream on the next tick.
       */
      requestAnimationFrame(() => {
        if (videoRef.current) {
          videoRef.current.srcObject = stream;
        }
      });
    } catch {
      setError(
        "Camera access is required to capture this photo. Please allow camera permissions and try again."
      );
    } finally {
      setIsStarting(false);
    }
  };

  const closeCamera = () => {
    stopStream();
    setIsOpen(false);
  };

  const capturePhoto = () => {
    const video = videoRef.current;

    if (!video || video.videoWidth === 0) {
      setError("Camera isn't ready yet — give it a second and try again.");
      return;
    }

    const canvas = document.createElement("canvas");
    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;

    const context = canvas.getContext("2d");

    if (!context) {
      setError("Couldn't capture the photo. Please try again.");
      return;
    }

    context.drawImage(video, 0, 0, canvas.width, canvas.height);

    const dataUrl = canvas.toDataURL("image/jpeg", 0.9);

    onCapture(dataUrl);
    closeCamera();
  };

  const retake = () => {
    openCamera();
  };

  /* Clean up the camera stream if this unmounts while open. */
  useEffect(() => {
    return () => {
      stopStream();
    };
  }, []);

  /* ==========================================================
     ALREADY CAPTURED
  ========================================================== */

  if (value && !isOpen) {
    return (
      <div className="camera-capture captured">
        <img
          src={value}
          alt="Captured verification"
          className="camera-capture-preview"
        />

        <button
          type="button"
          className="camera-retake-button"
          onClick={retake}
        >
          <RotateCcw size={16} strokeWidth={2.2} />
          <span>Retake photo</span>
        </button>
      </div>
    );
  }

  /* ==========================================================
     LIVE CAMERA
  ========================================================== */

  if (isOpen) {
    return (
      <div className="camera-capture live">
        <div className="camera-video-wrapper">
          <video
            ref={videoRef}
            autoPlay
            playsInline
            muted
            className="camera-video"
          />
        </div>

        {error && (
          <div className="camera-error">
            <AlertTriangle size={15} strokeWidth={2.2} />
            <span>{error}</span>
          </div>
        )}

        <div className="camera-controls">
          <button
            type="button"
            className="camera-cancel-button"
            onClick={closeCamera}
            aria-label="Cancel"
          >
            <X size={18} strokeWidth={2.2} />
          </button>

          <button
            type="button"
            className="camera-shutter-button"
            onClick={capturePhoto}
            aria-label="Take photo"
          />

          <span className="camera-controls-spacer" />
        </div>
      </div>
    );
  }

  /* ==========================================================
     TRIGGER
  ========================================================== */

  return (
    <div className="camera-capture idle">
      <button
        type="button"
        className="camera-trigger-button"
        onClick={openCamera}
        disabled={isStarting}
      >
        <span className="camera-trigger-icon">
          <Camera size={19} strokeWidth={2.2} />
        </span>

        <span className="camera-trigger-text">
          <strong>
            {isStarting ? "Opening camera…" : triggerLabel}
          </strong>

          {description && <small>{description}</small>}
        </span>
      </button>

      {error && (
        <div className="camera-error">
          <AlertTriangle size={15} strokeWidth={2.2} />
          <span>{error}</span>
        </div>
      )}

      <p className="camera-capture-note">
        Live camera capture only — uploading from your gallery
        or files isn't supported for verification photos.
      </p>
    </div>
  );
}

export default CameraCapture;
