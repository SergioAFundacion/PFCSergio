export default function useAlert() {
  return {
    showSuccess: (msg) => alert(`✅ ${msg}`),
    showError: (msg) => alert(`❌ ${msg}`)
  }
}
