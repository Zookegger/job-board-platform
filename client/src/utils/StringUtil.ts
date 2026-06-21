function formatSalary(min: number | null, max: number | null, currency: string): string {
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

const formatSalaryDisplay = (value: number | null | undefined, curr: string) => {
	if (value == null || Number.isNaN(value)) return "";
	if (curr === "VND") return new Intl.NumberFormat("vi-VN").format(value);
	return String(value);
};

const parseSalaryInput = (raw: string): number | null => {
	const digitsOnly = raw.replace(/[^\d]/g, "");
	if (digitsOnly === "") return null;
	return Number(digitsOnly);
};

export { formatSalary, formatSalaryDisplay, parseSalaryInput };

