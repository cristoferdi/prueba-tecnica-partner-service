const SuccessToast = ({ message, onClose }) => {
  return (
    <div
      className="flex items-start gap-3 rounded-lg bg-green-50 p-4 text-sm text-green-800 shadow-lg ring-1 ring-green-200 animate-in slide-in-from-top-2"
      role="status"
    >
      <svg
        className="mt-0.5 h-5 w-5 flex-shrink-0 text-green-600"
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
          d="M9 12.75 11.25 15 15 9.75M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0z"
        />
      </svg>
      <span className="flex-1">{message}</span>
      <button
        type="button"
        onClick={onClose}
        className="inline-flex items-center justify-center rounded-md p-1 text-green-600 transition-colors hover:bg-green-100 hover:text-green-700 focus:outline-none focus:ring-2 focus:ring-green-600 focus:ring-offset-2"
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

export default SuccessToast