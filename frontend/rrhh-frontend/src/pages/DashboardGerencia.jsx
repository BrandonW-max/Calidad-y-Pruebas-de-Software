import {
  Users, Hourglass, CheckCircle2, XCircle, CalendarDays,
} from "lucide-react";
import DashboardLayout from "../components/DashboardLayout";
import DashboardHero from "../components/DashboardHero";
import MetricCard from "../components/MetricCard";
import QuickAccessCard from "../components/QuickAccessCard";

export default function DashboardGerencia() {
  const metricas = [
    { key: "total", value: "—", label: "Total Empleados", icon: Users, tone: "gerencia" },
    { key: "porAprobar", value: "—", label: "Por Aprobar", icon: Hourglass, tone: "warning" },
    { key: "aprobadas", value: "—", label: "Aprobadas (Mes)", icon: CheckCircle2, tone: "success" },
    { key: "rechazadas", value: "—", label: "Rechazadas (Mes)", icon: XCircle, tone: "danger" },
  ];

  return (
    <DashboardLayout rol="GERENCIA">
      <DashboardHero nombre="Gerencia" rol="Panel Ejecutivo" badge="Gestión Ejecutiva" theme="gerencia" />

      <section aria-label="Resumen ejecutivo" className="mb-12 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {metricas.map(item => (
          <MetricCard key={item.key} value={item.value} label={item.label} icon={item.icon} tone={item.tone} />
        ))}
      </section>

      <section aria-label="Accesos de gerencia" className="grid grid-cols-1">
        <QuickAccessCard
          to="/gerencia/solicitudes"
          icon={CalendarDays}
          title="Solicitudes Pendientes"
          desc="Aprobar o rechazar solicitudes de permisos y vacaciones elevadas por Recursos Humanos."
          tone="gerencia"
          large
        />
      </section>
    </DashboardLayout>
  );
}
