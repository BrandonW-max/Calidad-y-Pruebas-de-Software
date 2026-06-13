import { Routes, Route, Navigate } from "react-router-dom";
import { useEffect } from "react";
import { getUser, clearSession } from "./services/api";
import Login from "./pages/Login";
import Acceso403 from "./pages/Acceso403";
import DashboardRRHH from "./pages/DashboardRRHH";
import DashboardEmpleado from "./pages/DashboardEmpleado";
import DashboardGerencia from "./pages/DashboardGerencia";
import GestionEmpleados from "./pages/GestionEmpleados";
import ControlAsistencia from "./pages/ControlAsistencia";
import CalculoNomina from "./pages/CalculoNomina";
import SolicitudPermisos from "./pages/SolicitudPermisos";
import GenerarReportes from "./pages/GenerarReportes";
import NominaEmpleado from "./pages/NominaEmpleado";
import GerenciaJustificaciones from "./pages/GerenciaJustificaciones";
import DevLogin from "./pages/DevLogin"; // ELIMINAR ANTES DE PRODUCCIÓN

/* ── Autocorrector de URL ────────────────────────────────────────
   URL inexistente + usuario logueado → redirige a su dashboard.
   URL inexistente + sin sesión       → redirige al login.
   URLs de otro rol son capturadas antes por ProtectedRoute → 403.
──────────────────────────────────────────────────────────────── */
function AutoRedirect() {
  const user = getUser();
  const DASH = { RRHH: '/rrhh', EMPLEADO: '/empleado', GERENCIA: '/gerencia' };

  useEffect(() => {
    if (!user?.token) { window.location.href = '/'; return; }
    const t = setTimeout(() => { window.location.href = DASH[user.rol] ?? '/'; }, 2000);
    return () => clearTimeout(t);
  }, []);

  if (!user?.token) return null;
  return (
    <div style={{
      minHeight: '100vh', display: 'flex', flexDirection: 'column',
      alignItems: 'center', justifyContent: 'center', gap: '16px',
      background: '#f8fafc', fontFamily: "'Poppins', system-ui, sans-serif",
    }}>
      <div style={{ fontSize: '60px' }}>🔍</div>
      <h2 style={{ margin: 0, color: '#1e293b', fontSize: '22px' }}>Página no encontrada</h2>
      <p style={{ color: '#64748b', margin: 0, fontSize: '14px', textAlign: 'center' }}>
        La dirección que ingresaste no existe. Redirigiendo a tu panel en 2 segundos…
      </p>
      <div style={{
        background: '#eff6ff', border: '1px solid #bfdbfe', borderRadius: '8px',
        padding: '10px 22px', fontSize: '13px', color: '#2563eb', fontFamily: 'monospace',
      }}>
        → {DASH[user.rol] ?? '/'}
      </div>
    </div>
  );
}

function ProtectedRoute({ children, requiredRole }) {
  const user = getUser();
  if (!user?.token) return <Navigate to="/" replace />;
  if (requiredRole && user.rol !== requiredRole) return <Acceso403 />;
  return children;
}

function App() {
  useEffect(() => {
    let timer;
    const reset = () => {
      clearTimeout(timer);
      timer = setTimeout(() => { clearSession(); window.location.href = '/'; }, 15 * 60 * 1000);
    };
    ['mousemove', 'keydown', 'click', 'scroll'].forEach(e => window.addEventListener(e, reset));
    reset();
    return () => {
      clearTimeout(timer);
      ['mousemove', 'keydown', 'click', 'scroll'].forEach(e => window.removeEventListener(e, reset));
    };
  }, []);

  return (
    <Routes>
      {/* Auth */}
      <Route path="/" element={<Login />} />

      {/* RRHH */}
      <Route path="/rrhh" element={<ProtectedRoute requiredRole="RRHH"><DashboardRRHH /></ProtectedRoute>} />
      <Route path="/rrhh/empleados" element={<ProtectedRoute requiredRole="RRHH"><GestionEmpleados /></ProtectedRoute>} />
      <Route path="/rrhh/asistencia" element={<ProtectedRoute requiredRole="RRHH"><ControlAsistencia /></ProtectedRoute>} />
      <Route path="/rrhh/nomina" element={<ProtectedRoute requiredRole="RRHH"><CalculoNomina /></ProtectedRoute>} />
      <Route path="/rrhh/solicitudes" element={<ProtectedRoute requiredRole="RRHH"><SolicitudPermisos /></ProtectedRoute>} />
      <Route path="/rrhh/reportes" element={<ProtectedRoute requiredRole="RRHH"><GenerarReportes /></ProtectedRoute>} />

      {/* Empleado */}
      <Route path="/empleado" element={<ProtectedRoute requiredRole="EMPLEADO"><DashboardEmpleado /></ProtectedRoute>} />
      <Route path="/empleado/asistencia" element={<ProtectedRoute requiredRole="EMPLEADO"><ControlAsistencia /></ProtectedRoute>} />
      <Route path="/empleado/solicitudes" element={<ProtectedRoute requiredRole="EMPLEADO"><SolicitudPermisos /></ProtectedRoute>} />
      <Route path="/empleado/nomina" element={<ProtectedRoute requiredRole="EMPLEADO"><NominaEmpleado /></ProtectedRoute>} />

      {/* Gerencia */}
      <Route path="/gerencia" element={<ProtectedRoute requiredRole="GERENCIA"><DashboardGerencia /></ProtectedRoute>} />
      <Route path="/gerencia/solicitudes" element={<ProtectedRoute requiredRole="GERENCIA"><SolicitudPermisos /></ProtectedRoute>} />
      <Route path="/gerencia/justificaciones" element={<ProtectedRoute requiredRole="GERENCIA"><GerenciaJustificaciones /></ProtectedRoute>} />

      {/* DEV — ELIMINAR ANTES DE PRODUCCIÓN */}
      <Route path="/dev" element={<DevLogin />} />

      {/* Catch-all: URL inexistente → autocorrector */}
      <Route path="*" element={<AutoRedirect />} />
    </Routes>
  );
}

export default App;
