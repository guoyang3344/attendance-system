import request from './request'

export function getLeaveTypes() {
  return request({
    url: '/leave/types',
    method: 'get'
  })
}

export function submitLeave(data) {
  return request({
    url: '/leave/submit',
    method: 'post',
    data
  })
}

export function getMyLeaves(page, size) {
  return request({
    url: '/leave/self',
    method: 'get',
    params: {
      page,
      size
    }
  })
}

export function getPendingLeaves(page, size) {
  return request({
    url: '/leave/pending',
    method: 'get',
    params: {
      page,
      size
    }
  })
}

export function getAllLeaves(page, size) {
  return request({
    url: '/leave/all',
    method: 'get',
    params: {
      page,
      size
    }
  })
}

export function getLeaveDetail(id) {
  return request({
    url: `/leave/detail/${id}`,
    method: 'get'
  })
}

export function approveLeave(data) {
  return request({
    url: '/leave/approve',
    method: 'post',
    data
  })
}

export function getMyApprovalLevel() {
  return request({
    url: '/leave/my-approval-level',
    method: 'get'
  })
}
