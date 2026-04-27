<template>
  <div class="attendance-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>打卡</span>
        </div>
      </template>
      <div class="current-time">
        <div class="date">{{ currentDate }}</div>
        <div class="time">{{ currentTime }}</div>
      </div>
      <div class="punch-status" v-if="todayRecord">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="punch-card" :class="punchInCardClass">
              <div class="punch-type">上班打卡</div>
              <div class="punch-time" v-if="todayRecord.punchInTime">
                {{ formatTime(todayRecord.punchInTime) }}
              </div>
              <div class="punch-time empty" v-else>
                未打卡
              </div>
              <div class="punch-status-text">
                <span v-if="todayRecord.punchInTime && todayRecord.status === 1" class="status-bad">迟到</span>
                <span v-else-if="todayRecord.punchInTime" class="status-good">已打卡</span>
                <span v-else class="status-normal">待打卡</span>
              </div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="punch-card" :class="punchOutCardClass">
              <div class="punch-type">下班打卡</div>
              <div class="punch-time" v-if="todayRecord.punchOutTime">
                {{ formatTime(todayRecord.punchOutTime) }}
              </div>
              <div class="punch-time empty" v-else>
                未打卡
              </div>
              <div class="punch-status-text">
                <span v-if="todayRecord.punchOutTime && (todayRecord.status === 2 || todayRecord.status === 3)" class="status-bad">
                  {{ todayRecord.status === 2 ? '早退' : '旷工' }}
                </span>
                <span v-else-if="todayRecord.punchOutTime" class="status-good">已打卡</span>
                <span v-else class="status-normal">待打卡</span>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
      <div class="no-record" v-else>
        <el-empty description="今日暂无打卡记录，请开始打卡" :image-size="80" />
      </div>
      <div class="punch-buttons">
        <el-button
          type="primary"
          size="large"
          :loading="punchInLoading"
          :disabled="!!todayRecord?.punchInTime"
          @click="handlePunchIn"
          class="punch-btn"
        >
          <el-icon><DocumentAdd /></el-icon>
          上班打卡
        </el-button>
        <el-button
          type="success"
          size="large"
          :loading="punchOutLoading"
          :disabled="!todayRecord?.punchInTime || !!todayRecord?.punchOutTime"
          @click="handlePunchOut"
          class="punch-btn"
        >
          <el-icon><DocumentChecked /></el-icon>
          下班打卡
        </el-button>
      </div>
      <div class="work-time-info">
        <el-alert
          title="工作时间：9:00 - 18:00"
          type="info"
          :closable="false"
          show-icon
        >
          <template #default>
            <span>迟到：超过9:00打卡上班 | 早退：早于18:00打卡下班</span>
          </template>
        </el-alert>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { punchIn, punchOut, getTodayRecord } from '../api/attendance'
import { DocumentAdd, DocumentChecked } from '@element-plus/icons-vue'

const currentDate = ref('')
const currentTime = ref('')
const todayRecord = ref(null)
const punchInLoading = ref(false)
const punchOutLoading = ref(false)

let timeInterval = null

const updateTime = () => {
  const now = new Date()
  currentDate.value = now.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
  currentTime.value = now.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

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

const punchInCardClass = computed(() => {
  if (!todayRecord.value?.punchInTime) return ''
  return todayRecord.value.status === 1 ? 'bad' : 'good'
})

const punchOutCardClass = computed(() => {
  if (!todayRecord.value?.punchOutTime) return ''
  return (todayRecord.value.status === 2 || todayRecord.value.status === 3) ? 'bad' : 'good'
})

const handlePunchIn = async () => {
  punchInLoading.value = true
  try {
    const response = await punchIn()
    if (response.code === 200) {
      ElMessage.success('上班打卡成功')
      await fetchTodayRecord()
    } else {
      ElMessage.error(response.message || '打卡失败')
    }
  } catch (error) {
    ElMessage.error('打卡失败，请稍后重试')
  } finally {
    punchInLoading.value = false
  }
}

const handlePunchOut = async () => {
  punchOutLoading.value = true
  try {
    const response = await punchOut()
    if (response.code === 200) {
      ElMessage.success('下班打卡成功')
      await fetchTodayRecord()
    } else {
      ElMessage.error(response.message || '打卡失败')
    }
  } catch (error) {
    ElMessage.error('打卡失败，请稍后重试')
  } finally {
    punchOutLoading.value = false
  }
}

onMounted(() => {
  updateTime()
  timeInterval = setInterval(updateTime, 1000)
  fetchTodayRecord()
})

onUnmounted(() => {
  if (timeInterval) {
    clearInterval(timeInterval)
  }
})
</script>

<style scoped>
.attendance-container {
  padding: 10px;
}

.box-card {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.current-time {
  text-align: center;
  padding: 30px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  margin-bottom: 20px;
}

.current-time .date {
  color: rgba(255, 255, 255, 0.8);
  font-size: 16px;
  margin-bottom: 10px;
}

.current-time .time {
  color: #fff;
  font-size: 48px;
  font-weight: 600;
  letter-spacing: 2px;
}

.punch-status {
  margin-bottom: 20px;
}

.punch-card {
  text-align: center;
  padding: 25px;
  border-radius: 8px;
  background-color: #f5f7fa;
  border: 2px solid #e4e7ed;
  transition: all 0.3s;
}

.punch-card.good {
  border-color: #67c23a;
  background-color: #f0f9eb;
}

.punch-card.bad {
  border-color: #f56c6c;
  background-color: #fef0f0;
}

.punch-type {
  font-size: 16px;
  color: #606266;
  margin-bottom: 15px;
}

.punch-time {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 10px;
}

.punch-time.empty {
  color: #c0c4cc;
}

.punch-status-text {
  font-size: 14px;
}

.status-good {
  color: #67c23a;
  font-weight: 500;
}

.status-bad {
  color: #f56c6c;
  font-weight: 500;
}

.status-normal {
  color: #909399;
}

.no-record {
  text-align: center;
  padding: 20px 0;
}

.punch-buttons {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-top: 30px;
  margin-bottom: 20px;
}

.punch-btn {
  min-width: 160px;
  padding: 15px 30px;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.work-time-info {
  margin-top: 20px;
}
</style>
