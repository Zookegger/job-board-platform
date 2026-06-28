/**
 * Định dạng mức lương theo kiểu "Từ X đến Y", "Từ X", "Đến Y" hoặc "Thương lượng" nếu cả hai đều null
 * @param min mức lương tối thiểu
 * @param max mức lương tối đa
 * @param currency ký hiệu tiền tệ (ví dụ: "VND", "USD")
 * @returns chuỗi định dạng mức lương phù hợp với ngôn ngữ Việt Nam
 */
function formatSalary(min: number | null, max: number | null, currency: string | null): string {
	if (!min && !max) return "Thương lượng";
	const fmt = (v: number) =>
		new Intl.NumberFormat("vi-VN", {
			style: "currency",
			currency: currency || "VND",
			maximumFractionDigits: 0,
		}).format(v);
	if (min && max) return `${fmt(min)} – ${fmt(max)}`;
	if (min) return `Từ ${fmt(min)}`;
	return `Đến ${fmt(max!)}`;
}

/**
 * Định dạng mức lương để hiển thị, nếu value là null hoặc không phải là số thì trả về chuỗi rỗng, nếu currency là "VND" thì định dạng theo kiểu Việt Nam, ngược lại trả về giá trị dưới dạng chuỗi
 * @param value giá trị mức lương cần định dạng
 * @param curr ký hiệu tiền tệ
 * @returns chuỗi định dạng mức lương phù hợp với ngôn ngữ Việt Nam hoặc giá trị dưới dạng chuỗi nếu không phải là VND
 */
const formatSalaryDisplay = (value: number | null | undefined, curr: string) => {
	if (value == null || Number.isNaN(value)) return "";
	if (curr === "VND") return new Intl.NumberFormat("vi-VN").format(value);
	return String(value);
};

/**
 * Phân tích đầu vào mức lương từ chuỗi thành số, loại bỏ tất cả ký tự không phải là chữ số, nếu chuỗi sau khi loại bỏ không còn gì thì trả về null
 * @param raw chuỗi đầu vào cần phân tích
 * @returns số mức lương hoặc null nếu không hợp lệ
 */
const parseSalaryInput = (raw: string): number | null => {
	const digitsOnly = raw.replace(/[^\d]/g, "");
	if (digitsOnly === "") return null;
	return Number(digitsOnly);
};

export { formatSalary, formatSalaryDisplay, parseSalaryInput };

