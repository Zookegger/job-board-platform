// @vitest-environment jsdom
import MockAdapter from "axios-mock-adapter"
import { describe, it, expect, beforeEach, afterEach, vi } from "vitest"
import client from "../client"
import * as authApi from "../auth"

let mock: MockAdapter

beforeEach(() => {
  mock = new MockAdapter(client)
  vi.spyOn(authApi.default, "refreshToken").mockResolvedValue()
})

afterEach(() => {
  mock.restore()
  vi.restoreAllMocks()
})

describe("Axios interceptor — xử lý 401 và token", () => {

  it("gọi refreshToken và retry lại request gốc khi nhận 401", async () => {
    const refreshSpy = vi.spyOn(authApi.default, "refreshToken")

    mock.onGet("/auth/me").replyOnce(401)
    mock.onGet("/auth/me").replyOnce(200, { id: "1", email: "test@test.com" })

    const res = await client.get("/auth/me")

    expect(refreshSpy).toHaveBeenCalledOnce()
    expect(res.status).toBe(200)
    expect(res.data).toEqual({ id: "1", email: "test@test.com" })
  })

  it("chuyển hướng sang /login khi refresh token thất bại", async () => {
    vi.spyOn(authApi.default, "refreshToken").mockRejectedValue(new Error("refresh failed"))

    Object.defineProperty(window, "location", {
      value: { href: "", ancestorOrigins: {} as DOMStringList, hash: "", host: "", hostname: "", origin: "", pathname: "", port: "", protocol: "", search: "", assign: vi.fn(), reload: vi.fn(), replace: vi.fn() },
      writable: true,
    })

    mock.onGet("/auth/me").reply(401)

    await expect(client.get("/auth/me")).rejects.toThrow()
    expect(window.location.href).toBe("/login")
  })

  it("hiển thị toast lỗi khi nhận 403 mà không gọi refresh", async () => {
    const refreshSpy = vi.spyOn(authApi.default, "refreshToken")
    mock.onGet("/auth/jobs").reply(403, { message: "Forbidden" })

    await expect(client.get("/auth/jobs")).rejects.toThrow()
    expect(refreshSpy).not.toHaveBeenCalled()
  })

  it("không retry lần 401 thứ hai trên cùng một request", async () => {
    const refreshSpy = vi.spyOn(authApi.default, "refreshToken")
    mock.onGet("/auth/me").reply(401)

    await expect(client.get("/auth/me")).rejects.toThrow()
    expect(refreshSpy).toHaveBeenCalledOnce()
  })

})
