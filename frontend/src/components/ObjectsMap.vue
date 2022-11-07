<template>
  <div class="pt-5" style="width: 80%; margin: 0 auto">
    <div class="d-flex">
      <div class="custom-filter-items">
        <v-autocomplete
          label="Выберите пул"
          class="mt-5"
          auto-select-first
          v-model="selectedPool"
          :items="poolsToSelect"
          item-text="name"
          return-object
          style="max-width: 400px"
          :readonly="!!(currentItem && currentItem.id)"
        >
        </v-autocomplete>
        <v-checkbox
          label="Только с рассчитаной стоимостью"
          class="ml-5 mt-6"
          v-model="onlyWithCalculatedValue"
        >
        </v-checkbox>
        <v-checkbox
          label="Показать аналоги"
          class="ml-5 mt-6"
          v-model="withAnalogs"
        >
        </v-checkbox>
      </div>
    </div>
    <div id="map" ref="map" style="height: 70vh"></div>
  </div>
</template>
<script>
import RequestService from "@/services/RequestService";
import { mapGetters } from "vuex";
import DG from "2gis-maps";

export default {
  name: "ObjectsMap",

  props: {
    initialCoirdinates: { default: () => [55.75, 37.62] },
    initialZoom: { default: 12 },
    currentItem: { default: () => {} },
  },

  data: () => ({
    map: null,
    realtyObjects: [],
    withAnalogs: false,
    onlyCurrentPull: false,
    onlyWithCalculatedValue: false,
    selectedPool: { id: null, name: "Все" },
    addedMarkers: {},
  }),

  created() {
    if (this.currentItem?.poolId) {
      this.selectedPool = this.pools.find(
        (x) => x.id === this.currentItem.poolId
      );
    }
  },

  watch: {
    selectedPool(newValue, oldValue) {
      if (newValue !== oldValue) {
        this.hideMarkers();
        this.showMarkers();
      }
    },

    onlyWithCalculatedValue(newValue, oldValue) {
      if (newValue !== oldValue) {
        this.hideMarkers();
        this.showMarkers();
      }
    },

    withAnalogs(newValue, oldValue) {
      if (newValue !== oldValue) {
        this.hideMarkers();
        this.showMarkers();
      }
    },
  },

  computed: {
    ...mapGetters(["pools"]),

    poolsToSelect() {
      return [{ id: null, name: "Все" }, ...this.pools];
    },

    filteredItems() {
      let res = this.realtyObjects;

      if (this.selectedPool?.id) {
        res = res.filter((x) => {
          return x.poolId === this.selectedPool.id;
        });
      }

      if (this.onlyWithCalculatedValue) {
        res = res.filter((x) => {
          return x.calculatedValue;
        });
      }

      return res;
    },

    markers() {
      const that = this;
      let markers = this.filteredItems.map((x) =>
        DG.marker([x?.coordinates?.lat, x?.coordinates?.lon])
          .bindPopup(this.getPopupText(x))
          .on("click", function () {
            const el = document.getElementById(x?.id);
            if (el) {
              console.log(el)
              el.addEventListener("click", () =>
                that.$router.push(`/realtyObject/${x?.id}`)
              );
            }
          })
      );

      if (this.withAnalogs && this.currentItem?.id) {
        this.currentItem.analogs?.forEach((x) => {
          markers.push(
            DG.marker([x?.latitude, x?.longitude]).bindPopup(
              this.getPopupText(x)
            )
          );
        });
      }

      if (this.withAnalogs && !this.currentItem?.id) {
        this.filteredItems.forEach((x) => {
          if (!x?.analogs) {
            return;
          }

          x?.analogs?.forEach((y) =>
            markers.push(
              DG.marker([y?.latitude, y?.longitude]).bindPopup(
                this.getPopupText(y)
              )
            )
          );
        });
      }

      return markers;
    },
  },

  async mounted() {
    this.$bus.$on("showOnMap", this.showOnMap);

    this.map = DG.map("map", {
      center: this.initialCoirdinates,
      zoom: this.initialZoom,
    });

    [this.realtyObjects] = await Promise.all([
      RequestService.getAllRealtyObjectsAsync(),
      this.$store.dispatch("loadlAllPoolsAsync"),
    ]);

    this.showMarkers();
  },

  beforeDestroy() {
    this.$bus.$off("showSuccess");
  },

  methods: {
    async showOnMap(coordinates) {
      this.$refs?.map?.scrollIntoView({ behavior: "smooth" });
      this.withAnalogs = true;
      this.map.panTo(coordinates);
      setTimeout(() => this.map.setZoom(18), 500)
    },

    showMarkers() {
      this.addedMarkers = DG.featureGroup(this.markers);
      this.addedMarkers?.addTo(this.map);
    },

    hideMarkers() {
      if (this.addedMarkers.removeFrom) {
        this.addedMarkers.removeFrom(this.map);
      }
    },

    getPopupText(item) {
      let text = "";
      if (item.url) {
        if (item.area) {
          text += `<p>Площадь: ${item.area} </p>`;
        }
        if (item.price) {
          text += `<p>Cтоимость: ${item.price} ₽</p>`;
        }
        if (item.roomsNumber) {
          text += `<p>Комнат: ${item.roomsNumber}</p>`;
        }
        return (text += `<a href="${item.url}" target="_blank">Внешняя ссылка</a>`);
      }

      text = `<p>${item.location}</p>`;
      if (item.calculatedValue) {
        text += `<p>Оценка стоимости: ${item.calculatedValue} ₽</p>`;
      }

      console.log(this.currentItem?.id)
      console.log( item?.id)
      if (this.currentItem?.id !== item?.id) {
        text += `<a id="${item?.id}">Подробнее</a>`;
      }
      return text;
    },
  },
};
</script>
