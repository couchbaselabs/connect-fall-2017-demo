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
    <!-- /.row -->
  </section>
  <!-- /.content -->
</template>

<script>
import Chart from 'chart.js'

export default {
  data () {
    return {
      generateRandomNumbers (numbers, max, min) {
        var a = []
        for (var i = 0; i < numbers; i++) {
          a.push(Math.floor(Math.random() * (max - min + 1)) + min)
        }
        return a
      }
    }
  },
  computed: {
    personalNumbers () {
      return this.generateRandomNumbers(12, 1000000, 10000)
    },
    isMobile () {
      return (window.innerWidth <= 800 && window.innerHeight <= 600)
    }
  },
  methods: {
    connectFeed (id) {
      let es = new EventSource('http://localhost:3000/feed/' + id)

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
    sleep (ms) {
      return new Promise(resolve => setTimeout(resolve, ms))
    }
  },
  mounted () {
    this.connectFeed('patient::perrykrug')

    this.$nextTick(() => {
      var ctx = document.getElementById('temperature').getContext('2d')
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

      var pieChartCanvas = document.getElementById('languagePie').getContext('2d')
      var pieConfig = {
        type: 'pie',
        data: {
          labels: ['HTML', 'JavaScript', 'CSS'],
          datasets: [{
            data: [56.6, 37.7, 4.1],
            backgroundColor: ['#00a65a', '#f39c12', '#00c0ef'],
            hoverBackgroundColor: ['#00a65a', '#f39c12', '#00c0ef']
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: !this.isMobile,
          legend: {
            position: 'bottom',
            display: true
          }
        }
      }

      new Chart(pieChartCanvas, pieConfig) // eslint-disable-line no-new
    })
  },
  beforeDestroy () {
    this.disconnectFeed()
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
