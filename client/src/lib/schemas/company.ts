import { z } from "zod";

export const companySchema = z.object({
	companyName: z.string().min(1, "Tên công ty không được để trống").max(100, "Tối đa 100 ký tự"),
	address: z.string().min(1, "Địa chỉ không được để trống"),
	description: z.string().optional(),
	website: z.url("Website không hợp lệ").or(z.literal("")).optional(),
	companyEmail: z.email("Email không hợp lệ").or(z.literal("")).optional(),
	companyPhone: z
		.string()
		.regex(/^(|(\\+84|84|0)(2|3|5|7|8|9)[0-9]{8,9})$/, "Số điện thoại không hợp lệ (phải 11 số, bắt đầu bằng 0)")
		.or(z.literal(""))
		.optional(),
	taxCode: z.string().max(20, "Mã số thuế không được quá 20 ký tự").optional(),
});

export type CompanyFormData = z.infer<typeof companySchema>;
