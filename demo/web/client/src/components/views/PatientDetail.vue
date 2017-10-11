<template>
  <!-- Main content -->
  <section class="content">
    <div class="row">
      <div class="col-xs-6">
        <div class="box">
          <div class="box-header with-border">
            <h3 class="box-title">Patient Profile</h3>
          </div>
          <!-- /.box-header -->
          <div class="box-body">
            <div v-if="error">
              Found an error
            </div>
            <div v-else-if="patient">
              <!-- Rendering controlled by model object
              <ul>
                <template v-for="(value, key, index) in patientModel">
                  <li :key="key">
                    {{ patientModel[key] }} - {{ patient[key] ? patient[key] : '(missing)' }}
                  </li>
                </template>
              </ul>
              -->
              <!-- Fixed rendering -->
              <p><strong>Name:</strong>  {{ patient.name[0].family[0] }}, {{ patient.name[0].given[0] }}</p>
              <p><strong>Gender:</strong>  {{ patient.gender.text }}</p>
              <p><strong>Birth Date:</strong>  {{ new Date(patient.birthDate).toDateString() }}</p>
            </div>
            <div v-else>
              No data
            </div>
          </div>
          <!-- /.box-body -->
        </div>
        <!-- /.box -->
      </div>
      <div v-if="patient && patient.extension">
        <div class="col-xs-6">
          <div class="box">
            <div class="box-header with-border">
              <h3 class="box-title">Extended Profile</h3>
            </div>
            <!-- /.box-header -->
            <div class="box-body">
              <template v-for="(value, key, index) in patient.extension">
                <p :key="key"><strong>{{ toCapitalized(key) }}</strong> {{ value }}</p>
              </template>
            </div>
            <!-- /.box-body -->
          </div>
          <!-- /.box -->
        </div>
      </div>
    </div>
    <div class="row">
      <div class="col-xs-12">
        <div class="box">
          <div class="box-header with-border">
            <h3 class="box-title"></h3>
            <div class="box-body">
              <div class="col-sm-6 col-xs-12">
                <p class="text-center">
                  <strong>Temperature Graph</strong>
                </p>
                <canvas id="temperature" ></canvas>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="row center-block">
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
                  <table style="width:100%" aria-describedby="example1_info" role="grid" id="example1" class="table table-bordered table-striped dataTable">
                    <tr>
                      <th>Date</th>
                      <template v-for="(record, index) in history">
                        <td :key="index">{{ recordStartDate(record.condition) }}</td>
                      </template>
                    </tr>
                    <tr>
                      <th>Condition</th>
                      <template v-for="(record, index) in history">
                        <td :key="index">{{ record.condition.code.text }}</td>
                      </template>
                    </tr>
                    <tr>
                      <th>Status</th>
                      <template v-for="(record, index) in history">
                        <td :key="index">{{ record.condition.clinicalStatus }}</td>
                      </template>
                    </tr>
                    <tr>
                      <th>Notes</th>
                      <template v-for="(record, index) in history">
                        <td :key="index">
                          <div v-for="(note, index) in record.condition.note" :key="index">
                              {{ note.text }}
                            </div>
                        </td>
                      </template>
                    </tr>
                    <template v-for="(value, key) in extensions">
                      <tr :key="key">
                        <th>{{ toCapitalized(key) }}</th>
                        <td v-for="item in value" :key="item">{{ item }}</td>
                      </tr>
                    </template>
                  </table>
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
import EventBus from '../../event-bus'
import Chart from 'chart.js'
import api from '../../api'
import config from '../../config'
import stringUtils from '../../bin/string_utils'

export default {
  data () {
    return {
      patientModel: null,
      patient: null,
      error: null,
      history: null,
      extensions: null
    }
  },
  computed: {
    isMobile () {
      return (window.innerWidth <= 800 && window.innerHeight <= 600)
    }
  },
  methods: {
    getExtensions () {
      if (!this.history) return false

      let labels = {}
      this.extensions = {}

      this.history.map((record, index) => {
        if (record.condition.extension) {
          Object.entries(record.condition.extension)
          .map(entry => {
            if (!labels[entry[0]]) {
              labels[entry[0]] = true
              this.extensions[entry[0]] = Array.from({ length: this.history.length })
            }
            this.extensions[entry[0]][index] = entry[1]
          })
        }
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
    getPatientHistory () {
      // Just reporting Conditions for now
      return api.request('get', `/records/patient/${config.id}/conditions`)
      .then(response => {
        this.history = response.data
        this.getExtensions()
      })
      .catch(error => {
        console.log(error)
      })
    },
    recordStartDate (record) {
      if (record.assertedDate) return record.assertedDate
      if (record.onsetDateTime) return record.onsetDateTime

      return ''
    },
    toCapitalized (string) {
      return stringUtils.toCapitalized(string)
    }
  },
  mounted () {
    this.getModelAndDoc('patient', `/records/patient/${config.id}`)
    this.getPatientHistory()

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

      EventBus.$on('update', data => {
        let values = data.values[0]
        let chartData = this.temperatureChart.data.datasets[0].data
        let chartLabels = this.temperatureChart.data.labels
        let time = new Date(data.values[0].recordedAt).toTimeString().substr(0, 8)

        chartData.push(values.value)
        chartLabels.push(time)

        if (chartData.length > 12) {
          chartData.shift()
          chartLabels.shift()
        }

        this.temperatureChart.update()
      })
    })
  },
  destroyed () {
    EventBus.$off('update')
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
