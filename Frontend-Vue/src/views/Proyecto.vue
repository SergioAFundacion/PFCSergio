<template>
    <div class="container">
        <!-- Contenedor de botones -->
        <div class="button-container">
            <button v-if="nivelActual !== 'root'" @click="volverAtras" class="buttonMenu">
                {{ $t('lang.proyecto.volver') }}
            </button>
            <input type="text" v-model="busqueda" :placeholder="$t('lang.proyecto.buscarArchivo')"
                class="buscador" />
            <button class="buttonMenu" v-if="filtroEnEdicion" @click="guardarCambiosFiltro">
                {{ $t('lang.proyecto.guardarCambios') }}
            </button>
            <div class="sidebar">
            <button class="buttonMenu" @click="openModal('Añadir')">
                {{ $t('lang.proyecto.Añadir') }}
            </button>
            <button class="buttonMenu" @click="openModal('Eliminar')">
                {{ $t('lang.proyecto.Eliminar') }}
            </button>
            <button class="buttonMenu" @click="openModal('Editar')">
                {{ $t('lang.proyecto.Editar') }}
            </button>
        </div>
        </div>

        <!-- Contenedor principal -->
        <div class="content-container">
            <!-- Contenedor de tarjetas -->
            <div class="cards-container">
                <div class="card-widget" v-for="card in cardsFiltradas" :key="card.id"
                    :class="{ 'active-card': card.active }" @click="toggleCard(card)"
                    @dblclick="nivelActual === 'root' ? openExplorer(card) : null">
                    <div class="card-icon">
                        <img :src="card.isFolder ? cardCarpeta : cardPrueba" alt="icono"
                            style="width: 100%; max-height: 100px;" />
                    </div>
                    <div class="card-title">{{ card.title }}</div>
                </div>
            </div>

            <!-- Contenedor de carga -->
            <div class="upload-container">
                <div class="clear-filters-button-container">
                    <button :class="{ visible: filtrosActivos.length > 0, invisible: filtrosActivos.length === 0 }"
                        class="buttonMenu clear-filters-button" @click="clearAllFilters">
                        {{ $t('lang.proyecto.eliminarFiltros') }}
                    </button>
                </div>

                <div v-if="archivoSeleccionado.length" class="file-preview">
                    <ul>
                        <li v-for="(file, index) in archivoSeleccionado" :key="file.name">
                            {{ file.name }}
                        </li>
                    </ul>
                    <div class="preview-buttons">
                        <button class="buttonMenu half-width" @click="confirmarSubida">
                            {{ $t('lang.proyecto.confirmar') }}
                        </button>
                        <button class="buttonMenu half-width" @click="removeFile">
                            {{ $t('lang.proyecto.cancelar') }}
                        </button>
                    </div>
                </div>

                <!-- Filtros -->
                <button class="buttonMenu" @click="toggleFilterDropdown">
                    {{ $t('lang.proyecto.filtros') }}
                    <span :class="{ 'rotate-arrow': filterDropdownOpen }" class="arrow-icon">▼</span>
                </button>

                <div v-if="filterDropdownOpen" class="filter-dropdown">
                    <div class="nuevo-filtro">
                        <input v-model="nuevoFiltroNombre" :placeholder="$t('lang.proyecto.nuevoFiltro')"
                            class="inputFiltro" @keyup.enter="crearNuevoFiltro" />
                        <button class="buttonCrearFiltro" @click="crearNuevoFiltro">+</button>
                    </div>
                    <ul>
                        <li v-for="filtro in filtros" :key="filtro"
                            :class="{ 'active-filter': filtrosActivos.includes(filtro) }">
                            <div class="filtro-item" @click="filtrarYSeleccionar(filtro)">
                                <span>{{ filtro }}</span>
                                <button class="botonEditarFiltro" @click.stop="editarFiltro(filtro)">✎</button>
                                <button class="botonEliminarFiltro" @click.stop="eliminarFiltro(filtro)">🗑</button>
                            </div>
                            <div v-if="filtroEnEdicion === filtro" class="edicion-filtro">
                                <input v-model="nuevoNombreFiltro" placeholder="Nuevo nombre"
                                    class="input-nombre-filtro" />
                                <button @click.stop="guardarCambiosFiltro">✔</button>
                                <button @click.stop="cancelarEdicionFiltro">✖</button>
                            </div>
                        </li>
                    </ul>
                </div>
            </div>
        </div>

        <PopUpAñadirCards
          v-if="showAddModal"
          :show="showAddModal"
          :nivelActual="nivelActual"
          :carpetasSubidas="cards"
          @close="closeAddModal"
          @add="handleAdd"
          @alert="sendMessageAlert"
        />

        <PopUpEliminarCards
          v-if="showDeleteModal"
          :show="showDeleteModal"
          :items="cardsSeleccionadas"
          :nivelActual="nivelActual"
          @close="closeDeleteModal"
          @delete="handleDelete"
          @alert="sendMessageAlert"
        />

        <!-- Modal Editor -->
        <div v-if="editorAbierto" class="modal-overlay" @click="cerrarEditor" :aria-hidden="!editorAbierto">
          <div class="modal-content" @click.stop>
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
              placeholder="Ingresa o edita el contenido JSON del archivo" rows="15" style="font-family: monospace;"></textarea>
            <div class="modal-buttons">
              <button class="buttonMenu" @click="guardarCambios">
                {{ $t('lang.proyecto.guardar') }}
              </button>
              <button class="buttonMenu" @click="cerrarEditor">
                {{ $t('lang.proyecto.cancelar') }}
              </button>
            </div>
          </div>
        </div>
    </div>
</template>

<script>
import axiosInstance from '@/plugins/axios';
import useAlert from '@/stores/alert.js';
import icono_prueba from '@/assets/icono_prueba.svg';
import icono_carpeta from '@/assets/icono_carpeta.svg';
import PopUpAñadirCards from './PopUps/PopUpAñadirCards.vue';
import PopUpEliminarCards from './PopUps/PopUpEliminarCards.vue';

export default {
  name: 'Proyecto',
  components: {
    PopUpAñadirCards,
    PopUpEliminarCards
  },
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

    async mounted() {
        await this.obtenerCards();
        try {
            const response = await axiosInstance.get('/listar-nombres-filtros');
            this.filtros = response.data;
        } catch (error) {
            console.error('Error al cargar filtros:', error);
        }
    },

    computed: {
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

    methods: {
        ...useAlert(),

        async obtenerCards() {
            try {
                const response = await axiosInstance.get('/test-regresion');
                console.log('Respuesta de /test-regresion:', response.data);
                const folders = Array.isArray(response.data) ? response.data : [];

                const newCards = folders.map((folder, index) => ({
                    id: `folder-${index + 1}`,
                    title: folder.nombreCarpeta || `Carpeta-${index + 1}`,
                    archivos: Array.isArray(folder.archivosCarpeta) ? [...folder.archivosCarpeta] : [], // Clonar para reactividad
                    isFolder: true,
                    active: false
                }));

                this.cards.splice(0, this.cards.length, ...newCards);
                console.log('Cards actualizadas:', this.cards);
            } catch (error) {
                console.error('Error al obtener carpetas:', error.response || error);
                this.sendMessageAlert({
                    title: this.$t('lang.proyecto.error'),
                    typeData: 'error',
                    descripcionData: this.$t('lang.proyecto.errorLlamada') || 'Error al cargar datos'
                });
            }
        },

        toggleCard(card) {
            card.active = !card.active;
            this.updateSelectedCards(card);
        },

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

        openExplorer(carpeta) {
            if (!carpeta.isFolder) return;
            this.historialNavegacion.push(this.nivelActual);
            this.nivelActual = carpeta.title;
            // Limpiar selección al cambiar de nivel
            this.cardsSeleccionadas = [];
            this.cards.forEach(card => (card.active = false));
        },

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
        async confirmarEliminarMultipleCards(nombresEliminados) {
            console.log('Eliminando:', nombresEliminados, 'nivelActual:', this.nivelActual);
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
                    console.log('Rutas enviadas:', itemsToDelete);
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
                            // No es necesario this.$set en Vue 3
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
                console.log('Cards después de eliminar:', this.cards);
                console.log('Cards filtradas después de eliminar:', this.cardsFiltradas);

                // Mostrar mensaje
                if (successCount > 0 && errorCount === 0) {
                    this.sendMessageAlert({
                        title: this.$t('lang.proyecto.success'),
                        typeData: 'success',
                        descripcionData: this.nivelActual === 'root'
                            ? this.$t('lang.proyecto.carpetaEliminada')
                            : this.$t('lang.proyecto.archivosEliminados') || 'Archivos eliminados correctamente'
                    });
                } else if (errorCount > 0) {
                    this.sendMessageAlert({
                        title: this.$t('lang.proyecto.error'),
                        typeData: 'error',
                        descripcionData: `${errorCount} ${this.$t('lang.proyecto.errorEliminar') || 'Errores al eliminar'}`
                    });
                }
            } catch (error) {
                console.error('Error general al eliminar:', error);
                this.sendMessageAlert({
                    title: this.$t('lang.proyecto.error'),
                    typeData: 'error',
                    descripcionData: this.$t('lang.proyecto.errorEliminar') || 'Error al eliminar'
                });
            }
        },

        agregarNuevaCarpeta({ nombreCarpeta, archivos }) {
            this.subirCarpetaConArchivos(nombreCarpeta, archivos);
        },

        async subirCarpetaConArchivos(nombreCarpeta, archivosSeleccionados) {
            if (this.nivelActual === 'root') {
                // Verificar si el nombre de la carpeta ya existe
                const carpetaExistente = this.cards.some(card => card.title === nombreCarpeta);
                if (carpetaExistente) {
                    this.sendMessageAlert({
                        title: this.$t('lang.proyecto.error'),
                        typeData: 'error',
                        descripcionData: this.$t('lang.proyecto.carpetaYaExiste') || 'El nombre de la carpeta ya está en uso'
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
                        typeData: 'error',
                        descripcionData: this.$t('lang.proyecto.carpetaNoEncontrada') || 'La carpeta especificada no existe'
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
                    typeData: 'success',
                    descripcionData: this.nivelActual === 'root'
                        ? this.$t('lang.proyecto.carpetaYArchivosSubidos')
                        : this.$t('lang.proyecto.archivosSubidos') || 'Archivos añadidos correctamente'
                });

                await this.obtenerCards();
            } catch (error) {
                this.sendMessageAlert({
                    title: this.$t('lang.proyecto.error'),
                    typeData: 'error',
                    descripcionData: this.$t('lang.proyecto.errorSubida')
                });
            }
        },

        toggleFilterDropdown() {
            this.filterDropdownOpen = !this.filterDropdownOpen;
            if (this.filterDropdownOpen && this.filtros.length === 0) {
                this.obtenerFiltros();
            }
        },

        async obtenerFiltros() {
            try {
                const response = await axiosInstance.get('/listar-nombres-filtros');
                this.filtros = response.data;
            } catch (error) {
                console.error('Error al obtener los filtros:', error);
            }
        },

        async filtrarYSeleccionar(nombreFiltro) {
          const index = this.filtrosActivos.indexOf(nombreFiltro);
          if (index !== -1) {
              this.filtrosActivos.splice(index, 1);
          } else {
              this.filtrosActivos.push(nombreFiltro);
          }
          await this.aplicarFiltros();
        },

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

              // Actualizar cardsFiltradas para reflejar el estado active
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

              console.log('cardsSeleccionadas después de aplicar filtros:', this.cardsSeleccionadas);
              console.log('cardsFiltradas después de aplicar filtros:', this.cardsFiltradas);

          } catch (error) {
              console.error('Error al aplicar los filtros acumulados:', error);
              this.sendMessageAlert({
                  title: this.$t('lang.proyecto.error'),
                  typeData: 'error',
                  descripcionData: this.$t('lang.proyecto.errorAplicarFiltros'),
              });
          }
        },

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
                  typeData: 'error',
                  descripcionData: this.$t('lang.proyecto.errorCargarFiltro'),
              });
          }
      },
        editarFiltro(nombreFiltro) {
            this.filtroEnEdicion = nombreFiltro;
            this.nuevoNombreFiltro = nombreFiltro;
            this.seleccionarArchivosDeFiltro(nombreFiltro);
        },

        cancelarEdicionFiltro() {
            this.filtroEnEdicion = null;
            this.nuevoNombreFiltro = '';
            this.filtrosActivos = [];
            this.cards.forEach(card => (card.active = false));
            this.cardsSeleccionadas = [];
        },

        async guardarCambiosFiltro() {
            const payload = {
                nombre: this.nuevoNombreFiltro,
                nombreAntiguo: this.filtroEnEdicion,
                archivos: this.cardsSeleccionadas.map(card => card.title),
            };

            try {
                await axiosInstance.put('/editar-filtros', payload);
                const i = this.filtros.indexOf(this.filtroEnEdicion);
                if (i !== -1) {
                    this.filtros.splice(i, 1, this.nuevoNombreFiltro);
                }
                const j = this.filtrosActivos.indexOf(this.filtroEnEdicion);
                if (j !== -1) {
                    this.filtrosActivos.splice(j, 1, this.nuevoNombreFiltro);
                }
                this.filtroEnEdicion = null;
                this.nuevoNombreFiltro = '';
                await this.aplicarFiltros();
            } catch (error) {
                console.error('Error al actualizar filtro:', error);
                this.sendMessageAlert({
                    title: this.$t('lang.proyecto.error'),
                    typeData: 'error',
                    descripcionData: this.$t('lang.proyecto.errorActualizarFiltro'),
                });
            }
        },

        async eliminarFiltro(nombreFiltro) {
            try {
                await axiosInstance.delete(`/eliminar-filtro/${encodeURIComponent(nombreFiltro)}`);
                this.filtros = this.filtros.filter(f => f !== nombreFiltro);
                this.filtrosActivos = this.filtrosActivos.filter(f => f !== nombreFiltro);
                await this.aplicarFiltros();
                this.sendMessageAlert({
                    title: this.$t('lang.proyecto.success'),
                    typeData: 'success',
                    descripcionData: this.$t('lang.proyecto.filtroEliminado'),
                });
            } catch (error) {
                console.error('Error al eliminar el filtro:', error);
                this.sendMessageAlert({
                    title: this.$t('lang.proyecto.error'),
                    typeData: 'error',
                    descripcionData: this.$t('lang.proyecto.errorEliminarFiltro'),
                });
            }
        },

        async crearNuevoFiltro() {
          const nombreFiltro = this.nuevoFiltroNombre.trim();
          if (!nombreFiltro) {
              this.sendMessageAlert({
                  title: this.$t('lang.proyecto.warning'),
                  typeData: 'warning',
                  descripcionData: this.$t('lang.proyecto.nombreFiltroVacio'),
              });
              return;
          }

          if (this.filtros.includes(nombreFiltro)) {
              this.sendMessageAlert({
                  title: this.$t('lang.proyecto.warning'),
                  typeData: 'warning',
                  descripcionData: this.$t('lang.proyecto.filtroYaExiste'),
              });
              return;
          }

          if (!this.cardsSeleccionadas.length) {
              this.sendMessageAlert({
                  title: this.$t('lang.proyecto.warning'),
                  typeData: 'warning',
                  descripcionData: this.$t('lang.proyecto.filtroSinTarjetas'),
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
                  typeData: 'success',
                  descripcionData: this.$t('lang.proyecto.filtroCreado'),
              });
          } catch (error) {
              console.error('Error al guardar el filtro:', error);
              this.sendMessageAlert({
                  title: this.$t('lang.proyecto.error'),
                  typeData: 'error',
                  descripcionData: this.$t('lang.proyecto.errorCrearFiltro'),
              });
          }
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
            this.manejarArchivo({ target: { files } });
        },

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
                        typeData: 'warning',
                        descripcionData: this.$t('lang.proyecto.archivoYaSubido'),
                    });
                } else {
                    nuevosArchivos.push(file);
                }
            });

            this.archivoSeleccionado.push(...nuevosArchivos);
        },

        removeFile() {
            this.archivoSeleccionado = [];
            this.filePreview = [];
            this.$refs.fileInput.value = null;
        },

        clearAllFilters() {
            this.filtrosActivos = [];
            this.cards.forEach(card => (card.active = false));
            this.cardsSeleccionadas = [];
            this.filtroEnEdicion = null;
        },

        async confirmarSubida() {
            if (!this.archivoSeleccionado.length) {
                this.sendMessageAlert({
                    title: this.$t('lang.proyecto.warning'),
                    typeData: 'warning',
                    descripcionData: this.$t('lang.proyecto.noArchivosSeleccionados')
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
                    typeData: 'success',
                    descripcionData: this.$t('lang.proyecto.subidaExitosa')
                });
                this.archivoSeleccionado = [];
            } catch (error) {
                console.error('Error al subir archivos:', error);
                this.sendMessageAlert({
                    title: this.$t('lang.proyecto.error'),
                    typeData: 'error',
                    descripcionData: this.$t('lang.proyecto.errorSubida')
                });
            }
        },

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
                    title: this.$t('lang.proyecto.error'),
                    typeData: 'error',
                    descripcionData: this.$t('lang.proyecto.errorSeleccion')
                });
            }
        },

        async obtenerContenidoArchivo(nombreArchivo) {
            try {
                const response = await axiosInstance.get('/get-contenido', {
                    params: { nombreArchivo: `${this.nivelActual}/${nombreArchivo}` }
                });
                if (this.cardEditando) {
                    this.cardEditando.contenido = response.data.contenido;
                }
            } catch (error) {
                console.error('Error al obtener el contenido del archivo:', error);
                this.sendMessageAlert({
                    title: this.$t('lang.proyecto.error'),
                    typeData: 'error',
                    descripcionData: this.$t('lang.proyecto.errorContenidoArchivo')
                });
            }
        },

        async guardarCambios() {
          try {
            if (this.nivelActual === 'root') {
              console.log('Renombrando carpeta:', {
                oldName: this.cardEditando.title,
                newName: this.nuevoNombreCarpeta,
                cardEditando: this.cardEditando,
                oldNameLength: this.cardEditando.title.length,
                oldNameChars: this.cardEditando.title.split('').map(c => c.charCodeAt(0)),
                newNameLength: this.nuevoNombreCarpeta.length,
                newNameChars: this.nuevoNombreCarpeta.split('').map(c => c.charCodeAt(0))
              });
              if (!this.nuevoNombreCarpeta.trim()) {
                this.sendMessageAlert({
                  title: this.$t('lang.utils.error'),
                  typeData: 'error',
                  descripcionData: this.$t('lang.proyecto.nombreCarpetaInvalido')
                });
                return;
              }
              if (!/^[a-zA-Z0-9_-]+$/.test(this.nuevoNombreCarpeta)) {
                this.sendMessageAlert({
                  title: this.$t('lang.utils.error'),
                  typeData: 'error',
                  descripcionData: this.$t('lang.proyecto.nombreCarpetaCaracteresInvalidos')
                });
                return;
              }
              if (this.nuevoNombreCarpeta === this.cardEditando.title) {
                this.cerrarEditor();
                return;
              }
              if (this.cards.some(card => card.isFolder && card.title === this.nuevoNombreCarpeta && card.id !== this.cardEditando.id)) {
                this.sendMessageAlert({
                  title: this.$t('lang.utils.error'),
                  typeData: 'error',
                  descripcionData: this.$t('lang.proyecto.nombreCarpetaEnUso')
                });
                return;
              }

              await axiosInstance.post('/rename-carpeta', {
                oldName: this.cardEditando.title,
                newName: this.nuevoNombreCarpeta
              });
              const index = this.cards.findIndex(card => card.id === this.cardEditando.id);
              if (index !== -1) {
                this.cards[index].title = this.nuevoNombreCarpeta;
                this.cards = [...this.cards];
              }
              this.sendMessageAlert({
                title: this.$t('lang.utils.success'),
                typeData: 'success',
                descripcionData: this.$t('lang.proyecto.carpetaRenombrada')
              });
            } else {
              if (this.cardEditando && typeof this.cardEditando.contenido === 'string') {
                let contenido = this.cardEditando.contenido.trim();
                try {
                  if (contenido) {
                    JSON.parse(contenido);
                  }
                } catch (error) {
                  this.sendMessageAlert({
                    title: this.$t('lang.utils.error'),
                    typeData: 'error',
                    descripcionData: this.$t('lang.proyecto.contenidoJsonInvalido')
                  });
                  return;
                }
                await axiosInstance.post('/guardar-contenido', {
                  nombreArchivo: `${this.nivelActual}/${this.cardEditando.title}`,
                  contenido: contenido
                });
                const index = this.cards.findIndex(card => card.id === this.cardEditando.id);
                if (index !== -1) {
                  this.cards[index].contenido = contenido;
                  this.cards = [...this.cards];
                }
                this.sendMessageAlert({
                  title: this.$t('lang.utils.success'),
                  typeData: 'success',
                  descripcionData: this.$t('lang.proyecto.archivoGuardado')
                });
              } else {
                this.sendMessageAlert({
                  title: this.$t('lang.utils.error'),
                  typeData: 'error',
                  descripcionData: this.$t('lang.proyecto.errorContenidoArchivo')
                });
              }
            }
            this.cerrarEditor();
          } catch (error) {
            console.error('Error al guardar cambios:', error);
            this.sendMessageAlert({
              title: this.$t('lang.utils.error'),
              typeData: 'error',
              descripcionData: this.nivelActual === 'root'
                ? this.$t('lang.proyecto.errorRenombrar')
                : this.$t('lang.proyecto.errorContenidoArchivo')
            });
          }
        },

        cerrarEditor() {
            this.editorAbierto = false;
            this.cardEditando = null;
            this.nuevoNombreCarpeta = '';
        },

        async openModal(modalName) {
          console.log(`Abriendo modal: ${modalName}, cardsSeleccionadas:`, this.cardsSeleccionadas);
          
          if (modalName === 'Añadir') {
            this.showAddModal = true;
          } else if (modalName === 'Eliminar') {
            if (this.cardsSeleccionadas.length === 0) {
              this.alertFallback({
                title: this.$t('lang.utils.warning'),
                typeData: 'warning',
                descripcionData: this.$t('lang.proyecto.seleccionarAlMenosUnElemento')
              });
              return;
            }
            this.showDeleteModal = true;
          } else if (modalName === 'Editar') {
            if (this.cardsSeleccionadas.length !== 1) {
              this.alertFallback({
                title: this.$t('lang.utils.warning'),
                typeData: 'warning',
                descripcionData: this.$t('lang.proyecto.seleccionarUnaSolaTarjeta')
              });
              console.log('Validación fallida: cardsSeleccionadas.length !== 1');
              return;
            }

            this.cardEditando = { ...this.cardsSeleccionadas[0] };
            this.nuevoNombreCarpeta = this.cardEditando.title;
            console.log('cardEditando inicializado:', this.cardEditando);

            if (this.nivelActual !== 'root') {
              try {
                console.log('Cargando contenido para:', `${this.nivelActual}/${this.cardEditando.title}`);
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
                  title: this.$t('lang.utils.error'),
                  typeData: 'error',
                  descripcionData: this.$t('lang.proyecto.errorCargarContenido')
                });
                this.cardEditando.contenido = '';
              }
            } else {
              this.cardEditando.contenido = '';
            }

            this.editorAbierto = true;
            console.log('Modal de edición abierto, editorAbierto:', this.editorAbierto);
          }
        },

        closeAddModal() {
          this.showAddModal = false;
        },

        handleAdd({ nombreCarpeta, archivos }) {
          this.subirCarpetaConArchivos(nombreCarpeta, archivos);
          this.closeAddModal();
        },
        closeDeleteModal() {
          this.showDeleteModal = false;
        },

        handleDelete(itemsToDelete) {
          this.confirmarEliminarMultipleCards(itemsToDelete);
          this.closeDeleteModal();
        },
    },
};
</script>

<style scoped>
/* Estilos para los botones */
.buttonMenu {
    padding: 12px 24px;
    background: #1168a6;
    color: #fff;
    border-radius: 6px;
    font-size: 16px;
    border: none;
    cursor: pointer;
}

.buttonMenu:hover {
    background: #0c4974;
}

.buttonMenu:active {
    background: #0a3758;
}

/* Estilos generales */
.container {
    display: flex;
    flex-direction: column;
    gap: 20px;
    padding: 0;
    margin: 0;
    width: 100%;
}

.button-container {
    display: flex;
    gap: 10px;
    align-items: center;
    width: 100%;
    justify-content: flex-start;
    border-radius: 6px;
}

.content-container {
    display: flex;
    gap: 20px;
    width: 100%;
}

.cards-container {
    display: flex;
    flex-wrap: wrap;
    gap: 20px;
    width: 100%;
}

.upload-container {
    width: 30%;
    display: flex;
    flex-direction: column;
    gap: 20px;
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
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    width: 100%;
    text-align: center;
}


.card-icon {
    display: flex;
    justify-content: center;
    align-items: center;
    margin-bottom: 10px;
    width: 80px;
    height: 80px;
    background: transparent;
}

.card-icon-img {
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

.clear-filters-button-container {
    height: 40px;
    position: relative;
}

.clear-filters-button {
    transition: opacity 0.3s ease;
}

.clear-filters-button.invisible {
    visibility: hidden;
    opacity: 0;
    pointer-events: none;
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
}

.clear-filters-button.visible {
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

.file-preview {
    margin-top: 20px;
    text-align: center;
}

.preview-image {
    max-width: 100%;
    max-height: 200px;
    margin-top: 10px;
}

.preview-buttons {
    display: flex;
    gap: 0.5rem;
    /* Espacio entre los botones */
    margin-top: 0.5rem;
}

.half-width {
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
    width: 100%;
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

.filter-dropdown li.active-filter {
    background-color: #1168a6;
    color: white;
    border-bottom: 1px solid white;
}

/* Estilos adicionales */
.file-actions {
    display: flex;
    gap: 10px;
}

.arrow-icon {
    transition: transform 0.3s ease;
}

.rotate-arrow {
    transform: rotate(180deg);
}

/* Botón para limpiar filtros */
.clear-filters-button {
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

.clear-filters-button:hover {
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

.modal-content {
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

.modal-buttons {
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

.buttonCrearFiltro {
    padding: 0.5rem 0.75rem;
    font-size: 1rem;
    background-color: #1168a6;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    line-height: 1;
}

.buttonCrearFiltro:hover {
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

.active-filter {
    background-color: #e0f7fa;
    font-weight: bold;
}

.input-nombre-filtro {
    margin-right: 15px;
}
</style>