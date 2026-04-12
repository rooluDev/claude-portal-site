import api from './axios'

export default {
  getMainData() {
    return api.get('/main')
  },
}
