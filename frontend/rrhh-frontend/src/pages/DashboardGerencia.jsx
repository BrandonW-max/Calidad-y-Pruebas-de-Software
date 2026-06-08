import { Link } from "react-router-dom";
import "../styles/Dashboard.css";

export default function DashboardGerencia() {
  return (
    <div className="dashboard">
      <aside className="sidebar">
        <div className="sidebar-header">
          <h2>Panel Gerencia</h2>
          <p>Gestión Ejecutiva</p>
        </div>
        <nav className="sidebar-nav">
          <Link to="/gerencia" className="active">🏠 Inicio</Link>
          <Link to="/gerencia/solicitudes">📅 Solicitudes</Link>
          <div className="nav-logout">
            <Link to="/">🚪 Cerrar Sesión</Link>
          </div>
        </nav>
      </aside>

      <main className="main-content">
        <header className="main-header">
          <h1>Bienvenido, Gerencia</h1>
          <span>Panel de Gestión Ejecutiva</span>
        </header>

        <div className="stats-row">
          <div className="stat-box">
            <div className="stat-value">48</div>
            <div className="stat-label">Total Empleados</div>
          </div>
          <div className="stat-box">
            <div className="stat-value">4</div>
            <div className="stat-label">Solicitudes por Aprobar</div>
          </div>
          <div className="stat-box">
            <div className="stat-value">12</div>
            <div className="stat-label">Aprobadas este Mes</div>
          </div>
          <div className="stat-box">
            <div className="stat-value">3</div>
            <div className="stat-label">Rechazadas este Mes</div>
          </div>
        </div>

        <div className="cards-grid">
          <Link to="/gerencia/solicitudes" className="card">
            <div className="card-icon">📅</div>
            <h3>Solicitudes Pendientes</h3>
            <p>Aprobar o rechazar permisos y vacaciones</p>
          </Link>
        </div>

        <div className="section-card">
          <h2>Solicitudes Recientes por Revisar</h2>
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Empleado</th>
                  <th>Tipo</th>
                  <th>Fechas</th>
                  <th>Días</th>
                  <th>Estado</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>Ana Pérez</td>
                  <td>Vacaciones</td>
                  <td>10/06 – 20/06/2026</td>
                  <td>10</td>
                  <td><span className="badge badge-warning">Enviado a Gerencia</span></td>
                </tr>
                <tr>
                  <td>Carlos López</td>
                  <td>Permiso médico</td>
                  <td>09/06/2026</td>
                  <td>1</td>
                  <td><span className="badge badge-warning">Enviado a Gerencia</span></td>
                </tr>
                <tr>
                  <td>María Torres</td>
                  <td>Permiso personal</td>
                  <td>15/06/2026</td>
                  <td>1</td>
                  <td><span className="badge badge-warning">Enviado a Gerencia</span></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </main>
    </div>
  );
}
