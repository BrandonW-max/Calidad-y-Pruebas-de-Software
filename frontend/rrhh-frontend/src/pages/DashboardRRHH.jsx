import { Link } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import "../styles/Dashboard.css";

export default function DashboardRRHH() {
  return (
    <div className="dashboard">
      <Sidebar rol="RRHH" />

      <main className="main-content">
        <header className="main-header header-rrhh">
          <div className="header-left">
            <span className="page-breadcrumb">Panel Principal</span>
            <h1>Bienvenido, RRHH</h1>
          </div>
          <span className="role-pill role-pill-rrhh">Recursos Humanos</span>
        </header>

        <div className="stats-row">
          <div className="stat-box accent-blue">
            <div className="stat-icon">👥</div>
            <div className="stat-info">
              <div className="stat-value">—</div>
              <div className="stat-label">Empleados Activos</div>
            </div>
          </div>
          <div className="stat-box accent-orange">
            <div className="stat-icon">📅</div>
            <div className="stat-info">
              <div className="stat-value">—</div>
              <div className="stat-label">Solicitudes Pendientes</div>
            </div>
          </div>
          <div className="stat-box accent-red">
            <div className="stat-icon">⏰</div>
            <div className="stat-info">
              <div className="stat-value">—</div>
              <div className="stat-label">Tardanzas Hoy</div>
            </div>
          </div>
          <div className="stat-box accent-purple">
            <div className="stat-icon">⚠️</div>
            <div className="stat-info">
              <div className="stat-value">—</div>
              <div className="stat-label">Incidencias</div>
            </div>
          </div>
        </div>

        <div className="cards-grid">
          <Link to="/rrhh/empleados" className="card">
            <span className="card-icon">👥</span>
            <h3>Gestión de Empleados</h3>
            <p>Registrar, editar y administrar empleados</p>
          </Link>
          <Link to="/rrhh/asistencia" className="card">
            <span className="card-icon">📋</span>
            <h3>Control de Asistencia</h3>
            <p>Gestionar correcciones y justificaciones</p>
          </Link>
          <Link to="/rrhh/nomina" className="card">
            <span className="card-icon">💰</span>
            <h3>Cálculo de Nómina</h3>
            <p>Procesar y detallar pagos del personal</p>
          </Link>
          <Link to="/rrhh/solicitudes" className="card">
            <span className="card-icon">📅</span>
            <h3>Solicitudes</h3>
            <p>Validar permisos y vacaciones</p>
          </Link>
          <Link to="/rrhh/reportes" className="card">
            <span className="card-icon">📊</span>
            <h3>Reportes</h3>
            <p>Generar y exportar reportes del sistema</p>
          </Link>
        </div>

        <div className="section-card">
          <h2>ℹ️ Accesos rápidos del módulo RRHH</h2>
          <div className="alert alert-info">
            Los datos estadísticos se cargarán automáticamente cuando el sistema esté conectado al backend.
            Utilice el menú lateral para acceder a cada módulo.
          </div>
          <div className="alert alert-warning">
            ⚠️ Recuerde: el registro de asistencia biométrica se realiza directamente desde el dispositivo lector de huella dactilar.
          </div>
        </div>
      </main>
    </div>
  );
}
