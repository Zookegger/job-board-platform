// @vitest-environment jsdom
import MockAdapter from "axios-mock-adapter";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import categoryApi from "../category";
import client from "../client";
import { employerJobApi } from "../employerJob";

let mock: MockAdapter;

beforeEach(() => {
	mock = new MockAdapter(client);
});

afterEach(() => {
	mock.restore();
});

const mockJobResponse = {
	id: "550e8400-e29b-41d4-a716-446655440000",
	title: "Software Engineer",
	slug: "software-engineer",
	description: "Job description",
	status: "DRAFT",
	locationTypes: "ONSITE",
	employmentType: "FULL_TIME",
	experienceLevel: "MID",
	salaryMin: 10000000,
	salaryMax: 20000000,
	currency: "VND",
	numberOfOpenings: 2,
	location: "Hồ Chí Minh",
	companyId: "660e8400-e29b-41d4-a716-446655440001",
	companyName: "Test Corp",
	categoryId: 1,
	categoryName: "IT",
	skills: [{ id: 1, name: "Java", isActive: true }],
	createdAt: "2026-06-18T10:00:00Z",
	updatedAt: "2026-06-18T10:00:00Z",
};

const mockPageResponse = {
	content: [mockJobResponse],
	totalElements: 1,
	totalPages: 1,
	size: 10,
	number: 0,
	first: true,
	last: true,
	numberOfElements: 1,
	empty: false,
};

describe("employerJobApi", () => {
	it("getEmployerJobs trả về danh sách phân trang", async () => {
		mock.onGet("/employer/jobs").reply(200, mockPageResponse);

		const result = await employerJobApi.getEmployerJobs({ page: 0, size: 10 });

		expect(result.content).toHaveLength(1);
		expect(result.totalElements).toBe(1);
		expect(result.content[0].title).toBe("Software Engineer");
	});

	it("getEmployerJobs gửi query params đúng", async () => {
		mock.onGet("/employer/jobs", { params: { page: 1, size: 5, status: "DRAFT" } }).reply(200, mockPageResponse);

		const result = await employerJobApi.getEmployerJobs({ page: 1, size: 5, status: "DRAFT" });

		expect(result.content).toHaveLength(1);
	});

	it("getEmployerJobs ném ApiError khi thất bại", async () => {
		mock.onGet("/employer/jobs").reply(500, { message: "Lỗi máy chủ" });

		await expect(employerJobApi.getEmployerJobs({})).rejects.toThrow(/Lỗi máy chủ/);
	});

	it("getEmployerJobDetail trả về chi tiết job", async () => {
		mock.onGet("/employer/jobs/" + mockJobResponse.id).reply(200, mockJobResponse);

		const result = await employerJobApi.getEmployerJobDetail(mockJobResponse.id);

		expect(result.title).toBe("Software Engineer");
		expect(result.skills).toHaveLength(1);
		expect(result.skills[0].name).toBe("Java");
	});

	it("getEmployerJobDetail ném ApiError với status 404", async () => {
		mock.onGet("/employer/jobs/not-found").reply(404);

		await expect(employerJobApi.getEmployerJobDetail("not-found")).rejects.toThrow(/Không tìm thấy/);
	});

	it("createEmployerJob tạo job và trả về kết quả", async () => {
		const request = {
			title: "New Job",
			description: "Description",
			categoryId: 1,
			locationTypes: "REMOTE" as const,
			employmentType: "FULL_TIME" as const,
			experienceLevel: "MID" as const,
			skillIds: [1],
		};

		mock.onPost("/employer/jobs", request).reply(201, mockJobResponse);

		const result = await employerJobApi.createEmployerJob(request);

		expect(result.title).toBe("Software Engineer");
		expect(result.status).toBe("DRAFT");
	});

	it("createEmployerJob ném ApiError khi thất bại", async () => {
		mock.onPost("/employer/jobs").reply(400, { message: "Validation failed" });

		await expect(
			employerJobApi.createEmployerJob({
				title: "Bad Job",
				description: "x",
				categoryId: 1,
				locationTypes: "ONSITE",
				employmentType: "FULL_TIME",
				experienceLevel: "JUNIOR",
			}),
		).rejects.toThrow(/Validation failed/);
	});

	it("updateEmployerJob cập nhật và trả về kết quả", async () => {
		const request = {
			title: "Updated Job",
			description: "Updated",
			categoryId: 1,
			locationTypes: "HYBRID" as const,
			employmentType: "FULL_TIME" as const,
			experienceLevel: "SENIOR" as const,
		};
		const updatedResponse = { ...mockJobResponse, title: "Updated Job", experienceLevel: "SENIOR" };

		mock.onPut("/employer/jobs/" + mockJobResponse.id, request).reply(200, updatedResponse);

		const result = await employerJobApi.updateEmployerJob(mockJobResponse.id, request);

		expect(result.title).toBe("Updated Job");
		expect(result.experienceLevel).toBe("SENIOR");
	});

	it("submitForReview gửi duyệt thành công", async () => {
		mock.onPost("/employer/jobs/" + mockJobResponse.id + "/submit").reply(200, { message: "Gửi duyệt thành công" });

		const result = await employerJobApi.submitForReview(mockJobResponse.id);

		expect(result.message).toBe("Gửi duyệt thành công");
	});

	it("submitForReview ném ApiError khi thất bại", async () => {
		mock.onPost("/employer/jobs/" + mockJobResponse.id + "/submit").reply(400, { message: "Yêu cầu không hợp lệ" });

		await expect(employerJobApi.submitForReview(mockJobResponse.id)).rejects.toThrow(/Yêu cầu không hợp lệ/);
	});

	it("deleteEmployerJob xóa job thành công", async () => {
		mock.onDelete("/employer/jobs/" + mockJobResponse.id).reply(200, { message: "Xóa tin tuyển dụng thành công" });

		const result = await employerJobApi.deleteEmployerJob(mockJobResponse.id);

		expect(result.message).toBe("Xóa tin tuyển dụng thành công");
	});

	it("getCategories trả về danh sách ngành nghề", async () => {
		const categories = [
			{ id: 1, name: "IT" },
			{ id: 2, name: "Finance" },
		];
		mock.onGet("/categories").reply(200, categories);

		const result = await categoryApi.getCategories();

		expect(result).toHaveLength(2);
		expect(result[0].name).toBe("IT");
	});

	it("getCategories ném ApiError khi thất bại", async () => {
		mock.onGet("/categories").reply(500);

		await expect(categoryApi.getCategories()).rejects.toThrow(/Request failed/);
	});
});
