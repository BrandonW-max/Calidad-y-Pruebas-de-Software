import Sidebar from "./Sidebar";
import "../styles/Dashboard.css";

export default function DashboardLayout({ rol, children }) {
  return (
    <div className="dashboard">
      <Sidebar rol={rol} />
      <main className="main-content">{children}</main>
    </div>
  );
}
