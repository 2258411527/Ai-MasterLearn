<template>
  <div class="single-practice-container">
    <div v-if="pageLoading" class="loading-box">
      <i class="el-icon-loading"></i>
      <p>加载习题中...</p>
    </div>

    <template v-else>
      <div class="practice-header glass-card">
        <div class="header-left">
          <el-button icon="el-icon-arrow-left" @click="goBack">返回</el-button>
          <span class="question-type">
            <el-tag :type="getTypeTag(question.quType)">{{ getTypeText(question.quType) }}</el-tag>
            <el-tag v-if="question.knowledgePoint" effect="plain" size="small">{{ question.knowledgePoint }}</el-tag>
          </span>
        </div>
        <div class="header-right">
          <span class="progress">第 {{ currentIndex + 1 }} / {{ totalQuestions }} 题</span>
        </div>
      </div>

      <div class="question-content glass-card" v-if="question.id">
        <div class="question-text" v-html="question.content"></div>

        <div class="options-list" v-if="question.options && question.options.length > 0">
          <div
            v-for="option in question.options"
            :key="option.key"
            class="option-item"
            :class="getOptionClass(option)"
            @click="selectOption(option)"
          >
            <span class="option-key">{{ option.key }}</span>
            <span class="option-content" v-html="option.content"></span>
            <i v-if="showAnswer && option.isCorrect" class="el-icon-check correct-icon"></i>
            <i v-if="showAnswer && isSelected(option) && !option.isCorrect" class="el-icon-close wrong-icon"></i>
          </div>
        </div>
        <div v-else class="no-options">
          <p>该题目暂无选项</p>
        </div>

        <div v-if="showAnswer" class="answer-analysis">
          <div class="analysis-header">
            <i class="el-icon-document"></i>
            <span>答案解析</span>
          </div>
          <div class="analysis-content">
            <p><strong>正确答案：</strong>{{ correctAnswer }}</p>
            <p v-if="question.analysis"><strong>解析：</strong>{{ question.analysis }}</p>
            <p v-else><strong>解析：</strong>暂无解析</p>
          </div>
        </div>
      </div>

      <div class="practice-footer glass-card">
        <div class="footer-left">
          <el-button v-if="!showAnswer" type="primary" :disabled="!canSubmit" @click="submitAnswer">
            提交答案
          </el-button>
          <el-button v-else type="success" @click="nextQuestion">
            {{ currentIndex < totalQuestions - 1 ? '下一题' : '完成练习' }}
          </el-button>
        </div>
        <div class="footer-right">
          <el-button v-if="!showAnswer" @click="skipQuestion">跳过</el-button>
        </div>
      </div>

      <el-dialog :visible.sync="resultVisible" title="练习完成" width="400px" :close-on-click-modal="false">
        <div class="result-content">
          <div class="result-stats">
            <div class="stat-item">
              <span class="stat-value">{{ correctCount }}</span>
              <span class="stat-label">正确</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ wrongCount }}</span>
              <span class="stat-label">错误</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ Math.round(correctCount / totalQuestions * 100) || 0 }}%</span>
              <span class="stat-label">正确率</span>
            </div>
          </div>
        </div>
        <span slot="footer">
          <el-button @click="goHome">返回首页</el-button>
          <el-button type="primary" @click="restartPractice">继续练习</el-button>
        </span>
      </el-dialog>
    </template>
  </div>
</template>

<script>
import { getAIRecommendQuestions } from '@/api/ai-question'

export default {
  name: 'SinglePractice',
  data() {
    return {
      pageLoading: true,
      questions: [],
      currentIndex: 0,
      selectedKeys: [],
      showAnswer: false,
      correctCount: 0,
      wrongCount: 0,
      resultVisible: false
    }
  },
  computed: {
    question() {
      return this.questions[this.currentIndex] || {}
    },
    totalQuestions() {
      return this.questions.length
    },
    isMultiSelect() {
      return this.question.quType === 2
    },
    canSubmit() {
      return this.selectedKeys.length > 0
    },
    correctAnswer() {
      if (!this.question.options) return ''
      var correct = this.question.options.filter(function(o) { return o.isCorrect }).map(function(o) { return o.key })
      return correct.join(',')
    }
  },
  created() {
    this.loadQuestions()
  },
  methods: {
    async loadQuestions() {
      this.pageLoading = true
      try {
        var questionId = this.$route.query.questionId
        var res = await getAIRecommendQuestions(4)
        if (res.code && res.data && res.data.length > 0) {
          this.questions = res.data
          if (questionId) {
            var idx = this.questions.findIndex(function(q) { return q.id === parseInt(questionId) })
            if (idx >= 0) this.currentIndex = idx
          }
        } else {
          this.$message.warning('暂无推荐习题')
        }
      } catch (e) {
        console.error('加载习题失败:', e)
        this.$message.error('加载习题失败')
      } finally {
        this.pageLoading = false
      }
    },

    getTypeTag(type) {
      var types = { 1: 'danger', 2: 'warning', 3: 'success' }
      return types[type] || 'info'
    },

    getTypeText(type) {
      var texts = { 1: '单选题', 2: '多选题', 3: '判断题', 4: '填空题', 5: '简答题' }
      return texts[type] || '其他'
    },

    isSelected(option) {
      return this.selectedKeys.indexOf(option.key) >= 0
    },

    getOptionClass(option) {
      if (!this.showAnswer) {
        return this.isSelected(option) ? 'selected' : ''
      }
      if (option.isCorrect) return 'correct'
      if (this.isSelected(option) && !option.isCorrect) return 'wrong'
      return ''
    },

    selectOption(option) {
      if (this.showAnswer) return
      var idx = this.selectedKeys.indexOf(option.key)
      if (this.isMultiSelect) {
        if (idx >= 0) {
          this.selectedKeys.splice(idx, 1)
        } else {
          this.selectedKeys.push(option.key)
        }
      } else {
        if (idx >= 0) {
          this.selectedKeys = []
        } else {
          this.selectedKeys = [option.key]
        }
      }
    },

    submitAnswer() {
      if (!this.canSubmit) return
      this.showAnswer = true
      var isCorrect = this.checkAnswer()
      if (isCorrect) {
        this.correctCount++
        this.$message.success('回答正确！')
      } else {
        this.wrongCount++
        this.$message.error('回答错误，正确答案：' + this.correctAnswer)
      }
    },

    checkAnswer() {
      if (!this.question.options) return false
      var correctKeys = this.question.options
        .filter(function(o) { return o.isCorrect })
        .map(function(o) { return o.key })
        .sort()
      var selected = this.selectedKeys.slice().sort()
      if (correctKeys.length !== selected.length) return false
      for (var i = 0; i < correctKeys.length; i++) {
        if (correctKeys[i] !== selected[i]) return false
      }
      return true
    },

    nextQuestion() {
      if (this.currentIndex < this.totalQuestions - 1) {
        this.currentIndex++
        this.selectedKeys = []
        this.showAnswer = false
      } else {
        this.resultVisible = true
      }
    },

    skipQuestion() {
      this.wrongCount++
      this.nextQuestion()
    },

    goBack() {
      this.$router.go(-1)
    },

    goHome() {
      this.$router.push('/home/index')
    },

    restartPractice() {
      this.resultVisible = false
      this.currentIndex = 0
      this.selectedKeys = []
      this.showAnswer = false
      this.correctCount = 0
      this.wrongCount = 0
      this.loadQuestions()
    }
  }
}
</script>

<style lang="scss" scoped>
.single-practice-container {
  min-height: 100vh;
  background: var(--bg-primary);
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.loading-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  color: var(--text-secondary);

  i { font-size: 40px; margin-bottom: 16px; color: var(--accent); }
  p { font-size: 14px; }
}

.glass-card {
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  border-radius: 12px;
  margin-bottom: 16px;
}

.practice-header {
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .question-type {
      display: flex;
      gap: 8px;
    }
  }

  .progress {
    font-size: 14px;
    color: var(--text-secondary);
  }
}

.question-content {
  padding: 24px;

  .question-text {
    font-size: 16px;
    color: var(--text-primary);
    line-height: 1.8;
    margin-bottom: 24px;
  }

  .no-options {
    text-align: center;
    padding: 30px;
    color: var(--text-secondary);
    font-size: 14px;
  }

  .options-list {
    .option-item {
      display: flex;
      align-items: flex-start;
      gap: 12px;
      padding: 16px;
      margin-bottom: 12px;
      background: var(--bg-hover);
      border: 2px solid var(--glass-border);
      border-radius: 10px;
      cursor: pointer;
      transition: all 0.2s ease;

      &:hover {
        border-color: var(--accent);
      }

      &.selected {
        border-color: var(--accent);
        background: rgba(99, 102, 241, 0.1);
      }

      &.correct {
        border-color: var(--success);
        background: rgba(16, 185, 129, 0.1);
        cursor: default;
      }

      &.wrong {
        border-color: var(--danger);
        background: rgba(239, 68, 68, 0.1);
        cursor: default;
      }

      .option-key {
        width: 28px;
        height: 28px;
        border-radius: 50%;
        background: var(--bg-active);
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 600;
        color: var(--text-primary);
        flex-shrink: 0;
      }

      .option-content {
        flex: 1;
        color: var(--text-primary);
        line-height: 1.6;
      }

      .correct-icon {
        color: var(--success);
        font-size: 20px;
      }

      .wrong-icon {
        color: var(--danger);
        font-size: 20px;
      }
    }
  }

  .answer-analysis {
    margin-top: 24px;
    padding: 20px;
    background: rgba(99, 102, 241, 0.05);
    border-radius: 10px;

    .analysis-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 15px;
      font-weight: 600;
      color: var(--text-primary);
      margin-bottom: 12px;

      i { color: var(--accent); }
    }

    .analysis-content {
      font-size: 14px;
      color: var(--text-secondary);
      line-height: 1.8;

      p { margin: 8px 0; }
      strong { color: var(--text-primary); }
    }
  }
}

.practice-footer {
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-content {
  .result-stats {
    display: flex;
    justify-content: center;
    gap: 40px;

    .stat-item {
      text-align: center;

      .stat-value {
        display: block;
        font-size: 32px;
        font-weight: 700;
        color: var(--accent);
      }

      .stat-label {
        font-size: 14px;
        color: var(--text-secondary);
      }
    }
  }
}
</style>
