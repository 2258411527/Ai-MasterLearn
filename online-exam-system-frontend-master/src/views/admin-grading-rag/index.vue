<template>
  <div class="app-container">
    <div class="grading-rag-container">
      <div class="page-header">
        <div class="header-content">
          <div class="title-section">
            <h1 class="page-title">
              <i class="el-icon-s-check title-icon" />
              阅卷解析中心
            </h1>
            <p class="page-description">配置AI阅卷评分标准与参考资料，对所有教师的AI阅卷生效</p>
          </div>
          <div class="action-section">
            <el-button type="primary" icon="el-icon-plus" :loading="loading" @click="showMaterialSelector">选择资料解析</el-button>
            <el-button type="warning" icon="el-icon-refresh" :loading="loading" @click="refreshTasks">刷新</el-button>
            <el-button type="danger" icon="el-icon-delete" @click="handleClearAll">清空知识库</el-button>
          </div>
        </div>
      </div>

      <el-row :gutter="20" class="stats-row">
        <el-col :xs="24" :sm="12" :md="8">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <i class="el-icon-document stat-icon" style="background: linear-gradient(135deg, #3b82f6, #60a5fa);" />
              <div class="stat-info">
                <div class="stat-value">{{ stats.totalChunks }}</div>
                <div class="stat-label">知识块数量</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <i class="el-icon-folder stat-icon" style="background: linear-gradient(135deg, #10b981, #34d399);" />
              <div class="stat-info">
                <div class="stat-value">{{ stats.totalMaterials }}</div>
                <div class="stat-label">参考材料数量</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <i class="el-icon-circle-check stat-icon" style="background: linear-gradient(135deg, #f59e0b, #fbbf24);" />
              <div class="stat-info">
                <div class="stat-value">{{ stats.status }}</div>
                <div class="stat-label">知识库状态</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-card class="task-list-card" shadow="hover">
        <div slot="header" class="card-header">
          <div class="header-title">
            <i class="el-icon-s-order header-icon" />
            <span>解析任务列表</span>
          </div>
        </div>

        <div v-loading="loading" class="task-list">
          <el-table :data="taskList" style="width: 100%">
            <el-table-column label="文件名" min-width="250">
              <template slot-scope="{ row }">
                <div class="file-info">
                  <i class="el-icon-document file-icon" />
                  <div class="file-details">
                    <span class="file-name">{{ row.materialName || '未知文件' }}</span>
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
                <el-progress :percentage="row.progress || 0" :status="getProgressStatus(row.taskStatus)" :stroke-width="18" />
              </template>
            </el-table-column>

            <el-table-column label="开始时间" width="180" align="center">
              <template slot-scope="{ row }">
                {{ formatTime(row.startTime || row.createdTime) }}
              </template>
            </el-table-column>

            <el-table-column label="操作" width="150" align="center">
              <template slot-scope="{ row }">
                <el-button v-if="row.taskStatus === 3" type="warning" size="mini" icon="el-icon-refresh" @click="handleRetry(row)">重新解析</el-button>
                <span v-else-if="row.taskStatus === 2" style="color: #67c23a;">已完成</span>
                <span v-else style="color: #909399;">处理中...</span>
              </template>
            </el-table-column>
          </el-table>

          <div v-if="!loading && taskList.length === 0" class="empty-state">
            <i class="el-icon-document empty-icon" />
            <p class="empty-text">暂无解析任务</p>
            <p class="empty-desc">点击"选择资料解析"按钮开始配置阅卷评分标准</p>
          </div>
        </div>
      </el-card>

      <el-card class="test-card" shadow="hover" style="margin-top: 20px;">
        <div slot="header" class="card-header">
          <div class="header-title">
            <i class="el-icon-search header-icon" />
            <span>检索测试</span>
          </div>
        </div>
        <div class="test-section">
          <el-input v-model="testQuestion" placeholder="输入题目内容测试RAG检索效果" style="width: 70%; margin-right: 10px;" @keyup.enter.native="handleTestSearch" />
          <el-button type="primary" :loading="testLoading" @click="handleTestSearch">测试检索</el-button>
          <div v-if="testResults.length > 0" class="test-results">
            <div v-for="(result, index) in testResults" :key="index" class="test-result-item">
              <div class="result-header">参考 {{ index + 1 }}</div>
              <div class="result-content">{{ result }}</div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <el-dialog title="选择阅卷参考材料" :visible.sync="materialDialogVisible" width="80%" :close-on-click-modal="false">
      <el-table :data="materialList" style="width: 100%" @selection-change="handleMaterialSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="文件名" min-width="300">
          <template slot-scope="{ row }">
            <span>{{ row.originalName || row.displayName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100" align="center">
          <template slot-scope="{ row }">
            <el-tag size="small">{{ (row.fileType || '').toUpperCase() }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template slot-scope="{ row }">
            <el-tag :type="row.isParsed ? 'success' : 'info'" size="small">{{ row.isParsed ? '已解析' : '未解析' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template slot-scope="{ row }">
            <el-button v-if="!row.isParsed" type="primary" size="mini" @click="handleSingleParse(row)">解析</el-button>
            <el-button v-else type="info" size="mini" disabled>已解析</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="materialList.length === 0" class="empty-state">
        <i class="el-icon-folder-opened empty-icon" />
        <p class="empty-text">暂无学习资料</p>
        <p class="empty-desc">请先在学习资料页面上传文件</p>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="materialDialogVisible = false">取消</el-button>
        <el-button type="primary" :disabled="selectedMaterials.length === 0" :loading="batchParsing" @click="handleBatchParse">解析选中资料 ({{ selectedMaterials.length }})</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { parseAdminGradingMaterial, getAdminGradingStats, clearAdminGradingKnowledge, testAdminGradingSearch, getAdminGradingMaterials, getAdminGradingTasks } from '@/api/admin-grading-rag'

export default {
  name: 'AdminGradingRag',
  data() {
    return {
      loading: false,
      taskList: [],
      materialList: [],
      selectedMaterials: [],
      materialDialogVisible: false,
      batchParsing: false,
      autoRefreshTimer: null,
      stats: { totalChunks: 0, totalMaterials: 0, status: '未启用' },
      testQuestion: '',
      testResults: [],
      testLoading: false
    }
  },
  mounted() {
    this.loadTasks()
    this.loadStats()
    this.startAutoRefresh()
  },
  beforeDestroy() {
    if (this.autoRefreshTimer) clearInterval(this.autoRefreshTimer)
  },
  methods: {
    async loadStats() {
      try {
        const res = await getAdminGradingStats()
        if (res.code === 1 && res.data) this.stats = res.data
      } catch (e) { console.error('加载统计失败', e) }
    },
    async loadTasks() {
      this.loading = true
      try {
        const res = await getAdminGradingTasks()
        if (res.code === 1 && res.data) this.taskList = res.data
      } catch (e) { console.error('加载任务失败', e) }
      finally { this.loading = false }
    },
    async loadMaterials() {
      try {
        const res = await getAdminGradingMaterials()
        if (res.code === 1 && res.data) this.materialList = res.data
      } catch (e) { console.error('加载资料失败', e) }
    },
    async showMaterialSelector() {
      await this.loadMaterials()
      this.materialDialogVisible = true
    },
    refreshTasks() {
      this.loadTasks()
      this.loadStats()
    },
    startAutoRefresh() {
      this.autoRefreshTimer = setInterval(() => {
        if (this.taskList.some(t => t.taskStatus === 1)) this.loadTasks()
      }, 3000)
    },
    handleMaterialSelectionChange(selection) { this.selectedMaterials = selection },
    async handleSingleParse(row) {
      try {
        const res = await parseAdminGradingMaterial(row.id)
        if (res.code === 1) {
          this.$message.success('解析任务已提交')
          this.materialDialogVisible = false
          this.loadTasks()
          this.loadStats()
        } else { this.$message.error(res.msg || '提交失败') }
      } catch (e) { this.$message.error('提交失败') }
    },
    async handleBatchParse() {
      if (this.selectedMaterials.length === 0) return
      this.batchParsing = true
      try {
        let successCount = 0
        for (const material of this.selectedMaterials) {
          const res = await parseAdminGradingMaterial(material.id)
          if (res.code === 1) successCount++
        }
        this.$message.success(`成功提交 ${successCount} 个解析任务`)
        this.materialDialogVisible = false
        this.loadTasks()
        this.loadStats()
      } catch (e) { this.$message.error('批量解析失败') }
      finally { this.batchParsing = false }
    },
    async handleRetry(row) {
      try {
        const res = await parseAdminGradingMaterial(row.studyMaterialId)
        if (res.code === 1) { this.$message.success('解析任务已提交'); this.loadTasks() }
        else this.$message.error(res.msg || '提交失败')
      } catch (e) { this.$message.error('提交失败') }
    },
    async handleClearAll() {
      try {
        await this.$confirm('确定要清空所有阅卷参考知识库吗？此操作不可恢复。', '确认清空', { type: 'warning' })
        const res = await clearAdminGradingKnowledge()
        if (res.code === 1) { this.$message.success(res.msg); this.loadStats() }
        else this.$message.error(res.msg || '清空失败')
      } catch (e) { if (e !== 'cancel') this.$message.error('清空失败') }
    },
    async handleTestSearch() {
      if (!this.testQuestion.trim()) { this.$message.warning('请输入测试题目'); return }
      this.testLoading = true
      try {
        const res = await testAdminGradingSearch(this.testQuestion)
        if (res.code === 1 && res.data) { this.testResults = res.data.searchResults || [] }
        else this.testResults = []
      } catch (e) { this.$message.error('检索测试失败') }
      finally { this.testLoading = false }
    },
    getStatusType(status) { return { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }[status] || 'info' },
    getStatusText(status) { return { 0: '等待处理', 1: '处理中', 2: '已完成', 3: '失败' }[status] || '未知' },
    getProgressStatus(status) { return status === 2 ? 'success' : status === 3 ? 'exception' : null },
    formatTime(time) {
      if (!time) return '-'
      const d = new Date(time)
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
    }
  }
}
</script>

<style scoped>
.grading-rag-container { padding: 24px; background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%); min-height: calc(100vh - 84px); }
.page-header { margin-bottom: 24px; background: white; border-radius: 16px; padding: 24px; box-shadow: 0 4px 16px rgba(0,0,0,0.08); }
.header-content { display: flex; justify-content: space-between; align-items: center; }
.title-section .page-title { font-size: 28px; font-weight: 700; margin: 0 0 8px 0; display: flex; align-items: center; gap: 16px; color: #1e293b; }
.title-icon { font-size: 32px; color: #3b82f6; }
.page-description { margin: 0; font-size: 16px; color: #64748b; }
.stats-row { margin-bottom: 24px; }
.stat-card { border-radius: 12px; border: none; transition: all 0.3s ease; }
.stat-card:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(0,0,0,0.12); }
.stat-content { display: flex; align-items: center; gap: 16px; padding: 16px; }
.stat-icon { font-size: 36px; width: 60px; height: 60px; border-radius: 12px; display: flex; align-items: center; justify-content: center; color: white; }
.stat-info { flex: 1; }
.stat-value { font-size: 32px; font-weight: 700; color: #1e293b; line-height: 1; }
.stat-label { font-size: 14px; color: #64748b; margin-top: 4px; font-weight: 500; }
.task-list-card { border-radius: 16px; border: none; }
.card-header { display: flex; justify-content: space-between; align-items: center; padding: 20px 24px; border-bottom: 1px solid #f1f5f9; }
.header-title { display: flex; align-items: center; gap: 12px; font-size: 18px; font-weight: 600; color: #1e293b; }
.header-icon { font-size: 20px; color: #3b82f6; }
.file-info { display: flex; align-items: center; gap: 12px; }
.file-icon { font-size: 24px; color: #3b82f6; }
.file-details { flex: 1; min-width: 0; }
.file-name { display: block; font-size: 14px; font-weight: 600; color: #1e293b; word-break: break-all; }
.empty-state { padding: 60px 24px; text-align: center; }
.empty-icon { font-size: 64px; color: #cbd5e1; margin-bottom: 16px; }
.empty-text { font-size: 18px; font-weight: 600; color: #475569; margin: 0 0 8px 0; }
.empty-desc { font-size: 14px; color: #64748b; margin: 0; }
.test-card { border-radius: 16px; border: none; }
.test-section { padding: 10px; }
.test-results { margin-top: 20px; }
.test-result-item { background: #f8fafc; border-radius: 8px; padding: 16px; margin-bottom: 12px; border-left: 4px solid #3b82f6; }
.result-header { font-weight: 600; color: #3b82f6; margin-bottom: 8px; }
.result-content { color: #475569; font-size: 14px; line-height: 1.6; white-space: pre-wrap; }
</style>
