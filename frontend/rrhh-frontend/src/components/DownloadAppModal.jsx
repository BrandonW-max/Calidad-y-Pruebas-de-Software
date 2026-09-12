import { useState } from "react";
import { enviarLinkApp } from "../services/api";

export default function DownloadAppModal({ idEmpleado, username, onClose }) {
  const STORAGE_KEY = `mobileAppPrompt_${username}`;
  const [enviando, setEnviando] = useState(false);
  const [enviado,  setEnviado]  = useState(false);
  const [error,    setError]    = useState("");

  const cerrar = () => {
    localStorage.setItem(STORAGE_KEY, '1');
    onClose();
  };

  const handleEnviar = async () => {
    setEnviando(true);
    setError("");
    try {
      await enviarLinkApp(idEmpleado);
      setEnviado(true);
      setTimeout(cerrar, 1500);
    } catch (err) {
      setError(err?.message || "No se pudo enviar el correo.");
    } finally {
      setEnviando(false);
    }
  };

  return (
    <div style={{
      position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.55)',
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      zIndex: 9000, fontFamily: "'Poppins', system-ui, sans-serif",
    }}>
      <div style={{
        background: '#fff', borderRadius: 18, padding: '36px 32px',
        maxWidth: 420, width: '90%', textAlign: 'center',
        boxShadow: '0 20px 60px rgba(0,0,0,0.25)',
      }}>
        {!enviado ? (<>
          <div style={{ fontSize: 56, marginBottom: 8 }}>📱</div>
          <h3 style={{ margin: '0 0 10px', color: '#1e293b', fontSize: 19 }}>
            Registra tu asistencia desde tu celular
          </h3>
          <p style={{ color: '#475569', fontSize: 13, margin: '0 0 6px', lineHeight: 1.6 }}>
            Descarga la app del Hospital San Gabriel y registra
            tu entrada y salida usando tu <strong>huella dactilar</strong>,
            sin necesidad de ingresar al sistema web.
          </p>
          <p style={{ color: '#94a3b8', fontSize: 12, margin: '0 0 24px' }}>
            Te enviaremos el enlace de descarga a tu correo registrado.
          </p>
          {error && (
            <p style={{ color: '#ef4444', fontSize: 13, margin: '0 0 16px' }}>⚠️ {error}</p>
          )}
          <div style={{ display: 'flex', gap: 12, justifyContent: 'center' }}>
            <button
              onClick={cerrar}
              style={{
                flex: 1, padding: '11px 0', borderRadius: 10,
                border: '1px solid #cbd5e1', background: '#f8fafc',
                color: '#475569', cursor: 'pointer', fontSize: 14,
              }}
            >
              Ahora no
            </button>
            <button
              onClick={handleEnviar}
              disabled={enviando}
              style={{
                flex: 1, padding: '11px 0', borderRadius: 10,
                border: 'none', background: '#2563eb',
                color: '#fff', cursor: 'pointer', fontSize: 14,
                fontWeight: 600, boxShadow: '0 2px 8px rgba(37,99,235,0.3)',
              }}
            >
              {enviando ? 'Enviando...' : '📧 Enviar al correo'}
            </button>
          </div>
        </>) : (
          <>
            <div style={{ fontSize: 52, marginBottom: 12 }}>✅</div>
            <h3 style={{ margin: '0 0 8px', color: '#16a34a', fontSize: 18 }}>
              ¡Enlace enviado!
            </h3>
            <p style={{ color: '#475569', fontSize: 13, margin: 0 }}>
              Revisa tu correo para descargar la app.
            </p>
          </>
        )}
      </div>
    </div>
  );
}
