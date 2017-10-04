<template>
  <ul class="sidebar-menu">
    <li class="header">Monitors</li>
    <li class="active pageLink" v-on:click="toggleMenu">
      <router-link to="/"><i class="fa fa-desktop"></i>
        <span class="page">Dashboard</span>
      </router-link>
    </li>
    <li class="pageLink" v-on:click="toggleMenu">
      <router-link to="/patient"><i class="fa fa-table"></i>
        <span class="page">Patient Detail</span>
      </router-link>
    </li>

    <li class="header">Tools</li>
    <li class="pageLink" v-on:click="toggleMenu">
      <router-link to="/analytics"><i class="fa fa-book"></i>
        <span class="page">Analytics</span>
      </router-link>
    </li>
    <li class="pageLink" v-on:click="toggleMenu">
      <router-link to="/casesearch"><i class="fa fa-book"></i>
        <span class="page">Case Search</span>
      </router-link>
    </li>
    <li class="pageLink" v-on:click="toggleMenu">
      <router-link to="/incidents"><i class="fa fa-book"></i>
        <span class="page">Incidents</span>
      </router-link>
    </li>

    <li class="header">Placeholders</li>
    <li class="pageLink" v-on:click="toggleMenu">
      <router-link to="/login">
        <i class="fa fa-circle-o text-yellow"></i>
        <span class="page"> Login</span>
      </router-link>
    </li>
    <li class="pageLink" v-on:click="toggleMenu">
      <router-link to="/404"><i class="fa fa-circle-o text-red"></i>
        <span class="page"> 404</span>
      </router-link>
    </li>
  </ul>
</template>
<script>
export default {
  name: 'SidebarName',
  methods: {
    toggleMenu (event) {
      // remove active from li
      var active = document.querySelector('li.pageLink.active')

      if (active) {
        active.classList.remove('active')
      }
      // window.$('li.pageLink.active').removeClass('active')
      // Add it to the item that was clicked
      event.toElement.parentElement.className = 'pageLink active'
    }
  }
}
</script>
<style>
  /* override default */
  .sidebar-menu>li>a {
    padding: 12px 15px 12px 15px;
  }

  .sidebar-menu li.active>a>.fa-angle-left, .sidebar-menu li.active>a>.pull-right-container>.fa-angle-left {
    animation-name: rotate;
    animation-duration: .2s;
    animation-fill-mode: forwards;
  }

  @keyframes rotate {
    0% {
      transform: rotate(0deg);
    }

    100% {
      transform: rotate(-90deg);
    }
  }
</style>
