import { BaseDialog } from "@/components/shared/BaseDialog";
import { DataTable } from "@/components/shared/DataTable";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Field, FieldContent, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { useAdminSkills, useCreateSkill, useDeleteSkill, useToggleSkill, useUpdateSkill } from "@/hooks/useAdminSkills";
import { skillSchema, type SkillFormData } from "@/lib/schemas/skill";
import type { PaginationParams } from "@/types/pagination";
import type { SkillResponse } from "@/types/skill";
import { formatDate } from "@/utils/DateUtils";
import getErrorMessage from "@/utils/getErrorMessage";
import { zodResolver } from "@hookform/resolvers/zod";
import { GraduationCap, Loader2, Pencil, Plus, Power, RefreshCw, Save, Search, Trash2, X } from "lucide-react";
import { useDeferredValue, useMemo, useState } from "react";
import { useForm, useWatch } from "react-hook-form";
import { toast } from "sonner";

const PAGE_SIZE = 10;

export default function SkillsPage() {
	const [page, setPage] = useState(0);
	const [pageSize, setPageSize] = useState(PAGE_SIZE);
	const [searchKeyword, setSearchKeyword] = useState("");
	const deferredSearchKeyword = useDeferredValue(searchKeyword.trim());
	const [statusFilter, setStatusFilter] = useState<"all" | "active" | "inactive">("all");
	const [sortField, setSortField] = useState("");
	const [sheetOpen, setSheetOpen] = useState(false);
	const [editingSkill, setEditingSkill] = useState<SkillResponse | null>(null);
	const [confirmDialog, setConfirmDialog] = useState<{
		open: boolean;
		title: string;
		description: string;
		onConfirm: () => void;
	}>({ open: false, title: "", description: "", onConfirm: () => {} });

	function handleSearchChange(keyword: string) {
		setSearchKeyword(keyword);
		setPage(0);
	}

	function handleStatusChange(status: "all" | "active" | "inactive") {
		setStatusFilter(status);
		setPage(0);
	}

	function handleSortChange(sortOption: string) {
		setSortField(sortOption);
		setPage(0);
	}

	const isActiveParam = useMemo(() => {
		if (statusFilter === "all") return undefined;
		return statusFilter === "active";
	}, [statusFilter]);

	const queryParams = useMemo<PaginationParams>(() => {
		const parts = sortField.split("_");
		const sortBy = parts[0] || undefined;
		const direction = (parts[1] as "asc" | "desc") || undefined;
		return { page, size: pageSize, sortBy, direction };
	}, [page, pageSize, sortField]);

	const { data, isError, isFetching, isLoading, refetch, error } = useAdminSkills(
		queryParams,
		deferredSearchKeyword || undefined,
		isActiveParam,
	);

	const skills = data?.content ?? [];
	const totalElements = data?.totalElements ?? 0;
	const totalPages = data?.totalPages ?? 0;

	const createSkill = useCreateSkill();
	const updateSkill = useUpdateSkill();
	const toggleSkill = useToggleSkill();
	const deleteSkill = useDeleteSkill();

	const handleToggle = (skill: SkillResponse) => {
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
	};

	const handleDelete = (skill: SkillResponse) => {
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
	};

	const openCreateSheet = () => {
		setEditingSkill(null);
		setSheetOpen(true);
	};

	const openEditSheet = (skill: SkillResponse) => {
		setEditingSkill(skill);
		setSheetOpen(true);
	};

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
		<div className='mx-auto flex w-full max-w-7xl flex-col gap-5'>
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

			{/* Search + Filter + Sort */}
			<div className='rounded-lg border bg-card p-4'>
				<div className='flex gap-3'>
					<div className='relative flex-1'>
						<Search className='absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground' />
						<Input
							value={searchKeyword}
							onChange={(e) => handleSearchChange(e.target.value)}
							placeholder='Tìm kiếm kỹ năng...'
							className='h-10 bg-background pl-10'
						/>
					</div>
					<select
						value={statusFilter}
						onChange={(e) => handleStatusChange(e.target.value as "all" | "active" | "inactive")}
						className='h-10 w-44 rounded-md border border-input bg-background px-3 text-sm outline-none transition focus:border-ring focus:ring-3 focus:ring-ring/50'
					>
						<option value='all'>Tất cả</option>
						<option value='active'>Đang hoạt động</option>
						<option value='inactive'>Đã tắt</option>
					</select>
					<Button
						variant='outline'
						onClick={() => refetch()}
						disabled={isFetching}
						title='Làm mới dữ liệu'
					>
						<RefreshCw className={isFetching ? "animate-spin" : ""} />
					</Button>

					<select
						value={sortField}
						onChange={(e) => handleSortChange(e.target.value)}
						className='h-10 w-44 rounded-md border border-input bg-background px-3 text-sm outline-none transition focus:border-ring focus:ring-3 focus:ring-ring/50'
						defaultValue={""}
					>
						<option value='' disabled>Sắp xếp theo...</option>
						<option value='name_asc'>Tên A-Z</option>
						<option value='name_desc'>Tên Z-A</option>
						<option value='createdAt_desc'>Mới nhất trước</option>
						<option value='createdAt_asc'>Cũ nhất trước</option>
					</select>
				</div>
			</div>

			{/* Table */}
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
					{
						key: "actions",
						header: "Hành động",
						render: (s) => (
							<div className='flex items-center gap-1'>
								<Button
									variant='ghost'
									size='icon'
									onClick={() => openEditSheet(s)}
								>
									<Pencil className='size-4' />
								</Button>
								<Button
									variant='ghost'
									size='icon'
									onClick={() => handleToggle(s)}
								>
									<Power
										className={`size-4 ${s.isActive ? "text-success" : "text-muted-foreground"}`}
									/>
								</Button>
								<Button
									variant='ghost'
									size='icon'
									onClick={() => handleDelete(s)}
								>
									<Trash2 className='size-4 text-destructive' />
								</Button>
							</div>
						),
					},
				]}
				data={skills}
				isLoading={isLoading}
				isError={isError}
				error={error}
				onRetry={() => refetch()}
				emptyState={{
					icon: GraduationCap,
					title: "Chưa có kỹ năng nào",
					subtitle:
						searchKeyword || statusFilter !== "all"
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
					onPageChange: setPage,
					onPageSizeChange: (newSize) => {
						setPageSize(newSize);
						setPage(0);
					},
					label: "kỹ năng",
				}}
				minWidth='min-w-[640px]'
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
