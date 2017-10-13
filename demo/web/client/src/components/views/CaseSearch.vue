<template>
  <!-- Main content -->
  <section class="content">
    <!-- Search field -->
    <div class="row">
      <div class="col-sm-6 col-xs-12">
      <form class="ui form" @submit.prevent="search">
        <div class="input-group">
          <input class="form-control" placeholder="search cases..." type="text" v-model="criteria">
          <span class="input-group-btn input-group-addon">
            <button type="submit" style="border-width:0;background-color:#fff;outline:none;">
              <span class="input-group-addon" style="border-width:0;">
                <i class="fa fa-lg fa-search"></i>
              </span>
            </button>
          </span>
        </div>
      </form>
      </div>
    </div>

    <!-- Everything row -->
    <div class="row">
    <!-- Results column -->
      <div class="col-md-8" style="max-width: 1250px">
        <div class="box" style="margin-top: 16px;border-color: #3575C6;">
          <div class="box-header">
            <div class="pull-right">
              <button v-on:click="map" class="btn btn-primary" style="margin-right:16px;">Map Results</button>
            </div>
            <h3 class="box-title">{{ total }} Results ( {{ took }} )</h3>
          </div>
          <!-- /.box-header -->
          <div class="box-body">
            <div v-for="entry in entries" :key="entry.id" style="margin-bottom:16px;">
              <div v-if="entry.fragments['note.text'][0]" v-html='entry.fragments["note.text"][0]' style="font-weight:600;"></div>
              <span>search rank: <em>{{ entry.score }}</em></span>
              &nbsp;|&nbsp;
              <span>diagnosis: <em>{{ entry.fields['code.text'] }}</em></span>
              &nbsp;|&nbsp;
              <span>patient: <em><a href="#">{{ patientName(records.get(entry)) }}</a></em></span>
            </div>
          </div>
        </div>
      </div>
      <div class="col-md-4">
      <div class="box" style="margin-top: 16px;">
        <div class="box-header with-border">
          <h3 class="box-title">Refine Results</h3>
        </div>
        <!-- /.box-header -->
        <div class="box-body">
          <form role="form">
            <!-- checkbox -->
            <div class="form-group">
              <label v-if="">Diagnosis</label>
              <div class="checkbox" v-for="diagnosis in diagnosisFacets" role="row">
                <label>
                  <input :value="diagnosis.term" v-model="checkedDiagnosis" type="checkbox" v-on:change="filterDiagnosis(diagnosis.term)">
                  {{ diagnosis.term }} ( {{ diagnosis.count }} )
                </label>
              </div>

              <label v-if="">Onset</label>
              <div class="checkbox" v-for="onset in onsetFacets" role="row">
                <label>
                  <input :value="onset.name" v-model="checkedOnset" type="checkbox" v-on:change="filterOnset(onset.name)">
                  {{ onset.name }} ( {{ onset.count }} )
                </label>
              </div>
            </div>
          </form>
        </div>
      <!-- /.box-body -->
      </div>
      </div>
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
      diagnosisFacets: [],
      checkedDiagnosis: [],
      onsetFacets: [],
      checkedOnset: [],
      filterOnlyDiagnosis: '',
      filterOnlyOnset: '',
      total: 0,
      took: 0,
      entries: [],
      response: ''
    }
  },
  methods: {
    filterDiagnosis (diagnosis) {
      if (this.filterOnlyDiagnosis) {
        this.filterOnlyDiagnosis = ''
      } else {
        this.filterOnlyDiagnosis = diagnosis
      }
      this.search()
    },
    filterOnset (onset) {
      if (this.filterOnlyOnset) {
        this.filterOnlyOnset = ''
      } else {
        this.filterOnlyOnset = onset
      }
      this.search()
    },
    roundTook (took) {
      if (took < 1000 * 1000) {
        return '<1ms'
      } else if (took < 1000 * 1000 * 1000) {
        return '' + Math.round(took / (1000 * 1000)) + 'ms'
      } else {
        let roundMs = Math.round(took / (1000 * 1000))
        return '' + roundMs / 1000 + 's'
      }
    },
    search () {
      this.toggleLoading()
      this.resetResponse()
      this.$store.commit('TOGGLE_LOADING')

      this.records.clear()

      api.request('post', '/search/diagnosis', { criteria: this.criteria, filterDiagnosis: this.filterOnlyDiagnosis, filterOnset: this.filterOnlyOnset })
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

          this.records.set(hit, id)
        }
        if (data.meta.facets && data.meta.facets.diagnosis) {
          this.diagnosisFacets = data.meta.facets.diagnosis.terms
        }
        if (data.meta.facets && data.meta.facets.onset) {
          this.onsetFacets = data.meta.facets.onset.date_ranges
        }
        this.total = data.meta.totalHits
        this.took = this.roundTook(data.meta.took)

        let ids = Array.from(this.records.values())
        this.$store.commit('SET_COHORT', ids)

        return api.request('post', '/records/patient/cohort', ids)
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
      if (!patient) return '(missing)'

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
