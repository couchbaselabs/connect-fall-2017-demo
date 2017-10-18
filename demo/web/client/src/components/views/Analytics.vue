<template>
  <!-- Main content -->
  <section class="content">
    <!-- Search field -->
    <div class="row">
      <div class="dropdown">
        <div>Diagnosis:</div>
        <button class="btn btn-primary dropdown-toggle" type="button" data-toggle="dropdown">
          {{ diagnosis }}
          <span class="caret"></span>
        </button>
        <ul class="dropdown-menu" v-on:click.prevent="diagnosis = $event.target.innerText">
          <li v-for="entry in diagnoses" :key="entry"><a href="#">{{ entry }}</a></li>
        </ul>
      </div>
      <div class="dropdown">
        <div>City:</div>
        <button class="btn btn-primary dropdown-toggle" type="button" data-toggle="dropdown">
          {{ city }}
          <span class="caret"></span>
        </button>
        <ul class="dropdown-menu" v-on:click.prevent="city = $event.target.innerText">
          <li v-for="entry in cityList" :key="entry"><a href="#">{{ entry }}</a></li>
        </ul>
      </div>
      <div class="dropdown">
        <div>Gender:</div>
        <button class="btn btn-primary dropdown-toggle" type="button" data-toggle="dropdown">
          {{ gender }}
          <span class="caret"></span>
        </button>
        <ul class="dropdown-menu" v-on:click.prevent="gender = $event.target.innerText">
          <li v-for="entry in genderList" :key="entry"><a href="#">{{ entry }}</a></li>
        </ul>
      </div>
      <div class="dropdown">
        <div>Group By:</div>
        <button class="btn btn-primary dropdown-toggle" type="button" data-toggle="dropdown">
          {{ grouping }}
          <span class="caret"></span>
        </button>
        <ul class="dropdown-menu" v-on:click.prevent="grouping = $event.target.innerText">
          <li v-for="entry in groupList" :key="entry"><a href="#">{{ entry }}</a></li>
        </ul>
      </div>
      <!--- <input type="text" v-model="age[0]" required>
      <input type="text" v-model="age[1]" required> -->
      <!-- <input id="age_slider" type="range" v-model="age" data-provide="slider" data-slider-min="0" data-slider-max="110" data-slider-step="1" data-slider-value="[0,110]"/>{{ age }} -->
      <!-- <input id="age_slider" type="range" data-provide="slider" data-slider-min="0" data-slider-max="110" data-slider-step="1" v-bind:data-slider-value="age"/>{{ age }} -->
      <button v-on:click.prevent="searchRouter" type="button" style="border-width:0;background-color:#fff;outline:none;">
        <span class="input-group-addon" style="border-width:0;">
          <i class="fa fa-lg fa-search"></i>
        </span>
      </button>
    </div>
    <!-- /.row -->

    <!-- Results row -->
    <div class="row" style="margin-top:32px;">
      <div class="col-md-12">
        <div class="box">
          <div class="box-header">
            <h3 class="box-title">Results</h3>
          </div>
          <!-- /.box-header -->
          <div class="box-body">
            <canvas id="analytics"></canvas>

            <div class="dataTables_wrapper form-inline dt-bootstrap" id="example1_wrapper" style="margin-top:24px;">
              <div class="row">
                <div class="col-sm-12 table-responsive">
                  <table aria-describedby="example1_info" role="grid" id="example1" class="table table-bordered table-striped dataTable">
                    <thead>
                      <tr role="row">
                        <th aria-label="Patient ID" aria-sort="ascending" style="width: 167px;" colspan="1" rowspan="1" aria-controls="example1" tabindex="0" class="sorting_asc">Patient ID</th>
                        <th aria-label="Patient Name" style="width: 207px;" colspan="1" rowspan="1" aria-controls="example1" tabindex="0" class="sorting">Patient Name</th>
                        <th aria-label="Patient Age" style="width: 182px;" colspan="1" rowspan="1" aria-controls="example1" tabindex="0" class="sorting">Patient Age</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="entry in search_details" :key="entry.id" class="even" role="row">
                        <td><a href="#">{{ entry.p_id }}</a></td>
                        <td>{{ entry.p_name[0].given[0] }} {{ entry.p_name[0].family }}</td>
                        <td>{{ entry.p_age }}</td>
                      </tr>
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
</template>

<script>
import api from '../../api'
import Chart from 'chart.js'

export default {
  name: 'Analytics',
  data () {
    return {
      age: [ '0', '100' ],
      diagnosis: 'Select Diagnosis',
      diagnoses: [ 'Scaleitis', 'Mongoitis', 'Diabetes' ],
      city: 'All',
      cityList: [ 'All', 'Boston', 'Worchester', 'Springfield', 'Cambridge', 'Taunton' ],
      gender: 'All',
      genderList: [ 'All', 'Male', 'Female', 'Other' ],
      grouping: 'Age',
      groupList: [ 'Age', 'Social Media' ],
      searching: '',
      hits: [],
      response: '',
      search_details: [],
      chart_data: {
        labels: []
      }
    }
  },
  methods: {
    searchRouter () {
      if (this.grouping === this.groupList[0]) {

      } else if (this.grouping === this.groupList[1]) {
        this.searchSocial(null, null)
      }
    },
    search (name, route) {
      this.analyticsChart.data.labels = []
      this.analyticsChart.data.datasets = []
      api.request('get', `/search/analytics/?code=${this.diagnosis}&gender=${this.gender}&min_age=${this.age[0]}&max_age=${this.age[1]}`)
      .then(response => {
        this.chart_data = response.data
        this.analyticsChart.data.labels = response.data.labels
        for (let i = 0; i < response.data.datasets.length; i++) {
          this.analyticsChart.data.datasets.push(response.data.datasets[i])
        }
        this.analyticsChart.update()
      })
      .catch(error => {
        console.log(error)
      })
    },
    searchDetails (yearMonth) {
      api.request('get', `/search/analytics-details/?diagnosis=${this.diagnosis}&gender=${this.gender}&min_age=${this.age[0]}&max_age=${this.age[1]}&year_month=${yearMonth}`)
      .then(response => {
        this.search_details = response.data
      })
      .catch(error => {
        console.log(error)
      })
    },
    searchSocial (name, route) {
      this.analyticsChart.data.labels = []
      this.analyticsChart.data.datasets = []

      api.request('get', `/search/analytics/social?diagnosis=${this.diagnosis}&gender=${this.gender}&city=${this.city}`)
      .then(response => {
        this.chart_data = response.data
        this.analyticsChart.data.labels = response.data.labels

        for (let set of response.data.datasets) {
          this.analyticsChart.data.datasets.push(set)
        }

        this.analyticsChart.update()
      })
      .catch(error => {
        console.log(error)
      })
    },
    toggleLoading () {
      this.loading = (this.loading === '') ? 'loading' : ''
    },
    resetResponse () {
      this.response = ''
    }
  },
  mounted () {
    let ctx = document.getElementById('analytics').getContext('2d')

    var config = {
      type: 'line',
      data: {
        labels: [],
        datasets: []
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
        },
        onClick: (event) => {
          var item = this.analyticsChart.getElementAtEvent(event)[0]
          if (item) {
            var label = this.analyticsChart.data.labels[item._index]
            // var value = this.analyticsChart.data.datasets[item._datasetIndex].data[item._index]
          }
          this.searchDetails(label)
        }
      }
    }

    this.analyticsChart = new Chart(ctx, config) // eslint-disable-line no-new
  }
}

</script>
<style>
.checkbox-inline, .radio-inline {
  vertical-align:inherit!important;
  top: -2px!important;
}
.info-box {
  cursor: pointer;
}
.info-box-content {
  text-align: center;
  vertical-align: middle;
  display: inherit;
}
canvas#analytics {

  height: 460px!important;
  max-width: 980px!important;
}
</style>
