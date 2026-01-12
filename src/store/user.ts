import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import defaultAvatar from '/default-avatar.png'

export interface UserInfo {
  id?: number
  username: string
  token: string
  avatar?: string
  roles?: string[]
}

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<UserInfo>(JSON.parse(localStorage.getItem('userInfo') || `{"username": "游客", "token": "", "avatar": "${defaultAvatar}"}`))

  const name = computed(() => userInfo.value.username)
  const token = computed(() => userInfo.value.token)
  const avatar = computed(() => userInfo.value.avatar || defaultAvatar)
  const isLoggedIn = computed(() => !!userInfo.value.token)

  function login(data: UserInfo) {
    userInfo.value = data
    localStorage.setItem('userInfo', JSON.stringify(data))
  }

  function logout() {
    userInfo.value = { id: undefined, username: '游客', token: '', avatar: defaultAvatar }
    localStorage.removeItem('userInfo')
  }

  return { userInfo, name, token, avatar, isLoggedIn, login, logout }
})