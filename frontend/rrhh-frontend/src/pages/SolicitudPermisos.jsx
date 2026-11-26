import { useState, useEffect } from "react";
import { useLocation } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import AccessibleDialog from "../components/AccessibleDialog";
import "../styles/Dashboard.css";
import {
  registrarSolicitud,
  getSolicitudesPorEmpleado,
  getSolicitudesPorEstado,
  revisarSolicitudRRHH,
  decidirSolicitudGerencia,
  getIdEmpleado,
} from "../services/api";

const STEP_COLORS = ["#ef4444", "#f97316", "#eab308", "#22c55e"];

const ESTADO_FLOW_STEP = {
  PENDIENTE:   0,
  OBSERVADO:   0,
  EN_REVISION: 1,
  PROCESADO:   2,
  APROBADO:    3,
  APROBADA:    3,
  RECHAZADO:   3,
  RECHAZADA:   3,
};

function FlowBar({ estadoSolicitud }) {
  const steps = ["EMPLEADO", "RRHH", "GERENCIA", "RESULTADO"];
  const activeUpTo = estadoSolicitud != null ? (ESTADO_FLOW_STEP[estadoSolicitud] ?? -1) : -1;
  const hasSelection = estadoSolicitud != null;

  return (
    <div className="sol-flow-bar">
      {steps.map((s, i) => {
        const baseColor = STEP_COLORS[i];
        const color = (hasSelection && i === 3 && estadoSolicitud === "RECHAZADO") ? "#ef4444" : baseColor;
        const lit = hasSelection && i <= activeUpTo;
        return (
          <div key={s} className="sol-flow-step">
            <div
              className="sol-flow-dot"
              style={{
                background: lit ? color : "#f8fafc",
                color:      lit ? "#fff" : color,
                border:     `2px solid ${color}`,
                fontWeight: 700,
                opacity:    hasSelection && !lit ? 0.35 : 1,
                transition: "all 0.3s",
              }}
            >
              {i + 1}
            </div>
            <span style={{
              color:      lit ? color : "var(--text-3)",
              fontWeight: hasSelection && i === activeUpTo ? 700 : 600,
              opacity:    hasSelection && !lit ? 0.35 : 1,
              transition: "all 0.3s",
            }}>
              {s}
            </span>
            {i < steps.length - 1 && (
              <div className="sol-flow-line" style={{
                background: hasSelection && i < activeUpTo ? "#22c55e" : "var(--border)",
                transition: "background 0.3s",
              }} />
            )}
          </div>
        );
      })}
    </div>
  );
}

const ESTADO_TAB_CONFIG = {
  PENDIENTE: { label: "Pendiente",   icon: "⏳", color: "#d97706", bg: "#fef3c7", activeBg: "#f59e0b" },
  PROCESADO: { label: "En Revisión", icon: "🔍", color: "#2563eb", bg: "#dbeafe", activeBg: "#3b82f6" },
  APROBADA:  { label: "Aprobado",    icon: "✅", color: "#16a34a", bg: "#dcfce7", activeBg: "#22c55e" },
  RECHAZADA: { label: "Rechazado",   icon: "❌", color: "#dc2626", bg: "#fee2e2", activeBg: "#ef4444" },
};

const TIPO_LABELS = {
  VACACIONES:       "Vacaciones",
  PERMISO_PERSONAL: "Permiso Personal",
  PERMISO_MEDICO:   "Permiso Médico",
  OTROS:            "Otros",
};

const estadoBadge = (estado) => {
  const m = {
    PENDIENTE:   "badge-sol-pendiente",
    OBSERVADO:   "badge-sol-pendiente",
    EN_REVISION: "badge-sol-gerencia",
    PROCESADO:   "badge-sol-gerencia",
    APROBADO:    "badge-sol-aprobada",
    APROBADA:    "badge-sol-aprobada",
    RECHAZADO:   "badge-sol-rechazada",
    RECHAZADA:   "badge-sol-rechazada",
  };
  return m[estado] ?? "badge-gray";
};

/* ══════════════════════════════════════════════════════════
   VISTA EMPLEADO
══════════════════════════════════════════════════════════ */
/* ── Detalle de la solicitud seleccionada ── */
function DetalleSolicitud({ solicitud, onCerrar }) {
  if (!solicitud) return null;
  const s = solicitud;
  const filas = [
    ["Empleado", s.nombreEmpleado ?? s.idEmpleado ?? "—"],
    ["Tipo", TIPO_LABELS[s.tipoSolicitud] ?? s.tipoSolicitud ?? "—"],
    ["Fecha inicio", s.fechaInicio ?? "—"],
    ["Fecha fin", s.fechaFin ?? "—"],
    ["Días", s.diasSolicitados ?? "—"],
    ["Estado", s.estado ?? "—"],
    ["Motivo", s.motivo || "—"],
    ["Respuesta", s.respuesta || "—"],
    ["Observación RRHH", s.observacionRrhh || "—"],
    ["Fecha de revisión", s.fechaRevision ? String(s.fechaRevision).substring(0, 10) : "—"],
    ["Fecha de decisión", s.fechaDecision ? String(s.fechaDecision).substring(0, 10) : "—"],
  ];
  return (
    <AccessibleDialog className="modal-overlay" labelledBy="solicitud-detalle-title" onClose={onCerrar}>
      <div className="modal-box" style={{ maxWidth: 560, width: "95%", borderRadius: 16, overflow: "hidden", padding: 0 }}>
        <div className="modal-header" style={{ padding: "20px 24px 16px", borderBottom: "1px solid var(--border)" }}>
          <h3 id="solicitud-detalle-title" style={{ margin: 0, fontSize: 16 }}>Detalle de la solicitud #{s.idSolicitud ?? s.id}</h3>
          <button type="button" className="modal-close" aria-label="Cerrar detalle de solicitud" onClick={onCerrar}>×</button>
        </div>
        <div className="modal-body" style={{ padding: "20px 24px" }}>
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(200px, 1fr))", gap: 14 }}>
            {filas.map(([label, valor]) => (
              <div key={label} style={{ minWidth: 0 }}>
                <div style={{ fontSize: 12, color: "var(--text-3)", fontWeight: 600, textTransform: "uppercase", letterSpacing: ".04em" }}>{label}</div>
                <div style={{ fontSize: 14, color: "var(--text-1)", wordBreak: "break-word" }}>{valor}</div>
              </div>
            ))}
          </div>
        </div>
        <div className="modal-footer" style={{ padding: "14px 24px", borderTop: "1px solid var(--border)" }}>
          <button className="btn btn-secondary" onClick={onCerrar}>Cerrar</button>
        </div>
      </div>
    </AccessibleDialog>
  );
}

function VistaEmpleado() {
  const idEmpleado = getIdEmpleado();
  const [solicitudes, setSolicitudes] = useState([]);
  const [loading,  setLoading]  = useState(false);
  const [mensaje,  setMensaje]  = useState("");
  const [error,    setError]    = useState("");
  const [enviando, setEnviando] = useState(false);
  const [tabActiva, setTabActiva] = useState("lista");
  const [solicitudActiva, setSolicitudActiva] = useState(null);
  const [detalleAbierto, setDetalleAbierto] = useState(false);
  const [form, setForm] = useState({
    tipo: "VACACIONES", fechaInicio: "", fechaFin: "", descripcion: "",
  });
  const [errorModal, setErrorModal] = useState({ open: false, message: "" });
  const closeErrorModal = () => setErrorModal({ open: false, message: "" });
  const errorFecha = "La fecha de inicio no puede ser mayor que la fecha fin.";

  useEffect(() => { if (idEmpleado) cargarMisSolicitudes(); }, [idEmpleado]); // eslint-disable-line react-hooks/exhaustive-deps

  async function cargarMisSolicitudes() {
    setLoading(true);
    setError("");
    try {
      const data = await getSolicitudesPorEmpleado(idEmpleado);
      setSolicitudes(Array.isArray(data) ? data : []);
    } catch { setError("No se pudieron cargar tus solicitudes."); }
    finally  { setLoading(false); }
  }

  const handleEnviar = async (e) => {
    e.preventDefault();
    if (!idEmpleado) { setError("No se pudo obtener tu ID de empleado. Contacta a RRHH."); return; }
    if (form.fechaInicio && form.fechaFin && form.fechaInicio > form.fechaFin) {
      setError(errorFecha);
      return;
    }
    setEnviando(true); setMensaje(""); setError("");
    try {
      await registrarSolicitud({ tipoSolicitud: form.tipo, fechaInicio: form.fechaInicio, fechaFin: form.fechaFin, motivo: form.descripcion });
      setMensaje("Solicitud enviada correctamente. RRHH la revisará pronto.");
      setForm({ tipo: "VACACIONES", fechaInicio: "", fechaFin: "", descripcion: "" });
      setTabActiva("lista");
      cargarMisSolicitudes();
    } catch (e) { setErrorModal({ open: true, message: e.message || "Error al enviar la solicitud. Intente de nuevo." }); }
    finally  { setEnviando(false); }
  };

  return (
    <div className="dashboard rrhh-accessible">
      <Sidebar rol="EMPLEADO" />
      <main className="main-content rrhh-accessible" id="main-content" tabIndex="-1">
        <header className="main-header header-empleado">
          <div className="header-left">
            <span className="page-breadcrumb">Dashboard / Mis Solicitudes</span>
            <h1>Solicitud de Permisos y Licencias</h1>
          </div>
          <span className="role-pill role-pill-empleado">Empleado</span>
        </header>

        {mensaje && <div className="alert alert-success" role="status" aria-live="polite">✅ {mensaje}</div>}
        {error && <div className="alert alert-warning" role="alert" id={error === errorFecha ? "solicitud-fecha-error" : undefined}>⚠️ {error}</div>}

        <FlowBar estadoSolicitud={solicitudActiva?.estado} />

        <div className="tab-bar">
          <button className={`tab ${tabActiva === "lista" ? "active" : ""}`}
            onClick={() => { setTabActiva("lista"); cargarMisSolicitudes(); }}>
            📋 Mis Solicitudes
          </button>
          <button className={`tab ${tabActiva === "nueva" ? "active" : ""}`}
            onClick={() => setTabActiva("nueva")}>
            ➕ Nueva Solicitud
          </button>
        </div>

        {tabActiva === "lista" && (
          <div className="section-card">
            <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 12, flexWrap: "wrap", gap: 8 }}>
              <h2 style={{ margin: 0 }}>Mis Solicitudes</h2>
              <div className="alert alert-info" style={{ margin: 0, padding: "6px 14px", fontSize: 12, display: "flex", alignItems: "center", gap: 8 }}>
                ⏰ ¿Llegaste tarde? Justifica tu tardanza en
                <a href="/empleado/asistencia" style={{ color: "var(--empleado)", fontWeight: 600, marginLeft: 4 }}>Mi Asistencia →</a>
              </div>
            </div>
            <div className="table-wrapper">
              {loading ? <div className="loading-text">Cargando solicitudes...</div> : (
                <table>
                  <thead>
                    <tr><th style={{ textAlign: "center" }}>Tipo</th><th style={{ textAlign: "center" }}>Desde</th><th style={{ textAlign: "center" }}>Hasta</th><th style={{ textAlign: "center" }}>Motivo</th><th style={{ textAlign: "center" }}>Estado</th><th style={{ textAlign: "center" }}>Respuesta</th></tr>
                  </thead>
                  <tbody>
                    {solicitudes.length === 0 ? (
                      <tr><td colSpan={6}><div className="empty-state"><div className="empty-state-icon">📋</div><p>No tienes solicitudes registradas</p></div></td></tr>
                    ) : solicitudes.map(s => {
                      const id = s.idSolicitud ?? s.id;
                      const isSelected = solicitudActiva?.idSolicitud === id || solicitudActiva?.id === id;
                      const respuestaVisible =
                        (s.estado === "OBSERVADO" || s.estado === "OBSERVADA")
                          ? s.observacionRrhh
                          : (s.estado === "APROBADA" || s.estado === "APROBADO" || s.estado === "RECHAZADA" || s.estado === "RECHAZADO")
                            ? s.respuesta
                            : null;
                      return (
                        <tr key={id}
                          style={{ background: isSelected ? "#eff6ff" : undefined, outline: isSelected ? "2px solid #3b82f6" : undefined }}>
                          <td style={{ textAlign: "center" }}>
                            <button type="button" className="table-row-action"
                              aria-label={`Ver detalle de solicitud ${id}`}
                              onClick={() => { setSolicitudActiva(s); setDetalleAbierto(true); }}>
                              {TIPO_LABELS[s.tipoSolicitud] ?? s.tipoSolicitud}
                            </button>
                          </td>
                          <td style={{ textAlign: "center" }}>{s.fechaInicio ?? "—"}</td>
                          <td style={{ textAlign: "center" }}>{s.fechaFin ?? "—"}</td>
                          <td style={{ textAlign: "center" }}>{s.motivo ?? "—"}</td>
                          <td style={{ textAlign: "center" }}><span className={`badge ${estadoBadge(s.estado)}`}>{s.estado}</span></td>
                          <td style={{ textAlign: "center" }}>{respuestaVisible ?? <span style={{ color: "var(--text-3)" }}>—</span>}</td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        )}

        {tabActiva === "nueva" && (
          <div className="sol-emp-layout">
            <div className="section-card">
              <h2>➕ Nueva Solicitud de Permiso</h2>
              <div className="alert alert-info" style={{ marginBottom: 16 }}>
                Las solicitudes pasan por revisión de RRHH y luego Gerencia (según el tipo).
              </div>
              <form onSubmit={handleEnviar}>
                <div className="form-grid">
                  <div className="form-group">
                    <label htmlFor="solicitud-tipo">Tipo de Solicitud *</label>
                    <select id="solicitud-tipo" value={form.tipo} onChange={e => setForm({ ...form, tipo: e.target.value })} required>
                      {Object.entries(TIPO_LABELS).map(([v, l]) => <option key={v} value={v}>{l}</option>)}
                    </select>
                  </div>
                  <div className="form-group" />
                  <div className="form-group">
                    <label htmlFor="solicitud-fecha-inicio">Fecha de Inicio *</label>
                    <input id="solicitud-fecha-inicio" type="date" value={form.fechaInicio}
                      aria-invalid={error === errorFecha || undefined}
                      aria-describedby={error === errorFecha ? "solicitud-fecha-error" : undefined}
                      onChange={e => { setForm({ ...form, fechaInicio: e.target.value }); if (error === errorFecha) setError(""); }} required />
                  </div>
                  <div className="form-group">
                    <label htmlFor="solicitud-fecha-fin">Fecha de Fin *</label>
                    <input id="solicitud-fecha-fin" type="date" value={form.fechaFin}
                      aria-invalid={error === errorFecha || undefined}
                      aria-describedby={error === errorFecha ? "solicitud-fecha-error" : undefined}
                      onChange={e => { setForm({ ...form, fechaFin: e.target.value }); if (error === errorFecha) setError(""); }} required />
                  </div>
                  <div className="form-group form-full">
                    <label htmlFor="solicitud-descripcion">Motivo / Descripción *</label>
                    <textarea id="solicitud-descripcion" rows={4} value={form.descripcion}
                      onChange={e => setForm({ ...form, descripcion: e.target.value })}
                      placeholder="Describa el motivo de su solicitud de permiso..." required />
                  </div>
                </div>
                <div className="form-actions">
                  <button type="submit" className="btn btn-primary" disabled={enviando}>
                    {enviando ? "Enviando..." : "📤 Enviar Solicitud"}
                  </button>
                  <button type="button" className="btn btn-secondary"
                    onClick={() => setForm({ tipo: "VACACIONES", fechaInicio: "", fechaFin: "", descripcion: "" })}>
                    Limpiar
                  </button>
                </div>
              </form>
            </div>

            <div className="notif-panel">
              <div className="section-card">
                <h2>⚡ ¿Cómo funciona?</h2>
                <ol style={{ margin: 0, paddingLeft: "18px", fontSize: "13px", color: "var(--text-2)", lineHeight: "2" }}>
                  <li>Completa el formulario con los datos de tu permiso.</li>
                  <li>RRHH revisa y valida la información.</li>
                  <li>Si requiere aprobación de Gerencia, pasa al siguiente nivel.</li>
                  <li>Recibes la notificación con la decisión final.</li>
                </ol>
                <div className="alert alert-warning" style={{ marginTop: 12 }}>
                  <strong>Nota:</strong> Las vacaciones deben solicitarse con al menos 5 días de anticipación.
                </div>
              </div>
            </div>
          </div>
        )}
        <DetalleSolicitud solicitud={detalleAbierto ? solicitudActiva : null} onCerrar={() => setDetalleAbierto(false)} />
      </main>

      {/* ══ MODAL: ERROR ══ */}
      {errorModal.open && (
        <AccessibleDialog className="modal-overlay" labelledBy="empleado-solicitud-error-title" describedBy="empleado-solicitud-error-message" onClose={closeErrorModal}>
          <div className="modal-box" style={{ maxWidth: 420 }}>
            <div className="modal-header">
              <div><h3 id="empleado-solicitud-error-title">⚠️ Error de validación</h3></div>
              <button type="button" className="modal-close" aria-label="Cerrar error" onClick={closeErrorModal}>×</button>
            </div>
            <div className="modal-body">
              <p id="empleado-solicitud-error-message" style={{ fontSize: 14, color: "var(--text-1)", margin: 0 }}>{errorModal.message}</p>
            </div>
            <div className="modal-footer">
              <button type="button" className="btn btn-primary" onClick={closeErrorModal}>Entendido</button>
            </div>
          </div>
        </AccessibleDialog>
      )}
    </div>
  );
}

/* ══════════════════════════════════════════════════════════
   VISTA RRHH
══════════════════════════════════════════════════════════ */
function VistaRRHH() {
  const [todasSolicitudes, setTodasSolicitudes] = useState([]);
  const [loading,          setLoading]          = useState(false);
  const [mensaje,          setMensaje]          = useState("");
  const [error,            setError]            = useState("");
  const [filtroEstado,     setFiltroEstado]     = useState("PENDIENTE");
  const [solicitudActiva,  setSolicitudActiva]  = useState(null);
  const [detalleAbierto, setDetalleAbierto] = useState(false);
  const [modalRevision,    setModalRevision]    = useState({ open: false, solicitud: null, decision: "", comentario: "" });
  const [procesando,       setProcesando]       = useState(false);
  const [errorModal, setErrorModal] = useState({ open: false, message: "" });
  const closeErrorModal = () => setErrorModal({ open: false, message: "" });

  // Derivado: solo las del filtro activo (para la tabla)
  // "PENDIENTE" incluye también OBSERVADO (devueltas), que se revisan de nuevo
  const solicitudes = todasSolicitudes.filter(s =>
    filtroEstado === "PENDIENTE" ? (s.estado === "PENDIENTE" || s.estado === "OBSERVADO") : s.estado === filtroEstado
  );

  useEffect(() => { cargarSolicitudes(); }, []);

  async function cargarSolicitudes() {
    setLoading(true); setError("");
    try {
      const [pend, obs, enRev, apro, rech] = await Promise.all([
        getSolicitudesPorEstado("PENDIENTE"),
        getSolicitudesPorEstado("OBSERVADO"),
        getSolicitudesPorEstado("PROCESADO"),
        getSolicitudesPorEstado("APROBADA"),
        getSolicitudesPorEstado("RECHAZADA"),
      ]);
      setTodasSolicitudes([
        ...(Array.isArray(pend)  ? pend  : []),
        ...(Array.isArray(obs)   ? obs   : []),
        ...(Array.isArray(enRev) ? enRev : []),
        ...(Array.isArray(apro)  ? apro  : []),
        ...(Array.isArray(rech)  ? rech  : []),
      ]);
    } catch { setError("No se pudieron cargar las solicitudes"); }
    finally  { setLoading(false); }
  }

  const contarPorEstado = (e) => todasSolicitudes.filter(s => s.estado === e).length;

  const handleRevisar = async () => {
    if (!modalRevision.decision || !modalRevision.comentario.trim()) return;
    setProcesando(true);
    try {
      await revisarSolicitudRRHH(modalRevision.solicitud.idSolicitud ?? modalRevision.solicitud.id, {
        decision: modalRevision.decision, comentario: modalRevision.comentario,
      });
      setMensaje(`Solicitud ${modalRevision.decision === "APROBAR" ? "aprobada" : "devuelta"} correctamente`);
      setModalRevision({ open: false, solicitud: null, decision: "", comentario: "" });
      cargarSolicitudes();
    } catch (err) {
      setErrorModal({ open: true, message: err.message || "Error al procesar la revisión" });
      setModalRevision({ open: false, solicitud: null, decision: "", comentario: "" });
    } finally { setProcesando(false); }
  };

  const stats = [
    { label: "Pendientes",  value: contarPorEstado("PENDIENTE") + contarPorEstado("OBSERVADO"),  color: "#f59e0b" },
    { label: "En revisión", value: contarPorEstado("PROCESADO"),  color: "#3b82f6" },
    { label: "Aprobadas",   value: contarPorEstado("APROBADA"),   color: "#10b981" },
    { label: "Rechazadas",  value: contarPorEstado("RECHAZADA"),  color: "#ef4444" },
  ];

  return (
    <div className="dashboard rrhh-accessible">
      <Sidebar rol="RRHH" />
      <main className="main-content rrhh-accessible" id="main-content" tabIndex="-1">
        <header className="main-header header-rrhh">
          <div className="header-left">
            <span className="page-breadcrumb">RRHH / Solicitud de Permisos</span>
            <h1>Solicitud de Permisos y Licencias</h1>
          </div>
          <span className="role-pill role-pill-rrhh">RRHH</span>
        </header>

        {mensaje && <div className="alert alert-success" role="status" aria-live="polite">✅ {mensaje}</div>}
        {error && <div className="alert alert-warning" role="alert">⚠️ {error}</div>}

        <FlowBar estadoSolicitud={solicitudActiva?.estado} />

        <div className="emp-stats-grid" style={{ marginBottom: 24 }}>
          {stats.map(s => (
            <div key={s.label} className="rrhh-stat-card" style={{ borderLeft: `4px solid ${s.color}` }}>
              <div className="rrhh-stat-label">{s.label}</div>
              <div className="rrhh-stat-value" style={{ color: s.color }}>{s.value}</div>
            </div>
          ))}
        </div>

        <div className="section-card" style={{ padding: 0, overflow: "hidden" }}>
          <div style={{ padding: "16px 20px", borderBottom: "1px solid var(--border)" }}>
            <div style={{ display: "flex", gap: "8px", flexWrap: "wrap" }}>
              {["PENDIENTE", "PROCESADO", "APROBADA", "RECHAZADA"].map(e => {
                const cfg = ESTADO_TAB_CONFIG[e];
                const isActive = filtroEstado === e;
                return (
                  <button key={e} onClick={() => { setFiltroEstado(e); setSolicitudActiva(null); }} style={{
                    padding: "7px 16px",
                    borderRadius: "20px",
                    border: `1.5px solid ${cfg.activeBg}`,
                    background: isActive ? cfg.activeBg : cfg.bg,
                    color: isActive ? "#fff" : cfg.color,
                    fontWeight: 600,
                    fontSize: 12,
                    cursor: "pointer",
                    fontFamily: "inherit",
                    transition: "all 0.18s",
                    display: "flex",
                    alignItems: "center",
                    gap: 5,
                  }}>
                    {cfg.icon} {cfg.label}
                  </button>
                );
              })}
              <button className="btn btn-secondary btn-sm" style={{ marginLeft: "auto" }}
                onClick={cargarSolicitudes}>🔄 Actualizar</button>
            </div>
          </div>
          <div className="table-wrapper" style={{ marginBottom: 0 }}>
            {loading ? <div className="loading-text">Cargando solicitudes...</div> : (
              <table>
                <thead>
                  <tr><th style={{ textAlign: "center" }}>Empleado</th><th style={{ textAlign: "center" }}>Tipo</th><th style={{ textAlign: "center" }}>Inicio</th><th style={{ textAlign: "center" }}>Fin</th><th style={{ textAlign: "center" }}>Motivo</th><th style={{ textAlign: "center" }}>Estado</th>{filtroEstado === "PENDIENTE" && <th style={{ textAlign: "center" }}>Acciones</th>}</tr>
                </thead>
                <tbody>
                  {solicitudes.length === 0 ? (
                    <tr><td colSpan={filtroEstado === "PENDIENTE" ? 7 : 6}><div className="empty-state"><div className="empty-state-icon">✅</div><p>No hay solicitudes en estado {filtroEstado}</p></div></td></tr>
                  ) : solicitudes.map(s => {
                    const id = s.idSolicitud ?? s.id;
                    const isSelected = solicitudActiva?.idSolicitud === id || solicitudActiva?.id === id;
                    return (
                       <tr key={id}
                         style={{ background: isSelected ? "#eff6ff" : undefined, outline: isSelected ? "2px solid #3b82f6" : undefined }}>
                         <td style={{ textAlign: "center" }}>
                           {filtroEstado !== "PENDIENTE" ? (
                             <button type="button" className="table-row-action"
                               aria-label={`Ver detalle de solicitud ${id} de ${s.nombreEmpleado ?? `empleado ${s.idEmpleado}`}`}
                               onClick={() => { setSolicitudActiva(s); setDetalleAbierto(true); }}>
                               {s.nombreEmpleado ?? s.idEmpleado}
                             </button>
                           ) : (s.nombreEmpleado ?? s.idEmpleado)}
                         </td>
                        <td style={{ textAlign: "center" }}>{TIPO_LABELS[s.tipoSolicitud] ?? s.tipoSolicitud}</td>
                        <td style={{ textAlign: "center" }}>{s.fechaInicio ?? "—"}</td>
                        <td style={{ textAlign: "center" }}>{s.fechaFin ?? "—"}</td>
                        <td style={{ textAlign: "center", maxWidth: "200px", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{s.motivo}</td>
                        <td style={{ textAlign: "center" }}><span className={`badge ${estadoBadge(s.estado)}`}>{s.estado}</span></td>
                        {filtroEstado === "PENDIENTE" && (
                          <td style={{ textAlign: "center" }}>
                            {(s.estado === "PENDIENTE" || s.estado === "OBSERVADO") && (
                               <button type="button" className="btn btn-primary btn-sm"
                                 onClick={() => setModalRevision({ open: true, solicitud: s, decision: "", comentario: "" })}>
                                Revisar
                              </button>
                            )}
                          </td>
                        )}
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            )}
          </div>
        </div>
        <DetalleSolicitud solicitud={detalleAbierto ? solicitudActiva : null} onCerrar={() => setDetalleAbierto(false)} />
      </main>

      {modalRevision.open && (
        <AccessibleDialog className="modal-overlay" labelledBy="rrhh-revision-title" onClose={() => setModalRevision({ ...modalRevision, open: false })}>
          <div className="modal-box" style={{ maxWidth: 480, width: "95%", borderRadius: 16, overflow: "hidden", boxShadow: "0 20px 60px rgba(0,0,0,0.18)", padding: 0 }}>

            {/* Header */}
            <div className="modal-header" style={{ padding: "20px 24px 16px", borderBottom: "1px solid var(--border)", background: "var(--bg)" }}>
              <div style={{ display: "flex", alignItems: "flex-start", gap: 12 }}>
                <div style={{ width: 40, height: 40, borderRadius: 10, background: "linear-gradient(135deg,#6366f1,#8b5cf6)", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 18, flexShrink: 0 }}>
                  📋
                </div>
                <div>
                  <h3 id="rrhh-revision-title" style={{ margin: 0, fontSize: 16, fontWeight: 700, color: "var(--text-1)", lineHeight: 1.3 }}>
                    Revisar Solicitud #{modalRevision.solicitud?.idSolicitud ?? modalRevision.solicitud?.id}
                  </h3>
                  <p style={{ margin: "3px 0 0", fontSize: 13, color: "var(--text-2)", fontWeight: 500 }}>
                    {TIPO_LABELS[modalRevision.solicitud?.tipoSolicitud] ?? modalRevision.solicitud?.tipoSolicitud}
                    <span style={{ margin: "0 6px", opacity: 0.4 }}>·</span>
                    {modalRevision.solicitud?.nombreEmpleado ?? modalRevision.solicitud?.idEmpleado}
                  </p>
                </div>
              </div>
              <button type="button" className="modal-close" aria-label="Cerrar revisión de solicitud" onClick={() => setModalRevision({ ...modalRevision, open: false })}
                style={{ width: 32, height: 32, borderRadius: 8, border: "none", background: "var(--bg-2)", color: "var(--text-2)", fontSize: 18, cursor: "pointer", display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0, transition: "background 0.15s" }}
                onMouseOver={e => e.currentTarget.style.background = "var(--border)"}
                onMouseOut={e => e.currentTarget.style.background = "var(--bg-2)"}>
                ×
              </button>
            </div>

            {/* Body */}
            <div className="modal-body" style={{ padding: "20px 24px", display: "flex", flexDirection: "column", gap: 16 }}>

              {/* Motivo — solo lectura */}
              <div className="form-group">
                  <div role="group" aria-labelledby="rrhh-revision-motivo-label" className="form-group">
                  <div id="rrhh-revision-motivo-label" className="form-label-like">Motivo del empleado</div>
                  <div style={{ padding: "10px 13px", background: "var(--bg-2,#f8fafc)", border: "1.5px solid var(--border)", borderRadius: "var(--radius-sm,8px)", fontSize: 13.5, color: "var(--text)", lineHeight: 1.6, whiteSpace: "pre-wrap", minHeight: 44 }}>
                  {modalRevision.solicitud?.motivo ?? "—"}
                </div>
                  </div>
              </div>

              {/* Decisión */}
              <div className="form-group">
                  <label htmlFor="rrhh-revision-decision">Decisión <span aria-hidden="true" style={{ color: "#ef4444" }}>*</span></label>
                  <select id="rrhh-revision-decision" required value={modalRevision.decision}
                  onChange={e => setModalRevision({ ...modalRevision, decision: e.target.value })}>
                  <option value="">Seleccione una opción...</option>
                  <option value="APROBAR">✅ Aprobar y pasar a Gerencia</option>
                  <option value="DEVOLVER">↩ Devolver al empleado</option>
                </select>
                  <div role="status" aria-live="polite">
                  {modalRevision.decision === "APROBAR" && (
                  <p style={{ margin: "4px 0 0", fontSize: 12, color: "#16a34a" }}>
                    ✓ La solicitud pasará a revisión de Gerencia
                  </p>
                  )}
                  </div>
                {modalRevision.decision === "DEVOLVER" && (
                  <p style={{ margin: "4px 0 0", fontSize: 12, color: "#d97706" }}>
                    ↩ El empleado podrá corregir y reenviar la solicitud
                  </p>
                )}
              </div>

              {/* Comentario */}
              <div className="form-group">
                  <label htmlFor="rrhh-revision-comentario">Comentario <span aria-hidden="true" style={{ color: "#ef4444" }}>*</span></label>
                  <textarea id="rrhh-revision-comentario" required aria-describedby="rrhh-revision-comentario-count" rows={3} value={modalRevision.comentario}
                  onChange={e => setModalRevision({ ...modalRevision, comentario: e.target.value })}
                  placeholder="Ingrese su observación o motivo de decisión..." />
                  <p id="rrhh-revision-comentario-count" style={{ margin: "4px 0 0", fontSize: 11, color: "var(--text-2)", textAlign: "right" }}>
                  {modalRevision.comentario.trim().length} caracteres
                </p>
              </div>
            </div>

            {/* Footer */}
            <div className="modal-footer" style={{ padding: "16px 24px", borderTop: "1px solid var(--border)", background: "var(--bg-2)", display: "flex", justifyContent: "flex-end", gap: 10 }}>
              <button className="btn btn-secondary"
                onClick={() => setModalRevision({ ...modalRevision, open: false })}
                style={{ padding: "9px 18px", borderRadius: 10, fontSize: 14, fontWeight: 600 }}>
                Cancelar
              </button>
              <button className="btn btn-primary" onClick={handleRevisar}
                disabled={!modalRevision.decision || !modalRevision.comentario.trim() || procesando}
                style={{
                  padding: "9px 20px", borderRadius: 10, fontSize: 14, fontWeight: 600,
                  opacity: (!modalRevision.decision || !modalRevision.comentario.trim() || procesando) ? 0.5 : 1,
                  cursor: (!modalRevision.decision || !modalRevision.comentario.trim() || procesando) ? "not-allowed" : "pointer",
                  transition: "opacity 0.2s"
                }}>
                {procesando ? (
                  <span style={{ display: "flex", alignItems: "center", gap: 8 }}>
                    <span style={{ width: 14, height: 14, border: "2px solid rgba(255,255,255,0.4)", borderTopColor: "#fff", borderRadius: "50%", display: "inline-block", animation: "spin 0.7s linear infinite" }} />
                    Procesando...
                  </span>
                ) : "Confirmar Revisión"}
              </button>
            </div>
          </div>
        </AccessibleDialog>
      )}

      {/* ══ MODAL: ERROR ══ */}
      {errorModal.open && (
        <AccessibleDialog className="modal-overlay" labelledBy="rrhh-solicitud-error-title" describedBy="rrhh-solicitud-error-message" onClose={closeErrorModal}>
          <div className="modal-box" style={{ maxWidth: 420 }}>
            <div className="modal-header">
              <div><h3 id="rrhh-solicitud-error-title">⚠️ Error de validación</h3></div>
              <button type="button" className="modal-close" aria-label="Cerrar error" onClick={closeErrorModal}>×</button>
            </div>
            <div className="modal-body">
              <p id="rrhh-solicitud-error-message" style={{ fontSize: 14, color: "var(--text-1)", margin: 0 }}>{errorModal.message}</p>
            </div>
            <div className="modal-footer">
              <button type="button" className="btn btn-primary" onClick={closeErrorModal}>Entendido</button>
            </div>
          </div>
        </AccessibleDialog>
      )}
    </div>
  );
}

/* ══════════════════════════════════════════════════════════
   VISTA GERENCIA
══════════════════════════════════════════════════════════ */
function VistaGerencia() {
  const [solicitudes,    setSolicitudes]    = useState([]);
  const [solAprobadas,   setSolAprobadas]   = useState([]);
  const [solRechazadas,  setSolRechazadas]  = useState([]);
  const [loading,        setLoading]        = useState(false);
  const [mensaje,        setMensaje]        = useState("");
  const [error,          setError]          = useState("");
  const [solicitudActiva,  setSolicitudActiva]  = useState(null);
  const [detalleAbierto, setDetalleAbierto] = useState(false);
  const [modalDecision,    setModalDecision]    = useState({ open: false, solicitud: null, decision: "", comentario: "" });
  const [procesando,       setProcesando]       = useState(false);
  const [errorModal, setErrorModal] = useState({ open: false, message: "" });
  const closeErrorModal = () => setErrorModal({ open: false, message: "" });

  useEffect(() => { cargarSolicitudes(); }, []);

  async function cargarSolicitudes() {
    setLoading(true); setError("");
    try {
      const [enRev, aprob, recha] = await Promise.all([
        getSolicitudesPorEstado("PROCESADO"),
        getSolicitudesPorEstado("APROBADA"),
        getSolicitudesPorEstado("RECHAZADA"),
      ]);
      setSolicitudes(Array.isArray(enRev) ? enRev : []);
      setSolAprobadas(Array.isArray(aprob) ? aprob : []);
      setSolRechazadas(Array.isArray(recha) ? recha : []);
    } catch { setError("No se pudieron cargar las solicitudes"); }
    finally  { setLoading(false); }
  }

  const handleDecidir = async () => {
    if (!modalDecision.decision || !modalDecision.comentario.trim()) return;
    setProcesando(true);
    try {
      await decidirSolicitudGerencia(
        modalDecision.solicitud.idSolicitud ?? modalDecision.solicitud.id,
        { decision: modalDecision.decision, comentario: modalDecision.comentario }
      );
      setMensaje(`Solicitud ${modalDecision.decision === "APROBAR" ? "aprobada" : "rechazada"} correctamente`);
      setModalDecision({ open: false, solicitud: null, decision: "", comentario: "" });
      cargarSolicitudes();
    } catch (err) {
      setErrorModal({ open: true, message: err.message || "Error al procesar la decisión" });
      setModalDecision({ open: false, solicitud: null, decision: "", comentario: "" });
    } finally { setProcesando(false); }
  };

  return (
    <div className="dashboard rrhh-accessible">
      <Sidebar rol="GERENCIA" />

      <main className="main-content rrhh-accessible" id="main-content" tabIndex="-1">
        <header className="main-header header-gerencia">
          <div className="header-left">
            <span className="page-breadcrumb">Gerencia / Solicitudes de Permisos</span>
            <h1>Decisión Final — Solicitudes de Permisos</h1>
          </div>
          <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
            <button className="btn btn-secondary" onClick={cargarSolicitudes}>🔄 Actualizar</button>
            <span className="role-pill role-pill-gerencia">Gestión Ejecutiva</span>
          </div>
        </header>

        {mensaje && <div className="alert alert-success" role="status" aria-live="polite" style={{ marginTop: 16 }}>✅ {mensaje}</div>}
        {error && <div className="alert alert-warning" role="alert" style={{ marginTop: 16 }}>⚠️ {error}</div>}

        <FlowBar estadoSolicitud={solicitudActiva?.estado} />

        {/* ── Tabla: En Revisión (decisión pendiente) ── */}
        <div style={{ marginTop: 16 }}>
          <h3 style={{ margin: "0 0 10px", fontSize: 15, color: "#f97316", display: "flex", alignItems: "center", gap: 8 }}>
            <span style={{ width: 10, height: 10, borderRadius: "50%", background: "#f97316", display: "inline-block" }} />
            Esperando Decisión ({solicitudes.length})
          </h3>
          <div className="section-card" style={{ padding: 0, overflow: "hidden" }}>
            <div className="table-wrapper" style={{ marginBottom: 0 }}>
              {loading ? <div className="loading-text">Cargando solicitudes...</div> : (
                <table>
                  <thead>
                    <tr><th style={{ textAlign: "center" }}>Empleado</th><th style={{ textAlign: "center" }}>Tipo</th><th style={{ textAlign: "center" }}>Inicio</th><th style={{ textAlign: "center" }}>Fin</th><th style={{ textAlign: "center" }}>Motivo</th><th style={{ textAlign: "center" }}>Rev. RRHH</th><th style={{ textAlign: "center" }}>Estado</th><th style={{ textAlign: "center" }}>Decisión</th></tr>
                  </thead>
                  <tbody>
                    {solicitudes.length === 0 ? (
                      <tr><td colSpan={8}><div className="empty-state"><div className="empty-state-icon">✅</div><p>No hay solicitudes pendientes de decisión</p></div></td></tr>
                    ) : solicitudes.map(s => {
                      const id = s.idSolicitud ?? s.id;
                      const isSelected = solicitudActiva?.idSolicitud === id || solicitudActiva?.id === id;
                      return (
                        <tr key={id}
                          style={{ background: isSelected ? "#eff6ff" : undefined, outline: isSelected ? "2px solid #3b82f6" : undefined }}>
                          <td style={{ textAlign: "center" }}>{s.nombreEmpleado ?? s.idEmpleado}</td>
                          <td style={{ textAlign: "center" }}>{TIPO_LABELS[s.tipoSolicitud] ?? s.tipoSolicitud}</td>
                          <td style={{ textAlign: "center" }}>{s.fechaInicio ?? "—"}</td>
                          <td style={{ textAlign: "center" }}>{s.fechaFin ?? "—"}</td>
                          <td style={{ textAlign: "center", maxWidth: "160px", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{s.motivo}</td>
                          <td style={{ textAlign: "center", fontSize: "12px", color: "var(--text-2)" }}>{s.observacionRrhh ?? "—"}</td>
                          <td style={{ textAlign: "center" }}><span className={`badge ${estadoBadge(s.estado)}`}>{s.estado}</span></td>
                          <td style={{ textAlign: "center" }}>
                            <div style={{ display: "flex", gap: "4px", justifyContent: "center" }}>
                               <button type="button" className="btn btn-success btn-sm"
                                 onClick={() => setModalDecision({ open: true, solicitud: s, decision: "APROBAR", comentario: "" })}>
                                ✅ Aprobar
                              </button>
                               <button type="button" className="btn btn-danger btn-sm"
                                 onClick={() => setModalDecision({ open: true, solicitud: s, decision: "RECHAZAR", comentario: "" })}>
                                ❌ Rechazar
                              </button>
                            </div>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        </div>

        {/* ── Tabla: Aprobadas ── */}
        <div style={{ marginTop: 24 }}>
          <h3 style={{ margin: "0 0 10px", fontSize: 15, color: "#22c55e", display: "flex", alignItems: "center", gap: 8 }}>
            <span style={{ width: 10, height: 10, borderRadius: "50%", background: "#22c55e", display: "inline-block" }} />
            Solicitudes Aprobadas ({solAprobadas.length})
          </h3>
          <div className="section-card" style={{ padding: 0, overflow: "hidden" }}>
            <div className="table-wrapper" style={{ marginBottom: 0 }}>
              {loading ? (
                <div className="loading-text">Actualizando solicitudes aprobadas...</div>
              ) : solAprobadas.length === 0 ? (
                <div className="empty-state">
                  <div className="empty-state-icon">📋</div>
                  <p>No hay solicitudes aprobadas todavía</p>
                </div>
              ) : (
                <table>
                  <thead>
                    <tr><th style={{ textAlign: "center" }}>Empleado</th><th style={{ textAlign: "center" }}>Tipo</th><th style={{ textAlign: "center" }}>Inicio</th><th style={{ textAlign: "center" }}>Fin</th><th style={{ textAlign: "center" }}>Días</th><th style={{ textAlign: "center" }}>Respuesta Gerencia</th></tr>
                  </thead>
                  <tbody>
                    {solAprobadas.map(s => {
                      const id = s.idSolicitud ?? s.id;
                      const isSelected = solicitudActiva?.idSolicitud === id || solicitudActiva?.id === id;
                      return (
                        <tr key={id}
                          style={{ background: isSelected ? "#eff6ff" : undefined, outline: isSelected ? "2px solid #3b82f6" : undefined }}>
                          <td style={{ textAlign: "center" }}>
                            <button type="button" className="table-row-action"
                              aria-label={`Ver detalle de solicitud ${id} de ${s.nombreEmpleado ?? `empleado ${s.idEmpleado}`}`}
                              onClick={() => { setSolicitudActiva(s); setDetalleAbierto(true); }}>
                              {s.nombreEmpleado ?? s.idEmpleado}
                            </button>
                          </td>
                          <td style={{ textAlign: "center" }}>{TIPO_LABELS[s.tipoSolicitud] ?? s.tipoSolicitud}</td>
                          <td style={{ textAlign: "center" }}>{s.fechaInicio ?? "—"}</td>
                          <td style={{ textAlign: "center" }}>{s.fechaFin ?? "—"}</td>
                          <td style={{ textAlign: "center" }}>{s.diasSolicitados ?? "—"}</td>
                          <td style={{ textAlign: "center", fontSize: 12, color: "#16a34a" }}>{s.respuesta ?? "—"}</td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        </div>

        {/* ── Tabla: Rechazadas ── */}
        <div style={{ marginTop: 24 }}>
          <h3 style={{ margin: "0 0 10px", fontSize: 15, color: "#ef4444", display: "flex", alignItems: "center", gap: 8 }}>
            <span style={{ width: 10, height: 10, borderRadius: "50%", background: "#ef4444", display: "inline-block" }} />
            Solicitudes Rechazadas ({solRechazadas.length})
          </h3>
          <div className="section-card" style={{ padding: 0, overflow: "hidden" }}>
            <div className="table-wrapper" style={{ marginBottom: 0 }}>
              {loading ? (
                <div className="loading-text">Actualizando solicitudes rechazadas...</div>
              ) : solRechazadas.length === 0 ? (
                <div className="empty-state">
                  <div className="empty-state-icon">📋</div>
                  <p>No hay solicitudes rechazadas</p>
                </div>
              ) : (
                <table>
                  <thead>
                    <tr><th style={{ textAlign: "center" }}>Empleado</th><th style={{ textAlign: "center" }}>Tipo</th><th style={{ textAlign: "center" }}>Inicio</th><th style={{ textAlign: "center" }}>Fin</th><th style={{ textAlign: "center" }}>Días</th><th style={{ textAlign: "center" }}>Motivo del Rechazo</th></tr>
                  </thead>
                  <tbody>
                    {solRechazadas.map(s => {
                      const id = s.idSolicitud ?? s.id;
                      const isSelected = solicitudActiva?.idSolicitud === id || solicitudActiva?.id === id;
                      return (
                        <tr key={id}
                          style={{ background: isSelected ? "#fff1f2" : undefined, outline: isSelected ? "2px solid #ef4444" : undefined }}>
                          <td style={{ textAlign: "center" }}>
                            <button type="button" className="table-row-action"
                              aria-label={`Ver detalle de solicitud ${id} de ${s.nombreEmpleado ?? `empleado ${s.idEmpleado}`}`}
                              onClick={() => { setSolicitudActiva(s); setDetalleAbierto(true); }}>
                              {s.nombreEmpleado ?? s.idEmpleado}
                            </button>
                          </td>
                          <td style={{ textAlign: "center" }}>{TIPO_LABELS[s.tipoSolicitud] ?? s.tipoSolicitud}</td>
                          <td style={{ textAlign: "center" }}>{s.fechaInicio ?? "—"}</td>
                          <td style={{ textAlign: "center" }}>{s.fechaFin ?? "—"}</td>
                          <td style={{ textAlign: "center" }}>{s.diasSolicitados ?? "—"}</td>
                          <td style={{ textAlign: "center", fontSize: 12, color: "#dc2626" }}>{s.respuesta ?? "—"}</td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        </div>
        <DetalleSolicitud solicitud={detalleAbierto ? solicitudActiva : null} onCerrar={() => setDetalleAbierto(false)} />
      </main>

      {modalDecision.open && (
        <AccessibleDialog className="modal-overlay" labelledBy="gerencia-decision-title" onClose={() => setModalDecision({ ...modalDecision, open: false })}>
          <div className="modal-box">
            <div className="modal-header">
              <div>
                <h3 id="gerencia-decision-title">{modalDecision.decision === "APROBAR" ? "✅ Aprobar" : "❌ Rechazar"} Solicitud</h3>
                <p>#{modalDecision.solicitud?.idSolicitud ?? modalDecision.solicitud?.id} — {modalDecision.solicitud?.nombreEmpleado ?? modalDecision.solicitud?.idEmpleado}</p>
              </div>
              <button type="button" className="modal-close" aria-label="Cerrar decisión de solicitud" onClick={() => setModalDecision({ ...modalDecision, open: false })}>×</button>
            </div>
            <div className="modal-body">
              <div className="form-group">
                <label htmlFor="gerencia-comentario">Observación / Comentario de Gerencia *</label>
                <textarea id="gerencia-comentario" required rows={3} value={modalDecision.comentario}
                  onChange={e => setModalDecision({ ...modalDecision, comentario: e.target.value })}
                  placeholder="Ingrese su decisión y motivo..." autoFocus />
              </div>
            </div>
            <div className="modal-footer">
              <button type="button" className="btn btn-secondary" onClick={() => setModalDecision({ ...modalDecision, open: false })}>Cancelar</button>
              <button
                className={modalDecision.decision === "APROBAR" ? "btn btn-success" : "btn btn-danger"}
                onClick={handleDecidir}
                disabled={!modalDecision.comentario.trim() || procesando}>
                {procesando ? "Procesando..." : (modalDecision.decision === "APROBAR" ? "✅ Confirmar Aprobación" : "❌ Confirmar Rechazo")}
              </button>
            </div>
          </div>
        </AccessibleDialog>
      )}

      {/* ══ MODAL: ERROR ══ */}
      {errorModal.open && (
        <AccessibleDialog className="modal-overlay" labelledBy="gerencia-solicitud-error-title" describedBy="gerencia-solicitud-error-message" onClose={closeErrorModal}>
          <div className="modal-box" style={{ maxWidth: 420 }}>
            <div className="modal-header">
              <div><h3 id="gerencia-solicitud-error-title">⚠️ Error de validación</h3></div>
              <button type="button" className="modal-close" aria-label="Cerrar error" onClick={closeErrorModal}>×</button>
            </div>
            <div className="modal-body">
              <p id="gerencia-solicitud-error-message" style={{ fontSize: 14, color: "var(--text-1)", margin: 0 }}>{errorModal.message}</p>
            </div>
            <div className="modal-footer">
              <button type="button" className="btn btn-primary" onClick={closeErrorModal}>Entendido</button>
            </div>
          </div>
        </AccessibleDialog>
      )}
    </div>
  );
}

/* ══════════════════════════════════════════════════════════
   ROUTER
══════════════════════════════════════════════════════════ */
export default function SolicitudPermisos() {
  const location = useLocation();
  const path = location.pathname;
  if (path.startsWith("/gerencia")) return <VistaGerencia />;
  if (path.startsWith("/rrhh"))     return <VistaRRHH />;
  return <VistaEmpleado />;
}
