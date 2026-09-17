import Sidebar from "./Sidebar";

export default function DashboardLayout({ rol, children }) {
  return (
    <div className="min-h-screen bg-ink-100">
      <Sidebar rol={rol} />
      <main className="min-h-screen px-5 pb-7 pt-20 sm:px-6 lg:ml-[272px] lg:px-9 lg:py-9 lg:pt-9">
        {children}
      </main>
    </div>
  );
}
