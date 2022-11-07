<template>
  <div v-if="realtyObject && realtyObject.id">
    <div class="pt-5">
      <h1>Объект</h1>
      <h3 v-if="realtyObject.calculatedValue" class="text-left">Оценка стоимости: {{ realtyObject.calculatedValue }} ₽</h3>
      <h3 v-else class="text-left ">Для данного объекта стоимость еще не рассчитана</h3>
      <v-card class="mt-2">
        <v-card-text>
          <v-container>
            <v-row>
              <v-col
                cols="12"
                sm="6"
                md="4"
              >
                <v-text-field
                  v-model="realtyObject.location"
                  label="Адрес"
                  readonly
                ></v-text-field>
              </v-col>
              <v-col
                cols="12"
                sm="6"
                md="4"
              >
                <v-text-field
                  v-model="realtyObject.roomsNumber"
                  type="number"
                  label="Количество комнат"
                  readonly
                ></v-text-field>
              </v-col>
              <v-col
                cols="12"
                sm="6"
                md="4"
              >
                <v-text-field
                  v-model="realtyObject.segment"
                  label="Сегмент"
                  readonly
                ></v-text-field>
              </v-col>
              <v-col
                cols="12"
                sm="6"
                md="4"
              >
                <v-text-field
                  v-model="realtyObject.floorCount"
                  label="Этажность дома"
                  type="number"
                  readonly
                ></v-text-field>
              </v-col>
              <v-col
                cols="12"
                sm="6"
                md="4"
              >
                <v-text-field
                  v-model="realtyObject.wallMaterial"
                  label="Материал стен"
                  readonly
                ></v-text-field>
              </v-col>
              <v-col
                cols="12"
                sm="6"
                md="4"
              >
                <v-text-field
                  v-model="realtyObject.floorNumber"
                  label="Этаж расположения"
                  type="number"
                  readonly
                ></v-text-field>
              </v-col>
              <v-col
                cols="12"
                sm="6"
                md="4"
              >
                <v-text-field
                  v-model="realtyObject.totalArea"
                  label="Площадь квартиры, кв.м"
                  type="number"
                  readonly
                ></v-text-field>
              </v-col>
              <v-col
                cols="12"
                sm="6"
                md="4"
              >
                <v-text-field
                  v-model="realtyObject.kitchenArea"
                  label="Площадь кухни, кв.м"
                  type="number"
                  readonly
                ></v-text-field>
              </v-col>
              <v-col
                cols="12"
                sm="6"
                md="4"
              >
              <v-checkbox
                v-model="realtyObject.gotBalcony"
                label="Наличие балкона/лоджии"
                readonly
              ></v-checkbox>
              </v-col>
              <v-col
                cols="12"
                sm="6"
                md="4"
              >
                <v-text-field
                  v-model="realtyObject.distanceFromMetro"
                  label="Удаленность от станции метро, мин. пешком"
                  type="number"
                  readonly
                ></v-text-field>
              </v-col>
              <v-col
                cols="12"
                sm="6"
                md="4"
              >
                <v-text-field
                  v-model="realtyObject.condition"
                  label="Состояние"
                  readonly
                ></v-text-field>
              </v-col>
              <v-col
                cols="12"
                sm="6"
                md="4"
              >
                <v-autocomplete 
                  label="Пул"
                  v-model="realtyObject.poolId"
                  :items="pools"
                  item-value="id"
                  item-text="name"
                  readonly
                >
                </v-autocomplete >
              </v-col>
              <v-col
                cols="12"
                sm="6"
                md="4"
              >
              </v-col>
            </v-row>
          </v-container>
        </v-card-text>
      </v-card>
    </div>
    <h3 class="mt-2">Карта</h3>
    <ObjectsMap :key="key" :initialCoirdinates="[realtyObject.coordinates.lat, realtyObject.coordinates.lon]" :initialZoom="17" :currentItem="realtyObject"></ObjectsMap>
    <Analogs :analogs="realtyObject.analogs"></Analogs>
  </div>
</template>
<script>
import RequestService from "@/services/RequestService";
import ObjectsMap from "@/components/ObjectsMap";
import Analogs from "@/components/Analogs";
import { mapGetters } from 'vuex'

export default {
  name: 'RealtyObject',
  components: {
    ObjectsMap,
    Analogs,
  },

  data: () => ({
    objectMap: null,
    realtyObject: null,
    key: 0,
  }),

  computed: {
    ...mapGetters(['pools']),
  },

  async created() {
    [this.realtyObject] = await Promise.all([
      RequestService.geyObjectByIdAsync(this.$router?.currentRoute?.params?.id),
      this.$store.dispatch('loadlAllPoolsAsync')
    ])
  },

  async beforeRouteUpdate(to, from, next) {
    if (to?.params?.id !== from?.params?.id) {
      this.realtyObject = await RequestService.geyObjectByIdAsync(to?.params?.id);
      this.key +=1;
      next();
    }
    next();
  },
}
</script>
