import { ref } from 'vue'
import axios from 'axios'

export function useApi() {
  const data = ref()
  const loading = ref(false)
  const error = ref<string | null>(null)

  const buildHeaders = () => {
    const accessToken = localStorage.getItem('accessToken')
    const headers: Record<string, string> = {}

    if (accessToken) {
      headers.Authorization = `Bearer ${accessToken}`
    }

    return headers
  }

  const fetchData = async (url: string) => {
    loading.value = true
    error.value = null

    try {
      const headers = buildHeaders()
      const response = await axios.get(url, { headers })
      data.value = response.data
    } catch (err) {
      error.value = (err as Error).message
    } finally {
      loading.value = false
    }
  }

  const postData = async (url: string, payload: any) => {
    loading.value = true
    error.value = null

    try {
      const headers = buildHeaders()
      const response = await axios.post(url, payload, { headers })
      data.value = response.data
    } catch (err) {
      error.value = (err as Error).message
    } finally {
      loading.value = false
    }
  }

  const putData = async (url: string, payload: any) => {
    loading.value = true
    error.value = null

    try {
      const headers = buildHeaders()
      const response = await axios.put(url, payload, { headers })
      data.value = response.data
    } catch (err) {
      error.value = (err as Error).message
    } finally {
      loading.value = false
    }
  }

  const deleteData = async (url: string) => {
    loading.value = true
    error.value = null

    try {
      const headers = buildHeaders()
      const response = await axios.delete(url, { headers })
      data.value = response.data
    } catch (err) {
      error.value = (err as Error).message
    } finally {
      loading.value = false
    }
  }

  const validate = async () => {
    const token = localStorage.getItem('accessToken')

    if (!token) {
      return false
    }

    await fetchData('/api/auth/validate?token=' + token)

    if (data.value && data.value.error === null) {
      return true
    }

    localStorage.removeItem('accessToken')
    return false
  }

  return { data, loading, error, fetchData, postData, putData, deleteData, validate }
}
