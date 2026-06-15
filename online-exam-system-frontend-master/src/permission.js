
import router from './router'
import store from './store'
import { Message } from 'element-ui'
import NProgress from 'nprogress' // progress bar
import 'nprogress/nprogress.css' // progress bar style
import { getToken } from '@/utils/auth' // get token from cookie
import getPageTitle from '@/utils/get-page-title'

NProgress.configure({ showSpinner: false }) // NProgress Configuration

const whiteList = ['/login', '/register', '/404'] // no redirect whitelist

router.beforeEach(async(to, from, next) => {
  // start progress bar
  NProgress.start()

  // set page title
  document.title = getPageTitle(to.meta.title)

  // determine whether the user has logged in
  const hasToken = getToken()

  if (hasToken) {
    if (to.path === '/login') {
      const roles = localStorage.getItem('roles')
      let defaultPath = '/home/index'
      if (roles === 'teacher') defaultPath = '/exam-management'
      else if (roles === 'admin') defaultPath = '/user-management'
      next({ path: defaultPath })
      NProgress.done()
    } else {
      const hasGetUserInfo = store.getters.name
      if (hasGetUserInfo) {
        // 检查是否需要跳转到备考目标调研
        if (await shouldRedirectToQuestionnaire(to)) {
          next('/user/onboarding')
          NProgress.done()
          return
        }

        // 添加标签到标签栏
        if (to.meta && to.meta.title && to.path !== '/home/index') {
          store.commit('menu/ADD_TAG', {
            title: to.meta.title,
            path: to.path,
            name: to.name
          })
        }
        next()
      } else {
        try {
          // get user info
          await store.dispatch('user/getInfo')

          // 检查是否需要跳转到备考目标调研
          if (await shouldRedirectToQuestionnaire(to)) {
            next('/user/onboarding')
            NProgress.done()
            return
          }

          // 添加标签到标签栏
          if (to.meta && to.meta.title && to.path !== '/home/index') {
            store.commit('menu/ADD_TAG', {
              title: to.meta.title,
              path: to.path,
              name: to.name
            })
          }
          next()
        } catch (error) {
          // remove token and go to login page to re-login
          await store.dispatch('user/resetToken')
          Message.error(error || 'Has Error')
          next(`/login?redirect=${to.path}`)
          NProgress.done()
        }
      }
    }
  } else {
    /* has no token*/

    if (whiteList.indexOf(to.path) !== -1) {
      // in the free login whitelist, go directly
      next()
    } else {
      // other pages that do not have permission to access are redirected to the login page.
      next(`/login?redirect=${to.path}`)
      NProgress.done()
    }
  }
})

// 检查是否需要跳转到备考目标调研
async function shouldRedirectToQuestionnaire(to) {
  // 如果当前已经是备考目标调研页面，不需要重定向
  if (to.path === '/user/onboarding') {
    return false
  }

  // 检查是否刚完成问卷提交（通过本地存储）
  const justCompletedQuestionnaire = localStorage.getItem('questionnaire_completed')
  if (justCompletedQuestionnaire === 'true') {
    // 清除标记，让后续检查正常工作
    localStorage.removeItem('questionnaire_completed')
    return false
  }

  // 检查是否是学生角色
  const roles = localStorage.getItem('roles')
  if (roles !== 'student') {
    return false
  }

  try {
    // 调用后端API检查用户是否已完成问卷
    const { checkQuestionnaireCompleted } = await import('@/api/user-questionnaire')
    const response = await checkQuestionnaireCompleted()

    // 如果用户未完成问卷，需要跳转到备考目标调研
    // 注意：根据request.js的响应拦截器，成功的响应code应该是1
    if (response.code === 1 && !response.data) {
      console.log('用户未完成问卷，需要跳转到备考目标调研')
      return true
    } else {
      console.log('用户已完成问卷，不需要跳转，response:', response)
      return false
    }
  } catch (error) {
    console.error('检查问卷完成状态失败:', error)
    // 如果API调用失败，默认不需要跳转，避免死循环
    return false
  }
}

router.afterEach(() => {
  // finish progress bar
  NProgress.done()
})
