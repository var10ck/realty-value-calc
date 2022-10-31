<template>
  <div id="app">
    <v-app id="app">
      <template v-if="!$route.path.includes('login')">
        <v-main>
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
      </v-main>
      </template>
      <v-content>
          <keep-alive :include="['Login']">
            <router-view></router-view>
          </keep-alive>
      </v-content>
    </v-app>
  </div>
</template>
<script>
export default {

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
