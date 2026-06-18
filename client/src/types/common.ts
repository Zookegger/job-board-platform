export interface ApiResponse<T> {
	/** Dữ liệu trả về từ API. */
	data?: T;
	/** Thông báo trạng thái từ API. */
	message?: string;
	/** Mã trạng thái HTTP của phản hồi. */
	status?: number;
}
