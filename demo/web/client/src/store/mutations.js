export default {
  TOGGLE_LOADING (state) {
    state.callingAPI = !state.callingAPI
  },
  TOGGLE_SEARCHING (state) {
    state.searching = (state.searching === '') ? 'loading' : ''
  },
  SET_USER (state, user) {
    state.user = user
  },
  SET_TOKEN (state, token) {
    state.token = token
  },
  SET_COHORT (state, cohort) {
    state.cohort = cohort
  },
  SET_ALERT (state, sample) {
    state.userInfo.alerts = sample
  }
}
