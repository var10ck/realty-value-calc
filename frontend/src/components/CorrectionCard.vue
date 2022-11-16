<template>
  <v-card v-if="editedCorrection" class="mt-2">
    <v-card-text>
      <v-container>
        <v-row>
          <v-col cols="12" sm="4" md="3">
            <v-autocomplete
              v-model="editedCorrection.fieldName"
              item-value="value"
              item-text="label"
              :items="consts.correctionsFields"
              label="Поле"
              :readonly="mode === 'read'"
            ></v-autocomplete>
          </v-col>
          <v-col
            v-if="editedCorrection.fieldName == 'condition'"
            cols="12"
            sm="4"
            md="3"
          >
            <v-autocomplete
              v-model="editedCorrection.referenceValue"
              :items="consts.conditions"
              label="Значение оцениваемого объекта"
              :readonly="mode === 'read'"
            ></v-autocomplete>
          </v-col>
          <v-col
            v-else-if="editedCorrection.fieldName == 'gotBalcony'"
            cols="12"
            sm="4"
            md="3"
          >
            <v-autocomplete
              v-model="editedCorrection.referenceValue"
              item-value="value"
              item-text="label"
              :items="[
                {
                  value: 'yes',
                  label: 'Да',
                },
                {
                  value: 'no',
                  label: 'Нет',
                },
              ]"
              label="Значение оцениваемого объекта"
              :readonly="mode === 'read'"
            ></v-autocomplete>
          </v-col>
          <v-col v-else cols="12" sm="4" md="3">
            <v-text-field
              v-model="editedCorrection.referenceValue"
              type="number"
              label="Значение оцениваемого объекта"
              :readonly="mode === 'read'"
            ></v-text-field>
          </v-col>
          <v-col v-show="canShowFields" cols="12" sm="4" md="3">
            <v-autocomplete
              v-model="editedCorrection.referenceValueType"
              item-value="value"
              item-text="label"
              :items="consts.comparisons"
              label="Тип сравнения"
              :readonly="mode === 'read'"
            ></v-autocomplete>
          </v-col>
          <v-col
            v-if="editedCorrection.fieldName == 'condition'"
            cols="12"
            sm="4"
            md="3"
          >
            <v-autocomplete
              v-model="editedCorrection.analogueValue"
              :items="consts.conditions"
              label="Значение аналога"
              :readonly="mode === 'read'"
            ></v-autocomplete>
          </v-col>
          <v-col
            v-else-if="editedCorrection.fieldName == 'gotBalcony'"
            cols="12"
            sm="4"
            md="3"
          >
            <v-autocomplete
              v-model="editedCorrection.analogueValue"
              item-value="value"
              item-text="label"
              :items="[
                {
                  value: 'yes',
                  label: 'Да',
                },
                {
                  value: 'no',
                  label: 'Нет',
                },
              ]"
              label="Значение аналога"
              :readonly="mode === 'read'"
            ></v-autocomplete>
          </v-col>
          <v-col v-else cols="12" sm="4" md="3">
            <v-text-field
              v-model="editedCorrection.analogueValue"
              label="Значение аналога"
              type="number"
              :readonly="mode === 'read'"
            ></v-text-field>
          </v-col>
          <v-col v-show="canShowFields" cols="12" sm="4" md="3">
            <v-autocomplete
              v-model="editedCorrection.analogueValueType"
              item-value="value"
              item-text="label"
              :items="consts.comparisons"
              label="Тип сравнения"
              :readonly="mode === 'read'"
            ></v-autocomplete>
          </v-col>
          <v-col cols="12" sm="4" md="3">
            <v-text-field
              v-model="editedCorrection.correction"
              label="Значение корректировки"
              type="number"
              :readonly="mode === 'read'"
            ></v-text-field>
          </v-col>
          <v-col cols="12" sm="4" md="3">
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

          <v-col v-if="mode !== 'read'" cols="12" sm="4" md="3">
            <v-row class="d-flex flex-nowrap align-end justify-end">
              <v-checkbox
                v-model="editedCorrection.isEnabled"
                item-value="value"
                item-text="label"
                :items="consts.correctionsTypes"
                label="Активна"
                :readonly="mode === 'read'"
              />
            </v-row>
            <v-row class="d-flex flex-nowrap align-end justify-end">
              <v-btn dark small v-if="mode === 'edit'" @click="remove"
                >Удалить</v-btn
              >
              <v-btn dark small v-if="mode === 'create'" @click="cancel"
                >Отменить</v-btn
              >
              <v-btn dark small @click="save" class="ml-5">Сохранить</v-btn>
            </v-row>
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
    correction: { default: null },
    mode: { default: "read" },
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

    canShowFields() {
      return !["gotBalcony", "condition"].includes(
        this.editedCorrection?.fieldName
      );
    },
  },

  watch: {
    "editedCorrection.fieldName": function (newValue, oldValue) {
      if (newValue == oldValue || !oldValue) {
        return;
      }
      this.editedCorrection.analogueValue = null;
      this.editedCorrection.referenceValue = null;
      if (["gotBalcony", "condition"].includes(newValue)) {
        delete this.editedCorrection.analogueValueType;
        delete this.editedCorrection.referenceValueType;
      }
    },
  },

  async created() {
    if (this.mode === "create" || this.mode === "edit") {
      this.editedCorrection = Object.assign({}, this.correction);
      this.editedCorrection.isEnabled = !!this.editedCorrection.isEnabled;
    }
  },

  methods: {
    async save() {
      if (!this.validate()) {
        return; // !!!
      }

      // if (this.editedCorrection.fieldName == 'gotBalcony') {
      //   if (this.editedCorrection.analogueValue) {
      //     this.editedCorrection.analogueValue = 'yes'
      //   } else {
      //     this.editedCorrection.analogueValue = 'no'
      //   }
      //   if (this.editedCorrection.referenceValue) {
      //     this.editedCorrection.referenceValue = 'yes'
      //   } else {
      //     this.editedCorrection.referenceValue = 'no'
      //   }
      // }

      if (this.mode === "create") {
        await RequestService.createCorrection(this.editedCorrection);
        this.$bus.$emit("showSuccess", "Новая корректировка добавлена");
      }
      if (this.mode === "edit") {
        await RequestService.editCorrection(this.editedCorrection);
        this.$bus.$emit("showSuccess", "Корректировка успешно изменена");
      }

      this.$emit("correctionsCanged");
    },

    validate() {
      if (
        // (this.editedCorrection.fieldName == 'gotBalcony' &&
        // (!!this.editedCorrection.referenceValue ==
        // !!this.editedCorrection.analogueValue)) ||
        this.editedCorrection.referenceValue ==
        this.editedCorrection.analogueValue
      ) {
        this.$bus.$emit(
          "showError",
          "Значение оцениваемого объекта не может быть равно значению аналога!"
        );
        return false;
      }
      return true;
    },

    async remove() {
      await RequestService.removeCorrection(this.editedCorrection);
      this.$bus.$emit("showSuccess", "Корректировка успешно удалена");
      this.$emit("correctionsCanged");
    },

    cancel() {
      this.$emit("cancel");
    },
  },
};
</script>
