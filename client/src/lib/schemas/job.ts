import { EmploymentTypes, ExperienceLevels, LocationTypes } from "@/types/job";
import { z } from "zod";

export const jobSchema = z
	.object({
		title: z.string().min(1, "Tiêu đề không được để trống").max(255, "Tiêu đề tối đa 255 ký tự"),
		description: z.string().min(1, "Mô tả không được để trống"),
		requirements: z.string().min(1, "Yêu cầu ứng viên không được để trống"),
		benefits: z.string().min(1, "Quyền lợi không được để trống"),
		categoryId: z.number({
			error: "Vui lòng chọn ngành nghề",
		}),
		numberOfOpenings: z.number().int().min(1).default(1).optional(),
		salaryMin: z.number().min(0, "Lương không được âm").optional().nullable(),
		salaryMax: z.number().min(0, "Lương không được âm").optional().nullable(),
		currency: z.string().default("VND"),
		location: z.string().default(""),
		locationTypes: z.enum(LocationTypes, { error: "Vui lòng chọn hình thức làm việc" }),
		employmentType: z.enum(EmploymentTypes, { error: "Vui lòng chọn loại hình" }),
		experienceLevel: z.enum(ExperienceLevels, { error: "Vui lòng chọn cấp bậc kinh nghiệm" }),
		skillIds: z.array(z.number()).optional().default([]),
	})
	.refine(
		(data) => {
			if (data.salaryMin != null && data.salaryMax != null) {
				return data.salaryMax >= data.salaryMin;
			}
			return true;
		},
		{ message: "Lương tối đa phải lớn hơn hoặc bằng lương tối thiểu", path: ["salaryMax"] },
	);

export type JobFormData = z.input<typeof jobSchema>;
