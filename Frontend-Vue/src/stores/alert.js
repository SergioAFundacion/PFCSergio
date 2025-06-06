import { defineStore } from 'pinia';

export const useAlertStore = defineStore('alert', {
  state: () => ({
    alert: {
      toggle: false,
      properties: {
        title: '',
        description: '',
        typeData: '',
        showIcon: true,
        closable: true,
        closeText: 'Cerrar',
        width: 90,
        background: '',
        border: '',
        hSize: null,
        center: false,
        rtl: false
      }
    }
  }),
  actions: {
    showSuccess({ title, description }) {
      console.log('showSuccess en store:', { title, description });
      this.alert = {
        toggle: true,
        properties: {
          ...this.alert.properties,
          title: title || 'Éxito',
          description,
          typeData: 'success',
          background: '#28a745',
          border: '#218838',
        },
      };
      console.log('Estado actualizado en store:', this.alert);
      setTimeout(() => {
        this.hideToast();
      }, 3000);
    },
    showError({ title, description }) {
      console.log('showError en store:', { title, description });
      this.alert = {
        toggle: true,
        properties: {
          ...this.alert.properties,
          title: title || 'Error',
          description,
          typeData: 'error',
          background: '#dc3545',
          border: '#c82333',
        },
      };
      console.log('Estado actualizado en store:', this.alert);
      setTimeout(() => {
        this.hideToast();
      }, 3000);
    },
    hideToast() {
      this.alert.toggle = false;
      console.log('Toast ocultado después de 3s');
    }
  }
});