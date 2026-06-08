import { Link } from "react-router-dom";
import "../styles/Dashboard.css";

export default function ControlAsistencia() {
  return (
    <div className="dashboard">
      <aside className="sidebar">
        <div className="sidebar-header">
          <h2>Panel RRHH</h2>
          <p>Recursos Humanos</p>
        </div>
        <nav className="sidebar-nav">
          <Link to="/rrhh">🏠 Inicio</Link>
          <Link to="/rrhh/empleados">👥 Gestión de Empleados</Link>
          <Link to="/rrhh/asistencia" className="active">📋 Control de Asistencia</Link>
          <Link to="/rrhh/nomina">💰 Cálculo de Nómina</Link>
          <Link to="/rrhh/solicitudes">📅 Solicitudes</Link>
          <Link to="/rrhh/reportes">📊 Reportes</Link>
          <div className="nav-logout">
            <Link to="/">🚪 Cerrar Sesión</Link>
          </div>
        </nav>
      </aside>

      <main className="main-content">
        <header className="main-header">
          <h1>Control de Asistencia</h1>
          <span>ECU-03 — Registro y Seguimiento de Asistencia</span>
        </header>

        {/* ── ESTADO BIOMÉTRICO ── */}
        <div className="alert alert-info">
          🖐️ Dispositivo biométrico conectado y operativo. Los empleados pueden registrar su huella dactilar.
        </div>

        <div className="stats-row">
          <div className="stat-box">
            <div className="stat-value">42</div>
            <div className="stat-label">Asistencias Hoy</div>
          </div>
          <div className="stat-box">
            <div className="stat-value">3</div>
            <div className="stat-label">Tardanzas Hoy</div>
          </div>
          <div className="stat-box">
            <div className="stat-value">2</div>
            <div className="stat-label">Ausentes</div>
          </div>
          <div className="stat-box">
            <div className="stat-value">1</div>
            <div className="stat-label">Justificaciones Pendientes</div>
          </div>
        </div>

        {/* ── FILTROS ── */}
        <div className="section-card">
          <h2>Registros de Asistencia</h2>
          <div className="toolbar">
            <input type="date" />
            <input type="date" />
            <select>
              <option value="">Área</option>
              <option>Administración</option>
              <option>Operaciones</option>
              <option>Sistemas</option>
            </select>
            <input type="text" placeholder="Empleado" />
            <button className="btn btn-primary">Filtrar</button>
            <button className="btn btn-success" style={{ marginLeft: "auto" }}>
              + Registro Manual
            </button>
          </div>

          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Empleado</th>
                  <th>Fecha</th>
                  <th>Entrada</th>
                  <th>Salida</th>
                  <th>Horas Trabajadas</th>
                  <th>Horas Extra</th>
                  <th>Estado</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>García López, Juan</td>
                  <td>08/06/2026</td>
                  <td>08:02</td>
                  <td>17:10</td>
                  <td>9h 08min</td>
                  <td>—</td>
                  <td><span className="badge badge-success">Normal</span></td>
                  <td><button className="btn btn-secondary btn-sm">Ver detalle</button></td>
                </tr>
                <tr>
                  <td>Ramírez Torres, Ana</td>
                  <td>08/06/2026</td>
                  <td>08:25</td>
                  <td>17:00</td>
                  <td>8h 35min</td>
                  <td>—</td>
                  <td><span className="badge badge-warning">Tardanza</span></td>
                  <td>
                    <button className="btn btn-warning btn-sm">Justificar</button>{" "}
                    <button className="btn btn-secondary btn-sm">Ver detalle</button>
                  </td>
                </tr>
                <tr>
                  <td>Flores Mendoza, Carlos</td>
                  <td>08/06/2026</td>
                  <td>07:55</td>
                  <td>19:00</td>
                  <td>11h 05min</td>
                  <td>2h 05min</td>
                  <td><span className="badge badge-info">Horas Extra</span></td>
                  <td><button className="btn btn-secondary btn-sm">Ver detalle</button></td>
                </tr>
                <tr>
                  <td>Vargas Huanca, Luis</td>
                  <td>08/06/2026</td>
                  <td>—</td>
                  <td>—</td>
                  <td>—</td>
                  <td>—</td>
                  <td><span className="badge badge-danger">Ausente</span></td>
                  <td><button className="btn btn-primary btn-sm">Registrar</button></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        {/* ── REGISTRO MANUAL ── */}
        <div className="section-card">
          <h2>Registro Manual de Asistencia (Corrección)</h2>
          <div className="alert alert-warning">
            ⚠️ El registro manual debe estar justificado. Esta acción queda registrada en el log de auditoría.
          </div>
          <div className="form-grid">
            <div className="form-group">
              <label>Empleado</label>
              <select>
                <option value="">Seleccione empleado</option>
                <option>García López, Juan</option>
                <option>Ramírez Torres, Ana</option>
                <option>Flores Mendoza, Carlos</option>
              </select>
            </div>
            <div className="form-group">
              <label>Fecha</label>
              <input type="date" />
            </div>
            <div className="form-group">
              <label>Tipo de Marcación</label>
              <select>
                <option value="">Seleccione</option>
                <option>Entrada</option>
                <option>Salida</option>
              </select>
            </div>
            <div className="form-group">
              <label>Hora</label>
              <input type="time" />
            </div>
            <div className="form-group form-full">
              <label>Motivo de Corrección</label>
              <textarea placeholder="Explique el motivo del registro manual..." />
            </div>
          </div>
          <div className="form-actions">
            <button className="btn btn-success">Guardar Registro Manual</button>
            <button className="btn btn-secondary">Cancelar</button>
          </div>
        </div>

        {/* ── JUSTIFICACIONES PENDIENTES ── */}
        <div className="section-card">
          <h2>Justificaciones de Tardanza Pendientes</h2>
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Empleado</th>
                  <th>Fecha</th>
                  <th>Hora Entrada</th>
                  <th>Minutos de Retraso</th>
                  <th>Motivo</th>
                  <th>Evidencia</th>
                  <th>Estado</th>
                  <th>Decisión</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>Ramírez Torres, Ana</td>
                  <td>07/06/2026</td>
                  <td>08:25</td>
                  <td>25 min</td>
                  <td>Tráfico vehicular</td>
                  <td>
                    <button className="btn btn-secondary btn-sm">Ver foto</button>
                  </td>
                  <td><span className="badge badge-warning">Pendiente</span></td>
                  <td>
                    <button className="btn btn-success btn-sm">Aprobar</button>{" "}
                    <button className="btn btn-danger btn-sm">Rechazar</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </main>
    </div>
  );
}
