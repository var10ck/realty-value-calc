<template>
  <div class="mb-10">
    <div class="pt-5">
      <h1>Аналитика</h1>
      <v-card class="mx-auto text-center my-5" max-width="1200">
        <apexchart
          type="bar"
          height="350"
          :options="chartCommonObjectsOptions"
          :series="commonObjectsValues"
        ></apexchart>
      </v-card>
      <v-card class="mx-auto text-center my-5" max-width="1200">
        <apexchart
          type="bar"
          height="350"
          :options="chartCommonAreasOptions"
          :series="commonAreasValues"
        ></apexchart>
      </v-card>
      <v-card class="mx-auto text-center pt-1" max-width="1200">
        <v-autocomplete
          label="Выберите пул"
          class="mt-5 ml-5"
          auto-select-first
          v-model="selectedPool"
          :items="poolsToSelect"
          item-text="name"
          return-object
          style="max-width: 400px"
          no-data-text="Нет доступных пулов"
          clearable
        />
        <apexchart
          type="line"
          height="350"
          :options="chartPoolOptions"
          :series="poolValues"
        ></apexchart>
      </v-card>
    </div>
  </div>
</template>
<script>
import RequestService from "@/services/RequestService";

import { mapGetters } from "vuex";

export default {
  name: "CorrectionsPage",

  data: () => ({
    common: {},
    poolData: {},
    selectedPool: null,

    chartCommonObjectsOptions: {
      chart: {
        height: 350,
        type: "bar",
        toolbar: {
          show: false,
        },
      },
      colors: ["#00E396"],
      plotOptions: {
        bar: {
          borderRadius: 10,
          dataLabels: {
            position: "top", // top, center, bottom
          },
        },
      },
      dataLabels: {
        enabled: true,

        style: {
          fontSize: "12px",
          colors: ["#304758"],
        },
      },

      xaxis: {
        categories: ["Пользователем", "Всего"],
        position: "top",
        axisBorder: {
          show: false,
        },
        axisTicks: {
          show: false,
        },
        crosshairs: {
          fill: {
            type: "gradient",
            gradient: {
              colorFrom: "#D8E3F0",
              colorTo: "#BED1E6",
              stops: [0, 100],
              opacityFrom: 0.4,
              opacityTo: 0.5,
            },
          },
        },
        tooltip: {
          enabled: false,
        },
      },
      yaxis: {
        axisBorder: {
          show: false,
        },
        axisTicks: {
          show: false,
        },
        labels: {
          show: false,
          formatter: function (val) {
            return val;
          },
        },
      },
      title: {
        text: "Объектов загружено",
        fontSize: '12px',
        floating: true,
        offsetY: 330,
        align: "center",
        style: {
          color: "#444",
        },
      },
    },

    chartCommonAreasOptions: {
      chart: {
        height: 350,
        type: "bar",
        toolbar: {
          show: false,
        },
      },
      colors: ["#775DD0"],

      plotOptions: {
        bar: {
          borderRadius: 10,
          dataLabels: {
            position: "top", // top, center, bottom
          },
        },
      },
      dataLabels: {
        enabled: true,

        style: {
          fontSize: "12px",
          colors: ["#304758"],
        },
      },

      xaxis: {
        categories: ["Пользователем", "Всего"],
        position: "top",
        axisBorder: {
          show: false,
        },
        axisTicks: {
          show: false,
        },
        crosshairs: {
          fill: {
            type: "gradient",
            gradient: {
              colorFrom: "#D8E3F0",
              colorTo: "#BED1E6",
              stops: [0, 100],
              opacityFrom: 0.4,
              opacityTo: 0.5,
            },
          },
        },
        tooltip: {
          enabled: true,
        },
      },
      yaxis: {
        axisBorder: {
          show: false,
        },
        axisTicks: {
          show: false,
        },
      },
      title: {
        text: "Средняя площадь",
        floating: true,
        offsetY: 330,
        align: "center",
        style: {
          color: "#444",
        },
      },
    },

    chartPoolOptions: {
      chart: {
        height: 350,
        type: "line",
        toolbar: {
          show: false,
        },
      },

      // dataLabels: {
      //   enabled: true,
      //   // offsetY: -20,
      //   show: false,
      //   formatter: function (val) {
      //     return val.toLocaleString() + "₽";
      //   },

      //   style: {
      //     width: "15px",
      //     fontSize: "12px",
      //     colors: ["#304758"],
      //   },
      // },

      xaxis: {
        categories: [
          "1 комната",
          "2 комныты",
          "3 комнаты",
          "4 комнаты",
          "5 комнат",
        ],
        position: "top",
        axisBorder: {
          show: false,
        },
        axisTicks: {
          show: false,
        },
        crosshairs: {
          fill: {
            type: "gradient",
            gradient: {
              colorFrom: "#D8E3F0",
              colorTo: "#BED1E6",
              stops: [0, 100],
              opacityFrom: 0.4,
              opacityTo: 0.5,
            },
          },
        },
        tooltip: {
          enabled: true,
        },
      },
      yaxis: {
        axisBorder: {
          show: false,
        },
        axisTicks: {
          show: false,
        },
        labels: {
          show: false,
          formatter: function (val) {
            return val.toLocaleString() + "₽";
          },
        },
      },
      title: {
        text: "Средняя цена",
        floating: true,
        offsetY: 330,
        align: "center",
        style: {
          color: "#444",
        },
      },
    },
  }),

  computed: {
    ...mapGetters(["pools"]),

    poolsToSelect() {
      return this.pools;
    },

    commonAreasValues() {
      return [
        {
          name: "Средняя площадь",
          data: Object.values(
            Object.fromEntries(
              Object.entries(this.common).filter(([key]) =>
                ["sumAllAreaOfUsersObjects", "sumAllAreaOfAllObjects"].includes(
                  key
                )
              )
            )
          ),
        },
      ];
    },

    commonObjectsValues() {
      return [
        {
          name: "Загружено",
          data: Object.values(
            Object.fromEntries(
              Object.entries(this.common).filter(([key]) =>
                [
                  "countAllRealtyObjectsOfUser",
                  "countAllRealtyObjects",
                ].includes(key)
              )
            )
          ),
        },
      ];
    },

    poolValues() {
      if (!this.selectedPool?.id && !this.common?.avgPriceByRoomsNumber) {
        return [];
      }

      return [
        {
          name: "Средняя цена",
          data: this.selectedPool?.id
            ? Object.values(this.poolData).map(
                (x) => Math.round(x / 1000) * 1000
              )
            : Object.values(this.common.avgPriceByRoomsNumber).map(
                (x) => Math.round(x / 1000) * 1000
              ),
        },
      ];
    },
  },

  async created() {
    [this.common] = await Promise.all([
      RequestService.getAnalyticsCommon(),
      this.$store.dispatch("loadlAllPoolsAsync"),
    ]);
  },

  watch: {
    async selectedPool(newValue) {
      if (newValue?.id) {
        this.poolData = await RequestService.getAnalyticsForPool(newValue?.id);
      }
    },
  },

  methods: {
    async correctionsCanged() {
      this.corrections = await RequestService.getAllCorrections();
      this.editedCorrection = null;
    },
  },
};
</script>
<style>
svg g text:first-child {
  transform: translateX(20px);
}
svg g text:last-child {
  transform: translateX(-20px);
}
</style>
