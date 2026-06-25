import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import "../styles/Dashboard.css";
import {
  buscarEmpleadosAvanzado,
  getSolicitudesPorEstado,
  listarJustificacionesPendientes,
} from "../services/api";

const toCount = (data) => {
  if (Array.isArray(data)) return data.length;
  if (data?.totalElements != null) return data.totalElements;
  if (Array.isArray(data?.content)) return data.content.length;
  return 0;
};

export default function DashboardRRHH() {
  const [stats, setStats] = useState({
    empleadosActivos:      null,
    solicitudesPendientes: null,
    tardanzas:             null,
    incidencias:           null,
  });

  useEffect(() => {
    buscarEmpleadosAvanzado({ estado: "ACTIVO" })
      .then(data => setStats(s => ({ ...s, empleadosActivos: toCount(data) })))
      .catch(() => setStats(s => ({ ...s, empleadosActivos: "-" })));

    getSolicitudesPorEstado("PENDIENTE")
      .then(data => setStats(s => ({ ...s, solicitudesPendientes: toCount(data) })))
      .catch(() => setStats(s => ({ ...s, solicitudesPendientes: "-" })));

    listarJustificacionesPendientes()
      .then(data => setStats(s => ({ ...s, tardanzas: toCount(data) })))
      .catch(() => setStats(s => ({ ...s, tardanzas: "-" })));

    getSolicitudesPorEstado("EN_REVISION")
      .then(data => setStats(s => ({ ...s, incidencias: toCount(data) })))
      .catch(() => setStats(s => ({ ...s, incidencias: "-" })));
  }, []);

  const fmt = (v) => v === null ? "..." : v;

  const metricas = [
    {
      key: "empleados",
      value: fmt(stats.empleadosActivos),
      label: "Empleados Activos",
      icon: "GR",
      tone: "blue",
    },
    {
      key: "solicitudes",
      value: fmt(stats.solicitudesPendientes),
      label: "Solicitudes Pendientes",
      icon: "CA",
      tone: "teal",
    },
    {
      key: "tardanzas",
      value: fmt(stats.tardanzas),
      label: "Tardanzas / Justif. Pendientes",
      icon: "HR",
      tone: "red",
    },
    {
      key: "incidencias",
      value: fmt(stats.incidencias),
      label: "En Revision (Gerencia)",
      icon: "!",
      tone: "dark",
    },
  ];

  const accesos = [
    {
      to: "/rrhh/empleados",
      icon: "ID",
      title: "Gestion de Empleados",
      desc: "Registrar, editar y administrar la informacion completa del personal hospitalario.",
      tone: "blue",
      large: true,
    },
    {
      to: "/rrhh/asistencia",
      icon: "OK",
      title: "Control de Asistencia",
      desc: "Supervisar registros biometricos, correcciones de turno y justificaciones de asistencia.",
      tone: "teal",
      large: true,
    },
    {
      to: "/rrhh/nomina",
      icon: "S/",
      title: "Calculo de Nomina",
      desc: "Procesar y detallar pagos del personal.",
      tone: "dark",
    },
    {
      to: "/rrhh/solicitudes",
      icon: "DOC",
      title: "Solicitudes",
      desc: "Validar permisos y vacaciones.",
      tone: "blue",
    },
    {
      to: "/rrhh/reportes",
      icon: "%",
      title: "Reportes",
      desc: "Generar y exportar reportes del sistema.",
      tone: "teal",
    },
  ];

  return (
    <div className="dashboard">
      <Sidebar rol="RRHH" />

      <main className="main-content rrhh-home">
        <header className="rrhh-hero">
          <div className="rrhh-hero-copy">
            <p className="rrhh-kicker"><span /> Panel Principal</p>
            <h1>Bienvenido, RRHH</h1>
          </div>
          <span className="rrhh-role-badge">Recursos Humanos</span>
        </header>

        <section className="rrhh-metrics" aria-label="Resumen de Recursos Humanos">
          {metricas.map(item => (
            <article className={`rrhh-metric rrhh-tone-${item.tone}`} key={item.key}>
              <div className="rrhh-metric-icon" aria-hidden="true">{item.icon}</div>
              <div className="rrhh-metric-body">
                <strong>{item.value}</strong>
                <span>{item.label}</span>
              </div>
            </article>
          ))}
        </section>

        <section className="rrhh-actions" aria-label="Accesos rapidos de Recursos Humanos">
          {accesos.map(item => (
            <Link
              to={item.to}
              className={`rrhh-action-card rrhh-tone-${item.tone}${item.large ? " rrhh-action-large" : ""}`}
              key={item.to}
            >
              <span className="rrhh-action-icon" aria-hidden="true">{item.icon}</span>
              <span className="rrhh-action-content">
                <strong>{item.title}</strong>
                <small>{item.desc}</small>
              </span>
              <span className="rrhh-action-link">Acceder al modulo <b aria-hidden="true">-&gt;</b></span>
            </Link>
          ))}
        </section>

        <aside className="rrhh-reminder">
          <span className="rrhh-reminder-icon" aria-hidden="true">!</span>
          <p>
            <strong>Recuerde:</strong> el registro de asistencia biometrica se realiza directamente desde el dispositivo lector de huella dactilar.
          </p>
        </aside>
      </main>
    </div>
  );
}
