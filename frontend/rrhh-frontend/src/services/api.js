const BASE = import.meta.env.VITE_API_URL || '';

export const getToken      = () => localStorage.getItem('token');
export const getUser       = () => JSON.parse(localStorage.getItem('user') || '{}');
export const getIdEmpleado = () => getUser()?.idEmpleado ?? null;
export const saveSession   = (token, rol, username, idEmpleado = null) =>
  localStorage.setItem('user', JSON.stringify({ token, rol, username, idEmpleado }));
export const clearSession  = () => localStorage.removeItem('user');

const json = (r) => {
  if (!r.ok && r.status !== 400 && r.status !== 401) throw new Error(`HTTP ${r.status}`);
  return r.json();
};

const h = () => ({
  'Content-Type': 'application/json',
  Authorization: `Bearer ${getUser().token}`,
});

// ── AUTH ──────────────────────────────────────────────────────────────────────
export const login = (nombreUsuario, password) =>
  fetch(`${BASE}/api/v1/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ nombreUsuario, password }),
  }).then(json);

export const verify2FA = (username, code) =>
  fetch(`${BASE}/api/v1/auth/verify-2fa`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, code }),
  }).then(json);

export const enable2FA = (username) =>
  fetch(`${BASE}/api/v1/auth/enable-2fa/${username}`, { headers: h() })
    .then(r => { if (!r.ok) throw new Error(`HTTP ${r.status}`); return r.blob(); });

// ── DEPARTAMENTOS ─────────────────────────────────────────────────────────────
export const getDepartamentos = () =>
  fetch(`${BASE}/api/v1/departamentos/listar`, { headers: h() }).then(json);

// ── EMPLEADOS ─────────────────────────────────────────────────────────────────
export const listarEmpleados = (page = 0, size = 20) =>
  fetch(`${BASE}/api/v1/empleados/listar?page=${page}&size=${size}`, { headers: h() }).then(json);

export const buscarEmpleados = (filtro, page = 0) =>
  fetch(`${BASE}/api/v1/empleados/buscar?filtro=${encodeURIComponent(filtro)}&page=${page}`, { headers: h() }).then(json);

export const buscarEmpleadosAvanzado = ({ estado, idDpto, cargo } = {}, page = 0) => {
  const p = new URLSearchParams({ page });
  if (estado) p.append('estado', estado);
  if (idDpto) p.append('idDpto', idDpto);
  if (cargo)  p.append('cargo', cargo);
  return fetch(`${BASE}/api/v1/empleados/buscar-avanzado?${p}`, { headers: h() }).then(json);
};

export const registrarEmpleado = (dto) =>
  fetch(`${BASE}/api/v1/empleados/registrar`, {
    method: 'POST', headers: h(), body: JSON.stringify(dto),
  }).then(json);

export const actualizarEmpleado = (id, dto) =>
  fetch(`${BASE}/api/v1/empleados/${id}`, {
    method: 'PUT', headers: h(), body: JSON.stringify(dto),
  }).then(json);

export const desactivarEmpleado = (id, motivo) =>
  fetch(`${BASE}/api/v1/empleados/${id}/desactivar`, {
    method: 'PUT', headers: h(), body: JSON.stringify({ motivo }),
  }).then(json);

export const reactivarEmpleado = (id, motivo) =>
  fetch(`${BASE}/api/v1/empleados/${id}/reactivar`, {
    method: 'PUT', headers: h(), body: JSON.stringify({ motivo }),
  }).then(json);

export const registrarSancion = (id, dto) =>
  fetch(`${BASE}/api/v1/empleados/${id}/sancion`, {
    method: 'PUT', headers: h(), body: JSON.stringify(dto),
  }).then(json);

export const registrarAscenso = (id, dto) =>
  fetch(`${BASE}/api/v1/empleados/${id}/ascenso`, {
    method: 'PUT', headers: h(), body: JSON.stringify(dto),
  }).then(json);

export const cambioSalarial = (id, dto) =>
  fetch(`${BASE}/api/v1/empleados/${id}/cambio-salarial`, {
    method: 'PUT', headers: h(), body: JSON.stringify(dto),
  }).then(json);

// ── ASISTENCIA ────────────────────────────────────────────────────────────────
export const registrarManual = (dto) =>
  fetch(`${BASE}/api/v1/asistencia/manual`, {
    method: 'POST', headers: h(), body: JSON.stringify(dto),
  }).then(json);

export const listarJustificacionesPendientes = () =>
  fetch(`${BASE}/api/v1/asistencia/justificaciones/pendientes`, { headers: h() }).then(json);

export const listarJustificacionesEmpleado = (idEmpleado) =>
  fetch(`${BASE}/api/v1/asistencia/justificaciones/empleado/${idEmpleado}`, { headers: h() }).then(json);

// ECU-03 A4: empleado registra justificación de tardanza
export const registrarJustificacion = (dto) =>
  fetch(`${BASE}/api/v1/asistencia/justificar`, {
    method: 'POST', headers: h(), body: JSON.stringify(dto),
  }).then(json);

export const revisarJustificacion = (id, decision, comentario = '') =>
  fetch(`${BASE}/api/v1/asistencia/justificar/${id}/revisar`, {
    method: 'PUT', headers: h(), body: JSON.stringify({ decision, comentario }),
  }).then(json);

// ── NÓMINA ────────────────────────────────────────────────────────────────────
// ECU-04: calcular nómina por mes y año
export const calcularNomina = (mes, anio) =>
  fetch(`${BASE}/api/nomina/calcular`, {
    method: 'POST', headers: h(), body: JSON.stringify({ mes, anio }),
  }).then(json);

// ECU-04: cargar nóminas de un período ya calculado
export const getNominasPorPeriodo = (mes, anio) =>
  fetch(`${BASE}/api/nomina/periodo/${anio}-${mes}`, { headers: h() }).then(json);

export const getNominasPorEmpleado = (idEmpleado) =>
  fetch(`${BASE}/api/nomina/empleado/${idEmpleado}`, { headers: h() }).then(json);

export const enviarComprobante = (id) =>
  fetch(`${BASE}/api/nomina/${id}/enviar`, {
    method: 'POST', headers: h(),
  }).then(json);

// Returns a Blob for PDF download
export const descargarComprobante = (id) =>
  fetch(`${BASE}/api/nomina/${id}/comprobante`, { headers: h() }).then(r => {
    if (!r.ok) throw new Error(`HTTP ${r.status}`);
    return r.blob();
  });

// ── SOLICITUDES ───────────────────────────────────────────────────────────────
export const registrarSolicitud = (dto) =>
  fetch(`${BASE}/api/solicitud/registrar`, {
    method: 'POST', headers: h(), body: JSON.stringify(dto),
  }).then(json);

// ECU-05: RRHH revisa y envía a Gerencia (decision: "APROBAR" | "DEVOLVER")
export const revisarSolicitudRRHH = (idSolicitud, { decision, comentario } = {}) =>
  fetch(`${BASE}/api/solicitud/revisar`, {
    method: 'PUT', headers: h(),
    body: JSON.stringify({
      idSolicitud,
      aprobado: decision === 'APROBAR',
      observacionRrhh: comentario,
    }),
  }).then(json);

// ECU-05: Gerencia aprueba o rechaza (decision: "APROBAR" | "RECHAZAR")
export const decidirSolicitudGerencia = (idSolicitud, { decision, comentario } = {}) =>
  fetch(`${BASE}/api/solicitud/decidir`, {
    method: 'PUT', headers: h(),
    body: JSON.stringify({
      idSolicitud,
      aprobado: decision === 'APROBAR',
      respuesta: comentario,
    }),
  }).then(json);

export const getSolicitudesPorEstado = (estado) =>
  fetch(`${BASE}/api/solicitud/estado/${estado}`, { headers: h() }).then(json);

export const getSolicitudesPorEmpleado = (idEmpleado) =>
  fetch(`${BASE}/api/solicitud/empleado/${idEmpleado}`, { headers: h() }).then(json);

// ── REPORTES ──────────────────────────────────────────────────────────────────
// ECU-06: params = { tipo, formato, mes, anio, fechaInicio, fechaFin, estado, ... }
export const generarReporte = (params) => {
  const { tipo, formato, ...filtros } = params;
  const query = formato ? `?formato=${encodeURIComponent(formato)}` : '';
  return fetch(`${BASE}/api/reporte/${tipo}${query}`, {
    method: 'POST', headers: h(), body: JSON.stringify(filtros),
  });
};
