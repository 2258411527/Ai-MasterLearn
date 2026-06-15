import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

/* Layout */
import Layout from '@/layout'

/**
 * Note: sub-menu only appear when route children.length >= 1
 * Detail see: https://panjiachen.github.io/vue-element-admin-site/guide/essentials/router-and-nav.html
 *
 * hidden: true                   if set true, item will not show in the sidebar(default is false)
 * alwaysShow: true               if set true, will always show the root menu
 *                                if not set alwaysShow, when item has more than one children route,
 *                                it will becomes nested mode, otherwise not show the root menu
 * redirect: noRedirect           if set noRedirect will no redirect in the breadcrumb
 * name:'router-name'             the name is used by <keep-alive> (must set!!!)
 * meta : {
    roles: ['admin','editor']    control the page roles (you can set multiple roles)
    title: 'title'               the name show in sidebar and breadcrumb (recommend set)
    icon: 'svg-name'/'el-icon-x' the icon show in the sidebar
    breadcrumb: false            if set false, the item will hidden in breadcrumb(default is true)
    activeMenu: '/example/list'  if set path, the sidebar will highlight the path you set
  }
 */

/**
 * constantRoutes
 * a base page that does not have permission requirements
 * all roles can be accessed
 */
export const constantRoutes = [
  {
    path: '/login',
    component: () => import('@/views/login/index'),
    hidden: true
  },

  {
    path: '/404',
    component: () => import('@/views/404'),
    hidden: true
  },
  {
    path: '/register',
    component: () => import('@/views/login/register'),
    hidden: true
  },

  {
    path: '/user/onboarding',
    component: Layout,
    hidden: true,
    meta: { requireAuth: true, roles: ['student'] },
    children: [{
      path: '',
      name: 'OnboardingQuestionnaire',
      component: () => import('@/views/user/OnboardingQuestionnaire'),
      meta: { title: '备考目标调研', icon: 'el-icon-survey', roles: ['student'] }
    }]
  },

  {
    path: '/',
    redirect: () => {
      const roles = localStorage.getItem('roles')
      if (roles === 'teacher') return '/exam-management'
      if (roles === 'admin') return '/user-management'
      return '/home/index'
    },
    hidden: true
  },

  {
    path: '/home',
    component: Layout,
    meta: { requireAuth: true, roles: ['student'] },
    redirect: { name: 'HomePage' },
    children: [{
      path: 'index',
      name: 'HomePage',
      component: () => import('@/views/home/index'),
      meta: { title: '首页', icon: 'el-icon-data-line', visible: true, roles: ['student'] }
    }]
  },

  {
    path: '/class-members',
    component: Layout,
    meta: { roles: ['teacher'] },
    children: [{
      path: '',
      name: 'class-members',
      component: () => import('@/views/user/index'),
      meta: { title: '管理班级成员', icon: 'el-icon-user', visible: true, roles: ['teacher'] }
    }]
  },
  {
    path: '/user-management',
    component: Layout,
    meta: { roles: ['admin'] },
    children: [{
      path: '',
      name: 'user-management',
      component: () => import('@/views/user/index'),
      meta: { title: '用户管理', icon: 'el-icon-user', visible: true, roles: ['admin'] }
    }]
  },
  {
    path: '/myself',
    component: Layout,
    hidden: true,
    meta: { roles: ['teacher', 'admin', 'student'] },
    children: [{
      path: '',
      name: 'myself',
      hidden: true,
      component: () => import('@/views/user/myself'),
      meta: { title: '个人中心', visible: true, roles: ['teacher', 'admin', 'student'], icon: 'dashboard' }
    }]
  },
  {
    path: '/change-password',
    component: Layout,
    hidden: true,
    meta: { roles: ['teacher', 'admin', 'student'] },
    children: [{
      path: '',
      name: 'change-password',
      hidden: true,
      component: () => import('@/views/user/updatePassword.vue'),
      meta: { title: '修改密码', visible: true, roles: ['teacher', 'admin', 'student'], icon: 'dashboard' }
    }]
  },
  {
    path: '/class-management',
    component: Layout,
    meta: { roles: ['teacher', 'admin'] },
    children: [{
      path: '',
      name: 'class-management',
      component: () => import('@/views/class/index'),
      meta: { title: '班级管理', visible: true, roles: ['teacher', 'admin'], icon: 'el-icon-takeaway-box' }
    }]
  },
  {
    path: '/discussion-management',
    component: Layout,
    meta: { roles: ['teacher', 'student', 'admin'] },
    children: [{
      path: 'discussion-management',
      name: 'discussion-management',
      component: () => import('@/views/discuss/new-index.vue'),
      meta: { title: '交流中心', visible: true, roles: ['teacher', 'student', 'admin'], icon: 'el-icon-chat-dot-square' }
    }]
  },
  {
    path: '/discussion-detail',
    component: Layout,
    hidden: true,
    meta: { roles: ['teacher', 'student', 'admin'] },
    children: [{
      path: 'discussion-detail',
      hidden: true,
      name: 'discussion-detail',
      component: () => import('@/views/discuss/new-detail.vue'),
      meta: { title: '讨论详情', visible: true, roles: ['teacher', 'student', 'admin'], icon: 'el-icon-takeaway-box' }
    }]
  },
  {
    path: '/exam-details',
    component: Layout,
    hidden: true,
    meta: { roles: ['teacher', 'admin'] },
    children: [{
      path: 'exam-details',
      hidden: true,
      name: 'exam-details',
      component: () => import('@/views/exam/details.vue'),
      meta: { title: '考试详情', visible: true, roles: ['teacher', 'admin'], icon: 'el-icon-takeaway-box' }
    }]
  },
  {
    path: '/discussion-block',
    component: Layout,
    hidden: true,
    meta: { roles: ['teacher', 'admin'] },
    children: [
      {
        path: '',
        name: 'discussion-block',
        hidden: true,
        component: () =>
          import('@/views/discuss/block.vue'),
        meta: {
          title: '投屏模式',
          visible: false,
          roles: ['teacher', 'admin']
        }
      }
    ]
  },
  {
    path: '/prepare-exam',
    component: Layout,
    hidden: true,
    meta: { roles: ['teacher', 'admin', 'student'] },
    children: [{
      path: '',
      name: 'prepare-exam',
      hidden: true,
      component: () => import('@/views/exam/examInformation.vue'),
      meta: { title: '准备考试', visible: true, roles: ['teacher', 'admin', 'student'], icon: 'dashboard' }
    }]
  },
  {
    path: '/text-center',
    component: Layout,
    meta: { roles: ['student'] },
    children: [{
      path: '',
      name: 'text-center',
      component: () => import('@/views/exam/student/index'),
      meta: { title: '试卷中心', visible: true, roles: ['student'], icon: 'el-icon-document-copy' }
    }]
  },
  {
    path: '/start-exam',
    name: 'start-exam',
    hidden: true,
    component: () => import('@/views/exam/index.vue'),
    meta: { title: '开始考试', visible: true, roles: ['teacher', 'admin', 'student'], icon: 'dashboard' }
  },
  {
    path: '/single-practice',
    name: 'single-practice',
    hidden: true,
    component: () => import('@/views/practice/single-practice.vue'),
    meta: { title: '单题练习', visible: true, roles: ['student'], icon: 'dashboard' }
  },
  {
    path: '/exercise-center',
    component: Layout,
    meta: { roles: ['student'] },
    children: [{
      path: '',
      name: 'exercise-center',
      component: () => import('@/views/exercise/index'),
      meta: { title: '刷题中心', visible: true, roles: ['student'], icon: 'el-icon-tickets' }
    }]
  },
  {
    path: '/start-exercise',
    name: 'start-exercise',
    hidden: true,
    component: () => import('@/views/exercise/exercise.vue'),
    meta: { title: '开始刷题', visible: true, roles: ['teacher', 'admin', 'student'], icon: 'dashboard' }

  },

  {
    path: '/record',
    component: Layout,
    name: 'record',
    meta: { title: '考试记录', icon: 'el-icon-folder-opened', visible: true, roles: ['student'], requireAuth: true },
    children: [
      {
        path: 'exam',
        name: 'exam-record',
        component: () => import('@/views/record/exam/index.vue'),
        meta: { title: '考试记录', visible: true, roles: ['student'], icon: 'table' }
      },
      {
        path: 'exercise',
        name: 'exercise-record',
        component: () => import('@/views/record/exercise/index.vue'),
        meta: { title: '刷题记录', visible: true, roles: ['student'], icon: 'tree' }
      },
      {
        path: 'ai-grading',
        name: 'ai-grading',
        component: () => import('@/views/ai-grading/index.vue'),
        meta: { title: 'AI智能阅卷', visible: true, roles: ['teacher', 'admin'], icon: 'el-icon-cpu' }
      }
    ]
  },
  {
    path: '/exam-record-detail',
    component: Layout,
    hidden: true,
    meta: { roles: ['teacher', 'admin', 'student'] },
    children: [{
      path: '',
      name: 'exam-record-detail',
      hidden: true,
      component: () => import('@/views/record/exam/newk'),
      meta: { title: '考试记录查看', visible: true, roles: ['teacher', 'admin', 'student'], icon: 'dashboard' }
    }]
  },
  {
    path: '/exercise-record-detail',
    component: Layout,
    hidden: true,
    meta: { roles: ['teacher', 'admin', 'student'] },
    children: [{
      path: '',
      name: 'exercise-record-detail',
      hidden: true,
      component: () => import('@/views/record/exercise/newk'),
      meta: { title: '刷题记录查看', visible: true, roles: ['teacher', 'admin', 'student'], icon: 'dashboard' }
    }]
  },
  {
    path: '/wrong-book',
    component: Layout,
    meta: { roles: ['student'] },
    children: [{
      path: '',
      name: 'wrong-book',
      component: () => import('@/views/userbook/index'),
      meta: { title: '错题本', visible: true, roles: ['student'], icon: 'el-icon-notebook-1' }
    }]
  },
  {
    path: '/rebrush',
    name: 'rebrush',
    hidden: true,
    component: () => import('@/views/userbook/rebrush.vue'),
    meta: { title: '重刷', visible: true, roles: ['teacher', 'admin', 'student'], icon: 'dashboard' }

  },
  {
    path: '/exam-management',
    component: Layout,
    meta: { roles: ['teacher', 'admin'] },
    children: [{
      path: '',
      name: 'exam-management',
      component: () => import('@/views/exam/teacher/index'),
      meta: { title: '考试管理', visible: true, roles: ['teacher', 'admin'], icon: 'el-icon-document' }
    }]
  },
  {
    path: '/exam-add',
    component: Layout,
    hidden: true,
    meta: { roles: ['teacher', 'admin'] },
    children: [{
      path: '',
      name: 'exam-add',
      hidden: true,
      component: () => import('@/views/exam/examAdd'),
      meta: { title: '考试添加', visible: true, roles: ['teacher', 'admin'], icon: 'dashboard' }
    }]
  },
  {
    path: '/repo-management',
    component: Layout,
    meta: { roles: ['teacher', 'admin'] },
    children: [{
      path: '',
      name: 'repo-management',
      component: () => import('@/views/repo/index'),
      meta: { title: '题库管理', visible: true, roles: ['teacher', 'admin'], icon: 'el-icon-folder-opened' }
    }]
  },
  {
    path: '/questions-management',
    component: Layout,
    meta: { roles: ['teacher', 'admin'] },
    children: [{
      path: '',
      name: 'questions-management',
      component: () => import('@/views/question/index'),
      meta: { title: '试题管理', visible: true, roles: ['teacher', 'admin'], icon: 'el-icon-document-copy' }
    }]
  },
  {
    path: '/questions-add',
    component: Layout,
    hidden: true,
    meta: { roles: ['teacher', 'admin'] },
    children: [{
      path: '',
      name: 'questions-add',
      hidden: true,
      component: () => import('@/views/question/add.vue'),
      meta: { title: '试题添加', visible: true, roles: ['teacher', 'admin'], icon: 'dashboard' }
    }]
  },
  {
    path: '/certificate',
    component: Layout,
    meta: { roles: ['admin', 'teacher'] },
    children: [{
      path: '',
      name: 'certificate-management',
      component: () => import('@/views/certificate/CertMgr'),
      meta: { title: '证书管理', visible: true, roles: ['admin', 'teacher'], icon: 'el-icon-postcard' }
    }]
  },
  {
    path: '/ai-config',
    component: Layout,
    meta: { roles: ['admin'] },
    children: [{
      path: '',
      name: 'ai-config-management',
      component: () => import('@/views/ai-config/index'),
      meta: { title: 'AI配置管理', visible: true, roles: ['admin'], icon: 'el-icon-cpu' }
    }]
  },
  {
    path: '/token-admin',
    component: Layout,
    meta: { roles: ['admin'] },
    children: [{
      path: '',
      name: 'token-admin',
      component: () => import('@/views/token/admin'),
      meta: { title: 'Token管理', visible: true, roles: ['admin'], icon: 'el-icon-coin' }
    }]
  },
  {
    path: '/alipay-config',
    component: Layout,
    meta: { roles: ['admin'] },
    children: [{
      path: '',
      name: 'alipay-config',
      component: () => import('@/views/alipay-config/index'),
      meta: { title: '支付配置', visible: true, roles: ['admin'], icon: 'el-icon-bank-card' }
    }]
  },
  {
    path: '/admin-grading-rag',
    component: Layout,
    meta: { roles: ['admin'] },
    children: [{
      path: '',
      name: 'admin-grading-rag',
      component: () => import('@/views/admin-grading-rag/index'),
      meta: { title: '阅卷解析中心', visible: true, roles: ['admin'], icon: 'el-icon-s-check' }
    }]
  },
  {
    path: '/notifications',
    component: Layout,
    meta: { roles: ['student', 'teacher', 'admin'] },
    children: [{
      path: '',
      name: 'notifications',
      component: () => import('@/views/notifications/index'),
      meta: { title: '消息中心', visible: true, roles: ['student', 'teacher', 'admin'], icon: 'el-icon-bell' }
    }]
  },
  {
    path: '/notification-sender',
    component: Layout,
    meta: { roles: ['admin'] },
    children: [{
      path: '',
      name: 'notification-sender',
      component: () => import('@/views/notifications/sender'),
      meta: { title: '发送通知', visible: true, roles: ['admin'], icon: 'el-icon-message' }
    }]
  },
  {
    path: '/mycertificate',
    component: Layout,
    meta: { roles: ['student'] },
    children: [{
      path: '',
      name: 'mycertificate',
      component: () => import('@/views/certificate/myCertificates'),
      meta: { title: '我的证书', icon: 'el-icon-postcard', visible: true, roles: ['student'] }
    }]
  },

  {
    path: '/score-analysis',
    component: Layout,
    meta: { roles: ['teacher'] },
    children: [{
      path: '',
      name: 'score-analysis',
      component: () => import('@/views/score/index'),
      meta: { title: '成绩分析', visible: true, roles: ['teacher'], icon: 'el-icon-pie-chart' }
    }]
  },
  {
    path: '/user-score',
    component: Layout,
    hidden: true,
    meta: { roles: ['teacher'] },
    children: [{
      path: '',
      name: 'user-score',
      hidden: true,
      component: () => import('@/views/score/detail'),
      meta: { title: '用户成绩', visible: true, roles: ['teacher'], icon: 'dashboard' }
    }]
  },
  {
    path: '/answer-manage',
    component: Layout,
    meta: { roles: ['teacher'] },
    children: [{
      path: '',
      name: 'marking-management',
      component: () => import('@/views/answer/index'),
      meta: { title: '阅卷管理', visible: true, roles: ['teacher'], icon: 'el-icon-files' }
    }]
  },
  {
    path: '/answer-show',
    component: Layout,
    hidden: true,
    meta: { roles: ['teacher'] },
    children: [{
      path: '',
      name: 'answer-show',
      hidden: true,
      component: () => import('@/views/answer/answerck'),
      meta: { title: '答卷查看', visible: true, roles: ['teacher'], icon: 'dashboard' }
    }]
  },
  {
    path: '/makeTest',
    component: Layout,
    hidden: true,
    meta: { roles: ['teacher'] },
    children: [{
      path: '',
      name: 'makeTest',
      hidden: true,
      component: () => import('@/views/answer/makeTest'),
      meta: { title: '批改试卷', visible: true, roles: ['teacher'], icon: 'dashboard' }
    }]
  },

  {
    path: '/notice-management',
    component: Layout,
    meta: { roles: ['teacher', 'admin'] },
    children: [{
      path: '',
      name: 'notice-management',
      component: () => import('@/views/notice/notice'),
      meta: { title: '公告管理', visible: true, roles: ['teacher', 'admin'], icon: 'el-icon-bell' }
    }]
  },
  {
    path: '/login-log',
    component: Layout,
    meta: { roles: ['teacher', 'admin', 'student'] },
    children: [
      {
        path: '',
        name: 'login-log',
        component: () => import('@/views/log/index'),
        meta: {
          title: '登录日志',
          visible: false,
          roles: ['teacher', 'admin', 'student'],
          icon: 'el-icon-receiving'
        }
      }
    ]
  },

  {
    path: '/study-material',
    component: Layout,
    redirect: '/study-material',
    meta: {
      title: '资料中心',
      visible: true,
      roles: ['student', 'teacher', 'admin'],
      icon: 'el-icon-folder-opened'
    },
    children: [
      {
        path: '',
        name: 'study-material',
        component: () => import('@/views/study-material'),
        meta: {
          title: '我的资料',
          visible: true,
          roles: ['student', 'teacher', 'admin'],
          icon: 'el-icon-document'
        }
      },
      {
        path: 'gallery',
        name: 'study-material-gallery',
        component: () => import('@/views/study-material/gallery'),
        meta: {
          title: '电子展馆',
          visible: true,
          roles: ['student', 'teacher', 'admin'],
          icon: 'el-icon-picture-outline'
        }
      }
    ]
  },

  {
    path: '/ai-chat',
    component: Layout,
    meta: { roles: ['student'] },
    children: [
      {
        path: '',
        name: 'ai-chat',
        component: () => import('@/views/ai-chat'),
        meta: {
          title: 'AI 问答',
          visible: true,
          roles: ['student'],
          icon: 'el-icon-chat-line-round'
        }
      }
    ]
  },

  {
    path: '/token',
    component: Layout,
    meta: { roles: ['student'] },
    children: [
      {
        path: '',
        name: 'token-center',
        component: () => import('@/views/token/index'),
        meta: {
          title: 'Token 充值',
          visible: true,
          roles: ['student'],
          icon: 'el-icon-coin'
        }
      }
    ]
  },

  {
    path: '/chat',
    component: Layout,
    meta: { roles: ['student', 'teacher', 'admin'] },
    children: [
      {
        path: '',
        name: 'chat',
        component: () => import('@/views/chat/index'),
        meta: {
          title: '好友聊天',
          visible: true,
          roles: ['student', 'teacher', 'admin'],
          icon: 'el-icon-chat-dot-round'
        }
      }
    ]
  },

  {
    path: '/rag-report',
    component: Layout,
    meta: { roles: ['student'] },
    children: [
      {
        path: '',
        name: 'rag-report',
        component: () => import('@/views/rag/report'),
        meta: {
          title: '解析报告',
          visible: true,
          roles: ['student'],
          icon: 'el-icon-document-checked'
        }
      }
    ]
  },

  { path: '*', redirect: '/404', hidden: true }
]

const createRouter = () => new Router({
  // mode: 'history', // require service support
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRoutes
})

const router = createRouter()

// Detail see: https://github.com/vuejs/vue-router/issues/1234#issuecomment-357941465
export function resetRouter() {
  const newRouter = createRouter()
  router.matcher = newRouter.matcher // reset router
}

export default router