import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/Home.vue'
import AboutView from '../views/About.vue'
import ProyectoView from '../views/Proyecto.vue'

const routes = [
  { path: '/', name: 'home', component: HomeView },
  { path: '/proyecto', name: 'proyecto', component: ProyectoView },
  { path: '/about', name: 'about', component: AboutView }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router

