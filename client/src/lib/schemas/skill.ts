import { z } from "zod";

export const skillSchema = z.object({
	name: z.string().min(1, "Tên kỹ năng không được để trống").max(100, "Tối đa 100 ký tự"),
	isActive: z.boolean(),
});

export type SkillFormData = z.infer<typeof skillSchema>;
