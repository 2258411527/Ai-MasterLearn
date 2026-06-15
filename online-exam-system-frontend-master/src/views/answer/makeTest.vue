<template>
  <el-container style="height: 700px; border: 1px solid #eee">
    <div class="left">
      <div class="fk">
        <div class="sj">
          <el-divider />
          <p>
            共 <span style="color: #1890ff"> {{ waitQuList.length }} </span> 题, 共
            <span style="color: #1890ff">{{
              waitQuList.length > 0 ? waitQuList.reduce((s, q) => s + (q.totalScore || 0), 0) : 0
            }}</span>
            分
          </p>
          <el-row>
            <el-tag
              v-for="(item, index) in waitQuList"
              :key="index"
              :type="index === quIndex ? 'success' : ''"
              class="type_tag"
              @click="handleTag(index)"
            >
              {{ index + 1 }}
            </el-tag>
          </el-row>
          <el-button type="primary" class="ann" :loading="aiGradingAllLoading" @click="handleAiGradeAll">AI辅助评分</el-button>
          <el-button type="success" class="ann" @click="subCorrect">提交批改</el-button>
        </div>
      </div>
    </div>

    <el-container>
      <el-main class="right">
        <el-col>
          <el-card class="qu_list">
            <div>
              <template v-for="(item, index) in waitQuList">
                <div :key="'q_' + index" :class="'index' + index">
                  <el-row :gutter="24">
                    <el-col :span="20" style="text-align: left">
                      <div>
                        <div class="qu_content">{{ index + 1 }}. {{ item.quTitle }}</div>
                      </div>
                      <div class="content">
                        {{ item.answer }}
                      </div>
                      <div class="qu_analysis">
                        <el-card>
                          <div style="display: flex; align-items: center; flex-wrap: wrap; gap: 8px;">
                            <span style="color: #e6a23c; font-weight: 600;">分数：</span>
                            <el-input
                              v-model="item.correctScore"
                              type="number"
                              style="width: 100px"
                            />
                            <span style="color: #909399; font-size: 12px;">/ {{ item.totalScore }}分</span>
                            <span
                              v-if="item.correctScore !== undefined && item.correctScore !== null && (item.correctScore < 0 || item.correctScore > item.totalScore)"
                              style="color: #f00; margin-left: 10px; font-size: 12px;"
                            >
                              评分只能在 0-{{ item.totalScore }}之间
                            </span>
                            <el-button
                              type="primary"
                              size="mini"
                              icon="el-icon-cpu"
                              :loading="item.aiLoading"
                              @click="handleAiGradeSingle(item, index)"
                              style="margin-left: auto;"
                            >
                              AI评分
                            </el-button>
                          </div>
                          <div v-if="item.aiSuggestedScore !== null && item.aiSuggestedScore !== undefined" class="ai-suggestion">
                            <el-tag type="warning" size="small" effect="dark" style="margin-right: 8px;">AI建议: {{ item.aiSuggestedScore }}分</el-tag>
                            <el-button type="text" size="mini" @click="item.correctScore = item.aiSuggestedScore">采纳</el-button>
                          </div>
                          <div style="margin-top: 18px">
                            <span style="font-weight: 600;">参考答案:</span>
                            <br>
                            <span>{{ item.refAnswer || '暂无' }}</span>
                          </div>
                          <div v-if="item.aiReason" style="margin-top: 14px; background: #f0f9ff; border-radius: 6px; padding: 12px; border-left: 3px solid #3b82f6;">
                            <div style="font-weight: 600; color: #3b82f6; margin-bottom: 6px;">
                              <i class="el-icon-cpu" /> AI评分分析
                            </div>
                            <div style="color: #475569; font-size: 13px; line-height: 1.6; white-space: pre-wrap;">{{ item.aiReason }}</div>
                          </div>
                        </el-card>
                      </div>
                    </el-col>
                    <el-col :span="4">
                      <el-row class="qu_assign_score">
                        本题
                        <el-input-number
                          :controls="false"
                          :min="0"
                          :precision="2"
                          disabled
                          :value="item.totalScore"
                          class="qu_assign_score_content"
                        />
                        分
                      </el-row>
                    </el-col>
                  </el-row>
                </div>
              </template>
            </div>
            <el-divider />
          </el-card>
        </el-col>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import { answerDetail, correct } from '@/api/answer'
import { aiGradeSingleQuestion } from '@/api/ai-grading'
import { getUserId } from '@/utils/auth'

export default {
  name: 'ExamProcess',
  data() {
    return {
      quIndex: -1,
      info: {},
      waitQuList: [],
      scoreData: null,
      aiGradingAllLoading: false
    }
  },
  created() {
    this.info = JSON.parse(sessionStorage.getItem('answer_info'))
    this.getUserAnswerDetail()
  },
  methods: {
    handleTag(index) {
      this.quIndex = index
      const page = document.querySelector('.index' + index)
      if (page) page.scrollIntoView()
    },
    async getUserAnswerDetail() {
      const params = { userId: this.info.userId, examId: this.info.examId }
      const res = await answerDetail(params)
      if (res.code === 1 && res.data) {
        this.waitQuList = res.data.map(item => ({
          ...item,
          aiLoading: false,
          aiSuggestedScore: null
        }))
      }
    },
    async handleAiGradeSingle(item, index) {
      this.$set(item, 'aiLoading', true)
      try {
        const params = {
          examId: item.examId,
          userId: item.userId,
          questionId: item.quId,
          userAnswer: item.answer,
          questionContent: item.quTitle,
          gradingMode: true
        }
        const res = await aiGradeSingleQuestion(params)
        if (res.code === 1 && res.data) {
          const data = res.data
          if (data.aiScore !== null && data.aiScore !== undefined) {
            this.$set(item, 'aiSuggestedScore', data.aiScore)
            this.$set(item, 'correctScore', data.aiScore)
          }
          if (data.detailedAnalysis) {
            this.$set(item, 'aiReason', data.detailedAnalysis)
          } else if (data.scoringCriteria) {
            this.$set(item, 'aiReason', data.scoringCriteria)
          }
          this.$message.success(`第${index + 1}题AI评分完成，建议${data.aiScore}分`)
        } else {
          this.$message.error(res.msg || 'AI评分失败')
        }
      } catch (e) {
        console.error('AI评分异常', e)
        this.$message.error('AI评分请求失败')
      } finally {
        this.$set(item, 'aiLoading', false)
      }
    },
    async handleAiGradeAll() {
      if (this.waitQuList.length === 0) return
      this.aiGradingAllLoading = true
      let successCount = 0
      try {
        for (let i = 0; i < this.waitQuList.length; i++) {
          const item = this.waitQuList[i]
          this.$set(item, 'aiLoading', true)
          try {
            const params = {
              examId: item.examId,
              userId: item.userId,
              questionId: item.quId,
              userAnswer: item.answer,
              questionContent: item.quTitle,
              gradingMode: true
            }
            const res = await aiGradeSingleQuestion(params)
            if (res.code === 1 && res.data) {
              const data = res.data
              if (data.aiScore !== null && data.aiScore !== undefined) {
                this.$set(item, 'aiSuggestedScore', data.aiScore)
                this.$set(item, 'correctScore', data.aiScore)
              }
              if (data.detailedAnalysis) {
                this.$set(item, 'aiReason', data.detailedAnalysis)
              } else if (data.scoringCriteria) {
                this.$set(item, 'aiReason', data.scoringCriteria)
              }
              successCount++
            }
          } catch (e) {
            console.error(`第${i + 1}题AI评分异常`, e)
          } finally {
            this.$set(item, 'aiLoading', false)
          }
        }
        this.$message.success(`AI评分完成，成功${successCount}/${this.waitQuList.length}题`)
      } finally {
        this.aiGradingAllLoading = false
      }
    },
    subCorrect() {
      const list = []
      for (let i = 0; i < this.waitQuList.length; i++) {
        const element = this.waitQuList[i]
        if (element.correctScore === null || element.correctScore === undefined || element.correctScore === '') {
          this.$message({ message: `请先给第${i + 1}题评分`, type: 'error' })
          return
        }
        if (element.correctScore < 0 || element.correctScore > element.totalScore) {
          this.$message({ message: `第${i + 1}题的评分只能在0-${element.totalScore}之间`, type: 'error' })
          return
        }
        list.push({
          userId: element.userId,
          examId: element.examId,
          questionId: element.quId,
          score: element.correctScore
        })
      }
      correct(list).then((res) => {
        if (res.code) {
          this.$notify({ title: '成功', message: `${res.msg}`, type: 'success', duration: 2000 })
          this.$router.push({ name: 'answer-show' })
        } else {
          this.$notify({ title: '失败', message: `${res.msg}`, type: 'error', duration: 2000 })
        }
      })
    }
  }
}
</script>

<style scoped lang="scss">
.content {
  width: 97%;
  min-height: 60px;
  border: 1px solid #0a84ff;
  margin-top: 8px;
  margin-left: 10px;
  padding: 10px;
  font-weight: 200;
}
.ann {
  width: 130px;
  margin-top: 15px;
  margin-left: 15px;
}
.sj {
  margin-top: 10px;
  margin-left: 10px;
  line-height: 22px;
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
  width: 17%;
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
.ai-suggestion {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.qu_list {
  height: 100%;
  width: 100%;
  overflow: auto;
  page-break-after: always;

  .qu_content {
    padding-left: 10px;
  }

  .qu_analysis {
    padding: 10px;
  }

  .qu_assign_score {
    background: #f5f5f5;
    height: 100px;
    padding-top: 35px;

    .qu_assign_score_content {
      width: 80px;
    }
  }
}
</style>
