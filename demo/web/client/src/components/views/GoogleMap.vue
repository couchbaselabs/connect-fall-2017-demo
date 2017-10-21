<template>
  <div>
    <GmapMap style="width: 100%; height: 400px;" :zoom="1" :center="{lat: 0, lng: 0}"
        ref="map">
      <GmapMarker v-for="(marker, index) in patients"
        :key="index"
        :position="marker.position"
        :icon='{ url: "/static/img/Map-Marker-Blue.png"}'
        />
      <GmapMarker v-for="(marker, index) in hospitals"
        :key="index"
        :position="marker.position"
        :icon='{ url: "/static/img/redcross_34.png" }'>
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

      this.message = null
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

      let facilities = {}

      for (let arr of data) {
        let matched = arr[0]

        this.patients.push({ position: { lat: Math.degrees(matched.pat.lat), lng: Math.degrees(matched.pat.lng) } })

        let details = matched.details

        if (facilities[details.name]) continue

        facilities[details.name] = true

        let html = `<div class='iw-frame'><div class='iw-name'>${details.name}</div>`

        if (details.url) html += `<p><a href="${details.url}">${details.url}</a></p>`
        if (details.rating) html += `<p>Rating : ${details.rating}</p></div>`

        html += '</div>'

        this.hospitals.push({ position: { lat: Math.degrees(matched.fac.lat), lng: Math.degrees(matched.fac.lng) },
          text: html })
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

<style>
.gm-style-iw {
  background-color:#48b5e9;
  text-align: center;
  border-radius: 3px;
	font-weight: 400;
	padding: 10px;
	color: white;
	margin: 0;
}

.iw-frame {
	font-weight: 400;
}
</style>
