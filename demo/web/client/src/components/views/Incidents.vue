<template>
  <section class="content">
    <h1 class="text-center">Incidents</h1>
    <h4 class="text-center">Locations of Incidents</h4>

    <div class="row">
      <div class="col-md-12">
        <!-- MAP & BOX PANE -->
        <div class="box box-success">
          <div class="box-header with-border">
            <h3 class="box-title">Incident Map</h3>
          </div>
          <!-- /.box-header -->
          <div class="box-body no-padding">
            <div class="row no-gutters">
              <div class="col-md-8">
                  <!-- Map will be created here -->
                  <GoogleMap></GoogleMap>
              </div>
            </div>
            <!-- /.row -->
          </div>
          <!-- /.box-body -->
        </div>
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
