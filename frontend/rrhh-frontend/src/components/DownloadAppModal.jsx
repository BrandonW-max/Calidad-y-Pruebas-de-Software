import { useState } from "react";
import { Smartphone, Mail, CheckCircle2, AlertTriangle, Fingerprint } from "lucide-react";
import { enviarLinkApp } from "../services/api";

export default function DownloadAppModal({ idEmpleado, username, onClose }) {
  const STORAGE_KEY = `mobileAppPrompt_${username}`;
  const [enviando, setEnviando] = useState(false);
  const [enviado, setEnviado] = useState(false);
  const [error, setError] = useState("");

  const cerrar = () => {
    localStorage.setItem(STORAGE_KEY, "1");
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
    <div className="fixed inset-0 z-[9000] flex items-center justify-center bg-ink-950/60 p-6 backdrop-blur-md">
      <div className="w-full max-w-[420px] rounded-3xl border border-white/90 bg-white/95 p-9 text-center shadow-glass-lg backdrop-blur-xl">
        {!enviado ? (
          <>
            <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-rrhh-100 text-rrhh-600">
              <Smartphone size={30} strokeWidth={2} />
            </div>
            <h3 className="font-[var(--font-display)] text-[19px] font-bold text-ink-900">
              Registra tu asistencia desde tu celular
            </h3>
            <p className="mt-2.5 text-[13px] leading-relaxed text-ink-500">
              Descarga la app del Hospital San Gabriel y registra tu entrada y salida usando tu{" "}
              <strong className="font-semibold text-ink-700">huella dactilar</strong>, sin necesidad de ingresar al sistema web.
            </p>
            <p className="mt-2 flex items-center justify-center gap-1.5 text-[12px] text-ink-400">
              <Fingerprint size={14} />
              Te enviaremos el enlace de descarga a tu correo registrado.
            </p>

            {error && (
              <p className="mt-4 flex items-center justify-center gap-1.5 text-[13px] font-medium text-danger-600">
                <AlertTriangle size={14} /> {error}
              </p>
            )}

            <div className="mt-6 flex gap-3">
              <button
                onClick={cerrar}
                className="flex-1 cursor-pointer rounded-xl border border-ink-200 bg-ink-50 py-2.5 text-[14px] font-semibold text-ink-600 transition-colors hover:bg-ink-100"
              >
                Ahora no
              </button>
              <button
                onClick={handleEnviar}
                disabled={enviando}
                className="flex flex-1 cursor-pointer items-center justify-center gap-1.5 rounded-xl bg-gradient-to-br from-rrhh-500 to-rrhh-600 py-2.5 text-[14px] font-bold text-white shadow-glow-rrhh transition-transform hover:-translate-y-0.5 disabled:cursor-not-allowed disabled:opacity-60 disabled:hover:translate-y-0"
              >
                {enviando ? "Enviando…" : (<><Mail size={15} /> Enviar al correo</>)}
              </button>
            </div>
          </>
        ) : (
          <>
            <div className="mx-auto mb-3 flex h-16 w-16 items-center justify-center rounded-2xl bg-success-100 text-success-600">
              <CheckCircle2 size={30} strokeWidth={2} />
            </div>
            <h3 className="font-[var(--font-display)] text-[18px] font-bold text-success-600">¡Enlace enviado!</h3>
            <p className="mt-1.5 text-[13px] text-ink-500">Revisa tu correo para descargar la app.</p>
          </>
        )}
      </div>
    </div>
  );
}
