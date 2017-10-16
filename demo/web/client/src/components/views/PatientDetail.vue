<template>
  <!-- Main content -->
  <section class="content">
    <div class="box">
      <div class="box-header with-border">
        <h3 class="box-title">Patient Profile</h3>
      </div>
      <!-- /.box-header -->
      <div class="box-body" style="padding-left:24px;">
        <div class="container">
          <div class="row">
            <div v-if="patient" class="col-md-6">
              <p><strong>Name:</strong>  {{ patient.name[0].family[0] }}, {{ patient.name[0].given[0] }}</p>
              <p><strong>Gender:</strong>  {{ patient.gender.text }}</p>
              <p><strong>Birth Date:</strong>  {{ new Date(patient.birthDate).toDateString() }}</p>
            </div>
            <div v-if="patient && patient.extension" class="col-6">
              <template v-for="(value, key, index) in patient.extension">
                <p :key="key"><strong>{{ toCapitalized(key) }}:</strong> {{ value }}</p>
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="row">
      <div class="col-xs-12">
        <div class="box">
          <div class="box-header with-border">
            <div class="box-body">
              <div class="col-md-6">
                <p><strong>Temperature Graph</strong></p>
                <canvas id="temperature" ></canvas>
              </div>
              <div class="col-md-6" style="padding-left:32px;">
                <img src="/static/img/patient_profile.png" height="auto" width="500px">
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="row">
      <div class="col-md-12">
        <div class="box">
          <div class="box-header">
            <h3 class="box-title">Patient History</h3>
          </div>
          <!-- /.box-header -->
          <div class="box-body">
            <div class="dataTables_wrapper form-inline dt-bootstrap" id="example1_wrapper">
              <div class="row">
                <div class="col-sm-6">
                  <div id="example1_length" class="dataTables_length">
                  </div>
                </div>
              </div>

              <div v-if="history" class="row">
                <div class="col-sm-12 table-responsive">
                  <Conditions :conditions="conditions"></Conditions>
                </div>
              </div>
            </div>
            <!-- /.box-body -->
          </div>
        </div>
      </div>
    </div>
  </section>
  <!-- /.content -->
</template>

<script>
import Conditions from './Conditions'
import EventBus from '../../event-bus'
import Chart from 'chart.js'
import api from '../../api'
import config from '../../config'
import stringUtils from '../../bin/string_utils'

export default {
  components: {
    Conditions
  },
  data () {
    return {
      patientModel: null,
      patient: null,
      error: null,
      history: null,
      conditions: null
    }
  },
  computed: {
    isMobile () {
      return (window.innerWidth <= 800 && window.innerHeight <= 600)
    }
  },
  methods: {
    getConditions () {
      this.conditions = []

      api.request('get', `/records/patient/${config.id}/conditions`)
      .then(response => {
        this.history = true

        response.data.map(record => {
          let condition = record.condition
          let date = new Date(condition.assertedDate ? condition.assertedDate : condition.onsetDateTime)

          condition.date = date

          this.conditions.push(condition)
        })

        this.conditions.sort((a, b) => { return a.date < b.date })
      })
      .catch(error => {
        console.log(error)
      })
    },
    getModelAndDoc (name, route) {
      return api.request('get', `/db/model/${name}`)
      .then(response => {
        var data = response.data

        this[`${name}Model`] = data
      })
      .then(() => api.request('get', route))
      .then(response => {
        var data = response.data

        this[name] = data[0].health
      })
      .catch(error => {
        console.log(error)
      })
    },
    toCapitalized (string) {
      return stringUtils.toCapitalized(string)
    }
  },
  mounted () {
    this.getModelAndDoc('patient', `/records/patient/${config.id}`)
    this.getConditions()
    this.lastSampleTime = 0

    this.$nextTick(() => {
      let ctx = document.getElementById('temperature').getContext('2d')

      var config = {
        type: 'line',
        data: {
          labels: [],
          datasets: [{
            label: '°F',
            fill: false,
            borderColor: '#284184',
            pointBackgroundColor: '#284184',
            backgroundColor: 'rgba(0, 0, 0, 0)',
            data: []
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: !this.isMobile,
          legend: {
            position: 'bottom',
            display: true
          },
          tooltips: {
            mode: 'label',
            xPadding: 10,
            yPadding: 10,
            bodySpacing: 10
          }
        }
      }

      this.temperatureChart = new Chart(ctx, config) // eslint-disable-line no-new
      this.updateCB = (data) => {
        let chartData = this.temperatureChart.data.datasets[0].data
        let chartLabels = this.temperatureChart.data.labels

        data.values.forEach(sample => {
          let recordedAt = new Date(sample.recordedAt)

          if (this.lastSampleTime >= recordedAt.valueOf()) return

          this.lastSampleTime = recordedAt.valueOf()

          let time = recordedAt.toTimeString().substr(0, 8)

          chartData.push(sample.value)
          chartLabels.push(time)

          if (chartData.length <= 12) return

          chartData.shift()
          chartLabels.shift()
        }, this)

        this.temperatureChart.update()
      }

      EventBus.$on('update', this.updateCB)
    })
  },
  destroyed () {
    EventBus.$off('update', this.updateCB)
  }
}
</script>

<style>
.info-box {
  cursor: pointer;
}
.info-box-content {
  text-align: center;
  vertical-align: middle;
  display: inherit;
}
.fullCanvas {
  width: 100%;
}
</style>
