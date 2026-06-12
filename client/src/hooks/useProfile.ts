import profileApi from "@/api/profile";
import type { CandidateProfileRequest, EmployerProfileRequest } from "@/types/profile";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

/** Query keys cho profile API, dùng để quản lý cache trong React Query. */
export const PROFILE_KEYS = {
	candidate: ["profile", "candidate"] as const,
	employer: ["profile", "employer"] as const,
	resume: ["profile", "resume"] as const,
};

/**
 * Lấy thông tin hồ sơ ứng viên hiện tại.
 * Dữ liệu được cache 5 phút, không retry khi lỗi.
 *
 * @example
 * const { data: profile, isLoading } = useCandidateProfile();
 */
export function useCandidateProfile() {
	return useQuery({
		queryKey: PROFILE_KEYS.candidate,
		queryFn: () => profileApi.getCandidateProfile(),
		staleTime: 5 * 60 * 1000,
		retry: false,
	});
}

/**
 * Lấy thông tin hồ sơ nhà tuyển dụng (bao gồm thông tin công ty).
 * Dữ liệu được cache 5 phút, không retry khi lỗi.
 *
 * @example
 * const { data: profile, isLoading } = useEmployerProfile();
 */
export function useEmployerProfile() {
	return useQuery({
		queryKey: PROFILE_KEYS.employer,
		queryFn: () => profileApi.getEmployerProfile(),
		staleTime: 5 * 60 * 1000,
		retry: false,
	});
}

/**
 * Cập nhật thông tin hồ sơ ứng viên.
 * Tự động cập nhật cache sau khi thành công, không cần gọi lại API.
 *
 * @example
 * const update = useUpdateCandidateProfile();
 * update.mutate({ fullName: "Nguyễn Văn A", phone: "0901234567" });
 */
export function useUpdateCandidateProfile() {
	const queryClient = useQueryClient();

	return useMutation({
		mutationFn: (data: CandidateProfileRequest) => profileApi.updateCandidateProfile(data),
		onSuccess: (updatedProfile) => {
			queryClient.setQueryData(PROFILE_KEYS.candidate, updatedProfile);
		},
	});
}

/**
 * Cập nhật thông tin hồ sơ nhà tuyển dụng (bao gồm thông tin công ty).
 * Tự động cập nhật cache sau khi thành công.
 *
 * @example
 * const update = useUpdateEmployerProfile();
 * update.mutate({ fullName: "Trần Thị B", companyName: "ABC Corp" });
 */
export function useUpdateEmployerProfile() {
	const queryClient = useQueryClient();

	return useMutation({
		mutationFn: (data: EmployerProfileRequest) => profileApi.updateEmployerProfile(data),
		onSuccess: (updatedProfile) => {
			queryClient.setQueryData(PROFILE_KEYS.employer, updatedProfile);
		},
	});
}

/**
 * Upload ảnh đại diện (avatar) cho người dùng.
 * Tự động invalidate cache candidate và employer profile để làm mới dữ liệu.
 *
 * @example
 * const upload = useUploadAvatar();
 * upload.mutate(file);
 */
export function useUploadAvatar() {
	const queryClient = useQueryClient();

	return useMutation({
		mutationFn: (file: File) => profileApi.uploadAvatar(file),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: PROFILE_KEYS.candidate });
			queryClient.invalidateQueries({ queryKey: PROFILE_KEYS.employer });
		},
	});
}

export function useUploadCompanyLogo() {
	const queryClient = useQueryClient();

	return useMutation({
		mutationFn: (file: File) => profileApi.uploadCompanyLogo(file),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: PROFILE_KEYS.employer });
		},
	});
}

/**
 * Lấy thông tin CV (resume) của ứng viên hiện tại.
 * Dữ liệu được cache 5 phút, không retry khi lỗi.
 *
 * @example
 * const { data: resume, isLoading } = useResume();
 */
export function useResume() {
	return useQuery({
		queryKey: PROFILE_KEYS.resume,
		queryFn: () => profileApi.getResume(),
		staleTime: 5 * 60 * 1000,
		retry: false,
	});
}

/**
 * Upload file CV (PDF) cho ứng viên.
 * Tự động invalidate cache resume để làm mới dữ liệu.
 *
 * @example
 * const upload = useUploadResume();
 * upload.mutate(file);
 */
export function useUploadResume() {
	const queryClient = useQueryClient();

	return useMutation({
		mutationFn: (file: File) => profileApi.uploadResume(file),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: PROFILE_KEYS.resume });
		},
	});
}

/**
 * Xoá CV (resume) của ứng viên.
 * Tự động invalidate cache resume để làm mới dữ liệu.
 *
 * @example
 * const remove = useDeleteResume();
 * remove.mutate();
 */
export function useDeleteResume() {
	const queryClient = useQueryClient();

	return useMutation({
		mutationFn: () => profileApi.deleteResume(),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: PROFILE_KEYS.resume });
		},
	});
}
