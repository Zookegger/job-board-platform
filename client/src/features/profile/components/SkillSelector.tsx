import { Check, ChevronDown, Loader2, Save, X } from "lucide-react";
import { useState } from "react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { useAllSkills, useCandidateSkills, useUpdateCandidateSkills } from "@/hooks/useSkills";
import { useToast } from "@/providers/ToastProvider";
import type { ProficientLevel } from "@/types/skill";
import { PROFICIENT_LEVEL_LABELS } from "@/types/skill";
import getErrorMessage from "@/utils/getErrorMessage";

const LEVEL_COLORS: Record<ProficientLevel, string> = {
	BEGINNER: "bg-gray-100 text-gray-700",
	INTERMEDIATE: "bg-blue-100 text-blue-700",
	ADVANCED: "bg-green-100 text-green-700",
	EXPERT: "bg-purple-100 text-purple-700",
};

export default function SkillSelector() {
	const { data: allSkills, isLoading: loadingAll } = useAllSkills();
	const { data: candidateSkills, isLoading: loadingMine } = useCandidateSkills();
	const updateSkills = useUpdateCandidateSkills();
	const toast = useToast();

	// Local draft: map skillId → proficientLevel
	const [draft, setDraft] = useState<Record<number, ProficientLevel>>(() => {
		return {};
	});
	const [initialized, setInitialized] = useState(false);
	const [search, setSearch] = useState("");
	const [openLevelMenu, setOpenLevelMenu] = useState<number | null>(null);

	// Initialize draft from server data once
	if (!initialized && candidateSkills) {
		const initial: Record<number, ProficientLevel> = {};
		candidateSkills.forEach((cs) => {
			initial[cs.skillId] = cs.proficientLevel;
		});
		setDraft(initial);
		setInitialized(true);
	}

	const toggleSkill = (skillId: number) => {
		setDraft((prev) => {
			if (skillId in prev) {
				const next = { ...prev };
				delete next[skillId];
				return next;
			}
			return { ...prev, [skillId]: "BEGINNER" };
		});
	};

	const setLevel = (skillId: number, level: ProficientLevel) => {
		setDraft((prev) => ({ ...prev, [skillId]: level }));
		setOpenLevelMenu(null);
	};

	const handleSave = async () => {
		try {
			await updateSkills.mutateAsync({
				skills: Object.entries(draft).map(([id, level]) => ({
					skillId: Number(id),
					proficientLevel: level,
				})),
			});
			toast.success("Đã cập nhật kỹ năng");
		} catch (error) {
			toast.error(getErrorMessage(error));
		}
	};

	const filteredSkills = (allSkills ?? []).filter((s) =>
		s.name.toLowerCase().includes(search.toLowerCase()),
	);

	const selectedIds = Object.keys(draft).map(Number);

	if (loadingAll || loadingMine) {
		return (
			<div className='space-y-3'>
				<Skeleton className='h-9 w-full' />
				<div className='flex flex-wrap gap-2'>
					{Array.from({ length: 8 }).map((_, i) => (
						<Skeleton key={i} className='h-7 w-20 rounded-full' />
					))}
				</div>
			</div>
		);
	}

	return (
		<div className='space-y-4'>
			{/* Search */}
			<input
				type='text'
				placeholder='Tìm kỹ năng...'
				value={search}
				onChange={(e) => setSearch(e.target.value)}
				className='w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2'
			/>

			{/* Selected skills with level picker */}
			{selectedIds.length > 0 && (
				<div>
					<p className='mb-2 text-sm font-medium text-muted-foreground'>
						Kỹ năng đã chọn ({selectedIds.length})
					</p>
					<div className='flex flex-wrap gap-2'>
						{selectedIds.map((skillId) => {
							const skill = allSkills?.find((s) => s.id === skillId);
							if (!skill) return null;
							const level = draft[skillId];
							const isOpen = openLevelMenu === skillId;
							return (
								<div key={skillId} className='relative'>
									<div
										className={`flex items-center gap-1 rounded-full px-3 py-1 text-sm font-medium ${LEVEL_COLORS[level]}`}
									>
										<span>{skill.name}</span>
										<button
											type='button'
											onClick={() => setOpenLevelMenu(isOpen ? null : skillId)}
											className='ml-1 opacity-70 hover:opacity-100'
											title='Chọn mức độ'
										>
											<ChevronDown className='h-3 w-3' />
										</button>
										<button
											type='button'
											onClick={() => toggleSkill(skillId)}
											className='ml-0.5 opacity-70 hover:opacity-100'
											title='Xoá kỹ năng'
										>
											<X className='h-3 w-3' />
										</button>
									</div>
									{isOpen && (
										<div className='absolute top-full left-0 z-10 mt-1 w-36 rounded-md border bg-background shadow-md'>
											{(Object.keys(PROFICIENT_LEVEL_LABELS) as ProficientLevel[]).map((lvl) => (
												<button
													key={lvl}
													type='button'
													onClick={() => setLevel(skillId, lvl)}
													className={`flex w-full items-center gap-2 px-3 py-1.5 text-sm hover:bg-muted ${level === lvl ? "font-semibold" : ""}`}
												>
													{level === lvl && <Check className='h-3 w-3' />}
													{PROFICIENT_LEVEL_LABELS[lvl]}
												</button>
											))}
										</div>
									)}
								</div>
							);
						})}
					</div>
				</div>
			)}

			{/* All skills list */}
			<div>
				<p className='mb-2 text-sm font-medium text-muted-foreground'>
					Tất cả kỹ năng
				</p>
				<div className='flex flex-wrap gap-2 max-h-48 overflow-y-auto'>
					{filteredSkills.map((skill) => {
						const isSelected = skill.id in draft;
						return (
							<button
								key={skill.id}
								type='button'
								onClick={() => toggleSkill(skill.id)}
								className={`inline-flex items-center gap-1 rounded-full border px-3 py-1 text-sm transition-colors ${
									isSelected
										? "border-primary bg-primary text-primary-foreground"
										: "border-border bg-background hover:bg-muted"
								}`}
							>
								{isSelected && <Check className='h-3 w-3' />}
								{skill.name}
							</button>
						);
					})}
					{filteredSkills.length === 0 && (
						<p className='text-sm text-muted-foreground'>Không tìm thấy kỹ năng phù hợp.</p>
					)}
				</div>
			</div>

			{/* Save button */}
			<div className='flex items-center gap-3 pt-2'>
				<Button
					variant='primary'
					onClick={handleSave}
					disabled={updateSkills.isPending}
				>
					{updateSkills.isPending ? (
						<Loader2 className='h-4 w-4 animate-spin' />
					) : (
						<Save className='h-4 w-4' />
					)}
					Lưu kỹ năng
				</Button>
				{selectedIds.length > 0 && (
					<Badge variant='secondary'>{selectedIds.length} kỹ năng</Badge>
				)}
			</div>
		</div>
	);
}
