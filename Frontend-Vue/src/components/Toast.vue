<template>
  <div v-if="alert?.toggle" class="toast" :style="toastStyle">
    <div class="toast-header">
      <strong>{{ alert?.properties?.title || 'Sin título' }}</strong>
      <button v-if="alert?.properties?.closable" @click="offToast" class="botonCerrar">
        X
      </button>
    </div>
    <div class="toast-body">
      {{ alert?.properties?.description || 'Sin descripción' }}
    </div>
  </div>
</template>

<script>
import { useAlertStore } from '@/stores/alert';
import { computed, watch } from 'vue';

export default {
  name: 'Toast',
  setup() {
    const alertStore = useAlertStore();
    const alert = computed(() => alertStore.alert);
    const toastStyle = computed(() => ({
      width: `${alert.value.properties.width || 90}%`,
      backgroundColor: alert.value.properties.background || '#fff',
      border: alert.value.properties.border ? `2px solid ${alert.value.properties.border}` : 'none',
      minHeight: alert.value.properties.hSize ? `${alert.value.properties.hSize}px` : 'auto',
      textAlign: alert.value.properties.center ? 'center' : 'left',
      direction: alert.value.properties.rtl ? 'rtl' : 'ltr',
    }));

    const offToast = () => {
      console.log('offToast ejecutado en Toast.vue');
      alertStore.hideToast();
    };

    watch(() => alert.value.toggle, (newValue) => {
      console.log('alert.toggle cambió:', newValue, 'Estado completo:', alert.value);
    });

    console.log('Estado inicial de alert:', alert.value);
    return { alert, toastStyle, offToast };
  },
};
</script>

<style scoped>
.toast {
  position: fixed;
  top: 20px;
  right: 20px;
  max-width: 400px;
  padding: 15px;
  border-radius: 5px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  z-index: 10000;
  background-color: #f0f0f0;
}
.toast-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.botonCerrar {
  background: none;
  border: none;
  cursor: pointer;
}
.toast-body {
  font-size: 14px;
}
</style>