import { createContext, useContext, useState } from 'react'
import ErrorToast from '../components/ErrorToast'
import SuccessToast from '../components/SuccessToast'

const ToastContext = createContext(null)

export const ToastProvider = ({ children }) => {
  const [toasts, setToasts] = useState([])

  const push = (message, type) => {
    const id = Date.now()
    setToasts((prev) => [...prev, { id, message, type }])
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id))
    }, 6000)
  }

  const showError = (message) => push(message, 'error')
  const showSuccess = (message) => push(message, 'success')

  const dismiss = (id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id))
  }

  return (
    <ToastContext.Provider value={{ showError, showSuccess }}>
      {children}
      <div className="fixed inset-x-0 top-0 z-50 flex flex-col-reverse gap-3 p-4 sm:top-4 sm:inset-auto sm:max-w-sm sm:right-4">
        {toasts.map((toast) =>
          toast.type === 'success' ? (
            <SuccessToast
              key={toast.id}
              message={toast.message}
              onClose={() => dismiss(toast.id)}
            />
          ) : (
            <ErrorToast
              key={toast.id}
              message={toast.message}
              onClose={() => dismiss(toast.id)}
            />
          )
        )}
      </div>
    </ToastContext.Provider>
  )
}

export const useToast = () => {
  const context = useContext(ToastContext)
  if (!context) {
    throw new Error('useToast must be used within a ToastProvider')
  }
  return context
}
