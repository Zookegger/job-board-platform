# JBP-31 & JBP-32 — Employer Job Management UI

> **Status:** Updated plan (codebase has evolved since original)
> **Stories:** [JBP-31](https://nguyenductrung11122004-demo.atlassian.net/browse/JBP-31), [JBP-32](https://nguyenductrung11122004-demo.atlassian.net/browse/JBP-32)

---

## What's Already Done (no longer in scope)

These items were implemented in a previous stash and are now in the codebase:

| Item | Description | Status |
|------|-------------|--------|
| **S1 — Skills fix** | `JobResponse.withSkills()` method + `JobServiceImpl` populates skills in create/update/detail | ✅ Done |
| **T2 — Report entity** | `Report.java` uses `@ManyToOne` for job, company, reportedBy, reviewedBy (no raw UUIDs) | ✅ Done |
| **T2 — RefreshToken entity** | `RefreshToken.java` uses `@ManyToOne` for user | ⚠️ Partial (see Note) |
| **Admin jobs** | `AdminController` + `AdminServiceImpl` job approval/rejection/pending list implemented | ✅ Done |
| **Server tests** | `EmployerJobControllerTest` has skills assertions; `PublicJobControllerTest` exists | ✅ Done |
| **Client types** | `src/types/job.ts` created with enums + `AdminPendingJobResponse` | ✅ Partial |
| **Client test** | `src/api/__tests__/employerJob.test.ts` created | ⚠️ Broken (see below) |

> [!WARNING]
> **RefreshToken cascade bug:** `RefreshToken.java` has `cascade = CascadeType.ALL` on the `@ManyToOne` to `User`. This means deleting a `RefreshToken` will cascade-delete the `User` — a critical data-loss bug. Change to no cascade: `@ManyToOne(fetch = FetchType.EAGER, optional = false)` — remove `cascade = CascadeType.ALL`.

> [!WARNING]
> **Broken test import:** `src/api/__tests__/employerJob.test.ts` imports `{ employerJobApi } from "../employerJob"` but `employerJob.ts` doesn't exist. This test will fail to compile until C2 is implemented.

---

## What Still Needs to Be Done

### Server — S2: Categories endpoint

**File:** `server/src/main/java/com/yoedu/job_board_platform/controllers/PublicJobController.java`

The `GET /api/public/categories` endpoint is still a stub returning `"Danh sách ngành"`. The job creation form needs it as a dropdown.

**Steps:**
1. Create DTO at `server/src/main/java/com/yoedu/job_board_platform/dtos/category/JobCategoryResponse.java`:
   ```java
   package com.yoedu.job_board_platform.dtos.category;

   public record JobCategoryResponse(Integer id, String name) {}
   ```

2. In `PublicJobController`, inject `JobCategoryRepository` and replace the stub:
   ```java
   private final JobCategoryRepository categoryRepository;

   // add to constructor or use @RequiredArgsConstructor

   @GetMapping("/categories")
   public ResponseEntity<List<JobCategoryResponse>> getCategories() {
       return ResponseEntity.ok(
           categoryRepository.findAll().stream()
               .map(c -> new JobCategoryResponse(c.getId(), c.getName()))
               .toList()
       );
   }
   ```

---

### Client — C1: Expand `src/types/job.ts`

**File:** `client/src/types/job.ts`

Currently only has enums + `AdminPendingJobResponse`. Add employer-facing types:

```typescript
// ── Append to existing file ──

export const JOB_STATUS_LABELS: Record<JobStatus, string> = {
  DRAFT: "Bản nháp",
  PENDING_APPROVAL: "Chờ duyệt",
  ACTIVE: "Đã đăng",
  EXPIRED: "Hết hạn",
  REJECTED: "Bị từ chối",
};

export const EMPLOYMENT_TYPE_LABELS: Record<EmploymentType, string> = {
  FULL_TIME: "Toàn thời gian",
  PART_TIME: "Bán thời gian",
  CONTRACT: "Hợp đồng",
  INTERNSHIP: "Thực tập",
};

export const LOCATION_TYPES_LABELS: Record<LocationTypes, string> = {
  ONSITE: "Tại văn phòng",
  REMOTE: "Remote",
  HYBRID: "Hybrid",
};

export const EXPERIENCE_LEVEL_LABELS: Record<ExperienceLevel, string> = {
  INTERN: "Thực tập sinh",
  JUNIOR: "Junior",
  MID: "Mid-level",
  SENIOR: "Senior",
  LEAD: "Lead / Manager",
};

export interface JobRequest {
  title: string;
  description: string;
  requirements?: string | null;
  benefits?: string | null;
  categoryId: number;
  numberOfOpenings?: number | null;
  salaryMin?: number | null;
  salaryMax?: number | null;
  currency?: string | null;
  location?: string | null;
  locationTypes: LocationTypes;
  employmentType: EmploymentType;
  experienceLevel: ExperienceLevel;
  skillIds?: number[] | null;
}

export interface JobListResponse {
  id: string;
  title: string;
  status: JobStatus;
  locationTypes: LocationTypes;
  employmentType: EmploymentType;
  experienceLevel: ExperienceLevel;
  salaryMin?: number | null;
  salaryMax?: number | null;
  currency?: string | null;
  numberOfOpenings?: number | null;
  companyName: string;
  createdAt: string;
}

export interface SkillResponse {
  id: number;
  name: string;
  isActive: boolean;
}

export interface JobResponse extends JobListResponse {
  slug: string;
  description: string;
  requirements?: string | null;
  benefits?: string | null;
  location?: string | null;
  postedDate?: string | null;
  expirationDate?: string | null;
  updatedAt: string;
  companyId: string;
  categoryId: number;
  categoryName: string;
  skills: SkillResponse[];
}

export interface JobCategoryResponse {
  id: number;
  name: string;
}

export interface EmployerJobParams {
  page?: number;
  size?: number;
  status?: JobStatus;
  keyword?: string;
}
```

---

### Client — C2: Create `src/api/employerJob.ts`

```typescript
import type { PageResponse } from "@/types/pagination";
import type { EmployerJobParams, JobCategoryResponse, JobRequest, JobResponse } from "@/types/job";
import ApiError from "@/utils/ApiError";
import client from "./client";

function withoutEmptyParams(params: Record<string, unknown>) {
  return Object.fromEntries(
    Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== "")
  );
}

export const employerJobApi = {
  getEmployerJobs: (params: EmployerJobParams): Promise<PageResponse<JobListResponse>> =>
    client
      .get("/employer/jobs", { params: withoutEmptyParams(params as Record<string, unknown>) })
      .then((r) => r.data)
      .catch((error) => {
        throw new ApiError(
          error.response?.data?.message || error.message || "Không thể tải danh sách việc làm.",
          error.response?.status || 500,
        );
      }),

  getEmployerJobDetail: (id: string): Promise<JobResponse> =>
    client
      .get(`/employer/jobs/${id}`)
      .then((r) => r.data)
      .catch((error) => {
        if (error.response?.status === 404)
          throw new ApiError("Không tìm thấy tin tuyển dụng.", 404);
        throw new ApiError(
          error.response?.data?.message || error.message || "Không thể tải chi tiết tin tuyển dụng.",
          error.response?.status || 500,
        );
      }),

  createEmployerJob: (request: JobRequest): Promise<JobResponse> =>
    client
      .post("/employer/jobs", request)
      .then((r) => r.data)
      .catch((error) => {
        throw new ApiError(
          error.response?.data?.message || error.message || "Tạo tin tuyển dụng thất bại.",
          error.response?.status || 500,
        );
      }),

  updateEmployerJob: (id: string, request: JobRequest): Promise<JobResponse> =>
    client
      .put(`/employer/jobs/${id}`, request)
      .then((r) => r.data)
      .catch((error) => {
        throw new ApiError(
          error.response?.data?.message || error.message || "Cập nhật tin tuyển dụng thất bại.",
          error.response?.status || 500,
        );
      }),

  submitForReview: (id: string): Promise<{ message: string }> =>
    client
      .post(`/employer/jobs/${id}/submit`)
      .then((r) => r.data)
      .catch((error) => {
        throw new ApiError(
          error.response?.data?.message || error.message || "Gửi duyệt thất bại.",
          error.response?.status || 500,
        );
      }),

  deleteEmployerJob: (id: string): Promise<{ message: string }> =>
    client
      .delete(`/employer/jobs/${id}`)
      .then((r) => r.data)
      .catch((error) => {
        throw new ApiError(
          error.response?.data?.message || error.message || "Xóa tin tuyển dụng thất bại.",
          error.response?.status || 500,
        );
      }),

  getCategories: (): Promise<JobCategoryResponse[]> =>
    client
      .get("/public/categories")
      .then((r) => r.data)
      .catch((error) => {
        throw new ApiError(
          error.message || "Không thể tải danh sách ngành nghề.",
          error.response?.status || 500,
        );
      }),
};
```

> [!IMPORTANT]
> You need to import `JobListResponse` since it's not used in function signatures but is the generic parameter for `PageResponse<JobListResponse>`.

---

### Client — C3: Create `src/hooks/useEmployerJobs.ts`

```typescript
import { employerJobApi } from "@/api/employerJob";
import type { EmployerJobParams, JobRequest } from "@/types/job";
import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

export const EMPLOYER_JOB_KEYS = {
  lists: () => ["employer", "jobs"] as const,
  list: (params: EmployerJobParams) => ["employer", "jobs", "list", params] as const,
  details: () => ["employer", "jobs", "detail"] as const,
  detail: (id: string) => ["employer", "jobs", "detail", id] as const,
};

export function useEmployerJobs(params: EmployerJobParams) {
  return useQuery({
    queryKey: EMPLOYER_JOB_KEYS.list(params),
    queryFn: () => employerJobApi.getEmployerJobs(params),
    placeholderData: keepPreviousData,
    retry: false,
  });
}

export function useEmployerJobDetail(id: string | undefined) {
  return useQuery({
    queryKey: EMPLOYER_JOB_KEYS.detail(id!),
    queryFn: () => employerJobApi.getEmployerJobDetail(id!),
    enabled: !!id,
    retry: false,
  });
}

export function useCreateEmployerJob() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: JobRequest) => employerJobApi.createEmployerJob(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: EMPLOYER_JOB_KEYS.lists() });
    },
  });
}

export function useUpdateEmployerJob() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, request }: { id: string; request: JobRequest }) =>
      employerJobApi.updateEmployerJob(id, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: EMPLOYER_JOB_KEYS.lists() });
      queryClient.invalidateQueries({ queryKey: EMPLOYER_JOB_KEYS.details() });
    },
  });
}

export function useSubmitForReview() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => employerJobApi.submitForReview(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: EMPLOYER_JOB_KEYS.lists() });
      queryClient.invalidateQueries({ queryKey: EMPLOYER_JOB_KEYS.details() });
    },
  });
}

export function useDeleteEmployerJob() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => employerJobApi.deleteEmployerJob(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: EMPLOYER_JOB_KEYS.lists() });
    },
  });
}

export function useCategories() {
  return useQuery({
    queryKey: ["categories"],
    queryFn: () => employerJobApi.getCategories(),
    staleTime: Infinity,
  });
}
```

---

### Client — C4: Create `src/lib/schemas/job.ts`

```typescript
import { z } from "zod";

export const jobSchema = z
  .object({
    title: z.string().min(1, "Tiêu đề không được để trống").max(255, "Tiêu đề tối đa 255 ký tự"),
    description: z.string().min(1, "Mô tả không được để trống"),
    requirements: z.string().optional().default(""),
    benefits: z.string().optional().default(""),
    categoryId: z.coerce.number({ required_error: "Vui lòng chọn ngành nghề", invalid_type_error: "Vui lòng chọn ngành nghề" }),
    numberOfOpenings: z.coerce.number().int().min(1).default(1).optional(),
    salaryMin: z.coerce.number().min(0, "Lương không được âm").optional().nullable(),
    salaryMax: z.coerce.number().min(0, "Lương không được âm").optional().nullable(),
    currency: z.string().default("VND"),
    location: z.string().optional().default(""),
    locationTypes: z.enum(["ONSITE", "REMOTE", "HYBRID"], { required_error: "Vui lòng chọn hình thức làm việc" }),
    employmentType: z.enum(["FULL_TIME", "PART_TIME", "CONTRACT", "INTERNSHIP"], { required_error: "Vui lòng chọn loại hình" }),
    experienceLevel: z.enum(
      ["INTERN", "JUNIOR", "MID", "SENIOR", "LEAD"],
      { required_error: "Vui lòng chọn cấp bậc kinh nghiệm" },
    ),
    skillIds: z.array(z.number()).optional().default([]),
  })
  .refine(
    (data) => {
      if (data.salaryMin != null && data.salaryMax != null) {
        return data.salaryMax >= data.salaryMin;
      }
      return true;
    },
    { message: "Lương tối đa phải lớn hơn hoặc bằng lương tối thiểu", path: ["salaryMax"] },
  );

export type JobFormData = z.infer<typeof jobSchema>;
```

> [!NOTE]
> The `EmploymentType` enum in the current `types/job.ts` includes `"INTERNSHIP"` (4 values), matching the updated schema. The `ExperienceLevel` enum does NOT include `"NOT_REQUIRED"` — keep `locationTypes` order consistent with the type file.

---

### Client — C5: `JobsPage.tsx`

**File:** `client/src/features/employer/JobsPage.tsx` (currently a stub)

**Reference pattern:** `src/features/admin/CompaniesPage.tsx` (same DataTable + pagination pattern)

**Implementation outline:**
- Header: "Việc làm của tôi" + "Tạo tin tuyển dụng" button (→ `/employer/jobs/new`)
- Filters: search input (debounced via `useDeferredValue`) + status dropdown
- DataTable with columns: title (clickable → detail), status (colored badge), employment type, salary range, created date, actions
- Actions: View (always), Edit (DRAFT only), Submit (DRAFT only), Delete (DRAFT only) — with confirmation dialogs via `BaseDialog` or shadcn `AlertDialog`
- Pagination via DataTable's `pageable` prop
- Uses `useEmployerJobs(queryParams)`, `useSubmitForReview()`, `useDeleteEmployerJob()`

---

### Client — C6: `CreateJobPage.tsx`

**File:** `client/src/features/employer/CreateJobPage.tsx` (currently a stub)

**Implementation outline:**
- Form using `react-hook-form` + `zodResolver(jobSchema)` + `useForm<JobFormData>`
- Cards:
  - **Thông tin cơ bản:** title (input), description (textarea), requirements (textarea), benefits (textarea)
  - **Chi tiết công việc:** categoryId (select via `useCategories()`), skillIds (checkbox grid, 3 cols, via `useAllSkills()`), experienceLevel (select), numberOfOpenings (number input)
  - **Lương & Địa điểm:** salaryMin/Max (number), currency (select: VND/USD/EUR/JPY), location (input), locationTypes (radio group), employmentType (select)
- Submit → `useCreateEmployerJob().mutateAsync(formData)` → toast + navigate to `/employer/jobs`

---

### Client — C7: `JobDetailPage.tsx`

**File:** `client/src/features/employer/JobDetailPage.tsx` (currently a stub)

**Implementation outline:**
- View/edit toggle mode on same page
- **View mode:** structured layout showing all job fields + skills as badges
- **Edit mode:** same form as CreateJobPage, pre-filled via `useEmployerJobDetail(id)`, save via `useUpdateEmployerJob()`
- Action buttons conditionally shown per status (see table below)
- Submit for review with confirmation dialog

**Action buttons per status:**

| Status | Edit | Submit | Delete |
|--------|------|--------|--------|
| DRAFT | ✅ | ✅ | ✅ |
| PENDING_APPROVAL | ❌ disabled | ❌ hidden | ❌ hidden |
| ACTIVE | ❌ disabled | ❌ hidden | ❌ hidden |
| REJECTED | ✅ | ✅ | ✅ |
| EXPIRED | ❌ disabled | ❌ hidden | ❌ hidden |

---

## Implementation Order

1. **Fix RefreshToken cascade** — `cascade = CascadeType.ALL` is a data-loss bug
2. **S2** — Categories endpoint (needed by C6 form dropdown)
3. **C1** — Expand `types/job.ts` (no dependencies)
4. **C2** — Create `api/employerJob.ts` (depends on C1; unblocks the broken test)
5. **C3** — Create `hooks/useEmployerJobs.ts` (depends on C2)
6. **C4** — Create `schemas/job.ts` (independent)
7. **C5** — Update `JobsPage.tsx` (depends on C3, C1)
8. **C6** — Update `CreateJobPage.tsx` (depends on C3, C4, C1)
9. **C7** — Update `JobDetailPage.tsx` (depends on C3, C4, C1)

---

## Verification

```bash
# Server
cd server && ./mvnw compile && ./mvnw test

# Client
cd client && npx tsc --noEmit && npx vitest run
```

---

## Reference: Existing Patterns

| What you need | Where to look |
|--------------|---------------|
| Paginated table with DataTable | `src/features/admin/CompaniesPage.tsx` |
| Query keys + keepPreviousData | `src/hooks/useAdminCompanies.ts` |
| API module structure | `src/api/skill.ts` or `src/api/admin.ts` |
| Zod schema with refine | `src/lib/schemas/profile.ts` |
| Form with react-hook-form + shadcn | `src/features/candidate/ProfilePage.tsx` |
| Shadcn components available | `src/components/ui/` (button, card, badge, input, select, checkbox, radio-group, dialog, form, label, textarea, skeleton, tooltip) |
| Route constants | `src/utils/RouterRoutes.ts` |
| Navigation helper | `src/lib/navigate.ts` |
