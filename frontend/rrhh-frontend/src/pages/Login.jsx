import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/Login.css";
import logo from "../assets/logo.png";
import TwoFA from "./TwoFA";
import { login } from "../services/api";

export default function Login() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [mensaje, setMensaje] = useState("");
  const [show2FA, setShow2FA] = useState(false);
  const [resetTwoFAKey, setResetTwoFAKey] = useState(0);

  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setMensaje("");

    try {
      const data = await login(username, password);

      if (!data.success) {
        setMensaje(data.message || "Credenciales inválidas, intente nuevamente");
        return;
      }

      if (data.requires2FA) {
        setResetTwoFAKey((prev) => prev + 1);
        setShow2FA(true);
        return;
      }

      redirectByRole(data.rol);
    } catch (err) {
      setMensaje("Error de conexión con el servidor");
    }
  };

  const redirectByRole = (role) => {
    if (role === "GERENCIA") navigate("/gerencia");
    else if (role === "RRHH") navigate("/rrhh");
    else if (role === "EMPLEADO") navigate("/empleado");
    else setMensaje("Rol no válido");
  };

  return (
    <div className="container">
      <div className="left-panel">
        <img src={logo} className="logo-img" />
        <p className="secure">Sistema seguro RRHH</p>
        <p className="roles">Empleado | RRHH | Gerencia</p>
      </div>

      <div className="center-panel">
        <h2>Iniciar Sesión</h2>
        <form onSubmit={handleLogin}>
          <input
            placeholder="Usuario"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
          />
          <input
            type="password"
            placeholder="Contraseña"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
          <button type="submit">Entrar</button>
        </form>
        {mensaje && <p className="error">{mensaje}</p>}
      </div>

      {show2FA && (
        <TwoFA
          key={resetTwoFAKey}
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
