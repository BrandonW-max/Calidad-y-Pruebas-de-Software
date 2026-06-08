import { Link } from "react-router-dom";
import "../styles/Dashboard.css";

export default function SolicitudPermisos() {
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
          <Link to="/rrhh/asistencia">📋 Control de Asistencia</Link>
          <Link to="/rrhh/nomina">💰 Cálculo de Nómina</Link>
          <Link to="/rrhh/solicitudes" className="active">📅 Solicitudes</Link>
          <Link to="/rrhh/reportes">📊 Reportes</Link>
          <div className="nav-logout">
            <Link to="/">🚪 Cerrar Sesión</Link>
          </div>
        </nav>
      </aside>

      <main className="main-content">
        <header className="main-header">
          <h1>Solicitud de Permisos y Vacaciones</h1>
          <span>ECU-05 — Gestión de Solicitudes</span>
        </header>

        {/* ── TABS ── */}
        <div className="tab-bar">
          <button className="tab active">Solicitudes Pendientes (RRHH)</button>
          <button className="tab">Nueva Solicitud (Empleado)</button>
          <button className="tab">Decisión Gerencia</button>
          <button className="tab">Historial</button>
        </div>

        {/* ══════════════ VISTA RRHH — PENDIENTES ══════════════ */}
        <div className="section-card">
          <h2>Solicitudes en Estado Pendiente</h2>
          <div className="toolbar">
            <select>
              <option value="">Tipo de Solicitud</option>
              <option>Vacaciones</option>
              <option>Permiso personal</option>
              <option>Permiso médico</option>
            </select>
            <input type="text" placeholder="Buscar empleado..." />
            <button className="btn btn-primary">Filtrar</button>
          </div>

          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Empleado</th>
                  <th>Área</th>
                  <th>Tipo</th>
                  <th>Fecha Inicio</th>
                  <th>Fecha Fin</th>
                  <th>Días</th>
                  <th>Motivo</th>
                  <th>Estado</th>
                  <th>Acción RRHH</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>García López, Juan</td>
                  <td>Sistemas</td>
                  <td>Vacaciones</td>
                  <td>15/06/2026</td>
                  <td>25/06/2026</td>
                  <td>10</td>
                  <td>Descanso anual</td>
                  <td><span className="badge badge-gray">Pendiente</span></td>
                  <td>
                    <button className="btn btn-success btn-sm">Enviar a Gerencia</button>{" "}
                    <button className="btn btn-warning btn-sm">Observar</button>
                  </td>
                </tr>
                <tr>
                  <td>Torres Vega, María</td>
                  <td>Administración</td>
                  <td>Permiso médico</td>
                  <td>09/06/2026</td>
                  <td>09/06/2026</td>
                  <td>1</td>
                  <td>Cita médica</td>
                  <td><span className="badge badge-gray">Pendiente</span></td>
                  <td>
                    <button className="btn btn-success btn-sm">Enviar a Gerencia</button>{" "}
                    <button className="btn btn-warning btn-sm">Observar</button>
                  </td>
                </tr>
                <tr>
                  <td>Paredes Quispe, Luis</td>
                  <td>Operaciones</td>
                  <td>Permiso personal</td>
                  <td>20/06/2026</td>
                  <td>20/06/2026</td>
                  <td>1</td>
                  <td>Trámite notarial</td>
                  <td><span className="badge badge-warning">Observado</span></td>
                  <td>
                    <button className="btn btn-secondary btn-sm">Ver detalle</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        {/* ══════════════ FORMULARIO NUEVA SOLICITUD (EMPLEADO) ══════════════ */}
        <div className="section-card">
          <h2>Nueva Solicitud de Permiso o Vacaciones</h2>

          <div className="alert alert-info">
            ℹ️ Saldo de vacaciones disponible: <strong>15 días</strong>
          </div>

          <div className="form-grid">
            <div className="form-group">
              <label>Tipo de Solicitud *</label>
              <select>
                <option value="">Seleccione</option>
                <option>Vacaciones</option>
                <option>Permiso personal</option>
                <option>Permiso médico</option>
                <option>Otros</option>
              </select>
            </div>
            <div className="form-group">
              <label>Fecha de Inicio *</label>
              <input type="date" />
            </div>
            <div className="form-group">
              <label>Fecha de Fin *</label>
              <input type="date" />
            </div>
            <div className="form-group">
              <label>Días Totales</label>
              <input type="text" value="— días" readOnly style={{ background: "#f9f9f9" }} />
            </div>
            <div className="form-group form-full">
              <label>Motivo / Detalle *</label>
              <textarea placeholder="Describa el motivo de su solicitud..." />
            </div>
          </div>

          <div className="form-actions">
            <button className="btn btn-primary">Registrar Solicitud</button>
            <button className="btn btn-secondary">Cancelar</button>
          </div>
        </div>

        {/* ══════════════ VISTA GERENCIA ══════════════ */}
        <div className="section-card">
          <h2>Solicitudes Enviadas a Gerencia</h2>
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Empleado</th>
                  <th>Cargo</th>
                  <th>Tipo</th>
                  <th>Fechas</th>
                  <th>Días</th>
                  <th>Saldo Disponible</th>
                  <th>Motivo</th>
                  <th>Estado</th>
                  <th>Decisión Gerencia</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>Ramírez Torres, Ana</td>
                  <td>Supervisora</td>
                  <td>Vacaciones</td>
                  <td>10/06 – 20/06/2026</td>
                  <td>10</td>
                  <td>18 días</td>
                  <td>Vacaciones anuales</td>
                  <td><span className="badge badge-purple">Enviado a Gerencia</span></td>
                  <td>
                    <button className="btn btn-success btn-sm">Aprobar</button>{" "}
                    <button className="btn btn-danger btn-sm">Rechazar</button>
                  </td>
                </tr>
                <tr>
                  <td>García López, Juan</td>
                  <td>Analista</td>
                  <td>Permiso médico</td>
                  <td>08/06/2026</td>
                  <td>1</td>
                  <td>15 días</td>
                  <td>Control médico anual</td>
                  <td><span className="badge badge-purple">Enviado a Gerencia</span></td>
                  <td>
                    <button className="btn btn-success btn-sm">Aprobar</button>{" "}
                    <button className="btn btn-danger btn-sm">Rechazar</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          {/* Formulario motivo de rechazo */}
          <hr className="divider" />
          <h2>Motivo de Rechazo</h2>
          <div className="form-grid">
            <div className="form-group form-full">
              <label>Ingrese el motivo del rechazo *</label>
              <textarea placeholder="Explique el motivo por el cual se rechaza la solicitud..." />
            </div>
          </div>
          <div className="form-actions">
            <button className="btn btn-danger">Confirmar Rechazo</button>
            <button className="btn btn-secondary">Cancelar</button>
          </div>
        </div>

        {/* ══════════════ HISTORIAL ══════════════ */}
        <div className="section-card">
          <h2>Historial de Solicitudes</h2>
          <div className="toolbar">
            <input type="date" />
            <input type="date" />
            <select>
              <option value="">Estado</option>
              <option>Aprobada</option>
              <option>Rechazada</option>
              <option>Procesada</option>
            </select>
            <button className="btn btn-primary">Filtrar</button>
          </div>
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Empleado</th>
                  <th>Tipo</th>
                  <th>Fechas</th>
                  <th>Días</th>
                  <th>Estado</th>
                  <th>Resultado</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>Torres Vega, María</td>
                  <td>Vacaciones</td>
                  <td>01/05 – 10/05/2026</td>
                  <td>9</td>
                  <td><span className="badge badge-success">Aprobada</span></td>
                  <td>Aprobado por Gerencia</td>
                </tr>
                <tr>
                  <td>Paredes Quispe, Luis</td>
                  <td>Permiso personal</td>
                  <td>15/04/2026</td>
                  <td>1</td>
                  <td><span className="badge badge-danger">Rechazada</span></td>
                  <td>Sin justificación suficiente</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </main>
    </div>
  );
}
