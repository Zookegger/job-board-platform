const RouterRoutes = {
	HOME: "/",
	LOGIN: "/login",
	REGISTER: "/sign-up",
	FORGOT_PASSWORD: "/forgot-password",
	JOBS: "/jobs",
	JOB_DETAIL: (id: string) => `/jobs/${id}`,

	PROFILE: "/profile",
	UNAUTHORIZED: "/unauthorized",

	// Candidate
	CANDIDATE: "/candidate",
	CANDIDATE_APPLICATIONS: "/candidate/applications",
	CANDIDATE_SAVED_JOBS: "/candidate/saved-jobs",
	CANDIDATE_SETTINGS: "/candidate/settings",

	// Employer
	EMPLOYER_DASHBOARD: "/employer/dashboard",
	EMPLOYER_JOBS: "/employer/jobs",
	EMPLOYER_CREATE_JOB: "/employer/jobs/new",
	EMPLOYER_JOB_DETAIL: (id: string) => `/employer/jobs/${id}`,
	EMPLOYER_APPLICATIONS: "/employer/applications",
	EMPLOYER_COMPANY: "/employer/company",
	EMPLOYER_SETTINGS: "/employer/settings",

	// Admin
	ADMIN_DASHBOARD: "/admin/dashboard",
	ADMIN_USERS: "/admin/users",
	ADMIN_COMPANIES: "/admin/companies",
	ADMIN_JOBS: "/admin/jobs",
	ADMIN_REPORTS: "/admin/reports",
	ADMIN_SETTINGS: "/admin/settings",
};

export default RouterRoutes;
