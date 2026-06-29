const ApiRoutes = {
    LOGIN: "/auth/login",
    LOGOUT: "/auth/logout",
    REGISTER: "/auth/register",
    ME: "/auth/me",
    REFRESH_TOKEN: "/auth/refresh-token",
    PUBLIC_JOBS: "/jobs/public",
    PUBLIC_RELATED_JOBS: (id: string) => `/jobs/${id}/related`,
    PUBLIC_JOB_DETAIL: (slug: string) => `/jobs/public/${slug}`,
    APPLICATIONS: "/applications",
    APPLICATION_DETAIL: (id: string) => `/applications/${id}`,
    APPLICATION_TIMELINE: (id: string) => `/applications/${id}/timeline`,
    APPLICATION_CV: (id: string) => `/applications/${id}/cv`,
    APPLICATION_BY_JOB: (jobId: string) => `/applications/by-job/${jobId}`,
};

export default ApiRoutes;