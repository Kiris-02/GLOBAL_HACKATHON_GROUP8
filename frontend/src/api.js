let rawBase = import.meta.env.VITE_API_BASE || '';
if (!rawBase || rawBase === '/api') {
  rawBase = '/api';
} else {
  if (!rawBase.startsWith('http://') && !rawBase.startsWith('https://')) {
    rawBase = 'https://' + rawBase;
  }
  rawBase = rawBase.replace(/\/+$/, '');
  if (!rawBase.endsWith('/api')) {
    rawBase = rawBase + '/api';
  }
}
const API_BASE = rawBase;

export async function fetchCurrentProfile() {
  const res = await fetch(`${API_BASE}/profiles/current`);
  if (!res.ok) throw new Error('Không thể tải hồ sơ');
  return res.json();
}

export async function saveProfile(profileData) {
  const res = await fetch(`${API_BASE}/profiles`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(profileData),
  });
  if (!res.ok) throw new Error('Không thể lưu hồ sơ');
  return res.json();
}

export async function uploadCvFile(file) {
  const formData = new FormData();
  formData.append('file', file);
  const res = await fetch(`${API_BASE}/profiles/upload-cv`, {
    method: 'POST',
    body: formData,
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.error || 'Lỗi khi tải file CV');
  }
  return res.json();
}

export async function resetSampleProfile(type) {
  const res = await fetch(`${API_BASE}/profiles/reset-sample/${type}`, {
    method: 'POST',
  });
  if (!res.ok) throw new Error('Không thể chuyển hồ sơ mẫu');
  return res.json();
}

export async function fetchJobs(params = {}) {
  const query = new URLSearchParams();
  if (params.keyword) query.append('keyword', params.keyword);
  if (params.isOverseas !== undefined && params.isOverseas !== null) query.append('isOverseas', params.isOverseas);
  if (params.workType) query.append('workType', params.workType);
  if (params.visaSponsorship !== undefined && params.visaSponsorship !== null) query.append('visaSponsorship', params.visaSponsorship);

  const res = await fetch(`${API_BASE}/jobs?${query.toString()}`);
  if (!res.ok) throw new Error('Không thể tải danh sách việc làm');
  return res.json();
}

export async function fetchMatches(params = {}) {
  const query = new URLSearchParams();
  if (params.keyword) query.append('keyword', params.keyword);
  if (params.isOverseas !== undefined && params.isOverseas !== null) query.append('isOverseas', params.isOverseas);
  if (params.workType) query.append('workType', params.workType);
  if (params.visaSponsorship !== undefined && params.visaSponsorship !== null) query.append('visaSponsorship', params.visaSponsorship);

  const res = await fetch(`${API_BASE}/matches?${query.toString()}`);
  if (!res.ok) throw new Error('Không thể tính toán kết quả ghép việc');
  return res.json();
}

export async function fetchProfileAudit() {
  const res = await fetch(`${API_BASE}/coach/audit`);
  if (!res.ok) throw new Error('Không thể phân tích hồ sơ');
  return res.json();
}

export async function fetchRoadmap() {
  const res = await fetch(`${API_BASE}/coach/roadmap`);
  if (!res.ok) throw new Error('Không thể tải lộ trình sự nghiệp');
  return res.json();
}

export async function sendChatMessage(message, history = []) {
  const res = await fetch(`${API_BASE}/coach/chat`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ message, history }),
  });
  if (!res.ok) throw new Error('Không thể kết nối với AI Coach');
  return res.json();
}

export async function syncExternalJobs() {
  const res = await fetch(`${API_BASE}/jobs/sync-external`, {
    method: 'POST',
  });
  if (!res.ok) throw new Error('Không thể đồng bộ việc làm từ API');
  return res.json();
}

export async function fetchJobAiDeepDive(jobId) {
  const res = await fetch(`${API_BASE}/matches/${jobId}/ai-deep-dive`);
  if (!res.ok) throw new Error('Failed to generate AI deep-dive analysis');
  return res.json();
}
