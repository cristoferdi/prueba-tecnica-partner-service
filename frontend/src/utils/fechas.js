export const fechaToInstant = (fecha, finDeDia = false) => {
  if (!fecha) return ''
  return finDeDia ? `${fecha}T23:59:59Z` : `${fecha}T00:00:00Z`
}
