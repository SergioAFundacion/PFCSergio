<template>
  <div v-if="alert.toggle" class="toast" :style="toastStyle">
    <div class="toast-header">
      <span v-if="alert.properties.showIcon">📢</span>
      <strong>{{ alert.properties.title }}</strong>
      <button v-if="alert.properties.closable" @click="alert.offToast" class="close-button">
        {{ alert.properties.closeText || 'Cerrar' }}
      </button>
    </div>
    <div class="toast-body">
      {{ alert.properties.description }}
    </div>
  </div>
</template>

<script>
import { useAlert } from '@/stores/alert';

export default {
  name: 'Toast',
  setup() {
    const alert = useAlert();
    const toastStyle = {
      width: `${alert.properties.width}%`,
      backgroundColor: alert.properties.background,
      border: alert.properties.border ? `2px solid ${alert.properties.border}` : 'none',
      minHeight: alert.properties.hSize ? `${alert.properties.hSize}px` : 'auto',
      textAlign: alert.properties.center ? 'center' : 'left',
      direction: alert.properties.rtl ? 'rtl' : 'ltr'
    };

    return { alert, toastStyle };
  }
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
  z-index: 1000;
}
.toast-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.close-button {
  background: none;
  border: none;
  cursor: pointer;
}
.toast-body {
  font-size: 14px;
}
</style>