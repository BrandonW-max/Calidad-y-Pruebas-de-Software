import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/Login.css";
import logo from "../assets/logo.png";
import TwoFA from "./TwoFA";
import { login } from "../services/api";

export default function Login() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [mensaje, setMensaje]   = useState("");
  const [show2FA, setShow2FA]   = useState(false);
  const [resetKey, setResetKey] = useState(0);

  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setMensaje("");
    try {
      const data = await login(username, password);
      if (!data.success) {
        setMensaje(data.message || "Credenciales inválidas. Intente nuevamente.");
        return;
      }
      if (data.requires2FA) {
        setResetKey((prev) => prev + 1);
        setShow2FA(true);
        return;
      }
      redirectByRole(data.rol);
    } catch {
      setMensaje("Error de conexión con el servidor. Verifique que el servicio esté disponible.");
    }
  };

  const redirectByRole = (role) => {
    if (role === "GERENCIA") navigate("/gerencia");
    else if (role === "RRHH") navigate("/rrhh");
    else if (role === "EMPLEADO") navigate("/empleado");
    else setMensaje("Rol de usuario no reconocido.");
  };

  return (
    <div className="login-page">
      <div className="login-card">
        {/* ── PANEL IZQUIERDO ── */}
        <div className="login-left">
          <img src={logo} className="logo-img" alt="Logo del sistema" />
          <p className="login-tagline">Sistema de Gestión de Recursos Humanos</p>
          <p className="login-roles">Empleado · RRHH · Gerencia</p>
          <div className="login-features">
            <div className="login-feature">🔒 Autenticación segura con verificación 2FA</div>
            <div className="login-feature">👥 Control de acceso por roles</div>
            <div className="login-feature">🖐️ Asistencia biométrica integrada</div>
            <div className="login-feature">📊 Reportes y nómina en tiempo real</div>
          </div>
        </div>

        {/* ── PANEL DERECHO ── */}
        <div className="login-right">
          <h2>Iniciar Sesión</h2>
          <p className="login-subtitle">Ingrese sus credenciales para acceder al sistema</p>

          <form className="login-form" onSubmit={handleLogin}>
            <div className="login-field">
              <label>Usuario o correo</label>
              <input
                type="text"
                placeholder="Ingrese su usuario"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                autoComplete="username"
                required
              />
            </div>
            <div className="login-field">
              <label>Contraseña</label>
              <input
                type="password"
                placeholder="Ingrese su contraseña"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete="current-password"
                required
              />
            </div>
            <button type="submit" className="login-btn">Entrar al Sistema</button>
          </form>

          {mensaje && (
            <div className="login-error">
              ⚠️ {mensaje}
            </div>
          )}

          <p className="login-security">
            🔐 Conexión cifrada — Sus datos están protegidos
          </p>
        </div>
      </div>

      {show2FA && (
        <TwoFA
          key={resetKey}
          open={show2FA}
          username={username}
          onClose={() => setShow2FA(false)}
          onSuccess={(rol) => {
            setShow2FA(false);
            redirectByRole(rol);
          }}
        />
      )}
    </div>
  );
}
