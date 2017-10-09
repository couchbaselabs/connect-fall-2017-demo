<template>
  <aside class="main-sidebar">

    <!-- sidebar: style can be found in sidebar.less -->
    <section class="sidebar">

      <!-- Sidebar user panel (optional)
      <div class="user-panel">
        <div class="pull-left image">
          <img :src="pictureUrl" />
        </div>
        <div class="pull-left info">
          <div>
            <p class="white">{{ displayName }}</p>
          </div>
          <a href="javascript:;">
            <i class="fa fa-circle text-success"></i> Online
          </a>
        </div>
      </div>  -->

        <div class="input-group">
        &nbsp;<br>&nbsp;<br>&nbsp;<br>&nbsp;<br>
        </div>

      <!-- Sidebar Menu -->
      <sidebar-menu />
      <!-- /.sidebar-menu -->
    </section>
    <!-- /.sidebar -->
  </aside>
</template>
<script>
import SidebarMenu from './SidebarMenu'

export default {
  name: 'Sidebar',
  props: ['displayName', 'pictureUrl'],
  components: { SidebarMenu }
}
</script>
<style>
  .user-panel .image img {
    display: none;
  }
</style>
