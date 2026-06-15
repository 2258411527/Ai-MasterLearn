<template>
  <el-dialog
    :title="mode === 'grade' ? 'AI人工智能评分与分析' : 'AI智能题目解析'"
    :visible.sync="dialogVisible"
    width="820px"
    @close="handleClose"
    :lock-scroll="false"
    :top="'6vh'"
    custom-class="ai-grading-dialog">
    <div class="scroll-wrapper">
      <div v-if="loading" class="loading-container">
        <i class="el-icon-loading" style="font-size: 48px; color: #409eff"></i>
        <p style="margin-top: 20px; font-size: 16px; color: #606266">
          {{ mode === 'grade' ? 'AI正在深度学习分析答案中，请稍候...' : 'AI正在解析题目与分析答案，请稍候...' }}
        </p>
        <p style="font-size: 13px; color: #909399; margin-top: 8px">
          {{ mode === 'grade' ? '正在匹配评分标准、分析知识点、生成学习建议' : '正在分析知识点、对比答案、生成解析建议' }}
        </p>
      </div>

      <div v-else-if="gradingResult" class="result-container">
        <div v-if="mode === 'grade'" class="score-header">
          <div class="score-circle" :style="{ borderColor: scoreLevelColor, color: scoreLevelColor }">
            <span class="score-number">{{ gradingResult.aiScore || '--' }}</span>
            <span class="score-label">分</span>
          </div>
          <div class="score-info">
            <h3 style="margin: 0; font-size: 18px">{{ scoreLevelText }}</h3>
            <el-tag v-if="gradingResult.isCorrect !== null" :type="gradingResult.isCorrect ? 'success' : 'danger'"
              effect="plain" style="margin-top: 8px">
              {{ gradingResult.isCorrect ? '合格' : '需改进' }}
            </el-tag>
            <p v-if="gradingResult.scoringCriteria" class="criteria-summary">
              {{ gradingResult.scoringCriteria }}
            </p>
          </div>
        </div>

        <div v-else class="analyze-header">
          <div class="analyze-icon">
            <i class="el-icon-cpu" style="font-size: 36px; color: #409eff"></i>
          </div>
          <div class="analyze-info">
            <h3 style="margin: 0; font-size: 18px; color: #303133">AI智能解析</h3>
            <p style="margin: 6px 0 0; font-size: 13px; color: #909399">以下为AI对本题的全面分析与学习建议</p>
          </div>
        </div>

        <div class="compare-section" v-if="gradingResult.standardAnswer">
          <h4 class="section-title">
            <i class="el-icon-document"></i> 答案对比
          </h4>
          <div class="compare-block">
            <div class="compare-item user">
              <span class="compare-label">你的答案</span>
              <p>{{ gradingResult.userAnswer || '未作答' }}</p>
            </div>
            <div class="compare-item standard">
              <span class="compare-label">标准答案</span>
              <p>{{ gradingResult.standardAnswer }}</p>
            </div>
          </div>
        </div>

        <div class="analysis-section" v-if="gradingResult.detailedAnalysis">
          <h4 class="section-title">
            <i class="el-icon-data-analysis"></i> 详细分析
          </h4>
          <p class="analysis-content">{{ gradingResult.detailedAnalysis }}</p>
        </div>

        <div class="knowledge-section" v-if="gradingResult.knowledgePoints">
          <h4 class="section-title">
            <i class="el-icon-collection-tag"></i> 知识点
          </h4>
          <div class="knowledge-tags">
            <el-tag v-for="(kp, idx) in knowledgeList" :key="idx"
              :type="knowledgeTagType(idx)" effect="plain" class="knowledge-tag">
              {{ kp }}
            </el-tag>
          </div>
        </div>

        <div class="improvement-section" v-if="gradingResult.improvementSuggestions">
          <h4 class="section-title">
            <i class="el-icon-sunny"></i> 改进建议
          </h4>
          <p class="improvement-content">{{ gradingResult.improvementSuggestions }}</p>
        </div>

        <div class="learning-path-section" v-if="gradingResult.learningPath">
          <h4 class="section-title">
            <i class="el-icon-guide"></i> 学习路径推荐
          </h4>
          <p class="learning-path-content">{{ gradingResult.learningPath }}</p>
        </div>

        <div class="option-analysis-section" v-if="gradingResult.optionAnalysis">
          <h4 class="section-title">
            <i class="el-icon-s-operation"></i> 选项分析
          </h4>
          <p class="option-analysis-content">{{ gradingResult.optionAnalysis }}</p>
        </div>
      </div>

      <div v-else class="error-container">
        <i class="el-icon-warning" style="font-size: 48px; color: #f56c6c"></i>
        <p>{{ mode === 'grade' ? 'AI阅卷未获取到结果，请稍后重试' : 'AI解析未获取到结果，请稍后重试' }}</p>
      </div>
    </div>

    <span slot="footer" class="dialog-footer">
      <el-button @click="handleClose">关闭</el-button>
      <el-button v-if="mode === 'grade'" type="primary" @click="handleRegrade" :loading="loading">
        重新评分
      </el-button>
      <el-button v-else-if="gradingResult" type="primary" @click="handleReanalyze" :loading="loading">
        重新解析
      </el-button>
    </span>
  </el-dialog>
</template>

<script>
import { aiGradeSingleQuestion, aiAnalyzeQuestion } from '@/api/ai-grading'
import { getUserId, getRole } from '@/utils/auth'

export default {
  name: 'AiGradingDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    questionData: {
      type: Object,
      default: () => ({})
    },
    examId: {
      type: [Number, String],
      default: null
    },
    mode: {
      type: String,
      default: 'grade',
      validator: (val) => ['grade', 'analyze'].includes(val)
    },
    configId: {
      type: Number,
      default: null
    }
  },
  data() {
    return {
      loading: false,
      gradingResult: null
    }
  },
  computed: {
    dialogVisible: {
      get() {
        return this.visible
      },
      set(val) {
        if (!val) {
          this.$emit('close')
        }
      }
    },
    scoreLevelColor() {
      if (!this.gradingResult || this.gradingResult.aiScore == null) return '#909399'
      const score = this.gradingResult.aiScore
      if (score >= 85) return '#67c23a'
      if (score >= 70) return '#409eff'
      if (score >= 60) return '#e6a23c'
      return '#f56c6c'
    },
    scoreLevelText() {
      if (!this.gradingResult || this.gradingResult.aiScore == null) return '未评分'
      const score = this.gradingResult.aiScore
      if (score >= 85) return '优秀 - 你的回答非常出色！'
      if (score >= 70) return '良好 - 还有提升空间'
      if (score >= 60) return '合格 - 需要继续努力'
      return '需加强 - 建议重点复习相关知识'
    },
    knowledgeList() {
      if (!this.gradingResult || !this.gradingResult.knowledgePoints) return []
      return this.gradingResult.knowledgePoints.split(/[,，、;；\n]/).filter(k => k.trim().length > 0).map(k => k.trim())
    }
  },
  watch: {
    async visible(newVal) {
      if (newVal && this.questionData) {
        await this.startGrading()
      }
    }
  },
  methods: {
    async startGrading() {
      this.loading = true
      this.gradingResult = null
      try {
        const questionId = this.questionData.questionId
          || this.questionData.id || this.questionData.question_id || this.questionData.quId
        const userAnswer = this.questionData.answer || this.questionData.myOption || ''
        const questionContent = this.questionData.content || this.questionData.title || ''

        if (!questionId) {
          this.$message.warning('题目数据不完整，无法操作')
          this.loading = false
          return
        }

        const params = {
          examId: this.examId,
          userId: Number(getUserId()),
          questionId: questionId,
          userAnswer: userAnswer,
          questionContent: questionContent,
          configId: this.configId
        }

        console.log('[AiGradingDialog] 发送AI请求, configId=', this.configId, ', mode=', this.mode)

        const apiFn = this.mode === 'grade' ? aiGradeSingleQuestion : aiAnalyzeQuestion
        const res = await apiFn(params)
        if (res && res.code === 1) {
          this.gradingResult = res.data
        } else {
          this.$message.error(res?.msg || (this.mode === 'grade' ? 'AI阅卷失败' : 'AI解析失败'))
        }
      } catch (e) {
        console.error('AI请求异常:', e)
        this.$message.error('AI服务暂不可用，请稍后重试')
      } finally {
        this.loading = false
      }
    },
    async handleRegrade() {
      this.loading = true
      try {
        await this.startGrading()
        this.$message.success('重新评分完成')
      } catch (e) {
        this.$message.error('重新评分失败')
      } finally {
        this.loading = false
      }
    },
    async handleReanalyze() {
      this.loading = true
      try {
        await this.startGrading()
        this.$message.success('重新解析完成')
      } catch (e) {
        this.$message.error('重新解析失败')
      } finally {
        this.loading = false
      }
    },
    handleClose() {
      this.loading = false
      this.gradingResult = null
      this.$emit('close')
    },
    knowledgeTagType(idx) {
      const types = ['', 'success', 'info', 'warning', 'danger']
      return types[idx % types.length]
    }
  }
}
</script>

<style scoped>
.scroll-wrapper {
  max-height: 70vh;
  overflow-y: auto;
  padding-right: 6px;
}

.scroll-wrapper::-webkit-scrollbar {
  width: 6px;
}

.scroll-wrapper::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 3px;
}

.scroll-wrapper::-webkit-scrollbar-thumb:hover {
  background: #909399;
}

.loading-container {
  text-align: center;
  padding: 60px 0;
}

.result-container {
  padding: 0 10px;
}

.score-header {
  display: flex;
  align-items: center;
  padding: 20px 16px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e6f0fb 100%);
  border-radius: 12px;
  margin-bottom: 20px;
}

.score-circle {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  border: 4px solid;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  background: #fff;
  flex-shrink: 0;
}

.score-number {
  font-size: 32px;
  font-weight: 700;
  line-height: 1;
}

.score-label {
  font-size: 14px;
  margin-top: 2px;
}

.score-info {
  flex: 1;
}

.criteria-summary {
  margin: 8px 0 0;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

.analyze-header {
  display: flex;
  align-items: center;
  padding: 16px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e6f0fb 100%);
  border-radius: 12px;
  margin-bottom: 20px;
}

.analyze-icon {
  margin-right: 16px;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: #ecf5ff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.analyze-info {
  flex: 1;
}

.section-title {
  font-size: 16px;
  color: #303133;
  margin: 0 0 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.section-title i {
  margin-right: 6px;
  color: #409eff;
}

.compare-block {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.compare-item {
  padding: 14px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.7;
}

.compare-item.user {
  background: #fef0f0;
  border: 1px solid #fde2e2;
}

.compare-item.standard {
  background: #f0f9ff;
  border: 1px solid #d9ecff;
}

.compare-label {
  display: block;
  font-weight: 600;
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}

.analysis-content,
.improvement-content,
.learning-path-content,
.option-analysis-content {
  font-size: 14px;
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
  padding: 12px 14px;
  background: #fafafa;
  border-radius: 8px;
}

.knowledge-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 4px 0;
}

.knowledge-tag {
  margin: 0;
}

.analysis-section,
.improvement-section,
.learning-path-section,
.compare-section,
.knowledge-section,
.option-analysis-section {
  margin-bottom: 18px;
}

.error-container {
  text-align: center;
  padding: 60px 0;
}
</style>

<style>
.ai-grading-dialog .el-dialog__body {
  padding: 16px 24px;
}
</style>