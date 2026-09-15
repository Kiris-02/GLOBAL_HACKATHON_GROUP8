export const DEFAULT_PROFILE = {
  id: 1,
  fullName: "Tran Huu Nam",
  currentRole: "Senior Backend / Cloud Engineer",
  experienceYears: 4,
  bio: "Experienced Backend Engineer specializing in Java, Spring Boot, AWS, and Distributed Microservices. Looking for Global Opportunities.",
  skills: ["Java", "Spring Boot", "PostgreSQL", "Docker", "AWS", "Microservices", "REST APIs", "Redis", "CI/CD"],
  targetRoles: ["Senior Backend Engineer", "Cloud Solutions Engineer", "Distributed Systems Engineer"],
  targetLocations: ["Singapore", "Remote Worldwide", "Germany", "Vietnam"],
  willingToRelocate: true,
  targetWorkType: "ANY",
  rawCvText: "Senior Backend Engineer with 4 years of experience building high-scale distributed services."
};

export function getApiBase() {
  const custom = typeof window !== 'undefined' ? localStorage.getItem('AICAREER_BACKEND_URL') : null;
  if (custom && custom.trim()) {
    let url = custom.trim();
    if (!url.startsWith('http://') && !url.startsWith('https://')) {
      url = 'https://' + url;
    }
    url = url.replace(/\/+$/, '');
    if (!url.endsWith('/api')) {
      url += '/api';
    }
    return url;
  }

  let rawBase = import.meta.env.VITE_API_BASE || '';
  if (!rawBase || rawBase === '/api') {
    return '/api';
  }
  if (!rawBase.startsWith('http://') && !rawBase.startsWith('https://')) {
    rawBase = 'https://' + rawBase;
  }
  rawBase = rawBase.replace(/\/+$/, '');
  if (!rawBase.endsWith('/api')) {
    rawBase += '/api';
  }
  return rawBase;
}

export function setCustomBackendUrl(url) {
  if (typeof window === 'undefined') return;
  if (!url || !url.trim()) {
    localStorage.removeItem('AICAREER_BACKEND_URL');
  } else {
    localStorage.setItem('AICAREER_BACKEND_URL', url.trim());
  }
}

async function fetchWithTimeout(url, options = {}, timeoutMs = 8000) {
  const controller = new AbortController();
  const id = setTimeout(() => controller.abort(), timeoutMs);
  try {
    const response = await fetch(url, { ...options, signal: controller.signal });
    clearTimeout(id);
    return response;
  } catch (error) {
    clearTimeout(id);
    throw error;
  }
}

export async function fetchCurrentProfile() {
  const res = await fetchWithTimeout(`${getApiBase()}/profiles/current`);
  if (!res.ok) throw new Error('Không thể tải hồ sơ');
  return res.json();
}

export async function saveProfile(profileData) {
  const res = await fetchWithTimeout(`${getApiBase()}/profiles`, {
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
  const res = await fetchWithTimeout(`${getApiBase()}/profiles/upload-cv`, {
    method: 'POST',
    body: formData,
  }, 20000);
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.error || 'Lỗi khi tải file CV');
  }
  return res.json();
}

export async function resetSampleProfile(type) {
  const res = await fetchWithTimeout(`${getApiBase()}/profiles/reset-sample/${type}`, {
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

  const res = await fetchWithTimeout(`${getApiBase()}/jobs?${query.toString()}`);
  if (!res.ok) throw new Error('Không thể tải danh sách việc làm');
  return res.json();
}

export async function fetchMatches(params = {}) {
  const query = new URLSearchParams();
  if (params.keyword) query.append('keyword', params.keyword);
  if (params.isOverseas !== undefined && params.isOverseas !== null) query.append('isOverseas', params.isOverseas);
  if (params.workType) query.append('workType', params.workType);
  if (params.visaSponsorship !== undefined && params.visaSponsorship !== null) query.append('visaSponsorship', params.visaSponsorship);

  const res = await fetchWithTimeout(`${getApiBase()}/matches?${query.toString()}`);
  if (!res.ok) throw new Error('Không thể tính toán kết quả ghép việc');
  return res.json();
}

export async function fetchProfileAudit() {
  const res = await fetchWithTimeout(`${getApiBase()}/coach/audit`, {}, 25000);
  if (!res.ok) throw new Error('Không thể phân tích hồ sơ');
  return res.json();
}

export async function fetchRoadmap() {
  const res = await fetchWithTimeout(`${getApiBase()}/coach/roadmap`, {}, 25000);
  if (!res.ok) throw new Error('Không thể tải lộ trình sự nghiệp');
  return res.json();
}

export async function sendChatMessage(message, history = []) {
  const res = await fetchWithTimeout(`${getApiBase()}/coach/chat`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ message, history }),
  }, 25000);
  if (!res.ok) throw new Error('Không thể kết nối với AI Coach');
  return res.json();
}

export async function syncExternalJobs() {
  const res = await fetchWithTimeout(`${getApiBase()}/jobs/sync-external`, {
    method: 'POST',
  }, 20000);
  if (!res.ok) throw new Error('Không thể đồng bộ việc làm từ API');
  return res.json();
}

export async function fetchJobAiDeepDive(jobId) {
  const res = await fetchWithTimeout(`${getApiBase()}/matches/${jobId}/ai-deep-dive`, {}, 25000);
  if (!res.ok) throw new Error('Failed to generate AI deep-dive analysis');
  return res.json();
}

