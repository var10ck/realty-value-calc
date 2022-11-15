import Vue from 'vue'
import VueRouter from 'vue-router'
import AllRealtyObjects from '@/views/AllRealtyObjects.vue'
import RealtyObject from '@/views/RealtyObject.vue'
import MapPage from '@/views/MapPage.vue'
import AnalyticsPage from '@/views/AnalyticsPage.vue'
import CorrectionsPage from '@/views/CorrectionsPage.vue'
import Login from '@/components/Login';
import Registry from '@/components/Registry';
import RequestService from "@/services/RequestService";

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'AllRealtyObjects',
    component: AllRealtyObjects
  },
  {
    path: '/mapPage',
    name: 'MapPage',
    component: MapPage
  },
  {
    path: '/analytics',
    name: 'AnalyticsPage',
    component: AnalyticsPage
  },
  {
    path: '/correctionsPage',
    name: 'CorrectionsPage',
    component: CorrectionsPage
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
  },
  {
    path: '/registry',
    name: 'Registry',
    component: Registry,
  },
  {
    path: '/realtyObject/:id',
    name: 'RealtyObject',
    component: RealtyObject,
  },
  {
    path: '/:pathMatch(.*)*',
    component: AllRealtyObjects,
  },
 
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

router.beforeEach((to, from, next) => {
  if (!['Login', 'Registry'].includes(to.name) && !RequestService.getCookie('userSessionId')) next({ name: 'Login' })
  else next()
})

export default router
