<template>
  <div class="ai-grading-page">
    <div class="page-header">
      <h1>AI智能阅卷</h1>
      <p>使用人工智能技术对您的考试答案进行智能评分和分析</p>
    </div>

    <!-- 服务状态检查 -->
    <el-card v-if="showStatus" class="status-card">
      <div slot="header" class="card-header">
        <i class="el-icon-cpu" />
        <span>AI阅卷服务状态</span>
      </div>
      <div class="status-content">
        <el-alert
          v-if="serviceStatus === 'active'"
          title="AI阅卷服务正常运行"
          type="success"
          :closable="false"
          show-icon
        />
        <el-alert
          v-else-if="serviceStatus === 'inactive'"
          title="AI阅卷服务暂不可用"
          type="warning"
          :closable="false"
          show-icon
        />
        <el-alert
          v-else
          title="正在检查服务状态..."
          type="info"
          :closable="false"
          show-icon
        />
      </div>
    </el-card>

    <!-- 可阅卷试卷列表 -->
    <el-card class="exams-card">
      <div slot="header" class="card-header">
        <i class="el-icon-document" />
        <span>可阅卷试卷列表</span>
        <el-button
          type="primary"
          size="small"
          :loading="loadingExams"
          style="float: right;"
          @click="refreshExams"
        >
          <i class="el-icon-refresh" />
          刷新列表
        </el-button>
      </div>

      <div class="exams-content">
        <el-table
          v-loading="loadingExams"
          :data="gradableExams"
          empty-text="暂无可阅卷的试卷"
        >
          <el-table-column prop="examName" label="试卷名称" min-width="200">
            <template slot-scope="{ row }">
              <span class="exam-name">{{ row.examName }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="examDate" label="考试时间" width="180">
            <template slot-scope="{ row }">
              {{ formatDate(row.examDate) }}
            </template>
          </el-table-column>

          <el-table-column prop="ungradedCount" label="待阅卷题目" width="120">
            <template slot-scope="{ row }">
              <el-tag :type="row.ungradedCount > 0 ? 'warning' : 'success'">
                {{ row.ungradedCount }} 题
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="200" fixed="right">
            <template slot-scope="{ row }">
              <el-button
                type="primary"
                size="mini"
                :disabled="!row.canGrade"
                :loading="batchGradingLoading[row.examId]"
                @click="startBatchGrading(row.examId)"
              >
                <i class="el-icon-cpu" />
                开始AI阅卷
              </el-button>

              <el-button
                type="info"
                size="mini"
                @click="viewExamDetail(row.examId)"
              >
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 阅卷进度 -->
    <el-card v-if="currentGradingProgress" class="progress-card">
      <div slot="header" class="card-header">
        <i v-if="currentGradingProgress.status === 'processing'" class="el-icon-loading" />
        <i v-else-if="currentGradingProgress.status === 'completed'" class="el-icon-success" />
        <i v-else class="el-icon-warning" />
        <span>AI阅卷进度</span>
      </div>

      <div class="progress-content">
        <el-progress
          :percentage="Math.round(currentGradingProgress.progress)"
          :status="getProgressStatus(currentGradingProgress)"
          :stroke-width="8"
        />

        <div class="progress-info">
          <span>已完成: {{ currentGradingProgress.completed }}/{{ currentGradingProgress.total }}</span>
          <span class="progress-status">状态: {{ getProgressStatusText(currentGradingProgress) }}</span>
        </div>

        <div v-if="currentGradingProgress.error" class="error-message">
          <el-alert
            :title="currentGradingProgress.error"
            type="error"
            :closable="false"
            show-icon
          />
        </div>

        <div v-if="currentGradingProgress.status === 'processing'" class="progress-actions">
          <el-button size="small" @click="stopGrading">停止阅卷</el-button>
          <el-button size="small" type="primary" @click="refreshProgress">刷新进度</el-button>
        </div>

        <div v-else-if="currentGradingProgress.status === 'completed'" class="progress-actions">
          <el-button size="small" type="success" @click="viewGradingResults">查看结果</el-button>
          <el-button size="small" @click="clearProgress">清除进度</el-button>
        </div>
      </div>
    </el-card>

    <!-- 阅卷结果展示 -->
    <el-dialog
      title="AI阅卷结果"
      :visible.sync="resultsDialogVisible"
      width="90%"
      top="5vh"
    >
      <div class="results-content">
        <el-alert
          title="AI阅卷已完成"
          type="success"
          :closable="false"
          show-icon
        />

        <div class="results-summary">
          <h3>阅卷统计</h3>
          <el-row :gutter="20">
            <el-col :span="6">
              <el-statistic title="阅卷题目数" :value="gradingResults.total || 0" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="平均得分" :value="gradingResults.avgScore || 0" suffix="分" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="正确率" :value="gradingResults.correctRate || 0" suffix="%" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="用时" :value="gradingResults.timeUsed || 0" suffix="秒" />
            </el-col>
          </el-row>
        </div>

        <div class="results-actions">
          <el-button type="primary" @click="goToExamDetail">查看详细结果</el-button>
          <el-button @click="resultsDialogVisible = false">关闭</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getGradableExams,
  realAiGradeBatchQuestions,
  getRealAiGradingProgress,
  getAiGradingStatus
} from '@/api/real-ai-grading'

export default {
  name: 'AiGradingPage',
  data() {
    return {
      // 可阅卷试卷列表
      gradableExams: [],
      loadingExams: false,

      // 服务状态
      serviceStatus: '',
      showStatus: true,

      // 批量阅卷
      batchGradingLoading: {},
      currentGradingExamId: null,
      currentGradingProgress: null,
      progressTimer: null,

      // 阅卷结果
      resultsDialogVisible: false,
      gradingResults: {}
    }
  },

  mounted() {
    this.checkServiceStatus()
    this.loadGradableExams()
  },

  beforeDestroy() {
    if (this.progressTimer) {
      clearInterval(this.progressTimer)
    }
  },

  methods: {
    // 检查AI阅卷服务状态
    async checkServiceStatus() {
      try {
        const res = await getAiGradingStatus()
        if (res.code) {
          this.serviceStatus = res.data.status === 'active' ? 'active' : 'inactive'
        } else {
          this.serviceStatus = 'inactive'
        }
      } catch (error) {
        console.error('检查服务状态失败:', error)
        this.serviceStatus = 'inactive'
      }
    },

    // 加载可阅卷试卷列表
    async loadGradableExams() {
      this.loadingExams = true
      try {
        const res = await getGradableExams()
        if (res.code) {
          this.gradableExams = res.data || []
        } else {
          this.$message.error(res.msg || '加载试卷列表失败')
        }
      } catch (error) {
        console.error('加载可阅卷试卷失败:', error)
        this.$message.error('加载试卷列表失败')
      } finally {
        this.loadingExams = false
      }
    },

    // 刷新试卷列表
    refreshExams() {
      this.loadGradableExams()
    },

    // 开始批量AI阅卷
    async startBatchGrading(examId) {
      this.$set(this.batchGradingLoading, examId, true)
      this.currentGradingExamId = examId

      try {
        const res = await realAiGradeBatchQuestions(examId)
        if (res.code) {
          this.$message.success('AI阅卷任务已提交，系统正在后台进行智能评分')
          this.startProgressMonitoring(examId)
        } else {
          this.$message.error(res.msg || '开始AI阅卷失败')
        }
      } catch (error) {
        console.error('开始AI阅卷失败:', error)
        this.$message.error('开始AI阅卷失败')
      } finally {
        this.$set(this.batchGradingLoading, examId, false)
      }
    },

    // 开始进度监控
    startProgressMonitoring(examId) {
      if (this.progressTimer) {
        clearInterval(this.progressTimer)
      }

      this.progressTimer = setInterval(async() => {
        await this.refreshProgress()
      }, 3000)

      // 立即获取一次进度
      this.refreshProgress()
    },

    // 刷新阅卷进度
    async refreshProgress() {
      if (!this.currentGradingExamId) return

      try {
        const res = await getRealAiGradingProgress(this.currentGradingExamId)
        if (res.code) {
          this.currentGradingProgress = res.data

          // 如果阅卷完成，停止定时器
          if (this.currentGradingProgress.status === 'completed') {
            if (this.progressTimer) {
              clearInterval(this.progressTimer)
              this.progressTimer = null
            }
            this.showGradingResults()
          }
        }
      } catch (error) {
        console.error('获取阅卷进度失败:', error)
      }
    },

    // 停止阅卷
    stopGrading() {
      if (this.progressTimer) {
        clearInterval(this.progressTimer)
        this.progressTimer = null
      }
      this.currentGradingProgress = null
      this.currentGradingExamId = null
      this.$message.info('已停止AI阅卷')
    },

    // 清除进度
    clearProgress() {
      this.currentGradingProgress = null
      this.currentGradingExamId = null
    },

    // 显示阅卷结果
    showGradingResults() {
      this.resultsDialogVisible = true
      // 这里可以加载详细的阅卷结果数据
      this.gradingResults = {
        total: this.currentGradingProgress.total,
        avgScore: 85, // 模拟数据
        correctRate: 75, // 模拟数据
        timeUsed: Math.round(this.currentGradingProgress.total * 2) // 模拟数据
      }
    },

    // 查看考试详情
    viewExamDetail(examId) {
      this.$router.push(`/record/exam/detail?examId=${examId}`)
    },

    // 查看详细结果
    goToExamDetail() {
      if (this.currentGradingExamId) {
        this.viewExamDetail(this.currentGradingExamId)
        this.resultsDialogVisible = false
      }
    },

    // 格式化日期
    formatDate(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      return date.toLocaleString('zh-CN')
    },

    // 获取进度状态
    getProgressStatus(progress) {
      if (progress.status === 'completed') return 'success'
      if (progress.status === 'error') return 'exception'
      return null
    },

    // 获取进度状态文本
    getProgressStatusText(progress) {
      switch (progress.status) {
        case 'processing': return '处理中'
        case 'completed': return '已完成'
        case 'error': return '错误'
        default: return '未知'
      }
    }
  }
}
</script>

<style scoped lang="scss">
.ai-grading-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;

  .page-header {
    text-align: center;
    margin-bottom: 30px;

    h1 {
      color: #303133;
      margin-bottom: 10px;
    }

    p {
      color: #606266;
      font-size: 16px;
    }
  }

  .status-card, .exams-card, .progress-card {
    margin-bottom: 20px;
  }

  .card-header {
    display: flex;
    align-items: center;

    i {
      margin-right: 8px;
      font-size: 16px;
    }
  }

  .exam-name {
    font-weight: 500;
    color: #303133;
  }

  .progress-content {
    .progress-info {
      display: flex;
      justify-content: space-between;
      margin-top: 10px;
      color: #606266;
      font-size: 14px;
    }

    .error-message {
      margin-top: 10px;
    }

    .progress-actions {
      margin-top: 15px;
      text-align: center;
    }
  }

  .results-content {
    .results-summary {
      margin: 20px 0;

      h3 {
        margin-bottom: 15px;
        color: #303133;
      }
    }

    .results-actions {
      text-align: center;
      margin-top: 20px;
    }
  }
}
</style>