import companyStatusApi from "@/api/companyStatus";
import { useQuery } from "@tanstack/react-query";

export const COMPANY_STATUS_KEYS = {
	status: ["employer", "company", "status"] as const,
	history: ["employer", "company", "approval-history"] as const,
};

/** Lấy trạng thái phê duyệt hiện tại của công ty employer. Tự động refetch mỗi 30 giây. */
export function useCompanyStatus() {
	return useQuery({
		queryKey: COMPANY_STATUS_KEYS.status,
		queryFn: () => companyStatusApi.getStatus(),
		refetchInterval: 30_000,
		retry: false,
	});
}

/** Lấy lịch sử phê duyệt của công ty employer. Tự động refetch mỗi 30 giây. */
export function useCompanyApprovalHistory() {
	return useQuery({
		queryKey: COMPANY_STATUS_KEYS.history,
		queryFn: () => companyStatusApi.getHistory(),
		refetchInterval: 30_000,
		retry: false,
	});
}
