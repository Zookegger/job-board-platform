const RouterRoutes = {
	HOME: "/",
	LOGIN: "/login",
	REGISTER: "/sign-up",
	FORGOT_PASSWORD: "/forgot-password",
	JOBS: "/jobs",
	JOB_DETAIL: (slug: string) => `/jobs/${slug}`,
	COMPANIES: "/companies",
	COMPANY_DETAIL: (slug: string) => `/companies/${slug}`,

	PROFILE: "/profile",
	UNAUTHORIZED: "/unauthorized",

	// Candidate
	CANDIDATE_APPLICATIONS: "/applications",
	CANDIDATE_APPLICATION_DETAIL: (id: string) => `/applications/${id}`,

	// Employer
	EMPLOYER_DASHBOARD: "/employer/dashboard",
	EMPLOYER_JOBS: "/employer/jobs",
	EMPLOYER_CREATE_JOB: "/employer/jobs/new",
	EMPLOYER_JOB_DETAIL: (id: string) => `/employer/jobs/${id}`,
	EMPLOYER_COMPANY: "/employer/company",

	// Admin
	ADMIN_DASHBOARD: "/admin/dashboard",
	ADMIN_USERS: "/admin/users",
	ADMIN_COMPANIES: "/admin/companies",
	ADMIN_JOBS: "/admin/jobs",
	ADMIN_REPORTS: "/admin/reports",
	ADMIN_SKILLS: "/admin/skills",
};

export default RouterRoutes;
