<template>
  <div class="app-container ai-dashboard">
    <!-- 顶部欢迎区域 -->
    <div class="welcome-section">
      <el-card class="welcome-card glass-effect">
        <div class="welcome-content">
          <div class="user-info">
            <el-avatar :size="60" :src="userInfo.avatar" class="user-avatar" />
            <div class="user-details">
              <h2>欢迎回来，{{ userInfo.username }}！</h2>
              <p class="study-goal">{{ studyGoal.examType }} · {{ studyGoal.targetYear }}年 · {{ studyGoal.studyIdentity }}</p>
              <p class="study-progress">已坚持学习 {{ dashboardData.totalStudyDays }} 天，日均学习 {{ dashboardData.avgDailyStudyHours }} 小时</p>
            </div>
          </div>
          <div class="quick-stats">
            <div class="stat-item">
              <div class="stat-value">{{ dashboardData.totalExamsTaken }}</div>
              <div class="stat-label">参加考试</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ dashboardData.totalQuestionsAnswered }}</div>
              <div class="stat-label">答题总数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ dashboardData.correctRate }}%</div>
              <div class="stat-label">正确率</div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 学习进度和AI推荐区域 -->
    <div class="main-content">
      <!-- 左侧：学习进度和知识点 -->
      <div class="left-panel">
        <!-- 学习进度卡片 -->
        <el-card class="progress-card glass-effect">
          <div slot="header" class="card-header">
            <i class="el-icon-timer" />
            <span>学习进度</span>
          </div>
          <div class="progress-content">
            <div class="progress-bar-container">
              <div class="progress-info">
                <span class="progress-text">备考进度</span>
                <span class="progress-percentage">{{ dashboardData.studyProgress.progressPercentage }}%</span>
              </div>
              <el-progress
                :percentage="dashboardData.studyProgress.progressPercentage"
                :stroke-width="12"
                color="#409EFF"
                :show-text="false"
              />
              <div class="progress-days">
                <span>已坚持 {{ dashboardData.studyProgress.completedDays }} 天</span>
                <span>/ 总 {{ dashboardData.studyProgress.totalDays }} 天</span>
              </div>
              <p class="progress-description">{{ dashboardData.studyProgress.progressDescription }}</p>
            </div>
          </div>
        </el-card>

        <!-- 知识点掌握情况 -->
        <el-card class="knowledge-card glass-effect">
          <div slot="header" class="card-header">
            <i class="el-icon-notebook-2" />
            <span>知识点掌握情况</span>
          </div>
          <div class="knowledge-content">
            <div
              v-for="point in dashboardData.knowledgePoints"
              :key="point.pointName"
              class="knowledge-item"
            >
              <div class="point-info">
                <span class="point-name">{{ point.pointName }}</span>
                <span class="point-status" :class="getStatusClass(point.status)">{{ point.status }}</span>
              </div>
              <div class="point-stats">
                <span class="point-rate">{{ point.masteryLevel }}%</span>
                <span class="point-count">{{ point.correctCount }}/{{ point.questionCount }}</span>
              </div>
              <el-progress
                :percentage="point.masteryLevel"
                :stroke-width="6"
                :color="getProgressColor(point.masteryLevel)"
              />
            </div>
          </div>
        </el-card>
      </div>

      <!-- 右侧：AI推荐和薄弱点 -->
      <div class="right-panel">
        <!-- AI个性化推荐 -->
        <el-card class="recommendation-card glass-effect">
          <div slot="header" class="card-header">
            <i class="el-icon-magic-stick" />
            <span>AI个性化推荐</span>
            <el-button type="text" size="mini" @click="refreshRecommendations">刷新</el-button>
          </div>
          <div class="recommendation-content">
            <div
              v-for="rec in dashboardData.aiRecommendations"
              :key="rec.title"
              class="recommendation-item"
              :class="{ completed: rec.isCompleted }"
            >
              <div class="rec-icon">
                <i :class="getRecommendationIcon(rec.type)" />
              </div>
              <div class="rec-content">
                <h4 class="rec-title">{{ rec.title }}</h4>
                <p class="rec-desc">{{ rec.content }}</p>
                <div class="rec-actions">
                  <el-button
                    type="primary"
                    size="mini"
                    :disabled="rec.isCompleted"
                    @click="handleRecommendation(rec)"
                  >
                    {{ rec.isCompleted ? '已完成' : '开始' }}
                  </el-button>
                  <el-tag size="mini" :type="getPriorityType(rec.priority)">
                    P{{ rec.priority }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 薄弱点分析 -->
        <el-card class="weakpoint-card glass-effect">
          <div slot="header" class="card-header">
            <i class="el-icon-warning-outline" />
            <span>薄弱点分析</span>
          </div>
          <div class="weakpoint-content">
            <div
              v-for="weak in dashboardData.weakPoints"
              :key="weak.pointName"
              class="weakpoint-item"
            >
              <div class="weakpoint-header">
                <span class="weakpoint-name">{{ weak.pointName }}</span>
                <el-tag size="mini" type="danger">薄弱度 {{ weak.weaknessLevel }}%</el-tag>
              </div>
              <p class="weakpoint-suggestion">{{ weak.suggestion }}</p>
              <div class="weakpoint-actions">
                <el-button type="text" size="mini" @click="practiceWeakPoint(weak)">专项练习</el-button>
                <el-button type="text" size="mini" @click="viewDetails(weak)">查看详情</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 底部：今日任务和学习趋势 -->
    <div class="bottom-section">
      <!-- 今日任务 -->
      <el-card class="task-card glass-effect">
        <div slot="header" class="card-header">
          <i class="el-icon-s-check" />
          <span>今日任务</span>
        </div>
        <div class="task-content">
          <div
            v-for="task in dashboardData.dailyTasks"
            :key="task.taskName"
            class="task-item"
            :class="{ completed: task.isCompleted }"
          >
            <el-checkbox v-model="task.isCompleted" @change="updateTaskStatus(task)">
              <div class="task-info">
                <span class="task-name">{{ task.taskName }}</span>
                <span class="task-desc">{{ task.description }}</span>
                <span class="task-time">{{ task.estimatedTime }}分钟 · {{ task.category }}</span>
              </div>
            </el-checkbox>
          </div>
        </div>
      </el-card>

      <!-- 学习趋势 -->
      <el-card class="trend-card glass-effect">
        <div slot="header" class="card-header">
          <i class="el-icon-trend-charts" />
          <span>学习趋势</span>
        </div>
        <div ref="trendChart" class="chart-container" />
        <p class="trend-description">{{ dashboardData.learningTrend.trendDescription }}</p>
      </el-card>
    </div>
  </div>
</template>

<script>
import { getStudyDashboard, generateAiRecommendations } from '@/api/user-questionnaire'
import { getUserInfo } from '@/api/user'
import * as echarts from 'echarts'

export default {
  name: 'AiStudentDashboard',
  data() {
    return {
      userInfo: {
        username: '',
        avatar: ''
      },
      studyGoal: {
        examType: '',
        targetYear: '',
        studyIdentity: ''
      },
      dashboardData: {
        totalStudyDays: 0,
        totalStudyHours: 0,
        avgDailyStudyHours: 0,
        totalExamsTaken: 0,
        totalQuestionsAnswered: 0,
        correctRate: 0,
        studyProgress: {
          completedDays: 0,
          totalDays: 0,
          progressPercentage: 0,
          progressDescription: ''
        },
        knowledgePoints: [],
        weakPoints: [],
        aiRecommendations: [],
        learningTrend: {},
        dailyTasks: []
      },
      trendChart: null
    }
  },
  mounted() {
    this.loadUserInfo()
    this.loadDashboardData()
  },
  beforeDestroy() {
    if (this.trendChart) {
      this.trendChart.dispose()
    }
  },
  methods: {
    async loadUserInfo() {
      try {
        const response = await getUserInfo()
        this.userInfo = response.data
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
    },

    async loadDashboardData() {
      try {
        const response = await getStudyDashboard()
        this.dashboardData = response.data
        this.studyGoal = {
          examType: this.dashboardData.examType,
          targetYear: this.dashboardData.targetYear,
          studyIdentity: this.dashboardData.studyIdentity
        }
        this.$nextTick(() => {
          this.initTrendChart()
        })
      } catch (error) {
        console.error('获取仪表盘数据失败:', error)
        this.$message.error('加载学习数据失败')
      }
    },

    initTrendChart() {
      if (!this.$refs.trendChart) return

      this.trendChart = echarts.init(this.$refs.trendChart)
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['学习时长', '正确率']
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
        },
        yAxis: [
          {
            type: 'value',
            name: '时长(小时)',
            min: 0,
            max: 20
          },
          {
            type: 'value',
            name: '正确率(%)',
            min: 0,
            max: 100
          }
        ],
        series: [
          {
            name: '学习时长',
            type: 'line',
            yAxisIndex: 0,
            data: this.dashboardData.learningTrend.weeklyStudyHours || [12, 14, 11, 15, 13, 16, 14],
            itemStyle: {
              color: '#409EFF'
            }
          },
          {
            name: '正确率',
            type: 'line',
            yAxisIndex: 1,
            data: this.dashboardData.learningTrend.weeklyCorrectRate || [65, 68, 72, 70, 75, 73, 76],
            itemStyle: {
              color: '#67C23A'
            }
          }
        ]
      }

      this.trendChart.setOption(option)
    },

    getStatusClass(status) {
      const classMap = {
        '良好': 'status-good',
        '一般': 'status-normal',
        '薄弱': 'status-weak'
      }
      return classMap[status] || 'status-normal'
    },

    getProgressColor(percentage) {
      if (percentage >= 80) return '#67C23A'
      if (percentage >= 60) return '#E6A23C'
      return '#F56C6C'
    },

    getRecommendationIcon(type) {
      const iconMap = {
        'daily_plan': 'el-icon-date',
        'weak_point': 'el-icon-warning',
        'exercise': 'el-icon-document',
        'review': 'el-icon-refresh'
      }
      return iconMap[type] || 'el-icon-question'
    },

    getPriorityType(priority) {
      if (priority === 1) return 'danger'
      if (priority === 2) return 'warning'
      if (priority === 3) return 'info'
      return ''
    },

    async refreshRecommendations() {
      try {
        await generateAiRecommendations()
        this.$message.success('AI推荐已刷新')
        this.loadDashboardData()
      } catch (error) {
        this.$message.error('刷新推荐失败')
      }
    },

    handleRecommendation(rec) {
      if (rec.actionUrl) {
        this.$router.push(rec.actionUrl)
      }
    },

    practiceWeakPoint(weak) {
      this.$message.info(`开始练习：${weak.pointName}`)
      // 跳转到专项练习页面
    },

    viewDetails(weak) {
      this.$message.info(`查看详情：${weak.pointName}`)
      // 显示详细信息弹窗
    },

    updateTaskStatus(task) {
      this.$message.success(`任务"${task.taskName}"状态已更新`)
    }
  }
}
</script>

<style lang="scss" scoped>
.ai-dashboard {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
}

.glass-effect {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.welcome-section {
  margin-bottom: 20px;

  .welcome-card {
    border-radius: 15px;

    .welcome-content {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .user-info {
        display: flex;
        align-items: center;

        .user-avatar {
          margin-right: 20px;
        }

        .user-details {
          h2 {
            margin: 0 0 8px 0;
            color: #303133;
          }

          .study-goal {
            color: #606266;
            font-size: 14px;
            margin-bottom: 4px;
          }

          .study-progress {
            color: #909399;
            font-size: 12px;
          }
        }
      }

      .quick-stats {
        display: flex;
        gap: 30px;

        .stat-item {
          text-align: center;

          .stat-value {
            font-size: 24px;
            font-weight: bold;
            color: #409EFF;
            margin-bottom: 4px;
          }

          .stat-label {
            font-size: 12px;
            color: #909399;
          }
        }
      }
    }
  }
}

.main-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.left-panel, .right-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  font-weight: bold;

  i {
    margin-right: 8px;
    color: #409EFF;
  }
}

.progress-card, .knowledge-card, .recommendation-card, .weakpoint-card {
  border-radius: 12px;

  .progress-content {
    .progress-bar-container {
      .progress-info {
        display: flex;
        justify-content: space-between;
        margin-bottom: 10px;

        .progress-text {
          font-weight: 500;
        }

        .progress-percentage {
          color: #409EFF;
          font-weight: bold;
        }
      }

      .progress-days {
        display: flex;
        justify-content: space-between;
        margin-top: 8px;
        font-size: 12px;
        color: #909399;
      }

      .progress-description {
        margin-top: 10px;
        color: #606266;
        font-style: italic;
      }
    }
  }
}

.knowledge-content {
  .knowledge-item {
    margin-bottom: 15px;

    .point-info {
      display: flex;
      justify-content: space-between;
      margin-bottom: 5px;

      .point-name {
        font-weight: 500;
      }

      .point-status {
        font-size: 12px;
        padding: 2px 6px;
        border-radius: 3px;

        &.status-good {
          background: #f0f9ff;
          color: #67C23A;
        }

        &.status-normal {
          background: #fdf6ec;
          color: #E6A23C;
        }

        &.status-weak {
          background: #fef0f0;
          color: #F56C6C;
        }
      }
    }

    .point-stats {
      display: flex;
      justify-content: space-between;
      font-size: 12px;
      color: #909399;
      margin-bottom: 5px;
    }
  }
}

.recommendation-content {
  .recommendation-item {
    display: flex;
    padding: 12px;
    margin-bottom: 10px;
    border-radius: 8px;
    background: #f8f9fa;
    transition: all 0.3s;

    &.completed {
      opacity: 0.6;
      background: #f0f0f0;
    }

    .rec-icon {
      margin-right: 12px;

      i {
        font-size: 20px;
        color: #409EFF;
      }
    }

    .rec-content {
      flex: 1;

      .rec-title {
        margin: 0 0 5px 0;
        font-size: 14px;
        font-weight: 500;
      }

      .rec-desc {
        margin: 0 0 8px 0;
        font-size: 12px;
        color: #606266;
        line-height: 1.4;
      }

      .rec-actions {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
    }
  }
}

.weakpoint-content {
  .weakpoint-item {
    padding: 12px;
    margin-bottom: 10px;
    border-radius: 8px;
    background: #fff5f5;

    .weakpoint-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      .weakpoint-name {
        font-weight: 500;
        color: #F56C6C;
      }
    }

    .weakpoint-suggestion {
      margin: 0 0 8px 0;
      font-size: 12px;
      color: #606266;
      line-height: 1.4;
    }

    .weakpoint-actions {
      text-align: right;
    }
  }
}

.bottom-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.task-card, .trend-card {
  border-radius: 12px;

  .task-content {
    .task-item {
      padding: 8px 0;
      border-bottom: 1px solid #f0f0f0;

      &.completed {
        opacity: 0.6;

        .task-name {
          text-decoration: line-through;
        }
      }

      .task-info {
        display: flex;
        flex-direction: column;

        .task-name {
          font-weight: 500;
          margin-bottom: 2px;
        }

        .task-desc {
          font-size: 12px;
          color: #606266;
          margin-bottom: 2px;
        }

        .task-time {
          font-size: 11px;
          color: #909399;
        }
      }
    }
  }

  .chart-container {
    height: 200px;
  }

  .trend-description {
    margin-top: 10px;
    font-size: 12px;
    color: #606266;
    text-align: center;
    font-style: italic;
  }
}

@media (max-width: 1200px) {
  .main-content, .bottom-section {
    grid-template-columns: 1fr;
  }
}
</style>
