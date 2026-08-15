import { useEffect, useState } from 'react'
import { apiFetch } from '../api/client'
import { fechaToInstant } from '../utils/fechas'

const TAMANIO_PAGINA = 10

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

const VentasEquipo = () => {
  const [ventas, setVentas] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [filtros, setFiltros] = useState({
    estado: '',
    agenteId: '',
    desde: '',
    hasta: '',
  })

  const [pagina, setPagina] = useState(0)

  useEffect(() => {
    let cancelled = false

    ;(async () => {
      setLoading(true)
      setError(null)
      try {
        const params = new URLSearchParams()
        if (filtros.estado) params.append('estado', filtros.estado)
        if (filtros.agenteId) params.append('agenteId', filtros.agenteId)
        if (filtros.desde) params.append('desde', fechaToInstant(filtros.desde))
        if (filtros.hasta) params.append('hasta', fechaToInstant(filtros.hasta, true))
        params.append('page', pagina)
        params.append('size', TAMANIO_PAGINA)

        const data = await apiFetch(`/ventas/equipo?${params.toString()}`)
        if (!cancelled) {
          setVentas(Array.isArray(data) ? data : data.content || [])
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
  }, [filtros, pagina])

  const aplicarFiltrado = (e) => {
    e.preventDefault()
    setPagina(0)
  }

  const limpiarFiltros = () => {
    setFiltros({ estado: '', agenteId: '', desde: '', hasta: '' })
  }

  const totalPaginas = Math.ceil((ventas.length || 0) / TAMANIO_PAGINA)

  return (
    <div className="space-y-6">
      <div className="rounded-2xl bg-white p-6 shadow ring-1 ring-slate-200">
        <form onSubmit={aplicarFiltrado} className="grid gap-4 md:grid-cols-4">
          <div>
            <label
              htmlFor="estado-venta"
              className="block text-sm font-medium text-slate-700"
            >
              Estado
            </label>
            <select
              id="estado-venta"
              value={filtros.estado}
              onChange={(e) =>
                setFiltros((prev) => ({ ...prev, estado: e.target.value }))
              }
              className="mt-1 block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
            >
              <option value="">Todos</option>
              <option value="PENDIENTE">Pendiente</option>
              <option value="APROBADA">Aprobada</option>
              <option value="RECHAZADA">Rechazada</option>
            </select>
          </div>

          <div>
            <label
              htmlFor="agente-id"
              className="block text-sm font-medium text-slate-700"
            >
              Agente ID
            </label>
            <input
              type="number"
              id="agente-id"
              value={filtros.agenteId}
              onChange={(e) =>
                setFiltros((prev) => ({
                  ...prev,
                  agenteId: e.target.value,
                }))
              }
              className="mt-1 block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
              placeholder="1, 2, etc."
            />
          </div>

          <div>
            <label
              htmlFor="desde-equipo"
              className="block text-sm font-medium text-slate-700"
            >
              Desde
            </label>
            <input
              type="date"
              id="desde-equipo"
              value={filtros.desde}
              onChange={(e) =>
                setFiltros((prev) => ({ ...prev, desde: e.target.value }))
              }
              className="mt-1 block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
            />
          </div>

          <div>
            <label
              htmlFor="hasta-equipo"
              className="block text-sm font-medium text-slate-700"
            >
              Hasta
            </label>
            <input
              type="date"
              id="hasta-equipo"
              value={filtros.hasta}
              onChange={(e) =>
                setFiltros((prev) => ({ ...prev, hasta: e.target.value }))
              }
              className="mt-1 block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
            />
          </div>

          <div className="md:col-span-4 flex gap-2">
            <button
              type="submit"
              className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700"
            >
              Filtrar
            </button>
            <button
              type="button"
              onClick={limpiarFiltros}
              className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
            >
              Limpiar
            </button>
          </div>
        </form>
      </div>

      {error && (
        <div
          role="alert"
          className="rounded-md bg-red-50 p-4 text-sm text-red-600"
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
          <p className="text-slate-500">No hay ventas para mostrar.</p>
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
                    Agente
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
                      {venta.agenteUsername || `ID: ${venta.agenteId}`}
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
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {totalPaginas > 1 && (
            <div className="mt-4 flex justify-center gap-2">
              <button
                onClick={() => setPagina((p) => Math.max(0, p - 1))}
                disabled={pagina === 0}
                className="rounded-lg border border-slate-300 px-3 py-1 text-sm text-slate-600 hover:bg-slate-50 disabled:opacity-50"
              >
                Anterior
              </button>
              {Array.from({ length: totalPaginas }).map((_, i) => (
                <button
                  key={i}
                  onClick={() => setPagina(i)}
                  className={`rounded-lg px-3 py-1 text-sm font-medium ${
                    i === pagina
                      ? 'bg-indigo-600 text-white'
                      : 'border border-slate-300 text-slate-600 hover:bg-slate-50'
                  }`}
                >
                  {i + 1}
                </button>
              ))}
              <button
                onClick={() => setPagina((p) => Math.min(totalPaginas - 1, p + 1))}
                disabled={pagina >= totalPaginas - 1}
                className="rounded-lg border border-slate-300 px-3 py-1 text-sm text-slate-600 hover:bg-slate-50 disabled:opacity-50"
              >
                Siguiente
              </button>
            </div>
          )}
        </>
      )}
    </div>
  )
}

export default VentasEquipo