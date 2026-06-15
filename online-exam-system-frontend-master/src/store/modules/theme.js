const state = {
  mode: localStorage.getItem('theme-mode') || 'dark'
}

const mutations = {
  SET_THEME_MODE(state, mode) {
    state.mode = mode
    localStorage.setItem('theme-mode', mode)
    document.documentElement.setAttribute('data-theme', mode)
  },
  TOGGLE_THEME(state) {
    const newMode = state.mode === 'dark' ? 'light' : 'dark'
    state.mode = newMode
    localStorage.setItem('theme-mode', newMode)
    document.documentElement.setAttribute('data-theme', newMode)
  }
}

const actions = {
  initTheme({ commit }) {
    const savedMode = localStorage.getItem('theme-mode') || 'dark'
    commit('SET_THEME_MODE', savedMode)
  },
  toggleTheme({ commit }) {
    commit('TOGGLE_THEME')
  },
  setThemeMode({ commit }, mode) {
    commit('SET_THEME_MODE', mode)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}