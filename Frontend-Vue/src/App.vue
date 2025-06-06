<template>
  <div>
    <nav>
      <router-link to="/">Inicio</router-link> |
      <router-link to="/proyecto">Mi Proyecto</router-link> | 
      <router-link to="/about">Acerca de</router-link> 

    </nav>
    <router-view />
    <Toast />
  </div>
</template>

<script>
import Toast from '@/components/Toast.vue';

export default {
  components: { Toast }
};
</script>

<style scoped>
nav {
  padding: 1rem;
}
router-link {
  margin: 0 1rem;
}
</style>
