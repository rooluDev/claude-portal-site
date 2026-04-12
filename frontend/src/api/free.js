import api from './axios'

export default {
  getList(params) {
    return api.get('/free', { params })
  },
  getDetail(id) {
    return api.get(`/free/${id}`)
  },
  create(formData) {
    return api.post('/free', formData)
  },
  update(id, formData) {
    return api.put(`/free/${id}`, formData)
  },
  remove(id) {
    return api.delete(`/free/${id}`)
  },
}
