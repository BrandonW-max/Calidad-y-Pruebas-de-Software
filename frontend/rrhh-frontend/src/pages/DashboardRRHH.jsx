import { Link } from "react-router-dom";
import "../styles/Dashboard.css";

export default function DashboardRRHH() {
  return (
    <div className="dashboard">
      <aside className="sidebar">
        <div className="sidebar-header">
          <h2>Panel RRHH</h2>
          <p>Recursos Humanos</p>
        </div>
        <nav className="sidebar-nav">
          <Link to="/rrhh" className="active">🏠 Inicio</Link>
          <Link to="/rrhh/empleados">👥 Gestión de Empleados</Link>
          <Link to="/rrhh/asistencia">📋 Control de Asistencia</Link>
          <Link to="/rrhh/nomina">💰 Cálculo de Nómina</Link>
          <Link to="/rrhh/solicitudes">📅 Solicitudes</Link>
          <Link to="/rrhh/reportes">📊 Reportes</Link>
          <div className="nav-logout">
            <Link to="/">🚪 Cerrar Sesión</Link>
          </div>
        </nav>
      </aside>

      <main className="main-content">
        <header className="main-header">
          <h1>Bienvenido, RRHH</h1>
          <span>Panel de Recursos Humanos</span>
        </header>

        <div className="stats-row">
          <div className="stat-box">
            <div className="stat-value">48</div>
            <div className="stat-label">Empleados Activos</div>
          </div>
          <div className="stat-box">
            <div className="stat-value">5</div>
            <div className="stat-label">Solicitudes Pendientes</div>
          </div>
          <div className="stat-box">
            <div className="stat-value">3</div>
            <div className="stat-label">Tardanzas Hoy</div>
          </div>
          <div className="stat-box">
            <div className="stat-value">2</div>
            <div className="stat-label">Incidencias</div>
          </div>
        </div>

        <div className="cards-grid">
          <Link to="/rrhh/empleados" className="card">
            <div className="card-icon">👥</div>
            <h3>Gestión de Empleados</h3>
            <p>Registrar, editar y administrar empleados</p>
          </Link>
          <Link to="/rrhh/asistencia" className="card">
            <div className="card-icon">📋</div>
            <h3>Control de Asistencia</h3>
            <p>Supervisar asistencia y tardanzas</p>
          </Link>
          <Link to="/rrhh/nomina" className="card">
            <div className="card-icon">💰</div>
            <h3>Cálculo de Nómina</h3>
            <p>Procesar los pagos del personal</p>
          </Link>
          <Link to="/rrhh/solicitudes" className="card">
            <div className="card-icon">📅</div>
            <h3>Solicitudes</h3>
            <p>Gestionar permisos y vacaciones</p>
          </Link>
          <Link to="/rrhh/reportes" className="card">
            <div className="card-icon">📊</div>
            <h3>Reportes</h3>
            <p>Generar reportes del sistema</p>
          </Link>
        </div>
      </main>
    </div>
  );
}
