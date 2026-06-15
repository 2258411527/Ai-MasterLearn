<template>
  <div class="questionnaire-form">
    <!-- 考研版问卷 -->
    <div v-if="currentExamType === '考研'" class="exam-section">
      <h3>🎓 考研个性化学习规划</h3>
      
      <!-- 基本信息 -->
      <el-form ref="kaoyanForm" :model="kaoyanForm" :rules="kaoyanRules" label-width="120px">
        <el-form-item label="考研届数" prop="attemptCount">
          <el-radio-group v-model="kaoyanForm.attemptCount">
            <el-radio label="一战">一战</el-radio>
            <el-radio label="二战">二战</el-radio>
            <el-radio label="三战及以上">三战及以上</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="学位类型" prop="degreeType">
          <el-radio-group v-model="kaoyanForm.degreeType">
            <el-radio label="学硕">学硕</el-radio>
            <el-radio label="专硕">专硕</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="报考方式" prop="majorType">
          <el-radio-group v-model="kaoyanForm.majorType">
            <el-radio label="本专业报考">本专业报考</el-radio>
            <el-radio label="跨专业报考">跨专业报考</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="目标院校层次" prop="targetUniversityLevel">
          <el-radio-group v-model="kaoyanForm.targetUniversityLevel">
            <el-radio label="双非">双非</el-radio>
            <el-radio label="省属重点">省属重点</el-radio>
            <el-radio label="211">211</el-radio>
            <el-radio label="985">985</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="英语科目" prop="englishSubject">
          <el-radio-group v-model="kaoyanForm.englishSubject">
            <el-radio label="英语一">英语一</el-radio>
            <el-radio label="英语二">英语二</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="数学科目" prop="mathSubject">
          <el-radio-group v-model="kaoyanForm.mathSubject">
            <el-radio label="数学一">数学一</el-radio>
            <el-radio label="数学二">数学二</el-radio>
            <el-radio label="数学三">数学三</el-radio>
            <el-radio label="不考数学">不考数学</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="专业课名称" prop="majorCourseName">
          <el-input v-model="kaoyanForm.majorCourseName" placeholder="请输入您的专业课名称" />
        </el-form-item>

        <el-form-item label="英语基础" prop="englishLevel">
          <el-radio-group v-model="kaoyanForm.englishLevel">
            <el-radio label="零基础">零基础</el-radio>
            <el-radio label="一般">一般</el-radio>
            <el-radio label="较好">较好</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="数学基础" prop="mathLevel">
          <el-radio-group v-model="kaoyanForm.mathLevel">
            <el-radio label="零基础">零基础</el-radio>
            <el-radio label="一般">一般</el-radio>
            <el-radio label="较好">较好</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="专业课基础" prop="majorLevel">
          <el-radio-group v-model="kaoyanForm.majorLevel">
            <el-radio label="零基础">零基础</el-radio>
            <el-radio label="遗忘较多">遗忘较多</el-radio>
            <el-radio label="基础扎实">基础扎实</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="薄弱模块" prop="weakModules">
          <el-checkbox-group v-model="kaoyanForm.weakModules">
            <el-checkbox label="词汇记忆">词汇记忆</el-checkbox>
            <el-checkbox label="阅读理解">阅读理解</el-checkbox>
            <el-checkbox label="写作表达">写作表达</el-checkbox>
            <el-checkbox label="数学计算">数学计算</el-checkbox>
            <el-checkbox label="逻辑推理">逻辑推理</el-checkbox>
            <el-checkbox label="专业课概念">专业课概念</el-checkbox>
            <el-checkbox label="专业课应用">专业课应用</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item label="备考需求" prop="studyNeeds">
          <el-checkbox-group v-model="kaoyanForm.studyNeeds">
            <el-checkbox label="基础巩固">基础巩固</el-checkbox>
            <el-checkbox label="专项突破">专项突破</el-checkbox>
            <el-checkbox label="模拟训练">模拟训练</el-checkbox>
            <el-checkbox label="真题演练">真题演练</el-checkbox>
            <el-checkbox label="错题分析">错题分析</el-checkbox>
            <el-checkbox label="时间管理">时间管理</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
    </div>

    <!-- 考公版问卷 -->
    <div v-else-if="currentExamType === '考公'" class="exam-section">
      <h3>🏛️ 考公个性化学习规划</h3>
      
      <el-form ref="kaogongForm" :model="kaogongForm" :rules="kaogongRules" label-width="120px">
        <el-form-item label="考试类型" prop="examCategory">
          <el-radio-group v-model="kaogongForm.examCategory">
            <el-radio label="国考">国考</el-radio>
            <el-radio label="省考">省考</el-radio>
            <el-radio label="选调生">选调生</el-radio>
            <el-radio label="事业单位">事业单位</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="意向报考地区" prop="targetRegion">
          <el-radio-group v-model="kaogongForm.targetRegion">
            <el-radio label="本省">本省</el-radio>
            <el-radio label="全国不限">全国不限</el-radio>
            <el-radio label="特定省份">特定省份</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="意向岗位类别" prop="positionCategory">
          <el-radio-group v-model="kaogongForm.positionCategory">
            <el-radio label="综合文职">综合文职</el-radio>
            <el-radio label="行政执法">行政执法</el-radio>
            <el-radio label="基层乡镇">基层乡镇</el-radio>
            <el-radio label="不限">不限</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="备考经历" prop="preparationExperience">
          <el-radio-group v-model="kaogongForm.preparationExperience">
            <el-radio label="首次备考">首次备考</el-radio>
            <el-radio label="曾参加过笔试">曾参加过笔试</el-radio>
            <el-radio label="进过面试">进过面试</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="行测基础" prop="xingceLevel">
          <el-radio-group v-model="kaogongForm.xingceLevel">
            <el-radio label="零基础">零基础</el-radio>
            <el-radio label="初学刷题">初学刷题</el-radio>
            <el-radio label="有实战基础">有实战基础</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="申论基础" prop="shenlunLevel">
          <el-radio-group v-model="kaogongForm.shenlunLevel">
            <el-radio label="从未动笔">从未动笔</el-radio>
            <el-radio label="偶尔练习">偶尔练习</el-radio>
            <el-radio label="经常写大作文">经常写大作文</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="薄弱模块" prop="weakModules">
          <el-checkbox-group v-model="kaogongForm.weakModules">
            <el-checkbox label="言语理解">言语理解</el-checkbox>
            <el-checkbox label="数量关系">数量关系</el-checkbox>
            <el-checkbox label="判断推理">判断推理</el-checkbox>
            <el-checkbox label="资料分析">资料分析</el-checkbox>
            <el-checkbox label="常识判断">常识判断</el-checkbox>
            <el-checkbox label="申论写作">申论写作</el-checkbox>
            <el-checkbox label="材料分析">材料分析</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item label="备考需求" prop="studyNeeds">
          <el-checkbox-group v-model="kaogongForm.studyNeeds">
            <el-checkbox label="基础理论">基础理论</el-checkbox>
            <el-checkbox label="题型训练">题型训练</el-checkbox>
            <el-checkbox label="模拟考试">模拟考试</el-checkbox>
            <el-checkbox label="真题解析">真题解析</el-checkbox>
            <el-checkbox label="答题技巧">答题技巧</el-checkbox>
            <el-checkbox label="时间控制">时间控制</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
    </div>

    <!-- 双线备考问卷 -->
    <div v-else class="exam-section">
      <h3>🎯 双线备考个性化学习规划</h3>
      
      <el-form ref="dualForm" :model="dualForm" :rules="dualRules" label-width="120px">
        <el-form-item label="备考身份" prop="studyIdentity">
          <el-radio-group v-model="dualForm.studyIdentity">
            <el-radio label="应届本科">应届本科</el-radio>
            <el-radio label="往届脱产">往届脱产</el-radio>
            <el-radio label="在职上班族">在职上班族</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="每日学习时长" prop="dailyStudyHours">
          <el-radio-group v-model="dualForm.dailyStudyHours">
            <el-radio label="2h内">2h内</el-radio>
            <el-radio label="3-4h">3-4h</el-radio>
            <el-radio label="5-7h">5-7h</el-radio>
            <el-radio label="8h以上">8h以上</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="当前备考进度" prop="currentProgress">
          <el-radio-group v-model="dualForm.currentProgress">
            <el-radio label="未开始">未开始</el-radio>
            <el-radio label="刚开始">刚开始</el-radio>
            <el-radio label="备考中已有基础">备考中已有基础</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="备考重点倾向" prop="focusTendency">
          <el-radio-group v-model="dualForm.focusTendency">
            <el-radio label="考研为主">考研为主</el-radio>
            <el-radio label="考公为主">考公为主</el-radio>
            <el-radio label="均衡发展">均衡发展</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="时间分配偏好" prop="timePreference">
          <el-radio-group v-model="dualForm.timePreference">
            <el-radio label="分时段学习">分时段学习</el-radio>
            <el-radio label="交替学习">交替学习</el-radio>
            <el-radio label="集中突破">集中突破</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="薄弱环节" prop="weakAreas">
          <el-checkbox-group v-model="dualForm.weakAreas">
            <el-checkbox label="考研专业课">考研专业课</el-checkbox>
            <el-checkbox label="考研英语">考研英语</el-checkbox>
            <el-checkbox label="考研数学">考研数学</el-checkbox>
            <el-checkbox label="行测">行测</el-checkbox>
            <el-checkbox label="申论">申论</el-checkbox>
            <el-checkbox label="时间管理">时间管理</el-checkbox>
            <el-checkbox label="学习效率">学习效率</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item label="学习需求" prop="learningNeeds">
          <el-checkbox-group v-model="dualForm.learningNeeds">
            <el-checkbox label="计划制定">计划制定</el-checkbox>
            <el-checkbox label="进度跟踪">进度跟踪</el-checkbox>
            <el-checkbox label="错题分析">错题分析</el-checkbox>
            <el-checkbox label="模拟测试">模拟测试</el-checkbox>
            <el-checkbox label="心理调节">心理调节</el-checkbox>
            <el-checkbox label="效率提升">效率提升</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作按钮 -->
    <div class="form-actions">
      <el-button @click="handleSkip">跳过问卷</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">
        {{ submitting ? '提交中...' : '提交问卷' }}
      </el-button>
    </div>
  </div>
</template>

<script>
import { submitQuestionnaire } from '@/api/user-questionnaire'

export default {
  name: 'UserQuestionnaireForm',
  props: {
    examType: {
      type: String,
      default: '考研'
    }
  },
  data() {
    return {
      currentExamType: this.examType,
      submitting: false,
      
      // 考研表单数据
      kaoyanForm: {
        attemptCount: '',
        degreeType: '',
        majorType: '',
        targetUniversityLevel: '',
        englishSubject: '',
        mathSubject: '',
        majorCourseName: '',
        englishLevel: '',
        mathLevel: '',
        majorLevel: '',
        weakModules: [],
        studyNeeds: []
      },
      
      // 考研表单验证规则
      kaoyanRules: {
        attemptCount: [{ required: true, message: '请选择考研届数', trigger: 'change' }],
        degreeType: [{ required: true, message: '请选择学位类型', trigger: 'change' }],
        majorType: [{ required: true, message: '请选择报考方式', trigger: 'change' }],
        targetUniversityLevel: [{ required: true, message: '请选择目标院校层次', trigger: 'change' }],
        englishSubject: [{ required: true, message: '请选择英语科目', trigger: 'change' }],
        mathSubject: [{ required: true, message: '请选择数学科目', trigger: 'change' }],
        majorCourseName: [{ required: true, message: '请输入专业课名称', trigger: 'blur' }],
        englishLevel: [{ required: true, message: '请选择英语基础', trigger: 'change' }],
        mathLevel: [{ required: true, message: '请选择数学基础', trigger: 'change' }],
        majorLevel: [{ required: true, message: '请选择专业课基础', trigger: 'change' }]
      },
      
      // 考公表单数据
      kaogongForm: {
        examCategory: '',
        targetRegion: '',
        positionCategory: '',
        preparationExperience: '',
        xingceLevel: '',
        shenlunLevel: '',
        weakModules: [],
        studyNeeds: []
      },
      
      // 考公表单验证规则
      kaogongRules: {
        examCategory: [{ required: true, message: '请选择考试类型', trigger: 'change' }],
        targetRegion: [{ required: true, message: '请选择意向报考地区', trigger: 'change' }],
        positionCategory: [{ required: true, message: '请选择意向岗位类别', trigger: 'change' }],
        preparationExperience: [{ required: true, message: '请选择备考经历', trigger: 'change' }],
        xingceLevel: [{ required: true, message: '请选择行测基础', trigger: 'change' }],
        shenlunLevel: [{ required: true, message: '请选择申论基础', trigger: 'change' }]
      },
      
      // 双线备考表单数据
      dualForm: {
        studyIdentity: '',
        dailyStudyHours: '',
        currentProgress: '',
        focusTendency: '',
        timePreference: '',
        weakAreas: [],
        learningNeeds: []
      },
      
      // 双线备考表单验证规则
      dualRules: {
        studyIdentity: [{ required: true, message: '请选择备考身份', trigger: 'change' }],
        dailyStudyHours: [{ required: true, message: '请选择每日学习时长', trigger: 'change' }],
        currentProgress: [{ required: true, message: '请选择当前备考进度', trigger: 'change' }],
        focusTendency: [{ required: true, message: '请选择备考重点倾向', trigger: 'change' }],
        timePreference: [{ required: true, message: '请选择时间分配偏好', trigger: 'change' }]
      }
    }
  },
  
  methods: {
    // 提交问卷
    async handleSubmit() {
      try {
        this.submitting = true
        
        let formData = {}
        let formRef = ''
        
        // 根据当前考试类型选择对应的表单数据和验证规则
        switch (this.currentExamType) {
          case '考研':
            formRef = 'kaoyanForm'
            formData = { ...this.kaoyanForm, examType: '考研' }
            break
          case '考公':
            formRef = 'kaogongForm'
            formData = { ...this.kaogongForm, examType: '考公' }
            break
          default:
            formRef = 'dualForm'
            formData = { ...this.dualForm, examType: '双线备考' }
        }
        
        // 验证表单
        await this.$refs[formRef].validate()
        
        // 提交问卷数据
        const res = await submitQuestionnaire(formData)
        
        if (res.code === 1) {
          this.$message.success('问卷提交成功！')
          this.$emit('submit-success')
        } else {
          this.$message.error(res.msg || '提交失败')
        }
      } catch (error) {
        console.error('提交问卷失败:', error)
        if (error.message) {
          this.$message.error(error.message)
        }
      } finally {
        this.submitting = false
      }
    },
    
    // 跳过问卷
    handleSkip() {
      this.$confirm('跳过问卷将无法获得精准的个性化推荐，确定要跳过吗？', '提示', {
        confirmButtonText: '确定跳过',
        cancelButtonText: '继续填写',
        type: 'warning'
      }).then(() => {
        this.$emit('skip')
      }).catch(() => {
        // 继续填写
      })
    }
  }
}
</script>

<style scoped>
.questionnaire-form {
  max-height: 70vh;
  overflow-y: auto;
  padding: 20px;
}

.exam-section {
  margin-bottom: 30px;
}

.exam-section h3 {
  margin-bottom: 20px;
  color: #303133;
  text-align: center;
  font-size: 20px;
}

.form-actions {
  text-align: center;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #e8e8e8;
}

.form-actions .el-button {
  margin: 0 10px;
  min-width: 120px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .questionnaire-form {
    padding: 10px;
  }
  
  .exam-section h3 {
    font-size: 18px;
  }
  
  .form-actions {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
  
  .form-actions .el-button {
    margin: 5px 0;
    width: 100%;
  }
}

/* 美化表单样式 */
:deep(.el-form-item__label) {
  font-weight: 600;
  color: #303133;
}

:deep(.el-radio-group) {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

:deep(.el-checkbox-group) {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

:deep(.el-radio) {
  margin-right: 0;
}

:deep(.el-checkbox) {
  margin-right: 0;
}

/* 移动端优化 */
@media (max-width: 480px) {
  :deep(.el-form-item__label) {
    width: 100px !important;
  }
  
  :deep(.el-form-item__content) {
    margin-left: 100px !important;
  }
  
  :deep(.el-radio-group),
  :deep(.el-checkbox-group) {
    flex-direction: column;
    gap: 8px;
  }
}
</style>