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
import DevLogin from "./pages/DevLogin"; // ELIMINAR ANTES DE PRODUCCIÓN

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

      {/* DEV — ELIMINAR ANTES DE PRODUCCIÓN */}
      <Route path="/dev" element={<DevLogin />} />

      {/* Catch-all: ruta no encontrada */}
      <Route path="*" element={<Acceso403 />} />
    </Routes>
  );
}

export default App;
