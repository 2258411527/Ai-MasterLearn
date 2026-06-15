<template>
  <div class="ai-grading-result-center">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1>AI阅卷结果中心</h1>
      <p>查看您的AI阅卷结果，获取详细的分析和改进建议</p>
    </div>

    <!-- 考试选择区域 -->
    <el-card class="exam-select-card">
      <div slot="header" class="card-header">
        <i class="el-icon-document" />
        <span>选择考试</span>
      </div>

      <div class="exam-select-content">
        <el-select
          v-model="selectedExamId"
          placeholder="请选择考试"
          style="width: 300px; margin-right: 20px;"
          @change="onExamChange"
        >
          <el-option
            v-for="exam in examList"
            :key="exam.examId"
            :label="exam.examName"
            :value="exam.examId"
          >
            <span style="float: left">{{ exam.examName }}</span>
            <span style="float: right; color: #8492a6; font-size: 13px">
              {{ formatDate(exam.examDate) }}
            </span>
          </el-option>
        </el-select>

        <el-button
          type="primary"
          :loading="loadingResults"
          @click="loadExamResults"
        >
          <i class="el-icon-search" />
          查看阅卷结果
        </el-button>
      </div>
    </el-card>

    <!-- 阅卷状态概览 -->
    <el-card v-if="selectedExamId" class="status-overview-card">
      <div slot="header" class="card-header">
        <i class="el-icon-data-analysis" />
        <span>阅卷状态概览</span>
      </div>

      <div class="status-overview-content">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="status-item">
              <div class="status-icon ai-status">
                <i class="el-icon-cpu" />
              </div>
              <div class="status-info">
                <div class="status-label">AI阅卷状态</div>
                <div class="status-value">{{ aiGradingStatus.completed ? '已完成' : '进行中' }}</div>
              </div>
            </div>
          </el-col>

          <el-col :span="6">
            <div class="status-item">
              <div class="status-icon teacher-status">
                <i class="el-icon-user" />
              </div>
              <div class="status-info">
                <div class="status-label">教师阅卷状态</div>
                <div class="status-value">{{ teacherGradingStatus.completed ? '已完成' : '未开始' }}</div>
              </div>
            </div>
          </el-col>

          <el-col :span="6">
            <div class="status-item">
              <div class="status-icon score-status">
                <i class="el-icon-star-on" />
              </div>
              <div class="status-info">
                <div class="status-label">最终成绩</div>
                <div class="status-value">{{ finalScore }} 分</div>
              </div>
            </div>
          </el-col>

          <el-col :span="6">
            <div class="status-item">
              <div class="status-icon progress-status">
                <i class="el-icon-timer" />
              </div>
              <div class="status-info">
                <div class="status-label">AI阅卷进度</div>
                <div class="status-value">{{ aiGradingStatus.progress }}%</div>
              </div>
            </div>
          </el-col>
        </el-row>

        <!-- 进度条 -->
        <el-progress
          :percentage="aiGradingStatus.progress"
          :status="aiGradingStatus.completed ? 'success' : 'primary'"
          style="margin-top: 20px;"
        />
      </div>
    </el-card>

    <!-- 阅卷结果列表 -->
    <el-card v-if="selectedExamId && gradingResults.length > 0" class="results-card">
      <div slot="header" class="card-header">
        <i class="el-icon-chat-dot-round" />
        <span>阅卷结果详情</span>
      </div>

      <div class="results-content">
        <el-collapse v-model="activeNames" accordion>
          <el-collapse-item
            v-for="(result, index) in gradingResults"
            :key="result.questionId"
            :name="index"
            :class="{ 'has-score': result.aiScore !== null }"
          >

            <template slot="title">
              <div class="result-header">
                <span class="question-index">题目 {{ index + 1 }}</span>
                <span class="question-type">{{ getQuestionTypeName(result.questionType) }}</span>
                <!-- 简答题显示AI评分 -->
                <span v-if="result.questionType === 4 && result.aiScore !== null" class="ai-score">
                  <i class="el-icon-cpu" />
                  AI评分: {{ result.aiScore }} 分
                  <el-tag :type="result.aiScore >= 60 ? 'success' : 'danger'" size="mini">
                    {{ result.aiScore >= 60 ? '合格' : '不合格' }}
                  </el-tag>
                </span>
                <!-- 客观题显示系统自动判分 -->
                <span v-else-if="result.questionType !== 4" class="system-score">
                  <i class="el-icon-check" />
                  <el-tag type="success" size="mini">系统已判分</el-tag>
                </span>
                <!-- 简答题未评分 -->
                <span v-else class="no-score">
                  <i class="el-icon-warning" />
                  <el-tag type="warning" size="mini">待AI评分</el-tag>
                </span>
              </div>
            </template>

            <div class="result-content">
              <!-- 题目内容 -->
              <div class="question-section">
                <h4>题目内容</h4>
                <p class="question-content">{{ result.questionContent }}</p>
              </div>

              <!-- 学生答案 -->
              <div class="answer-section">
                <h4>您的答案</h4>
                <p class="user-answer">{{ result.userAnswer || '未作答' }}</p>
              </div>

              <!-- AI分析结果 -->
              <div class="analysis-section">
                <h4>AI分析</h4>
                <div class="analysis-content">
                  <p><strong>详细分析：</strong>{{ result.detailedAnalysis }}</p>
                  <p><strong>改进建议：</strong>{{ result.improvementSuggestions }}</p>
                  <p><strong>知识点：</strong>{{ result.knowledgePoints }}</p>
                </div>
              </div>

              <!-- 教师评阅（如果有） -->
              <div v-if="result.teacherScore !== null" class="teacher-section">
                <h4>教师评阅</h4>
                <div class="teacher-content">
                  <p><strong>教师评分：</strong>{{ result.teacherScore }} 分</p>
                  <p><strong>教师评语：</strong>{{ result.teacherComment }}</p>
                </div>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>
    </el-card>

    <!-- 空状态 -->
    <el-empty
      v-if="selectedExamId && gradingResults.length === 0"
      description="暂无阅卷结果"
      :image-size="200"
    >
      <el-button type="primary" @click="triggerAiGrading">
        <i class="el-icon-cpu" />
        开始AI阅卷
      </el-button>
    </el-empty>
  </div>
</template>

<script>
import { getExamAiGradingResults, getExamAiGradingStatus } from '@/api/ai-grading-result'
import { getGradableExams } from '@/api/real-ai-grading'
import { realAiGradeBatchQuestions } from '@/api/real-ai-grading'

export default {
  name: 'AiGradingResultCenter',
  data() {
    return {
      selectedExamId: null,
      examList: [],
      gradingResults: [],
      loadingResults: false,
      activeNames: [0],

      // 状态数据
      aiGradingStatus: {
        completed: false,
        progress: 0,
        totalQuestions: 0,
        gradedQuestions: 0
      },
      teacherGradingStatus: {
        completed: false
      },
      finalScore: 0
    }
  },

  mounted() {
    this.loadGradableExams()
  },

  methods: {
    // 加载可阅卷的考试列表
    async loadGradableExams() {
      try {
        const response = await getGradableExams()
        if (response.code === 1) {
          this.examList = response.data || []
          if (this.examList.length > 0) {
            this.selectedExamId = this.examList[0].examId
            this.loadExamResults()
          }
        }
      } catch (error) {
        this.$message.error('加载考试列表失败')
        console.error('加载考试列表失败:', error)
      }
    },

    // 考试选择变化
    onExamChange() {
      this.loadExamResults()
    },

    // 加载考试阅卷结果
    async loadExamResults() {
      if (!this.selectedExamId) return

      this.loadingResults = true
      try {
        // 获取阅卷状态
        const statusResponse = await getExamAiGradingStatus(this.selectedExamId, this.$store.getters.userId)
        if (statusResponse.code === 1) {
          this.aiGradingStatus = statusResponse.data
        }

        // 获取阅卷结果
        const resultsResponse = await getExamAiGradingResults(this.selectedExamId, this.$store.getters.userId)
        if (resultsResponse.code === 1) {
          this.gradingResults = resultsResponse.data || []
        }
      } catch (error) {
        this.$message.error('加载阅卷结果失败')
        console.error('加载阅卷结果失败:', error)
      } finally {
        this.loadingResults = false
      }
    },

    // 触发AI阅卷（只对简答题进行AI阅卷）
    async triggerAiGrading() {
      if (!this.selectedExamId) return

      try {
        // 检查是否有简答题
        const essayQuestions = this.gradingResults?.filter(q => q.questionType === 4) || []

        if (essayQuestions.length === 0) {
          this.$message.info('本次考试没有简答题，无需AI阅卷')
          return
        }

        this.$message.info(`检测到 ${essayQuestions.length} 道简答题，开始AI阅卷...`)

        const response = await realAiGradeBatchQuestions(this.selectedExamId)
        if (response.code === 1) {
          this.$message.success(`简答题AI阅卷任务已提交，系统将对${essayQuestions.length}道简答题进行评分，客观题由系统自动判断`)

          // 显示统分说明
          this.$notify({
            title: 'AI阅卷说明',
            message: `系统将对${essayQuestions.length}道简答题进行AI评分，客观题（选择题、判断题等）由系统自动判断对错，总分由系统自动计算。`,
            type: 'info',
            duration: 8000
          })

          // 3秒后自动刷新结果
          setTimeout(() => {
            this.loadExamResults()
          }, 3000)
        } else {
          this.$message.error(response.msg || '提交简答题AI阅卷失败')
        }
      } catch (error) {
        this.$message.error('提交简答题AI阅卷失败')
        console.error('提交简答题AI阅卷失败:', error)
      }
    },

    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString() + ' ' + date.toLocaleTimeString()
    },

    // 获取题目类型名称
    getQuestionTypeName(type) {
      const typeMap = {
        1: '单选题',
        2: '多选题',
        3: '判断题',
        4: '简答题',
        5: '论述题',
        6: '编程题'
      }
      return typeMap[type] || '未知题型'
    }
  }
}
</script>

<style scoped>
.ai-grading-result-center {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h1 {
  color: #303133;
  margin-bottom: 10px;
}

.page-header p {
  color: #606266;
  font-size: 16px;
}

/* 状态概览样式 */
.status-item {
  display: flex;
  align-items: center;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.status-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 24px;
  color: white;
}

.status-icon.ai-status {
  background: #67C23A;
}

.status-icon.teacher-status {
  background: #E6A23C;
}

.status-icon.score-status {
  background: #F56C6C;
}

.status-icon.progress-status {
  background: #409EFF;
}

.status-info {
  flex: 1;
}

.status-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.status-value {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

/* 结果列表样式 */
.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding-right: 20px;
}

.question-index {
  font-weight: bold;
  color: #303133;
}

.question-type {
  color: #606266;
  font-size: 14px;
}

.ai-score, .analysis-only {
  font-weight: bold;
}

.result-content {
  padding: 15px;
}

.question-section, .answer-section, .analysis-section, .teacher-section {
  margin-bottom: 20px;
}

.question-section h4, .answer-section h4, .analysis-section h4, .teacher-section h4 {
  color: #303133;
  margin-bottom: 10px;
  border-left: 4px solid #409EFF;
  padding-left: 10px;
}

.question-content, .user-answer {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  line-height: 1.6;
}

.analysis-content p {
  margin-bottom: 10px;
  line-height: 1.6;
}

/* 有评分的题目样式 */
.has-score >>> .el-collapse-item__header {
  background: #f0f9ff;
}
</style>
