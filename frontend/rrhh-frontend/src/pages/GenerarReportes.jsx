import { useState } from "react";
import Sidebar from "../components/Sidebar";
import "../styles/Dashboard.css";
import { generarReporte } from "../services/api";

const TIPOS = [
  { key: "empleados",   label: "Empleados",  icon: "👥", desc: "Estado del personal, antigüedad y contratos" },
  { key: "asistencia",  label: "Asistencia", icon: "📋", desc: "Registros de entrada/salida, tardanzas y horas" },
  { key: "nomina",      label: "Nómina",     icon: "💰", desc: "Sueldos, bonificaciones y descuentos del período" },
  { key: "solicitudes", label: "Solicitudes", icon: "📅", desc: "Permisos y vacaciones aprobados/rechazados" },
];

const FILTROS_INICIALES = {
  empleados:   { area: "", cargo: "", tipoContrato: "", estado: "", nivelJerarquico: "", empleado: "" },
  asistencia:  { fechaInicio: "", fechaFin: "", area: "", empleado: "" },
  nomina:      { fechaInicio: "", fechaFin: "", area: "", empleado: "", tipoPago: "", rangoSalarial: "" },
  solicitudes: { fechaInicio: "", fechaFin: "", area: "", empleado: "", tipoSolicitud: "", estado: "" },
};

export default function GenerarReportes() {
  const [tipoActivo, setTipoActivo] = useState("empleados");
  const [filtros, setFiltros]       = useState(FILTROS_INICIALES);
  const [resultado, setResultado]   = useState(null);
  const [loading, setLoading]       = useState(false);
  const [mensaje, setMensaje]       = useState("");
  const [error, setError]           = useState("");

  const f    = filtros[tipoActivo];
  const setF = (campo, valor) => setFiltros({ ...filtros, [tipoActivo]: { ...f, [campo]: valor } });

  const tipoInfo = TIPOS.find((t) => t.key === tipoActivo);

  const handleGenerar = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    setMensaje("");
    setResultado(null);
    try {
      const res  = await generarReporte(tipoActivo, f, null);
      const data = await res.json();
      setResultado(data);
      setMensaje("Reporte generado correctamente");
    } catch {
      setError("No se encontraron datos para los filtros seleccionados. Verifique los criterios e intente nuevamente.");
    } finally {
      setLoading(false);
    }
  };

  const handleExportar = async (formato) => {
    setMensaje("");
    try {
      const res  = await generarReporte(tipoActivo, f, formato);
      const blob = await res.blob();
      const ext  = formato === "PDF" ? "pdf" : formato === "XLSX" ? "xlsx" : "csv";
      const url  = URL.createObjectURL(blob);
      const a    = document.createElement("a");
      a.href     = url;
      a.download = `reporte_${tipoActivo}_${new Date().toISOString().slice(0, 10)}.${ext}`;
      a.click();
      URL.revokeObjectURL(url);
    } catch {
      setMensaje("Error al exportar el reporte. Intente nuevamente.");
    }
  };

  const limpiar = () => {
    setFiltros(FILTROS_INICIALES);
    setResultado(null);
    setMensaje("");
    setError("");
  };

  const tieneFechas = ["asistencia", "nomina", "solicitudes"].includes(tipoActivo);

  return (
    <div className="dashboard">
      <Sidebar rol="RRHH" />

      <main className="main-content">
        <header className="main-header header-rrhh">
          <div className="header-left">
            <span className="page-breadcrumb">RRHH / Generar Reportes</span>
            <h1>Generar Reportes</h1>
          </div>
          <span className="role-pill role-pill-rrhh">ECU-06</span>
        </header>

        {mensaje && <div className="alert alert-success">✅ {mensaje}</div>}
        {error   && <div className="alert alert-warning">⚠️ {error}</div>}

        {/* ── SELECCIÓN DE TIPO ── */}
        <div className="section-card">
          <h2>📊 Seleccione el Tipo de Reporte</h2>
          <div className="cards-grid">
            {TIPOS.map((t) => (
              <div
                key={t.key}
                className="card"
                style={tipoActivo === t.key ? { borderColor: "#2563eb", boxShadow: "0 0 0 3px rgba(37,99,235,0.15)" } : {}}
                onClick={() => { setTipoActivo(t.key); setResultado(null); setMensaje(""); setError(""); }}
              >
                <span className="card-icon">{t.icon}</span>
                <h3>Reporte de {t.label}</h3>
                <p>{t.desc}</p>
              </div>
            ))}
          </div>
        </div>

        {/* ── FILTROS ── */}
        <div className="section-card">
          <h2>
            {tipoInfo?.icon} Filtros — Reporte de {tipoInfo?.label}
          </h2>
          <div className="alert alert-info">
            Los campos de filtro son opcionales. Deje en blanco para incluir todos los registros.
          </div>
          <form onSubmit={handleGenerar}>
            <div className="form-grid">

              {tieneFechas && (
                <>
                  <div className="form-group">
                    <label>Fecha Inicio</label>
                    <input type="date" value={f.fechaInicio} onChange={(e) => setF("fechaInicio", e.target.value)} />
                  </div>
                  <div className="form-group">
                    <label>Fecha Fin</label>
                    <input type="date" value={f.fechaFin} onChange={(e) => setF("fechaFin", e.target.value)} />
                  </div>
                </>
              )}

              <div className="form-group">
                <label>Área / Departamento</label>
                <input placeholder="Todas las áreas" value={f.area} onChange={(e) => setF("area", e.target.value)} />
              </div>

              <div className="form-group">
                <label>Empleado (nombre o DNI)</label>
                <input placeholder="Todos los empleados" value={f.empleado} onChange={(e) => setF("empleado", e.target.value)} />
              </div>

              {tipoActivo === "empleados" && (
                <>
                  <div className="form-group">
                    <label>Cargo</label>
                    <input placeholder="Todos los cargos" value={f.cargo} onChange={(e) => setF("cargo", e.target.value)} />
                  </div>
                  <div className="form-group">
                    <label>Tipo de Contrato</label>
                    <select value={f.tipoContrato} onChange={(e) => setF("tipoContrato", e.target.value)}>
                      <option value="">Todos</option>
                      <option value="PLAZO_FIJO">Plazo fijo</option>
                      <option value="INDEFINIDO">Indefinido</option>
                      <option value="SERVICIOS">Por servicio</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Estado del Empleado</label>
                    <select value={f.estado} onChange={(e) => setF("estado", e.target.value)}>
                      <option value="">Todos</option>
                      <option value="ACTIVO">Activo</option>
                      <option value="INACTIVO">Inactivo</option>
                    </select>
                  </div>
                </>
              )}

              {tipoActivo === "nomina" && (
                <>
                  <div className="form-group">
                    <label>Tipo de Pago</label>
                    <select value={f.tipoPago} onChange={(e) => setF("tipoPago", e.target.value)}>
                      <option value="">Todos</option>
                      <option value="MENSUAL">Mensual</option>
                      <option value="QUINCENAL">Quincenal</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Rango Salarial</label>
                    <input placeholder="Ej: 1000-4000" value={f.rangoSalarial} onChange={(e) => setF("rangoSalarial", e.target.value)} />
                  </div>
                </>
              )}

              {tipoActivo === "solicitudes" && (
                <>
                  <div className="form-group">
                    <label>Tipo de Solicitud</label>
                    <select value={f.tipoSolicitud} onChange={(e) => setF("tipoSolicitud", e.target.value)}>
                      <option value="">Todos</option>
                      <option value="VACACIONES">Vacaciones</option>
                      <option value="PERMISO_PERSONAL">Permiso personal</option>
                      <option value="PERMISO_MEDICO">Permiso médico</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Estado de la Solicitud</label>
                    <select value={f.estado} onChange={(e) => setF("estado", e.target.value)}>
                      <option value="">Todos</option>
                      <option value="PENDIENTE">Pendiente</option>
                      <option value="APROBADA">Aprobada</option>
                      <option value="RECHAZADA">Rechazada</option>
                    </select>
                  </div>
                </>
              )}
            </div>

            <div className="form-actions">
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? "⏳ Generando..." : "📊 Generar Reporte"}
              </button>
              <button type="button" className="btn btn-secondary" onClick={limpiar}>
                Limpiar Filtros
              </button>
            </div>
          </form>
        </div>

        {/* ── RESULTADO ── */}
        {resultado && (
          <div className="section-card">
            <h2>📋 Resultado del Reporte — {tipoInfo?.label}</h2>

            {resultado.resumen && (
              <div className="stats-row" style={{ marginBottom: "20px" }}>
                {Object.entries(resultado.resumen).map(([k, v]) => (
                  <div className="stat-box accent-blue" key={k}>
                    <div className="stat-info">
                      <div className="stat-value">{v}</div>
                      <div className="stat-label">{k}</div>
                    </div>
                  </div>
                ))}
              </div>
            )}

            {resultado.datos && resultado.datos.length > 0 ? (
              <div className="table-wrapper">
                <table>
                  <thead>
                    <tr>{Object.keys(resultado.datos[0]).map((col) => <th key={col}>{col}</th>)}</tr>
                  </thead>
                  <tbody>
                    {resultado.datos.map((fila, i) => (
                      <tr key={i}>
                        {Object.values(fila).map((val, j) => <td key={j}>{val ?? "—"}</td>)}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <div className="empty-state">
                <div className="empty-state-icon">🔍</div>
                <p>No hay datos para mostrar con los filtros seleccionados</p>
              </div>
            )}

            <hr className="divider" />
            <h2>⬇️ Exportar Reporte</h2>
            <div className="alert alert-info" style={{ marginBottom: "14px" }}>
              Seleccione el formato de exportación. El archivo se descargará automáticamente.
            </div>
            <div className="form-actions">
              <button className="btn btn-danger" onClick={() => handleExportar("PDF")}>
                📄 Exportar PDF
              </button>
              <button className="btn btn-success" onClick={() => handleExportar("XLSX")}>
                📊 Exportar Excel
              </button>
              <button className="btn btn-secondary" onClick={() => handleExportar("CSV")}>
                📑 Exportar CSV
              </button>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
