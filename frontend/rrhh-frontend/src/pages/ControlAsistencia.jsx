import { useState, useEffect } from "react";
import { useLocation } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import "../styles/Dashboard.css";
import {
  listarJustificacionesPendientes,
  listarJustificacionesEmpleado,
  registrarManual,
  revisarJustificacion,
  justificarTardanza,
  listarHorarios,
  crearHorario,
  actualizarHorario,
  eliminarHorario,
  getUser,
} from "../services/api";

function getRolFromPath(pathname) {
  if (pathname.startsWith("/empleado")) return "EMPLEADO";
  if (pathname.startsWith("/gerencia")) return "GERENCIA";
  return "RRHH";
}

export default function ControlAsistencia() {
  const location = useLocation();
  const rol = getRolFromPath(location.pathname);

  const [justificaciones, setJustificaciones] = useState([]);
  const [misJustificaciones, setMisJustificaciones] = useState([]);
  const [loading, setLoading]                 = useState(false);
  const [mensaje, setMensaje]                 = useState("");
  const [error, setError]                     = useState("");
  const [tabActiva, setTabActiva]             = useState(rol === "RRHH" ? "manual" : "misJustificaciones");

  const [manualForm, setManualForm] = useState({
    idEmpleado: "", tipo: "ENTRADA", fechaHora: "", motivo: "",
  });

  const [justificacionForm, setJustificacionForm] = useState({
    idRegistroAsistencia: "", motivo: "",
  });

  const [horarios, setHorarios] = useState([]);
  const [horarioForm, setHorarioForm] = useState({
    idHorario: null, nombreTurno: "", horaEntrada: "", horaSalida: "", tolerancia: 10,
  });

  useEffect(() => {
    if (rol === "RRHH") cargarJustificaciones();
    if (rol === "EMPLEADO") cargarMisJustificaciones();
  }, [rol]);

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

  const cargarMisJustificaciones = async () => {
    setLoading(true);
    setError("");
    try {
      const user = getUser();
      const data = await listarJustificacionesEmpleado(user.idUsuario);
      setMisJustificaciones(Array.isArray(data) ? data : []);
    } catch {
      setError("No se pudieron cargar tus justificaciones");
    } finally {
      setLoading(false);
    }
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

  const handleRevisar = async (id, decision) => {
    const comentario = decision === "RECHAZADO" ? prompt("Motivo del rechazo:") ?? "" : "";
    try {
      await revisarJustificacion(id, decision, comentario);
      setMensaje(`Justificación ${decision === "APROBADA" ? "aprobada" : "rechazada"} correctamente`);
      cargarJustificaciones();
    } catch {
      setMensaje("Error al revisar la justificación");
    }
  };

  const handleJustificar = async (e) => {
    e.preventDefault();
    setMensaje("");
    try {
      await justificarTardanza({
        idRegistroAsistencia: parseInt(justificacionForm.idRegistroAsistencia),
        motivo: justificacionForm.motivo,
        evidenciaUrl: "",
      });
      setMensaje("Justificación enviada correctamente. RRHH la revisará pronto.");
      setJustificacionForm({ idRegistroAsistencia: "", motivo: "" });
      cargarMisJustificaciones();
    } catch {
      setMensaje("Error al enviar la justificación");
    }
  };

  const cargarHorarios = async () => {
    setLoading(true);
    try {
      const data = await listarHorarios();
      setHorarios(Array.isArray(data) ? data : []);
    } catch {
      setError("No se pudieron cargar los horarios");
    } finally {
      setLoading(false);
    }
  };

  const handleHorarioSubmit = async (e) => {
    e.preventDefault();
    setMensaje("");
    try {
      const dto = {
        nombreTurno: horarioForm.nombreTurno,
        horaEntrada: horarioForm.horaEntrada,
        horaSalida: horarioForm.horaSalida,
        tolerancia: parseInt(horarioForm.tolerancia) || 10,
      };
      if (horarioForm.idHorario) {
        await actualizarHorario(horarioForm.idHorario, dto);
        setMensaje("Horario actualizado correctamente");
      } else {
        await crearHorario(dto);
        setMensaje("Horario creado correctamente");
      }
      setHorarioForm({ idHorario: null, nombreTurno: "", horaEntrada: "", horaSalida: "", tolerancia: 10 });
      cargarHorarios();
    } catch {
      setMensaje("Error al guardar el horario");
    }
  };

  const handleEliminarHorario = async (id) => {
    try {
      await eliminarHorario(id);
      setMensaje("Horario eliminado correctamente");
      cargarHorarios();
    } catch {
      setMensaje("Error al eliminar el horario");
    }
  };

  const handleEditarHorario = (h) => {
    setHorarioForm({
      idHorario: h.idHorario,
      nombreTurno: h.nombreTurno || "",
      horaEntrada: h.horaEntrada || "",
      horaSalida: h.horaSalida || "",
      tolerancia: h.tolerancia ?? 10,
    });
    setTabActiva("horarios");
  };

  /* ══════════════════════════════════════ */
  /*  VISTA EMPLEADO                        */
  /* ══════════════════════════════════════ */
  if (rol === "EMPLEADO") {
    return (
      <div className="dashboard">
        <Sidebar rol="EMPLEADO" />

        <main className="main-content">
          <header className="main-header header-empleado">
            <div className="header-left">
              <span className="page-breadcrumb">Mi Portal / Mi Asistencia</span>
              <h1>Mi Asistencia</h1>
            </div>
            <span className="role-pill role-pill-empleado">Empleado</span>
          </header>

          {mensaje && <div className="alert alert-success">✅ {mensaje}</div>}
          {error   && <div className="alert alert-warning">⚠️ {error}</div>}

          <div className="biometric-banner">
            <div className="biometric-banner-icon">🖐️</div>
            <div className="biometric-banner-text">
              <h3>Registro biométrico de asistencia</h3>
              <p>Tu entrada y salida se registran automáticamente mediante el lector de huella dactilar.</p>
            </div>
          </div>

          <div className="tab-bar">
            <button className={`tab ${tabActiva === "misJustificaciones" ? "active" : ""}`} onClick={() => setTabActiva("misJustificaciones")}>
              📋 Mis Justificaciones
            </button>
            <button className={`tab ${tabActiva === "infoAsistencia" ? "active" : ""}`} onClick={() => setTabActiva("infoAsistencia")}>
              ℹ️ Información
            </button>
          </div>

          {tabActiva === "misJustificaciones" && (
            <div className="section-card">
              <h2>📋 Mis Justificaciones de Tardanza</h2>
              <div className="alert alert-warning">
                ⏱ Tienes <strong>1 día</strong> desde la incidencia para registrar tu justificación.
                Pasado ese plazo, ya no podrás justificar la tardanza.
              </div>

              <h3 style={{ marginTop: "20px", marginBottom: "12px" }}>Nueva Justificación</h3>
              <form onSubmit={handleJustificar} style={{ marginBottom: "20px" }}>
                <div className="form-grid">
                  <div className="form-group">
                    <label>ID Registro de Asistencia *</label>
                    <input
                      type="number"
                      value={justificacionForm.idRegistroAsistencia}
                      onChange={(e) => setJustificacionForm({ ...justificacionForm, idRegistroAsistencia: e.target.value })}
                      placeholder="ID del registro con tardanza"
                      required
                    />
                  </div>
                  <div className="form-group form-full">
                    <label>Motivo de la Justificación *</label>
                    <textarea
                      value={justificacionForm.motivo}
                      onChange={(e) => setJustificacionForm({ ...justificacionForm, motivo: e.target.value })}
                      placeholder="Explique el motivo de su tardanza..."
                      required
                    />
                  </div>
                </div>
                <div className="form-actions">
                  <button type="submit" className="btn btn-primary">📤 Enviar Justificación</button>
                </div>
              </form>

              <h3 style={{ marginBottom: "12px" }}>Historial de Justificaciones</h3>
              <div className="table-wrapper">
                {loading ? (
                  <div className="loading-text">Cargando justificaciones...</div>
                ) : (
                  <table>
                    <thead>
                      <tr>
                        <th>#</th>
                        <th>Registro ID</th>
                        <th>Motivo</th>
                        <th>Estado</th>
                        <th>Fecha</th>
                        <th>Comentario</th>
                      </tr>
                    </thead>
                    <tbody>
                      {misJustificaciones.length === 0 ? (
                        <tr>
                          <td colSpan={6}>
                            <div className="empty-state">
                              <div className="empty-state-icon">✅</div>
                              <p>No tienes justificaciones registradas</p>
                            </div>
                          </td>
                        </tr>
                      ) : misJustificaciones.map((j) => (
                        <tr key={j.id ?? j.idJustificacion}>
                          <td>{j.idJustificacion ?? j.id ?? "—"}</td>
                          <td>{j.idRegistroAsistencia ?? "—"}</td>
                          <td>{j.motivo}</td>
                          <td>
                            <span className={`badge ${j.estado === "APROBADA" ? "badge-success" : j.estado === "RECHAZADA" ? "badge-danger" : "badge-warning"}`}>
                              {j.estado ?? "PENDIENTE"}
                            </span>
                          </td>
                          <td>{j.fechaJustificacion ?? "—"}</td>
                          <td>{j.comentarioRevision ?? "—"}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            </div>
          )}

          {tabActiva === "infoAsistencia" && (
            <div className="section-card">
              <h2>ℹ️ ¿Cómo funciona el control de asistencia?</h2>
              <div className="alert alert-info">
                <div>
                  <strong>Registro de entrada:</strong> Coloca tu huella dactilar en el dispositivo biométrico al llegar. El sistema registrará tu hora de entrada automáticamente.
                </div>
              </div>
              <div className="alert alert-info">
                <div>
                  <strong>Registro de salida:</strong> Al terminar tu jornada, vuelve a colocar tu huella. El sistema calculará automáticamente las horas trabajadas.
                </div>
              </div>
              <div className="alert alert-warning">
                <div>
                  <strong>Tardanzas:</strong> Si llegas después del horario establecido (más allá del margen de tolerancia configurado), se registrará una incidencia de tardanza. Dispondrás de 1 día para justificarla.
                </div>
              </div>
              <div className="alert alert-warning">
                <div>
                  <strong>Correcciones:</strong> Si hay un error en tu registro de asistencia, solicita una corrección a RRHH. Ellos podrán ajustar el registro manualmente.
                </div>
              </div>
            </div>
          )}
        </main>
      </div>
    );
  }

  /* ══════════════════════════════════════ */
  /*  VISTA RRHH (por defecto)             */
  /* ══════════════════════════════════════ */
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

        <div className="tab-bar">
          <button className={`tab ${tabActiva === "manual"         ? "active" : ""}`} onClick={() => setTabActiva("manual")}>
            ✏️ Registro Manual (Corrección)
          </button>
          <button className={`tab ${tabActiva === "justificaciones" ? "active" : ""}`} onClick={() => { setTabActiva("justificaciones"); cargarJustificaciones(); }}>
            📋 Justificaciones Pendientes
          </button>
          <button className={`tab ${tabActiva === "horarios" ? "active" : ""}`} onClick={() => { setTabActiva("horarios"); cargarHorarios(); }}>
            ⏰ Configurar Horarios
          </button>
        </div>

        {/* ── REGISTRO MANUAL ── */}
        {tabActiva === "manual" && (
          <div className="section-card">
            <h2>✏️ Registro Manual de Asistencia — Corrección</h2>
            <div className="alert alert-warning">
              ⚠️ Esta acción requiere que el empleado haya solicitado la corrección previamente. Quedará registrada en el log de auditoría con el usuario RRHH que la realiza.
            </div>
            <form onSubmit={handleRegistrarManual}>
              <div className="form-grid">
                <div className="form-group">
                  <label>ID Empleado *</label>
                  <input
                    type="number"
                    value={manualForm.idEmpleado}
                    onChange={(e) => setManualForm({ ...manualForm, idEmpleado: e.target.value })}
                    placeholder="Ingrese el ID del empleado"
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Tipo de Marcación *</label>
                  <select
                    value={manualForm.tipo}
                    onChange={(e) => setManualForm({ ...manualForm, tipo: e.target.value })}
                  >
                    <option value="ENTRADA">Entrada</option>
                    <option value="SALIDA">Salida</option>
                  </select>
                </div>
                <div className="form-group">
                  <label>Fecha y Hora *</label>
                  <input
                    type="datetime-local"
                    value={manualForm.fechaHora}
                    onChange={(e) => setManualForm({ ...manualForm, fechaHora: e.target.value })}
                    required
                  />
                </div>
                <div className="form-group form-full">
                  <label>Motivo de Corrección *</label>
                  <textarea
                    value={manualForm.motivo}
                    onChange={(e) => setManualForm({ ...manualForm, motivo: e.target.value })}
                    placeholder="Explique el motivo de la corrección manual..."
                    required
                  />
                </div>
              </div>
              <div className="form-actions">
                <button type="submit" className="btn btn-success">💾 Guardar Registro Manual</button>
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setManualForm({ idEmpleado: "", tipo: "ENTRADA", fechaHora: "", motivo: "" })}
                >
                  Limpiar
                </button>
              </div>
            </form>
          </div>
        )}

        {/* ── JUSTIFICACIONES ── */}
        {tabActiva === "justificaciones" && (
          <div className="section-card">
            <h2>📋 Justificaciones de Tardanza — Pendientes de Revisión</h2>
            <div className="alert alert-info">
              Revise cada justificación y decida si aprobarla o rechazarla. Al rechazar, deberá ingresar el motivo.
            </div>
            <div className="table-wrapper">
              {loading ? (
                <div className="loading-text">Cargando justificaciones...</div>
              ) : (
                <table>
                  <thead>
                    <tr>
                      <th>#</th>
                      <th>Empleado</th>
                      <th>Fecha Registro</th>
                      <th>Motivo</th>
                      <th>Estado</th>
                      <th>Decisión</th>
                    </tr>
                  </thead>
                  <tbody>
                    {justificaciones.length === 0 ? (
                      <tr>
                        <td colSpan={6}>
                          <div className="empty-state">
                            <div className="empty-state-icon">✅</div>
                            <p>No hay justificaciones pendientes de revisión</p>
                          </div>
                        </td>
                      </tr>
                    ) : justificaciones.map((j) => (
                      <tr key={j.id ?? j.idJustificacion}>
                        <td>{j.id ?? j.idJustificacion}</td>
                        <td>{j.idEmpleado}</td>
                        <td>{j.fechaRegistro ?? "—"}</td>
                        <td>{j.motivo}</td>
                        <td>
                          <span className="badge badge-warning">{j.estado ?? "Pendiente"}</span>
                        </td>
                        <td style={{ display: "flex", gap: "6px" }}>
                          <button className="btn btn-success btn-sm" onClick={() => handleRevisar(j.id ?? j.idJustificacion, "APROBADA")}>
                            ✅ Aprobar
                          </button>
                          <button className="btn btn-danger btn-sm" onClick={() => handleRevisar(j.id ?? j.idJustificacion, "RECHAZADO")}>
                            ❌ Rechazar
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
            <div className="form-actions" style={{ marginTop: "12px" }}>
              <button className="btn btn-secondary" onClick={cargarJustificaciones}>🔄 Actualizar</button>
            </div>
          </div>
        )}

        {/* ── CONFIGURAR HORARIOS ── */}
        {tabActiva === "horarios" && (
          <div className="section-card">
            <h2>⏰ Configurar Horarios y Tolerancia</h2>
            <div className="alert alert-info">
              Defina los turnos de trabajo con su hora de entrada, salida y tolerancia en minutos.
            </div>
            <form onSubmit={handleHorarioSubmit} style={{ marginBottom: "20px" }}>
              <div className="form-grid">
                <div className="form-group">
                  <label>Nombre Turno *</label>
                  <input
                    value={horarioForm.nombreTurno}
                    onChange={(e) => setHorarioForm({ ...horarioForm, nombreTurno: e.target.value })}
                    placeholder="Ej: Turno Mañana"
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Hora Entrada *</label>
                  <input
                    type="time"
                    value={horarioForm.horaEntrada}
                    onChange={(e) => setHorarioForm({ ...horarioForm, horaEntrada: e.target.value })}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Hora Salida *</label>
                  <input
                    type="time"
                    value={horarioForm.horaSalida}
                    onChange={(e) => setHorarioForm({ ...horarioForm, horaSalida: e.target.value })}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Tolerancia (min)</label>
                  <input
                    type="number"
                    min="0"
                    value={horarioForm.tolerancia}
                    onChange={(e) => setHorarioForm({ ...horarioForm, tolerancia: e.target.value })}
                    placeholder="10"
                  />
                </div>
              </div>
              <div className="form-actions">
                <button type="submit" className="btn btn-primary">
                  {horarioForm.idHorario ? "💾 Actualizar" : "➕ Crear Horario"}
                </button>
                {horarioForm.idHorario && (
                  <button type="button" className="btn btn-secondary" onClick={() => setHorarioForm({ idHorario: null, nombreTurno: "", horaEntrada: "", horaSalida: "", tolerancia: 10 })}>
                    Cancelar edición
                  </button>
                )}
              </div>
            </form>

            <h3 style={{ marginBottom: "12px" }}>Horarios Configurados</h3>
            <div className="table-wrapper">
              {loading ? (
                <div className="loading-text">Cargando horarios...</div>
              ) : (
                <table>
                  <thead>
                    <tr>
                      <th>Turno</th>
                      <th>Entrada</th>
                      <th>Salida</th>
                      <th>Tolerancia</th>
                      <th>Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    {horarios.length === 0 ? (
                      <tr>
                        <td colSpan={5}>
                          <div className="empty-state">
                            <div className="empty-state-icon">⏰</div>
                            <p>No hay horarios configurados</p>
                          </div>
                        </td>
                      </tr>
                    ) : horarios.map((h) => (
                      <tr key={h.idHorario}>
                        <td><strong>{h.nombreTurno}</strong></td>
                        <td>{h.horaEntrada ?? "—"}</td>
                        <td>{h.horaSalida ?? "—"}</td>
                        <td>{h.tolerancia ?? 10} min</td>
                        <td style={{ display: "flex", gap: "6px" }}>
                          <button className="btn btn-secondary btn-sm" onClick={() => handleEditarHorario(h)}>
                            ✏️ Editar
                          </button>
                          <button className="btn btn-danger btn-sm" onClick={() => handleEliminarHorario(h.idHorario)}>
                            🗑️ Eliminar
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
