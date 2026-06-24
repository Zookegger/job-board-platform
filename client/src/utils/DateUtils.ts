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

export { formatDate };

