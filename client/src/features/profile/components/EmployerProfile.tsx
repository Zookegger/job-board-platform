import { zodResolver } from "@hookform/resolvers/zod";
import { Camera, ExternalLink, Loader2, Pencil, Save, X } from "lucide-react";
import { useRef, useState } from "react";
import { useForm } from "react-hook-form";

import UserAvatar from "@/components/shared/UserAvatar";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Field, FieldContent, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import { useEmployerProfile, useUpdateEmployerProfile, useUploadAvatar } from "@/hooks/useProfile";
import { employerProfileSchema, type EmployerProfileFormData } from "@/lib/schemas/profile";
import { useToast } from "@/providers/ToastProvider";
import getErrorMessage from "@/utils/getErrorMessage";
import RouterRoutes from "@/utils/RouterRoutes";
import { Link } from "react-router-dom";

export default function EmployerProfile() {
	const { data: profile, isLoading } = useEmployerProfile();
	const updateProfile = useUpdateEmployerProfile();
	const uploadAvatar = useUploadAvatar();
	const toast = useToast();
	const fileInputRef = useRef<HTMLInputElement>(null);
	const [isEditing, setIsEditing] = useState(false);

	const {
		register,
		handleSubmit,
		formState: { errors, isSubmitting },
		reset,
	} = useForm<EmployerProfileFormData>({
		resolver: zodResolver(employerProfileSchema),
		values: {
			fullName: profile?.fullName ?? "",
			phone: profile?.phone ?? "",
			companyName: profile?.companyName ?? "",
			roleInCompany: profile?.roleInCompany ?? "",
		},
	});

	if (isLoading) {
		return (
			<div className='mx-auto max-w-2xl space-y-6 p-6'>
				<Skeleton className='h-8 w-48' />
				<Card>
					<CardContent className='flex items-center gap-4 p-6'>
						<Skeleton className='h-16 w-16 rounded-full' />
						<div className='space-y-2'>
							<Skeleton className='h-5 w-40' />
							<Skeleton className='h-4 w-60' />
						</div>
					</CardContent>
				</Card>
				<Card>
					<CardContent className='space-y-3 p-6'>
						<Skeleton className='h-5 w-32' />
						<Skeleton className='h-4 w-full' />
						<Skeleton className='h-4 w-3/4' />
					</CardContent>
				</Card>
			</div>
		);
	}

	const onSubmit = async (data: EmployerProfileFormData) => {
		try {
			await updateProfile.mutateAsync(data);
			toast.success("Cập nhật hồ sơ thành công");
			setIsEditing(false);
		} catch (error) {
			toast.error(getErrorMessage(error));
		}
	};

	const handleAvatarClick = () => {
		fileInputRef.current?.click();
	};

	const handleAvatarChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
		const file = e.target.files?.[0];
		if (!file) return;
		try {
			await uploadAvatar.mutateAsync(file);
			toast.success("Đã cập nhật ảnh đại diện");
		} catch (error) {
			toast.error(getErrorMessage(error));
		}
	};

	const cancelEdit = () => {
		reset();
		setIsEditing(false);
	};

	return (
		<div className='mx-auto max-w-2xl space-y-6 p-6'>
			<h1 className='text-2xl font-bold'>Hồ sơ nhà tuyển dụng</h1>

			{/* Personal info card */}
			<Card>
				<CardHeader>
					<CardTitle>Thông tin cá nhân</CardTitle>
				</CardHeader>
				<CardContent className='space-y-6 flex flex-row items-start'>
					<div className='flex-1 flex items-center justify-center'>
						<button
							type='button'
							onClick={handleAvatarClick}
							className='group relative cursor-pointer w-full max-w-40 aspect-square rounded-full overflow-hidden'
						>
							<UserAvatar
								fill
								fullName={profile?.fullName ?? "User"}
								avatarUrl={profile?.avatarUrl}
							/>
							<div className='absolute inset-0 flex items-center justify-center rounded-full bg-black/40 opacity-0 transition-opacity group-hover:opacity-100'>
								<Camera className='h-6 w-6 text-white' />
							</div>
						</button>
						<input
							ref={fileInputRef}
							type='file'
							accept='image/*'
							className='hidden'
							onChange={handleAvatarChange}
						/>
					</div>

					<div className='px-5 py-3 flex-2'>
						{isEditing ? (
							<form onSubmit={handleSubmit(onSubmit)}>
								<FieldGroup>
									<Field>
										<FieldLabel htmlFor='fullName'>Họ và tên</FieldLabel>
										<FieldContent>
											<Input
												id='fullName'
												placeholder='Nhập họ và tên'
												{...register("fullName")}
											/>
										</FieldContent>
										<FieldError
											errors={errors.fullName ? [{ message: errors.fullName.message }] : []}
										/>
									</Field>

									<Field>
										<FieldLabel htmlFor='email'>Email</FieldLabel>
										<FieldContent>
											<Input
												id='email'
												value={profile?.email ?? ""}
												disabled
											/>
										</FieldContent>
									</Field>

									<Field>
										<FieldLabel htmlFor='phone'>Số điện thoại</FieldLabel>
										<FieldContent>
											<Input
												id='phone'
												placeholder='Nhập số điện thoại'
												{...register("phone")}
											/>
										</FieldContent>
										<FieldError errors={errors.phone ? [{ message: errors.phone.message }] : []} />
									</Field>
									
									<Field>
										<FieldLabel htmlFor='title'>Chức danh</FieldLabel>
										<FieldContent>
											<Input
												id='title'
												placeholder='Nhập chức danh'
												{...register("roleInCompany")}
											/>
										</FieldContent>
										<FieldError errors={errors.roleInCompany ? [{ message: errors.roleInCompany.message }] : []} />
									</Field>
								</FieldGroup>

								<div className='mt-6 flex gap-3'>
									<Button
										type='submit'
										variant='primary'
										disabled={isSubmitting || updateProfile.isPending}
									>
										{isSubmitting || updateProfile.isPending ? (
											<Loader2 className='h-4 w-4 animate-spin' />
										) : (
											<Save className='h-4 w-4' />
										)}
										Lưu thay đổi
									</Button>
									<Button
										type='button'
										variant='outline'
										onClick={cancelEdit}
									>
										<X className='h-4 w-4' />
										Huỷ
									</Button>
								</div>
							</form>
						) : (
							<div className='space-y-4'>
								<div>
									<p className='text-sm text-muted-foreground'>Họ và tên</p>
									<p className='font-medium'>{profile?.fullName || "—"}</p>
								</div>
								<div>
									<p className='text-sm text-muted-foreground'>Email</p>
									<p className='font-medium'>{profile?.email || "—"}</p>
								</div>
								<div>
									<p className='text-sm text-muted-foreground'>Số điện thoại</p>
									<p className='font-medium'>{profile?.phone || "—"}</p>
								</div>
								<div>
									<p className='text-sm text-muted-foreground'>Chức danh</p>
									<p className='font-medium'>{profile?.roleInCompany || "—"}</p>
								</div>
								<div className='flex items-center gap-2'>
									<div>
										<p className='text-sm text-muted-foreground'>Tên công ty</p>
										<p className='font-medium'>{profile?.companyName || "—"}</p>
									</div>
									<Button asChild>
										<Link to={RouterRoutes.EMPLOYER_COMPANY}>
											<ExternalLink className='h-4 w-4 text-muted-foreground' />
										</Link>
									</Button>
								</div>

								<Button
									variant='outline'
									onClick={() => setIsEditing(true)}
								>
									<Pencil className='h-4 w-4' />
									Chỉnh sửa
								</Button>
							</div>
						)}
					</div>
				</CardContent>
			</Card>
		</div>
	);
}
