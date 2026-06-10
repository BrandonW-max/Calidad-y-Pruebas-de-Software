import { useState, useEffect } from "react";
import "../styles/Login.css";
import logo from "../assets/logo.png";
import TwoFA from "./TwoFA";
import { login, saveSession, clearSession } from "../services/api";

export default function Login() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [mensaje, setMensaje]   = useState("");
  const [show2FA, setShow2FA]   = useState(false);
  const [resetKey, setResetKey] = useState(0);

  const [showForgot, setShowForgot] = useState(false);
  const [forgotInput, setForgotInput] = useState("");
  const [forgotOk, setForgotOk]     = useState(false);

  const handleForgot = (e) => {
    e.preventDefault();
    setForgotOk(true);
  };

  const cerrarForgot = () => { setShowForgot(false); setForgotInput(""); setForgotOk(false); };

  useEffect(() => { clearSession(); }, []);

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
      saveSession(data.token, data.rol, username);
      redirectByRole(data.rol);
    } catch {
      setMensaje("Error de conexión con el servidor. Verifique que el servicio esté disponible.");
    }
  };

  const redirectByRole = (role) => {
    if (role === "GERENCIA") window.location.href = "/gerencia";
    else if (role === "RRHH") window.location.href = "/rrhh";
    else if (role === "EMPLEADO") window.location.href = "/empleado";
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

          <p className="login-forgot">
            <button type="button" className="login-forgot-btn" onClick={() => setShowForgot(true)}>
              ¿Olvidó su contraseña?
            </button>
          </p>

          {mensaje && (
            <div className="login-error">
              ⚠️ {mensaje}
            </div>
          )}
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

      {showForgot && (
        <div className="login-modal-overlay" onClick={(e) => e.target === e.currentTarget && cerrarForgot()}>
          <div className="login-modal-box">
            <h3>🔑 Recuperar Contraseña</h3>
            {!forgotOk ? (
              <>
                <p>Ingrese su usuario o correo electrónico registrado. Le enviaremos las instrucciones de recuperación.</p>
                <form onSubmit={handleForgot}>
                  <div className="login-field">
                    <label>Usuario o correo</label>
                    <input
                      type="text"
                      placeholder="Ingrese su usuario o correo"
                      value={forgotInput}
                      onChange={(e) => setForgotInput(e.target.value)}
                      required
                      autoFocus
                    />
                  </div>
                  <div className="login-modal-actions">
                    <button type="button" className="login-modal-btn-secondary" onClick={cerrarForgot}>
                      Cancelar
                    </button>
                    <button type="submit" className="login-btn" style={{ margin: 0 }}>
                      Enviar instrucciones
                    </button>
                  </div>
                </form>
              </>
            ) : (
              <>
                <div className="login-modal-success">
                  ✅ Si su usuario está registrado en el sistema, recibirá las instrucciones de recuperación en el correo electrónico asociado. Por favor revise su bandeja de entrada.
                </div>
                <div className="login-modal-actions">
                  <button className="login-btn" style={{ margin: 0 }} onClick={cerrarForgot}>
                    Cerrar
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
