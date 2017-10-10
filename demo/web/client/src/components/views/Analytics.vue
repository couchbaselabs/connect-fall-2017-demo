<template>
  <!-- Main content -->
  <section class="content" style="margin-left: 16px;">
    <!-- Search field -->
    <div class="row">
      <form class="ui form" @submit.prevent="search">
        <div class="input-group" style="width:580px;margin-left:16px;">
          <input class="form-control" placeholder="" type="text" v-model="criteria">
          <span class="input-group-btn input-group-addon">
          <span class="input-group-btn">
            <button type="submit" style="border-width:0;background-color:#fff;outline:none;">
              <span class="input-group-addon" style="border-width:0;">
                <i class="fa fa-lg fa-search"></i>
              </span>
            </button>
            </span>
          </span>
        </div>
      </form>
    </div>
    <!-- /.row -->

    <!-- Results row -->
    <div class="row center-block" style="margin-top:32px;">
      <div class="col-md-12" style="padding-left:0;">
        <div class="box">
          <div class="box-header">
            <h3 class="box-title">Search Results</h3>
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

              <div class="row">
                <div class="col-xs-12">
                  <div class="box">
                    <div class="box-header with-border">
                      <h3 class="box-title"></h3>
                      <div class="box-body">
                        <div class="col-md-12">
                          <p class="text-center">
                            <strong>Analytics Chart</strong>
                          </p>
                          <canvas id="analytics"></canvas>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="row">
                <div class="col-sm-12 table-responsive">
                  <table aria-describedby="example1_info" role="grid" id="example1" class="table table-bordered table-striped dataTable">
                    <thead>
                      <tr role="row">
                        <th aria-label="Encounter Identifier (Unique)" aria-sort="ascending" style="width: 167px;" colspan="1" rowspan="1" aria-controls="example1" tabindex="0" class="sorting_asc">Encounter ID</th>
                        <th aria-label="Patient Identifier (Unique)" style="width: 207px;" colspan="1" rowspan="1" aria-controls="example1" tabindex="0" class="sorting">Patient ID</th>
                        <th aria-label="Notes" style="width: 182px;" colspan="1" rowspan="1" aria-controls="example1" tabindex="0" class="sorting">Notes</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="entry in hits" :key="entry.id" class="even" role="row">
                        <td>{{ entry.id }}</td>
                        <td>{{ entry.fields['subject.reference'] }}</td>
                        <td>{{ entry.fields['reason.text'] }}</td>
                      </tr>
                    </tbody>
                    <tfoot>
                      <tr>
                        <th colspan="1" rowspan="1">Encounter ID</th>
                        <th colspan="1" rowspan="1">Patient ID</th>
                        <th colspan="1" rowspan="1">Notes</th>
                      </tr>
                    </tfoot>
                  </table>
                </div>
              </div>
            </div>
            <!-- /.box-body -->
          </div>
        </div>
      </div>
    </div>

    <div class="row">
      <button v-on:click="map" class="btn btn-lg btn-primary" style="margin-left:16px;">Map Results</button>
    </div>
  </section>
</template>

<script>
import api from '../../api'
import Chart from 'chart.js'

export default {
  name: 'Analytics',
  data () {
    return {
      criteria: '',
      searching: '',
      hits: [],
      response: '',
      year_month: [],
      patient_count: []
    }
  },
  methods: {
    getModelAndDoc (name, route) {
      api.request('get', `/search/analytics/`)
      .then(response => {
        for (let i = 0; i < response.data.length; i++) {
          this.year_month.push(response.data[i].year_month)
          this.patient_count.push(response.data[i].patient_count)
        }
        this.analyticsChart.update()
      })
      .catch(error => {
        console.log(error)
      })
    },
    search () {
      this.toggleLoading()
      this.resetResponse()
      this.$store.commit('TOGGLE_LOADING')

      api.request('post', '/casesearch', { criteria: this.criteria })
      .then(response => {
        this.toggleLoading()

        var data = response.data

        if (data.error) {
          var errorName = data.error.name
          if (errorName) {
            this.response = errorName === 'InvalidCriteriaError'
            ? 'Invalid criteria. Please try again.'
            : errorName
          } else {
            this.response = data.error
          }

          return
        }

        if (data.hits) {
          this.hits = data.hits
        } else {
          this.hits = {}
        }
      })
      .catch(error => {
        this.$store.commit('TOGGLE_LOADING')
        console.log(error)

        this.response = 'Server appears to be offline'
        this.toggleLoading()
      })
    },
    map () {
      let ids = this.hits.map(hit => hit.fields['subject.reference'])
      this.$store.commit('SET_COHORT', ids)
      this.$router.push({ path: 'incidents' })
    },
    toggleLoading () {
      this.loading = (this.loading === '') ? 'loading' : ''
    },
    resetResponse () {
      this.response = ''
    }
  },
  mounted () {
    this.getModelAndDoc()
    let ctx = document.getElementById('analytics').getContext('2d')

    var config = {
      type: 'line',
      data: {
        labels: this.year_month,
        datasets: [{
          label: 'Patient Count',
          fill: false,
          borderColor: '#284184',
          pointBackgroundColor: '#284184',
          backgroundColor: 'rgba(0, 0, 0, 0)',
          data: this.patient_count
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

    this.analyticsChart = new Chart(ctx, config) // eslint-disable-line no-new
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
