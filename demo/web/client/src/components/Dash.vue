<template>
  <div :class="['wrapper', classes]">
    <header class="main-header">
      <span class="logo">
        <a href="/"><img src="/static/img/demo_logo.png" alt="Logo" height="90%" width="auto"></a>
      </span>
      <!-- Header Navbar -->
      <nav class="navbar navbar-static-top" role="navigation">
      <h1 v-if="$route.name.length > 1" class="header-title">
        {{$route.name}}
      </h1>

        <!-- Sidebar toggle button
        <a href="javascript:;" class="sidebar-toggle" data-toggle="offcanvas" role="button">
          <span class="sr-only">Toggle navigation</span>
        </a> -->
        <!-- Navbar Right Menu -->
        <div class="navbar-custom-menu" style="margin-right:32px;">
          <ul class="nav navbar-nav">
            <!-- Messages-->
            <li class="dropdown messages-menu">
              <a href="javascript:;" class="dropdown-toggle" data-toggle="dropdown">
                <i class="fa fa-2x fa-envelope-o"></i>
                <span class="label label-success">{{ userInfo.messages | count }}</span>
              </a>
              <ul class="dropdown-menu">
                <li class="header">You have {{ userInfo.messages | count }} message(s)</li>
                <li v-if="userInfo.messages.length > 0">
                  <!-- inner menu: contains the messages -->
                  <ul class="menu">
                    <li>
                      <!-- start message -->
                      <a href="javascript:;">
                        <!-- Message title and timestamp -->
                        <h4>
                          Support Team
                          <small>
                            <i class="fa fa-2x fa-clock-o"></i> 5 mins</small>
                        </h4>
                        <!-- The message -->
                        <p>Why not consider this a test message?</p>
                      </a>
                    </li>
                    <!-- end message -->
                  </ul>
                  <!-- /.menu -->
                </li>
                <li class="footer" v-if="userInfo.messages.length > 0">
                  <a href="javascript:;">See All Messages</a>
                </li>
              </ul>
            </li>
            <!-- /.messages-menu -->

            <!-- Notifications Menu -->
            <li class="dropdown notifications-menu">
              <a href="javascript:;" class="dropdown-toggle" data-toggle="dropdown">
                <i class="fa fa-2x fa-bell-o"></i>
                <span class="label label-warning">{{ userInfo.notifications | count }}</span>
              </a>
              <ul class="dropdown-menu">
                <li class="header">You have {{ userInfo.notifications | count }} notification(s)</li>
                <li v-if="userInfo.notifications.length > 0">
                  <!-- Inner Menu: contains the notifications -->
                  <ul class="menu">
                    <li>
                      <!-- start notification -->
                      <a href="javascript:;">
                        <i class="fa fa-users text-aqua"></i> 5 new members joined today
                      </a>
                    </li>
                    <!-- end notification -->
                  </ul>
                </li>
                <li class="footer" v-if="userInfo.notifications.length > 0">
                  <a href="javascript:;">View all</a>
                </li>
              </ul>
            </li>

            <!-- Tasks Menu -->
            <li class="dropdown tasks-menu">
              <a href="javascript:;" class="dropdown-toggle" data-toggle="dropdown">
                <i class="fa fa-2x fa-flag-o"></i>
                <span class="label label-danger">{{ userInfo.tasks | count }} </span>
              </a>
              <ul class="dropdown-menu">
                <li class="header">You have {{ userInfo.tasks | count }} task(s)</li>
                <li v-if="userInfo.tasks.length > 0">
                  <!-- Inner menu: contains the tasks -->
                  <ul class="menu">
                    <li>
                      <!-- Task item -->
                      <a href="javascript:;">
                        <!-- Task title and progress text -->
                        <h3>
                          Design some buttons
                          <small class="pull-right">20%</small>
                        </h3>
                        <!-- The progress bar -->
                        <div class="progress xs">
                          <!-- Change the css width attribute to simulate progress -->
                          <div class="progress-bar progress-bar-aqua" style="width: 20%" role="progressbar" aria-valuenow="20" aria-valuemin="0" aria-valuemax="100">
                            <span class="sr-only">20% Complete</span>
                          </div>
                        </div>
                      </a>
                    </li>
                    <!-- end task item -->
                  </ul>
                </li>
                <li class="footer" v-if="userInfo.tasks.length > 0">
                  <a href="javascript:;">View all tasks</a>
                </li>
              </ul>
            </li>

          </ul>
        </div>
      </nav>
    </header>
    <!-- Left side column. contains the logo and sidebar -->
    <sidebar :display-name="demo.displayName" />

    <!-- Content Wrapper. Contains page content -->
    <div class="content-wrapper">
      <!-- Content Header (Page header) -->


      <router-view></router-view>
    </div>
    <!-- /.content-wrapper -->

    <!-- Main Footer -->
    <footer class="main-footer">
      <strong>Copyright &copy; {{year}}
        <a href="javascript:;">CoPilot</a>.</strong> All rights reserved.
    </footer>
  </div>
  <!-- ./wrapper -->
</template>

<script>
import faker from 'faker'
import { mapState } from 'vuex'
import config from '../config'
import Sidebar from './Sidebar'
import 'hideseek'

export default {
  name: 'Dash',
  components: {
    Sidebar
  },
  data: function () {
    return {
      // section: 'Dash',
      year: new Date().getFullYear(),
      classes: {
        fixed_layout: config.fixedLayout,
        hide_logo: config.hideLogoOnMobile
      },
      error: ''
    }
  },
  computed: {
    ...mapState([
      'userInfo'
    ]),
    demo () {
      return {
        displayName: faker.name.findName(),
        avatar: faker.image.avatar(),
        email: faker.internet.email(),
        randomCard: faker.helpers.createCard()
      }
    }
  },
  methods: {
    changeloading () {
      this.$store.commit('TOGGLE_SEARCHING')
    }
  }
}
</script>

<style lang="scss">

body {
font-size: 16px!important;
}

.content {
padding: 32px!important;
}

.main-header>.navbar {
  margin-left: 230px;
  height: 68px!important;
}

.skin-blue .main-header .logo {
background-color: #3575C6!important;
height: 68px!important;
padding: 8px!important;
margin: 0!important;
width: 230px;
text-align: left;
}

.skin-blue .main-header .logo img {
padding: 0;
margin: 0;

}
.skin-blue .main-header .navbar {
background-color: #3575C6!important;
}

h1.header-title {
line-height: 2.9!important;
margin: 0 0 0 32px!important;
padding: 0!important;
color: #fff!important;
display:inline!important;
font-weight: 400;
font-style: italic;
font-size: 24px
}

.input-group-addon {
  padding: 3px 12px;
}

.form-control {
  font-size: 16px;
  color: #555;
}

.btn.btn-primary {
  background-color: #3575C6!important;
}

.mark, mark {
  background-color: #fffaa8;
}

.main-header .navbar .nav>li>a>.label {
font-size: 15px!important;
}

.wrapper.fixed_layout {
  .main-header {
    position: fixed;
    width: 100%;
  }

  .content-wrapper {
    padding-top: 50px;
  }

  .main-sidebar {
    position: fixed;
    height: 100vh;
  }
}

.wrapper.hide_logo {
  @media (max-width: 767px) {
    .main-header .logo {
      display: none;
    }
  }
}

.logo-mini,
.logo-lg {
  text-align: left;

  img {
    padding: .4em !important;
  }
}

.logo-lg {
  img {
    display: -webkit-inline-box;
    width: 25%;
  }
}

.user-panel {
  height: 4em;
}

hr.visible-xs-block {
  width: 100%;
  background-color: rgba(0, 0, 0, 0.17);
  height: 1px;
  border-color: transparent;
}
</style>
