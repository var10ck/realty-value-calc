import Vue from 'vue'
import Vuex from 'vuex'
import RequestService from "@/services/RequestService";

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    pools: [],
  },

  getters: {
    pools (state) {
      return state.pools;
    }
  },

  mutations: {
    SET_POOLS(state, { pools }) {
      state.pools = pools;
    },

    ADD_POOL(state, { pool }) {
      if (!state.pools.map(x => x.id).includes(pool.id)) {
        state.pools.push(pool)
      }
    },
  },

  actions: {
    async loadPoolAsync({ commit }, { poolId }) {
      const pool = await RequestService.loadPoolByIdAsync(poolId)
      commit({
        type: 'ADD_POOL',
        pool: pool,
      });
    },

    async loadlAllPoolsAsync({ commit }, ) {
      const pools = await RequestService.loadlAllPoolsAsync()
      commit({
        type: 'SET_POOLS',
        pools: pools,
      });
    },

    addPool({ commit }, { pool }) {
      commit({
        type: 'ADD_POOL',
        pool: pool,
      });
    }
  },

  modules: {
  }
})
// /realty/pools