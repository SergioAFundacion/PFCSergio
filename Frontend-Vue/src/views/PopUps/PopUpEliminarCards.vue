<template>
  <div class="pop-up" v-if="show">
    <div class="pop-up-inner" v-click-outside="close">
      <div class="pop-up-close" @click="close">
        <!--<img src="@/assets/SVG/botonera/ico_close_blue.svg" alt="Cerrar" />-->
      </div>
      <h1 class="pop-up-h1">{{ $t('lang.proyecto.tituloEliminar') }}</h1>

      <div class="contInput" style="margin-bottom: 12px; text-align: left;">
        <template v-if="items.length > 0">
          <p>
            {{ nivelActual === 'root' ?
               $t('lang.proyecto.confirmarEliminacionCarpetas', { count: items.length }) :
               $t('lang.proyecto.confirmarEliminacionArchivos', { count: items.length }) }}
          </p>
          <ul class="file-list">
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

      <div style="display: flex; gap: 18px; justify-content: flex-end">
        <button @click="close" class="buttonMenuSecund">
          {{ $t('lang.proyecto.cancelar') }}
          <!--<img src="@/assets/SVG/botonera/ico_close_blue.svg" style="height: 13px; margin-left: 10px" />-->
        </button>
        <button
          v-if="items.length > 0"
          @click="confirmDelete"
          class="buttonMenuDestructive"
        >
          {{ $t('lang.proyecto.eliminar') }}
          <!--<img src="@/assets/SVG/botonera/ico_trash_white.svg" style="height: 13px; margin-left: 10px" />-->
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

.buttonMenuDestructive {
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

.buttonMenuSecund {
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

.file-list {
  list-style: none;
  padding: 0;
  margin: 10px 0 0 0;
  max-height: 150px;
  overflow-y: auto;
}

.file-list li {
  padding: 5px 0;
  border-bottom: 1px solid #e0e0e0;
}
</style>