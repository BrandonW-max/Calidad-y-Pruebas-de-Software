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
  const [modalDetalle, setModalDetalle] = useState(null);
  const [modalEmail, setModalEmail]     = useState(null);

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
    } catch (err) {
      setError(err?.message || "Error al calcular la nómina. Verifique los registros de asistencia.");
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
                            <button className="btn btn-purple btn-sm" onClick={() => setModalDetalle(n)}>
                              🔍 Ver
                            </button>
                            <button className="btn btn-secondary btn-sm" onClick={() => handleDescargar(n.idNomina)}>
                              📄 PDF
                            </button>
                            <button className="btn btn-info btn-sm" onClick={() => setModalEmail(n.idNomina)}>
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

      {/* ── MODAL DETALLE NÓMINA ── */}
      {modalDetalle && (
        <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && setModalDetalle(null)}>
          <div className="modal-box modal-box-wide">
            <div className="modal-header">
              <div>
                <h3>📄 Detalle de Nómina — {modalDetalle.nombreEmpleado}</h3>
                <p>Período: {modalDetalle.periodo} · Estado: {modalDetalle.estadoPago ?? "Calculada"}</p>
              </div>
              <button className="modal-close" onClick={() => setModalDetalle(null)}>×</button>
            </div>
            <div className="modal-body">
              <div className="stats-row" style={{ marginBottom: "20px" }}>
                <div className="stat-box accent-blue">
                  <div className="stat-info">
                    <div className="stat-value">S/ {parseFloat(modalDetalle.sueldoBase ?? 0).toFixed(2)}</div>
                    <div className="stat-label">Sueldo Base</div>
                  </div>
                </div>
                <div className="stat-box accent-orange">
                  <div className="stat-info">
                    <div className="stat-value">{modalDetalle.totalHorasExtra ?? 0}h</div>
                    <div className="stat-label">Horas Extra</div>
                  </div>
                </div>
                <div className="stat-box accent-green">
                  <div className="stat-info">
                    <div className="stat-value">S/ {parseFloat(modalDetalle.sueldoBruto ?? 0).toFixed(2)}</div>
                    <div className="stat-label">Sueldo Bruto</div>
                  </div>
                </div>
                <div className="stat-box accent-teal">
                  <div className="stat-info">
                    <div className="stat-value">S/ {parseFloat(modalDetalle.sueldoNeto ?? 0).toFixed(2)}</div>
                    <div className="stat-label">Sueldo Neto</div>
                  </div>
                </div>
              </div>

              <p className="modal-section-title">Desglose de Bonificaciones</p>
              <div className="table-wrapper" style={{ marginBottom: "16px" }}>
                <table>
                  <thead><tr><th>Tipo de Bonificación</th><th>Monto (S/)</th></tr></thead>
                  <tbody>
                    <tr><td>Bonificación Familiar (por hijos)</td><td>S/ {parseFloat(modalDetalle.bonifFamiliar ?? 0).toFixed(2)}</td></tr>
                    <tr><td>Turno Nocturno</td><td>S/ {parseFloat(modalDetalle.bonifTurnoNocturno ?? 0).toFixed(2)}</td></tr>
                    <tr><td>Guardia Hospitalaria</td><td>S/ {parseFloat(modalDetalle.bonifGuardia ?? 0).toFixed(2)}</td></tr>
                    <tr><td>Riesgo Hospitalario</td><td>S/ {parseFloat(modalDetalle.bonifRiesgo ?? 0).toFixed(2)}</td></tr>
                    <tr><td>Cargo / Productividad</td><td>S/ {parseFloat(modalDetalle.bonifCargo ?? 0).toFixed(2)}</td></tr>
                    <tr style={{ background: "#f0fdf4" }}>
                      <td><strong>Total Bonificaciones</strong></td>
                      <td><strong>S/ {(
                        parseFloat(modalDetalle.bonifFamiliar ?? 0) +
                        parseFloat(modalDetalle.bonifTurnoNocturno ?? 0) +
                        parseFloat(modalDetalle.bonifGuardia ?? 0) +
                        parseFloat(modalDetalle.bonifRiesgo ?? 0) +
                        parseFloat(modalDetalle.bonifCargo ?? 0)
                      ).toFixed(2)}</strong></td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <p className="modal-section-title">Descuentos Aplicados</p>
              <div className="table-wrapper">
                <table>
                  <thead><tr><th>Tipo de Descuento</th><th>Monto (S/)</th></tr></thead>
                  <tbody>
                    <tr><td>Descuento por Tardanzas</td><td style={{ color: "#ef4444" }}>- S/ {parseFloat(modalDetalle.descuentoTardanzas ?? 0).toFixed(2)}</td></tr>
                    <tr><td>Descuentos de Ley</td><td style={{ color: "#ef4444" }}>- S/ {parseFloat(modalDetalle.descuentoLey ?? 0).toFixed(2)}</td></tr>
                    <tr style={{ background: "#fff1f2" }}>
                      <td><strong>Total Descuentos</strong></td>
                      <td><strong style={{ color: "#ef4444" }}>- S/ {(
                        parseFloat(modalDetalle.descuentoTardanzas ?? 0) +
                        parseFloat(modalDetalle.descuentoLey ?? 0)
                      ).toFixed(2)}</strong></td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModalDetalle(null)}>Cerrar</button>
              <button className="btn btn-info" onClick={() => { setModalEmail(modalDetalle.idNomina); setModalDetalle(null); }}>✉️ Enviar por Email</button>
              <button className="btn btn-danger" onClick={() => handleDescargar(modalDetalle.idNomina)}>📄 Descargar PDF</button>
            </div>
          </div>
        </div>
      )}

      {/* ── MODAL CONFIRMACIÓN EMAIL ── */}
      {modalEmail && (
        <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && setModalEmail(null)}>
          <div className="modal-box">
            <div className="modal-header">
              <div>
                <h3>✉️ Enviar Comprobante por Correo</h3>
                <p>Nómina #{modalEmail}</p>
              </div>
              <button className="modal-close" onClick={() => setModalEmail(null)}>×</button>
            </div>
            <div className="modal-body">
              <p style={{ fontSize: "14px", color: "#475569" }}>
                Se enviará el comprobante de pago al correo electrónico registrado del empleado. ¿Desea continuar?
              </p>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModalEmail(null)}>Cancelar</button>
              <button className="btn btn-info" onClick={async () => { await handleEnviarComprobante(modalEmail); setModalEmail(null); }}>
                ✉️ Confirmar y Enviar
              </button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
}
