<template>
  <div class="app-container">
    <!-- 页面标题 -->
    <div class="filter-container">
      <el-page-header content="用户审核管理" @back="goBack" />
    </div>

    <!-- 筛选条件 -->
    <div class="filter-container">
      <el-form :inline="true" :model="listQuery" class="demo-form-inline">
        <el-form-item label="审核状态：">
          <el-select v-model="listQuery.auditStatus" placeholder="请选择审核状态" clearable>
            <el-option label="待审核" :value="0" />
            <el-option label="审核通过" :value="1" />
            <el-option label="审核拒绝" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户名：">
          <el-input v-model="listQuery.userName" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 用户列表 -->
    <el-table
      v-loading="listLoading"
      :data="list"
      border
      fit
      highlight-current-row
      style="width: 100%"
    >
      <el-table-column label="ID" prop="id" align="center" width="80" />
      <el-table-column label="用户名" prop="userName" align="center" min-width="120" />
      <el-table-column label="真实姓名" prop="realName" align="center" min-width="120" />
      <el-table-column label="角色" align="center" width="100">
        <template slot-scope="{row}">
          <el-tag :type="getRoleType(row.roleId)">
            {{ getRoleName(row.roleId) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" prop="createTime" align="center" width="180" />
      <el-table-column label="审核状态" align="center" width="120">
        <template slot-scope="{row}">
          <el-tag :type="getAuditStatusType(row.auditStatus)">
            {{ getAuditStatusName(row.auditStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审核备注" prop="auditRemark" align="center" min-width="150" />
      <el-table-column label="审核时间" prop="auditTime" align="center" width="180" />
      <el-table-column label="操作" align="center" width="200" fixed="right">
        <template slot-scope="{row}">
          <el-button
            v-if="row.auditStatus === 0"
            type="success"
            size="mini"
            @click="handleApprove(row)"
          >
            通过
          </el-button>
          <el-button
            v-if="row.auditStatus === 0"
            type="danger"
            size="mini"
            @click="handleReject(row)"
          >
            拒绝
          </el-button>
          <span v-else-if="row.auditStatus === 1">
            <el-tag type="success">已通过</el-tag>
          </span>
          <span v-else-if="row.auditStatus === 2">
            <el-tag type="danger">已拒绝</el-tag>
          </span>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-show="total>0"
        :current-page="listQuery.page"
        :page-sizes="[10, 20, 30, 40]"
        :page-size="listQuery.limit"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 审核备注对话框 -->
    <el-dialog
      :title="auditDialog.title"
      :visible.sync="auditDialog.visible"
      width="500px"
      @close="handleAuditClose"
    >
      <el-form :model="auditForm" label-width="80px">
        <el-form-item label="审核备注：">
          <el-input
            v-model="auditForm.auditRemark"
            type="textarea"
            :rows="3"
            placeholder="请输入审核备注（可选）"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="auditDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="handleAuditConfirm">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getPendingUsers, approveUser, rejectUser, getAuditRecords } from '@/api/user-audit'

export default {
  name: 'UserAudit',
  data() {
    return {
      list: [],
      total: 0,
      listLoading: true,
      listQuery: {
        page: 1,
        limit: 10,
        auditStatus: null,
        userName: ''
      },
      auditDialog: {
        visible: false,
        title: '',
        type: '', // 'approve' or 'reject'
        userId: null
      },
      auditForm: {
        auditRemark: ''
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    // 获取用户列表
    async getList() {
      this.listLoading = true
      try {
        const params = {
          current: this.listQuery.page,
          size: this.listQuery.limit
        }

        if (this.listQuery.auditStatus !== null && this.listQuery.auditStatus !== '') {
          params.auditStatus = this.listQuery.auditStatus
        }

        let response
        if (this.listQuery.auditStatus === 0 || this.listQuery.auditStatus === '0') {
          response = await getPendingUsers(params)
        } else {
          response = await getAuditRecords(params)
        }

        if (response.code === 1) {
          this.list = response.data.records || []
          this.total = response.data.total || 0
        } else {
          this.$message.error(response.msg || '获取数据失败')
        }
      } catch (error) {
        console.error('获取用户列表失败:', error)
        this.$message.error('获取数据失败')
      } finally {
        this.listLoading = false
      }
    },

    // 查询
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },

    // 重置查询条件
    resetFilter() {
      this.listQuery = {
        page: 1,
        limit: 10,
        auditStatus: null,
        userName: ''
      }
      this.getList()
    },

    // 分页大小改变
    handleSizeChange(val) {
      this.listQuery.limit = val
      this.listQuery.page = 1
      this.getList()
    },

    // 当前页改变
    handleCurrentChange(val) {
      this.listQuery.page = val
      this.getList()
    },

    // 返回上一页
    goBack() {
      this.$router.go(-1)
    },

    // 审核通过
    handleApprove(row) {
      this.auditDialog = {
        visible: true,
        title: '审核通过',
        type: 'approve',
        userId: row.id
      }
      this.auditForm.auditRemark = ''
    },

    // 审核拒绝
    handleReject(row) {
      this.auditDialog = {
        visible: true,
        title: '审核拒绝',
        type: 'reject',
        userId: row.id
      }
      this.auditForm.auditRemark = ''
    },

    // 审核确认
    async handleAuditConfirm() {
      try {
        let response
        if (this.auditDialog.type === 'approve') {
          response = await approveUser(this.auditDialog.userId, this.auditForm)
        } else {
          response = await rejectUser(this.auditDialog.userId, this.auditForm)
        }

        if (response.code === 1) {
          this.$message.success(response.msg || '操作成功')
          this.auditDialog.visible = false
          this.getList()
        } else {
          this.$message.error(response.msg || '操作失败')
        }
      } catch (error) {
        console.error('审核操作失败:', error)
        this.$message.error('操作失败')
      }
    },

    // 审核对话框关闭
    handleAuditClose() {
      this.auditForm.auditRemark = ''
    },

    // 获取角色名称
    getRoleName(roleId) {
      const roleMap = {
        1: '学生',
        2: '老师',
        3: '管理员'
      }
      return roleMap[roleId] || '未知'
    },

    // 获取角色标签类型
    getRoleType(roleId) {
      const typeMap = {
        1: 'info',
        2: 'warning',
        3: 'danger'
      }
      return typeMap[roleId] || 'info'
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

<style scoped>
.filter-container {
  padding-bottom: 10px;
}

.demo-form-inline {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.demo-form-inline .el-form-item {
  margin-bottom: 10px;
  margin-right: 10px;
}
</style>
