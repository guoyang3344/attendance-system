<template>
  <div class="leave-apply-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>请假申请</span>
        </div>
      </template>
      <el-form
        ref="leaveFormRef"
        :model="leaveForm"
        :rules="leaveRules"
        label-width="120px"
        style="max-width: 600px; margin: 0 auto;"
      >
        <el-form-item label="请假类型" prop="leaveTypeId">
          <el-select
            v-model="leaveForm.leaveTypeId"
            placeholder="请选择请假类型"
            style="width: 100%"
            @change="handleTypeChange"
          >
            <el-option
              v-for="type in leaveTypes"
              :key="type.id"
              :label="type.typeName"
              :value="type.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="leaveForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%"
            :disabled-date="disabledDate"
            @change="calculateDays"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="leaveForm.endTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 100%"
            :disabled-date="disabledDate"
            @change="calculateDays"
          />
        </el-form-item>
        <el-form-item label="请假天数" prop="leaveDays">
          <el-input-number
            v-model="leaveForm.leaveDays"
            :min="0.5"
            :max="365"
            :step="0.5"
            placeholder="自动计算，可手动调整"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="请假原因" prop="reason">
          <el-input
            v-model="leaveForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入请假原因"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item v-if="nextApproverName">
          <div class="approval-info">
            <el-alert
              :title="`提交后将由 ${nextApproverName} 进行审批`"
              type="info"
              :closable="false"
              show-icon
            />
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">
            提交申请
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getLeaveTypes, submitLeave, getMyApprovalLevel } from '../api/leave'

const router = useRouter()

const leaveFormRef = ref(null)
const loading = ref(false)
const leaveTypes = ref([])
const nextApproverName = ref('')

const leaveForm = reactive({
  leaveTypeId: null,
  leaveTypeName: '',
  startTime: null,
  endTime: null,
  leaveDays: 0.5,
  reason: ''
})

const leaveRules = {
  leaveTypeId: [
    { required: true, message: '请选择请假类型', trigger: 'change' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ],
  leaveDays: [
    { required: true, message: '请输入请假天数', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请输入请假原因', trigger: 'blur' },
    { min: 2, max: 500, message: '请假原因长度在 2 到 500 个字符', trigger: 'blur' }
  ]
}

const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

const fetchLeaveTypes = async () => {
  try {
    const response = await getLeaveTypes()
    if (response.code === 200) {
      leaveTypes.value = response.data || []
    }
  } catch (error) {
    console.error('获取请假类型失败', error)
  }
}

const fetchApprovalInfo = async () => {
  try {
    const response = await getMyApprovalLevel()
    if (response.code === 200) {
      const level = response.data.level
      if (level) {
        nextApproverName.value = response.data.nextApproverName || '组长'
      } else {
        nextApproverName.value = '组长'
      }
    }
  } catch (error) {
    console.error('获取审批信息失败', error)
  }
}

const handleTypeChange = (value) => {
  const type = leaveTypes.value.find(t => t.id === value)
  if (type) {
    leaveForm.leaveTypeName = type.typeName
  }
}

const calculateDays = () => {
  if (leaveForm.startTime && leaveForm.endTime) {
    const start = new Date(leaveForm.startTime).getTime()
    const end = new Date(leaveForm.endTime).getTime()
    if (end > start) {
      const diff = end - start
      const days = Math.ceil(diff / (1000 * 60 * 60 * 24) * 10) / 10
      leaveForm.leaveDays = Math.max(days, 0.5)
    }
  }
}

const handleSubmit = async () => {
  const valid = await leaveFormRef.value.validate().catch(() => false)
  if (!valid) return

  if (leaveForm.startTime && leaveForm.endTime) {
    const start = new Date(leaveForm.startTime).getTime()
    const end = new Date(leaveForm.endTime).getTime()
    if (end <= start) {
      ElMessage.error('结束时间必须晚于开始时间')
      return
    }
  }

  loading.value = true
  try {
    const response = await submitLeave(leaveForm)
    if (response.code === 200) {
      ElMessage.success('请假申请提交成功')
      router.push('/leave-records')
    } else {
      ElMessage.error(response.message || '提交失败')
    }
  } catch (error) {
    ElMessage.error('提交失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  leaveFormRef.value.resetFields()
}

onMounted(() => {
  fetchLeaveTypes()
  fetchApprovalInfo()
})
</script>

<style scoped>
.leave-apply-container {
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

.approval-info {
  width: 100%;
}
</style>
