import { Link } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import "../styles/Dashboard.css";

export default function DashboardGerencia() {
  return (
    <div className="dashboard">
      <Sidebar rol="GERENCIA" />

      <main className="main-content">
        <header className="main-header header-gerencia">
          <div className="header-left">
            <span className="page-breadcrumb">Panel Ejecutivo</span>
            <h1>Bienvenido, Gerencia</h1>
          </div>
          <span className="role-pill role-pill-gerencia">Gestión Ejecutiva</span>
        </header>

        <div className="stats-row">
          <div className="stat-box accent-purple">
            <div className="stat-icon">👥</div>
            <div className="stat-info">
              <div className="stat-value">—</div>
              <div className="stat-label">Total Empleados</div>
            </div>
          </div>
          <div className="stat-box accent-orange">
            <div className="stat-icon">⏳</div>
            <div className="stat-info">
              <div className="stat-value">—</div>
              <div className="stat-label">Por Aprobar</div>
            </div>
          </div>
          <div className="stat-box accent-green">
            <div className="stat-icon">✅</div>
            <div className="stat-info">
              <div className="stat-value">—</div>
              <div className="stat-label">Aprobadas (Mes)</div>
            </div>
          </div>
          <div className="stat-box accent-red">
            <div className="stat-icon">❌</div>
            <div className="stat-info">
              <div className="stat-value">—</div>
              <div className="stat-label">Rechazadas (Mes)</div>
            </div>
          </div>
        </div>

        <div className="cards-grid">
          <Link to="/gerencia/solicitudes" className="card">
            <span className="card-icon">📅</span>
            <h3>Solicitudes Pendientes</h3>
            <p>Aprobar o rechazar solicitudes de permisos y vacaciones</p>
          </Link>
        </div>

        <div className="section-card">
          <h2>📅 Solicitudes por Revisar</h2>
          <div className="alert alert-info">
            Las solicitudes enviadas por RRHH aparecerán aquí para su evaluación y decisión final.
          </div>
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
                  <td colSpan={5}>
                    <div className="empty-state">
                      <div className="empty-state-icon">📅</div>
                      <p>Las solicitudes pendientes de aprobación se mostrarán aquí.</p>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div className="form-actions" style={{ marginTop: "12px" }}>
            <Link to="/gerencia/solicitudes" className="btn btn-purple">
              Ver todas las solicitudes
            </Link>
          </div>
        </div>
      </main>
    </div>
  );
}
