import { Link } from "react-router-dom";
import { Users, Hourglass, CheckCircle2, XCircle, CalendarDays, ArrowUpRight } from "lucide-react";
import Sidebar from "../components/Sidebar";

const metricas = [
  { key: "total", value: "—", label: "Total Empleados", icon: Users, tone: "gerencia" },
  { key: "porAprobar", value: "—", label: "Por Aprobar", icon: Hourglass, tone: "warning" },
  { key: "aprobadas", value: "—", label: "Aprobadas (Mes)", icon: CheckCircle2, tone: "success" },
  { key: "rechazadas", value: "—", label: "Rechazadas (Mes)", icon: XCircle, tone: "danger" },
];

const TONE = {
  gerencia: { icon: "bg-gerencia-100 text-gerencia-600", glow: "from-gerencia-100/70" },
  warning: { icon: "bg-warning-100 text-warning-600", glow: "from-warning-100/80" },
  success: { icon: "bg-success-100 text-success-600", glow: "from-success-100/80" },
  danger: { icon: "bg-danger-100 text-danger-600", glow: "from-danger-100/80" },
};

export default function DashboardGerencia() {
  return (
    <div className="min-h-screen bg-ink-100">
      <Sidebar rol="GERENCIA" />

      <main className="min-h-screen px-5 pb-7 pt-20 sm:px-6 lg:ml-[272px] lg:px-9 lg:py-9 lg:pt-9">
        {/* ── Hero ── */}
        <header className="relative mb-7 flex flex-col items-start gap-5 overflow-hidden rounded-3xl border border-white bg-white/75 px-8 py-9 shadow-glass-md backdrop-blur-xl sm:flex-row sm:items-center sm:justify-between">
          <div
            className="pointer-events-none absolute inset-y-0 right-0 w-[45%] bg-gradient-to-bl from-gerencia-100 via-signal-100/50 to-transparent"
            style={{ clipPath: "polygon(28% 0, 100% 0, 100% 100%, 0 100%)" }}
            aria-hidden="true"
          />
          <div className="relative z-10">
            <p className="mb-2 inline-flex items-center gap-2 text-[12px] font-extrabold tracking-[0.16em] text-gerencia-600 uppercase">
              <span className="h-[3px] w-8 rounded-full bg-gerencia-600" />
              Panel Ejecutivo
            </p>
            <h1 className="font-[var(--font-display)] text-4xl font-extrabold leading-[1.05] text-ink-900 sm:text-[44px]">
              Bienvenido, Gerencia
            </h1>
          </div>
          <span className="relative z-10 inline-flex h-9 items-center rounded-full border border-gerencia-500/30 bg-gerencia-100 px-5 text-[12px] font-extrabold uppercase tracking-wide text-gerencia-600">
            Gestión Ejecutiva
          </span>
        </header>

        {/* ── Métricas ── */}
        <section aria-label="Resumen ejecutivo" className="mb-7 grid grid-cols-2 gap-4 lg:grid-cols-4">
          {metricas.map((item) => {
            const Icon = item.icon;
            const tone = TONE[item.tone];
            return (
              <article
                key={item.key}
                className="group relative overflow-hidden rounded-2xl border border-white bg-white/85 p-5 shadow-glass-sm backdrop-blur-md transition-all duration-200 hover:-translate-y-1 hover:shadow-glass-lg"
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

        {/* ── Acceso principal ── */}
        <section aria-label="Accesos de gerencia" className="grid grid-cols-1">
          <Link
            to="/gerencia/solicitudes"
            className="group relative flex flex-col justify-between overflow-hidden rounded-2xl border border-white bg-white/85 p-7 shadow-glass-sm backdrop-blur-md transition-all duration-200 hover:-translate-y-1 hover:shadow-glass-lg sm:min-h-[190px]"
          >
            <div className="pointer-events-none absolute -right-8 -top-8 h-32 w-32 rounded-full bg-gradient-to-br from-gerencia-100/80 to-transparent opacity-70 blur-xl transition-transform duration-300 group-hover:scale-110" aria-hidden="true" />
            <div className="relative z-10">
              <div className="mb-5 flex h-14 w-14 items-center justify-center rounded-2xl bg-gerencia-100 text-gerencia-600">
                <CalendarDays size={26} strokeWidth={2.1} />
              </div>
              <h3 className="font-[var(--font-display)] text-[22px] font-bold leading-snug text-ink-900">
                Solicitudes Pendientes
              </h3>
              <p className="mt-2 max-w-md text-[13.5px] leading-relaxed text-ink-500">
                Aprobar o rechazar solicitudes de permisos y vacaciones elevadas por Recursos Humanos.
              </p>
            </div>
            <span className="relative z-10 mt-6 inline-flex items-center gap-1.5 text-[13px] font-bold text-ink-800">
              Ir a Solicitudes
              <ArrowUpRight size={15} className="transition-transform duration-200 group-hover:translate-x-0.5 group-hover:-translate-y-0.5" />
            </span>
          </Link>
        </section>
      </main>
    </div>
  );
}
