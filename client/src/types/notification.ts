export type NotificationType =
	| "APPLICATION_STATUS_CHANGED"
	| "APPLICATION_RECEIVED"
	| "JOB_STATUS_CHANGED"
	| "COMPANY_STATUS_CHANGED"
	| "COMPANY_PENDING_REVIEW"
	| "JOB_PENDING_REVIEW";

export interface NotificationResponse {
	id: string;
	type: NotificationType;
	entityId: string;
	message: string;
	isRead: boolean;
	createdAt: string;
	readAt: string | null;
}
