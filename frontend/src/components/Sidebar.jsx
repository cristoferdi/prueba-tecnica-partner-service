import { NavLink } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const SIDEBAR_GROUPS = {
  AGENTE: [
    { label: 'Dashboard', to: '/', icon: 'M3 12l2-2v4a2 2 0 002 2h2' },
    { label: 'Mis Ventas', to: '/mis-ventas', icon: 'M3 12l2-2v4a2 2 0 002 2h2' },
    { label: 'Crear Venta', to: '/crear-venta', icon: 'M12 5v2m0 4h2m-2 4v2m4-6h2' },
  ],
  BACKOFFICE: [
    { label: 'Dashboard', to: '/', icon: 'M3 12l2-2v4a2 2 0 002 2h2' },
    { label: 'Ventas Pendientes', to: '/pendientes', icon: 'M12 5v2m0 4h2m-2 4v2m4-6h2' },
  ],
  SUPERVISOR: [
    { label: 'Dashboard', to: '/', icon: 'M3 12l2-2v4a2 2 0 002 2h2' },
    { label: 'Equipo', to: '/equipo', icon: 'M12 5v2m0 4h2m-2 4v2m4-6h2' },
    { label: 'Reportes', to: '/reportes', icon: 'M3 12l2-2v4a2 2 0 002 2h2' },
  ],
  ADMIN: [
    { label: 'Dashboard', to: '/', icon: 'M3 12l2-2v4a2 2 0 002 2h2' },
  ],
}

const Sidebar = () => {
  const { role, logout } = useAuth()
  const items = SIDEBAR_GROUPS[role] || SIDEBAR_GROUPS.ADMIN

  return (
    <aside className="flex h-screen w-64 flex-shrink-0 flex-col overflow-y-auto bg-slate-900 text-slate-300">
      <div className="flex h-16 items-center justify-center border-b border-slate-800">
        <span className="text-lg font-bold text-white">Telco Hogar</span>
      </div>

      <nav className="flex-1 py-4">
        <ul className="space-y-1 px-4">
          {items.map((item) => (
            <li key={item.to}>
              <NavLink
                to={item.to}
                end
                className={({ active }) =>
                  `flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors ${
                    active
                      ? 'bg-indigo-600/20 text-indigo-300'
                      : 'text-slate-300 hover:bg-slate-800 hover:text-slate-100'
                  }`
                }
              >
                <svg
                  className="h-5 w-5 flex-shrink-0"
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 24 24"
                  aria-hidden="true"
                >
                  <path
                    stroke="currentColor"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth="2"
                    d={item.icon}
                  />
                </svg>
                {item.label}
              </NavLink>
            </li>
          ))}
        </ul>
      </nav>

      <div className="border-t border-slate-800 p-4">
        <button
          type="button"
          onClick={logout}
          className="flex w-full items-center justify-center gap-2 rounded-md px-3 py-2 text-sm font-medium text-red-400 hover:bg-red-950/30 hover:text-red-300 transition-colors"
        >
          <svg
            className="h-5 w-5"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            aria-hidden="true"
          >
            <path
              stroke="currentColor"
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="2"
              d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a2 2 0 11-2 2H7a2 2 0 01-2-2V7a2 2 0 01 2-2h6a2 2 0 01 2 2v1"
            />
          </svg>
          Cerrar sesión
        </button>
      </div>
    </aside>
  )
}

export default Sidebar
