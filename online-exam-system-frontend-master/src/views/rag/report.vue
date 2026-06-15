<template>
  <div class="app-container">
    <div class="rag-report-container">
      <!-- 页面标题 -->
      <div class="page-header">
        <div class="header-content">
          <div class="title-section">
            <h1 class="page-title">
              <i class="el-icon-document-checked title-icon" />
              RAG智能解析中心
            </h1>
            <p class="page-description">管理您的学习资料，构建专属AI知识库</p>
          </div>
          <div class="action-section">
            <el-button
              type="primary"
              icon="el-icon-plus"
              :loading="loading"
              @click="showMaterialSelector"
            >
              选择资料解析
            </el-button>
            <el-button
              type="warning"
              icon="el-icon-refresh"
              :loading="loading"
              @click="refreshTasks"
            >
              刷新
            </el-button>
          </div>
        </div>
      </div>

      <!-- 统计卡片 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="stat-card pending-card" shadow="hover">
            <div class="stat-content">
              <i class="el-icon-time stat-icon" />
              <div class="stat-info">
                <div class="stat-value">{{ stats.pending }}</div>
                <div class="stat-label">等待处理</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="stat-card processing-card" shadow="hover">
            <div class="stat-content">
              <i class="el-icon-loading stat-icon" />
              <div class="stat-info">
                <div class="stat-value">{{ stats.processing }}</div>
                <div class="stat-label">处理中</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="stat-card completed-card" shadow="hover">
            <div class="stat-content">
              <i class="el-icon-success stat-icon" />
              <div class="stat-info">
                <div class="stat-value">{{ stats.completed }}</div>
                <div class="stat-label">已完成</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="stat-card failed-card" shadow="hover">
            <div class="stat-content">
              <i class="el-icon-error stat-icon" />
              <div class="stat-info">
                <div class="stat-value">{{ stats.failed }}</div>
                <div class="stat-label">失败</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 任务列表 -->
      <el-card class="task-list-card" shadow="hover">
        <div slot="header" class="card-header">
          <div class="header-title">
            <i class="el-icon-s-order header-icon" />
            <span>解析任务列表</span>
          </div>
          <div class="header-actions">
            <el-button
              type="danger"
              size="mini"
              icon="el-icon-delete"
              :disabled="selectedTasks.length === 0"
              @click="handleBatchDelete"
            >
              批量删除
            </el-button>
          </div>
        </div>

        <div v-loading="loading" class="task-list">
          <el-table
            :data="taskList"
            style="width: 100%"
            :row-class-name="getRowClassName"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="55" align="center" />

            <el-table-column label="文件名" min-width="250">
              <template slot-scope="{ row }">
                <div class="file-info">
                  <i :class="getFileIcon(row)" class="file-icon" />
                  <div class="file-details">
                    <span class="file-name" :title="row.materialName">{{ row.materialName || '未知文件' }}</span>
                    <div class="file-meta">
                      <span v-if="row.fileSize" class="file-size">{{ formatFileSize(row.fileSize) }}</span>
                      <span v-if="row.fileType" class="file-type">{{ row.fileType.toUpperCase() }}</span>
                    </div>
                  </div>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="状态" width="120" align="center">
              <template slot-scope="{ row }">
                <el-tag :type="getStatusType(row.taskStatus)" size="medium" effect="dark">
                  {{ getStatusText(row.taskStatus) }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column label="进度" width="200" align="center">
              <template slot-scope="{ row }">
                <div class="progress-wrapper">
                  <el-progress
                    :percentage="row.progress || 0"
                    :status="getProgressStatus(row.taskStatus)"
                    :stroke-width="18"
                    :show-text="false"
                  />
                  <div class="progress-details">
                    <span class="progress-text">{{ row.progress || 0 }}%</span>
                    <span v-if="row.taskStatus === 1" class="progress-time">
                      {{ getRemainingTime(row) }}
                    </span>
                  </div>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="页数/分块" width="150" align="center">
              <template slot-scope="{ row }">
                <div class="pages-info">
                  <span v-if="row.totalPages">
                    {{ row.processedPages || 0 }} / {{ row.totalPages }} 页
                  </span>
                  <span v-else-if="row.totalChunks">
                    {{ row.processedChunks || 0 }} / {{ row.totalChunks }} 块
                  </span>
                  <span v-else>-</span>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="开始时间" width="180" align="center">
              <template slot-scope="{ row }">
                {{ formatTime(row.startTime || row.createdTime) }}
              </template>
            </el-table-column>

            <el-table-column label="结束时间" width="180" align="center">
              <template slot-scope="{ row }">
                {{ row.endTime ? formatTime(row.endTime) : '-' }}
              </template>
            </el-table-column>

            <el-table-column label="操作" width="200" align="center" fixed="right">
              <template slot-scope="{ row }">
                <div class="action-buttons">
                  <el-button
                    v-if="row.taskStatus === 3"
                    type="warning"
                    size="mini"
                    icon="el-icon-refresh"
                    @click="handleRetry(row)"
                  >
                    重新解析
                  </el-button>
                  <el-button
                    v-else-if="row.taskStatus === 2"
                    type="success"
                    size="mini"
                    icon="el-icon-view"
                    @click="handleViewDetail(row)"
                  >
                    查看详情
                  </el-button>
                  <el-button
                    v-else
                    type="info"
                    size="mini"
                    disabled
                  >
                    处理中...
                  </el-button>
                  <el-button
                    type="danger"
                    size="mini"
                    icon="el-icon-delete"
                    :loading="row.deleting"
                    @click="handleDelete(row)"
                  >
                    删除
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <!-- 空状态 -->
          <div v-if="!loading && taskList.length === 0" class="empty-state">
            <i class="el-icon-document empty-icon" />
            <p class="empty-text">暂无解析任务</p>
            <p class="empty-desc">点击"选择资料解析"按钮开始处理您的学习资料</p>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 资料选择对话框 -->
    <el-dialog
      title="选择资料进行解析"
      :visible.sync="materialDialogVisible"
      width="80%"
      :close-on-click-modal="false"
    >
      <div class="material-selector">
        <!-- 资料列表 -->
        <el-table
          :data="materialList"
          style="width: 100%"
          @selection-change="handleMaterialSelectionChange"
        >
          <el-table-column type="selection" width="55" align="center" />

          <el-table-column label="文件名" min-width="300">
            <template slot-scope="{ row }">
              <div class="file-info">
                <i :class="getFileTypeIcon(row.fileType)" class="file-icon" />
                <div class="file-details">
                  <span class="file-name">{{ row.originalName || row.displayName }}</span>
                  <div class="file-meta">
                    <span class="file-type">{{ row.fileType.toUpperCase() }}</span>
                    <span class="file-size">{{ formatFileSize(row.fileSize) }}</span>
                    <span class="upload-time">{{ formatTime(row.uploadTime) }}</span>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="状态" width="120" align="center">
            <template slot-scope="{ row }">
              <el-tag
                :type="row.isParsed ? 'success' : 'info'"
                size="small"
              >
                {{ row.isParsed ? '已解析' : '未解析' }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="120" align="center">
            <template slot-scope="{ row }">
              <el-button
                v-if="!row.isParsed"
                type="primary"
                size="mini"
                @click="handleSingleParse(row)"
              >
                解析
              </el-button>
              <el-button
                v-else
                type="info"
                size="mini"
                disabled
              >
                已解析
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 空状态 -->
        <div v-if="materialList.length === 0" class="empty-state">
          <i class="el-icon-folder-opened empty-icon" />
          <p class="empty-text">暂无学习资料</p>
          <p class="empty-desc">请先在学习资料页面上传文件</p>
        </div>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="materialDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :disabled="selectedMaterials.length === 0"
          :loading="batchParsing"
          @click="handleBatchParse"
        >
          解析选中资料 ({{ selectedMaterials.length }})
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getUserTasks, parseMaterial, deleteTask, deleteTasks, getAllMaterials } from '@/api/rag'

export default {
  name: 'RagReport',
  data() {
    return {
      loading: false,
      taskList: [],
      materialList: [],
      selectedTasks: [],
      selectedMaterials: [],
      materialDialogVisible: false,
      batchParsing: false,
      autoRefreshTimer: null,
      stats: {
        pending: 0,
        processing: 0,
        completed: 0,
        failed: 0
      }
    }
  },
  mounted() {
    this.loadTasks()
    // 启动自动刷新(每3秒，更频繁的进度更新)
    this.startAutoRefresh()
  },
  beforeDestroy() {
    // 清除定时器
    if (this.autoRefreshTimer) {
      clearInterval(this.autoRefreshTimer)
    }
  },
  methods: {
    // 加载任务列表
    async loadTasks() {
      this.loading = true
      try {
        const res = await getUserTasks()
        if (res.code === 1 && res.data) {
          this.taskList = res.data.map(task => ({
            ...task,
            // 根据文件大小估算处理时间
            estimatedTime: this.estimateProcessingTime(task.fileSize)
          }))
          this.calculateStats()
        } else {
          this.$message.error('获取任务列表失败')
        }
      } catch (error) {
        console.error('获取任务列表失败:', error)
        this.$message.error('获取任务列表失败')
      } finally {
        this.loading = false
      }
    },

    // 加载资料列表
    async loadMaterials() {
      try {
        const res = await getAllMaterials()
        if (res.code === 1 && res.data) {
          this.materialList = res.data.map(material => ({
            ...material,
            // 使用后端返回的解析状态
            isParsed: material.isParsed || false
          }))
        } else {
          this.$message.error('获取资料列表失败')
        }
      } catch (error) {
        console.error('加载资料列表失败:', error)
        this.$message.error('加载资料列表失败')
      }
    },

    // 显示资料选择对话框
    async showMaterialSelector() {
      await this.loadMaterials()
      this.materialDialogVisible = true
    },

    // 估算处理时间（基于OCR特性）
    estimateProcessingTime(fileSize) {
      // OCR处理时间估算：
      // - 100MB以内文件：个人电脑上约5-15分钟
      // - 基于文件大小线性估算
      const baseTime = 5 * 60 // 5分钟基础时间
      const sizeFactor = fileSize / (100 * 1024 * 1024) // 相对于100MB的比例
      return Math.round(baseTime * Math.max(1, sizeFactor)) // 最少5分钟
    },

    // 获取剩余时间
    getRemainingTime(row) {
      if (!row.startTime || !row.progress || row.progress === 0) return '估算中...'

      const elapsed = Date.now() - new Date(row.startTime).getTime()
      const totalTime = (elapsed / row.progress) * 100
      const remaining = Math.max(0, totalTime - elapsed)

      if (remaining < 60000) return '剩余<1分钟'
      if (remaining < 3600000) return `剩余${Math.round(remaining / 60000)}分钟`
      return `剩余${Math.round(remaining / 3600000)}小时`
    },

    // 计算统计数据
    calculateStats() {
      this.stats = {
        pending: this.taskList.filter(t => t.taskStatus === 0).length,
        processing: this.taskList.filter(t => t.taskStatus === 1).length,
        completed: this.taskList.filter(t => t.taskStatus === 2).length,
        failed: this.taskList.filter(t => t.taskStatus === 3).length
      }
    },

    // 刷新任务
    refreshTasks() {
      this.loadTasks()
    },

    // 启动自动刷新
    startAutoRefresh() {
      this.autoRefreshTimer = setInterval(() => {
        // 只刷新进行中的任务
        const hasProcessing = this.taskList.some(t => t.taskStatus === 1)
        if (hasProcessing) {
          this.loadTasks()
        }
      }, 3000) // 每3秒刷新一次，更频繁的进度更新
    },

    // 任务选择变化
    handleSelectionChange(selection) {
      this.selectedTasks = selection
    },

    // 资料选择变化
    handleMaterialSelectionChange(selection) {
      this.selectedMaterials = selection
    },

    // 重新解析
    async handleRetry(row) {
      try {
        const res = await parseMaterial(row.studyMaterialId)
        if (res.code === 1) {
          this.$message.success('解析任务已提交')
          this.loadTasks()
        } else {
          this.$message.error(res.msg || '提交失败')
        }
      } catch (error) {
        console.error('重新解析失败:', error)
        this.$message.error('提交失败')
      }
    },

    // 单文件解析
    async handleSingleParse(row) {
      try {
        const res = await parseMaterial(row.id)
        if (res.code === 1) {
          this.$message.success('解析任务已提交')
          this.materialDialogVisible = false
          this.loadTasks()
        } else {
          this.$message.error(res.msg || '提交失败')
        }
      } catch (error) {
        console.error('解析失败:', error)
        this.$message.error('提交失败')
      }
    },

    // 批量解析
    async handleBatchParse() {
      if (this.selectedMaterials.length === 0) {
        this.$message.warning('请选择要解析的资料')
        return
      }

      this.batchParsing = true
      try {
        let successCount = 0
        for (const material of this.selectedMaterials) {
          const res = await parseMaterial(material.id)
          if (res.code === 1) {
            successCount++
          }
        }

        this.$message.success(`成功提交 ${successCount} 个解析任务`)
        this.materialDialogVisible = false
        this.loadTasks()
      } catch (error) {
        console.error('批量解析失败:', error)
        this.$message.error('批量解析失败')
      } finally {
        this.batchParsing = false
      }
    },

    // 删除任务
    async handleDelete(row) {
      try {
        await this.$confirm('确定要删除该解析任务吗？删除后相关数据将无法恢复。', '确认删除', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        row.deleting = true

        // 检查任务ID是否有效
        if (!row.id) {
          this.$message.error('任务ID无效，无法删除')
          return
        }

        const res = await deleteTask(row.id)
        if (res.code === 1) {
          this.$message.success('删除成功')
          this.loadTasks()
        } else {
          // 显示具体的错误信息
          this.$message.error(res.msg || `删除失败，错误码: ${res.code}`)
          console.error('删除失败响应:', res)
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除失败错误:', error)

          // 根据错误类型显示不同的提示信息
          if (error.response && error.response.status === 401) {
            this.$message.error('请先登录系统')
          } else if (error.response && error.response.status === 403) {
            this.$message.error('没有权限删除此任务')
          } else if (error.response && error.response.status === 404) {
            this.$message.error('任务不存在或已被删除')
          } else {
            this.$message.error('删除失败，请检查网络连接')
          }
        }
      } finally {
        row.deleting = false
      }
    },

    // 批量删除
    async handleBatchDelete() {
      if (this.selectedTasks.length === 0) {
        this.$message.warning('请选择要删除的任务')
        return
      }

      try {
        await this.$confirm(`确定要删除选中的 ${this.selectedTasks.length} 个任务吗？`, '确认删除', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        // 检查任务ID是否有效
        const taskIds = this.selectedTasks.map(task => task.id).filter(id => id)
        if (taskIds.length === 0) {
          this.$message.error('没有有效的任务ID，无法删除')
          return
        }

        const res = await deleteTasks(taskIds)
        if (res.code === 1) {
          this.$message.success('批量删除成功')
          this.loadTasks()
          this.selectedTasks = []
        } else {
          // 显示具体的错误信息
          this.$message.error(res.msg || `批量删除失败，错误码: ${res.code}`)
          console.error('批量删除失败响应:', res)
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('批量删除失败错误:', error)

          // 根据错误类型显示不同的提示信息
          if (error.response && error.response.status === 401) {
            this.$message.error('请先登录系统')
          } else if (error.response && error.response.status === 403) {
            this.$message.error('没有权限删除这些任务')
          } else if (error.response && error.response.status === 404) {
            this.$message.error('任务不存在或已被删除')
          } else {
            this.$message.error('批量删除失败，请检查网络连接')
          }
        }
      }
    },

    // 查看详情
    handleViewDetail(row) {
      const detailHtml = `
        <div class="task-detail">
          <p><strong>文件名：</strong>${row.materialName}</p>
          <p><strong>文件大小：</strong>${this.formatFileSize(row.fileSize)}</p>
          <p><strong>总页数：</strong>${row.totalPages || '-'}</p>
          <p><strong>总分块数：</strong>${row.totalChunks || '-'}</p>
          <p><strong>已处理页数：</strong>${row.processedPages || 0}</p>
          <p><strong>OCR准确率：</strong>${row.accuracy ? row.accuracy + '%' : '计算中...'}</p>
          ${row.errorMessage ? `<p><strong>错误信息：</strong><span style="color: #f56c6c;">${row.errorMessage}</span></p>` : ''}
          ${row.estimatedTime ? `<p><strong>预估总时间：</strong>${Math.round(row.estimatedTime / 60)}分钟</p>` : ''}
        </div>
      `

      this.$alert(detailHtml, '任务详情', {
        dangerouslyUseHTMLString: true,
        confirmButtonText: '关闭',
        customClass: 'task-detail-dialog'
      })
    },

    // 获取状态类型
    getStatusType(status) {
      const types = {
        0: 'info',
        1: 'warning',
        2: 'success',
        3: 'danger'
      }
      return types[status] || 'info'
    },

    // 获取状态文本
    getStatusText(status) {
      const texts = {
        0: '等待处理',
        1: 'OCR处理中',
        2: '已完成',
        3: '失败'
      }
      return texts[status] || '未知'
    },

    // 获取进度条状态
    getProgressStatus(status) {
      if (status === 2) return 'success'
      if (status === 3) return 'exception'
      return null
    },

    // 获取行类名
    getRowClassName({ row }) {
      if (row.taskStatus === 2) return 'completed-row'
      if (row.taskStatus === 3) return 'failed-row'
      if (row.taskStatus === 1) return 'processing-row'
      return ''
    },

    // 获取文件图标
    getFileIcon(row) {
      if (!row.materialName) return 'el-icon-document'
      const ext = row.materialName.split('.').pop().toLowerCase()
      const icons = {
        pdf: 'el-icon-document',
        doc: 'el-icon-document',
        docx: 'el-icon-document',
        xls: 'el-icon-s-grid',
        xlsx: 'el-icon-s-grid',
        txt: 'el-icon-tickets'
      }
      return icons[ext] || 'el-icon-document'
    },

    // 获取文件类型图标
    getFileTypeIcon(fileType) {
      const icons = {
        pdf: 'el-icon-document',
        doc: 'el-icon-document',
        docx: 'el-icon-document',
        xls: 'el-icon-s-grid',
        xlsx: 'el-icon-s-grid',
        txt: 'el-icon-tickets',
        jpg: 'el-icon-picture',
        png: 'el-icon-picture',
        jpeg: 'el-icon-picture'
      }
      return icons[fileType] || 'el-icon-document'
    },

    // 格式化文件大小
    formatFileSize(bytes) {
      if (!bytes) return '-'
      const sizes = ['B', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(1024))
      return Math.round(bytes / Math.pow(1024, i) * 100) / 100 + ' ' + sizes[i]
    },

    // 格式化时间
    formatTime(time) {
      if (!time) return '-'
      const date = new Date(time)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}`
    }
  }
}
</script>

<style scoped>
.rag-report-container {
  padding: 24px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  min-height: calc(100vh - 84px);
}

/* 页面标题 */
.page-header {
  margin-bottom: 24px;
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-section .page-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 16px;
  color: #1e293b;
}

.title-icon {
  font-size: 32px;
  color: #3b82f6;
}

.page-description {
  margin: 0;
  font-size: 16px;
  color: #64748b;
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
  border: none;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
}

.stat-icon {
  font-size: 36px;
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.pending-card .stat-icon { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
.processing-card .stat-icon { background: linear-gradient(135deg, #3b82f6, #60a5fa); }
.completed-card .stat-icon { background: linear-gradient(135deg, #10b981, #34d399); }
.failed-card .stat-icon { background: linear-gradient(135deg, #ef4444, #f87171); }

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #64748b;
  margin-top: 4px;
  font-weight: 500;
}

/* 任务列表 */
.task-list-card {
  border-radius: 16px;
  border: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.header-icon {
  font-size: 20px;
  color: #3b82f6;
}

/* 文件信息 */
.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.file-icon {
  font-size: 24px;
  color: #3b82f6;
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: rgba(59, 130, 246, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
}

.file-details {
  flex: 1;
  min-width: 0;
}

.file-name {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
  word-break: break-all;
}

.file-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #64748b;
}

.file-type {
  background: #f1f5f9;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 600;
}

/* 进度条 */
.progress-wrapper {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.progress-details {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #64748b;
}

.progress-time {
  color: #f59e0b;
  font-weight: 600;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

/* 空状态 */
.empty-state {
  padding: 60px 24px;
  text-align: center;
}

.empty-icon {
  font-size: 64px;
  color: #cbd5e1;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 18px;
  font-weight: 600;
  color: #475569;
  margin: 0 0 8px 0;
}

.empty-desc {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

/* 行样式 */
.completed-row {
  background-color: rgba(16, 185, 129, 0.05) !important;
}

.failed-row {
  background-color: rgba(239, 68, 68, 0.05) !important;
}

.processing-row {
  background-color: rgba(59, 130, 246, 0.05) !important;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.8; }
  100% { opacity: 1; }
}

/* 资料选择器 */
.material-selector {
  max-height: 400px;
  overflow-y: auto;
}

.dialog-footer {
  padding: 20px 24px;
  border-top: 1px solid #f1f5f9;
}
</style>
