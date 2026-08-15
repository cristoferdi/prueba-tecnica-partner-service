import { useEffect, useState } from 'react'
import { apiFetch } from '../api/client'

const TarjetaConteo = ({ titulo, valor, colorClass = 'blue' }) => {
  const containerClasses = {
    blue: 'border-l-4 border-indigo-500',
    green: 'border-l-4 border-green-500',
    red: 'border-l-4 border-red-500',
    yellow: 'border-l-4 border-yellow-500',
  }

  return (
    <div className={`rounded-lg bg-white p-4 shadow ring-1 ring-slate-200 ${containerClasses[colorClass] || containerClasses.blue}`}>
      <p className="text-sm font-medium text-slate-500">{titulo}</p>
      <p className="mt-1 text-2xl font-bold text-slate-800">{valor}</p>
    </div>
  )
}

const ResumenSupervisor = () => {
  const [resumen, setResumen] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      setLoading(true)
      setError(null)
      try {
        const data = await apiFetch('/reportes/resumen')
        if (!cancelled) setResumen(data)
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

  if (loading) {
    return (
      <div className="text-center py-8">
        <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
      </div>
    )
  }

  if (error) {
    return (
      <div
        role="alert"
        className="rounded-md bg-red-50 p-4 text-sm text-red-600"
      >
        {error}
      </div>
    )
  }

  if (!resumen) {
    return (
      <div className="text-center py-8 text-slate-500">
        No hay datos disponibles.
      </div>
    )
  }

  const { conteosPorEstado, montoTotalAprobadas, ventasPorDia } = resumen

  const maxCount = Math.max(...ventasPorDia.map((v) => v.count), 1)

  return (
    <div className="space-y-6">
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <TarjetaConteo
          titulo="Ventas Pendientes"
          valor={conteosPorEstado.find((c) => c.estado === 'PENDIENTE')?.count || 0}
          colorClass="yellow"
        />
        <TarjetaConteo
          titulo="Ventas Aprobadas"
          valor={conteosPorEstado.find((c) => c.estado === 'APROBADA')?.count || 0}
          colorClass="green"
        />
        <TarjetaConteo
          titulo="Ventas Rechazadas"
          valor={conteosPorEstado.find((c) => c.estado === 'RECHAZADA')?.count || 0}
          colorClass="red"
        />
        <TarjetaConteo
          titulo="Monto Total Aprobado"
          valor={`S/ ${montoTotalAprobadas?.toLocaleString() || '0'}`}
          colorClass="blue"
        />
      </div>

      <div className="rounded-lg bg-white p-6 shadow ring-1 ring-slate-200">
        <h3 className="text-lg font-semibold text-slate-800 mb-4">Ventas por Día (últimos 30 días)</h3>

        {ventasPorDia.length === 0 ? (
          <p className="text-slate-500">No hay datos por día.</p>
        ) : (
          <div className="space-y-3">
            {ventasPorDia.map((dia) => (
              <div key={dia.fecha}>
                <div className="flex items-center justify-between text-sm">
                  <span className="text-slate-600">{dia.fecha}</span>
                  <span className="font-medium text-slate-800">
                    {dia.count} venta{dia.count !== 1 ? 's' : ''}, S/ {dia.monto.toFixed(2)}
                  </span>
                </div>
                <div className="mt-1 h-2 bg-slate-100 rounded overflow-hidden">
                  <div
                    className="h-full bg-indigo-600 rounded"
                    style={{ width: `${(dia.count / maxCount) * 100}%` }}
                  ></div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

export default ResumenSupervisor