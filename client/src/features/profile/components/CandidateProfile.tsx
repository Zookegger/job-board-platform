import { zodResolver } from "@hookform/resolvers/zod";
import { Camera, Loader2, Pencil, Save, X } from "lucide-react";
import { useRef, useState } from "react";
import { useForm } from "react-hook-form";

import CVViewer from "@/components/shared/CVViewer";
import UserAvatar from "@/components/shared/UserAvatar";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Field, FieldContent, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { useCandidateProfile, useUpdateCandidateProfile, useUploadAvatar } from "@/hooks/useProfile";
import { candidateProfileSchema, type CandidateProfileFormData } from "@/lib/schemas/profile";
import { useToast } from "@/providers/ToastProvider";
import getErrorMessage from "@/utils/getErrorMessage";
import SkillSelector from "./SkillSelector";

export default function CandidateProfile() {
	const { data: profile, isLoading } = useCandidateProfile();
	const updateProfile = useUpdateCandidateProfile();
	const uploadAvatar = useUploadAvatar();
	const toast = useToast();
	const fileInputRef = useRef<HTMLInputElement>(null);
	const [isEditing, setIsEditing] = useState(false);
	const [activeTab, setActiveTab] = useState("profile");

	const {
		register,
		handleSubmit,
		formState: { errors, isSubmitting },
		reset,
	} = useForm<CandidateProfileFormData>({
		resolver: zodResolver(candidateProfileSchema),
		values: {
			fullName: profile?.fullName ?? "",
			phone: profile?.phone ?? "",
		},
	});

	if (isLoading) {
		return (
			<div className='mx-auto max-w-2xl space-y-6 p-6'>
				<Card>
					<CardHeader>
						<Skeleton className='h-5 w-40' />
						<div className='flex gap-1 mt-2'>
							<Skeleton className='h-8 flex-1 sm:flex-initial sm:w-32 rounded-md' />
							<Skeleton className='h-8 flex-1 sm:flex-initial sm:w-32 rounded-md' />
						</div>
					</CardHeader>
					<CardContent className='space-y-6'>
						<div className='flex flex-col md:flex-row items-center gap-6'>
							<Skeleton className='size-28 sm:size-32 md:size-40 rounded-full shrink-0' />
							<div className='w-full space-y-3'>
								<Skeleton className='h-5 w-40' />
								<Skeleton className='h-4 w-60' />
								<Skeleton className='h-4 w-48' />
							</div>
						</div>
					</CardContent>
				</Card>
			</div>
		);
	}

	const onSubmit = async (data: CandidateProfileFormData) => {
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
		<div className='mx-auto w-full max-w-240 space-y-6 p-6'>
			<Tabs
				defaultValue='profile'
				value={activeTab}
				onValueChange={setActiveTab}
			>
				<Card>
					<CardHeader>
						<TabsList className='w-full sm:w-auto mt-2'>
							<CardTitle>
								<TabsTrigger
									value='profile'
									className='flex-1 sm:flex-initial'
								>
									Hồ sơ cá nhân
								</TabsTrigger>
							</CardTitle>
							<CardTitle>
								<TabsTrigger
									value='skills'
									className='flex-1 sm:flex-initial'
								>
									Kỹ năng
								</TabsTrigger>
							</CardTitle>
							<CardTitle>
								<TabsTrigger
									value='resume'
									className='flex-1 sm:flex-initial'
								>
									Sơ yếu lý lịch
								</TabsTrigger>
							</CardTitle>
						</TabsList>
					</CardHeader>

					<TabsContent value='profile'>
						<CardContent className='space-y-6'>
							<div className='flex flex-col md:flex-row items-start gap-4 md:gap-0'>
								{/* Avatar */}
								<div className='md:flex-1 flex items-center justify-center w-full md:w-auto'>
									<button
										type='button'
										onClick={handleAvatarClick}
										className='group relative cursor-pointer w-full max-w-28 sm:max-w-32 md:max-w-40 aspect-square rounded-full overflow-hidden'
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

								{/* Profile Details */}
								<div className='w-full md:px-5 md:py-3 md:flex-2'>
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
														errors={
															errors.fullName
																? [{ message: errors.fullName.message }]
																: []
														}
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
													<FieldError
														errors={errors.phone ? [{ message: errors.phone.message }] : []}
													/>
												</Field>
											</FieldGroup>

											<div className='mt-6 flex flex-col-reverse sm:flex-row gap-3'>
												<Button
													type='submit'
													variant='primary'
													disabled={isSubmitting || updateProfile.isPending}
													className='w-full sm:w-auto'
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
													className='w-full sm:w-auto'
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
							</div>
						</CardContent>
					</TabsContent>

					<TabsContent value='skills'>
						<CardContent>
							<SkillSelector />
						</CardContent>
					</TabsContent>

					<TabsContent
						value='resume'
						forceMount
						hidden={activeTab !== "resume"}
					>
						<CardContent>
							<CVViewer />
						</CardContent>
					</TabsContent>
				</Card>
			</Tabs>
		</div>
	);
}
