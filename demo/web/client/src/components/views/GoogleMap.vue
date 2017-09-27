<template>
  <div>
    <GmapMap style="width: 100%; height: 400px;" :zoom="1" :center="{lat: 0, lng: 0}"
        ref="map">
      <GmapMarker v-for="(marker, index) in markers"
        :key="index"
        :position="marker.position"
        />
    </GmapMap>
  </div>
</template>

<script>
import api from '../../api'

export default {
  name: 'GoogleMap',
  data () {
    return {
      markers: []
    }
  },
  description: `
  In which a random set of points are generated, and
  the bounds of the map are changed to fit the points
  `,
  watch: {
    markers (markers) {
      if (markers.length > 2) {
        const bounds = new google.maps.LatLngBounds() // eslint-disable-line no-undef
        for (let m of markers) {
          bounds.extend(m.latLng)
        }
        this.$refs.map.$mapObject.fitBounds(bounds)
      }
    }
  },
  methods: {
    toggleLoading () {
      this.loading = (this.loading === '') ? 'loading' : ''
    },
    resetResponse () {
      this.response = ''
    }
  },
  mounted: function () {
    for (let patient of this.$store.state.cohort) {
      api.request('get', `/patient/${patient}/location`, null)
      .then(response => {
        this.toggleLoading()

        var data = response.data

        if (data.error) {
          var errorName = data.error.name
          if (errorName) {
            this.response = errorName === 'InvalidCohortError'
            ? 'Invalid cohort. Please try again.'
            : errorName
          } else {
            this.response = data.error
          }

          return
        }

        this.markers.push({ position: { lat: data.lat, lng: data.lng } })
      })
      .catch(error => {
        this.$store.commit('TOGGLE_LOADING')
        console.log(error)

        this.response = 'Server appears to be offline'
        this.toggleLoading()
      })
    }
  }
}
</script>
