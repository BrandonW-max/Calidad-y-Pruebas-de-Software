import { useState, useEffect } from "react";
import Sidebar from "../components/Sidebar";
import "../styles/Dashboard.css";
import { getNominasPorEmpleado, getIdEmpleado, descargarComprobante } from "../services/api";
import { Volume2 } from "lucide-react";
import PayrollAudioModal from "../components/PayrollAudioModal";

const getBonif = (n) =>
  (Number(n.bonifFamiliar)      || 0) +
  (Number(n.bonifTurnoNocturno) || 0) +
  (Number(n.bonifGuardia)       || 0) +
  (Number(n.bonifRiesgo)        || 0) +
  (Number(n.bonifCargo)         || 0);

const getDesc = (n) =>
  (Number(n.descuentoTardanzas) || 0) +
  (Number(n.descuentoLey)       || 0);

export default function NominaEmpleado() {
  const idEmpleado = getIdEmpleado();
  const [nominas,  setNominas]  = useState([]);
  const [loading,  setLoading]  = useState(false);
  const [error,    setError]    = useState("");
  const [audioModal, setAudioModal] = useState({ open: false, nomina: null, periodoLabel: "" });

  async function cargarNominas() {
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
  }

  useEffect(() => {
    if (idEmpleado) cargarNominas();
    else setError("No se pudo obtener tu ID de empleado. Contacta a RRHH.");
  }, [idEmpleado]); // eslint-disable-line react-hooks/exhaustive-deps

  const [descargando, setDescargando] = useState(null);

  const handleDescargar = async (idNomina, periodo) => {
    setDescargando(idNomina);
    setError("");
    try {
      const blob = await descargarComprobante(idNomina);
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `Comprobante_Nomina_${periodo ?? idNomina}.pdf`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    } catch {
      setError("No se pudo descargar el comprobante. Intente de nuevo.");
    } finally {
      setDescargando(null);
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

      <main className="main-content" id="main-content" tabIndex="-1">
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

        {error   && <div className="alert alert-warning">⚠️ {error}</div>}

        {nominas.length > 0 && (
          <div style={{
            background: "linear-gradient(135deg, #f0fdf4 0%, #eff6ff 100%)",
            border: "1px solid #bfdbfe",
            borderRadius: 12,
            padding: "14px 18px",
            marginBottom: 16,
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            flexWrap: "wrap",
            gap: 12
          }}>
            <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
              <span style={{ fontSize: 26 }}>🎧</span>
              <div>
                <strong style={{ fontSize: 14, color: "#1e3a8a", display: "block" }}>
                  Inclusión ODS 10: Asistente por Voz y Lectura Fácil
                </strong>
                <span style={{ fontSize: 12, color: "#475569" }}>
                  Escucha el desglose de tu sueldo o léelo sin tecnicismos en cualquier momento.
                </span>
              </div>
            </div>
            <button
              type="button"
              className="btn btn-primary btn-sm"
              style={{ display: "inline-flex", alignItems: "center", gap: 6 }}
              onClick={() => setAudioModal({
                open: true,
                nomina: nominas[0],
                periodoLabel: nominas[0].mes && nominas[0].anio
                  ? fmtPeriodo(nominas[0].mes, nominas[0].anio)
                  : (nominas[0].periodo ?? "Último período")
              })}
            >
              <Volume2 size={16} />
              <span>Escuchar última nómina</span>
            </button>
          </div>
        )}

        <div className="section-card asistencia-panel">
          <div className="section-header asistencia-panel-header" style={{ marginBottom: "14px" }}>
            <h2 style={{ margin: 0, border: "none", padding: 0 }}>Historial de Pagos</h2>
          </div>

          <div className="table-wrapper" style={{ marginBottom: 0, paddingBottom: 14, ...(nominas.length > 15 ? { maxHeight: 690, overflowY: "auto" } : {}) }}>
            {loading ? (
              <div className="loading-text">Cargando tu historial de nómina...</div>
            ) : (
              <table>
                <thead>
                  <tr>
                    <th style={{ textAlign: "center" }}>#</th>
                    <th style={{ textAlign: "center" }}>Período</th>
                    <th style={{ textAlign: "center" }}>Sueldo Devengado</th>
                    <th style={{ textAlign: "center" }}>Bonificaciones</th>
                    <th style={{ textAlign: "center" }}>Descuentos</th>
                    <th style={{ textAlign: "center" }}>Sueldo Neto</th>
                    <th style={{ textAlign: "center" }}>Acciones</th>
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
                      <td style={{ textAlign: "center", color: "var(--text-3)", fontSize: "13px" }}>{n.idNomina ?? i + 1}</td>
                      <td style={{ textAlign: "center", fontWeight: 600 }}>
                        {n.mes && n.anio
                          ? fmtPeriodo(n.mes, n.anio)
                          : n.periodo ?? "—"}
                      </td>
                      <td style={{ textAlign: "center" }}>{fmtS(n.sueldoBase ?? n.sueldo)}</td>
                      <td style={{ textAlign: "center", color: "var(--success)" }}>
                        {fmtS(getBonif(n))}
                      </td>
                      <td style={{ textAlign: "center", color: "#ef4444" }}>
                        {fmtS(getDesc(n))}
                      </td>
                      <td style={{ textAlign: "center", fontWeight: 700, color: "#0f172a" }}>
                        {fmtS(n.sueldoNeto)}
                      </td>
                      <td style={{ textAlign: "center" }}>
                        <div style={{ display: "flex", gap: "6px", justifyContent: "center", alignItems: "center" }}>
                          <button
                            type="button"
                            className="btn btn-primary btn-sm"
                            style={{ display: "inline-flex", alignItems: "center", gap: 4, background: "#0284c7" }}
                            title="Escuchar resumen en voz alta y modo lectura fácil (ODS 10)"
                            aria-label={`Escuchar resumen por voz de nómina ${n.periodo ?? ""}`}
                            onClick={() => setAudioModal({
                              open: true,
                              nomina: n,
                              periodoLabel: n.mes && n.anio ? fmtPeriodo(n.mes, n.anio) : (n.periodo ?? "")
                            })}
                          >
                            <Volume2 size={14} />
                            <span>Escuchar</span>
                          </button>
                          <button
                            className="btn btn-secondary btn-sm"
                            onClick={() => handleDescargar(n.idNomina, n.periodo)}
                            disabled={!n.idNomina || descargando === n.idNomina}
                          >
                            {descargando === n.idNomina ? "..." : "⬇ PDF"}
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>

          {nominas.length > 0 && (
            <div className="table-footer-bar">
              <span>ℹ️ Haga clic en <strong>Escuchar</strong> para la narración accesible y lectura fácil, o en ⬇ PDF para descargar el comprobante oficial.</span>
            </div>
          )}
        </div>
      </main>

      {/* Modal de Lectura Fácil y Síntesis de Voz */}
      <PayrollAudioModal
        nomina={audioModal.nomina}
        periodoLabel={audioModal.periodoLabel}
        isOpen={audioModal.open}
        onClose={() => setAudioModal({ open: false, nomina: null, periodoLabel: "" })}
      />
    </div>
  );
}
