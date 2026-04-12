import api from './axios'

export default {
  getList(params) {
    return api.get('/notice', { params })
  },
  getDetail(id) {
    return api.get(`/notice/${id}`)
  },
  create(data) {
    return api.post('/notice', data)
  },
  update(id, data) {
    return api.put(`/notice/${id}`, data)
  },
  remove(id) {
    return api.delete(`/notice/${id}`)
  },
}
