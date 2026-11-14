import { useState, useEffect } from "react";
import {
  Volume2,
  Play,
  Pause,
  Square,
  X
} from "lucide-react";
import {
  speakText,
  stopSpeech,
  pauseSpeech,
  resumeSpeech,
  isSpeechSupported,
  buildPayrollSpeechNarrative
} from "../utils/speech";

export default function PayrollAudioModal({ nomina, periodoLabel, isOpen, onClose }) {
  const [playing, setPlaying] = useState(false);
  const [paused, setPaused] = useState(false);
  const [rate, setRate] = useState(0.95);
  const supported = isSpeechSupported();

  useEffect(() => {
    // Al abrir el modal o cambiar de nómina, detener audio anterior
    return () => {
      stopSpeech();
    };
  }, [nomina]);

  if (!isOpen || !nomina) return null;

  const handlePlay = () => {
    if (paused) {
      resumeSpeech();
      setPaused(false);
      setPlaying(true);
      return;
    }

    const narrative = buildPayrollSpeechNarrative(nomina, periodoLabel);
    setPlaying(true);
    setPaused(false);

    speakText(narrative, {
      rate: rate,
      onStart: () => {
        setPlaying(true);
        setPaused(false);
      },
      onEnd: () => {
        setPlaying(false);
        setPaused(false);
      },
      onError: () => {
        setPlaying(false);
        setPaused(false);
      }
    });
  };

  const handlePause = () => {
    pauseSpeech();
    setPaused(true);
    setPlaying(false);
  };

  const handleStop = () => {
    stopSpeech();
    setPlaying(false);
    setPaused(false);
  };

  const handleClose = () => {
    stopSpeech();
    setPlaying(false);
    setPaused(false);
    onClose();
  };

  const fmtS = (v) =>
    v != null ? `S/ ${Number(v).toLocaleString("es-PE", { minimumFractionDigits: 2 })}` : "S/ 0.00";

  const sueldoBase = Number(nomina.sueldoBase ?? nomina.sueldo ?? 0);
  const bonifFam = Number(nomina.bonifFamiliar || 0);
  const bonifNoct = Number(nomina.bonifTurnoNocturno || 0);
  const bonifGuard = Number(nomina.bonifGuardia || 0);
  const bonifRiesgo = Number(nomina.bonifRiesgo || 0);
  const bonifCargo = Number(nomina.bonifCargo || 0);
  const totalBonif = bonifFam + bonifNoct + bonifGuard + bonifRiesgo + bonifCargo;

  const descTard = Number(nomina.descuentoTardanzas || 0);
  const descLey = Number(nomina.descuentoLey || 0);
  const totalDesc = descTard + descLey;

  const totalNeto = Number(nomina.sueldoNeto ?? Math.max(0, sueldoBase + totalBonif - totalDesc));
  const nombreEmpleado = nomina.nombreEmpleado || nomina.empleadoNombre || nomina.nombres || "Colaborador";

  return (
    <div
      className="tts-modal-backdrop"
      onClick={handleClose}
      role="dialog"
      aria-modal="true"
      aria-labelledby="tts-modal-title"
    >
      <div
        className="tts-modal-card"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Cabecera */}
        <div className="tts-modal-header">
          <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
            <div style={{
              width: 40,
              height: 40,
              borderRadius: 10,
              background: "rgba(255, 255, 255, 0.2)",
              display: "flex",
              alignItems: "center",
              justifyContent: "center"
            }}>
              <Volume2 size={24} color="#ffffff" />
            </div>
            <div>
              <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                <h2 id="tts-modal-title" style={{ margin: 0, fontSize: 17, fontWeight: 700, color: "#ffffff" }}>
                  Lectura Fácil y Asistente por Voz
                </h2>
                {playing && (
                  <span className="tts-pulse-badge">
                    <span className="tts-pulse-dot" />
                    Narrando...
                  </span>
                )}
              </div>
              <p style={{ margin: 0, fontSize: 12, color: "#e0f2fe" }}>
                Resumen accesible de boleta de pago para {nombreEmpleado}
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={handleClose}
            style={{
              background: "transparent",
              border: "none",
              color: "#ffffff",
              cursor: "pointer",
              padding: 4,
              borderRadius: 6,
            }}
            aria-label="Cerrar ventana de lectura fácil"
          >
            <X size={22} />
          </button>
        </div>

        {/* Barra de Controles de Audio */}
        <div style={{
          padding: "14px 20px",
          background: "#f8fafc",
          borderBottom: "1px solid #e2e8f0",
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          flexWrap: "wrap",
          gap: 12
        }}>
          <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
            {!playing ? (
              <button
                type="button"
                className="btn btn-primary"
                onClick={handlePlay}
                disabled={!supported}
                style={{
                  display: "inline-flex",
                  alignItems: "center",
                  gap: 6,
                  padding: "8px 16px",
                  borderRadius: 8,
                  fontWeight: 600,
                  fontSize: 14
                }}
              >
                <Play size={16} fill="currentColor" />
                <span>{paused ? "Reanudar Voz" : "Escuchar en Voz Alta"}</span>
              </button>
            ) : (
              <button
                type="button"
                className="btn btn-warning"
                onClick={handlePause}
                style={{
                  display: "inline-flex",
                  alignItems: "center",
                  gap: 6,
                  padding: "8px 16px",
                  borderRadius: 8,
                  fontWeight: 600,
                  fontSize: 14,
                  background: "#f59e0b",
                  color: "#ffffff",
                  border: "none"
                }}
              >
                <Pause size={16} />
                <span>Pausar</span>
              </button>
            )}

            {(playing || paused) && (
              <button
                type="button"
                className="btn btn-secondary"
                onClick={handleStop}
                style={{
                  display: "inline-flex",
                  alignItems: "center",
                  gap: 6,
                  padding: "8px 14px",
                  borderRadius: 8,
                  fontSize: 13
                }}
              >
                <Square size={14} fill="currentColor" />
                <span>Detener</span>
              </button>
            )}
          </div>

          {/* Selector de velocidad */}
          <div style={{ display: "flex", alignItems: "center", gap: 6, fontSize: 13, color: "#475569" }}>
            <span>Velocidad:</span>
            {[
              { label: "Pausada (0.8x)", val: 0.8 },
              { label: "Normal (1.0x)", val: 0.95 },
              { label: "Ágil (1.2x)", val: 1.2 }
            ].map(s => (
              <button
                key={s.val}
                type="button"
                onClick={() => setRate(s.val)}
                style={{
                  padding: "4px 8px",
                  borderRadius: 6,
                  border: rate === s.val ? "1.5px solid #2563eb" : "1px solid #cbd5e1",
                  background: rate === s.val ? "#eff6ff" : "#ffffff",
                  color: rate === s.val ? "#1d4ed8" : "#475569",
                  fontWeight: rate === s.val ? 700 : 500,
                  fontSize: 12,
                  cursor: "pointer"
                }}
              >
                {s.label.split(" ")[0]}
              </button>
            ))}
          </div>
        </div>

        {/* Cuerpo del Modal: Lectura Fácil (Plain Language ODS 10) */}
        <div style={{ padding: "20px", overflowY: "auto", flex: 1 }}>
          <div style={{
            background: "#eff6ff",
            borderLeft: "4px solid #3b82f6",
            padding: "10px 14px",
            borderRadius: "0 8px 8px 0",
            marginBottom: 16,
            fontSize: 13,
            color: "#1e3a8a"
          }}>
            <strong>📖 Modo Lectura Fácil:</strong> Explicación clara sin tecnicismos contables para que conozcas exactamente cómo se compone tu pago.
          </div>

          {/* 1. Ingresos */}
          <div className="tts-easyread-card income">
            <div style={{ fontSize: 24, lineHeight: 1 }}>🟢</div>
            <div style={{ flex: 1 }}>
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <h3 style={{ margin: 0, fontSize: 15, fontWeight: 700, color: "#166534" }}>
                  1. Tus Ingresos Ganados
                </h3>
                <span style={{ fontSize: 16, fontWeight: 800, color: "#166534" }}>
                  {fmtS(sueldoBase + totalBonif)}
                </span>
              </div>
              <p style={{ margin: "4px 0 8px", fontSize: 13, color: "#15803d" }}>
                Es el dinero ganado por tu trabajo durante el mes.
              </p>
              <div style={{ fontSize: 12, color: "#166534", display: "flex", flexDirection: "column", gap: 3 }}>
                <div>• Sueldo base acordado en contrato: <strong>{fmtS(sueldoBase)}</strong></div>
                {totalBonif > 0 ? (
                  <div>• Bonificaciones y beneficios adicionales: <strong>{fmtS(totalBonif)}</strong></div>
                ) : (
                  <div>• Sin bonificaciones extraordinarias este período.</div>
                )}
              </div>
            </div>
          </div>

          {/* 2. Descuentos */}
          <div className="tts-easyread-card deduction">
            <div style={{ fontSize: 24, lineHeight: 1 }}>🔴</div>
            <div style={{ flex: 1 }}>
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <h3 style={{ margin: 0, fontSize: 15, fontWeight: 700, color: "#991b1b" }}>
                  2. Descuentos Obligatorios
                </h3>
                <span style={{ fontSize: 16, fontWeight: 800, color: "#991b1b" }}>
                  — {fmtS(totalDesc)}
                </span>
              </div>
              <p style={{ margin: "4px 0 8px", fontSize: 13, color: "#b91c1c" }}>
                Retenciones obligatorias según las leyes peruanas y registro de asistencia.
              </p>
              <div style={{ fontSize: 12, color: "#991b1b", display: "flex", flexDirection: "column", gap: 3 }}>
                <div>• Fondo de pensión y salud por ley (AFP / ONP): <strong>{fmtS(descLey)}</strong></div>
                {descTard > 0 ? (
                  <div>• Minutos de tardanza registrados: <strong>{fmtS(descTard)}</strong></div>
                ) : (
                  <div>• ¡Excelente! No tuviste descuentos por tardanza.</div>
                )}
              </div>
            </div>
          </div>

          {/* 3. Dinero Neto */}
          <div className="tts-easyread-card net">
            <div style={{ fontSize: 28, lineHeight: 1 }}>💰</div>
            <div style={{ flex: 1 }}>
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <div>
                  <h3 style={{ margin: 0, fontSize: 16, fontWeight: 800, color: "#1e40af" }}>
                    3. Sueldo Neto a Recibir
                  </h3>
                  <span style={{ fontSize: 12, color: "#2563eb", fontWeight: 600 }}>
                    Monto final que ingresará directamente a tu cuenta bancaria
                  </span>
                </div>
                <span style={{ fontSize: 22, fontWeight: 900, color: "#1d4ed8" }}>
                  {fmtS(totalNeto)}
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Pie del modal */}
        <div style={{
          padding: "14px 20px",
          background: "#f8fafc",
          borderTop: "1px solid #e2e8f0",
          display: "flex",
          justifyContent: "flex-end"
        }}>
          <button
            type="button"
            className="btn btn-secondary"
            onClick={handleClose}
            style={{ padding: "8px 18px", borderRadius: 8, fontWeight: 600, fontSize: 13 }}
          >
            Cerrar
          </button>
        </div>
      </div>
    </div>
  );
}
