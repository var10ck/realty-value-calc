import Vue from 'vue'
import VueRouter from 'vue-router'
import RealtyObjects from '../views/RealtyObjects.vue'
import Login from '@/components/Login';
import Registry from '@/components/Registry';
import RequestService from "@/services/RequestService";

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'RealtyObjects',
    component: RealtyObjects
  },
  {
    path: '/about',
    name: 'About',
    // route level code-splitting
    // this generates a separate chunk (about.[hash].js) for this route
    // which is lazy-loaded when the route is visited.
    component: () => import(/* webpackChunkName: "about" */ '../views/About.vue')
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
    path: '/:pathMatch(.*)*',
    component: RealtyObjects,
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
