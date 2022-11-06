<template>
  <v-card v-if="editedCorrection" class="mt-2">
    <v-card-text>
      <v-container>
        <v-row>
          <v-col cols="12" sm="6" md="4">
            <v-autocomplete
              v-model="editedCorrection.fieldName"
              item-value="value"
              item-text="label"
              :items="consts.correctionsFields"
              label="Поле"
              :readonly="mode === 'read'"
            ></v-autocomplete>
          </v-col>
          <v-col cols="12" sm="6" md="4">
            <v-text-field
              v-model="editedCorrection.referenceValue"
              type="number"
              label="Значение оцениваемого объекта"
              :readonly="mode === 'read'"
            ></v-text-field>
          </v-col>
          <v-col cols="12" sm="6" md="4">
            <v-autocomplete
              v-model="editedCorrection.referenceValueType"
              item-value="value"
              item-text="label"
              :items="consts.comparisons"
              label="Тип сравнения"
              :readonly="mode === 'read'"
            ></v-autocomplete>
          </v-col>
          <v-col cols="12" sm="6" md="4">
            <v-text-field
              v-model="editedCorrection.analogueValue"
              label="Значение аналога"
              type="number"
              :readonly="mode === 'read'"
            ></v-text-field>
          </v-col>
          <v-col cols="12" sm="6" md="4">
            <v-autocomplete
              v-model="editedCorrection.analogueValueType"
              item-value="value"
              item-text="label"
              :items="consts.comparisons"
              label="Тип сравнения"
              :readonly="mode === 'read'"
            ></v-autocomplete>
          </v-col>
          <v-col cols="12" sm="6" md="4">
            <v-text-field
              v-model="editedCorrection.correction"
              label="Значение корректировки"
              type="number"
              :readonly="mode === 'read'"
            ></v-text-field>
          </v-col>
          <v-col cols="12" sm="6" md="4">
            <v-autocomplete
              v-model="editedCorrection.correctionType"
              item-value="value"
              item-text="label"
              :items="consts.correctionsTypes"
              label="Тип корректировки"
              :readonly="mode === 'read'"
            ></v-autocomplete>
          </v-col>
          <v-spacer></v-spacer>
          <v-col v-if="mode !== 'read'" cols="12" sm="6" md="4" class="d-flex align-end justify-end">
            <v-btn dark small v-if="mode === 'edit'" @click="remove">Удалить</v-btn>
            <v-btn dark small v-if="mode === 'create'" @click="cancel">Отменить</v-btn>
            <v-btn dark small @click="save" class="ml-5">Сохранить</v-btn>
          </v-col>
        </v-row>
      </v-container>
    </v-card-text>
  </v-card>
</template>
<script>
import RequestService from "@/services/RequestService";

import { mapGetters } from "vuex";
import * as consts from "@/consts/consts";

export default {
  name: "CorrectionsPage",
  components: {},

  props: {
    correction: {default: null},
    mode: {default: 'read'}
  },

  data: () => ({
    objectMap: null,
    editedCorrection: {},
  }),

  computed: {
    ...mapGetters(["pools"]),

    consts() {
      return consts;
    },
  },

  async created() {
    if (this.mode === 'create' || this.mode === 'edit' ) {
      this.editedCorrection = Object.assign({}, this.correction)
    }
  },

  methods: {
    async save() {
      if (this.mode === 'create') {
        await RequestService.createCorrection(this.editedCorrection);
      }
      if (this.mode === 'edit') {
        await RequestService.editCorrection(this.editedCorrection);
      }
      
      this.$emit('correctionsCanged')
    },

    async remove() {
      await RequestService.removeCorrection(this.editedCorrection.id);
      this.$emit('correctionsCanged')
    },

    cancel() {
      this.$emit('cancel')
    },
  }
};
</script>
