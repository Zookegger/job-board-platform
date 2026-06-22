const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:5000";

export interface PublicCompany {
  id: string;
  companyName: string;
  slug: string;
  logoUrl?: string;
  description?: string;
  website?: string;
  email?: string;
  phone?: string;
  address?: string;
  taxCode?: string;
  createdAt?: string;
  totalOpenJobs: number;
}

export interface PublicCompanyJob {
  id: string;
  title: string;
  location?: string;
  status?: string;
  createdAt?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

async function request<T>(url: string): Promise<T> {
  console.log("Calling API:", url);

  const response = await fetch(url);

  if (!response.ok) {
    const errorText = await response.text();
    console.error("API ERROR:", response.status, errorText);
    throw new Error(`API lỗi ${response.status}: ${errorText}`);
  }

  return response.json();
}

export function getPublicCompanyDetail(companyId: string) {
  return request<PublicCompany>(
    `${API_BASE_URL}/api/public/company-pages/${companyId}`
  );
}

export function getPublicCompanyJobs(companyId: string, page = 0, size = 6) {
  return request<PageResponse<PublicCompanyJob>>(
    `${API_BASE_URL}/api/public/company-pages/${companyId}/jobs?page=${page}&size=${size}`
  );
}