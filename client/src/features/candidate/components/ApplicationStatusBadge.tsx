import { Badge } from "@/components/ui/badge";
import type { ApplicationStatus } from "@/types/application";
import { APPLICATION_STATUS_LABELS } from "@/types/application";
import { CheckCircle2, Clock, MessageSquare, UserCheck, XCircle } from "lucide-react";

const STATUS_CONFIG: Record<
	ApplicationStatus,
	{ label: string; className: string; icon: React.ReactNode }
> = {
	PENDING: {
		label: APPLICATION_STATUS_LABELS.PENDING,
		className: "border-amber-300 bg-amber-100 text-amber-800",
		icon: <Clock className="size-3" />,
	},
	REVIEWING: {
		label: APPLICATION_STATUS_LABELS.REVIEWING,
		className: "border-blue-300 bg-blue-100 text-blue-800",
		icon: <MessageSquare className="size-3" />,
	},
	INTERVIEW: {
		label: APPLICATION_STATUS_LABELS.INTERVIEW,
		className: "border-violet-300 bg-violet-100 text-violet-800",
		icon: <UserCheck className="size-3" />,
	},
	HIRED: {
		label: APPLICATION_STATUS_LABELS.HIRED,
		className: "border-green-300 bg-green-100 text-green-800",
		icon: <CheckCircle2 className="size-3" />,
	},
	REJECTED: {
		label: APPLICATION_STATUS_LABELS.REJECTED,
		className: "border-red-300 bg-red-100 text-red-800",
		icon: <XCircle className="size-3" />,
	},
};

export function ApplicationStatusBadge({ status }: { status: ApplicationStatus }) {
	const config = STATUS_CONFIG[status];

	return (
		<Badge
			variant="outline"
			className={`inline-flex items-center gap-1 px-3 py-1 text-sm font-medium ${config.className}`}
		>
			{config.icon}
			{config.label}
		</Badge>
	);
}
