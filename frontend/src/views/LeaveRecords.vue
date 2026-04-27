<template>
  <div class="leave-records-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>我的请假记录</span>
          <el-button type="primary" size="small" @click="goToApply">
            <el-icon><Plus /></el-icon>
            申请请假
          </el-button>
        </div>
      </template>
      <el-table
        v-loading="loading"
        :data="records"
        stripe
        style="width: 100%"
        :empty-text="loading ? '加载中...' : '暂无请假记录'"
      >
        <el-table-column prop="leaveTypeName" label="请假类型" width="100" />
        <el-table-column label="请假时间" width="250">
          <template #default="{ row }">
            <div>{{ formatDateTime(row.startTime) }}</div>
            <div class="text-secondary">至 {{ formatDateTime(row.endTime) }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="leaveDays" label="天数" width="80" />
        <el-table-column prop="reason" label="请假原因" min-width="150" show-overflow-tooltip />
        <el-table-column prop="statusName" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.statusName)" size="small">
              {{ row.statusName || '待审批' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审批进度" min-width="150">
          <template #default="{ row }">
            <div v-if="row.currentApprovalLevelName">
              <span>当前审批：{{ row.currentApprovalLevelName }}</span>
              <div v-if="row.nextApproverName" class="text-secondary">
                下一个审批人：{{ row.nextApproverName }}
              </div>
            </div>
            <div v-else-if="row.statusName === '已通过' || row.statusName === '已驳回'">
              <span class="text-secondary">已完成</span>
            </div>
            <div v-else>
              <span class="text-secondary">等待审批</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination
          v-if="!loading && total > 0"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :current-page="currentPage"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="detailVisible"
      title="请假详情"
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-if="currentLeave" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="请假类型">{{ currentLeave.leaveTypeName }}</el-descriptions-item>
          <el-descriptions-item label="请假天数">{{ currentLeave.leaveDays }} 天</el-descriptions-item>
          <el-descriptions-item label="开始时间" :span="2">
            {{ formatDateTime(currentLeave.startTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="结束时间" :span="2">
            {{ formatDateTime(currentLeave.endTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="请假原因" :span="2">
            {{ currentLeave.reason }}
          </el-descriptions-item>
          <el-descriptions-item label="申请状态">
            <el-tag :type="getStatusType(currentLeave.statusName)" size="small">
              {{ currentLeave.statusName }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="当前审批">
            {{ currentLeave.currentApprovalLevelName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="下一个审批人" :span="2">
            {{ currentLeave.nextApproverName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentLeave.statusName === '已驳回'" label="驳回人">
            {{ currentLeave.rejectedByName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentLeave.statusName === '已驳回'" label="驳回时间">
            {{ formatDateTime(currentLeave.rejectedTime) }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentLeave.rejectedComment" label="驳回意见" :span="2">
            {{ currentLeave.rejectedComment }}
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="currentLeave.approvals && currentLeave.approvals.length > 0" class="approval-history">
          <h4>审批记录</h4>
          <el-timeline>
            <el-timeline-item
              v-for="(approval, index) in currentLeave.approvals"
              :key="approval.id"
              :timestamp="formatDateTime(approval.approvalTime)"
              placement="top"
              :type="getTimelineType(approval.approvalStatusName)"
              :icon="getTimelineIcon(approval.approvalStatusName)"
            >
              <el-card>
                <h4>{{ approval.approvalLevelName }} - {{ approval.approverName }}</h4>
                <p>
                  审批结果：
                  <el-tag :type="getApprovalStatusType(approval.approvalStatusName)" size="small">
                    {{ approval.approvalStatusName }}
                  </el-tag>
                </p>
                <p v-if="approval.approvalComment">审批意见：{{ approval.approvalComment }}</p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMyLeaves, getLeaveDetail } from '../api/leave'
import { Plus, Check, Close } from '@element-plus/icons-vue'

const router = useRouter()

const loading = ref(false)
const records = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const detailVisible = ref(false)
const currentLeave = ref(null)

const fetchRecords = async () => {
  loading.value = true
  try {
    const response = await getMyLeaves(currentPage.value, pageSize.value)
    if (response.code === 200) {
      records.value = response.data?.records || []
      total.value = response.data?.total || 0
    }
  } catch (error) {
    console.error('获取请假记录失败', error)
  } finally {
    loading.value = false
  }
}

const goToApply = () => {
  router.push('/leave-apply')
}

const viewDetail = async (row) => {
  try {
    const response = await getLeaveDetail(row.id)
    if (response.code === 200) {
      currentLeave.value = response.data
      detailVisible.value = true
    } else {
      ElMessage.error(response.message || '获取详情失败')
    }
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getStatusType = (statusName) => {
  const typeMap = {
    '待审批': 'info',
    '审批中': 'warning',
    '已通过': 'success',
    '已驳回': 'danger'
  }
  return typeMap[statusName] || 'info'
}

const getApprovalStatusType = (statusName) => {
  return statusName === '通过' ? 'success' : 'danger'
}

const getTimelineType = (statusName) => {
  return statusName === '通过' ? 'success' : 'danger'
}

const getTimelineIcon = (statusName) => {
  return statusName === '通过' ? Check : Close
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchRecords()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchRecords()
}

onMounted(() => {
  fetchRecords()
})
</script>

<style scoped>
.leave-records-container {
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

.text-secondary {
  color: #909399;
  font-size: 12px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.detail-content {
  padding: 10px;
}

.approval-history {
  margin-top: 20px;
}

.approval-history h4 {
  margin-bottom: 15px;
  color: #333;
  font-size: 15px;
}

.approval-history h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
}

.approval-history p {
  margin: 5px 0;
  font-size: 13px;
}
</style>
