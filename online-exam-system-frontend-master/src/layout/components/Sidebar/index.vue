<template>
  <div :class="{ 'has-logo': showLogo }" class="sidebar-container">
    <logo v-if="showLogo" :collapse="isCollapse" />
    <el-scrollbar wrap-class="scrollbar-wrapper">
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        background-color="transparent"
        :text-color="menuTextColor"
        :unique-opened="false"
        :active-text-color="menuActiveTextColor"
        :collapse-transition="false"
        mode="vertical"
        class="sidebar-menu"
      >
        <sidebar-item
          v-for="(route, index) in routes"
          :key="index"
          :item="route"
          :base-path="route.path"
        />
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import Logo from './Logo'
import SidebarItem from './SidebarItem'

export default {
  components: { SidebarItem, Logo },

  computed: {
    ...mapGetters(['sidebar']),
    routes() {
      const menuList = this.$router.options.routes

      const roleKey = localStorage.getItem('roles')
      const userInfo = JSON.parse(localStorage.getItem('user') || '{}')
      const adminLevel = userInfo.adminLevel || 0

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
      return menuList
    },
    activeMenu() {
      const route = this.$route
      const { meta, path } = route
      if (meta.activeMenu) {
        return meta.activeMenu
      }
      return path
    },
    showLogo() {
      return this.$store.state.settings.sidebarLogo
    },
    isCollapse() {
      return !this.sidebar.opened
    },
    menuTextColor() {
      return this.$store.state.theme.mode === 'dark' ? '#94A3B8' : '#64748B'
    },
    menuActiveTextColor() {
      return this.$store.state.theme.mode === 'dark' ? '#F8FAFC' : '#6366F1'
    }
  }
}
</script>

<style lang="scss" scoped>
.sidebar-container {
  transition: width 0.28s;
  background: var(--sidebar-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border-right: 1px solid var(--border-primary);
  height: 100%;
  position: fixed;
  font-size: 0;
  top: 0;
  bottom: 0;
  left: 0;
  z-index: 1001;
  overflow: hidden;

  ::v-deep .el-scrollbar__bar.is-vertical {
    right: 0;
  }

  ::v-deep .el-scrollbar {
    height: 100%;
  }

  .sidebar-menu {
    border: none;
    height: 100%;
    width: 100% !important;
    background: transparent;
  }
}
</style>