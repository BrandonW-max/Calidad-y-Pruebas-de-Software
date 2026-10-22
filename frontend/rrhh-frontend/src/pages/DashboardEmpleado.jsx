import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import {
  Clock, ClipboardList, Wallet, Bell, TreePalm, CheckCircle2,
} from "lucide-react";
import DashboardLayout from "../components/DashboardLayout";
import DashboardHero from "../components/DashboardHero";
import MetricCard from "../components/MetricCard";
import { getUser, getIdEmpleado, getSolicitudesPorEmpleado, getNotificacionesEmpleado, getAsistenciaHoy } from "../services/api";

export default function DashboardEmpleado() {
  const user = getUser();
  const idEmpleado = getIdEmpleado();

  const [solicitudesPendientes, setSolicitudesPendientes] = useState(null);
  const [asistenciaHoy, setAsistenciaHoy] = useState(null);
  const [vacacionesDias] = useState(null);
  const [notifCount, setNotifCount] = useState(0);

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
      const hoy = await getAsistenciaHoy(idEmpleado);
      setAsistenciaHoy(hoy);
    } catch { /* silently fail */ }
    try {
      const notif = await getNotificacionesEmpleado(idEmpleado);
      if (Array.isArray(notif)) setNotifCount(notif.length);
    } catch { /* silently fail */ }
  };

  useEffect(() => {
    if (idEmpleado) cargarDatos();
  }, [idEmpleado]); // eslint-disable-line react-hooks/exhaustive-deps

  const displayName = user?.username || "Usuario";

  const metricas = [
    {
      key: "asistencia",
      value: asistenciaHoy?.horaEntrada || "—:—",
      label: "Entrada Hoy",
      icon: Clock,
      tone: "teal",
    },
    {
      key: "solicitudes",
      value: solicitudesPendientes ?? "···",
      label: "Solicitudes Pendientes",
      icon: ClipboardList,
      tone: "warning",
    },
    {
      key: "vacaciones",
      value: vacacionesDias ?? "···",
      label: "Vacaciones Disponibles",
      icon: TreePalm,
      tone: "success",
    },
  ];

  const accesos = [
    { to: "/empleado/asistencia", icon: Clock, label: "Registrar Asistencia" },
    { to: "/empleado/solicitudes", icon: ClipboardList, label: "Solicitar Permiso" },
    { to: "/empleado/nomina", icon: Wallet, label: "Mi Nómina" },
    { to: "/empleado/solicitudes", icon: Bell, label: "Ver Notificaciones", badge: notifCount },
  ];

  return (
    <DashboardLayout rol="EMPLEADO">
      <DashboardHero nombre={displayName} rol="Mi Portal" badge="Empleado" theme="empleado" />

      <section aria-label="Resumen de asistencia y solicitudes" className="mb-12 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {metricas.map(item => (
          <MetricCard key={item.key} value={item.value} label={item.label} icon={item.icon} tone={item.tone} />
        ))}
      </section>

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
    </DashboardLayout>
  );
}
