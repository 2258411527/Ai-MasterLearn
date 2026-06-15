<template>
  <div class="home-container">
    <div class="bg-decoration">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="gradient-orb orb-3"></div>
    </div>

    <div class="content-wrapper">
      <div class="welcome-header glass-card">
        <div class="user-section">
          <el-avatar :size="64" :src="userInfo.avatarUrl" class="avatar">
            <i class="el-icon-user-solid"></i>
          </el-avatar>
          <div class="user-info">
            <h1>{{ userInfo.username || '学习者' }}，加油！</h1>
            <p>
              <span v-if="userInfo.examType">{{ userInfo.examType }}</span>
              <span v-if="userInfo.targetUniversityLevel"> · {{ userInfo.targetUniversityLevel }}</span>
              <span v-if="userInfo.degreeType"> · {{ userInfo.degreeType }}</span>
            </p>
          </div>
        </div>
        <div class="header-stats">
          <div class="stat-item">
            <i class="el-icon-time"></i>
            <span>今日 {{ userInfo.todayStudyMinutes || 0 }} 分钟</span>
          </div>
          <div v-if="userInfo.streakDays > 0" class="streak-badge">
            <span class="fire">🔥</span>
            <span>连续 {{ userInfo.streakDays }} 天</span>
          </div>
        </div>
      </div>

      <div class="recommend-section glass-card">
        <div class="section-header">
          <div class="header-title">
            <i class="el-icon-edit-outline"></i>
            <span>AI推荐习题</span>
          </div>
          <div class="header-actions">
          </div>
        </div>
        <div class="question-single" v-if="recommendQuestions.length > 0">
          <div class="question-card">
            <div class="question-header">
              <el-tag :type="getQuestionTypeTag(currentQuestion.quType)" size="mini">{{ getQuestionTypeText(currentQuestion.quType) }}</el-tag>
              <el-tag v-if="currentQuestion.knowledgePoint" size="mini" effect="plain">{{ currentQuestion.knowledgePoint }}</el-tag>
            </div>
            <div class="question-content" v-html="currentQuestion.content"></div>

            <!-- 单选题 / 判断题 -->
            <div class="question-options" v-if="currentQuestion.quType === 1 || currentQuestion.quType === 3">
              <div
                v-for="opt in currentQuestion.options"
                :key="opt.key"
                class="option-item"
                :class="getOptionClass(opt)"
                @click="selectSingleOption(opt)"
              >
                <span class="option-key">{{ opt.key }}</span>
                <span class="option-text" v-html="opt.content"></span>
                <i v-if="answered && opt.isCorrect" class="el-icon-check result-icon correct"></i>
                <i v-if="answered && selectedOption === opt.key && !opt.isCorrect" class="el-icon-close result-icon wrong"></i>
              </div>
            </div>

            <!-- 多选题 -->
            <div class="question-options" v-if="currentQuestion.quType === 2">
              <div
                v-for="opt in currentQuestion.options"
                :key="opt.key"
                class="option-item"
                :class="getMultiOptionClass(opt)"
                @click="selectMultiOption(opt)"
              >
                <span class="option-check" :class="{ checked: selectedOptions.includes(opt.key) }">
                  <i v-if="selectedOptions.includes(opt.key)" class="el-icon-check"></i>
                </span>
                <span class="option-key">{{ opt.key }}</span>
                <span class="option-text" v-html="opt.content"></span>
                <i v-if="answered && opt.isCorrect" class="el-icon-check result-icon correct"></i>
                <i v-if="answered && selectedOptions.includes(opt.key) && !opt.isCorrect" class="el-icon-close result-icon wrong"></i>
              </div>
            </div>

            <!-- 填空题 -->
            <div class="question-input" v-if="currentQuestion.quType === 4">
              <el-input v-model="textAnswer" placeholder="请输入答案" :disabled="answered" @keyup.enter.native="submitAnswer" />
            </div>

            <!-- 简答题 -->
            <div class="question-input" v-if="currentQuestion.quType === 5">
              <el-input v-model="textAnswer" type="textarea" :rows="4" placeholder="请输入您的回答" :disabled="answered" />
            </div>

            <div class="question-reason" v-if="!answered">
              <i class="el-icon-info"></i>
              <span>{{ currentQuestion.recommendReason || '智能推荐' }}</span>
            </div>
            <div class="question-result" v-if="answered">
              <div class="result-tip" :class="isCorrect ? 'is-correct' : 'is-wrong'">
                <i :class="isCorrect ? 'el-icon-circle-check' : 'el-icon-circle-close'"></i>
                <span v-if="isChoiceQuestion">{{ isCorrect ? '回答正确！' : '回答错误' }}</span>
                <span v-else>参考答案已展示</span>
              </div>
              <div class="result-answer" v-if="currentQuestion.answer && !isChoiceQuestion">
                <div class="answer-title">参考答案</div>
                <div class="answer-content">{{ currentQuestion.answer }}</div>
              </div>
              <div class="result-analysis" v-if="currentQuestion.analysis">
                <div class="analysis-title"><i class="el-icon-document"></i> 解析</div>
                <div class="analysis-content" v-html="currentQuestion.analysis"></div>
              </div>
            </div>
            <div class="question-actions">
              <el-button v-if="!answered" type="primary" size="small" :disabled="!canSubmit" @click="submitAnswer">提交答案</el-button>
              <el-button v-else type="primary" size="small" :loading="questionLoading" @click="loadRecommendQuestions">下一题</el-button>
            </div>
          </div>
        </div>
        <div v-else class="empty-questions">
          <i class="el-icon-document"></i>
          <p>暂无推荐习题</p>
          <el-button type="primary" size="small" @click="loadRecommendQuestions">获取推荐</el-button>
        </div>
      </div>

      <div class="stats-row">
        <div class="stat-card glass-card">
          <div class="stat-icon blue"><i class="el-icon-reading"></i></div>
          <div class="stat-body">
            <span class="stat-value">{{ studyStats.weeklyStudyHours || 0 }}h</span>
            <span class="stat-label">本周学习</span>
          </div>
        </div>
        <div class="stat-card glass-card">
          <div class="stat-icon green"><i class="el-icon-circle-check"></i></div>
          <div class="stat-body">
            <span class="stat-value">{{ studyStats.completedTasks || 0 }}/{{ studyStats.totalTasks || 0 }}</span>
            <span class="stat-label">今日任务</span>
          </div>
        </div>
        <div class="stat-card glass-card">
          <div class="stat-icon orange"><i class="el-icon-document-delete"></i></div>
          <div class="stat-body">
            <span class="stat-value">{{ studyStats.wrongCount || 0 }}</span>
            <span class="stat-label">待复习错题</span>
          </div>
        </div>
        <div class="stat-card glass-card">
          <div class="stat-icon purple"><i class="el-icon-trophy"></i></div>
          <div class="stat-body">
            <span class="stat-value">{{ studyStats.goalProgress || 0 }}%</span>
            <span class="stat-label">目标进度</span>
          </div>
        </div>
      </div>

      <div class="main-content">
        <div class="left-panel">
          <div class="chart-card glass-card">
            <div class="card-header">
              <div class="header-title">
                <i class="el-icon-data-line"></i>
                <span>学习趋势</span>
              </div>
              <el-button type="text" icon="el-icon-refresh" :loading="refreshing" @click="refreshData"></el-button>
            </div>
            <div ref="chartRef" class="chart-area"></div>
          </div>

          <div class="weak-card glass-card" v-if="weakAnalysis.totalWrongCount > 0">
            <div class="card-header">
              <div class="header-title">
                <i class="el-icon-warning-outline"></i>
                <span>薄弱知识点</span>
              </div>
              <el-tag size="mini" type="warning">{{ weakAnalysis.totalWrongCount }}题待复习</el-tag>
            </div>
            <div class="weak-list">
              <div v-for="(point, idx) in weakAnalysis.weakPoints" :key="idx" class="weak-item">
                <div class="weak-header">
                  <el-tag size="small">{{ point.subject }}</el-tag>
                  <span class="weak-name">{{ point.knowledgePointName }}</span>
                </div>
                <div class="weak-progress">
                  <el-progress :percentage="point.masteryRate" :show-text="false" :stroke-width="6" />
                  <span class="progress-text">{{ point.masteryRate }}%掌握</span>
                </div>
              </div>
            </div>
            <div class="ai-tip" v-if="weakAnalysis.aiSuggestion">
              <i class="el-icon-cpu"></i>
              <span>{{ weakAnalysis.aiSuggestion }}</span>
            </div>
          </div>
        </div>

        <div class="right-panel">
          <div class="tasks-card glass-card">
            <div class="card-header">
              <div class="header-title">
                <i class="el-icon-document-checked"></i>
                <span>今日任务</span>
              </div>
              <el-button type="text" size="mini" @click="showAddTask = true">添加</el-button>
            </div>
            <div class="tasks-list" v-if="todayTasks.length > 0">
              <div
                v-for="task in todayTasks"
                :key="task.id"
                class="task-item"
                :class="{ completed: task.isCompleted === 1 }"
              >
                <div class="task-check" @click="toggleTask(task)">
                  <i v-if="task.isCompleted === 1" class="el-icon-check"></i>
                </div>
                <div class="task-body">
                  <span class="task-text">{{ task.taskContent }}</span>
                  <div class="task-meta">
                    <el-tag size="mini" :type="getPriorityType(task.priority)">{{ getPriorityText(task.priority) }}</el-tag>
                    <span>{{ task.estimatedMinutes }}分钟</span>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="empty-box">
              <i class="el-icon-document-add"></i>
              <p>暂无任务</p>
            </div>
          </div>

          <div class="countdown-card glass-card" v-if="countdowns.length > 0">
            <div class="card-header">
              <div class="header-title">
                <i class="el-icon-alarm-clock"></i>
                <span>重要日期</span>
              </div>
            </div>
            <div class="countdown-list">
              <div v-for="c in countdowns" :key="c.id" class="countdown-item">
                <h4>{{ c.eventName }}</h4>
                <div class="time-blocks">
                  <div class="time-block">
                    <span class="num">{{ c.remainingDays }}</span>
                    <span class="label">天</span>
                  </div>
                  <div class="time-block">
                    <span class="num">{{ c.remainingHours }}</span>
                    <span class="label">时</span>
                  </div>
                  <div class="time-block">
                    <span class="num">{{ c.remainingMinutes }}</span>
                    <span class="label">分</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="quote-card glass-card">
            <div class="card-header">
              <div class="header-title">
                <i class="el-icon-chat-dot-round"></i>
                <span>每日一句</span>
              </div>
            </div>
            <p class="quote-text">{{ dailyQuote }}</p>
          </div>
        </div>
      </div>
    </div>

    <el-dialog title="添加任务" :visible.sync="showAddTask" width="400px">
      <el-form :model="taskForm" label-width="80px">
        <el-form-item label="任务内容">
          <el-input v-model="taskForm.taskContent" placeholder="请输入任务内容" />
        </el-form-item>
        <el-form-item label="预计时长">
          <el-input-number v-model="taskForm.estimatedMinutes" :min="5" :max="240" />
          <span style="margin-left:8px">分钟</span>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="taskForm.priority">
            <el-option label="高" :value="1" />
            <el-option label="中" :value="2" />
            <el-option label="低" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="showAddTask = false">取消</el-button>
        <el-button type="primary" @click="addTask">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getPersonalizedHome, refreshHomeCache } from '@/api/personalized-home'
import { getAIRecommendations, getDailyStudyPlan } from '@/api/ai-recommend'
import { getAIRecommendQuestions } from '@/api/ai-question'
import { addTask, completeTask } from '@/api/home'
import { getUserId } from '@/utils/auth'

export default {
  name: 'StudentHome',
  data() {
    return {
      loading: false,
      refreshing: false,
      aiLoading: false,
      questionLoading: false,
      selectedOption: '',
      selectedOptions: [],
      textAnswer: '',
      answered: false,
      isCorrect: false,
      userInfo: {},
      recommendations: [],
      recommendQuestions: [],
      studyStats: {},
      studyTrend: [],
      todayTasks: [],
      countdowns: [],
      weakAnalysis: {},
      dailyQuote: '',
      cacheExpireTime: null,
      chart: null,
      showAddTask: false,
      taskForm: {
        taskContent: '',
        estimatedMinutes: 30,
        priority: 2,
        subject: '其他'
      }
    }
  },
  computed: {
    currentQuestion() {
      return this.recommendQuestions.length > 0 ? this.recommendQuestions[0] : {}
    },
    isChoiceQuestion() {
      const t = this.currentQuestion.quType
      return t === 1 || t === 2 || t === 3
    },
    canSubmit() {
      if (this.answered) return false
      const t = this.currentQuestion.quType
      if (t === 1 || t === 3) return !!this.selectedOption
      if (t === 2) return this.selectedOptions.length >= 2
      if (t === 4 || t === 5) return !!this.textAnswer.trim()
      return false
    }
  },
  mounted() {
    this.loadData()
    this.loadRecommendQuestions()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    if (this.chart) this.chart.dispose()
    window.removeEventListener('resize', this.handleResize)
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await getPersonalizedHome()
        if (res.code && res.data) {
          const data = res.data
          this.userInfo = data.userInfo || {}
          this.recommendations = data.recommendations || []
          this.studyStats = data.studyStats || {}
          this.studyTrend = data.studyTrend || []
          this.todayTasks = data.todayTasks || []
          this.countdowns = data.countdowns || []
          this.weakAnalysis = data.weakAnalysis || {}
          this.dailyQuote = data.dailyQuote || '坚持就是胜利！'
          this.cacheExpireTime = data.cacheExpireTime || null

          this.$nextTick(() => this.initChart())
        }
      } catch (e) {
        console.error('加载首页数据失败:', e)
        this.initDefaultData()
      } finally {
        this.loading = false
      }
    },

    initDefaultData() {
      this.userInfo = { username: '学习者', todayStudyMinutes: 0 }
      this.dailyQuote = '生活就像海洋，只有意志坚强的人才能到达彼岸。'
      this.studyTrend = [
        { date: '05-22', durationMinutes: 120 },
        { date: '05-23', durationMinutes: 90 },
        { date: '05-24', durationMinutes: 150 },
        { date: '05-25', durationMinutes: 80 },
        { date: '05-26', durationMinutes: 180 },
        { date: '05-27', durationMinutes: 60 },
        { date: '05-28', durationMinutes: 110 }
      ]
      this.$nextTick(() => this.initChart())
    },

    async refreshData() {
      this.refreshing = true
      try {
        await refreshHomeCache()
        await this.loadData()
        this.$message.success('数据已刷新')
      } catch (e) {
        console.error(e)
      } finally {
        this.refreshing = false
      }
    },

    async loadAIRecommendations() {
      this.aiLoading = true
      try {
        const res = await getAIRecommendations()
        if (res.code && res.data && res.data.length > 0) {
          this.recommendations = res.data.map((item, idx) => ({
            id: idx + 1,
            type: 'ai',
            title: item.title,
            description: item.description,
            icon: 'el-icon-magic-stick',
            color: ['#6366F1', '#8B5CF6', '#10B981', '#F59E0B'][idx % 4],
            actionUrl: '/exercise-center',
            priority: idx + 1,
            reason: item.reason
          }))
          this.$message.success('AI推荐已更新')
        }
      } catch (e) {
        console.error('AI推荐失败:', e)
        this.$message.warning('AI服务暂时不可用')
      } finally {
        this.aiLoading = false
      }
    },

    async loadRecommendQuestions() {
      this.questionLoading = true
      this.selectedOption = ''
      this.selectedOptions = []
      this.textAnswer = ''
      this.answered = false
      this.isCorrect = false
      try {
        const res = await getAIRecommendQuestions(1)
        if (res.code && res.data) {
          this.recommendQuestions = res.data
        }
      } catch (e) {
        console.error('获取推荐习题失败:', e)
        this.$message.warning('获取习题失败')
      } finally {
        this.questionLoading = false
      }
    },

    getQuestionTypeTag(type) {
      const types = { 1: 'danger', 2: 'warning', 3: 'success' }
      return types[type] || 'info'
    },

    getQuestionTypeText(type) {
      const texts = { 1: '单选题', 2: '多选题', 3: '判断题', 4: '填空题', 5: '简答题' }
      return texts[type] || '其他'
    },

    selectSingleOption(opt) {
      if (this.answered) return
      this.selectedOption = opt.key
    },

    selectMultiOption(opt) {
      if (this.answered) return
      const idx = this.selectedOptions.indexOf(opt.key)
      if (idx >= 0) {
        this.selectedOptions.splice(idx, 1)
      } else {
        this.selectedOptions.push(opt.key)
      }
    },

    getOptionClass(opt) {
      if (!this.answered) {
        return { 'is-selected': this.selectedOption === opt.key }
      }
      return {
        'is-correct-option': opt.isCorrect,
        'is-wrong-option': this.selectedOption === opt.key && !opt.isCorrect
      }
    },

    getMultiOptionClass(opt) {
      if (!this.answered) {
        return { 'is-selected': this.selectedOptions.includes(opt.key) }
      }
      return {
        'is-correct-option': opt.isCorrect,
        'is-wrong-option': this.selectedOptions.includes(opt.key) && !opt.isCorrect
      }
    },

    submitAnswer() {
      if (this.answered || !this.canSubmit) return
      const q = this.currentQuestion
      const t = q.quType

      if (t === 1 || t === 3) {
        // 单选/判断
        const correctOpt = q.options.find(o => o.isCorrect)
        this.isCorrect = correctOpt && correctOpt.key === this.selectedOption
      } else if (t === 2) {
        // 多选
        const correctKeys = q.options.filter(o => o.isCorrect).map(o => o.key).sort()
        const selectedKeys = [...this.selectedOptions].sort()
        this.isCorrect = correctKeys.length === selectedKeys.length &&
          correctKeys.every((k, i) => k === selectedKeys[i])
      } else {
        // 填空/简答 - 不判对错，展示参考答案
        this.isCorrect = false
      }
      this.answered = true
    },

    initChart() {
      const dom = this.$refs.chartRef
      if (!dom) return

      if (this.chart) this.chart.dispose()
      this.chart = echarts.init(dom)

      const dates = this.studyTrend.map(d => d.date)
      const values = this.studyTrend.map(d => d.durationMinutes || 0)

      this.chart.setOption({
        tooltip: { trigger: 'axis', formatter: '{b}: {c}分钟' },
        grid: { left: 40, right: 20, top: 20, bottom: 30 },
        xAxis: {
          type: 'category',
          data: dates,
          axisLine: { lineStyle: { color: 'rgba(0,0,0,0.1)' } },
          axisLabel: { color: '#6B7280', fontSize: 12 }
        },
        yAxis: {
          type: 'value',
          axisLine: { show: false },
          axisTick: { show: false },
          splitLine: { lineStyle: { color: 'rgba(0,0,0,0.05)' } },
          axisLabel: { color: '#6B7280' }
        },
        series: [{
          data: values,
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 8,
          lineStyle: { color: '#6366F1', width: 3 },
          itemStyle: { color: '#6366F1', borderWidth: 2, borderColor: '#fff' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(99,102,241,0.3)' },
              { offset: 1, color: 'rgba(99,102,241,0.05)' }
            ])
          }
        }]
      })
    },

    handleResize() {
      if (this.chart) this.chart.resize()
    },

    formatCacheTime(time) {
      if (!time) return ''
      const d = new Date(time)
      return `${d.getHours()}:${String(d.getMinutes()).padStart(2, '0')}`
    },

    goTo(url) {
      if (url) this.$router.push(url)
    },

    getPriorityType(p) {
      return p === 1 ? 'danger' : p === 2 ? 'warning' : 'info'
    },

    getPriorityText(p) {
      return p === 1 ? '高' : p === 2 ? '中' : '低'
    },

    async toggleTask(task) {
      if (task.isCompleted === 1) return
      try {
        const userId = parseInt(getUserId())
        await completeTask(userId, task.id)
        this.$message.success('任务已完成')
        await this.loadData()
      } catch (e) {
        task.isCompleted = 1
        this.$message.success('任务已完成')
      }
    },

    async addTask() {
      if (!this.taskForm.taskContent.trim()) {
        this.$message.warning('请输入任务内容')
        return
      }
      try {
        const userId = parseInt(getUserId())
        await addTask(userId, this.taskForm)
        this.$message.success('添加成功')
        this.showAddTask = false
        this.taskForm = { taskContent: '', estimatedMinutes: 30, priority: 2, subject: '其他' }
        await this.loadData()
      } catch (e) {
        this.$message.error('添加失败')
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.home-container {
  min-height: 100vh;
  background: var(--bg-primary);
  position: relative;
  overflow: hidden;
}

.bg-decoration {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  pointer-events: none;

  .gradient-orb {
    position: absolute;
    border-radius: 50%;
    filter: blur(100px);
    opacity: 0.3;

    &.orb-1 {
      width: 600px; height: 600px;
      background: linear-gradient(135deg, #6366F1 0%, #8B5CF6 100%);
      top: -200px; right: -100px;
    }
    &.orb-2 {
      width: 400px; height: 400px;
      background: linear-gradient(135deg, #8B5CF6 0%, #EC4899 100%);
      bottom: -100px; left: -100px;
    }
    &.orb-3 {
      width: 300px; height: 300px;
      background: linear-gradient(135deg, #06B6D4 0%, #6366F1 100%);
      top: 50%; left: 50%;
      transform: translate(-50%, -50%);
    }
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
  border: 1px solid var(--glass-border);
  border-radius: 16px;
  transition: all 0.3s ease;
  &:hover { border-color: var(--border-focus); }
}

.welcome-header {
  padding: 20px 28px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .user-section {
    display: flex;
    align-items: center;
    gap: 16px;

    .avatar {
      border: 3px solid rgba(99, 102, 241, 0.5);
    }

    .user-info {
      h1 { margin: 0 0 6px; font-size: 22px; font-weight: 700; color: var(--text-primary); }
      p { margin: 0; font-size: 14px; color: var(--text-secondary); }
    }
  }

  .header-stats {
    display: flex;
    align-items: center;
    gap: 16px;

    .stat-item {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 8px 16px;
      background: var(--bg-hover);
      border-radius: 8px;
      font-size: 14px;
      color: var(--text-secondary);
      i { color: var(--accent); }
    }

    .streak-badge {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 8px 16px;
      background: rgba(251, 146, 60, 0.1);
      border: 1px solid rgba(251, 146, 60, 0.3);
      border-radius: 20px;
      color: #FB923C;
      font-weight: 500;
      .fire { font-size: 18px; }
    }
  }
}

.recommend-section {
  padding: 20px;
  margin-bottom: 20px;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    .header-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 600;
      color: var(--text-primary);
      i { color: var(--accent); }
    }

    .header-actions {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .cache-tip {
      font-size: 12px;
      color: var(--text-secondary);
    }
  }

  .recommend-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;

    @media (max-width: 1200px) { grid-template-columns: repeat(2, 1fr); }
    @media (max-width: 576px) { grid-template-columns: 1fr; }
  }

  .question-single {
    .question-card {
      padding: 20px;
      background: var(--bg-hover);
      border: 1px solid var(--glass-border);
      border-radius: 12px;
      transition: all 0.3s ease;

      &:hover {
        border-color: var(--accent);
        box-shadow: 0 4px 12px rgba(99, 102, 241, 0.15);
      }

      .question-header {
        display: flex;
        gap: 8px;
        margin-bottom: 14px;
      }

      .question-content {
        font-size: 15px;
        color: var(--text-primary);
        line-height: 1.8;
        margin-bottom: 14px;
      }

      .question-options {
        margin-bottom: 14px;

        .option-item {
          display: flex;
          align-items: flex-start;
          gap: 10px;
          padding: 10px 14px;
          margin-bottom: 8px;
          background: rgba(99, 102, 241, 0.03);
          border: 1px solid var(--glass-border);
          border-radius: 8px;
          cursor: pointer;
          transition: all 0.2s;
          position: relative;

          &:hover:not(.is-correct-option):not(.is-wrong-option) {
            border-color: var(--accent);
            background: rgba(99, 102, 241, 0.06);
          }

          &.is-selected {
            border-color: var(--accent);
            background: rgba(99, 102, 241, 0.1);
          }

          &.is-correct-option {
            border-color: #10B981;
            background: rgba(16, 185, 129, 0.08);
            cursor: default;

            .option-check {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 18px;
            height: 18px;
            border: 2px solid var(--glass-border);
            border-radius: 4px;
            flex-shrink: 0;
            transition: all 0.2s;

            &.checked {
              background: var(--accent);
              border-color: var(--accent);

              i { color: #fff; font-size: 12px; }
            }
          }

          .option-key {
              background: #10B981;
            }
          }

          &.is-wrong-option {
            border-color: #EF4444;
            background: rgba(239, 68, 68, 0.08);
            cursor: default;

            .option-key {
              background: #EF4444;
            }
          }

          .option-key {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 24px;
            height: 24px;
            border-radius: 50%;
            background: var(--accent);
            color: #fff;
            font-size: 12px;
            font-weight: 600;
            flex-shrink: 0;
            transition: background 0.2s;
          }

          .option-text {
            font-size: 14px;
            color: var(--text-primary);
            line-height: 1.6;
            flex: 1;
          }

          .result-icon {
            flex-shrink: 0;
            font-size: 18px;
            align-self: center;

            &.correct { color: #10B981; }
            &.wrong { color: #EF4444; }
          }
        }
      }

      .question-input {
        margin-bottom: 14px;
      }

      .question-result {
        margin-bottom: 14px;

        .result-tip {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 10px 14px;
          border-radius: 8px;
          font-size: 14px;
          font-weight: 500;
          margin-bottom: 12px;

          &.is-correct {
            background: rgba(16, 185, 129, 0.1);
            color: #10B981;
          }

          &.is-wrong {
            background: rgba(239, 68, 68, 0.1);
            color: #EF4444;
          }

          i { font-size: 18px; }
        }

        .result-answer {
          padding: 14px;
          background: rgba(99, 102, 241, 0.08);
          border-radius: 8px;
          margin-bottom: 12px;

          .answer-title {
            font-size: 13px;
            font-weight: 600;
            color: var(--accent);
            margin-bottom: 8px;
          }

          .answer-content {
            font-size: 14px;
            color: var(--text-primary);
            line-height: 1.8;
          }
        }

        .result-analysis {
          padding: 14px;
          background: rgba(99, 102, 241, 0.05);
          border-radius: 8px;

          .analysis-title {
            font-size: 13px;
            font-weight: 600;
            color: var(--accent);
            margin-bottom: 8px;
            display: flex;
            align-items: center;
            gap: 6px;
          }

          .analysis-content {
            font-size: 13px;
            color: var(--text-secondary);
            line-height: 1.8;
          }
        }
      }

      .question-reason {
        font-size: 12px;
        color: var(--text-secondary);
        margin-bottom: 14px;
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 8px 12px;
        background: rgba(99, 102, 241, 0.05);
        border-radius: 6px;

        i { color: var(--accent); }
      }

      .question-actions {
        text-align: right;
      }
    }
  }

  .empty-questions {
    text-align: center;
    padding: 40px 20px;
    color: var(--text-secondary);

    i { font-size: 40px; margin-bottom: 12px; display: block; opacity: 0.5; }
    p { margin: 0 0 16px 0; }
  }

  .recommend-card {
    padding: 20px;
    background: linear-gradient(135deg, rgba(99, 102, 241, 0.05) 0%, rgba(139, 92, 246, 0.05) 100%);
    border: 1px solid var(--glass-border);
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(99, 102, 241, 0.2);
      border-color: var(--card-color);
    }

    .card-icon {
      width: 48px;
      height: 48px;
      background: var(--card-color);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 12px;
      i { font-size: 24px; color: white; }
    }

    .card-content {
      h3 { margin: 0 0 6px; font-size: 15px; font-weight: 600; color: var(--text-primary); }
      p { margin: 0; font-size: 13px; color: var(--text-secondary); line-height: 1.5; }
    }

    .card-reason {
      margin-top: 12px;
      padding-top: 12px;
      border-top: 1px dashed var(--glass-border);
      font-size: 12px;
      color: var(--text-hint);
      display: flex;
      align-items: center;
      gap: 6px;
    }
  }
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;

  @media (max-width: 768px) { grid-template-columns: repeat(2, 1fr); }
}

.stat-card {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;

  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    i { font-size: 22px; color: white; }

    &.blue { background: linear-gradient(135deg, #3B82F6 0%, #6366F1 100%); }
    &.green { background: linear-gradient(135deg, #10B981 0%, #34D399 100%); }
    &.orange { background: linear-gradient(135deg, #F59E0B 0%, #FBBF24 100%); }
    &.purple { background: linear-gradient(135deg, #8B5CF6 0%, #A78BFA 100%); }
  }

  .stat-body {
    .stat-value { display: block; font-size: 24px; font-weight: 700; color: var(--text-primary); }
    .stat-label { display: block; font-size: 13px; color: var(--text-secondary); margin-top: 2px; }
  }
}

.main-content {
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: 20px;

  @media (max-width: 1200px) { grid-template-columns: 1fr; }
}

.left-panel, .right-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--glass-border);

  .header-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 15px;
    font-weight: 600;
    color: var(--text-primary);
    i { color: var(--accent); }
  }
}

.chart-card {
  .chart-area { height: 260px; padding: 16px; }
}

.weak-card {
  .weak-list { padding: 16px 20px; }

  .weak-item {
    margin-bottom: 16px;
    &:last-child { margin-bottom: 0; }

    .weak-header {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 8px;
      .weak-name { font-size: 14px; font-weight: 500; color: var(--text-primary); }
    }

    .weak-progress {
      display: flex;
      align-items: center;
      gap: 12px;
      .progress-text { font-size: 12px; color: var(--text-secondary); min-width: 50px; }
    }
  }

  .ai-tip {
    margin: 0 20px 16px;
    padding: 12px 16px;
    background: rgba(99, 102, 241, 0.1);
    border-radius: 8px;
    font-size: 13px;
    color: var(--accent);
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.tasks-card {
  .tasks-list { padding: 12px 20px; }

  .task-item {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    padding: 12px;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover { background: var(--bg-hover); }

    .task-check {
      width: 22px;
      height: 22px;
      border: 2px solid var(--glass-border);
      border-radius: 6px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      i { font-size: 12px; color: white; }
    }

    &.completed .task-check {
      background: var(--success);
      border-color: var(--success);
    }

    &.completed .task-text {
      text-decoration: line-through;
      color: var(--text-hint);
    }

    .task-body {
      flex: 1;
      .task-text { display: block; font-size: 14px; color: var(--text-primary); margin-bottom: 6px; }
      .task-meta {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 12px;
        color: var(--text-secondary);
      }
    }
  }
}

.countdown-card {
  .countdown-list { padding: 16px 20px; }

  .countdown-item {
    margin-bottom: 16px;
    &:last-child { margin-bottom: 0; }

    h4 { margin: 0 0 12px; font-size: 14px; font-weight: 600; color: var(--text-primary); }

    .time-blocks {
      display: flex;
      gap: 8px;

      .time-block {
        flex: 1;
        background: var(--bg-active);
        border: 1px solid var(--border-primary);
        border-radius: 8px;
        padding: 10px 8px;
        text-align: center;

        .num { display: block; font-size: 20px; font-weight: 700; color: var(--accent); }
        .label { display: block; font-size: 10px; color: var(--text-secondary); margin-top: 2px; }
      }
    }
  }
}

.quote-card {
  .quote-text {
    margin: 0;
    padding: 20px;
    font-size: 14px;
    color: var(--text-secondary);
    line-height: 1.8;
    text-align: center;
    font-style: italic;
  }
}

.empty-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 20px;
  color: var(--text-hint);
  i { font-size: 40px; margin-bottom: 12px; opacity: 0.5; }
  p { margin: 0; font-size: 14px; }
}

::v-deep .el-progress {
  .el-progress-bar__outer { background: var(--bg-hover); }
  .el-progress-bar__inner { background: var(--accent-gradient); }
}

::v-deep .el-tag { border: none; }
</style>
