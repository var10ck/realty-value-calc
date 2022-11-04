<template>
  <v-container fluid fill-height>
    <v-layout align-center justify-center>
      <v-flex xs12 sm8 md4>
        <v-card class="elevation-12">
          <v-toolbar dark color="primary">
            <v-toolbar-title class="ml-3">Регистрация</v-toolbar-title>
          </v-toolbar>
          <v-card-text>
            <v-form>
              <v-text-field
                v-model="login"
                name="login"
                label="Логин"
                type="text"
              ></v-text-field>
              <v-text-field
                v-model="password"
                id="password"
                name="password"
                label="Пароль"
                type="password"
              ></v-text-field>
              <v-text-field
                v-model="passwordRepeat"
                id="passwordRepeat"
                name="passwordRepeat"
                label="Повторите пароль"
                type="password"
              ></v-text-field>
              <v-text-field
                v-model="firstName"
                name="firstName"
                label="Имя"
                type="text"
              ></v-text-field>
              <v-text-field
                v-model="lastName"
                name="lastName"
                label="Фамилия"
                type="text"
              ></v-text-field>
              <v-col data-app>
                <v-menu
                  ref="menu"
                  v-model="dateMenu"
                  :close-on-content-click="false"
                  transition="scale-transition"
                  offset-y
                  min-width="auto"
                >
                  <template v-slot:activator="{ on, attrs }">
                    <v-text-field
                      v-model="dateFormatted"
                      label="Дата рождения"
                      prepend-icon="mdi-calendar"
                      v-bind="attrs"
                      v-on="on"
                    ></v-text-field>
                  </template>
                  <v-date-picker
                    v-model="birthdate"
                    no-title
                    scrollable
                    @input="dateMenu = false"
                  />
                </v-menu>
              </v-col>
            </v-form>
          </v-card-text>
          <v-card-actions>
            <v-btn color="primary" to="/login">К странице входа</v-btn>
            <v-spacer></v-spacer>
            <v-btn color="primary" @click="register">Зарегестрировать</v-btn>
          </v-card-actions>
        </v-card>
      </v-flex>
    </v-layout>
  </v-container>
</template>

<script>
import RequestService from "@/services/RequestService";

export default {
  name: "Registry",

  data: () => ({
    login: null,
    password: null,
    passwordRepeat: null,
    firstName: null,
    lastName: null,
    birthdate: null,

    dateFormatted: null,
    dateMenu: false,
    snackbar: false,
  }),

  watch: {
    birthdate() {
      this.dateFormatted = this.formatDate(this.birthdate);
    },
  },

  methods: {
    formatDate(date) {
      if (!date) return null;

      const [year, month, day] = date.split("-");
      return `${day}.${month}.${year}`;
    },

    async register() {
      if (
        !this.login ||
        !this.password ||
        !this.firstName ||
        !this.lastName ||
        !this.birthdate ||
        this.password !== this.passwordRepeat
      ) {
        this.$bus.$emit("showError", "Проверьте корректность введенных данных");
        return; // !!!
      }
      try {
        await RequestService.registerAsync({
          login: this.login,
          password: this.password,
          firstName: this.firstName,
          lastName: this.lastName,
          birthdate: this.formatDate(this.birthdate),
        });
        this.$bus.$emit("showSuccess", "Регистрация успешно завершена");
        this.$router.push("/login");
      } catch (e) {
        this.$bus.$emit("showError", e);
      }
    },
  },
};
</script>

<style></style>
