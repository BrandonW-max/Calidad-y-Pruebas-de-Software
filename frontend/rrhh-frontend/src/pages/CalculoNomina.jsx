import { useState } from "react";
import Sidebar from "../components/Sidebar";
import logo from "../assets/logo.png";
import "../styles/Dashboard.css";
import {
  calcularNomina,
  getNominasPorPeriodo,
  enviarComprobante,
} from "../services/api";
import { abrirComprobante } from "../services/comprobanteHtml";

const getBonif = (n) =>
  (Number(n.bonifFamiliar)      || 0) +
  (Number(n.bonifTurnoNocturno) || 0) +
  (Number(n.bonifGuardia)       || 0) +
  (Number(n.bonifRiesgo)        || 0) +
  (Number(n.bonifCargo)         || 0);

const getDesc = (n) =>
  (Number(n.descuentoTardanzas) || 0) +
  (Number(n.descuentoLey)       || 0);

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
  const [previewNomina, setPreviewNomina] = useState(null);

  const periodo = `${anio}-${mes}`;

  const handleCalcular = async () => {
    setCalculando(true);
    setError("");
    setMensaje("");
    try {
      const data = await calcularNomina(mes, anio);
      if (Array.isArray(data)) {
        setResultado(data);
        setHistorial(h => {
          const entry = { ...data, periodo, timestamp: new Date().toISOString() };
          return [entry, ...h.filter(x => x.periodo !== periodo)].slice(0, 10);
        });
        setMensaje("Nómina calculada correctamente (" + data.length + " empleados)");
      } else {
        throw new Error(data?.message || "Respuesta inválida");
      }
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
        const byEmployee = {};
        for (const n of data) {
          if (!byEmployee[n.idEmpleado] || n.idNomina > byEmployee[n.idEmpleado].idNomina) {
            byEmployee[n.idEmpleado] = n;
          }
        }
        const deduped = Object.values(byEmployee);
        setResultado(deduped);
        setMensaje(`Se cargaron ${deduped.length} registros del período ${periodo}`);
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

  const handleDescargar = (idNomina) => {
    const n = resultList.find(x => String(x.idNomina ?? x.id) === String(idNomina));
    if (n) {
      const logoUrl = window.location.origin + logo;
      abrirComprobante(n, logoUrl, "#2563eb", "Comprobante de Nómina", "Sistema de Recursos Humanos — Comprobante de Nómina");
    } else {
      setError("Calcule la nómina primero para ver el comprobante.");
    }
  };

  const fmtS = (v) => v != null ? `S/ ${Number(v).toLocaleString("es-PE", { minimumFractionDigits: 2 })}` : "—";

  const resultList = resultado
    ? (Array.isArray(resultado) ? resultado : [resultado])
    : [];

  const totalNeto  = resultList.reduce((sum, n) => sum + Number(n.sueldoNeto ?? 0), 0);
  const totalBruto = resultList.reduce((sum, n) => sum + Number(n.sueldoBruto ?? n.sueldoBase ?? 0), 0);

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

            <div className="section-card" style={{ padding: 0, overflow: "hidden" }}>
              <div style={{ padding: "16px 20px", borderBottom: "1px solid var(--border)", background: "linear-gradient(135deg, #f0fdf4 0%, #eff6ff 100%)", display: "flex", alignItems: "center", gap: 12 }}>
                <div style={{ width: 40, height: 40, borderRadius: 10, background: "#22c55e", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 20, flexShrink: 0 }}>
                  📅
                </div>
                <div>
                  <h2 style={{ margin: 0, fontSize: 16 }}>Configuración del Período</h2>
                  <p style={{ margin: 0, fontSize: 12, color: "var(--text-3)" }}>Seleccione el mes y año para calcular la nómina</p>
                </div>
              </div>

              <div style={{ padding: "16px 20px 0" }}>
                <div className="form-grid" style={{ gap: 12, marginBottom: 12 }}>
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

                <div style={{ background: "linear-gradient(90deg, #eff6ff 0%, #f0fdf4 100%)", border: "1px solid #bfdbfe", borderRadius: 10, padding: "10px 14px", marginBottom: 14, display: "flex", alignItems: "center", gap: 10 }}>
                  <span style={{ fontSize: 20 }}>📆</span>
                  <div>
                    <div style={{ fontSize: 11, color: "var(--text-3)", fontWeight: 600, textTransform: "uppercase", letterSpacing: "0.05em" }}>Período seleccionado</div>
                    <div style={{ fontSize: 15, fontWeight: 700, color: "var(--primary)" }}>{MESES.find(m => m.v === mes)?.l} {anio}</div>
                  </div>
                </div>
              </div>

              <div style={{ padding: "0 20px 20px", display: "flex", gap: 10 }}>
                <button className="btn btn-success" onClick={handleCalcular} disabled={calculando} style={{ flex: 1 }}>
                  {calculando ? "Calculando..." : "⚙️ Calcular Nómina"}
                </button>
                <button className="btn btn-secondary" onClick={handleCargarPeriodo} disabled={loading} style={{ flex: 1 }}>
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
                          <td style={{ color: "var(--success)" }}>{fmtS(getBonif(n))}</td>
                          <td style={{ color: "#ef4444" }}>{fmtS(getDesc(n))}</td>
                          <td style={{ fontWeight: 700, color: "#0f172a" }}>{fmtS(n.sueldoNeto)}</td>
                          <td>
                            <div style={{ display: "flex", gap: "4px" }}>
                              <button className="btn btn-secondary btn-sm"
                                title="Vista previa detallada"
                                onClick={() => setPreviewNomina(n)}>
                                👁
                              </button>
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

      {/* ── MODAL VISTA PREVIA NÓMINA ── */}
      {previewNomina && (() => {
        const n = previewNomina;
        const base  = Number(n.sueldoBase ?? n.sueldo ?? 0);
        const bonifs = [
          { label: "Bonif. Familiar",     val: n.bonifFamiliar },
          { label: "Bonif. Turno Nocturno", val: n.bonifTurnoNocturno },
          { label: "Bonif. Guardia",       val: n.bonifGuardia },
          { label: "Bonif. Riesgo",        val: n.bonifRiesgo },
          { label: "Bonif. Cargo",         val: n.bonifCargo },
        ];
        const descs = [
          { label: "Descuento Tardanzas", val: n.descuentoTardanzas },
          { label: "Descuento de Ley",    val: n.descuentoLey },
        ];
        const totalBonif = getBonif(n);
        const totalDesc  = getDesc(n);
        const neto  = Number(n.sueldoNeto ?? 0);
        const mesLabel = MESES.find(m => m.v === (n.mes ?? mes))?.l ?? n.mes ?? mes;
        return (
          <div className="modal-overlay" onClick={e => e.target === e.currentTarget && setPreviewNomina(null)}>
            <div className="modal-box" style={{ maxWidth: 500 }}>
              <div className="modal-header">
                <div>
                  <h3>📊 Vista Previa — Nómina #{n.idNomina}</h3>
                  <p style={{ margin: 0, color: "var(--text-2)", fontSize: 13 }}>
                    {n.empleadoNombre ?? `Empleado #${n.idEmpleado}`} — {mesLabel} {n.anio ?? anio}
                  </p>
                </div>
                <button className="modal-close" onClick={() => setPreviewNomina(null)}>×</button>
              </div>

              <div className="modal-body" style={{ display: "flex", flexDirection: "column", gap: 14 }}>

                {/* Sueldo base */}
                <div style={{ display: "flex", justifyContent: "space-between", padding: "10px 0", borderBottom: "1px solid var(--border)" }}>
                  <span style={{ fontWeight: 600 }}>Sueldo Base</span>
                  <strong>{fmtS(base)}</strong>
                </div>

                {/* Bonificaciones */}
                <div>
                  <div style={{ fontWeight: 700, color: "#16a34a", marginBottom: 8, fontSize: 13 }}>
                    + Bonificaciones
                  </div>
                  {bonifs.filter(r => Number(r.val)).map(r => (
                    <div key={r.label} style={{ display: "flex", justifyContent: "space-between", fontSize: 13, marginBottom: 4, paddingLeft: 12 }}>
                      <span style={{ color: "var(--text-2)" }}>{r.label}</span>
                      <span style={{ color: "#16a34a" }}>{fmtS(r.val)}</span>
                    </div>
                  ))}
                  <div style={{ display: "flex", justifyContent: "space-between", fontWeight: 700, borderTop: "1px dashed var(--border)", paddingTop: 6, marginTop: 4 }}>
                    <span>Total Bonificaciones</span>
                    <span style={{ color: "#16a34a" }}>{fmtS(totalBonif)}</span>
                  </div>
                </div>

                {/* Descuentos */}
                <div>
                  <div style={{ fontWeight: 700, color: "#ef4444", marginBottom: 8, fontSize: 13 }}>
                    — Descuentos
                  </div>
                  {descs.filter(r => Number(r.val)).map(r => (
                    <div key={r.label} style={{ display: "flex", justifyContent: "space-between", fontSize: 13, marginBottom: 4, paddingLeft: 12 }}>
                      <span style={{ color: "var(--text-2)" }}>{r.label}</span>
                      <span style={{ color: "#ef4444" }}>{fmtS(r.val)}</span>
                    </div>
                  ))}
                  <div style={{ display: "flex", justifyContent: "space-between", fontWeight: 700, borderTop: "1px dashed var(--border)", paddingTop: 6, marginTop: 4 }}>
                    <span>Total Descuentos</span>
                    <span style={{ color: "#ef4444" }}>{fmtS(totalDesc)}</span>
                  </div>
                </div>

                {/* Neto */}
                <div style={{ background: "#f0fdf4", borderRadius: 10, padding: "12px 16px", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                  <span style={{ fontWeight: 700, fontSize: 15 }}>Sueldo Neto a Pagar</span>
                  <span style={{ fontWeight: 800, fontSize: 18, color: "#15803d" }}>{fmtS(neto)}</span>
                </div>

              </div>

              <div className="modal-footer">
                <button className="btn btn-secondary" onClick={() => setPreviewNomina(null)}>Cerrar</button>
              </div>
            </div>
          </div>
        );
      })()}
    </div>
  );
}
