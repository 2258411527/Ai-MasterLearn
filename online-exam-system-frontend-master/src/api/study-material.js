import request from '@/utils/request'

/**
 * 上传学习资料
 * @param {FormData} formData - 包含文件的FormData对象
 */
export function studyMaterialUpload(formData) {
  return request({
    url: 'study-material/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取学习资料列表
 */
export function studyMaterialList() {
  return request({
    url: 'study-material',
    method: 'get'
  })
}

/**
 * 分页获取学习资料
 */
export function studyMaterialPaging(params) {
  return request({
    url: 'study-material/paging',
    method: 'get',
    params
  })
}

/**
 * 删除学习资料
 */
export function studyMaterialDelete(id) {
  return request({
    url: 'study-material/' + id,
    method: 'delete'
  })
}

/**
 * 下载学习资料
 */
export function studyMaterialDownload(id) {
  return request({
    url: 'study-material/download/' + id,
    method: 'get',
    responseType: 'blob',
    skipErrorHandler: true // 跳过错误处理，直接返回二进制流
  })
}

/**
 * 预览学习资料
 */
export function studyMaterialPreview(id) {
  return request({
    url: 'study-material/preview/' + id,
    method: 'get',
    responseType: 'blob',
    skipErrorHandler: true // 跳过错误处理，直接返回二进制流
  })
}

/**
 * 获取电子展馆资料列表
 */
export function studyMaterialGallery(params) {
  return request({
    url: 'study-material/gallery',
    method: 'get',
    params
  })
}

/**
 * 将文件添加到个人资料库
 */
export function studyMaterialAddToLibrary(id) {
  return request({
    url: 'study-material/add-to-library/' + id,
    method: 'post'
  })
}

/**
 * 从个人资料库移除文件
 */
export function studyMaterialRemoveFromLibrary(id) {
  return request({
    url: 'study-material/remove-from-library/' + id,
    method: 'delete'
  })
}

export function studyMaterialSetGalleryPermission(id, showInGallery) {
  return request({
    url: 'study-material/set-gallery-permission/' + id,
    method: 'put',
    params: { showInGallery }
  })
}
