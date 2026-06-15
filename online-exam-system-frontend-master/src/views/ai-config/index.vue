<template>
  <div class="ai-config-container">
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
              <i class="el-icon-cpu"></i>
              AI 服务配置
            </h1>
            <p class="page-subtitle">统一采用阿里百炼 DashScope 框架，兼容 OpenAI 接口协议</p>
          </div>
        </div>
        <div class="header-right">
          <div class="stats-info">
            <div class="stat-item">
              <span class="stat-value">{{ activeConfigs.length }}</span>
              <span class="stat-label">已激活</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-value">{{ data.total || 0 }}</span>
              <span class="stat-label">总配置</span>
            </div>
          </div>
          <el-button type="primary" icon="el-icon-plus" class="add-btn" @click="handleAdd">
            新增配置
          </el-button>
        </div>
      </div>

      <div v-if="activeConfigs.length > 0" class="active-config-card glass-card">
        <div class="card-header">
          <div class="header-title">
            <i class="el-icon-circle-check"></i>
            <span>当前激活的 AI 模型</span>
          </div>
        </div>
        <div class="active-configs-grid">
          <div v-for="cfg in activeConfigs" :key="cfg.id" class="active-config-item">
            <div class="config-header">
              <div class="config-name">{{ cfg.configName }}</div>
              <el-tag type="success" size="mini" effect="dark">运行中</el-tag>
            </div>
            <div class="config-body">
              <div class="config-row">
                <i class="el-icon-connection"></i>
                <span class="config-value">{{ cfg.baseUrl }}</span>
              </div>
              <div class="config-row">
                <i class="el-icon-data-analysis"></i>
                <span class="model-tag">{{ cfg.model }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="config-management-card glass-card">
        <div class="search-bar">
          <el-input
            v-model="searchForm.configName"
            placeholder="搜索配置名称..."
            prefix-icon="el-icon-search"
            clearable
            class="search-input"
            @clear="resetSearch"
            @keyup.enter.native="searchConfig"
          />
          <el-button type="primary" icon="el-icon-search" @click="searchConfig">查询</el-button>
          <el-button icon="el-icon-refresh" @click="resetSearch">重置</el-button>
        </div>

        <el-table :data="data.records" class="config-table" stripe>
          <el-table-column label="配置信息" min-width="280">
            <template slot-scope="{ row }">
              <div class="config-info-cell">
                <div class="config-title">{{ row.configName }}</div>
                <div class="config-meta">
                  <el-tag type="primary" size="mini" effect="plain">{{ row.model }}</el-tag>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="API 密钥" min-width="180">
            <template slot-scope="{ row }">
              <div class="api-key-cell">
                <i class="el-icon-key"></i>
                <span>{{ row.apiKeyDisplay }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="基础 URL" min-width="240">
            <template slot-scope="{ row }">
              <div class="url-cell">
                <i class="el-icon-link"></i>
                <span>{{ row.baseUrl }}</span>
              </div>
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
          <el-table-column label="操作" align="center" width="140">
            <template slot-scope="{ row }">
              <div class="action-cell">
                <el-tooltip :content="row.isActive ? '取消激活' : '激活'" placement="top">
                  <el-button
                    :type="row.isActive ? 'warning' : 'success'"
                    size="mini"
                    :icon="row.isActive ? 'el-icon-video-pause' : 'el-icon-video-play'"
                    circle
                    @click="toggleActivate(row)"
                  />
                </el-tooltip>
                <el-tooltip content="编辑" placement="top">
                  <el-button
                    type="primary"
                    size="mini"
                    icon="el-icon-edit"
                    circle
                    @click="editConfig(row)"
                  />
                </el-tooltip>
                <el-tooltip content="删除" placement="top">
                  <el-button
                    type="danger"
                    size="mini"
                    icon="el-icon-delete"
                    circle
                    @click="deleteConfig(row.id)"
                  />
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="data.total > 0" class="pagination-container">
          <el-pagination
            :current-page="data.current"
            :page-sizes="[10, 20, 30, 40]"
            :page-size="data.size"
            layout="total, sizes, prev, pager, next, jumper"
            :total="data.total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>

    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="520px"
      top="8vh"
      :close-on-click-modal="false"
      class="config-dialog"
    >
      <el-form ref="form" :model="form" :rules="rules" label-position="top" class="config-form">
        <el-form-item label="配置名称" prop="configName">
          <el-input v-model="form.configName" placeholder="例如：阿里百炼-生产环境">
            <i slot="prefix" class="el-icon-edit-outline"></i>
          </el-input>
        </el-form-item>
        <el-form-item label="API 密钥" prop="apiKey">
          <el-input
            v-model="form.apiKey"
            type="password"
            placeholder="请输入 API 密钥"
            show-password
          >
            <i slot="prefix" class="el-icon-key"></i>
          </el-input>
        </el-form-item>
        <el-form-item label="基础 URL" prop="baseUrl">
          <el-input v-model="form.baseUrl" placeholder="https://dashscope.aliyuncs.com/compatible-mode/v1">
            <i slot="prefix" class="el-icon-link"></i>
          </el-input>
        </el-form-item>
        <el-form-item label="模型名称" prop="model">
          <el-input v-model="form.model" placeholder="deepseek-v4-pro">
            <i slot="prefix" class="el-icon-data-analysis"></i>
          </el-input>
        </el-form-item>
        <el-form-item label="激活状态">
          <div class="activate-switch">
            <el-switch v-model="form.isActive" active-color="#13ce66" inactive-color="#ff4949" />
            <span class="switch-label">{{ form.isActive ? '立即激活此配置' : '暂不激活' }}</span>
          </div>
          <div class="switch-hint">激活后用户可在前端选择此模型进行 AI 对话</div>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="submitForm">
          <i class="el-icon-check"></i>
          {{ isEdit ? '保存修改' : '创建配置' }}
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  aiConfigPaging,
  aiConfigAdd,
  aiConfigUpdate,
  aiConfigDel,
  aiConfigActivate,
  aiConfigGetActive
} from '@/api/ai-config'

export default {
  name: 'AiConfigManagement',
  data() {
    return {
      pageNum: 1,
      pageSize: 10,
      data: {},
      searchForm: {
        configName: ''
      },
      activeConfigs: [],
      dialogVisible: false,
      isEdit: false,
      form: {
        configName: '',
        apiKey: '',
        baseUrl: '',
        model: '',
        isActive: false
      },
      rules: {
        configName: [
          { required: true, message: '请输入配置名称', trigger: 'blur' }
        ],
        apiKey: [
          { required: true, message: '请输入 API 密钥', trigger: 'blur' }
        ],
        baseUrl: [
          { required: true, message: '请输入基础 URL', trigger: 'blur' }
        ],
        model: [
          { required: true, message: '请输入模型名称', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '编辑 AI 配置' : '新增 AI 配置'
    }
  },
  created() {
    this.getConfigPage()
    this.getActiveConfig()
  },
  methods: {
    async getConfigPage() {
      const params = {
        pageNum: this.pageNum,
        pageSize: this.pageSize,
        configName: this.searchForm.configName
      }
      const res = await aiConfigPaging(params)
      if (res.code) {
        this.data = res.data
      }
    },

    async getActiveConfig() {
      const res = await aiConfigGetActive()
      if (res.code && res.data) {
        this.activeConfigs = Array.isArray(res.data) ? res.data : [res.data]
      } else {
        this.activeConfigs = []
      }
    },

    searchConfig() {
      this.pageNum = 1
      this.getConfigPage()
    },

    resetSearch() {
      this.searchForm.configName = ''
      this.pageNum = 1
      this.getConfigPage()
    },

    async toggleActivate(row) {
      const action = row.isActive ? '取消激活' : '激活'
      this.$confirm(`确定要${action}此配置吗？`, '提示', {
        confirmButtonText: `确定${action}`,
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async() => {
        try {
          const res = await aiConfigActivate(row.id)
          if (res.code === 1) {
            this.$message.success(res.msg || `${action}成功`)
            await this.getConfigPage()
            await this.getActiveConfig()
          } else {
            this.$message.error(res.msg || `${action}失败`)
          }
        } catch (error) {
          console.error(`${action}配置失败:`, error)
        }
      }).catch(() => {})
    },

    editConfig(row) {
      this.isEdit = true
      this.form = {
        id: row.id,
        configName: row.configName,
        apiKey: row.apiKey,
        baseUrl: row.baseUrl,
        model: row.model,
        isActive: row.isActive
      }
      this.dialogVisible = true
    },

    handleAdd() {
      this.resetForm()
      this.dialogVisible = true
    },

    deleteConfig(id) {
      this.$confirm('确定要删除此配置吗？删除后无法恢复。', '删除确认', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async() => {
        const res = await aiConfigDel(id)
        if (res.code) {
          this.$message.success('删除成功')
          this.getConfigPage()
          this.getActiveConfig()
        } else {
          this.$message.error(res.msg || '删除失败')
        }
      })
    },

    submitForm() {
      this.$refs.form.validate(async(valid) => {
        if (valid) {
          try {
            let res
            if (this.isEdit) {
              res = await aiConfigUpdate(this.form.id, this.form)
            } else {
              res = await aiConfigAdd(this.form)
            }
            if (res && res.code) {
              this.$message.success(this.isEdit ? '修改成功' : '添加成功')
              this.dialogVisible = false
              this.resetForm()
              this.getConfigPage()
              this.getActiveConfig()
            } else {
              this.$message.error((res && res.msg) || (this.isEdit ? '修改失败' : '添加失败'))
            }
          } catch (e) {
            console.error('提交AI配置异常:', e)
          }
        }
      })
    },

    handleCancel() {
      this.dialogVisible = false
      this.resetForm()
    },

    resetForm() {
      this.form = {
        id: null,
        configName: '',
        apiKey: '',
        baseUrl: '',
        model: '',
        isActive: false
      }
      this.isEdit = false
      if (this.$refs.form) {
        this.$refs.form.clearValidate()
      }
    },

    handleSizeChange(val) {
      this.pageSize = val
      this.getConfigPage()
    },

    handleCurrentChange(val) {
      this.pageNum = val
      this.getConfigPage()
    }
  }
}
</script>

<style lang="scss" scoped>
.ai-config-container {
  min-height: 100vh;
  background: var(--bg-primary);
  position: relative;
  overflow: hidden;
  transition: background 0.3s ease;
}

.bg-decoration {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  overflow: hidden;
}

.gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);

  &.orb-1 {
    width: 500px;
    height: 500px;
    background: linear-gradient(135deg, #6366F1 0%, #06B6D4 100%);
    top: -150px;
    right: -100px;
  }

  &.orb-2 {
    width: 400px;
    height: 400px;
    background: linear-gradient(135deg, #8B5CF6 0%, #EC4899 100%);
    bottom: -100px;
    left: -100px;
  }

  &.orb-3 {
    width: 250px;
    height: 250px;
    background: linear-gradient(135deg, #10B981 0%, #6366F1 100%);
    top: 40%;
    left: 50%;
    transform: translate(-50%, -50%);
  }
}

.content-wrapper {
  position: relative;
  z-index: 1;
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.glass-card {
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  border-radius: 16px;
  transition: all 0.3s ease;

  &:hover { border-color: var(--border-focus); }
}

.page-header {
  padding: 20px 28px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .header-left {
    .title-area {
      .page-title {
        margin: 0 0 6px 0;
        font-size: 24px;
        font-weight: 700;
        color: var(--text-primary);
        display: flex;
        align-items: center;
        gap: 10px;

        i { 
          color: var(--accent-light);
          font-size: 26px;
        }
      }

      .page-subtitle {
        margin: 0;
        font-size: 13px;
        color: var(--text-secondary);
        padding-left: 36px;
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 20px;

    .stats-info {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 10px 20px;
      background: var(--bg-hover);
      border-radius: 12px;

      .stat-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 2px;

        .stat-value {
          font-size: 22px;
          font-weight: 700;
          color: var(--accent-light);
        }

        .stat-label {
          font-size: 12px;
          color: var(--text-secondary);
        }
      }

      .stat-divider {
        width: 1px;
        height: 32px;
        background: var(--border-primary);
      }
    }

    .add-btn {
      border-radius: 10px;
      padding: 12px 24px;
      font-weight: 500;
      background: var(--accent-gradient);
      border: none;
      transition: all 0.2s;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 24px rgba(99, 102, 241, 0.4);
      }
    }
  }
}

.active-config-card {
  padding: 20px 24px;
  margin-bottom: 20px;

  .card-header {
    padding-bottom: 14px;
    margin-bottom: 16px;
    border-bottom: 1px solid var(--glass-border);

    .header-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 15px;
      font-weight: 600;
      color: var(--text-primary);

      i { 
        color: var(--success);
        font-size: 18px;
      }
    }
  }

  .active-configs-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 14px;

    .active-config-item {
      padding: 16px;
      background: linear-gradient(135deg, rgba(16, 185, 129, 0.08) 0%, rgba(99, 102, 241, 0.08) 100%);
      border: 1px solid rgba(16, 185, 129, 0.2);
      border-radius: 12px;
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 20px rgba(16, 185, 129, 0.15);
        border-color: rgba(16, 185, 129, 0.4);
      }

      .config-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;

        .config-name {
          font-size: 15px;
          font-weight: 600;
          color: var(--text-primary);
        }
      }

      .config-body {
        display: flex;
        flex-direction: column;
        gap: 8px;

        .config-row {
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 13px;
          color: var(--text-secondary);

          i {
            color: var(--accent-light);
            font-size: 14px;
          }

          .config-value {
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .model-tag {
            display: inline-block;
            background: var(--bg-active);
            color: var(--accent-light);
            padding: 3px 10px;
            border-radius: 6px;
            font-size: 12px;
            font-weight: 500;
          }
        }
      }
    }
  }
}

.config-management-card {
  padding: 20px 24px;

  .search-bar {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 18px;

    .search-input {
      max-width: 320px;

      ::v-deep .el-input__inner {
        background: var(--bg-input);
        border: 1px solid var(--border-primary);
        color: var(--text-primary);
        border-radius: 10px;
        height: 38px;

        &::placeholder { color: var(--text-hint); }

        &:focus {
          border-color: var(--accent);
          background: var(--bg-hover);
        }
      }
    }
  }
}

.config-table {
  background: transparent;

  ::v-deep .el-table__header th {
    background: var(--table-header-bg);
    color: var(--text-primary);
    font-weight: 600;
    border-bottom: 1px solid var(--border-primary);
    padding: 12px 0;
  }

  ::v-deep .el-table__body td {
    background: transparent;
    color: var(--text-primary);
    border-bottom: 1px solid var(--border-secondary);
    padding: 14px 0;
  }

  ::v-deep .el-table__body tr:hover > td {
    background: var(--bg-hover) !important;
  }

  ::v-deep .el-table__empty-block {
    background: transparent;
  }

  ::v-deep .el-table__empty-text {
    color: var(--text-secondary);
  }

  .config-info-cell {
    .config-title {
      font-weight: 600;
      color: var(--text-primary);
      margin-bottom: 6px;
      font-size: 14px;
    }

    .config-meta {
      display: flex;
      gap: 6px;
    }
  }

  .api-key-cell, .url-cell {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    color: var(--text-secondary);

    i {
      color: var(--accent-light);
      font-size: 14px;
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

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

::v-deep .el-dialog {
  background: var(--dialog-bg);
  backdrop-filter: var(--glass-blur);
  border: 1px solid var(--dialog-border);
  border-radius: 16px;

  .el-dialog__header {
    padding: 20px 24px 16px;
    border-bottom: 1px solid var(--glass-border);
  }

  .el-dialog__title { 
    color: var(--text-primary); 
    font-weight: 600;
    font-size: 18px;
  }

  .el-dialog__body {
    padding: 24px;
  }

  .el-dialog__footer {
    padding: 16px 24px 20px;
    border-top: 1px solid var(--glass-border);
  }
}

.config-form {
  .el-form-item {
    margin-bottom: 20px;

    ::v-deep .el-form-item__label {
      color: var(--text-primary);
      font-weight: 500;
      padding-bottom: 8px;
    }
  }

  ::v-deep .el-input__inner {
    background: var(--bg-input);
    border-color: var(--border-primary);
    color: var(--text-primary);
    border-radius: 10px;
    height: 42px;

    &::placeholder { color: var(--text-hint); }
    &:focus { border-color: var(--accent); }
  }

  ::v-deep .el-input__prefix {
    left: 12px;
    color: var(--accent-light);
  }

  .activate-switch {
    display: flex;
    align-items: center;
    gap: 12px;

    .switch-label {
      font-size: 14px;
      color: var(--text-primary);
    }
  }

  .switch-hint {
    margin-top: 8px;
    color: var(--text-secondary);
    font-size: 12px;
  }
}

::v-deep .el-pagination {
  .el-pager li {
    background: var(--pagination-bg);
    border: 1px solid var(--border-primary);
    color: var(--pagination-text);
    border-radius: 8px;

    &.is-active {
      background: var(--accent-gradient);
      border-color: var(--accent);
      color: white;
    }

    &:hover { color: var(--accent-light); }
  }

  .btn-prev,
  .btn-next {
    background: var(--pagination-bg);
    border: 1px solid var(--border-primary);
    color: var(--pagination-text);
    border-radius: 8px;

    &:hover { color: var(--accent-light); }
  }
}

::v-deep .el-tag { 
  border: none; 
  border-radius: 6px; 
}

::v-deep .el-button.is-circle {
  padding: 8px;
}

::v-deep .el-tooltip {
  display: inline-block;
}
</style>
