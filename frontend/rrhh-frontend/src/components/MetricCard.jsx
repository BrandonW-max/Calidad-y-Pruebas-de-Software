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

export default function MetricCard({ value, label, icon: Icon, tone = "blue" }) {
  const t = TONE[tone] || TONE.blue;
  return (
    <article
      className={`group relative overflow-hidden rounded-2xl border border-white bg-white/85 p-5 shadow-glass-sm ring-1 ring-transparent backdrop-blur-md transition-all duration-200 hover:-translate-y-1 hover:shadow-glass-lg ${t.ring}`}
    >
      <div
        className={`pointer-events-none absolute -right-6 -top-6 h-24 w-24 rounded-full bg-gradient-to-br opacity-70 blur-xl ${t.glow} to-transparent`}
        aria-hidden="true"
      />
      <div className={`relative z-10 mb-5 flex h-11 w-11 items-center justify-center rounded-xl ${t.icon}`}>
        <Icon size={20} strokeWidth={2.25} />
      </div>
      <div className="relative z-10">
        <strong className="block font-[var(--font-display)] text-[34px] font-extrabold leading-none text-ink-900 sm:text-[42px]">
          {value}
        </strong>
        <span className="mt-1.5 block text-[13px] font-semibold leading-snug text-ink-500">
          {label}
        </span>
      </div>
    </article>
  );
}
