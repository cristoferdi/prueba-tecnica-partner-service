const ErrorToast = ({ message, onClose }) => {
  return (
    <div
      className="flex items-start gap-3 rounded-lg bg-red-50 p-4 text-sm text-red-800 shadow-lg ring-1 ring-red-200 animate-in slide-in-from-top-2"
      role="alert"
    >
      <svg
        className="mt-0.5 h-5 w-5 flex-shrink-0 text-red-600"
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
          d="M12 8v4m0 4h.01M9 16h6a3 3 0 0 1 3 3v1"
        />
        <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2" />
      </svg>
      <span className="flex-1">{message}</span>
      <button
        type="button"
        onClick={onClose}
        className="inline-flex items-center justify-center rounded-md p-1 text-red-600 transition-colors hover:bg-red-100 hover:text-red-700 focus:outline-none focus:ring-2 focus:ring-red-600 focus:ring-offset-2"
        aria-label="Cerrar"
      >
        <svg
          className="h-4 w-4"
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
            d="M6 18L18 6M6 6l12 12"
          />
        </svg>
      </button>
    </div>
  )
}

export default ErrorToast
