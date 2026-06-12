import { Building2 } from "lucide-react";

import { Skeleton } from "@/components/ui/skeleton";
import { useEmployerProfile } from "@/hooks/useProfile";
import CompanyLogoUpload from "./components/CompanyLogoUpload";

export default function EmployerCompanyPage() {
	const { data: profile, isLoading } = useEmployerProfile();

	if (isLoading) {
		return (
			<div className="mx-auto max-w-2xl space-y-6 p-6">
				<Skeleton className="h-8 w-48" />
				<Skeleton className="h-52 w-full rounded-xl" />
			</div>
		);
	}

	return (
		<div className="mx-auto max-w-2xl space-y-6 p-6">
			<div className="flex items-center gap-2">
				<Building2 className="h-6 w-6 text-muted-foreground" />
				<h1 className="text-2xl font-bold">Hồ sơ công ty</h1>
			</div>

			<CompanyLogoUpload
				companyName={profile?.companyName ?? "Company"}
				currentLogoUrl={profile?.logoUrl}
			/>
		</div>
	);
}

