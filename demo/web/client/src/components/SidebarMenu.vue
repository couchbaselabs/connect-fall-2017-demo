<template>
  <ul class="sidebar-menu">
    <li class="header">Patient Center</li>
    <li class="active pageLink" v-on:click="toggleMenu">
      <router-link to="/"><i class="fa fa-lg fa-desktop"></i>
        <span class="page">Clinical Dashboard</span>
      </router-link>
    </li>
    <li class="pageLink" v-on:click="toggleMenu">
      <router-link to="/patient"><i class="fa fa-lg fa-table"></i>
        <span class="page">Patient Detail</span>
      </router-link>
    </li>

    <li class="header">Data-Mining Tools</li>
    <li class="pageLink" v-on:click="toggleMenu">
      <router-link to="/records/analytics"><i class="fa fa-lg fa-line-chart"></i>
        <span class="page">Data Analysis</span>
      </router-link>
    </li>
    <li class="pageLink" v-on:click="toggleMenu">
      <router-link to="/records/search"><i class="fa fa-lg fa-search"></i>
        <span class="page">Case Search</span>
      </router-link>
    </li>
    <li class="pageLink" v-on:click="toggleMenu">
      <router-link to="/incidents"><i class="fa fa-lg fa-map-marker"></i>
        <span class="page">Geo-Mapping</span>
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
      event.currentTarget.className = 'pageLink active'
    }
  }
}
</script>
<style>
  /* override default */
  .sidebar-menu>li>a {
    padding: 15px;
  }
 span.page {padding-left: 8px;}
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

.sidebar-menu>li.header {
font-size:18px;
font-weight:600;
color: #ccc!important;
}

</style>
