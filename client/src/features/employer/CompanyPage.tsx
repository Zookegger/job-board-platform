import { zodResolver } from "@hookform/resolvers/zod";
import { Building2, Camera, FileText, Globe, Loader2, Mail, MapPin, Pencil, Phone, Save, X } from "lucide-react";
import { useRef, useState } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Field, FieldContent, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import { useEmployerProfile, useUpdateEmployerProfile, useUploadCompanyLogo } from "@/hooks/useProfile";
import { useToast } from "@/providers/ToastProvider";
import getErrorMessage from "@/utils/getErrorMessage";

const companySchema = z.object({
	companyName: z.string().min(1, "Tên công ty không được để trống").max(100, "Tối đa 100 ký tự"),
	address: z.string().min(1, "Địa chỉ không được để trống"),
	description: z.string().optional(),
	website: z.string().url("Website không hợp lệ").or(z.literal("")).optional(),
	companyEmail: z.string().email("Email không hợp lệ").or(z.literal("")).optional(),
	companyPhone: z
		.string()
		.regex(/^(\\+84|84|0)(3|5|7|8|9)[0-9]{8}$/, "Số điện thoại không hợp lệ (phải 10 số, bắt đầu bằng 0)")
		.or(z.literal(""))
		.optional(),
	taxCode: z.string().max(20, "Mã số thuế không được quá 20 ký tự").optional(),
});

type CompanyFormData = z.infer<typeof companySchema>;

export default function EmployerCompanyPage() {
	const { data: profile, isLoading } = useEmployerProfile();
	const updateProfile = useUpdateEmployerProfile();
	const uploadLogo = useUploadCompanyLogo();
	const toast = useToast();
	const logoInputRef = useRef<HTMLInputElement>(null);
	const [isEditing, setIsEditing] = useState(false);

	const {
		register,
		handleSubmit,
		formState: { errors, isSubmitting },
		reset,
	} = useForm<CompanyFormData>({
		resolver: zodResolver(companySchema),
		values: {
			companyName: profile?.companyName ?? "",
			address: profile?.address ?? "",
			description: profile?.description ?? "",
			website: profile?.website ?? "",
			companyEmail: profile?.companyEmail ?? "",
			companyPhone: profile?.companyPhone ?? "",
			taxCode: profile?.taxCode ?? "",
		},
	});

	const onSubmit = async (data: CompanyFormData) => {
		try {
			await updateProfile.mutateAsync(data);
			toast.success("Cập nhật thông tin công ty thành công");
			setIsEditing(false);
		} catch (error) {
			toast.error(getErrorMessage(error));
		}
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

	if (isLoading) {
		return (
			<div className='mx-auto max-w-3xl space-y-6 p-6'>
				<Skeleton className='h-8 w-48' />
				<Card>
					<CardContent className='flex items-center gap-6 p-6'>
						<Skeleton className='h-24 w-24 rounded-lg' />
						<div className='space-y-2'>
							<Skeleton className='h-6 w-48' />
							<Skeleton className='h-4 w-32' />
						</div>
					</CardContent>
				</Card>
				<Card>
					<CardContent className='space-y-4 p-6'>
						<Skeleton className='h-4 w-full' />
						<Skeleton className='h-4 w-3/4' />
						<Skeleton className='h-4 w-1/2' />
					</CardContent>
				</Card>
			</div>
		);
	}

	return (
		<div className='mx-auto max-w-3xl space-y-6 p-6'>
			<h1 className='text-2xl font-bold'>Hồ sơ công ty</h1>

			{/* Logo + tên công ty */}
			<Card>
				<CardHeader>
					<CardTitle className='flex items-center gap-2'>
						<Building2 className='h-5 w-5' />
						Nhận diện thương hiệu
					</CardTitle>
				</CardHeader>
				<CardContent className='flex items-center gap-6'>
					<button
						type='button'
						onClick={() => logoInputRef.current?.click()}
						className='group relative flex h-24 w-24 shrink-0 cursor-pointer items-center justify-center overflow-hidden rounded-xl border-2 border-dashed border-muted-foreground/30 bg-muted hover:border-primary/50'
						disabled={uploadLogo.isPending}
					>
						{profile?.logoUrl ? (
							<img
								src={profile.logoUrl}
								alt='Logo công ty'
								className='h-full w-full object-contain p-1'
							/>
						) : (
							<Building2 className='h-10 w-10 text-muted-foreground' />
						)}
						<div className='absolute inset-0 flex items-center justify-center rounded-xl bg-black/40 opacity-0 transition-opacity group-hover:opacity-100'>
							{uploadLogo.isPending ? (
								<Loader2 className='h-6 w-6 animate-spin text-white' />
							) : (
								<Camera className='h-6 w-6 text-white' />
							)}
						</div>
					</button>
					<input
						ref={logoInputRef}
						type='file'
						accept='image/*'
						className='hidden'
						onChange={handleLogoChange}
					/>
					<div>
						<p className='text-xl font-semibold'>{profile?.companyName || "—"}</p>
						<p className='mt-1 text-sm text-muted-foreground'>
							Nhấn vào logo để thay đổi. PNG, JPG tối đa 5MB.
						</p>
					</div>
				</CardContent>
			</Card>

			{/* Thông tin chi tiết công ty */}
			<Card>
				<CardHeader className='flex flex-row items-center justify-between space-y-0'>
					<CardTitle>Thông tin công ty</CardTitle>
					{!isEditing && (
						<Button
							variant='outline'
							size='sm'
							onClick={() => setIsEditing(true)}
						>
							<Pencil className='h-4 w-4' />
							Chỉnh sửa
						</Button>
					)}
				</CardHeader>
				<CardContent>
					{isEditing ? (
						<form onSubmit={handleSubmit(onSubmit)}>
							<FieldGroup>
								<Field>
									<FieldLabel htmlFor='companyName'>Tên công ty</FieldLabel>
									<FieldContent>
										<Input
											id='companyName'
											placeholder='Tên công ty'
											{...register("companyName")}
										/>
									</FieldContent>
									<FieldError
										errors={errors.companyName ? [{ message: errors.companyName.message }] : []}
									/>
								</Field>

								<Field>
									<FieldLabel htmlFor='address'>Địa chỉ</FieldLabel>
									<FieldContent>
										<Input
											id='address'
											placeholder='Địa chỉ công ty'
											{...register("address")}
										/>
									</FieldContent>
									<FieldError errors={errors.address ? [{ message: errors.address.message }] : []} />
								</Field>

								<Field>
									<FieldLabel htmlFor='website'>Website</FieldLabel>
									<FieldContent>
										<Input
											id='website'
											placeholder='https://company.com'
											{...register("website")}
										/>
									</FieldContent>
									<FieldError errors={errors.website ? [{ message: errors.website.message }] : []} />
								</Field>

								<Field>
									<FieldLabel htmlFor='companyEmail'>Email công ty</FieldLabel>
									<FieldContent>
										<Input
											id='companyEmail'
											placeholder='contact@company.com'
											{...register("companyEmail")}
										/>
									</FieldContent>
									<FieldError
										errors={errors.companyEmail ? [{ message: errors.companyEmail.message }] : []}
									/>
								</Field>

								<Field>
									<FieldLabel htmlFor='companyPhone'>Số điện thoại công ty</FieldLabel>
									<FieldContent>
										<Input
											id='companyPhone'
											placeholder='02812345678'
											{...register("companyPhone")}
										/>
									</FieldContent>
									<FieldError
										errors={errors.companyPhone ? [{ message: errors.companyPhone.message }] : []}
									/>
								</Field>

								<Field>
									<FieldLabel htmlFor='taxCode'>Mã số thuế</FieldLabel>
									<FieldContent>
										<Input
											id='taxCode'
											placeholder='Mã số thuế'
											{...register("taxCode")}
										/>
									</FieldContent>
									<FieldError errors={errors.taxCode ? [{ message: errors.taxCode.message }] : []} />
								</Field>

								<Field>
									<FieldLabel htmlFor='description'>Mô tả công ty</FieldLabel>
									<FieldContent>
										<textarea
											id='description'
											placeholder='Mô tả về công ty...'
											rows={4}
											className='w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2'
											{...register("description")}
										/>
									</FieldContent>
								</Field>
							</FieldGroup>

							<div className='mt-6 flex gap-3'>
								<Button
									type='submit'
									variant='primary'
									disabled={isSubmitting || updateProfile.isPending}
								>
									{isSubmitting || updateProfile.isPending ? (
										<Loader2 key='loader' className='h-4 w-4 animate-spin' />
									) : (
										<Save key='save' className='h-4 w-4' />
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
							<div className='flex items-start gap-3'>
								<MapPin className='mt-0.5 h-4 w-4 shrink-0 text-muted-foreground' />
								<div>
									<p className='text-sm text-muted-foreground'>Địa chỉ</p>
									<p className='font-medium'>{profile?.address || "—"}</p>
								</div>
							</div>
							<div className='flex items-start gap-3'>
								<Globe className='mt-0.5 h-4 w-4 shrink-0 text-muted-foreground' />
								<div>
									<p className='text-sm text-muted-foreground'>Website</p>
									{profile?.website ? (
										<a
											href={profile.website}
											target='_blank'
											rel='noopener noreferrer'
											className='font-medium text-primary hover:underline'
										>
											{profile.website}
										</a>
									) : (
										<p className='font-medium'>—</p>
									)}
								</div>
							</div>
							<div className='flex items-start gap-3'>
								<Mail className='mt-0.5 h-4 w-4 shrink-0 text-muted-foreground' />
								<div>
									<p className='text-sm text-muted-foreground'>Email công ty</p>
									<p className='font-medium'>{profile?.companyEmail || "—"}</p>
								</div>
							</div>
							<div className='flex items-start gap-3'>
								<Phone className='mt-0.5 h-4 w-4 shrink-0 text-muted-foreground' />
								<div>
									<p className='text-sm text-muted-foreground'>Số điện thoại công ty</p>
									<p className='font-medium'>{profile?.companyPhone || "—"}</p>
								</div>
							</div>
							<div className='flex items-start gap-3'>
								<FileText className='mt-0.5 h-4 w-4 shrink-0 text-muted-foreground' />
								<div>
									<p className='text-sm text-muted-foreground'>Mã số thuế</p>
									<p className='font-medium'>{profile?.taxCode || "—"}</p>
								</div>
							</div>
							<div>
								<p className='text-sm text-muted-foreground'>Mô tả</p>
								<p className='mt-1 whitespace-pre-line font-medium'>{profile?.description || "—"}</p>
							</div>
						</div>
					)}
				</CardContent>
			</Card>
		</div>
	);
}
