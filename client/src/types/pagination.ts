/**
 * Tham số phân trang và sắp xếp được gửi từ giao diện người dùng (Frontend).
 */
export interface PaginationParams {
	/** Số trang hiện tại (bắt đầu từ 0). */
	page?: number;
	/** Số lượng phần tử trên mỗi trang. */
	size?: number;
	/** Tên trường (thuộc tính) dùng để sắp xếp dữ liệu. */
	sortBy?: string;
	/** Hướng sắp xếp: tăng dần (`asc`) hoặc giảm dần (`desc`). */
	direction?: "asc" | "desc";
}

/**
 * Cấu trúc tham số phân trang theo đúng định dạng mà Java Spring Data Pageable yêu cầu.
 */
export interface SpringPageableParams {
	/** Số trang muốn thực hiện truy vấn. */
	page?: number;
	/** Số lượng bản ghi tối đa trên một trang. */
	size?: number;
	/** Chuỗi định dạng sắp xếp cấu hình theo kiểu: "tên_trường,hướng_sắp_xếp" (Ví dụ: "createdAt,desc"). */
	sort?: string;
}

/**
 * Chuyển đổi cấu trúc tham số phân trang của Frontend thành định dạng tương thích với Spring Boot Pageable.
 * * @param {PaginationParams} params - Đối tượng chứa các tham số phân trang gốc từ client.
 * @returns {SpringPageableParams} Đối tượng đã được chuẩn hóa để gửi qua HTTP Request cho Backend Spring.
 * * @example
 * const frontendParams = { page: 1, size: 10, sortBy: 'name', direction: 'desc' };
 * const apiParams = toPageableParams(frontendParams);
 * // Kết quả: { page: 1, size: 10, sort: 'name,desc' }
 */
export function toPageableParams(params: PaginationParams): SpringPageableParams {
	const httpParams: SpringPageableParams = {};

	if (params.page !== undefined) httpParams.page = params.page;
	if (params.size !== undefined) httpParams.size = params.size;
	if (params.sortBy !== undefined) {
		const direction = params.direction || "asc";
		httpParams.sort = `${params.sortBy},${direction}`;
	}

	return httpParams;
}

/**
 * Cấu trúc dữ liệu phân trang khớp 1:1 với đối tượng org.springframework.data.domain.Page của Java Spring.
 * @template T Kiểu dữ liệu của các phần tử trong danh sách `content`.
 */
export interface PaginationResponse<T> {
	/** Mảng chứa dữ liệu danh sách của trang hiện tại. */
	content: T[];
	/** Tổng số lượng phần tử tính trên tất cả các trang. */
	totalElements: number;
	/** Tổng số lượng trang hệ thống tính được dựa trên kích thước cấu hình. */
	totalPages: number;
	/** Số lượng phần tử tối đa được cấu hình trên một trang (Kích thước trang). */
	size: number;
	/** Chỉ mục (index) của trang hiện tại (bắt đầu từ số 0). */
	number: number;
	/** Xác định đây có phải là trang đầu tiên (Trang 0) hay không. */
	first: boolean;
	/** Xác định đây có phải là trang cuối cùng hay không. */
	last: boolean;
	/** Số lượng phần tử thực tế đang có trong trang hiện tại (thường bằng `size`, riêng trang cuối có thể ít hơn). */
	numberOfElements: number;
	/** Xác định trang hiện tại có trống rỗng (không có dữ liệu) hay không. */
	empty: boolean;
}
