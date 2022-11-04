<template>
    <v-app id="app">
      <v-main>
        <template v-if="isAuthenticated">
          <v-app-bar dark color="#162130" style="height: 63px; padding: 0px 10vw">
            <v-app-bar-title class="white--text">
              <div>
                <v-img
                  :src="require('@/assets/apartments.svg')"
                  contain
                  height="48"
                  width="48"
                />
              </div>
            </v-app-bar-title>

            <v-spacer></v-spacer>
            <v-tabs fixed-tabs>
              <v-tab to="/">Объекты</v-tab>
              <v-tab to="/about">About</v-tab>
            </v-tabs>
            <v-spacer></v-spacer>
            <v-btn @click="logout" light>Выход</v-btn>
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
        <router-view :style=" isAuthenticated ? 'width: 80vw; margin: 0 auto' : ''"></router-view>
      </v-main>
    </v-app>
</template>
<script>
import RequestService from "@/services/RequestService";

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

   computed: {
     isAuthenticated() {
       return !['Login', 'Registry'].includes(this.$route.name) && RequestService.getCookie('userSessionId');
     }
   },

   methods: {
      showSuccess(successMessage = "Успех") {
        this.snackbarSuccess = true;
        this.snackbarMessage = successMessage;
      },

      showError(successMessage = "Ошибка") {
        this.snackbarError = true;
        this.snackbarMessage = successMessage;
      },

      logout() {
        RequestService.deleteCookie('userSessionId');
        this.$router.push('login');
      }
   },
}
</script>

<style lang="scss">
html { overflow-y: auto }

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

.v-toolbar__content {
  padding: 0 ;
}
</style>
