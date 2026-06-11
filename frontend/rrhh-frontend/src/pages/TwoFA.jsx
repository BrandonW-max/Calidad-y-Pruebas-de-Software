import { useState, useEffect, useRef } from "react";
import "../styles/TwoFA.css";
import { verify2FA, saveSession } from "../services/api";

export default function TwoFA({ open, username, onClose, onSuccess }) {
  const [code, setCode] = useState(["", "", "", "", "", ""]);
  const [time, setTime] = useState(300);
  const [intentos, setIntentos] = useState(3);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const intervalRef = useRef(null);
  const inputRefs = useRef([]);

  useEffect(() => {
    if (open) inputRefs.current[0]?.focus();
  }, [open]);

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

  const handleChange = (value, index) => {
    if (!/^[0-9]?$/.test(value)) return;
    const newCode = [...code];
    newCode[index] = value;
    setCode(newCode);
    if (value && index < 5) inputRefs.current[index + 1]?.focus();
  };

  const handleKeyDown = (e, index) => {
    if (e.key === "Backspace" && !code[index] && index > 0) {
      inputRefs.current[index - 1]?.focus();
    }
  };

  const verify = async () => {
    const finalCode = code.join("");
    if (finalCode.length < 6) {
      setError("Ingresa el código completo de 6 dígitos");
      return;
    }

    setLoading(true);
    setError("");

    try {
      const data = await verify2FA(username, finalCode);

      if (data.success) {
        saveSession(data.token, data.rol, username);
        onSuccess(data.rol);
        onClose();
      } else {
        const nuevosIntentos = intentos - 1;
        setIntentos(nuevosIntentos);
        setError(data.message || "El token de sesión es incorrecto");
        setCode(["", "", "", "", "", ""]);
        inputRefs.current[0]?.focus();
        if (nuevosIntentos <= 0) {
          setError("Cuenta bloqueada por demasiados intentos");
          setTimeout(onClose, 2000);
        }
      }
    } catch {
      setError("Error de conexión con el servidor");
    } finally {
      setLoading(false);
    }
  };

  if (!open) return null;

  return (
    <div className="twofa-modal">
      <div className="twofa-content">
        <h2>Verificación 2FA</h2>
        <p className="twofa-subtitle">
          Ingresa el código de 6 dígitos de Google Authenticator
        </p>

        <div className="twofa-code-box">
          {code.map((c, i) => (
            <input
              key={i}
              ref={(el) => (inputRefs.current[i] = el)}
              maxLength="1"
              value={c}
              onChange={(e) => handleChange(e.target.value, i)}
              onKeyDown={(e) => handleKeyDown(e, i)}
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
          disabled={code.some((c) => c === "") || loading}
          className="twofa-button"
        >
          {loading ? "Verificando..." : "Verificar"}
        </button>
      </div>
    </div>
  );
}
