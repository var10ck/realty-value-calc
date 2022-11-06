<template>
  <div>
    <div class="pt-5">
      <h1>Корректировки</h1>
      <CorrectionCard v-if="editedCorrection" :correction="editedCorrection" mode='create' @cancel="editedCorrection=null" @correctionsCanged="correctionsCanged" class="mt-2"></CorrectionCard>
      <v-btn v-else dark @click="editedCorrection = {}">Добавить</v-btn>
      <CorrectionCard v-for="(correction, i) in corrections" :key="i" :correction="correction" @correctionsCanged="correctionsCanged" mode='edit' class="mt-2"></CorrectionCard>
    </div>
  </div>
</template>
<script>
import RequestService from "@/services/RequestService";

import CorrectionCard from "@/components/CorrectionCard";
import { mapGetters } from "vuex";
import * as consts from "@/consts/consts";

export default {
  name: "CorrectionsPage",
  components: {
    CorrectionCard,
  },

  data: () => ({
    objectMap: null,
    corrections: [],
    editedCorrection: null,
  }),

  computed: {
    ...mapGetters(["pools"]),

    consts() {
      return consts;
    },
  },

  async created() {
    this.corrections = await RequestService.getAllCorrections()
  },

  methods: {
    async correctionsCanged() {
      this.corrections = await RequestService.getAllCorrections();
      this.editedCorrection = null;
    }
  }
};
</script>
