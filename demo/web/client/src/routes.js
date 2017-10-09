import DashView from './components/Dash.vue'
import LoginView from './components/Login.vue'
import NotFoundView from './components/404.vue'

// Import Views - Dash
import DashboardView from './components/views/Dashboard.vue'
import PatientDetailView from './components/views/PatientDetail'
import AnalyticsView from './components/views/Analytics.vue'
import CaseSearchView from './components/views/CaseSearch.vue'
import IncidentsView from './components/views/Incidents.vue'

// Routes
const routes = [
  {
    path: '/login',
    component: LoginView
  },
  {
    path: '/',
    component: DashView,
    children: [
      {
        path: 'dashboard',
        alias: '',
        component: DashboardView,
        name: 'Clinical Dashboard',
        meta: {description: 'Overview of environment'}
      }, {
        path: 'patient',
        component: PatientDetailView,
        name: 'Patient Detail',
        meta: {description: 'Detailed information for a single patient'}
      }, {
        path: 'records/analytics',
        component: AnalyticsView,
        name: 'Trend Analysis',
        meta: {description: ''}
      }, {
        path: 'records/search',
        component: CaseSearchView,
        name: 'Case Search',
        meta: {description: 'Records Search'}
      }, {
        path: 'incidents',
        component: IncidentsView,
        name: 'Geo-Mapping',
        meta: {description: ' '}
      }
    ]
  }, {
    // not found handler
    path: '*',
    component: NotFoundView
  }
]

export default routes
