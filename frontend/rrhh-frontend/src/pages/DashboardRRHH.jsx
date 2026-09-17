import { useState, useEffect } from "react";
import {
  Users, ClipboardCheck, AlarmClockOff, AlertTriangle,
  IdCard, ClipboardList, Wallet, CalendarDays, BarChart3,
  Fingerprint,
} from "lucide-react";
import DashboardLayout from "../components/DashboardLayout";
import DashboardHero from "../components/DashboardHero";
import MetricCard from "../components/MetricCard";
import QuickAccessCard from "../components/QuickAccessCard";
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

const fmt = (v) => (v === null ? "···" : v);

export default function DashboardRRHH() {
  const [stats, setStats] = useState({
    empleadosActivos: null,
    solicitudesPendientes: null,
    tardanzas: null,
    incidencias: null,
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

  const metricas = [
    { key: "empleados", value: fmt(stats.empleadosActivos), label: "Empleados Activos", icon: Users, tone: "blue" },
    { key: "solicitudes", value: fmt(stats.solicitudesPendientes), label: "Solicitudes Pendientes", icon: ClipboardCheck, tone: "teal" },
    { key: "tardanzas", value: fmt(stats.tardanzas), label: "Tardanzas / Justif. Pendientes", icon: AlarmClockOff, tone: "red" },
    { key: "incidencias", value: fmt(stats.incidencias), label: "En Revisión (Gerencia)", icon: AlertTriangle, tone: "dark" },
  ];

  const accesos = [
    {
      to: "/rrhh/empleados", icon: IdCard, title: "Gestión de Empleados",
      desc: "Registrar, editar y administrar la información completa del personal hospitalario.",
      tone: "blue", large: true,
    },
    {
      to: "/rrhh/asistencia", icon: ClipboardList, title: "Control de Asistencia",
      desc: "Supervisar registros biométricos, correcciones de turno y justificaciones de asistencia.",
      tone: "teal", large: true,
    },
    { to: "/rrhh/nomina", icon: Wallet, title: "Cálculo de Nómina", desc: "Procesar y detallar pagos del personal.", tone: "dark" },
    { to: "/rrhh/solicitudes", icon: CalendarDays, title: "Solicitudes", desc: "Validar permisos y vacaciones.", tone: "blue" },
    { to: "/rrhh/reportes", icon: BarChart3, title: "Reportes", desc: "Generar y exportar reportes del sistema.", tone: "teal" },
  ];

  return (
    <DashboardLayout rol="RRHH">
      <DashboardHero nombre="RRHH" rol="Panel Principal" badge="Recursos Humanos" theme="rrhh" />

      <section aria-label="Resumen de Recursos Humanos" className="mb-7 grid grid-cols-2 gap-4 lg:grid-cols-4">
        {metricas.map(item => (
          <MetricCard key={item.key} value={item.value} label={item.label} icon={item.icon} tone={item.tone} />
        ))}
      </section>

      <section aria-label="Accesos rápidos de Recursos Humanos" className="mb-7 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {accesos.map(item => (
          <QuickAccessCard key={item.to} to={item.to} icon={item.icon} title={item.title} desc={item.desc} tone={item.tone} large={item.large} />
        ))}
      </section>

      <aside className="relative flex items-center gap-4 overflow-hidden rounded-2xl border border-warning-100 bg-gradient-to-r from-warning-100/90 to-warning-100/40 px-6 py-5 shadow-glass-sm">
        <div className="absolute inset-y-0 left-0 w-1 bg-warning-600" aria-hidden="true" />
        <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-warning-600/15 text-warning-600">
          <Fingerprint size={20} strokeWidth={2.1} />
        </div>
        <p className="text-[15px] leading-relaxed text-[#7a4d00]">
          <strong className="font-bold text-[#5c3a00]">Recuerde: </strong>
          el registro de asistencia biométrica se realiza directamente desde el dispositivo lector de huella dactilar.
        </p>
      </aside>
    </DashboardLayout>
  );
}
