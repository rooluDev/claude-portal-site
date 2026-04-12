import api from './axios'

export default {
  getComments(boardType, postId) {
    return api.get('/comments', { params: { boardType, postId } })
  },
  create(data) {
    return api.post('/comments', data)
  },
  remove(id) {
    return api.delete(`/comments/${id}`)
  },
}
