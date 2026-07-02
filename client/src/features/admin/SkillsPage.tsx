import { BaseDialog } from "@/components/shared/BaseDialog";
import { DataTable, type DataTableActions } from "@/components/shared/DataTable";
import { FilterToolbar } from "@/components/shared/FilterToolbar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Field, FieldContent, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { useAdminSkills, useCreateSkill, useDeleteSkill, useToggleSkill, useUpdateSkill } from "@/hooks/useAdminSkills";
import { useDebounce } from "@/hooks/useDebounce";
import { skillSchema, type SkillFormData } from "@/lib/schemas/skill";
import type { PaginationParams } from "@/types/pagination";
import type { SkillResponse } from "@/types/skill";
import { formatDate } from "@/utils/DateUtils";
import getErrorMessage from "@/utils/getErrorMessage";
import { zodResolver } from "@hookform/resolvers/zod";
import { GraduationCap, Loader2, Pencil, Plus, Power, Save, Trash2, X } from "lucide-react";
import { useCallback, useEffect, useMemo, useState } from "react";
import { useForm, useWatch } from "react-hook-form";
import { useSearchParams } from "react-router-dom";
import { toast } from "sonner";

const PAGE_SIZE = 10;
const DEFAULT_PAGE = 0;

export default function SkillsPage() {
	const [searchParams, setSearchParams] = useSearchParams();

	const page = parseInt(searchParams.get("page") || String(DEFAULT_PAGE), 10);
	const pageSize = parseInt(searchParams.get("size") || String(PAGE_SIZE), 10);
	const statusParam = searchParams.get("status") || "ALL";
	const sortParam = searchParams.get("sort") || "ALL";
	const keywordParam = searchParams.get("keyword") || null;

	const [localSearchParams, setLocalSearchParams] = useState(keywordParam || "");
	const debouncedSearch = useDebounce(localSearchParams, 400);

	const [sheetOpen, setSheetOpen] = useState(false);
	const [editingSkill, setEditingSkill] = useState<SkillResponse | null>(null);
	const [confirmDialog, setConfirmDialog] = useState<{
		open: boolean;
		title: string;
		description: string;
		onConfirm: () => void;
	}>({ open: false, title: "", description: "", onConfirm: () => {} });

	const updateSearchParams = useCallback(
		(updates: Record<string, string | null>) => {
			const nextParams = new URLSearchParams(searchParams);
			for (const [key, value] of Object.entries(updates)) {
				if (value !== null) {
					nextParams.set(key, value);
				} else {
					nextParams.delete(key);
				}
			}
			setSearchParams(nextParams);
		},
		[searchParams, setSearchParams],
	);

	useEffect(() => {
		if (debouncedSearch !== (keywordParam || "")) {
			const nextParams = new URLSearchParams(searchParams);
			if (debouncedSearch) {
				nextParams.set("keyword", debouncedSearch);
			} else {
				nextParams.delete("keyword");
			}
			nextParams.set("page", String(DEFAULT_PAGE));
			setSearchParams(nextParams);
		}
	}, [debouncedSearch, keywordParam, searchParams, setSearchParams]);

	const handleStatusChange = useCallback(
		(value: string) => {
			updateSearchParams({ status: value, page: String(DEFAULT_PAGE) });
		},
		[updateSearchParams],
	);

	const handleSortChange = useCallback(
		(value: string) => {
			updateSearchParams({ sort: value, page: String(DEFAULT_PAGE) });
		},
		[updateSearchParams],
	);

	const handleResetFilters = useCallback(() => {
		const nextParams = new URLSearchParams();
		setSearchParams(nextParams);
		setLocalSearchParams("");
	}, [setSearchParams]);

	const hasActiveFilters = Boolean(localSearchParams || statusParam !== "ALL" || sortParam !== "ALL");

	const isActiveParam = useMemo(() => {
		if (statusParam === "ALL") return undefined;
		return statusParam === "active";
	}, [statusParam]);

	const queryParams = useMemo<PaginationParams>(() => {
		if (sortParam === "ALL") return { page, size: pageSize };
		const parts = sortParam.split("_");
		const sortBy = parts[0] || undefined;
		const direction = (parts[1] as "asc" | "desc") || undefined;
		return { page, size: pageSize, sortBy, direction };
	}, [page, pageSize, sortParam]);

	const { data, isError, isFetching, isLoading, refetch, error } = useAdminSkills(
		queryParams,
		keywordParam || undefined,
		isActiveParam,
	);

	const skills = data?.content ?? [];
	const totalElements = data?.totalElements ?? 0;
	const totalPages = data?.totalPages ?? 0;

	const createSkill = useCreateSkill();
	const updateSkill = useUpdateSkill();
	const toggleSkill = useToggleSkill();
	const deleteSkill = useDeleteSkill();

	const handleToggle = useCallback(
		(skill: SkillResponse) => {
			const label = skill.isActive ? "tắt" : "bật";
			setConfirmDialog({
				open: true,
				title: `Xác nhận ${label} kỹ năng`,
				description: `Bạn có chắc muốn ${label} kỹ năng "${skill.name}"?`,
				onConfirm: () => {
					toggleSkill.mutate(skill.id, {
						onSuccess: () => toast.success(`Đã ${label} kỹ năng "${skill.name}"`),
						onError: (err) => toast.error(getErrorMessage(err, "Không thể thay đổi trạng thái")),
					});
					setConfirmDialog((prev) => ({ ...prev, open: false }));
				},
			});
		},
		[toggleSkill],
	);

	const handleDelete = useCallback(
		(skill: SkillResponse) => {
			setConfirmDialog({
				open: true,
				title: "Xác nhận xóa kỹ năng",
				description: `Xóa kỹ năng "${skill.name}"? Hành động này không thể hoàn tác.`,
				onConfirm: () => {
					deleteSkill.mutate(skill.id, {
						onSuccess: () => toast.success(`Đã xóa kỹ năng "${skill.name}"`),
						onError: (err) => toast.error(getErrorMessage(err, "Không thể xóa kỹ năng")),
					});
					setConfirmDialog((prev) => ({ ...prev, open: false }));
				},
			});
		},
		[deleteSkill],
	);

	const openCreateSheet = useCallback(() => {
		setEditingSkill(null);
		setSheetOpen(true);
	}, []);

	const openEditSheet = useCallback((skill: SkillResponse) => {
		setEditingSkill(skill);
		setSheetOpen(true);
	}, []);

	const tableActions = useMemo<DataTableActions<SkillResponse>[]>(
		() => [
			{
				header: "Thao tác",
				items: [
					{
						label: "Chỉnh sửa",
						icon: Pencil,
						variant: "ghost",
						onClick: (skill) => openEditSheet(skill),
					},
					{
						label: "Bật/tắt",
						icon: Power,
						variant: "ghost",
						onClick: (skill) => handleToggle(skill),
					},
					{
						label: "Xóa",
						icon: Trash2,
						variant: "destructive",
						onClick: (skill) => handleDelete(skill),
					},
				],
			},
		],
		[handleToggle, handleDelete, openEditSheet],
	);

	const {
		register,
		handleSubmit,
		formState: { errors, isSubmitting },
		control,
	} = useForm<SkillFormData>({
		resolver: zodResolver(skillSchema),
		values: editingSkill
			? { name: editingSkill.name, isActive: editingSkill.isActive }
			: { name: "", isActive: true },
	});

	const watchIsActive = useWatch({ name: "isActive", control });

	const onSubmit = async (formData: SkillFormData) => {
		try {
			if (editingSkill) {
				await updateSkill.mutateAsync({ id: editingSkill.id, request: formData });
				toast.success("Cập nhật kỹ năng thành công");
			} else {
				await createSkill.mutateAsync(formData);
				toast.success("Thêm kỹ năng thành công");
			}
			setSheetOpen(false);
		} catch (err) {
			toast.error(getErrorMessage(err, "Không thể lưu kỹ năng"));
		}
	};

	return (
		<div className='mx-auto flex w-full flex-col gap-5'>
			{/* Header */}
			<div className='flex flex-col gap-3 md:flex-row md:items-start md:justify-between'>
				<div>
					<h1 className='text-2xl font-semibold text-foreground'>Quản lý kỹ năng</h1>
					<p className='mt-1 text-sm text-muted-foreground'>
						{totalElements.toLocaleString("vi-VN")} kỹ năng
					</p>
				</div>
				<Button
					onClick={openCreateSheet}
					variant={"outline"}
				>
					<Plus /> Thêm kỹ năng
				</Button>
			</div>

			<FilterToolbar
				searchValue={localSearchParams}
				onSearchChange={setLocalSearchParams}
				searchPlaceholder='Tìm kiếm kỹ năng...'
				resetDisabled={!hasActiveFilters}
				onReset={handleResetFilters}
				onRefetch={() => refetch()}
				isFetching={isFetching}
				selects={[
					{
						key: "status-filter",
						value: statusParam,
						onValueChange: handleStatusChange,
						placeholder: "Lọc trạng thái",
						options: [
							{ value: "ALL", label: "Tất cả" },
							{ value: "active", label: "Đang hoạt động" },
							{ value: "inactive", label: "Đã tắt" },
						],
					},
					{
						key: "sort-filter",
						value: sortParam,
						onValueChange: handleSortChange,
						placeholder: "Sắp xếp theo...",
						options: [
							{ value: "ALL", label: "Mặc định" },
							{ value: "name_asc", label: "Tên A-Z" },
							{ value: "name_desc", label: "Tên Z-A" },
							{ value: "createdAt_desc", label: "Mới nhất trước" },
							{ value: "createdAt_asc", label: "Cũ nhất trước" },
						],
					},
				]}
			/>

			<DataTable
				columns={[
					{
						key: "name",
						header: "Tên kỹ năng",
						render: (s) => <span className='font-medium'>{s.name}</span>,
					},
					{
						key: "status",
						header: "Trạng thái",
						render: (s) =>
							s.isActive ? (
								<Badge className='border-success/40 bg-success text-success-foreground py-3'>
									Đang hoạt động
								</Badge>
							) : (
								<Badge
									variant='secondary'
									className='py-3'
								>
									Đã tắt
								</Badge>
							),
					},
					{
						key: "createdAt",
						header: "Ngày tạo",
						className: "text-sm text-muted-foreground",
						render: (s) => formatDate(s.createdAt),
					},
				]}
				data={skills}
				isLoading={isLoading}
				isError={isError}
				error={error}
				onRetry={() => refetch()}
				actions={tableActions}
				emptyState={{
					icon: GraduationCap,
					title: "Chưa có kỹ năng nào",
					subtitle:
						localSearchParams || statusParam !== "ALL"
							? "Thay đổi bộ lọc hoặc tìm kiếm để xem kết quả khác."
							: 'Nhấn "Thêm kỹ năng" để tạo kỹ năng đầu tiên.',
				}}
				pageResponse={data}
				pageable={{
					page,
					pageSize,
					totalPages,
					totalElements,
					isFetching,
					onPageChange: (newPage) => updateSearchParams({ page: String(newPage) }),
					onPageSizeChange: (newSize) => {
						const nextParams = new URLSearchParams(searchParams);
						nextParams.set("size", String(newSize));
						nextParams.set("page", String(DEFAULT_PAGE));
						setSearchParams(nextParams);
					},
					label: "kỹ năng",
				}}
				minWidth='min-w-[640px]'
				size="thin"
			/>

			{/* Create/Edit Sheet */}
			<Dialog
				open={sheetOpen}
				onOpenChange={(open) => {
					if (!open) {
						setEditingSkill(null);
					}
					setSheetOpen(open);
				}}
			>
				<DialogContent className='max-w-xl'>
					<DialogHeader>
						<DialogTitle>{editingSkill ? "Chỉnh sửa kỹ năng" : "Thêm kỹ năng"}</DialogTitle>
					</DialogHeader>
					<form onSubmit={handleSubmit(onSubmit)}>
						<FieldGroup className='px-5'>
							<Field>
								<FieldLabel htmlFor='name'>Tên kỹ năng</FieldLabel>
								<FieldContent>
									<Input
										id='name'
										placeholder='Nhập tên kỹ năng'
										{...register("name")}
									/>
								</FieldContent>
								<FieldError errors={errors.name ? [{ message: errors.name.message }] : []} />
							</Field>
							<Field>
								<FieldLabel>Trạng thái</FieldLabel>
								<FieldContent>
									<label className='flex items-center gap-2 cursor-pointer'>
										<input
											type='checkbox'
											{...register("isActive")}
											className='size-4 rounded border-gray-300 text-primary focus:ring-primary'
										/>
										<span>{watchIsActive ? "Đang hoạt động" : "Đã tắt"}</span>
									</label>
								</FieldContent>
							</Field>
						</FieldGroup>
						<DialogFooter>
							<div className='flex justify-end gap-3 pt-6'>
								<Button
									variant='outline'
									type='button'
									onClick={() => {
										setSheetOpen(false);
										setEditingSkill(null);
									}}
								>
									<X /> Hủy
								</Button>
								<Button
									type='submit'
									disabled={isSubmitting || createSkill.isPending || updateSkill.isPending}
									variant={"success"}
								>
									{isSubmitting ? (
										<Loader2 className='size-4 animate-spin' />
									) : (
										<Save className='size-4' />
									)}
									Lưu
								</Button>
							</div>
						</DialogFooter>
					</form>
				</DialogContent>
			</Dialog>

			{/* Confirm Dialog */}
			<BaseDialog
				isOpen={confirmDialog.open}
				onClose={() => setConfirmDialog((prev) => ({ ...prev, open: false }))}
				title={confirmDialog.title}
				description={confirmDialog.description}
				footer={
					<div className='flex gap-3'>
						<Button
							variant='outline'
							onClick={() => setConfirmDialog((prev) => ({ ...prev, open: false }))}
						>
							Hủy
						</Button>
						<Button
							variant='destructive'
							onClick={confirmDialog.onConfirm}
							disabled={toggleSkill.isPending || deleteSkill.isPending}
						>
							Xác nhận
						</Button>
					</div>
				}
			/>
		</div>
	);
}
