<template>
  <!-- Main content -->
  <section class="content" style="padding:16px!important;">
    <img src="/static/img/dashboard3.png" width="99%" height="auto">
  </section>
  <!-- /.content -->
</template>

<script>
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
