import request from './request'

export function getDepartmentTree() {
  return request({
    url: '/department/tree',
    method: 'get'
  })
}

export function getDepartmentList() {
  return request({
    url: '/department/list',
    method: 'get'
  })
}

export function getDepartmentById(id) {
  return request({
    url: `/department/${id}`,
    method: 'get'
  })
}

export function createDepartment(data) {
  return request({
    url: '/department',
    method: 'post',
    data
  })
}

export function updateDepartment(data) {
  return request({
    url: '/department',
    method: 'put',
    data
  })
}

export function deleteDepartment(id) {
  return request({
    url: `/department/${id}`,
    method: 'delete'
  })
}
