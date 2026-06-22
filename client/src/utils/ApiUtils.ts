function withoutEmptyParams(params: Record<string, unknown>) {
	return Object.fromEntries(Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== ""));
}

export { withoutEmptyParams };

