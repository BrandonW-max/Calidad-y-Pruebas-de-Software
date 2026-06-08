import { Link } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import "../styles/Dashboard.css";

export default function DashboardEmpleado() {
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
      </main>
    </div>
  );
}
