<template>
  <v-app id="app">
    <v-main>
      <template v-if="isAuthenticated">
        <v-app-bar dark color="#162130" class="custom-app-bar">
          <v-app-bar-title class="white--text">
            <div class="d-flex align-center">
              <v-img
                :src="require('@/assets/apartments.svg')"
                contain
                height="48"
                width="48"
              />
              <div class="ml-5 custom-app-name">Realty value calculator</div>
            </div>
          </v-app-bar-title>

          <v-spacer class="custom-spacer"></v-spacer>
          <v-tabs fixed-tabs>
            <v-tab to="/">
              <span class="custon-nav-button">Объекты</span>
              <v-icon small class="custon-nav-icon mr-2"> mdi-home </v-icon>
            </v-tab>
            <v-tab to="/mapPage">
              <span class="custon-nav-button">Карта</span>
              <v-icon small class="custon-nav-icon mr-2"> mdi-map </v-icon>
            </v-tab>
            <v-tab to="/correctionsPage">
              <span class="custon-nav-button">Корректировки</span>
              <v-icon small class="custon-nav-icon mr-2"> mdi-pencil </v-icon>
            </v-tab>
            <v-tab to="/analytics">
              <span class="custon-nav-button">Аналитика</span>
              <v-icon small class="custon-nav-icon mr-2"> mdi-poll </v-icon>
            </v-tab>
          </v-tabs>
          <v-spacer></v-spacer>
          <v-btn class="custon-nav-button" @click="logout" light>Выход</v-btn>
          <v-icon class="custon-nav-icon" @click="logout"> mdi-door </v-icon>
        </v-app-bar>
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
      <router-view
        :style="isAuthenticated ? 'width: 80vw; margin: 0 auto' : ''"
      ></router-view>
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
    this.$bus.$on("showSuccess", this.showSuccess);
    this.$bus.$on("showError", this.showError);
  },

  beforeDestroy() {
    this.$bus.$off("showSuccess");
    this.$bus.$off("showError");
  },

  computed: {
    isAuthenticated() {
      return (
        !["Login", "Registry"].includes(this.$route.name) &&
        RequestService.getCookie("userSessionId")
      );
    },
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
      RequestService.deleteCookie("userSessionId");
      this.$router.push("login");
    },
  },
};
</script>

<style lang="scss">
html {
  overflow-y: auto;
}

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
    color: #efebe9;
  }
}

.v-toolbar__content {
  padding: 0;
}

.custom-app-bar {
  height: 63px;
  padding: 0px 10vw;
}

@media (max-width: 1240px) {
  .custom-app-name {
    display: none;
  }
}

@media (max-width: 900px) {
  .custom-app-bar {
    padding: 0 20px;
  }
}

.custon-nav-icon {
  display: none !important;
}

@media (max-width: 620px) {
  .v-slide-group__prev {
      display: none !important;
  }

  .v-tab {
    padding: 0px;
    min-width: 70px;
  }

  .custom-app-bar {
    padding: 0 10px;
  }

  .v-app-bar-title,
  .custom-spacer,
  .custon-nav-button,
  .custon-nav-button {
    display: none;
  }

  .custon-nav-icon {
    display: block !important;
  }
}
</style>
