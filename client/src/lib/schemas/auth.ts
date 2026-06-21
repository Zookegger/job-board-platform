import { z } from "zod"

export const loginSchema = z.object({
  email: z.email("Email không hợp lệ"),
  password: z.string().min(6, "Mật khẩu phải có ít nhất 6 ký tự"),
})

export const candidateRegisterSchema = z.object({
  email: z.email("Email không hợp lệ"),
  fullName: z.string().min(1, "Họ tên không được để trống"),
  password: z.string().min(8, "Mật khẩu phải có ít nhất 8 ký tự"),
  confirmPassword: z.string().min(1, "Yêu cầu xác nhận mật khẩu"),
}).refine((data) => data.password === data.confirmPassword, {
  message: "Mật khẩu xác nhận không khớp",
  path: ["confirmPassword"],
})

export const companyRegisterSchema = z.object({
  companyName: z.string().min(1, "Tên công ty không được để trống"),
  address: z.string().min(1, "Địa chỉ không được để trống"),
  taxCode: z.string().min(1, "Mã số thuế không được để trống").max(20, "Mã số thuế không được quá 20 ký tự"),
  fullName: z.string().min(1, "Họ tên không được để trống"),
  phone: z.string().min(1, "Số điện thoại không được để trống").max(15, "Số điện thoại không được quá 15 ký tự"),
  userEmail: z.email("Email không hợp lệ"),
  password: z.string().min(8, "Mật khẩu phải có ít nhất 8 ký tự"),
  confirmPassword: z.string().min(1, "Yêu cầu xác nhận mật khẩu"),
}).refine((data) => data.password === data.confirmPassword, {
  message: "Mật khẩu xác nhận không khớp",
  path: ["confirmPassword"],
})
// TODO: Sau này thêm form cập nhật email + số điện thoại công ty trong Employer Dashboard

export type CandidateRegisterData = z.infer<typeof candidateRegisterSchema>
export type CompanyRegisterData = z.infer<typeof companyRegisterSchema>
