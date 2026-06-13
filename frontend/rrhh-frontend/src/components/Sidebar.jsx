import { Link, useLocation } from "react-router-dom";
import logo from "../assets/logo.png";
import { clearSession } from "../services/api";

const NAV = {
  RRHH: {
    title: "Panel RRHH",
    subtitle: "Recursos Humanos",
    themeClass: "sidebar-rrhh",
    links: [
      { to: "/rrhh",             icon: "🏠", label: "Inicio" },
      { to: "/rrhh/empleados",   icon: "👥", label: "Gestión de Empleados" },
      { to: "/rrhh/asistencia",  icon: "📋", label: "Control de Asistencia" },
      { to: "/rrhh/nomina",      icon: "💰", label: "Cálculo de Nómina" },
      { to: "/rrhh/solicitudes", icon: "📅", label: "Solicitudes" },
      { to: "/rrhh/reportes",    icon: "📊", label: "Reportes" },
    ],
  },
  EMPLEADO: {
    title: "Mi Portal",
    subtitle: "Portal del Empleado",
    themeClass: "sidebar-empleado",
    links: [
      { to: "/empleado",                icon: "🏠", label: "Inicio" },
      { to: "/empleado/asistencia",     icon: "📋", label: "Mi Asistencia" },
      { to: "/empleado/solicitudes",    icon: "📅", label: "Mis Solicitudes" },
      { to: "/empleado/nomina",         icon: "💰", label: "Mi Nómina" },
    ],
  },
  GERENCIA: {
    title: "Panel Gerencia",
    subtitle: "Gestión Ejecutiva",
    themeClass: "sidebar-gerencia",
    links: [
      { to: "/gerencia",            icon: "🏠", label: "Inicio" },
      { to: "/gerencia/solicitudes", icon: "📅", label: "Solicitudes" },
    ],
  },
};

export default function Sidebar({ rol }) {
  const location = useLocation();
  const config = NAV[rol] || NAV.RRHH;

  const isActive = (to) => location.pathname === to;

  return (
    <aside className={`sidebar ${config.themeClass}`}>
      <div className="sidebar-brand">
        <div className="sidebar-brand-icon">
          <img src={logo} alt="Logo Hospital" className="sidebar-logo-img" />
        </div>
        <div className="sidebar-brand-text">
          <h2>{config.title}</h2>
          <p>{config.subtitle}</p>
        </div>
      </div>
      <nav className="sidebar-nav">
        {config.links.map((link) => (
          <Link
            key={link.to}
            to={link.to}
            className={isActive(link.to) ? "active" : ""}
          >
            <span className="nav-icon">{link.icon}</span>
            <span>{link.label}</span>
          </Link>
        ))}
        <div className="nav-logout">
          <button
            onClick={() => { clearSession(); window.location.href = "/"; }}
            style={{ background: "none", border: "none", cursor: "pointer", width: "100%", textAlign: "left", padding: 0 }}
          >
            <span className="nav-icon">🚪</span>
            <span>Cerrar Sesión</span>
          </button>
        </div>
      </nav>
    </aside>
  );
}
