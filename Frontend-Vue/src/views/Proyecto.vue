<template>
  <!-- Contenedor general de la interfaz -->
  <div class="contenedor">
    <!-- Contenedor de botones -->
    <div class="contenedorBotones">
      <button v-if="nivelActual !== 'root'" @click="volverAtras" class="botonMenu">
        {{ $t('lang.proyecto.volver') }}
      </button>
      <input type="text" v-model="busqueda" :placeholder="$t('lang.proyecto.buscarArchivo')" class="buscador" />
      <button class="botonMenu" v-if="filtroEnEdicion" @click="guardarCambiosFiltro">
        {{ $t('lang.proyecto.guardarCambios') }}
      </button>
      <!-- Grupo de botones para abrir modales de añadir, eliminar y editar -->
      <div>
        <button class="botonModal" @click="abrirModal('Añadir')">
          {{ $t('lang.proyecto.Añadir') }}
        </button>
        <button class="botonModal" @click="abrirModal('Eliminar')">
          {{ $t('lang.proyecto.Eliminar') }}
        </button>
        <button class="botonModal" @click="abrirModal('Editar')">
          {{ $t('lang.proyecto.Editar') }}
        </button>
      </div>
    </div>

    <!-- Contenedor principal -->
    <div class="contenedorPrincipal">
      <!-- Contenedor de tarjetas -->
      <div class="contenedorCards">
        <div class="cardWidget" v-for="card in cardsFiltradas" :key="card.id" :class="{ 'cardActiva': card.active }"
          @click="toggleCard(card)" @dblclick="nivelActual === 'root' ? abrirExplorador(card) : null">
          <div class="iconoCard">
            <img :src="card.isFolder ? cardCarpeta : cardPrueba" alt="icono" style="width: 100%; max-height: 100px;" />
          </div>
          <div class="tituloCard">{{ card.title }}</div>
        </div>
      </div>

      <!-- Contenedor de filtros -->
      <div class="contenedorFiltros">
        <div class="limpiarFiltrosContenedorBotones">
          <button :class="{ visible: filtrosActivos.length > 0, invisible: filtrosActivos.length === 0 }"
            class="botonMenu botonLimpiarFiltros" @click="limpiarFiltros">
            {{ $t('lang.proyecto.limpiarFiltros') }}
          </button>
        </div>

        <div v-if="archivoSeleccionado.length" class="previsualizacion">
          <ul>
            <li v-for="(file, index) in archivoSeleccionado" :key="file.name">
              {{ file.name }}
            </li>
          </ul>
          <div class="botonesPrevisualizacion">
            <button class="mitadAncho" @click="confirmarSubida">
              {{ $t('lang.proyecto.confirmar') }}
            </button>
            <button class="botonMenu mitadAncho" @click="quitarArchivo">
              {{ $t('lang.proyecto.cancelar') }}
            </button>
          </div>
        </div>

        <!-- Filtros -->
        <button class="botonMenu" @click="toggleFilterDropdown">
          {{ $t('lang.proyecto.filtros') }}
          <span :class="{ 'rotarFlecha': filterDropdownOpen }" class="iconoFlecha">▼</span>
        </button>

        <div v-if="filterDropdownOpen" class="filter-dropdown">
          <div class="nuevo-filtro">
            <input v-model="nuevoFiltroNombre" :placeholder="$t('lang.proyecto.nuevoFiltro')" class="inputFiltro"
              @keyup.enter="crearNuevoFiltro" />
            <button class="botonCrearFiltro" @click="crearNuevoFiltro">+</button>
          </div>
          <ul>
            <li v-for="filtro in filtros" :key="filtro" :class="{ 'filtroActivo': filtrosActivos.includes(filtro) }">
              <div class="filtro-item" @click="filtrarYSeleccionar(filtro)">
                <span>{{ filtro }}</span>
                <button class="botonEliminarFiltro" @click.stop="eliminarFiltro(filtro)">🗑</button>
              </div>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <!-- Componente para añadir nuevas carpetas o archivos -->
    <PopUpAñadirCards v-if="showAddModal" :show="showAddModal" :nivelActual="nivelActual" :carpetasSubidas="cards"
      @close="closeAddModal" @add="handleAdd" @alert="sendMessageAlert" />

    <!-- Componente para eliminar elementos seleccionados -->
    <PopUpEliminarCards v-if="showDeleteModal" :show="showDeleteModal" :items="cardsSeleccionadas"
      :nivelActual="nivelActual" @close="closeDeleteModal" @delete="handleDelete" @alert="sendMessageAlert" />

    <!-- Componente para mostrar notificaciones -->  
    <Toast />

    <!-- Modal Editor -->
    <div v-if="editorAbierto" class="modal-overlay" @click="cerrarEditor" :aria-hidden="!editorAbierto">
      <div class="contenidoModal" @click.stop>
        <h3 v-if="nivelActual === 'root'">
          {{ $t('lang.proyecto.renombrarCarpeta') }}: {{ cardEditando.title }}
        </h3>
        <h3 v-else>
          {{ $t('lang.proyecto.editando') }}: {{ cardEditando.title }}
        </h3>
        <div v-if="nivelActual === 'root'">
          <input v-model="nuevoNombreCarpeta" :placeholder="$t('lang.proyecto.nuevoNombreCarpeta')"
            class="input-nombre-carpeta" />
        </div>
        <textarea v-else v-model="cardEditando.contenido" class="editor-textarea"
          placeholder="Ingresa o edita el contenido JSON del archivo" rows="15"
          style="font-family: monospace;"></textarea>
        <div class="botonesModal">
          <button class="botonMenu" @click="guardarCambios">
            {{ $t('lang.proyecto.guardar') }}
          </button>
          <button class="botonMenu" @click="cerrarEditor">
            {{ $t('lang.proyecto.cancelar') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
/* Importaciones de dependencias y componentes */
import axiosInstance from '@/plugins/axios';
import useAlertStore from '@/stores/useAlert.js';
import Toast from '@/components/Toast.vue';
import icono_prueba from '@/assets/icono_prueba.svg';
import icono_carpeta from '@/assets/icono_carpeta.svg';
import PopUpAñadirCards from './PopUps/PopUpAñadirCards.vue';
import PopUpEliminarCards from './PopUps/PopUpEliminarCards.vue';

export default {
  name: 'Proyecto',
  components: {
    PopUpAñadirCards,
    PopUpEliminarCards,
    Toast
  },
  /* Estado inicial del componente */
  data() {
    return {
      cardPrueba: icono_prueba,
      cardCarpeta: icono_carpeta,
      cards: [],
      filterDropdownOpen: false,
      isDragging: false,
      archivoSeleccionado: [],
      filePreview: [],
      cardsSeleccionadas: [],
      filtros: [],
      filtrosActivos: [],
      filtroEnEdicion: null,
      nuevoNombreFiltro: '',
      editorAbierto: false,
      cardEditando: null,
      showModal: false,
      modalContent: '',
      nuevoNombreCarpeta: '',
      fileId: null,
      busqueda: '',
      nuevoFiltroNombre: '',
      nivelActual: 'root',
      historialNavegacion: [],
      showAddModal: false,
      showDeleteModal: false
    };
  },

  /* Inicialización del componente */
  async mounted() {
    await this.obtenerCards();
    try {
      const response = await axiosInstance.get('/listar-nombres-filtros');
      this.filtros = response.data;
    } catch (error) {
    }
  },

  /* Propiedades computadas */
  computed: {
    // Filtra las tarjetas según la búsqueda y el nivel actual
    cardsFiltradas() {
      const texto = this.busqueda.trim().toLowerCase();
      let elementos = [];

      if (this.nivelActual === 'root') {
        elementos = this.cards.filter(card => card.isFolder);
      } else {
        const carpetaActual = this.cards.find(card => card.isFolder && card.title === this.nivelActual);
        elementos = carpetaActual?.archivos.map((archivo, index) => ({
          id: `${carpetaActual.id}-${index}`,
          title: archivo,
          isFolder: false,
          active: this.cardsSeleccionadas.some(selected => selected.id === `${carpetaActual.id}-${index}`),
          carpetaId: carpetaActual.id
        })) || [];
      }

      if (!texto) return elementos;
      return elementos.filter(card => card.title.toLowerCase().includes(texto));
    }
  },
  /* Configuración inicial y métodos de alertas */
  setup() {
    const alertStore = useAlertStore();

    // Método para mostrar alertas de éxito o error
    const sendMessageAlert = ({ title, description, type }) => {
      if (type === 'success') {
        alertStore.showSuccess({ title, description });
      } else if (type === 'error') {
        alertStore.showError({ title, description });
      } else {
        console.error('Tipo de alerta no válido:', type);
        alertStore.showError({ title: 'Error', description: 'Tipo de alerta no especificado' });
      }
    };
    return { alertStore, sendMessageAlert };
  },
  methods: {
     /* Obtiene la lista de carpetas desde el servidor */
    async obtenerCards() {
      try {
        const response = await axiosInstance.get('/test-regresion');
        const folders = Array.isArray(response.data) ? response.data : [];

        const newCards = folders.map((folder, index) => ({
          id: `folder-${index + 1}`,
          title: folder.nombreCarpeta || `Carpeta-${index + 1}`,
          archivos: Array.isArray(folder.archivosCarpeta) ? [...folder.archivosCarpeta] : [], // Clonar para reactividad
          isFolder: true,
          active: false
        }));

        this.cards.splice(0, this.cards.length, ...newCards);
      } catch (error) {
        console.error('Error al obtener carpetas:', error.response || error);
        this.sendMessageAlert({
          title: this.$t('lang.proyecto.error'),
          type: 'error',
          description: this.$t('lang.proyecto.errorLlamada') || 'Error al cargar datos'
        });
      }
    },

    /* Alterna la selección de una tarjeta */
    toggleCard(card) {
      card.active = !card.active;
      this.updateSelectedCards(card);
    },

    /* Actualiza la lista de tarjetas seleccionadas */
    updateSelectedCards(card) {
      if (card.active) {
        if (!this.cardsSeleccionadas.some(selected => selected.id === card.id)) {
          this.cardsSeleccionadas.push({ ...card });
        }
      } else {
        this.cardsSeleccionadas = this.cardsSeleccionadas.filter(
          selected => selected.id !== card.id
        );
      }
    },

    /* Navega al interior de una carpeta */
    abrirExplorador(carpeta) {
      if (!carpeta.isFolder) return;
      this.historialNavegacion.push(this.nivelActual);
      this.nivelActual = carpeta.title;
      this.cardsSeleccionadas = [];
      this.cards.forEach(card => (card.active = false));
    },

    /* Vuelve al nivel anterior en la navegación */
    volverAtras() {
      if (this.historialNavegacion.length) {
        this.nivelActual = this.historialNavegacion.pop();
      } else {
        this.nivelActual = 'root';
      }
      // Limpiar selección al volver
      this.cardsSeleccionadas = [];
      this.cards.forEach(card => (card.active = false));
    },

    /* Confirma la eliminación de múltiples tarjetas */
    async confirmarEliminarMultipleCards(nombresEliminados) {
      let successCount = 0;
      let errorCount = 0;

      try {
        if (this.nivelActual === 'root') {
          // Eliminar carpetas en nivel raíz
          for (const nombre of nombresEliminados) {
            try {
              await axiosInstance.post('/delete-carpeta', [nombre]);
              const index = this.cards.findIndex(card => card.isFolder && card.title === nombre);
              if (index !== -1) {
                this.cards.splice(index, 1);
                successCount++;
              }
            } catch (error) {
              console.error(`Error al eliminar carpeta ${nombre}:`, error.response || error);
              errorCount++;
            }
          }
        } else {
          // Eliminar archivos
          const itemsToDelete = nombresEliminados.map(nombre => `${this.nivelActual}/${nombre}`);
          try {
            await axiosInstance.post('/delete-carpeta', itemsToDelete);
            const carpetaIndex = this.cards.findIndex(
              card => card.isFolder && card.title === this.nivelActual
            );
            if (carpetaIndex !== -1) {
              const carpeta = this.cards[carpetaIndex];
              nombresEliminados.forEach(nombre => {
                const archivoIndex = carpeta.archivos.indexOf(nombre);
                if (archivoIndex !== -1) {
                  carpeta.archivos.splice(archivoIndex, 1);
                  successCount++;
                } else {
                  console.warn(`Archivo no encontrado en carpeta: ${nombre}`);
                }
              });
              this.cards[carpetaIndex] = { ...carpeta };
            } else {
              console.warn(`Carpeta no encontrada: ${this.nivelActual}`);
              errorCount += nombresEliminados.length;
            }
          } catch (error) {
            console.error(`Error al eliminar archivos ${itemsToDelete.join(', ')}:`, error.response || error);
            errorCount += nombresEliminados.length;
          }
        }

        // Limpiar selección
        this.cardsSeleccionadas = [];
        this.cards.forEach(card => (card.active = false));

        // Mostrar mensaje
        if (successCount > 0 && errorCount === 0) {
          this.sendMessageAlert({
            title: this.$t('lang.proyecto.success'),
            type: 'success',
            description: this.nivelActual === 'root'
              ? this.$t('lang.proyecto.carpetaEliminada')
              : this.$t('lang.proyecto.archivosEliminados') || 'Archivos eliminados correctamente'
          });
        } else if (errorCount > 0) {
          this.sendMessageAlert({
            title: this.$t('lang.proyecto.error'),
            type: 'error',
            description: `${errorCount} ${this.$t('lang.proyecto.errorEliminar') || 'Errores al eliminar'}`
          });
        }
      } catch (error) {
        console.error('Error general al eliminar:', error);
        this.sendMessageAlert({
          title: this.$t('lang.proyecto.error'),
          type: 'error',
          description: this.$t('lang.proyecto.errorEliminar') || 'Error al eliminar'
        });
      }
    },

    /* Inicia el proceso de añadir una nueva carpeta */
    agregarNuevaCarpeta({ nombreCarpeta, archivos }) {
      this.subirCarpetaConArchivos(nombreCarpeta, archivos);
    },

    /* Sube una carpeta con sus archivos al servidor */
    async subirCarpetaConArchivos(nombreCarpeta, archivosSeleccionados) {
      if (this.nivelActual === 'root') {
        // Verificar si el nombre de la carpeta ya existe
        const carpetaExistente = this.cards.some(card => card.title === nombreCarpeta);
        if (carpetaExistente) {
          this.sendMessageAlert({
            title: this.$t('lang.proyecto.error'),
            type: 'error',
            description: this.$t('lang.proyecto.carpetaYaExiste') || 'El nombre de la carpeta ya está en uso'
          });
          return;
        }
      }

      let newCard;
      if (this.nivelActual === 'root') {
        newCard = {
          id: `folder-${Date.now()}`,
          title: nombreCarpeta,
          archivos: archivosSeleccionados.map(file => file.name),
          isFolder: true,
          active: false,
        };
        this.cards.push(newCard);
      } else {
        const carpetaActualIndex = this.cards.findIndex(card => card.title === this.nivelActual);
        if (carpetaActualIndex !== -1) {
          const nuevosArchivos = [
            ...this.cards[carpetaActualIndex].archivos,
            ...archivosSeleccionados.map(file => file.name),
          ];
          const updatedCard = { ...this.cards[carpetaActualIndex], archivos: nuevosArchivos };
          this.cards.splice(carpetaActualIndex, 1, updatedCard);
        } else {
          this.sendMessageAlert({
            title: this.$t('lang.proyecto.error'),
            type: 'error',
            description: this.$t('lang.proyecto.carpetaNoEncontrada') || 'La carpeta especificada no existe'
          });
          return;
        }
      }

      try {
        const formData = new FormData();
        formData.append('nombreCarpeta', nombreCarpeta);
        for (const file of archivosSeleccionados) {
          formData.append('files', file);
        }

        await axiosInstance.post('/upload-carpeta', formData, {
          headers: { 'Content-Type': 'multipart/form-data' },
        });

        this.sendMessageAlert({
          title: this.$t('lang.proyecto.success'),
          type: 'success',
          description: this.nivelActual === 'root'
            ? this.$t('lang.proyecto.carpetaYArchivosSubidos')
            : this.$t('lang.proyecto.archivosSubidos') || 'Archivos añadidos correctamente'
        });

        await this.obtenerCards();
      } catch (error) {
        this.sendMessageAlert({
          title: this.$t('lang.proyecto.error'),
          type: 'error',
          description: this.$t('lang.proyecto.errorSubida')
        });
      }
    },

    /* Alterna la visibilidad del desplegable de filtros */
    toggleFilterDropdown() {
      this.filterDropdownOpen = !this.filterDropdownOpen;
      if (this.filterDropdownOpen && this.filtros.length === 0) {
        this.obtenerFiltros();
      }
    },

    /* Obtiene la lista de filtros desde el servidor */
    async obtenerFiltros() {
      try {
        const response = await axiosInstance.get('/listar-nombres-filtros');
        this.filtros = response.data;
      } catch (error) {
        console.error('Error al obtener los filtros:', error);
      }
    },

    /* Aplica o elimina un filtro y actualiza las tarjetas */
    async filtrarYSeleccionar(nombreFiltro) {
      const index = this.filtrosActivos.indexOf(nombreFiltro);
      if (index !== -1) {
        this.filtrosActivos.splice(index, 1);
      } else {
        this.filtrosActivos.push(nombreFiltro);
      }
      await this.aplicarFiltros();
    },

    /* Aplica los filtros activos a las tarjetas */
    async aplicarFiltros() {
      if (this.filtrosActivos.length === 0) {
        this.cards.forEach(card => (card.active = false));
        this.cardsSeleccionadas = [];
        this.archivoSeleccionado = [];
        this.cardsFiltradas = [...this.cardsFiltradas]; // Forzar reactividad
        return;
      }

      try {
        const archivosAcumulados = new Set();
        for (const filtro of this.filtrosActivos) {
          const response = await axiosInstance.get(`/obtener-archivos-filtro/${filtro}`);
          response.data.forEach(archivo => archivosAcumulados.add(archivo));
        }

        // Limpiar selecciones previas
        this.cards.forEach(card => (card.active = false));
        this.cardsSeleccionadas = [];
        this.archivoSeleccionado = [];

        // Procesar cada elemento del filtro
        archivosAcumulados.forEach(nombre => {
          const [carpetaNombre, archivoNombre] = nombre.includes('/')
            ? nombre.split('/')
            : [nombre, null];

          // Buscar si el nombre corresponde a una carpeta
          const carpeta = this.cards.find(card => card.isFolder && card.title === carpetaNombre);
          if (carpeta && !archivoNombre) {
            // Marcar la carpeta como activa
            carpeta.active = true;
            if (!this.cardsSeleccionadas.some(c => c.id === carpeta.id)) {
              this.cardsSeleccionadas.push({ ...carpeta });
            }

            // Añadir archivos de la carpeta a cardsSeleccionadas si estamos en root
            if (this.nivelActual === 'root' && carpeta.archivos && Array.isArray(carpeta.archivos)) {
              carpeta.archivos.forEach((archivo, index) => {
                const archivoObj = {
                  id: `${carpeta.id}-${index}`,
                  title: archivo,
                  isFolder: false,
                  active: true,
                  carpetaId: carpeta.id
                };
                if (!this.cardsSeleccionadas.some(c => c.id === archivoObj.id)) {
                  this.cardsSeleccionadas.push(archivoObj);
                }
              });
            }
          } else if (carpeta && archivoNombre) {
            // Si es un archivo dentro de una carpeta
            if (carpeta.archivos && carpeta.archivos.includes(archivoNombre)) {
              const index = carpeta.archivos.indexOf(archivoNombre);
              const archivoObj = {
                id: `${carpeta.id}-${index}`,
                title: archivoNombre,
                isFolder: false,
                active: true,
                carpetaId: carpeta.id
              };
              if (!this.cardsSeleccionadas.some(c => c.id === archivoObj.id)) {
                this.cardsSeleccionadas.push(archivoObj);
              }
            }
          }
        });

        // Actualizar cardsFiltradas para reflejar el estado activo
        if (this.nivelActual !== 'root') {
          const carpetaActual = this.cards.find(card => card.isFolder && card.title === this.nivelActual);
          if (carpetaActual) {
            this.cardsFiltradas = carpetaActual.archivos.map((archivo, index) => ({
              id: `${carpetaActual.id}-${index}`,
              title: archivo,
              isFolder: false,
              active: this.cardsSeleccionadas.some(selected => selected.id === `${carpetaActual.id}-${index}`),
              carpetaId: carpetaActual.id
            })).filter(card => {
              const texto = this.busqueda.trim().toLowerCase();
              return !texto || card.title.toLowerCase().includes(texto);
            });
          }
        } else {
          this.cardsFiltradas = this.cards.filter(card => card.isFolder).map(card => ({
            ...card,
            active: this.cardsSeleccionadas.some(selected => selected.id === card.id)
          }));
        }

        // Forzar reactividad
        this.cards = [...this.cards];
        this.cardsFiltradas = [...this.cardsFiltradas];

      } catch (error) {
        console.error('Error al aplicar los filtros acumulados:', error);
        this.sendMessageAlert({
          title: this.$t('lang.proyecto.error'),
          type: 'error',
          description: this.$t('lang.proyecto.errorAplicarFiltros'),
        });
      }
    },

    /* Selecciona archivos asociados a un filtro */
    async seleccionarArchivosDeFiltro(nombreFiltro) {
      try {
        const response = await axiosInstance.get(`/obtener-archivos-filtro/${nombreFiltro}`);
        const archivosDelFiltro = response.data;

        // Limpiar selecciones previas
        this.cards.forEach(card => (card.active = false));
        this.cardsSeleccionadas = [];
        this.archivoSeleccionado = [];

        // Procesar cada elemento del filtro
        archivosDelFiltro.forEach(nombre => {
          const [carpetaNombre, archivoNombre] = nombre.includes('/')
            ? nombre.split('/')
            : [nombre, null];

          const carpeta = this.cards.find(card => card.isFolder && card.title === carpetaNombre);
          if (carpeta && !archivoNombre) {
            // Marcar la carpeta como activa
            carpeta.active = true;
            if (!this.cardsSeleccionadas.some(c => c.id === carpeta.id)) {
              this.cardsSeleccionadas.push({ ...carpeta });
            }

            // Añadir archivos de la carpeta a cardsSeleccionadas
            if (carpeta.archivos && Array.isArray(carpeta.archivos)) {
              carpeta.archivos.forEach((archivo, index) => {
                const archivoObj = {
                  id: `${carpeta.id}-${index}`,
                  title: archivo,
                  isFolder: false,
                  active: true,
                  carpetaId: carpeta.id
                };
                if (!this.cardsSeleccionadas.some(c => c.id === archivoObj.id)) {
                  this.cardsSeleccionadas.push(archivoObj);
                }
              });
            }
          } else if (carpeta && archivoNombre) {
            // Si es un archivo dentro de una carpeta
            if (carpeta.archivos && carpeta.archivos.includes(archivoNombre)) {
              const index = carpeta.archivos.indexOf(archivoNombre);
              const archivoObj = {
                id: `${carpeta.id}-${index}`,
                title: archivoNombre,
                isFolder: false,
                active: true,
                carpetaId: carpeta.id
              };
              if (!this.cardsSeleccionadas.some(c => c.id === archivoObj.id)) {
                this.cardsSeleccionadas.push(archivoObj);
              }
              // Si estamos en la carpeta correspondiente, marcar el archivo como activo
              if (this.nivelActual === carpetaNombre) {
                const cardFiltrada = this.cardsFiltradas.find(c => c.id === archivoObj.id);
                if (cardFiltrada) {
                  cardFiltrada.active = true;
                }
              }
            }
          }
        });

        // Forzar reactividad en cardsFiltradas
        this.cardsFiltradas = [...this.cardsFiltradas];

      } catch (error) {
        console.error('Error al obtener los archivos del filtro:', error);
        this.sendMessageAlert({
          title: this.$t('lang.proyecto.error'),
          type: 'error',
          description: this.$t('lang.proyecto.errorCargarFiltro'),
        });
      }
    },

    /* Elimina un filtro del servidor y la interfaz */
    async eliminarFiltro(nombreFiltro) {
      try {
        await axiosInstance.delete(`/eliminar-filtro/${encodeURIComponent(nombreFiltro)}`);
        this.filtros = this.filtros.filter(f => f !== nombreFiltro);
        this.filtrosActivos = this.filtrosActivos.filter(f => f !== nombreFiltro);
        await this.aplicarFiltros();
        this.sendMessageAlert({
          title: this.$t('lang.proyecto.success'),
          type: 'success',
          description: this.$t('lang.proyecto.filtroEliminado'),
        });
      } catch (error) {
        console.error('Error al eliminar el filtro:', error);
        this.sendMessageAlert({
          title: this.$t('lang.proyecto.error'),
          type: 'error',
          description: this.$t('lang.proyecto.errorEliminarFiltro'),
        });
      }
    },

    /* Crea un nuevo filtro con los archivos seleccionados */
    async crearNuevoFiltro() {
      const nombreFiltro = this.nuevoFiltroNombre.trim();
      if (!nombreFiltro) {
        this.sendMessageAlert({
          title: this.$t('lang.proyecto.warning'),
          type: 'warning',
          description: this.$t('lang.proyecto.nombreFiltroVacio'),
        });
        return;
      }

      if (this.filtros.includes(nombreFiltro)) {
        this.sendMessageAlert({
          title: this.$t('lang.proyecto.warning'),
          type: 'warning',
          description: this.$t('lang.proyecto.filtroYaExiste'),
        });
        return;
      }

      if (!this.cardsSeleccionadas.length) {
        this.sendMessageAlert({
          title: this.$t('lang.proyecto.warning'),
          type: 'warning',
          description: this.$t('lang.proyecto.filtroSinTarjetas'),
        });
        return;
      }

      try {
        // Crear una lista de archivos para el filtro, incluyendo los archivos de las carpetas seleccionadas
        const archivosParaFiltro = [];
        this.cardsSeleccionadas.forEach(card => {
          if (card.isFolder) {
            // Si es una carpeta, añadir todos sus archivos
            const carpeta = this.cards.find(c => c.id === card.id);
            if (carpeta && carpeta.archivos && Array.isArray(carpeta.archivos)) {
              carpeta.archivos.forEach(archivo => {
                archivosParaFiltro.push(`${carpeta.title}/${archivo}`);
              });
            }
          } else {
            // Si es un archivo, añadirlo con la ruta completa (carpeta/archivo)
            archivosParaFiltro.push(`${card.carpetaId ? this.cards.find(c => c.id === card.carpetaId).title : this.nivelActual}/${card.title}`);
          }
        });

        // Guardar el filtro en la base de datos
        await axiosInstance.post('/guardar-filtro', {
          nombre: nombreFiltro,
          archivos: archivosParaFiltro
        });

        this.filtros.push(nombreFiltro);
        this.nuevoFiltroNombre = '';
        this.cardsSeleccionadas = [];
        this.cards.forEach(card => (card.active = false));
        this.sendMessageAlert({
          title: this.$t('lang.proyecto.success'),
          type: 'success',
          description: this.$t('lang.proyecto.filtroCreado'),
        });
      } catch (error) {
        console.error('Error al guardar el filtro:', error);
        this.sendMessageAlert({
          title: this.$t('lang.proyecto.error'),
          type: 'error',
          description: this.$t('lang.proyecto.errorCrearFiltro'),
        });
      }
    },

    /* Gestiona el evento de arrastrar sobre el área de carga */
    handleDragOver() {
      this.isDragging = true;
    },

    
    /* Gestiona el evento de salir del área de carga */
    handleDragLeave() {
      this.isDragging = false;
    },

    /* Gestiona el evento de soltar archivos en el área de carga */
    handleDrop(event) {
      this.isDragging = false;
      const files = Array.from(event.dataTransfer.files);
      this.manejarArchivo({ target: { files } });
    },

    /* Procesa los archivos seleccionados para subir */
    manejarArchivo(event) {
      const files = Array.from(event.target.files);
      const nuevosArchivos = [];

      files.forEach(file => {
        const yaExiste =
          this.cards.some(card => card.title === file.name) ||
          this.archivoSeleccionado.some(f => f.name === file.name);

        if (yaExiste) {
          this.sendMessageAlert({
            title: this.$t('lang.proyecto.warning'),
            type: 'warning',
            description: this.$t('lang.proyecto.archivoYaSubido'),
          });
        } else {
          nuevosArchivos.push(file);
        }
      });

      this.archivoSeleccionado.push(...nuevosArchivos);
    },

    /* Elimina los archivos seleccionados para subir */
    quitarArchivo() {
      this.archivoSeleccionado = [];
      this.filePreview = [];
      this.$refs.fileInput.value = null;
    },

    /* Limpia los filtros que estan activos y se deseleccionan las cards */
    limpiarFiltros() {
      this.filtrosActivos = [];
      this.cards.forEach(card => (card.active = false));
      this.cardsSeleccionadas = [];
      this.filtroEnEdicion = null;
    },

    /* Confirma la subida de archivos al servidor */
    async confirmarSubida() {
      if (!this.archivoSeleccionado.length) {
        this.sendMessageAlert({
          title: this.$t('lang.proyecto.warning'),
          type: 'warning',
          description: this.$t('lang.proyecto.noArchivosSeleccionados')
        });
        return;
      }

      try {
        const archivos = this.archivoSeleccionado.map(file => ({
          nombre: file.name,
          carpetaId: file.carpetaId
        }));
        await axiosInstance.post('/subir-archivos', { archivos });
        this.sendMessageAlert({
          title: this.$t('lang.proyecto.success'),
          type: 'success',
          description: this.$t('lang.proyecto.subidaExitosa')
        });
        this.archivoSeleccionado = [];
      } catch (error) {
        console.error('Error al subir archivos:', error);
        this.sendMessageAlert({
          title: this.$t('lang.proyecto.error'),
          type: 'error',
          description: this.$t('lang.proyecto.errorSubida')
        });
      }
    },

    /* Abre el editor para una tarjeta específica */
    async abrirEditor(card) {
      this.closeEditPopUp();
      if (card && card.title) {
        this.cardEditando = { ...card };
        if (this.nivelActual === 'root') {
          this.nuevoNombreCarpeta = card.title;
        } else {
          await this.obtenerContenidoArchivo(card.title);
        }
        this.editorAbierto = true;
      } else {
        console.error('El card no contiene title');
        this.sendMessageAlert({
          title: 'Error',
          type: 'error',
          description: 'Selección inválida',
        });
      }
    },

    /* Obtiene el contenido de un archivo desde el servidor */
    async obtenerContenidoArchivo(title) {
      try {
        const nombreArchivo = `${this.nivelActual.replace(/[\\/]+$/, '')}/${title}`.replace(/\\/g, '/');
        const response = await axiosInstance.get('/obtener-contenido', {
          params: { nombreArchivo },
        });
        let contenido = response.data.contenido || '';
        try {
          if (contenido) {
            JSON.parse(contenido);
          }
        } catch (error) {
          console.warn('Contenido no es JSON válido:', error);
        }
        // Reemplazar this.$set
        this.cardEditando.contenido = contenido;
      } catch (error) {
        console.error('Error al obtener contenido:', error.response || error);
        this.sendMessageAlert({
          title: 'Error',
          type: 'error',
          description: 'Error al cargar el contenido',
        });
        this.cardEditando.contenido = '';
      }
    },

    /* Guarda los cambios realizados en el editor */
    async guardarCambios() {
      try {
        if (this.nivelActual === 'root') {
          if (!this.nuevoNombreCarpeta.trim()) {
            this.sendMessageAlert({
              title: 'Error',
              type: 'error',
              description: 'El nombre de la carpeta no puede estar vacío',
            });
            return;
          }
          if (!/^[a-zA-Z0-9_-]+$/.test(this.nuevoNombreCarpeta)) {
            this.sendMessageAlert({
              title: 'Error',
              type: 'error',
              description: 'El nombre de la carpeta solo puede contener letras, números, guiones y guiones bajos',
            });
            return;
          }
          if (this.nuevoNombreCarpeta === this.cardEditando.title) {
            this.sendMessageAlert({
              title: 'Información',
              type: 'info',
              description: 'El nombre de la carpeta no ha cambiado',
            });
            return;
          }
          if (
            this.cards.some(
              card =>
                card.isFolder &&
                card.title === this.nuevoNombreCarpeta &&
                card.id !== this.cardEditando.id
            )
          ) {
            this.sendMessageAlert({
              title: 'Error',
              type: 'error',
              description: 'Ya existe una carpeta con ese nombre',
            });
            return;
          }
          const response = await axiosInstance.post('/rename-carpeta', {
            oldName: this.cardEditando.title,
            newName: this.nuevoNombreCarpeta,
          });
          console.log('Respuesta de /rename-carpeta:', response);

          const index = this.cards.findIndex(card => card.id === this.cardEditando.id);
          if (index !== -1) {
            this.cards[index] = { ...this.cards[index], title: this.nuevoNombreCarpeta };
          } else {
            console.warn('Carpeta no encontrada en cards:', this.cardEditando.id);
          }
          this.sendMessageAlert({
            title: 'Éxito',
            type: 'success',
            description: 'Carpeta renombrada correctamente',
          });
        } else {
          if (this.cardEditando && typeof this.cardEditando.contenido === 'string') {
            let contenido = this.cardEditando.contenido.trim();
            try {
              if (contenido) {
                JSON.parse(contenido);
              }
            } catch (error) {
              console.error('JSON inválido:', error);
              this.sendMessageAlert({
                title: 'Error',
                type: 'error',
                description: 'Contenido JSON inválido',
              });
              return;
            }

            const nombreArchivo = `${this.nivelActual.replace(/[\\/]+$/, '')}/${this.cardEditando.title}`.replace(/\\/g, '/');
            const response = await axiosInstance.post('/guardar-contenido', {
              nombreArchivo,
              contenido,
            });
            console.log('Respuesta de /guardar-contenido:', response);

            const index = this.cards.findIndex(card => card.id === this.cardEditando.id);
            if (index !== -1) {
              this.cards[index] = { ...this.cards[index], contenido };
            } else {
              console.warn('Archivo no encontrado en cards:', this.cardEditando.id);
            }
            this.sendMessageAlert({
              title: 'Éxito',
              type: 'success',
              description: 'Archivo guardado correctamente',
            });
          } else {
            console.error('cardEditando inválido:', this.cardEditando);
            this.sendMessageAlert({
              title: 'Error',
              type: 'error',
              description: 'Contenido del archivo inválido',
            });
          }
        }
      } catch (error) {
        console.error('Error al guardar cambios:', {
          message: error.message,
          response: error.response?.data,
          status: error.response?.status,
        });
        this.sendMessageAlert({
          title: 'Error',
          type: 'error',
          description:
            this.nivelActual === 'root'
              ? error.response?.data?.error || error.message || 'Error al renombrar la carpeta'
              : error.response?.data?.error || error.message || 'Error al guardar el archivo',
        });
      } finally {
          this.cerrarEditor();
      }
    },

    /* Cierra el editor modal */
    cerrarEditor() {
      this.editorAbierto = false;
      this.cardEditando = null;
      this.nuevoNombreCarpeta = '';
    },

    /* Abre el modal correspondiente (Añadir, Eliminar, Editar) */
    async abrirModal(modalName) {

      if (modalName === 'Añadir') {
        this.showAddModal = true;
      } else if (modalName === 'Eliminar') {
        if (this.cardsSeleccionadas.length === 0) {
          this.sendMessageAlert({
            title: this.$t('lang.proyecto.warning'),
            type: 'warning',
            description: this.$t('lang.proyecto.seleccionarAlMenosUnElemento')
          });
          return;
        }
        this.showDeleteModal = true;
      } else if (modalName === 'Editar') {
        if (this.cardsSeleccionadas.length !== 1) {
          this.alertFallback({
            title: this.$t('lang.proyecto.warning'),
            type: 'warning',
            description: this.$t('lang.proyecto.seleccionarUnaSolaTarjeta')
          });
          return;
        }

        this.cardEditando = { ...this.cardsSeleccionadas[0] };
        this.nuevoNombreCarpeta = this.cardEditando.title;

        if (this.nivelActual !== 'root') {
          try {
            const response = await axiosInstance.get('/get-contenido', {
              params: {
                nombreArchivo: `${this.nivelActual}/${this.cardEditando.title}`
              }
            });
            console.log('Respuesta de /get-contenido:', response.data);
            this.cardEditando.contenido = response.data.contenido
              ? (typeof response.data.contenido === 'object'
                ? JSON.stringify(response.data.contenido, null, 2)
                : response.data.contenido)
              : '';
          } catch (error) {
            console.error('Error al cargar el contenido:', error);
            this.alertFallback({
              title: this.$t('lang.proyecto.error'),
              type: 'error',
              description: this.$t('lang.proyecto.errorCargarContenido')
            });
            this.cardEditando.contenido = '';
          }
        } else {
          this.cardEditando.contenido = '';
        }

        this.editorAbierto = true;
      }
    },

    /* Cierra el modal de añadir elementos */
    closeAddModal() {
      this.showAddModal = false;
    },

    /* Maneja la adición de una nueva carpeta o archivos */
    handleAdd({ nombreCarpeta, archivos }) {
      this.subirCarpetaConArchivos(nombreCarpeta, archivos);
      this.closeAddModal();
    },

    /* Cierra el modal de eliminación */
    closeDeleteModal() {
      this.showDeleteModal = false;
    },

    /* Maneja la eliminación de elementos seleccionados */
    handleDelete(itemsToDelete) {
      this.confirmarEliminarMultipleCards(itemsToDelete);
      this.closeDeleteModal();
    },
  },
};
</script>

<style scoped>
/* Estilos para los botones */
.botonMenu {
  padding: 12px 24px;
  background: #1168a6;
  color: #fff;
  border-radius: 6px;
  font-size: 16px;
  border: none;
  cursor: pointer;
}

.botonMenu:hover {
  background: #0c4974;
}

.botonMenu:active {
  background: #0a3758;
}

.botonModal {
  padding: 12px 24px;
  background: #1168a6;
  color: #fff;
  border-radius: 6px;
  font-size: 16px;
  border: none;
  cursor: pointer;
  margin-right: 8px;
}
/* Estilos generales */
.contenedor {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 0px 20px;
  margin: 0;
  width: 100%;
  box-sizing: border-box;
}

.contenedorBotones {
  display: flex;
  gap: 10px;
  align-items: center;
  width: 100%;
  justify-content: flex-start;
  border-radius: 6px;
}

.contenedorPrincipal {
  display: flex;
  gap: 20px;
  width: 100%;
}

.contenedorCards {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  width: 100%;
}

.contenedorFiltros {
  width: 30%;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* Estilos de las tarjetas */
.cardWidget {
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

.cardWidget:hover {
  border-color: #5d87a1;
}

.cardWidget.cardActiva {
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
}

.card-check.active {
  background: #1168a6;
}

/* Títulos e iconos dentro de las tarjetas */
.tituloCard {
  font-weight: bold;
  font-size: 14px;
  color: #1a3e5c;
  margin-top: 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100%;
  text-align: center;
}


.iconoCard {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 10px;
  width: 80px;
  height: 80px;
  background: transparent;
}

.iconoCard-img {
  max-height: 100%;
  max-width: 80px;
  object-fit: contain;
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
}

/* Drag and Drop */
.drag-area {
  position: relative;
  padding: 20px;
  border: 2px dashed #1168a6;
  cursor: pointer;
  background-color: #f8f8f8;
  border-radius: 6px;
  transition: background-color 0.3s ease;
}

.drag-area.dragging {
  background-color: #eaeaea;
}

.limpiarFiltrosContenedorBotones {
  height: 40px;
  position: relative;
}

.botonLimpiarFiltros {
  transition: opacity 0.3s ease;
}

.botonLimpiarFiltros.invisible {
  visibility: hidden;
  opacity: 0;
  pointer-events: none;
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.botonLimpiarFiltros.visible {
  visibility: visible;
  opacity: 1;
  pointer-events: auto;
  position: relative;
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

.previsualizacion {
  margin-top: 20px;
  text-align: center;
}

.preview-image {
  max-width: 100%;
  max-height: 200px;
  margin-top: 10px;
}

.botonesPrevisualizacion {
  display: flex;
  gap: 0.5rem;
  /* Espacio entre los botones */
  margin-top: 0.5rem;
}

.mitadAncho {
  flex: 1;
  /* Hace que ambos botones compartan el mismo ancho */
}

/* Filtros desplegables */
.filter-dropdown {
  margin-top: 10px;
  background: #f4f4f4;
  border: 1px solid #ddd;
  padding: 10px;
  border-radius: 6px;
  width: fit-content;
  min-width: 100%; 
  box-sizing: border-box;
}

.filter-dropdown ul {
  list-style: none;
  padding: 0;
  margin: 0;
  width: 100%;
}

.filter-dropdown li {
  padding: 8px 12px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.filter-dropdown li:hover {
  background-color: #f0f0f0;
}

.filter-dropdown li.filtroActivo {
  background-color: #1168a6;
  color: white;
  border-bottom: 1px solid white;
}

/* Estilos adicionales */
.file-actions {
  display: flex;
  gap: 10px;
}

.iconoFlecha {
  transition: transform 0.3s ease;
}

.rotarFlecha {
  transform: rotate(180deg);
}

/* Botón para limpiar filtros */
.botonLimpiarFiltros {
  background-color: #1168a6;
  color: white;
  padding: 8px 16px;
  margin: 10px 0;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  width: 100%;
  transition: background-color 0.3s ease;
}

.botonLimpiarFiltros:hover {
  background-color: #1168a6;
}

.contImg {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  flex-direction: row;
  background: #f7f8f8;
  padding: 32px;
  min-height: 132px;
}

.card {
  border: solid #d7dadc 2px;
  padding: 32px;
  width: 200px;
  cursor: pointer;
}

/*Estilos de la ventana del editor*/
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
}

.contenidoModal {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  width: 80%;
  max-width: 800px;
}

.editor-textarea {
  width: 100%;
  height: 400px;
}

.botonesModal {
  margin-top: 10px;
  text-align: right;
}

.card-edit {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 24px;
  height: 24px;
  background: #1168a6;
  color: white;
  font-size: 16px;
  border: none;
  cursor: pointer;
  padding: 0;
  border-radius: 4px;
}

.input-nombre-carpeta {
  width: 100%;
  padding: 8px;
  font-size: 14px;
  border: 1px solid #ccc;
  border-radius: 4px;
  margin-bottom: 12px;
}

.buscador {
  padding: 12px 24px;
  margin: 0 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
  width: 200px;
}

.nuevo-filtro {
  display: flex;
  gap: 0.5rem;
  padding: 0.5rem;
  align-items: center;
}

.inputFiltro {
  flex-grow: 1;
  padding: 0.25rem 0.5rem;
}

.botonCrearFiltro {
  padding: 0.5rem 0.75rem;
  font-size: 1rem;
  background-color: #1168a6;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  line-height: 1;
}

.botonCrearFiltro:hover {
  background-color: #0056b3;
}

.filtro-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 12px;
  width: 100%;
  box-sizing: border-box;
}

.botonEditarFiltro {
  background: transparent;
  border: 1px solid black;
  cursor: pointer;
  font-size: 16px;
  color: black;
  margin-left: auto;
}

.botonEliminarFiltro {
  background: transparent;
  border: 1px solid black;
  cursor: pointer;
  font-size: 16px;
  color: black;
  margin-left: 5px;
}

.filtroActivo {
  background-color: #e0f7fa;
  font-weight: bold;
}

.input-nombre-filtro {
  margin-right: 15px;
}
</style>