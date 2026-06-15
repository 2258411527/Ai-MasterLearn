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
                  <linearGradient id="regLogoGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" stop-color="#6366F1" />
                    <stop offset="100%" stop-color="#8B5CF6" />
                  </linearGradient>
                  <linearGradient id="regLogoGrad2" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" stop-color="#818CF8" />
                    <stop offset="100%" stop-color="#A78BFA" />
                  </linearGradient>
                </defs>
                <rect x="8" y="8" width="64" height="64" rx="18" stroke="url(#regLogoGrad)" stroke-width="2.5" fill="none" />
                <path d="M22 24h10v14h7V24h10" stroke="url(#regLogoGrad)" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M22 38h36" stroke="url(#regLogoGrad2)" stroke-width="2.5" stroke-linecap="round"/>
                <circle cx="58" cy="22" r="6" fill="url(#regLogoGrad2)" opacity="0.3" />
                <circle cx="18" cy="56" r="4" fill="url(#regLogoGrad)" opacity="0.25" />
              </svg>
            </div>
            <h1 class="brand-name">AI 研学平台</h1>
            <p class="brand-tagline">智能驱动 · 高效备考 · 精准提分</p>
          </div>

          <div class="register-tips">
            <div class="tip-card">
              <div class="tip-icon">
                <i class="el-icon-circle-check" />
              </div>
              <div class="tip-content">
                <h4>注册须知</h4>
                <ul>
                  <li>学生注册后可直接登录使用全部功能</li>
                  <li>教师/管理员需要管理员审核通过</li>
                  <li>首次登录将引导完成备考目标设置</li>
                </ul>
              </div>
            </div>
            <div class="gift-card">
              <div class="gift-icon">
                <i class="el-icon-present" />
              </div>
              <div class="gift-content">
                <h4>新用户福利</h4>
                <ul>
                  <li><strong>注册即送 100 Tokens</strong>，免费体验AI功能</li>
                  <li>首充享 <strong>8折优惠</strong>，超值划算</li>
                  <li>完成问卷解锁个性化学习方案</li>
                </ul>
              </div>
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
            <h2 class="form-title">创建账号</h2>
            <p class="form-subtitle">填写信息，开启智能学习之旅</p>
          </div>

          <el-form ref="registerForm" :model="registerForm" :rules="registerRules" class="register-form">
            <el-form-item prop="role">
              <el-select
                v-model="registerForm.role"
                placeholder="请选择身份"
                size="large"
                class="role-select"
                popper-class="role-popper"
              >
                <el-option label="🎓 学生" value="student" />
                <el-option label="👨‍🏫 教师" value="teacher" />
                <el-option label="⚙️ 管理员" value="admin" />
              </el-select>
            </el-form-item>

            <el-form-item prop="userName">
              <el-input
                v-model="registerForm.userName"
                placeholder="请输入用户名"
                prefix-icon="el-icon-user"
                size="large"
              />
            </el-form-item>

            <el-form-item prop="realName">
              <el-input
                v-model="registerForm.realName"
                placeholder="请输入真实姓名"
                prefix-icon="el-icon-postcard"
                size="large"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                :type="passwordType"
                v-model="registerForm.password"
                placeholder="请输入密码（至少6位）"
                prefix-icon="el-icon-lock"
                size="large"
              >
                <i
                  slot="suffix"
                  class="el-icon-view pwd-toggle"
                  @click="togglePassword"
                />
              </el-input>
            </el-form-item>

            <el-form-item prop="checkedPassword">
              <el-input
                :type="checkedPasswordType"
                v-model="registerForm.checkedPassword"
                placeholder="请确认密码"
                prefix-icon="el-icon-lock"
                size="large"
                @keyup.enter.native="registerFn"
              >
                <i
                  slot="suffix"
                  class="el-icon-view pwd-toggle"
                  @click="togglePassword2"
                />
              </el-input>
            </el-form-item>

            <div v-if="captchaEnabled" class="captcha-row">
              <el-form-item prop="code" class="captcha-input">
                <el-input
                  v-model="registerForm.code"
                  placeholder="验证码"
                  prefix-icon="el-icon-key"
                  size="large"
                  @keyup.enter.native="registerFn"
                />
              </el-form-item>
              <div class="captcha-img-box" @click="refreshCaptcha">
                <img :src="captchaUrl" alt="验证码" />
              </div>
            </div>

            <div v-if="showAdminTip" class="role-hint warning">
              <i class="el-icon-warning" />
              <span>教师/管理员身份需审核通过后才能登录</span>
            </div>
            <div v-if="registerForm.role === 'student'" class="role-hint success">
              <i class="el-icon-circle-check" />
              <span>学生注册后可直接登录，首次登录将引导完成备考目标设置</span>
            </div>

            <el-button
              class="submit-btn"
              type="primary"
              :loading="loading"
              @click="registerFn"
              size="large"
            >
              注 册
            </el-button>
          </el-form>

          <div class="switch-link">
            <span>已有账号？</span>
            <router-link to="/login">立即登录</router-link>
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
import { verifyCode, register } from '@/api/user'
import { Message } from 'element-ui'

export default {
  name: 'Register',
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

    const validateRealName = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入真实姓名'))
      } else {
        callback()
      }
    }

    const validateCheckedPassword = (rule, value, callback) => {
      if (value !== this.registerForm.password) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }

    const validateCode = (rule, value, callback) => {
      if (process.env.VUE_APP_CAPTCHA_ENABLED === 'false') {
        callback()
      } else if (!value) {
        callback(new Error('请输入验证码'))
      } else {
        callback()
      }
    }

    const validateRole = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请选择身份'))
      } else {
        callback()
      }
    }

    return {
      icpNumber: process.env.VUE_APP_ICP_NUMBER,
      icpLink: process.env.VUE_APP_ICP_LINK,
      registerForm: {
        userName: '',
        password: '',
        realName: '',
        checkedPassword: '',
        code: '',
        role: ''
      },
      registerRules: {
        role: [{ required: true, trigger: 'change', validator: validateRole }],
        userName: [{ required: true, trigger: 'blur', validator: validateUsername }],
        password: [{ required: true, trigger: 'blur', validator: validatePassword }],
        realName: [{ required: true, trigger: 'blur', validator: validateRealName }],
        checkedPassword: [{ required: true, trigger: 'blur', validator: validateCheckedPassword }],
        code: [{ required: true, trigger: 'blur', validator: validateCode }]
      },
      loading: false,
      passwordType: 'password',
      checkedPasswordType: 'password',
      captchaEnabled: process.env.VUE_APP_CAPTCHA_ENABLED !== 'false',
      captchaUrl: `/api/auths/captcha?${Math.random()}`
    }
  },
  computed: {
    showAdminTip() {
      return this.registerForm.role === 'teacher' || this.registerForm.role === 'admin'
    }
  },
  methods: {
    togglePassword() {
      this.passwordType = this.passwordType === 'password' ? '' : 'password'
    },
    togglePassword2() {
      this.checkedPasswordType = this.checkedPasswordType === 'password' ? '' : 'password'
    },
    refreshCaptcha() {
      this.captchaUrl = `/api/auths/captcha?${Math.random()}`
    },
    registerFn() {
      this.$refs.registerForm.validate(valid => {
        if (valid) {
          const doRegister = () => {
            const registerData = {
              userName: this.registerForm.userName,
              realName: this.registerForm.realName,
              password: this.registerForm.password,
              checkedPassword: this.registerForm.checkedPassword,
              roleType: this.registerForm.role
            }

            this.loading = true
            register(registerData).then((res2) => {
              if (res2.code) {
                let successMsg = res2.msg || '注册成功'
                Message({
                  message: successMsg,
                  type: 'success',
                  duration: 5 * 1000
                })
                if (this.registerForm.role === 'student') {
                  this.$router.push('/user/onboarding')
                } else {
                  this.$router.push('/login')
                }
              } else {
                this.refreshCaptcha()
                Message({
                  message: res2.msg,
                  type: 'error',
                  duration: 5 * 1000
                })
              }
            }).catch((error) => {
              this.refreshCaptcha()
              Message({
                message: error.msg || '注册失败，请重试',
                type: 'error',
                duration: 5 * 1000
              })
            }).finally(() => {
              this.loading = false
            })
          }

          if (this.captchaEnabled) {
            verifyCode(this.registerForm.code).then((res) => {
              if (res.code) {
                doRegister()
              } else {
                this.refreshCaptcha()
                Message({
                  message: res.msg || '验证码验证失败',
                  type: 'error',
                  duration: 5 * 1000
                })
              }
            }).catch(() => {
              this.refreshCaptcha()
              Message({
                message: '验证码验证失败',
                type: 'error',
                duration: 5 * 1000
              })
            })
          } else {
            doRegister()
          }
        } else {
          Message({
            message: '请填写完整的注册信息',
            type: 'warning',
            duration: 5 * 1000
          })
        }
      })
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
  min-height: 620px;
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

.register-tips {
  position: relative;
  z-index: 1;
  margin: 28px 0;

  .tip-card, .gift-card {
    padding: 20px 24px;
    background: var(--glass-bg);
    border: 1px solid var(--glass-border);
    border-radius: 16px;
    display: flex;
    gap: 16px;
    margin-bottom: 16px;

    .tip-icon, .gift-icon {
      width: 40px;
      height: 40px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;

      i {
        font-size: 20px;
        color: #fff;
      }
    }

    .tip-icon {
      background: var(--accent-gradient);
    }

    .gift-icon {
      background: linear-gradient(135deg, #f59e0b 0%, #ef4444 100%);
    }

    .tip-content, .gift-content {
      h4 {
        font-size: 15px;
        font-weight: 600;
        color: var(--text-primary);
        margin: 0 0 10px;
      }

      ul {
        margin: 0;
        padding: 0;
        list-style: none;

        li {
          font-size: 13px;
          color: var(--text-secondary);
          line-height: 1.8;
          padding-left: 16px;
          position: relative;

          &::before {
            content: '';
            position: absolute;
            left: 0;
            top: 8px;
            width: 6px;
            height: 6px;
            border-radius: 50%;
            background: var(--accent);
          }

          strong {
            color: var(--accent);
          }
        }
      }
    }

    .gift-content li::before {
      background: #f59e0b;
    }
  }
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
  margin-bottom: 28px;

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

.register-form {
  ::v-deep .el-form-item {
    margin-bottom: 16px;
  }

  ::v-deep .el-input__inner,
  ::v-deep .el-select .el-input__inner {
    height: 44px;
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

  ::v-deep .el-select .el-input__prefix {
    display: none;
  }

  ::v-deep .el-select .el-input__inner {
    padding-left: 16px;
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
  margin-bottom: 16px;

  .captcha-input {
    flex: 1;
    margin-bottom: 0;
  }

  .captcha-img-box {
    height: 44px;
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

.role-hint {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 12px;
  margin-bottom: 16px;

  i { font-size: 16px; }

  &.warning {
    background: rgba(245, 158, 11, 0.1);
    color: var(--warning);
    border: 1px solid rgba(245, 158, 11, 0.2);
  }

  &.success {
    background: rgba(16, 185, 129, 0.1);
    color: var(--success);
    border: 1px solid rgba(16, 185, 129, 0.2);
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
  margin-top: 20px;
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
  margin-top: 28px;
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
  .register-tips { margin: 20px 0; }
  .stats-row { gap: 20px; }
}
</style>