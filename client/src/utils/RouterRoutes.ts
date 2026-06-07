const RouterRoutes = {
	HOME: "/",
	LOGIN: "/login",
	REGISTER: "/sign-up",
	FORGOT_PASSWORD: "/forgot-password",
	DASHBOARD: "/dashboard",
	JOBS: "/jobs",
	JOB_DETAIL: (id: string) => `/jobs/${id}`,
	PROFILE: "/profile",
	SETTINGS: "/settings",
};

export default RouterRoutes;
