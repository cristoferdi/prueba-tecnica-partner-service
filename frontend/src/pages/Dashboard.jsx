import { useAuth } from '../context/AuthContext'

const ROLE_TITLE = {
  AGENTE: 'Agente',
  BACKOFFICE: 'Backoffice',
  SUPERVISOR: 'Supervisor',
  ADMIN: 'Administrador',
}

const ROLE_DESC = {
  AGENTE: 'Administra las ventas que registraste y crea nuevas ventas.',
  BACKOFFICE: 'Revisa las ventas pendientes y aprueba o rechaza según corresponda.',
  SUPERVISOR: 'Visualiza las ventas de tu equipo y genera reportes de productividad.',
  ADMIN: 'Panel de control general del sistema de ventas.',
}

const Dashboard = () => {
  const { role } = useAuth()
  const title = ROLE_TITLE[role] || 'Usuario'
  const desc = ROLE_DESC[role] || 'Bienvenido al sistema.'

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-800">Dashboard</h1>
        <p className="mt-1 text-sm text-slate-500">
          Rol autenticado: <span className="font-medium text-slate-800">{role}</span>
        </p>
      </div>

      <div className="rounded-xl bg-white p-6 shadow ring-1 ring-slate-200">
        <h2 className="text-lg font-semibold text-slate-800">
          Bienvenido, {title}
        </h2>
        <p className="mt-2 max-w-lg text-slate-600">{desc}</p>
        <div className="mt-4 rounded-md bg-indigo-50/50 px-4 py-3 text-sm text-indigo-800 ring-1 ring-indigo-100">
          El contenido de esta sección se habilitará en las próximas fases.
        </div>
      </div>
    </div>
  )
}

export default Dashboard
