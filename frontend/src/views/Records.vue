<template>
  <div class="records-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>我的打卡记录</span>
          <el-button type="primary" size="small" @click="fetchRecords">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      <el-table
        v-loading="loading"
        :data="records"
        stripe
        style="width: 100%"
        :empty-text="loading ? '加载中...' : '暂无打卡记录'"
      >
        <el-table-column prop="punchDate" label="日期" width="150">
          <template #default="{ row }">
            {{ formatDate(row.punchDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="punchInTime" label="上班打卡时间" width="180">
          <template #default="{ row }">
            <span v-if="row.punchInTime">{{ formatTime(row.punchInTime) }}</span>
            <span v-else class="no-punch">未打卡</span>
          </template>
        </el-table-column>
        <el-table-column prop="punchOutTime" label="下班打卡时间" width="180">
          <template #default="{ row }">
            <span v-if="row.punchOutTime">{{ formatTime(row.punchOutTime) }}</span>
            <span v-else class="no-punch">未打卡</span>
          </template>
        </el-table-column>
        <el-table-column prop="statusName" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.statusName)" size="small">
              {{ row.statusName || '未完成' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="工作时长" width="120">
          <template #default="{ row }">
            <span v-if="row.punchInTime && row.punchOutTime">
              {{ calculateWorkHours(row.punchInTime, row.punchOutTime) }}
            </span>
            <span v-else class="no-punch">-</span>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!loading && records.length === 0" class="empty-container">
        <el-empty description="暂无打卡记录" :image-size="80" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getSelfRecords } from '../api/attendance'
import { Refresh } from '@element-plus/icons-vue'

const loading = ref(false)
const records = ref([])

const fetchRecords = async () => {
  loading.value = true
  try {
    const response = await getSelfRecords()
    if (response.code === 200) {
      records.value = response.data || []
    }
  } catch (error) {
    console.error('获取打卡记录失败', error)
  } finally {
    loading.value = false
  }
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

const formatTime = (time) => {
  if (!time) return ''
  const t = new Date(time)
  return t.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
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

const calculateWorkHours = (punchInTime, punchOutTime) => {
  if (!punchInTime || !punchOutTime) return ''
  const start = new Date(punchInTime).getTime()
  const end = new Date(punchOutTime).getTime()
  const diff = end - start
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  return `${hours}小时${minutes}分钟`
}

onMounted(() => {
  fetchRecords()
})
</script>

<style scoped>
.records-container {
  padding: 10px;
}

.box-card {
  max-width: 1000px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.no-punch {
  color: #c0c4cc;
}

.empty-container {
  padding: 40px 0;
}
</style>
