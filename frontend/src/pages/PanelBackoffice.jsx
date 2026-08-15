import { useEffect, useState } from 'react'
import { apiFetch } from '../api/client'

const EstadoBadge = ({ estado }) => {
  const styles = {
    PENDIENTE: 'bg-yellow-100 text-yellow-800',
    APROBADA: 'bg-green-100 text-green-800',
    RECHAZADA: 'bg-red-100 text-red-800',
  }

  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${styles[estado] || 'bg-gray-100 text-gray-800'}`}
    >
      {estado}
    </span>
  )
}

const ModalRechazar = ({ venta, onClose, onConfirm }) => {
  const [motivo, setMotivo] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  if (!venta) return null

  const handleSubmit = async () => {
    if (!motivo.trim()) {
      setError('El motivo del rechazo es obligatorio')
      return
    }
    setLoading(true)
    setError(null)
    try {
      await apiFetch(`/ventas/${venta.id}/rechazar`, {
        method: 'POST',
        body: JSON.stringify({ motivo: motivo.trim() }),
      })
      onConfirm()
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm"
      style={{ display: 'block' }}
    >
      <div
        className="relative w-full max-w-md transform rounded-lg bg-white p-6 shadow-xl transition-all animate-in"
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-title"
      >
        <div className="flex items-start justify-between">
          <h3 className="text-lg font-semibold text-slate-900" id="modal-title">
            Rechazar Venta
          </h3>
          <button
            type="button"
            onClick={onClose}
            className="text-slate-400 hover:text-slate-600"
          >
            <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" aria-hidden="true">
              <path stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <div className="mt-4">
          <p className="text-sm text-slate-600 mb-3">
            Cliente: <span className="font-medium">{venta.nombreCliente}</span>
          </p>
          <p className="text-sm text-slate-600 mb-4">
            Producto: <span className="font-medium">{venta.producto}</span>
          </p>

          <div>
            <label
              htmlFor="motivo-rechazo"
              className="block text-sm font-medium text-slate-700"
            >
              Motivo del rechazo *
            </label>
            <textarea
              id="motivo-rechazo"
              value={motivo}
              onChange={(e) => setMotivo(e.target.value)}
              rows={3}
              className="mt-1 block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
              placeholder="Ej: Faltan documentos, cliente negativo, etc."
              required
            />
          </div>

          {error && (
            <div
              role="alert"
              className="mt-3 rounded-md bg-red-50 p-3 text-sm text-red-600"
            >
              {error}
            </div>
          )}
        </div>

        <div className="mt-6 flex justify-end gap-3 pt-4 border-t border-slate-200">
          <button
            type="button"
            onClick={onClose}
            className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
          >
            Cancelar
          </button>
          <button
            type="button"
            onClick={handleSubmit}
            disabled={loading}
            className="inline-flex items-center justify-center rounded-lg bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2 disabled:opacity-50"
          >
            {loading ? 'Rechazando...' : 'Rechazar'}
          </button>
        </div>
      </div>
    </div>
  )
}

const PanelBackoffice = () => {
  const [ventas, setVentas] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [ventaActiva, setVentaActiva] = useState(null)

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      setLoading(true)
      setError(null)
      try {
        const data = await apiFetch('/ventas/pendientes')
        if (!cancelled) {
          setVentas(data || [])
        }
      } catch (err) {
        if (!cancelled) setError(err.message)
      } finally {
        if (!cancelled) setLoading(false)
      }
    })()

    return () => {
      cancelled = true
    }
  }, [])

  const refetch = async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await apiFetch('/ventas/pendientes')
      setVentas(data || [])
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const aprobarVenta = async (id) => {
    try {
      await apiFetch(`/ventas/${id}/aprobar`, { method: 'POST' })
      refetch()
    } catch (err) {
      setError(err.message)
    }
  }

  const rechazarConfirmado = async () => {
    await refetch()
    setVentaActiva(null)
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-slate-800">Panel Backoffice</h1>

      <div className="rounded-2xl bg-white p-6 shadow ring-1 ring-slate-200">
        {error && (
          <div
            role="alert"
            className="mb-4 rounded-md bg-red-50 p-3 text-sm text-red-600"
          >
            {error}
          </div>
        )}

        {loading ? (
          <div className="text-center py-8">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
          </div>
        ) : ventas.length === 0 ? (
          <div className="text-center py-12">
            <p className="text-slate-500">No hay ventas pendientes.</p>
          </div>
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-slate-200">
                <thead className="bg-slate-50">
                  <tr>
                    <th className="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">
                      Cliente
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">
                      Producto
                    </th>
                    <th className="px-4 py-3 text-right text-xs font-medium text-slate-500 uppercase">
                      Plan
                    </th>
                    <th className="px-4 py-3 text-right text-xs font-medium text-slate-500 uppercase">
                      Monto (S/)
                    </th>
                    <th className="px-4 py-3 text-center text-xs font-medium text-slate-500 uppercase">
                      Estado
                    </th>
                    <th className="px-4 py-3 text-center text-xs font-medium text-slate-500 uppercase">
                      Acciones
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-slate-200">
                  {ventas.map((venta) => (
                    <tr key={venta.id}>
                      <td className="px-4 py-3 whitespace-nowrap text-sm text-slate-800">
                        <div className="font-medium">{venta.nombreCliente}</div>
                        <div className="text-slate-400 text-xs">
                          DNI: {venta.dniCliente}
                        </div>
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap text-sm text-slate-600">
                        {venta.producto}
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap text-sm text-slate-800 text-right">
                        {venta.planNuevo}
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap text-sm text-slate-800 text-right">
                        {venta.monto?.toLocaleString(undefined, {
                          style: 'currency',
                          currency: 'PEN',
                        })}
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap text-center">
                        <EstadoBadge estado={venta.estado} />
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap text-center">
                        <div className="flex gap-2 justify-center">
                          <button
                            onClick={() => aprobarVenta(venta.id)}
                            className="rounded-lg bg-green-600 px-3 py-1 text-xs font-medium text-white hover:bg-green-700"
                          >
                            Aprobar
                          </button>
                          <button
                            onClick={() => setVentaActiva(venta)}
                            className="rounded-lg bg-red-600 px-3 py-1 text-xs font-medium text-white hover:bg-red-700"
                          >
                            Rechazar
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </>
        )}
      </div>

      {/* Modal Rechazar */}
      {ventaActiva && (
        <div className="fixed inset-0 z-40">
          <ModalRechazar
            venta={ventaActiva}
            onClose={() => setVentaActiva(null)}
            onConfirm={rechazarConfirmado}
          />
        </div>
      )}
    </div>
  )
}

export default PanelBackoffice
