<template>
  <div v-loading="loading" class="ai-learning-platform">
    <!-- 问卷引导弹窗 -->
    <el-dialog
      v-if="showQuestionnaire"
      title="🎯 个性化学习规划"
      :visible.sync="showQuestionnaire"
      width="800px"
      :before-close="handleQuestionnaireClose"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <user-questionnaire-form 
        @submit-success="handleQuestionnaireSuccess"
        @skip="handleQuestionnaireSkip"
      />
    </el-dialog>

    <!-- 顶部欢迎区域 -->
    <div class="welcome-section">
      <el-card class="welcome-card glass-effect" shadow="hover">
        <div class="welcome-content">
          <div class="welcome-text">
            <h1>🎓 AI个性化学习平台</h1>
            <p v-if="userProfile.goalType">基于您的{{ userProfile.goalType }}目标，为您量身定制学习路径</p>
            <p v-else>基于您的学习数据，为您量身定制学习路径</p>
          </div>
          <div class="user-stats">
            <div class="stat-item">
              <i class="el-icon-time" />
              <span>已学习 {{ userAnalysis.totalStudyDays || 0 }} 天</span>
            </div>
            <div class="stat-item">
              <i class="el-icon-success" />
              <span>正确率 {{ userAnalysis.correctRate || 0 }}%</span>
            </div>
            <div class="stat-item">
              <i class="el-icon-trophy" />
              <span>完成 {{ userAnalysis.completedTasks || 0 }} 项任务</span>
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
            <div v-for="(task, index) in aiRecommendations.todayTasks" :key="index" class="recommendation-item">
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
            <div v-if="!aiRecommendations.todayTasks || aiRecommendations.todayTasks.length === 0" class="empty-recommendations">
              <i class="el-icon-info" />
              <span>暂无推荐任务，请先完成学习问卷</span>
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
              <div v-for="(stage, index) in aiRecommendations.learningPath" :key="index" class="path-stage">
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
                  <div class="stat-value">{{ userAnalysis.totalQuestions || 0 }}</div>
                  <div class="stat-label">累计答题</div>
                </div>
              </div>
              <div class="stat-card">
                <i class="el-icon-chat-dot-round" />
                <div class="stat-content">
                  <div class="stat-value">{{ userAnalysis.aiChatCount || 0 }}</div>
                  <div class="stat-label">AI咨询次数</div>
                </div>
              </div>
              <div class="stat-card">
                <i class="el-icon-medal" />
                <div class="stat-content">
                  <div class="stat-value">{{ userAnalysis.streakDays || 0 }}</div>
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
            <div v-for="(weakness, index) in userAnalysis.weakKnowledgePoints" :key="index" class="weakness-item">
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
            <div v-if="!userAnalysis.weakKnowledgePoints || userAnalysis.weakKnowledgePoints.length === 0" class="empty-weakness">
              <i class="el-icon-info" />
              <span>暂无薄弱知识点分析数据</span>
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
import { 
  checkQuestionnaireCompleted, 
  getStudyDashboard, 
  getUserProfile, 
  getUserStudyAnalysis, 
  getAiRecommendations,
  generateAiRecommendations,
  updateUserLearningBehavior
} from '@/api/user-questionnaire'
import * as echarts from 'echarts'

// 导入问卷组件
const UserQuestionnaireForm = () => import('@/components/user-questionnaire/QuestionnaireForm.vue')

export default {
  name: 'AILearningPlatform',
  components: {
    UserQuestionnaireForm
  },
  data() {
    return {
      // 用户数据
      userProfile: {},
      userAnalysis: {},
      aiRecommendations: {},
      
      // AI助手
      aiMessage: '',
      currentTime: '',

      // 问卷状态
      showQuestionnaire: false,
      questionnaireCompleted: false,

      // 图表实例
      studyTrendChart: null,
      subjectDistributionChart: null,

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
    // 加载所有数据
    async loadAllData() {
      try {
        this.loading = true
        this.loadError = null

        // 1. 检查问卷状态
        await this.checkQuestionnaireStatus()
        
        // 2. 如果已完成问卷，加载所有数据
        if (this.questionnaireCompleted) {
          await Promise.all([
            this.loadUserProfile(),
            this.loadStudyAnalysis(),
            this.loadAiRecommendations(),
            this.loadAIMessage()
          ])
        }

        this.loading = false
      } catch (error) {
        console.error('加载数据失败:', error)
        this.loadError = '数据加载失败: ' + (error.message || '未知错误')
        this.loading = false
        this.$message.error('数据加载失败，请刷新页面重试')
      }
    },

    // 检查问卷状态
    async checkQuestionnaireStatus() {
      try {
        const res = await checkQuestionnaireCompleted()
        if (res.code === 1) {
          this.questionnaireCompleted = res.data
          this.showQuestionnaire = !this.questionnaireCompleted
        }
      } catch (error) {
        console.error('检查问卷状态失败:', error)
        // 如果检查失败，默认显示问卷
        this.showQuestionnaire = true
      }
    },

    // 加载用户画像数据
    async loadUserProfile() {
      try {
        const res = await getUserProfile()
        if (res.code === 1) {
          this.userProfile = res.data || {}
        }
      } catch (error) {
        console.error('加载用户画像失败:', error)
      }
    },

    // 加载学习分析数据
    async loadStudyAnalysis() {
      try {
        const res = await getUserStudyAnalysis()
        if (res.code === 1) {
          this.userAnalysis = res.data || {}
          this.updateCharts()
        }
      } catch (error) {
        console.error('加载学习分析失败:', error)
      }
    },

    // 加载AI推荐数据
    async loadAiRecommendations() {
      try {
        const res = await getAiRecommendations()
        if (res.code === 1) {
          this.aiRecommendations = res.data || {}
        }
      } catch (error) {
        console.error('加载AI推荐失败:', error)
      }
    },

    // 加载AI消息
    async loadAIMessage() {
      try {
        // 根据用户画像生成个性化消息
        if (this.userProfile.goalType) {
          const goalMessages = {
            '考研': '考研之路充满挑战，但坚持就是胜利！根据您的学习情况，我建议重点加强专业课和英语的复习。',
            '考公': '考公需要系统性的准备，建议您按照行测、申论的模块进行针对性训练。',
            '双线备考': '双线备考需要合理的时间安排，建议您制定详细的学习计划，平衡好两个方向的学习。'
          }
          this.aiMessage = goalMessages[this.userProfile.goalType] || '欢迎使用AI学习平台！我将根据您的学习情况为您提供个性化建议。'
        } else {
          this.aiMessage = '欢迎使用AI学习平台！请先完成学习问卷，让我更好地了解您的需求。'
        }
      } catch (error) {
        console.error('生成AI消息失败:', error)
        this.aiMessage = '欢迎使用AI学习平台！我将根据您的学习情况为您提供个性化建议。'
      }
    },

    // 问卷提交成功处理
    handleQuestionnaireSuccess() {
      this.showQuestionnaire = false
      this.questionnaireCompleted = true
      this.$message.success('问卷提交成功！正在为您生成个性化学习计划...')
      
      // 重新加载数据
      this.loadAllData()
      
      // 生成AI推荐
      this.generateAiRecommendations()
    },

    // 跳过问卷处理
    handleQuestionnaireSkip() {
      this.showQuestionnaire = false
      this.$message.info('您可以稍后在个人中心完成问卷')
    },

    // 关闭问卷弹窗处理
    handleQuestionnaireClose(done) {
      this.$confirm('完成问卷可以获得更精准的学习推荐，确定要跳过吗？', '提示', {
        confirmButtonText: '确定跳过',
        cancelButtonText: '继续填写',
        type: 'warning'
      }).then(() => {
        done()
        this.handleQuestionnaireSkip()
      }).catch(() => {
        // 继续填写
      })
    },

    // 生成AI推荐
    async generateAiRecommendations() {
      try {
        await generateAiRecommendations()
        this.$message.success('AI推荐生成成功！')
        this.loadAiRecommendations()
      } catch (error) {
        console.error('生成AI推荐失败:', error)
        this.$message.error('生成AI推荐失败')
      }
    },

    // 刷新推荐
    refreshRecommendations() {
      this.generateAiRecommendations()
    },

    // 开始任务
    startTask(task) {
      this.$message.info(`开始任务: ${task.title}`)
      // 记录学习行为
      this.updateLearningBehavior({
        taskId: task.id,
        taskType: task.type,
        startTime: new Date().toISOString()
      })
    },

    // 更新学习行为
    async updateLearningBehavior(data) {
      try {
        await updateUserLearningBehavior(data)
      } catch (error) {
        console.error('更新学习行为失败:', error)
      }
    },

    // 咨询AI
    chatWithAI() {
      this.$router.push('/ai-chat')
    },

    // 生成新消息
    generateNewMessage() {
      this.loadAIMessage()
    },

    // 更新当前时间
    updateCurrentTime() {
      this.currentTime = new Date().toLocaleString('zh-CN')
    },

    // 初始化图表
    initCharts() {
      this.studyTrendChart = echarts.init(this.$refs.studyTrendChart)
      this.subjectDistributionChart = echarts.init(this.$refs.subjectDistributionChart)
    },

    // 更新图表
    updateCharts() {
      if (this.studyTrendChart && this.userAnalysis.studyTrend) {
        const option = {
          tooltip: { trigger: 'axis' },
          xAxis: { type: 'category', data: this.userAnalysis.studyTrend.dates },
          yAxis: { type: 'value', name: '学习时长(分钟)' },
          series: [{
            name: '学习时长',
            type: 'line',
            smooth: true,
            data: this.userAnalysis.studyTrend.durations,
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
        }
        this.studyTrendChart.setOption(option)
      }

      if (this.subjectDistributionChart && this.userAnalysis.subjectDistribution) {
        const option = {
          tooltip: { trigger: 'item' },
          series: [{
            name: '科目分布',
            type: 'pie',
            radius: ['40%', '70%'],
            data: this.userAnalysis.subjectDistribution,
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }]
        }
        this.subjectDistributionChart.setOption(option)
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

    // 获取薄弱知识点颜色
    getWeaknessColor(level) {
      if (level >= 80) return '#67C23A'
      if (level >= 60) return '#E6A23C'
      return '#F56C6C'
    }
  }
}
</script>

<style scoped>
.ai-learning-platform {
  padding: 20px;
  min-height: calc(100vh - 84px);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.welcome-section {
  margin-bottom: 20px;
}

.welcome-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-text h1 {
  margin: 0;
  color: #303133;
  font-size: 28px;
}

.welcome-text p {
  margin: 10px 0 0 0;
  color: #606266;
  font-size: 16px;
}

.user-stats {
  display: flex;
  gap: 30px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 14px;
}

.stat-item i {
  font-size: 18px;
  color: #409EFF;
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

.card-header i {
  color: #409EFF;
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
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.recommendation-item:hover {
  background: #e9ecef;
  transform: translateY(-2px);
}

.task-icon i {
  font-size: 24px;
  color: #409EFF;
}

.task-info {
  flex: 1;
}

.task-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.task-desc {
  color: #606266;
  font-size: 12px;
  margin-bottom: 6px;
}

.task-meta {
  display: flex;
  gap: 10px;
  font-size: 12px;
}

.priority {
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 10px;
}

.priority-1 { background: #f0f9ff; color: #1890ff; }
.priority-2 { background: #f6ffed; color: #52c41a; }
.priority-3 { background: #fff7e6; color: #fa8c16; }

.duration {
  color: #909399;
}

.empty-recommendations,
.empty-weakness {
  text-align: center;
  padding: 40px;
  color: #909399;
}

.empty-recommendations i,
.empty-weakness i {
  font-size: 48px;
  margin-bottom: 10px;
  display: block;
}

.path-content {
  padding: 10px 0;
}

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
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
  color: #909399;
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
  color: #303133;
  margin-bottom: 4px;
}

.stage-progress {
  margin-bottom: 4px;
}

.stage-desc {
  color: #606266;
  font-size: 12px;
}

.overview-content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}

.overview-charts {
  display: flex;
  flex-direction: column;
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
  display: flex;
  flex-direction: column;
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

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  color: #606266;
  font-size: 12px;
}

.weakness-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.weakness-item {
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.weakness-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.weakness-name {
  font-weight: 600;
  color: #303133;
}

.weakness-rate {
  color: #606266;
  font-size: 12px;
}

.weakness-suggestion {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
}

.weakness-suggestion i {
  color: #E6A23C;
}

.ai-assistant-section {
  margin-bottom: 20px;
}

.assistant-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
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
  font-size: 20px;
  color: white;
}

.message-content {
  flex: 1;
  background: #f0f2f5;
  padding: 12px;
  border-radius: 8px;
}

.message-content p {
  margin: 0;
  color: #303133;
}

.message-time {
  margin-top: 5px;
  color: #909399;
  font-size: 12px;
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
  
  .overview-content {
    grid-template-columns: 1fr;
  }
  
  .welcome-content {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }
  
  .user-stats {
    justify-content: center;
  }
}

@media (max-width: 768px) {
  .ai-learning-platform {
    padding: 10px;
  }
  
  .user-stats {
    flex-direction: column;
    gap: 10px;
  }
  
  .recommendation-item {
    flex-direction: column;
    text-align: center;
    gap: 10px;
  }
  
  .assistant-message {
    flex-direction: column;
    text-align: center;
  }
}
</style>