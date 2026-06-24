import categoryApi from "@/api/category";
import { useQuery } from "@tanstack/react-query";

export function useCategories() {
	return useQuery({
		queryKey: ["categories"],
		queryFn: () => categoryApi.getCategories(),
		staleTime: 10 * 60 * 1000,
		retry: false,
	});
}
