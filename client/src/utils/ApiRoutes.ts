const ApiRoutes = {
    LOGIN: "/auth/login",
    LOGOUT: "/auth/logout",
    REGISTER: "/auth/register",
    ME: "/auth/me",
    REFRESH_TOKEN: "/auth/refresh-token",
    PUBLIC_JOBS: "/public/jobs",
    PUBLIC_JOB_DETAIL: (id: string) => `/public/jobs/${id}`,
};

export default ApiRoutes;