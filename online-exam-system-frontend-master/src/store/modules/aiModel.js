import { aiConfigGetModels } from '@/api/ai-config'

const state = {
  selectedConfigId: parseInt(localStorage.getItem('ai-config-id')) || null,
  selectedModelName: localStorage.getItem('ai-model-name') || '',
  availableModels: []
}

const mutations = {
  SET_SELECTED_MODEL(state, { configId, modelName }) {
    state.selectedConfigId = configId
    state.selectedModelName = modelName
    if (configId) {
      localStorage.setItem('ai-config-id', configId)
      localStorage.setItem('ai-model-name', modelName || '')
    } else {
      localStorage.removeItem('ai-config-id')
      localStorage.removeItem('ai-model-name')
    }
  },
  SET_AVAILABLE_MODELS(state, models) {
    state.availableModels = models
  }
}

const actions = {
  async loadModels({ commit, state }) {
    try {
      const res = await aiConfigGetModels()
      if (res && res.code === 1 && res.data) {
        commit('SET_AVAILABLE_MODELS', res.data)
        if (!state.selectedConfigId && res.data.length > 0) {
          const activeModel = res.data.find(m => m.isActive === 1 || m.isActive === true)
          if (activeModel) {
            commit('SET_SELECTED_MODEL', { configId: activeModel.id, modelName: activeModel.configName })
          } else {
            const first = res.data[0]
            commit('SET_SELECTED_MODEL', { configId: first.id, modelName: first.configName })
          }
        }
        return res.data
      }
    } catch (e) {
      console.error('加载AI模型列表失败:', e)
    }
    return []
  },
  selectModel({ commit, state }, { configId, modelName }) {
    console.log('[aiModel] selectModel: configId=', configId, ', modelName=', modelName)
    commit('SET_SELECTED_MODEL', { configId, modelName })
  },
  clearSelection({ commit }) {
    commit('SET_SELECTED_MODEL', { configId: null, modelName: '' })
  }
}

const getters = {
  currentConfigId: state => state.selectedConfigId,
  currentModelName: state => state.selectedModelName,
  modelList: state => state.availableModels
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}
