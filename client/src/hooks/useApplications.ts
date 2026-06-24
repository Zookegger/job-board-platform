import applicationsApi, { type MyApplicationsParams } from "@/api/applications";
import { keepPreviousData, useQuery } from "@tanstack/react-query";

export const APPLICATION_KEYS = {
	my: (params: MyApplicationsParams) => ["applications", "my", params] as const,
	myRoot: ["applications", "my"] as const,
};

export function useMyApplications(params: MyApplicationsParams) {
	return useQuery({
		queryKey: APPLICATION_KEYS.my(params),
		queryFn: () => applicationsApi.getMyApplications(params),
		placeholderData: keepPreviousData,
		retry: false,
	});
}
