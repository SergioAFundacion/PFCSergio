<template>
  <div class="pop-up" v-if="show">
    <div class="pop-up-inner">

      <h1 class="pop-up-h1">{{ $t('lang.proyecto.tituloAñadir') }}</h1>

      <!-- Input para el nombre de la carpeta (solo en root) -->
      <h2 v-if="nivelActual === 'root'">{{ $t('lang.proyecto.nombreCarpeta') }}</h2>
      <input
        v-if="nivelActual === 'root'"
        type="text"
        v-model="nombreCarpeta"
        :placeholder="$t('lang.proyecto.placeholderNombreCarpeta')"
        class="input-nombre-carpeta"
      />

      <!-- Drag & drop + selector de archivos -->
      <div
        class="drag-area"
        @click="abrirSelectorArchivos"
        @dragover.prevent="handleDragOver"
        @dragleave="handleDragLeave"
        @drop.prevent="handleDrop"
        :class="{ dragging: isDragging }"
      >
        <p>{{ $t('lang.proyecto.textoContenedorSubida') }}</p>
        <input
          type="file"
          multiple
          @change="manejarArchivo"
          ref="fileInput"
          class="file-input-hidden"
        />
      </div>

      <!-- Lista de archivos seleccionados -->
      <div v-if="archivoSeleccionado.length" class="contenedorArchivosSeleccionados">
        <h3>{{ $t('lang.proyecto.archivosSeleccionados') }}</h3>
        <ul class="listaArchivos">
          <li v-for="(file, index) in archivoSeleccionado" :key="index" class="filaArchivo">
            <span class="nombreArchivo">{{ file.name }}</span>
            <button @click="removeFile(index)" class="botonQuitarArchivo">✖</button>
          </li>
        </ul>
      </div>

      <!-- Mensaje si no se selecciona ningún archivo -->
      <p v-else style="color: red;">
        {{ $t('lang.proyecto.seleccionarAlMenosUnArchivo') }}
      </p>

      <!-- Botones -->
      <div class="buttons-container" style="text-align: right; margin-top: 20px;">
        <button @click="close" class="buttonMenuSecund">
          {{ $t('lang.proyecto.cancelar') }}
        </button>

        <button
          :disabled="(nivelActual === 'root' && !nombreCarpeta) || archivoSeleccionado.length === 0"
          @click="confirmarSubida"
          class="botonMenuPrimario"
        >
          {{ $t('lang.proyecto.aceptar') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>

export default {
  name: 'PopUpAñadirCards',

  props: {
    show: {
      type: Boolean,
      required: true
    },
    nivelActual: {
      type: String,
      required: true,
      default: 'root'
    },
    carpetasSubidas: {
      type: Array,
      required: false,
      default: () => []
    }
  },
  data() {
    return {
      nombreCarpeta: '',
      archivoSeleccionado: [],
      isDragging: false
    };
  },
  methods: {
    close() {
      this.nombreCarpeta = '';
      this.archivoSeleccionado = [];
      this.isDragging = false;
      if (this.$refs.fileInput) {
        this.$refs.fileInput.value = null;
      }
      this.$emit('close');
    },
    handleDragOver() {
      this.isDragging = true;
    },
    handleDragLeave() {
      this.isDragging = false;
    },
    handleDrop(event) {
      this.isDragging = false;
      const files = Array.from(event.dataTransfer.files);
      this.agregarArchivos(files);
    },
    abrirSelectorArchivos() {
      this.$refs.fileInput.click();
    },
    manejarArchivo(event) {
      const files = Array.from(event.target.files);
      this.agregarArchivos(files);
    },
    agregarArchivos(files) {
      const nuevosArchivos = [];
      files.forEach(file => {
        if (!file || !file.name) return;
        const yaExiste = this.archivoSeleccionado.some(
          f => f.name === file.name && f.size === file.size
        );
        if (yaExiste) {
          this.$emit('alert', {
            title: this.$t('lang.utils.warning'),
            typeData: 'warning',
            descripcionData: this.$t('lang.proyecto.archivoYaSeleccionado')
          });
        } else {
          nuevosArchivos.push(file);
        }
      });
      if (nuevosArchivos.length) {
        this.archivoSeleccionado = [...this.archivoSeleccionado, ...nuevosArchivos];
      }
    },
    removeFile(index) {
      this.archivoSeleccionado.splice(index, 1);
      if (this.archivoSeleccionado.length === 0 && this.$refs.fileInput) {
        this.$refs.fileInput.value = null;
      }
    },
    async confirmarSubida() {
      if (this.nivelActual === 'root' && !this.nombreCarpeta.trim()) {
        this.$emit('alert', {
          title: this.$t('lang.utils.warning'),
          typeData: 'warning',
          descripcionData: this.$t('lang.proyecto.debeEscribirNombreCarpeta')
        });
        return;
      }
      if (!this.archivoSeleccionado.length) {
        this.$emit('alert', {
          title: this.$t('lang.utils.warning'),
          typeData: 'warning',
          descripcionData: this.$t('lang.proyecto.seleccionarAlMenosUnArchivo')
        });
        return;
      }
      if (this.nivelActual === 'root') {
        const carpetaExistente = this.carpetasSubidas.some(carpeta => carpeta.title === this.nombreCarpeta);
        if (carpetaExistente) {
          this.$emit('alert', {
            title: this.$t('lang.utils.error'),
            typeData: 'error',
            descripcionData: this.$t('lang.proyecto.carpetaYaExiste')
          });
          return;
        }
      }
      const nombreCarpetaFinal = this.nivelActual === 'root' ? this.nombreCarpeta : this.nivelActual;
      this.$emit('add', {
        nombreCarpeta: nombreCarpetaFinal,
        archivos: this.archivoSeleccionado
      });
      this.close();
    }
  }
};
</script>

<style lang="scss" scoped>
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
    font-weight: bold;
    margin-bottom: 15px;
  }
}

.botonMenuPrimario {
  padding: 8px 16px;
  font-size: 12px;
  border-radius: 4px;
  border: #fff;
  color: #fff;
  background-color: #1168a6;
  cursor: pointer;
  display: inline-flex;
  align-items: center;

  &:hover {
    background-color: #0c4974;
  }

  &:disabled {
    background-color: #cccccc;
    cursor: #cccccc;
  }
}

.buttonMenuSecund {
  padding: 8px 16px;
  font-size: 12px;
  border-radius: 2px;
  border: 2px solid #1168a6;
  color: #1168a6;
  background-color: transparent;
  cursor: pointer;
  display: inline-flex;
  align-items: center;

  &:hover {
    background-color: #e6f0fa;
  }
}

.drag-area {
  border: 2px dashed #1168a6;
  padding: 20px;
  text-align: center;
  cursor: pointer;
  transition: background-color 0.3s ease-in-out;
}

.drag-area.dragging {
  background-color: #e6f1ff;
}

.file-input-hidden {
  display: none;
}

.contenedorArchivosSeleccionados {
  margin-top: 10px;
}

.listaArchivos {
  list-style: none;
  padding: 0;
  margin: 0;
}

.filaArchivo {
  display: flex;
  align-items: center;
  justify-content-between: space-between;
  padding: 6px 0;
  border-bottom: 1px #000;
}

.nombreArchivo {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-right: 10px;
}

.botonQuitarArchivo {
  background-color: transparent;
  border: red;
  color: #ff0000;
  font-size: 16px;
  cursor: pointer;
}

p {
  font-weight: normal;
  margin: 0 0 10px;
}
</style>