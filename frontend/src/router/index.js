import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'attendance',
        name: 'Attendance',
        component: () => import('../views/Attendance.vue'),
        meta: { title: '打卡' }
      },
      {
        path: 'records',
        name: 'Records',
        component: () => import('../views/Records.vue'),
        meta: { title: '打卡记录' }
      },
      {
        path: 'all-records',
        name: 'AllRecords',
        component: () => import('../views/AllRecords.vue'),
        meta: { title: '所有打卡记录' }
      },
      {
        path: 'leave-apply',
        name: 'LeaveApply',
        component: () => import('../views/LeaveApply.vue'),
        meta: { title: '申请请假' }
      },
      {
        path: 'leave-records',
        name: 'LeaveRecords',
        component: () => import('../views/LeaveRecords.vue'),
        meta: { title: '我的请假' }
      },
      {
        path: 'leave-pending',
        name: 'LeavePending',
        component: () => import('../views/LeavePending.vue'),
        meta: { title: '待审批' }
      },
      {
        path: 'organization',
        name: 'Organization',
        component: () => import('../views/Organization.vue'),
        meta: { title: '组织架构' }
      },
      {
        path: 'employee-manage',
        name: 'EmployeeManage',
        component: () => import('../views/EmployeeManage.vue'),
        meta: { title: '员工管理' }
      },
      {
        path: 'role-manage',
        name: 'RoleManage',
        component: () => import('../views/RoleManage.vue'),
        meta: { title: '角色管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title || '员工打卡系统'
  const userStore = useUserStore()
  
  if (to.path === '/login') {
    if (userStore.isLoggedIn) {
      next('/')
    } else {
      next()
    }
  } else {
    if (userStore.isLoggedIn) {
      next()
    } else {
      next('/login')
    }
  }
})

export default router
