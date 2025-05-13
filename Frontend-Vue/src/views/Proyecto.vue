<template>
  <div style="padding: 20px; border: 1px solid #ccc; width: 100%; box-sizing: border-box; display: flex; justify-content: space-between;">
    <div class="cards-container">
      <div
        class="card-widget"
        v-for="(card, index) in pruebas"
        :key="card.id"
        :class="{ 'active-card': card.active }"
      >
        <button class="card-check" @click="toggleCard(card)">
          {{ card.active ? '✔' : '' }}
        </button>

    <button class="card-close" @click="removeCard(index)">
      ✖
    </button>

    <div class="card-icon"></div>
    <div class="card-title">{{ card.title }}</div>
  </div>
</div>

<div class="button-container">
  <div class="upload-container">
    <div
      class="drag-area"
      @dragover.prevent="handleDragOver"
      @dragleave="handleDragLeave"
      @drop.prevent="handleDrop"
      :class="{ 'dragging': isDragging }"
    >
      <p v-if="!archivoSeleccionado">Arrastra un archivo aquí o haz clic para seleccionar</p>
      <input type="file" @change="manejarArchivo" ref="fileInput" class="file-input" />
    </div>

    <div v-if="archivoSeleccionado" class="file-preview">
      <p><strong>Archivo cargado:</strong></p>
      <div v-if="archivoSeleccionado.type.startsWith('image/')">
        <img :src="filePreview" alt="Vista previa" class="preview-image" />
      </div>
      <div v-else>
        <p>{{ archivoSeleccionado.name }}</p>
      </div>
      <div class="file-actions">
        <button class="confirm-button" @click="confirmarSubida">Confirmar Subida</button>
        <button class="remove-button" @click="removeFile">Eliminar Archivo</button>
      </div>
    </div>
  </div>

  <button class="buttonMenu" @click="executeSelectedPruebas">Ejecutar</button>

  <button class="buttonMenu" @click="toggleFilterDropdown">
    Filtros
    <span :class="{ 'rotate-arrow': filterDropdownOpen }" class="arrow-icon">▼</span>
  </button>

  <div v-if="filterDropdownOpen" class="filter-dropdown">
    <ul>
      <li>Filtro 1</li>
      <li>Filtro 2</li>
      <li>Filtro 3</li>
    </ul>
  </div>
</div>
  </div>

  <div v-if="respuestaBackend" style="margin-top: 15px; font-weight: bold; color: #1168a6">
    {{ respuestaBackend }}
  </div>
</template>

<script>
import axiosInstance from '../plugins/axios'
import useAlert from '@/stores/alert'

export default {
  name: 'ProyectoView',
  data() {
    return {
      respuestaBackend: null,
      pruebas: [],
      filterDropdownOpen: false,
      isDragging: false,
      archivoSeleccionado: null,
      filePreview: null,
      pruebasSeleccionadas: []
    }
  },
  mounted() {
    this.obtenerPruebas();
  },
  methods: {
    async obtenerPruebas() {
  try {
    const response = await axiosInstance.get('/test-regresion');
    console.log('Datos recibidos del backend:', response.data);  // Verifica lo que llega
    const jsonFiles = response.data;

    if (Array.isArray(jsonFiles) && jsonFiles.length > 0) {
      this.pruebas = jsonFiles.map((fileName, index) => ({
        id: index + 1,
        title: fileName,
        active: false
      }));
      this.respuestaBackend = "Archivos cargados correctamente.";
    } else {
      this.pruebas = [];
      this.respuestaBackend = "No se encontraron archivos.";
    }
  } catch (error) {
    this.respuestaBackend = null;
    console.error('Error:', error);
  }
},

    executeSelectedPruebas() {
      const selectedPruebas = this.pruebas.filter(prueba => prueba.active);
      if (selectedPruebas.length === 0) {
        alert('No hay pruebas seleccionadas.');
        return;
      }
      alert(`Se ejecutarán las pruebas: ${selectedPruebas.map(p => p.title).join(', ')}`);
    },

    ...useAlert(),

    toggleCard(prueba) {
      prueba.active = !prueba.active;
    }
  }
}
</script>

<style scoped>
/* Botones principales */
.buttonMenu {
  padding: 8px 16px;
  background: #1168a6;
  color: #fff;
  border-radius: 2px;
  font-size: 12px;
  border: none;
  cursor: pointer;
}
.buttonMenu:hover {
  background: #0c4974;
}
.buttonMenu:active {
  background: #0a3758;
}

/* Íconos de flechas */
.arrow-icon {
  margin-left: 5px;
  font-size: 12px;
  transition: transform 0.3s ease;
}
.rotate-arrow {
  transform: rotate(180deg);
}

/* Filtros desplegables */
.filter-dropdown {
  margin-top: 10px;
  background: #f4f4f4;
  border: 1px solid #ddd;
  padding: 10px;
  border-radius: 4px;
  width: 150px;
}
.filter-dropdown ul {
  list-style: none;
  padding: 0;
  margin: 0;
}
.filter-dropdown li {
  padding: 8px;
  cursor: pointer;
}
.filter-dropdown li:hover {
  background: #eaeaea;
}

/* Estilos contenedores */
.cards-container {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  padding: 20px 0;
  width: 70%;
  margin-right: 20px;
}

.button-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 25%;
  padding: 20px;
}

/* Estilos de las tarjetas */
.card-widget {
  position: relative;
  border: 2px solid #d7dadc;
  width: 160px;
  height: 230px;
  background: #fafafa;
  text-align: center;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 18px;
  border-radius: 4px;
  transition: border 0.3s, box-shadow 0.3s, background-color 0.3s;
}
.card-widget:hover {
  border-color: #5d87a1;
}

.card-widget.active-card {
  border-color: #1168a6;
  background-color: #eaf5fb;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

/* Estilos del check de las tarjetas */
.card-check {
  position: absolute;
  top: 0;
  left: 0;
  width: 24px;
  height: 24px;
  background: #d0d0d0;
  color: white;
  font-size: 16px;
  border: none;
  cursor: pointer;
  padding: 0;
  border-radius: 0;
}
.card-check.active {
  background: #1168a6;
}

/* Títulos e iconos dentro de las tarjetas */
.card-title {
  font-weight: bold;
  font-size: 14px;
  color: #1a3e5c;
  margin-top: 10px;
}
.card-icon {
  width: 60px;
  height: 60px;
  background: #eaeaea;
  margin-bottom: 10px;
  border-radius: 4px;
}

/* Estilo del botón de cierre de las tarjetas */
.card-close {
  position: absolute;
  top: 0;
  right: 0;
  width: 24px;
  height: 24px;
  background: #f44336;
  color: white;
  font-size: 16px;
  border: none;
  cursor: pointer;
  padding: 0;
  border-radius: 0;
}

/* Drag and Drop */
.upload-container {
  margin-top: 20px;
}
.drag-area {
  position: relative;
  padding: 20px;
  border: 2px dashed #1168a6;
  cursor: pointer;
  background-color: #f8f8f8;
  border-radius: 5px;
  transition: background-color 0.3s ease;
}
.drag-area.dragging {
  background-color: #eaeaea;
}
.file-input {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
}
.file-preview {
  margin-top: 20px;
  text-align: center;
}
.preview-image {
  max-width: 100%;
  max-height: 200px;
  margin-top: 10px;
}

/* Botones de confirmación y eliminación */
.confirm-button,
.remove-button {
  padding: 8px 16px;
  background: #1168a6;
  color: #fff;
  border-radius: 2px;
  font-size: 12px;
  border: none;
  cursor: pointer;
}
.confirm-button:hover,
.remove-button:hover {
  background: #0c4974;
}
</style>
