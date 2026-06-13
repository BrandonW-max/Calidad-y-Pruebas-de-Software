import { useState, useEffect } from "react";
import Sidebar from "../components/Sidebar";
import logo from "../assets/logo.png";
import "../styles/Dashboard.css";
import { getNominasPorEmpleado, getIdEmpleado } from "../services/api";

export default function NominaEmpleado() {
  const idEmpleado = getIdEmpleado();
  const [nominas,  setNominas]  = useState([]);
  const [loading,  setLoading]  = useState(false);
  const [error,    setError]    = useState("");
  const [mensaje,  setMensaje]  = useState("");
  useEffect(() => {
    if (idEmpleado) cargarNominas();
    else setError("No se pudo obtener tu ID de empleado. Contacta a RRHH.");
  }, [idEmpleado]);

  const cargarNominas = async () => {
    setLoading(true);
    setError("");
    try {
      const data = await getNominasPorEmpleado(idEmpleado);
      setNominas(Array.isArray(data) ? data : []);
    } catch {
      setError("No se pudo cargar tu historial de nómina.");
    } finally {
      setLoading(false);
    }
  };

  const MESES_LABELS = ["","Enero","Febrero","Marzo","Abril","Mayo","Junio",
                         "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"];

  const abrirComprobanteHTML = (n) => {
    const base     = Number(n.sueldoBase ?? n.sueldo ?? 0);
    const bonif    = Number(n.bonificaciones ?? n.totalBonificaciones ?? 0);
    const desc     = Number(n.descuentos ?? n.totalDescuentos ?? 0);
    const neto     = Number(n.sueldoNeto ?? n.totalNeto ?? 0);
    const mesNum   = parseInt(n.mes);
    const mesLabel = !isNaN(mesNum) ? MESES_LABELS[mesNum] : (n.mes ?? "—");
    const logoUrl  = window.location.origin + logo;
    const fam      = n.cantidadFamiliares ?? 3;
    const fmt      = (v) => `S/ ${Number(v).toLocaleString("es-PE", { minimumFractionDigits: 2 })}`;

    const html = `<!DOCTYPE html><html lang="es"><head>
<meta charset="utf-8"><title>Comprobante Nómina #${n.idNomina}</title>
<style>
  body{font-family:Arial,sans-serif;font-size:12px;padding:28px;max-width:680px;margin:0 auto;color:#1e293b}
  .hdr{display:flex;align-items:center;gap:16px;margin-bottom:20px;padding-bottom:14px;border-bottom:2px solid #0891b2}
  .hdr img{height:60px}
  .hdr h1{margin:0 0 2px;font-size:18px;color:#1e293b}
  .hdr p{margin:0;font-size:11px;color:#64748b}
  .titulo{font-size:14px;font-weight:700;color:#0891b2;margin:0 0 16px;text-align:center}
  .grid{display:grid;grid-template-columns:1fr 1fr;gap:10px;margin-bottom:20px;background:#f0f9ff;padding:14px;border-radius:8px}
  .gi{display:flex;flex-direction:column;gap:2px}
  .gl{font-size:10px;color:#64748b;text-transform:uppercase;letter-spacing:0.5px}
  .gv{font-weight:600;font-size:13px}
  .sec{margin-bottom:16px}
  .st{font-size:12px;font-weight:700;padding:6px 10px;border-radius:4px;margin-bottom:8px}
  .gt{background:#dcfce7;color:#15803d}
  .rt{background:#fee2e2;color:#b91c1c}
  .row{display:flex;justify-content:space-between;padding:5px 10px;font-size:12px}
  .row:nth-child(even){background:#f8fafc}
  .sub{display:flex;justify-content:space-between;padding:6px 10px;font-weight:700;border-top:1px dashed #cbd5e1;margin-top:4px}
  .total{background:#0e7490;color:#fff;border-radius:8px;padding:16px 20px;display:flex;justify-content:space-between;align-items:center;margin-top:20px}
  .tl{font-size:14px;font-weight:600}
  .tv{font-size:22px;font-weight:800}
  .footer{margin-top:24px;font-size:10px;color:#94a3b8;text-align:center;border-top:1px solid #e2e8f0;padding-top:12px}
  .pbtn{display:block;margin:16px auto 0;padding:9px 22px;background:#0891b2;color:#fff;border:none;border-radius:6px;cursor:pointer;font-size:13px}
  @media print{.pbtn{display:none}}
</style></head><body>
<div class="hdr">
  <img src="${logoUrl}" alt="Logo" onerror="this.style.display='none'" />
  <div><h1>Hospital San Gabriel</h1><p>Sistema de Recursos Humanos — Boleta de Pago</p></div>
</div>
<div class="titulo">BOLETA DE PAGO — ${mesLabel.toUpperCase()} ${n.anio ?? "—"}</div>
<div class="grid">
  <div class="gi"><span class="gl">Empleado</span><span class="gv">${n.empleadoNombre ?? `Empleado #${n.idEmpleado}`}</span></div>
  <div class="gi"><span class="gl">N° Nómina</span><span class="gv">#${n.idNomina}</span></div>
  <div class="gi"><span class="gl">Período</span><span class="gv">${mesLabel} ${n.anio ?? "—"}</span></div>
  <div class="gi"><span class="gl">Sueldo Base</span><span class="gv">${fmt(base)}</span></div>
</div>
<div class="sec">
  <div class="st gt">+ BONIFICACIONES</div>
  <div class="row"><span>Bonif. Familiar (${fam} dependientes)</span><span style="color:#15803d">+${fmt(bonif * 0.40)}</span></div>
  <div class="row"><span>Bonif. Transporte</span><span style="color:#15803d">+${fmt(bonif * 0.35)}</span></div>
  <div class="row"><span>Bonif. Extraordinaria</span><span style="color:#15803d">+${fmt(bonif * 0.25)}</span></div>
  <div class="sub"><span>Total Bonificaciones</span><span style="color:#15803d">+${fmt(bonif)}</span></div>
</div>
<div class="sec">
  <div class="st rt">— DESCUENTOS</div>
  <div class="row"><span>AFP (10% sueldo base)</span><span style="color:#b91c1c">-${fmt(desc * 0.50)}</span></div>
  <div class="row"><span>ESSALUD (3%)</span><span style="color:#b91c1c">-${fmt(desc * 0.30)}</span></div>
  <div class="row"><span>Impuesto 5ª Categoría</span><span style="color:#b91c1c">-${fmt(desc * 0.20)}</span></div>
  <div class="sub"><span>Total Descuentos</span><span style="color:#b91c1c">-${fmt(desc)}</span></div>
</div>
<div class="total"><span class="tl">SUELDO NETO A PAGAR</span><span class="tv">${fmt(neto)}</span></div>
<div class="footer">Documento generado automáticamente por el sistema RRHH — Hospital San Gabriel<br>${new Date().toLocaleString("es-PE")}</div>
<button class="pbtn" onclick="window.print()">🖨 Imprimir / Guardar como PDF</button>
</body></html>`;

    const blob = new Blob([html], { type: "text/html;charset=utf-8" });
    const url = URL.createObjectURL(blob);
    window.open(url, "_blank");
    setTimeout(() => URL.revokeObjectURL(url), 10000);
  };

  const handleDescargar = (idNomina) => {
    const n = nominas.find(x => String(x.idNomina ?? x.id) === String(idNomina));
    if (n) {
      abrirComprobanteHTML(n);
    } else {
      setError("No se encontró la nómina. Recargue la página e intente de nuevo.");
    }
  };

  const fmtS = (v) =>
    v != null ? `S/ ${Number(v).toLocaleString("es-PE", { minimumFractionDigits: 2 })}` : "—";

  const fmtPeriodo = (mes, anio) => {
    const MESES = ["Enero","Febrero","Marzo","Abril","Mayo","Junio",
                   "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"];
    const m = parseInt(mes);
    return `${isNaN(m) ? mes : MESES[m - 1]} ${anio}`;
  };

  return (
    <div className="dashboard">
      <Sidebar rol="EMPLEADO" />

      <main className="main-content">
        <header className="main-header header-empleado">
          <div className="header-left">
            <span className="page-breadcrumb">Dashboard / Mi Nómina</span>
            <h1>Mi Nómina</h1>
            <p style={{ margin: 0, fontSize: "13px", color: "var(--text-3)" }}>
              Historial de pagos y comprobantes de nómina.
            </p>
          </div>
          <span className="role-pill role-pill-empleado">Empleado</span>
        </header>

        {mensaje && <div className="alert alert-success">✅ {mensaje}</div>}
        {error   && <div className="alert alert-warning">⚠️ {error}</div>}

        <div className="section-card" style={{ padding: 0, overflow: "hidden" }}>
          <div style={{ padding: "16px 20px 12px", borderBottom: "1px solid var(--border)" }}>
            <h2 style={{ margin: 0 }}>Historial de Pagos</h2>
          </div>

          <div className="table-wrapper" style={{ marginBottom: 0 }}>
            {loading ? (
              <div className="loading-text">Cargando tu historial de nómina...</div>
            ) : (
              <table>
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Período</th>
                    <th>Sueldo Base</th>
                    <th>Bonificaciones</th>
                    <th>Descuentos</th>
                    <th>Sueldo Neto</th>
                    <th>Comprobante</th>
                  </tr>
                </thead>
                <tbody>
                  {nominas.length === 0 ? (
                    <tr>
                      <td colSpan={7}>
                        <div className="empty-state">
                          <div className="empty-state-icon">💲</div>
                          <p>No hay registros de nómina disponibles</p>
                        </div>
                      </td>
                    </tr>
                  ) : nominas.map((n, i) => (
                    <tr key={n.idNomina ?? i}>
                      <td style={{ color: "var(--text-3)", fontSize: "13px" }}>{n.idNomina ?? i + 1}</td>
                      <td style={{ fontWeight: 600 }}>
                        {n.mes && n.anio
                          ? fmtPeriodo(n.mes, n.anio)
                          : n.periodo ?? "—"}
                      </td>
                      <td>{fmtS(n.sueldoBase ?? n.sueldo)}</td>
                      <td style={{ color: "var(--success)" }}>
                        {fmtS(n.bonificaciones ?? n.totalBonificaciones)}
                      </td>
                      <td style={{ color: "#ef4444" }}>
                        {fmtS(n.descuentos ?? n.totalDescuentos)}
                      </td>
                      <td style={{ fontWeight: 700, color: "#0f172a" }}>
                        {fmtS(n.sueldoNeto ?? n.totalNeto)}
                      </td>
                      <td>
                        <button
                          className="btn btn-secondary btn-sm"
                          onClick={() => handleDescargar(n.idNomina)}
                          disabled={!n.idNomina}
                        >
                          ⬇ PDF
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>

          {nominas.length > 0 && (
            <div className="table-footer-bar">
              <span>ℹ️ Haga clic en ⬇ PDF para descargar el comprobante de cada período.</span>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
