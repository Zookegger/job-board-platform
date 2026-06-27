// @vitest-environment jsdom
import MockAdapter from "axios-mock-adapter";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import client from "../client";
import { employerApplicationApi } from "../employerApplication";

let mock: MockAdapter;

beforeEach(() => {
	mock = new MockAdapter(client);
});

afterEach(() => {
	mock.restore();
});

const mockApplicationResponse = {
	id: "550e8400-e29b-41d4-a716-446655440000",
	candidateId: "660e8400-e29b-41d4-a716-446655440001",
	candidateName: "Nguyễn Văn A",
	candidateAvatarUrl: null,
	candidateEmail: "candidate@test.com",
	candidatePhone: "0900000001",
	jobId: "770e8400-e29b-41d4-a716-446655440002",
	jobTitle: "Software Engineer",
	status: "PENDING",
	coverLetter: null,
	resumeUrl: "/uploads/resumes/cv.pdf",
	appliedAt: "2026-06-18T10:00:00Z",
	skills: [{ skillId: 1, skillName: "Java", proficientLevel: "INTERMEDIATE" }],
};

const mockPageResponse = {
	content: [mockApplicationResponse],
	totalElements: 1,
	totalPages: 1,
	size: 10,
	number: 0,
	first: true,
	last: true,
	numberOfElements: 1,
	empty: false,
};

describe("employerApplicationApi", () => {
	it("getEmployerApplications trả về danh sách phân trang", async () => {
		mock.onGet("/employer/applications").reply(200, mockPageResponse);

		const result = await employerApplicationApi.getEmployerApplications({ page: 0, size: 10 });

		expect(result.content).toHaveLength(1);
		expect(result.totalElements).toBe(1);
		expect(result.content[0].candidateName).toBe("Nguyễn Văn A");
		expect(result.content[0].jobTitle).toBe("Software Engineer");
	});

	it("getEmployerApplications gửi query params đúng", async () => {
		mock.onGet("/employer/applications", { params: { jobId: "job-1", status: "REVIEWING", page: 0, size: 5 } }).reply(200, mockPageResponse);

		const result = await employerApplicationApi.getEmployerApplications({ jobId: "job-1", status: "REVIEWING", page: 0, size: 5 });

		expect(result.content).toHaveLength(1);
	});

	it("getEmployerApplications ném ApiError khi thất bại", async () => {
		mock.onGet("/employer/applications").reply(500, { message: "Lỗi máy chủ" });

		await expect(employerApplicationApi.getEmployerApplications({})).rejects.toThrow(/Lỗi máy chủ/);
	});

	it("updateApplicationStatus cập nhật thành công", async () => {
		mock.onPut("/employer/applications/abc-123/status").reply(200);

		const result = await employerApplicationApi.updateApplicationStatus("abc-123", "INTERVIEW");

		expect(result).toBeUndefined();
	});

	it("updateApplicationStatus gửi reason đúng", async () => {
		mock.onPut("/employer/applications/abc-123/status").reply((config) => {
			if (config.params?.status === "REJECTED" && config.params?.reason === "Thiếu kinh nghiệm") {
				return [200];
			}
			return [400];
		});

		const result = await employerApplicationApi.updateApplicationStatus("abc-123", "REJECTED", "Thiếu kinh nghiệm");

		expect(result).toBeUndefined();
	});

	it("updateApplicationStatus ném ApiError khi thất bại", async () => {
		mock.onPut("/employer/applications/abc-123/status").reply(400, { message: "Yêu cầu không hợp lệ" });

		await expect(employerApplicationApi.updateApplicationStatus("abc-123", "INVALID")).rejects.toThrow(/Yêu cầu không hợp lệ/);
	});
});
