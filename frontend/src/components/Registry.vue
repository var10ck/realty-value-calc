<template>
   <v-container fluid fill-height>
      <v-layout align-center justify-center>
         <v-flex xs12 sm8 md4>
            <v-card class="elevation-12">
               <v-toolbar dark color="primary">
                  <v-toolbar-title>Регистрация</v-toolbar-title>
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
                     <v-col
                       data-app
                     >
                        <v-menu
                           ref="menu"
                           v-model="dateMenu"
                           :close-on-content-click="false"
                           :return-value.sync="birthdate"
                           transition="scale-transition"
                           offset-y
                           min-width="auto"
                        >
                           <template v-slot:activator="{ on, attrs }">
                              <v-text-field
                                 v-model="birthdate"
                                 label="Дата рождения"
                                 prepend-icon="mdi-calendar"
                                 readonly
                                 v-bind="attrs"
                                 v-on="on"
                              ></v-text-field>
                           </template>
                           <v-date-picker
                              v-model="birthdate"
                              no-title
                              scrollable
                           >
                              <v-spacer></v-spacer>
                              <v-btn
                                 text
                                 color="primary"
                                 @click="dateMenu = false"
                              >
                                 Отменить
                              </v-btn>
                              <v-btn
                                 text
                                 color="primary"
                                 @click="$refs.menu.save(birthdate)"
                              >
                                 ОК
                              </v-btn>
                           </v-date-picker>
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
export default {
   name: 'Registry',

   data: () => ({
      login: null,
      password: null,
      passwordRepeat: null,
      firstName: null,
      lastName: null,

      dateMenu: false,
      birthdate: null,
      snackbar: false,
   }),

   methods: {
      async register() {
         
         this.$bus.$emit('showSuccess', 'Ура')
      }
   },

};
</script>

<style></style>
