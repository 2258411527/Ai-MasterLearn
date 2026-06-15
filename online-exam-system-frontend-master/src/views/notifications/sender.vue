<template>
  <div class="notification-sender">
    <div class="page-header">
      <h2 class="page-title">
        <i class="el-icon-message" />
        发送通知
      </h2>
    </div>

    <el-card class="form-card">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="通知类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择通知类型">
            <el-option label="系统通知" value="system" />
            <el-option label="系统升级通知" value="system_upgrade" />
            <el-option label="公告通知" value="notice" />
          </el-select>
        </el-form-item>

        <el-form-item label="通知标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入通知标题" maxlength="100" show-word-limit />
        </el-form-item>

        <el-form-item label="通知内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入通知内容" maxlength="1000" show-word-limit />
        </el-form-item>

        <el-form-item label="是否必读">
          <el-switch v-model="form.isMustRead" active-text="必读" inactive-text="普通" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="sending" @click="handleSend">发送通知</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="history-card">
      <div slot="header">
        <span>发送历史</span>
      </div>
      <el-table :data="history" border>
        <el-table-column prop="type" label="类型" width="120">
          <template slot-scope="{ row }">
            <el-tag :type="getTypeTag(row.type)">{{ getTypeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="createTime" label="发送时间" width="180" />
        <el-table-column prop="isMustRead" label="必读" width="80">
          <template slot-scope="{ row }">
            <el-tag v-if="row.isMustRead" type="danger" size="mini">是</el-tag>
            <el-tag v-else type="info" size="mini">否</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { broadcastNotification, sendSystemUpgradeNotification } from '@/api/notification'

export default {
  name: 'NotificationSender',
  data() {
    return {
      sending: false,
      form: {
        type: 'system',
        title: '',
        content: '',
        isMustRead: false
      },
      rules: {
        type: [{ required: true, message: '请选择通知类型', trigger: 'change' }],
        title: [{ required: true, message: '请输入通知标题', trigger: 'blur' }],
        content: [{ required: true, message: '请输入通知内容', trigger: 'blur' }]
      },
      history: []
    }
  },
  methods: {
    async handleSend() {
      this.$refs.form.validate(async(valid) => {
        if (!valid) return

        this.sending = true
        try {
          const data = {
            type: this.form.type,
            title: this.form.title,
            content: this.form.content,
            isMustRead: this.form.isMustRead ? 1 : 0
          }

          let res
          if (this.form.type === 'system_upgrade') {
            res = await sendSystemUpgradeNotification(data)
          } else {
            res = await broadcastNotification(data)
          }

          if (res.code) {
            this.$message.success('通知发送成功')
            this.history.unshift({
              type: this.form.type,
              title: this.form.title,
              createTime: new Date().toLocaleString(),
              isMustRead: this.form.isMustRead
            })
            this.resetForm()
          } else {
            this.$message.error(res.msg || '发送失败')
          }
        } catch (e) {
          this.$message.error('发送失败')
        } finally {
          this.sending = false
        }
      })
    },
    resetForm() {
      this.$refs.form.resetFields()
      this.form.isMustRead = false
    },
    getTypeLabel(type) {
      const types = { system: '系统通知', system_upgrade: '系统升级', notice: '公告' }
      return types[type] || type
    },
    getTypeTag(type) {
      const tags = { system: '', system_upgrade: 'primary', notice: 'info' }
      return tags[type] || 'info'
    }
  }
}
</script>

<style lang="scss" scoped>
.notification-sender {
  padding: 24px;
}

.page-header {
  margin-bottom: 20px;

  .page-title {
    font-size: 22px;
    font-weight: 600;
    margin: 0;
    display: flex;
    align-items: center;
    gap: 10px;

    i { color: #409eff; }
  }
}

.form-card {
  margin-bottom: 20px;
}

.history-card {
  margin-top: 20px;
}
</style>
