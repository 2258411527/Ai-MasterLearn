<template>
  <div class="auth-page">
    <div class="auth-bg">
      <div class="bg-shape shape-1" />
      <div class="bg-shape shape-2" />
      <div class="bg-shape shape-3" />
      <div class="bg-gradient" />
      <div class="bg-dots" />
    </div>

    <div class="auth-container">
      <div class="auth-card">
        <div class="auth-left">
          <div class="brand-section">
            <div class="logo-wrapper">
              <svg class="logo-icon" viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
                <defs>
                  <linearGradient id="logoGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" stop-color="#6366F1" />
                    <stop offset="100%" stop-color="#8B5CF6" />
                  </linearGradient>
                  <linearGradient id="logoGrad2" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" stop-color="#818CF8" />
                    <stop offset="100%" stop-color="#A78BFA" />
                  </linearGradient>
                </defs>
                <rect x="8" y="8" width="64" height="64" rx="18" stroke="url(#logoGrad)" stroke-width="2.5" fill="none" />
                <path d="M22 24h10v14h7V24h10" stroke="url(#logoGrad)" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M22 38h36" stroke="url(#logoGrad2)" stroke-width="2.5" stroke-linecap="round"/>
                <circle cx="58" cy="22" r="6" fill="url(#logoGrad2)" opacity="0.3" />
                <circle cx="18" cy="56" r="4" fill="url(#logoGrad)" opacity="0.25" />
              </svg>
            </div>
            <h1 class="brand-name">AI 研学平台</h1>
            <p class="brand-tagline">智能驱动 · 高效备考 · 精准提分</p>
          </div>

          <div class="feature-showcase">
            <transition name="feature-fade" mode="out-in">
              <div :key="currentFeature" class="feature-card">
                <div class="feature-icon-wrap">
                  <i :class="features[currentFeature].icon" />
                </div>
                <h3 class="feature-title">{{ features[currentFeature].title }}</h3>
                <p class="feature-desc">{{ features[currentFeature].desc }}</p>
              </div>
            </transition>

            <div class="feature-dots">
              <span
                v-for="(f, i) in features"
                :key="i"
                class="dot"
                :class="{ active: i === currentFeature }"
                @click="currentFeature = i"
              />
            </div>
          </div>

          <div class="stats-row">
            <div class="stat-item">
              <span class="stat-num">10,000+</span>
              <span class="stat-label">活跃用户</span>
            </div>
            <div class="stat-item">
              <span class="stat-num">50万+</span>
              <span class="stat-label">题库资源</span>
            </div>
            <div class="stat-item">
              <span class="stat-num">AI</span>
              <span class="stat-label">智能引擎</span>
            </div>
          </div>
        </div>

        <div class="auth-right">
          <div class="form-header">
            <h2 class="form-title">欢迎回来</h2>
            <p class="form-subtitle">登录您的账号，继续学习之旅</p>
          </div>

          <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                prefix-icon="el-icon-user"
                size="large"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                :type="passwordType"
                v-model="loginForm.password"
                placeholder="请输入密码"
                prefix-icon="el-icon-lock"
                size="large"
                @keyup.enter.native="handleLogin"
              >
                <i
                  slot="suffix"
                  :class="passwordType === 'password' ? 'el-icon-view' : 'el-icon-view'"
                  class="pwd-toggle"
                  @click="togglePassword"
                />
              </el-input>
            </el-form-item>

            <div v-if="captchaEnabled" class="captcha-row">
              <el-form-item prop="code" class="captcha-input">
                <el-input
                  v-model="loginForm.code"
                  placeholder="验证码"
                  prefix-icon="el-icon-key"
                  size="large"
                  @keyup.enter.native="handleLogin"
                />
              </el-form-item>
              <div class="captcha-img-box" @click="refreshCaptcha">
                <img :src="captchaUrl" alt="验证码" />
              </div>
            </div>

            <div class="form-options">
              <el-checkbox v-model="loginForm.remember">记住我</el-checkbox>
              <span class="forgot-link">忘记密码?</span>
            </div>

            <el-button
              class="submit-btn"
              type="primary"
              :loading="loading"
              @click="handleLogin"
              size="large"
            >
              登 录
            </el-button>
          </el-form>

          <div class="switch-link">
            <span>还没有账号？</span>
            <router-link to="/register">立即注册</router-link>
          </div>

          <div class="auth-footer">
            <span v-if="icpNumber">
              <a :href="icpLink" target="_blank">{{ icpNumber }}</a>
            </span>
            <span>Copyright &copy; 2026 AI 研学平台</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { validUsername } from '@/utils/validate'
import { getTokenInfo } from '@/utils/jwtUtils'
import { verifyCode } from '@/api/user'
import { Message } from 'element-ui'

export default {
  name: 'Login',
  data() {
    const validateUsername = (rule, value, callback) => {
      if (!validUsername(value)) {
        callback(new Error('请输入正确的用户名'))
      } else {
        callback()
      }
    }
    const validatePassword = (rule, value, callback) => {
      if (value.length < 6) {
        callback(new Error('密码不能少于6位'))
      } else {
        callback()
      }
    }
    const validateCode = (rule, value, callback) => {
      if (this.captchaEnabled && !value) {
        callback(new Error('请输入验证码'))
      } else {
        callback()
      }
    }

    return {
      loginForm: {
        username: '',
        password: '',
        code: '',
        remember: false
      },
      captchaEnabled: process.env.VUE_APP_CAPTCHA_ENABLED !== 'false',
      icpNumber: process.env.VUE_APP_ICP_NUMBER,
      icpLink: process.env.VUE_APP_ICP_LINK,
      loginRules: {
        username: [{ required: true, trigger: 'blur', validator: validateUsername }],
        password: [{ required: true, trigger: 'blur', validator: validatePassword }],
        code: [{ required: false, trigger: 'blur', validator: validateCode }]
      },
      loading: false,
      passwordType: 'password',
      captchaUrl: `/api/auths/captcha?${Math.random()}`,
      currentFeature: 0,
      featureTimer: null,
      features: [
        { icon: 'el-icon-cpu', title: 'AI 智能答疑', desc: '基于深度学习的智能问答系统，7x24小时在线解答学习疑问，精准定位知识盲区' },
        { icon: 'el-icon-document-checked', title: '智能错题分析', desc: '自动归集错题，AI分析薄弱环节，生成个性化强化训练方案' },
        { icon: 'el-icon-data-analysis', title: '学习数据洞察', desc: '多维度学习行为分析，可视化进度追踪，科学规划备考路径' },
        { icon: 'el-icon-collection-tag', title: '海量题库资源', desc: '覆盖考研、考公全科目，智能组卷，模拟真实考试环境' }
      ]
    }
  },
  computed: {
    redirect() {
      return this.$route.query.redirect || '/home/index'
    }
  },
  mounted() {
    this.featureTimer = setInterval(() => {
      this.currentFeature = (this.currentFeature + 1) % this.features.length
    }, 5000)
  },
  beforeDestroy() {
    if (this.featureTimer) clearInterval(this.featureTimer)
  },
  methods: {
    togglePassword() {
      this.passwordType = this.passwordType === 'password' ? '' : 'password'
    },
    refreshCaptcha() {
      this.captchaUrl = `/api/auths/captcha?${Math.random()}`
    },
    handleLogin() {
      const validateAndLogin = () => {
        this.$refs.loginForm.validate((valid) => {
          if (valid) {
            this.loading = true
            const loginData = {
              username: this.loginForm.username,
              password: this.loginForm.password
            }
            this.$store
              .dispatch('user/login', loginData)
              .then(() => {
                this.$store.commit('menu/CLOSE_SIDEBAR')
                const userInfo = getTokenInfo()
                this.$store.dispatch('loginUser', { id: userInfo.id })
                this.$router.push(this.redirect)
                this.loading = false
              })
              .catch((error) => {
                if (this.captchaEnabled) {
                  this.refreshCaptcha()
                }
                Message.error(error.msg || '登录失败')
                this.loading = false
              })
          }
        })
      }

      if (this.captchaEnabled) {
        verifyCode(this.loginForm.code).then((res) => {
          if (res.code) {
            validateAndLogin()
          } else {
            this.loginForm.code = ''
            this.refreshCaptcha()
            Message.error(res.msg || '验证码错误')
          }
        }).catch(() => {
          this.loginForm.code = ''
          this.refreshCaptcha()
          Message.error('验证码验证失败')
        })
      } else {
        validateAndLogin()
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: var(--bg-primary);
  transition: background 0.4s ease;
}

.auth-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;

  .bg-gradient {
    position: absolute;
    inset: 0;
    background:
      radial-gradient(ellipse 80% 60% at 30% 20%, rgba(99, 102, 241, 0.08) 0%, transparent 50%),
      radial-gradient(ellipse 60% 80% at 70% 80%, rgba(139, 92, 246, 0.06) 0%, transparent 50%),
      radial-gradient(ellipse 50% 50% at 50% 50%, rgba(99, 102, 241, 0.03) 0%, transparent 60%);
  }

  .bg-dots {
    position: absolute;
    inset: 0;
    background-image: radial-gradient(var(--text-hint) 1px, transparent 1px);
    background-size: 40px 40px;
    opacity: 0.06;
  }

  .bg-shape {
    position: absolute;
    border-radius: 50%;
    opacity: 0.4;
    animation: floatShape 20s ease-in-out infinite;

    &.shape-1 {
      width: 400px;
      height: 400px;
      background: radial-gradient(circle, rgba(99, 102, 241, 0.12), transparent 70%);
      top: -100px;
      left: -80px;
      animation-delay: 0s;
    }

    &.shape-2 {
      width: 300px;
      height: 300px;
      background: radial-gradient(circle, rgba(139, 92, 246, 0.1), transparent 70%);
      bottom: -80px;
      right: -60px;
      animation-delay: -7s;
    }

    &.shape-3 {
      width: 250px;
      height: 250px;
      background: radial-gradient(circle, rgba(99, 102, 241, 0.08), transparent 70%);
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      animation-delay: -14s;
    }
  }
}

@keyframes floatShape {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(30px, -20px) scale(1.05); }
  50% { transform: translate(-10px, 25px) scale(0.95); }
  75% { transform: translate(-25px, -15px) scale(1.02); }
}

.auth-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 1000px;
  margin: 0 24px;
}

.auth-card {
  display: flex;
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  border-radius: 24px;
  box-shadow: var(--shadow-lg);
  overflow: hidden;
  min-height: 580px;
}

.auth-left {
  flex: 1.1;
  background: linear-gradient(160deg, rgba(99, 102, 241, 0.06) 0%, rgba(139, 92, 246, 0.04) 100%);
  padding: 48px 40px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  border-right: 1px solid var(--glass-border);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -40%;
    right: -30%;
    width: 300px;
    height: 300px;
    background: radial-gradient(circle, rgba(99, 102, 241, 0.08), transparent 70%);
    border-radius: 50%;
  }
}

.brand-section {
  position: relative;
  z-index: 1;

  .logo-wrapper {
    margin-bottom: 20px;

    .logo-icon {
      width: 72px;
      height: 72px;
      filter: drop-shadow(0 4px 12px rgba(99, 102, 241, 0.2));
    }
  }

  .brand-name {
    font-size: 30px;
    font-weight: 800;
    background: var(--accent-gradient);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    margin: 0 0 8px;
    letter-spacing: 2px;
  }

  .brand-tagline {
    font-size: 14px;
    color: var(--text-secondary);
    margin: 0;
    letter-spacing: 1px;
  }
}

.feature-showcase {
  position: relative;
  z-index: 1;
  margin: 32px 0;

  .feature-card {
    padding: 24px;
    background: var(--glass-bg);
    border: 1px solid var(--glass-border);
    border-radius: 16px;
    min-height: 160px;
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;

    .feature-icon-wrap {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      background: var(--accent-gradient);
      display: flex;
      align-items: center;
      justify-content: center;

      i {
        font-size: 24px;
        color: #fff;
      }
    }

    .feature-title {
      font-size: 18px;
      font-weight: 700;
      color: var(--text-primary);
      margin: 0;
    }

    .feature-desc {
      font-size: 13px;
      color: var(--text-secondary);
      line-height: 1.7;
      margin: 0;
    }
  }

  .feature-dots {
    display: flex;
    gap: 8px;
    margin-top: 16px;
    justify-content: center;

    .dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: var(--border-primary);
      cursor: pointer;
      transition: all 0.3s ease;

      &.active {
        width: 24px;
        border-radius: 4px;
        background: var(--accent-gradient);
      }

      &:hover {
        background: var(--accent-light);
      }
    }
  }
}

.feature-fade-enter-active,
.feature-fade-leave-active {
  transition: all 0.4s ease;
}

.feature-fade-enter {
  opacity: 0;
  transform: translateY(12px);
}

.feature-fade-leave-to {
  opacity: 0;
  transform: translateY(-12px);
}

.stats-row {
  display: flex;
  gap: 32px;
  position: relative;
  z-index: 1;

  .stat-item {
    display: flex;
    flex-direction: column;
    gap: 4px;

    .stat-num {
      font-size: 22px;
      font-weight: 800;
      color: var(--text-primary);
    }

    .stat-label {
      font-size: 12px;
      color: var(--text-hint);
    }
  }
}

.auth-right {
  flex: 1;
  padding: 48px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-header {
  text-align: center;
  margin-bottom: 32px;

  .form-title {
    font-size: 24px;
    font-weight: 700;
    color: var(--text-primary);
    margin: 0 0 8px;
  }

  .form-subtitle {
    font-size: 14px;
    color: var(--text-secondary);
    margin: 0;
  }
}

.login-form {
  ::v-deep .el-form-item {
    margin-bottom: 20px;
  }

  ::v-deep .el-input__inner {
    height: 48px;
    border-radius: 12px;
    border: 1px solid var(--border-primary);
    background: var(--bg-input);
    color: var(--text-primary);
    font-size: 14px;
    padding-left: 40px;
    transition: all 0.25s ease;

    &::placeholder { color: var(--text-hint); }

    &:focus {
      border-color: var(--accent);
      box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
    }
  }

  ::v-deep .el-input__prefix {
    left: 12px;
    color: var(--text-hint);
    font-size: 16px;
  }

  .pwd-toggle {
    cursor: pointer;
    color: var(--text-hint);
    font-size: 16px;
    padding: 0 12px;
    transition: color 0.2s;

    &:hover { color: var(--accent); }
  }
}

.captcha-row {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;

  .captcha-input {
    flex: 1;
    margin-bottom: 0;
  }

  .captcha-img-box {
    height: 48px;
    width: 110px;
    border-radius: 12px;
    border: 1px solid var(--border-primary);
    overflow: hidden;
    cursor: pointer;
    transition: all 0.2s;
    flex-shrink: 0;

    &:hover { border-color: var(--accent); }

    img { width: 100%; height: 100%; object-fit: cover; }
  }
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  ::v-deep .el-checkbox__label {
    font-size: 13px;
    color: var(--text-secondary);
  }

  .forgot-link {
    font-size: 13px;
    color: var(--accent);
    cursor: pointer;
    transition: opacity 0.2s;

    &:hover { opacity: 0.8; }
  }
}

.submit-btn {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 2px;
  background: var(--accent-gradient);
  border: none;
  transition: all 0.3s ease;

  &:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 8px 25px rgba(99, 102, 241, 0.3);
  }

  &:active:not(:disabled) {
    transform: translateY(0);
  }
}

.switch-link {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: var(--text-secondary);

  a {
    color: var(--accent);
    margin-left: 6px;
    text-decoration: none;
    font-weight: 600;
    transition: opacity 0.2s;

    &:hover { opacity: 0.8; }
  }
}

.auth-footer {
  text-align: center;
  margin-top: 32px;
  font-size: 12px;
  color: var(--text-hint);

  a {
    color: var(--text-hint);
    text-decoration: none;
    margin-right: 16px;
    &:hover { text-decoration: underline; }
  }
}

@media (max-width: 768px) {
  .auth-card {
    flex-direction: column;
    min-height: auto;
  }

  .auth-left {
    padding: 32px 24px;
    border-right: none;
    border-bottom: 1px solid var(--glass-border);
  }

  .auth-right {
    padding: 32px 24px;
  }

  .brand-name { font-size: 24px; }
  .feature-showcase { margin: 24px 0; }
  .feature-card { min-height: auto; }
  .stats-row { gap: 20px; }
}
</style>