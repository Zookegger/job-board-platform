/**
 * Định dạng ngày tháng theo định dạng "ngày tháng năm, giờ:phút" (ví dụ: "15 thg 8, 2023, 14:30")
 * Nếu giá trị không hợp lệ hoặc null/undefined, trả về "—"
 * @param value Ngày tháng cần định dạng, có thể là đối tượng Date hoặc chuỗi ngày tháng
 * @param options Tùy chọn định dạng ngày tháng theo Intl.DateTimeFormatOptions (nếu không cung cấp sẽ sử dụng định dạng mặc định)
 * @returns Chuỗi ngày tháng đã được định dạng hoặc "—" nếu giá trị không hợp lệ
 */
function formatDate(value: Date | string | null | undefined, options?: Intl.DateTimeFormatOptions): string {
	if (!value) return "—";

	if (!options) {
		options = {
			dateStyle: "medium",
			timeStyle: "short",
		};
	}

	const date = value instanceof Date ? value : new Date(value);

	if (isNaN(date.getTime())) return "—";

	return new Intl.DateTimeFormat("vi-VN", options).format(date);
}

/**
 * Tính toán khoảng thời gian từ một ngày cho trước đến hiện tại
 * @param dateStr Ngày cần tính toán
 * @returns Chuỗi mô tả khoảng thời gian
 */
function TimeAgo(dateStr: string | null): string {
	if (!dateStr) return "";
	const date = new Date(dateStr);
	const now = new Date();
	const diffMs = Math.abs(now.getTime() - date.getTime());
	const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
	if (diffDays === 0) return "Hôm nay";
	if (diffDays === 1) return "Hôm qua";
	if (diffDays < 7) return `${diffDays} ngày trước`;
	if (diffDays < 30) return `${Math.floor(diffDays / 7)} tuần trước`;
	return `${Math.floor(diffDays / 30)} tháng trước`;
}

/**
 * Tính toán khoảng thời gian từ hiện tại đến một ngày cho trước
 * @param dateStr Ngày cần tính toán
 * @returns Chuỗi mô tả khoảng thời gian
 */
function TimeFromNow(dateStr: string | null): string {
	if (!dateStr) return "";
	const date = new Date(dateStr);
	const now = new Date();
	const diffMs = date.getTime() - now.getTime();
	const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
	if (diffDays === 0) return "Hôm nay";
	if (diffDays === 1) return "Ngày mai";
	if (diffDays < 7) return `Còn ${diffDays} ngày`;
	if (diffDays < 30) return `Còn ${Math.floor(diffDays / 7)} tuần`;
	return `Còn ${Math.floor(diffDays / 30)} tháng`;
}

export { formatDate, TimeAgo, TimeFromNow };

