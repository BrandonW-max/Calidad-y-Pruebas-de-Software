import { useState } from "react";
import { Link } from "react-router-dom";
import "../styles/Dashboard.css";
import { generarReporte } from "../services/api";

const TIPOS = [
  { key: "empleados",   label: "Empleados",   icon: "👥" },
  { key: "asistencia",  label: "Asistencia",  icon: "📋" },
  { key: "nomina",      label: "Nómina",       icon: "💰" },
  { key: "solicitudes", label: "Solicitudes",  icon: "📅" },
];

const FILTROS_INICIALES = {
  empleados:   { area: "", cargo: "", tipoContrato: "", estado: "", nivelJerarquico: "", empleado: "" },
  asistencia:  { fechaInicio: "", fechaFin: "", area: "", empleado: "" },
  nomina:      { fechaInicio: "", fechaFin: "", area: "", empleado: "", tipoPago: "", rangoSalarial: "" },
  solicitudes: { fechaInicio: "", fechaFin: "", area: "", empleado: "", tipoSolicitud: "", estado: "" },
};

export default function GenerarReportes() {
  const [tipoActivo, setTipoActivo] = useState("empleados");
  const [filtros, setFiltros] = useState(FILTROS_INICIALES);
  const [resultado, setResultado] = useState(null);
  const [loading, setLoading] = useState(false);
  const [mensaje, setMensaje] = useState("");
  const [error, setError] = useState("");

  const f = filtros[tipoActivo];
  const setF = (campo, valor) =>
    setFiltros({ ...filtros, [tipoActivo]: { ...f, [campo]: valor } });

  const handleGenerar = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    setMensaje("");
    setResultado(null);
    try {
      const res = await generarReporte(tipoActivo, f, null);
      const data = await res.json();
      setResultado(data);
      setMensaje("Reporte generado correctamente");
    } catch {
      setError("No se encontraron datos para los filtros seleccionados");
    } finally {
      setLoading(false);
    }
  };

  const handleExportar = async (formato) => {
    setMensaje("");
    try {
      const res = await generarReporte(tipoActivo, f, formato);
      const blob = await res.blob();
      const ext = formato === "PDF" ? "pdf" : formato === "XLSX" ? "xlsx" : "csv";
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `reporte_${tipoActivo}_${new Date().toISOString().slice(0, 10)}.${ext}`;
      a.click();
      URL.revokeObjectURL(url);
    } catch {
      setMensaje("Error al exportar el reporte");
    }
  };

  const limpiar = () => {
    setFiltros(FILTROS_INICIALES);
    setResultado(null);
    setMensaje("");
    setError("");
  };

  return (
    <div className="dashboard">
      <aside className="sidebar">
        <div className="sidebar-header"><h2>Panel RRHH</h2><p>Recursos Humanos</p></div>
        <nav className="sidebar-nav">
          <Link to="/rrhh">🏠 Inicio</Link>
          <Link to="/rrhh/empleados">👥 Gestión de Empleados</Link>
          <Link to="/rrhh/asistencia">📋 Control de Asistencia</Link>
          <Link to="/rrhh/nomina">💰 Cálculo de Nómina</Link>
          <Link to="/rrhh/solicitudes">📅 Solicitudes</Link>
          <Link to="/rrhh/reportes" className="active">📊 Reportes</Link>
          <div className="nav-logout"><Link to="/">🚪 Cerrar Sesión</Link></div>
        </nav>
      </aside>

      <main className="main-content">
        <header className="main-header">
          <h1>Generar Reportes</h1>
          <span>ECU-06</span>
        </header>

        {mensaje && <div className="alert alert-success">{mensaje}</div>}
        {error   && <div className="alert alert-warning">{error}</div>}

        {/* ── TIPO DE REPORTE ── */}
        <div className="section-card">
          <h2>Seleccione el Tipo de Reporte</h2>
          <div className="cards-grid">
            {TIPOS.map((t) => (
              <div
                key={t.key}
                className="card"
                style={{ border: tipoActivo === t.key ? "2px solid #0d6efd" : "2px solid transparent" }}
                onClick={() => { setTipoActivo(t.key); setResultado(null); setMensaje(""); setError(""); }}
              >
                <div className="card-icon">{t.icon}</div>
                <h3>Reporte de {t.label}</h3>
              </div>
            ))}
          </div>
        </div>

        {/* ── FILTROS ── */}
        <div className="section-card">
          <h2>Filtros — Reporte de {TIPOS.find((t) => t.key === tipoActivo)?.label}</h2>
          <form onSubmit={handleGenerar}>
            <div className="form-grid">
              {/* Fechas (asistencia, nomina, solicitudes) */}
              {["asistencia", "nomina", "solicitudes"].includes(tipoActivo) && (
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

              {/* Área (todos) */}
              <div className="form-group">
                <label>Área</label>
                <input placeholder="Todas" value={f.area} onChange={(e) => setF("area", e.target.value)} />
              </div>

              {/* Empleado (todos) */}
              <div className="form-group">
                <label>Empleado (nombre o DNI)</label>
                <input placeholder="Todos" value={f.empleado} onChange={(e) => setF("empleado", e.target.value)} />
              </div>

              {/* Cargo (empleados) */}
              {tipoActivo === "empleados" && (
                <>
                  <div className="form-group">
                    <label>Cargo</label>
                    <input value={f.cargo} onChange={(e) => setF("cargo", e.target.value)} />
                  </div>
                  <div className="form-group">
                    <label>Tipo Contrato</label>
                    <select value={f.tipoContrato} onChange={(e) => setF("tipoContrato", e.target.value)}>
                      <option value="">Todos</option>
                      <option value="PLAZO_FIJO">Plazo fijo</option>
                      <option value="INDEFINIDO">Indefinido</option>
                      <option value="SERVICIOS">Por servicio</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Estado</label>
                    <select value={f.estado} onChange={(e) => setF("estado", e.target.value)}>
                      <option value="">Todos</option>
                      <option value="ACTIVO">Activo</option>
                      <option value="INACTIVO">Inactivo</option>
                    </select>
                  </div>
                </>
              )}

              {/* Tipo pago + rango salarial (nomina) */}
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

              {/* Tipo + estado solicitud */}
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
                    <label>Estado</label>
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
                {loading ? "Generando..." : "Generar Reporte"}
              </button>
              <button type="button" className="btn btn-secondary" onClick={limpiar}>Limpiar Filtros</button>
            </div>
          </form>
        </div>

        {/* ── RESULTADO ── */}
        {resultado && (
          <div className="section-card">
            <h2>Resultado del Reporte</h2>
            {resultado.resumen && (
              <div className="stats-row">
                {Object.entries(resultado.resumen).map(([k, v]) => (
                  <div className="stat-box" key={k}>
                    <div className="stat-value">{v}</div>
                    <div className="stat-label">{k}</div>
                  </div>
                ))}
              </div>
            )}
            {resultado.datos && resultado.datos.length > 0 && (
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
            )}
            <hr className="divider" />
            <h2>Exportar</h2>
            <div className="form-actions">
              <button className="btn btn-danger" onClick={() => handleExportar("PDF")}>📄 PDF</button>
              <button className="btn btn-success" onClick={() => handleExportar("XLSX")}>📊 Excel</button>
              <button className="btn btn-secondary" onClick={() => handleExportar("CSV")}>📑 CSV</button>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
