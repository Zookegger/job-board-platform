/**
 * Định nghĩa kiểu đơn vị kích thước tệp tin có thể là "B", "KB", hoặc "MB"
 */
type FileSizeUnit = "B" | "KB" | "MB";

/**
 * Định dạng kích thước tệp tin theo định dạng "B", "KB", hoặc "MB" tùy thuộc vào kích thước của tệp tin
 * @param bytes số byte của tệp tin cần định dạng
 * @returns Chuỗi kích thước tệp tin đã được định dạng
 */
function formatFileSize(bytes: number, unit: FileSizeUnit): string {
	if (unit === "B") {
		return `${bytes} B`;
	}
	if (unit === "KB") {
		return `${(bytes / 1024).toFixed(1)} KB`;
	}
	return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

export { formatFileSize, type FileSizeUnit };

