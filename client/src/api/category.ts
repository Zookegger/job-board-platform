import type { CategoryResponse } from "@/types/job";
import ApiError from "@/utils/ApiError";
import client from "./client";

const categoryApi = {
	getCategories: (): Promise<CategoryResponse[]> =>
		client
			.get("/categories")
			.then((r) => r.data)
			.catch((error) => {
				throw new ApiError(
					error.message || "Không thể tải danh sách ngành nghề.",
					error.response?.status || 500,
				);
			}),
};

export default categoryApi;