import { useState } from "react";
import Sidebar from "../components/Sidebar";
import "../styles/Dashboard.css";
import { calcularNomina, enviarComprobante, descargarComprobante } from "../services/api";

export default function CalculoNomina() {
  const [params, setParams] = useState({ fechaInicio: "", fechaFin: "", idEmpleado: "", idDepartamento: "" });
  const [nominas, setNominas]   = useState([]);
  const [loading, setLoading]   = useState(false);
  const [mensaje, setMensaje]   = useState("");
  const [error, setError]       = useState("");

  const totales = nominas.reduce(
    (acc, n) => ({
      bruto:      acc.bruto      + parseFloat(n.sueldoBruto ?? 0),
      neto:       acc.neto       + parseFloat(n.sueldoNeto ?? 0),
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
        fechaInicio:   params.fechaInicio,
        fechaFin:      params.fechaFin,
        idEmpleado:    params.idEmpleado    ? parseInt(params.idEmpleado)    : null,
        idDepartamento: params.idDepartamento ? parseInt(params.idDepartamento) : null,
      };
      const data = await calcularNomina(dto);
      setNominas(Array.isArray(data) ? data : [data]);
      setMensaje("Nómina calculada correctamente. Revise el detalle a continuación.");
    } catch {
      setError("Error al calcular la nómina. Verifique que los registros de asistencia estén completos y sin inconsistencias.");
    } finally {
      setLoading(false);
    }
  };

  const handleEnviarComprobante = async (id) => {
    try {
      await enviarComprobante(id);
      setMensaje("Comprobante enviado al correo del empleado correctamente");
    } catch {
      setMensaje("Error al enviar el comprobante");
    }
  };

  const handleDescargar = async (id) => {
    try {
      const res = await descargarComprobante(id);
      const blob = await res.blob();
      const url  = URL.createObjectURL(blob);
      const a    = document.createElement("a");
      a.href     = url;
      a.download = `comprobante_${id}.pdf`;
      a.click();
      URL.revokeObjectURL(url);
    } catch {
      setMensaje("Error al descargar el comprobante");
    }
  };

  const limpiar = () => {
    setParams({ fechaInicio: "", fechaFin: "", idEmpleado: "", idDepartamento: "" });
    setNominas([]);
    setMensaje("");
    setError("");
  };

  return (
    <div className="dashboard">
      <Sidebar rol="RRHH" />

      <main className="main-content">
        <header className="main-header header-rrhh">
          <div className="header-left">
            <span className="page-breadcrumb">RRHH / Cálculo de Nómina</span>
            <h1>Cálculo de Nómina</h1>
          </div>
          <span className="role-pill role-pill-rrhh">ECU-04</span>
        </header>

        {mensaje && <div className="alert alert-success">✅ {mensaje}</div>}
        {error   && <div className="alert alert-warning">⚠️ {error}</div>}

        {/* ── PARÁMETROS ── */}
        <div className="section-card">
          <h2>⚙️ Parámetros de Cálculo</h2>
          <div className="alert alert-info">
            Seleccione el período y, opcionalmente, filtre por departamento o empleado específico.
            El sistema validará la integridad de los registros de asistencia antes de calcular.
          </div>
          <form onSubmit={handleCalcular}>
            <div className="form-grid">
              <div className="form-group">
                <label>Fecha Inicio *</label>
                <input
                  type="date"
                  value={params.fechaInicio}
                  onChange={(e) => setParams({ ...params, fechaInicio: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>Fecha Fin *</label>
                <input
                  type="date"
                  value={params.fechaFin}
                  onChange={(e) => setParams({ ...params, fechaFin: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>ID Departamento (opcional)</label>
                <input
                  type="number"
                  placeholder="Todos los departamentos"
                  value={params.idDepartamento}
                  onChange={(e) => setParams({ ...params, idDepartamento: e.target.value })}
                />
              </div>
              <div className="form-group">
                <label>ID Empleado (opcional)</label>
                <input
                  type="number"
                  placeholder="Todos los empleados activos"
                  value={params.idEmpleado}
                  onChange={(e) => setParams({ ...params, idEmpleado: e.target.value })}
                />
              </div>
            </div>
            <div className="form-actions">
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? "⏳ Calculando..." : "💰 Calcular Nómina"}
              </button>
              <button type="button" className="btn btn-secondary" onClick={limpiar}>
                Limpiar
              </button>
            </div>
          </form>
        </div>

        {/* ── RESUMEN ── */}
        {nominas.length > 0 && (
          <>
            <div className="stats-row">
              <div className="stat-box accent-blue">
                <div className="stat-icon">👥</div>
                <div className="stat-info">
                  <div className="stat-value">{nominas.length}</div>
                  <div className="stat-label">Empleados Procesados</div>
                </div>
              </div>
              <div className="stat-box accent-green">
                <div className="stat-icon">💵</div>
                <div className="stat-info">
                  <div className="stat-value">S/ {totales.bruto.toFixed(2)}</div>
                  <div className="stat-label">Total Bruto</div>
                </div>
              </div>
              <div className="stat-box accent-red">
                <div className="stat-icon">➖</div>
                <div className="stat-info">
                  <div className="stat-value">S/ {totales.descuentos.toFixed(2)}</div>
                  <div className="stat-label">Total Descuentos</div>
                </div>
              </div>
              <div className="stat-box accent-teal">
                <div className="stat-icon">💰</div>
                <div className="stat-info">
                  <div className="stat-value">S/ {totales.neto.toFixed(2)}</div>
                  <div className="stat-label">Total Neto a Pagar</div>
                </div>
              </div>
            </div>

            <div className="section-card">
              <h2>📄 Detalle de Nómina — {params.fechaInicio} al {params.fechaFin}</h2>
              <div className="table-wrapper">
                <table>
                  <thead>
                    <tr>
                      <th>Empleado</th>
                      <th>Período</th>
                      <th>Sueldo Base</th>
                      <th>H. Extra</th>
                      <th>Bonificaciones</th>
                      <th>Descuentos</th>
                      <th>Bruto</th>
                      <th>Neto</th>
                      <th>Estado</th>
                      <th>Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    {nominas.map((n) => {
                      const bonif = (
                        parseFloat(n.bonifFamiliar      ?? 0) +
                        parseFloat(n.bonifTurnoNocturno ?? 0) +
                        parseFloat(n.bonifGuardia       ?? 0) +
                        parseFloat(n.bonifRiesgo        ?? 0) +
                        parseFloat(n.bonifCargo         ?? 0)
                      ).toFixed(2);
                      const desc = (
                        parseFloat(n.descuentoTardanzas ?? 0) +
                        parseFloat(n.descuentoLey       ?? 0)
                      ).toFixed(2);
                      return (
                        <tr key={n.idNomina}>
                          <td><strong>{n.nombreEmpleado}</strong></td>
                          <td>{n.periodo}</td>
                          <td>S/ {parseFloat(n.sueldoBase ?? 0).toFixed(2)}</td>
                          <td>{n.totalHorasExtra ?? 0}h</td>
                          <td>S/ {bonif}</td>
                          <td style={{ color: "#ef4444" }}>- S/ {desc}</td>
                          <td>S/ {parseFloat(n.sueldoBruto ?? 0).toFixed(2)}</td>
                          <td><strong style={{ color: "#10b981" }}>S/ {parseFloat(n.sueldoNeto ?? 0).toFixed(2)}</strong></td>
                          <td>
                            <span className="badge badge-info">{n.estadoPago ?? "Calculada"}</span>
                          </td>
                          <td style={{ display: "flex", gap: "6px" }}>
                            <button className="btn btn-secondary btn-sm" onClick={() => handleDescargar(n.idNomina)}>
                              📄 PDF
                            </button>
                            <button className="btn btn-info btn-sm" onClick={() => handleEnviarComprobante(n.idNomina)}>
                              ✉️ Email
                            </button>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            </div>

            <div className="section-card">
              <h2>ℹ️ Desglose de Bonificaciones</h2>
              <div className="alert alert-info">
                Las bonificaciones consideradas en el cálculo son: familiar (por hijos), turno nocturno, guardias, riesgo hospitalario y cargo/productividad.
                Los descuentos corresponden a tardanzas acumuladas y descuentos de ley aplicables.
              </div>
            </div>
          </>
        )}
      </main>
    </div>
  );
}
