import request from './request'

export function getRoleList() {
  return request({
    url: '/role/list',
    method: 'get'
  })
}

export function getRoleById(id) {
  return request({
    url: `/role/${id}`,
    method: 'get'
  })
}

export function getRolePermissions(id) {
  return request({
    url: `/role/${id}/permissions`,
    method: 'get'
  })
}

export function createRole(data) {
  return request({
    url: '/role',
    method: 'post',
    data
  })
}

export function updateRole(data) {
  return request({
    url: '/role',
    method: 'put',
    data
  })
}

export function assignPermissions(roleId, permissionIds) {
  return request({
    url: `/role/${roleId}/permissions`,
    method: 'put',
    data: {
      permissionIds
    }
  })
}

export function deleteRole(id) {
  return request({
    url: `/role/${id}`,
    method: 'delete'
  })
}
