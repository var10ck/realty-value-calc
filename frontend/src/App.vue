<template>
  <div id="app">
    <v-app id="app">
      <v-main>
        <template v-if="!['/login', '/registry'].includes($route.path)">
          <v-app-bar dark color="#162130" style="height: 100px; padding: 18px">
            <v-app-bar-title class="white--text">
              <div>
                <v-img
                  :src="require('@/assets/apartments.svg')"
                  class="my-3"
                  contain
                  height="55"
                />
              </div>
            </v-app-bar-title>

            <v-spacer></v-spacer>
            <v-tabs fixed-tabs>
              <v-tab to="/">Home</v-tab>
              <v-tab to="/about">About</v-tab>
            </v-tabs>
            <v-spacer></v-spacer>
            <v-btn @click="$router.push('login')">Выход</v-btn>
          </v-app-bar>
          <!-- <v-container>
            <router-view/>
          </v-container> -->
        </template>
        <v-snackbar
            top
            right
            v-model="snackbarSuccess"
            :timeout="2500"
            color="success"
            outlined
        >
            {{ snackbarMessage }}
            <template v-slot:action="{ attrs }">
            <v-btn
              color="success"
              text
              v-bind="attrs"
              @click="snackbarSuccess = false"
            >
              Закрыть
            </v-btn>
            </template>
        </v-snackbar>

        <v-snackbar
            top
            right
            v-model="snackbarError"
            :timeout="2500"
            color="error"
            outlined
        >
            {{ snackbarMessage }}
            <template v-slot:action="{ attrs }">
            <v-btn
              color="error"
              text
              v-bind="attrs"
              @click="snackbarError = false"
            >
              Закрыть
            </v-btn>
            </template>
        </v-snackbar>
        <keep-alive :include="['Login']">
          <router-view></router-view>
        </keep-alive>
      </v-main>
    </v-app>
  </div>
</template>
<script>
export default {
   data: () => ({
      snackbarSuccess: false,
      snackbarError: false,
      successMessage: null,
      snackbarMessage: null,
   }),

   created() {
    this.$bus.$on('showSuccess', this.showSuccess)
    this.$bus.$on('showError', this.showError)
   },

   beforeDestroy() {
     this.$bus.$off('showSuccess')
     this.$bus.$off('showError')
   },

   methods: {
      showSuccess(successMessage = "Успех") {
        this.snackbarSuccess = true;
        this.snackbarMessage = successMessage;
      },

      showError(successMessage = "Ошибка") {
        this.snackbarError = true;
        this.snackbarMessage = successMessage;
      }
   },
}
</script>

<style lang="scss">
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  // color: #2c3e50;
}

#nav {
  padding: 30px;

  a {
    font-weight: bold;
    color: #757575;
  }

  .v-tab--active {
    color: #EFEBE9;
  }
}
</style>
