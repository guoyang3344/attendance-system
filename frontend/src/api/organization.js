import request from './request'

export function getMyOrganizationInfo() {
  return request({
    url: '/organization/my-info',
    method: 'get'
  })
}

export function getOrganizationTree() {
  return request({
    url: '/organization/tree',
    method: 'get'
  })
}

export function getDepartmentTree() {
  return request({
    url: '/organization/department-tree',
    method: 'get'
  })
}

export function getUserOrganizationInfo(id) {
  return request({
    url: `/organization/user/${id}`,
    method: 'get'
  })
}

export function getUserLevel(userId) {
  return request({
    url: `/organization/level/${userId}`,
    method: 'get'
  })
}
