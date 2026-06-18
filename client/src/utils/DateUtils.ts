function formatDate(value: Date | string | null | undefined) {
	if (!value) return "—";

	const date = value instanceof Date ? value : new Date(value);
    
	if (isNaN(date.getTime())) return "—";

	return new Intl.DateTimeFormat("vi-VN", {
		dateStyle: "medium",
		timeStyle: "short",
	}).format(date);
}

export { formatDate };

