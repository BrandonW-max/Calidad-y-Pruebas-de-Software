import { useState } from "react";
import { Link } from "react-router-dom";
import "../styles/Dashboard.css";
import { calcularNomina, enviarComprobante, descargarComprobante } from "../services/api";

export default function CalculoNomina() {
  const [params, setParams] = useState({ fechaInicio: "", fechaFin: "", idEmpleado: "", idDepartamento: "" });
  const [nominas, setNominas] = useState([]);
  const [loading, setLoading] = useState(false);
  const [mensaje, setMensaje] = useState("");
  const [error, setError] = useState("");

  const totales = nominas.reduce(
    (acc, n) => ({
      bruto: acc.bruto + parseFloat(n.sueldoBruto ?? 0),
      neto: acc.neto + parseFloat(n.sueldoNeto ?? 0),
      descuentos: acc.descuentos + parseFloat(n.descuentoTardanzas ?? 0) + parseFloat(n.descuentoLey ?? 0),
    }),
    { bruto: 0, neto: 0, descuentos: 0 }
  );

  const handleCalcular = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    setMensaje("");
    setNominas([]);
    try {
      const dto = {
        fechaInicio: params.fechaInicio,
        fechaFin: params.fechaFin,
        idEmpleado: params.idEmpleado ? parseInt(params.idEmpleado) : null,
        idDepartamento: params.idDepartamento ? parseInt(params.idDepartamento) : null,
      };
      const data = await calcularNomina(dto);
      setNominas(Array.isArray(data) ? data : [data]);
      setMensaje("Nómina calculada correctamente");
    } catch (err) {
      setError("Error al calcular la nómina. Verifique los registros de asistencia.");
    } finally {
      setLoading(false);
    }
  };

  const handleEnviarComprobante = async (id) => {
    try {
      await enviarComprobante(id);
      setMensaje(`Comprobante enviado al correo del empleado`);
    } catch {
      setMensaje("Error al enviar el comprobante");
    }
  };

  const handleDescargar = async (id) => {
    try {
      const res = await descargarComprobante(id);
      const blob = await res.blob();
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `comprobante_${id}.pdf`;
      a.click();
      URL.revokeObjectURL(url);
    } catch {
      setMensaje("Error al descargar el comprobante");
    }
  };

  return (
    <div className="dashboard">
      <aside className="sidebar">
        <div className="sidebar-header"><h2>Panel RRHH</h2><p>Recursos Humanos</p></div>
        <nav className="sidebar-nav">
          <Link to="/rrhh">🏠 Inicio</Link>
          <Link to="/rrhh/empleados">👥 Gestión de Empleados</Link>
          <Link to="/rrhh/asistencia">📋 Control de Asistencia</Link>
          <Link to="/rrhh/nomina" className="active">💰 Cálculo de Nómina</Link>
          <Link to="/rrhh/solicitudes">📅 Solicitudes</Link>
          <Link to="/rrhh/reportes">📊 Reportes</Link>
          <div className="nav-logout"><Link to="/">🚪 Cerrar Sesión</Link></div>
        </nav>
      </aside>

      <main className="main-content">
        <header className="main-header">
          <h1>Cálculo de Nómina</h1>
          <span>ECU-04</span>
        </header>

        {mensaje && <div className="alert alert-success">{mensaje}</div>}
        {error   && <div className="alert alert-warning">{error}</div>}

        {/* ── PARÁMETROS ── */}
        <div className="section-card">
          <h2>Parámetros de Cálculo</h2>
          <form onSubmit={handleCalcular}>
            <div className="form-grid">
              <div className="form-group">
                <label>Fecha Inicio *</label>
                <input type="date" value={params.fechaInicio}
                  onChange={(e) => setParams({ ...params, fechaInicio: e.target.value })} required />
              </div>
              <div className="form-group">
                <label>Fecha Fin *</label>
                <input type="date" value={params.fechaFin}
                  onChange={(e) => setParams({ ...params, fechaFin: e.target.value })} required />
              </div>
              <div className="form-group">
                <label>ID Departamento (opcional)</label>
                <input type="number" placeholder="Todos" value={params.idDepartamento}
                  onChange={(e) => setParams({ ...params, idDepartamento: e.target.value })} />
              </div>
              <div className="form-group">
                <label>ID Empleado (opcional)</label>
                <input type="number" placeholder="Todos" value={params.idEmpleado}
                  onChange={(e) => setParams({ ...params, idEmpleado: e.target.value })} />
              </div>
            </div>
            <div className="form-actions">
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? "Calculando..." : "Calcular Nómina"}
              </button>
              <button type="button" className="btn btn-secondary"
                onClick={() => { setParams({ fechaInicio: "", fechaFin: "", idEmpleado: "", idDepartamento: "" }); setNominas([]); setMensaje(""); setError(""); }}>
                Limpiar
              </button>
            </div>
          </form>
        </div>

        {/* ── RESUMEN ── */}
        {nominas.length > 0 && (
          <>
            <div className="stats-row">
              <div className="stat-box">
                <div className="stat-value">{nominas.length}</div>
                <div className="stat-label">Empleados Procesados</div>
              </div>
              <div className="stat-box">
                <div className="stat-value">S/ {totales.bruto.toFixed(2)}</div>
                <div className="stat-label">Total Bruto</div>
              </div>
              <div className="stat-box">
                <div className="stat-value">S/ {totales.descuentos.toFixed(2)}</div>
                <div className="stat-label">Total Descuentos</div>
              </div>
              <div className="stat-box">
                <div className="stat-value">S/ {totales.neto.toFixed(2)}</div>
                <div className="stat-label">Total Neto</div>
              </div>
            </div>

            <div className="section-card">
              <h2>Detalle de Nómina — {params.fechaInicio} al {params.fechaFin}</h2>
              <div className="table-wrapper">
                <table>
                  <thead>
                    <tr>
                      <th>Empleado</th><th>Período</th><th>Sueldo Base</th>
                      <th>H. Extra</th><th>Bonificaciones</th><th>Descuentos</th>
                      <th>Bruto</th><th>Neto</th><th>Estado</th><th>Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    {nominas.map((n) => {
                      const bonif = (parseFloat(n.bonifFamiliar ?? 0) + parseFloat(n.bonifTurnoNocturno ?? 0) +
                        parseFloat(n.bonifGuardia ?? 0) + parseFloat(n.bonifRiesgo ?? 0) + parseFloat(n.bonifCargo ?? 0)).toFixed(2);
                      const desc = (parseFloat(n.descuentoTardanzas ?? 0) + parseFloat(n.descuentoLey ?? 0)).toFixed(2);
                      return (
                        <tr key={n.idNomina}>
                          <td>{n.nombreEmpleado}</td>
                          <td>{n.periodo}</td>
                          <td>S/ {parseFloat(n.sueldoBase ?? 0).toFixed(2)}</td>
                          <td>{n.totalHorasExtra ?? 0}h</td>
                          <td>S/ {bonif}</td>
                          <td>S/ {desc}</td>
                          <td>S/ {parseFloat(n.sueldoBruto ?? 0).toFixed(2)}</td>
                          <td><strong>S/ {parseFloat(n.sueldoNeto ?? 0).toFixed(2)}</strong></td>
                          <td><span className="badge badge-info">{n.estadoPago ?? "Calculada"}</span></td>
                          <td>
                            <button className="btn btn-secondary btn-sm" onClick={() => handleDescargar(n.idNomina)}>PDF</button>{" "}
                            <button className="btn btn-info btn-sm" onClick={() => handleEnviarComprobante(n.idNomina)}>Email</button>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            </div>
          </>
        )}
      </main>
    </div>
  );
}
