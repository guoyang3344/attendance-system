<template>
  <div class="role-manage-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button type="primary" size="small" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增角色
          </el-button>
        </div>
      </template>
      <el-table
        v-loading="loading"
        :data="roles"
        stripe
        style="width: 100%"
        :empty-text="loading ? '加载中...' : '暂无角色数据'"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleCode" label="角色编码" width="150" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="warning" link size="small" @click="handlePermission(row)">
              权限分配
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑角色' : '新增角色'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="formData.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="formData.roleCode" placeholder="请输入角色编码（英文）" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入角色描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="permissionVisible"
      title="权限分配"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-tree
        ref="treeRef"
        :data="permissionTree"
        :props="treeProps"
        show-checkbox
        node-key="id"
        default-expand-all
        :check-strictly="true"
      />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="permissionVisible = false">取消</el-button>
          <el-button type="primary" :loading="permissionLoading" @click="handlePermissionSubmit">
            保存
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoleList, createRole, updateRole, deleteRole, getRolePermissions, assignPermissions } from '../api/role'
import { Plus } from '@element-plus/icons-vue'

const loading = ref(false)
const roles = ref([])
const dialogVisible = ref(false)
const permissionVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const permissionLoading = ref(false)
const formRef = ref(null)
const treeRef = ref(null)
const currentRoleId = ref(null)

const formData = reactive({
  id: null,
  roleName: '',
  roleCode: '',
  description: ''
})

const rules = {
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' }
  ],
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' }
  ]
}

const treeProps = {
  label: 'permissionName',
  children: 'children'
}

const permissionTree = ref([])

const fetchRoles = async () => {
  loading.value = true
  try {
    const response = await getRoleList()
    if (response.code === 200) {
      roles.value = response.data || []
    }
  } catch (error) {
    console.error('获取角色列表失败', error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  formData.id = null
  formData.roleName = ''
  formData.roleCode = ''
  formData.description = ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  formData.id = row.id
  formData.roleName = row.roleName
  formData.roleCode = row.roleCode
  formData.description = row.description
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    let response
    if (isEdit.value) {
      response = await updateRole(formData)
    } else {
      response = await createRole(formData)
    }

    if (response.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      fetchRoles()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该角色吗？删除后无法恢复。', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await deleteRole(row.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      fetchRoles()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handlePermission = async (row) => {
  currentRoleId.value = row.id
  permissionLoading.value = true
  try {
    const response = await getRolePermissions(row.id)
    if (response.code === 200) {
      const permissions = response.data || []
      const checkedKeys = permissions.map(p => p.id)
      
      permissionTree.value = buildPermissionTree()
      setTimeout(() => {
        treeRef.value.setCheckedKeys(checkedKeys)
      }, 100)
      
      permissionVisible.value = true
    } else {
      ElMessage.error(response.message || '获取权限失败')
    }
  } catch (error) {
    ElMessage.error('获取权限失败')
  } finally {
    permissionLoading.value = false
  }
}

const buildPermissionTree = () => {
  return [
    {
      id: 1,
      permissionName: '系统登录',
      permissionCode: 'system:login',
      children: []
    },
    {
      id: 2,
      permissionName: '打卡管理',
      permissionCode: 'attendance',
      children: [
        {
          id: 3,
          permissionName: '打卡',
          permissionCode: 'attendance:punch',
          children: []
        },
        {
          id: 4,
          permissionName: '查看个人打卡记录',
          permissionCode: 'attendance:view:self',
          children: []
        },
        {
          id: 5,
          permissionName: '查看所有打卡记录',
          permissionCode: 'attendance:view:all',
          children: []
        }
      ]
    },
    {
      id: 6,
      permissionName: '请假管理',
      permissionCode: 'leave',
      children: [
        {
          id: 7,
          permissionName: '申请请假',
          permissionCode: 'leave:apply',
          children: []
        },
        {
          id: 8,
          permissionName: '查看个人请假记录',
          permissionCode: 'leave:view:self',
          children: []
        },
        {
          id: 9,
          permissionName: '查看待审批请假',
          permissionCode: 'leave:view:pending',
          children: []
        },
        {
          id: 10,
          permissionName: '审批请假',
          permissionCode: 'leave:approve',
          children: []
        },
        {
          id: 11,
          permissionName: '驳回请假',
          permissionCode: 'leave:reject',
          children: []
        }
      ]
    },
    {
      id: 12,
      permissionName: '系统管理',
      permissionCode: 'system',
      children: [
        {
          id: 13,
          permissionName: '用户管理',
          permissionCode: 'system:user',
          children: []
        },
        {
          id: 14,
          permissionName: '角色管理',
          permissionCode: 'system:role',
          children: []
        },
        {
          id: 15,
          permissionName: '部门管理',
          permissionCode: 'system:department',
          children: []
        },
        {
          id: 16,
          permissionName: '查看组织架构',
          permissionCode: 'system:organization:view',
          children: []
        }
      ]
    }
  ]
}

const handlePermissionSubmit = async () => {
  if (!currentRoleId.value) return

  const checkedKeys = treeRef.value.getCheckedKeys()
  permissionLoading.value = true
  try {
    const response = await assignPermissions(currentRoleId.value, checkedKeys)
    if (response.code === 200) {
      ElMessage.success('权限分配成功')
      permissionVisible.value = false
    } else {
      ElMessage.error(response.message || '权限分配失败')
    }
  } catch (error) {
    ElMessage.error('权限分配失败')
  } finally {
    permissionLoading.value = false
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

onMounted(() => {
  fetchRoles()
})
</script>

<style scoped>
.role-manage-container {
  padding: 10px;
}

.box-card {
  max-width: 1200px;
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

.dialog-footer {
  text-align: right;
}
</style>
