import { useEffect, useState } from 'react'
import { apiFetch } from '../api/client'
import { fechaToInstant } from '../utils/fechas'

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

const MisVentas = () => {
  const [ventas, setVentas] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [filtros, setFiltros] = useState({
    estado: '',
    desde: '',
    hasta: '',
  })
  const [pageInfo, setPageInfo] = useState({
    page: 0,
    size: 10,
    totalPages: 0,
    totalElements: 0,
  })

  useEffect(() => {
    let cancelled = false

    ;(async () => {
      setLoading(true)
      setError(null)
      try {
        const params = new URLSearchParams()
        if (filtros.estado) params.append('estado', filtros.estado)
        if (filtros.desde) params.append('desde', fechaToInstant(filtros.desde))
        if (filtros.hasta) params.append('hasta', fechaToInstant(filtros.hasta, true))
        params.append('page', pageInfo.page)
        params.append('size', pageInfo.size)

        const data = await apiFetch(`/ventas/mis-ventas?${params.toString()}`)
        if (!cancelled) {
          setVentas(data.content || [])
          setPageInfo({
            page: data.page,
            size: data.size,
            totalPages: data.totalPages,
            totalElements: data.totalElements,
          })
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
  }, [filtros, pageInfo.page, pageInfo.size])

  const aplicarFiltrado = (e) => {
    e.preventDefault()
    setPageInfo((prev) => ({ ...prev, page: 0 }))
  }

  const limpiarFiltros = () => {
    setFiltros({ estado: '', desde: '', hasta: '' })
  }

  const paginar = (page) => {
    if (page >= 0 && page < pageInfo.totalPages) {
      setPageInfo((prev) => ({ ...prev, page }))
    }
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-slate-800">Mis Ventas</h1>

      <div className="rounded-2xl bg-white p-6 shadow ring-1 ring-slate-200">
        <form onSubmit={aplicarFiltrado} className="grid gap-4 md:grid-cols-4">
          <div>
            <label
              htmlFor="estado"
              className="block text-sm font-medium text-slate-700"
            >
              Estado
            </label>
            <select
              id="estado"
              value={filtros.estado}
              onChange={(e) =>
                setFiltros((prev) => ({ ...prev, estado: e.target.value }))
              }
              className="mt-1 block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-800 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
            >
              <option value="">Todos</option>
              <option value="PENDIENTE">Pendiente</option>
              <option value="APROBADA">Aprobada</option>
              <option value="RECHAZADA">Rechazada</option>
            </select>
          </div>

          <div>
            <label
              htmlFor="desde"
              className="block text-sm font-medium text-slate-700"
            >
              Desde
            </label>
            <input
              type="date"
              id="desde"
              value={filtros.desde}
              onChange={(e) =>
                setFiltros((prev) => ({ ...prev, desde: e.target.value }))
              }
              className="mt-1 block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-800 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
            />
          </div>

          <div>
            <label
              htmlFor="hasta"
              className="block text-sm font-medium text-slate-700"
            >
              Hasta
            </label>
            <input
              type="date"
              id="hasta"
              value={filtros.hasta}
              onChange={(e) =>
                setFiltros((prev) => ({ ...prev, hasta: e.target.value }))
              }
              className="mt-1 block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-800 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
            />
          </div>

          <div className="flex items-end gap-2">
            <button
              type="submit"
              className="flex-1 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700"
            >
              Filtrar
            </button>
            <button
              type="button"
              onClick={limpiarFiltros}
              className="flex-1 rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
            >
              Limpiar
            </button>
          </div>
        </form>
      </div>

      {error && (
        <div
          role="alert"
          className="rounded-lg bg-red-50 p-4 text-sm text-red-800"
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
          <p className="text-slate-500">No hay ventas registradas.</p>
        </div>
      ) : (
        <>
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-slate-200">
              <thead className="bg-slate-50">
                <tr>
                  <th className="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">
                    Cliente
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">
                    Producto
                  </th>
                  <th className="px-4 py-3 text-right text-xs font-medium text-slate-500 uppercase tracking-wider">
                    Plan
                  </th>
                  <th className="px-4 py-3 text-right text-xs font-medium text-slate-500 uppercase tracking-wider">
                    Monto (S/)
                  </th>
                  <th className="px-4 py-3 text-center text-xs font-medium text-slate-500 uppercase tracking-wider">
                    Estado
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">
                    Fecha Registro
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
                    <td className="px-4 py-3 whitespace-nowrap text-sm text-center">
                      <EstadoBadge estado={venta.estado} />
                    </td>
                    <td className="px-4 py-3 whitespace-nowrap text-sm text-slate-500">
                      {new Date(venta.fechaRegistro).toLocaleDateString('es-PE')}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {pageInfo.totalPages > 1 && (
            <div className="mt-4 flex justify-center gap-2">
              <button
                onClick={() => paginar(pageInfo.page - 1)}
                disabled={pageInfo.page === 0}
                className="rounded-lg border border-slate-300 px-3 py-1 text-sm text-slate-600 hover:bg-slate-50 disabled:opacity-50"
              >
                Anterior
              </button>
              {Array.from({ length: pageInfo.totalPages }).map((_, i) => (
                <button
                  key={i}
                  onClick={() => paginar(i)}
                  className={`rounded-lg px-3 py-1 text-sm font-medium ${
                    i === pageInfo.page
                      ? 'bg-indigo-600 text-white'
                      : 'border border-slate-300 text-slate-600 hover:bg-slate-50'
                  }`}
                >
                  {i + 1}
                </button>
              ))}
              <button
                onClick={() => paginar(pageInfo.page + 1)}
                disabled={pageInfo.page >= pageInfo.totalPages - 1}
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

export default MisVentas
