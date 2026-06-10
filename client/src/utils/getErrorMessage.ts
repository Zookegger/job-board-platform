function getErrorMessage(error: unknown, fallback?: string): string {
	if (error instanceof Error) return error.message;
	if (typeof error === "string") return error;
	return fallback ?? "Đã có lỗi xảy ra. Vui lòng thử lại sau.";
}

export default getErrorMessage;
