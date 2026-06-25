import type { ApplicationStatus, ApplicationTimelineResponse } from "@/types/application";
import { formatDate } from "@/utils/DateUtils";
import { Ban, CheckCircle2, Clock, MessageSquare, UserCheck, XCircle } from "lucide-react";

const DOT_COLORS: Record<ApplicationStatus, string> = {
	PENDING: "bg-amber-500",
	REVIEWING: "bg-blue-500",
	INTERVIEW: "bg-violet-500",
	HIRED: "bg-green-500",
	REJECTED: "bg-red-500",
	WITHDRAWN: "bg-gray-400",
};

const DOT_ICONS: Record<ApplicationStatus, React.ReactNode> = {
	PENDING: <Clock className="size-4 text-white" />,
	REVIEWING: <MessageSquare className="size-4 text-white" />,
	INTERVIEW: <UserCheck className="size-4 text-white" />,
	HIRED: <CheckCircle2 className="size-4 text-white" />,
	REJECTED: <XCircle className="size-4 text-white" />,
	WITHDRAWN: <Ban className="size-4 text-white" />,
};

interface ApplicationTimelineProps {
	timeline: ApplicationTimelineResponse[];
	currentStatus: ApplicationStatus;
}

export function ApplicationTimeline({ timeline, currentStatus }: ApplicationTimelineProps) {
	if (timeline.length === 0) {
		return (
			<p className="py-6 text-center text-sm text-muted-foreground">
				Chưa có lịch sử cập nhật trạng thái.
			</p>
		);
	}

	return (
		<ol className="relative border-l border-border pl-6">
			{timeline.map((entry, i) => {
				const isActive = entry.status === currentStatus;
				const isLast = i === timeline.length - 1;

				return (
					<li key={entry.id} className="mb-6 last:mb-0 ml-2">
						<span
							className={`absolute -left-3 mt-1.5 flex size-6 items-center justify-center rounded-full border-2 border-white ${
								isActive ? "size-8 -left-4.5" : ""
							} ${DOT_COLORS[entry.status]}`}
						>
							{DOT_ICONS[entry.status]}
						</span>

						<div className="flex flex-wrap items-center gap-2">
							<span
								className={`text-sm font-medium ${
									isActive ? "text-foreground" : "text-muted-foreground"
								}`}
							>
								{entry.statusLabel}
							</span>
							{isActive && (
								<span className="rounded-full bg-primary/10 px-2 py-0.5 text-xs font-medium text-primary">
									Hiện tại
								</span>
							)}
						</div>

						{entry.note && (
							<p className="mt-1 text-sm text-muted-foreground">{entry.note}</p>
						)}

						<div className="mt-1 flex items-center gap-2 text-xs text-muted-foreground">
							<time>{formatDate(entry.changedAt)}</time>
							{entry.changedByName && (
								<>
									<span>·</span>
									<span>{entry.changedByName}</span>
								</>
							)}
						</div>

						{!isLast && <div className="mt-2 border-l border-dashed border-border" />}
					</li>
				);
			})}
		</ol>
	);
}
