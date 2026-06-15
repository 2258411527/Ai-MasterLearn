<template>
  <div class="app-container">
    <div class="token-page">
      <div class="balance-card">
        <div class="balance-info">
          <div class="balance-icon">
            <i class="el-icon-coin" />
          </div>
          <div class="balance-text">
            <div class="balance-label">当前余额</div>
            <div class="balance-value">{{ balance }} <span class="balance-unit">Tokens</span></div>
          </div>
        </div>
        <div class="balance-stats">
          <div class="stat-item">
            <span class="stat-label">累计充值</span>
            <span class="stat-value">{{ totalPurchased }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">累计消耗</span>
            <span class="stat-value">{{ totalConsumed }}</span>
          </div>
        </div>
      </div>

      <div class="section-title">
        <i class="el-icon-shopping-cart" />
        选择充值套餐
      </div>

      <div v-loading="loadingPackages" class="packages-grid">
        <div
          v-for="pkg in packages"
          :key="pkg.id"
          :class="['package-card', { 'package-selected': selectedPackage && selectedPackage.id === pkg.id, 'package-discount': pkg.discount && pkg.discount < 1 }]"
          @click="selectPackage(pkg)"
        >
          <div v-if="pkg.discount && pkg.discount < 1" class="discount-badge">
            {{ formatDiscount(pkg.discount) }}折
          </div>
          <div class="package-name">{{ pkg.name }}</div>
          <div class="package-tokens">{{ pkg.tokens }} <span>Tokens</span></div>
          <div v-if="pkg.discount && pkg.discount < 1" class="package-original-price">
            <span class="original-label">原价</span>
            <span class="original-value">¥{{ pkg.originalPrice || pkg.price }}</span>
          </div>
          <div class="package-price" :class="{ 'price-discount': pkg.discount && pkg.discount < 1 }">
            <span class="price-symbol">¥</span>{{ pkg.price }}
          </div>
          <div v-if="pkg.discount && pkg.discount < 1" class="package-save">
            立省 ¥{{ ((pkg.originalPrice || pkg.price) - pkg.price).toFixed(2) }}
          </div>
          <div v-if="pkg.unitPrice" class="package-unit">
            约 {{ pkg.unitPrice }} Tokens/元
          </div>
          <div v-if="pkg.description" class="package-desc">{{ pkg.description }}</div>
          <div v-if="selectedPackage && selectedPackage.id === pkg.id" class="package-check">
            <i class="el-icon-check" />
          </div>
        </div>
      </div>

      <div class="purchase-action">
        <el-button
          type="primary"
          size="large"
          :disabled="!selectedPackage"
          :loading="purchasing"
          class="purchase-btn"
          @click="handlePurchase"
        >
          {{ selectedPackage ? `支付宝支付 - ¥${selectedPackage.price}` : '请选择套餐' }}
        </el-button>
      </div>

      <div class="section-title">
        <i class="el-icon-document" />
        交易记录
      </div>

      <el-table
        v-loading="loadingTransactions"
        :data="transactions"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="typeName" label="类型" width="100" />
        <el-table-column label="数量" width="120">
          <template slot-scope="{ row }">
            <span :class="row.type === 2 ? 'amount-consume' : 'amount-add'">
              {{ row.type === 2 ? '-' : '+' }}{{ row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="balanceAfter" label="余额" width="100" />
        <el-table-column prop="aiServiceType" label="服务" width="120" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="createTime" label="时间" width="170" />
      </el-table>

      <el-pagination
        v-if="total > 0"
        :current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        style="margin-top: 20px; text-align: right"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script>
import { getTokenBalance, getTokenPackages, getTokenTransactions, createAlipayPayment } from '@/api/token'

export default {
  name: 'TokenCenter',
  data() {
    return {
      balance: 0,
      totalPurchased: 0,
      totalConsumed: 0,
      packages: [],
      selectedPackage: null,
      loadingPackages: false,
      purchasing: false,
      transactions: [],
      loadingTransactions: false,
      pageNum: 1,
      pageSize: 10,
      total: 0
    }
  },
  mounted() {
    this.loadBalance()
    this.loadPackages()
    this.loadTransactions()
  },
  methods: {
    formatDiscount(discount) {
      if (!discount) return '10'
      return (discount * 10).toFixed(1).replace(/\.0$/, '')
    },
    async loadBalance() {
      try {
        const res = await getTokenBalance()
        if (res.code && res.data) {
          this.balance = res.data.balance
          this.totalPurchased = res.data.totalPurchased
          this.totalConsumed = res.data.totalConsumed
        }
      } catch (e) {
        console.error('加载余额失败:', e)
      }
    },
    async loadPackages() {
      this.loadingPackages = true
      try {
        const res = await getTokenPackages()
        if (res.code && res.data) {
          this.packages = res.data
        }
      } catch (e) {
        console.error('加载套餐失败:', e)
      } finally {
        this.loadingPackages = false
      }
    },
    async loadTransactions() {
      this.loadingTransactions = true
      try {
        const res = await getTokenTransactions({ pageNum: this.pageNum, pageSize: this.pageSize })
        if (res.code && res.data) {
          this.transactions = res.data.records
          this.total = res.data.total
        }
      } catch (e) {
        console.error('加载交易记录失败:', e)
      } finally {
        this.loadingTransactions = false
      }
    },
    selectPackage(pkg) {
      this.selectedPackage = pkg
    },
    async handlePurchase() {
      if (!this.selectedPackage) return
      try {
        await this.$confirm(
          `确定购买「${this.selectedPackage.name}」套餐？将通过支付宝支付 ¥${this.selectedPackage.price}，支付成功后获得 ${this.selectedPackage.tokens} Tokens`,
          '确认支付',
          { confirmButtonText: '去支付', cancelButtonText: '取消', type: 'info' }
        )
      } catch {
        return
      }

      this.purchasing = true
      try {
        const res = await createAlipayPayment(this.selectedPackage.id)
        if (res.code && res.data) {
          if (res.data.startsWith('TEST_MODE_SUCCESS:')) {
            this.$message.success('测试模式：支付成功！')
            this.selectedPackage = null
            this.loadBalance()
            this.pageNum = 1
            this.loadTransactions()
          } else {
            const div = document.createElement('div')
            div.innerHTML = res.data
            document.body.appendChild(div)
            const form = div.querySelector('form')
            if (form) {
              form.submit()
            } else {
              this.$message.error('支付表单生成失败')
            }
          }
        }
      } catch (e) {
        console.error('创建支付订单失败:', e)
        this.$message.error('创建支付订单失败，请重试')
      } finally {
        this.purchasing = false
      }
    },
    handlePageChange(page) {
      this.pageNum = page
      this.loadTransactions()
    }
  }
}
</script>

<style scoped>
.app-container {
  padding: 20px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  min-height: calc(100vh - 84px);
}

.token-page {
  max-width: 1000px;
  margin: 0 auto;
}

.balance-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  padding: 32px;
  color: white;
  margin-bottom: 32px;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);
}

.balance-info {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
}

.balance-icon {
  width: 64px;
  height: 64px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
}

.balance-label {
  font-size: 14px;
  opacity: 0.85;
  margin-bottom: 4px;
}

.balance-value {
  font-size: 42px;
  font-weight: 700;
  letter-spacing: -1px;
}

.balance-unit {
  font-size: 18px;
  font-weight: 400;
  opacity: 0.8;
}

.balance-stats {
  display: flex;
  gap: 40px;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 12px;
  opacity: 0.7;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #334155;
  margin: 28px 0 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.packages-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.package-card {
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 16px;
  padding: 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.package-card:hover {
  border-color: #409eff;
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.15);
}

.package-selected {
  border-color: #409eff;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.05), rgba(64, 158, 255, 0.1));
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.2);
}

.package-discount {
  border-color: #f56c6c;
  background: linear-gradient(135deg, #fff5f5, #fff0f0);
}

.package-discount:hover {
  border-color: #f56c6c;
  box-shadow: 0 8px 24px rgba(245, 108, 108, 0.2);
}

.discount-badge {
  position: absolute;
  top: 0;
  right: 0;
  background: linear-gradient(135deg, #f56c6c, #fa5555);
  color: white;
  padding: 4px 12px;
  font-size: 12px;
  font-weight: 600;
  border-bottom-left-radius: 12px;
}

.package-name {
  font-size: 16px;
  font-weight: 600;
  color: #334155;
  margin-bottom: 12px;
}

.package-tokens {
  font-size: 32px;
  font-weight: 700;
  color: #409eff;
  margin-bottom: 8px;
}

.package-tokens span {
  font-size: 14px;
  font-weight: 400;
  color: #64748b;
}

.package-original-price {
  margin-bottom: 4px;
}

.original-label {
  font-size: 11px;
  color: #999;
}

.original-value {
  text-decoration: line-through;
  color: #999;
  font-size: 14px;
  margin-left: 4px;
}

.package-price {
  font-size: 28px;
  font-weight: 700;
  color: #e6a23c;
  margin-bottom: 8px;
}

.price-discount {
  color: #f56c6c;
}

.price-symbol {
  font-size: 16px;
  font-weight: 400;
}

.package-save {
  font-size: 12px;
  color: #f56c6c;
  font-weight: 600;
  margin-bottom: 8px;
}

.package-unit {
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 8px;
}

.package-desc {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
}

.package-check {
  position: absolute;
  top: 0;
  right: 0;
  width: 32px;
  height: 32px;
  background: #409eff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
  border-bottom-left-radius: 12px;
}

.purchase-action {
  text-align: center;
  margin-bottom: 16px;
}

.purchase-btn {
  width: 300px;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #1677ff, #4096ff);
  border: none;
}

.amount-consume {
  color: #f56c6c;
  font-weight: 600;
}

.amount-add {
  color: #67c23a;
  font-weight: 600;
}
</style>
