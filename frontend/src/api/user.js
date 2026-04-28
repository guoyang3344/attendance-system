import request from './request'

export function getUserList() {
  return request({
    url: '/user/list',
    method: 'get'
  })
}

export function getUserById(id) {
  return request({
    url: `/user/${id}`,
    method: 'get'
  })
}

export function getUserTree() {
  return request({
    url: '/user/tree',
    method: 'get'
  })
}

export function getSupervisor(id) {
  return request({
    url: `/user/${id}/supervisor`,
    method: 'get'
  })
}

export function getSubordinates(id) {
  return request({
    url: `/user/${id}/subordinates`,
    method: 'get'
  })
}

export function checkUsername(username, excludeId) {
  return request({
    url: '/user/check-username',
    method: 'get',
    params: {
      username,
      excludeId
    }
  })
}

export function createUser(data) {
  return request({
    url: '/user',
    method: 'post',
    data
  })
}

export function updateUser(data) {
  return request({
    url: '/user',
    method: 'put',
    data
  })
}

export function deleteUser(id) {
  return request({
    url: `/user/${id}`,
    method: 'delete'
  })
}

export function assignRoles(userId, roleIds) {
  return request({
    url: `/user/${userId}/roles`,
    method: 'put',
    data: {
      roleIds
    }
  })
}
