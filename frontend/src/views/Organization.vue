<template>
  <div class="organization-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>我的组织信息</span>
            </div>
          </template>
          <div v-loading="myInfoLoading" class="my-info">
            <template v-if="myInfo && myInfo.user">
              <div class="info-section">
                <h4 class="section-title">基本信息</h4>
                <div class="info-item">
                  <span class="info-label">姓名：</span>
                  <span class="info-value">{{ myInfo.user.realName }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">用户名：</span>
                  <span class="info-value">{{ myInfo.user.username }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">部门：</span>
                  <span class="info-value">{{ myInfo.user.deptName || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">职位：</span>
                  <span class="info-value">{{ myInfo.user.position || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">层级：</span>
                  <el-tag type="primary" size="small">第 {{ myLevel }} 层</el-tag>
                </div>
              </div>

              <div v-if="myInfo.supervisor" class="info-section">
                <h4 class="section-title">
                  <el-icon><ArrowUp /></el-icon>
                  上级领导
                </h4>
                <el-card class="user-card" shadow="hover">
                  <div class="user-header">
                    <el-avatar :size="50" icon="UserFilled" />
                    <div class="user-info">
                      <div class="user-name">{{ myInfo.supervisor.realName }}</div>
                      <div class="user-detail">{{ myInfo.supervisor.position || myInfo.supervisor.deptName || '领导' }}</div>
                    </div>
                  </div>
                </el-card>
              </div>

              <div class="info-section">
                <h4 class="section-title">
                  <el-icon><ArrowDown /></el-icon>
                  下属员工 ({{ myInfo.subordinates?.length || 0 }}人)
                </h4>
                <div v-if="myInfo.subordinates && myInfo.subordinates.length > 0" class="subordinate-list">
                  <div
                    v-for="sub in myInfo.subordinates"
                    :key="sub.id"
                    class="subordinate-item"
                    @click="viewUserDetail(sub)"
                  >
                    <el-avatar :size="40" icon="UserFilled" />
                    <div class="subordinate-info">
                      <div class="subordinate-name">{{ sub.realName }}</div>
                      <div class="subordinate-detail">{{ sub.position || sub.deptName || '员工' }}</div>
                    </div>
                  </div>
                </div>
                <el-empty v-else description="暂无下属" :image-size="60" />
              </div>
            </template>
            <el-empty v-else description="暂无数据" :image-size="80" />
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>公司组织架构</span>
              <el-radio-group v-model="treeType" size="small">
                <el-radio-button value="user">按人员</el-radio-button>
                <el-radio-button value="dept">按部门</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div v-loading="treeLoading" class="tree-container">
            <el-tree
              v-show="treeType === 'user'"
              :key="'user-tree'"
              :data="userTree"
              :props="userTreeProps"
              :default-expand-all="true"
              node-key="id"
              @node-click="handleUserNodeClick"
            >
              <template #default="{ node, data }">
                <div class="tree-node">
                  <el-avatar :size="24" icon="UserFilled" style="margin-right: 8px;" />
                  <span>{{ data.realName }}</span>
                  <el-tag v-if="data.position" :type="getPositionType(data.position)" size="small" style="margin-left: 8px;">
                    {{ data.position }}
                  </el-tag>
                  <el-tag v-if="data.deptName" type="info" size="small" style="margin-left: 8px;">
                    {{ data.deptName }}
                  </el-tag>
                </div>
              </template>
            </el-tree>
            <el-tree
              v-show="treeType === 'dept'"
              :key="'dept-tree'"
              :data="deptTree"
              :props="deptTreeProps"
              :default-expand-all="true"
              node-key="id"
            >
              <template #default="{ node, data }">
                <div class="tree-node">
                  <el-icon><OfficeBuilding /></el-icon>
                  <span style="margin-left: 8px;">{{ data.deptName }}</span>
                  <el-tag v-if="data.leaderName" type="primary" size="small" style="margin-left: 8px;">
                    负责人: {{ data.leaderName }}
                  </el-tag>
                </div>
              </template>
            </el-tree>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog
      v-model="userDetailVisible"
      title="员工详情"
      width="500px"
    >
      <div v-if="selectedUser" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">{{ selectedUser.realName }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ selectedUser.username }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ selectedUser.email || '-' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ selectedUser.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="部门">{{ selectedUser.deptName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="职位">{{ selectedUser.position || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="selectedUser.status === 1 ? 'success' : 'danger'" size="small">
              {{ selectedUser.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="角色">
            <el-tag
              v-for="role in selectedUser.roles"
              :key="role.id"
              :type="getRoleType(role.roleCode)"
              size="small"
              style="margin-right: 5px;"
            >
              {{ role.roleName }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="userDetailInfo" class="relation-section">
          <h4 class="section-title">组织关系</h4>
          <div v-if="userDetailInfo.supervisor" class="relation-item">
            <el-icon><ArrowUp /></el-icon>
            <span class="relation-label">上级：</span>
            <span class="relation-value">{{ userDetailInfo.supervisor.realName }}</span>
          </div>
          <div v-if="userDetailInfo.subordinates && userDetailInfo.subordinates.length > 0" class="relation-item">
            <el-icon><ArrowDown /></el-icon>
            <span class="relation-label">下属：</span>
            <span class="relation-value">
              {{ userDetailInfo.subordinates.map(s => s.realName).join('、') }}
            </span>
          </div>
          <div v-if="userLevel !== null" class="relation-item">
            <el-icon><TrendCharts /></el-icon>
            <span class="relation-label">层级：</span>
            <el-tag type="primary" size="small">第 {{ userLevel }} 层</el-tag>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '../store/user'
import { getMyOrganizationInfo, getOrganizationTree, getDepartmentTree, getUserOrganizationInfo, getUserLevel } from '../api/organization'
import { ArrowUp, ArrowDown, OfficeBuilding, TrendCharts } from '@element-plus/icons-vue'

const userStore = useUserStore()

const myInfoLoading = ref(false)
const treeLoading = ref(false)
const myInfo = ref(null)
const myLevel = ref(null)
const userTree = ref([])
const deptTree = ref([])
const treeType = ref('user')

const userDetailVisible = ref(false)
const selectedUser = ref(null)
const userDetailInfo = ref(null)
const userLevel = ref(null)

const userTreeProps = {
  label: 'realName',
  children: 'children'
}

const deptTreeProps = {
  label: 'deptName',
  children: 'children'
}

const fetchMyInfo = async () => {
  myInfoLoading.value = true
  try {
    const response = await getMyOrganizationInfo()
    if (response.code === 200 && response.data) {
      myInfo.value = response.data
      
      if (userStore.userId) {
        try {
          const levelResponse = await getUserLevel(userStore.userId)
          if (levelResponse.code === 200 && levelResponse.data) {
            myLevel.value = levelResponse.data.level
          }
        } catch (levelError) {
          console.warn('获取用户层级失败', levelError)
        }
      }
    }
  } catch (error) {
    console.error('获取个人组织信息失败', error)
    myInfo.value = null
  } finally {
    myInfoLoading.value = false
  }
}

const fetchUserTree = async () => {
  treeLoading.value = true
  try {
    const response = await getOrganizationTree()
    if (response.code === 200) {
      userTree.value = response.data || []
    } else {
      userTree.value = []
    }
  } catch (error) {
    console.error('获取用户树失败', error)
    userTree.value = []
  } finally {
    treeLoading.value = false
  }
}

const fetchDeptTree = async () => {
  treeLoading.value = true
  try {
    const response = await getDepartmentTree()
    if (response.code === 200) {
      deptTree.value = response.data || []
    } else {
      deptTree.value = []
    }
  } catch (error) {
    console.error('获取部门树失败', error)
    deptTree.value = []
  } finally {
    treeLoading.value = false
  }
}

const handleUserNodeClick = async (data) => {
  selectedUser.value = data
  userDetailInfo.value = null
  userLevel.value = null
  userDetailVisible.value = true

  try {
    const [detailResponse, levelResponse] = await Promise.all([
      getUserOrganizationInfo(data.id),
      getUserLevel(data.id)
    ])
    
    if (detailResponse.code === 200) {
      userDetailInfo.value = detailResponse.data
    }
    if (levelResponse.code === 200) {
      userLevel.value = levelResponse.data?.level
    }
  } catch (error) {
    console.error('获取用户详情失败', error)
  }
}

const viewUserDetail = async (user) => {
  await handleUserNodeClick(user)
}

const getPositionType = (position) => {
  if (!position) return 'info'
  const typeMap = {
    '董事长': 'danger',
    '总经理': 'warning',
    '经理': 'warning',
    '组长': 'success',
    '主管': 'success'
  }
  return typeMap[position] || 'primary'
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

onMounted(() => {
  fetchMyInfo()
  fetchUserTree()
  fetchDeptTree()
})
</script>

<style scoped>
.organization-container {
  padding: 10px;
}

.box-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.my-info {
  padding: 10px;
}

.info-section {
  margin-bottom: 20px;
}

.info-section:last-child {
  margin-bottom: 0;
}

.section-title {
  margin: 0 0 15px 0;
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 5px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  font-size: 14px;
}

.info-label {
  color: #909399;
  min-width: 60px;
}

.info-value {
  color: #303133;
  font-weight: 500;
}

.user-card {
  padding: 15px;
  background: #f5f7fa;
}

.user-header {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 5px;
}

.user-detail {
  font-size: 13px;
  color: #909399;
}

.subordinate-list {
  max-height: 300px;
  overflow-y: auto;
}

.subordinate-item {
  display: flex;
  align-items: center;
  padding: 10px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.subordinate-item:hover {
  background-color: #f5f7fa;
}

.subordinate-info {
  margin-left: 10px;
}

.subordinate-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.subordinate-detail {
  font-size: 12px;
  color: #909399;
}

.tree-container {
  min-height: 400px;
  padding: 10px;
}

.tree-node {
  display: flex;
  align-items: center;
  padding: 5px 0;
}

.detail-content {
  padding: 10px;
}

.relation-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.relation-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  font-size: 14px;
}

.relation-label {
  color: #909399;
  margin: 0 8px;
}

.relation-value {
  color: #303133;
  font-weight: 500;
}
</style>
