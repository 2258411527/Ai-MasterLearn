<template>
  <div>
    <div v-if="loading">
      <el-card>
        <div style="text-align: center; padding: 40px;">
          <i class="el-icon-loading" style="font-size: 48px; color: #409EFF;" />
          <p style="margin-top: 20px; color: #606266;">加载中...</p>
        </div>
      </el-card>
    </div>
    <div v-else-if="error">
      <el-card>
        <div style="text-align: center; padding: 40px;">
          <i class="el-icon-warning" style="font-size: 48px; color: #F56C6C;" />
          <p style="margin-top: 20px; color: #606266;">{{ error }}</p>
          <el-button style="margin-top: 20px;" type="primary" @click="reloadPage">重新加载</el-button>
        </div>
      </el-card>
    </div>
    <div v-else>
      <com-admin v-if="url === 0" />
      <ai-learning-platform v-else-if="url === 2" />
      <div v-else>
        <el-card>
          <div style="text-align: center; padding: 40px;">
            <i class="el-icon-info" style="font-size: 48px; color: #E6A23C;" />
            <p style="margin-top: 20px; color: #606266;">未识别的用户角色，请重新登录</p>
            <el-button style="margin-top: 20px;" type="primary" @click="handleLogout">重新登录</el-button>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>
<script>
import comAdmin from './rolePage/adminAndTeacher.vue'
import AILearningPlatform from './rolePage/ai-learning-platform.vue'
export default {
  components: {
    comAdmin,
    'ai-learning-platform': AILearningPlatform
  },
  data() {
    return {
      url: 0,
      loading: true,
      error: null
    }
  },
  created() {
    this.checkUserRole()
  },
  methods: {
    checkUserRole() {
      try {
        const roles = localStorage.getItem('roles')
        console.log('当前用户角色:', roles)

        if (!roles) {
          this.error = '未检测到用户信息，请重新登录'
          this.loading = false
          return
        }

        switch (roles) {
          case 'admin': {
            this.url = 0
            break
          }
          case 'teacher': {
            this.url = 0
            break
          }
          case 'student': {
            this.url = 2
            break
          }
          default: {
            this.error = `未知的用户角色: ${roles}`
            this.loading = false
            return
          }
        }

        this.loading = false
      } catch (err) {
        console.error('检查用户角色失败:', err)
        this.error = '加载用户信息失败，请刷新页面重试'
        this.loading = false
      }
    },
    reloadPage() {
      window.location.reload()
    },
    handleLogout() {
      localStorage.clear()
      this.$router.push('/login')
    }
  }
}
</script>
<style scoped>

</style>