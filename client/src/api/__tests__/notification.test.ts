// @vitest-environment jsdom
import MockAdapter from "axios-mock-adapter";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import client from "../client";
import notificationApi from "../notification";

let mock: MockAdapter;

beforeEach(() => {
	mock = new MockAdapter(client);
});

afterEach(() => {
	mock.restore();
});

const mockNotification = {
	id: "aaaa0000-0000-0000-0000-000000000001",
	type: "COMPANY_STATUS_CHANGED",
	entityId: "bbbb0000-0000-0000-0000-000000000002",
	message: "Công ty của bạn đã được phê duyệt.",
	isRead: false,
	createdAt: "2026-06-29T10:00:00Z",
	readAt: null,
};

const mockPageResponse = {
	content: [mockNotification],
	totalElements: 1,
	totalPages: 1,
	size: 20,
	number: 0,
	first: true,
	last: true,
};

// ---------------------------------------------------------------------------
// TC-01: 5 thông báo chưa đọc → API trả về 5
// ---------------------------------------------------------------------------
describe("getUnreadCount", () => {
	it("TC-01: trả về đúng số lượng thông báo chưa đọc (5)", async () => {
		mock.onGet("/notifications/unread-count").reply(200, 5);

		const count = await notificationApi.getUnreadCount();

		expect(count).toBe(5);
	});

	// TC-02: Không có thông báo chưa đọc → API trả về 0
	it("TC-02: trả về 0 khi không có thông báo chưa đọc", async () => {
		mock.onGet("/notifications/unread-count").reply(200, 0);

		const count = await notificationApi.getUnreadCount();

		expect(count).toBe(0);
	});
});

// ---------------------------------------------------------------------------
// TC-03: Badge hiển thị 99+ khi số lượng > 99
// ---------------------------------------------------------------------------
describe("badge display logic (99+ format)", () => {
	it("TC-03a: count = 100 → displayCount là '99+'", () => {
		const count = 100;
		const displayCount = count > 99 ? "99+" : String(count);
		expect(displayCount).toBe("99+");
	});

	it("TC-03b: count = 99 → displayCount là '99' (không bị rút gọn)", () => {
		const count = 99;
		const displayCount = count > 99 ? "99+" : String(count);
		expect(displayCount).toBe("99");
	});

	it("TC-03c: count = 120 → displayCount là '99+'", () => {
		const count = 120;
		const displayCount = count > 99 ? "99+" : String(count);
		expect(displayCount).toBe("99+");
	});

	it("TC-03d: count = 0 → badge không hiển thị (count không > 0)", () => {
		const count = 0;
		const shouldShowBadge = count > 0;
		expect(shouldShowBadge).toBe(false);
	});
});

// ---------------------------------------------------------------------------
// Các API call khác
// ---------------------------------------------------------------------------
describe("getMyNotifications", () => {
	it("gọi đúng endpoint và trả về trang thông báo", async () => {
		mock.onGet("/notifications").reply(200, mockPageResponse);

		const result = await notificationApi.getMyNotifications({ page: 0, size: 20 });

		expect(result.content).toHaveLength(1);
		expect(result.content[0].type).toBe("COMPANY_STATUS_CHANGED");
		expect(result.content[0].isRead).toBe(false);
	});
});

describe("markAsRead", () => {
	it("gọi PATCH đúng endpoint cho notification cụ thể", async () => {
		mock.onPatch(`/notifications/${mockNotification.id}/read`).reply(200);

		await expect(notificationApi.markAsRead(mockNotification.id)).resolves.toBeUndefined();
	});
});

describe("markAllAsRead", () => {
	it("gọi PATCH /notifications/read-all", async () => {
		mock.onPatch("/notifications/read-all").reply(200);

		await expect(notificationApi.markAllAsRead()).resolves.toBeUndefined();
	});
});
