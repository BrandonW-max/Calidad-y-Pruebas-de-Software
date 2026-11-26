import { useEffect, useId, useRef, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import {
  Home, Users, UserRound, Clock, ClipboardList, Wallet,
  CalendarDays, BarChart3, ChevronRight, LogOut, Menu, X,
} from "lucide-react";
import logo from "../assets/logo.png";
import { clearSession, logout } from "../services/api";

const NAV = {
  RRHH: {
    title: "Panel RRHH",
    subtitle: "Recursos Humanos",
    accent: "rrhh",
    links: [
      { to: "/rrhh", icon: Home, label: "Inicio" },
      {
        icon: Users, label: "Gestión de Empleados",
        group: true,
        basePaths: ["/rrhh/empleados", "/rrhh/horarios"],
        children: [
          { to: "/rrhh/empleados", icon: UserRound, label: "Empleados" },
          { to: "/rrhh/horarios", icon: Clock, label: "Horarios" },
        ],
      },
      { to: "/rrhh/asistencia", icon: ClipboardList, label: "Control de Asistencia" },
      { to: "/rrhh/nomina", icon: Wallet, label: "Cálculo de Nómina" },
      { to: "/rrhh/solicitudes", icon: CalendarDays, label: "Solicitudes" },
      { to: "/rrhh/reportes", icon: BarChart3, label: "Reportes" },
    ],
  },
  EMPLEADO: {
    title: "Mi Portal",
    subtitle: "Portal del Empleado",
    accent: "empleado",
    links: [
      { to: "/empleado", icon: Home, label: "Inicio" },
      { to: "/empleado/asistencia", icon: ClipboardList, label: "Mi Asistencia" },
      { to: "/empleado/solicitudes", icon: CalendarDays, label: "Mis Solicitudes" },
      { to: "/empleado/nomina", icon: Wallet, label: "Mi Nómina" },
    ],
  },
  GERENCIA: {
    title: "Panel Gerencia",
    subtitle: "Gestión Ejecutiva",
    accent: "gerencia",
    links: [
      { to: "/gerencia", icon: Home, label: "Inicio" },
      { to: "/gerencia/solicitudes", icon: CalendarDays, label: "Solicitudes" },
      { to: "/gerencia/asistencia", icon: ClipboardList, label: "Mi Asistencia" },
    ],
  },
};

const ACCENT = {
  rrhh: {
    rail: "from-ink-950 via-ink-900 to-rrhh-950",
    glow: "bg-rrhh-500/30",
    activeBg: "bg-rrhh-500/20",
    activeBorder: "border-rrhh-400/40",
    activeText: "text-white",
    iconActive: "text-rrhh-400",
    ring: "focus-visible:ring-rrhh-400",
    badge: "bg-rrhh-500",
  },
  empleado: {
    rail: "from-ink-950 via-ink-900 to-empleado-900",
    glow: "bg-empleado-500/30",
    activeBg: "bg-empleado-500/20",
    activeBorder: "border-empleado-500/40",
    activeText: "text-white",
    iconActive: "text-empleado-500",
    ring: "focus-visible:ring-empleado-500",
    badge: "bg-empleado-600",
  },
  gerencia: {
    rail: "from-ink-950 via-ink-900 to-gerencia-900",
    glow: "bg-gerencia-500/30",
    activeBg: "bg-gerencia-500/20",
    activeBorder: "border-gerencia-500/40",
    activeText: "text-white",
    iconActive: "text-gerencia-500",
    ring: "focus-visible:ring-gerencia-500",
    badge: "bg-gerencia-600",
  },
};

export default function Sidebar({ rol }) {
  const location = useLocation();
  const navigate = useNavigate();
  const config = NAV[rol] || NAV.RRHH;
  const tone = ACCENT[config.accent];

  const initialOpen = () => {
    const item = config.links.find(l =>
      l.group && l.basePaths?.some(p => location.pathname.startsWith(p))
    );
    return item ? item.label : null;
  };
  const [openGroup, setOpenGroup] = useState(initialOpen);
  const [mobileOpen, setMobileOpen] = useState(false);
  const [isMobileView, setIsMobileView] = useState(() =>
    typeof window !== "undefined" && !window.matchMedia("(min-width: 64rem)").matches
  );
  const mobileTriggerRef = useRef(null);
  const mobileCloseRef = useRef(null);
  const sidebarId = useId();

  useEffect(() => {
    const mediaQuery = window.matchMedia("(min-width: 64rem)");
    const updateViewport = event => {
      setIsMobileView(!event.matches);
      if (event.matches) setMobileOpen(false);
    };
    setIsMobileView(!mediaQuery.matches);
    mediaQuery.addEventListener("change", updateViewport);
    return () => mediaQuery.removeEventListener("change", updateViewport);
  }, []);

  useEffect(() => {
    if (!mobileOpen || !isMobileView) return undefined;
    mobileCloseRef.current?.focus();
    const handleEscape = event => {
      if (event.key === "Escape") {
        setMobileOpen(false);
        requestAnimationFrame(() => mobileTriggerRef.current?.focus());
      }
    };
    window.addEventListener("keydown", handleEscape);
    return () => window.removeEventListener("keydown", handleEscape);
  }, [mobileOpen, isMobileView]);

  const isActive = (to) => location.pathname === to;
  const toggleGroup = (label) => setOpenGroup(g => (g === label ? null : label));
  const closeMobile = () => {
    setMobileOpen(false);
    requestAnimationFrame(() => mobileTriggerRef.current?.focus());
  };
  const closeMobileForNavigation = () => setMobileOpen(false);

  const handleLogout = async () => {
    try { await logout(); } catch { /* sesión ya inválida en servidor */ }
    clearSession();
    navigate("/", { replace: true });
  };

  const linkBase =
    "group relative flex w-full items-center gap-3.5 rounded-2xl pl-6 pr-4 py-4 text-[17px] font-medium " +
    "text-white/85 transition-all duration-200 hover:bg-white/10 hover:text-white outline-none " +
    "focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-offset-ink-900 " + tone.ring;

  return (
    <>
      <button
        ref={mobileTriggerRef}
        type="button"
        onClick={() => setMobileOpen(true)}
        aria-label="Abrir menú de navegación"
        aria-expanded={mobileOpen}
        aria-controls={`${sidebarId}-navigation`}
        className="fixed left-4 top-4 z-30 flex h-11 w-11 items-center justify-center rounded-xl border border-ink-200 bg-white/90 text-ink-700 shadow-glass-md backdrop-blur-md lg:hidden"
      >
        <Menu size={20} strokeWidth={2.2} />
      </button>

      {mobileOpen && (
        <div
          className="fixed inset-0 z-40 bg-ink-950/55 backdrop-blur-sm lg:hidden"
          onClick={closeMobile}
          aria-hidden="true"
        />
      )}

    <aside
      inert={isMobileView && !mobileOpen}
      className={
        "fixed inset-y-0 left-0 z-50 flex h-screen w-[300px] flex-col overflow-hidden " +
        "bg-gradient-to-b " + tone.rail + " shadow-[4px_0_28px_rgba(0,0,0,0.28)] " +
        "transition-transform duration-300 ease-out lg:translate-x-0 " +
        (mobileOpen ? "translate-x-0" : "-translate-x-full")
      }
    >
      <button
        ref={mobileCloseRef}
        type="button"
        onClick={closeMobile}
        aria-label="Cerrar menú"
        className="absolute right-3 top-3 z-20 flex h-8 w-8 items-center justify-center rounded-lg text-white/60 hover:bg-white/10 hover:text-white lg:hidden"
      >
        <X size={18} />
      </button>
      {/* glow decorativo */}
      <div className={"pointer-events-none absolute -top-24 -right-20 h-56 w-56 rounded-full blur-3xl " + tone.glow} aria-hidden="true" />
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top,rgba(255,255,255,0.06),transparent_55%)]" aria-hidden="true" />

      <div className="relative z-10 flex items-center gap-3 border-b border-white/10 px-5 pb-7 pt-6">
        <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-white/10 ring-1 ring-white/15">
          <img src={logo} alt="Logo Hospital" className="h-9 w-9 object-contain" />
        </div>
        <div className="min-w-0">
          <h2 className="truncate text-[18px] font-bold leading-tight text-white">{config.title}</h2>
          <p className="truncate text-[14px] font-medium text-white/70">{config.subtitle}</p>
        </div>
      </div>

      <nav id={`${sidebarId}-navigation`} aria-label="Navegación principal" className="relative z-10 flex flex-1 flex-col gap-2.5 overflow-y-auto px-3 pt-8 pb-4 [scrollbar-width:none] [&::-webkit-scrollbar]:hidden">
        {config.links.map((link, linkIndex) => {
          if (link.group) {
            const isExpanded = openGroup === link.label;
            const groupPanelId = `${sidebarId}-group-${linkIndex}`;
            const isGroupActive = link.basePaths?.some(p => location.pathname.startsWith(p));
            const GroupIcon = link.icon;
            return (
              <div key={link.label}>
                <button
                  type="button"
                  onClick={() => toggleGroup(link.label)}
                  aria-expanded={isExpanded}
                  aria-controls={groupPanelId}
                  className={
                    linkBase + " cursor-pointer border border-transparent " +
                    (isGroupActive ? tone.activeBg + " " + tone.activeBorder + " " + tone.activeText + " font-semibold" : "")
                  }
                >
                  {isGroupActive && <span className={"absolute left-0 top-1/2 h-8 w-[3px] -translate-y-1/2 rounded-r-full " + tone.badge} aria-hidden="true" />}
                  <GroupIcon size={24} className={"shrink-0 " + (isGroupActive ? tone.iconActive : "text-white/55 group-hover:text-white/80")} strokeWidth={2} />
                  <span className="flex-1 text-left">{link.label}</span>
                  <ChevronRight size={20} className={"shrink-0 text-white/40 transition-transform duration-200 " + (isExpanded ? "rotate-90" : "")} />
                </button>
                <div id={groupPanelId} style={isExpanded ? undefined : { display: "none" }} className={isExpanded ? "mt-2 ml-4 flex flex-col gap-1.5 border-l border-white/12 pl-3" : undefined}>
                    {link.children.map((child) => {
                      const ChildIcon = child.icon;
                      const active = isActive(child.to);
                      return (
                        <Link
                          key={child.to}
                          to={child.to}
                          onClick={closeMobileForNavigation}
                          className={
                            "flex items-center gap-3 rounded-xl px-4 py-3 text-[16px] font-medium transition-all duration-150 " +
                            (active
                              ? "bg-white/12 text-white"
                              : "text-white/60 hover:bg-white/8 hover:text-white")
                          }
                        >
                          <ChildIcon size={22} className={"shrink-0 " + (active ? tone.iconActive : "text-white/45")} strokeWidth={2} />
                          <span>{child.label}</span>
                        </Link>
                      );
                    })}
                </div>
              </div>
            );
          }

          const Icon = link.icon;
          const active = isActive(link.to);
          return (
            <Link
              key={link.to}
              to={link.to}
              onClick={closeMobileForNavigation}
              aria-current={active ? "page" : undefined}
              className={
                linkBase + " border " +
                (active ? tone.activeBg + " " + tone.activeBorder + " " + tone.activeText + " font-semibold" : "border-transparent")
              }
            >
              {active && <span className={"absolute left-0 top-1/2 h-8 w-[3px] -translate-y-1/2 rounded-r-full " + tone.badge} aria-hidden="true" />}
              <Icon size={24} className={"shrink-0 " + (active ? tone.iconActive : "text-white/55 group-hover:text-white/80")} strokeWidth={2} />
              <span className="flex-1">{link.label}</span>
            </Link>
          );
        })}
      </nav>

      <div className="relative z-10 border-t border-white/10 px-3 py-4">
        <button
          type="button"
          onClick={handleLogout}
          className="flex w-full cursor-pointer items-center gap-3.5 rounded-2xl border border-white/10 bg-white/5 px-4 py-5 text-[17px] font-medium text-white/65 transition-all duration-200 hover:border-danger-600/40 hover:bg-danger-600/20 hover:text-red-200 outline-none focus-visible:ring-2 focus-visible:ring-red-400"
        >
          <LogOut size={22} strokeWidth={2} className="shrink-0" />
          <span>Cerrar Sesión</span>
        </button>
      </div>
    </aside>
    </>
  );
}
