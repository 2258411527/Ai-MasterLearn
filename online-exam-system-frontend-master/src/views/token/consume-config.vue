<template>
  <div class="token-consume-config-container">
    <div class="bg-decoration">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
    </div>

    <div class="content-wrapper">
      <div class="page-header glass-card">
        <div class="header-left">
          <h1 class="page-title">
            <i class="el-icon-coin"></i>
            Token 消耗配置
          </h1>
          <p class="page-subtitle">自定义各类操作的Token消耗规则，灵活控制成本</p>
        </div>
        <div class="header-right">
          <el-button type="primary" icon="el-icon-refresh" @click="initConfigs">初始化默认配置</el-button>
        </div>
      </div>

      <div class="config-cards glass-card">
        <div class="cards-header">
          <span class="header-title">消耗规则设置</span>
          <span class="header-tip">数值越大消耗越多，设为0则不消耗</span>
        </div>

        <div class="config-grid">
          <div v-for="config in configs" :key="config.id" class="config-item" :class="{ disabled: !config.isEnabled }">
            <div class="config-header">
              <div class="config-icon" :style="{ background: getIconColor(config.configKey) }">
                <i :class="getIcon(config.configKey)"></i>
              </div>
              <div class="config-info">
                <span class="config-name">{{ config.configName }}</span>
                <span class="config-desc">{{ config.description }}</span>
              </div>
              <el-switch v-model="config.isEnabled" active-color="#13ce66" inactive-color="#ff4949" @change="toggleConfig(config)" />
            </div>
            <div class="config-body">
              <div class="cost-display">
                <span class="cost-label">消耗</span>
                <el-input-number v-model="config.tokenCost" :min="0" :max="100" size="medium" :disabled="!config.isEnabled" @change="updateConfig(config)" />
                <span class="cost-unit">Tokens</span>
              </div>
            </div>
          </div>
        </div>

        <div v-if="configs.length === 0" class="empty-state">
          <i class="el-icon-setting"></i>
          <p>暂无配置数据，请点击"初始化默认配置"</p>
        </div>
      </div>

      <div class="usage-tips glass-card">
        <div class="tips-header">
          <i class="el-icon-info"></i>
          <span>使用说明</span>
        </div>
        <div class="tips-content">
          <div class="tip-item">
            <strong>AI对话消耗：</strong>
            <span>基础消耗 + 输入字数/100 × 每100字消耗</span>
          </div>
          <div class="tip-item">
            <strong>AI阅卷消耗：</strong>
            <span>每题消耗 × 题目数量</span>
          </div>
          <div class="tip-item">
            <strong>学习分析：</strong>
            <span>每次生成分析报告固定消耗</span>
          </div>
          <div class="tip-item">
            <strong>禁用规则：</strong>
            <span>禁用后该操作将不消耗Token</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getTokenConsumeConfigs, updateTokenConsumeConfig, initTokenConsumeConfigs, toggleTokenConsumeConfig } from '@/api/token-consume-config'

export default {
  name: 'TokenConsumeConfig',
  data() {
    return {
      configs: [],
      loading: false
    }
  },
  created() {
    this.loadConfigs()
  },
  methods: {
    async loadConfigs() {
      this.loading = true
      try {
        const res = await getTokenConsumeConfigs()
        if (res.code) {
          this.configs = res.data || []
        }
      } catch (e) {
        this.$message.error('加载配置失败')
      } finally {
        this.loading = false
      }
    },
    async updateConfig(config) {
      try {
        const res = await updateTokenConsumeConfig({
          id: config.id,
          tokenCost: config.tokenCost
        })
        if (res.code) {
          this.$message.success('更新成功')
        } else {
          this.$message.error(res.msg || '更新失败')
          this.loadConfigs()
        }
      } catch (e) {
        this.$message.error('更新失败')
        this.loadConfigs()
      }
    },
    async toggleConfig(config) {
      try {
        const res = await toggleTokenConsumeConfig(config.id)
        if (res.code) {
          this.$message.success(res.data || '操作成功')
        } else {
          this.$message.error(res.msg || '操作失败')
          this.loadConfigs()
        }
      } catch (e) {
        this.$message.error('操作失败')
        this.loadConfigs()
      }
    },
    async initConfigs() {
      try {
        const res = await initTokenConsumeConfigs()
        if (res.code) {
          this.$message.success('初始化成功')
          this.loadConfigs()
        } else {
          this.$message.error(res.msg || '初始化失败')
        }
      } catch (e) {
        this.$message.error('初始化失败')
      }
    },
    getIcon(key) {
      const icons = {
        'ai_chat': 'el-icon-chat-dot-round',
        'ai_chat_per_100_chars': 'el-icon-document',
        'ai_grading': 'el-icon-edit-outline',
        'ai_analysis': 'el-icon-data-analysis',
        'ai_recommend': 'el-icon-star-on',
        'question_generate': 'el-icon-plus',
        'knowledge_qa': 'el-icon-question'
      }
      return icons[key] || 'el-icon-setting'
    },
    getIconColor(key) {
      const colors = {
        'ai_chat': 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        'ai_chat_per_100_chars': 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
        'ai_grading': 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
        'ai_analysis': 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
        'ai_recommend': 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
        'question_generate': 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
        'knowledge_qa': 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)'
      }
      return colors[key] || 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
    }
  }
}
</script>

<style lang="scss" scoped>
.token-consume-config-container {
  min-height: 100vh;
  background: var(--bg-primary);
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

  .gradient-orb {
    position: absolute;
    border-radius: 50%;
    filter: blur(80px);

    &.orb-1 {
      width: 400px;
      height: 400px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      top: -100px;
      right: -100px;
      opacity: 0.3;
    }

    &.orb-2 {
      width: 300px;
      height: 300px;
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      bottom: -50px;
      left: -50px;
      opacity: 0.3;
    }
  }
}

.content-wrapper {
  position: relative;
  z-index: 1;
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.glass-card {
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  border-radius: 16px;
  margin-bottom: 20px;
}

.page-header {
  padding: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .header-left {
    .page-title {
      margin: 0 0 8px 0;
      font-size: 24px;
      font-weight: 700;
      color: var(--text-primary);
      display: flex;
      align-items: center;
      gap: 12px;

      i {
        color: var(--accent-light);
      }
    }

    .page-subtitle {
      margin: 0;
      font-size: 14px;
      color: var(--text-secondary);
    }
  }
}

.config-cards {
  padding: 24px;

  .cards-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 1px solid var(--glass-border);

    .header-title {
      font-size: 16px;
      font-weight: 600;
      color: var(--text-primary);
    }

    .header-tip {
      font-size: 13px;
      color: var(--text-secondary);
    }
  }
}

.config-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 16px;
}

.config-item {
  padding: 20px;
  background: var(--bg-hover);
  border-radius: 12px;
  border: 1px solid var(--glass-border);
  transition: all 0.3s ease;

  &:hover {
    border-color: var(--accent);
  }

  &.disabled {
    opacity: 0.6;
  }

  .config-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;

    .config-icon {
      width: 40px;
      height: 40px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;

      i {
        font-size: 20px;
        color: white;
      }
    }

    .config-info {
      flex: 1;

      .config-name {
        display: block;
        font-size: 15px;
        font-weight: 600;
        color: var(--text-primary);
      }

      .config-desc {
        display: block;
        font-size: 12px;
        color: var(--text-secondary);
        margin-top: 2px;
      }
    }
  }

  .config-body {
    .cost-display {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px 16px;
      background: var(--bg-input);
      border-radius: 8px;

      .cost-label {
        font-size: 14px;
        color: var(--text-secondary);
      }

      .cost-unit {
        font-size: 14px;
        color: var(--text-secondary);
        font-weight: 500;
      }
    }
  }
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-secondary);

  i {
    font-size: 48px;
    margin-bottom: 16px;
    display: block;
  }

  p {
    margin: 0;
    font-size: 14px;
  }
}

.usage-tips {
  padding: 24px;

  .tips-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 16px;
    font-size: 15px;
    font-weight: 600;
    color: var(--text-primary);

    i {
      color: var(--accent);
    }
  }

  .tips-content {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 12px;

    .tip-item {
      padding: 12px 16px;
      background: var(--bg-hover);
      border-radius: 8px;
      font-size: 13px;
      color: var(--text-secondary);

      strong {
        color: var(--text-primary);
      }
    }
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .config-grid {
    grid-template-columns: 1fr;
  }
}
</style>
