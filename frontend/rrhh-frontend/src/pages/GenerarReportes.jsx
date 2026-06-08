import { Link } from "react-router-dom";
import "../styles/Dashboard.css";

export default function GenerarReportes() {
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
          <Link to="/rrhh/solicitudes">📅 Solicitudes</Link>
          <Link to="/rrhh/reportes" className="active">📊 Reportes</Link>
          <div className="nav-logout">
            <Link to="/">🚪 Cerrar Sesión</Link>
          </div>
        </nav>
      </aside>

      <main className="main-content">
        <header className="main-header">
          <h1>Generar Reportes</h1>
          <span>ECU-06 — Consulta y Exportación de Información</span>
        </header>

        {/* ── SELECCIÓN DE TIPO ── */}
        <div className="section-card">
          <h2>Seleccione el Tipo de Reporte</h2>
          <div className="cards-grid">
            <div className="card">
              <div className="card-icon">👥</div>
              <h3>Reporte de Empleados</h3>
              <p>Listado y datos del personal</p>
            </div>
            <div className="card">
              <div className="card-icon">📋</div>
              <h3>Reporte de Asistencia</h3>
              <p>Registros, tardanzas y justificaciones</p>
            </div>
            <div className="card">
              <div className="card-icon">💰</div>
              <h3>Reporte de Nómina</h3>
              <p>Detalle de pagos y descuentos</p>
            </div>
            <div className="card">
              <div className="card-icon">📅</div>
              <h3>Reporte de Solicitudes</h3>
              <p>Permisos, vacaciones y estados</p>
            </div>
          </div>
        </div>

        {/* ── FILTROS REPORTE EMPLEADOS ── */}
        <div className="section-card">
          <h2>Filtros — Reporte de Empleados</h2>
          <div className="form-grid">
            <div className="form-group">
              <label>Área</label>
              <select>
                <option value="">Todas</option>
                <option>Administración</option>
                <option>Operaciones</option>
                <option>Sistemas</option>
              </select>
            </div>
            <div className="form-group">
              <label>Cargo</label>
              <select>
                <option value="">Todos</option>
                <option>Analista</option>
                <option>Técnico</option>
                <option>Supervisor</option>
              </select>
            </div>
            <div className="form-group">
              <label>Tipo de Contrato</label>
              <select>
                <option value="">Todos</option>
                <option>Plazo fijo</option>
                <option>Indefinido</option>
                <option>Por servicio</option>
              </select>
            </div>
            <div className="form-group">
              <label>Estado</label>
              <select>
                <option value="">Todos</option>
                <option>Activo</option>
                <option>Inactivo</option>
              </select>
            </div>
            <div className="form-group">
              <label>Nivel Jerárquico</label>
              <select>
                <option value="">Todos</option>
                <option>Operativo</option>
                <option>Mando medio</option>
                <option>Directivo</option>
              </select>
            </div>
            <div className="form-group">
              <label>Empleado específico</label>
              <input type="text" placeholder="Nombre o DNI" />
            </div>
          </div>
          <div className="form-actions">
            <button className="btn btn-primary">Generar Reporte</button>
            <button className="btn btn-secondary">Limpiar Filtros</button>
          </div>
        </div>

        {/* ── FILTROS REPORTE ASISTENCIA ── */}
        <div className="section-card">
          <h2>Filtros — Reporte de Asistencia</h2>
          <div className="form-grid">
            <div className="form-group">
              <label>Fecha Inicio</label>
              <input type="date" />
            </div>
            <div className="form-group">
              <label>Fecha Fin</label>
              <input type="date" />
            </div>
            <div className="form-group">
              <label>Área</label>
              <select>
                <option value="">Todas</option>
                <option>Administración</option>
                <option>Operaciones</option>
                <option>Sistemas</option>
              </select>
            </div>
            <div className="form-group">
              <label>Empleado</label>
              <input type="text" placeholder="Nombre o DNI" />
            </div>
          </div>
          <div className="form-actions">
            <button className="btn btn-primary">Generar Reporte</button>
            <button className="btn btn-secondary">Limpiar Filtros</button>
          </div>
        </div>

        {/* ── FILTROS REPORTE NÓMINA ── */}
        <div className="section-card">
          <h2>Filtros — Reporte de Nómina</h2>
          <div className="form-grid">
            <div className="form-group">
              <label>Fecha Inicio</label>
              <input type="date" />
            </div>
            <div className="form-group">
              <label>Fecha Fin</label>
              <input type="date" />
            </div>
            <div className="form-group">
              <label>Área</label>
              <select>
                <option value="">Todas</option>
                <option>Administración</option>
                <option>Operaciones</option>
                <option>Sistemas</option>
              </select>
            </div>
            <div className="form-group">
              <label>Empleado</label>
              <input type="text" placeholder="Nombre o DNI" />
            </div>
            <div className="form-group">
              <label>Tipo de Pago</label>
              <select>
                <option value="">Todos</option>
                <option>Mensual</option>
                <option>Quincenal</option>
              </select>
            </div>
            <div className="form-group">
              <label>Rango Salarial</label>
              <select>
                <option value="">Todos</option>
                <option>S/ 0 – 2,000</option>
                <option>S/ 2,001 – 4,000</option>
                <option>S/ 4,001+</option>
              </select>
            </div>
          </div>
          <div className="form-actions">
            <button className="btn btn-primary">Generar Reporte</button>
            <button className="btn btn-secondary">Limpiar Filtros</button>
          </div>
        </div>

        {/* ── FILTROS REPORTE SOLICITUDES ── */}
        <div className="section-card">
          <h2>Filtros — Reporte de Solicitudes</h2>
          <div className="form-grid">
            <div className="form-group">
              <label>Fecha Inicio</label>
              <input type="date" />
            </div>
            <div className="form-group">
              <label>Fecha Fin</label>
              <input type="date" />
            </div>
            <div className="form-group">
              <label>Área</label>
              <select>
                <option value="">Todas</option>
                <option>Administración</option>
                <option>Operaciones</option>
                <option>Sistemas</option>
              </select>
            </div>
            <div className="form-group">
              <label>Empleado</label>
              <input type="text" placeholder="Nombre o DNI" />
            </div>
            <div className="form-group">
              <label>Tipo de Solicitud</label>
              <select>
                <option value="">Todos</option>
                <option>Vacaciones</option>
                <option>Permiso personal</option>
                <option>Permiso médico</option>
              </select>
            </div>
            <div className="form-group">
              <label>Estado</label>
              <select>
                <option value="">Todos</option>
                <option>Pendiente</option>
                <option>Aprobada</option>
                <option>Rechazada</option>
              </select>
            </div>
          </div>
          <div className="form-actions">
            <button className="btn btn-primary">Generar Reporte</button>
            <button className="btn btn-secondary">Limpiar Filtros</button>
          </div>
        </div>

        {/* ── RESULTADO DEL REPORTE ── */}
        <div className="section-card">
          <h2>Resultado del Reporte</h2>

          <div className="stats-row">
            <div className="stat-box">
              <div className="stat-value">48</div>
              <div className="stat-label">Total Registros</div>
            </div>
            <div className="stat-box">
              <div className="stat-value">45</div>
              <div className="stat-label">Activos</div>
            </div>
            <div className="stat-box">
              <div className="stat-value">3</div>
              <div className="stat-label">Inactivos</div>
            </div>
            <div className="stat-box">
              <div className="stat-value">4.2 años</div>
              <div className="stat-label">Antigüedad Prom.</div>
            </div>
          </div>

          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>DNI</th>
                  <th>Apellidos y Nombres</th>
                  <th>Área</th>
                  <th>Cargo</th>
                  <th>Contrato</th>
                  <th>Estado</th>
                  <th>Antigüedad</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>12345678</td>
                  <td>García López, Juan</td>
                  <td>Sistemas</td>
                  <td>Analista</td>
                  <td>Indefinido</td>
                  <td><span className="badge badge-success">Activo</span></td>
                  <td>3 años 2 meses</td>
                </tr>
                <tr>
                  <td>87654321</td>
                  <td>Ramírez Torres, Ana</td>
                  <td>Administración</td>
                  <td>Supervisora</td>
                  <td>Indefinido</td>
                  <td><span className="badge badge-success">Activo</span></td>
                  <td>6 años 8 meses</td>
                </tr>
                <tr>
                  <td>11223344</td>
                  <td>Flores Mendoza, Carlos</td>
                  <td>Operaciones</td>
                  <td>Técnico</td>
                  <td>Plazo fijo</td>
                  <td><span className="badge badge-danger">Inactivo</span></td>
                  <td>1 año 0 meses</td>
                </tr>
              </tbody>
            </table>
          </div>

          <hr className="divider" />

          <h2>Exportar Reporte</h2>
          <div className="form-actions">
            <button className="btn btn-danger">📄 Exportar PDF</button>
            <button className="btn btn-success">📊 Exportar Excel</button>
            <button className="btn btn-secondary">📑 Exportar CSV</button>
          </div>
        </div>
      </main>
    </div>
  );
}
