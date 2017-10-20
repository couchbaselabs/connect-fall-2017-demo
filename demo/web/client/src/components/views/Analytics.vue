<template>
  <!-- Main content -->
  <section class="content">
    <!-- Search field -->
    <div style="display: flex; align-items: center;justify-content: flex-start;">
      <div class="dropdown">
        <button class="btn dropdown-toggle" type="button" data-toggle="dropdown">
          {{ diagnosis }}
          <span class="caret"></span>
        </button>
        <ul class="dropdown-menu" v-on:click.prevent="diagnosis = $event.target.innerText">
          <li v-for="entry in diagnoses" :key="entry"><a href="#">{{ entry }}</a></li>
        </ul>
      </div>
      <div class="dropdown">
        <button class="btn dropdown-toggle" type="button" data-toggle="dropdown">
          {{ city }}
          <span class="caret"></span>
        </button>
        <ul class="dropdown-menu" v-on:click.prevent="city = $event.target.innerText">
          <li v-for="entry in cityList" :key="entry"><a href="#">{{ entry }}</a></li>
        </ul>
      </div>
      <div class="dropdown">
        <button class="btn dropdown-toggle" type="button" data-toggle="dropdown">
          {{ gender }}
          <span class="caret"></span>
        </button>
        <ul class="dropdown-menu" v-on:click.prevent="gender = $event.target.innerText">
          <li v-for="entry in genderList" :key="entry"><a href="#">{{ entry }}</a></li>
        </ul>
      </div>
      <div class="dropdown">
        <button class="btn dropdown-toggle" type="button" data-toggle="dropdown">
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

      <button v-on:click.prevent="searchRouter" type="button" class="btn-primary" style="border-width:0;outline:none;color:#fff; width:180px;padding:8px 0;">
        Find Patients
        <span style="color:#fff">
          <i class="fa fa-lg fa-search" style="margin-left:4px;"></i>
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
                        <th aria-label="Patient Name" style="width: 207px;" colspan="1" rowspan="1" aria-controls="example1" tabindex="0" class="sorting">Patient Name</th>
                        <th aria-label="Patient Gender" style="width: 167px;" colspan="1" rowspan="1" tabindex="0">Patient Gender</th>
                        <th aria-label="Age at Onset" style="width: 167px;" colspan="1" rowspan="1" tabindex="0">Age at Onset</th>
                        <th aria-label="Current Age" style="width: 182px;" colspan="1" rowspan="1" tabindex="0">Current Age</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="entry in search_details" :key="entry.p_id" class="even" role="row">
                        <td><a v-bind:href="'/patient/' + entry.p_id">{{ entry.p_name[0].given[0] }} {{ entry.p_name[0].family }}</a></td>
                        <td>{{ toCapitalized(entry.p_gender) }}</td>
                        <td>{{ entry.c_age }}</td>
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
import stringUtils from '../../bin/string_utils'

export default {
  name: 'Analytics',
  data () {
    return {
      age: [ '0', '100' ],
      diagnosis: 'Select Diagnosis',
      diagnoses: [ 'Scaleitis', 'Mongoitis', 'Diabetes' ],
      city: 'All Cities',
      cityList: [ 'All Cities', 'Boston', 'Worchester', 'Springfield', 'Cambridge', 'Taunton' ],
      gender: 'All Genders',
      genderList: [ 'All Genders', 'Male', 'Female', 'Other' ],
      grouping: 'Group by Age',
      groupList: [ 'Group by Age', 'By Social Media' ],
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
      // These must match terms expected in server/controllers/searchController.js
      this.search_details = []

      if (this.grouping === this.groupList[0]) {
        this.searchByAge()
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
    searchByAge () {
      this.analyticsChart.data.labels = []
      this.analyticsChart.data.datasets = []

      api.request('get', `/search/analytics/age?city=${this.city}&diagnosis=${this.diagnosis}&gender=${this.gender}`)
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
    searchByAgeDetails (yearMonth, ageGroup) {
      api.request('get', `/search/analytics/age/details?age_group=${ageGroup}&city=${this.city}&diagnosis=${this.diagnosis}&gender=${this.gender}&year_month=${yearMonth}`)
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

      api.request('get', `/search/analytics/social?city=${this.city}&diagnosis=${this.diagnosis}&gender=${this.gender}`)
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
    searchSocialDetails (yearMonth, media) {
      api.request('get', `/search/analytics/social/details?city=${this.city}&diagnosis=${this.diagnosis}&gender=${this.gender}&media=${media}&year_month=${yearMonth}`)
      .then(response => {
        this.search_details = response.data
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
    },
    toCapitalized (string) {
      return stringUtils.toCapitalized(string)
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

          if (!item) return

          var label = this.analyticsChart.data.labels[item._index]
          // var value = this.analyticsChart.data.datasets[item._datasetIndex].data[item._index]

          if (this.grouping === this.groupList[0]) {
            this.searchByAgeDetails(label, item._datasetIndex)
          } else if (this.grouping === this.groupList[1]) {
            let media = item._chart.controller.legend.legendItems[item._datasetIndex].text
            this.searchSocialDetails(label, media.toLowerCase())
          }
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

.dropdown {
  margin-right: 4px;
}
.btn.dropdown-toggle {
  width: 140px;
}
</style>
