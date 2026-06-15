<template>
  <div class="app-container">
    <div class="study-material-container">
      <!-- 标题区域 -->
      <div class="page-header">
        <div class="header-content">
          <div class="title-section">
            <div class="title-wrapper">
              <h1 class="page-title">
                <i class="el-icon-folder-opened title-icon" />
                我的学习资料
              </h1>
              <p class="page-description">高效管理您的考研学习资料，支持PDF、Word、Excel格式文件上传与预览</p>
            </div>
          </div>
          <div v-if="hasStudentRole" class="action-section">
            <el-button
              type="primary"
              icon="el-icon-picture-outline"
              class="gallery-btn"
              @click="$router.push('gallery')"
            >
              <i class="el-icon-view" />
              浏览电子展馆
            </el-button>
          </div>
        </div>
      </div>

      <!-- 上传区域 -->
      <el-card class="upload-card" shadow="hover">
        <div slot="header" class="card-header">
          <div class="header-title">
            <i class="el-icon-upload header-icon" />
            <span>上传新资料</span>
          </div>
        </div>
        <div class="upload-area">
          <div class="upload-content">
            <el-upload
              ref="upload"
              class="upload-demo"
              action=""
              :auto-upload="false"
              :on-change="handleFileChange"
              :show-file-list="false"
              :disabled="uploading"
              accept=".pdf,.doc,.docx,.xls,.xlsx"
              drag
            >
              <div class="upload-drop-area">
                <i class="el-icon-upload upload-icon" />
                <div class="upload-text">
                  <p class="upload-main-text">点击或拖拽文件到此处上传</p>
                  <p class="upload-sub-text">支持 PDF、Word、Excel 格式，单个文件不超过200MB</p>
                </div>
              </div>
            </el-upload>

            <div v-if="selectedFile" class="selected-file-info">
              <div class="file-preview">
                <i :class="getFileIcon(selectedFile.name.split('.').pop())" class="file-preview-icon" />
                <div class="file-details">
                  <p class="file-name">{{ selectedFile.name }}</p>
                  <p class="file-size">{{ formatFileSize(selectedFile.size) }}</p>
                </div>
              </div>
              <div class="upload-actions">
                <el-button
                  type="success"
                  :loading="uploading"
                  size="medium"
                  class="upload-confirm-btn"
                  @click="handleManualUpload"
                >
                  <i class="el-icon-check" />
                  确认上传
                </el-button>
                <el-button
                  type="default"
                  size="medium"
                  class="upload-cancel-btn"
                  @click="selectedFile = null"
                >
                  <i class="el-icon-close" />
                  取消
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 资料列表区域 -->
      <el-card class="material-list-card" shadow="hover">
        <div slot="header" class="card-header">
          <div class="header-title">
            <i class="el-icon-document header-icon" />
            <span>资料列表</span>
            <span class="total-count">(共 {{ total }} 个文件)</span>
          </div>
          <div class="header-actions">
            <el-input
              v-model="searchFileName"
              placeholder="搜索文件名..."
              prefix-icon="el-icon-search"
              class="search-input"
              clearable
              @input="handleSearch"
            />
            <el-button
              type="primary"
              icon="el-icon-refresh"
              class="refresh-btn"
              @click="refreshList"
            >
              刷新
            </el-button>
          </div>
        </div>

        <!-- 资料表格 -->
        <div class="table-container">
          <el-table
            v-loading="loading"
            :data="materialList"
            :border="false"
            fit
            highlight-current-row
            class="material-table"
            :header-cell-style="{ background: '#f8fafc', color: '#475569', fontWeight: '600' }"
          >
            <el-table-column label="序号" width="80" align="center">
              <template slot-scope="scope">
                <span class="index-number">{{ scope.$index + 1 }}</span>
              </template>
            </el-table-column>

            <el-table-column label="文件信息" min-width="300">
              <template slot-scope="{ row }">
                <div class="file-info-wrapper">
                  <div class="file-icon-container">
                    <i :class="getFileIcon(row.fileType)" class="file-icon" />
                  </div>
                  <div class="file-details">
                    <span class="file-name" @click="previewMaterial(row.id, row.originalName)">
                      {{ row.originalName }}
                    </span>
                    <div class="file-meta">
                      <span class="upload-time">
                        <i class="el-icon-time" />
                        {{ formatTime(row.uploadTime) }}
                      </span>
                    </div>
                  </div>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="类型" width="100" align="center">
              <template slot-scope="{ row }">
                <el-tag
                  :type="getFileTypeColor(row.fileType)"
                  size="small"
                  class="file-type-tag"
                >
                  {{ row.fileType.toUpperCase() }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column label="大小" width="100" align="center">
              <template slot-scope="{ row }">
                <span class="file-size">{{ formatFileSize(row.fileSize) }}</span>
              </template>
            </el-table-column>

            <el-table-column label="进入方式" width="120" align="center">
              <template slot-scope="{ row }">
                <el-tag
                  :type="getEntryTypeColor(row.entryType)"
                  size="small"
                  class="entry-type-tag"
                >
                  {{ getEntryTypeText(row.entryType) }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column
              v-if="hasStudentRole"
              label="展馆状态"
              width="140"
              align="center"
            >
              <template slot-scope="{ row }">
                <div class="gallery-status">
                  <el-switch
                    v-model="row.showInGallery"
                    active-color="#10b981"
                    inactive-color="#9ca3af"
                    :active-text="row.showInGallery ? '公开' : '私有'"
                    @change="handleToggleGallery($event, row)"
                  />
                </div>
              </template>
            </el-table-column>

            <el-table-column
              label="解析状态"
              width="120"
              align="center"
            >
              <template slot-scope="{ row }">
                <el-tag :type="row.parseStatus === 'parsed' ? 'success' : 'info'" size="mini">
                  {{ row.parseStatus === 'parsed' ? '已解析' : '未解析' }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column
              label="操作"
              width="320"
              align="center"
              fixed="right"
            >
              <template slot-scope="{ row }">
                <div class="action-buttons">
                  <el-button
                    type="primary"
                    size="mini"
                    class="action-btn preview-btn"
                    @click="handlePreview(row)"
                  >
                    <i class="el-icon-view" />
                    预览
                  </el-button>
                  <el-button
                    type="success"
                    size="mini"
                    class="action-btn download-btn"
                    @click="handleDownload(row)"
                  >
                    <i class="el-icon-download" />
                    下载
                  </el-button>
                  <el-button
                    v-if="hasStudentRole"
                    type="warning"
                    size="mini"
                    class="action-btn parse-btn"
                    :loading="parsingMaterialIds.has(row.id)"
                    @click="handleParseMaterial(row)"
                  >
                    <i class="el-icon-upload2" />
                    解析
                  </el-button>
                  <el-button
                    v-if="!isAdminUser"
                    type="danger"
                    size="mini"
                    class="action-btn delete-btn"
                    @click="deleteMaterial(row.id)"
                  >
                    <i class="el-icon-delete" />
                    删除
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <!-- 空状态 -->
          <div v-if="!loading && materialList.length === 0" class="empty-state">
            <i class="el-icon-document empty-icon" />
            <p class="empty-text">暂无学习资料</p>
            <p class="empty-desc">点击上方上传区域添加您的第一份学习资料</p>
          </div>

          <!-- 分页 -->
          <div v-if="total > 0" class="pagination-container">
            <el-pagination
              :current-page="currentPage"
              :page-sizes="[10, 20, 50, 100]"
              :page-size="pageSize"
              layout="total, sizes, prev, pager, next, jumper"
              :total="total"
              class="custom-pagination"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import { studyMaterialUpload, studyMaterialPaging, studyMaterialDelete, studyMaterialDownload, studyMaterialPreview, studyMaterialSetGalleryPermission } from '@/api/study-material'
import { parseMaterial, getLatestTaskByMaterial } from '@/api/rag'

export default {
  name: 'StudyMaterial',
  data() {
    return {
      // 上传相关
      uploading: false,
      selectedFile: null,

      // 列表相关
      materialList: [],
      loading: false,
      searchFileName: '',
      parsingMaterialIds: new Set(),

      // 分页相关
      currentPage: 1,
      pageSize: 10,
      total: 0
    }
  },
  computed: {
    hasStudentRole() {
      const roles = this.$store?.getters?.roles || []
      return roles.includes('student') || roles.includes('role_student')
    },
    hasTeacherOrAdminRole() {
      const roles = this.$store?.getters?.roles || []
      return roles.includes('teacher') || roles.includes('admin') || roles.includes('role_teacher') || roles.includes('role_admin')
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
    this.getMaterialList()

    // 监听全局刷新事件
    this.$root.$on('refresh-material-list', this.getMaterialList)
  },

  beforeDestroy() {
    // 清理监听器
    this.$root.$off('refresh-material-list', this.getMaterialList)
  },
  methods: {
    // 获取资料列表
    async getMaterialList() {
      this.loading = true
      try {
        const params = {
          pageNum: this.currentPage,
          pageSize: this.pageSize,
          fileName: this.searchFileName
        }
        const res = await studyMaterialPaging(params)
        if (res.code && res.data) {
          this.materialList = res.data.records
          this.total = res.data.total

          // 为每个资料设置进入方式
          this.materialList.forEach(material => {
            material.entryType = this.determineEntryType(material)
            material.parseStatus = 'unparsed'
          })

          // 如果有学生角色，检查解析状态
          if (this.hasStudentRole) {
            const statusPromises = this.materialList.map(async(material) => {
              try {
                const taskRes = await getLatestTaskByMaterial(material.id)
                if (taskRes.code === 1 && taskRes.data) {
                  const status = taskRes.data.taskStatus === 2 ? 'parsed' : 'parsing'
                  this.$set(material, 'parseStatus', status)
                }
              } catch (error) {
                // 忽略错误，保持默认状态
              }
            })
            await Promise.all(statusPromises)
          }
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

    // 文件选择处理
    handleFileChange(file) {
      const allowedTypes = ['application/pdf', 'application/msword',
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
        'application/vnd.ms-excel',
        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet']
      const isAllowedType = allowedTypes.includes(file.raw.type)
      const isLt200M = file.size / 1024 / 1024 < 200

      if (!isAllowedType) {
        this.$message.error('只支持上传 PDF、Word、Excel 格式文件!')
        return false
      }
      if (!isLt200M) {
        this.$message.error('文件大小不能超过 200MB!')
        return false
      }

      this.selectedFile = file.raw
    },

    // 手动上传文件
    async handleManualUpload() {
      if (!this.selectedFile) {
        this.$message.warning('请先选择文件')
        return
      }

      this.uploading = true

      try {
        const formData = new FormData()
        formData.append('file', this.selectedFile)

        const res = await studyMaterialUpload(formData)

        if (res.code) {
          this.$message.success('文件上传成功')
          this.selectedFile = null

          // 刷新列表
          setTimeout(() => {
            this.getMaterialList()
          }, 500)
        } else {
          this.$message.error('文件上传失败：' + res.msg)
        }
      } catch (error) {
        console.error('文件上传失败:', error)
        this.$message.error('文件上传失败：' + (error.response?.data?.msg || error.message))
      } finally {
        this.uploading = false
      }
    },

    // 预览文件
    async previewMaterial(id, fileName = '') {
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
      }
    },

    // 下载文件
    async downloadMaterial(id) {
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
      }
    },

    // 删除文件
    async deleteMaterial(id) {
      try {
        await this.$confirm('确定要删除这个文件吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        const res = await studyMaterialDelete(id)
        if (res.code) {
          this.$message.success('文件删除成功')
          this.getMaterialList()
        } else {
          this.$message.error('文件删除失败：' + res.msg)
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('文件删除失败:', error)
          this.$message.error('文件删除失败')
        }
      }
    },

    // 解析学习资料
    async handleParseMaterial(material) {
      if (this.parsingMaterialIds.has(material.id)) {
        this.$message.warning('该资料正在解析中，请稍候')
        return
      }

      try {
        await this.$confirm('确定要解析该学习资料吗？解析后的资料将被AI学习助手使用。', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        })

        this.parsingMaterialIds.add(material.id)
        this.$message.info('解析任务已提交，正在后台处理...')

        const res = await parseMaterial(material.id)
        if (res.code === 1) {
          this.$message.success('解析任务已提交，您可以在"RAG智能解析中心"查看进度')
          // 跳转到RAG页面
          this.$router.push('/rag-report')
        } else {
          this.$message.error('提交解析任务失败：' + res.msg)
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('解析资料失败:', error)
          this.$message.error('提交解析任务失败')
        }
      } finally {
        this.parsingMaterialIds.delete(material.id)
      }
    },

    // 切换电子展馆展示状态
    async handleToggleGallery(newStatus, row) {
      const originalStatus = row.showInGallery
      const action = newStatus ? '展示' : '隐藏'

      try {
        await this.$confirm(`确定${action}该文件在电子展馆中吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        const res = await studyMaterialSetGalleryPermission(row.id, newStatus)
        if (res.code) {
          this.$message.success(`${action}成功`)
          this.$set(row, 'showInGallery', newStatus)
        } else {
          this.$message.error(res.msg || `${action}失败`)
        }
      } catch (error) {
        const isCancelled = error === 'cancel' || error === 'close'
        if (!isCancelled) {
          console.error('切换展馆状态失败:', error)
          this.$message.error('切换展馆状态失败')
        }
        this.$set(row, 'showInGallery', originalStatus)
      }
    },

    // 搜索
    handleSearch() {
      this.currentPage = 1
      this.getMaterialList()
    },

    // 刷新列表
    refreshList() {
      this.searchFileName = ''
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
      const type = fileType.toLowerCase()
      switch (type) {
        case 'pdf':
          return 'el-icon-document'
        case 'doc':
        case 'docx':
          return 'el-icon-document'
        case 'xls':
        case 'xlsx':
          return 'el-icon-document'
        default:
          return 'el-icon-document'
      }
    },

    // 获取文件类型颜色
    getFileTypeColor(fileType) {
      switch (fileType.toLowerCase()) {
        case 'pdf':
          return 'danger'
        case 'doc':
        case 'docx':
          return 'primary'
        case 'xls':
        case 'xlsx':
          return 'success'
        default:
          return 'info'
      }
    },

    // 获取进入方式对应的颜色
    getEntryTypeColor(entryType) {
      const typeMap = {
        'upload': 'primary',
        'collect': 'success',
        'share': 'warning'
      }
      return typeMap[entryType] || 'info'
    },

    // 获取进入方式的显示文本
    getEntryTypeText(entryType) {
      const textMap = {
        'upload': '直接上传',
        'collect': '收藏导入',
        'share': '分享获得'
      }
      return textMap[entryType] || '未知'
    },

    // 判断资料的进入方式
    determineEntryType(material) {
      // 这里需要根据资料的具体属性来判断进入方式
      // 目前先返回默认值，后续需要根据实际数据结构调整
      return 'upload' // 默认为直接上传
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
.study-material-container {
  padding: 32px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 50%, #e2e8f0 100%);
  min-height: calc(100vh - 84px);
}

/* 页面标题区域 */
.page-header {
  margin-bottom: 32px;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-wrapper {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.title-section .page-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  background: linear-gradient(135deg, #3b82f6 0%, #10b981 50%, #8b5cf6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.title-icon {
  font-size: 36px;
  color: #3b82f6;
}

.title-section .page-description {
  font-size: 16px;
  color: #64748b;
  line-height: 1.6;
  margin: 0;
  font-weight: 400;
}

.action-section {
  display: flex;
  gap: 16px;
}

.gallery-btn {
  background: linear-gradient(135deg, #8b5cf6 0%, #a855f7 100%);
  border: none;
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.3);
  transition: all 0.3s ease;
}

.gallery-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(139, 92, 246, 0.4);
}

/* 卡片样式 */
.upload-card, .material-list-card {
  margin-bottom: 32px;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  transition: all 0.3s ease;
}

.upload-card:hover, .material-list-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
}

.upload-card >>> .el-card__header, .material-list-card >>> .el-card__header {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-bottom: 1px solid #e2e8f0;
  padding: 24px 32px;
  border-radius: 16px 16px 0 0;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.header-icon {
  font-size: 20px;
  color: #3b82f6;
}

.total-count {
  font-size: 14px;
  color: #64748b;
  font-weight: 400;
  margin-left: 8px;
}

/* 上传区域 */
.upload-area {
  padding: 40px 32px;
  background: #fafbfc;
  border-radius: 0 0 16px 16px;
  box-sizing: border-box;
}

.upload-content {
  width: 100%;
  margin: 0 auto;
  box-sizing: border-box;
}

/* Element UI 上传组件样式覆盖 */
.upload-demo >>> .el-upload {
  width: 100% !important;
  display: block !important;
}

.upload-demo >>> .el-upload-dragger {
  width: 100% !important;
  height: auto !important;
  min-height: 120px !important;
  border: none !important;
  background: transparent !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  padding: 0 !important;
  margin: 0 !important;
}

.upload-demo >>> .el-upload-dragger:hover {
  border: none !important;
  background: transparent !important;
}

.upload-drop-area {
  padding: 40px 30px;
  border: 2px dashed #d1d5db;
  border-radius: 12px;
  background: #ffffff;
  transition: all 0.3s ease;
  cursor: pointer;
  min-height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.upload-drop-area:hover {
  border-color: #3b82f6;
  background: #f8fafc;
}

.upload-icon {
  font-size: 48px;
  color: #9ca3af;
  margin-bottom: 16px;
}

.upload-text {
  text-align: center;
}

.upload-main-text {
  font-size: 18px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
}

.upload-sub-text {
  font-size: 14px;
  color: #6b7280;
  line-height: 1.5;
}

/* 已选择文件信息 */
.selected-file-info {
  margin-top: 24px;
  padding: 24px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 12px;
  border: 1px dashed #7dd3fc;
}

.file-preview {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.file-preview-icon {
  font-size: 40px;
  color: #3b82f6;
}

.file-details {
  flex: 1;
}

.file-name {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
}

.file-size {
  font-size: 14px;
  color: #64748b;
}

.upload-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.upload-confirm-btn {
  background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
  border: none;
  border-radius: 8px;
  padding: 10px 24px;
  font-weight: 600;
}

.upload-cancel-btn {
  border-radius: 8px;
  padding: 10px 24px;
  font-weight: 600;
}

/* 搜索和操作区域 */
.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-input {
  width: 280px;
}

.search-input >>> .el-input__inner {
  border-radius: 8px;
  border: 1px solid #d1d5db;
  transition: all 0.3s ease;
}

.search-input >>> .el-input__inner:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.refresh-btn {
  border-radius: 8px;
  padding: 10px 20px;
  font-weight: 600;
}

/* 表格容器 */
.table-container {
  padding: 0 8px 24px;
}

.material-table {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.material-table >>> .el-table__header-wrapper {
  border-radius: 12px 12px 0 0;
}

.material-table >>> .el-table__body-wrapper {
  border-radius: 0 0 12px 12px;
}

/* 表格内容样式 */
.index-number {
  display: inline-block;
  width: 32px;
  height: 32px;
  line-height: 32px;
  text-align: center;
  background: #f1f5f9;
  border-radius: 50%;
  color: #475569;
  font-weight: 600;
  font-size: 14px;
}

.file-info-wrapper {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 0;
}

.file-icon-container {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  border-radius: 12px;
}

.file-icon {
  font-size: 24px;
  color: #3b82f6;
}

.file-details {
  flex: 1;
  min-width: 0;
}

.file-name {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  cursor: pointer;
  transition: color 0.3s ease;
  margin-bottom: 4px;
  line-height: 1.4;
  word-break: break-all;
}

.file-name:hover {
  color: #3b82f6;
  text-decoration: underline;
}

.file-meta {
  display: flex;
  align-items: center;
  gap: 16px;
}

.upload-time {
  font-size: 12px;
  color: #64748b;
  display: flex;
  align-items: center;
  gap: 4px;
}

.file-type-tag {
  font-weight: 600;
  border-radius: 6px;
  padding: 4px 8px;
}

.file-size {
  font-size: 14px;
  font-weight: 600;
  color: #475569;
}

.gallery-status {
  display: flex;
  justify-content: center;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.action-btn {
  border-radius: 6px;
  font-weight: 600;
  padding: 8px 12px;
  transition: all 0.3s ease;
}

.action-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.preview-btn {
  background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
  border: none;
}

.download-btn {
  background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
  border: none;
}

.delete-btn {
    background: linear-gradient(135deg, #ef4444 0%, #f87171 100%);
    border: none;
  }

.parse-btn {
    background: linear-gradient(135deg, #e6a23c 0%, #f0c78a 100%);
    border: none;
  }

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 40px;
  color: #64748b;
}

.empty-icon {
  font-size: 64px;
  color: #cbd5e1;
  margin-bottom: 24px;
}

.empty-text {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #475569;
}

.empty-desc {
  font-size: 14px;
  color: #94a3b8;
}

/* 分页 */
.pagination-container {
  padding: 24px 0 0;
  text-align: center;
}

.custom-pagination >>> .el-pagination {
  justify-content: center;
}

.custom-pagination >>> .el-pagination .btn-prev,
.custom-pagination >>> .el-pagination .btn-next,
.custom-pagination >>> .el-pagination .number {
  border-radius: 8px;
  margin: 0 4px;
}

.custom-pagination >>> .el-pagination .active {
  background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
  border-color: #3b82f6;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .study-material-container {
    padding: 16px;
  }

  .page-header {
    padding: 24px 20px;
    margin-bottom: 24px;
  }

  .header-content {
    flex-direction: column;
    gap: 20px;
    align-items: stretch;
  }

  .title-section .page-title {
    font-size: 24px;
    justify-content: center;
    text-align: center;
  }

  .title-section .page-description {
    text-align: center;
    font-size: 14px;
  }

  .action-section {
    justify-content: center;
  }

  .upload-area {
    padding: 24px 16px;
  }

  .upload-drop-area {
    padding: 30px 20px;
    min-height: 100px;
  }

  .header-actions {
    flex-direction: column;
    gap: 12px;
  }

  .search-input {
    width: 100% !important;
  }

  .action-buttons {
    flex-direction: column;
    gap: 6px;
  }

  .action-btn {
    width: 100%;
  }

  .file-info-wrapper {
    gap: 12px;
  }

  .file-icon-container {
    width: 40px;
    height: 40px;
  }

  .file-icon {
    font-size: 20px;
  }
}

@media (max-width: 480px) {
  .study-material-container {
    padding: 12px;
  }

  .page-header {
    padding: 20px 16px;
  }

  .upload-area {
    padding: 20px 12px;
  }

  .upload-drop-area {
    padding: 30px 16px;
  }

  .upload-main-text {
    font-size: 16px;
    text-align: center;
    line-height: 1.4;
  }

  .upload-sub-text {
    font-size: 13px;
    text-align: center;
    line-height: 1.4;
  }
}
</style>
