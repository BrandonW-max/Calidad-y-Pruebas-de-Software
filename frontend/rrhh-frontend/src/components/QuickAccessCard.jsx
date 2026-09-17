import { Link } from "react-router-dom";
import { ArrowUpRight } from "lucide-react";

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
  gerencia: {
    icon: "bg-gerencia-100 text-gerencia-600",
    ring: "hover:ring-gerencia-200",
    glow: "from-gerencia-100/70",
  },
  warning: {
    icon: "bg-warning-100 text-warning-600",
    ring: "hover:ring-warning-200",
    glow: "from-warning-100/80",
  },
  success: {
    icon: "bg-success-100 text-success-600",
    ring: "hover:ring-success-200",
    glow: "from-success-100/80",
  },
  danger: {
    icon: "bg-danger-100 text-danger-600",
    ring: "hover:ring-danger-200",
    glow: "from-danger-100/80",
  },
  empleado: {
    icon: "bg-empleado-100 text-empleado-600",
    ring: "hover:ring-empleado-200",
    glow: "from-empleado-100/70",
  },
};

export default function QuickAccessCard({ to, icon: Icon, title, desc, tone = "blue", large = false }) {
  const t = TONE[tone] || TONE.blue;
  return (
    <Link
      to={to}
      className={`group relative flex flex-col justify-between overflow-hidden rounded-2xl border border-white bg-white/85 p-6 shadow-glass-sm ring-1 ring-transparent backdrop-blur-md transition-all duration-200 hover:-translate-y-1 hover:shadow-glass-lg ${t.ring} ${
        large ? "min-h-[240px] lg:p-7" : "min-h-[240px]"
      }`}
    >
      <div
        className={`pointer-events-none absolute -right-8 -top-8 h-28 w-28 rounded-full bg-gradient-to-br opacity-60 blur-xl transition-transform duration-300 group-hover:scale-110 ${t.glow} to-transparent`}
        aria-hidden="true"
      />
      <div className="relative z-10">
        <div
          className={`mb-5 flex items-center justify-center rounded-2xl transition-colors duration-200 ${t.icon} ${
            large ? "h-14 w-14" : "h-12 w-12"
          }`}
        >
          <Icon size={large ? 26 : 22} strokeWidth={2.1} />
        </div>
        <h3
          className={`font-[var(--font-display)] font-bold leading-snug text-ink-900 ${
            large ? "text-[22px]" : "text-[17px]"
          }`}
        >
          {title}
        </h3>
        <p className="mt-2 text-[13.5px] leading-relaxed text-ink-500">{desc}</p>
      </div>
      <span className="relative z-10 mt-6 inline-flex items-center gap-1.5 text-[13px] font-bold text-ink-800">
        Acceder al módulo
        <ArrowUpRight
          size={15}
          className="transition-transform duration-200 group-hover:translate-x-0.5 group-hover:-translate-y-0.5"
        />
      </span>
    </Link>
  );
}
