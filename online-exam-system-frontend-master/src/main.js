/*
 * @Author: 暮安 14122148+muanananan@user.noreply.gitee.com
 * @Date: 2024-03-04 10:34:47
 * @LastEditors: 暮安 14122148+muanananan@user.noreply.gitee.com
 * @LastEditTime: 2024-04-25 15:06:09
 * @FilePath: \vue-admin-template\src\main.js
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
import Vue from 'vue'
import 'normalize.css/normalize.css' // A modern alternative to CSS resets
import htmlToPdf from '@/utils/htmlToPdf'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
// import locale from 'element-ui/lib/locale/lang/en' // lang i18n
import locale from 'element-ui/lib/locale/lang/zh-CN'
import '@/styles/index.scss' // global css
import '@/styles/theme.scss' // theme css variables
import '@/styles/glass.scss' // glass morphism utilities
import echarts from 'echarts'
import App from './App'
import store from './store'
import router from './router'
import '@/icons' // icon
import '@/permission' // permission control
import axios from 'axios'
import { getToken } from './utils/auth'
// 富文本编辑器
import VueQuillEditor from 'vue-quill-editor'
import 'quill/dist/quill.core.css' // 引入样式
import 'quill/dist/quill.snow.css' // snow theme
import 'quill/dist/quill.bubble.css' // bubble theme
import { connectWebSocket, sendMessage } from '@/utils/websocket'

// 如果是开发环境，关闭一些提示
if (process.env.NODE_ENV === 'development') {
  console.warn = function(message) {
    if (!message.includes('DOMNodeInserted')) {
      console.log(message)
    }
  }
}

// 定义全局属性
Vue.prototype.$echarts = echarts

// 路由守卫，在路由切换前判断是否连接 WebSocket
router.beforeEach((to, from, next) => {
  const isLoginOrRegister = ['login', 'register'].includes(to.name)
  // 页面加载时 不是登录页或注册页 尝试重新连接
  if (!isLoginOrRegister) {
    connectWebSocket()
  }
  next()
})

axios.defaults.withCredentials = true

/**
 * If you don't want to use mock-server
 * you want to use MockJs for mock api
 * you can execute: mockXHR()
 *
 * Currently MockJs will be used in the production environment,
 * please remove it before going online ! ! !
 */
// if (process.env.NODE_ENV === 'production') {
//   const { mockXHR } = require('../mock')
//   mockXHR()
// }
Vue.use(htmlToPdf)
// 富文本
Vue.use(VueQuillEditor /* { default global options } */)
// set ElementUI lang to EN
Vue.use(ElementUI, { locale })
// 如果想要中文版 element-ui，按如下方式声明
// Vue.use(ElementUI)

Vue.config.productionTip = false

// 将 WebSocket 相关方法挂载到 Vue 原型上
Vue.prototype.$connectWebSocket = connectWebSocket
Vue.prototype.$sendMessage = sendMessage

new Vue({
  el: '#app',
  router,
  store,
  render: h => h(App)
})