import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import {
  Users, ClipboardCheck, AlarmClockOff, AlertTriangle,
  IdCard, ClipboardList, Wallet, CalendarDays, BarChart3,
  ArrowUpRight, Fingerprint,
} from "lucide-react";
import Sidebar from "../components/Sidebar";
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

const TONE = {
  blue: {
    icon: "bg-rrhh-100 text-rrhh-600",
    ring: "hover:ring-rrhh-200",
    glow: "from-rrhh-100/70",
  },
  teal: {
    icon: "bg-signal-100 text-signal-600",
    ring: "hover:ring-signal-200",
    glow: "from-signal-100/70",
  },
  red: {
    icon: "bg-danger-100 text-danger-600",
    ring: "hover:ring-danger-100",
    glow: "from-danger-100/70",
  },
  dark: {
    icon: "bg-ink-200 text-ink-700",
    ring: "hover:ring-ink-200",
    glow: "from-ink-100/80",
  },
};

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

  const fmt = (v) => (v === null ? "···" : v);

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
    <div className="min-h-screen bg-ink-100">
      <Sidebar rol="RRHH" />

      <main className="min-h-screen px-5 pb-7 pt-20 sm:px-6 lg:ml-[272px] lg:px-9 lg:py-9 lg:pt-9">
        {/* ── Hero ── */}
        <header className="relative mb-7 overflow-hidden rounded-3xl border border-white bg-white/75 px-8 py-9 shadow-glass-md backdrop-blur-xl sm:flex sm:items-center sm:justify-between sm:gap-6">
          <div
            className="pointer-events-none absolute inset-y-0 right-0 w-[45%] bg-gradient-to-bl from-rrhh-100 via-signal-100/60 to-transparent"
            style={{ clipPath: "polygon(28% 0, 100% 0, 100% 100%, 0 100%)" }}
            aria-hidden="true"
          />
          <div className="relative z-10">
            <p className="mb-2 inline-flex items-center gap-2 text-[12px] font-extrabold tracking-[0.16em] text-rrhh-600 uppercase">
              <span className="h-[3px] w-8 rounded-full bg-rrhh-600" />
              Panel Principal
            </p>
            <h1 className="font-[var(--font-display)] text-4xl font-extrabold leading-[1.05] text-ink-900 sm:text-[44px]">
              Bienvenido, RRHH
            </h1>
          </div>
          <span className="relative z-10 mt-5 inline-flex h-9 w-fit items-center rounded-full border border-signal-500/30 bg-signal-100 px-5 text-[12px] font-extrabold uppercase tracking-wide text-signal-600 sm:mt-0">
            Recursos Humanos
          </span>
        </header>

        {/* ── Métricas ── */}
        <section aria-label="Resumen de Recursos Humanos" className="mb-7 grid grid-cols-2 gap-4 lg:grid-cols-4">
          {metricas.map((item) => {
            const Icon = item.icon;
            const tone = TONE[item.tone];
            return (
              <article
                key={item.key}
                className={
                  "group relative overflow-hidden rounded-2xl border border-white bg-white/85 p-5 shadow-glass-sm ring-1 ring-transparent backdrop-blur-md transition-all duration-200 hover:-translate-y-1 hover:shadow-glass-lg " +
                  tone.ring
                }
              >
                <div className={"pointer-events-none absolute -right-6 -top-6 h-24 w-24 rounded-full bg-gradient-to-br opacity-70 blur-xl " + tone.glow + " to-transparent"} aria-hidden="true" />
                <div className={"relative z-10 mb-5 flex h-11 w-11 items-center justify-center rounded-xl " + tone.icon}>
                  <Icon size={20} strokeWidth={2.25} />
                </div>
                <div className="relative z-10">
                  <strong className="block font-[var(--font-display)] text-[34px] font-extrabold leading-none text-ink-900 sm:text-[42px]">
                    {item.value}
                  </strong>
                  <span className="mt-1.5 block text-[13px] font-semibold leading-snug text-ink-500">
                    {item.label}
                  </span>
                </div>
              </article>
            );
          })}
        </section>

        {/* ── Accesos rápidos ── */}
        <section aria-label="Accesos rápidos de Recursos Humanos" className="mb-7 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-12">
          {accesos.map((item) => {
            const Icon = item.icon;
            const tone = TONE[item.tone];
            return (
              <Link
                key={item.to}
                to={item.to}
                className={
                  "group relative flex flex-col justify-between overflow-hidden rounded-2xl border border-white bg-white/85 p-6 shadow-glass-sm ring-1 ring-transparent backdrop-blur-md transition-all duration-200 hover:-translate-y-1 hover:shadow-glass-lg " +
                  tone.ring + " " +
                  (item.large ? "lg:col-span-6 lg:min-h-[210px] lg:p-7" : "lg:col-span-4 lg:min-h-[210px]")
                }
              >
                <div className={"pointer-events-none absolute -right-8 -top-8 h-28 w-28 rounded-full bg-gradient-to-br opacity-60 blur-xl transition-transform duration-300 group-hover:scale-110 " + tone.glow + " to-transparent"} aria-hidden="true" />
                <div className="relative z-10">
                  <div className={"mb-5 flex items-center justify-center rounded-2xl transition-colors duration-200 " + tone.icon + (item.large ? " h-14 w-14" : " h-12 w-12")}>
                    <Icon size={item.large ? 26 : 22} strokeWidth={2.1} />
                  </div>
                  <h3 className={"font-[var(--font-display)] font-bold leading-snug text-ink-900 " + (item.large ? "text-[22px]" : "text-[17px]")}>
                    {item.title}
                  </h3>
                  <p className="mt-2 text-[13.5px] leading-relaxed text-ink-500">{item.desc}</p>
                </div>
                <span className="relative z-10 mt-6 inline-flex items-center gap-1.5 text-[13px] font-bold text-ink-800">
                  Acceder al módulo
                  <ArrowUpRight size={15} className="transition-transform duration-200 group-hover:translate-x-0.5 group-hover:-translate-y-0.5" />
                </span>
              </Link>
            );
          })}
        </section>

        {/* ── Recordatorio ── */}
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
      </main>
    </div>
  );
}
