import { zodResolver } from "@hookform/resolvers/zod";
import { Building2, Camera, Loader2, Pencil, Save, X } from "lucide-react";
import { useRef, useState } from "react";
import { useForm } from "react-hook-form";

import UserAvatar from "@/components/shared/UserAvatar";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Field, FieldContent, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import { useEmployerProfile, useUpdateEmployerProfile, useUploadAvatar, useUploadCompanyLogo } from "@/hooks/useProfile";
import { employerProfileSchema, type EmployerProfileFormData } from "@/lib/schemas/profile";
import { useToast } from "@/providers/ToastProvider";
import getErrorMessage from "@/utils/getErrorMessage";

export default function EmployerProfile() {
	const { data: profile, isLoading } = useEmployerProfile();
	const updateProfile = useUpdateEmployerProfile();
	const uploadAvatar = useUploadAvatar();
	const uploadLogo = useUploadCompanyLogo();
	const toast = useToast();
	const fileInputRef = useRef<HTMLInputElement>(null);
	const logoInputRef = useRef<HTMLInputElement>(null);
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
			<div className="mx-auto max-w-2xl space-y-6 p-6">
				<Skeleton className="h-8 w-48" />
				<Card>
					<CardContent className="flex items-center gap-4 p-6">
						<Skeleton className="h-16 w-16 rounded-full" />
						<div className="space-y-2">
							<Skeleton className="h-5 w-40" />
							<Skeleton className="h-4 w-60" />
						</div>
					</CardContent>
				</Card>
				<Card>
					<CardContent className="space-y-3 p-6">
						<Skeleton className="h-5 w-32" />
						<Skeleton className="h-4 w-full" />
						<Skeleton className="h-4 w-3/4" />
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

	const handleLogoClick = () => {
		logoInputRef.current?.click();
	};

	const handleLogoChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
		const file = e.target.files?.[0];
		if (!file) return;
		try {
			await uploadLogo.mutateAsync(file);
			toast.success("Đã cập nhật logo công ty");
		} catch (error) {
			toast.error(getErrorMessage(error));
		}
		e.target.value = "";
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

			{/* Company info card */}
			<Card>
				<CardHeader>
					<CardTitle className='flex items-center gap-2'>
						<Building2 className='h-5 w-5' />
						Thông tin công ty
					</CardTitle>
				</CardHeader>
				<CardContent>
					<div className='space-y-4'>
						{/* Logo upload */}
						<div className='flex items-center gap-4'>
							<button
								type='button'
								onClick={handleLogoClick}
								className='group relative flex h-20 w-20 cursor-pointer items-center justify-center overflow-hidden rounded-lg border-2 border-dashed border-muted-foreground/30 bg-muted hover:border-primary/50'
								disabled={uploadLogo.isPending}
							>
								{profile?.logoUrl ? (
									<img
										src={profile.logoUrl}
										alt='Logo công ty'
										className='h-full w-full object-contain p-1'
									/>
								) : (
									<Building2 className='h-8 w-8 text-muted-foreground' />
								)}
								<div className='absolute inset-0 flex items-center justify-center rounded-lg bg-black/40 opacity-0 transition-opacity group-hover:opacity-100'>
									{uploadLogo.isPending ? (
										<Loader2 className='h-5 w-5 animate-spin text-white' />
									) : (
										<Camera className='h-5 w-5 text-white' />
									)}
								</div>
							</button>
							<div>
								<p className='text-sm font-medium'>Logo công ty</p>
								<p className='text-xs text-muted-foreground'>Nhấn vào để thay đổi. PNG, JPG tối đa 5MB.</p>
							</div>
							<input
								ref={logoInputRef}
								type='file'
								accept='image/*'
								className='hidden'
								onChange={handleLogoChange}
							/>
						</div>

						<div>
							<p className='text-sm text-muted-foreground'>Tên công ty</p>
							<p className='font-medium'>{profile?.companyName || "—"}</p>
						</div>
						<div>
							<p className='text-sm text-muted-foreground'>Chức danh</p>
							<p className='font-medium'>{profile?.roleInCompany || "—"}</p>
						</div>
					</div>
				</CardContent>
			</Card>
		</div>
	);
}
