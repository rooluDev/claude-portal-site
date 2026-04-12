import api from './axios'

export default {
  getList(params) {
    return api.get('/inquiry', { params })
  },
  getDetail(id) {
    return api.get(`/inquiry/${id}`)
  },
  create(data) {
    return api.post('/inquiry', data)
  },
  update(id, data) {
    return api.put(`/inquiry/${id}`, data)
  },
  remove(id) {
    return api.delete(`/inquiry/${id}`)
  },
  createAnswer(inquiryId, data) {
    return api.post(`/inquiry-answer/${inquiryId}`, data)
  },
  updateAnswer(inquiryId, data) {
    return api.put(`/inquiry-answer/${inquiryId}`, data)
  },
  removeAnswer(inquiryId) {
    return api.delete(`/inquiry-answer/${inquiryId}`)
  },
}
