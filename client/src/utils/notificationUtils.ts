import type { NotificationType } from "@/types/notification";
import RouterRoutes from "./RouterRoutes";

/**
 * Trả về route điều hướng khi user click vào một notification.
 * Trả về null nếu loại notification không có trang cụ thể trong sprint này.
 */
export function getNotificationRoute(type: NotificationType, entityId: string): string | null {
	switch (type) {
		case "APPLICATION_STATUS_CHANGED":
			// entityId = application UUID → trang chi tiết đơn ứng tuyển của candidate
			return RouterRoutes.CANDIDATE_APPLICATION_DETAIL(entityId);

		case "JOB_STATUS_CHANGED":
			// entityId = job UUID → trang chi tiết job của employer
			return RouterRoutes.EMPLOYER_JOB_DETAIL(entityId);

		case "COMPANY_STATUS_CHANGED":
			// entityId = company UUID → trang quản lý công ty (employer chỉ có 1 company)
			return RouterRoutes.EMPLOYER_COMPANY;

		case "JOB_PENDING_REVIEW":
			// entityId = job UUID → admin duyệt danh sách jobs
			return RouterRoutes.ADMIN_JOBS;

		case "COMPANY_PENDING_REVIEW":
			// entityId = company UUID → admin duyệt danh sách companies
			return RouterRoutes.ADMIN_COMPANIES;

		case "APPLICATION_RECEIVED":
			// Chưa có trang employer application detail — skip trong sprint này
			return null;

		default:
			return null;
	}
}
