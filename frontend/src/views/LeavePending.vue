<template>
  <div class="leave-pending-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>待审批请假</span>
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
        :empty-text="loading ? '加载中...' : '暂无待审批的请假申请'"
      >
        <el-table-column prop="userName" label="申请人" width="100" />
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
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">
              详情
            </el-button>
            <el-button type="success" link size="small" @click="handleApprove(row, 1)">
              通过
            </el-button>
            <el-button type="danger" link size="small" @click="handleReject(row)">
              驳回
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
          <el-descriptions-item label="申请人">{{ currentLeave.userName }}</el-descriptions-item>
          <el-descriptions-item label="请假类型">{{ currentLeave.leaveTypeName }}</el-descriptions-item>
          <el-descriptions-item label="请假天数">{{ currentLeave.leaveDays }} 天</el-descriptions-item>
          <el-descriptions-item label="申请状态">
            <el-tag :type="getStatusType(currentLeave.statusName)" size="small">
              {{ currentLeave.statusName }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="开始时间" :span="2">
            {{ formatDateTime(currentLeave.startTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="结束时间" :span="2">
            {{ formatDateTime(currentLeave.endTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="请假原因" :span="2">
            {{ currentLeave.reason }}
          </el-descriptions-item>
          <el-descriptions-item label="当前审批级别">
            {{ currentLeave.currentApprovalLevelName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="下一个审批人">
            {{ currentLeave.nextApproverName || '-' }}
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
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailVisible = false">取消</el-button>
          <el-button type="success" @click="confirmApprove">通过</el-button>
          <el-button type="danger" @click="showRejectDialog">驳回</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="rejectVisible"
      title="驳回请假申请"
      width="400px"
    >
      <el-form label-width="80px">
        <el-form-item label="驳回意见">
          <el-input
            v-model="rejectComment"
            type="textarea"
            :rows="4"
            placeholder="请输入驳回意见（选填）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="rejectVisible = false">取消</el-button>
          <el-button type="danger" :loading="approveLoading" @click="confirmReject">确认驳回</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPendingLeaves, getLeaveDetail, approveLeave } from '../api/leave'
import { Refresh, Check, Close } from '@element-plus/icons-vue'

const loading = ref(false)
const records = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const detailVisible = ref(false)
const currentLeave = ref(null)
const rejectVisible = ref(false)
const rejectComment = ref('')
const approveLoading = ref(false)

const fetchRecords = async () => {
  loading.value = true
  try {
    const response = await getPendingLeaves(currentPage.value, pageSize.value)
    if (response.code === 200) {
      records.value = response.data?.records || []
      total.value = response.data?.total || 0
    }
  } catch (error) {
    console.error('获取待审批请假失败', error)
  } finally {
    loading.value = false
  }
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

const handleApprove = async (row, status) => {
  try {
    await ElMessageBox.confirm('确认通过该请假申请吗？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'success'
    })
    
    approveLoading.value = true
    const response = await approveLeave({
      leaveId: row.id,
      approvalStatus: status,
      approvalComment: ''
    })
    
    if (response.code === 200) {
      ElMessage.success('审批通过')
      fetchRecords()
    } else {
      ElMessage.error(response.message || '审批失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('审批失败')
    }
  } finally {
    approveLoading.value = false
  }
}

const handleReject = (row) => {
  currentLeave.value = row
  rejectComment.value = ''
  rejectVisible.value = true
}

const showRejectDialog = () => {
  rejectComment.value = ''
  rejectVisible.value = true
}

const confirmApprove = async () => {
  if (!currentLeave.value) return
  
  try {
    await ElMessageBox.confirm('确认通过该请假申请吗？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'success'
    })
    
    approveLoading.value = true
    const response = await approveLeave({
      leaveId: currentLeave.value.id,
      approvalStatus: 1,
      approvalComment: ''
    })
    
    if (response.code === 200) {
      ElMessage.success('审批通过')
      detailVisible.value = false
      fetchRecords()
    } else {
      ElMessage.error(response.message || '审批失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('审批失败')
    }
  } finally {
    approveLoading.value = false
  }
}

const confirmReject = async () => {
  if (!currentLeave.value) return
  
  try {
    await ElMessageBox.confirm('确认驳回该请假申请吗？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    approveLoading.value = true
    const response = await approveLeave({
      leaveId: currentLeave.value.id,
      approvalStatus: 2,
      approvalComment: rejectComment.value
    })
    
    if (response.code === 200) {
      ElMessage.success('已驳回')
      rejectVisible.value = false
      detailVisible.value = false
      fetchRecords()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  } finally {
    approveLoading.value = false
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
.leave-pending-container {
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

.dialog-footer {
  text-align: right;
}
</style>
