import { Routes, Route, Navigate, Outlet } from 'react-router-dom'
import { useAuth } from './context/AuthContext'
import Layout from './components/Layout'
import Login from './components/Login'
import Dashboard from './pages/Dashboard'
import FormularioVenta from './pages/FormularioVenta'
import MisVentas from './pages/MisVentas'
import PanelBackoffice from './pages/PanelBackoffice'
import ResumenSupervisor from './pages/ResumenSupervisor'
import VentasEquipo from './pages/VentasEquipo'

const RequireAuth = () => {
  const { isAuthenticated } = useAuth()
  return isAuthenticated ? <Outlet /> : <Navigate to="/login" replace />
}

function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route element={<RequireAuth />}>
        <Route element={<Layout />}>
          <Route path="/" element={<Dashboard />} />
          <Route path="/mis-ventas" element={<MisVentas />} />
          <Route path="/crear-venta" element={<FormularioVenta />} />
          <Route path="/pendientes" element={<PanelBackoffice />} />
          <Route path="/reportes" element={<ResumenSupervisor />} />
          <Route path="/equipo" element={<VentasEquipo />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default App
