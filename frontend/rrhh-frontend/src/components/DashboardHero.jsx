import { UserRound } from "lucide-react";

const THEME = {
  rrhh: {
    badgeBorder: "border-signal-500/30",
    badgeBg: "bg-signal-100",
    badgeText: "text-signal-600",
    gradient: "from-rrhh-100 via-signal-100/60",
    line: "bg-rrhh-600",
    text: "text-rrhh-600",
  },
  empleado: {
    badgeBorder: "border-empleado-500/30",
    badgeBg: "bg-empleado-100",
    badgeText: "text-empleado-600",
    gradient: "from-empleado-100 via-signal-100/60",
    line: "bg-empleado-600",
    text: "text-empleado-600",
  },
  gerencia: {
    badgeBorder: "border-gerencia-500/30",
    badgeBg: "bg-gerencia-100",
    badgeText: "text-gerencia-600",
    gradient: "from-gerencia-100 via-signal-100/50",
    line: "bg-gerencia-600",
    text: "text-gerencia-600",
  },
};

export default function DashboardHero({ nombre, rol, badge, theme = "rrhh" }) {
  const t = THEME[theme];
  return (
    <header className="relative mb-7 flex flex-col items-start gap-5 overflow-hidden rounded-3xl border border-white bg-white/75 px-8 py-9 shadow-glass-md backdrop-blur-xl sm:flex-row sm:items-center sm:justify-between">
      <div
        className={`pointer-events-none absolute inset-y-0 right-0 w-[45%] bg-gradient-to-bl ${t.gradient} to-transparent`}
        style={{ clipPath: "polygon(28% 0, 100% 0, 100% 100%, 0 100%)" }}
        aria-hidden="true"
      />
      <div className="relative z-10 min-w-0 flex-1">
        <p className={`mb-2 inline-flex items-center gap-2 text-[12px] font-extrabold tracking-[0.16em] uppercase ${t.text}`}>
          <span className={`h-[3px] w-8 rounded-full ${t.line}`} />
          {rol}
        </p>
        <h1 className="break-words font-[var(--font-display)] text-4xl font-extrabold leading-[1.05] text-ink-900 sm:text-[44px]">
          Bienvenido, {nombre}
        </h1>
      </div>
      <div className="relative z-10 flex shrink-0 items-center gap-3">
        <span className={`inline-flex h-9 items-center rounded-full border ${t.badgeBorder} ${t.badgeBg} px-5 text-[12px] font-extrabold uppercase tracking-wide ${t.badgeText}`}>
          {badge}
        </span>
        {theme === "empleado" && (
          <div className="flex h-11 w-11 items-center justify-center rounded-full border border-white bg-white text-ink-500 shadow-glass-sm">
            <UserRound size={20} strokeWidth={2} />
          </div>
        )}
      </div>
    </header>
  );
}
