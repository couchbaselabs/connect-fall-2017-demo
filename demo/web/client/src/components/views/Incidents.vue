<template>
  <section class="content">
  <!--    <h1 class="text-center">Incidents</h1>
    <h4 class="text-center">Locations of Incidents</h4> -->

    <div class="row">

        <!-- MAP & BOX PANE -->

              <div class="col-md-12">
                  <!-- Map will be created here -->
                  <GoogleMap></GoogleMap>
              </div>

    </div>
  </section>
</template>
<script>
import GoogleMap from './GoogleMap'

export default {
  name: 'Incidents',
  components: {
    GoogleMap
  },
  data () {
    return {
    }
  },
  mounted () {
  }
}
</script>

<style>
.fake {
  color: 'red';
}

#world-map-markers svg {
  height: 355px;
}

.row.no-gutters {
  margin-right: 0;
  margin-left: 0;
}

.row.no-gutters > [class^="col-"],
.row.no-gutters > [class*=" col-"] {
  padding-right: 0;
  padding-left: 0;
}
</style>
