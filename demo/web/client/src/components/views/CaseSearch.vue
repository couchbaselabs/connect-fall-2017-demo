<template>
  <!-- Main content -->
  <section class="content">
    <!-- Search field -->
    <div class="row center-block">
      <div class="col-sm-6 col-xs-12">
      <form class="ui form" @submit.prevent="search">
        <div class="input-group">
          <input class="form-control" placeholder="Case Search" type="text" v-model="criteria">
          <span class="input-group-btn input-group-addon">
            <button type="submit">
              <span class="input-group-addon">
                <i class="fa fa-search"></i>
              </span>
            </button>
          </span>
        </div>
      </form>
      </div>
    </div>
    <div class="row center-block">
      <div class="col-sm-6 col-xs-12">
      <div class="box box-warning">
        <div class="box-header with-border">
          <h3 class="box-title">Refine Results</h3>
        </div>
        <!-- /.box-header -->
        <div class="box-body">
          <form role="form">
            <!-- checkbox -->
            <div class="form-group">
              <label>Diagnosis</label>
              <div class="checkbox">
                <label>
                  <input type="checkbox">
                  Checkbox 1
                </label>
              </div>

              <div class="checkbox">
                <label>
                  <input type="checkbox">
                  Checkbox 2
                </label>
              </div>

              <div class="checkbox">
                <label>
                  <input type="checkbox" disabled="">
                  Checkbox disabled
                </label>
              </div>
            </div>
          </form>
        </div>
      <!-- /.box-body -->
      </div>
      </div>
    </div>
    <!-- Results row -->
    <div class="row center-block">
      <h2>Cases</h2>
      <div class="col-md-12">
        <div class="box">
          <div class="box-header">
            <h3 class="box-title">Case Search Results</h3>
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
                <div class="col-sm-12 table-responsive">
                  <table aria-describedby="example1_info" role="grid" id="example1" class="table table-bordered table-striped dataTable">
                    <thead>
                      <tr role="row">
                        <th aria-label="Text" aria-sort="ascending" style="width: 167px;" colspan="1" rowspan="1" aria-controls="example1" tabindex="0" class="sorting_asc">Records Text</th>
                        <th aria-label="Diagnosis" aria-sort="ascending" style="width: 167px;" colspan="1" rowspan="1" aria-controls="example1" tabindex="0" class="sorting_asc">Diagnosis</th>
                        <th aria-label="Patient" style="width: 207px;" colspan="1" rowspan="1" aria-controls="example1" tabindex="0" class="sorting">Patient</th>
                        <th aria-label="Notes" style="width: 182px;" colspan="1" rowspan="1" aria-controls="example1" tabindex="0" class="sorting">Rank</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="entry in entries" :key="entry.id" class="even" role="row">
                        <td v-html='entry.fragments["note.text"][0]'></td>
                        <td>{{ entry.fields['code.text'] }}</td>
                        <td>{{ patientName(records.get(entry)) }}</td>
                        <td>{{ entry.score }}</td>
                      </tr>
                    </tbody>
                    <tfoot>
                      <tr>
                        <th colspan="1" rowspan="1">Records Text</th>
                        <th colspan="1" rowspan="1">Diagnosis</th>
                        <th colspan="1" rowspan="1">Patient</th>
                        <th colspan="1" rowspan="1">Rank</th>
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
      <button v-on:click="map">Map Results</button>
    </div>
  </section>
</template>

<script>
import api from '../../api'

export default {
  name: 'CaseSearch',
  data () {
    return {
      criteria: '',
      searching: '',
      records: new Map(),
      entries: [],
      response: ''
    }
  },
  methods: {
    search () {
      this.toggleLoading()
      this.resetResponse()
      this.$store.commit('TOGGLE_LOADING')

      this.records.clear()

      api.request('post', '/search/diagnosis', { criteria: this.criteria })
      .then(response => {
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

        for (const hit of data.hits) {
          let idIndex = hit.fields['subject.reference'].lastIndexOf(':') + 1
          let id = hit.fields['subject.reference'].substring(idIndex)
          console.dir(hit)
          this.records.set(hit, id)
        }

        let ids = Array.from(this.records.values())
        this.$store.commit('SET_COHORT', ids)

        return api.request('post', '/patient/cohort', ids)
      })
      .then(response => {
        let idToData = new Map()

        for (const item of response.data) {
          idToData.set(item.patient.id, item.patient)
        }

        for (const key of this.records.keys()) {
          this.records.set(key, idToData.get(this.records.get(key)))
        }

        this.entries = Array.from(this.records.keys())
      })
      .catch(error => {
        console.log(error)

        this.response = 'Server appears to be offline'
      })
      .finally(() => {
        this.$store.commit('TOGGLE_LOADING')
        this.toggleLoading()
      })
    },
    map () {
      this.$router.push({ path: '/incidents' })
    },
    toggleLoading () {
      this.loading = (this.loading === '') ? 'loading' : ''
    },
    resetResponse () {
      this.response = ''
    },
    patientName (patient) {
      return `${patient.name[0].given[0]} ${patient.name[0].family[0]}`
    }
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
