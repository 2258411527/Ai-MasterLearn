<template>
  <el-dialog
    title="交卷确认"
    :visible.sync="dialogVisible"
    width="600px"
    @close="handleClose">
    <div style="padding: 10px 0">
      <el-alert
        title="答题情况"
        type="info"
        :closable="false"
        style="margin-bottom: 20px">
        <template v-slot:default>
          <p>总题数: {{ totalCount }}</p>
          <p>已答: {{ answeredCount }} 题</p>
          <p>未答: {{ unansweredCount }} 题</p>
        </template>
      </el-alert>
      
      <el-collapse v-if="recordData && recordData.length > 0">
        <el-collapse-item title="查看答题详情">
          <div>
            <div v-for="(item, index) in recordData" :key="item.id || index" style="margin-bottom: 10px">
              <span :style="{ 
                color: isAnswered(item) ? '#67c23a' : '#f56c6c', 
                marginRight: '10px' 
              }">
                [{{ isAnswered(item) ? '已答' : '未答' }}]
              </span>
              <span>{{ index + 1 }}. {{ item.title || item.content || '题目内容' }}</span>
              <div v-if="isAnswered(item)" style="margin-left: 60px; color: #409eff; font-size: 13px;">
                我的答案：{{ formatAnswer(item) }}
              </div>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>
    
    <span slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleConfirm">确认交卷</el-button>
    </span>
  </el-dialog>
</template>

<script>
export default {
  name: 'ExamSummaryDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    recordData: {
      type: Array,
      default: () => []
    }
  },
  computed: {
    dialogVisible: {
      get() {
        return this.visible
      },
      set(val) {
        this.$emit('update:visible', val)
      }
    },
    totalCount() {
      return this.recordData.length
    },
    answeredCount() {
      return this.recordData.filter(item => this.isAnswered(item)).length
    },
    unansweredCount() {
      return this.recordData.filter(item => !this.isAnswered(item)).length
    }
  },
  methods: {
    isAnswered(item) {
      return item.myOption !== null && item.myOption !== undefined && item.myOption !== ''
    },
    formatAnswer(item) {
      if (!this.isAnswered(item)) return '未作答'
      if (item.quType === 4) return item.myOption
      const numberToCharMap = { 0: 'A', 1: 'B', 2: 'C', 3: 'D', 4: 'E', 5: 'F' }
      return item.myOption.split(',').map(s => numberToCharMap[parseInt(s.trim())] || s).join(',')
    },
    handleClose() {
      this.$emit('close')
    },
    handleConfirm() {
      this.$emit('confirm')
    }
  }
}
</script>
