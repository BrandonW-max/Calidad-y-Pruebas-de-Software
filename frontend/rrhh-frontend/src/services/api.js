const BASE = 'http://localhost:8080';

export const getToken  = () => localStorage.getItem('token');
export const getUser   = () => JSON.parse(localStorage.getItem('user') || '{}');
export const saveSession = (token, rol, username) =>
  localStorage.setItem('user', JSON.stringify({ token, rol, username }));
export const clearSession = () => localStorage.removeItem('user');

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

export const revisarJustificacion = (id, decision, comentario = '') =>
  fetch(`${BASE}/api/v1/asistencia/justificar/${id}/revisar`, {
    method: 'PUT', headers: h(), body: JSON.stringify({ decision, comentario }),
  }).then(json);

// ── NÓMINA ────────────────────────────────────────────────────────────────────
export const calcularNomina = (dto) =>
  fetch(`${BASE}/api/nomina/calcular`, {
    method: 'POST', headers: h(), body: JSON.stringify(dto),
  }).then(json);

export const getNominasPorPeriodo = (periodo) =>
  fetch(`${BASE}/api/nomina/periodo/${periodo}`, { headers: h() }).then(json);

export const getNominasPorEmpleado = (idEmpleado) =>
  fetch(`${BASE}/api/nomina/empleado/${idEmpleado}`, { headers: h() }).then(json);

export const enviarComprobante = (id) =>
  fetch(`${BASE}/api/nomina/${id}/enviar`, {
    method: 'POST', headers: h(),
  }).then(json);

export const descargarComprobante = (id) =>
  fetch(`${BASE}/api/nomina/${id}/comprobante`, { headers: h() });

// ── SOLICITUDES ───────────────────────────────────────────────────────────────
export const registrarSolicitud = (dto) =>
  fetch(`${BASE}/api/solicitud/registrar`, {
    method: 'POST', headers: h(), body: JSON.stringify(dto),
  }).then(json);

export const revisarSolicitudRRHH = (idSolicitud, aprobado, observacionRrhh = '') =>
  fetch(`${BASE}/api/solicitud/revisar`, {
    method: 'PUT', headers: h(),
    body: JSON.stringify({ idSolicitud, aprobado, observacionRrhh }),
  }).then(json);

export const decidirSolicitudGerencia = (idSolicitud, aprobado, respuesta = '') =>
  fetch(`${BASE}/api/solicitud/decidir`, {
    method: 'PUT', headers: h(),
    body: JSON.stringify({ idSolicitud, aprobado, respuesta }),
  }).then(json);

export const getSolicitudesPorEstado = (estado) =>
  fetch(`${BASE}/api/solicitud/estado/${estado}`, { headers: h() }).then(json);

export const getSolicitudesPorEmpleado = (idEmpleado) =>
  fetch(`${BASE}/api/solicitud/empleado/${idEmpleado}`, { headers: h() }).then(json);

// ── REPORTES ──────────────────────────────────────────────────────────────────
export const generarReporte = (tipo, filtros, formato) => {
  const params = formato ? `?formato=${formato}` : '';
  return fetch(`${BASE}/api/reporte/${tipo}${params}`, {
    method: 'POST', headers: h(), body: JSON.stringify(filtros),
  });
};
