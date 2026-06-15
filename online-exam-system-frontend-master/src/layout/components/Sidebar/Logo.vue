<template>
  <div class="sidebar-logo-container" :class="{'collapse':collapse}">
    <transition name="sidebarLogoFade">
      <router-link v-if="collapse" key="collapse" class="sidebar-logo-link" to="/">
        <h1 class="sidebar-title">{{ shortTitle }} </h1>
      </router-link>
      <router-link v-else key="expand" class="sidebar-logo-link" to="/">
        <h1 class="sidebar-title">{{ title }} </h1>
      </router-link>
    </transition>
  </div>
</template>

<script>
export default {
  name: 'SidebarLogo',
  props: {
    collapse: {
      type: Boolean,
      required: true
    }
  },
  data() {
    return {
      title: 'AI 学习平台',
      shortTitle: 'AI'
    }
  }
}
</script>

<style lang="scss" scoped>
.sidebarLogoFade-enter-active {
  transition: opacity 1.5s;
}

.sidebarLogoFade-enter,
.sidebarLogoFade-leave-to {
  opacity: 0;
}

.sidebar-logo-container {
  position: relative;
  width: 100%;
  height: 66px;
  line-height: 66px;
  text-align: center;
  overflow: hidden;
  border-bottom: 1px solid var(--border-primary);
  background: var(--bg-active);

  & .sidebar-logo-link {
    height: 100%;
    width: 100%;
    text-decoration: none;

    & .sidebar-title {
      display: inline-block;
      margin: 0;
      color: var(--accent-light);
      font-weight: 700;
      line-height: 66px;
      font-size: 16px;
      letter-spacing: 2px;
      vertical-align: middle;
    }
  }

  &.collapse {
    .sidebar-title {
      font-size: 14px;
    }
  }
}
</style>