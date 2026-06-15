ujvemplate>
  <div class="app-container">
    <!-- AI 学习计划展示区 -->
    <el-card v-if="aiPlan && aiPlan.planStatus === 1" class="ai-plan-card glass-effect" shadow="hover">
      <div slot="header" class="clearfix">
        <i class="el-icon-reading"></i>
        <span>AI 个性化学习规划</span>
        <el-tag size="mini" type="success" style="float: right;">已生成</el-tag>
      </div>
      <div class="plan-content">
        <h4>📅 总体复习策略</h4>
        <p>{{ parseAiContent('strategy') }}</p>
        <h4>🎯 阶段性进度安排</h4>
        <ul>
          <li v-for="(phase, index) in parseAiContent('phases')" :key="index">{{ phase }}</li>
        </ul>
        <h4>💡 针对薄弱模块建议</h4>
        <p>{{ parseAiContent('suggestions') }}</p>
      </div>
    </el-card>

    <!-- 顶部任务倒计时模块 -->
    <div class="top-section">
      <el-card class="countdown-card glass-effect">
        <div class="countdown-content">
          <div class="countdown-header">
            <div class="countdown-title">
              <i class="el-icon-alarm-clock"></i>
              <span>{{ taskName }}</span>
              <el-button
                type="text"
                icon="el-icon-edit"
                @click="editTask"
                class="edit-btn"
              ></el-button>
            </div>
          </div>
          <div class="countdown-days">{{ countdownDays }}天</div>
          <div class="review-stage">当前阶段：{{ currentStage }}</div>
        </div>
      </el-card>
    </div>

    <!-- 上半区：数据统计区 vs 核心任务区 -->
    <div class="upper-section">
      <!-- 左侧数据统计区（60%宽度） -->
      <div class="data-section">
        <!-- 学习时长趋势图 -->
        <el-card class="chart-card glass-effect">
          <div class="card-header">
            <i class="el-icon-data-analysis"></i>
            <span>近30天学习时长趋势</span>
          </div>
          <div ref="studyChart" class="chart-div"></div>
        </el-card>

        <!-- 学习数据统计卡片 -->
        <div class="stats-cards">
          <!-- 科目正确率卡片 -->
          <el-card class="stat-card glass-effect">
            <div class="stat-header">
              <span>科目正确率</span>
              <el-select
                v-model="selectedSubject"
                size="mini"
                @change="updateSubjectStats"
                class="subject-select"
              >
                <el-option
                  v-for="subject in subjects"
                  :key="subject"
                  :label="subject"
                  :value="subject"
                ></el-option>
              </el-select>
            </div>
            <div class="stat-content">
              <div class="stat-icon correct-rate">
                <i class="el-icon-success"></i>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ subjectCorrectRate }}%</div>
                <div class="stat-label">{{ selectedSubject }}正确率</div>
              </div>
            </div>
          </el-card>

          <!-- 错题统计卡片 -->
          <el-card class="stat-card glass-effect">
            <div class="stat-header">
              <span>错题统计</span>
              <el-select
                v-model="selectedWrongSubject"
                size="mini"
                @change="updateWrongStats"
                class="subject-select"
              >
                <el-option
                  v-for="subject in subjects"
                  :key="subject"
                  :label="subject"
                  :value="subject"
                ></el-option>
              </el-select>
            </div>
            <div class="stat-content">
              <div class="stat-icon wrong-count">
                <i class="el-icon-warning"></i>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ wrongQuestionCount }}</div>
                <div class="stat-label">{{ selectedWrongSubject }}错题</div>
              </div>
            </div>
          </el-card>
        </div>
      </div>

      <!-- 右侧核心任务区（40%宽度） -->
      <div class="task-section">
        <!-- 今日学习任务 - 核心模块，加宽加高 -->
        <el-card class="task-card glass-effect">
          <div class="card-header">
            <i class="el-icon-tickets"></i>
            <span>今日学习任务</span>
            <div class="task-actions">
              <el-button
                type="primary"
                size="mini"
                @click="generateAITasks"
                :loading="aiGenerating"
                title="AI生成个性化任务"
              >
                <i class="el-icon-magic"></i>
                AI 生成
              </el-button>
              <el-button
                type="text"
                icon="el-icon-plus"
                @click="addCustomTask"
                title="添加自定义任务"
              ></el-button>
            </div>
          </div>
          <div class="task-list">
            <div v-for="(task, index) in todayTasks" :key="index" class="task-item">
              <div class="task-main">
                <el-checkbox v-model="task.completed" @change="updateTaskProgress(index)">
                  {{ task.name }}
                </el-checkbox>
                <span class="task-progress">{{ task.progress }}</span>
              </div>
              <div class="task-actions">
                <el-button
                  type="text"
                  icon="el-icon-edit"
                  size="mini"
                  @click="editTaskItem(index)"
                ></el-button>
                <el-button
                  type="text"
                  icon="el-icon-delete"
                  size="mini"
                  @click="deleteTask(index)"
                ></el-button>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 下半区：节点区 vs AI贴士区 -->
    <div class="lower-section">
      <!-- 左侧考研关键节点 -->
      <div class="node-section">
        <el-card class="calendar-card glass-effect">
          <div class="card-header">
            <i class="el-icon-date"></i>
            <span>考研关键节点</span>
            <div class="task-actions">
              <el-button
                type="text"
                icon="el-icon-plus"
                @click="addKeyDate"
                title="添加节点"
              ></el-button>
            </div>
          </div>
          <div class="key-dates">
            <div v-for="(date, index) in keyDates" :key="index" class="date-item">
              <div class="date-info">
                <div class="date-name">{{ date.name }}</div>
                <div class="date-time">{{ date.time }}</div>
              </div>
              <div class="date-actions">
                <el-button
                  type="text"
                  icon="el-icon-edit"
                  size="mini"
                  @click="editKeyDate(index)"
                ></el-button>
                <el-button
                  type="text"
                  icon="el-icon-delete"
                  size="mini"
                  @click="deleteKeyDate(index)"
                ></el-button>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 右侧AI学习小贴士 - 通栏模块 -->
      <div class="tips-section">
        <el-card class="tips-card glass-effect">
          <div class="card-header">
            <i class="el-icon-lightbulb"></i>
            <span>AI学习小贴士</span>
            <el-button
              type="primary"
              size="mini"
              @click="generateAITips"
              :loading="tipsGenerating"
              title="刷新AI建议"
            >
              <i class="el-icon-refresh"></i>
              刷新
            </el-button>
          </div>
          <div class="tips-content">
            <div v-for="(tip, index) in studyTips" :key="index" class="tip-item">
              {{ tip }}
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 底部快捷入口 -->
    <div class="bottom-section">
      <div class="quick-actions">
        <el-card class="quick-card glass-effect" @click.native="goToExercise">
          <div class="quick-content">
            <i class="el-icon-document"></i>
            <span>真题刷题</span>
          </div>
        </el-card>

        <el-card class="quick-card glass-effect" @click.native="goToWrongQuestions">
          <div class="quick-content">
            <i class="el-icon-collection"></i>
            <span>错题本</span>
          </div>
        </el-card>

        <el-card class="quick-card glass-effect" @click.native="goToMockExam">
          <div class="quick-content">
            <i class="el-icon-edit"></i>
            <span>模考中心</span>
          </div>
        </el-card>

        <el-card class="quick-card glass-effect" @click.native="goToAIChat">
          <div class="quick-content">
            <i class="el-icon-chat-line-round"></i>
            <span>AI 问答</span>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 任务编辑对话框 -->
    <el-dialog
      title="编辑任务"
      :visible.sync="taskDialogVisible"
      width="400px"
    >
      <el-form :model="taskForm">
        <el-form-item label="任务名称">
          <el-input v-model="taskForm.name" placeholder="请输入任务名称"></el-input>
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker
            v-model="taskForm.deadline"
            type="date"
            placeholder="选择截止日期"
            value-format="yyyy-MM-dd"
          ></el-date-picker>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="taskDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTask">保存</el-button>
      </span>
    </el-dialog>

    <!-- 关键节点编辑对话框 -->
    <el-dialog
      title="编辑关键节点"
      :visible.sync="dateDialogVisible"
      width="400px"
    >
      <el-form :model="dateForm">
        <el-form-item label="节点名称">
          <el-input v-model="dateForm.name" placeholder="请输入节点名称"></el-input>
        </el-form-item>
        <el-form-item label="时间">
          <el-input v-model="dateForm.time" placeholder="请输入时间（如：9月）"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveKeyDate">保存</el-button>
      </span>
    </el-dialog>

    <!-- 任务项编辑对话框 -->
    <el-dialog
      title="编辑学习任务"
      :visible.sync="taskItemDialogVisible"
      width="400px"
    >
      <el-form :model="taskItemForm">
        <el-form-item label="任务内容">
          <el-input v-model="taskItemForm.name" placeholder="请输入任务内容"></el-input>
        </el-form-item>
        <el-form-item label="进度">
          <el-input v-model="taskItemForm.progress" placeholder="如：0/5"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="taskItemDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTaskItem">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getDaily } from '@/api/stat'
import { getExamRecords, getExerciseRecords } from '@/api/record'
import { aiChat } from '@/api/ai-chat'
import { getLearningPlan } from '@/api/user-profile'
import * as echarts from 'echarts'
import { mapGetters } from 'vuex'

export default {
  name: 'StudentDashboard',
  data() {
    return {
      // AI 学习计划相关
      aiPlan: null,
      planLoading: false,

      // 任务倒计时相关
      taskName: '考研初试',
      taskDeadline: '2025-12-21',
      countdownDays: 0,
      currentStage: '基础复习阶段',

      // 科目选择
      subjects: ['政治', '英语', '数学', '专业课'],
      selectedSubject: '政治',
      selectedWrongSubject: '政治',
      subjectCorrectRate: 75,
      wrongQuestionCount: 42,

      // 今日任务
      todayTasks: [
        { name: '英语阅读理解', completed: false, progress: '0/5' },
        { name: '数学练习题', completed: false, progress: '0/10' },
        { name: '政治知识点复习', completed: false, progress: '0/3章' },
        { name: '专业课模拟题', completed: false, progress: '0/2套' }
      ],

      // 关键节点
      keyDates: [
        { name: '考研大纲发布', time: '9月' },
        { name: '网上报名', time: '10月' },
        { name: '现场确认', time: '11月' },
        { name: '准考证打印', time: '12月' },
        { name: '初试时间', time: '12月21-22日' }
      ],

      // AI学习小贴士
      studyTips: [
        '每天保持6-8小时高效学习时间',
        '定期复习错题，巩固薄弱环节',
        '模拟考试要严格计时，模拟真实环境',
        '注意劳逸结合，保证充足睡眠'
      ],

      // AI生成状态
      aiGenerating: false,
      tipsGenerating: false,

      // 对话框控制
      taskDialogVisible: false,
      dateDialogVisible: false,
      taskItemDialogVisible: false,

      // 表单数据
      taskForm: {
        name: '',
        deadline: ''
      },
      dateForm: {
        name: '',
        time: ''
      },
      taskItemForm: {
        name: '',
        progress: ''
      },
      editingIndex: -1,

      // 图表配置
      chartOption: {
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
          lineStyle: {
            color: '#409EFF'
          },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [{
                offset: 0, color: 'rgba(64, 158, 255, 0.3)'
              }, {
                offset: 1, color: 'rgba(64, 158, 255, 0.1)'
              }]
            }
          }
        }]
      },

      myChart: null
    }
  },
  computed: {
    ...mapGetters(['aiConfigId']),
    selectedConfigId() {
      return this.aiConfigId || (this.$store.state.aiModel ? this.$store.state.aiModel.selectedConfigId : null)
    }
  },
  created() {
    this.$store.dispatch('aiModel/loadModels')
    localStorage.removeItem('questionnaire_completed')

    this.loadUserConfig()
    this.calculateCountdown()
    this.getStudyData()
    this.getLearningStats()
    this.fetchAiPlan()
  },
  mounted() {
    this.initChart()
    window.addEventListener('resize', this.resizeChart)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.resizeChart)
    if (this.myChart) {
      this.myChart.dispose()
    }
  },
  methods: {
    // 解析 AI 返回的内容 (简化版，实际应根据 JSON 结构解析)
    parseAiContent(type) {
      if (!this.aiPlan || !this.aiPlan.aiPlan) return '暂无数据'
      try {
        const data = JSON.parse(this.aiPlan.aiPlan)
        if (type === 'strategy') return data['总体复习策略'] || data.strategy || ''
        if (type === 'phases') return data['阶段性进度安排'] || data.phases || []
        if (type === 'suggestions') return data['针对薄弱模块建议'] || data.suggestions || ''
      } catch (e) {
        return this.aiPlan.aiPlan // 如果不是 JSON，直接返回原文
      }
      return ''
    },

    // 获取 AI 学习计划
    fetchAiPlan() {
      this.planLoading = true
      getLearningPlan().then(res => {
        // 注意：后端 Result 成功时 code 通常为 1 或 200，请根据你项目的实际定义调整
        if (res.code === 1 || res.code === 200) {
          if (res.data) {
            this.aiPlan = res.data
            if (this.aiPlan.aiPlan && this.aiPlan.planStatus === 1) {
              this.$message.success('AI 已为您更新学习计划')
            } else if (!this.aiPlan.planStatus || this.aiPlan.planStatus === 0) {
              this.$notify({
                title: '温馨提示',
                message: '您的个性化学习计划正在生成中，请稍后刷新页面...',
                type: 'info'
              })
            }
          } else {
            // 如果 data 为空，说明用户还没填问卷，强制跳转
            console.log('未检测到用户画像，跳转至问卷页')
            this.$router.push('/user/onboarding')
          }
        } else {
          // 接口调用失败（如 403 权限问题），也尝试跳转或提示
          console.error('获取学习计划失败:', res.msg)
          this.$message.error(res.msg || '获取数据失败，请检查登录状态')
        }
      }).catch(err => {
        console.error('网络请求异常:', err)
        // 如果是 403 或 401，可能需要重新登录
        if (err.response && (err.response.status === 401 || err.response.status === 403)) {
          this.$message.error('您没有该资源的访问权限，请重新登录')
          this.$router.push('/login')
        }
      }).finally(() => {
        this.planLoading = false
      })
    },

    // 加载用户配置
    loadUserConfig() {
      const config = localStorage.getItem('studentDashboardConfig')
      if (config) {
        const parsed = JSON.parse(config)
        this.taskName = parsed.taskName || this.taskName
        this.taskDeadline = parsed.taskDeadline || this.taskDeadline
        this.selectedSubject = parsed.selectedSubject || this.selectedSubject
        this.selectedWrongSubject = parsed.selectedWrongSubject || this.selectedWrongSubject
        this.todayTasks = parsed.todayTasks || this.todayTasks
        this.keyDates = parsed.keyDates || this.keyDates
      }
    },

    // 保存用户配置
    saveUserConfig() {
      const config = {
        taskName: this.taskName,
        taskDeadline: this.taskDeadline,
        selectedSubject: this.selectedSubject,
        selectedWrongSubject: this.selectedWrongSubject,
        todayTasks: this.todayTasks,
        keyDates: this.keyDates
      }
      localStorage.setItem('studentDashboardConfig', JSON.stringify(config))
    },

    // 计算倒计时
    calculateCountdown() {
      const today = new Date()
      const deadline = new Date(this.taskDeadline)
      const timeDiff = deadline.getTime() - today.getTime()
      this.countdownDays = Math.ceil(timeDiff / (1000 * 3600 * 24))

      // 根据倒计时天数确定当前阶段
      if (this.countdownDays > 180) {
        this.currentStage = '基础复习阶段'
      } else if (this.countdownDays > 90) {
        this.currentStage = '强化提高阶段'
      } else if (this.countdownDays > 30) {
        this.currentStage = '冲刺阶段'
      } else {
        this.currentStage = '最后冲刺阶段'
      }
    },

    // 编辑任务
    editTask() {
      this.taskForm = {
        name: this.taskName,
        deadline: this.taskDeadline
      }
      this.taskDialogVisible = true
    },

    // 保存任务
    saveTask() {
      this.taskName = this.taskForm.name
      this.taskDeadline = this.taskForm.deadline
      this.calculateCountdown()
      this.saveUserConfig()
      this.taskDialogVisible = false
    },

    // 更新科目统计
    updateSubjectStats() {
      // 根据选择的科目更新统计数据
      this.getLearningStats()
      this.saveUserConfig()
    },

    updateWrongStats() {
      // 根据选择的科目更新错题统计
      this.getLearningStats()
      this.saveUserConfig()
    },

    // AI生成任务
    async generateAITasks() {
      this.aiGenerating = true
      try {
        // 构建更详细的提示词
        const prompt = `你是一个考研学习助手。请基于以下用户学习数据，生成3-4个今日学习任务建议：
- 当前科目：${this.selectedSubject}
- 科目正确率：${this.subjectCorrectRate}%
- 错题数量：${this.wrongQuestionCount}道
- 剩余考研天数：${this.countdownDays}天
- 当前复习阶段：${this.currentStage}

请生成具体、可执行的学习任务，每个任务包含任务名称和进度（如：英语阅读理解|0/5）。任务要符合当前复习阶段，重点针对薄弱环节。`

        const response = await aiChat({ question: prompt, configId: this.selectedConfigId })

        if (response && response.code === 1) {
          this.parseAITasks(response.data)
          this.$message.success('AI任务生成成功！')
        } else {
          // 如果AI接口失败，使用默认任务作为降级方案
          this.useDefaultTasks()
          this.$message.warning('AI服务暂时不可用，已使用推荐任务')
        }
      } catch (error) {
        console.error('AI任务生成异常:', error)
        // 异常处理：使用默认任务
        this.useDefaultTasks()
        this.$message.warning('网络异常，已使用推荐任务')
      } finally {
        this.aiGenerating = false
      }
    },

    parseAITasks(aiResponse) {
      try {
        // 更健壮的解析逻辑
        const lines = aiResponse.split('\n').filter(line =>
          line.trim() && !line.includes('思考过程') && !line.includes('完整回复')
        )

        const newTasks = []
        lines.forEach(line => {
          if (line.includes('|')) {
            const [name, progress] = line.split('|')
            if (name && progress) {
              newTasks.push({
                name: name.trim(),
                completed: false,
                progress: progress.trim()
              })
            }
          } else if (line.trim().length > 5) {
            // 如果没有分隔符，但内容较长，作为单个任务
            newTasks.push({
              name: line.trim(),
              completed: false,
              progress: '0/1'
            })
          }
        })

        if (newTasks.length > 0) {
          // 保留用户自定义任务，添加AI生成任务
          const customTasks = this.todayTasks.filter(task =>
            !task.name.includes('英语阅读理解') &&
            !task.name.includes('数学练习题') &&
            !task.name.includes('政治知识点') &&
            !task.name.includes('专业课模拟')
          )
          this.todayTasks = [...newTasks.slice(0, 4), ...customTasks]
          this.saveUserConfig()
        } else {
          this.useDefaultTasks()
        }
      } catch (error) {
        console.error('解析AI任务失败:', error)
        this.useDefaultTasks()
      }
    },

    useDefaultTasks() {
      // 默认推荐任务
      const defaultTasks = [
        { name: `${this.selectedSubject}专项练习`, completed: false, progress: '0/10' },
        { name: '错题回顾与整理', completed: false, progress: '0/1' },
        { name: '模拟测试一套', completed: false, progress: '0/1' },
        { name: '知识点系统复习', completed: false, progress: '0/3章' }
      ]
      const customTasks = this.todayTasks.filter(task =>
        !task.name.includes('专项练习') &&
        !task.name.includes('错题回顾') &&
        !task.name.includes('模拟测试') &&
        !task.name.includes('知识点系统')
      )
      this.todayTasks = [...defaultTasks, ...customTasks]
      this.saveUserConfig()
    },

    // 添加自定义任务
    addCustomTask() {
      this.taskItemForm = { name: '', progress: '0/1' }
      this.editingIndex = -1
      this.taskItemDialogVisible = true
    },

    // 编辑任务项
    editTaskItem(index) {
      this.taskItemForm = { ...this.todayTasks[index] }
      this.editingIndex = index
      this.taskItemDialogVisible = true
    },

    // 保存任务项
    saveTaskItem() {
      if (this.editingIndex >= 0) {
        this.todayTasks.splice(this.editingIndex, 1, { ...this.taskItemForm })
      } else {
        this.todayTasks.push({ ...this.taskItemForm, completed: false })
      }
      this.saveUserConfig()
      this.taskItemDialogVisible = false
    },

    // 删除任务
    deleteTask(index) {
      this.todayTasks.splice(index, 1)
      this.saveUserConfig()
    },

    // 更新任务进度
    updateTaskProgress(index) {
      this.saveUserConfig()
    },

    // 添加关键节点
    addKeyDate() {
      this.dateForm = { name: '', time: '' }
      this.editingIndex = -1
      this.dateDialogVisible = true
    },

    // 编辑关键节点
    editKeyDate(index) {
      this.dateForm = { ...this.keyDates[index] }
      this.editingIndex = index
      this.dateDialogVisible = true
    },

    // 保存关键节点
    saveKeyDate() {
      if (this.editingIndex >= 0) {
        this.keyDates.splice(this.editingIndex, 1, { ...this.dateForm })
      } else {
        this.keyDates.push({ ...this.dateForm })
      }
      // 按时间排序
      this.keyDates.sort((a, b) => this.parseMonth(a.time) - this.parseMonth(b.time))
      this.saveUserConfig()
      this.dateDialogVisible = false
    },

    // 删除关键节点
    deleteKeyDate(index) {
      this.keyDates.splice(index, 1)
      this.saveUserConfig()
    },

    // 解析月份用于排序
    parseMonth(timeStr) {
      const monthMap = { '1月': 1, '2月': 2, '3月': 3, '4月': 4, '5月': 5, '6月': 6, '7月': 7, '8月': 8, '9月': 9, '10月': 10, '11月': 11, '12月': 12 }
      return monthMap[timeStr] || 13
    },

    // AI生成学习贴士
    async generateAITips() {
      this.tipsGenerating = true
      try {
        // 构建更详细的提示词
        const prompt = `你是一个考研学习专家。请基于以下用户学习数据，生成3条个性化学习建议：
- 当前科目：${this.selectedSubject}
- 科目正确率：${this.subjectCorrectRate}%
- 错题数量：${this.wrongQuestionCount}道
- 剩余考研天数：${this.countdownDays}天
- 当前复习阶段：${this.currentStage}
- 今日学习时长：${this.getTodayStudyTime()}分钟

请提供具体、实用的学习建议，每条建议要针对用户当前的学习状况，帮助提高学习效率。`

        const response = await aiChat({ question: prompt, configId: this.selectedConfigId })

        if (response && response.code === 1) {
          this.parseAITips(response.data)
          this.$message.success('AI学习建议已更新！')
        } else {
          // 如果AI接口失败，使用默认建议作为降级方案
          this.useDefaultTips()
          this.$message.warning('AI服务暂时不可用，已使用推荐建议')
        }
      } catch (error) {
        console.error('AI建议生成异常:', error)
        // 异常处理：使用默认建议
        this.useDefaultTips()
        this.$message.warning('网络异常，已使用推荐建议')
      } finally {
        this.tipsGenerating = false
      }
    },

    parseAITips(aiResponse) {
      try {
        // 更健壮的解析逻辑
        const lines = aiResponse.split('\n').filter(line =>
          line.trim() &&
          line.trim().length > 10 &&
          !line.includes('思考过程') &&
          !line.includes('完整回复')
        )

        if (lines.length > 0) {
          // 取前3条有效建议
          this.studyTips = lines.slice(0, 3).map(line => line.trim())
        } else {
          this.useDefaultTips()
        }
      } catch (error) {
        console.error('解析AI建议失败:', error)
        this.useDefaultTips()
      }
    },

    useDefaultTips() {
      // 默认学习建议
      this.studyTips = [
        `针对${this.selectedSubject}科目，建议每天保持${this.getRecommendedStudyTime()}分钟专注学习`,
        `当前正确率${this.subjectCorrectRate}%，重点复习错题，提高薄弱环节`,
        `${this.currentStage}阶段，建议制定详细计划，保持学习节奏`
      ]
    },

    getRecommendedStudyTime() {
      // 根据复习阶段推荐学习时长
      if (this.countdownDays > 180) return 120
      else if (this.countdownDays > 90) return 180
      else if (this.countdownDays > 30) return 240
      else return 300
    },

    // 获取今日学习时长（示例）
    getTodayStudyTime() {
      return 120 // 示例数据
    },

    // 获取学习数据
    async getStudyData() {
      try {
        const res = await getDaily()
        if (res.code === 1) {
          this.processStudyData(res.data)
        }
      } catch (error) {
        console.error('获取学习数据失败:', error)
      }
    },

    // 处理学习数据
    processStudyData(data) {
      const currentDate = new Date()
      const dateArray = []
      const studyData = []

      for (let i = 29; i >= 0; i--) {
        const date = new Date(currentDate.getTime() - i * 24 * 60 * 60 * 1000)
        const dateStr = date.toISOString().split('T')[0]
        dateArray.push(dateStr.substring(5))

        const dayData = data.find(item => item.loginDate === dateStr)
        studyData.push(dayData ? Math.round(dayData.totalSeconds / 60) : 0)
      }

      this.chartOption.xAxis.data = dateArray
      this.chartOption.series[0].data = studyData

      if (this.myChart) {
        this.myChart.setOption(this.chartOption)
      }
    },

    // 获取学习统计
    async getLearningStats() {
      // 这里需要根据实际API调整，按科目统计
      this.subjectCorrectRate = Math.floor(Math.random() * 30) + 70 // 示例数据
      this.wrongQuestionCount = Math.floor(Math.random() * 20) + 30 // 示例数据
    },

    // 初始化图表
    initChart() {
      this.myChart = echarts.init(this.$refs.studyChart)
      this.myChart.setOption(this.chartOption)
    },

    // 调整图表大小
    resizeChart() {
      if (this.myChart) {
        this.myChart.resize()
      }
    },

    // 快捷入口跳转
    goToExercise() {
      this.$router.push('/exercise-center')
    },

    goToWrongQuestions() {
      this.$router.push('/wrong-book')
    },

    goToMockExam() {
      this.$router.push('/text-center')
    },

    goToAIChat() {
      this.$router.push('/ai-chat')
    }
  }
}
</script>

<style scoped>
.app-container {
  min-height: 100vh;
  background: #ffffff;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 磨砂玻璃效果 */
.glass-effect {
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

/* 顶部倒计时 - 通栏 */
.top-section {
  width: 100%;
}

.countdown-card {
  border-radius: 15px;
  border: none;
  width: 100%;
}

.countdown-content {
  text-align: center;
  padding: 25px;
}

.countdown-header {
  margin-bottom: 15px;
}

.countdown-title {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #333;
  gap: 8px;
}

.countdown-title i {
  color: #409EFF;
  font-size: 22px;
}

.edit-btn {
  margin-left: 8px;
}

.countdown-days {
  font-size: 42px;
  font-weight: bold;
  color: #E6A23C;
  margin: 15px 0;
}

.review-stage {
  font-size: 16px;
  color: #666;
  font-weight: 500;
}

/* 上半区：数据统计区 vs 核心任务区 */
.upper-section {
  display: flex;
  gap: 20px;
  height: 450px;
}

/* 左侧数据统计区 - 占60%宽度 */
.data-section {
  flex: 0 0 60%;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 右侧核心任务区 - 占40%宽度 */
.task-section {
  flex: 0 0 40%;
}

/* 下半区：节点区 vs AI贴士区 */
.lower-section {
  display: flex;
  gap: 20px;
  height: 250px;
}

/* 左侧考研关键节点 */
.node-section {
  flex: 0 0 60%;
}

/* 右侧AI学习小贴士 - 通栏模块 */
.tips-section {
  flex: 0 0 40%;
}

/* 卡片样式 */
.chart-card, .task-card, .calendar-card, .tips-card {
  border-radius: 15px;
  border: none;
  height: 100%;
}

/* 突出核心模块的视觉效果 */
.task-card, .tips-card {
  border: 1px solid rgba(64, 158, 255, 0.2);
  background: rgba(255, 255, 255, 0.3);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 15px;
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.card-header i {
  margin-right: 8px;
  color: #409EFF;
}

.task-actions {
  display: flex;
  gap: 8px;
}

/* 学习时长趋势图 */
.chart-div {
  height: 250px;
  width: 100%;
}

/* 统计卡片 - 并列显示 */
.stats-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

.stat-card {
  border-radius: 12px;
  border: none;
  padding: 20px;
}

.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.stat-header span {
  font-size: 14px;
  font-weight: bold;
  color: #333;
}

.subject-select {
  width: 80px;
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 24px;
}

.correct-rate {
  background: rgba(103, 194, 58, 0.2);
  color: #67C23A;
}

.wrong-count {
  background: rgba(245, 108, 108, 0.2);
  color: #F56C6C;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 12px;
  color: #666;
}

/* 今日学习任务 - 核心模块，高度加大 */
.task-card {
  min-height: 400px;
}

.task-list {
  max-height: 320px;
  overflow-y: auto;
  padding: 0 5px;
}

.task-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 8px;
  border-bottom: 1px solid rgba(64, 158, 255, 0.1);
  transition: background-color 0.3s ease;
}

.task-item:hover {
  background: rgba(64, 158, 255, 0.05);
  border-radius: 8px;
}

.task-item:last-child {
  border-bottom: none;
}

.task-main {
  display: flex;
  align-items: center;
  flex: 1;
  justify-content: space-between;
}

.task-progress {
  font-size: 12px;
  color: #666;
  margin-left: 10px;
  background: rgba(64, 158, 255, 0.1);
  padding: 2px 8px;
  border-radius: 10px;
}

/* 关键日期 */
.key-dates {
  max-height: 150px;
  overflow-y: auto;
}

.date-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.3);
}

.date-item:last-child {
  border-bottom: none;
}

.date-info {
  flex: 1;
}

.date-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.date-time {
  font-size: 12px;
  color: #666;
}

.date-actions {
  display: flex;
  gap: 4px;
}

/* AI学习小贴士 - 通栏模块 */
.tips-card {
  min-height: 180px;
}

.tips-content {
  max-height: 140px;
  overflow-y: auto;
  padding: 0 5px;
}

.tip-item {
  padding: 12px 8px;
  font-size: 14px;
  color: #333;
  border-bottom: 1px solid rgba(64, 158, 255, 0.1);
  line-height: 1.5;
  transition: background-color 0.3s ease;
}

.tip-item:hover {
  background: rgba(64, 158, 255, 0.05);
  border-radius: 8px;
}

.tip-item:last-child {
  border-bottom: none;
}

/* 底部快捷入口 - 通栏 */
.bottom-section {
  width: 100%;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15px;
}

.quick-card {
  border-radius: 12px;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
  padding: 25px 20px;
}

.quick-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(64, 158, 255, 0.3);
}

.quick-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.quick-content i {
  font-size: 28px;
  color: #409EFF;
}

.quick-content span {
  font-size: 15px;
  color: #333;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .upper-section, .lower-section {
    flex-direction: column;
    height: auto;
  }

  .data-section, .task-section, .node-section, .tips-section {
    flex: 1;
    width: 100%;
  }

  .quick-actions {
    grid-template-columns: repeat(2, 1fr);
  }

  .task-card {
    min-height: 300px;
  }
}

@media (max-width: 768px) {
  .app-container {
    padding: 15px;
    gap: 15px;
  }

  .stats-cards {
    grid-template-columns: 1fr;
  }

  .quick-actions {
    grid-template-columns: 1fr;
  }

  .countdown-title {
    font-size: 18px;
  }

  .countdown-days {
    font-size: 32px;
  }

  .countdown-content {
    padding: 20px;
  }

  .task-list {
    max-height: 250px;
  }

  .tips-content {
    max-height: 120px;
  }
}
</style>
