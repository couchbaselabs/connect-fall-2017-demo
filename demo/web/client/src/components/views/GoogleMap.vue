<template>
  <div>
    <GmapMap style="width: 100%; height: 400px;" :zoom="1" :center="{lat: 0, lng: 0}"
        ref="map">
      <GmapMarker v-for="(marker, index) in patients"
        :key="index"
        :position="marker.position"
        />
      <GmapMarker v-for="(marker, index) in hospitals"
        :key="index"
        :position="marker.position"
        :icon='{ url: "/static/img/mapicon_hospital.png" }'>
          <GmapInfoWindow :options="{ content: marker.text }"/>
      </GmapMarker>
    </GmapMap>
    <div class="row col-md-12" style="margin-top: 32px;">
    <div class="col-md-3">
    </div>
      <div class="col-md-4">
        <input v-model="message" type="text" class="col-md-12 input-lg" placeholder="Add a message">
      </div>
      <div class="col-md-5">
        <button v-on:click="messagePatients" class="btn btn-lg btn-primary">Message Patients</button>
      </div>
    </div>
  </div>
</template>

<script>
import api from '../../api'

export default {
  name: 'GoogleMap',
  data () {
    return {
      patients: [],
      hospitals: [],
      bounds: false,
      message: null
    }
  },
  description: ``,
  watch: {
    bounds () {
      if (this.patients.length + this.hospitals.length < 2) return

      const bounds = new google.maps.LatLngBounds() // eslint-disable-line no-undef

      for (let p of this.patients) {
        bounds.extend(p.position)
      }

      for (let h of this.hospitals) {
        bounds.extend(h.position)
      }

      this.$refs.map.$mapObject.fitBounds(bounds)
    }
  },
  methods: {
    messagePatients () {
      api.request('post', '/messaging/alert', {
        'audience': 'all',
        'notification': { 'alert': `${this.message}` },
        'device_types': 'all'
      })
      .catch(error => console.log(error))
    },
    toggleLoading () {
      this.loading = (this.loading === '') ? 'loading' : ''
    },
    resetResponse () {
      this.response = ''
    }
  },
  mounted: function () {
    api.request('post', `/records/patient/cohort/locations`, this.$store.state.cohort)
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
        let details = matched.details
        let text = `<div id="content"><p><b>${details.name}</b></p>`

        if (details.url) text += `<p><a href="${details.url}">${details.url}</a></p>`
        if (details.rating) text += `<p>Rating : ${details.rating}</p></div>`

        this.patients.push({ position: { lat: Math.degrees(matched.pat.lat), lng: Math.degrees(matched.pat.lng) } })
        this.hospitals.push({ position: { lat: Math.degrees(matched.fac.lat), lng: Math.degrees(matched.fac.lng) },
          text: text })
      }

      this.bounds = true
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
