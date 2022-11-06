<template>
  <div class="pt-5">
    <h1>Карта загруженных объектов</h1>
    <ObjectsMap></ObjectsMap>
  </div>
</template>
<script>
import ObjectsMap from "@/components/ObjectsMap";

export default {
  name: 'MapPage',
  components: {
    ObjectsMap,
  },
}
</script>
