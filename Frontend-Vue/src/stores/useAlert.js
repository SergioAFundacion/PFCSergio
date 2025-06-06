import { useAlertStore } from '@/stores/alert';

export default function useAlert() {
  const alertStore = useAlertStore();
  return {
    showSuccess: ({ title, description }) => {
      alertStore.showSuccess({ title, description });
    },
    showError: ({ title, description }) => {
      alertStore.showError({ title, description });
    },
    offToast: () => {
      alertStore.alert.toggle = false;
    }
  };
}