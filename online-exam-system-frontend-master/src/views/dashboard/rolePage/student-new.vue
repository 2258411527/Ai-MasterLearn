<template>
  <div v-loading="loading" class="ai-learning-platform">
    <!-- 顶部欢迎区域 -->
    <el-alert
      v-if="loadError"
      :title="loadError"
      type="error"
      show-icon
      closable
      style="margin-bottom: 20px;"
    >
      <el-button size="small" type="primary" @click="retryLoad">重试</el-button>
    </el-alert>

    <div class="welcome-section">
      <el-card class="welcome-card glass-effect" shadow="hover">
        <div class="welcome-content">
          <div class="welcome-text">
            <h1>🎓 AI个性化学习平台</h1>
            <p>基于您的学习数据，为您量身定制学习路径</p>
          </div>
          <div class="user-stats">
            <div class="stat-item">
              <i class="el-icon-time" />
              <span>已学习 {{ totalStudyDays }} 天</span>
            </div>
            <div class="stat-item">
              <i class="el-icon-success" />
              <span>正确率 {{ overallCorrectRate }}%</span>
            </div>
            <div class="stat-item">
              <i class="el-icon-trophy" />
              <span>完成 {{ completedTasks }} 项任务</span>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 主要内容区 -->
    <div class="main-content">
      <!-- 左侧：AI推荐区 -->
      <div class="ai-recommendation-section">
        <!-- 今日推荐任务 -->
        <el-card class="recommendation-card glass-effect" shadow="hover">
          <div slot="header" class="card-header">
            <i class="el-icon-star-on" />
            <span>今日推荐任务</span>
            <el-button type="text" icon="el-icon-refresh" @click="refreshRecommendations" />
          </div>
          <div class="recommendation-list">
            <div v-for="(task, index) in todayRecommendations" :key="index" class="recommendation-item">
              <div class="task-icon">
                <i :class="task.icon" />
              </div>
              <div class="task-info">
                <div class="task-title">{{ task.title }}</div>
                <div class="task-desc">{{ task.description }}</div>
                <div class="task-meta">
                  <span class="priority" :class="'priority-' + task.priority">{{ task.priorityText }}</span>
                  <span class="duration">{{ task.duration }}</span>
                </div>
              </div>
              <el-button type="primary" size="mini" @click="startTask(task)">开始</el-button>
            </div>
          </div>
        </el-card>

        <!-- 学习路径规划 -->
        <el-card class="path-card glass-effect" shadow="hover">
          <div slot="header" class="card-header">
            <i class="el-icon-map-location" />
            <span>个性化学习路径</span>
          </div>
          <div class="path-content">
            <div class="path-stages">
              <div v-for="(stage, index) in learningPath" :key="index" class="path-stage">
                <div class="stage-icon" :class="{ active: stage.completed, current: stage.current }">
                  <i :class="stage.icon" />
                </div>
                <div class="stage-info">
                  <div class="stage-title">{{ stage.title }}</div>
                  <div class="stage-progress">
                    <el-progress :percentage="stage.progress" :show-text="false" />
                  </div>
                  <div class="stage-desc">{{ stage.description }}</div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 右侧：数据可视化区 -->
      <div class="data-visualization-section">
        <!-- 学习数据概览 -->
        <el-card class="overview-card glass-effect" shadow="hover">
          <div slot="header" class="card-header">
            <i class="el-icon-data-analysis" />
            <span>学习数据概览</span>
          </div>
          <div class="overview-content">
            <div class="overview-charts">
              <div class="chart-container">
                <div ref="studyTrendChart" class="chart-div" />
              </div>
              <div class="chart-container">
                <div ref="subjectDistributionChart" class="chart-div" />
              </div>
            </div>
            <div class="overview-stats">
              <div class="stat-card">
                <i class="el-icon-document-checked" />
                <div class="stat-content">
                  <div class="stat-value">{{ totalQuestions }}</div>
                  <div class="stat-label">累计答题</div>
                </div>
              </div>
              <div class="stat-card">
                <i class="el-icon-chat-dot-round" />
                <div class="stat-content">
                  <div class="stat-value">{{ aiChatCount }}</div>
                  <div class="stat-label">AI咨询次数</div>
                </div>
              </div>
              <div class="stat-card">
                <i class="el-icon-medal" />
                <div class="stat-content">
                  <div class="stat-value">{{ streakDays }}</div>
                  <div class="stat-label">连续学习天数</div>
                </div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 薄弱知识点分析 -->
        <el-card class="weakness-card glass-effect" shadow="hover">
          <div slot="header" class="card-header">
            <i class="el-icon-warning-outline" />
            <span>薄弱知识点分析</span>
          </div>
          <div class="weakness-content">
            <div v-for="(weakness, index) in weakKnowledgePoints" :key="index" class="weakness-item">
              <div class="weakness-header">
                <span class="weakness-name">{{ weakness.name }}</span>
                <span class="weakness-rate">正确率: {{ weakness.correctRate }}%</span>
              </div>
              <el-progress
                :percentage="weakness.masteryLevel"
                :color="getWeaknessColor(weakness.masteryLevel)"
                :show-text="false"
              />
              <div class="weakness-suggestion">
                <i class="el-icon-lightbulb" />
                <span>{{ weakness.suggestion }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 底部：AI学习助手 -->
    <div class="ai-assistant-section">
      <el-card class="assistant-card glass-effect" shadow="hover">
        <div slot="header" class="card-header">
          <i class="el-icon-chat-line-round" />
          <span>AI学习助手</span>
        </div>
        <div class="assistant-content">
          <div class="assistant-message">
            <div class="message-avatar">🤖</div>
            <div class="message-content">
              <p>{{ aiMessage }}</p>
              <div class="message-time">{{ currentTime }}</div>
            </div>
          </div>
          <div class="assistant-actions">
            <el-button type="primary" icon="el-icon-chat-dot-round" @click="chatWithAI">咨询AI</el-button>
            <el-button icon="el-icon-refresh" @click="generateNewMessage">换一条建议</el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import { getLearningData, getAIRecommendations, getWeakKnowledgePoints, getAIMessage } from '@/api/ai-learning'
import * as echarts from 'echarts'

export default {
  name: 'AILearningPlatform',
  data() {
    return {
      // 用户统计数据
      totalStudyDays: 0,
      overallCorrectRate: 0,
      completedTasks: 0,
      totalQuestions: 0,
      aiChatCount: 0,
      streakDays: 0,

      // AI推荐数据
      todayRecommendations: [],
      learningPath: [],
      weakKnowledgePoints: [],

      // AI助手
      aiMessage: '',
      currentTime: '',

      // 图表实例
      studyTrendChart: null,
      subjectDistributionChart: null,

      // 图表配置
      studyTrendOption: {
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: []
        },
        yAxis: {
          type: 'value',
          name: '学习时长(分钟)'
        },
        series: [{
          name: '学习时长',
          type: 'line',
          smooth: true,
          data: [],
          lineStyle: { color: '#409EFF' },
          areaStyle: {
            color: {
              type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
                { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
              ]
            }
          }
        }]
      },

      subjectDistributionOption: {
        tooltip: {
          trigger: 'item'
        },
        series: [{
          name: '科目分布',
          type: 'pie',
          radius: ['40%', '70%'],
          data: [],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }]
      },

      // 加载状态
      loading: true,
      loadError: null
    }
  },

  created() {
    this.loadAllData()
    this.updateCurrentTime()
    // 每分钟更新一次时间
    setInterval(this.updateCurrentTime, 60000)
  },

  mounted() {
    this.initCharts()
    window.addEventListener('resize', this.resizeCharts)
  },

  beforeDestroy() {
    window.removeEventListener('resize', this.resizeCharts)
    if (this.studyTrendChart) {
      this.studyTrendChart.dispose()
    }
    if (this.subjectDistributionChart) {
      this.subjectDistributionChart.dispose()
    }
  },

  methods: {
    // 重试加载
    retryLoad() {
      this.loadError = null
      this.loading = true
      this.loadAllData()
    },

    // 加载所有数据
    async loadAllData() {
      try {
        this.loading = true
        this.loadError = null

        await Promise.all([
          this.loadLearningData(),
          this.loadAIRecommendations(),
          this.loadWeakKnowledgePoints(),
          this.loadAIMessage()
        ])

        this.loading = false
      } catch (error) {
        console.error('加载数据失败:', error)
        this.loadError = '数据加载失败: ' + (error.message || '未知错误')
        this.loading = false
        this.$message.error('数据加载失败，请点击重试')
      }
    },

    // 加载学习数据
    async loadLearningData() {
      try {
        const res = await getLearningData()
        if (res.code === 1) {
          const data = res.data || {}
          this.totalStudyDays = data.totalStudyDays || 0
          this.overallCorrectRate = data.overallCorrectRate || 0
          this.completedTasks = data.completedTasks || 0
          this.totalQuestions = data.totalQuestions || 0
          this.aiChatCount = data.aiChatCount || 0
          this.streakDays = data.streakDays || 0

          // 更新图表数据
          this.updateStudyTrendChart(data.studyTrend)
          this.updateSubjectDistributionChart(data.subjectDistribution)
        } else {
          console.warn('获取学习数据失败:', res.msg)
        }
      } catch (error) {
        console.error('加载学习数据异常:', error)
        throw error
      }
    },

    // 加载AI推荐
    async loadAIRecommendations() {
      try {
        const res = await getAIRecommendations()
        if (res.code === 1) {
          this.todayRecommendations = res.data?.todayRecommendations || []
          this.learningPath = res.data?.learningPath || []
        } else {
          console.warn('获取AI推荐失败:', res.msg)
        }
      } catch (error) {
        console.error('加载AI推荐异常:', error)
        throw error
      }
    },

    // 加载薄弱知识点
    async loadWeakKnowledgePoints() {
      try {
        const res = await getWeakKnowledgePoints()
        if (res.code === 1) {
          this.weakKnowledgePoints = res.data || []
        } else {
          console.warn('获取薄弱知识点失败:', res.msg)
        }
      } catch (error) {
        console.error('加载薄弱知识点异常:', error)
        throw error
      }
    },

    // 加载AI消息
    async loadAIMessage() {
      try {
        const res = await getAIMessage()
        if (res.code === 1) {
          this.aiMessage = res.data?.message || '欢迎使用AI学习平台！'
        } else {
          console.warn('获取AI消息失败:', res.msg)
          this.aiMessage = '欢迎使用AI学习平台！'
        }
      } catch (error) {
        console.error('加载AI消息异常:', error)
        this.aiMessage = '欢迎使用AI学习平台！'
        throw error
      }
    },

    // 初始化图表
    initCharts() {
      this.studyTrendChart = echarts.init(this.$refs.studyTrendChart)
      this.subjectDistributionChart = echarts.init(this.$refs.subjectDistributionChart)

      this.studyTrendChart.setOption(this.studyTrendOption)
      this.subjectDistributionChart.setOption(this.subjectDistributionOption)
    },

    // 更新学习趋势图表
    updateStudyTrendChart(trendData) {
      if (trendData && trendData.length > 0) {
        const dates = trendData.map(item => item.date)
        const durations = trendData.map(item => item.duration)

        this.studyTrendOption.xAxis.data = dates
        this.studyTrendOption.series[0].data = durations
        this.studyTrendChart.setOption(this.studyTrendOption)
      }
    },

    // 更新科目分布图表
    updateSubjectDistributionChart(distributionData) {
      if (distributionData && distributionData.length > 0) {
        this.subjectDistributionOption.series[0].data = distributionData
        this.subjectDistributionChart.setOption(this.subjectDistributionOption)
      }
    },

    // 调整图表大小
    resizeCharts() {
      if (this.studyTrendChart) {
        this.studyTrendChart.resize()
      }
      if (this.subjectDistributionChart) {
        this.subjectDistributionChart.resize()
      }
    },

    // 刷新推荐
    refreshRecommendations() {
      this.loadAIRecommendations()
      this.$message.success('推荐内容已更新')
    },

    // 开始任务
    startTask(task) {
      this.$message.success(`开始任务: ${task.title}`)
      // 根据任务类型跳转到相应页面
      if (task.type === 'exercise') {
        this.$router.push('/exercise-center')
      } else if (task.type === 'exam') {
        this.$router.push('/exam-center')
      } else if (task.type === 'review') {
        this.$router.push('/wrong-questions')
      }
    },

    // 获取薄弱知识点颜色
    getWeaknessColor(masteryLevel) {
      if (masteryLevel >= 80) return '#67C23A'
      if (masteryLevel >= 60) return '#E6A23C'
      return '#F56C6C'
    },

    // 更新当前时间
    updateCurrentTime() {
      const now = new Date()
      this.currentTime = now.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },

    // 与AI聊天
    chatWithAI() {
      this.$router.push('/ai-chat')
    },

    // 生成新消息
    generateNewMessage() {
      this.loadAIMessage()
    }
  }
}
</script>

<style scoped>
.ai-learning-platform {
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
}

.welcome-section {
  margin-bottom: 20px;
}

.welcome-card {
  border: none;
  background: rgba(255, 255, 255, 0.95);
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-text h1 {
  margin: 0;
  color: #303133;
  font-size: 24px;
}

.welcome-text p {
  margin: 5px 0 0 0;
  color: #606266;
}

.user-stats {
  display: flex;
  gap: 30px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #409EFF;
  font-weight: 500;
}

.main-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.ai-recommendation-section,
.data-visualization-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.glass-effect {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;
}

.recommendation-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.recommendation-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.recommendation-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.task-icon {
  font-size: 24px;
  color: #409EFF;
}

.task-info {
  flex: 1;
}

.task-title {
  font-weight: 600;
  margin-bottom: 5px;
}

.task-desc {
  color: #606266;
  font-size: 12px;
  margin-bottom: 5px;
}

.task-meta {
  display: flex;
  gap: 10px;
  font-size: 12px;
}

.priority {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 10px;
}

.priority-high { background: #fef0f0; color: #f56c6c; }
.priority-medium { background: #fdf6ec; color: #e6a23c; }
.priority-low { background: #f0f9ff; color: #409EFF; }

.path-stages {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.path-stage {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stage-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #909399;
  font-size: 20px;
  transition: all 0.3s;
}

.stage-icon.active {
  background: #67C23A;
  color: white;
}

.stage-icon.current {
  background: #409EFF;
  color: white;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

.stage-info {
  flex: 1;
}

.stage-title {
  font-weight: 600;
  margin-bottom: 5px;
}

.stage-desc {
  color: #606266;
  font-size: 12px;
}

.overview-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.overview-charts {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.chart-container {
  height: 200px;
}

.chart-div {
  width: 100%;
  height: 100%;
}

.overview-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-card i {
  font-size: 24px;
  color: #409EFF;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  font-size: 12px;
  color: #606266;
}

.weakness-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.weakness-item {
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.weakness-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.weakness-name {
  font-weight: 600;
}

.weakness-rate {
  color: #f56c6c;
  font-size: 12px;
}

.weakness-suggestion {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
  font-size: 12px;
  color: #606266;
}

.weakness-suggestion i {
  color: #e6a23c;
}

.ai-assistant-section {
  margin-bottom: 20px;
}

.assistant-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.assistant-message {
  display: flex;
  gap: 15px;
  align-items: flex-start;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #409EFF;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: white;
}

.message-content {
  flex: 1;
}

.message-content p {
  margin: 0;
  color: #303133;
  line-height: 1.5;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.assistant-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

@media (max-width: 1200px) {
  .main-content {
    grid-template-columns: 1fr;
  }

  .overview-charts {
    grid-template-columns: 1fr;
  }

  .overview-stats {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .welcome-content {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }

  .user-stats {
    justify-content: center;
  }

  .overview-stats {
    grid-template-columns: 1fr;
  }
}
</style>
