// LOCAL: usa tu IP (ipconfig → IPv4), ej: 'http://192.168.1.100:8080'
// AZURE: usa la URL pública del backend, ej: 'https://mi-backend.azurewebsites.net'
const BASE_URL = process.env.EXPO_PUBLIC_API_URL ?? 'http://192.168.1.100:8080';

export const getEstadoAsistenciaHoy = async (idEmpleado, deviceToken) => {
  const res = await fetch(
    `${BASE_URL}/api/v1/asistencia/biometrico/estado/${idEmpleado}?deviceToken=${encodeURIComponent(deviceToken)}`
  );
  if (!res.ok) throw new Error(`Error ${res.status}`);
  return res.json();
};

export const buscarEmpleadoPorDni = async (numeroDi) => {
  const res = await fetch(`${BASE_URL}/api/dispositivos/dni/${numeroDi}`);
  if (res.status === 404) throw new Error('DNI no encontrado. Verifica tu número de documento.');
  if (res.status === 400) throw new Error('Empleado inactivo o desvinculado.');
  if (!res.ok) throw new Error(`Error ${res.status}`);
  return res.json();
};

export const registrarDispositivo = async (numeroDi, deviceToken) => {
  const res = await fetch(`${BASE_URL}/api/dispositivos/registrar`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ numeroDi, deviceToken }),
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `HTTP ${res.status}`);
  }
  return res.json();
};

export const registrarAsistenciaBiometrica = async (idEmpleado, deviceToken, tipo) => {
  const res = await fetch(`${BASE_URL}/api/v1/asistencia/biometrico`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ idEmpleado, deviceToken, tipo }),
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `HTTP ${res.status}`);
  }
  return res.json();
};
