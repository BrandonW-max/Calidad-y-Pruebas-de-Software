import { useState, useEffect } from "react";
import { useLocation } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import "../styles/Dashboard.css";
import {
  listarJustificacionesPendientes,
  listarJustificacionesEmpleado,
  listarAsistenciaEmpleado,
  getAsistenciaHoy,
  registrarManual,
  registrarJustificacion,
  revisarJustificacion,
  listarEmpleados,
  buscarEmpleados,
  getIdEmpleado,
  getUser,
} from "../services/api";

function getRolFromPath(pathname) {
  if (pathname.startsWith("/empleado")) return "EMPLEADO";
  if (pathname.startsWith("/gerencia")) return "GERENCIA";
  return "RRHH";
}

/* ══════════════════════════════════════════════════════════
   VISTA EMPLEADO
══════════════════════════════════════════════════════════ */
function VistaEmpleado() {
  const idEmpleado = getIdEmpleado();

  const [hora, setHora] = useState(new Date());
  const [justificaciones, setJustificaciones] = useState([]);
  const [asistenciaHoy,   setAsistenciaHoy]   = useState(null);
  const [historial,       setHistorial]        = useState([]);
  const [loading, setLoading]   = useState(false);
  const [mensaje, setMensaje]   = useState("");
  const [error,   setError]     = useState("");
  const [tabActiva, setTabActiva] = useState("info");

  // Registro manual modal
  const [modalManual, setModalManual] = useState({ open: false, tipo: "ENTRADA" });
  const [manualMotivo, setManualMotivo] = useState("");
  const [saving, setSaving] = useState(false);

  // ECU-03 A4: justificación de tardanza
  const [modalJustificar, setModalJustificar] = useState({ open: false, justificacion: null });
  const [justifMotivo, setJustifMotivo] = useState("");
  const [justifSaving, setJustifSaving] = useState(false);

  // Reloj
  useEffect(() => {
    const t = setInterval(() => setHora(new Date()), 1000);
    return () => clearInterval(t);
  }, []);

  useEffect(() => {
    if (!idEmpleado) return;
    cargarJustificaciones();
    cargarAsistenciaHoy();
    cargarHistorial();
  }, [idEmpleado]);

  const cargarJustificaciones = async () => {
    try {
      const data = await listarJustificacionesEmpleado(idEmpleado);
      setJustificaciones(Array.isArray(data) ? data : []);
    } catch { /* silencioso */ }
  };

  const cargarAsistenciaHoy = async () => {
    try {
      const data = await getAsistenciaHoy(idEmpleado);
      setAsistenciaHoy(data);
    } catch { /* silencioso */ }
  };

  const cargarHistorial = async () => {
    setLoading(true);
    try {
      const data = await listarAsistenciaEmpleado(idEmpleado);
      setHistorial(Array.isArray(data) ? data : []);
    } catch {
      setError("No se pudo cargar el historial");
    } finally {
      setLoading(false);
    }
  };

  const abrirModal = (tipo) => {
    setModalManual({ open: true, tipo });
    setManualMotivo("");
    setMensaje("");
  };

  // ECU-03 A4: empleado envía justificación de tardanza
  const handleEnviarJustificacion = async () => {
    if (!justifMotivo.trim()) return;
    setJustifSaving(true);
    setMensaje("");
    try {
      const j = modalJustificar.justificacion;
      await registrarJustificacion({
        idEmpleado,
        idAsistencia: j.idAsistencia ?? j.id ?? j.idJustificacion,
        motivo: justifMotivo,
        fecha: j.fechaRegistro ?? j.fecha ?? new Date().toISOString().split("T")[0],
      });
      setMensaje("Justificación enviada correctamente. RRHH la revisará en breve.");
      setModalJustificar({ open: false, justificacion: null });
      setJustifMotivo("");
      cargarJustificaciones();
    } catch {
      setError("Error al enviar la justificación. Intente de nuevo.");
      setModalJustificar({ open: false, justificacion: null });
    } finally {
      setJustifSaving(false);
    }
  };

  const handleRegistrarManual = async () => {
    if (!manualMotivo.trim()) return;
    if (!idEmpleado) {
      setMensaje("No se encontró su ID de empleado. Contacte a RRHH.");
      setModalManual({ open: false, tipo: "ENTRADA" });
      return;
    }
    setSaving(true);
    try {
      const now = new Date();
      const fechaHora = now.toISOString().replace("T", " ").substring(0, 19);
      await registrarManual({
        idEmpleado,
        tipo: modalManual.tipo,
        fechaHora,
        motivo: manualMotivo,
      });
      setMensaje(`${modalManual.tipo === "ENTRADA" ? "Entrada" : "Salida"} registrada correctamente.`);
      setModalManual({ open: false, tipo: "ENTRADA" });
      setManualMotivo("");
    } catch {
      setMensaje("Error al registrar la asistencia. Intente de nuevo.");
      setModalManual({ open: false, tipo: "ENTRADA" });
    } finally {
      setSaving(false);
    }
  };

  const horaStr = hora.toLocaleTimeString("es-PE", { hour: "2-digit", minute: "2-digit", second: "2-digit", hour12: false });
  const fechaStr = hora.toLocaleDateString("es-PE", { weekday: "long", year: "numeric", month: "long", day: "numeric" });
  const fechaCorta = hora.toLocaleDateString("es-PE", { day: "2-digit", month: "2-digit", year: "numeric" });

  const estadoBadge = (estado) => {
    const m = {
      PENDIENTE: "badge-warning",
      APROBADO:  "badge-success",
      RECHAZADO: "badge-danger",
    };
    return m[estado] ?? "badge-gray";
  };

  return (
    <div className="dashboard">
      <Sidebar rol="EMPLEADO" />

      <main className="main-content">
        <header className="main-header header-empleado">
          <div className="header-left">
            <span className="page-breadcrumb">Dashboard / Asistencia / Control de Asistencia</span>
            <h1>Control de Asistencia</h1>
          </div>
          <div className="header-right">
            <span className="role-pill role-pill-empleado">Empleado</span>
          </div>
        </header>

        {mensaje && <div className="alert alert-success" style={{ marginBottom: 16 }}>✅ {mensaje}</div>}
        {error   && <div className="alert alert-warning" style={{ marginBottom: 16 }}>⚠️ {error}</div>}

        {/* ── FILA SUPERIOR: Registro manual | Asistencia Hoy ── */}
        <div className="asist-grid">

          {/* IZQUIERDA: Registro manual */}
          <div className="section-card">
            <h2>Registro de Entrada / Salida (Asistencia Manual)</h2>
            <div className="alert alert-info" style={{ marginBottom: 18 }}>
              ℹ️ Utiliza esta opción solo cuando no puedas registrar tu asistencia con el dispositivo biométrico.
            </div>

            {/* Reloj digital */}
            <div className="asist-clock-display">{horaStr}</div>
            <div className="asist-date-display">{fechaStr}</div>

            <button className="asist-btn-entrada" onClick={() => abrirModal("ENTRADA")}>
              → Registrar Entrada (Asistencia Manual)
            </button>
            <button className="asist-btn-salida" onClick={() => abrirModal("SALIDA")}>
              ← Registrar Salida (Asistencia Manual)
            </button>

            <div className="asist-horario-tag">
              🕐 Horario establecido: 08:00 a.m. - 05:00 p.m.
            </div>
          </div>

          {/* DERECHA: Mi Asistencia Hoy */}
          <div className="section-card">
            <div className="section-header">
              <h2 style={{ margin: 0, border: "none", padding: 0 }}>Mi Asistencia Hoy</h2>
              <span style={{ fontSize: "12px", color: "var(--text-3)" }}>Fecha: {fechaCorta}</span>
            </div>

            <div className="asist-today-grid">
              <div className="asist-today-item">
                <label>Entrada</label>
                <div className="asist-today-val">
                  {asistenciaHoy?.horaEntrada
                    ? asistenciaHoy.horaEntrada.substring(0, 5)
                    : "—:—"}
                </div>
                <span className={`badge ${asistenciaHoy?.horaEntrada ? "badge-success" : "badge-gray"}`}
                  style={{ marginTop: 4, fontSize: "11px" }}>
                  {asistenciaHoy?.horaEntrada ? "Registrada" : "Pendiente"}
                </span>
              </div>
              <div className="asist-today-item">
                <label>Salida</label>
                <div className="asist-today-val" style={{ color: asistenciaHoy?.horaSalida ? "var(--text)" : "var(--text-3)" }}>
                  {asistenciaHoy?.horaSalida
                    ? asistenciaHoy.horaSalida.substring(0, 5)
                    : "—:—"}
                </div>
                <span className={`badge ${asistenciaHoy?.horaSalida ? "badge-success" : "badge-gray"}`}
                  style={{ marginTop: 4, fontSize: "11px" }}>
                  {asistenciaHoy?.horaSalida ? "Registrada" : "Pendiente"}
                </span>
              </div>
              <div className="asist-today-item">
                <label>Horas trabajadas</label>
                <span className="badge badge-info" style={{ fontSize: "11px" }}>
                  {asistenciaHoy?.horasTrabajadas != null
                    ? `${asistenciaHoy.horasTrabajadas} h`
                    : asistenciaHoy?.horaEntrada ? "En curso" : "—"}
                </span>
              </div>
              <div className="asist-today-item">
                <label>Estado del día</label>
                <span className={`badge ${
                  asistenciaHoy?.estado === "PUNTUAL"  ? "badge-success" :
                  asistenciaHoy?.estado === "TARDANZA" ? "badge-warning" : "badge-gray"
                }`} style={{ fontSize: "11px" }}>
                  {asistenciaHoy?.estado ?? "Sin registro"}
                </span>
              </div>
            </div>

            <div className="alert alert-warning" style={{ marginTop: 8, marginBottom: 0 }}>
              <strong>Importante:</strong> Si llegas después del horario establecido, deberás justificar tu tardanza.
            </div>
          </div>
        </div>

        {/* ── TABS ── */}
        <div className="tab-bar">
          <button className={`tab ${tabActiva === "justificaciones" ? "active" : ""}`}
            onClick={() => { setTabActiva("justificaciones"); if (idEmpleado) cargarJustificaciones(); }}>
            📋 Mis Justificaciones
          </button>
          <button className={`tab ${tabActiva === "nueva-justif" ? "active" : ""}`}
            onClick={() => setTabActiva("nueva-justif")}>
            ✍️ Nueva Justificación
          </button>
          <button className={`tab ${tabActiva === "info" ? "active" : ""}`}
            onClick={() => setTabActiva("info")}>
            ℹ️ Información
          </button>
        </div>

        {/* ── JUSTIFICACIONES ── */}
        {tabActiva === "justificaciones" && (
          <div className="section-card">
            <div className="section-header">
              <h2 style={{ margin: 0, border: "none", padding: 0 }}>
                Mis Justificaciones (Inasistencias o Tardanzas)
              </h2>
              <span style={{ fontSize: "12px", color: "var(--text-2)" }}>
                Aquí puedes registrar y dar seguimiento a tus justificaciones.
              </span>
            </div>
            <div className="alert alert-warning">
              ⏱ Solo tienes <strong>1 día</strong> desde la incidencia para registrar tu justificación.
            </div>
            <div className="table-wrapper">
              {loading ? (
                <div className="loading-text">Cargando justificaciones...</div>
              ) : (
                <table>
                  <thead>
                    <tr>
                      <th>Fecha</th>
                      <th>Tipo</th>
                      <th>Motivo</th>
                      <th>Evidencia</th>
                      <th>Estado</th>
                      <th>Observación</th>
                      <th>Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    {justificaciones.length === 0 ? (
                      <tr>
                        <td colSpan={7}>
                          <div className="empty-state">
                            <div className="empty-state-icon">✅</div>
                            <p>No tienes justificaciones pendientes</p>
                          </div>
                        </td>
                      </tr>
                    ) : justificaciones.map((j) => (
                      <tr key={j.id ?? j.idJustificacion}>
                        <td>{j.fechaRegistro ?? j.fecha ?? "—"}</td>
                        <td>{j.tipo ?? "Tardanza"}</td>
                        <td>{j.motivo}</td>
                        <td>
                          {j.evidencia
                            ? <span>📎 1 archivo</span>
                            : <span style={{ color: "var(--text-3)" }}>-</span>}
                        </td>
                        <td>
                          <span className={`badge ${estadoBadge(j.estado)}`}>
                            {j.estado ?? "Pendiente"}
                          </span>
                        </td>
                        <td>{j.comentario ?? j.observacion ?? <span style={{ color: "var(--text-3)" }}>-</span>}</td>
                        <td>
                          {(!j.estado || j.estado === "PENDIENTE") ? (
                            <button className="btn btn-warning btn-sm"
                              onClick={() => {
                                setModalJustificar({ open: true, justificacion: j });
                                setJustifMotivo(j.motivo ?? "");
                              }}>
                              ✍️ Justificar
                            </button>
                          ) : (
                            <span className="badge badge-gray" style={{ fontSize: "11px" }}>
                              {j.estado === "APROBADO" ? "✅ Aprobado" : "❌ Rechazado"}
                            </span>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        )}

        {/* ── NUEVA JUSTIFICACIÓN (ECU-03 A4) ── */}
        {tabActiva === "nueva-justif" && (
          <div className="section-card">
            <h2>✍️ Registrar Nueva Justificación de Tardanza o Inasistencia</h2>
            <div className="alert alert-warning">
              ⏱ Solo puedes justificar incidencias del <strong>día anterior o del día actual</strong>. Pasado 1 día, no será posible registrar la justificación.
            </div>
            <form onSubmit={async (e) => {
              e.preventDefault();
              const fd = new FormData(e.target);
              setJustifSaving(true); setMensaje(""); setError("");
              try {
                await registrarJustificacion({
                  idEmpleado,
                  fechaIncidencia: fd.get("fechaIncidencia"),
                  tipo: fd.get("tipo"),
                  motivo: fd.get("motivo"),
                });
                setMensaje("Justificación enviada correctamente. RRHH la revisará en breve.");
                e.target.reset();
                setTabActiva("justificaciones");
                cargarJustificaciones();
              } catch {
                setError("Error al enviar la justificación. Intente de nuevo.");
              } finally { setJustifSaving(false); }
            }}>
              <div className="form-grid">
                <div className="form-group">
                  <label>Fecha de la incidencia *</label>
                  <input type="date" name="fechaIncidencia"
                    max={new Date().toISOString().split("T")[0]}
                    defaultValue={new Date().toISOString().split("T")[0]} required />
                </div>
                <div className="form-group">
                  <label>Tipo de incidencia *</label>
                  <select name="tipo" required>
                    <option value="TARDANZA">Tardanza</option>
                    <option value="INASISTENCIA">Inasistencia</option>
                    <option value="SALIDA_ANTICIPADA">Salida anticipada</option>
                  </select>
                </div>
                <div className="form-group form-full">
                  <label>Motivo de la justificación *</label>
                  <textarea name="motivo" rows={4}
                    placeholder="Describa detalladamente el motivo de su tardanza o inasistencia..."
                    required />
                </div>
              </div>
              <div className="form-actions">
                <button type="submit" className="btn btn-primary" disabled={justifSaving}>
                  {justifSaving ? "Enviando..." : "📤 Enviar Justificación"}
                </button>
                <button type="reset" className="btn btn-secondary">Limpiar</button>
              </div>
            </form>
          </div>
        )}

        {/* ── INFORMACIÓN ── */}
        {tabActiva === "info" && (
          <div className="section-card">
            <h2>ℹ️ ¿Cómo funciona el control de asistencia?</h2>
            <div className="alert alert-info">
              <strong>Registro de entrada:</strong> Coloca tu huella dactilar en el dispositivo biométrico al llegar. El sistema registrará tu hora de entrada automáticamente.
            </div>
            <div className="alert alert-info">
              <strong>Registro de salida:</strong> Al terminar tu jornada, vuelve a colocar tu huella. El sistema calculará automáticamente las horas trabajadas.
            </div>
            <div className="alert alert-warning">
              <strong>Tardanzas:</strong> Si llegas después del horario establecido (más allá del margen de tolerancia configurado), se registrará una incidencia de tardanza. Dispondrás de 1 día para justificarla.
            </div>
            <div className="alert alert-warning">
              <strong>Correcciones:</strong> Si hay un error en tu registro de asistencia, usa el formulario de registro manual o solicita una corrección a RRHH.
            </div>
          </div>
        )}

        {/* ── HISTORIAL ── */}
        <div className="section-card">
          <div className="section-header">
            <h2 style={{ margin: 0, border: "none", padding: 0 }}>Historial de Asistencia</h2>
            <button className="btn btn-secondary btn-sm" onClick={cargarHistorial}>🔄 Actualizar</button>
          </div>
          <div className="table-wrapper">
            {loading ? <div className="loading-text">Cargando historial...</div> : (
              <table>
                <thead>
                  <tr>
                    <th>Fecha</th>
                    <th>Entrada</th>
                    <th>Salida</th>
                    <th>Horas</th>
                    <th>Tardanza</th>
                    <th>Estado</th>
                    <th>Tipo registro</th>
                  </tr>
                </thead>
                <tbody>
                  {historial.length === 0 ? (
                    <tr><td colSpan={7}>
                      <div className="empty-state">
                        <div className="empty-state-icon">📋</div>
                        <p>No hay registros de asistencia aún</p>
                      </div>
                    </td></tr>
                  ) : historial.map(r => (
                    <tr key={r.idRegistro}>
                      <td>{r.fecha}</td>
                      <td>{r.horaEntrada ? r.horaEntrada.substring(0, 5) : "—"}</td>
                      <td>{r.horaSalida  ? r.horaSalida.substring(0, 5)  : "—"}</td>
                      <td>{r.horasTrabajadas != null ? `${r.horasTrabajadas} h` : "—"}</td>
                      <td>{r.minutosTardanza > 0 ? `${r.minutosTardanza} min` : "—"}</td>
                      <td>
                        <span className={`badge ${r.estado === "PUNTUAL" ? "badge-success" : r.estado === "TARDANZA" ? "badge-warning" : "badge-gray"}`}>
                          {r.estado ?? "—"}
                        </span>
                      </td>
                      <td style={{ fontSize: "12px", color: "var(--text-3)" }}>
                        {r.tipoUltimoRegistro ?? "—"}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>

      </main>

      {/* ── MODAL: JUSTIFICACIÓN DE TARDANZA (ECU-03 A4) ── */}
      {modalJustificar.open && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && setModalJustificar({ open: false, justificacion: null })}>
          <div className="modal-box">
            <div className="modal-header">
              <div>
                <h3>✍️ Registrar Justificación de Tardanza</h3>
                <p>Fecha: {modalJustificar.justificacion?.fechaRegistro ?? modalJustificar.justificacion?.fecha ?? "—"}</p>
              </div>
              <button className="modal-close" onClick={() => setModalJustificar({ open: false, justificacion: null })}>×</button>
            </div>
            <div className="modal-body">
              <div className="alert alert-warning" style={{ marginBottom: 14 }}>
                ⏱ Tienes <strong>1 día</strong> desde la incidencia para justificarla. Una vez enviada, RRHH la revisará.
              </div>
              <div className="form-group">
                <label>Motivo de la tardanza *</label>
                <textarea
                  rows={4}
                  value={justifMotivo}
                  onChange={e => setJustifMotivo(e.target.value)}
                  placeholder="Describa el motivo de su tardanza (tráfico, emergencia, problema de transporte, etc.)..."
                  autoFocus
                />
              </div>
              <div className="form-group">
                <label>Evidencia (opcional)</label>
                <div className="alert alert-info" style={{ marginBottom: 0, fontSize: "12px" }}>
                  Si tienes un documento de respaldo (certificado médico, etc.), adjúntalo en físico a RRHH.
                </div>
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModalJustificar({ open: false, justificacion: null })}>Cancelar</button>
              <button className="btn btn-primary" onClick={handleEnviarJustificacion}
                disabled={!justifMotivo.trim() || justifSaving}>
                {justifSaving ? "Enviando..." : "📤 Enviar Justificación"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── MODAL REGISTRO MANUAL ── */}
      {modalManual.open && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && setModalManual({ open: false, tipo: "ENTRADA" })}>
          <div className="modal-box">
            <div className="modal-header">
              <div>
                <h3>
                  {modalManual.tipo === "ENTRADA"
                    ? "→ Registrar Entrada (Asistencia Manual)"
                    : "← Registrar Salida (Asistencia Manual)"}
                </h3>
                <p>Inicio de jornada — {new Date().toLocaleTimeString("es-PE", { hour: "2-digit", minute: "2-digit" })}</p>
              </div>
              <button className="modal-close" onClick={() => setModalManual({ open: false, tipo: "ENTRADA" })}>×</button>
            </div>
            <div className="modal-body">
              <div className="alert alert-warning" style={{ marginBottom: 14 }}>
                Esta opción se usa solo cuando el dispositivo biométrico no esté disponible.
              </div>
              <div className="form-group">
                <label>Motivo del registro manual *</label>
                <textarea
                  rows={3}
                  value={manualMotivo}
                  onChange={e => setManualMotivo(e.target.value)}
                  placeholder="Ej: Dispositivo biométrico no disponible, problema técnico..."
                  autoFocus
                />
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModalManual({ open: false, tipo: "ENTRADA" })}>Cancelar</button>
              <button
                className={modalManual.tipo === "ENTRADA" ? "btn btn-success" : "btn btn-warning"}
                onClick={handleRegistrarManual}
                disabled={!manualMotivo.trim() || saving}
              >
                {saving ? "Guardando..." : (modalManual.tipo === "ENTRADA" ? "→ Confirmar Entrada" : "← Confirmar Salida")}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

/* ══════════════════════════════════════════════════════════
   VISTA RRHH
══════════════════════════════════════════════════════════ */
function VistaRRHH() {
  const [justificaciones, setJustificaciones] = useState([]);
  const [loading,  setLoading]  = useState(false);
  const [mensaje,  setMensaje]  = useState("");
  const [error,    setError]    = useState("");
  const [modalRechazar, setModalRechazar] = useState({ open: false, id: null, comentario: "" });

  const [manualForm, setManualForm] = useState({
    idEmpleado: "", tipo: "ENTRADA", fechaHora: "", motivo: "",
  });
  const [empleados,       setEmpleados]       = useState([]);
  const [busquedaNombre,  setBusquedaNombre]  = useState("");
  const [empleadoSelec,   setEmpleadoSelec]   = useState(null);
  const [sugerencias,     setSugerencias]     = useState([]);
  const [mostrarSugere,   setMostrarSugere]   = useState(false);

  useEffect(() => {
    cargarJustificaciones();
    listarEmpleados(0, 100).then(d => setEmpleados(d.content ?? [])).catch(() => {});
  }, []);

  const cargarJustificaciones = async () => {
    setLoading(true);
    setError("");
    try {
      const data = await listarJustificacionesPendientes();
      setJustificaciones(Array.isArray(data) ? data : []);
    } catch {
      setError("No se pudieron cargar las justificaciones");
    } finally {
      setLoading(false);
    }
  };

  const handleBuscarEmpleado = (valor) => {
    setBusquedaNombre(valor);
    setEmpleadoSelec(null);
    setManualForm(f => ({ ...f, idEmpleado: "" }));
    if (valor.trim().length < 2) { setSugerencias([]); setMostrarSugere(false); return; }
    const filtro = valor.toLowerCase();
    const hits = empleados.filter(e =>
      `${e.nombres} ${e.apellidos}`.toLowerCase().includes(filtro)
    ).slice(0, 8);
    setSugerencias(hits);
    setMostrarSugere(true);
  };

  const seleccionarEmpleado = (emp) => {
    setEmpleadoSelec(emp);
    setBusquedaNombre(`${emp.nombres} ${emp.apellidos}`);
    setManualForm(f => ({ ...f, idEmpleado: emp.idEmpleado }));
    setSugerencias([]);
    setMostrarSugere(false);
  };

  const handleRegistrarManual = async (e) => {
    e.preventDefault();
    setMensaje("");
    try {
      await registrarManual({
        idEmpleado: parseInt(manualForm.idEmpleado),
        tipo: manualForm.tipo,
        fechaHora: manualForm.fechaHora ? manualForm.fechaHora.replace("T", " ") + ":00" : null,
        motivo: manualForm.motivo,
      });
      setMensaje("Registro manual guardado correctamente");
      setManualForm({ idEmpleado: "", tipo: "ENTRADA", fechaHora: "", motivo: "" });
    } catch {
      setMensaje("Error al guardar el registro manual");
    }
  };

  const handleAprobar = async (id) => {
    try {
      await revisarJustificacion(id, "APROBADA", "");
      setMensaje("Justificación aprobada correctamente");
      cargarJustificaciones();
    } catch {
      setMensaje("Error al aprobar la justificación");
    }
  };

  const handleConfirmarRechazo = async () => {
    if (!modalRechazar.comentario.trim()) return;
    try {
      await revisarJustificacion(modalRechazar.id, "RECHAZADA", modalRechazar.comentario);
      setMensaje("Justificación rechazada correctamente");
      setModalRechazar({ open: false, id: null, comentario: "" });
      cargarJustificaciones();
    } catch {
      setMensaje("Error al rechazar la justificación");
      setModalRechazar({ open: false, id: null, comentario: "" });
    }
  };

  return (
    <div className="dashboard">
      <Sidebar rol="RRHH" />

      <main className="main-content">
        <header className="main-header header-rrhh">
          <div className="header-left">
            <span className="page-breadcrumb">RRHH / Control de Asistencia</span>
            <h1>Control de Asistencia</h1>
          </div>
          <span className="role-pill role-pill-rrhh">ECU-03</span>
        </header>

        {mensaje && <div className="alert alert-success">✅ {mensaje}</div>}
        {error   && <div className="alert alert-warning">⚠️ {error}</div>}

        <div className="biometric-banner">
          <div className="biometric-banner-icon">🖐️</div>
          <div className="biometric-banner-text">
            <h3>Sistema Biométrico Activo</h3>
            <p>El registro de entrada/salida de empleados se realiza desde el dispositivo biométrico. Aquí gestionas correcciones y justificaciones.</p>
          </div>
        </div>

        {/* ── SECCIÓN 1: JUSTIFICACIONES PENDIENTES ── */}
        <div className="section-card">
          <div className="section-header" style={{ marginBottom: "14px" }}>
            <h2 style={{ margin: 0, border: "none", padding: 0 }}>
              📋 Justificaciones Pendientes
              {justificaciones.length > 0 && (
                <span style={{
                  marginLeft: 10, background: "#fef3c7", color: "#92400e",
                  fontSize: "12px", fontWeight: 700, padding: "2px 8px",
                  borderRadius: 20, verticalAlign: "middle",
                }}>
                  {justificaciones.length}
                </span>
              )}
            </h2>
            <button className="btn btn-secondary btn-sm" onClick={cargarJustificaciones}>🔄 Actualizar</button>
          </div>
            <div className="alert alert-info">
              Revise cada justificación y decida si aprobarla o rechazarla.
            </div>
            <div className="table-wrapper">
              {loading ? <div className="loading-text">Cargando justificaciones...</div> : (
                <table>
                  <thead>
                    <tr>
                      <th>#</th><th>Empleado</th><th>Fecha Registro</th><th>Motivo</th><th>Estado</th><th>Decisión</th>
                    </tr>
                  </thead>
                  <tbody>
                    {justificaciones.length === 0 ? (
                      <tr><td colSpan={6}>
                        <div className="empty-state">
                          <div className="empty-state-icon">✅</div>
                          <p>No hay justificaciones pendientes de revisión</p>
                        </div>
                      </td></tr>
                    ) : justificaciones.map(j => (
                      <tr key={j.id ?? j.idJustificacion}>
                        <td>{j.id ?? j.idJustificacion}</td>
                        <td>{j.idEmpleado}</td>
                        <td>{j.fechaRegistro ?? "—"}</td>
                        <td>{j.motivo}</td>
                        <td><span className="badge badge-warning">{j.estado ?? "Pendiente"}</span></td>
                        <td style={{ display: "inline-flex", gap: "6px" }}>
                          <button className="btn btn-success btn-sm" onClick={() => handleAprobar(j.id ?? j.idJustificacion)}>✅ Aprobar</button>
                          <button className="btn btn-danger btn-sm" onClick={() => setModalRechazar({ open: true, id: j.id ?? j.idJustificacion, comentario: "" })}>❌ Rechazar</button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
        </div>

        {/* ── SECCIÓN 2: REGISTRO MANUAL ── */}
        <div className="section-card">
          <div className="section-header" style={{ marginBottom: "14px" }}>
            <h2 style={{ margin: 0, border: "none", padding: 0 }}>✏️ Registro Manual de Asistencia</h2>
            <span style={{ fontSize: "12px", color: "var(--text-3)" }}>Corrección · requiere solicitud previa del empleado</span>
          </div>
            <div className="alert alert-warning">
              ⚠️ Esta acción quedará registrada en el log de auditoría.
            </div>
            <form onSubmit={handleRegistrarManual}>
              <div className="form-grid">
                <div className="form-group" style={{ position: "relative" }}>
                  <label>Empleado *</label>
                  <input
                    type="text"
                    value={busquedaNombre}
                    onChange={e => handleBuscarEmpleado(e.target.value)}
                    onFocus={() => sugerencias.length > 0 && setMostrarSugere(true)}
                    onBlur={() => setTimeout(() => setMostrarSugere(false), 150)}
                    placeholder="Buscar por nombre..."
                    autoComplete="off"
                    required={!manualForm.idEmpleado}
                  />
                  {/* campo oculto para validación del formulario */}
                  <input type="hidden" value={manualForm.idEmpleado} required />
                  {mostrarSugere && sugerencias.length > 0 && (
                    <div className="emp-autocomplete-drop">
                      {sugerencias.map(emp => (
                        <div key={emp.idEmpleado} className="emp-autocomplete-item"
                          onMouseDown={() => seleccionarEmpleado(emp)}>
                          <span className="emp-ac-nombre">{emp.nombres} {emp.apellidos}</span>
                          <span className="emp-ac-sub">{emp.cargo ?? "—"} · {emp.departamento ?? "—"}</span>
                        </div>
                      ))}
                    </div>
                  )}
                  {empleadoSelec && (
                    <div className="emp-selec-tag">
                      ✅ {empleadoSelec.nombres} {empleadoSelec.apellidos}
                      <span style={{ color: "var(--text-3)", marginLeft: 6 }}>#{empleadoSelec.idEmpleado}</span>
                    </div>
                  )}
                </div>
                <div className="form-group">
                  <label>Tipo de Marcación *</label>
                  <select value={manualForm.tipo} onChange={e => setManualForm({ ...manualForm, tipo: e.target.value })}>
                    <option value="ENTRADA">Entrada</option>
                    <option value="SALIDA">Salida</option>
                  </select>
                </div>
                <div className="form-group">
                  <label>Fecha y Hora *</label>
                  <input type="datetime-local" value={manualForm.fechaHora}
                    onChange={e => setManualForm({ ...manualForm, fechaHora: e.target.value })} required />
                </div>
                <div className="form-group form-full">
                  <label>Motivo de Corrección *</label>
                  <textarea value={manualForm.motivo}
                    onChange={e => setManualForm({ ...manualForm, motivo: e.target.value })}
                    placeholder="Explique el motivo de la corrección manual..." required />
                </div>
              </div>
              <div className="form-actions">
                <button type="submit" className="btn btn-success">💾 Guardar Registro Manual</button>
                <button type="button" className="btn btn-secondary"
                  onClick={() => setManualForm({ idEmpleado: "", tipo: "ENTRADA", fechaHora: "", motivo: "" })}>
                  Limpiar
                </button>
              </div>
            </form>
          </div>

        {/* ── SECCIÓN 2: JUSTIFICACIONES PENDIENTES ── */}
        <div className="section-card">
          <div className="section-header" style={{ marginBottom: "14px" }}>
            <h2 style={{ margin: 0, border: "none", padding: 0 }}>
              📋 Justificaciones Pendientes
              {justificaciones.length > 0 && (
                <span style={{
                  marginLeft: 10, background: "#fef3c7", color: "#92400e",
                  fontSize: "12px", fontWeight: 700, padding: "2px 8px",
                  borderRadius: 20, verticalAlign: "middle",
                }}>
                  {justificaciones.length}
                </span>
              )}
            </h2>
            <button className="btn btn-secondary btn-sm" onClick={cargarJustificaciones}>🔄 Actualizar</button>
          </div>
            <div className="alert alert-info">
              Revise cada justificación y decida si aprobarla o rechazarla.
            </div>
            <div className="table-wrapper">
              {loading ? <div className="loading-text">Cargando justificaciones...</div> : (
                <table>
                  <thead>
                    <tr>
                      <th>#</th><th>Empleado</th><th>Fecha Registro</th><th>Motivo</th><th>Estado</th><th>Decisión</th>
                    </tr>
                  </thead>
                  <tbody>
                    {justificaciones.length === 0 ? (
                      <tr><td colSpan={6}>
                        <div className="empty-state">
                          <div className="empty-state-icon">✅</div>
                          <p>No hay justificaciones pendientes de revisión</p>
                        </div>
                      </td></tr>
                    ) : justificaciones.map(j => (
                      <tr key={j.id ?? j.idJustificacion}>
                        <td>{j.id ?? j.idJustificacion}</td>
                        <td>{j.idEmpleado}</td>
                        <td>{j.fechaRegistro ?? "—"}</td>
                        <td>{j.motivo}</td>
                        <td><span className="badge badge-warning">{j.estado ?? "Pendiente"}</span></td>
                        <td style={{ display: "flex", gap: "6px" }}>
                          <button className="btn btn-success btn-sm" onClick={() => handleAprobar(j.id ?? j.idJustificacion)}>✅ Aprobar</button>
                          <button className="btn btn-danger btn-sm" onClick={() => setModalRechazar({ open: true, id: j.id ?? j.idJustificacion, comentario: "" })}>❌ Rechazar</button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
      </main>

      {/* ── MODAL: RECHAZAR JUSTIFICACIÓN ── */}
      {modalRechazar.open && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && setModalRechazar({ open: false, id: null, comentario: "" })}>
          <div className="modal-box">
            <div className="modal-header">
              <div><h3>❌ Rechazar Justificación</h3><p>Justificación #{modalRechazar.id}</p></div>
              <button className="modal-close" onClick={() => setModalRechazar({ open: false, id: null, comentario: "" })}>×</button>
            </div>
            <div className="modal-body">
              <div className="alert alert-warning" style={{ marginBottom: 12 }}>El motivo será registrado en el sistema.</div>
              <div className="form-group">
                <label>Motivo del rechazo *</label>
                <textarea
                  rows={3}
                  value={modalRechazar.comentario}
                  onChange={e => setModalRechazar({ ...modalRechazar, comentario: e.target.value })}
                  placeholder="Explique el motivo del rechazo..."
                  autoFocus
                />
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModalRechazar({ open: false, id: null, comentario: "" })}>Cancelar</button>
              <button className="btn btn-danger" onClick={handleConfirmarRechazo} disabled={!modalRechazar.comentario.trim()}>
                ❌ Confirmar Rechazo
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

/* ══════════════════════════════════════════════════════════
   ROUTER PRINCIPAL
══════════════════════════════════════════════════════════ */
export default function ControlAsistencia() {
  const location = useLocation();
  const rol = getRolFromPath(location.pathname);
  return rol === "EMPLEADO" ? <VistaEmpleado /> : <VistaRRHH />;
}
