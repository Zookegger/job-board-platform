import { z } from "zod";

export const candidateProfileSchema = z.object({
	fullName: z.string().min(2, "Tên phải có ít nhất 2 ký tự").max(100, "Tên không được quá 100 ký tự"),
	phone: z
		.string()
		.regex(/^(0[0-9]{9})$/, "Số điện thoại không hợp lệ (phải 10 số, bắt đầu bằng 0)")
		.or(z.literal(""))
		.optional(),
});

export const employerProfileSchema = z.object({
	fullName: z.string().min(2, "Tên phải có ít nhất 2 ký tự").max(100, "Tên không được quá 100 ký tự"),
	phone: z
		.string()
		.regex(/^(0[0-9]{9})$/, "Số điện thoại không hợp lệ (phải 10 số, bắt đầu bằng 0)")
		.or(z.literal(""))
		.optional(),
	companyName: z.string().min(1, "Tên công ty không được để trống").max(100, "Tên công ty không được quá 100 ký tự"),
	roleInCompany: z.string().max(50, "Chức danh không được quá 50 ký tự").optional(),
	address: z.string().min(1, "Địa chỉ không được để trống").optional(),
	description: z.string().optional(),
	website: z.string().url("Website không hợp lệ").or(z.literal("")).optional(),
});

export type CandidateProfileFormData = z.infer<typeof candidateProfileSchema>;
export type EmployerProfileFormData = z.infer<typeof employerProfileSchema>;
