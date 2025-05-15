<template>
    <div class="contenedorGeneral">
        <!-- Contenedor de botones (Ejecutar y Eliminar Seleccionadas) -->
        <div class="contenedorBotones">
            <button class="botonMenu" @click="ejecutarCardsSeleccionadas">Ejecutar</button>

            <input type="text" v-model="busqueda" placeholder="Buscar archivo..." class="buscador" />

            <button v-if="cardsSeleccionadas.length > 0" @click="confirmarEliminarMultipleCards" class="botonMenu">
                Eliminar seleccionados
            </button>

        </div>

        <!-- Contenedor principal (tarjetas y área de carga de archivos) -->
        <div class="contenedorCardsCarga">
            <!-- Contenedor de tarjetas -->
            <div class="contenedorCards">
                <div class="card-widget" v-for="(card, index) in cardsFiltradas" :key="card.id"
                    :class="{ 'card-activa': card.active }">
                    <!-- Botones -->
                    <button class="card-check" @click="toggleCard(card)">{{ card.active ? '✔' : '' }}</button>
                    <button class="card-eliminar" @click="eliminarCard(index)">✖</button>
                    <!-- Imagen fija -->
                    <div class="card-icono">
                        <img :src="icono_prueba" alt="icono" style="width: 100%; max-height: 100px;" />
                    </div>

                    <!-- Título del archivo -->
                    <div class="card-nombre">{{ card.nombre }}</div>
                </div>
            </div>

            <!-- Contenedor de carga -->
            <div class="contenedorCarga">
                <div class="drag-area" @dragover.prevent="handleDragOver" @dragleave="handleDragLeave"
                    @drop.prevent="handleDrop" :class="{ 'dragging': isDragging }">
                    <p>Arrastra un archivo aquí o haz clic para subir.</p>
                    <input type="file" multiple @change="manejarArchivo" ref="fileInput" class="file-input" />
                </div>

                <div v-if="archivoSeleccionado.length" class="file-preview">
                    <ul>
                        <li v-for="(file, index) in archivoSeleccionado" :key="file.name">
                            {{ file.name }}
                        </li>
                    </ul>

                    <div class="botones-preview">
                        <button class="botonMenu mitadAncho" @click="confirmarSubida">Confirmar</button>
                        <button class="botonMenu mitadAncho" @click="cancelarSubida">Cancelar</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import axiosInstance from '@/plugins/axios'
import useAlert from '@/stores/alert.js'
import icono_prueba from '@/assets/icono_prueba.svg'

export default {
    name: 'Proyecto',
    data() {
        return {
            icono_prueba,
            cards: [],
            filterDropdownOpen: false,
            isDragging: false,
            archivoSeleccionado: [],
            cardsSeleccionadas: [],
            filtros: [],
            filtrosActivos: [],
            fileId: null,
            busqueda: '',
        }
    },

    mounted() {
        this.obtenerCards();
    },

    computed: {
        cardsFiltradas() {
            const texto = this.busqueda.trim().toLowerCase();
            if (!texto) return this.cards;

            const coincidencias = this.cards.filter(card =>
                card.nombre.toLowerCase().includes(texto)
            );
            const activadas = this.cards.filter(card => card.active);
            return [...new Set([...coincidencias, ...activadas])];
        }
    },

    methods: {
        ...useAlert(),

        async obtenerCards() {
            try {
                const response = await axiosInstance.get('/test-regresion');
                const jsonFiles = response.data;

                if (Array.isArray(jsonFiles) && jsonFiles.length > 0) {
                    this.cards = jsonFiles.map((fileName, index) => ({
                        id: index + 1,
                        nombre: fileName,
                        active: false
                    }));
                } else {
                    this.cards = [];
                }
            } catch (error) {
                this.sendMessageAlert({
                    nombre: this.$t('error'),
                    typeData: 'error',
                    descripcionData: this.$t('errorLlamada')
                });
                console.error('Error:', error);
            }
        },

        async ejecutarCardsSeleccionadas() {
            const selectedCards = this.cards.filter(prueba => prueba.active);
            if (selectedCards.length === 0) {
                alert('No hay cards seleccionadas.');
                return;
            }

            const fileNames = selectedCards.map(p => p.nombre);

            try {
                await axiosInstance.post('/ejecutar-script', fileNames);
                alert('Script ejecutado correctamente.');
            } catch (error) {
                this.sendMessageAlert({
                    nombre: this.$t('error'),
                    typeData: 'error',
                    descripcionData: 'Error al ejecutar el script.'
                });
                console.error('Error al ejecutar scripts:', error);
            }
        },

        toggleCard(prueba) {
            prueba.active = !prueba.active;
            this.actualizarCardsSeleccionadas(prueba);
        },

        actualizarCardsSeleccionadas(prueba) {
            if (prueba.active) {
                this.cardsSeleccionadas.push(prueba);
            } else {
                const index = this.cardsSeleccionadas.indexOf(prueba);
                if (index !== -1) {
                    this.cardsSeleccionadas.splice(index, 1);
                }
            }
        },

        async confirmarEliminarMultipleCards() {
            const cardsAEliminar = this.cardsSeleccionadas;
            if (cardsAEliminar.length === 0) {
                this.close();
                return;
            }

            const confirmado = window.confirm(`¿Estás seguro de que deseas eliminar ${cardsAEliminar.length} archivo(s)?`);
            if (!confirmado) {
                return;
            }

            const fileNames = cardsAEliminar.map(card => card.nombre);

            try {
                await axiosInstance.delete('/delete-test', { data: fileNames });

                this.cards = this.cards.filter(p => !cardsAEliminar.includes(p));
                this.cardsSeleccionadas = [];

                this.sendMessageAlert({
                    nombre: this.$t('lang.utils.success'),
                    typeData: 'success',
                    descripcionData: this.$t('lang.testRegresion.archivosEliminados', { count: fileNames.length })
                });
            } catch (error) {
                this.sendMessageAlert({
                    nombre: this.$t('error'),
                    typeData: 'error',
                    descripcionData: this.$t('lang.testRegresion.noSePudieronEliminarArchivos')
                });
                console.error('Error al eliminar archivos:', error);
            } finally {
                this.close();
            }
        },

        async eliminarCard(index) {
            const prueba = this.cards[index];
            const fileName = prueba.nombre;

            const confirmado = confirm(`¿Quieres eliminar el archivo "${fileName}"?`);
            if (!confirmado) return;

            try {
                await axiosInstance.delete('/delete-test', { data: [fileName] });
                this.cards.splice(index, 1);
            } catch (error) {
                this.sendMessageAlert({
                    nombre: this.$t('error'),
                    typeData: 'error',
                    descripcionData: 'No se pudo eliminar el archivo del servidor.'
                });
                console.error('Error al eliminar archivo:', error);
            }
        },

        toggleFilterDropdown() {
            this.filterDropdownOpen = !this.filterDropdownOpen;
            if (this.filterDropdownOpen && this.filtros.length === 0) {
                this.obtenerFiltros();
            }
        },

        filtrarYSeleccionar(filtroArchivo) {
            if (this.filtrosActivos.includes(filtroArchivo)) {
                this.filtrosActivos = this.filtrosActivos.filter(f => f !== filtroArchivo);
            } else {
                this.filtrosActivos.push(filtroArchivo);
            }
            this.aplicarFiltros();
        },

        aplicarFiltros() {
            if (this.filtrosActivos.length === 0) {
                this.cards.forEach(card => card.active = false);
                this.cardsSeleccionadas = [];
                return;
            }

            this.cards.forEach(card => {
                const coincide = this.filtrosActivos.some(filtro =>
                    card.nombre.toLowerCase().includes(filtro.toLowerCase())
                );
                card.active = coincide;
            });
            this.cardsSeleccionadas = this.cards.filter(card => card.active);
        },

        handleDragOver() {
            this.isDragging = true;
        },
        handleDragLeave() {
            this.isDragging = false;
        },
        handleDrop(event) {
            this.isDragging = false;
            const file = event.dataTransfer.files[0];
            this.manejarArchivo({ target: { files: [file] } });
        },

        manejarArchivo(event) {
            const files = Array.from(event.target.files);
            const nuevosArchivos = [];

            files.forEach(file => {
                const yaExiste =
                    this.cards.some(card => card.nombre === file.name) ||
                    this.archivoSeleccionado.some(f => f.name === file.name);

                if (yaExiste) {
                    alert(`El archivo "${file.name}" ya ha sido añadido previamente.`);
                } else {
                    nuevosArchivos.push(file);
                }
            });

            this.archivoSeleccionado.push(...nuevosArchivos);
        },

        confirmarSubida() {
            if (!this.archivoSeleccionado.length) {
                alert("No hay archivos seleccionados");
                return;
            }

            const formData = new FormData();
            this.archivoSeleccionado.forEach(file => {
                formData.append("files", file);
            });

            axiosInstance.post("/upload-file", formData, {
                headers: {
                    "Content-Type": "multipart/form-data"
                }
            })
                .then(() => {
                    alert("Archivos subidos: " + this.archivoSeleccionado.map(f => f.name).join(', '));
                    const nuevasCards = this.archivoSeleccionado.map((file, i) => ({
                        id: this.cards.length + i + 1,
                        nombre: file.name,
                        active: false
                    }));
                    this.cards.push(...nuevasCards);
                    this.archivoSeleccionado = [];
                    this.filePreview = [];
                    this.$refs.fileInput.value = null;
                })
                .catch((error) => {
                    this.sendMessageAlert({
                        nombre: this.$t("error"),
                        typeData: "error",
                        descripcionData: "Error al subir los archivos."
                    });
                    console.error("Error al subir los archivos:", error);
                });
        },

        cancelarSubida() {
            this.archivoSeleccionado = [];
            this.filePreview = [];
            this.$refs.fileInput.value = null;
        },
    }
}
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

/* Estilos generales */
.contenedorGeneral {
    display: flex;
    flex-direction: column;
    gap: 20px;
    padding: 0;
    margin: 0;
    width: 100%;
}

.contenedorBotones {
    display: flex;
    gap: 10px;
    align-items: center;
    width: 100%;
    justify-content: flex-start;
    border-radius: 6px;
}

.contenedorCardsCarga {
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

.contenedorCarga {
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

.card-widget.card-activa {
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
.card-nombre {
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


.card-icono {
    display: flex;
    justify-content: center;
    align-items: center;
    margin-bottom: 10px;
    width: 80px;
    height: 80px;
    background: transparent;
}

.card-icono-img {
    max-height: 100%;
    max-width: 80px;
    object-fit: contain;
}

/* Estilo del botón de cierre de las tarjetas */
.card-eliminar {
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
    /* Uniformidad con botones */
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

.botones-preview {
    display: flex;
    gap: 0.5rem;
    /* Espacio entre los botones */
    margin-top: 0.5rem;
}

.mitadAncho {
    flex: 1;
    /* Hace que ambos botones compartan el mismo ancho */
}

.card {
    border: solid #d7dadc 2px;
    padding: 32px;
    width: 200px;
    cursor: pointer;
}

.buscador {
    padding: 12px 24px;
    margin: 0 10px;
    border: 1px solid #ccc;
    border-radius: 6px;
    width: 200px;
}
</style>
