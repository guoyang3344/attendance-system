<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>今日打卡状态</span>
            </div>
          </template>
          <div v-if="todayRecord" class="today-status">
            <div class="status-item">
              <span class="label">上班打卡：</span>
              <span :class="['value', getPunchInStatusClass()]">
                {{ todayRecord.punchInTime ? formatTime(todayRecord.punchInTime) : '未打卡' }}
              </span>
              <el-tag v-if="todayRecord.punchInTime && todayRecord.status === 1" type="danger" size="small">迟到</el-tag>
            </div>
            <div class="status-item">
              <span class="label">下班打卡：</span>
              <span :class="['value', getPunchOutStatusClass()]">
                {{ todayRecord.punchOutTime ? formatTime(todayRecord.punchOutTime) : '未打卡' }}
              </span>
              <el-tag v-if="todayRecord.punchOutTime && (todayRecord.status === 2 || todayRecord.status === 3)" type="warning" size="small">
                {{ todayRecord.status === 2 ? '早退' : '旷工' }}
              </el-tag>
            </div>
            <div class="status-item">
              <span class="label">当前状态：</span>
              <el-tag :type="getStatusType(todayRecord.statusName)">
                {{ todayRecord.statusName }}
              </el-tag>
            </div>
          </div>
          <div v-else class="no-record">
            <el-empty description="今日暂无打卡记录" :image-size="80">
              <el-button type="primary" @click="goToAttendance">去打卡</el-button>
            </el-empty>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>用户信息</span>
            </div>
          </template>
          <div class="user-info">
            <div class="info-item">
              <span class="info-label">用户名：</span>
              <span class="info-value">{{ userStore.username }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">真实姓名：</span>
              <span class="info-value">{{ userStore.realName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">角色：</span>
              <span class="info-value">
                <el-tag v-for="role in userStore.roles" :key="role.id" :type="getRoleType(role.roleCode)" class="role-tag">
                  {{ role.roleName }}
                </el-tag>
              </span>
            </div>
            <div v-if="userStore.isChairman" class="info-item">
              <el-tag type="success">拥有查看所有员工打卡记录的权限</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>快捷操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" size="large" @click="goToAttendance">
              <el-icon><Document /></el-icon>
              打卡
            </el-button>
            <el-button type="success" size="large" @click="goToRecords">
              <el-icon><List /></el-icon>
              查看我的记录
            </el-button>
            <el-button v-if="userStore.isChairman" type="warning" size="large" @click="goToAllRecords">
              <el-icon><DataAnalysis /></el-icon>
              查看所有记录
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { getTodayRecord } from '../api/attendance'
import { Document, List, DataAnalysis } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const todayRecord = ref(null)

const fetchTodayRecord = async () => {
  try {
    const response = await getTodayRecord()
    if (response.code === 200) {
      todayRecord.value = response.data
    }
  } catch (error) {
    console.error('获取今日打卡记录失败', error)
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
}

const getPunchInStatusClass = () => {
  if (!todayRecord.value?.punchInTime) return 'not-punched'
  return todayRecord.value.status === 1 ? 'late' : 'normal'
}

const getPunchOutStatusClass = () => {
  if (!todayRecord.value?.punchOutTime) return 'not-punched'
  return (todayRecord.value.status === 2 || todayRecord.value.status === 3) ? 'early' : 'normal'
}

const getStatusType = (statusName) => {
  const typeMap = {
    '正常': 'success',
    '迟到': 'danger',
    '早退': 'warning',
    '旷工': 'info'
  }
  return typeMap[statusName] || 'info'
}

const getRoleType = (roleCode) => {
  const typeMap = {
    'EMPLOYEE': 'primary',
    'TEAM_LEADER': 'success',
    'MANAGER': 'warning',
    'CHAIRMAN': 'danger'
  }
  return typeMap[roleCode] || 'info'
}

const goToAttendance = () => {
  router.push('/attendance')
}

const goToRecords = () => {
  router.push('/records')
}

const goToAllRecords = () => {
  router.push('/all-records')
}

onMounted(() => {
  fetchTodayRecord()
})
</script>

<style scoped>
.dashboard-container {
  padding: 10px;
}

.box-card {
  margin-bottom: 20px;
}

.card-header {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.today-status {
  padding: 10px 0;
}

.status-item {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  font-size: 15px;
}

.status-item:last-child {
  margin-bottom: 0;
}

.label {
  color: #606266;
  margin-right: 10px;
}

.value {
  font-weight: 500;
}

.value.normal {
  color: #67c23a;
}

.value.late {
  color: #f56c6c;
}

.value.early {
  color: #e6a23c;
}

.value.not-punched {
  color: #909399;
}

.status-item .el-tag {
  margin-left: 10px;
}

.no-record {
  text-align: center;
  padding: 20px 0;
}

.user-info {
  padding: 10px 0;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  font-size: 15px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-label {
  color: #606266;
  margin-right: 10px;
  min-width: 80px;
}

.info-value {
  color: #303133;
}

.role-tag {
  margin-right: 5px;
}

.quick-actions {
  display: flex;
  gap: 20px;
  padding: 10px 0;
}

.quick-actions .el-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 15px 30px;
}
</style>
