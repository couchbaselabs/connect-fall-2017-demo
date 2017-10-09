<template>
  <!-- Main content -->
  <section class="content">
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
  </section>
  <!-- /.content -->
</template>

<script>
import EventBus from '../../event-bus'
import Chart from 'chart.js'

export default {
  computed: {
    isMobile () {
      return (window.innerWidth <= 800 && window.innerHeight <= 600)
    }
  },
  methods: {
    /*
    connectFeed (id) {
      let es = new EventSource(`${config.serverURI}/feed/${id}`)

      es.addEventListener('update', event => {
        let data = JSON.parse(event.data)
        let chartData = this.temperatureChart.data.datasets[0].data

        chartData.push(data.values[0])

        if (chartData.length > 12) {
          chartData.shift()
        }

        this.temperatureChart.update()
      }, false)

      es.addEventListener('error', event => {
        if (event.readyState === EventSource.CLOSED) {
          console.log('Event was closed')
          console.log(EventSource)
        }
      }, false)

      this.feed = es
    },
    disconnectFeed () {
      if (this.feed) {
        this.feed.close()
        this.feed = null
      }
    },
    */
    sleep (ms) {
      return new Promise(resolve => setTimeout(resolve, ms))
    }
  },
  created () {
    this.$nextTick(() => {
      let ctx = document.getElementById('temperature').getContext('2d')

      var config = {
        type: 'line',
        data: {
          labels: ['8am', '9am', '10am', '11am', '12pm', '1pm', '2pm', '3pm', '4pm', '5pm', '6pm', '7pm'],
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
        let chartData = this.temperatureChart.data.datasets[0].data

        chartData.push(data.values[0])

        if (chartData.length > 12) {
          chartData.shift()
        }

        this.temperatureChart.update()
      })
    })
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
