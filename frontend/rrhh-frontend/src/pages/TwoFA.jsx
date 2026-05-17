import { useState, useEffect, useRef } from "react";
import "../styles/TwoFA.css";

export default function TwoFA({ open, onClose, onSuccess }) {
  const [code, setCode] = useState(["", "", "", ""]);
  const [time, setTime] = useState(300);
  const [intentos, setIntentos] = useState(5);
  const [error, setError] = useState("");

  const intervalRef = useRef(null);
  const inputRefs = useRef([]);

  // ✅ Focus al primer input cuando se abre
  useEffect(() => {
    if (open) {
      inputRefs.current[0]?.focus();
    }
  }, [open]);

  // TIMER
  useEffect(() => {
    if (!open) return;

    intervalRef.current = setInterval(() => {
      setTime((prev) => {
        if (prev <= 1) {
          clearInterval(intervalRef.current);
          onClose();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(intervalRef.current);
  }, [open, onClose]);

  // CAMBIO DE INPUT + auto-next
  const handleChange = (value, index) => {
    if (!/^[0-9]?$/.test(value)) return;

    const newCode = [...code];
    newCode[index] = value;
    setCode(newCode);

    if (value && index < 3) {
      inputRefs.current[index + 1]?.focus();
    }
  };

  const verify = () => {
    const finalCode = code.join("");

    if (finalCode.length < 4) {
      setError("Ingresa el código completo");
      return;
    }

    if (finalCode === "1234") {
      onSuccess("FAKE_TOKEN");
      onClose();
    } else {
      const newIntentos = intentos - 1;
      setIntentos(newIntentos);
      setError("Código incorrecto");

      if (newIntentos <= 0) {
        setError("Bloqueado por demasiados intentos");
        onClose();
      }
    }
  };

  if (!open) return null;

  return (
    <div className="twofa-modal">
      <div className="twofa-content">
        <h2>Verificación 2FA</h2>
        <p className="twofa-subtitle">
          Ingresa el código de 4 dígitos enviado a tu dispositivo
        </p>

        <div className="twofa-code-box">
          {code.map((c, i) => (
            <input
              key={i}
              ref={(el) => (inputRefs.current[i] = el)}
              maxLength="1"
              value={c}
              onChange={(e) => handleChange(e.target.value, i)}
            />
          ))}
        </div>

        <p className="twofa-timer">
          ⏱ {Math.floor(time / 60)}:{String(time % 60).padStart(2, "0")}
        </p>

        <p className="twofa-attempts">
          Intentos restantes: <span>{intentos}</span>
        </p>

        {error && <p className="twofa-error">{error}</p>}

        <button
          onClick={verify}
          disabled={code.some((c) => c === "")}
          className="twofa-button"
        >
          Verificar
        </button>
      </div>
    </div>
  );
}