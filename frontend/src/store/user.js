import { defineStore } from 'pinia'
import { login, logout, getUserInfo } from '../api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    userId: null,
    username: '',
    realName: '',
    roles: [],
    permissions: [],
    isLoggedIn: false
  }),

  getters: {
    hasRole: (state) => (roleCode) => {
      return state.roles.some(role => role.roleCode === roleCode)
    },
    hasPermission: (state) => (permissionCode) => {
      return state.permissions.includes(permissionCode)
    },
    isChairman: (state) => {
      return state.permissions.includes('attendance:view:all')
    }
  },

  actions: {
    async login(username, password) {
      try {
        const response = await login(username, password)
        if (response.code === 200) {
          const data = response.data
          this.userId = data.userId
          this.username = data.username
          this.realName = data.realName
          this.roles = data.roles
          this.permissions = data.permissions
          this.isLoggedIn = true
          return { success: true }
        } else {
          return { success: false, message: response.message }
        }
      } catch (error) {
        return { success: false, message: '登录失败，请稍后重试' }
      }
    },

    async logout() {
      try {
        await logout()
      } finally {
        this.resetUser()
      }
    },

    async fetchUserInfo() {
      try {
        const response = await getUserInfo()
        if (response.code === 200) {
          const data = response.data
          this.userId = data.userId
          this.username = data.username
          this.realName = data.realName
          this.roles = data.roles
          this.permissions = data.permissions
          this.isLoggedIn = true
          return { success: true }
        } else {
          this.resetUser()
          return { success: false }
        }
      } catch (error) {
        this.resetUser()
        return { success: false }
      }
    },

    resetUser() {
      this.userId = null
      this.username = ''
      this.realName = ''
      this.roles = []
      this.permissions = []
      this.isLoggedIn = false
    }
  }
})
