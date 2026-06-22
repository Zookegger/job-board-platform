import type { CreateReportRequest } from "@/types/report";
import reportApi from "@/api/report";
import { useMutation } from "@tanstack/react-query";

export function useCreateReport() {
	return useMutation({
		mutationFn: (data: CreateReportRequest) => reportApi.createReport(data),
	});
}
