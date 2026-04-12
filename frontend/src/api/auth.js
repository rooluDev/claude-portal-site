import api from './axios'

export default {
  join(data) {
    return api.post('/auth/join', data)
  },
  login(data) {
    return api.post('/auth/login', data)
  },
}
