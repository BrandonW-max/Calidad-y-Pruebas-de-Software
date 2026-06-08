import { useState, useEffect } from "react";
import { useLocation } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import "../styles/Dashboard.css";
import {
  registrarSolicitud,
  revisarSolicitudRRHH,
  decidirSolicitudGerencia,
  getSolicitudesPorEstado,
} from "../services/api";

const ESTADOS_BADGE = {
  PENDIENTE: "badge-gray",
  PROCESADO: "badge-purple",
  APROBADA:  "badge-success",
  RECHAZADA: "badge-danger",
  OBSERVADO: "badge-warning",
};

const ESTADOS_LABEL = {
  PENDIENTE: "Pendiente",
  PROCESADO: "Enviado a Gerencia",
  APROBADA:  "Aprobada",
  RECHAZADA: "Rechazada",
  OBSERVADO: "Con Observación",
};

function getRolFromPath(pathname) {
  if (pathname.startsWith("/empleado")) return "EMPLEADO";
  if (pathname.startsWith("/gerencia")) return "GERENCIA";
  return "RRHH";
}

export default function SolicitudPermisos() {
  const location = useLocation();
  const rol = getRolFromPath(location.pathname);

  const defaultTab = rol === "GERENCIA" ? "gerencia"
                   : rol === "EMPLEADO" ? "nueva"
                   : "pendientes";

  const [tabActiva, setTabActiva]     = useState(defaultTab);
  const [solicitudes, setSolicitudes] = useState([]);
  const [loading, setLoading]         = useState(false);
  const [mensaje, setMensaje]         = useState("");
  const [error, setError]             = useState("");

  const [nuevaForm, setNuevaForm] = useState({
    tipoSolicitud: "", fechaInicio: "", fechaFin: "", motivo: "",
  });

  const [rechazoForm, setRechazoForm] = useState({ idSolicitud: null, respuesta: "" });

  const estadoParaTab = {
    pendientes: "PENDIENTE",
    gerencia:   "PROCESADO",
    historial:  "APROBADA",
    misSolicitudes: "PENDIENTE",
  };

  useEffect(() => {
    const estado = estadoParaTab[tabActiva];
    if (estado) cargarSolicitudes(estado);
  }, [tabActiva]);

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
      setMensaje("Error al registrar la solicitud. Verifique los datos ingresados.");
    }
  };

  const handleEnviarGerencia = async (idSolicitud) => {
    setMensaje("");
    try {
      await revisarSolicitudRRHH(idSolicitud, true, "");
      setMensaje("Solicitud enviada a Gerencia correctamente");
      cargarSolicitudes("PENDIENTE");
    } catch {
      setMensaje("Error al enviar la solicitud a Gerencia");
    }
  };

  const handleObservar = async (idSolicitud) => {
    const obs = prompt("Ingrese la observación para devolver al empleado:");
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
      setMensaje("Solicitud aprobada correctamente. El saldo de vacaciones ha sido actualizado.");
      cargarSolicitudes("PROCESADO");
    } catch {
      setMensaje("Error al aprobar la solicitud");
    }
  };

  const handleConfirmarRechazo = async (e) => {
    e.preventDefault();
    try {
      await decidirSolicitudGerencia(rechazoForm.idSolicitud, false, rechazoForm.respuesta);
      setMensaje("Solicitud rechazada. El empleado recibirá una notificación con el motivo.");
      setRechazoForm({ idSolicitud: null, respuesta: "" });
      cargarSolicitudes("PROCESADO");
    } catch {
      setMensaje("Error al rechazar la solicitud");
    }
  };

  const diasSolicitados = () => {
    if (!nuevaForm.fechaInicio || !nuevaForm.fechaFin) return "—";
    const diff = Math.round((new Date(nuevaForm.fechaFin) - new Date(nuevaForm.fechaInicio)) / 86400000) + 1;
    return diff > 0 ? `${diff} día${diff > 1 ? "s" : ""}` : "—";
  };

  /* ─── Tabla reutilizable para RRHH/Gerencia ─── */
  const TablaSolicitudes = ({ cols, renderAcciones }) => (
    <div className="table-wrapper">
      {loading ? (
        <div className="loading-text">Cargando solicitudes...</div>
      ) : (
        <table>
          <thead>
            <tr>{cols.map((c) => <th key={c}>{c}</th>)}</tr>
          </thead>
          <tbody>
            {solicitudes.length === 0 ? (
              <tr>
                <td colSpan={cols.length}>
                  <div className="empty-state">
                    <div className="empty-state-icon">📅</div>
                    <p>No hay solicitudes para mostrar</p>
                  </div>
                </td>
              </tr>
            ) : solicitudes.map((s) => (
              <tr key={s.idSolicitud}>
                <td>{s.idSolicitud}</td>
                <td><strong>{s.nombreEmpleado ?? `Empleado ${s.idEmpleado}`}</strong></td>
                <td>{s.tipoSolicitud?.replace("_", " ")}</td>
                <td>{s.fechaInicio}</td>
                <td>{s.fechaFin}</td>
                <td>{s.diasSolicitados}</td>
                <td style={{ maxWidth: "200px", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{s.motivo}</td>
                <td>
                  <span className={`badge ${ESTADOS_BADGE[s.estado] ?? "badge-gray"}`}>
                    {ESTADOS_LABEL[s.estado] ?? s.estado}
                  </span>
                </td>
                {renderAcciones && <td style={{ display: "flex", gap: "6px" }}>{renderAcciones(s)}</td>}
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );

  /* ════════════════════════════════════════ */
  /*  VISTA EMPLEADO                          */
  /* ════════════════════════════════════════ */
  if (rol === "EMPLEADO") {
    return (
      <div className="dashboard">
        <Sidebar rol="EMPLEADO" />
        <main className="main-content">
          <header className="main-header header-empleado">
            <div className="header-left">
              <span className="page-breadcrumb">Mi Portal / Mis Solicitudes</span>
              <h1>Solicitudes de Permisos y Vacaciones</h1>
            </div>
            <span className="role-pill role-pill-empleado">ECU-05</span>
          </header>

          {mensaje && <div className="alert alert-success">✅ {mensaje}</div>}
          {error   && <div className="alert alert-warning">⚠️ {error}</div>}

          <div className="tab-bar">
            <button className={`tab ${tabActiva === "nueva" ? "active" : ""}`} onClick={() => setTabActiva("nueva")}>
              ➕ Nueva Solicitud
            </button>
            <button className={`tab ${tabActiva === "misSolicitudes" ? "active" : ""}`} onClick={() => setTabActiva("misSolicitudes")}>
              📋 Mis Solicitudes
            </button>
          </div>

          {tabActiva === "nueva" && (
            <div className="section-card">
              <h2>➕ Registrar Nueva Solicitud</h2>
              <div className="alert alert-info">
                Complete todos los campos. Su solicitud pasará primero por validación de RRHH y luego será evaluada por Gerencia.
              </div>
              <form onSubmit={handleNuevaSolicitud}>
                <div className="form-grid">
                  <div className="form-group">
                    <label>Tipo de Solicitud *</label>
                    <select
                      value={nuevaForm.tipoSolicitud}
                      onChange={(e) => setNuevaForm({ ...nuevaForm, tipoSolicitud: e.target.value })}
                      required
                    >
                      <option value="">Seleccione una opción</option>
                      <option value="VACACIONES">Vacaciones</option>
                      <option value="PERMISO_PERSONAL">Permiso personal</option>
                      <option value="PERMISO_MEDICO">Permiso médico</option>
                      <option value="OTROS">Otros</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Fecha de Inicio *</label>
                    <input
                      type="date"
                      value={nuevaForm.fechaInicio}
                      onChange={(e) => setNuevaForm({ ...nuevaForm, fechaInicio: e.target.value })}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label>Fecha de Fin *</label>
                    <input
                      type="date"
                      value={nuevaForm.fechaFin}
                      onChange={(e) => setNuevaForm({ ...nuevaForm, fechaFin: e.target.value })}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label>Días Totales</label>
                    <input value={diasSolicitados()} readOnly style={{ background: "#f8fafc", cursor: "default" }} />
                  </div>
                  <div className="form-group form-full">
                    <label>Motivo de la solicitud *</label>
                    <textarea
                      value={nuevaForm.motivo}
                      onChange={(e) => setNuevaForm({ ...nuevaForm, motivo: e.target.value })}
                      placeholder="Describa el motivo de su solicitud..."
                      required
                    />
                  </div>
                </div>
                <div className="form-actions">
                  <button type="submit" className="btn btn-primary">📤 Registrar Solicitud</button>
                  <button
                    type="button"
                    className="btn btn-secondary"
                    onClick={() => setNuevaForm({ tipoSolicitud: "", fechaInicio: "", fechaFin: "", motivo: "" })}
                  >
                    Limpiar
                  </button>
                </div>
              </form>
            </div>
          )}

          {tabActiva === "misSolicitudes" && (
            <div className="section-card">
              <h2>📋 Mis Solicitudes</h2>
              <TablaSolicitudes
                cols={["#", "Empleado", "Tipo", "Inicio", "Fin", "Días", "Motivo", "Estado"]}
                renderAcciones={null}
              />
            </div>
          )}
        </main>
      </div>
    );
  }

  /* ════════════════════════════════════════ */
  /*  VISTA GERENCIA                          */
  /* ════════════════════════════════════════ */
  if (rol === "GERENCIA") {
    return (
      <div className="dashboard">
        <Sidebar rol="GERENCIA" />
        <main className="main-content">
          <header className="main-header header-gerencia">
            <div className="header-left">
              <span className="page-breadcrumb">Gerencia / Solicitudes</span>
              <h1>Decisión de Solicitudes</h1>
            </div>
            <span className="role-pill role-pill-gerencia">ECU-05</span>
          </header>

          {mensaje && <div className="alert alert-success">✅ {mensaje}</div>}
          {error   && <div className="alert alert-warning">⚠️ {error}</div>}

          <div className="tab-bar">
            <button className={`tab ${tabActiva === "gerencia" ? "active" : ""}`} onClick={() => setTabActiva("gerencia")}>
              ⏳ Por Aprobar
            </button>
            <button className={`tab ${tabActiva === "historial" ? "active" : ""}`} onClick={() => setTabActiva("historial")}>
              📋 Historial
            </button>
          </div>

          {tabActiva === "gerencia" && (
            <div className="section-card">
              <h2>⏳ Solicitudes Enviadas por RRHH — Requieren su Decisión</h2>
              <div className="alert alert-info">
                Estas solicitudes ya fueron validadas por RRHH. Revise la información del empleado y tome su decisión.
                Al rechazar, deberá ingresar el motivo que será notificado al empleado.
              </div>
              <TablaSolicitudes
                cols={["#", "Empleado", "Tipo", "Inicio", "Fin", "Días", "Motivo", "Estado", "Decisión"]}
                renderAcciones={(s) => (
                  <>
                    <button className="btn btn-success btn-sm" onClick={() => handleDecisionGerencia(s.idSolicitud, true)}>
                      ✅ Aprobar
                    </button>
                    <button className="btn btn-danger btn-sm" onClick={() => handleDecisionGerencia(s.idSolicitud, false)}>
                      ❌ Rechazar
                    </button>
                  </>
                )}
              />

              {rechazoForm.idSolicitud && (
                <form onSubmit={handleConfirmarRechazo} style={{ marginTop: "20px" }}>
                  <hr className="divider" />
                  <h2>❌ Motivo de Rechazo — Solicitud #{rechazoForm.idSolicitud}</h2>
                  <div className="alert alert-danger">
                    Este motivo será notificado automáticamente al empleado.
                  </div>
                  <div className="form-grid">
                    <div className="form-group form-full">
                      <label>Ingrese el motivo de rechazo *</label>
                      <textarea
                        value={rechazoForm.respuesta}
                        onChange={(e) => setRechazoForm({ ...rechazoForm, respuesta: e.target.value })}
                        placeholder="Explique el motivo del rechazo..."
                        required
                      />
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

          {tabActiva === "historial" && (
            <div className="section-card">
              <h2>📋 Historial de Solicitudes Aprobadas</h2>
              <TablaSolicitudes
                cols={["#", "Empleado", "Tipo", "Inicio", "Fin", "Días", "Motivo", "Estado"]}
                renderAcciones={null}
              />
            </div>
          )}
        </main>
      </div>
    );
  }

  /* ════════════════════════════════════════ */
  /*  VISTA RRHH (por defecto)               */
  /* ════════════════════════════════════════ */
  return (
    <div className="dashboard">
      <Sidebar rol="RRHH" />
      <main className="main-content">
        <header className="main-header header-rrhh">
          <div className="header-left">
            <span className="page-breadcrumb">RRHH / Solicitudes de Permisos y Vacaciones</span>
            <h1>Gestión de Solicitudes</h1>
          </div>
          <span className="role-pill role-pill-rrhh">ECU-05</span>
        </header>

        {mensaje && <div className="alert alert-success">✅ {mensaje}</div>}
        {error   && <div className="alert alert-warning">⚠️ {error}</div>}

        <div className="tab-bar">
          <button className={`tab ${tabActiva === "pendientes" ? "active" : ""}`} onClick={() => setTabActiva("pendientes")}>
            📥 Pendientes (RRHH)
          </button>
          <button className={`tab ${tabActiva === "nueva" ? "active" : ""}`} onClick={() => setTabActiva("nueva")}>
            ➕ Nueva Solicitud
          </button>
          <button className={`tab ${tabActiva === "gerencia" ? "active" : ""}`} onClick={() => setTabActiva("gerencia")}>
            📤 Enviadas a Gerencia
          </button>
          <button className={`tab ${tabActiva === "historial" ? "active" : ""}`} onClick={() => setTabActiva("historial")}>
            📋 Historial
          </button>
        </div>

        {/* ── PENDIENTES RRHH ── */}
        {tabActiva === "pendientes" && (
          <div className="section-card">
            <h2>📥 Solicitudes Pendientes de Validación RRHH</h2>
            <div className="alert alert-info">
              Valide los datos de cada solicitud. Si todo es correcto, envíe a Gerencia. Si hay inconsistencias, devuelva al empleado con una observación.
            </div>
            <TablaSolicitudes
              cols={["#", "Empleado", "Tipo", "Inicio", "Fin", "Días", "Motivo", "Estado", "Acción RRHH"]}
              renderAcciones={(s) => (
                <>
                  <button className="btn btn-primary btn-sm" onClick={() => handleEnviarGerencia(s.idSolicitud)}>
                    📤 Enviar a Gerencia
                  </button>
                  <button className="btn btn-warning btn-sm" onClick={() => handleObservar(s.idSolicitud)}>
                    👁 Observar
                  </button>
                </>
              )}
            />
          </div>
        )}

        {/* ── NUEVA SOLICITUD ── */}
        {tabActiva === "nueva" && (
          <div className="section-card">
            <h2>➕ Registrar Nueva Solicitud</h2>
            <div className="alert alert-info">
              Registre una solicitud en nombre de un empleado. El estado inicial será PENDIENTE para revisión de RRHH.
            </div>
            <form onSubmit={handleNuevaSolicitud}>
              <div className="form-grid">
                <div className="form-group">
                  <label>Tipo de Solicitud *</label>
                  <select
                    value={nuevaForm.tipoSolicitud}
                    onChange={(e) => setNuevaForm({ ...nuevaForm, tipoSolicitud: e.target.value })}
                    required
                  >
                    <option value="">Seleccione una opción</option>
                    <option value="VACACIONES">Vacaciones</option>
                    <option value="PERMISO_PERSONAL">Permiso personal</option>
                    <option value="PERMISO_MEDICO">Permiso médico</option>
                    <option value="OTROS">Otros</option>
                  </select>
                </div>
                <div className="form-group">
                  <label>Fecha de Inicio *</label>
                  <input type="date" value={nuevaForm.fechaInicio} onChange={(e) => setNuevaForm({ ...nuevaForm, fechaInicio: e.target.value })} required />
                </div>
                <div className="form-group">
                  <label>Fecha de Fin *</label>
                  <input type="date" value={nuevaForm.fechaFin} onChange={(e) => setNuevaForm({ ...nuevaForm, fechaFin: e.target.value })} required />
                </div>
                <div className="form-group">
                  <label>Días Totales</label>
                  <input value={diasSolicitados()} readOnly style={{ background: "#f8fafc", cursor: "default" }} />
                </div>
                <div className="form-group form-full">
                  <label>Motivo *</label>
                  <textarea value={nuevaForm.motivo} onChange={(e) => setNuevaForm({ ...nuevaForm, motivo: e.target.value })} placeholder="Describa el motivo..." required />
                </div>
              </div>
              <div className="form-actions">
                <button type="submit" className="btn btn-primary">📤 Registrar Solicitud</button>
                <button type="button" className="btn btn-secondary" onClick={() => setNuevaForm({ tipoSolicitud: "", fechaInicio: "", fechaFin: "", motivo: "" })}>Limpiar</button>
              </div>
            </form>
          </div>
        )}

        {/* ── ENVIADAS A GERENCIA ── */}
        {tabActiva === "gerencia" && (
          <div className="section-card">
            <h2>📤 Solicitudes Enviadas a Gerencia — En espera de decisión</h2>
            <div className="alert alert-info">
              Estas solicitudes ya fueron validadas por RRHH y están pendientes de la decisión de Gerencia.
            </div>
            <TablaSolicitudes
              cols={["#", "Empleado", "Tipo", "Inicio", "Fin", "Días", "Motivo", "Estado"]}
              renderAcciones={null}
            />
          </div>
        )}

        {/* ── HISTORIAL ── */}
        {tabActiva === "historial" && (
          <div className="section-card">
            <h2>📋 Historial de Solicitudes Aprobadas</h2>
            <TablaSolicitudes
              cols={["#", "Empleado", "Tipo", "Inicio", "Fin", "Días", "Motivo", "Estado"]}
              renderAcciones={null}
            />
          </div>
        )}
      </main>
    </div>
  );
}
