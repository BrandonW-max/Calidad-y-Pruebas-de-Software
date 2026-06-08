import { Link } from "react-router-dom";
import "../styles/Dashboard.css";

export default function GestionEmpleados() {
  return (
    <div className="dashboard">
      <aside className="sidebar">
        <div className="sidebar-header">
          <h2>Panel RRHH</h2>
          <p>Recursos Humanos</p>
        </div>
        <nav className="sidebar-nav">
          <Link to="/rrhh">🏠 Inicio</Link>
          <Link to="/rrhh/empleados" className="active">👥 Gestión de Empleados</Link>
          <Link to="/rrhh/asistencia">📋 Control de Asistencia</Link>
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
          <h1>Gestión de Empleados</h1>
          <span>ECU-02 — Administración de Personal</span>
        </header>

        {/* ── BÚSQUEDA ── */}
        <div className="section-card">
          <h2>Buscar Empleado</h2>
          <div className="toolbar">
            <input type="text" placeholder="DNI o nombre" />
            <select>
              <option value="">Área</option>
              <option>Administración</option>
              <option>Operaciones</option>
              <option>Sistemas</option>
            </select>
            <select>
              <option value="">Cargo</option>
              <option>Analista</option>
              <option>Técnico</option>
              <option>Supervisor</option>
            </select>
            <select>
              <option value="">Estado</option>
              <option>Activo</option>
              <option>Inactivo</option>
            </select>
            <button className="btn btn-primary">Buscar</button>
            <button className="btn btn-success" style={{ marginLeft: "auto" }}>
              + Nuevo Empleado
            </button>
          </div>

          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>DNI</th>
                  <th>Apellidos y Nombres</th>
                  <th>Área</th>
                  <th>Cargo</th>
                  <th>Sueldo</th>
                  <th>Estado</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>12345678</td>
                  <td>García López, Juan</td>
                  <td>Sistemas</td>
                  <td>Analista</td>
                  <td>S/ 3,500.00</td>
                  <td><span className="badge badge-success">Activo</span></td>
                  <td>
                    <button className="btn btn-warning btn-sm">Editar</button>{" "}
                    <button className="btn btn-danger btn-sm">Desactivar</button>{" "}
                    <button className="btn btn-secondary btn-sm">Sanción</button>{" "}
                    <button className="btn btn-info btn-sm">Ascenso</button>
                  </td>
                </tr>
                <tr>
                  <td>87654321</td>
                  <td>Ramírez Torres, Ana</td>
                  <td>Administración</td>
                  <td>Supervisora</td>
                  <td>S/ 4,200.00</td>
                  <td><span className="badge badge-success">Activo</span></td>
                  <td>
                    <button className="btn btn-warning btn-sm">Editar</button>{" "}
                    <button className="btn btn-danger btn-sm">Desactivar</button>{" "}
                    <button className="btn btn-secondary btn-sm">Sanción</button>{" "}
                    <button className="btn btn-info btn-sm">Ascenso</button>
                  </td>
                </tr>
                <tr>
                  <td>11223344</td>
                  <td>Flores Mendoza, Carlos</td>
                  <td>Operaciones</td>
                  <td>Técnico</td>
                  <td>S/ 2,800.00</td>
                  <td><span className="badge badge-danger">Inactivo</span></td>
                  <td>
                    <button className="btn btn-secondary btn-sm" disabled>Editar</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        {/* ── FORMULARIO REGISTRAR EMPLEADO ── */}
        <div className="section-card">
          <h2>Registrar Nuevo Empleado</h2>
          <div className="form-grid">
            <div className="form-group">
              <label>DNI / Carnet de Extranjería *</label>
              <input type="text" placeholder="Ej: 12345678" />
            </div>
            <div className="form-group">
              <label>Nombres *</label>
              <input type="text" placeholder="Nombres" />
            </div>
            <div className="form-group">
              <label>Apellidos *</label>
              <input type="text" placeholder="Apellidos" />
            </div>
            <div className="form-group">
              <label>Fecha de Nacimiento</label>
              <input type="date" />
            </div>
            <div className="form-group">
              <label>Área *</label>
              <select>
                <option value="">Seleccione</option>
                <option>Administración</option>
                <option>Operaciones</option>
                <option>Sistemas</option>
              </select>
            </div>
            <div className="form-group">
              <label>Cargo *</label>
              <select>
                <option value="">Seleccione</option>
                <option>Analista</option>
                <option>Técnico</option>
                <option>Supervisor</option>
              </select>
            </div>
            <div className="form-group">
              <label>Sueldo Base *</label>
              <input type="number" placeholder="S/ 0.00" />
            </div>
            <div className="form-group">
              <label>Tipo de Contrato</label>
              <select>
                <option value="">Seleccione</option>
                <option>Plazo fijo</option>
                <option>Indefinido</option>
                <option>Por servicio</option>
              </select>
            </div>
            <div className="form-group">
              <label>Email</label>
              <input type="email" placeholder="correo@empresa.com" />
            </div>
            <div className="form-group">
              <label>Teléfono</label>
              <input type="text" placeholder="Ej: 987654321" />
            </div>
          </div>
          <div className="form-actions">
            <button className="btn btn-success">Registrar Empleado</button>
            <button className="btn btn-secondary">Cancelar</button>
          </div>
        </div>

        {/* ── REGISTRAR SANCIÓN ── */}
        <div className="section-card">
          <h2>Registrar Sanción</h2>
          <div className="form-grid">
            <div className="form-group">
              <label>Empleado</label>
              <select>
                <option value="">Seleccione empleado</option>
                <option>García López, Juan</option>
                <option>Ramírez Torres, Ana</option>
              </select>
            </div>
            <div className="form-group">
              <label>Tipo de Sanción</label>
              <select>
                <option value="">Seleccione</option>
                <option>Amonestación verbal</option>
                <option>Amonestación escrita</option>
                <option>Suspensión</option>
              </select>
            </div>
            <div className="form-group">
              <label>Días de Suspensión</label>
              <input type="number" placeholder="0" min="0" />
            </div>
            <div className="form-group form-full">
              <label>Justificación *</label>
              <textarea placeholder="Describa el motivo de la sanción..." />
            </div>
          </div>
          <div className="form-actions">
            <button className="btn btn-danger">Registrar Sanción</button>
            <button className="btn btn-secondary">Cancelar</button>
          </div>
        </div>

        {/* ── REGISTRAR ASCENSO / CAMBIO SALARIAL ── */}
        <div className="section-card">
          <h2>Registrar Ascenso / Cambio Salarial</h2>
          <div className="form-grid">
            <div className="form-group">
              <label>Empleado</label>
              <select>
                <option value="">Seleccione empleado</option>
                <option>García López, Juan</option>
                <option>Ramírez Torres, Ana</option>
              </select>
            </div>
            <div className="form-group">
              <label>Nuevo Cargo</label>
              <input type="text" placeholder="Ingrese nuevo cargo" />
            </div>
            <div className="form-group">
              <label>Nuevo Sueldo</label>
              <input type="number" placeholder="S/ 0.00" />
            </div>
            <div className="form-group form-full">
              <label>Observaciones</label>
              <textarea placeholder="Observaciones del cambio..." />
            </div>
          </div>
          <div className="form-actions">
            <button className="btn btn-primary">Guardar Cambio</button>
            <button className="btn btn-secondary">Cancelar</button>
          </div>
        </div>
      </main>
    </div>
  );
}
