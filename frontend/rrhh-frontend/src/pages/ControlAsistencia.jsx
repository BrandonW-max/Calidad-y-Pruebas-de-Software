import { useState, useEffect } from "react";
import { useLocation } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import "../styles/Dashboard.css";
import {
  listarJustificacionesPendientes,
  registrarManual,
  revisarJustificacion,
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
  const [loading, setLoading]                 = useState(false);
  const [mensaje, setMensaje]                 = useState("");
  const [error, setError]                     = useState("");
  const [tabActiva, setTabActiva]             = useState(rol === "RRHH" ? "manual" : "misJustificaciones");

  const [manualForm, setManualForm] = useState({
    idEmpleado: "", tipo: "ENTRADA", fechaHora: "", motivo: "",
  });

  useEffect(() => {
    if (rol === "RRHH") cargarJustificaciones();
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
      setMensaje(`Justificación ${decision === "APROBADO" ? "aprobada" : "rechazada"} correctamente`);
      cargarJustificaciones();
    } catch {
      setMensaje("Error al revisar la justificación");
    }
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
              <div className="table-wrapper">
                <table>
                  <thead>
                    <tr>
                      <th>#</th>
                      <th>Fecha Tardanza</th>
                      <th>Minutos de Retraso</th>
                      <th>Motivo</th>
                      <th>Estado</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr>
                      <td colSpan={5}>
                        <div className="empty-state">
                          <div className="empty-state-icon">✅</div>
                          <p>No tienes justificaciones pendientes</p>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
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
                          <button className="btn btn-success btn-sm" onClick={() => handleRevisar(j.id ?? j.idJustificacion, "APROBADO")}>
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
      </main>
    </div>
  );
}
