<template>
  <div class="pt-5">
    <h1>Загруженные объекты недвижимости</h1>
    <div class="d-flex justify-end">

      <input
        ref="uploader"
        class="d-none"
        type="file"
        @change="onFileChanged"
      />
    </div>
    <v-data-table
      v-model="selectedObjects"
      :headers="headers"
      :items="filteredItems"
      show-select
      item-key="id"
      class="elevation-1 mt-3"
      :search="searchValue"
      :no-data-text="noDataText"
      :footer-props="{
        'items-per-page-options': [ -1, 10, 20, 50, 100 ],
        'items-per-page-all-text': 'Все',
        'items-per-page-text': 'Элементов на странице',
        'page-text': '',
        'show-current-page': true,
      }"
      @dblclick:row="(e, options) => $router.push(`/objectInfo/${options.item.id}`)"
      @click:row="(e, options) => isMobile ? $router.push(`/objectInfo/${options.item.id}`) : null"
    >
    <template v-slot:top>
      <v-toolbar
        flat
        height="extended"
        class="custom-toolbar"
      >
        <v-dialog
          v-model="dialog"
          max-width="900px"
        >
          <template v-slot:activator="{ on, attrs }">
            <div class="custom-filter-items">
              <v-autocomplete 
                label="Выберите пул" class="ml-5 mt-5"
                auto-select-first
                v-model="selectedPool"
                :items="poolsToSelect"
                item-text="name"
                return-object
              >
              </v-autocomplete>

              <v-autocomplete
                v-model="calculatedValue"
                item-value="value"
                item-text="label"
                class="ml-5 mt-5"
                :items="valueFilter"
                label="Значение стоимости"
              ></v-autocomplete>

              <v-text-field 
                label="Поиск" class="ml-5 mt-5"
                auto-select-first
                v-model="searchValue"
                :items="addressItems"
                item-text="name"
                return-object
              >
              </v-text-field>
            </div>
            <div class="d-flex flex-row-reverse flex-wrap">
              <v-btn
              type="file"
              dark
              small
              class="mr-3 mt-3"
              :loading="isSelecting"
              @click="handleFileImport"
              >
                Импортировать пул из Excel
              </v-btn>
              <v-btn
                dark
                small
                class="mr-3 mt-3"
                :loading="isSelecting"
                @click="exportFile"
              >
                Экспортировать {{selectedPool?.id ? 'пул' : 'все'}} в Excel
              </v-btn>
              <v-btn
                dark
                small
                class="mr-3 mt-3"
                @click="dialogPool = true"
              >
                Добавить пул
              </v-btn>
              <v-btn
                dark
                small
                v-bind="attrs"
                class="mr-3 mt-3"
                v-on="on"
                :disabled="!pools.length"
              >
                Добавить объект
              </v-btn>
              <v-btn
                dark
                small
                class="mr-3 mt-3"
                v-show="selectedPool.id || selectedObjects.length"
                @click="calculatePool"
              >
                Рассчитать стоимость
              </v-btn>
              <v-checkbox
                v-model="useCorrections"
                v-show="selectedPool.id || selectedObjects.length"
                class="mr-3 mt-3"
                label="Использовать корректировки при расчете"
              ></v-checkbox>
            </div>
            <v-spacer></v-spacer>
          </template>
          <v-card>
            <v-card-title>
              <span class="text-h5">{{ formTitle }}</span>
            </v-card-title>

            <v-card-text>
              <v-container>
                <v-row>
                  <v-col
                    cols="12"
                    sm="6"
                    md="4"
                  >
                    <v-text-field
                      v-model="editedItem.location"
                      label="Адрес"
                    ></v-text-field>
                  </v-col>
                  <v-col
                    cols="12"
                    sm="6"
                    md="4"
                  >
                    <v-text-field
                      v-model="editedItem.roomsNumber"
                      type="number"
                      label="Количество комнат"
                    ></v-text-field>
                  </v-col>
                  <v-col
                    cols="12"
                    sm="6"
                    md="4"
                  >
                    <v-autocomplete
                      v-model="editedItem.segment"
                      :items="segments"
                      label="Сегмент"
                    ></v-autocomplete>
                  </v-col>
                  <v-col
                    cols="12"
                    sm="6"
                    md="4"
                  >
                    <v-text-field
                      v-model="editedItem.floorCount"
                      label="Этажность дома"
                      type="number"
                    ></v-text-field>
                  </v-col>
                  <v-col
                    cols="12"
                    sm="6"
                    md="4"
                  >
                    <v-autocomplete
                      v-model="editedItem.wallMaterial"
                      :items="wallMaterials"
                      label="Материал стен"
                    ></v-autocomplete>
                  </v-col>
                  <v-col
                    cols="12"
                    sm="6"
                    md="4"
                  >
                    <v-text-field
                      v-model="editedItem.floorNumber"
                      label="Этаж расположения"
                      type="number"
                    ></v-text-field>
                  </v-col>
                  <v-col
                    cols="12"
                    sm="6"
                    md="4"
                  >
                    <v-text-field
                      v-model="editedItem.totalArea"
                      label="Площадь квартиры, кв.м"
                      type="number"
                    ></v-text-field>
                  </v-col>
                  <v-col
                    cols="12"
                    sm="6"
                    md="4"
                  >
                    <v-text-field
                      v-model="editedItem.kitchenArea"
                      label="Площадь кухни, кв.м"
                      type="number"
                    ></v-text-field>
                  </v-col>
                  <v-col
                    cols="12"
                    sm="6"
                    md="4"
                  >
                  <v-checkbox
                    v-model="editedItem.gotBalcony"
                    label="Наличие балкона/лоджии"
                  ></v-checkbox>
                  </v-col>
                  <v-col
                    cols="12"
                    sm="6"
                    md="4"
                  >
                    <v-text-field
                      v-model="editedItem.distanceFromMetro"
                      label="Удаленность от станции метро, мин. пешком"
                      type="number"
                    ></v-text-field>
                  </v-col>
                  <v-col
                    cols="12"
                    sm="6"
                    md="4"
                  >
                    <v-autocomplete
                      v-model="editedItem.condition"
                      :items="conditions"
                      label="Состояние"
                    ></v-autocomplete>
                  </v-col>
                  <v-col
                    cols="12"
                    sm="6"
                    md="4"
                  >
                  <v-autocomplete 
                    label="Пул"
                    v-model="editedItem.poolId"
                    :items="pools"
                    item-value="id"
                    item-text="name"
                  >
                  </v-autocomplete>
                  </v-col>
                </v-row>
              </v-container>
            </v-card-text>

            <v-card-actions>
              <v-spacer></v-spacer>
              <v-btn
                color="blue darken-1"
                text
                @click="close"
              >
                Отменить
              </v-btn>
              <v-btn
                color="blue darken-1"
                text
                @click="save"
              >
                Сохранить
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>
        <v-dialog v-model="dialogPool" max-width="700px">
          <v-card>
            <v-card-title class="text-h6">Создание нового пула</v-card-title>
            <v-card-text class="mb-5">
              <v-tooltip bottom content-class="tooltip">
                 <template v-slot:activator="{ on, attrs }">
                  <v-text-field
                    v-bind="attrs"
                    v-on="on"
                    v-model="poolName"
                    label="Название пула"
                    hide-details
                  ></v-text-field>
                </template>
                <span>Можно оставить поле пустым, тогда имя сгенерируется автоматически</span>
              </v-tooltip>
            </v-card-text>
            <v-card-actions>
              <v-spacer></v-spacer>
              <v-btn
                color="blue darken-1"
                text
                @click="closeDialogPool"
              >
                Отменить
              </v-btn>
              <v-btn
                color="blue darken-1"
                text
                @click="createPool"
              >
                Сохранить
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>
        <v-dialog
          v-model="dialogDelete"
          max-width="900px"
        >
          <v-card>
            <v-card-title class="text-h6">Вы уверены, что хотите удалить объект недвижимости?</v-card-title>
            <v-card-actions>
              <v-spacer></v-spacer>
              <v-btn color="blue darken-1" text @click="dialogDelete = false">Отменить</v-btn>
              <v-btn color="blue darken-1" text @click="deleteItemConfirm">Удалить</v-btn>
              <v-spacer></v-spacer>
            </v-card-actions>
          </v-card>
        </v-dialog>
      </v-toolbar>
    </template>
    <template v-slot:item.actions="{ item }">
      <v-icon
       isMobile
        v-bind="isMobile ? 'large' : 'small'"
        :class="isMobile ? 'mr-5' : 'mr-2'"
        @click="editItem($event, item)"
      >
        mdi-pencil
      </v-icon>
      <v-icon
        v-bind="isMobile ? 'large' : 'small'"
        :class="isMobile ? 'mr-5' : 'mr-2'"
        @click="copyItem($event, item)"
      >
        mdi-content-copy
      </v-icon>
      <v-icon
        v-bind="isMobile ? 'large' : 'small'"
        @click="deleteItem($event, item)"
      >
        mdi-delete
      </v-icon>
    </template>
    </v-data-table>
  </div>
</template>

<script>
import RequestService from "@/services/RequestService";
import MobileDetect from "mobile-detect";
import * as consts from "@/consts/consts";
import { mapGetters } from 'vuex'

export default {
  name: "AllRealtyObjects",
  components: {},

  data: () => ({
    defaultItem: {
      location: null,
      roomsNumber: null,
      segment: null,
      floorCount: null,
      wallMaterial: null,
      floorNumber: null,
      totalArea: null,
      kitchenArea: null,
      gotBalcony: false,
      condition: null,
      distanceFromMetro: null,
      poolId: null,
    },


    selectedObjects: [],
    dialog: false,
    dialogDelete: false,
    dialogPool: false,
    poolName: '',
    editedIndex: -1,
    isSelecting: false,
    useCorrections: false,
    calculatedValue: 1,

    addressItems: [],
    searchValue: '',

    editedItem: {},
    selectedPool: {id: null, name: 'Все'},
    realtyObjects: [],
  }),

  computed: {
    noDataText() {
      if (this.selectedPool?.id) {
        return 'В выбранном пуле нет объектов недвижимости. Вы можете добавить объекты в выбранный пул по кнопке "Добавить объект"'
      }

      if (!this.pools?.length) {
        return 'Нет загруженных объектов недвижимости. Добавьте новый пул вручную или импортируйте объекты из excel'
      }

      return 'Нет данных'
    },

    footerProps() {
      const options = {
        'items-per-page-all-text': 'Все',
      }
      return options
    },

     filteredItems () {
      let res = this.realtyObjects;

      if (this.selectedPool?.id) {
        res = res.filter(x => {
          return x.poolId === this.selectedPool.id;
        })
      }

      if (this.calculatedValue === 2) {
        res = res.filter(x => {
          return x.calculatedValue
        })
      }

      if (this.calculatedValue === 3) {
        res = res.filter(x => {
          return !x.calculatedValue
        })
      }

      return res;
    },

    ...mapGetters(['pools']),

    poolsToSelect() {
      return [ {id: null, name: 'Все'}, ...this.pools]
    },

    conditions() {
      return consts.conditions;
    },

    wallMaterials() {
      return consts.wallMaterials;
    },

    segments() {
      return consts.segments;
    },

    valueFilter() {
      return consts.valueFilter;
    },

    isMobile() {
      return !!new MobileDetect(window.navigator.userAgent).mobile()
    },

    headers() {
      return [
         { text: 'Адрес', value: 'location', },
         { text: 'Площадь', value: 'totalArea' },
         { text: 'Сегмент', value: 'segment' },
         { text: 'Этаж', value: 'floorNumber' },
         { text: 'Состояние', value: 'condition' },
          { text: 'Действия', value: 'actions'},
      ]
    },

    formTitle () {
      return this.editedIndex === -1 ? 'Новый объект' : 'Редактирование объекта'
    },
  },

  async created() {
    [this.realtyObjects] = await Promise.all([
      RequestService.getAllRealtyObjectsAsync(),
      this.$store.dispatch('loadlAllPoolsAsync')
    ])
  },

  watch: {
    selectedPool() {
      this.defaultItem.poolId = this.selectedPool?.id;
      this.editedItem.poolId = this.selectedPool?.id;
    },
  },

  methods: {
    async exportFile() {
      if (this.selectedPool?.id) {
        await RequestService.exportPoolById(this.selectedPool?.id, this.selectedPool.name);
      } else {
        await RequestService.exportAllObjects('AllRealtyObjects');
      }
    },

    async calculatePool() {
      if (this.selectedObjects.length) {
        await RequestService.calculateSelectedObjectsIdAsync(this.selectedObjects?.map(x => x.id), this.useCorrections);
        this.$bus.$emit("showSuccess", "Выбранные объекты успешно отправлены для рассчета стоимости");
      } else {
        await RequestService.calculatePoolByIdAsync(this.selectedPool?.id, this.useCorrections);
        console.log('here')
        this.$bus.$emit("showSuccess", "Выбранный пул успешно отправлен для рассчета стоимости");
      }
    },
    
    async createPool() {
      const newPool = await RequestService.createPoolAsync(this.poolName.trim())
      this.$store.dispatch('addPool', {pool: newPool});
      this.selectedPool = newPool;
      this.poolName = ''
      this.dialogPool = false
    },

    closeDialogPool() {
      this.dialogPool = false
      this.poolName = '';
    },

    editItem (e, item) {
      if (this.isMobile) {
        e.stopPropagation()
      }
      
        console.log('here')
        this.editedIndex = this.realtyObjects.indexOf(item)
        this.editedItem = Object.assign({}, item)
        this.dialog = true
      },

    copyItem (e, item) {
        if (this.isMobile) {
          e.stopPropagation()
        }

        this.editedItem = Object.assign({}, item)
        // удаляем служебную информацию
        delete this.editedItem.analogs;
        delete this.editedItem.id;
        delete this.editedItem.createdAt;
        delete this.editedItem.updatedAt;
        delete this.editedItem.coordinates;
        this.dialog = true
      },

      deleteItem (e, item) {
        if (this.isMobile) {
          e.stopPropagation()
        }

        this.editedIndex = this.realtyObjects.indexOf(item)
        this.editedItem = Object.assign({}, item)
        this.dialogDelete = true
      },

      async deleteItemConfirm () {
        if (await RequestService.removeRealtyObjectAsync(this.editedItem.id)) {
          this.realtyObjects.splice(this.editedIndex, 1)
        }
        this.closeDelete()
      },

      close () {
        this.dialog = false
        this.$nextTick(() => {
          this.editedItem = Object.assign({}, this.defaultItem)
          this.editedIndex = -1
        })
      },

      closeDelete () {
        this.dialogDelete = false
        this.$nextTick(() => {
          this.editedItem = Object.assign({}, this.defaultItem)
          this.editedIndex = -1
        })
      },

      async save () {
        if (this.editedIndex > -1) {
          if (await RequestService.editRealtyObjectAsync(this.editedItem)) {
            Object.assign(this.realtyObjects[this.editedIndex], this.editedItem)
          }
        } else {
          const asnwer = await RequestService.createRealtyObjectAsync(this.editedItem)
          if (asnwer) {
            this.realtyObjects.push(asnwer)
          }
        }
        this.close()
      },

    handleFileImport() {
      this.isSelecting = true;

      window.addEventListener(
        "focus",
        () => {
          this.isSelecting = false;
        },
        { once: true }
      );

      this.$refs.uploader.click();
    },

    async onFileChanged(e) {
      const poolId = await RequestService.loadFileAsync(e.target.files[0]);
      await this.$store.dispatch('loadPoolAsync', { poolId })
      this.realtyObjects = await RequestService.getAllRealtyObjectsAsync();
      this.selectedPool = this.pools.find(x => x.id === poolId);
    },
  },
};
</script>
<style lang="scss">
    .custom-filter-items {
      display: flex;
    }
  @media (max-width: 720px) {
    .custom-toolbar > .v-toolbar__content {
       flex-wrap: wrap !important;
     }
   
    .tooltip {
       width: 70%
     }
    .custom-filter-items {
      display: block;
    }
  }

  @media (max-width: 600px) {
    .v-data-table-header-mobile__wrapper {
      flex-direction: row-reverse;

      .v-input--selection-controls__input {
        margin-right: -15px;
      }
    }
  }


</style>
