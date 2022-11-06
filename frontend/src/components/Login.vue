<template>
  <v-container fluid fill-height>
    <v-layout align-center justify-center>
      <v-flex xs12 sm8 md4>
        <v-card class="elevation-12">
          <v-toolbar dark color="primary">
            <v-toolbar-title class="ml-3">Вход</v-toolbar-title>
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
            </v-form>
          </v-card-text>
          <v-card-actions>
            <v-btn color="primary" to="/registry">Зарегистрироваться</v-btn>
            <v-spacer></v-spacer>
            <v-btn color="primary" @click="auth">Войти</v-btn>
          </v-card-actions>
        </v-card>
      </v-flex>
    </v-layout>
  </v-container>
</template>

<script>
import RequestService from "@/services/RequestService";

export default {
  name: "Login",

  data: () => ({
    login: null,
    password: null,
  }),

  methods: {
    async auth() {
      if (
        !this.login ||
        !this.password
      ) {
        this.$bus.$emit("showError", "Проверьте корректность введенных данных");
        return; // !!!
      }
      try {
        await RequestService.authAsync({
          login: this.login,
          password: this.password,
        });
        this.$router.push("/");
      } catch (e) {
        this.$bus.$emit("showError", e);
      }
    },
  },
};
</script>

<style></style>
