import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import {
  Clock, ClipboardList, Wallet, Bell, TreePalm, CheckCircle2, UserRound,
} from "lucide-react";
import Sidebar from "../components/Sidebar";
import { getUser, getIdEmpleado, getSolicitudesPorEmpleado, getNotificacionesEmpleado } from "../services/api";

export default function DashboardEmpleado() {
  const user = getUser();
  const idEmpleado = getIdEmpleado();

  const [solicitudesPendientes, setSolicitudesPendientes] = useState(null);
  const [asistenciaHoy] = useState(null);
  const [vacacionesDias] = useState(null);
  const [hora, setHora] = useState(new Date());
  const [notifCount, setNotifCount] = useState(0);

  useEffect(() => {
    const t = setInterval(() => setHora(new Date()), 60000);
    return () => clearInterval(t);
  }, []);

  const cargarDatos = async () => {
    try {
      const solicitudes = await getSolicitudesPorEmpleado(idEmpleado);
      if (Array.isArray(solicitudes)) {
        const pendientes = solicitudes.filter(
          s => s.estado === "PENDIENTE" || s.estado === "PROCESADO"
        ).length;
        setSolicitudesPendientes(pendientes);
      }
    } catch { /* silently fail */ }
    try {
      const notif = await getNotificacionesEmpleado(idEmpleado);
      if (Array.isArray(notif)) setNotifCount(notif.length);
    } catch { /* silently fail */ }
  };

  useEffect(() => {
    if (idEmpleado) cargarDatos();
  }, [idEmpleado]); // eslint-disable-line react-hooks/exhaustive-deps

  const horaFmt = hora.toLocaleTimeString("es-PE", { hour: "2-digit", minute: "2-digit", hour12: true });
  const displayName = user?.username || "Usuario";

  const accesos = [
    { to: "/empleado/asistencia", icon: Clock, label: "Registrar Asistencia" },
    { to: "/empleado/solicitudes", icon: ClipboardList, label: "Solicitar Permiso" },
    { to: "/empleado/nomina", icon: Wallet, label: "Mi Nómina" },
    { to: "/empleado/solicitudes", icon: Bell, label: "Ver Notificaciones", badge: notifCount },
  ];

  return (
    <div className="min-h-screen bg-ink-100">
      <Sidebar rol="EMPLEADO" />

      <main className="min-h-screen px-5 pb-7 pt-20 sm:px-6 lg:ml-[272px] lg:px-9 lg:py-9 lg:pt-9">
        {/* ── Hero ── */}
        <header className="relative mb-7 flex flex-col items-start gap-5 overflow-hidden rounded-3xl border border-white bg-white/75 px-8 py-9 shadow-glass-md backdrop-blur-xl sm:flex-row sm:items-center sm:justify-between">
          <div
            className="pointer-events-none absolute inset-y-0 right-0 w-[45%] bg-gradient-to-bl from-empleado-100 via-signal-100/60 to-transparent"
            style={{ clipPath: "polygon(28% 0, 100% 0, 100% 100%, 0 100%)" }}
            aria-hidden="true"
          />
          <div className="relative z-10">
            <p className="mb-2 inline-flex items-center gap-2 text-[12px] font-extrabold tracking-[0.16em] text-empleado-600 uppercase">
              <span className="h-[3px] w-8 rounded-full bg-empleado-600" />
              Mi Portal
            </p>
            <h1 className="font-[var(--font-display)] text-4xl font-extrabold leading-[1.05] text-ink-900 sm:text-[44px]">
              Bienvenido, {displayName}
            </h1>
          </div>
          <div className="relative z-10 flex items-center gap-3">
            <span className="inline-flex h-9 items-center rounded-full border border-empleado-500/30 bg-empleado-100 px-5 text-[12px] font-extrabold uppercase tracking-wide text-empleado-600">
              Empleado
            </span>
            <div className="flex h-11 w-11 items-center justify-center rounded-full border border-white bg-white text-ink-500 shadow-glass-sm">
              <UserRound size={20} strokeWidth={2} />
            </div>
          </div>
        </header>

        {/* ── Resumen ── */}
        <section aria-label="Resumen de asistencia y solicitudes" className="mb-7 grid grid-cols-1 gap-4 md:grid-cols-3">
          {/* Asistencia hoy */}
          <article className="rounded-2xl border border-white bg-white/85 p-6 shadow-glass-sm backdrop-blur-md">
            <div className="mb-5 flex items-center gap-3">
              <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-signal-100 text-signal-600">
                <Clock size={19} strokeWidth={2.1} />
              </div>
              <h3 className="text-[14.5px] font-bold text-ink-900">Mi Asistencia Hoy</h3>
            </div>
            <div className="flex gap-7">
              <div>
                <p className="text-[11px] font-bold uppercase tracking-wide text-ink-400">Entrada</p>
                <p className="mt-1 font-[var(--font-display)] text-[22px] font-bold tabular-nums text-ink-900">
                  {asistenciaHoy?.horaEntrada || horaFmt}
                </p>
                <span className="mt-1.5 inline-flex items-center gap-1 rounded-full bg-success-100 px-2.5 py-0.5 text-[11px] font-bold text-success-600">
                  <CheckCircle2 size={12} /> A tiempo
                </span>
              </div>
              <div>
                <p className="text-[11px] font-bold uppercase tracking-wide text-ink-400">Salida</p>
                <p className="mt-1 font-[var(--font-display)] text-[22px] font-bold tabular-nums text-ink-300">—:—</p>
                <p className="mt-1.5 text-[11px] text-ink-400">{asistenciaHoy?.restante || "Jornada en curso"}</p>
              </div>
            </div>
          </article>

          {/* Solicitudes pendientes */}
          <article className="relative rounded-2xl border border-white bg-white/85 p-6 shadow-glass-sm backdrop-blur-md">
            <div className="mb-5 flex items-center gap-3">
              <div className="relative flex h-10 w-10 items-center justify-center rounded-xl bg-warning-100 text-warning-600">
                <ClipboardList size={19} strokeWidth={2.1} />
              </div>
              <h3 className="text-[14.5px] font-bold text-ink-900">Solicitudes Pendientes</h3>
              {solicitudesPendientes !== null && solicitudesPendientes > 0 && (
                <span className="ml-auto flex h-5 min-w-[20px] items-center justify-center rounded-full bg-warning-600 px-1.5 text-[11px] font-bold text-white">
                  {solicitudesPendientes}
                </span>
              )}
            </div>
            <div className="flex flex-col items-center gap-2 py-2">
              <div className="flex h-14 w-14 items-center justify-center rounded-full bg-warning-100 text-warning-600">
                <ClipboardList size={24} strokeWidth={2} />
              </div>
              <span className="text-[12.5px] font-semibold text-ink-500">
                {solicitudesPendientes ?? "···"} pendientes
              </span>
            </div>
          </article>

          {/* Vacaciones */}
          <article className="relative rounded-2xl border border-white bg-white/85 p-6 shadow-glass-sm backdrop-blur-md">
            <div className="mb-5 flex items-center gap-3">
              <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-success-100 text-success-600">
                <TreePalm size={19} strokeWidth={2.1} />
              </div>
              <h3 className="text-[14.5px] font-bold text-ink-900">Vacaciones Disponibles</h3>
              {vacacionesDias !== null && (
                <span className="ml-auto flex h-5 min-w-[20px] items-center justify-center rounded-full bg-success-600 px-1.5 text-[11px] font-bold text-white">
                  {vacacionesDias}
                </span>
              )}
            </div>
            <div className="flex flex-col items-center gap-2 py-2">
              <div className="flex h-14 w-14 items-center justify-center rounded-full bg-success-100 text-success-600">
                <TreePalm size={24} strokeWidth={2} />
              </div>
              <span className="text-[12.5px] font-semibold text-ink-500">Días disponibles</span>
            </div>
          </article>
        </section>

        {/* ── Accesos directos ── */}
        <section className="rounded-2xl border border-white bg-white/85 p-6 shadow-glass-sm backdrop-blur-md" aria-label="Accesos directos">
          <h2 className="mb-5 font-[var(--font-display)] text-[16px] font-bold text-ink-900">Accesos Directos</h2>
          <div className="grid grid-cols-2 gap-3.5 sm:grid-cols-4">
            {accesos.map((item) => {
              const Icon = item.icon;
              return (
                <Link
                  key={item.label}
                  to={item.to}
                  className="group flex flex-col items-center gap-2.5 rounded-2xl border border-ink-200 px-3 py-5 text-center transition-all duration-200 hover:-translate-y-0.5 hover:border-empleado-500/50 hover:shadow-glass-md"
                >
                  <div className="relative flex h-12 w-12 items-center justify-center rounded-xl bg-empleado-100 text-empleado-600 transition-colors group-hover:bg-empleado-600 group-hover:text-white">
                    <Icon size={20} strokeWidth={2} />
                    {!!item.badge && (
                      <span className="absolute -right-1.5 -top-1.5 flex h-4 min-w-[16px] items-center justify-center rounded-full bg-danger-600 px-1 text-[10px] font-bold text-white">
                        {item.badge}
                      </span>
                    )}
                  </div>
                  <span className="text-[12.5px] font-semibold leading-tight text-ink-600">{item.label}</span>
                </Link>
              );
            })}
          </div>
        </section>
      </main>
    </div>
  );
}
