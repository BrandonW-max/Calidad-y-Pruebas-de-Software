import { Routes, Route, Navigate } from "react-router-dom";
import { getUser } from "./services/api";
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

function ProtectedRoute({ children, requiredRole }) {
  const user = getUser();
  if (!user?.token) return <Navigate to="/" replace />;
  if (requiredRole && user.rol !== requiredRole) return <Acceso403 />;
  return children;
}

function App() {
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

      {/* Gerencia */}
      <Route path="/gerencia" element={<ProtectedRoute requiredRole="GERENCIA"><DashboardGerencia /></ProtectedRoute>} />
      <Route path="/gerencia/solicitudes" element={<ProtectedRoute requiredRole="GERENCIA"><SolicitudPermisos /></ProtectedRoute>} />
    </Routes>
  );
}

export default App;
