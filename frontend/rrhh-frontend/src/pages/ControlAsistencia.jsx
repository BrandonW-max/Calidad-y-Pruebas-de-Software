import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import "../styles/Dashboard.css";
import {
  listarJustificacionesPendientes,
  registrarManual,
  revisarJustificacion,
} from "../services/api";

export default function ControlAsistencia() {
  const [justificaciones, setJustificaciones] = useState([]);
  const [loading, setLoading] = useState(true);
  const [mensaje, setMensaje] = useState("");
  const [error, setError] = useState("");

  const [manualForm, setManualForm] = useState({
    idEmpleado: "", tipo: "ENTRADA", fechaHora: "", motivo: "",
  });

  useEffect(() => { cargarJustificaciones(); }, []);

  const cargarJustificaciones = async () => {
    setLoading(true);
    try {
      const data = await listarJustificacionesPendientes();
      setJustificaciones(Array.isArray(data) ? data : []);
    } catch {
      setError("No se pudieron cargar las justificaciones");
    } finally {
      setLoading(false);
    }
  };

  const handleRegistrarManual = async (e) => {
    e.preventDefault();
    setMensaje("");
    try {
      await registrarManual({
        idEmpleado: parseInt(manualForm.idEmpleado),
        tipo: manualForm.tipo,
        fechaHora: manualForm.fechaHora ? manualForm.fechaHora.replace("T", " ") + ":00" : null,
        motivo: manualForm.motivo,
      });
      setMensaje("Registro manual guardado correctamente");
      setManualForm({ idEmpleado: "", tipo: "ENTRADA", fechaHora: "", motivo: "" });
    } catch {
      setMensaje("Error al guardar el registro manual");
    }
  };

  const handleRevisar = async (id, decision) => {
    const comentario = decision === "RECHAZADO" ? prompt("Motivo del rechazo:") ?? "" : "";
    try {
      await revisarJustificacion(id, decision, comentario);
      setMensaje(`Justificación ${decision.toLowerCase()} correctamente`);
      cargarJustificaciones();
    } catch {
      setMensaje("Error al revisar la justificación");
    }
  };

  return (
    <div className="dashboard">
      <aside className="sidebar">
        <div className="sidebar-header"><h2>Panel RRHH</h2><p>Recursos Humanos</p></div>
        <nav className="sidebar-nav">
          <Link to="/rrhh">🏠 Inicio</Link>
          <Link to="/rrhh/empleados">👥 Gestión de Empleados</Link>
          <Link to="/rrhh/asistencia" className="active">📋 Control de Asistencia</Link>
          <Link to="/rrhh/nomina">💰 Cálculo de Nómina</Link>
          <Link to="/rrhh/solicitudes">📅 Solicitudes</Link>
          <Link to="/rrhh/reportes">📊 Reportes</Link>
          <div className="nav-logout"><Link to="/">🚪 Cerrar Sesión</Link></div>
        </nav>
      </aside>

      <main className="main-content">
        <header className="main-header">
          <h1>Control de Asistencia</h1>
          <span>ECU-03</span>
        </header>

        {mensaje && <div className="alert alert-success">{mensaje}</div>}
        {error   && <div className="alert alert-warning">{error}</div>}

        <div className="alert alert-info">
          🖐️ El registro de entrada/salida se realiza desde el dispositivo biométrico. Aquí se gestionan correcciones y justificaciones.
        </div>

        {/* ── REGISTRO MANUAL ── */}
        <div className="section-card">
          <h2>Registro Manual de Asistencia (Corrección)</h2>
          <div className="alert alert-warning">
            ⚠️ Esta acción queda registrada en el log de auditoría.
          </div>
          <form onSubmit={handleRegistrarManual}>
            <div className="form-grid">
              <div className="form-group">
                <label>ID Empleado *</label>
                <input type="number" value={manualForm.idEmpleado}
                  onChange={(e) => setManualForm({ ...manualForm, idEmpleado: e.target.value })} required />
              </div>
              <div className="form-group">
                <label>Tipo de Marcación *</label>
                <select value={manualForm.tipo}
                  onChange={(e) => setManualForm({ ...manualForm, tipo: e.target.value })}>
                  <option value="ENTRADA">Entrada</option>
                  <option value="SALIDA">Salida</option>
                </select>
              </div>
              <div className="form-group">
                <label>Fecha y Hora *</label>
                <input type="datetime-local" value={manualForm.fechaHora}
                  onChange={(e) => setManualForm({ ...manualForm, fechaHora: e.target.value })} required />
              </div>
              <div className="form-group form-full">
                <label>Motivo de Corrección *</label>
                <textarea value={manualForm.motivo}
                  onChange={(e) => setManualForm({ ...manualForm, motivo: e.target.value })} required />
              </div>
            </div>
            <div className="form-actions">
              <button type="submit" className="btn btn-success">Guardar Registro Manual</button>
              <button type="button" className="btn btn-secondary"
                onClick={() => setManualForm({ idEmpleado: "", tipo: "ENTRADA", fechaHora: "", motivo: "" })}>
                Limpiar
              </button>
            </div>
          </form>
        </div>

        {/* ── JUSTIFICACIONES PENDIENTES ── */}
        <div className="section-card">
          <h2>Justificaciones de Tardanza Pendientes</h2>
          <div className="table-wrapper">
            {loading ? (
              <p style={{ padding: "16px", color: "#888" }}>Cargando...</p>
            ) : (
              <table>
                <thead>
                  <tr>
                    <th>ID</th><th>Empleado</th><th>Fecha Registro</th>
                    <th>Motivo</th><th>Estado</th><th>Decisión</th>
                  </tr>
                </thead>
                <tbody>
                  {justificaciones.length === 0 ? (
                    <tr><td colSpan={6} style={{ textAlign: "center", color: "#888" }}>No hay justificaciones pendientes</td></tr>
                  ) : justificaciones.map((j) => (
                    <tr key={j.id ?? j.idJustificacion}>
                      <td>{j.id ?? j.idJustificacion}</td>
                      <td>{j.idEmpleado}</td>
                      <td>{j.fechaRegistro ?? "—"}</td>
                      <td>{j.motivo}</td>
                      <td><span className="badge badge-warning">{j.estado ?? "Pendiente"}</span></td>
                      <td>
                        <button className="btn btn-success btn-sm"
                          onClick={() => handleRevisar(j.id ?? j.idJustificacion, "APROBADO")}>
                          Aprobar
                        </button>{" "}
                        <button className="btn btn-danger btn-sm"
                          onClick={() => handleRevisar(j.id ?? j.idJustificacion, "RECHAZADO")}>
                          Rechazar
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
          <div className="form-actions" style={{ marginTop: "12px" }}>
            <button className="btn btn-secondary" onClick={cargarJustificaciones}>Actualizar</button>
          </div>
        </div>
      </main>
    </div>
  );
}
