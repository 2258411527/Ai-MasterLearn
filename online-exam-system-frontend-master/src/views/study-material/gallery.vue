<template>
  <div class="app-container">
    <div class="gallery-container">
      <!-- 标题区域 -->
      <div class="page-header">
        <div class="header-content">
          <div class="title-section">
            <h2 class="page-title">电子展馆</h2>
            <p class="page-description">浏览和选择管理员、教师上传的学习资料，支持PDF、Word、Excel格式</p>
          </div>
        </div>
      </div>

      <!-- 搜索区域 -->
      <el-card class="search-card" shadow="never">
        <div class="search-area">
          <el-input
            v-model="searchFileName"
            placeholder="搜索文件名"
            prefix-icon="el-icon-search"
            style="width: 200px; margin-right: 10px;"
            @input="handleSearch"
          />
          <el-input
            v-model="searchUploaderName"
            placeholder="搜索上传者"
            prefix-icon="el-icon-user"
            style="width: 200px; margin-right: 10px;"
            @input="handleSearch"
          />
          <el-button type="primary" @click="refreshList">
            <i class="el-icon-refresh" />
            刷新
          </el-button>
          <el-button
            v-if="hasAdminRole"
            type="danger"
            :loading="clearLoading"
            style="margin-left: 10px;"
            @click="handleClearGallery"
          >
            <i class="el-icon-delete" />
            清空展馆
          </el-button>
        </div>
      </el-card>

      <!-- 资料展示区域 -->
      <div class="material-gallery">
        <div v-loading="loading" class="gallery-grid">
          <div
            v-for="material in materialList"
            :key="material.id"
            class="gallery-item"
          >
            <el-card shadow="hover" class="material-card">
              <div class="card-content">
                <!-- 文件图标 -->
                <div class="file-icon-container">
                  <i :class="getFileIcon(material.fileType)" class="file-icon-large" />
                </div>

                <!-- 文件信息 -->
                <div class="file-info">
                  <div class="file-header">
                    <h4 class="file-name" :title="material.originalName">
                      {{ material.originalName }}
                    </h4>
                    <el-tooltip
                      v-if="material.userId !== currentUserId"
                      :content="material.isCollected ? '已收藏' : '点击收藏'"
                      placement="top"
                    >
                      <i
                        :class="['collect-icon', material.isCollected ? 'el-icon-star-on collected' : 'el-icon-star-off']"
                        @click="toggleCollect(material)"
                      />
                    </el-tooltip>
                  </div>
                  <p class="file-meta">
                    <span class="file-type">{{ material.fileType.toUpperCase() }}</span>
                    <span class="file-size">{{ formatFileSize(material.fileSize) }}</span>
                  </p>
                  <p class="uploader-info">
                    <i class="el-icon-user" />
                    {{ material.userName || '未知用户' }}
                  </p>
                  <p class="upload-time">
                    <i class="el-icon-time" />
                    {{ formatTime(material.uploadTime) }}
                  </p>
                </div>

                <!-- 操作按钮 -->
                <div class="action-buttons">
                  <div class="button-group">
                    <el-button
                      type="primary"
                      size="small"
                      :loading="previewLoading === material.id"
                      @click="previewMaterial(material.id, material.originalName)"
                    >
                      <i class="el-icon-view" />
                      预览
                    </el-button>
                    <el-button
                      type="success"
                      size="small"
                      :loading="downloadLoading === material.id"
                      @click="downloadMaterial(material.id)"
                    >
                      <i class="el-icon-download" />
                      下载
                    </el-button>
                  </div>
                  <div class="right-buttons">
                    <!-- 收藏按钮 -->
                    <el-button
                      v-if="material.userId !== currentUserId"
                      class="collect-btn"
                      :type="material.isCollected ? 'warning' : 'default'"
                      size="small"
                      :loading="addLoading === material.id"
                      @click="toggleCollect(material)"
                    >
                      <i :class="material.isCollected ? 'el-icon-star-on' : 'el-icon-star-off'" />
                      {{ material.isCollected ? '已收藏' : '收藏' }}
                    </el-button>

                    <!-- 删除按钮 -->
                    <el-button
                      v-if="canDeleteMaterial(material)"
                      type="danger"
                      size="small"
                      :loading="deleteLoading === material.id"
                      class="delete-btn"
                      @click="handleDelete(material)"
                    >
                      <i class="el-icon-delete" />
                      删除
                    </el-button>
                  </div>
                </div>
              </div>
            </el-card>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="!loading && materialList.length === 0" class="empty-state">
          <i class="el-icon-document" style="font-size: 64px; color: #c0c4cc;" />
          <p style="margin-top: 16px; color: #909399;">暂无资料</p>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="total > 0" class="pagination-container">
        <el-pagination
          :current-page="currentPage"
          :page-sizes="[12, 24, 48, 96]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { studyMaterialGallery, studyMaterialDownload, studyMaterialPreview, studyMaterialAddToLibrary, studyMaterialRemoveFromLibrary, studyMaterialDelete } from '@/api/study-material'
import { getUserId } from '@/utils/auth'

export default {
  name: 'StudyMaterialGallery',
  data() {
    return {
      // 列表相关
      materialList: [],
      loading: false,
      searchFileName: '',
      searchUploaderName: '',

      // 操作加载状态
      previewLoading: null,
      downloadLoading: null,
      addLoading: null,
      deleteLoading: null,
      clearLoading: false,

      // 当前用户 ID
      currentUserId: null,

      // 分页相关
      currentPage: 1,
      pageSize: 12,
      total: 0
    }
  },
  computed: {
    hasStudentRole() {
      // 从多个可能的位置获取角色信息
      const storeRoles = this.$store?.getters?.roles || []
      const localStorageRoles = localStorage.getItem('roles') || ''
      const allRoles = [...storeRoles, ...localStorageRoles.split(',')].filter(Boolean)

      console.log('所有角色信息:', allRoles)

      return allRoles.includes('student') ||
             allRoles.includes('role_student') ||
             allRoles.includes('ROLE_STUDENT')
    },

    hasAdminRole() {
      // 从多个可能的位置获取角色信息
      const storeRoles = this.$store?.getters?.roles || []
      const localStorageRoles = localStorage.getItem('roles') || ''
      const allRoles = [...storeRoles, ...localStorageRoles.split(',')].filter(Boolean)

      return allRoles.includes('admin') ||
             allRoles.includes('role_admin') ||
             allRoles.includes('ROLE_ADMIN')
    },

    isAdminUser() {
      // 检查是否是admin用户（用户名或角色包含"admin"）
      const username = this.$store?.getters?.name || localStorage.getItem('username') || ''
      const roles = this.$store?.getters?.roles || []

      return username.toLowerCase().includes('admin') ||
             roles.some(role => role.toLowerCase().includes('admin'))
    }
  },
  mounted() {
    this.currentUserId = parseInt(getUserId() || '', 10)
    console.log('当前用户ID:', this.currentUserId)
    console.log('用户角色:', this.$store?.getters?.roles)
    console.log('是否有管理员权限:', this.hasAdminRole)
    console.log('是否有学生权限:', this.hasStudentRole)
    this.getMaterialList()
  },
  methods: {
    // 获取资料列表
    async getMaterialList() {
      this.loading = true
      try {
        const params = {
          pageNum: this.currentPage,
          pageSize: this.pageSize,
          fileName: this.searchFileName,
          uploaderName: this.searchUploaderName
        }
        const res = await studyMaterialGallery(params)
        if (res.code && res.data) {
          this.materialList = res.data.records
          this.total = res.data.total

          // 初始化收藏状态
          this.initCollectionStates()
        } else {
          this.$message.error('获取资料列表失败')
        }
      } catch (error) {
        console.error('获取资料列表失败:', error)
        this.$message.error('获取资料列表失败')
      } finally {
        this.loading = false
      }
    },

    // 初始化收藏状态
    initCollectionStates() {
      this.materialList.forEach(material => {
        // 设置收藏状态
        material.isCollected = this.loadCollectionState(material.id)
      })
    },

    // 预览文件
    async previewMaterial(id, fileName = '') {
      this.previewLoading = id
      try {
        console.log('开始预览文件，ID:', id, '文件名:', fileName)

        // 检查文件类型是否支持预览
        const fileExtension = fileName.split('.').pop().toLowerCase()
        const previewSupported = ['pdf'].includes(fileExtension)

        if (!previewSupported) {
          // 不支持预览的文件类型，直接下载
          this.$message.info('该文件类型不支持预览，将直接下载')
          await this.downloadMaterial(id)
          return
        }

        const response = await studyMaterialPreview(id)
        console.log('预览API响应:', response)

        if (!response.data) {
          throw new Error('响应数据为空')
        }

        // 创建Blob对象
        const blob = new Blob([response.data])
        const url = URL.createObjectURL(blob)

        // 在新窗口打开预览
        const previewWindow = window.open(url, '_blank')

        // 如果新窗口被阻止，提示用户手动打开
        if (!previewWindow || previewWindow.closed || typeof previewWindow.closed === 'undefined') {
          this.$message.warning('弹出窗口被阻止，请允许弹出窗口或手动打开链接')
          // 提供下载选项
          const downloadLink = document.createElement('a')
          downloadLink.href = url
          downloadLink.download = fileName || 'preview-file'
          downloadLink.click()
        }

        // 清理URL对象
        setTimeout(() => URL.revokeObjectURL(url), 1000)
        this.$message.success('文件预览成功')
      } catch (error) {
        console.error('文件预览失败详情:', error)
        console.error('错误响应:', error.response)
        this.$message.error('文件预览失败：' + (error.response?.data?.msg || error.message || '未知错误'))
      } finally {
        this.previewLoading = null
      }
    },

    // 下载文件
    async downloadMaterial(id) {
      this.downloadLoading = id
      try {
        console.log('开始下载文件，ID:', id)
        const response = await studyMaterialDownload(id)
        console.log('下载API响应:', response)

        if (!response.data) {
          throw new Error('响应数据为空')
        }

        // 创建Blob对象
        const blob = new Blob([response.data])
        const url = URL.createObjectURL(blob)
        const link = document.createElement('a')

        // 从响应头获取文件名
        const contentDisposition = response.headers['content-disposition']
        let filename = 'download'
        if (contentDisposition) {
          const filenameMatch = contentDisposition.match(/filename="?(.+)"?/)
          if (filenameMatch) {
            filename = filenameMatch[1]
          }
        }

        link.href = url
        link.download = filename
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)

        // 延迟清理URL对象，确保下载完成
        setTimeout(() => URL.revokeObjectURL(url), 1000)

        this.$message.success('文件下载开始')
      } catch (error) {
        console.error('文件下载失败详情:', error)
        console.error('错误响应:', error.response)
        this.$message.error('文件下载失败：' + (error.response?.data?.msg || error.message || '未知错误'))
      } finally {
        this.downloadLoading = null
      }
    },

    // 添加到个人资料库
    async addToLibrary(id) {
      this.addLoading = id
      try {
        const res = await studyMaterialAddToLibrary(id)
        if (res.code) {
          this.$message.success('已成功添加到资料库')
          // 更新收藏状态
          const material = this.materialList.find(item => item.id === id)
          if (material) {
            material.isCollected = true
            // 保存到本地存储
            this.saveCollectionState(id, true)
          }
        } else {
          this.$message.error('添加到资料库失败：' + res.msg)
        }
      } catch (error) {
        console.error('添加到资料库失败:', error)
        this.$message.error('添加到资料库失败')
      } finally {
        this.addLoading = null
      }
    },

    // 切换收藏状态
    async toggleCollect(material) {
      if (material.isCollected) {
        await this.removeFromLibrary(material.id)
      } else {
        await this.addToLibrary(material.id)
      }
    },

    // 从资料库移除
    async removeFromLibrary(id) {
      this.addLoading = id
      try {
        // 调用后端接口从资料库移除文件
        const res = await studyMaterialRemoveFromLibrary(id)
        if (res.code) {
          this.$message.success('已从资料库移除')
          // 更新收藏状态
          const material = this.materialList.find(item => item.id === id)
          if (material) {
            material.isCollected = false
            // 同时移除本地存储的收藏状态
            this.saveCollectionState(id, false)
          }
          // 通知"我的资料"页面刷新列表
          this.notifyMaterialListRefresh()
        } else {
          this.$message.error('移除失败：' + res.msg)
        }
      } catch (error) {
        console.error('移除失败:', error)
        this.$message.error('移除失败')
      } finally {
        this.addLoading = null
      }
    },

    // 通知"我的资料"页面刷新列表
    notifyMaterialListRefresh() {
      // 简单高效的方法：直接检查当前路由并刷新
      try {
        // 如果当前路由是"我的资料"页面，直接刷新
        if (this.$route.path.includes('/study-material') && !this.$route.path.includes('gallery')) {
          // 当前就在"我的资料"页面，直接调用刷新方法
          this.$root.$emit('refresh-material-list')
        }
      } catch (error) {
        console.error('通知刷新失败:', error)
      }
    },

    // 保存收藏状态到本地存储
    saveCollectionState(materialId, isCollected) {
      try {
        const userId = this.currentUserId
        if (!userId) return

        const storageKey = `user_${userId}_collections`
        const collections = JSON.parse(localStorage.getItem(storageKey) || '{}')

        if (isCollected) {
          collections[materialId] = true
        } else {
          delete collections[materialId]
        }

        localStorage.setItem(storageKey, JSON.stringify(collections))
      } catch (error) {
        console.error('保存收藏状态失败:', error)
      }
    },

    // 从本地存储加载收藏状态
    loadCollectionState(materialId) {
      try {
        const userId = this.currentUserId
        if (!userId) return false

        const storageKey = `user_${userId}_collections`
        const collections = JSON.parse(localStorage.getItem(storageKey) || '{}')

        return !!collections[materialId]
      } catch (error) {
        console.error('加载收藏状态失败:', error)
        return false
      }
    },

    // 判断是否可以删除资料
    canDeleteMaterial(material) {
      // 管理员可以删除所有资料
      if (this.hasAdminRole) {
        return true
      }
      // 用户只能删除自己上传的资料
      if (material.userId === this.currentUserId) {
        return true
      }
      return false
    },

    // 处理删除操作
    async handleDelete(material) {
      this.$confirm(`确定要删除资料"${material.originalName}"吗？此操作不可恢复。`, '确认删除', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async() => {
        this.deleteLoading = material.id
        try {
          const res = await studyMaterialDelete(material.id)
          if (res.code) {
            this.$message.success('删除成功')
            // 从列表中移除
            this.materialList = this.materialList.filter(item => item.id !== material.id)
            // 更新总数
            this.total -= 1
          } else {
            this.$message.error('删除失败：' + res.msg)
          }
        } catch (error) {
          console.error('删除失败:', error)
          this.$message.error('删除失败')
        } finally {
          this.deleteLoading = null
        }
      }).catch(() => {
        this.$message.info('已取消删除')
      })
    },

    // 清空电子展馆
    async handleClearGallery() {
      if (this.materialList.length === 0) {
        this.$message.warning('电子展馆中暂无资料')
        return
      }

      this.$confirm(`确定要清空电子展馆吗？这将删除所有 ${this.total} 个资料，此操作不可恢复！`, '确认清空', {
        confirmButtonText: '确定清空',
        cancelButtonText: '取消',
        type: 'error',
        dangerouslyUseHTMLString: true,
        customClass: 'clear-confirm-dialog'
      }).then(async() => {
        this.clearLoading = true
        try {
          // 获取所有资料的ID
          const materialIds = this.materialList.map(item => item.id)

          // 批量删除所有资料
          let successCount = 0
          let errorCount = 0

          for (const id of materialIds) {
            try {
              const res = await studyMaterialDelete(id)
              if (res.code) {
                successCount++
              } else {
                errorCount++
                console.error(`删除资料 ${id} 失败：`, res.msg)
              }
            } catch (error) {
              errorCount++
              console.error(`删除资料 ${id} 失败：`, error)
            }

            // 添加小延迟，避免请求过于频繁
            await new Promise(resolve => setTimeout(resolve, 100))
          }

          if (errorCount === 0) {
            this.$message.success(`清空成功！已删除 ${successCount} 个资料`)
          } else {
            this.$message.warning(`清空完成！成功删除 ${successCount} 个资料，失败 ${errorCount} 个`)
          }

          // 刷新列表
          this.getMaterialList()
        } catch (error) {
          console.error('清空电子展馆失败:', error)
          this.$message.error('清空失败：' + (error.message || '未知错误'))
        } finally {
          this.clearLoading = false
        }
      }).catch(() => {
        this.$message.info('已取消清空')
      })
    },

    // 搜索
    handleSearch() {
      this.currentPage = 1
      this.getMaterialList()
    },

    // 刷新列表
    refreshList() {
      this.searchFileName = ''
      this.searchUploaderName = ''
      this.currentPage = 1
      this.getMaterialList()
    },

    // 分页大小改变
    handleSizeChange(val) {
      this.pageSize = val
      this.getMaterialList()
    },

    // 当前页改变
    handleCurrentChange(val) {
      this.currentPage = val
      this.getMaterialList()
    },

    // 获取文件图标
    getFileIcon(fileType) {
      switch (fileType.toLowerCase()) {
        case 'pdf':
          return 'el-icon-document' // PDF图标
        case 'doc':
        case 'docx':
          return 'el-icon-document' // Word图标
        case 'xls':
        case 'xlsx':
          return 'el-icon-document' // Excel图标
        default:
          return 'el-icon-document' // 默认图标
      }
    },

    // 格式化文件大小
    formatFileSize(bytes) {
      if (bytes === 0) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    },

    // 格式化时间
    formatTime(time) {
      if (!time) return ''
      const date = new Date(time)
      return date.toLocaleString('zh-CN')
    }
  }
}
</script>

<style scoped>
.gallery-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
}

.page-description {
  font-size: 14px;
  color: #909399;
}

.search-card {
  margin-bottom: 20px;
  border: none;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-radius: 8px;
}

.search-area {
  display: flex;
  align-items: center;
  justify-content: center;
}

.material-gallery {
  margin-bottom: 20px;
}

.gallery-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.gallery-item {
  transition: transform 0.3s ease;
}

.gallery-item:hover {
  transform: translateY(-4px);
}

.material-card {
  border: 1px solid #f0f2f5;
  border-radius: 12px;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

.material-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #409eff, #67c23a, #e6a23c);
  opacity: 0.7;
  transition: opacity 0.3s ease;
}

.material-card:hover {
  border-color: #e4e7ed;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.material-card:hover::before {
  opacity: 1;
}

.card-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 16px;
}

.file-icon-container {
  text-align: center;
  margin-bottom: 16px;
}

.file-icon-large {
  font-size: 48px;
  color: #409eff;
  opacity: 0.9;
  transition: all 0.3s ease;
}

.material-card:hover .file-icon-large {
  opacity: 1;
  transform: scale(1.05);
}

.file-info {
  text-align: center;
  margin-bottom: 16px;
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}

.file-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
  min-height: 40px;
}

.file-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
  flex: 1;
  margin-right: 8px;
  text-align: left;
  word-break: break-word;
  transition: color 0.3s ease;
}

.material-card:hover .file-name {
  color: #409eff;
}

.collect-icon {
  font-size: 18px;
  color: #e6a23c;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 2px;
  flex-shrink: 0;
}

.collect-icon:hover {
  color: #f56c6c;
  transform: scale(1.2);
}

.collect-icon.collected {
  color: #f56c6c;
}

.file-meta, .uploader-info, .upload-time {
  margin-bottom: 6px;
  line-height: 1.3;
}

.file-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.file-type {
  background: linear-gradient(135deg, #409eff, #67c23a);
  color: white;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 500;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.file-size {
  color: #6b7280;
  font-size: 12px;
  font-weight: 500;
}

.uploader-info, .upload-time {
  color: #6b7280;
  font-size: 12px;
  margin-bottom: 4px;
  opacity: 0.8;
}

.uploader-info i, .upload-time i {
  margin-right: 4px;
  opacity: 0.7;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: auto;
  min-height: 80px;
  justify-content: flex-end;
}

.button-group {
  display: flex;
  gap: 8px;
  justify-content: space-between;
}

.button-group .el-button {
  flex: 1;
  min-width: 0;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.right-buttons {
  display: flex;
  gap: 8px;
  flex-direction: column;
}

.right-buttons .el-button {
  width: 100%;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.collect-btn {
  width: 100%;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.delete-btn {
  background: linear-gradient(135deg, #f56c6c, #f78989) !important;
  border-color: #f56c6c !important;
}

.delete-btn:hover {
  background: linear-gradient(135deg, #f78989, #f9a7a7) !important;
  border-color: #f78989 !important;
}

.collect-btn .el-icon-star-on {
  color: #f56c6c;
}

.collect-btn .el-icon-star-off {
  color: #e6a23c;
}

.action-buttons .el-button {
  margin: 0;
  padding: 8px 12px;
  font-size: 12px;
  border: 1px solid transparent;
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.action-buttons .el-button--primary {
  background: linear-gradient(135deg, #409eff, #66b1ff);
  border-color: #409eff;
}

.action-buttons .el-button--success {
  background: linear-gradient(135deg, #67c23a, #85ce61);
  border-color: #67c23a;
}

.action-buttons .el-button--warning {
  background: linear-gradient(135deg, #e6a23c, #ebb563);
  border-color: #e6a23c;
}

.action-buttons .el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.empty-state {
  text-align: center;
  padding: 60px 0;
}

.pagination-container {
  text-align: center;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .gallery-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 16px;
  }

  .search-area {
    flex-direction: column;
    gap: 10px;
  }

  .search-area .el-input {
    width: 100% !important;
    margin-right: 0 !important;
  }
}
</style>
