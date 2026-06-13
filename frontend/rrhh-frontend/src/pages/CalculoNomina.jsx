import { useState } from "react";
import Sidebar from "../components/Sidebar";
import "../styles/Dashboard.css";
import {
  calcularNomina,
  getNominasPorPeriodo,
  enviarComprobante,
  descargarComprobante,
} from "../services/api";

const MESES = [
  { v: "01", l: "Enero" }, { v: "02", l: "Febrero" }, { v: "03", l: "Marzo" },
  { v: "04", l: "Abril" }, { v: "05", l: "Mayo" },    { v: "06", l: "Junio" },
  { v: "07", l: "Julio" }, { v: "08", l: "Agosto" },  { v: "09", l: "Septiembre" },
  { v: "10", l: "Octubre" }, { v: "11", l: "Noviembre" }, { v: "12", l: "Diciembre" },
];

const anioActual = new Date().getFullYear();
const ANIOS = [anioActual - 1, anioActual, anioActual + 1];

export default function CalculoNomina() {
  const [mes,    setMes]    = useState(String(new Date().getMonth() + 1).padStart(2, "0"));
  const [anio,   setAnio]   = useState(String(anioActual));
  const [loading, setLoading] = useState(false);
  const [calculando, setCalculando] = useState(false);
  const [resultado, setResultado] = useState(null);
  const [historial, setHistorial] = useState([]);
  const [mensaje,   setMensaje]   = useState("");
  const [error,     setError]     = useState("");
  const [enviando,  setEnviando]  = useState({});

  const periodo = `${anio}-${mes}`;

  const handleCalcular = async () => {
    setCalculando(true);
    setError("");
    setMensaje("");
    try {
      const data = await calcularNomina(mes, anio);
      setResultado(data);
      setHistorial(h => {
        const entry = { ...data, periodo, timestamp: new Date().toISOString() };
        return [entry, ...h.filter(x => x.periodo !== periodo)].slice(0, 10);
      });
      setMensaje("Nómina calculada correctamente");
    } catch {
      setError("Error al calcular la nómina. Verifique el período.");
    } finally {
      setCalculando(false);
    }
  };

  const handleCargarPeriodo = async () => {
    setLoading(true);
    setError("");
    setMensaje("");
    try {
      const data = await getNominasPorPeriodo(mes, anio);
      if (Array.isArray(data) && data.length > 0) {
        setResultado(data);
        setMensaje(`Se cargaron ${data.length} registros del período ${periodo}`);
      } else {
        setError(`No hay nóminas registradas para ${periodo}. Calcule primero.`);
      }
    } catch {
      setError("Error al cargar el período");
    } finally {
      setLoading(false);
    }
  };

  const handleEnviarComprobante = async (idNomina) => {
    setEnviando(v => ({ ...v, [idNomina]: true }));
    setMensaje("");
    setError("");
    try {
      await enviarComprobante(idNomina);
      setMensaje(`Comprobante enviado correctamente (Nómina #${idNomina})`);
    } catch {
      setError("Error al enviar el comprobante");
    } finally {
      setEnviando(v => ({ ...v, [idNomina]: false }));
    }
  };

  const handleDescargar = async (idNomina) => {
    setMensaje("");
    setError("");
    try {
      const blob = await descargarComprobante(idNomina);
      const url  = URL.createObjectURL(blob);
      const a    = document.createElement("a");
      a.href     = url;
      a.download = `comprobante_nomina_${idNomina}.pdf`;
      a.click();
      URL.revokeObjectURL(url);
    } catch {
      setError("Error al descargar el comprobante");
    }
  };

  const fmtS = (v) => v != null ? `S/ ${Number(v).toLocaleString("es-PE", { minimumFractionDigits: 2 })}` : "—";

  const resultList = resultado
    ? (Array.isArray(resultado) ? resultado : [resultado])
    : [];

  const totalNeto  = resultList.reduce((sum, n) => sum + Number(n.sueldoNeto  ?? n.totalNeto  ?? 0), 0);
  const totalBruto = resultList.reduce((sum, n) => sum + Number(n.sueldoBase  ?? n.sueldo     ?? 0), 0);

  return (
    <div className="dashboard">
      <Sidebar rol="RRHH" />

      <main className="main-content">
        <header className="main-header header-rrhh">
          <div className="header-left">
            <span className="page-breadcrumb">RRHH / Cálculo de Nómina</span>
            <h1>Cálculo de Nómina</h1>
            <p style={{ margin: 0, fontSize: "13px", color: "var(--text-3)" }}>
              Gestiona los pagos mensuales y descuentos de los empleados.
            </p>
          </div>
        </header>

        {mensaje && <div className="alert alert-success">✅ {mensaje}</div>}
        {error   && <div className="alert alert-warning">⚠️ {error}</div>}

        <div className="nomina-layout">

          {/* ── COLUMNA IZQUIERDA ── */}
          <div className="nomina-left">

            <div className="section-card">
              <h2>Configuración del Período</h2>
              <div className="form-grid">
                <div className="form-group">
                  <label>Mes</label>
                  <select value={mes} onChange={e => setMes(e.target.value)}>
                    {MESES.map(m => <option key={m.v} value={m.v}>{m.l}</option>)}
                  </select>
                </div>
                <div className="form-group">
                  <label>Año</label>
                  <select value={anio} onChange={e => setAnio(e.target.value)}>
                    {ANIOS.map(a => <option key={a} value={a}>{a}</option>)}
                  </select>
                </div>
              </div>
              <div className="nomina-periodo-badge">
                📅 Período seleccionado: <strong>{MESES.find(m => m.v === mes)?.l} {anio}</strong>
              </div>
              <div className="form-actions">
                <button className="btn btn-success" onClick={handleCalcular} disabled={calculando}>
                  {calculando ? "Calculando..." : "⚙️ Calcular Nómina"}
                </button>
                <button className="btn btn-secondary" onClick={handleCargarPeriodo} disabled={loading}>
                  {loading ? "Cargando..." : "📋 Cargar Período"}
                </button>
              </div>
            </div>

            {resultList.length > 0 && (
              <div className="section-card" style={{ padding: 0, overflow: "hidden" }}>
                <div style={{ padding: "20px 20px 10px" }}>
                  <h2 style={{ margin: 0 }}>
                    Resultados — {MESES.find(m => m.v === mes)?.l} {anio}
                    <span className="badge badge-success" style={{ marginLeft: "10px", fontSize: "12px" }}>
                      {resultList.length} empleado(s)
                    </span>
                  </h2>
                </div>
                <div className="table-wrapper" style={{ marginBottom: 0 }}>
                  <table>
                    <thead>
                      <tr>
                        <th>ID</th>
                        <th>Empleado</th>
                        <th>Sueldo Base</th>
                        <th>Bonificaciones</th>
                        <th>Descuentos</th>
                        <th>Sueldo Neto</th>
                        <th>Acciones</th>
                      </tr>
                    </thead>
                    <tbody>
                      {resultList.map((n, i) => (
                        <tr key={n.idNomina ?? i}>
                          <td style={{ color: "var(--text-3)", fontSize: "13px" }}>{n.idNomina ?? i + 1}</td>
                          <td>
                            <strong>{n.empleadoNombre ?? n.nombres ?? (n.idEmpleado ? `Emp. #${n.idEmpleado}` : "—")}</strong>
                          </td>
                          <td>{fmtS(n.sueldoBase ?? n.sueldo)}</td>
                          <td style={{ color: "var(--success)" }}>{fmtS(n.bonificaciones ?? n.totalBonificaciones)}</td>
                          <td style={{ color: "#ef4444" }}>{fmtS(n.descuentos ?? n.totalDescuentos)}</td>
                          <td style={{ fontWeight: 700, color: "#0f172a" }}>{fmtS(n.sueldoNeto ?? n.totalNeto)}</td>
                          <td>
                            <div style={{ display: "flex", gap: "4px" }}>
                              <button className="btn btn-secondary btn-sm"
                                onClick={() => handleDescargar(n.idNomina)}>
                                ⬇ PDF
                              </button>
                              <button className="btn btn-primary btn-sm"
                                onClick={() => handleEnviarComprobante(n.idNomina)}
                                disabled={enviando[n.idNomina]}>
                                {enviando[n.idNomina] ? "..." : "📧 Email"}
                              </button>
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                    <tfoot>
                      <tr style={{ background: "#f8fafc", fontWeight: 600 }}>
                        <td colSpan={2} style={{ padding: "10px 12px", fontSize: "13px" }}>
                          TOTALES ({resultList.length} empleados)
                        </td>
                        <td style={{ padding: "10px 12px" }}>{fmtS(totalBruto)}</td>
                        <td></td>
                        <td></td>
                        <td style={{ padding: "10px 12px", color: "var(--primary)" }}>{fmtS(totalNeto)}</td>
                        <td></td>
                      </tr>
                    </tfoot>
                  </table>
                </div>
              </div>
            )}

            {resultList.length === 0 && (
              <div className="section-card">
                <div className="empty-state">
                  <div className="empty-state-icon">📊</div>
                  <p>Seleccione un período y calcule o cargue la nómina para ver los resultados.</p>
                </div>
              </div>
            )}
          </div>

          {/* ── COLUMNA DERECHA ── */}
          <div className="nomina-right">
            <div className="section-card">
              <h2>Estado del Período</h2>
              <div className="nomina-status-item">
                <span>Período actual:</span>
                <strong>{MESES.find(m => m.v === mes)?.l} {anio}</strong>
              </div>
              <div className="nomina-status-item">
                <span>Total empleados:</span>
                <strong>{resultList.length > 0 ? resultList.length : "—"}</strong>
              </div>
              <div className="nomina-status-item">
                <span>Total neto a pagar:</span>
                <strong style={{ color: "var(--primary)" }}>{resultList.length > 0 ? fmtS(totalNeto) : "—"}</strong>
              </div>
              <div className="nomina-status-item">
                <span>Estado:</span>
                <span className={`badge ${resultado ? "badge-success" : "badge-gray"}`}>
                  {resultado ? "Calculado" : "Pendiente"}
                </span>
              </div>
            </div>

            {historial.length > 0 && (
              <div className="section-card">
                <h2>Historial Reciente</h2>
                {historial.map((h, i) => (
                  <div key={i} className="nomina-hist-item">
                    <span className="nomina-hist-periodo">{h.periodo}</span>
                    <span className="badge badge-success" style={{ fontSize: "11px" }}>Calculado</span>
                  </div>
                ))}
              </div>
            )}

            <div className="section-card">
              <h2>Guía Rápida</h2>
              <ol style={{ margin: 0, paddingLeft: "18px", fontSize: "13px", color: "var(--text-2)", lineHeight: "1.9" }}>
                <li>Seleccione mes y año del período.</li>
                <li>Presione <strong>Calcular Nómina</strong> para procesar.</li>
                <li>Revise los resultados en la tabla.</li>
                <li>Descargue o envíe comprobantes individualmente.</li>
              </ol>
            </div>
          </div>

        </div>
      </main>
    </div>
  );
}
