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
          bounds.extend(m.position)
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
    api.request('post', `/patient/cohort/locations`, this.$store.state.cohort)
    .then(response => {
      this.toggleLoading()

      let data = response.data

      if (data.error) {
        let errorName = data.error.name

        if (errorName) {
          this.response = errorName === 'InvalidCohortError'
          ? 'Invalid cohort. Please try again.'
          : errorName
        } else {
          this.response = data.error
        }

        return
      }

      for (let arr of data) {
        let matched = arr[0]
        this.markers.push({ position: { lat: Math.degrees(matched.pat.lat), lng: Math.degrees(matched.pat.lng) } })
        this.markers.push({ position: { lat: Math.degrees(matched.fac.lat), lng: Math.degrees(matched.fac.lng) } })
      }
    })
    .catch(error => {
      this.$store.commit('TOGGLE_LOADING')
      console.log(error)

      this.response = 'Server appears to be offline'
      this.toggleLoading()
    })
  }
}
</script>
