import { Link } from "react-router-dom";
import "../styles/Dashboard.css";

export default function CalculoNomina() {
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
          <Link to="/rrhh/nomina" className="active">💰 Cálculo de Nómina</Link>
          <Link to="/rrhh/solicitudes">📅 Solicitudes</Link>
          <Link to="/rrhh/reportes">📊 Reportes</Link>
          <div className="nav-logout">
            <Link to="/">🚪 Cerrar Sesión</Link>
          </div>
        </nav>
      </aside>

      <main className="main-content">
        <header className="main-header">
          <h1>Cálculo de Nómina</h1>
          <span>ECU-04 — Procesamiento de Remuneraciones</span>
        </header>

        {/* ── SELECCIÓN DE PERIODO ── */}
        <div className="section-card">
          <h2>Parámetros de Cálculo</h2>
          <div className="form-grid">
            <div className="form-group">
              <label>Fecha Inicio *</label>
              <input type="date" />
            </div>
            <div className="form-group">
              <label>Fecha Fin *</label>
              <input type="date" />
            </div>
            <div className="form-group">
              <label>Filtrar por Área</label>
              <select>
                <option value="">Todos los empleados activos</option>
                <option>Administración</option>
                <option>Operaciones</option>
                <option>Sistemas</option>
              </select>
            </div>
            <div className="form-group">
              <label>Filtrar por Empleado</label>
              <select>
                <option value="">Todos</option>
                <option>García López, Juan</option>
                <option>Ramírez Torres, Ana</option>
              </select>
            </div>
          </div>
          <div className="form-actions">
            <button className="btn btn-primary">Calcular Nómina</button>
            <button className="btn btn-secondary">Limpiar</button>
          </div>
        </div>

        {/* ── RESUMEN NÓMINA ── */}
        <div className="stats-row">
          <div className="stat-box">
            <div className="stat-value">45</div>
            <div className="stat-label">Empleados Procesados</div>
          </div>
          <div className="stat-box">
            <div className="stat-value">S/ 158,400</div>
            <div className="stat-label">Total Bruto</div>
          </div>
          <div className="stat-box">
            <div className="stat-value">S/ 3,200</div>
            <div className="stat-label">Total Descuentos</div>
          </div>
          <div className="stat-box">
            <div className="stat-value">S/ 155,200</div>
            <div className="stat-label">Total Neto a Pagar</div>
          </div>
        </div>

        {/* ── DETALLE NÓMINA ── */}
        <div className="section-card">
          <h2>Detalle de Nómina — Junio 2026</h2>
          <div className="toolbar">
            <input type="text" placeholder="Buscar empleado..." />
            <select>
              <option value="">Estado nómina</option>
              <option>Calculada</option>
              <option>Disponible</option>
            </select>
            <button className="btn btn-secondary">Filtrar</button>
            <span className="badge badge-info" style={{ padding: "6px 14px" }}>
              Estado: Calculada
            </span>
          </div>

          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Empleado</th>
                  <th>Sueldo Base</th>
                  <th>Horas Trabajadas</th>
                  <th>Horas Extra</th>
                  <th>Bonificaciones</th>
                  <th>Descuentos</th>
                  <th>Salario Bruto</th>
                  <th>Salario Neto</th>
                  <th>Detalle</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>García López, Juan</td>
                  <td>S/ 3,500.00</td>
                  <td>176h</td>
                  <td>4h</td>
                  <td>S/ 200.00</td>
                  <td>S/ 30.00</td>
                  <td>S/ 3,790.00</td>
                  <td>S/ 3,760.00</td>
                  <td><button className="btn btn-secondary btn-sm">Ver</button></td>
                </tr>
                <tr>
                  <td>Ramírez Torres, Ana</td>
                  <td>S/ 4,200.00</td>
                  <td>174h</td>
                  <td>0h</td>
                  <td>S/ 350.00</td>
                  <td>S/ 60.00</td>
                  <td>S/ 4,550.00</td>
                  <td>S/ 4,490.00</td>
                  <td><button className="btn btn-secondary btn-sm">Ver</button></td>
                </tr>
                <tr>
                  <td>Flores Mendoza, Carlos</td>
                  <td>S/ 2,800.00</td>
                  <td>180h</td>
                  <td>10h</td>
                  <td>S/ 150.00</td>
                  <td>S/ 0.00</td>
                  <td>S/ 3,100.00</td>
                  <td>S/ 3,100.00</td>
                  <td><button className="btn btn-secondary btn-sm">Ver</button></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        {/* ── DESGLOSE BONIFICACIONES ── */}
        <div className="section-card">
          <h2>Conceptos de Bonificación Aplicados</h2>
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Tipo de Bonificación</th>
                  <th>Descripción</th>
                  <th>Empleados con Beneficio</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>Bonificación Familiar</td>
                  <td>Dinero extra por tener hijos a cargo</td>
                  <td>12</td>
                </tr>
                <tr>
                  <td>Turno Nocturno</td>
                  <td>Pago adicional por trabajar de noche</td>
                  <td>8</td>
                </tr>
                <tr>
                  <td>Guardia</td>
                  <td>Extra por disponibilidad en emergencias</td>
                  <td>5</td>
                </tr>
                <tr>
                  <td>Riesgo Hospitalario</td>
                  <td>Pago por trabajar en zonas de riesgo</td>
                  <td>4</td>
                </tr>
                <tr>
                  <td>Cargo / Productividad</td>
                  <td>Extra por puesto o desempeño destacado</td>
                  <td>10</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        {/* ── ACCIONES FINALES ── */}
        <div className="section-card">
          <h2>Acciones de Nómina</h2>
          <div className="alert alert-success">
            ✅ La nómina ha sido calculada correctamente. Puede proceder a generar los comprobantes.
          </div>
          <div className="form-actions">
            <button className="btn btn-success">Aprobar y Cerrar Nómina</button>
            <button className="btn btn-primary">Generar Comprobantes PDF</button>
            <button className="btn btn-info">Exportar a Excel</button>
            <button className="btn btn-secondary">Recalcular</button>
          </div>
        </div>
      </main>
    </div>
  );
}
