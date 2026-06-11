import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import "../styles/Dashboard.css";
import { getSolicitudesPorEmpleado, getCurrentUserId } from "../services/api";

export default function DashboardEmpleado() {
  const [notificaciones, setNotificaciones] = useState([]);
  const [leidas, setLeidas] = useState(() => JSON.parse(sessionStorage.getItem("notifLeidas") || "[]"));

  const marcarLeida = (id) => {
    if (!leidas.includes(id)) {
      const nuevas = [...leidas, id];
      setLeidas(nuevas);
      sessionStorage.setItem("notifLeidas", JSON.stringify(nuevas));
    }
  };

  const dismissNotif = (id) => {
    setNotificaciones((prev) => prev.filter((n) => n.idSolicitud !== id));
  };

  useEffect(() => {
    const id = getCurrentUserId();
    if (!id) return;
    const fetchNotifs = () => {
      getSolicitudesPorEmpleado(id).then((data) => {
        setNotificaciones((Array.isArray(data) ? data : [])
          .filter((s) => s.estado === "APROBADA" || s.estado === "RECHAZADA")
          .slice(0, 5));
      }).catch(() => {});
    };
    fetchNotifs();
    const intervalo = setInterval(fetchNotifs, 30000);
    return () => clearInterval(intervalo);
  }, []);
  return (
    <div className="dashboard">
      <Sidebar rol="EMPLEADO" />

      <main className="main-content">
        <header className="main-header header-empleado">
          <div className="header-left">
            <span className="page-breadcrumb">Portal del Empleado</span>
            <h1>Bienvenido</h1>
          </div>
          <span className="role-pill role-pill-empleado">Mi Cuenta</span>
        </header>

        <div className="stats-row">
          <div className="stat-box accent-teal">
            <div className="stat-icon">📅</div>
            <div className="stat-info">
              <div className="stat-value">—</div>
              <div className="stat-label">Días Asistidos (Mes)</div>
            </div>
          </div>
          <div className="stat-box accent-orange">
            <div className="stat-icon">⏰</div>
            <div className="stat-info">
              <div className="stat-value">—</div>
              <div className="stat-label">Tardanzas (Mes)</div>
            </div>
          </div>
          <div className="stat-box accent-green">
            <div className="stat-icon">🌴</div>
            <div className="stat-info">
              <div className="stat-value">—</div>
              <div className="stat-label">Días de Vacaciones</div>
            </div>
          </div>
          <div className="stat-box accent-blue">
            <div className="stat-icon">📋</div>
            <div className="stat-info">
              <div className="stat-value">—</div>
              <div className="stat-label">Solicitudes Activas</div>
            </div>
          </div>
        </div>

        <div className="cards-grid">
          <Link to="/empleado/asistencia" className="card">
            <span className="card-icon">📋</span>
            <h3>Mi Asistencia</h3>
            <p>Consultar registros de asistencia y justificaciones</p>
          </Link>
          <Link to="/empleado/solicitudes" className="card">
            <span className="card-icon">📅</span>
            <h3>Mis Solicitudes</h3>
            <p>Solicitar permisos y vacaciones</p>
          </Link>
        </div>

        <div className="section-card">
          <h2>📋 Asistencia Reciente</h2>
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Fecha</th>
                  <th>Entrada</th>
                  <th>Salida</th>
                  <th>Horas Trabajadas</th>
                  <th>Estado</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td colSpan={5}>
                    <div className="empty-state">
                      <div className="empty-state-icon">📋</div>
                      <p>Los registros de asistencia se mostrarán aquí cuando el sistema esté conectado.</p>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div className="section-card">
          <h2>ℹ️ Información importante</h2>
          <div className="alert alert-info">
            🖐️ Tu registro de entrada y salida se realiza mediante el <strong>dispositivo biométrico</strong> de huella dactilar ubicado en la entrada.
          </div>
          <div className="alert alert-warning">
            Si tienes una tardanza, dispones de <strong>1 día</strong> para registrar tu justificación desde la sección "Mi Asistencia".
          </div>
        </div>

        {notificaciones.length > 0 && (
          <div className="section-card">
            <h2>🔔 Notificaciones Recientes
              {notificaciones.filter(n => !leidas.includes(n.idSolicitud)).length > 0 && (
                <span className="badge badge-danger" style={{ marginLeft: "8px" }}>
                  {notificaciones.filter(n => !leidas.includes(n.idSolicitud)).length} nueva(s)
                </span>
              )}
            </h2>
            {notificaciones.map((s) => {
              const esNueva = !leidas.includes(s.idSolicitud);
              return (
                <Link
                  key={s.idSolicitud}
                  to="/empleado/solicitudes"
                  onClick={() => marcarLeida(s.idSolicitud)}
                  className={`alert ${s.estado === "APROBADA" ? "alert-success" : "alert-danger"}`}
                  style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "8px", textDecoration: "none", cursor: "pointer", ...(esNueva ? { borderLeft: "4px solid #2563eb" } : {}) }}
                >
                  <div style={{ flex: 1 }}>
                    <strong>{s.estado === "APROBADA" ? "✅" : "❌"} Solicitud {s.estado === "APROBADA" ? "Aprobada" : "Rechazada"}</strong>
                    {esNueva && <span className="badge badge-info" style={{ marginLeft: "8px" }}>Nuevo</span>}
                    <p style={{ margin: "4px 0 0", fontSize: "13px" }}>
                      {s.tipoSolicitud?.replace("_", " ")} — {s.fechaInicio} al {s.fechaFin}
                      {s.respuesta && <span> — Respuesta: {s.respuesta}</span>}
                    </p>
                  </div>
                  <button
                    onClick={(e) => { e.preventDefault(); e.stopPropagation(); marcarLeida(s.idSolicitud); dismissNotif(s.idSolicitud); }}
                    style={{ background: "none", border: "none", fontSize: "18px", cursor: "pointer", color: "#94a3b8", padding: "0 4px" }}
                    title="Descartar"
                  >
                    ×
                  </button>
                </Link>
              );
            })}
            <Link to="/empleado/solicitudes" style={{ fontSize: "13px" }}>Ver todas mis solicitudes →</Link>
          </div>
        )}
      </main>
    </div>
  );
}
