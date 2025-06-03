import { createApp } from 'vue'
import { createI18n } from 'vue-i18n'
import es from './langs/es_ES'
import en from './langs/en_GB'


const app = createApp({})

const i18n = createI18n({
  legacy: false, // Desactiva Legacy API para usar Composition API
  globalInjection: true, // Permite usar $t y $d globalmente en los templates
  locale: window.navigator.language.split('-')[0], // Idioma del navegador
  fallbackLocale: 'es', // Idioma por defecto
  messages: {
    es: es, // No necesitas el prefijo lang
    en: en
  }
})

app.use(i18n)

export default i18n