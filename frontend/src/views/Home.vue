<template>
  <el-container class="home-container">
    <el-aside width="220px" class="home-aside">
      <div class="logo">
        <span>员工打卡系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
        :collapse="false"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Grid /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/attendance">
          <el-icon><Document /></el-icon>
          <span>打卡</span>
        </el-menu-item>
        <el-menu-item index="/records">
          <el-icon><List /></el-icon>
          <span>我的记录</span>
        </el-menu-item>
        <el-menu-item v-if="userStore.isChairman" index="/all-records">
          <el-icon><DataAnalysis /></el-icon>
          <span>所有记录</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="home-header">
        <div class="header-left">
          <span>欢迎您，{{ userStore.realName }}</span>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><User /></el-icon>
              {{ userStore.realName }}
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="home-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import {
  Grid,
  Document,
  List,
  DataAnalysis,
  User,
  ArrowDown
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.home-container {
  height: 100vh;
}

.home-aside {
  background-color: #304156;
  transition: width 0.3s;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: 500;
  background-color: #263445;
}

.el-menu {
  border-right: none;
}

.home-header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  font-size: 16px;
  color: #333;
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  color: #333;
}

.home-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
