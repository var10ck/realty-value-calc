<template>
    <div class="pt-5 mb-10">
      <h3 v-if="analogs.length">Аналоги</h3>
      <h3 v-else>Аналоги отсутствуют</h3>

      <div class="d-flex flex-wrap">
        <v-card
          v-for="(item, i) in analogs" :key=i
          class="mx-auto my-3"
          max-width="344"
        >
          <v-card-text v-if="item" class="text-left">
            <p v-if="item.price" class="text-h4 text--primary">
              Цена: {{ item.price }}
            </p>
            <div v-if="item.area" >Общая площадь: {{ item.area }}</div>
            <div v-if="item.kitchenArea">Площадь кухни: {{ item.kitchenArea }}</div>
            <div v-if="item.roomsNumber">Удаленность от станции метро, мин. пешком: {{ item.distanceToMetroInMinutes }}</div>
            <div v-if="item.distanceToMetroInMinutes">Количество комнат: {{ item.roomsNumber }}</div>
          </v-card-text>
          <v-card-actions>
            <v-btn
              color="teal accent-4"
              @click="showOnMap(item)"
            >
              Показать на карте
            </v-btn>
            <v-btn
              text
              color="teal accent-4"
              :href="item.url"
              target="_blank"
            >
              Подробнее
            </v-btn>
          </v-card-actions>
        </v-card>
      </div>
    </div>
</template>
<script>
// import RequestService from "@/services/RequestService";
// import { mapGetters } from 'vuex'

export default {
  name: 'Analogs',

  props: {
    analogs: {default: () => []},
  },

  data: () => ({
    objectMap: null,
    realtyObject: null,
  }),

  methods: {
    showOnMap(item) {
      this.$bus.$emit('showOnMap', [item?.latitude, item?.longitude])
    }
  }

}
</script>
