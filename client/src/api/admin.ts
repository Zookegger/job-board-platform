import client from './client';

export interface PendingCompany {
  id: string;
  companyName: string;
  email: string;
  phone: string;
  taxCode: string;
  address: string;
  description: string;
  website: string;
  logoUrl: string;
  createdAt: string;
}

export interface ApiResponse {
  success: boolean;
  message: string;
  data?: any;
}

export interface CompanyRejectionRequest {
  rejectionReason: string;
}

export interface CompanySuspensionRequest {
  suspensionReason: string;
}

/**
 * Lấy danh sách công ty đang chờ duyệt
 */
export async function getPendingCompanies(page: number = 0, size: number = 20) {
  try {
    const response = await client.get('/admin/companies/pending', {
      params: {
        page,
        size,
      },
    });
    return response;
  } catch (error) {
    console.error('Error fetching pending companies:', error);
    throw error;
  }
}

/**
 * Phê duyệt công ty
 */
export async function approveCompany(companyId: string): Promise<ApiResponse> {
  try {
    const response = await client.post(`/admin/companies/${companyId}/approve`);
    return response.data;
  } catch (error) {
    console.error('Error approving company:', error);
    throw error;
  }
}

/**
 * Từ chối công ty
 */
export async function rejectCompany(
  companyId: string,
  request: CompanyRejectionRequest
): Promise<ApiResponse> {
  try {
    const response = await client.post(
      `/admin/companies/${companyId}/reject`,
      request
    );
    return response.data;
  } catch (error) {
    console.error('Error rejecting company:', error);
    throw error;
  }
}

/**
 * Tạm ngưng công ty
 */
export async function suspendCompany(
  companyId: string,
  request: CompanySuspensionRequest
): Promise<ApiResponse> {
  try {
    const response = await client.post(
      `/admin/companies/${companyId}/suspend`,
      request
    );
    return response.data;
  } catch (error) {
    console.error('Error suspending company:', error);
    throw error;
  }
}
