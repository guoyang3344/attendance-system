import request from './request'

export function punchIn() {
  return request({
    url: '/attendance/punchIn',
    method: 'post'
  })
}

export function punchOut() {
  return request({
    url: '/attendance/punchOut',
    method: 'post'
  })
}

export function getTodayRecord() {
  return request({
    url: '/attendance/today',
    method: 'get'
  })
}

export function getSelfRecords() {
  return request({
    url: '/attendance/self',
    method: 'get'
  })
}

export function getAllRecords(page, size) {
  return request({
    url: '/attendance/all',
    method: 'get',
    params: {
      page,
      size
    }
  })
}
