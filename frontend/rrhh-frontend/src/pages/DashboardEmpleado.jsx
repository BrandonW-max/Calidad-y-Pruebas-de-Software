import { Link } from "react-router-dom";
import "../styles/Dashboard.css";

export default function DashboardEmpleado() {
  return (
    <div className="dashboard">
      <aside className="sidebar">
        <div className="sidebar-header">
          <h2>Panel Empleado</h2>
          <p>Mi espacio de trabajo</p>
        </div>
        <nav className="sidebar-nav">
          <Link to="/empleado" className="active">🏠 Inicio</Link>
          <Link to="/empleado/asistencia">📋 Mi Asistencia</Link>
          <Link to="/empleado/solicitudes">📅 Mis Solicitudes</Link>
          <div className="nav-logout">
            <Link to="/">🚪 Cerrar Sesión</Link>
          </div>
        </nav>
      </aside>

      <main className="main-content">
        <header className="main-header">
          <h1>Bienvenido, Empleado</h1>
          <span>Portal del Empleado</span>
        </header>

        <div className="stats-row">
          <div className="stat-box">
            <div className="stat-value">22</div>
            <div className="stat-label">Días Asistidos (Mes)</div>
          </div>
          <div className="stat-box">
            <div className="stat-value">1</div>
            <div className="stat-label">Tardanzas (Mes)</div>
          </div>
          <div className="stat-box">
            <div className="stat-value">15</div>
            <div className="stat-label">Días de Vacaciones</div>
          </div>
          <div className="stat-box">
            <div className="stat-value">2</div>
            <div className="stat-label">Solicitudes Activas</div>
          </div>
        </div>

        <div className="cards-grid">
          <Link to="/empleado/asistencia" className="card">
            <div className="card-icon">📋</div>
            <h3>Mi Asistencia</h3>
            <p>Ver mis registros de asistencia</p>
          </Link>
          <Link to="/empleado/solicitudes" className="card">
            <div className="card-icon">📅</div>
            <h3>Mis Solicitudes</h3>
            <p>Solicitar permisos y vacaciones</p>
          </Link>
        </div>

        <div className="section-card">
          <h2>Asistencia Reciente</h2>
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
                  <td>08/06/2026</td>
                  <td>08:05</td>
                  <td>17:00</td>
                  <td>8h 55min</td>
                  <td><span className="badge badge-success">Normal</span></td>
                </tr>
                <tr>
                  <td>07/06/2026</td>
                  <td>08:22</td>
                  <td>17:00</td>
                  <td>8h 38min</td>
                  <td><span className="badge badge-warning">Tardanza</span></td>
                </tr>
                <tr>
                  <td>06/06/2026</td>
                  <td>07:58</td>
                  <td>17:02</td>
                  <td>9h 04min</td>
                  <td><span className="badge badge-success">Normal</span></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </main>
    </div>
  );
}
