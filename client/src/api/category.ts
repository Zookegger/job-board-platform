import type { CategoryResponse } from "@/types/job";
import ApiError from "@/utils/ApiError";
import client from "./client";

const categoryApi = {
	/**
	 * Lấy danh sách ngành nghề
	 * @returns Các ngành nghề dưới dạng mảng CategoryResponse
	 */
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
	/**
	 * Tạo ngành nghề mới (Admin only)
	 * @param name Tên ngành nghề
	 * @returns Ngành nghề được tạo
	 */
	createCategory: (name: string): Promise<CategoryResponse> =>
		client
			.post("/categories", { name })
			.then((r) => r.data)
			.catch((error) => {
				throw new ApiError(error.message || "Không thể tạo ngành nghề.", error.response?.status || 500);
			}),
	/**
	 * Cập nhật ngành nghề (Admin only)
	 * @param id ID của ngành nghề
	 * @param name Tên mới của ngành nghề
	 * @returns Ngành nghề được cập nhật
	 */
	updateCategory: (id: number, name: string): Promise<CategoryResponse> =>
		client
			.put(`/categories/${id}`, { name })
			.then((r) => r.data)
			.catch((error) => {
				throw new ApiError(error.message || "Không thể cập nhật ngành nghề.", error.response?.status || 500);
			}),

	/**
	 * Xóa ngành nghề (Admin only)
	 * @param id ID của ngành nghề
	 * @returns void
	 */
	deleteCategory: (id: number): Promise<void> =>
		client
			.delete(`/categories/${id}`)
			.then(() => {})
			.catch((error) => {
				throw new ApiError(error.message || "Không thể xóa ngành nghề.", error.response?.status || 500);
			}),
};

export default categoryApi;
