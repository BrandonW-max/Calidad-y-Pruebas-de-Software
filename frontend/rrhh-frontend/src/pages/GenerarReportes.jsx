import { useState } from "react";
import Sidebar from "../components/Sidebar";
import "../styles/Dashboard.css";
import { generarReporte } from "../services/api";

const TIPOS_REPORTE = [
  { id: "empleados",   label: "Reporte de Empleados",   icon: "👥", desc: "Listado general o filtrado de empleados con sus datos personales y laborales.", color: "#3b82f6" },
  { id: "nomina",      label: "Reporte de Nómina",      icon: "💲", desc: "Detalle de pagos, bonificaciones y descuentos por período.",                    color: "#10b981" },
  { id: "asistencia",  label: "Reporte de Asistencia",  icon: "🕐", desc: "Registro de entradas, salidas, tardanzas e inasistencias.",                     color: "#f59e0b" },
  { id: "solicitudes", label: "Reporte de Solicitudes", icon: "📋", desc: "Permisos y licencias solicitadas, aprobadas o rechazadas.",                      color: "#8b5cf6" },
];

const FORMATOS = ["PDF", "XLSX", "CSV"];

const MESES = [
  { v: "01", l: "Enero" }, { v: "02", l: "Febrero" }, { v: "03", l: "Marzo" },
  { v: "04", l: "Abril" }, { v: "05", l: "Mayo" },    { v: "06", l: "Junio" },
  { v: "07", l: "Julio" }, { v: "08", l: "Agosto" },  { v: "09", l: "Septiembre" },
  { v: "10", l: "Octubre" }, { v: "11", l: "Noviembre" }, { v: "12", l: "Diciembre" },
];

const anioActual = new Date().getFullYear();
const ANIOS = [anioActual - 2, anioActual - 1, anioActual];

export default function GenerarReportes() {
  const [tipoSeleccionado, setTipoSeleccionado] = useState("empleados");
  const [formato,   setFormato]   = useState("PDF");
  const [mes,       setMes]       = useState(String(new Date().getMonth() + 1).padStart(2, "0"));
  const [anio,      setAnio]      = useState(String(anioActual));
  const [fechaIni,  setFechaIni]  = useState("");
  const [fechaFin,  setFechaFin]  = useState("");
  const [estado,    setEstado]    = useState("");
  const [generando, setGenerando] = useState(false);
  const [mensaje,   setMensaje]   = useState("");
  const [error,     setError]     = useState("");
  const [resultado, setResultado] = useState(null);
  const [historial, setHistorial] = useState([]);
  const [modalPreview, setModalPreview] = useState(false);

  const handleGenerar = async (e) => {
    e.preventDefault();
    setGenerando(true); setMensaje(""); setError(""); setResultado(null);

    const params = {
      tipo:    tipoSeleccionado,
      formato,
      mes:        (tipoSeleccionado === "nomina" || tipoSeleccionado === "asistencia") ? mes       : undefined,
      anio:       (tipoSeleccionado === "nomina" || tipoSeleccionado === "asistencia") ? anio      : undefined,
      fechaInicio: fechaIni || undefined,
      fechaFin:    fechaFin || undefined,
      estado:     (tipoSeleccionado === "solicitudes" || tipoSeleccionado === "empleados") ? estado || undefined : undefined,
    };

    try {
      const data = await generarReporte(params);
      if (data instanceof Blob) {
        const url = URL.createObjectURL(data);
        const a   = document.createElement("a");
        a.href     = url;
        a.download = `reporte_${tipoSeleccionado}_${new Date().toISOString().split("T")[0]}.${formato.toLowerCase()}`;
        a.click();
        URL.revokeObjectURL(url);
        setMensaje("Reporte generado y descargado correctamente.");
        setHistorial(h => [{ tipo: tipoSeleccionado, formato, fecha: new Date().toLocaleString("es-PE"), estado: "Descargado" }, ...h].slice(0, 10));
      } else {
        const lista = Array.isArray(data) ? data : (data?.data ?? data?.content ?? []);
        setResultado(lista);
        setMensaje(`Reporte generado correctamente (${lista.length} registros).`);
        setHistorial(h => [{ tipo: tipoSeleccionado, formato, fecha: new Date().toLocaleString("es-PE"), estado: "Generado" }, ...h].slice(0, 10));
      }
    } catch {
      setError("Error al generar el reporte. Verifique los filtros e intente de nuevo.");
    } finally {
      setGenerando(false);
    }
  };

  const tipoActual = TIPOS_REPORTE.find(t => t.id === tipoSeleccionado);

  const renderTabla = () => {
    if (!resultado || resultado.length === 0) return (
      <div className="empty-state"><div className="empty-state-icon">📊</div><p>No hay datos para mostrar</p></div>
    );
    const keys = Object.keys(resultado[0]);
    return (
      <table>
        <thead><tr>{keys.map(k => <th key={k}>{k}</th>)}</tr></thead>
        <tbody>
          {resultado.slice(0, 50).map((row, i) => (
            <tr key={i}>{keys.map(k => <td key={k} style={{ fontSize: "12px" }}>{row[k] != null ? String(row[k]) : "—"}</td>)}</tr>
          ))}
        </tbody>
      </table>
    );
  };

  return (
    <div className="dashboard">
      <Sidebar rol="RRHH" />

      <main className="main-content">
        <header className="main-header header-rrhh">
          <div className="header-left">
            <span className="page-breadcrumb">RRHH / Generación de Reportes</span>
            <h1>Generación de Reportes</h1>
            <p style={{ margin: 0, fontSize: "13px", color: "var(--text-3)" }}>
              Genera reportes de empleados, nómina, asistencia y solicitudes.
            </p>
          </div>
        </header>

        {mensaje && <div className="alert alert-success">✅ {mensaje}</div>}
        {error   && <div className="alert alert-warning">⚠️ {error}</div>}

        <div className="reportes-layout">

          {/* ── COLUMNA IZQUIERDA ── */}
          <div className="reportes-left">

            <div className="section-card">
              <h2>Tipo de Reporte</h2>
              <div className="tipo-reporte-grid">
                {TIPOS_REPORTE.map(t => (
                  <div key={t.id}
                    className={`tipo-reporte-card ${tipoSeleccionado === t.id ? "active" : ""}`}
                    style={{ borderLeft: `4px solid ${t.color}` }}
                    onClick={() => setTipoSeleccionado(t.id)}>
                    <div className="tipo-reporte-icon" style={{ color: t.color }}>{t.icon}</div>
                    <div>
                      <div className="tipo-reporte-label">{t.label}</div>
                      <div className="tipo-reporte-desc">{t.desc}</div>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="section-card">
              <h2>{tipoActual?.icon} {tipoActual?.label} — Configuración</h2>
              <form onSubmit={handleGenerar}>
                <div className="form-grid">
                  <div className="form-group">
                    <label>Formato de Exportación</label>
                    <select value={formato} onChange={e => setFormato(e.target.value)}>
                      {FORMATOS.map(f => <option key={f} value={f}>{f}</option>)}
                    </select>
                  </div>

                  {(tipoSeleccionado === "nomina" || tipoSeleccionado === "asistencia") && (<>
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
                  </>)}

                  {(tipoSeleccionado === "empleados" || tipoSeleccionado === "solicitudes") && (<>
                    <div className="form-group">
                      <label>Fecha Desde</label>
                      <input type="date" value={fechaIni} onChange={e => setFechaIni(e.target.value)} />
                    </div>
                    <div className="form-group">
                      <label>Fecha Hasta</label>
                      <input type="date" value={fechaFin} onChange={e => setFechaFin(e.target.value)} />
                    </div>
                  </>)}

                  {(tipoSeleccionado === "solicitudes" || tipoSeleccionado === "empleados") && (
                    <div className="form-group">
                      <label>Estado</label>
                      <select value={estado} onChange={e => setEstado(e.target.value)}>
                        <option value="">Todos</option>
                        {tipoSeleccionado === "solicitudes"
                          ? <><option value="PENDIENTE">Pendiente</option><option value="APROBADO">Aprobado</option><option value="RECHAZADO">Rechazado</option></>
                          : <><option value="ACTIVO">Activo</option><option value="INACTIVO">Inactivo</option></>
                        }
                      </select>
                    </div>
                  )}
                </div>
                <div className="form-actions" style={{ marginTop: 16 }}>
                  <button type="submit" className="btn btn-primary" disabled={generando}>
                    {generando ? "Generando..." : `📊 Generar Reporte ${formato}`}
                  </button>
                  {resultado && resultado.length > 0 && (
                    <button type="button" className="btn btn-secondary" onClick={() => setModalPreview(true)}>
                      👁 Vista Previa
                    </button>
                  )}
                </div>
              </form>
            </div>

            {resultado && (
              <div className="section-card" style={{ padding: 0, overflow: "hidden" }}>
                <div style={{ padding: "16px 20px 10px" }}>
                  <h2 style={{ margin: 0 }}>
                    Resultados — {tipoActual?.label}
                    <span className="badge badge-success" style={{ marginLeft: 10, fontSize: "12px" }}>{resultado.length} registros</span>
                  </h2>
                </div>
                <div className="table-wrapper" style={{ marginBottom: 0 }}>{renderTabla()}</div>
              </div>
            )}

            {!resultado && !generando && (
              <div className="section-card">
                <div className="empty-state">
                  <div className="empty-state-icon">📊</div>
                  <p>Configure los parámetros y genere el reporte para ver los resultados.</p>
                </div>
              </div>
            )}
          </div>

          {/* ── COLUMNA DERECHA ── */}
          <div className="reportes-right">
            <div className="section-card">
              <h2>Historial de Reportes</h2>
              {historial.length === 0 ? (
                <p style={{ fontSize: "13px", color: "var(--text-3)" }}>Aún no has generado reportes en esta sesión.</p>
              ) : historial.map((h, i) => (
                <div key={i} className="nomina-hist-item">
                  <div>
                    <div style={{ fontSize: "13px", fontWeight: 600 }}>
                      {TIPOS_REPORTE.find(t => t.id === h.tipo)?.icon} {h.tipo} ({h.formato})
                    </div>
                    <div style={{ fontSize: "11px", color: "var(--text-3)" }}>{h.fecha}</div>
                  </div>
                  <span className="badge badge-success" style={{ fontSize: "11px" }}>{h.estado}</span>
                </div>
              ))}
            </div>

            <div className="section-card">
              <h2>Formatos Disponibles</h2>
              <div style={{ fontSize: "13px", color: "var(--text-2)", lineHeight: 2 }}>
                <div><strong>📄 PDF</strong> — Para impresión y archivo oficial.</div>
                <div><strong>📊 XLSX</strong> — Para análisis en Excel.</div>
                <div><strong>📑 CSV</strong> — Para importación a otros sistemas.</div>
              </div>
            </div>
          </div>
        </div>
      </main>

      {modalPreview && resultado && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && setModalPreview(false)}>
          <div className="modal-box modal-box-wide">
            <div className="modal-header">
              <div><h3>👁 Vista Previa — {tipoActual?.label}</h3></div>
              <button className="modal-close" onClick={() => setModalPreview(false)}>×</button>
            </div>
            <div className="modal-body" style={{ maxHeight: "60vh", overflowY: "auto" }}>
              <div className="table-wrapper">{renderTabla()}</div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModalPreview(false)}>Cerrar</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
