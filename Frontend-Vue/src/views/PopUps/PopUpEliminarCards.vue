<template>
    <!-- Contenedor principal del modal de eliminación -->
    <div class="pop-up" v-if="show">
    <!-- Contenedor interno del modal, cierra al hacer clic fuera -->
    <div class="pop-up-inner">
      <!-- Título del modal -->
      <h1 class="pop-up-h1">{{ $t('lang.proyecto.tituloEliminar') }}</h1>
      <!-- Sección para mostrar los elementos a eliminar o mensaje de error -->
      <div class="contInput" style="margin-bottom: 12px; text-align: left;">
        <!-- Lista de elementos seleccionados para eliminar -->
        <template v-if="items.length > 0">
          <p>
            {{ nivelActual === 'root' ?
               $t('lang.proyecto.confirmarEliminacionCarpetas', { count: items.length }) :
               $t('lang.proyecto.confirmarEliminacionArchivos', { count: items.length }) }}
          </p>
          <ul class="listaArchivos">
            <li v-for="(item, index) in items" :key="index">
              {{ item.title }}
            </li>
          </ul>
        </template>
        <template v-else>
          <p style="color: red;">
            {{ $t('lang.proyecto.seleccionarAlMenosUnElemento') }}
          </p>
        </template>
      </div>

      <!-- Botones de acción (cancelar y eliminar) -->
      <div style="display: flex; gap: 18px; justify-content: flex-end">
        <button @click="close" class="botonMenuSecundario">
          {{ $t('lang.proyecto.cancelar') }}
        </button>
        <button
          v-if="items.length > 0"
          @click="confirmDelete"
          class="botonMenuEliminar"
        >
          {{ $t('lang.proyecto.eliminar') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>

export default {
  name: 'PopUpEliminarCards',
  props: {
    show: {
      type: Boolean,
      required: true
    },
    items: {
      type: Array,
      required: true,
      default: () => []
    },
    nivelActual: {
      type: String,
      required: true,
      default: 'root'
    }
  },
  methods: {
    confirmDelete() {
      const itemsToDelete = this.items.map(item => item.title);
      if (itemsToDelete.length) {
        this.$emit('delete', itemsToDelete);
      }
      this.close();
    },
    close() {
      this.$emit('close');
    }
  }
};
</script>

<style lang="scss" scoped>
p {
  font-weight: 500;
  margin: 0 0 5px 0;
}

.pop-up {
  position: fixed;
  top: 0;
  left: 0;
  z-index: 1000;
  height: 100vh;
  width: 100vw;
  backdrop-filter: blur(1px);
  display: grid;
  place-items: center;

  &-inner {
    background-color: #fff;
    position: relative;
    width: 65%;
    max-width: 800px;
    padding: 25px;
    border: 2px solid #d7dadc;
    box-shadow: 0 5px 5px rgba(0, 0, 0, 0.2);
    border-radius: 6px;
  }

  &-close {
    position: absolute;
    height: 20px;
    width: 20px;
    top: 20px;
    right: 20px;
    cursor: pointer;
    display: flex;
    justify-content: center;
    align-items: center;
    color: #1168a6;
  }

  &-h1 {
    font-size: 14px;
    font-weight: 500;
    margin-bottom: 15px;
  }
}

.botonMenuEliminar {
  padding: 8px 16px;
  background: #a73535;
  color: #fff;
  border-radius: 2px;
  font-size: 12px;
  border: none;
  cursor: pointer;
  display: inline-flex;
  align-items: center;

  &:hover {
    background: #7c080d;
  }

  &:active {
    background: #230204;
  }
}

.botonMenuSecundario {
  padding: 8px 16px;
  background: none;
  color: #1168a6;
  border-radius: 2px;
  font-size: 12px;
  border: 1px solid #1168a6;
  cursor: pointer;
  display: inline-flex;
  align-items: center;

  &:hover {
    background: #e6f0fa;
  }
}

.listaArchivos {
  list-style: none;
  padding: 0;
  margin: 10px 0 0 0;
  max-height: 150px;
  overflow-y: auto;
}

.listaArchivos li {
  padding: 5px 0;
  border-bottom: 1px solid #e0e0e0;
}
</style>