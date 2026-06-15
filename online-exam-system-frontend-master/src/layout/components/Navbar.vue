<template>
  <div class="navbar">
    <div class="navbar-top">
      <hamburger
        :is-active="sidebar.opened"
        class="hamburger-container"
        @toggleClick="toggleSideBar"
      />

      <breadcrumb class="breadcrumb-container" />

      <div class="right-menu">
        <div class="theme-toggle" @click="toggleTheme" :title="themeLabel">
          <i :class="themeIcon" />
        </div>

        <div class="notification-bell" @click="goNotifications" title="消息中心">
          <el-badge :value="totalUnreadCount" :hidden="totalUnreadCount === 0" :max="99">
            <i class="el-icon-bell" />
          </el-badge>
          <span v-if="mustReadCount > 0" class="must-read-dot"></span>
        </div>

        <el-dropdown class="ai-model-selector" trigger="click" @command="handleModelChange">
          <div class="ai-model-toggle" title="切换AI模型">
            <i class="el-icon-cpu" />
            <span class="ai-model-name">{{ currentModelLabel }}</span>
          </div>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item
              v-for="model in aiModelList"
              :key="model.id"
              :command="model"
              :class="{ 'is-active': model.id === aiConfigId }"
            >
              <i class="el-icon-cpu" style="margin-right: 6px;" />
              {{ model.configName }}
              <span v-if="model.model" class="model-tag">{{ model.model }}</span>
            </el-dropdown-item>
            <el-dropdown-item v-if="aiModelList.length === 0" disabled>
              暂无可用模型
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>

        <el-dropdown class="avatar-container" trigger="click">
          <div class="avatar-wrapper">
            <img :src="getAvatarUrl(avatar)" class="user-avatar" @error="handleAvatarError">
            <i class="el-icon-caret-bottom" />
          </div>
          <el-dropdown-menu slot="dropdown" class="user-dropdown">
            <router-link to="/myself">
              <el-dropdown-item> 个人中心 </el-dropdown-item>
            </router-link>
            <router-link to="/change-password">
              <el-dropdown-item> 修改密码</el-dropdown-item>
            </router-link>

            <el-dropdown-item divided @click.native="logout">
              <span style="display: block">退出登录</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </div>
    <div class="tags-bar">
      <template v-for="(item,index) in tags">
        <el-tag
          v-if="item.title"
          :key="index"
          closable
          disable-transitions
          :class="{ active: item.checked }"
          @click="$router.push(item.path)"
          @close="handleTagClose(item)"
        >
          {{ item.title }}
        </el-tag>
      </template>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import Breadcrumb from '@/components/Breadcrumb'
import Hamburger from '@/components/Hamburger'
import { getToken } from '@/utils/auth'
import { parseJwt } from '@/utils/jwtUtils'
import { getUnreadCount, getMustReadCount, getUnreadSummary } from '@/api/notification'
import { noticeGetUnreadCount } from '@/api/notice'
import messagePopup from '@/utils/messagePopup'
export default {
  components: {
    Breadcrumb,
    Hamburger
  },
  data() {
    return {
      user: {},
      unreadCount: 0,
      mustReadCount: 0,
      noticeUnreadCount: 0,
      totalUnreadCount: 0,
      notificationTimer: null
    }
  },
  computed: {
    ...mapGetters(['sidebar', 'avatar', 'tags', 'aiConfigId', 'aiModelName', 'aiModelList']),
    themeMode() {
      return this.$store.state.theme.mode
    },
    themeIcon() {
      return this.themeMode === 'dark' ? 'el-icon-sunny' : 'el-icon-moon'
    },
    themeLabel() {
      return this.themeMode === 'dark' ? '切换浅色模式' : '切换深色模式'
    },
    currentModelLabel() {
      if (this.aiModelName) {
        return this.aiModelName.length > 8 ? this.aiModelName.substring(0, 8) + '...' : this.aiModelName
      }
      return 'AI'
    }
  },
  created() {
    this.decode()
    this.$store.dispatch('aiModel/loadModels')
    this.fetchNotificationCount()
    this.notificationTimer = setInterval(() => {
      this.fetchNotificationCount()
    }, 60000)
  },
  beforeDestroy() {
    if (this.notificationTimer) {
      clearInterval(this.notificationTimer)
    }
  },
  methods: {
    async fetchNotificationCount() {
      try {
        const [summaryRes, mustReadRes, noticeRes] = await Promise.all([
          getUnreadSummary(),
          getMustReadCount(),
          noticeGetUnreadCount()
        ])
        if (summaryRes.code) {
          const summary = summaryRes.data || {}
          this.unreadCount = summary.total || 0
        }
        if (mustReadRes.code) this.mustReadCount = mustReadRes.data || 0
        if (noticeRes.code) this.noticeUnreadCount = noticeRes.data || 0
        this.totalUnreadCount = this.unreadCount + this.noticeUnreadCount
        if (this.mustReadCount > 0) {
          this.$notify({
            title: '必读公告',
            message: `您有${this.mustReadCount}条必读公告未阅读`,
            type: 'warning',
            duration: 5000
          })
        }
      } catch (e) { /* ignore */ }
    },
    goNotifications() {
      this.$router.push('/notifications')
    },
    handleModelChange(model) {
      this.$store.dispatch('aiModel/selectModel', {
        configId: model.id,
        modelName: model.configName
      })
      this.$message.success('已切换AI模型: ' + model.configName)
    },
    toggleTheme() {
      this.$store.dispatch('theme/toggleTheme')
    },
    handleTagClose(item) {
      if (this.$route.path === item.path) {
        this.$store.commit('menu/REMOVE_TAG', item)
        const tags = this.$store.state.menu.tags
        if (tags.length > 0) {
          this.$router.push(tags[tags.length - 1].path).then(() => {
            window.location.reload()
          })
        }
      } else {
        this.$store.commit('menu/REMOVE_TAG', item)
      }
    },
    decode() {
      const token = getToken()
      const user = parseJwt(token)
      this.user = JSON.parse(user.userInfo)
    },
    getAvatarUrl(avatar) {
      if (!avatar || avatar.trim() === '') {
        return this.getDefaultAvatarUrl()
      }
      if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
        return avatar
      }
      const baseApi = process.env.VUE_APP_BASE_API || '/api'
      const avatarPath = avatar.startsWith('/') ? avatar : '/' + avatar
      return baseApi + avatarPath
    },
    getDefaultAvatarUrl() {
      return 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIHZpZXdCb3g9IjAgMCA0MCA0MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPGNpcmNsZSBjeD0iMjAiIGN5PSIyMCIgcj0iMjAiIGZpbGw9IiNFNUU1RTUiLz4KPHBhdGggZD0iTTIwIDE4QzIyLjIwOTEgMTggMjQgMTYuMjA5MSAyNCAxNEMyNCAxMS43OTA5IDIyLjIwOTEgMTAgMjAgMTBDMTcuNzkwOSAxMCAxNiAxMS43OTA5IDE2IDE0QzE2IDE2LjIwOTEgMTcuNzkwOSAxOCAyMCAxOFoiIGZpbGw9IndoaXRlIi8+CjxwYXRoIGQ9Ik0yMCAyMkMxNy43OTA5IDIyIDE1IDIzLjEyNDMgMTUgMjVWMjdIMjVWMjVDMjUgMjMuMTI0MyAyMi4yMDkgMjIgMjAgMjJaIiBmaWxsPSJ3aGl0ZSIvPgo8L3N2Zz4K'
    },
    handleAvatarError(event) {
      console.warn('右上角头像加载失败，使用默认头像')
      event.target.src = this.getDefaultAvatarUrl()
    },
    toggleSideBar() {
      this.$store.dispatch('app/toggleSideBar')
    },
    async logout() {
      this.$store.dispatch('logoutUser')
      await this.$store.dispatch('user/logout')
      this.$router.push(`/login`)
    }
  }
}
</script>

<style lang="scss" scoped>
.navbar {
  height: 110px;
  overflow: hidden;
  position: relative;
  background: var(--navbar-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border-bottom: 1px solid var(--navbar-border);
  box-shadow: var(--navbar-shadow);
  transition: background 0.3s ease, border-color 0.3s ease;

  .navbar-top {
    width: 100%;
    height: 66px;
    display: flex;
    align-items: center;
  }

  .hamburger-container {
    line-height: 46px;
    height: 100%;
    flex-shrink: 0;
    cursor: pointer;
    transition: background 0.3s;
    -webkit-tap-highlight-color: transparent;
    color: var(--text-secondary);

    &:hover {
      background: var(--bg-hover);
    }
  }

  .breadcrumb-container {
    flex-shrink: 0;
  }

  .right-menu {
    margin-left: auto;
    height: 100%;
    display: flex;
    align-items: center;
    gap: 12px;
    padding-right: 20px;

    &:focus {
      outline: none;
    }

    .theme-toggle {
      width: 36px;
      height: 36px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: all 0.2s ease;
      color: var(--text-secondary);
      font-size: 18px;
      background: var(--bg-hover);
      border: 1px solid var(--border-primary);

      &:hover {
        color: var(--accent);
        background: var(--bg-active);
        border-color: var(--accent);
      }
    }

    .notification-bell {
      width: 36px;
      height: 36px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: all 0.2s ease;
      color: var(--text-secondary);
      font-size: 18px;
      background: var(--bg-hover);
      border: 1px solid var(--border-primary);
      position: relative;

      &:hover {
        color: var(--accent);
        background: var(--bg-active);
        border-color: var(--accent);
      }

      .must-read-dot {
        position: absolute;
        top: -2px;
        right: -2px;
        width: 8px;
        height: 8px;
        background: #f56c6c;
        border-radius: 50%;
      }
    }

    .ai-model-selector {
      .ai-model-toggle {
        display: flex;
        align-items: center;
        gap: 5px;
        padding: 5px 12px;
        border-radius: 10px;
        cursor: pointer;
        transition: all 0.2s ease;
        background: var(--bg-hover);
        border: 1px solid var(--border-primary);
        color: var(--text-secondary);
        font-size: 13px;
        white-space: nowrap;

        i {
          font-size: 16px;
          color: var(--accent);
        }

        &:hover {
          background: var(--bg-active);
          border-color: var(--accent);
          color: var(--accent);
        }
      }
    }

    .avatar-container {

      .avatar-wrapper {
        display: flex;
        align-items: center;
        gap: 4px;
        cursor: pointer;

        .user-avatar {
          width: 40px;
          height: 40px;
          border-radius: 10px;
          border: 2px solid var(--border-primary);
          transition: border-color 0.2s;
        }

        &:hover .user-avatar {
          border-color: var(--accent);
        }

        .el-icon-caret-bottom {
          font-size: 12px;
          color: var(--text-secondary);
        }
      }
    }
  }

  .tags-bar {
    width: 100%;
    height: 44px;
    overflow: hidden;
    display: flex;
    align-items: center;
    padding: 0 12px;
    background: var(--bg-secondary);
    border-top: 1px solid var(--border-secondary);
  }
}

.el-tag {
  background: var(--tag-bg);
  border-color: var(--tag-border);
  display: inline-block;
  height: 32px;
  padding: 0 10px;
  line-height: 30px;
  margin-left: 5px;
  font-size: 12px;
  color: var(--tag-text);
  border-width: 1px;
  border-style: solid;
  border-radius: 6px;
  box-sizing: border-box;
  white-space: nowrap;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: var(--bg-hover);
  }
}

.active {
  background: var(--tag-active-bg) !important;
  color: var(--tag-active-text) !important;
  border-color: var(--accent) !important;
}
</style>

<style>
.ai-model-selector .model-tag {
  font-size: 11px;
  color: #909399;
  margin-left: 6px;
  background: #f0f2f5;
  padding: 1px 6px;
  border-radius: 4px;
}

.ai-model-selector .el-dropdown-menu__item.is-active {
  color: #409eff;
  font-weight: 600;
}
</style>