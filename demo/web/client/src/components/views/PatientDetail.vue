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
              <ul>
                <template v-for="(value, key, index) in patientModel">
                  <li :key="key">
                    {{ patientModel[key] }} - {{ patient[key] ? patient[key] : '(missing)' }}
                  </li>
                </template>
              </ul>
            </div>
            <div v-else>
              No data
            </div>
          </div>
          <!-- /.box-body -->
          <div class="box-footer">
            The footer of the box
          </div>
          <!-- box-footer -->
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
            <div class="box-footer">
              The footer of the box
            </div>
            <!-- box-footer -->
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
                  <table aria-describedby="example1_info" role="grid" id="example1" class="table table-bordered table-striped dataTable">
                    <thead>
                      <tr role="row">
                        <th aria-label="Date" aria-sort="ascending" style="width: 80px;" colspan="1" rowspan="1" aria-controls="example1" tabindex="0" class="sorting_asc">Date</th>
                        <th aria-label="Condition" style="width: 160px;" colspan="1" rowspan="1" aria-controls="example1" tabindex="0" class="sorting">Condition</th>
                        <th aria-label="Status" style="width: 40px;" colspan="1" rowspan="1" aria-controls="example1" tabindex="0" class="sorting">Status</th>
                        <th aria-label="Notes" style="width: 200px;" colspan="1" rowspan="1" aria-controls="example1" tabindex="0" class="sorting">Notes</th>
                        <th v-if="this.hasExtensions" aria-label="Additional Information" style="width: 200px;" colspan="1" rowspan="1" aria-controls="example1" tabindex="0" class="sorting">Additional Information</th>
                       </tr>
                    </thead>
                    <tbody>
                      <template v-for="(record, index) in history">
                        <tr :class="(index & 1) ? 'odd' : 'even'" :key="index" role="row">
                          <td class="sorting_1">{{ recordStartDate(record.condition) }}</td>
                          <td>{{ record.condition.code.text }}</td>
                          <td>{{ record.condition.clinicalStatus }}</td>
                          <td>
                            <div v-for="(note, index) in record.condition.note" :key="index">
                              {{ note.text }}
                            </div>
                          </td>
                          <template v-if="hasExtensions">
                            <td v-for="(value, key, index) in record.condition.extension" :key="key">
                                <strong>{{ toCapitalized(key) }}</strong> {{ value }}
                            </td>
                          </template>
                        </tr>
                      </template>
                    </tbody>
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
      history: null
    }
  },
  computed: {
    hasExtensions () {
      if (!this.history) return false

      for (let record of this.history) {
        if (record.condition.extension) return true
      }

      return false
    },
    isMobile () {
      return (window.innerWidth <= 800 && window.innerHeight <= 600)
    }
  },
  methods: {
    getModelAndDoc (name, route) {
      api.request('get', `/db/model/${name}`)
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
      api.request('get', `/records/patient/${config.id}/conditions`)
      .then(response => {
        this.history = response.data

        console.dir(this.history)
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
        let date = new Date(data.values[0].recordedAt)
        let time = `${date.getDay()} ${date.getHours()} ${date.getMinutes()} ${date.getSeconds()}`

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
