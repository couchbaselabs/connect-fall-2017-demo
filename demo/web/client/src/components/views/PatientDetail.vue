<template>
  <!-- Main content -->
  <section class="content">
    <div class="row">
      <div class="box">
        <div class="box-header with-border">
          <h3 class="box-title">Default Box Example</h3>
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
  </section>
  <!-- /.content -->
</template>

<script>
import EventBus from '../../event-bus'
import Chart from 'chart.js'
import api from '../../api'
import config from '../../config'

export default {
  data () {
    return {
      patientModel: null,
      patient: null,
      error: null
    }
  },
  computed: {
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
    }
  },
  mounted () {
    this.getModelAndDoc('patient', `/patient/${config.id}`)

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
