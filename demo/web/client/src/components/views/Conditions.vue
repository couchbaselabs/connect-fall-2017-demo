<template>
  <table style="width:100%" aria-describedby="Conditions" role="grid" id="Conditions" class="table table-bordered table-striped dataTable">
    <thead>
      <tr>
        <th>Date</th><th>Condition</th><th>Status</th><th>Notes</th>
          <template v-for="(value, key) in extensions">
            <th :key="key">{{ toCapitalized(key) }}</th>
        </template>
      </tr>
    </thead>
    <tbody>
      <template v-for="(record, index) in conditions">
        <tr :key="index">
          <td>{{ record.date.toDateString() }}</td>
          <td>{{ record.code.text }}</td>
          <td>{{ toCapitalized(record.clinicalStatus) }}</td>
          <td>
            <div v-for="(note, index) in record.note" :key="index">
              {{ note.text }}
            </div>
          </td>
          <template v-for="(value, key) in extensions">
            <td v-if="record.extension && record.extension[key]">{{ record.extension[key] }}</td>
            <td v-else></td>
          </template>
        </tr>
      </template>
    </tbody>
  </table>
</template>

<script>
import stringUtils from '../../bin/string_utils'

export default {
  'name': 'Conditions',
  props: [ 'conditions' ],
  computed: {
    extensions () {
      let extensions = {}

      this.conditions.map(record => {
        if (!record.extension) return

        Object.entries(record.extension)
        .map(entry => {
          if (!extensions[entry[0]]) extensions[entry[0]] = true
        })
      })

      return extensions
    }
  },
  methods: {
    toCapitalized (string) {
      return stringUtils.toCapitalized(string)
    }
  }
}
</script>

<style>
</style>
