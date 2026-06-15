<template>
  <div class="user-management-container">
    <!-- 页面标题和操作栏 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">{{ role === 'teacher' ? '管理班级成员' : '用户管理' }}</h2>
        <p class="page-subtitle">{{ role === 'teacher' ? '管理所加入班级的学生成员' : '管理系统用户信息与审核状态' }}</p>
      </div>
      <div class="header-actions">
        <el-button v-if="role === 'admin'" type="primary" icon="el-icon-plus" @click="addUserDiologVisible = true">
          新增用户
        </el-button>
        <el-button v-if="role === 'admin'" type="success" icon="el-icon-upload2" @click="fileDialogVisible = true">
          批量导入
        </el-button>
        <el-button
          v-if="role === 'admin'"
          type="danger"
          icon="el-icon-delete"
          :disabled="multipleSelection.length === 0"
          @click="batchDeleteUsers"
        >
          批量删除
        </el-button>
        <el-button v-if="role === 'admin'" type="warning" icon="el-icon-plus" @click="showAdminJoinGradeDialog">
          加入班级
        </el-button>
        <el-button
          v-if="role === 'teacher'"
          type="danger"
          icon="el-icon-remove-outline"
          :disabled="multipleSelection.length === 0 || multipleSelection.some(u => !u.gradeId)"
          @click="batchRemoveFromClass"
        >
          批量移出班级
        </el-button>
      </div>
    </div>

    <!-- 筛选卡片 -->
    <el-card class="filter-card" shadow="never">
      <div slot="header" class="filter-header">
        <span class="filter-title">筛选条件</span>
        <el-button type="text" class="filter-toggle" @click="toggleFilter">
          {{ filterExpanded ? '收起' : '展开' }}
          <i :class="filterExpanded ? 'el-icon-arrow-up' : 'el-icon-arrow-down'" />
        </el-button>
      </div>

      <el-collapse-transition>
        <div v-show="filterExpanded" class="filter-content">
          <el-form :model="searchForm" label-width="80px" class="filter-form">
            <el-row :gutter="20">
              <el-col :xs="24" :sm="12" :md="8" :lg="6">
                <el-form-item label="真实姓名">
                  <el-input
                    v-model="searchForm.searchRealName"
                    placeholder="请输入姓名"
                    prefix-icon="el-icon-user"
                    clearable
                    show-clear-btn
                  />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8" :lg="6">
                <el-form-item label="班级">
                  <ClassSelect
                    v-model="searchForm.searchClass"
                    :is-multiple="false"
                    placeholder="请选择班级"
                    clearable
                  />
                </el-form-item>
              </el-col>
              <el-col v-if="role === 'admin'" :xs="24" :sm="12" :md="8" :lg="6">
                <el-form-item label="审核状态">
                  <el-select
                    v-model="searchForm.auditStatus"
                    placeholder="请选择审核状态"
                    clearable
                    style="width: 100%"
                  >
                    <el-option label="全部" :value="null" />
                    <el-option label="待审核" :value="0" />
                    <el-option label="审核通过" :value="1" />
                    <el-option label="审核拒绝" :value="2" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8" :lg="6">
                <el-form-item label=" " class="filter-actions">
                  <el-button type="primary" icon="el-icon-search" @click="searchUser">
                    查询
                  </el-button>
                  <el-button icon="el-icon-refresh" @click="resetFilter">
                    重置
                  </el-button>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>
      </el-collapse-transition>
    </el-card>

    <!-- 数据统计卡片 -->
    <div v-if="role === 'admin'" class="stats-cards">
      <el-card class="stat-card" shadow="hover" :class="{ 'active': searchForm.auditStatus === 0 }" @click.native="filterByStatus(0)">
        <div class="stat-content">
          <div class="stat-icon pending">
            <i class="el-icon-time" />
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.pendingCount || 0 }}</div>
            <div class="stat-label">待审核用户</div>
          </div>
        </div>
      </el-card>
      <el-card class="stat-card" shadow="hover" :class="{ 'active': searchForm.auditStatus === 1 }" @click.native="filterByStatus(1)">
        <div class="stat-content">
          <div class="stat-icon approved">
            <i class="el-icon-success" />
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.approvedCount || 0 }}</div>
            <div class="stat-label">审核通过</div>
          </div>
        </div>
      </el-card>
      <el-card class="stat-card" shadow="hover" :class="{ 'active': searchForm.auditStatus === 2 }" @click.native="filterByStatus(2)">
        <div class="stat-content">
          <div class="stat-icon rejected">
            <i class="el-icon-error" />
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.rejectedCount || 0 }}</div>
            <div class="stat-label">审核拒绝</div>
          </div>
        </div>
      </el-card>
      <el-card class="stat-card" shadow="hover" :class="{ 'active': searchForm.auditStatus === null }" @click.native="filterByStatus(null)">
        <div class="stat-content">
          <div class="stat-icon total">
            <i class="el-icon-user-solid" />
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalCount || 0 }}</div>
            <div class="stat-label">总用户数</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 用户列表 -->
    <el-card class="table-card" shadow="never">
      <div class="table-header">
        <span class="table-title">用户列表</span>
        <div class="table-actions">
          <el-button
            type="text"
            icon="el-icon-refresh"
            :loading="listLoading"
            @click="getUserPage(pageNum, pageSize)"
          >
            刷新
          </el-button>
        </div>
      </div>

      <el-table
        ref="userTable"
        v-loading="listLoading"
        :data="data.records"
        fit
        highlight-current-row
        class="user-table"
        :header-cell-style="{ background: '#f8f9fa', color: '#606266', fontWeight: '600' }"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />

        <el-table-column label="序号" width="80" align="center">
          <template slot-scope="{ $index }">
            <span class="serial-number">{{ $index + 1 + (pageNum - 1) * pageSize }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="userName" label="用户名" min-width="120" show-overflow-tooltip>
          <template slot-scope="{ row }">
            <div class="user-info">
              <el-avatar :size="32" :src="row.avatar" class="user-avatar">
                {{ row.realName ? row.realName.charAt(0) : 'U' }}
              </el-avatar>
              <span class="user-name">{{ row.userName }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="realName" label="真实姓名" min-width="100" show-overflow-tooltip />

        <el-table-column label="用户类型" width="180" align="center">
          <template slot-scope="{ row }">
            <div class="user-type-display">
              <el-tag :type="getUserTypeTagType(row)" effect="dark" size="medium">
                <i :class="getUserTypeIcon(row)" style="margin-right: 4px;" />
                {{ getUserTypeName(row) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="gradeName" label="班级" min-width="120" show-overflow-tooltip />

        <el-table-column prop="createTime" label="注册时间" width="160" align="center" />

        <el-table-column v-if="role === 'admin'" label="审核状态" width="120" align="center">
          <template slot-scope="{ row }">
            <el-tag
              :type="getAuditStatusType(row.auditStatus)"
              effect="light"
              :class="`status-tag status-${row.auditStatus}`"
            >
              <i :class="getAuditStatusIcon(row.auditStatus)" class="status-icon" />
              {{ getAuditStatusName(row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template slot-scope="{ row }">
            <div class="action-buttons">
              <!-- 审核操作 -->
              <el-tooltip v-if="role === 'admin' && row.auditStatus === 0" content="审核通过" placement="top">
                <el-button
                  type="success"
                  size="mini"
                  icon="el-icon-check"
                  circle
                  class="action-btn approve-btn"
                  @click="handleApprove(row)"
                />
              </el-tooltip>

              <el-tooltip v-if="role === 'admin' && row.auditStatus === 0" content="审核拒绝" placement="top">
                <el-button
                  type="danger"
                  size="mini"
                  icon="el-icon-close"
                  circle
                  class="action-btn reject-btn"
                  @click="handleReject(row)"
                />
              </el-tooltip>

              <!-- 移除班级操作 -->
              <el-tooltip v-if="role === 'teacher' && row.gradeId" content="移出班级" placement="top">
                <el-button
                  type="warning"
                  size="mini"
                  icon="el-icon-remove-outline"
                  circle
                  class="action-btn remove-btn"
                  @click="removeUserClass(row)"
                />
              </el-tooltip>

              <!-- 删除用户操作 -->
              <el-tooltip v-if="role === 'admin'" content="删除用户" placement="top">
                <el-button
                  type="danger"
                  size="mini"
                  icon="el-icon-delete"
                  circle
                  class="action-btn delete-btn"
                  :disabled="!canDeleteUser(row)"
                  @click="delUser(row)"
                />
              </el-tooltip>

              <!-- 查看详情 -->
              <el-tooltip content="查看用户详情信息" placement="top">
                <el-button
                  type="info"
                  size="mini"
                  icon="el-icon-view"
                  circle
                  class="action-btn view-btn"
                  @click="viewUserDetail(row)"
                />
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-show="data.total > 0"
          :current-page="pageNum"
          :page-sizes="[10, 20, 30, 50]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="data.total"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增用户对话框 -->
    <el-dialog
      title="新增用户"
      :visible.sync="addUserDiologVisible"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="addFormRef" :model="addForm" :rules="addFormRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="userName">
              <el-input v-model="addForm.userName" placeholder="请输入用户名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="addForm.realName" placeholder="请输入真实姓名" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row v-if="role === 'admin'" :gutter="20">
          <el-col :span="12">
            <el-form-item label="身份选择" prop="roleId">
              <el-select v-model="addForm.roleId" placeholder="请选择身份" style="width: 100%">
                <el-option label="学生" value="1" />
                <el-option label="教师" value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col v-if="role === 'teacher' || (role === 'admin' && addForm.roleId === '1')" :span="12">
            <el-form-item label="班级选择" prop="gradeId">
              <ClassSelect v-model="addForm.gradeId" :is-multiple="false" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="addUserDiologVisible = false">取消</el-button>
        <el-button type="primary" :loading="addLoading" @click="addUser">确定</el-button>
      </div>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog
      :title="auditDialog.title"
      :visible.sync="auditDialog.visible"
      width="500px"
      :close-on-click-modal="false"
      @close="handleAuditClose"
    >
      <el-form ref="auditFormRef" :model="auditForm" :rules="auditFormRules" label-width="80px">
        <el-form-item label="审核备注" prop="auditRemark">
          <el-input
            v-model="auditForm.auditRemark"
            type="textarea"
            :rows="4"
            :placeholder="auditDialog.type === 'approve' ? '请输入审核通过备注（可选）' : '请输入审核拒绝原因（建议填写）'"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="auditDialog.visible = false">取消</el-button>
        <el-button
          :type="auditDialog.type === 'approve' ? 'success' : 'danger'"
          :loading="auditLoading"
          @click="handleAuditConfirm"
        >
          {{ auditDialog.type === 'approve' ? '审核通过' : '审核拒绝' }}
        </el-button>
      </div>
    </el-dialog>

    <!-- 文件上传对话框 -->
    <el-dialog
      title="批量导入用户"
      :visible.sync="fileDialogVisible"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-upload
        class="upload-demo"
        drag
        action="#"
        :auto-upload="false"
        :on-change="handleFileChange"
        :file-list="fileList"
        accept=".xlsx,.xls"
      >
        <i class="el-icon-upload" />
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <div slot="tip" class="el-upload__tip">
          只能上传xls/xlsx文件，且不超过500kb
          <el-button type="text" style="margin-left: 10px;" @click="startDownload">下载模板</el-button>
        </div>
      </el-upload>

      <div slot="footer" class="dialog-footer">
        <el-button @click="fileDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="importLoading" @click="importUser">导入</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import ClassSelect from '@/components/ClassSelect'
import { userPaging, classAdd, userDel, userImport, adminJoinGrade } from '@/api/user'
import { userClassRemove, teacherRemoveStudent } from '@/api/class_'
import { approveUser, rejectUser } from '@/api/user-audit'

export default {
  name: 'UserManagement',
  components: { ClassSelect },
  data() {
    return {
      // 当前登录角色
      role: '',
      pageNum: 1,
      pageSize: 10,
      listLoading: false,
      addLoading: false,
      auditLoading: false,
      importLoading: false,

      // 分页查找的数据
      data: {},

      // 筛选相关
      filterExpanded: true,
      searchForm: {
        searchRealName: '',
        searchClass: '',
        auditStatus: null
      },

      // 统计信息
      stats: {
        pendingCount: 0,
        approvedCount: 0,
        rejectedCount: 0,
        totalCount: 0
      },

      // 新增用户相关
      addUserDiologVisible: false,
      addForm: {
        userName: '',
        realName: '',
        roleId: '',
        gradeId: ''
      },
      addFormRules: {
        userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
        roleId: [{ required: true, message: '请选择身份', trigger: 'change' }]
      },

      // 表格选择
      multipleSelection: [],

      // 审核相关
      auditDialog: {
        visible: false,
        title: '',
        type: '',
        userId: null
      },
      auditForm: {
        auditRemark: ''
      },
      auditFormRules: {
        auditRemark: [
          { max: 200, message: '备注长度不能超过200个字符', trigger: 'blur' }
        ]
      },

      // 文件上传相关
      fileDialogVisible: false,
      fileList: []
    }
  },

  computed: {
    isCurrentUserAdmin() {
      // 检查当前用户是否是admin用户（用户名包含"admin"）
      const username = this.$store?.getters?.name || localStorage.getItem('username') || ''
      return username.toLowerCase() === 'admin'
    }
  },

  created() {
    // 获取当前用户角色，支持多种格式
    const storedRole = localStorage.getItem('roles') || ''

    // 处理角色名称，支持 'admin'、'role_admin' 等格式
    if (storedRole.includes('admin') || storedRole === '3') {
      this.role = 'admin'
    } else if (storedRole.includes('teacher') || storedRole === '2') {
      this.role = 'teacher'
    } else {
      this.role = 'student'
    }

    console.log('当前用户角色:', this.role, '存储的角色:', storedRole)

    this.getUserPage()
    if (this.role === 'admin') {
      this.getStats()
    }
  },

  methods: {

    // 分页查询用户
    async getUserPage(pageNum = 1, pageSize = 10, realName = null, gradeId = null, auditStatus = null) {
      this.listLoading = true
      try {
        const params = {
          pageNum: pageNum,
          pageSize: pageSize,
          realName: realName,
          gradeId: gradeId
        }

        // 如果是管理员，添加审核状态筛选（包括null值，表示全部）
        if (this.role === 'admin') {
          params.auditStatus = auditStatus
        }

        const res = await userPaging(params)
        if (res.code) {
          this.data = res.data
          // 更新现有用户的审核状态为通过（模拟后端更新）
          this.updateExistingUsersAuditStatus()
        }
      } catch (error) {
        console.error('获取用户列表失败:', error)
        this.$message.error('获取数据失败')
      } finally {
        this.listLoading = false
      }
    },

    // 更新现有用户的审核状态为通过
    updateExistingUsersAuditStatus() {
      if (this.data.records && this.role === 'admin') {
        this.data.records.forEach(user => {
          // 如果用户没有审核状态，设置为审核通过
          if (user.auditStatus === undefined || user.auditStatus === null) {
            user.auditStatus = 1
            user.auditRemark = '系统自动审核通过'
          }
        })
      }
    },

    // 获取统计信息
    async getStats() {
      try {
        // 调用API获取真实的统计数据
        const res = await userPaging({
          pageNum: 1,
          pageSize: 10000, // 获取足够大的数据来计算统计
          realName: this.searchForm.searchRealName,
          gradeId: this.searchForm.searchClass
        })

        if (res.code && res.data && res.data.records) {
          const records = res.data.records
          this.stats = {
            pendingCount: records.filter(u => u.auditStatus === 0).length,
            approvedCount: records.filter(u => u.auditStatus === 1).length,
            rejectedCount: records.filter(u => u.auditStatus === 2).length,
            totalCount: records.length
          }
        } else {
          // 如果API调用失败，使用当前页面的数据进行统计
          this.stats = {
            pendingCount: this.data.records ? this.data.records.filter(u => u.auditStatus === 0).length : 0,
            approvedCount: this.data.records ? this.data.records.filter(u => u.auditStatus === 1).length : 0,
            rejectedCount: this.data.records ? this.data.records.filter(u => u.auditStatus === 2).length : 0,
            totalCount: this.data.total || 0
          }
        }
      } catch (error) {
        console.error('获取统计信息失败:', error)
        // 出错时使用当前页面的数据进行统计
        this.stats = {
          pendingCount: this.data.records ? this.data.records.filter(u => u.auditStatus === 0).length : 0,
          approvedCount: this.data.records ? this.data.records.filter(u => u.auditStatus === 1).length : 0,
          rejectedCount: this.data.records ? this.data.records.filter(u => u.auditStatus === 2).length : 0,
          totalCount: this.data.total || 0
        }
      }
    },

    // 搜索功能
    async searchUser() {
      this.pageNum = 1
      await this.getUserPage(
        this.pageNum,
        this.pageSize,
        this.searchForm.searchRealName,
        this.searchForm.searchClass,
        this.searchForm.auditStatus
      )
      // 更新统计数据
      if (this.role === 'admin') {
        await this.getStats()
      }
    },

    // 重置筛选
    async resetFilter() {
      this.searchForm = {
        searchRealName: '',
        searchClass: '',
        auditStatus: null
      }
      this.pageNum = 1
      await this.getUserPage()
      // 更新统计数据
      if (this.role === 'admin') {
        await this.getStats()
      }
      this.$message.success('筛选条件已重置')
    },

    // 通过统计卡片筛选
    async filterByStatus(status) {
      this.searchForm.auditStatus = status
      this.pageNum = 1

      // 调用API获取筛选后的数据
      await this.getUserPage(
        this.pageNum,
        this.pageSize,
        this.searchForm.searchRealName,
        this.searchForm.searchClass,
        status
      )

      // 更新统计数据
      if (this.role === 'admin') {
        await this.getStats()
      }

      let statusText = '全部'
      if (status === 0) statusText = '待审核'
      else if (status === 1) statusText = '审核通过'
      else if (status === 2) statusText = '审核拒绝'

      this.$message.success(`已筛选${statusText}用户`)
    },

    // 切换筛选栏显示
    toggleFilter() {
      this.filterExpanded = !this.filterExpanded
    },

    // 分页大小改变
    async handleSizeChange(val) {
      this.pageSize = val
      this.pageNum = 1
      await this.getUserPage(this.pageNum, val, this.searchForm.searchRealName, this.searchForm.searchClass, this.searchForm.auditStatus)
      // 更新统计数据
      if (this.role === 'admin') {
        await this.getStats()
      }
    },
    // 当前页改变
    async handleCurrentChange(val) {
      this.pageNum = val
      await this.getUserPage(val, this.pageSize, this.searchForm.searchRealName, this.searchForm.searchClass, this.searchForm.auditStatus)
      // 更新统计数据
      if (this.role === 'admin') {
        await this.getStats()
      }
    },

    // 新增用户
    addUser() {
      this.$refs.addFormRef.validate(async(valid) => {
        if (valid) {
          this.addLoading = true
          try {
            const data = {
              userName: this.addForm.userName,
              realName: this.addForm.realName,
              roleId: this.addForm.roleId,
              gradeId: this.addForm.gradeId
            }

            const res = await classAdd(data)
            if (res.code) {
              this.$message.success('新增用户成功')
              this.addUserDiologVisible = false
              this.$refs.addFormRef.resetFields()
              this.getUserPage()
            } else {
              this.$message.error(res.msg || '新增失败')
            }
          } catch (error) {
            console.error('新增用户失败:', error)
            this.$message.error('新增失败')
          } finally {
            this.addLoading = false
          }
        }
      })
    },

    // 审核通过
    handleApprove(row) {
      this.auditDialog = {
        visible: true,
        title: `审核通过 - ${row.realName}`,
        type: 'approve',
        userId: row.id
      }
      this.auditForm.auditRemark = ''
    },

    // 审核拒绝
    handleReject(row) {
      this.auditDialog = {
        visible: true,
        title: `审核拒绝 - ${row.realName}`,
        type: 'reject',
        userId: row.id
      }
      this.auditForm.auditRemark = ''
    },

    // 审核确认
    async handleAuditConfirm() {
      this.$refs.auditFormRef.validate(async(valid) => {
        if (valid) {
          this.auditLoading = true
          try {
            // 调用后端审核API
            let response
            const auditData = this.auditForm.auditRemark ? { auditRemark: this.auditForm.auditRemark } : {}

            if (this.auditDialog.type === 'approve') {
              response = await approveUser(this.auditDialog.userId, auditData)
            } else {
              response = await rejectUser(this.auditDialog.userId, auditData)
            }

            if (response.code === 1) {
              if (this.auditDialog.type === 'approve') {
                this.$message.success(`用户审核通过${this.auditForm.auditRemark ? '，备注：' + this.auditForm.auditRemark : ''}`)
              } else {
                this.$message.success(`用户审核拒绝${this.auditForm.auditRemark ? '，原因：' + this.auditForm.auditRemark : ''}`)
              }

              this.auditDialog.visible = false

              // 立即重新加载数据
              await this.getUserPage(
                this.pageNum,
                this.pageSize,
                this.searchForm.searchRealName,
                this.searchForm.searchClass,
                this.searchForm.auditStatus
              )

              // 更新统计数据
              if (this.role === 'admin') {
                await this.getStats()
              }
            } else {
              this.$message.error(response.msg || '审核操作失败')
            }
          } catch (error) {
            console.error('审核操作失败:', error)
            this.$message.error('审核操作失败，请检查网络连接')
          } finally {
            this.auditLoading = false
          }
        }
      })
    },

    // 审核对话框关闭
    handleAuditClose() {
      this.auditForm.auditRemark = ''
      this.$refs.auditFormRef.clearValidate()
    },

    // 查看用户详情
    viewUserDetail(row) {
      this.$message.info(`查看用户详情：${row.realName}（${row.userName}）`)
      // 这里可以打开用户详情对话框
    },

    // 删除用户
    delUser(row) {
      // 检查是否是admin用户，admin用户不可删除（除非当前用户也是admin）
      if (row.userName.toLowerCase() === 'admin' && !this.isCurrentUserAdmin) {
        this.$message.warning('系统管理员账户不可删除')
        return
      }

      // 检查删除权限
      if (!this.canDeleteUser(row)) {
        this.$message.warning('您没有权限删除该用户')
        return
      }

      this.$confirm(`
        <div style="text-align: left;">
          <p style="color: #f56c6c; font-weight: 600; margin-bottom: 8px;">⚠️ 确认删除用户</p>
          <p style="margin-bottom: 4px;"><strong>用户名：</strong>${row.userName}</p>
          <p style="margin-bottom: 4px;"><strong>真实姓名：</strong>${row.realName}</p>
          <p style="margin-bottom: 4px;"><strong>角色：</strong>${this.getRoleName(row.roleId)}</p>
          <p style="margin-bottom: 4px;"><strong>管理员等级：</strong>${this.getAdminLevelText(row.adminLevel)}</p>
          <p style="color: #f56c6c; font-size: 12px; margin-top: 8px;">此操作将永久删除用户数据，且无法恢复，请谨慎操作！</p>
        </div>
      `, '确认删除用户', {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger',
        dangerouslyUseHTMLString: true,
        customClass: 'delete-confirm-dialog'
      }).then(async() => {
        try {
          console.log('开始删除用户，用户ID:', row.id)

          // 检查当前用户是否真的是管理员
          const currentUserInfo = this.getCurrentUserInfo()
          console.log('删除前验证当前用户信息:', currentUserInfo)

          // 如果当前用户不是管理员，提示重新登录
          if (!currentUserInfo.isAdmin) {
            this.$message.warning('当前用户权限异常，请重新登录')
            return
          }

          const res = await userDel(row.id)
          console.log('删除API响应:', res)

          if (res.code) {
            this.$message.success(`用户"${row.realName}"删除成功`)
            this.getUserPage()
            this.getStats()
          } else {
            this.$message.error(res.msg || '删除失败')
            console.error('删除失败，响应信息:', res)

            // 如果是权限问题，提示重新登录
            if (res.msg && res.msg.includes('不是管理员')) {
              this.$message.warning('权限验证失败，请重新登录系统')
            }
          }
        } catch (error) {
          console.error('删除用户失败:', error)
          this.$message.error('删除失败: ' + (error.message || '未知错误'))
        }
      }).catch(() => {
        this.$message.info('已取消删除')
      })
    },

    // 批量删除用户
    batchDeleteUsers() {
      if (this.multipleSelection.length === 0) {
        this.$message.warning('请选择要删除的用户')
        return
      }

      // 检查是否有系统管理员
      const systemAdmins = this.multipleSelection.filter(user =>
        user.adminLevel === 1
      )

      if (systemAdmins.length > 0) {
        this.$message.warning('无法删除系统管理员')
        return
      }

      // 检查删除权限
      const cannotDeleteUsers = this.multipleSelection.filter(user =>
        !this.canDeleteUser(user)
      )

      if (cannotDeleteUsers.length > 0) {
        this.$message.warning('您没有权限删除部分选中的用户')
        return
      }

      const userNames = this.multipleSelection.map(user => user.realName).join('、')

      this.$confirm(`
        <div style="text-align: left;">
          <p style="color: #f56c6c; font-weight: 600; margin-bottom: 8px;">⚠️ 确认批量删除用户</p>
          <p style="margin-bottom: 4px;"><strong>删除用户：</strong>${userNames}</p>
          <p style="margin-bottom: 4px;"><strong>删除数量：</strong>${this.multipleSelection.length} 个用户</p>
          <p style="color: #f56c6c; font-size: 12px; margin-top: 8px;">此操作将永久删除用户数据，且无法恢复，请谨慎操作！</p>
        </div>
      `, '确认批量删除用户', {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger',
        dangerouslyUseHTMLString: true,
        customClass: 'delete-confirm-dialog'
      }).then(async() => {
        try {
          const userIds = this.multipleSelection.map(user => user.id).join(',')
          console.log('开始批量删除用户，用户ID列表:', userIds)
          const res = await userDel(userIds)
          console.log('批量删除API响应:', res)

          if (res.code) {
            this.$message.success(`成功删除 ${this.multipleSelection.length} 个用户`)
            this.multipleSelection = []
            this.getUserPage()
            this.getStats()
          } else {
            this.$message.error(res.msg || '批量删除失败')
            console.error('批量删除失败，响应信息:', res)
          }
        } catch (error) {
          console.error('批量删除用户失败:', error)
          this.$message.error('批量删除失败: ' + (error.message || '未知错误'))
        }
      }).catch(() => {
        this.$message.info('已取消删除')
      })
    },

    // 表格选择变化
    handleSelectionChange(selection) {
      this.multipleSelection = selection
    },

    // 批量移出班级
    batchRemoveFromClass() {
      if (this.multipleSelection.length === 0) {
        this.$message.warning('请选择要移出班级的学生')
        return
      }
      const removable = this.multipleSelection.filter(u => u.gradeId)
      if (removable.length === 0) {
        this.$message.warning('所选学生均未加入班级')
        return
      }
      const names = removable.map(u => u.realName).join('、')
      this.$confirm(`确定要将以下学生移出班级吗？\n${names}`, '确认批量移出', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async() => {
        let successCount = 0
        let failCount = 0
        for (const user of removable) {
          try {
            const res = await teacherRemoveStudent(user.id)
            if (res.code) successCount++
            else failCount++
          } catch (e) {
            failCount++
          }
        }
        this.$message.success(`移出完成：成功${successCount}人${failCount > 0 ? '，失败' + failCount + '人' : ''}`)
        this.multipleSelection = []
        this.getUserPage()
      }).catch(() => {
        this.$message.info('已取消批量移出')
      })
    },

    // 显示加入班级对话框（临时实现）
    showAdminJoinGradeDialog() {
      this.$message.info('加入班级功能暂未实现')
    },

    // 获取当前用户信息
    getCurrentUserInfo() {
      try {
        // 尝试从不同位置获取用户信息
        let userInfo = {}

        // 1. 从localStorage获取
        const userStr = localStorage.getItem('user') || '{}'
        if (userStr !== '{}') {
          try {
            userInfo = JSON.parse(userStr)
          } catch (e) {
            console.warn('解析用户信息失败:', e)
          }
        }

        // 2. 如果localStorage中没有，尝试从其他字段获取
        if (Object.keys(userInfo).length === 0) {
          const username = localStorage.getItem('username') || ''
          const roles = localStorage.getItem('roles') || ''

          if (roles) {
            let roleId = 1
            let adminLevel = 0

            if (roles.includes('admin') || roles === '3') {
              roleId = 3
              adminLevel = roles.includes('admin') ? 1 : 2
            } else if (roles.includes('teacher') || roles === '2') {
              roleId = 2
            }

            userInfo = {
              userName: username || '未知用户',
              roleId: roleId,
              adminLevel: adminLevel
            }
          }
        }

        // 3. 检查是否是管理员
        const isAdmin = userInfo.roleId === 3 || userInfo.adminLevel > 0

        return {
          ...userInfo,
          isAdmin: isAdmin
        }
      } catch (error) {
        console.error('获取当前用户信息失败:', error)
        return { isAdmin: false }
      }
    },

    // 移除班级
    removeUserClass(row) {
      this.$confirm(`确定要将用户"${row.realName}"移出班级吗？`, '确认移出', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async() => {
        try {
          let res
          if (this.role === 'teacher') {
            res = await teacherRemoveStudent(row.id)
          } else {
            res = await userClassRemove(row.id)
          }
          if (res.code) {
            this.$message.success('移出成功')
            this.getUserPage()
          } else {
            this.$message.error(res.msg || '移出失败')
          }
        } catch (error) {
          console.error('移出班级失败:', error)
          this.$message.error('移出失败')
        }
      }).catch(() => {
        this.$message.info('已取消移出')
      })
    },

    // 文件上传处理
    handleFileChange(file, fileList) {
      this.fileList = fileList
    },

    // 导入用户
    async importUser() {
      if (this.fileList.length === 0) {
        this.$message.warning('请选择文件后再导入')
        return
      }

      this.importLoading = true
      try {
        const formData = new FormData()
        formData.append('file', this.fileList[0].raw)

        const res = await userImport(formData)
        if (res.code) {
          this.$message.success('导入成功')
          this.fileDialogVisible = false
          this.fileList = []
          this.getUserPage()
          this.getStats()
        } else {
          this.$message.error(res.msg || '导入失败')
        }
      } catch (error) {
        console.error('导入失败:', error)
        this.$message.error('导入失败')
      } finally {
        this.importLoading = false
      }
    },

    // 下载模板
    startDownload() {
      this.$message.info('开始下载导入模板')
      // 这里可以添加下载模板的逻辑
    },

    // 获取角色名称
    getRoleName(roleId) {
      const roleMap = {
        1: '学生',
        2: '教师',
        3: '管理员'
      }
      return roleMap[roleId] || '未知'
    },

    // 获取角色标签类型
    getRoleTagType(roleId) {
      const typeMap = {
        1: 'info',
        2: 'warning',
        3: 'danger'
      }
      return typeMap[roleId] || 'info'
    },

    // 获取管理员等级名称
    getAdminLevelName(adminLevel) {
      const levelMap = {
        1: '系统管理员',
        2: '一级管理员',
        3: '二级管理员'
      }
      return levelMap[adminLevel] || '未知'
    },

    // 获取管理员等级标签类型
    getAdminLevelTagType(adminLevel) {
      const typeMap = {
        1: 'danger',
        2: 'warning',
        3: 'info'
      }
      return typeMap[adminLevel] || 'info'
    },

    // 获取角色图标
    getRoleIcon(roleId) {
      const iconMap = {
        1: 'el-icon-user',
        2: 'el-icon-s-custom',
        3: 'el-icon-s-platform'
      }
      return iconMap[roleId] || 'el-icon-user'
    },

    // 获取管理员等级图标
    getAdminLevelIcon(adminLevel) {
      const iconMap = {
        1: 'el-icon-s-flag',
        2: 'el-icon-star-on',
        3: 'el-icon-star-off'
      }
      return iconMap[adminLevel] || 'el-icon-star-off'
    },

    // 获取用户类型名称（统一显示一个身份）
    getUserTypeName(user) {
      // 如果是管理员角色，根据管理员等级显示
      if (user.roleId === 3) {
        if (user.adminLevel === 1) {
          return '系统管理员'
        } else if (user.adminLevel === 2 || user.adminLevel === 3) {
          return '管理员'
        }
      }

      // 其他角色正常显示
      const roleMap = {
        1: '学生',
        2: '教师',
        3: '管理员'
      }
      return roleMap[user.roleId] || '未知'
    },

    // 获取用户类型标签类型
    getUserTypeTagType(user) {
      // 如果是管理员角色，根据管理员等级显示不同颜色
      if (user.roleId === 3) {
        if (user.adminLevel === 1) {
          return 'danger' // 系统管理员用红色
        } else if (user.adminLevel === 2 || user.adminLevel === 3) {
          return 'warning' // 普通管理员用橙色
        }
      }

      // 其他角色正常显示
      const typeMap = {
        1: 'info', // 学生用绿色
        2: 'warning', // 教师用蓝色
        3: 'danger' // 管理员用灰色（默认）
      }
      return typeMap[user.roleId] || 'info'
    },

    // 获取用户类型图标
    getUserTypeIcon(user) {
      // 如果是管理员角色，根据管理员等级显示不同图标
      if (user.roleId === 3) {
        if (user.adminLevel === 1) {
          return 'el-icon-s-flag' // 系统管理员用旗帜图标
        } else if (user.adminLevel === 2 || user.adminLevel === 3) {
          return 'el-icon-s-platform' // 普通管理员用平台图标
        }
      }

      // 其他角色正常显示
      const iconMap = {
        1: 'el-icon-user', // 学生用用户图标
        2: 'el-icon-s-custom', // 教师用人员图标
        3: 'el-icon-s-platform' // 管理员用平台图标
      }
      return iconMap[user.roleId] || 'el-icon-user'
    },

    // 判断是否可以删除用户
    canDeleteUser(row) {
      try {
        // 获取当前用户信息，支持多种存储格式
        let currentUser = {}

        // 尝试从不同位置获取用户信息
        const userStr = localStorage.getItem('user') || '{}'

        // 如果localStorage中没有用户信息，尝试从Vuex store获取
        if (userStr === '{}') {
          const username = this.$store?.getters?.name || ''
          const roleId = this.$store?.getters?.roleId || 0
          const adminLevel = this.$store?.getters?.adminLevel || 0

          if (username) {
            currentUser = {
              userName: username,
              roleId: roleId,
              adminLevel: adminLevel
            }
          }
        } else {
          try {
            currentUser = JSON.parse(userStr)
          } catch (e) {
            console.warn('解析用户信息失败，使用默认值:', e)
            currentUser = {}
          }
        }

        // 如果仍然没有用户信息，尝试从localStorage的其他字段获取
        if (Object.keys(currentUser).length === 0) {
          const username = localStorage.getItem('username') || ''
          const roles = localStorage.getItem('roles') || ''

          console.log('从localStorage获取角色信息:', roles)

          if (roles) {
            // 根据角色信息判断用户类型
            let roleId = 1 // 默认学生
            let adminLevel = 0 // 默认非管理员

            if (roles.includes('admin') || roles === '3') {
              roleId = 3 // 管理员
              adminLevel = roles.includes('admin') ? 1 : 2 // 系统管理员或普通管理员
            } else if (roles.includes('teacher') || roles === '2') {
              roleId = 2 // 教师
            }

            currentUser = {
              userName: username || '未知用户',
              roleId: roleId,
              adminLevel: adminLevel
            }
          }
        }

        const currentAdminLevel = currentUser.adminLevel || 0
        const currentRoleId = currentUser.roleId || 0

        console.log('当前用户信息:', currentUser)
        console.log('目标用户信息:', row)
        console.log('localStorage user:', localStorage.getItem('user'))
        console.log('localStorage username:', localStorage.getItem('username'))
        console.log('localStorage roles:', localStorage.getItem('roles'))

        // 只有管理员才能删除用户
        if (currentRoleId !== 3) {
          console.log('不是管理员，不能删除用户，当前角色ID:', currentRoleId)
          return false
        }

        // 系统管理员不能删除（保护系统管理员账户）
        if (row.adminLevel === 1) {
          console.log('不能删除系统管理员')
          return false
        }

        // 当前用户是系统管理员（adminLevel=1），可以删除所有用户（除了系统管理员）
        if (currentAdminLevel === 1) {
          console.log('系统管理员可以删除该用户')
          return true
        }

        // 一级管理员（adminLevel=2）可以删除二级管理员和学生
        if (currentAdminLevel === 2) {
          // 一级管理员不能删除系统管理员和一级管理员
          if (row.adminLevel === 1 || row.adminLevel === 2) {
            console.log('一级管理员不能删除系统管理员和一级管理员')
            return false
          }

          // 一级管理员可以删除二级管理员、学生和教师
          console.log('一级管理员可以删除该用户')
          return true
        }

        // 二级管理员（adminLevel=3）只能删除学生
        if (currentAdminLevel === 3) {
          // 二级管理员只能删除学生和教师（非管理员）
          if (row.roleId !== 3) {
            console.log('二级管理员可以删除非管理员用户')
            return true
          }

          console.log('二级管理员不能删除其他管理员')
          return false
        }

        console.log('默认返回false，当前管理员等级:', currentAdminLevel)
        return false
      } catch (error) {
        console.error('权限检查异常:', error)
        return false
      }
    },

    // 获取管理员等级文本
    getAdminLevelText(adminLevel) {
      const levelMap = {
        1: '系统管理员',
        2: '一级管理员',
        3: '二级管理员'
      }
      return levelMap[adminLevel] || '普通用户'
    },

    // 检查当前用户是否是管理员
    get isCurrentUserAdmin() {
      const currentUser = JSON.parse(localStorage.getItem('user') || '{}')
      return currentUser.roleId === 3
    },

    // 判断是否可以移除班级
    canRemoveFromClass(row) {
      const currentUser = JSON.parse(localStorage.getItem('user') || '{}')
      const currentAdminLevel = currentUser.adminLevel || 0

      // 系统管理员可以移除任何人的班级
      if (currentAdminLevel === 1) {
        return true
      }

      // 一级管理员可以移除二级管理员和学生的班级
      if (currentAdminLevel === 2) {
        if (row.roleId === 3 && row.adminLevel === 2) {
          return false // 不能移除一级管理员的班级
        }
        return true
      }

      // 二级管理员只能移除学生的班级
      if (currentAdminLevel === 3) {
        return row.roleId === 1 // 只能移除学生的班级
      }

      return false
    },

    // 获取审核状态名称
    getAuditStatusName(status) {
      const statusMap = {
        0: '待审核',
        1: '审核通过',
        2: '审核拒绝'
      }
      return statusMap[status] || '未知'
    },

    // 获取审核状态图标
    getAuditStatusIcon(status) {
      const iconMap = {
        0: 'el-icon-time',
        1: 'el-icon-success',
        2: 'el-icon-error'
      }
      return iconMap[status] || 'el-icon-question'
    },

    // 获取审核状态标签类型
    getAuditStatusType(status) {
      const typeMap = {
        0: 'warning',
        1: 'success',
        2: 'danger'
      }
      return typeMap[status] || 'info'
    }
  }
}
</script>

<style lang="scss" scoped>
.user-management-container {
  padding: 20px;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 24px;

    .header-left {
      .page-title {
        font-size: 24px;
        font-weight: 600;
        color: #303133;
        margin: 0 0 8px 0;
      }

      .page-subtitle {
        font-size: 14px;
        color: #909399;
        margin: 0;
      }
    }

    .header-actions {
      display: flex;
      gap: 12px;
    }
  }

  .filter-card {
    margin-bottom: 24px;
    border: 1px solid #e4e7ed;

    .filter-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .filter-title {
        font-weight: 600;
        color: #303133;
      }

      .filter-toggle {
        color: #409eff;
      }
    }

    .filter-content {
      .filter-form {
        .el-form-item {
          margin-bottom: 0;
        }

        .filter-actions {
          ::v-deep .el-form-item__content {
            display: flex;
            gap: 12px;
          }
        }
      }
    }
  }

  .stats-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
    gap: 16px;
    margin-bottom: 24px;

    .stat-card {
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      }

      &.active {
        border: 2px solid #409eff;
        background: #f0f9ff;

        .stat-icon {
          &.pending { background: #d48806; }
          &.approved { background: #52c41a; }
          &.rejected { background: #f5222d; }
          &.total { background: #1890ff; }
        }
      }

      .stat-content {
        display: flex;
        align-items: center;

        .stat-icon {
          width: 48px;
          height: 48px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 16px;
          font-size: 24px;
          color: white;
          transition: background 0.3s ease;

          &.pending { background: #e6a23c; }
          &.approved { background: #67c23a; }
          &.rejected { background: #f56c6c; }
          &.total { background: #409eff; }
        }

        .stat-info {
          .stat-value {
            font-size: 24px;
            font-weight: 600;
            color: #303133;
            line-height: 1;
          }

          .stat-label {
            font-size: 14px;
            color: #909399;
            margin-top: 4px;
          }
        }
      }
    }
  }

  .table-card {
    border: 1px solid #e4e7ed;

    .table-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      .table-title {
        font-size: 18px;
        font-weight: 600;
        color: #303133;
      }

      .table-actions {
        display: flex;
        gap: 8px;
      }
    }

    .user-table {
      border-radius: 4px;

      .serial-number {
        color: #909399;
        font-weight: 500;
      }

      .user-info {
        display: flex;
        align-items: center;
        gap: 8px;

        .user-avatar {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }

        .user-name {
          font-weight: 500;
        }

        .user-type-display {
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 6px;
          padding: 4px 0;

          .el-tag {
            font-weight: 500;
            letter-spacing: 0.5px;
            border: none;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
            transition: all 0.3s ease;

            &:hover {
              transform: translateY(-2px);
              box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
            }
          }

          .admin-level-tag {
            margin-top: 4px;
            animation: fadeIn 0.5s ease;
          }
        }

        @keyframes fadeIn {
          from {
            opacity: 0;
            transform: translateY(-10px);
          }
          to {
            opacity: 1;
            transform: translateY(0);
          }
        }
      }

      .status-tag {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        padding: 4px 8px;
        border-radius: 12px;

        .status-icon {
          font-size: 12px;
        }

        &.status-0 {
          background: #fdf6ec;
          border-color: #f5dab1;
          color: #e6a23c;
        }

        &.status-1 {
          background: #f0f9eb;
          border-color: #c2e7b0;
          color: #67c23a;
        }

        &.status-2 {
          background: #fef0f0;
          border-color: #fbc4c4;
          color: #f56c6c;
        }
      }

      .action-buttons {
        display: flex;
        justify-content: center;
        gap: 8px;

        .action-btn {
          transition: all 0.3s ease;
          margin: 0 2px;

          &:hover {
            transform: scale(1.1);
          }

          &.approve-btn:hover {
            background: #67c23a;
            border-color: #67c23a;
          }

          &.reject-btn:hover {
            background: #f56c6c;
            border-color: #f56c6c;
          }

          &.remove-btn:hover {
            background: #e6a23c;
            border-color: #e6a23c;
          }

          &.delete-btn {
            background-color: #f56c6c !important;
            border-color: #f56c6c !important;
          }

          &.delete-btn:hover {
            background-color: #f78989 !important;
            border-color: #f78989 !important;
          }

          &.view-btn:hover {
            background: #909399;
            border-color: #909399;
          }
        }
      }

      /* 批量删除按钮样式 */
      .header-actions .el-button--danger {
        background-color: #f56c6c !important;
        border-color: #f56c6c !important;
      }

      .header-actions .el-button--danger:hover {
        background-color: #f78989 !important;
        border-color: #f78989 !important;
      }

      .header-actions .el-button--danger:disabled {
        background-color: #fbc4c4 !important;
        border-color: #fbc4c4 !important;
        color: #fff !important;
      }

      /* 删除确认对话框样式 */
      .delete-confirm-dialog {
        .el-message-box__header {
          background-color: #fef0f0;
          border-bottom: 1px solid #fde2e2;
        }

        .el-message-box__title {
          color: #f56c6c;
          font-weight: 600;
        }
      }
    }

    .pagination-container {
      margin-top: 20px;
      text-align: right;
    }
  }

  // 响应式设计
  @media (max-width: 768px) {
    .page-header {
      flex-direction: column;
      gap: 16px;

      .header-actions {
        width: 100%;
        justify-content: flex-end;
      }
    }

    .stats-cards {
      grid-template-columns: repeat(2, 1fr);
    }

    .filter-form {
      .el-col {
        margin-bottom: 16px;
      }
    }

    .action-buttons {
      flex-wrap: wrap;
      gap: 4px;
    }
  }

  @media (max-width: 480px) {
    .stats-cards {
      grid-template-columns: 1fr;
    }

    .action-buttons {
      justify-content: flex-start;
    }
  }
}

// 全局样式优化
.el-dialog {
  border-radius: 8px;

  .el-dialog__header {
    border-bottom: 1px solid #e4e7ed;
    padding: 16px 20px;

    .el-dialog__title {
      font-weight: 600;
      color: #303133;
    }
  }

  .el-dialog__body {
    padding: 20px;
  }

  .el-dialog__footer {
    border-top: 1px solid #e4e7ed;
    padding: 16px 20px;
  }
}

.el-card {
  border-radius: 8px;

  .el-card__header {
    padding: 16px 20px;
    border-bottom: 1px solid #e4e7ed;
  }

  .el-card__body {
    padding: 20px;
  }
}

.el-button {
  border-radius: 4px;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
}

.el-table {
  border-radius: 4px;

  th {
    font-weight: 600;
  }

  .el-table__body-wrapper {
    &::-webkit-scrollbar {
      width: 6px;
      height: 6px;
    }

    &::-webkit-scrollbar-track {
      background: #f5f5f5;
      border-radius: 3px;
    }

    &::-webkit-scrollbar-thumb {
      background: #c0c4cc;
      border-radius: 3px;

      &:hover {
        background: #909399;
      }
    }
  }
}

.el-pagination {
  .el-pager li {
    border-radius: 4px;

    &.active {
      background: #409eff;
      color: white;
    }
  }
}

.el-tag {
  border-radius: 12px;

  &.el-tag--light {
    border: none;
  }
}

.el-upload-dragger {
  border-radius: 8px;
  border: 2px dashed #dcdfe6;

  &:hover {
    border-color: #409eff;
  }
}

// 动画效果
.el-collapse-transition-enter-active,
.el-collapse-transition-leave-active {
  transition: all 0.3s ease;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter, .fade-leave-to {
  opacity: 0;
}

// 加载动画优化
.el-loading-mask {
  background-color: rgba(255, 255, 255, 0.9);

  .el-loading-spinner {
    .circular {
      animation: rotate 2s linear infinite;
    }

    .path {
      stroke: #409eff;
      stroke-linecap: round;
      animation: dash 1.5s ease-in-out infinite;
    }
  }
}

@keyframes rotate {
  100% {
    transform: rotate(360deg);
  }
}

@keyframes dash {
  0% {
    stroke-dasharray: 1, 150;
    stroke-dashoffset: 0;
  }
  50% {
    stroke-dasharray: 90, 150;
    stroke-dashoffset: -35;
  }
  100% {
    stroke-dasharray: 90, 150;
    stroke-dashoffset: -124;
  }
}

// 删除确认对话框样式
.delete-confirm-dialog {
  .el-message-box {
    border-radius: 8px;
    border: 1px solid #f56c6c;

    .el-message-box__header {
      background: #fef0f0;
      border-bottom: 1px solid #fbc4c4;
      padding: 16px 20px;

      .el-message-box__title {
        color: #f56c6c;
        font-weight: 600;
      }
    }

    .el-message-box__content {
      padding: 20px;
    }

    .el-message-box__btns {
      padding: 16px 20px;
      border-top: 1px solid #e4e7ed;

      .el-button--danger {
        background: #f56c6c;
        border-color: #f56c6c;

        &:hover {
          background: #f78989;
          border-color: #f78989;
        }
      }
    }
  }
}
</style>
