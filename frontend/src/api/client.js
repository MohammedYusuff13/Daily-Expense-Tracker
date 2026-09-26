import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '',
  headers: { 'Content-Type': 'application/json' },
});

// Unwrap the ApiResponse envelope and surface a clean error message.
api.interceptors.response.use(
  (res) => res,
  (err) => {
    const message =
      err.response?.data?.message || err.message || 'Something went wrong';
    return Promise.reject(new Error(message));
  }
);

const unwrap = (res) => res.data?.data;

export const dashboardApi = {
  get: () => api.get('/api/dashboard').then(unwrap),
};

export const transactionApi = {
  list: () => api.get('/api/transactions').then(unwrap),
  search: (params) => api.get('/api/transactions/search', { params }).then(unwrap),
  create: (body) => api.post('/api/transactions', body).then((r) => r.data),
  update: (id, body) => api.put(`/api/transactions/${id}`, body).then((r) => r.data),
  remove: (id) => api.delete(`/api/transactions/${id}`).then((r) => r.data),
};

export const buyerApi = {
  list: () => api.get('/api/buyers').then(unwrap),
  search: (q) => api.get('/api/buyers/search', { params: { q } }).then(unwrap),
  create: (body) => api.post('/api/buyers', body).then((r) => r.data),
};

export const balanceSheetApi = {
  get: () => api.get('/api/balance-sheet').then(unwrap),
  exportExcelUrl: `${import.meta.env.VITE_API_BASE || ''}/api/balance-sheet/export/excel`,
  exportPdfUrl: `${import.meta.env.VITE_API_BASE || ''}/api/balance-sheet/export/pdf`,
};

export const reportApi = {
  daily: (date) => api.get('/api/reports/daily', { params: { date } }).then(unwrap),
  weekly: (date) => api.get('/api/reports/weekly', { params: { date } }).then(unwrap),
  monthly: (month) => api.get('/api/reports/monthly', { params: { month } }).then(unwrap),
  buyer: (name) => api.get(`/api/reports/buyer/${encodeURIComponent(name)}`).then(unwrap),
  outstanding: () => api.get('/api/reports/outstanding').then(unwrap),
};

export const dataApi = {
  backup: () => api.post('/api/data/backup').then((r) => r.data),
  importFile: (file) => {
    const form = new FormData();
    form.append('file', file);
    return api
      .post('/api/data/import', form, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      .then((r) => r.data);
  },
  exportUrl: `${import.meta.env.VITE_API_BASE || ''}/api/data/export`,
};

export default api;
