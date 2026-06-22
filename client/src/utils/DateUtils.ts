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

