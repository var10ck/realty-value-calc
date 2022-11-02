import Vue from 'vue'
import App from './App.vue'
import vuetify from './plugins/vuetify'
import router from './router'
import store from './store'

Vue.config.productionTip = false

Object.defineProperty(Vue.prototype,"$bus",{
	get: function() {
		return this.$root.bus;
	}
});

new Vue({
  vuetify,
  router,
  store,
  render: h => h(App),
  data: {
		bus: new Vue({})
	}
}).$mount('#app')
