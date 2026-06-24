const ApiRoutes = {
    LOGIN: "/auth/login",
    LOGOUT: "/auth/logout",
    REGISTER: "/auth/register",
    ME: "/auth/me",
    REFRESH_TOKEN: "/auth/refresh-token",
    PUBLIC_JOBS: "/public/jobs",
    PUBLIC_JOB_DETAIL: (id: string) => `/public/jobs/${id}`,
    APPLICATIONS: "/applications",
    APPLICATION_DETAIL: (id: string) => `/applications/${id}`,
    APPLICATION_TIMELINE: (id: string) => `/applications/${id}/timeline`,
    APPLICATION_CV: (id: string) => `/applications/cv/application/${id}`,
    APPLICATION_CHECK: (jobId: string) => `/applications/check/${jobId}`,
};

export default ApiRoutes;