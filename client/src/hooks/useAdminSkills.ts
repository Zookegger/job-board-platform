import adminApi from "@/api/admin";
import type { PaginationParams } from "@/types/pagination";
import type { SkillRequest } from "@/types/skill";
import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

export const ADMIN_SKILL_KEYS = {
	all: ["admin", "skills"] as const,
	list: (params: PaginationParams, keyword?: string, isActive?: boolean) =>
		["admin", "skills", params, keyword, isActive] as const,
} as const;

export const useAdminSkills = (params: PaginationParams, keyword?: string, isActive?: boolean) => {
	return useQuery({
		queryKey: ADMIN_SKILL_KEYS.list(params, keyword, isActive),
		queryFn: () => adminApi.getAllSkills(params, keyword, isActive),
		placeholderData: keepPreviousData,
		retry: false,
	});
};

export const useCreateSkill = () => {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: (request: SkillRequest) => adminApi.createSkill(request),
		onSuccess: () => queryClient.invalidateQueries({ queryKey: ADMIN_SKILL_KEYS.all }),
	});
};

export const useUpdateSkill = () => {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: ({ id, request }: { id: number; request: SkillRequest }) => adminApi.updateSkill(id, request),
		onSuccess: () => queryClient.invalidateQueries({ queryKey: ADMIN_SKILL_KEYS.all }),
	});
};

export const useToggleSkill = () => {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: (id: number) => adminApi.toggleSkillStatus(id),
		onSuccess: () => queryClient.invalidateQueries({ queryKey: ADMIN_SKILL_KEYS.all }),
	});
};

export const useDeleteSkill = () => {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: (id: number) => adminApi.deleteSkill(id),
		onSuccess: () => queryClient.invalidateQueries({ queryKey: ADMIN_SKILL_KEYS.all }),
	});
};
