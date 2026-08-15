import { useState } from 'react'
import { apiFetch } from '../api/client'
import { useToast } from '../context/ToastContext'

const FormularioVenta = () => {
  const { showSuccess } = useToast()
  const [form, setForm] = useState({
    dniCliente: '',
    nombreCliente: '',
    telefonoCliente: '',
    direccionCliente: '',
    planActual: '',
    planNuevo: '',
    producto: 'Internet',
    monto: '',
  })
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(null)

  const handleChange = (e) => {
    const { name, value } = e.target
    setForm((prev) => ({ ...prev, [name]: value }))
  }

  const validate = () => {
    const dniRegex = /^\d{8,11}$/
    const telRegex = /^\d{9}$/

    if (!dniRegex.test(form.dniCliente)) {
      return 'DNI debe tener 8 o 11 dígitos'
    }
    if (!form.nombreCliente.trim()) {
      return 'Nombre del cliente es obligatorio'
    }
    if (!telRegex.test(form.telefonoCliente)) {
      return 'Teléfono debe tener 9 dígitos'
    }
    if (!form.direccionCliente.trim()) {
      return 'Dirección es obligatoria'
    }
    if (!form.planActual.trim()) {
      return 'Plan Actual es obligatorio'
    }
    if (!form.planNuevo.trim()) {
      return 'Plan Nuevo es obligatorio'
    }
    if (!form.producto.trim()) {
      return 'Producto es obligatorio'
    }
    if (!form.monto || parseFloat(form.monto) <= 0) {
      return 'Monto debe ser mayor a 0'
    }
    return null
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    const validationError = validate()
    if (validationError) {
      setError(validationError)
      return
    }

    setSubmitting(true)
    setError(null)
    try {
      const payload = {
        dniCliente: form.dniCliente,
        nombreCliente: form.nombreCliente.trim(),
        telefonoCliente: form.telefonoCliente,
        direccionCliente: form.direccionCliente.trim(),
        planActual: form.planActual.trim(),
        planNuevo: form.planNuevo.trim(),
        producto: form.producto,
        monto: parseFloat(form.monto),
        codigoLlamada: `CALL-${new Date().toISOString().slice(0, 10)}-${Math.random()
          .toString(36)
          .substring(2, 6)
          .toUpperCase()}`,
      }
      await apiFetch('/ventas', {
        method: 'POST',
        body: JSON.stringify(payload),
      })

      showSuccess('Venta registrada correctamente')
      setForm({
        dniCliente: '',
        nombreCliente: '',
        telefonoCliente: '',
        direccionCliente: '',
        planActual: '',
        planNuevo: '',
        producto: 'Internet',
        monto: '',
      })
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-slate-800">Crear Venta</h1>

      <div className="rounded-2xl bg-white p-6 shadow ring-1 ring-slate-200">
        <form onSubmit={handleSubmit} className="grid gap-6 md:grid-cols-2">
          <div>
            <label
              htmlFor="dniCliente"
              className="block text-sm font-medium text-slate-700"
            >
              DNI Cliente
            </label>
            <input
              type="text"
              id="dniCliente"
              name="dniCliente"
              value={form.dniCliente}
              onChange={handleChange}
              className="mt-1 block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-800 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
              placeholder="12345678"
              required
            />
          </div>

          <div>
            <label
              htmlFor="nombreCliente"
              className="block text-sm font-medium text-slate-700"
            >
              Nombre Cliente
            </label>
            <input
              type="text"
              id="nombreCliente"
              name="nombreCliente"
              value={form.nombreCliente}
              onChange={handleChange}
              className="mt-1 block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-800 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
              placeholder="Juan Pérez"
              required
            />
          </div>

          <div>
            <label
              htmlFor="telefonoCliente"
              className="block text-sm font-medium text-slate-700"
            >
              Teléfono Cliente
            </label>
            <input
              type="text"
              id="telefonoCliente"
              name="telefonoCliente"
              value={form.telefonoCliente}
              onChange={handleChange}
              className="mt-1 block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-800 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
              placeholder="987654321"
              required
            />
          </div>

          <div>
            <label
              htmlFor="direccionCliente"
              className="block text-sm font-medium text-slate-700"
            >
              Dirección
            </label>
            <input
              type="text"
              id="direccionCliente"
              name="direccionCliente"
              value={form.direccionCliente}
              onChange={handleChange}
              className="mt-1 block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-800 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
              placeholder="Av. Larco 123, Lima"
              required
            />
          </div>

          <div>
            <label
              htmlFor="planActual"
              className="block text-sm font-medium text-slate-700"
            >
              Plan Actual
            </label>
            <input
              type="text"
              id="planActual"
              name="planActual"
              value={form.planActual}
              onChange={handleChange}
              className="mt-1 block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-800 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
              placeholder="Plan Básico"
              required
            />
          </div>

          <div>
            <label
              htmlFor="planNuevo"
              className="block text-sm font-medium text-slate-700"
            >
              Plan Nuevo
            </label>
            <input
              type="text"
              id="planNuevo"
              name="planNuevo"
              value={form.planNuevo}
              onChange={handleChange}
              className="mt-1 block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-800 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
              placeholder="Plan Premium"
              required
            />
          </div>

          <div>
            <label
              htmlFor="producto"
              className="block text-sm font-medium text-slate-700"
            >
              Producto
            </label>
            <select
              id="producto"
              name="producto"
              value={form.producto}
              onChange={handleChange}
              className="mt-1 block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-800 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
              required
            >
              <option value="Internet">Internet</option>
              <option value="Fibra Óptica">Fibra Óptica</option>
              <option value="Móvil">Móvil</option>
              <option value="TV">TV</option>
            </select>
          </div>

          <div>
            <label
              htmlFor="monto"
              className="block text-sm font-medium text-slate-700"
            >
              Monto (S/)
            </label>
            <input
              type="number"
              id="monto"
              name="monto"
              value={form.monto}
              onChange={handleChange}
              className="mt-1 block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-800 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
              placeholder="5000.00"
              step="0.01"
              min="0"
              required
            />
          </div>

        {error && (
          <div
            role="alert"
            className="mt-4 rounded-lg bg-red-50 p-3 text-sm text-red-600 md:col-span-2"
          >
            {error}
          </div>
        )}

        <div className="mt-6 border-t border-slate-200 pt-4 md:col-span-2">
          <button
            type="submit"
            disabled={submitting}
            className="inline-flex items-center justify-center rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:opacity-50"
          >
            {submitting ? (
              <>
                <svg
                  className="-ml-1 mr-2 h-4 w-4 animate-spin"
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 24 24"
                >
                  <circle
                    className="opacity-25"
                    cx="12"
                    cy="12"
                    r="10"
                    stroke="currentColor"
                    strokeWidth="4"
                  />
                  <path
                    className="opacity-75"
                    fill="currentColor"
                    d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"
                  />
                </svg>
                Guardando...
              </>
            ) : (
              'Guardar Venta'
            )}
          </button>
        </div>
        </form>
      </div>
    </div>
  )
}

export default FormularioVenta
