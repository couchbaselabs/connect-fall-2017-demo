<template>
  <aside class="main-sidebar">

    <!-- sidebar: style can be found in sidebar.less -->
    <section class="sidebar">

        <div class="input-group">
        &nbsp;<br>&nbsp;<br>&nbsp;<br>
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
  components: { SidebarMenu }
}
</script>
