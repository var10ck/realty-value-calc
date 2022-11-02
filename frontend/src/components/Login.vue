<template>
   <v-container fluid fill-height>
      <v-layout align-center justify-center>
         <v-flex xs12 sm8 md4>
            <v-card class="elevation-12">
               <v-toolbar dark color="primary">
                  <v-toolbar-title>Воийти</v-toolbar-title>
               </v-toolbar>
               <v-card-text>
                  <v-form>
                     <v-text-field
                        name="login"
                        label="Логин"
                        type="text"
                     ></v-text-field>
                     <v-text-field
                        id="password"
                        name="password"
                        label="Пароль"
                        type="password"
                     ></v-text-field>
                  </v-form>
               </v-card-text>
               <v-card-actions>
                  <v-btn color="primary" to="/registry">Заррегистрироваться</v-btn>
                  <v-spacer></v-spacer>
                  <v-btn color="primary" to="/">Войти</v-btn>
               </v-card-actions>
            </v-card>
         </v-flex>
      </v-layout>
   </v-container>
</template>

<script>
export default {
   name: 'Login',
};
</script>

<style></style>
