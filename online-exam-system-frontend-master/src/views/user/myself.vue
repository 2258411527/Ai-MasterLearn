<template>
  <div class="item-contain">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>个人信息</span>
        <el-button
          v-if="data.gradeName == null && !isAdmin"
          style="float: right; padding: 3px 0; margin-right: 15px"
          type="text"
          size="mini"
          @click="addClassBt"
        >
          加入班级</el-button>

        <el-button
          v-if="data.gradeName != null"
          size="mini"
          style="float: right; padding: 3px 0; margin-right: 15px"
          type="text"
          @click="exitGrade"
        >
          退出班级</el-button>
        <el-button
          type="text"
          size="mini"
          style="float: right; padding: 3px 0; margin-right: 15px"
          @click="fileDialogVisible = true"
        >编辑头像</el-button>
      </div>
      <div class="card-body">
        <div class="left">
          <div>
            <span>用户名:</span>
            <span> {{ data.userName }}</span>
          </div>
          <div>
            <span>真实姓名:</span>
            <span> {{ data.realName }}</span>
          </div>
          <div class="user-type-section">
            <span>用户类型:</span>
            <div class="user-type-tags">
              <el-tag :type="getUserTypeTagType(data)" effect="dark" size="medium">
                <i :class="getUserTypeIcon(data)" style="margin-right: 6px;" />
                {{ getUserTypeName(data) }}
              </el-tag>
            </div>
          </div>
          <div v-if="!isAdmin">
            <span>班级:</span>
            <span> {{ data.gradeName?data.gradeName:"暂未加入班级" }} </span>
          </div>
        </div>
        <el-dialog
          width="400px"
          :show-close="false"
          :close-on-click-modal="false"
          title="上传头像"
          :visible.sync="fileDialogVisible"
        >
          <el-upload
            class="upload-demo"
            drag
            action="xxxxxx"
            multiple
            :limit="1"
            accept="png, jpg, jpeg, bmp"
            :auto-upload="false"
            :on-remove="handleRemove"
            :on-change="handleFileChange"
            :file-list="fileList"
          >
            <i class="el-icon-upload" />
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <div slot="tip" class="el-upload__tip">
              只能上传"png", "jpg", "jpeg",
              "bmp"文件，且不超过2MB。注:右上角头像，重新登录更新
            </div>
          </el-upload>
          <div slot="footer" class="dialog-footer">
            <el-button @click="fileDialogVisible = false">取 消</el-button>
            <el-button type="primary" @click="importAvatar">确 定</el-button>
          </div>
        </el-dialog>

        <el-dialog title="加入班级" :visible.sync="addClassDialogVisible">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form :model="form">
                <el-form-item label="班级口令">
                  <el-input v-model="form.code" autocomplete="off" />
                </el-form-item>
              </el-form>
            </el-col>
          </el-row>
          <div slot="footer" class="dialog-footer">
            <el-button
              @click="addClassDialogVisible = false"
            >取 消</el-button>
            <el-button type="primary" @click="addClass">确 定</el-button>
          </div>
        </el-dialog>

        <div class="right">
          <img
            style="
              width: 150px;
              height: 150px;
              border-radius: 200px;
            "
            :src="getAvatarUrl(data.avatar)"
            alt=""
            @error="handleAvatarError"
          >
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { exitUserGrade, getInfo, userAddClass, uploadAvatar, adminJoinGrade } from '@/api/user'
import { trackPresence } from '@/api/user'
import { setToken } from '@/utils/auth'
import { getTokenInfo, getRole } from '@/utils/jwtUtils'
import { mapActions } from 'vuex'

export default {
  data() {
    return {
      fileDialogVisible: false,
      fileList: [],
      data: {},
      form: {
        code: ''
      },
      isAdmin: false,
      addClassDialogVisible: false,
      form: {
        code: ''
      },
      hasFiles: false
    }
  },
  created() {
    // 获取角色判断是否是教师和管理员
    const role = getRole()
    if (role === 3 || role === 2) {
      this.isAdmin = true
    }
    this.getInfoFun()
  },
  methods: {
    ...mapActions('user', ['getInfo']),

    // 退出班级逻辑
    exitGrade() {
      this.$confirm('退出班级, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          exitUserGrade()
            .then((res) => {
              if (res.code) {
                this.getInfoFun()
                this.$message({
                  type: 'success',
                  message: '退出成功!'
                })
              } else {
                this.$message({
                  type: 'error',
                  message: res.msg
                })
              }
            })
            .catch(() => {
              this.$message({
                type: 'info',
                message: '已取消退出'
              })
            })
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '已取消退出'
          })
        })
    },
    // 获取个人系信息
    async getInfoFun() {
      const res = await getInfo()
      if (res.code) {
        this.data = res.data
      } else {
        this.$message.error('获取个人信息失败')
      }
    },
    // 修改文件逻辑
    handleFileChange(file, fileList) {
      this.fileList = fileList // 收集文件信息
    },
    // 移除文件处理方法
    handleRemove(file, fileList) {
      if (fileList.length === 0) {
        this.hasFiles = false
      }
    },
    // 上传文件逻辑
    importAvatar() {
      if (this.fileList.length > 0) {
        const formData = new FormData() // 创建FormData对象
        formData.append('file', this.fileList[0].raw) // 添加文件到formData
        uploadAvatar(formData)
          .then((res) => {
            if (res.code) {
              this.getInfoFun()
              // 更新Vuex store中的用户信息（包括头像）
              this.$store.dispatch('user/getInfo').then(() => {
                console.log('用户信息已更新，包括新头像')
              }).catch(err => {
                console.error('更新用户信息失败:', err)
              })
              this.$message.success('文件上传成功！')
              this.fileDialogVisible = false // 关闭对话框
              // 可以在这里处理成功后的逻辑，如刷新数据等
            }
          })
          .catch((error) => {
            console.error('文件上传失败：', error)
            this.$message.error('文件上传失败！')
          })
      } else {
        this.$message.warning('请选择文件后再上传！')
      }
    },
    // 添加班级按钮
    addClassBt() {
      this.addClassDialogVisible = true
    },

    // 添加班级逻辑
    addClass() {
      const params = { code: this.form.code }
      userAddClass(params).then((res) => {
        if (res.code) {
          this.addClassDialogVisible = false
          this.getInfoFun()
          this.$message({
            type: 'success',
            message: '加入成功'
          })
        } else {
          this.$message({
            type: 'error',
            message: res.msg
          })
        }
      })
    },

    // 处理头像URL
    getAvatarUrl(avatar) {
      if (!avatar || avatar.trim() === '') {
        return this.getDefaultAvatarUrl()
      }

      // 如果已经是完整URL，直接返回
      if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
        return avatar
      }

      // 如果是相对路径（以/开头），需要拼接基础API URL
      if (avatar.startsWith('/')) {
        // 获取基础API URL（去掉末尾的/api）
        const baseUrl = process.env.VUE_APP_BASE_API || ''
        // 如果avatar已经包含/uploads，直接拼接
        return baseUrl + avatar
      }

      // 默认情况，使用默认头像
      return this.getDefaultAvatarUrl()
    },

    // 获取默认头像URL
    getDefaultAvatarUrl() {
      // 返回一个base64编码的简单灰色圆形作为默认头像
      return 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTUwIiBoZWlnaHQ9IjE1MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48Y2lyY2xlIGN4PSI3NSIgY3k9Ijc1IiByPSI3NSIgZmlsbD0iI2UwZTBlMCIvPjx0ZXh0IHg9Ijc1IiB5PSI4MCIgZm9udC1mYW1pbHk9IkFyaWFsLCBzYW5zLXNlcmlmIiBmb250LXNpemU9IjYwIiBmaWxsPSIjOTk5IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIj7kv6Hlj7g8L3RleHQ+PC9zdmc+'
    },

    // 头像加载错误处理
    handleAvatarError(event) {
      console.warn('头像加载失败，使用默认头像:', event.target.src)
      event.target.src = this.getDefaultAvatarUrl()
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
      if (!user || !user.roleId) {
        return '未知'
      }

      // 如果是管理员角色，根据管理员等级显示
      if (user.roleId === 3) {
        // 处理 adminLevel 为 null、undefined、0 的情况
        const adminLevel = user.adminLevel || 0
        if (adminLevel === 1) {
          return '系统管理员'
        } else if (adminLevel === 2 || adminLevel === 3) {
          return '管理员'
        } else {
          // 管理员角色但没有设置管理员等级，默认显示为管理员
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
      if (!user || !user.roleId) {
        return 'info'
      }

      // 如果是管理员角色，根据管理员等级显示不同颜色
      if (user.roleId === 3) {
        // 处理 adminLevel 为 null、undefined、0 的情况
        const adminLevel = user.adminLevel || 0
        if (adminLevel === 1) {
          return 'danger' // 系统管理员用红色
        } else if (adminLevel === 2 || adminLevel === 3) {
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
      if (!user || !user.roleId) {
        return 'el-icon-user'
      }

      // 如果是管理员角色，根据管理员等级显示不同图标
      if (user.roleId === 3) {
        // 处理 adminLevel 为 null、undefined、0 的情况
        const adminLevel = user.adminLevel || 0
        if (adminLevel === 1) {
          return 'el-icon-s-flag' // 系统管理员用旗帜图标
        } else if (adminLevel === 2 || adminLevel === 3) {
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
    }
  }
}
</script>

<style scoped>
/* 卡片样式 */
.item-contain {
  padding: 30px 100px 0;
  display: flex;
  justify-content: center;
  height: 60vh;
}

.box-card {
  padding: 15px;
  width: 70% !important;
}
.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both;
}

.card-body {
  height: 29em;
  display: flex;
  justify-content: space-evenly;
}
.left {
  display: flex;
  flex-direction: column;
  width: 380px;
  height: 100%;
  padding: 60px;
  border-right: 1px solid rgb(228, 232, 235);
  div {
    margin-bottom: 40px;
    font-size: 14px Base;
    span {
      display: inline-block;
      width: 115px;
    }
  }

  .user-type-section {
    span {
      vertical-align: top;
      line-height: 32px;
    }

    .user-type-tags {
      display: inline-block;
      vertical-align: top;

      .el-tag {
        margin-right: 8px;
        font-weight: 500;
        letter-spacing: 0.5px;
        border: none;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        transition: all 0.3s ease;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
        }

        &:last-child {
          margin-right: 0;
        }
      }
    }
  }
}
.right{
  padding: 60px;
}
</style>
