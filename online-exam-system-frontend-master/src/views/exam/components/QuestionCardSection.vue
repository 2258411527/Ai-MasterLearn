<template>
  <div>
    <h4 style="margin-bottom: 10px; font-weight: bold">{{ title }}</h4>
    <div style="display: flex; flex-wrap: wrap; gap: 8px; padding: 5px">
      <div
        v-for="item in questions"
        :key="item.questionId"
        @click="selectQuestion(item)"
        class="card-item"
        :style="{
          backgroundColor: item.checkout ? '#f0f9ff' : '#fff',
          border: currentItem && item.questionId === currentItem.questionId ? '2px solid #409eff' : '1px solid #dcdfe6',
          borderRadius: '4px',
          padding: '6px 12px',
          minWidth: '40px',
          textAlign: 'center',
          cursor: 'pointer'
        }">
        <span
          :style="{
            color: item.checkout ? '#67c23a' : '#606266',
            fontWeight: '500'
          }">
          {{ item.sort + 1 }}
        </span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'QuestionCardSection',
  props: {
    title: {
      type: String,
      required: true
    },
    questions: {
      type: Array,
      default: () => []
    },
    currentItem: {
      type: Object,
      default: () => {}
    }
  },
  methods: {
    selectQuestion(question) {
      this.$emit('select-question', question)
    }
  }
}
</script>

<style scoped>
.card-item {
  cursor: pointer;
  transition: all 0.2s;
}

.card-item:hover {
  background-color: #ecf5ff;
  border-color: #409eff !important;
}
</style>
