<template>
  <div :class="classObj" class="app-wrapper">
    <div v-if="device === 'mobile' && sidebar.opened" class="drawer-bg" @click="handleClickOutside" />
    <sidebar class="sidebar-container" />
    <div class="main-container">
      <div :class="{ 'fixed-header': fixedHeader }">
        <navbar />
      </div>
      <app-main />
    </div>
  </div>
</template>

<script>
import { Navbar, Sidebar, AppMain } from './components'
import ResizeMixin from './mixin/ResizeHandler'

export default {
  name: 'Layout',
  components: {
    Navbar,
    Sidebar,
    AppMain
  },
  mixins: [ResizeMixin],
  computed: {
    sidebar() {
      return this.$store.state.app.sidebar
    },
    device() {
      return this.$store.state.app.device
    },
    fixedHeader() {
      return this.$store.state.settings.fixedHeader
    },
    classObj() {
      return {
        hideSidebar: !this.sidebar.opened,
        openSidebar: this.sidebar.opened,
        withoutAnimation: this.sidebar.withoutAnimation,
        mobile: this.device === 'mobile'
      }
    }
  },
  created() {
    const roleKey = localStorage.getItem('roles')
    const userInfo = JSON.parse(localStorage.getItem('user') || '{}')
    const adminLevel = userInfo.adminLevel || 0

    const menuList = this.$router.options.routes

    const filterByRole = (routeList) => {
      routeList.forEach((element) => {
        if (element.meta && element.meta.roles) {
          let isVisible = false
          element.meta.roles.forEach((role) => {
            if (role === roleKey) {
              if (element.path === '/ai-config') {
                if (adminLevel === 1) {
                  isVisible = true
                  return
                }
              } else {
                isVisible = true
                return
              }
            }
          })
          element.meta.visible = isVisible
        }
        if (element.children && element.children.length > 0) {
            element.children.forEach((child) => {
              if (child.meta && element.meta && element.meta.visible === false) {
                child.meta.visible = false
              }
            })
            filterByRole(element.children)
          }
      })
    }

    filterByRole(menuList)
  },
  methods: {
    handleClickOutside() {
      this.$store.dispatch('app/closeSideBar', { withoutAnimation: false })
    }
  }
}
</script>

<style lang="scss" scoped>
@use "~@/styles/mixin.scss" as *;
@use "~@/styles/variables.scss" as *;

.app-wrapper {
  @include clearfix;
  position: relative;
  height: 100%;
  width: 100%;
  background: var(--bg-primary);
  transition: background 0.3s ease;

  &.mobile.openSidebar {
    position: fixed;
    top: 0;
  }
}

.drawer-bg {
  background: rgba(0, 0, 0, 0.4);
  width: 100%;
  top: 0;
  height: 100%;
  position: absolute;
  z-index: 999;
}

.fixed-header {
  position: fixed;
  top: 0;
  right: 0;
  z-index: 9;
  width: calc(100% - #{$sideBarWidth});
  transition: width 0.28s;
}

.hideSidebar .fixed-header {
  width: calc(100% - 54px);
}

.mobile .fixed-header {
  width: 100%;
}
</style>