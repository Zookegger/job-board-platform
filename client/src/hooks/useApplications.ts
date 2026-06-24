import { useMutation } from "@tanstack/react-query";
import applicationApi from "@/api/application";
import type { ApplicationRequest } from "@/types/application";

export function useSubmitApplication() {
	return useMutation({
		mutationFn: (request: ApplicationRequest) => applicationApi.submit(request),
	});
}
