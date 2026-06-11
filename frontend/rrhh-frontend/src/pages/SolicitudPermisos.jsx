import { useState, useEffect } from "react";
import { useLocation } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import "../styles/Dashboard.css";
import {
  registrarSolicitud,
  revisarSolicitudRRHH,
  decidirSolicitudGerencia,
  getSolicitudesPorEstado,
  getSolicitudesPorEmpleado,
  getCurrentUserId,
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

  // Modals
  const [modalObservar, setModalObservar]             = useState({ open: false, id: null, obs: "" });
  const [modalEnviarGerencia, setModalEnviarGerencia] = useState({ open: false, id: null });
  const [modalAprobar, setModalAprobar]               = useState({ open: false, id: null });
  const [modalRechazo, setModalRechazo]               = useState({ open: false, id: null, respuesta: "" });
  const [modalDetalle, setModalDetalle]               = useState(null);

  const estadoParaTab = {
    pendientes:     "PENDIENTE",
    gerencia:       "PROCESADO",
  };

  useEffect(() => {
    const estado = estadoParaTab[tabActiva];
    if (estado) cargarSolicitudes(estado);
    if (tabActiva === "misSolicitudes") cargarMisSolicitudes();
    if (tabActiva === "historial") cargarHistorial();
  }, [tabActiva]);

  useEffect(() => {
    const intervalo = setInterval(() => {
      const estado = estadoParaTab[tabActiva];
      if (estado) cargarSolicitudes(estado);
      if (tabActiva === "misSolicitudes") cargarMisSolicitudes();
      if (tabActiva === "historial") cargarHistorial();
    }, 30000);
    return () => clearInterval(intervalo);
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

  const cargarMisSolicitudes = async () => {
    setLoading(true);
    setError("");
    try {
      const idUsuario = getCurrentUserId();
      const data = await getSolicitudesPorEmpleado(idUsuario);
      setSolicitudes(Array.isArray(data) ? data : []);
    } catch {
      setError("No se pudieron cargar tus solicitudes");
    } finally {
      setLoading(false);
    }
  };

  const cargarHistorial = async () => {
    setLoading(true);
    setError("");
    try {
      const [aprobadas, rechazadas] = await Promise.all([
        getSolicitudesPorEstado("APROBADA"),
        getSolicitudesPorEstado("RECHAZADA"),
      ]);
      setSolicitudes([...(Array.isArray(aprobadas) ? aprobadas : []), ...(Array.isArray(rechazadas) ? rechazadas : [])]);
    } catch {
      setError("No se pudo cargar el historial");
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

  // RRHH — Enviar a Gerencia (modal de confirmación)
  const abrirEnviarGerencia = (id) => setModalEnviarGerencia({ open: true, id });
  const confirmarEnviarGerencia = async () => {
    try {
      await revisarSolicitudRRHH(modalEnviarGerencia.id, true, "");
      setMensaje("Solicitud enviada a Gerencia correctamente");
      setModalEnviarGerencia({ open: false, id: null });
      cargarSolicitudes("PENDIENTE");
    } catch {
      setMensaje("Error al enviar la solicitud a Gerencia");
      setModalEnviarGerencia({ open: false, id: null });
    }
  };

  // RRHH — Observar (modal)
  const handleObservar = (id) => setModalObservar({ open: true, id, obs: "" });
  const confirmarObservar = async () => {
    if (!modalObservar.obs.trim()) return;
    try {
      await revisarSolicitudRRHH(modalObservar.id, false, modalObservar.obs);
      setMensaje("Solicitud devuelta al empleado con observación");
      setModalObservar({ open: false, id: null, obs: "" });
      cargarSolicitudes("PENDIENTE");
    } catch {
      setMensaje("Error al observar la solicitud");
      setModalObservar({ open: false, id: null, obs: "" });
    }
  };

  // Gerencia — Aprobar (modal de confirmación)
  const handleAprobar = (id) => setModalAprobar({ open: true, id });
  const confirmarAprobar = async () => {
    try {
      await decidirSolicitudGerencia(modalAprobar.id, true, "");
      setMensaje("Solicitud aprobada correctamente. El saldo de vacaciones ha sido actualizado.");
      setModalAprobar({ open: false, id: null });
      cargarSolicitudes("PROCESADO");
    } catch {
      setMensaje("Error al aprobar la solicitud");
      setModalAprobar({ open: false, id: null });
    }
  };

  // Gerencia — Rechazar (modal con motivo)
  const handleRechazar = (id) => setModalRechazo({ open: true, id, respuesta: "" });
  const confirmarRechazo = async () => {
    if (!modalRechazo.respuesta.trim()) return;
    try {
      await decidirSolicitudGerencia(modalRechazo.id, false, modalRechazo.respuesta);
      setMensaje("Solicitud rechazada. El empleado recibirá una notificación con el motivo.");
      setModalRechazo({ open: false, id: null, respuesta: "" });
      cargarSolicitudes("PROCESADO");
    } catch {
      setMensaje("Error al rechazar la solicitud");
      setModalRechazo({ open: false, id: null, respuesta: "" });
    }
  };

  const diasSolicitados = () => {
    if (!nuevaForm.fechaInicio || !nuevaForm.fechaFin) return "—";
    const inicio = new Date(nuevaForm.fechaInicio);
    const fin = new Date(nuevaForm.fechaFin);
    if (fin < inicio) return "—";
    let count = 0;
    const current = new Date(inicio);
    while (current <= fin) {
      const day = current.getDay();
      if (day !== 0 && day !== 6) count++;
      current.setDate(current.getDate() + 1);
    }
    return `${count} día${count !== 1 ? "s" : ""}`;
  };

  /* ─── Tabla reutilizable ─── */
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
                <td style={{ maxWidth: "200px", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                  {s.motivo}
                </td>
                <td>
                  <span className={`badge ${ESTADOS_BADGE[s.estado] ?? "badge-gray"}`}>
                    {ESTADOS_LABEL[s.estado] ?? s.estado}
                  </span>
                </td>
                {renderAcciones && (
                  <td style={{ display: "flex", gap: "6px" }}>
                    <button className="btn btn-secondary btn-sm" onClick={() => setModalDetalle(s)}>
                      🔍 Ver
                    </button>
                    {renderAcciones(s)}
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );

  /* ─── Formulario nueva solicitud (reutilizable) ─── */
  const FormSolicitud = ({ titulo, alerta }) => (
    <div className="section-card">
      <h2>{titulo}</h2>
      <div className="alert alert-info">{alerta}</div>
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
  );

  /* ─── Config de encabezado por rol ─── */
  const headerCfg = {
    EMPLEADO: { breadcrumb: "Mi Portal / Mis Solicitudes",                    title: "Solicitudes de Permisos y Vacaciones", cls: "header-empleado", pill: "role-pill-empleado" },
    GERENCIA: { breadcrumb: "Gerencia / Solicitudes",                         title: "Decisión de Solicitudes",             cls: "header-gerencia", pill: "role-pill-gerencia" },
    RRHH:     { breadcrumb: "RRHH / Solicitudes de Permisos y Vacaciones",    title: "Gestión de Solicitudes",              cls: "header-rrhh",     pill: "role-pill-rrhh"     },
  }[rol];

  return (
    <div className="dashboard">
      <Sidebar rol={rol} />

      <main className="main-content">

        <header className={`main-header ${headerCfg.cls}`}>
          <div className="header-left">
            <span className="page-breadcrumb">{headerCfg.breadcrumb}</span>
            <h1>{headerCfg.title}</h1>
          </div>
          <span className={`role-pill ${headerCfg.pill}`}>ECU-05</span>
        </header>

        {mensaje && <div className="alert alert-success">✅ {mensaje}</div>}
        {error   && <div className="alert alert-warning">⚠️ {error}</div>}

        {/* ══ VISTA EMPLEADO ══ */}
        {rol === "EMPLEADO" && (
          <>
            <div className="tab-bar">
              <button className={`tab ${tabActiva === "nueva" ? "active" : ""}`} onClick={() => setTabActiva("nueva")}>
                ➕ Nueva Solicitud
              </button>
              <button className={`tab ${tabActiva === "misSolicitudes" ? "active" : ""}`} onClick={() => setTabActiva("misSolicitudes")}>
                📋 Mis Solicitudes
              </button>
            </div>

            {tabActiva === "nueva" && (
              <FormSolicitud
                titulo="➕ Registrar Nueva Solicitud"
                alerta="Complete todos los campos. Su solicitud pasará primero por validación de RRHH y luego será evaluada por Gerencia."
              />
            )}

            {tabActiva === "misSolicitudes" && (
              <div className="section-card">
                <h2>📋 Mis Solicitudes</h2>
                <TablaSolicitudes
                  cols={["#", "Empleado", "Tipo", "Inicio", "Fin", "Días", "Motivo", "Estado", "Acciones"]}
                  renderAcciones={() => null}
                />
              </div>
            )}
          </>
        )}

        {/* ══ VISTA GERENCIA ══ */}
        {rol === "GERENCIA" && (
          <>
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
                  Estas solicitudes ya fueron validadas por RRHH. Revise el detalle de cada una y tome su decisión.
                  Al rechazar, deberá ingresar el motivo que será notificado al empleado.
                </div>
                <TablaSolicitudes
                  cols={["#", "Empleado", "Tipo", "Inicio", "Fin", "Días", "Motivo", "Estado", "Decisión"]}
                  renderAcciones={(s) => (
                    <>
                      <button className="btn btn-success btn-sm" onClick={() => handleAprobar(s.idSolicitud)}>
                        ✅ Aprobar
                      </button>
                      <button className="btn btn-danger btn-sm" onClick={() => handleRechazar(s.idSolicitud)}>
                        ❌ Rechazar
                      </button>
                    </>
                  )}
                />
              </div>
            )}

            {tabActiva === "historial" && (
              <div className="section-card">
                <h2>📋 Historial de Solicitudes Resueltas</h2>
                <TablaSolicitudes
                  cols={["#", "Empleado", "Tipo", "Inicio", "Fin", "Días", "Motivo", "Estado", "Detalle"]}
                  renderAcciones={() => null}
                />
              </div>
            )}
          </>
        )}

        {/* ══ VISTA RRHH ══ */}
        {rol === "RRHH" && (
          <>
            <div className="tab-bar">
              <button className={`tab ${tabActiva === "pendientes" ? "active" : ""}`} onClick={() => setTabActiva("pendientes")}>
                📥 Pendientes (RRHH)
              </button>
              <button className={`tab ${tabActiva === "gerencia" ? "active" : ""}`} onClick={() => setTabActiva("gerencia")}>
                📤 Enviadas a Gerencia
              </button>
              <button className={`tab ${tabActiva === "historial" ? "active" : ""}`} onClick={() => setTabActiva("historial")}>
                📋 Historial
              </button>
            </div>

            {tabActiva === "pendientes" && (
              <div className="section-card">
                <h2>📥 Solicitudes Pendientes de Validación RRHH</h2>
                <div className="alert alert-info">
                  Valide los datos de cada solicitud. Si todo es correcto, envíe a Gerencia.
                  Si hay inconsistencias, devuelva al empleado con una observación.
                </div>
                <TablaSolicitudes
                  cols={["#", "Empleado", "Tipo", "Inicio", "Fin", "Días", "Motivo", "Estado", "Acción RRHH"]}
                  renderAcciones={(s) => (
                    <>
                      <button className="btn btn-primary btn-sm" onClick={() => abrirEnviarGerencia(s.idSolicitud)}>
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

            {tabActiva === "gerencia" && (
              <div className="section-card">
                <h2>📤 Solicitudes Enviadas a Gerencia — En espera de decisión</h2>
                <div className="alert alert-info">
                  Estas solicitudes ya fueron validadas por RRHH y están pendientes de la decisión de Gerencia.
                </div>
                <TablaSolicitudes
                  cols={["#", "Empleado", "Tipo", "Inicio", "Fin", "Días", "Motivo", "Estado", "Detalle"]}
                  renderAcciones={() => null}
                />
              </div>
            )}

            {tabActiva === "historial" && (
              <div className="section-card">
                <h2>📋 Historial de Solicitudes Aprobadas</h2>
                <TablaSolicitudes
                  cols={["#", "Empleado", "Tipo", "Inicio", "Fin", "Días", "Motivo", "Estado", "Detalle"]}
                  renderAcciones={() => null}
                />
              </div>
            )}
          </>
        )}

        {/* ══ MODAL: VER DETALLE ══ */}
        {modalDetalle && (
          <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && setModalDetalle(null)}>
            <div className="modal-box">
              <div className="modal-header">
                <div>
                  <h3>📋 Detalle de Solicitud #{modalDetalle.idSolicitud}</h3>
                  <p>{modalDetalle.nombreEmpleado ?? `Empleado ${modalDetalle.idEmpleado}`}</p>
                </div>
                <button className="modal-close" onClick={() => setModalDetalle(null)}>×</button>
              </div>
              <div className="modal-body">
                <div className="form-grid">
                  <div className="form-group">
                    <label style={{ fontSize: "11px", textTransform: "uppercase", letterSpacing: "0.4px", color: "#94a3b8" }}>Tipo</label>
                    <p style={{ fontWeight: 600 }}>{modalDetalle.tipoSolicitud?.replace("_", " ")}</p>
                  </div>
                  <div className="form-group">
                    <label style={{ fontSize: "11px", textTransform: "uppercase", letterSpacing: "0.4px", color: "#94a3b8" }}>Estado</label>
                    <span className={`badge ${ESTADOS_BADGE[modalDetalle.estado] ?? "badge-gray"}`}>
                      {ESTADOS_LABEL[modalDetalle.estado] ?? modalDetalle.estado}
                    </span>
                  </div>
                  <div className="form-group">
                    <label style={{ fontSize: "11px", textTransform: "uppercase", letterSpacing: "0.4px", color: "#94a3b8" }}>Fecha Inicio</label>
                    <p style={{ fontWeight: 600 }}>{modalDetalle.fechaInicio}</p>
                  </div>
                  <div className="form-group">
                    <label style={{ fontSize: "11px", textTransform: "uppercase", letterSpacing: "0.4px", color: "#94a3b8" }}>Fecha Fin</label>
                    <p style={{ fontWeight: 600 }}>{modalDetalle.fechaFin}</p>
                  </div>
                  <div className="form-group">
                    <label style={{ fontSize: "11px", textTransform: "uppercase", letterSpacing: "0.4px", color: "#94a3b8" }}>Días Solicitados</label>
                    <p style={{ fontWeight: 700, fontSize: "20px", color: "#2563eb" }}>{modalDetalle.diasSolicitados}</p>
                  </div>
                </div>
                <div className="form-group" style={{ marginTop: "8px" }}>
                  <label style={{ fontSize: "11px", textTransform: "uppercase", letterSpacing: "0.4px", color: "#94a3b8" }}>Motivo</label>
                  <div style={{ background: "#f8fafc", borderRadius: "8px", padding: "12px 14px", fontSize: "13.5px", color: "#1e293b", border: "1px solid #e2e8f0" }}>
                    {modalDetalle.motivo || "—"}
                  </div>
                </div>
                {modalDetalle.observacionRrhh && (
                  <div className="alert alert-warning" style={{ marginTop: "12px" }}>
                    <strong>Observación RRHH:</strong> {modalDetalle.observacionRrhh}
                  </div>
                )}
                {modalDetalle.respuesta && (
                  <div className="alert alert-info" style={{ marginTop: "12px" }}>
                    <strong>Respuesta Gerencia:</strong> {modalDetalle.respuesta}
                  </div>
                )}
              </div>
              <div className="modal-footer">
                <button className="btn btn-secondary" onClick={() => setModalDetalle(null)}>Cerrar</button>
              </div>
            </div>
          </div>
        )}

        {/* ══ MODAL: OBSERVAR ══ */}
        {modalObservar.open && (
          <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && setModalObservar({ open: false, id: null, obs: "" })}>
            <div className="modal-box">
              <div className="modal-header">
                <div>
                  <h3>👁 Devolver con Observación</h3>
                  <p>Solicitud #{modalObservar.id} — será devuelta al empleado.</p>
                </div>
                <button className="modal-close" onClick={() => setModalObservar({ open: false, id: null, obs: "" })}>×</button>
              </div>
              <div className="modal-body">
                <div className="form-group">
                  <label>Motivo de la observación *</label>
                  <textarea
                    rows={3}
                    value={modalObservar.obs}
                    onChange={(e) => setModalObservar({ ...modalObservar, obs: e.target.value })}
                    placeholder="Explique qué debe corregir o completar el empleado..."
                    autoFocus
                  />
                </div>
              </div>
              <div className="modal-footer">
                <button className="btn btn-secondary" onClick={() => setModalObservar({ open: false, id: null, obs: "" })}>Cancelar</button>
                <button className="btn btn-warning" onClick={confirmarObservar} disabled={!modalObservar.obs.trim()}>
                  👁 Enviar Observación
                </button>
              </div>
            </div>
          </div>
        )}

        {/* ══ MODAL: ENVIAR A GERENCIA ══ */}
        {modalEnviarGerencia.open && (
          <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && setModalEnviarGerencia({ open: false, id: null })}>
            <div className="modal-box">
              <div className="modal-header">
                <div>
                  <h3>📤 Enviar a Gerencia</h3>
                  <p>Solicitud #{modalEnviarGerencia.id}</p>
                </div>
                <button className="modal-close" onClick={() => setModalEnviarGerencia({ open: false, id: null })}>×</button>
              </div>
              <div className="modal-body">
                <p style={{ fontSize: "14px", color: "#475569" }}>
                  ¿Desea enviar esta solicitud a Gerencia para su aprobación?
                  La solicitud cambiará de estado a <strong>"Enviado a Gerencia"</strong>.
                </p>
              </div>
              <div className="modal-footer">
                <button className="btn btn-secondary" onClick={() => setModalEnviarGerencia({ open: false, id: null })}>Cancelar</button>
                <button className="btn btn-primary" onClick={confirmarEnviarGerencia}>📤 Confirmar Envío</button>
              </div>
            </div>
          </div>
        )}

        {/* ══ MODAL: APROBAR ══ */}
        {modalAprobar.open && (
          <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && setModalAprobar({ open: false, id: null })}>
            <div className="modal-box">
              <div className="modal-header">
                <div>
                  <h3>✅ Aprobar Solicitud</h3>
                  <p>Solicitud #{modalAprobar.id}</p>
                </div>
                <button className="modal-close" onClick={() => setModalAprobar({ open: false, id: null })}>×</button>
              </div>
              <div className="modal-body">
                <p style={{ fontSize: "14px", color: "#475569" }}>
                  ¿Confirma la aprobación de esta solicitud?
                  El saldo de vacaciones del empleado se actualizará automáticamente y recibirá una notificación.
                </p>
              </div>
              <div className="modal-footer">
                <button className="btn btn-secondary" onClick={() => setModalAprobar({ open: false, id: null })}>Cancelar</button>
                <button className="btn btn-success" onClick={confirmarAprobar}>✅ Confirmar Aprobación</button>
              </div>
            </div>
          </div>
        )}

        {/* ══ MODAL: RECHAZAR ══ */}
        {modalRechazo.open && (
          <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && setModalRechazo({ open: false, id: null, respuesta: "" })}>
            <div className="modal-box">
              <div className="modal-header">
                <div>
                  <h3>❌ Rechazar Solicitud</h3>
                  <p>Solicitud #{modalRechazo.id} — el empleado recibirá el motivo.</p>
                </div>
                <button className="modal-close" onClick={() => setModalRechazo({ open: false, id: null, respuesta: "" })}>×</button>
              </div>
              <div className="modal-body">
                <div className="alert alert-danger" style={{ marginBottom: "14px" }}>
                  Este motivo será notificado automáticamente al empleado vía el sistema.
                </div>
                <div className="form-group">
                  <label>Motivo de rechazo *</label>
                  <textarea
                    rows={3}
                    value={modalRechazo.respuesta}
                    onChange={(e) => setModalRechazo({ ...modalRechazo, respuesta: e.target.value })}
                    placeholder="Explique el motivo del rechazo..."
                    autoFocus
                  />
                </div>
              </div>
              <div className="modal-footer">
                <button className="btn btn-secondary" onClick={() => setModalRechazo({ open: false, id: null, respuesta: "" })}>Cancelar</button>
                <button className="btn btn-danger" onClick={confirmarRechazo} disabled={!modalRechazo.respuesta.trim()}>
                  ❌ Confirmar Rechazo
                </button>
              </div>
            </div>
          </div>
        )}

      </main>
    </div>
  );
}
