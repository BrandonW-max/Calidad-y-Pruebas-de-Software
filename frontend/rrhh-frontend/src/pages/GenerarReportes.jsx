import { useState, useEffect } from "react";
import * as XLSX from "xlsx";
import Sidebar from "../components/Sidebar";
import logo from "../assets/logo.png";
import "../styles/Dashboard.css";
import { generarReporte, getDepartamentos, getCargos, listarEmpleados } from "../services/api";

const TIPOS_REPORTE = [
  { id: "empleados",   label: "Reporte de Empleados",   icon: "👥", desc: "Listado general o filtrado de empleados con sus datos personales y laborales.", color: "#3b82f6" },
  { id: "nomina",      label: "Reporte de Nómina",      icon: "💲", desc: "Detalle de pagos, bonificaciones y descuentos por período.",                    color: "#10b981" },
  { id: "asistencia",  label: "Reporte de Asistencia",  icon: "🕐", desc: "Registro de entradas, salidas, tardanzas e inasistencias.",                     color: "#f59e0b" },
  { id: "solicitudes", label: "Reporte de Solicitudes", icon: "📋", desc: "Permisos y licencias solicitadas, aprobadas o rechazadas.",                      color: "#8b5cf6" },
  { id: "historial-laboral", label: "Historial Laboral", icon: "📜", desc: "Ascensos, cambios de sueldo y sanciones de un empleado o de todos.",            color: "#ec4899" },
];

const FORMATOS = [
  { v: "PDF",   l: "PDF" },
  { v: "EXCEL", l: "XLSX" },
  { v: "CSV",   l: "CSV" },
];

// Nombres legibles de las columnas; las que no estén aquí se muestran con su clave
const ETIQUETAS_COLUMNAS = {
  fecha: "Fecha",
  empleado: "Empleado",
  tipo: "Tipo",
  detalle: "Detalle",
  periodo: "Periodo",
  sueldoContrato: "Sueldo Contrato",
  sueldoBase: "Sueldo Devengado",
  horasExtra: "Horas Extra",
  bonificaciones: "Bonificaciones",
  descuentos: "Descuentos",
  sueldoBruto: "Sueldo Bruto",
  sueldoNeto: "Sueldo Neto",
  estado: "Estado",
};

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
  const [departamentos, setDepartamentos] = useState([]);
  const [empleados, setEmpleados] = useState([]);
  const [empleadoId, setEmpleadoId] = useState("");
  const [departamento,  setDepartamento]  = useState("");
  const [cargo,         setCargo]         = useState("");
  const [cargos,        setCargos]        = useState([]);
  const [generando, setGenerando] = useState(false);
  const [mensaje,   setMensaje]   = useState("");
  // El aviso desaparece a los 7 s (el CSS lo desvanece en ese tiempo)
  useEffect(() => {
    if (!mensaje) return;
    const t = setTimeout(() => setMensaje(""), 7000);
    return () => clearTimeout(t);
  }, [mensaje]);
  useEffect(() => {
    getDepartamentos().then(setDepartamentos).catch(() => {});
    listarEmpleados(0, 200)
      .then(d => setEmpleados((d?.content ?? []).filter(e => e.estado === "ACTIVO")))
      .catch(() => {});
  }, []);
  useEffect(() => {
    getCargos(departamento).then(setCargos).catch(() => setCargos([]));
  }, [departamento]);
  const [error,     setError]     = useState("");
  const [resultado, setResultado] = useState(null);
  const [historial, setHistorial] = useState([]);
  const [modalPreview, setModalPreview] = useState(false);
  const [errorModal, setErrorModal] = useState({ open: false, message: "" });
  const closeErrorModal = () => setErrorModal({ open: false, message: "" });

  const nombreArchivo = (ext) =>
    `reporte_${tipoSeleccionado}_${new Date().toISOString().split("T")[0]}.${ext}`;

  const exportar = () => {
    if (!resultado?.length) return;

    if (formato === "CSV") {
      const headers = Object.keys(resultado[0]);
      const rows = resultado.map(r =>
        headers.map(h => `"${String(r[h] ?? "").replace(/"/g, '""')}"`).join(",")
      );
      const csv = [headers.join(","), ...rows].join("\n");
      const blob = new Blob(["﻿" + csv], { type: "text/csv;charset=utf-8;" });
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a"); a.href = url; a.download = nombreArchivo("csv"); a.click();
      URL.revokeObjectURL(url);
      return;
    }

    if (formato === "EXCEL") {
      const ws = XLSX.utils.json_to_sheet(resultado);
      const wb = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(wb, ws, tipoActual?.label ?? tipoSeleccionado);
      XLSX.writeFile(wb, nombreArchivo("xlsx"));
      return;
    }

    // PDF — genera HTML completo y lo abre en nueva pestaña vía blob URL
    const logoUrl = window.location.origin + logo;
    const headers = Object.keys(resultado[0]);
    const ths = headers.map(h => `<th>${h}</th>`).join("");
    const trs = resultado.map(r =>
      `<tr>${headers.map(h => `<td>${r[h] ?? ""}</td>`).join("")}</tr>`
    ).join("");

    const html = `<!DOCTYPE html><html lang="es"><head>
<meta charset="utf-8"><title>Reporte ${tipoSeleccionado}</title>
<style>
  body{font-family:Arial,sans-serif;font-size:11px;padding:24px;color:#1e293b}
  .hdr{display:flex;align-items:center;gap:16px;margin-bottom:20px;padding-bottom:14px;border-bottom:2px solid #3b82f6}
  .hdr img{height:54px}
  .hdr h1{margin:0 0 2px;font-size:16px;color:#1e293b}
  .hdr p{margin:0;font-size:11px;color:#64748b}
  h2{margin:0 0 4px;font-size:13px;color:#3b82f6}
  .meta{color:#64748b;font-size:11px;margin:0 0 14px}
  table{border-collapse:collapse;width:100%}
  th{background:#3b82f6;color:#fff;padding:6px 8px;text-align:left;border:1px solid #2563eb}
  td{padding:4px 8px;border:1px solid #e2e8f0}
  tr:nth-child(even){background:#f8fafc}
  .footer{margin-top:16px;font-size:10px;color:#94a3b8}
  .print-btn{margin-top:14px;padding:8px 18px;background:#3b82f6;color:#fff;border:none;border-radius:6px;cursor:pointer;font-size:13px}
  @media print{.print-btn{display:none}}
</style></head><body>
<div class="hdr">
  <img src="${logoUrl}" alt="Hospital San Gabriel" onerror="this.style.display='none'" />
  <div><h1>Hospital San Gabriel</h1><p>Sistema de Recursos Humanos</p></div>
</div>
<h2>${tipoActual?.icon ?? ""} ${tipoActual?.label ?? tipoSeleccionado}</h2>
<p class="meta">Generado: ${new Date().toLocaleString("es-PE")} — ${resultado.length} registros</p>
<table><thead><tr>${ths}</tr></thead><tbody>${trs}</tbody></table>
<p class="footer">Documento generado automáticamente — Hospital San Gabriel</p>
<button class="print-btn" onclick="window.print()">🖨 Imprimir / Guardar como PDF</button>
</body></html>`;

    const blob = new Blob([html], { type: "text/html;charset=utf-8" });
    const url = URL.createObjectURL(blob);
    window.open(url, "_blank");
    setTimeout(() => URL.revokeObjectURL(url), 10000);
  };

  const handleGenerar = async (e) => {
    e.preventDefault();
    setGenerando(true); setMensaje(""); setError(""); setResultado(null);

    let inicio = fechaIni || undefined;
    let fin    = fechaFin || undefined;

    if (tipoSeleccionado === "nomina" || tipoSeleccionado === "asistencia") {
      const ultimoDia = new Date(Number(anio), Number(mes), 0).getDate();
      inicio = `${anio}-${String(mes).padStart(2, '0')}-01`;
      fin    = `${anio}-${String(mes).padStart(2, '0')}-${String(ultimoDia).padStart(2, '0')}`;
    }

    const params = {
      tipo:    tipoSeleccionado,
      formato: undefined,
      fechaInicio: inicio,
      fechaFin:    fin,
      estado:     (tipoSeleccionado === "solicitudes" || tipoSeleccionado === "empleados") ? estado || undefined : undefined,
      area:       (tipoSeleccionado === "nomina" || tipoSeleccionado === "solicitudes") ? departamento || undefined : undefined,
      cargo:      tipoSeleccionado === "solicitudes" ? cargo.trim() || undefined : undefined,
      idEmpleado: tipoSeleccionado === "historial-laboral" && empleadoId ? Number(empleadoId) : undefined,
    };

    try {
      // generarReporte returns raw Response — must resolve content-type before consuming body
      const response = await generarReporte(params);
      if (!response.ok) throw new Error(`HTTP ${response.status}`);

      const contentType = response.headers.get("content-type") || "";
      const isFile = contentType.includes("application/pdf")
        || contentType.includes("application/vnd")
        || contentType.includes("text/csv")
        || contentType.includes("application/octet-stream");

      if (isFile) {
        const blob = await response.blob();
        const url  = URL.createObjectURL(blob);
        const a    = document.createElement("a");
        a.href     = url;
        const ext = formato === "EXCEL" ? "xlsx" : formato.toLowerCase();
        a.download = `reporte_${tipoSeleccionado}_${new Date().toISOString().split("T")[0]}.${ext}`;
        a.click();
        URL.revokeObjectURL(url);
        setMensaje("Reporte generado y descargado correctamente.");
        setHistorial(h => [{ tipo: tipoSeleccionado, formato, fecha: new Date().toLocaleString("es-PE"), estado: "Descargado" }, ...h].slice(0, 10));
      } else {
        const data = await response.json();
        const lista = Array.isArray(data) ? data : (data?.datos ?? data?.data ?? data?.content ?? []);
        setResultado(lista);
        if (lista.length === 0) {
          setMensaje("No se encontraron datos para el período o filtros seleccionados. Pruebe con otro mes o amplíe los criterios.");
        } else {
          setMensaje(`Reporte generado correctamente — ${lista.length} registro(s) encontrado(s).`);
        }
        setHistorial(h => [{ tipo: tipoSeleccionado, formato, fecha: new Date().toLocaleString("es-PE"), estado: lista.length > 0 ? "Generado" : "Sin datos" }, ...h].slice(0, 10));
      }
    } catch (e) {
      setErrorModal({ open: true, message: e.message || "Error al generar el reporte. Verifique los filtros e intente de nuevo." });
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
        <thead><tr>{keys.map(k => <th key={k} style={{ textAlign: "center", ...(k === "fecha" ? { whiteSpace: "nowrap", minWidth: 120 } : {}) }}>{ETIQUETAS_COLUMNAS[k] ?? k}</th>)}</tr></thead>
        <tbody>
          {resultado.slice(0, 50).map((row, i) => (
            <tr key={i}>{keys.map(k => <td key={k} style={{ fontSize: "12px", textAlign: "center", whiteSpace: k === "fecha" ? "nowrap" : "pre-line", ...(k === "fecha" ? { minWidth: 120 } : {}) }}>{row[k] != null ? String(row[k]) : "—"}</td>)}</tr>
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

        {mensaje && <div className="alert alert-floating alert-success" onClick={() => setMensaje("")}>✅ {mensaje}</div>}
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
                      {FORMATOS.map(f => <option key={f.v} value={f.v}>{f.l}</option>)}
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

                  {(tipoSeleccionado === "nomina" || tipoSeleccionado === "solicitudes") && (
                    <div className="form-group">
                      <label>Departamento</label>
                      <select value={departamento} onChange={e => { setDepartamento(e.target.value); setCargo(""); }}>
                        <option value="">Todos</option>
                        {departamentos.map(d => <option key={d.idDpto} value={d.nombre}>{d.nombre}</option>)}
                      </select>
                    </div>
                  )}

                  {tipoSeleccionado === "solicitudes" && (
                    <div className="form-group">
                      <label>Cargo</label>
                      <select value={cargo} onChange={e => setCargo(e.target.value)}>
                        <option value="">Todos</option>
                        {cargos.map(c => <option key={c} value={c}>{c}</option>)}
                      </select>
                    </div>
                  )}

                  {tipoSeleccionado === "historial-laboral" && (
                    <div className="form-group">
                      <label>Empleado</label>
                      <select value={empleadoId} onChange={e => setEmpleadoId(e.target.value)}>
                        <option value="">Todos los empleados</option>
                        {empleados.map(e => <option key={e.idEmpleado} value={e.idEmpleado}>{e.nombres} {e.apellidos}</option>)}
                      </select>
                    </div>
                  )}

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
                          ? <><option value="PENDIENTE">Pendiente</option><option value="APROBADA">Aprobado</option><option value="RECHAZADA">Rechazado</option></>
                          : <><option value="ACTIVO">Activo</option><option value="DESVINCULADO">Inactivo</option></>
                        }
                      </select>
                    </div>
                  )}
                </div>
                <div className="form-actions" style={{ marginTop: 16, flexWrap: "wrap", gap: "8px" }}>
                  <button type="submit" className="btn btn-primary" disabled={generando}>
                    {generando ? "Generando..." : "📊 Generar Reporte"}
                  </button>
                  {resultado && resultado.length > 0 && (<>
                    <button type="button" className="btn btn-secondary" onClick={() => setModalPreview(true)}>
                      👁 Vista Previa
                    </button>
                    <button type="button" className="btn btn-secondary" onClick={exportar}>
                      💾 Exportar
                    </button>
                  </>)}
                </div>
              </form>
            </div>

            {resultado && (
              <div className="section-card asistencia-panel">
                <div className="section-header asistencia-panel-header" style={{ marginBottom: "14px" }}>
                  <h2 style={{ margin: 0, border: "none", padding: 0 }}>
                    Resultados — {tipoActual?.label}
                  </h2>
                </div>
                <div className="table-wrapper" style={{ marginBottom: 0, paddingBottom: 14, ...(resultado.length > 15 ? { maxHeight: 690, overflowY: "auto" } : {}) }}>{renderTabla()}</div>
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
        <div className="modal-overlay">
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
              <button className="btn btn-secondary" onClick={exportar}>💾 Exportar</button>
            </div>
          </div>
        </div>
      )}

      {/* ══ MODAL: ERROR ══ */}
      {errorModal.open && (
        <div className="modal-overlay" style={{ zIndex: 10000 }}>
          <div className="modal-box" style={{ maxWidth: 420 }}>
            <div className="modal-header">
              <div><h3>⚠️ Error de validación</h3></div>
              <button className="modal-close" onClick={closeErrorModal}>×</button>
            </div>
            <div className="modal-body">
              <p style={{ fontSize: 14, color: "var(--text-1)", margin: 0 }}>{errorModal.message}</p>
            </div>
            <div className="modal-footer">
              <button className="btn btn-primary" onClick={closeErrorModal}>Entendido</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
