export interface SkillRequest {
	name: string;
	isActive: boolean;
}

export interface SkillResponse {
	id: number;
	name: string;
	isActive: boolean;
	createdAt?: Date;
	updatedAt?: Date;
}

export type ProficientLevel = "BEGINNER" | "INTERMEDIATE" | "ADVANCED" | "EXPERT";

export const PROFICIENT_LEVEL_LABELS: Record<ProficientLevel, string> = {
	BEGINNER: "Mới bắt đầu",
	INTERMEDIATE: "Trung cấp",
	ADVANCED: "Nâng cao",
	EXPERT: "Chuyên gia",
};

export interface CandidateSkillResponse {
	skillId: number;
	skillName: string;
	proficientLevel: ProficientLevel;
}

export interface UpdateCandidateSkillsRequest {
	skills: { skillId: number; proficientLevel: ProficientLevel }[];
}
