<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane label="套餐管理" name="packages">
        <div style="margin-bottom: 16px">
          <el-button type="primary" icon="el-icon-plus" @click="handleAddPackage">添加套餐</el-button>
        </div>

        <el-table v-loading="loadingPackages" :data="packages" stripe border>
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="name" label="套餐名称" width="140" />
          <el-table-column prop="tokens" label="Token数量" width="120" />
          <el-table-column label="价格" width="150">
            <template slot-scope="{ row }">
              <div v-if="row.discount && row.discount < 1">
                <span style="text-decoration: line-through; color: #999; font-size: 12px">¥{{ row.originalPrice || row.price }}</span>
                <br>
                <span style="color: #f56c6c; font-weight: 600">¥{{ row.price }}</span>
                <el-tag type="danger" size="mini" style="margin-left: 4px">{{ formatDiscount(row.discount) }}折</el-tag>
              </div>
              <span v-else style="color: #e6a23c; font-weight: 600">¥{{ row.price }}</span>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="100">
            <template slot-scope="{ row }">{{ row.unitPrice || '-' }} Tokens/元</template>
          </el-table-column>
          <el-table-column prop="description" label="描述" show-overflow-tooltip />
          <el-table-column label="状态" width="80">
            <template slot-scope="{ row }">
              <el-tag :type="row.isActive !== false ? 'success' : 'info'" size="small">
                {{ row.isActive !== false ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template slot-scope="{ row }">
              <el-button size="mini" type="primary" @click="handleEditPackage(row)">编辑</el-button>
              <el-button size="mini" type="danger" @click="handleDeletePackage(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-if="pkgTotal > 0"
          :current-page="pkgPageNum"
          :page-size="pkgPageSize"
          :total="pkgTotal"
          layout="total, prev, pager, next"
          style="margin-top: 16px; text-align: right"
          @current-change="val => { pkgPageNum = val; loadPackages() }"
        />
      </el-tab-pane>

      <el-tab-pane label="用户Token管理" name="users">
        <div style="margin-bottom: 16px; display: flex; gap: 12px; align-items: center">
          <el-input
            v-model="userSearchName"
            placeholder="搜索用户姓名"
            style="width: 200px"
            clearable
            @clear="handleSearchUser"
            @keyup.enter.native="handleSearchUser"
          />
          <el-button type="primary" icon="el-icon-search" @click="handleSearchUser">搜索</el-button>
        </div>

        <el-table v-loading="loadingUsers" :data="userTokens" stripe border>
          <el-table-column prop="userId" label="用户ID" width="80" />
          <el-table-column prop="userName" label="用户名" width="120" />
          <el-table-column prop="realName" label="姓名" width="100" />
          <el-table-column prop="balance" label="当前余额" width="100">
            <template slot-scope="{ row }">
              <span style="font-weight: 600; color: #409eff">{{ row.balance }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="totalPurchased" label="累计充值" width="100" />
          <el-table-column prop="totalConsumed" label="累计消耗" width="100" />
          <el-table-column prop="updateTime" label="更新时间" width="170" />
          <el-table-column label="操作" width="120" fixed="right">
            <template slot-scope="{ row }">
              <el-button size="mini" type="warning" @click="handleAdjustToken(row)">调整余额</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-if="userTotal > 0"
          :current-page="userPageNum"
          :page-size="userPageSize"
          :total="userTotal"
          layout="total, prev, pager, next"
          style="margin-top: 16px; text-align: right"
          @current-change="val => { userPageNum = val; loadUserTokens() }"
        />
      </el-tab-pane>

      <el-tab-pane label="消耗配置" name="consume">
        <div style="margin-bottom: 16px; display: flex; justify-content: space-between; align-items: center">
          <span style="font-size: 13px; color: #909399">数值越大消耗越多，设为0则不消耗</span>
          <el-button type="primary" icon="el-icon-refresh" size="small" @click="initConsumeConfigs">初始化默认配置</el-button>
        </div>

        <el-table v-loading="loadingConsume" :data="consumeConfigs" stripe border>
          <el-table-column label="操作类型" width="200">
            <template slot-scope="{ row }">
              <div style="display: flex; align-items: center; gap: 8px">
                <i :class="getConsumeIcon(row.configKey)" style="font-size: 18px; color: #409eff" />
                <span>{{ row.configName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="说明" show-overflow-tooltip />
          <el-table-column label="消耗Tokens" width="150">
            <template slot-scope="{ row }">
              <el-input-number v-model="row.tokenCost" :min="0" :max="100" size="small" :disabled="!row.isEnabled" @change="updateConsumeConfig(row)" />
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template slot-scope="{ row }">
              <el-switch v-model="row.isEnabled" active-color="#13ce66" inactive-color="#ff4949" @change="toggleConsumeConfig(row)" />
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog :title="packageDialogTitle" :visible.sync="packageDialogVisible" width="550px">
      <el-form ref="packageForm" :model="packageForm" :rules="packageRules" label-width="100px">
        <el-form-item label="套餐名称" prop="name">
          <el-input v-model="packageForm.name" placeholder="如：体验包" />
        </el-form-item>
        <el-form-item label="Token数量" prop="tokens">
          <el-input-number v-model="packageForm.tokens" :min="1" :max="999999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="原价(元)" prop="originalPrice">
          <el-input-number v-model="packageForm.originalPrice" :min="0.01" :precision="2" :step="1" style="width: 100%" @change="calculateDiscountedPrice" />
        </el-form-item>
        <el-form-item label="折扣">
          <div style="display: flex; align-items: center; gap: 12px">
            <el-slider v-model="discountPercent" :min="10" :max="100" :step="5" style="flex: 1" @change="handleDiscountChange" />
            <el-input-number v-model="discountPercent" :min="10" :max="100" :step="5" style="width: 80px" @change="handleDiscountChange" />
            <span style="color: #f56c6c; font-weight: 600; min-width: 50px">{{ discountPercent }}%</span>
          </div>
        </el-form-item>
        <el-form-item label="现价(元)">
          <div style="display: flex; align-items: center; gap: 8px">
            <span style="font-size: 20px; font-weight: 600; color: #f56c6c">¥{{ packageForm.price }}</span>
            <el-tag v-if="discountPercent < 100" type="danger" size="small">省 ¥{{ (packageForm.originalPrice - packageForm.price).toFixed(2) }}</el-tag>
          </div>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="packageForm.description" type="textarea" :rows="2" placeholder="套餐描述" />
        </el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="packageForm.isActive" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="packageForm.sortOrder" :min="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="packageDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingPackage" @click="handleSavePackage">确定</el-button>
      </span>
    </el-dialog>

    <el-dialog title="调整用户Token" :visible.sync="adjustDialogVisible" width="450px">
      <el-form ref="adjustForm" :model="adjustForm" :rules="adjustRules" label-width="100px">
        <el-form-item label="用户">
          <span>{{ adjustForm.realName }} (ID: {{ adjustForm.userId }})</span>
        </el-form-item>
        <el-form-item label="当前余额">
          <span style="font-weight: 600; color: #409eff">{{ adjustForm.currentBalance }} Tokens</span>
        </el-form-item>
        <el-form-item label="调整数量" prop="amount">
          <el-input-number v-model="adjustForm.amount" :step="10" style="width: 100%" />
          <div style="font-size: 12px; color: #909399; margin-top: 4px">正数为增加，负数为扣减</div>
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="adjustForm.reason" type="textarea" :rows="2" placeholder="调整原因" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="adjustDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="adjusting" @click="handleSaveAdjust">确定调整</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  adminGetPackages,
  adminAddPackage,
  adminUpdatePackage,
  adminDeletePackage,
  adminGetUserTokens,
  adminAdjustToken
} from '@/api/token'
import { getTokenConsumeConfigs, updateTokenConsumeConfig, initTokenConsumeConfigs, toggleTokenConsumeConfig } from '@/api/token-consume-config'

export default {
  name: 'TokenAdmin',
  data() {
    return {
      activeTab: 'packages',
      packages: [],
      loadingPackages: false,
      pkgPageNum: 1,
      pkgPageSize: 10,
      pkgTotal: 0,
      packageDialogVisible: false,
      packageDialogTitle: '添加套餐',
      editingPackageId: null,
      savingPackage: false,
      discountPercent: 100,
      packageForm: {
        name: '',
        tokens: 100,
        price: 9.9,
        originalPrice: 9.9,
        discount: 1.0,
        description: '',
        isActive: true,
        sortOrder: 0
      },
      packageRules: {
        name: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
        tokens: [{ required: true, message: '请输入Token数量', trigger: 'change' }],
        originalPrice: [{ required: true, message: '请输入原价', trigger: 'change' }]
      },
      userTokens: [],
      loadingUsers: false,
      userPageNum: 1,
      userPageSize: 10,
      userTotal: 0,
      userSearchName: '',
      adjustDialogVisible: false,
      adjusting: false,
      adjustForm: {
        userId: null,
        realName: '',
        currentBalance: 0,
        amount: 0,
        reason: ''
      },
      adjustRules: {
        amount: [{ required: true, message: '请输入调整数量', trigger: 'change' }]
      },
      consumeConfigs: [],
      loadingConsume: false
    }
  },
  mounted() {
    this.loadPackages()
  },
  methods: {
    formatDiscount(discount) {
      if (!discount) return '10'
      return (discount * 10).toFixed(1).replace(/\.0$/, '')
    },
    calculateDiscountedPrice() {
      if (this.packageForm.originalPrice && this.discountPercent) {
        this.packageForm.price = parseFloat((this.packageForm.originalPrice * this.discountPercent / 100).toFixed(2))
        this.packageForm.discount = parseFloat((this.discountPercent / 100).toFixed(2))
      }
    },
    handleDiscountChange() {
      this.calculateDiscountedPrice()
    },
    async loadPackages() {
      this.loadingPackages = true
      try {
        const res = await adminGetPackages({ pageNum: this.pkgPageNum, pageSize: this.pkgPageSize })
        if (res.code && res.data) {
          this.packages = res.data.records
          this.pkgTotal = res.data.total
        }
      } catch (e) {
        console.error('加载套餐失败:', e)
      } finally {
        this.loadingPackages = false
      }
    },
    async loadUserTokens() {
      this.loadingUsers = true
      try {
        const res = await adminGetUserTokens({
          pageNum: this.userPageNum,
          pageSize: this.userPageSize,
          realName: this.userSearchName || undefined
        })
        if (res.code && res.data) {
          this.userTokens = res.data.records
          this.userTotal = res.data.total
        }
      } catch (e) {
        console.error('加载用户Token失败:', e)
      } finally {
        this.loadingUsers = false
      }
    },
    handleAddPackage() {
      this.editingPackageId = null
      this.packageDialogTitle = '添加套餐'
      this.discountPercent = 100
      this.packageForm = {
        name: '',
        tokens: 100,
        price: 9.9,
        originalPrice: 9.9,
        discount: 1.0,
        description: '',
        isActive: true,
        sortOrder: 0
      }
      this.packageDialogVisible = true
    },
    handleEditPackage(row) {
      this.editingPackageId = row.id
      this.packageDialogTitle = '编辑套餐'
      this.discountPercent = row.discount ? Math.round(row.discount * 100) : 100
      this.packageForm = {
        name: row.name,
        tokens: row.tokens,
        price: row.price,
        originalPrice: row.originalPrice || row.price,
        discount: row.discount || 1.0,
        description: row.description || '',
        isActive: row.isActive !== false,
        sortOrder: row.sortOrder || 0
      }
      this.packageDialogVisible = true
    },
    async handleSavePackage() {
      try {
        await this.$refs.packageForm.validate()
      } catch {
        return
      }

      this.savingPackage = true
      try {
        let res
        if (this.editingPackageId) {
          res = await adminUpdatePackage(this.editingPackageId, this.packageForm)
        } else {
          res = await adminAddPackage(this.packageForm)
        }
        if (res.code) {
          this.$message.success(res.msg || '保存成功')
          this.packageDialogVisible = false
          this.loadPackages()
        }
      } catch (e) {
        console.error('保存套餐失败:', e)
      } finally {
        this.savingPackage = false
      }
    },
    async handleDeletePackage(row) {
      try {
        await this.$confirm(`确定删除套餐「${row.name}」？`, '确认删除', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
      } catch {
        return
      }

      try {
        const res = await adminDeletePackage(row.id)
        if (res.code) {
          this.$message.success('删除成功')
          this.loadPackages()
        }
      } catch (e) {
        console.error('删除套餐失败:', e)
      }
    },
    handleSearchUser() {
      this.userPageNum = 1
      this.loadUserTokens()
    },
    handleAdjustToken(row) {
      this.adjustForm = {
        userId: row.userId,
        realName: row.realName || row.userName,
        currentBalance: row.balance,
        amount: 0,
        reason: ''
      }
      this.adjustDialogVisible = true
    },
    async handleSaveAdjust() {
      try {
        await this.$refs.adjustForm.validate()
      } catch {
        return
      }

      if (this.adjustForm.amount === 0) {
        this.$message.warning('调整数量不能为0')
        return
      }

      this.adjusting = true
      try {
        const res = await adminAdjustToken({
          userId: this.adjustForm.userId,
          amount: this.adjustForm.amount,
          reason: this.adjustForm.reason
        })
        if (res.code) {
          this.$message.success('调整成功')
          this.adjustDialogVisible = false
          this.loadUserTokens()
        }
      } catch (e) {
        console.error('调整Token失败:', e)
      } finally {
        this.adjusting = false
      }
    },
    async loadConsumeConfigs() {
      this.loadingConsume = true
      try {
        const res = await getTokenConsumeConfigs()
        if (res.code) {
          this.consumeConfigs = res.data || []
        }
      } catch (e) {
        console.error('加载消耗配置失败:', e)
      } finally {
        this.loadingConsume = false
      }
    },
    async updateConsumeConfig(config) {
      try {
        const res = await updateTokenConsumeConfig({ id: config.id, tokenCost: config.tokenCost })
        if (res.code) {
          this.$message.success('更新成功')
        } else {
          this.$message.error(res.msg || '更新失败')
          this.loadConsumeConfigs()
        }
      } catch (e) {
        this.$message.error('更新失败')
        this.loadConsumeConfigs()
      }
    },
    async toggleConsumeConfig(config) {
      try {
        const res = await toggleTokenConsumeConfig(config.id)
        if (res.code) {
          this.$message.success(res.data || '操作成功')
        } else {
          this.$message.error(res.msg || '操作失败')
          this.loadConsumeConfigs()
        }
      } catch (e) {
        this.$message.error('操作失败')
        this.loadConsumeConfigs()
      }
    },
    async initConsumeConfigs() {
      try {
        const res = await initTokenConsumeConfigs()
        if (res.code) {
          this.$message.success('初始化成功')
          this.loadConsumeConfigs()
        } else {
          this.$message.error(res.msg || '初始化失败')
        }
      } catch (e) {
        this.$message.error('初始化失败')
      }
    },
    getConsumeIcon(key) {
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
    }
  },
  watch: {
    activeTab(val) {
      if (val === 'users' && this.userTokens.length === 0) {
        this.loadUserTokens()
      }
      if (val === 'consume' && this.consumeConfigs.length === 0) {
        this.loadConsumeConfigs()
      }
    }
  }
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}
</style>
