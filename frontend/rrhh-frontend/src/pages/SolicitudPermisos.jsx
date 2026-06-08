import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import "../styles/Dashboard.css";
import {
  registrarSolicitud,
  revisarSolicitudRRHH,
  decidirSolicitudGerencia,
  getSolicitudesPorEstado,
  getUser,
} from "../services/api";

const ESTADOS_BADGE = {
  PENDIENTE: "badge-gray",
  PROCESADO: "badge-purple",
  APROBADA: "badge-success",
  RECHAZADA: "badge-danger",
  OBSERVADO: "badge-warning",
};

export default function SolicitudPermisos() {
  const user = getUser();
  const rol = user.rol ?? "RRHH";

  const [tabActiva, setTabActiva] = useState("pendientes");
  const [solicitudes, setSolicitudes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [mensaje, setMensaje] = useState("");
  const [error, setError] = useState("");

  const [nuevaForm, setNuevaForm] = useState({
    tipoSolicitud: "", fechaInicio: "", fechaFin: "", motivo: "",
  });

  const [rechazoForm, setRechazoForm] = useState({ idSolicitud: null, respuesta: "" });

  const estadoTabla = tabActiva === "pendientes" ? "PENDIENTE"
    : tabActiva === "gerencia" ? "PROCESADO"
    : tabActiva === "historial" ? "APROBADA" : "PENDIENTE";

  useEffect(() => { cargarSolicitudes(estadoTabla); }, [tabActiva]);

  const cargarSolicitudes = async (estado) => {
    setLoading(true);
    setError("");
    try {
      const data = await getSolicitudesPorEstado(estado);
      setSolicitudes(Array.isArray(data) ? data : []);
    } catch {
      setError("No se pudieron cargar las solicitudes");
    } finally {
      setLoading(false);
    }
  };

  const handleNuevaSolicitud = async (e) => {
    e.preventDefault();
    setMensaje("");
    try {
      await registrarSolicitud(nuevaForm);
      setMensaje("Solicitud registrada correctamente en estado PENDIENTE");
      setNuevaForm({ tipoSolicitud: "", fechaInicio: "", fechaFin: "", motivo: "" });
    } catch {
      setMensaje("Error al registrar la solicitud");
    }
  };

  const handleEnviarGerencia = async (idSolicitud) => {
    setMensaje("");
    try {
      await revisarSolicitudRRHH(idSolicitud, true, "");
      setMensaje("Solicitud enviada a Gerencia correctamente");
      cargarSolicitudes("PENDIENTE");
    } catch {
      setMensaje("Error al enviar a Gerencia");
    }
  };

  const handleObservar = async (idSolicitud) => {
    const obs = prompt("Ingrese la observación:");
    if (!obs) return;
    try {
      await revisarSolicitudRRHH(idSolicitud, false, obs);
      setMensaje("Solicitud devuelta al empleado con observación");
      cargarSolicitudes("PENDIENTE");
    } catch {
      setMensaje("Error al observar la solicitud");
    }
  };

  const handleDecisionGerencia = async (idSolicitud, aprobado) => {
    if (!aprobado) {
      setRechazoForm({ idSolicitud, respuesta: "" });
      return;
    }
    setMensaje("");
    try {
      await decidirSolicitudGerencia(idSolicitud, true, "");
      setMensaje("Solicitud aprobada correctamente");
      cargarSolicitudes("PROCESADO");
    } catch {
      setMensaje("Error al aprobar la solicitud");
    }
  };

  const handleConfirmarRechazo = async (e) => {
    e.preventDefault();
    try {
      await decidirSolicitudGerencia(rechazoForm.idSolicitud, false, rechazoForm.respuesta);
      setMensaje("Solicitud rechazada correctamente");
      setRechazoForm({ idSolicitud: null, respuesta: "" });
      cargarSolicitudes("PROCESADO");
    } catch {
      setMensaje("Error al rechazar la solicitud");
    }
  };

  const diasSolicitados = () => {
    if (!nuevaForm.fechaInicio || !nuevaForm.fechaFin) return "—";
    const d1 = new Date(nuevaForm.fechaInicio);
    const d2 = new Date(nuevaForm.fechaFin);
    const diff = Math.round((d2 - d1) / 86400000) + 1;
    return diff > 0 ? `${diff} días` : "—";
  };

  return (
    <div className="dashboard">
      <aside className="sidebar">
        <div className="sidebar-header"><h2>Panel RRHH</h2><p>Recursos Humanos</p></div>
        <nav className="sidebar-nav">
          <Link to="/rrhh">🏠 Inicio</Link>
          <Link to="/rrhh/empleados">👥 Gestión de Empleados</Link>
          <Link to="/rrhh/asistencia">📋 Control de Asistencia</Link>
          <Link to="/rrhh/nomina">💰 Cálculo de Nómina</Link>
          <Link to="/rrhh/solicitudes" className="active">📅 Solicitudes</Link>
          <Link to="/rrhh/reportes">📊 Reportes</Link>
          <div className="nav-logout"><Link to="/">🚪 Cerrar Sesión</Link></div>
        </nav>
      </aside>

      <main className="main-content">
        <header className="main-header">
          <h1>Solicitud de Permisos y Vacaciones</h1>
          <span>ECU-05</span>
        </header>

        {mensaje && <div className="alert alert-success">{mensaje}</div>}
        {error   && <div className="alert alert-warning">{error}</div>}

        <div className="tab-bar">
          <button className={`tab ${tabActiva === "pendientes" ? "active" : ""}`} onClick={() => setTabActiva("pendientes")}>
            Pendientes (RRHH)
          </button>
          <button className={`tab ${tabActiva === "nueva" ? "active" : ""}`} onClick={() => setTabActiva("nueva")}>
            Nueva Solicitud
          </button>
          <button className={`tab ${tabActiva === "gerencia" ? "active" : ""}`} onClick={() => setTabActiva("gerencia")}>
            Decisión Gerencia
          </button>
          <button className={`tab ${tabActiva === "historial" ? "active" : ""}`} onClick={() => setTabActiva("historial")}>
            Historial
          </button>
        </div>

        {/* ══ PENDIENTES RRHH ══ */}
        {tabActiva === "pendientes" && (
          <div className="section-card">
            <h2>Solicitudes Pendientes de Validación</h2>
            <div className="table-wrapper">
              {loading ? <p style={{ padding: "16px", color: "#888" }}>Cargando...</p> : (
                <table>
                  <thead>
                    <tr>
                      <th>#</th><th>Empleado</th><th>Tipo</th>
                      <th>Fecha Inicio</th><th>Fecha Fin</th><th>Días</th>
                      <th>Motivo</th><th>Estado</th><th>Acción RRHH</th>
                    </tr>
                  </thead>
                  <tbody>
                    {solicitudes.length === 0 ? (
                      <tr><td colSpan={9} style={{ textAlign: "center", color: "#888" }}>No hay solicitudes pendientes</td></tr>
                    ) : solicitudes.map((s) => (
                      <tr key={s.idSolicitud}>
                        <td>{s.idSolicitud}</td>
                        <td>{s.nombreEmpleado ?? `Empleado ${s.idEmpleado}`}</td>
                        <td>{s.tipoSolicitud}</td>
                        <td>{s.fechaInicio}</td>
                        <td>{s.fechaFin}</td>
                        <td>{s.diasSolicitados}</td>
                        <td>{s.motivo}</td>
                        <td><span className={`badge ${ESTADOS_BADGE[s.estado] ?? "badge-gray"}`}>{s.estado}</span></td>
                        <td>
                          <button className="btn btn-success btn-sm" onClick={() => handleEnviarGerencia(s.idSolicitud)}>
                            Enviar a Gerencia
                          </button>{" "}
                          <button className="btn btn-warning btn-sm" onClick={() => handleObservar(s.idSolicitud)}>
                            Observar
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

        {/* ══ NUEVA SOLICITUD ══ */}
        {tabActiva === "nueva" && (
          <div className="section-card">
            <h2>Registrar Nueva Solicitud</h2>
            <form onSubmit={handleNuevaSolicitud}>
              <div className="form-grid">
                <div className="form-group">
                  <label>Tipo de Solicitud *</label>
                  <select value={nuevaForm.tipoSolicitud}
                    onChange={(e) => setNuevaForm({ ...nuevaForm, tipoSolicitud: e.target.value })} required>
                    <option value="">Seleccione</option>
                    <option value="VACACIONES">Vacaciones</option>
                    <option value="PERMISO_PERSONAL">Permiso personal</option>
                    <option value="PERMISO_MEDICO">Permiso médico</option>
                    <option value="OTROS">Otros</option>
                  </select>
                </div>
                <div className="form-group">
                  <label>Fecha de Inicio *</label>
                  <input type="date" value={nuevaForm.fechaInicio}
                    onChange={(e) => setNuevaForm({ ...nuevaForm, fechaInicio: e.target.value })} required />
                </div>
                <div className="form-group">
                  <label>Fecha de Fin *</label>
                  <input type="date" value={nuevaForm.fechaFin}
                    onChange={(e) => setNuevaForm({ ...nuevaForm, fechaFin: e.target.value })} required />
                </div>
                <div className="form-group">
                  <label>Días Totales</label>
                  <input value={diasSolicitados()} readOnly style={{ background: "#f9f9f9" }} />
                </div>
                <div className="form-group form-full">
                  <label>Motivo *</label>
                  <textarea value={nuevaForm.motivo}
                    onChange={(e) => setNuevaForm({ ...nuevaForm, motivo: e.target.value })} required />
                </div>
              </div>
              <div className="form-actions">
                <button type="submit" className="btn btn-primary">Registrar Solicitud</button>
                <button type="button" className="btn btn-secondary"
                  onClick={() => setNuevaForm({ tipoSolicitud: "", fechaInicio: "", fechaFin: "", motivo: "" })}>
                  Limpiar
                </button>
              </div>
            </form>
          </div>
        )}

        {/* ══ DECISIÓN GERENCIA ══ */}
        {tabActiva === "gerencia" && (
          <div className="section-card">
            <h2>Solicitudes para Decisión de Gerencia</h2>
            <div className="table-wrapper">
              {loading ? <p style={{ padding: "16px", color: "#888" }}>Cargando...</p> : (
                <table>
                  <thead>
                    <tr>
                      <th>#</th><th>Empleado</th><th>Tipo</th>
                      <th>Fechas</th><th>Días</th><th>Motivo</th>
                      <th>Estado</th><th>Decisión</th>
                    </tr>
                  </thead>
                  <tbody>
                    {solicitudes.length === 0 ? (
                      <tr><td colSpan={8} style={{ textAlign: "center", color: "#888" }}>No hay solicitudes para revisar</td></tr>
                    ) : solicitudes.map((s) => (
                      <tr key={s.idSolicitud}>
                        <td>{s.idSolicitud}</td>
                        <td>{s.nombreEmpleado ?? `Empleado ${s.idEmpleado}`}</td>
                        <td>{s.tipoSolicitud}</td>
                        <td>{s.fechaInicio} – {s.fechaFin}</td>
                        <td>{s.diasSolicitados}</td>
                        <td>{s.motivo}</td>
                        <td><span className={`badge ${ESTADOS_BADGE[s.estado] ?? "badge-gray"}`}>{s.estado}</span></td>
                        <td>
                          <button className="btn btn-success btn-sm" onClick={() => handleDecisionGerencia(s.idSolicitud, true)}>
                            Aprobar
                          </button>{" "}
                          <button className="btn btn-danger btn-sm" onClick={() => handleDecisionGerencia(s.idSolicitud, false)}>
                            Rechazar
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>

            {rechazoForm.idSolicitud && (
              <form onSubmit={handleConfirmarRechazo} style={{ marginTop: "20px" }}>
                <hr className="divider" />
                <h2>Motivo de Rechazo — Solicitud #{rechazoForm.idSolicitud}</h2>
                <div className="form-grid">
                  <div className="form-group form-full">
                    <label>Ingrese el motivo *</label>
                    <textarea value={rechazoForm.respuesta}
                      onChange={(e) => setRechazoForm({ ...rechazoForm, respuesta: e.target.value })} required />
                  </div>
                </div>
                <div className="form-actions">
                  <button type="submit" className="btn btn-danger">Confirmar Rechazo</button>
                  <button type="button" className="btn btn-secondary" onClick={() => setRechazoForm({ idSolicitud: null, respuesta: "" })}>
                    Cancelar
                  </button>
                </div>
              </form>
            )}
          </div>
        )}

        {/* ══ HISTORIAL ══ */}
        {tabActiva === "historial" && (
          <div className="section-card">
            <h2>Historial de Solicitudes Aprobadas</h2>
            <div className="table-wrapper">
              {loading ? <p style={{ padding: "16px", color: "#888" }}>Cargando...</p> : (
                <table>
                  <thead>
                    <tr>
                      <th>#</th><th>Empleado</th><th>Tipo</th>
                      <th>Fechas</th><th>Días</th><th>Estado</th><th>Respuesta</th>
                    </tr>
                  </thead>
                  <tbody>
                    {solicitudes.length === 0 ? (
                      <tr><td colSpan={7} style={{ textAlign: "center", color: "#888" }}>Sin registros</td></tr>
                    ) : solicitudes.map((s) => (
                      <tr key={s.idSolicitud}>
                        <td>{s.idSolicitud}</td>
                        <td>{s.nombreEmpleado ?? `Empleado ${s.idEmpleado}`}</td>
                        <td>{s.tipoSolicitud}</td>
                        <td>{s.fechaInicio} – {s.fechaFin}</td>
                        <td>{s.diasSolicitados}</td>
                        <td><span className={`badge ${ESTADOS_BADGE[s.estado] ?? "badge-gray"}`}>{s.estado}</span></td>
                        <td>{s.respuesta ?? s.observacionRrhh ?? "—"}</td>
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
