import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/Login.css";
import logo from "../assets/logo.png";
import TwoFA from "./TwoFA";

export default function Login() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [mensaje, setMensaje] = useState("");
  const [show2FA, setShow2FA] = useState(false);
  const [rol, setRol] = useState("");
  const [resetTwoFAKey, setResetTwoFAKey] = useState(0); // 👈 NUEVO

  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setMensaje("");

    try {
      const res = await fetch("http://localhost:8080/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password })
      });

      const data = await res.json();

      console.log("LOGIN RESPONSE:", data);

      if (!data.success) {
        setMensaje(data.message || "Error en login");
        return;
      }

      if (data.require2FA) {
        setRol(data.rol);
        setResetTwoFAKey(prev => prev + 1); // 🔑 fuerza remontaje del modal
        setShow2FA(true);
        return;
      }

      redirectByRole(data.rol);

    } catch (error) {
      console.error("Error login:", error);
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
          onClose={() => setShow2FA(false)}
          onSuccess={() => {
            setShow2FA(false);
            redirectByRole(rol);
          }}
        />
      )}
    </div>
  );
}