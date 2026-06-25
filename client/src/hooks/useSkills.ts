import skillApi from "@/api/skill";
import type { UpdateCandidateSkillsRequest } from "@/types/skill";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

export const SKILL_KEYS = {
	all: ["skills"] as const,
	candidate: ["skills", "candidate"] as const,
};

/** Lấy toàn bộ danh sách kỹ năng (public — không phân trang, cache 10 phút). */
export function useAllSkillsPublic() {
	return useQuery({
		queryKey: ["skills", "public"],
		queryFn: () => skillApi.getAllSkillsPublic(),
		staleTime: 10 * 60 * 1000,
	});
}

/** Lấy toàn bộ danh sách kỹ năng có sẵn (cache 10 phút). */
export function useAllSkills() {
	return useQuery({
		queryKey: SKILL_KEYS.all,
		queryFn: () => skillApi.getAllSkills(),
		staleTime: 10 * 60 * 1000,
		select: (data) => data.content,
	});
}

/** Lấy danh sách kỹ năng hiện tại của ứng viên. */
export function useCandidateSkills() {
	return useQuery({
		queryKey: SKILL_KEYS.candidate,
		queryFn: () => skillApi.getCandidateSkills(),
		staleTime: 5 * 60 * 1000,
		retry: false,
	});
}

/** Cập nhật toàn bộ kỹ năng của ứng viên. */
export function useUpdateCandidateSkills() {
	const queryClient = useQueryClient();

	return useMutation({
		mutationFn: (data: UpdateCandidateSkillsRequest) => skillApi.updateCandidateSkills(data),
		onSuccess: (updated) => {
			queryClient.setQueryData(SKILL_KEYS.candidate, updated);
		},
	});
}
