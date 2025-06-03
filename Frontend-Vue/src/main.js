// src/main.js
import { createApp } from 'vue';
import { createI18n } from 'vue-i18n';
import './style.css';
import App from './App.vue';
import router from './router';
import es from '/src/local/langs/es_ES.json';
import en from '/src/local/langs/en_GB.json';

// Detectar idioma del navegador
const getBrowserLanguage = () => {
  const browserLang = navigator.language.split('-')[0]; // Ejemplo: 'en-US' -> 'en'
  const supportedLanguages = ['es', 'en']; // Idiomas soportados
  return supportedLanguages.includes(browserLang) ? browserLang : 'es'; // Fallback a español
};

// Cargar idioma preferido desde localStorage o navegador
const preferredLanguage = localStorage.getItem('preferredLanguage') || getBrowserLanguage();

const i18n = createI18n({
  locale: preferredLanguage, // Usar idioma preferido o del navegador
  fallbackLocale: 'es', // Fallback a español
  messages: {
    es,
    en
  }
});

const app = createApp(App);
app.use(i18n);
app.use(router);
app.mount('#app');
