import { Routes, Route } from "react-router-dom";
import Login from "./pages/Login";
import DashboardRRHH from "./pages/DashboardRRHH";
import DashboardEmpleado from "./pages/DashboardEmpleado";
import DashboardGerencia from "./pages/DashboardGerencia";
import GestionEmpleados from "./pages/GestionEmpleados";
import ControlAsistencia from "./pages/ControlAsistencia";
import CalculoNomina from "./pages/CalculoNomina";
import SolicitudPermisos from "./pages/SolicitudPermisos";
import GenerarReportes from "./pages/GenerarReportes";

function App() {
  return (
    <Routes>
      {/* Auth — ECU-01 */}
      <Route path="/" element={<Login />} />

      {/* RRHH */}
      <Route path="/rrhh" element={<DashboardRRHH />} />
      <Route path="/rrhh/empleados" element={<GestionEmpleados />} />
      <Route path="/rrhh/asistencia" element={<ControlAsistencia />} />
      <Route path="/rrhh/nomina" element={<CalculoNomina />} />
      <Route path="/rrhh/solicitudes" element={<SolicitudPermisos />} />
      <Route path="/rrhh/reportes" element={<GenerarReportes />} />

      {/* Empleado */}
      <Route path="/empleado" element={<DashboardEmpleado />} />
      <Route path="/empleado/asistencia" element={<ControlAsistencia />} />
      <Route path="/empleado/solicitudes" element={<SolicitudPermisos />} />

      {/* Gerencia */}
      <Route path="/gerencia" element={<DashboardGerencia />} />
      <Route path="/gerencia/solicitudes" element={<SolicitudPermisos />} />
    </Routes>
  );
}

export default App;