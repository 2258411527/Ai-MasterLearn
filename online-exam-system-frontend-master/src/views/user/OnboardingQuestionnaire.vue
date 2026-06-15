<template>
  <div class="onboarding-container">
    <div class="bg-decoration">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="gradient-orb orb-3"></div>
    </div>

    <div class="onboarding-content">
      <div class="progress-header">
        <div class="logo-section">
          <div class="logo-icon">
            <i class="el-icon-reading"></i>
          </div>
          <div class="logo-text">
            <h1>考研AI学习助手</h1>
            <p>个性化学习规划 · 智能推荐</p>
          </div>
        </div>
        <div class="progress-bar">
          <div class="progress-track">
            <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
          </div>
          <div class="progress-steps">
            <div v-for="(step, index) in steps" :key="index" class="step-dot" :class="{ active: currentStep >= index, completed: currentStep > index }">
              <span v-if="currentStep > index"><i class="el-icon-check"></i></span>
              <span v-else>{{ index + 1 }}</span>
            </div>
          </div>
          <div class="progress-label">{{ currentStep + 1 }} / {{ steps.length }}</div>
        </div>
      </div>

      <transition name="slide-fade" mode="out-in">
        <div :key="currentStep" class="step-container">
          <div v-if="currentStep === 0" class="step-content">
            <div class="step-header">
              <h2>让我们了解你的考研目标</h2>
              <p>这将帮助我们为你制定专属学习计划</p>
            </div>
            
            <div class="question-card">
              <div class="question-title">
                <span class="question-number">Q1</span>
                <span>你准备报考哪一年？</span>
              </div>
              <div class="year-selector">
                <div v-for="year in targetYears" :key="year.value" class="year-option" :class="{ selected: formData.targetYear === year.value }" @click="formData.targetYear = year.value">
                  <span class="year-value">{{ year.label }}</span>
                  <span class="year-desc">{{ year.desc }}</span>
                </div>
              </div>
            </div>

            <div class="question-card">
              <div class="question-title">
                <span class="question-number">Q2</span>
                <span>这是你第几次考研？</span>
              </div>
              <div class="option-grid">
                <div v-for="item in attemptOptions" :key="item.value" class="option-item" :class="{ selected: formData.attemptCount === item.value }" @click="formData.attemptCount = item.value">
                  <i :class="item.icon"></i>
                  <span class="option-label">{{ item.label }}</span>
                  <span class="option-desc">{{ item.desc }}</span>
                </div>
              </div>
            </div>

            <div class="question-card">
              <div class="question-title">
                <span class="question-number">Q3</span>
                <span>你的备考身份是？</span>
              </div>
              <div class="option-grid">
                <div v-for="item in identityOptions" :key="item.value" class="option-item" :class="{ selected: formData.studyIdentity === item.value }" @click="formData.studyIdentity = item.value">
                  <i :class="item.icon"></i>
                  <span class="option-label">{{ item.label }}</span>
                  <span class="option-desc">{{ item.desc }}</span>
                </div>
              </div>
            </div>

            <div class="question-card">
              <div class="question-title">
                <span class="question-number">Q4</span>
                <span>当前备考进度如何？</span>
              </div>
              <div class="progress-selector">
                <div v-for="item in progressOptions" :key="item.value" class="progress-item" :class="{ selected: formData.currentProgress === item.value }" @click="formData.currentProgress = item.value">
                  <div class="progress-icon"><i :class="item.icon"></i></div>
                  <span class="progress-label">{{ item.label }}</span>
                  <span class="progress-desc">{{ item.desc }}</span>
                </div>
              </div>
            </div>
          </div>

          <div v-if="currentStep === 1" class="step-content">
            <div class="step-header">
              <h2>你的目标院校与专业</h2>
              <p>精准定位，高效备考</p>
            </div>
            
            <div class="question-card">
              <div class="question-title">
                <span class="question-number">Q5</span>
                <span>目标院校层次</span>
              </div>
              <div class="level-selector">
                <div v-for="level in universityLevels" :key="level.value" class="level-item" :class="{ selected: formData.targetUniversityLevel === level.value }" @click="formData.targetUniversityLevel = level.value">
                  <div class="level-badge" :style="{ background: level.color }">{{ level.value }}</div>
                  <span class="level-name">{{ level.name }}</span>
                </div>
              </div>
            </div>

            <div class="question-card">
              <div class="question-title">
                <span class="question-number">Q6</span>
                <span>报考类型</span>
              </div>
              <div class="type-selector">
                <div class="type-item" :class="{ selected: formData.degreeType === '学硕' }" @click="formData.degreeType = '学硕'">
                  <div class="type-icon"><i class="el-icon-document"></i></div>
                  <div class="type-info">
                    <span class="type-name">学术型硕士</span>
                    <span class="type-desc">偏重理论研究，适合读博深造</span>
                  </div>
                </div>
                <div class="type-item" :class="{ selected: formData.degreeType === '专硕' }" @click="formData.degreeType = '专硕'">
                  <div class="type-icon"><i class="el-icon-suitcase"></i></div>
                  <div class="type-info">
                    <span class="type-name">专业型硕士</span>
                    <span class="type-desc">偏重实践应用，适合就业导向</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="question-card">
              <div class="question-title">
                <span class="question-number">Q7</span>
                <span>是否跨专业报考？</span>
              </div>
              <div class="switch-selector">
                <div class="switch-item" :class="{ selected: formData.majorType === '本专业' }" @click="formData.majorType = '本专业'">
                  <i class="el-icon-circle-check"></i>
                  <span>本专业报考</span>
                </div>
                <div class="switch-item" :class="{ selected: formData.majorType === '跨专业' }" @click="formData.majorType = '跨专业'">
                  <i class="el-icon-refresh-right"></i>
                  <span>跨专业报考</span>
                </div>
              </div>
            </div>
          </div>

          <div v-if="currentStep === 2" class="step-content">
            <div class="step-header">
              <h2>你的考试科目</h2>
              <p>我们将为你匹配对应的学习资源</p>
            </div>
            
            <div class="question-card">
              <div class="question-title">
                <span class="question-number">Q8</span>
                <span>英语科目</span>
              </div>
              <div class="subject-selector">
                <div class="subject-item" :class="{ selected: formData.englishSubject === '英语一' }" @click="formData.englishSubject = '英语一'">
                  <span class="subject-name">英语一</span>
                  <span class="subject-desc">学硕通常考英语一</span>
                </div>
                <div class="subject-item" :class="{ selected: formData.englishSubject === '英语二' }" @click="formData.englishSubject = '英语二'">
                  <span class="subject-name">英语二</span>
                  <span class="subject-desc">专硕通常考英语二</span>
                </div>
              </div>
            </div>

            <div class="question-card">
              <div class="question-title">
                <span class="question-number">Q9</span>
                <span>数学科目</span>
              </div>
              <div class="math-selector">
                <div v-for="math in mathOptions" :key="math.value" class="math-item" :class="{ selected: formData.mathSubject === math.value }" @click="formData.mathSubject = math.value">
                  <span class="math-name">{{ math.label }}</span>
                  <span class="math-desc">{{ math.desc }}</span>
                </div>
              </div>
            </div>

            <div class="question-card">
              <div class="question-title">
                <span class="question-number">Q10</span>
                <span>专业课名称</span>
              </div>
              <div class="major-input">
                <el-input v-model="formData.majorCourseName" placeholder="例如：408计算机学科专业基础、数据结构..." prefix-icon="el-icon-edit" clearable />
                <div class="major-tags">
                  <span class="tag-label">常见专业课：</span>
                  <span v-for="tag in commonMajors" :key="tag" class="major-tag" @click="formData.majorCourseName = tag">{{ tag }}</span>
                </div>
              </div>
            </div>
          </div>

          <div v-if="currentStep === 3" class="step-content">
            <div class="step-header">
              <h2>你的学习基础</h2>
              <p>了解你的现状，制定提升方案</p>
            </div>
            
            <div class="question-card">
              <div class="question-title">
                <span class="question-number">Q11</span>
                <span>各科目基础自评</span>
              </div>
              <div class="level-rating">
                <div class="rating-item">
                  <span class="rating-label">英语基础</span>
                  <div class="rating-stars">
                    <div v-for="n in 3" :key="'en'+n" class="star" :class="{ active: formData.englishLevel >= n }" @click="formData.englishLevel = n">
                      <i class="el-icon-star-on"></i>
                    </div>
                  </div>
                  <span class="rating-text">{{ levelTexts[formData.englishLevel] }}</span>
                </div>
                <div class="rating-item">
                  <span class="rating-label">数学基础</span>
                  <div class="rating-stars">
                    <div v-for="n in 3" :key="'math'+n" class="star" :class="{ active: formData.mathLevel >= n }" @click="formData.mathLevel = n">
                      <i class="el-icon-star-on"></i>
                    </div>
                  </div>
                  <span class="rating-text">{{ levelTexts[formData.mathLevel] }}</span>
                </div>
                <div class="rating-item">
                  <span class="rating-label">专业课基础</span>
                  <div class="rating-stars">
                    <div v-for="n in 3" :key="'major'+n" class="star" :class="{ active: formData.majorLevel >= n }" @click="formData.majorLevel = n">
                      <i class="el-icon-star-on"></i>
                    </div>
                  </div>
                  <span class="rating-text">{{ levelTexts[formData.majorLevel] }}</span>
                </div>
              </div>
            </div>

            <div class="question-card">
              <div class="question-title">
                <span class="question-number">Q12</span>
                <span>每日可用学习时长</span>
              </div>
              <div class="time-slider">
                <el-slider v-model="formData.dailyStudyHours" :min="1" :max="12" :marks="timeMarks" show-stops />
                <div class="time-display">
                  <i class="el-icon-time"></i>
                  <span>每天约 <strong>{{ formData.dailyStudyHours }}</strong> 小时</span>
                </div>
              </div>
            </div>

            <div class="question-card">
              <div class="question-title">
                <span class="question-number">Q13</span>
                <span>薄弱模块（可多选）</span>
              </div>
              <div class="weak-modules">
                <div v-for="module in weakModuleOptions" :key="module" class="module-tag" :class="{ selected: formData.weakModules.includes(module) }" @click="toggleWeakModule(module)">
                  {{ module }}
                </div>
              </div>
            </div>
          </div>

          <div v-if="currentStep === 4" class="step-content complete-step">
            <div class="complete-animation">
              <div class="check-circle">
                <i class="el-icon-check"></i>
              </div>
            </div>
            <div class="complete-content">
              <h2>太棒了！问卷已完成</h2>
              <p>AI正在为你生成专属学习计划...</p>
              <div class="summary-card">
                <div class="summary-header">
                  <i class="el-icon-document-checked"></i>
                  <span>你的学习档案</span>
                </div>
                <div class="summary-body">
                  <div class="summary-item">
                    <span class="summary-label">目标年份</span>
                    <span class="summary-value">{{ formData.targetYear }}年考研</span>
                  </div>
                  <div class="summary-item">
                    <span class="summary-label">备考身份</span>
                    <span class="summary-value">{{ formData.studyIdentity }}</span>
                  </div>
                  <div class="summary-item">
                    <span class="summary-label">目标层次</span>
                    <span class="summary-value">{{ formData.targetUniversityLevel }}</span>
                  </div>
                  <div class="summary-item">
                    <span class="summary-label">报考类型</span>
                    <span class="summary-value">{{ formData.degreeType }} · {{ formData.majorType }}</span>
                  </div>
                  <div class="summary-item">
                    <span class="summary-label">考试科目</span>
                    <span class="summary-value">{{ formData.englishSubject }} + {{ formData.mathSubject }} + 专业课</span>
                  </div>
                  <div class="summary-item">
                    <span class="summary-label">备考进度</span>
                    <span class="summary-value">{{ formData.currentProgress }}</span>
                  </div>
                  <div class="summary-item">
                    <span class="summary-label">每日学习</span>
                    <span class="summary-value">{{ formData.dailyStudyHours }}小时</span>
                  </div>
                </div>
              </div>
              <div class="ai-tips">
                <div class="tip-item">
                  <i class="el-icon-cpu"></i>
                  <span>AI将根据你的目标院校推荐学习资料</span>
                </div>
                <div class="tip-item">
                  <i class="el-icon-data-analysis"></i>
                  <span>智能分析薄弱点，针对性训练</span>
                </div>
                <div class="tip-item">
                  <i class="el-icon-trophy"></i>
                  <span>每日学习计划自动推送到首页</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </transition>

      <div class="action-footer">
        <el-button v-if="currentStep > 0" class="prev-btn" @click="prevStep">
          <i class="el-icon-arrow-left"></i>
          上一步
        </el-button>
        <div class="footer-spacer"></div>
        <el-button v-if="currentStep < steps.length - 1" class="next-btn" type="primary" :disabled="!canNextStep" @click="nextStep">
          下一步
          <i class="el-icon-arrow-right"></i>
        </el-button>
        <el-button v-if="currentStep === steps.length - 1" class="submit-btn" type="primary" :loading="submitting" @click="submitQuestionnaire">
          <i class="el-icon-check"></i>
          开始我的考研之旅
        </el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { submitQuestionnaire } from '@/api/user-questionnaire'

export default {
  name: 'OnboardingQuestionnaire',
  data() {
    return {
      currentStep: 0,
      submitting: false,
      steps: ['目标', '院校', '科目', '基础', '完成'],
      formData: {
        targetYear: '',
        attemptCount: '',
        studyIdentity: '',
        currentProgress: '',
        targetUniversityLevel: '',
        degreeType: '',
        majorType: '',
        englishSubject: '',
        mathSubject: '',
        majorCourseName: '',
        englishLevel: 0,
        mathLevel: 0,
        majorLevel: 0,
        dailyStudyHours: 4,
        weakModules: [],
        examType: '考研'
      },
      targetYears: [
        { value: '2026', label: '2026年', desc: '今年12月考试' },
        { value: '2027', label: '2027年', desc: '明年12月考试' }
      ],
      attemptOptions: [
        { value: '一战', label: '一战', desc: '首次参加考研', icon: 'el-icon-star-off' },
        { value: '二战', label: '二战', desc: '第二次备考', icon: 'el-icon-star-on' },
        { value: '三战+', label: '三战及以上', desc: '坚持就是胜利', icon: 'el-icon-trophy' }
      ],
      identityOptions: [
        { value: '应届本科', label: '应届本科', desc: '在校大学生', icon: 'el-icon-school' },
        { value: '往届脱产', label: '往届脱产', desc: '全职备考', icon: 'el-icon-reading' },
        { value: '在职上班族', label: '在职备考', desc: '边工作边备考', icon: 'el-icon-suitcase' }
      ],
      progressOptions: [
        { value: '未开始', label: '未开始', desc: '准备开始备考', icon: 'el-icon-document' },
        { value: '刚开始', label: '刚开始', desc: '刚开始不久', icon: 'el-icon-edit' },
        { value: '备考中已有基础', label: '已有基础', desc: '有一定基础', icon: 'el-icon-circle-check' }
      ],
      universityLevels: [
        { value: '985', name: '顶尖名校', color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
        { value: '211', name: '重点大学', color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
        { value: '双一流', name: '双一流高校', color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
        { value: '普本', name: '普通本科', color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)' }
      ],
      mathOptions: [
        { value: '数学一', label: '数学一', desc: '理工科' },
        { value: '数学二', label: '数学二', desc: '工科部分专业' },
        { value: '数学三', label: '数学三', desc: '经管类' },
        { value: '不考数学', label: '不考数学', desc: '文科/艺术等' }
      ],
      commonMajors: ['408计算机', '数据结构', '机械原理', '管理学', '金融学', '教育学'],
      levelTexts: ['', '薄弱', '一般', '扎实'],
      timeMarks: {
        1: '1h',
        4: '4h',
        8: '8h',
        12: '12h'
      },
      weakModuleOptions: [
        '英语阅读', '英语写作', '英语翻译',
        '高数', '线代', '概率论',
        '政治选择', '政治大题',
        '专业课计算', '专业课背诵'
      ]
    }
  },
  computed: {
    progressPercent() {
      return ((this.currentStep) / (this.steps.length - 1)) * 100
    },
    canNextStep() {
      const validations = [
        this.formData.targetYear && this.formData.attemptCount && this.formData.studyIdentity && this.formData.currentProgress,
        this.formData.targetUniversityLevel && this.formData.degreeType && this.formData.majorType,
        this.formData.englishSubject && this.formData.mathSubject && this.formData.majorCourseName,
        this.formData.englishLevel > 0 && this.formData.mathLevel > 0 && this.formData.majorLevel > 0,
        true
      ]
      return validations[this.currentStep]
    }
  },
  methods: {
    toggleWeakModule(module) {
      const index = this.formData.weakModules.indexOf(module)
      if (index > -1) {
        this.formData.weakModules.splice(index, 1)
      } else {
        this.formData.weakModules.push(module)
      }
    },
    nextStep() {
      if (this.canNextStep && this.currentStep < this.steps.length - 1) {
        this.currentStep++
      }
    },
    prevStep() {
      if (this.currentStep > 0) {
        this.currentStep--
      }
    },
    async submitQuestionnaire() {
      try {
        this.submitting = true
        const hours = this.formData.dailyStudyHours
        let dailyStudyHoursStr = '3-4h'
        if (hours <= 2) dailyStudyHoursStr = '2h内'
        else if (hours <= 4) dailyStudyHoursStr = '3-4h'
        else if (hours <= 7) dailyStudyHoursStr = '5-7h'
        else dailyStudyHoursStr = '8h以上'

        const submitData = {
          examType: this.formData.examType || '考研',
          targetYear: parseInt(this.formData.targetYear) || 2026,
          studyIdentity: this.formData.studyIdentity || '应届本科',
          dailyStudyHours: dailyStudyHoursStr,
          currentProgress: this.formData.currentProgress || '刚开始',
          attemptCount: this.formData.attemptCount || '一战',
          degreeType: this.formData.degreeType || '学硕',
          majorType: this.formData.majorType || '本专业',
          targetUniversityLevel: this.formData.targetUniversityLevel || '211',
          englishSubject: this.formData.englishSubject || '英语一',
          mathSubject: this.formData.mathSubject || '数学一',
          majorCourseName: this.formData.majorCourseName || '',
          englishLevel: String(this.formData.englishLevel || 1),
          mathLevel: String(this.formData.mathLevel || 1),
          majorLevel: String(this.formData.majorLevel || 1),
          weakModules: JSON.stringify(this.formData.weakModules || []),
          studyNeeds: JSON.stringify(['AI定制学习计划', '知识点答疑', '刷题推送', '错题复盘'])
        }
        await submitQuestionnaire(submitData)
        this.$message.success('问卷提交成功！AI个性化学习平台已为您准备好')
        localStorage.setItem('questionnaire_completed', 'true')
        this.$confirm('新用户首充享8折优惠，是否立即前往充值？', '恭喜完成设置', {
          confirmButtonText: '前往充值',
          cancelButtonText: '稍后再说',
          type: 'success',
          customClass: 'welcome-dialog'
        }).then(() => {
          this.$router.push('/token')
        }).catch(() => {
          this.$router.push('/home/index')
        })
      } catch (error) {
        this.$message.error('提交失败，请重试')
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.onboarding-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

.bg-decoration {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.4;

  &.orb-1 {
    width: 600px;
    height: 600px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    top: -200px;
    right: -200px;
  }

  &.orb-2 {
    width: 500px;
    height: 500px;
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    bottom: -150px;
    left: -150px;
  }

  &.orb-3 {
    width: 300px;
    height: 300px;
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
  }
}

.onboarding-content {
  width: 100%;
  max-width: 720px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  overflow: hidden;
  position: relative;
  z-index: 1;
}

.progress-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 24px 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .logo-section {
    display: flex;
    align-items: center;
    gap: 16px;

    .logo-icon {
      width: 48px;
      height: 48px;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;

      i {
        font-size: 24px;
        color: white;
      }
    }

    .logo-text {
      h1 {
        color: white;
        font-size: 20px;
        font-weight: 700;
        margin: 0;
      }

      p {
        color: rgba(255, 255, 255, 0.8);
        font-size: 12px;
        margin: 4px 0 0 0;
      }
    }
  }

  .progress-bar {
    display: flex;
    align-items: center;
    gap: 12px;

    .progress-track {
      width: 120px;
      height: 4px;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 2px;
      overflow: hidden;

      .progress-fill {
        height: 100%;
        background: white;
        border-radius: 2px;
        transition: width 0.3s ease;
      }
    }

    .progress-steps {
      display: flex;
      gap: 6px;

      .step-dot {
        width: 24px;
        height: 24px;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.2);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 12px;
        color: rgba(255, 255, 255, 0.6);
        transition: all 0.3s ease;

        &.active {
          background: white;
          color: #667eea;
        }

        &.completed {
          background: #4CAF50;
          color: white;
        }
      }
    }

    .progress-label {
      color: rgba(255, 255, 255, 0.8);
      font-size: 13px;
      font-weight: 500;
    }
  }
}

.step-container {
  padding: 32px;
  min-height: 480px;
}

.step-content {
  .step-header {
    text-align: center;
    margin-bottom: 32px;

    h2 {
      font-size: 24px;
      font-weight: 700;
      color: #1a1a2e;
      margin: 0 0 8px 0;
    }

    p {
      font-size: 14px;
      color: #666;
      margin: 0;
    }
  }
}

.question-card {
  background: #f8f9fa;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;

  .question-title {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 20px;
    font-size: 16px;
    font-weight: 600;
    color: #333;

    .question-number {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      padding: 4px 12px;
      border-radius: 20px;
      font-size: 13px;
      font-weight: 700;
    }
  }
}

.year-selector {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;

  .year-option {
    padding: 20px;
    background: white;
    border: 2px solid #e0e0e0;
    border-radius: 12px;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      border-color: #667eea;
      transform: translateY(-2px);
    }

    &.selected {
      border-color: #667eea;
      background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
    }

    .year-value {
      display: block;
      font-size: 20px;
      font-weight: 700;
      color: #333;
    }

    .year-desc {
      display: block;
      font-size: 12px;
      color: #999;
      margin-top: 4px;
    }
  }
}

.option-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;

  .option-item {
    padding: 20px 16px;
    background: white;
    border: 2px solid #e0e0e0;
    border-radius: 12px;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      border-color: #667eea;
      transform: translateY(-2px);
    }

    &.selected {
      border-color: #667eea;
      background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
    }

    i {
      font-size: 28px;
      color: #667eea;
      margin-bottom: 8px;
      display: block;
    }

    .option-label {
      display: block;
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }

    .option-desc {
      display: block;
      font-size: 12px;
      color: #999;
      margin-top: 4px;
    }
  }
}

.progress-selector {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;

  .progress-item {
    padding: 20px 16px;
    background: white;
    border: 2px solid #e0e0e0;
    border-radius: 12px;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      border-color: #667eea;
      transform: translateY(-2px);
    }

    &.selected {
      border-color: #667eea;
      background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
    }

    .progress-icon {
      width: 48px;
      height: 48px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 50%;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 8px;

      i {
        font-size: 24px;
        color: white;
      }
    }

    .progress-label {
      display: block;
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }

    .progress-desc {
      display: block;
      font-size: 12px;
      color: #999;
      margin-top: 4px;
    }
  }
}

.level-selector {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;

  .level-item {
    padding: 16px 12px;
    background: white;
    border: 2px solid #e0e0e0;
    border-radius: 12px;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      border-color: #667eea;
    }

    &.selected {
      border-color: #667eea;
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
    }

    .level-badge {
      display: inline-block;
      padding: 6px 16px;
      border-radius: 20px;
      color: white;
      font-size: 14px;
      font-weight: 700;
      margin-bottom: 8px;
    }

    .level-name {
      display: block;
      font-size: 13px;
      color: #666;
    }
  }
}

.type-selector {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;

  .type-item {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 20px;
    background: white;
    border: 2px solid #e0e0e0;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      border-color: #667eea;
    }

    &.selected {
      border-color: #667eea;
      background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
    }

    .type-icon {
      width: 48px;
      height: 48px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;

      i {
        font-size: 24px;
        color: white;
      }
    }

    .type-info {
      .type-name {
        display: block;
        font-size: 16px;
        font-weight: 600;
        color: #333;
      }

      .type-desc {
        display: block;
        font-size: 12px;
        color: #999;
        margin-top: 4px;
      }
    }
  }
}

.switch-selector {
  display: flex;
  gap: 16px;

  .switch-item {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    padding: 20px;
    background: white;
    border: 2px solid #e0e0e0;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      border-color: #667eea;
    }

    &.selected {
      border-color: #667eea;
      background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
    }

    i {
      font-size: 24px;
      color: #667eea;
    }

    span {
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }
  }
}

.subject-selector, .math-selector {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;

  .subject-item, .math-item {
    padding: 16px;
    background: white;
    border: 2px solid #e0e0e0;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      border-color: #667eea;
    }

    &.selected {
      border-color: #667eea;
      background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
    }

    .subject-name, .math-name {
      display: block;
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }

    .subject-desc, .math-desc {
      display: block;
      font-size: 12px;
      color: #999;
      margin-top: 4px;
    }
  }
}

.major-input {
  .major-tags {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
    margin-top: 12px;

    .tag-label {
      font-size: 13px;
      color: #999;
    }

    .major-tag {
      padding: 4px 12px;
      background: #f0f0f0;
      border-radius: 16px;
      font-size: 12px;
      color: #666;
      cursor: pointer;
      transition: all 0.2s ease;

      &:hover {
        background: #667eea;
        color: white;
      }
    }
  }
}

.level-rating {
  .rating-item {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 16px;
    background: white;
    border-radius: 12px;
    margin-bottom: 12px;

    .rating-label {
      width: 80px;
      font-size: 14px;
      font-weight: 500;
      color: #333;
    }

    .rating-stars {
      display: flex;
      gap: 8px;

      .star {
        width: 36px;
        height: 36px;
        border-radius: 50%;
        background: #f0f0f0;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: all 0.2s ease;

        i {
          font-size: 20px;
          color: #ccc;
        }

        &.active {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

          i {
            color: white;
          }
        }

        &:hover {
          transform: scale(1.1);
        }
      }
    }

    .rating-text {
      font-size: 14px;
      color: #667eea;
      font-weight: 500;
      min-width: 40px;
    }
  }
}

.time-slider {
  padding: 16px;
  background: white;
  border-radius: 12px;

  .time-display {
    text-align: center;
    margin-top: 16px;
    font-size: 16px;
    color: #333;

    i {
      color: #667eea;
      margin-right: 8px;
    }

    strong {
      color: #667eea;
      font-size: 24px;
    }
  }
}

.weak-modules {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;

  .module-tag {
    padding: 10px 20px;
    background: white;
    border: 2px solid #e0e0e0;
    border-radius: 20px;
    font-size: 14px;
    color: #666;
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover {
      border-color: #667eea;
    }

    &.selected {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-color: #667eea;
      color: white;
    }
  }
}

.complete-step {
  text-align: center;

  .complete-animation {
    margin-bottom: 24px;

    .check-circle {
      width: 80px;
      height: 80px;
      background: linear-gradient(135deg, #4CAF50 0%, #8BC34A 100%);
      border-radius: 50%;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      animation: scaleIn 0.5s ease;

      i {
        font-size: 40px;
        color: white;
      }
    }
  }

  .complete-content {
    h2 {
      font-size: 28px;
      font-weight: 700;
      color: #333;
      margin: 0 0 8px 0;
    }

    p {
      font-size: 16px;
      color: #666;
      margin: 0 0 32px 0;
    }
  }

  .summary-card {
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
    border-radius: 16px;
    padding: 24px;
    margin-bottom: 24px;
    text-align: left;

    .summary-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 600;
      color: #333;
      margin-bottom: 16px;

      i {
        color: #667eea;
      }
    }

    .summary-body {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 12px;

      @media (max-width: 480px) {
        grid-template-columns: 1fr;
      }

      .summary-item {
        .summary-label {
          display: block;
          font-size: 12px;
          color: #999;
        }

        .summary-value {
          display: block;
          font-size: 14px;
          font-weight: 500;
          color: #333;
          margin-top: 4px;
        }
      }
    }
  }

  .ai-tips {
    display: flex;
    flex-direction: column;
    gap: 12px;

    .tip-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px 20px;
      background: white;
      border-radius: 12px;
      font-size: 14px;
      color: #666;

      i {
        color: #667eea;
        font-size: 20px;
      }
    }
  }
}

@keyframes scaleIn {
  from {
    transform: scale(0);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

.action-footer {
  padding: 24px 32px;
  background: #f8f9fa;
  border-top: 1px solid #e0e0e0;
  display: flex;
  align-items: center;

  .footer-spacer {
    flex: 1;
  }

  .prev-btn, .next-btn, .submit-btn {
    padding: 14px 28px;
    border-radius: 12px;
    font-size: 15px;
    font-weight: 500;
  }

  .prev-btn {
    background: white;
    border: 1px solid #e0e0e0;
    color: #666;

    &:hover {
      border-color: #667eea;
      color: #667eea;
    }
  }

  .next-btn, .submit-btn {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;

    &:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
    }

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }
}

.slide-fade-enter-active {
  transition: all 0.3s ease;
}

.slide-fade-leave-active {
  transition: all 0.2s ease;
}

.slide-fade-enter {
  transform: translateX(20px);
  opacity: 0;
}

.slide-fade-leave-to {
  transform: translateX(-20px);
  opacity: 0;
}

@media (max-width: 768px) {
  .onboarding-content {
    border-radius: 16px;
  }

  .progress-header {
    flex-direction: column;
    gap: 16px;
    padding: 20px;

    .progress-bar {
      width: 100%;
      justify-content: center;
    }
  }

  .step-container {
    padding: 20px;
  }

  .option-grid {
    grid-template-columns: 1fr;
  }

  .level-selector {
    grid-template-columns: repeat(2, 1fr);
  }

  .type-selector {
    grid-template-columns: 1fr;
  }

  .complete-step .summary-card .summary-body {
    grid-template-columns: 1fr;
  }
}
</style>
