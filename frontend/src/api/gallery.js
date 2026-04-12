import api from './axios'

export default {
  getList(params) {
    return api.get('/gallery', { params })
  },
  getDetail(id) {
    return api.get(`/gallery/${id}`)
  },
  create(formData) {
    return api.post('/gallery', formData)
  },
  update(id, formData) {
    return api.put(`/gallery/${id}`, formData)
  },
  remove(id) {
    return api.delete(`/gallery/${id}`)
  },
}
