<template>
  <el-container style="height: 100vh; border: 1px solid #eee">
    <!-- <div class="left">
      <div class="fk">
        <div
          style="
            font-size: 25px;
            font-weight: 500;
            margin-left: 10px;
            padding: 10px 0 0 0;
          "
        >
          11
        </div>
        <div class="sj">
          <div>
            <span>得分</span>
            <span>50</span>
          </div>
          <div>
            <span>耗时</span>
            <span>50</span>
          </div>
          <div>
            <span>提交人</span>
            <span>50</span>
          </div>
          <el-divider></el-divider>
          <p>
            共 <span style="color: #1890ff">5 </span> 题, 共
            <span style="color: #1890ff">100</span> 分
          </p>
          <el-row>
            <el-tag
              v-for="index in 5"
              :type="index === quIndex ? 'success' : ''"
              @click="handleTag(index)"
              class="type_tag"
            >
              {{ index }}
            </el-tag>
          </el-row>
        </div>
      </div>
    </div> -->

    <el-container>
      <el-main class="right">
        <!-- AI阅卷按钮区域 -->
        <el-row style="margin-bottom: 20px;">
          <el-col :span="24">
            <el-card shadow="hover" class="ai-grading-header">
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <div>
                  <h3 style="margin: 0; color: #409EFF;">
                    <i class="el-icon-cpu" />
                    {{ isTeacherOrAdmin ? '考试详情 - AI阅卷功能' : '考试详情 - 答案解析' }}
                  </h3>
                  <p style="margin: 5px 0 0 0; color: #666; font-size: 14px;">
                    {{ isTeacherOrAdmin ? '教师可通过AI对简答题进行智能评分' : '查看每道题目的AI智能解析' }}
                  </p>
                </div>
                <div>
                  <el-button
                    v-if="isTeacherOrAdmin"
                    type="primary"
                    icon="el-icon-cpu"
                    :loading="entireExamLoading"
                    @click="handleGradeEntireExam"
                  >
                    整卷AI阅卷
                  </el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-col>
          <el-card class="qu_list">
            <div>
              <!-- eslint-disable-next-line vue/no-template-shadow -->
              <template v-for="(index, indexx) in data">
                <!-- eslint-disable-next-line vue/require-v-for-key -->
                <div
                  v-if="
                    index.quType === 1 ||
                      index.quType === 2 ||
                      index.quType === 3
                  "
                  :class="'index' + index"
                >
                  <el-row :gutter="24">
                    <el-col :span="20" style="text-align: left">
                      <!-- 题目: 序号、类型、题干 -->
                      <div>
                        <!-- <div class="qu_num">{{ index }}</div> -->
                        <!-- 【 单选题 】 -->
                        <div class="qu_content">
                          {{ indexx + 1 }}、{{ index.title }}
                          <span style="color: #409EFF; font-size: 13px; margin-left: 8px;">（{{ index.score }}分）</span>
                        </div>

                        <!-- <div v-if="item.image != null && item.image != ''" style="clear: both">
                          <el-image :src="item.image" style="max-width: 200px" />
                        </div> -->
                      </div>
                      <div v-if="index.image != null && index.image != ''">
                        <el-image
                          :src="index.image"
                          :preview-src="[index.image]"
                          style="height: 100px"
                        />
                      </div>
                      <!-- 选项 -->
                      <el-radio-group class="qu_choose_group">
                        <!-- ['A', 'B', 'C', 'D'] -->
                        <el-radio
                          v-for="(item, indexs) in index.option"
                          :key="indexs"
                          :label="item.content"
                          border
                          class="qu_choose"
                          :class="{
                            imgC: item.image != null && item.image != '',
                            isRight:
                              index.myOption != null &&
                              isCheck(index.myOption, item.sort) &&
                              item.isRight,
                            incorrect:
                              index.myOption != null &&
                              isCheck(index.myOption, item.sort) &&
                              !item.isRight,
                          }"
                        >
                          <!-- 选项flex浮动 -->
                          <div class="qu_choose_tag">
                            <div class="qu_choose_tag_type">
                              {{ numberToLetter(indexs) }}、{{ item.content }}.
                            </div>
                            <!-- 选项内容和图片 -->
                            <div
                              v-if="item.image != null && item.image != ''"
                              style="clear: both"
                            >
                              <el-image
                                :src="item.image"
                                :preview-src="[item.image]"
                                style="max-width: 200px"
                              />
                            </div>
                            <div v-if="item.image != null && item.image != ''">
                              <el-image
                                :src="item.image"
                                :preview-src="[item.image]"
                                class="qu_choose_tag_img"
                              />
                            </div>
                          </div>
                        </el-radio>
                      </el-radio-group>

                      <!-- 题目解析 -->
                      <div class="qu_analysis">
                        <el-card>
                          <div>
                            <span>题目分值：</span>
                            <span style="font-weight: bold; color: #409EFF;">{{ index.score }}分</span>
                            <template v-if="index.actualScore != null">
                              <span style="margin-left: 10px; font-weight: bold;" :style="{ color: index.actualScore > 0 ? '#67C23A' : '#F56C6C' }">得分：{{ index.actualScore }}分</span>
                            </template>
                            <template v-else-if="index.isRight === 1">
                              <span style="margin-left: 10px; color: #67C23A; font-weight: bold;">得分：{{ index.score }}分</span>
                            </template>
                            <template v-else-if="index.isRight === 0">
                              <span style="margin-left: 10px; color: #F56C6C; font-weight: bold;">得分：0分</span>
                            </template>
                            <br>
                          </div>
                          <div style="margin-top: 8px">
                            <span>考生答案：</span>
                            <span>{{ numberToLetter(index.myOption) }}</span><br>
                          </div>
                          <div style="margin-top: 8px">
                            <span>正确答案：</span>
                            <span>{{ numberToLetter(index.rightOption) }}</span><br>
                          </div>
                          <div style="margin-top: 8px">
                            <span>试题解析：</span>
                            <span>{{ index.analyse }}</span><br>
                          </div>
                          <!-- AI阅卷/解析按钮 -->
                          <div v-if="index.quType === 4" style="margin-top: 15px; text-align: right;">
                            <el-button
                              v-if="isTeacherOrAdmin"
                              type="primary"
                              size="mini"
                              icon="el-icon-cpu"
                              :loading="aiGradingLoading === index.id"
                              @click="handleAiGrading(index)"
                            >
                              AI阅卷
                            </el-button>
                            <el-button
                              v-else
                              type="success"
                              size="mini"
                              icon="el-icon-cpu"
                              :loading="aiGradingLoading === index.id"
                              @click="handleAiAnalyze(index)"
                            >
                              AI解析
                            </el-button>
                            <el-button
                              type="warning"
                              size="mini"
                              icon="el-icon-chat-dot-round"
                              @click="handleOpenChatBubble(index)"
                            >
                              AI答疑
                            </el-button>
                          </div>
                          <!-- 客观题区域 -->
                          <div v-else style="margin-top: 15px; text-align: right;">
                            <el-button
                              type="warning"
                              size="mini"
                              icon="el-icon-chat-dot-round"
                              @click="handleOpenChatBubble(index)"
                            >
                              AI答疑
                            </el-button>
                          </div>
                        </el-card>
                      </div>
                    </el-col>
                  </el-row>
                  <el-divider />
                </div>
              </template>
              <!-- eslint-disable-next-line vue/no-template-shadow -->
              <template v-for="index in data">
                <!-- eslint-disable-next-line vue/require-v-for-key -->
                <div v-if="index.quType === 4" :class="'index' + index">
                  <el-row :gutter="24">
                    <el-col :span="20" style="text-align: left">
                      <!-- 题目: 序号、类型、题干 -->
                      <div>
                        <!-- <div class="qu_num">{{ index }}</div> -->
                        <!-- 【 单选题 】 -->
                        <div class="qu_content">{{ index.title }}
                          <span style="color: #409EFF; font-size: 13px; margin-left: 8px;">（{{ index.score }}分）</span>
                        </div>
                      </div>

                      <!-- 选项 -->
                      <el-radio-group class="qu_choose_group">
                        <!-- ['A', 'B', 'C', 'D'] -->
                        <el-input
                          v-model="index.myOption"
                          style="margin-top: 10px"
                          type="textarea"
                          :autosize="{ minRows: 2, maxRows: 4 }"
                          placeholder=""
                          :disabled="true"
                        />
                      </el-radio-group>

                      <!-- 题目解析 -->
                      <div class="qu_analysis">
                        <el-card>
                          <div>
                            <span>题目分值：</span>
                            <span style="font-weight: bold; color: #409EFF;">{{ index.score }}分</span>
                            <template v-if="index.actualScore != null">
                              <span style="margin-left: 10px; font-weight: bold;" :style="{ color: index.actualScore > 0 ? '#E6A23C' : '#F56C6C' }">得分：{{ index.actualScore }}分</span>
                              <el-tag v-if="index.aiScore != null && index.aiScore > 0" type="warning" size="mini" effect="plain" style="margin-left: 4px;">AI</el-tag>
                            </template>
                            <br>
                          </div>
                          <div v-if="index.aiReason" style="margin-top: 8px">
                            <span>AI评分分析：</span>
                            <span style="white-space: pre-line; color: #606266;">{{ index.aiReason }}</span><br>
                          </div>
                          <div style="margin-top: 8px">
                            <span>正确答案：</span>
                            <span>{{ index.rightOption }}</span>
                            <br>
                          </div>
                          <div style="margin-top: 8px">
                            <span>试题解析：</span>
                            <span>{{ index.analyse }}</span><br>
                          </div>
                          <!-- AI阅卷/解析按钮 -->
                          <div style="margin-top: 15px; text-align: right;">
                            <el-button
                              v-if="isTeacherOrAdmin"
                              type="primary"
                              size="mini"
                              icon="el-icon-cpu"
                              :loading="aiGradingLoading === index.id"
                              @click="handleAiGrading(index)"
                            >
                              AI阅卷
                            </el-button>
                            <el-button
                              v-else
                              type="success"
                              size="mini"
                              icon="el-icon-cpu"
                              :loading="aiGradingLoading === index.id"
                              @click="handleAiAnalyze(index)"
                            >
                              AI解析
                            </el-button>
                            <el-button
                              type="warning"
                              size="mini"
                              icon="el-icon-chat-dot-round"
                              @click="handleOpenChatBubble(index)"
                            >
                              AI答疑
                            </el-button>
                          </div>
                        </el-card>
                      </div>
                    </el-col>
                  </el-row>
                  <el-divider />
                </div>
              </template>
            </div>
            <el-divider />
          </el-card>
        </el-col>
      </el-main>
    </el-container>

    <!-- AI阅卷/解析对话框 -->
    <ai-grading-dialog
      :visible.sync="aiGradingDialogVisible"
      :question-data="currentQuestion"
      :exam-id="examId"
      :mode="aiDialogMode"
      :config-id="selectedConfigId"
      @close="handleAiGradingClose"
    />

    <!-- AI题目答疑悬浮窗口 -->
    <ai-chat-bubble
      :visible="chatBubbleVisible"
      :question-title="chatBubbleQuestion"
      :standard-answer="chatBubbleStandardAnswer"
      :user-answer="chatBubbleUserAnswer"
      :config-id="selectedConfigId"
      @close="handleChatBubbleClose"
    />
  </el-container>
</template>

<script>
import { recordExamDetail } from '@/api/record'
import { aiGradeEntireExam } from '@/api/ai-grading'
import { getRole } from '@/utils/auth'
import AiGradingDialog from '@/components/AiGrading/AiGradingDialog.vue'
import AiChatBubble from '@/components/AiGrading/AiChatBubble.vue'

export default {
  name: 'ExamProcess',
  components: {
    AiGradingDialog,
    AiChatBubble
  },
  data() {
    return {
      input: '',
      quIndex: -1,
      examId: 0,
      data: null,
      userId: null,
      index: {
        quType: 4
      },
      // AI阅卷相关
      aiGradingDialogVisible: false,
      currentQuestion: null,
      aiGradingLoading: null,
      entireExamLoading: false,
      aiDialogMode: 'grade',
      // AI答疑悬浮窗口
      chatBubbleVisible: false,
      chatBubbleQuestion: '',
      chatBubbleStandardAnswer: '',
      chatBubbleUserAnswer: ''
    }
  },
  computed: {
    isTeacherOrAdmin() {
      const role = getRole()
      return role === 'role_teacher' || role === 'role_admin'
    },
    selectedConfigId() {
      return this.$store.getters.aiConfigId
    }
  },
  created() {
    this.$store.dispatch('aiModel/loadModels')

    if (this.$route.query?.data?.type === 1) {
      this.userId = this.$route.query.data.userId
    }

    // 优先从路由参数获取examId，其次从localStorage获取
    const routeExamId = this.$route.query.examId || this.$route.query.zhi?.examId
    if (routeExamId) {
      this.examId = parseInt(routeExamId)
      localStorage.setItem('record_exam_examId', this.examId)
    } else {
      const storedExamId = localStorage.getItem('record_exam_examId')
      this.examId = storedExamId ? parseInt(storedExamId) : 0
    }

    console.log('考试ID:', this.examId)

    if (this.examId > 0) {
      this.ExamDetail()
    } else {
      this.$message.error('考试ID无效，请从考试记录列表进入')
    }
  },
  methods: {
    isCheck(myOption, sort) {
      const arr = myOption.split(',').map(Number) // 将字符串转换为数字数组
      if (arr.includes(sort)) {
        return true
      } else {
        return false
      }
    },
    numberToLetter(input) {
      const numberToCharMap = {
        0: 'A',
        1: 'B',
        2: 'C',
        3: 'D',
        4: 'E',
        5: 'F'
      }

      // 辅助函数：将单个数字（字符串或数字类型）转换为字母
      const singleNumberToLetter = (num) =>
        numberToCharMap[parseInt(num, 10)] || ''

      // 辅助函数：处理逗号分隔的数字字符串
      const commaSeparatedNumbersToLetters = (str) => {
        const numbers = str.split(',').map((item) => parseInt(item.trim(), 10))
        return numbers.map((number) => numberToCharMap[number] || '').join(',')
      }

      // 判断输入类型并调用相应函数
      if (/^\d+$/.test(input)) {
        // 单个数字（字符串形式也可以匹配）
        return singleNumberToLetter(input)
      } else if (/^\d+(,\d+)*$/.test(input)) {
        // 包含逗号分隔的数字字符串
        return commaSeparatedNumbersToLetters(input)
      } else {
        return '' // 输入不符合预期，返回空字符串或根据需要处理
      }
    },
    // 分页查询
    async ExamDetail() {
      const params = { examId: this.examId, userId: this.userId }
      const res = await recordExamDetail(params)
      this.data = res.data
    },
    // 点击答题卡题号, 右侧题目滑动
    handleTag(index) {
      // 高亮选中的题目index标签
      this.quIndex = index
      // 题目滑动到锚定点
      const page = document.querySelector('.index' + index)
      page.scrollIntoView()
    },

    // AI阅卷单个题目
    handleAiGrading(question) {
      this.currentQuestion = question
      this.aiDialogMode = 'grade'
      this.aiGradingDialogVisible = true
    },

    // AI解析单个题目（学生端）
    handleAiAnalyze(question) {
      this.currentQuestion = question
      this.aiDialogMode = 'analyze'
      this.aiGradingDialogVisible = true
    },

    // 整卷AI阅卷（只对简答题进行AI阅卷）
    async handleGradeEntireExam() {
      this.entireExamLoading = true
      try {
        // 检查是否有简答题
        const essayQuestions = this.data?.filter(q => q.quType === 4) || []

        if (essayQuestions.length === 0) {
          this.$message.info('本次考试没有简答题，无需AI阅卷')
          return
        }

        this.$message.info(`检测到 ${essayQuestions.length} 道简答题，开始AI阅卷...`)

        const res = await aiGradeEntireExam(this.examId)
        if (res.code) {
          if (res.data.aiScore > 0) {
            this.$message.success(`简答题AI阅卷完成！AI评分：${res.data.aiScore}分`)
            this.$notify({
              title: '简答题AI阅卷结果',
              message: `已对${essayQuestions.length}道简答题进行AI评分，总分由系统自动计算`,
              type: 'success',
              duration: 5000
            })
          } else {
            this.$message.success('AI阅卷任务已提交，系统正在对简答题进行智能评分')
            this.$notify({
              title: '简答题AI阅卷进行中',
              message: `正在对${essayQuestions.length}道简答题进行AI评分`,
              type: 'info',
              duration: 5000
            })
          }

          // 刷新页面数据以显示AI评分结果
          setTimeout(() => {
            this.ExamDetail()
          }, 2000)
        } else {
          this.$message.error(res.msg || '简答题AI阅卷失败')
        }
      } catch (error) {
        console.error('简答题AI阅卷异常:', error)
        this.$message.error('简答题AI阅卷异常，请重试')
      } finally {
        this.entireExamLoading = false
      }
    },

    // AI阅卷对话框关闭
    handleAiGradingClose() {
      this.aiGradingDialogVisible = false
      this.currentQuestion = null
    },

    // AI答疑悬浮窗口
    handleOpenChatBubble(question) {
      this.chatBubbleQuestion = question.title || question.content || ''
      this.chatBubbleStandardAnswer = question.rightOption || question.analyse || ''
      this.chatBubbleUserAnswer = question.myOption || ''
      this.chatBubbleVisible = true
    },

    handleChatBubbleClose() {
      this.chatBubbleVisible = false
    }
  }
}
</script>

<style scoped lang="scss">
.content {
  width: 97%;
  height: 60px;
  border: 1px solid #0a84ff;
  margin-top: 8px;
  margin-left: 10px;
  padding: 10px;
  font-weight: 200;
}
.sj {
  margin-top: 10px;
  margin-left: 10px;
  line-height: 22px;
}
.isRight {
  background-color: rgb(215, 245, 215);
}
.incorrect {
  background-color: rgb(248, 197, 197);
}
.fk {
  width: 200px;
  height: 100%;
  box-shadow: 0 0 15px rgb(197, 197, 197);
  margin: auto;
  margin-top: 20px;
  margin-left: 15px;
}
.el-header {
  background-color: #b3c0d1;
  color: #333;
  line-height: 60px;
}

.left {
  width: 250px;
  height: 100%;
}
.right {
  width: 70%;
  height: 100%;
}
.el-divider--horizontal {
  display: block;
  height: 1px;
  width: 95%;
  margin: 24px 0;
}
.type_tag {
  margin-right: 5px;
  margin-top: 10px;
}

// 试题内容样式
.qu_list {
  height: 100%;
  width: 100%;
  overflow: auto;
  page-break-after: always;

  .qu_num {
    display: inline-block;
    // background: url('~@/assets/images/tkxl/btbj.png') no-repeat 100% 100%;
    background-size: contain;
    height: 30px;
    width: 30px;
    line-height: 25px;
    color: #fff;
    font-size: 14px;
    text-align: center;
    margin-right: 15px;
    flex-shrink: 0;
  }

  .qu_content {
    padding-left: 10px;
  }

  // 选项组
  .qu_choose_group {
    width: 100%;

    // 单个选项
    .qu_choose {
      display: block;
      margin: 10px;

      // 去除前面的radio
      ::v-deep .el-radio__input .el-radio__inner {
        display: none;
      }

      // 单个选项内容样式
      .qu_choose_tag {
        display: inline-flex;
        width: 90%;
        // 选项标签
        .qu_choose_tag_type {
          font-weight: bold;
          // color: #0a84ff;
          width: 10px;
        }
        // 选项内容
        .qu_choose_tag_content {
          padding: 0 10px 10px 10px;
        }
        .qu_choose_tag_img {
          // max-height:90px;
          // max-width:300px;
          height: 100px;
          display: block;
          margin: 10px;
        }

        .qu_choose_tag_el_image {
          clear: both;
          padding-top: 10px;
        }
      }
      // 选项答案
      .qu_choose_answer {
        float: right;
      }
    }
  }

  // 试题解析
  .qu_analysis {
    padding: 10px;

    .qu_analysis_content {
      padding-top: 10px;
    }
  }

  // 试题赋分
  .qu_assign_score {
    background: #f5f5f5;
    height: 100px;
    padding-top: 35px;

    .qu_assign_score_content {
      width: 80px;
    }
  }
}
.imgC {
  height: 150px;
}
</style>
