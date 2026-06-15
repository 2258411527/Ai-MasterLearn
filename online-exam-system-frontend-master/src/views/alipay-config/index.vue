<template>
  <div class="alipay-config-container">
    <div class="bg-decoration">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="gradient-orb orb-3"></div>
    </div>

    <div class="content-wrapper">
      <div class="page-header glass-card">
        <div class="header-left">
          <div class="title-area">
            <h1 class="page-title">
              <i class="el-icon-bank-card"></i>
              支付配置管理
            </h1>
            <p class="page-subtitle">配置支付宝支付参数，支持沙箱测试与正式环境</p>
          </div>
        </div>
        <div class="header-right">
          <div class="status-info">
            <div class="status-item" :class="{ active: activeConfig }">
              <i :class="activeConfig ? 'el-icon-circle-check' : 'el-icon-circle-close'"></i>
              <span>{{ activeConfig ? '支付功能已启用' : '支付功能未启用' }}</span>
            </div>
          </div>
          <el-button type="primary" icon="el-icon-plus" class="add-btn" @click="handleAdd">添加配置</el-button>
        </div>
      </div>

      <div v-if="activeConfig" class="active-config-card glass-card">
        <div class="card-header">
          <div class="header-title">
            <i class="el-icon-success"></i>
            <span>当前激活配置</span>
          </div>
        </div>
        <div class="active-config-content">
          <div class="config-main">
            <div class="config-name">{{ activeConfig.configName }}</div>
            <div class="config-badges">
              <el-tag :type="activeConfig.testMode ? 'warning' : 'success'" size="small" effect="dark">
                {{ activeConfig.testMode ? '测试模式' : '正式环境' }}
              </el-tag>
              <el-tag :type="activeConfig.privateKey && activeConfig.alipayPublicKey ? 'success' : 'danger'" size="small">
                {{ activeConfig.privateKey && activeConfig.alipayPublicKey ? '密钥已配置' : '密钥未配置' }}
              </el-tag>
            </div>
          </div>
          <div class="config-details">
            <div class="detail-item">
              <span class="detail-label">AppId</span>
              <span class="detail-value">{{ activeConfig.appId || '未设置' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">网关</span>
              <span class="detail-value">{{ activeConfig.serverUrl && activeConfig.serverUrl.includes('sandbox') ? '沙箱环境' : '正式环境' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">更新时间</span>
              <span class="detail-value">{{ activeConfig.updateTime }}</span>
            </div>
          </div>
          <div class="config-actions">
            <el-button size="small" type="primary" icon="el-icon-edit" @click="handleEdit(activeConfig)">编辑</el-button>
            <el-button size="small" type="warning" icon="el-icon-video-pause" @click="handleDeactivate(activeConfig)">取消激活</el-button>
          </div>
        </div>
      </div>

      <div class="config-list-card glass-card">
        <div class="card-header">
          <span class="header-title">全部配置</span>
          <div class="header-tip">
            <i class="el-icon-info"></i>
            <span>测试模式下支付自动成功，无需真实支付宝调用</span>
          </div>
        </div>

        <el-table v-loading="loading" :data="configs" class="config-table" stripe>
          <el-table-column label="配置信息" min-width="200">
            <template slot-scope="{ row }">
              <div class="config-info-cell">
                <div class="config-title">{{ row.configName }}</div>
                <div class="config-meta">
                  <el-tag :type="row.testMode ? 'warning' : 'primary'" size="mini" effect="plain">
                    {{ row.testMode ? '测试' : '正式' }}
                  </el-tag>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="AppId" min-width="160">
            <template slot-scope="{ row }">
              <span class="app-id-cell">{{ row.appId || '未配置' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="密钥状态" align="center" width="100">
            <template slot-scope="{ row }">
              <el-tooltip :content="row.privateKey && row.alipayPublicKey ? '密钥已配置' : '密钥未配置'" placement="top">
                <div class="key-status" :class="{ configured: row.privateKey && row.alipayPublicKey }">
                  <i :class="row.privateKey && row.alipayPublicKey ? 'el-icon-lock' : 'el-icon-unlock'"></i>
                </div>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column label="状态" align="center" width="100">
            <template slot-scope="{ row }">
              <div class="status-cell" :class="{ active: row.isActive }">
                <span class="status-dot"></span>
                <span>{{ row.isActive ? '已激活' : '未激活' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="更新时间" align="center" width="160">
            <template slot-scope="{ row }">
              <span class="time-text">{{ row.updateTime }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="180">
            <template slot-scope="{ row }">
              <div class="action-cell">
                <el-tooltip content="编辑" placement="top">
                  <el-button size="mini" type="primary" icon="el-icon-edit" circle @click="handleEdit(row)" />
                </el-tooltip>
                <el-tooltip v-if="!row.isActive" content="激活" placement="top">
                  <el-button size="mini" type="success" icon="el-icon-video-play" circle @click="handleActivate(row)" />
                </el-tooltip>
                <el-tooltip v-else content="取消激活" placement="top">
                  <el-button size="mini" type="warning" icon="el-icon-video-pause" circle @click="handleDeactivate(row)" />
                </el-tooltip>
                <el-tooltip content="删除" placement="top">
                  <el-button size="mini" type="danger" icon="el-icon-delete" circle :disabled="row.isActive" @click="handleDelete(row)" />
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="configs.length === 0 && !loading" class="empty-state">
          <i class="el-icon-setting"></i>
          <p>暂无支付配置</p>
          <el-button type="primary" @click="handleAdd">添加配置</el-button>
        </div>

        <div v-if="total > 0" class="pagination-container">
          <el-pagination
            :current-page="pageNum"
            :page-size="pageSize"
            :total="total"
            layout="total, prev, pager, next"
            @current-change="val => { pageNum = val; loadConfigs() }"
          />
        </div>
      </div>

      <div class="help-section">
        <div class="help-card glass-card">
          <div class="help-header">
            <i class="el-icon-question"></i>
            <span>测试模式</span>
          </div>
          <div class="help-body">
            <p>开启测试模式后，支付将自动成功，无需调用真实支付宝接口。适合开发测试阶段使用。</p>
          </div>
        </div>
        <div class="help-card glass-card">
          <div class="help-header">
            <i class="el-icon-document"></i>
            <span>沙箱环境</span>
          </div>
          <div class="help-body">
            <p>使用支付宝官方沙箱环境进行测试，需要配置沙箱AppId和密钥。访问 open.alipay.com 获取。</p>
          </div>
        </div>
        <div class="help-card glass-card">
          <div class="help-header">
            <i class="el-icon-s-promotion"></i>
            <span>正式环境</span>
          </div>
          <div class="help-body">
            <p>生产环境使用，需要真实的支付宝应用配置。请确保配置正确后再激活使用。</p>
          </div>
        </div>
      </div>
    </div>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px" top="5vh" class="config-dialog">
      <el-form ref="configForm" :model="form" :rules="rules" label-position="top" class="config-form">
        <el-form-item label="配置名称" prop="configName">
          <el-input v-model="form.configName" placeholder="如：正式环境配置、测试配置" />
        </el-form-item>
        <el-form-item label="AppId" prop="appId">
          <el-input v-model="form.appId" placeholder="支付宝应用AppId">
            <i slot="prefix" class="el-icon-key"></i>
          </el-input>
        </el-form-item>
        <el-form-item label="应用私钥">
          <el-input
            v-model="form.privateKey"
            type="textarea"
            :rows="3"
            :placeholder="isEdit ? '不修改请留空' : '请填写应用私钥'"
          />
        </el-form-item>
        <el-form-item label="支付宝公钥">
          <el-input
            v-model="form.alipayPublicKey"
            type="textarea"
            :rows="3"
            :placeholder="isEdit ? '不修改请留空' : '请填写支付宝公钥'"
          />
        </el-form-item>
        <el-form-item label="网关环境">
          <el-radio-group v-model="form.serverUrl" class="server-radio">
            <el-radio-button label="https://openapi.alipay.com/gateway.do">
              <i class="el-icon-s-promotion"></i> 正式环境
            </el-radio-button>
            <el-radio-button label="https://openapi-sandbox.dl.alipaydev.com/gateway.do">
              <i class="el-icon-document"></i> 沙箱环境
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="异步通知地址">
              <el-input v-model="form.notifyUrl" placeholder="/api/alipay/notify" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="同步跳转地址">
              <el-input v-model="form.returnUrl" placeholder="/token" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="测试模式">
          <div class="test-mode-box">
            <el-switch v-model="form.testMode" active-color="#13ce66" inactive-color="#ff4949" />
            <div class="mode-desc">
              <span class="mode-label">{{ form.testMode ? '已开启' : '已关闭' }}</span>
              <span class="mode-hint">{{ form.testMode ? '支付将自动成功' : '调用真实支付宝' }}</span>
            </div>
          </div>
          <div class="test-mode-warning" v-if="form.testMode">
            <i class="el-icon-warning"></i>
            <span>测试模式下无需真实支付宝配置，支付将自动成功</span>
          </div>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">
          <i class="el-icon-check"></i> 保存
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getAlipayConfigPage,
  addAlipayConfig,
  updateAlipayConfig,
  deleteAlipayConfig,
  activateAlipayConfig
} from '@/api/alipay-config'

export default {
  name: 'AlipayConfig',
  data() {
    return {
      configs: [],
      loading: false,
      pageNum: 1,
      pageSize: 10,
      total: 0,
      dialogVisible: false,
      dialogTitle: '添加配置',
      isEdit: false,
      editingId: null,
      saving: false,
      form: {
        configName: '',
        appId: '',
        privateKey: '',
        alipayPublicKey: '',
        serverUrl: 'https://openapi-sandbox.dl.alipaydev.com/gateway.do',
        notifyUrl: '',
        returnUrl: '',
        signType: 'RSA2',
        testMode: true
      },
      rules: {
        configName: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
        appId: [{ required: true, message: '请输入AppId', trigger: 'blur' }]
      }
    }
  },
  computed: {
    activeConfig() {
      return this.configs.find(c => c.isActive)
    }
  },
  mounted() {
    this.loadConfigs()
  },
  methods: {
    async loadConfigs() {
      this.loading = true
      try {
        const res = await getAlipayConfigPage({
          pageNum: this.pageNum,
          pageSize: this.pageSize
        })
        if (res.code && res.data) {
          this.configs = res.data.records
          this.total = res.data.total
        }
      } catch (e) {
        console.error('加载配置失败:', e)
      } finally {
        this.loading = false
      }
    },
    handleAdd() {
      this.isEdit = false
      this.editingId = null
      this.dialogTitle = '添加支付配置'
      this.form = {
        configName: '',
        appId: '',
        privateKey: '',
        alipayPublicKey: '',
        serverUrl: 'https://openapi-sandbox.dl.alipaydev.com/gateway.do',
        notifyUrl: '',
        returnUrl: '',
        signType: 'RSA2',
        testMode: true
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.isEdit = true
      this.editingId = row.id
      this.dialogTitle = '编辑支付配置'
      this.form = {
        configName: row.configName,
        appId: row.appId,
        privateKey: '',
        alipayPublicKey: '',
        serverUrl: row.serverUrl || 'https://openapi.alipay.com/gateway.do',
        notifyUrl: row.notifyUrl || '',
        returnUrl: row.returnUrl || '',
        signType: row.signType || 'RSA2',
        testMode: row.testMode || false
      }
      this.dialogVisible = true
    },
    async handleSave() {
      try {
        await this.$refs.configForm.validate()
      } catch {
        return
      }

      this.saving = true
      try {
        let res
        if (this.isEdit) {
          res = await updateAlipayConfig(this.editingId, this.form)
        } else {
          res = await addAlipayConfig(this.form)
        }
        if (res.code) {
          this.$message.success(res.msg || '保存成功')
          this.dialogVisible = false
          this.loadConfigs()
        }
      } catch (e) {
        console.error('保存配置失败:', e)
      } finally {
        this.saving = false
      }
    },
    async handleActivate(row) {
      const msg = row.testMode
        ? `确定激活「${row.configName}」？\n\n✅ 测试模式：支付将自动成功`
        : `确定激活「${row.configName}」？\n\n⚠️ 正式环境：将调用真实支付宝`
      try {
        await this.$confirm(msg, '确认激活', { type: 'warning' })
      } catch {
        return
      }

      try {
        const res = await activateAlipayConfig(row.id)
        if (res.code) {
          this.$message.success('激活成功')
          this.loadConfigs()
        }
      } catch (e) {
        console.error('激活失败:', e)
      }
    },
    async handleDeactivate(row) {
      try {
        await this.$confirm(`确定取消激活「${row.configName}」？取消后支付功能将不可用`, '确认', { type: 'warning' })
      } catch {
        return
      }

      try {
        const res = await activateAlipayConfig(row.id)
        if (res.code) {
          this.$message.success('已取消激活')
          this.loadConfigs()
        }
      } catch (e) {
        console.error('操作失败:', e)
      }
    },
    async handleDelete(row) {
      if (row.isActive) {
        this.$message.warning('请先取消激活')
        return
      }
      
      try {
        await this.$confirm(`确定删除「${row.configName}」？`, '确认删除', { type: 'warning' })
      } catch {
        return
      }

      try {
        const res = await deleteAlipayConfig(row.id)
        if (res.code) {
          this.$message.success('删除成功')
          this.loadConfigs()
        }
      } catch (e) {
        console.error('删除失败:', e)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.alipay-config-container {
  min-height: 100vh;
  background: var(--bg-primary);
  position: relative;
  overflow: hidden;
}

.bg-decoration {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;

  .gradient-orb {
    position: absolute;
    border-radius: 50%;
    filter: blur(100px);
    opacity: 0.3;

    &.orb-1 {
      width: 500px;
      height: 500px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      top: -150px;
      right: -100px;
    }

    &.orb-2 {
      width: 400px;
      height: 400px;
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      bottom: -100px;
      left: -100px;
    }

    &.orb-3 {
      width: 250px;
      height: 250px;
      background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
    }
  }
}

.content-wrapper {
  position: relative;
  z-index: 1;
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.glass-card {
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  border-radius: 16px;
  margin-bottom: 20px;
  transition: all 0.3s ease;

  &:hover { border-color: var(--border-focus); }
}

.page-header {
  padding: 20px 28px;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .header-left {
    .page-title {
      margin: 0 0 6px 0;
      font-size: 24px;
      font-weight: 700;
      color: var(--text-primary);
      display: flex;
      align-items: center;
      gap: 12px;

      i { color: var(--accent-light); }
    }

    .page-subtitle {
      margin: 0;
      font-size: 13px;
      color: var(--text-secondary);
      padding-left: 36px;
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 20px;

    .status-info {
      .status-item {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 10px 20px;
        background: var(--bg-hover);
        border-radius: 10px;
        font-size: 14px;
        color: var(--text-secondary);

        i { font-size: 18px; }

        &.active {
          background: rgba(16, 185, 129, 0.1);
          color: var(--success);

          i { color: var(--success); }
        }
      }
    }

    .add-btn {
      border-radius: 10px;
      padding: 12px 24px;
      font-weight: 500;
      background: var(--accent-gradient);
      border: none;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 24px rgba(99, 102, 241, 0.4);
      }
    }
  }
}

.active-config-card {
  padding: 20px 24px;
  border: 1px solid rgba(16, 185, 129, 0.3);
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.05) 0%, rgba(99, 102, 241, 0.05) 100%);

  .card-header {
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid var(--glass-border);

    .header-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 15px;
      font-weight: 600;
      color: var(--text-primary);

      i { color: var(--success); }
    }
  }

  .active-config-content {
    display: flex;
    align-items: center;
    gap: 24px;

    .config-main {
      flex: 1;

      .config-name {
        font-size: 18px;
        font-weight: 600;
        color: var(--text-primary);
        margin-bottom: 8px;
      }

      .config-badges {
        display: flex;
        gap: 8px;
      }
    }

    .config-details {
      display: flex;
      gap: 24px;

      .detail-item {
        .detail-label {
          display: block;
          font-size: 12px;
          color: var(--text-secondary);
          margin-bottom: 2px;
        }

        .detail-value {
          font-size: 14px;
          color: var(--text-primary);
        }
      }
    }

    .config-actions {
      display: flex;
      gap: 8px;
    }
  }
}

.config-list-card {
  padding: 20px 24px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    .header-title {
      font-size: 16px;
      font-weight: 600;
      color: var(--text-primary);
    }

    .header-tip {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      color: var(--text-secondary);

      i { color: var(--accent); }
    }
  }
}

.config-table {
  ::v-deep .el-table__header th {
    background: var(--table-header-bg);
    color: var(--text-primary);
    font-weight: 600;
  }

  ::v-deep .el-table__body td {
    color: var(--text-primary);
  }

  .config-info-cell {
    .config-title {
      font-weight: 600;
      color: var(--text-primary);
      margin-bottom: 4px;
    }

    .config-meta { display: flex; gap: 6px; }
  }

  .app-id-cell {
    font-family: monospace;
    font-size: 13px;
    color: var(--text-secondary);
  }

  .key-status {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: rgba(245, 158, 11, 0.1);
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto;

    i {
      font-size: 16px;
      color: var(--warning);
    }

    &.configured {
      background: rgba(16, 185, 129, 0.1);

      i { color: var(--success); }
    }
  }

  .status-cell {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    font-size: 13px;
    color: var(--text-secondary);

    .status-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: var(--text-hint);
    }

    &.active {
      color: var(--success);

      .status-dot {
        background: var(--success);
        box-shadow: 0 0 8px var(--success);
        animation: pulse 2s infinite;
      }
    }
  }

  .time-text {
    font-size: 13px;
    color: var(--text-secondary);
  }

  .action-cell {
    display: flex;
    justify-content: center;
    gap: 8px;
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-secondary);

  i {
    font-size: 48px;
    margin-bottom: 16px;
    display: block;
  }

  p { margin: 0 0 20px 0; }
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.help-section {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.help-card {
  padding: 20px;

  .help-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
    font-size: 15px;
    font-weight: 600;
    color: var(--text-primary);

    i { color: var(--accent); }
  }

  .help-body {
    font-size: 13px;
    color: var(--text-secondary);
    line-height: 1.6;
  }
}

::v-deep .config-dialog {
  .el-dialog {
    background: var(--dialog-bg);
    border: 1px solid var(--dialog-border);
    border-radius: 16px;
  }

  .el-dialog__header {
    padding: 20px 24px 16px;
    border-bottom: 1px solid var(--glass-border);
  }

  .el-dialog__title {
    color: var(--text-primary);
    font-weight: 600;
  }

  .el-dialog__body { padding: 24px; }

  .el-dialog__footer {
    padding: 16px 24px 20px;
    border-top: 1px solid var(--glass-border);
  }
}

.config-form {
  ::v-deep .el-form-item__label {
    color: var(--text-primary);
    font-weight: 500;
  }

  ::v-deep .el-input__inner {
    background: var(--bg-input);
    border-color: var(--border-primary);
    color: var(--text-primary);

    &:focus { border-color: var(--accent); }
  }

  .server-radio {
    width: 100%;

    ::v-deep .el-radio-button__inner {
      background: var(--bg-input);
      border-color: var(--border-primary);
      color: var(--text-secondary);

      i { margin-right: 6px; }
    }

    ::v-deep .el-radio-button__orig-radio:checked + .el-radio-button__inner {
      background: var(--accent-gradient);
      border-color: var(--accent);
      color: white;
    }
  }
}

.test-mode-box {
  display: flex;
  align-items: center;
  gap: 16px;

  .mode-desc {
    .mode-label {
      display: block;
      font-size: 14px;
      font-weight: 500;
      color: var(--text-primary);
    }

    .mode-hint {
      font-size: 12px;
      color: var(--text-secondary);
    }
  }
}

.test-mode-warning {
  margin-top: 12px;
  padding: 12px 16px;
  background: rgba(245, 158, 11, 0.1);
  border-radius: 8px;
  font-size: 13px;
  color: var(--warning);
  display: flex;
  align-items: center;
  gap: 8px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;

    .header-right {
      width: 100%;
      flex-direction: column;
    }
  }

  .active-config-card .active-config-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .help-section {
    grid-template-columns: 1fr;
  }
}
</style>
