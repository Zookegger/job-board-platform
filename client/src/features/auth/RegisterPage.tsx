import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Field, FieldContent, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Separator } from "@/components/ui/separator";
import {
	candidateRegisterSchema,
	companyRegisterSchema,
	type CandidateRegisterData,
	type CompanyRegisterData,
} from "@/lib/schemas/auth";
import { useToast } from "@/providers/ToastProvider";
import RouterRoutes from "@/utils/RouterRoutes";
import { zodResolver } from "@hookform/resolvers/zod";
import { Building, Eye, EyeOff, Lock, Mail, MapPin, Phone, Receipt, User } from "lucide-react";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { Link, useNavigate } from "react-router-dom";
import AuthApi from "../../api/auth";

export default function RegisterPage() {
	const [role, setRole] = useState<"candidate" | "employer" | null>(null);
	const navigate = useNavigate();

	if (!role) {
		return (
			<div className='max-w-md mx-auto mt-10 p-6'>
				<h2 className='text-2xl font-bold mb-6 text-center'>Bạn muốn?</h2>
				<div className='flex flex-col gap-4'>
					<Button
						onClick={() => setRole("candidate")}
						className='border-2 border-border rounded-xl p-6 text-left hover:border-primary transition-colors cursor-pointer group flex-col flex-1 items-start'
					>
						<h3 className='text-lg font-semibold'>Tôi muốn ứng tuyển</h3>
						<p className='text-sm text-muted-foreground mt-1'>Tạo tài khoản ứng viên để tìm việc</p>
					</Button>
					<Button
						onClick={() => setRole("employer")}
						className='border-2 border-border rounded-xl p-6 text-left hover:border-primary transition-colors cursor-pointer group flex-col flex-1 items-start'
						
					>
						<h3 className='text-lg font-semibold'>Tôi muốn tuyển dụng</h3>
						<p className='text-sm text-muted-foreground mt-1'>Đăng ký tài khoản nhà tuyển dụng</p>
					</Button>
				</div>
				<p className='text-center mt-6'>
					Đã có tài khoản?{" "}
					<Link
						to={RouterRoutes.LOGIN}
						className='text-primary no-underline hover:underline'
					>
						Đăng nhập
					</Link>
				</p>
			</div>
		);
	}

	if (role === "candidate")
		return (
			<CandidateRegisterForm
				onBack={() => setRole(null)}
				navigate={navigate}
			/>
		);
	return (
		<CompanyRegisterForm
			onBack={() => setRole(null)}
			navigate={navigate}
		/>
	);
}

function CandidateRegisterForm({ onBack, navigate }: { onBack: () => void; navigate: ReturnType<typeof useNavigate> }) {
	const [showPassword, setShowPassword] = useState(false);
	const toast = useToast();
	const {
		register,
		handleSubmit,
		formState: { errors, isSubmitting },
	} = useForm<CandidateRegisterData>({
		resolver: zodResolver(candidateRegisterSchema),
		defaultValues: { email: "", fullName: "", password: "", confirmPassword: "" },
	});

	const onSubmit = async (data: CandidateRegisterData) => {
		try {
			await AuthApi.registerCandidate(data);
			toast.success("Đăng ký thành công! Vui lòng đăng nhập.");
			navigate(RouterRoutes.LOGIN);
		} catch {
			toast.error("Đăng ký thất bại. Vui lòng thử lại.");
		}
	};

	return (
		<div className='max-w-md mx-auto mt-10 p-6'>
			<h2 className='text-2xl font-bold mb-6 text-center'>Đăng ký ứng viên</h2>
			<form
				onSubmit={handleSubmit(onSubmit)}
				className='flex flex-col gap-4'
			>
				<FieldGroup>
					<Field>
						<FieldLabel htmlFor='fullName'>Họ và tên</FieldLabel>
						<FieldContent>
							<Input
								id='fullName'
								placeholder='Nguyễn Văn A'
								itemType='name'
								startIcon={<User size={18} />}
								{...register("fullName")}
							/>
						</FieldContent>
						<FieldError errors={errors.fullName ? [{ message: errors.fullName.message }] : []} />
					</Field>

					<Field>
						<FieldLabel htmlFor='email'>Email</FieldLabel>
						<FieldContent>
							<Input
								id='email'
								type='email'
								placeholder='email@example.com'
								autoComplete='email'
								startIcon={<Mail size={18} />}
								{...register("email")}
							/>
						</FieldContent>
						<FieldError errors={errors.email ? [{ message: errors.email.message }] : []} />
					</Field>

					<Field>
						<FieldLabel htmlFor='password'>Mật khẩu</FieldLabel>
						<FieldContent>
							<Input
								id='password'
								type={showPassword ? "text" : "password"}
								placeholder='Ít nhất 8 ký tự'
								autoComplete='new-password'
								startIcon={<Lock size={18} />}
								endIcon={
									<button
										type='button'
										onClick={() => setShowPassword(!showPassword)}
										className='flex items-center justify-center text-muted-foreground hover:text-foreground cursor-pointer'
										tabIndex={-1}
									>
										{showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
									</button>
								}
								{...register("password")}
							/>
						</FieldContent>
						<FieldError errors={errors.password ? [{ message: errors.password.message }] : []} />
					</Field>

					<Field>
						<FieldLabel htmlFor='confirmPassword'>Xác nhận mật khẩu</FieldLabel>
						<FieldContent>
							<Input
								id='confirmPassword'
								type={showPassword ? "text" : "password"}
								placeholder='Nhập lại mật khẩu'
								autoComplete='new-password'
								startIcon={<Lock size={18} />}
								endIcon={
									<button
										type='button'
										onClick={() => setShowPassword(!showPassword)}
										className='flex items-center justify-center text-muted-foreground hover:text-foreground cursor-pointer'
										tabIndex={-1}
									>
										{showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
									</button>
								}
								{...register("confirmPassword")}
							/>
						</FieldContent>
						<FieldError
							errors={errors.confirmPassword ? [{ message: errors.confirmPassword.message }] : []}
						/>
					</Field>
				</FieldGroup>

				<Button
					type='submit'
					disabled={isSubmitting}
					className='w-full mt-2'
					variant={"primary"}
				>
					{isSubmitting ? "Đang đăng ký..." : "Đăng ký"}
				</Button>

				<button
					type='button'
					onClick={onBack}
					className='text-sm text-muted-foreground hover:underline cursor-pointer'
				>
					Quay lại chọn vai trò
				</button>
			</form>
		</div>
	);
}

function CompanyRegisterForm({ onBack, navigate }: { onBack: () => void; navigate: ReturnType<typeof useNavigate> }) {
	const [step, setStep] = useState<1 | 2 | 3>(1);
	const [confirmed, setConfirmed] = useState(false);
	const [showPassword, setShowPassword] = useState(false);
	const toast = useToast();

	const {
		register,
		handleSubmit,
		trigger,
		getValues,
		formState: { errors, isSubmitting },
	} = useForm<CompanyRegisterData>({
		resolver: zodResolver(companyRegisterSchema),
		defaultValues: {
			companyName: "",
			address: "",
			taxCode: "",
			fullName: "",
			phone: "",
			userEmail: "",
			password: "",
			confirmPassword: "",
		},
	});

	const handleNext = async () => {
		if (step === 1) {
			const valid = await trigger(["fullName", "phone", "userEmail", "password", "confirmPassword"]);
			if (!valid) return;
		}
		if (step === 2) {
			const valid = await trigger(["companyName", "address", "taxCode"]);
			if (!valid) return;
		}
		setStep((s) => (s + 1) as 1 | 2 | 3);
	};

	const handlePrev = () => {
		setStep((s) => (s - 1) as 1 | 2 | 3);
	};

	const onSubmit = async (data: CompanyRegisterData) => {
		try {
			await AuthApi.registerCompany(data);
			toast.success("Đăng ký thành công! Vui lòng đăng nhập.");
			navigate(RouterRoutes.LOGIN);
		} catch {
			toast.error("Đăng ký thất bại. Vui lòng thử lại.");
		}
	};

	const vals = getValues();

	return (
		<div className='max-w-2xl mx-auto mt-10 p-6'>
			<h2 className='text-2xl font-bold mb-8 text-center'>Đăng ký nhà tuyển dụng</h2>

			{/* Stepper */}
			<div className='flex items-center justify-center mb-8 gap-0'>
				{([1, 2, 3] as const).map((s) => {
					const isActive = step === s;
					const isCompleted = step > s;
					return (
						<div
							key={s}
							className='flex items-center'
						>
							<div className='flex flex-col items-center'>
								<div
									className={`w-10 h-10 rounded-full flex items-center justify-center text-sm font-semibold transition-colors ${
										isCompleted
											? "bg-primary text-primary-foreground"
											: isActive
												? "bg-primary text-primary-foreground ring-4 ring-primary/20"
												: "bg-muted text-muted-foreground"
									}`}
								>
									{isCompleted ? "✓" : s}
								</div>
								<span
									className={`text-xs mt-1.5 ${step === s ? "font-semibold text-foreground" : "text-muted-foreground"}`}
								>
									{s === 1 ? "Nhà tuyển dụng" : s === 2 ? "Công ty" : "Xem lại"}
								</span>
							</div>
							{s < 3 && <div className='w-16 h-px bg-border mx-2 mb-5' />}
						</div>
					);
				})}
			</div>

			<form onSubmit={handleSubmit(onSubmit)}>
				{/* Step 1: Employer Info */}
				{step === 1 && (
					<Card>
						<CardHeader>
							<CardTitle>Thông tin tài khoản</CardTitle>
							<CardDescription>Thông tin của người đại diện tuyển dụng</CardDescription>
						</CardHeader>
						<CardContent>
							<div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
								<FieldGroup>
									<Field>
										<FieldLabel htmlFor='fullName'>Người đại diện</FieldLabel>
										<FieldContent>
											<Input
												id='fullName'
												placeholder='Nguyễn Văn A'
												autoComplete='name'
												startIcon={<User size={18} />}
												{...register("fullName")}
											/>
										</FieldContent>
										<FieldError
											errors={errors.fullName ? [{ message: errors.fullName.message }] : []}
										/>
									</Field>

									<Field>
										<FieldLabel htmlFor='phone'>Số điện thoại</FieldLabel>
										<FieldContent>
											<Input
												id='phone'
												type='tel'
												placeholder='0901234567'
												autoComplete='tel'
												startIcon={<Phone size={18} />}
												{...register("phone")}
											/>
										</FieldContent>
										<FieldError errors={errors.phone ? [{ message: errors.phone.message }] : []} />
									</Field>

									<Field>
										<FieldLabel htmlFor='userEmail'>Email đăng nhập</FieldLabel>
										<FieldContent>
											<Input
												id='userEmail'
												type='email'
												placeholder='recruiter@example.com'
												autoComplete='email'
												startIcon={<Mail size={18} />}
												{...register("userEmail")}
											/>
										</FieldContent>
										<p className='text-xs text-muted-foreground mt-1'>
											Dùng để đăng nhập và liên hệ khi cần
										</p>
										<FieldError
											errors={errors.userEmail ? [{ message: errors.userEmail.message }] : []}
										/>
									</Field>
								</FieldGroup>

								<FieldGroup>
									<Field>
										<FieldLabel htmlFor='password'>Mật khẩu</FieldLabel>
										<FieldContent>
											<Input
												id='password'
												type={showPassword ? "text" : "password"}
												placeholder='Ít nhất 8 ký tự'
												autoComplete='new-password'
												startIcon={<Lock size={18} />}
												endIcon={
													<button
														type='button'
														onClick={() => setShowPassword(!showPassword)}
														className='flex items-center justify-center text-muted-foreground hover:text-foreground cursor-pointer'
														tabIndex={-1}
													>
														{showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
													</button>
												}
												{...register("password")}
											/>
										</FieldContent>
										<FieldError
											errors={errors.password ? [{ message: errors.password.message }] : []}
										/>
									</Field>

									<Field>
										<FieldLabel htmlFor='confirmPassword'>Xác nhận mật khẩu</FieldLabel>
										<FieldContent>
											<Input
												id='confirmPassword'
												type={showPassword ? "text" : "password"}
												placeholder='Nhập lại mật khẩu'
												autoComplete='new-password'
												startIcon={<Lock size={18} />}
												endIcon={
													<button
														type='button'
														onClick={() => setShowPassword(!showPassword)}
														className='flex items-center justify-center text-muted-foreground hover:text-foreground cursor-pointer'
														tabIndex={-1}
													>
														{showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
													</button>
												}
												{...register("confirmPassword")}
											/>
										</FieldContent>
										<FieldError
											errors={
												errors.confirmPassword
													? [{ message: errors.confirmPassword.message }]
													: []
											}
										/>
									</Field>
								</FieldGroup>
							</div>
						</CardContent>
					</Card>
				)}

				{/* Step 2: Company Info */}
				{step === 2 && (
					<Card>
						<CardHeader>
							<CardTitle>Thông tin công ty</CardTitle>
							<CardDescription>Thông tin doanh nghiệp dùng để đăng tuyển</CardDescription>
						</CardHeader>
						<CardContent>
							<div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
								<Field>
									<FieldLabel htmlFor='companyName'>Tên công ty</FieldLabel>
									<FieldContent>
										<Input
											id='companyName'
											placeholder='Yoedu Technology Corporation'
											autoComplete='organization'
											startIcon={<Building size={18} />}
											{...register("companyName")}
										/>
									</FieldContent>
									<FieldError
										errors={errors.companyName ? [{ message: errors.companyName.message }] : []}
									/>
								</Field>

								<Field>
									<FieldLabel htmlFor='taxCode'>Mã số thuế</FieldLabel>
									<FieldContent>
										<Input
											id='taxCode'
											placeholder='0123456789'
											autoComplete='off'
											startIcon={<Receipt size={18} />}
											{...register("taxCode")}
										/>
									</FieldContent>
									<FieldError errors={errors.taxCode ? [{ message: errors.taxCode.message }] : []} />
								</Field>

								<Field className='md:col-span-2'>
									<FieldLabel htmlFor='address'>Địa chỉ</FieldLabel>
									<FieldContent>
										<Input
											id='address'
											placeholder='123 Nguyễn Huệ, Quận 1'
											autoComplete='street-address'
											startIcon={<MapPin size={18} />}
											{...register("address")}
										/>
									</FieldContent>
									<FieldError errors={errors.address ? [{ message: errors.address.message }] : []} />
								</Field>
							</div>
						</CardContent>
					</Card>
				)}

				{/* Step 3: Review & Confirm */}
				{step === 3 && (
					<Card>
						<CardHeader>
							<CardTitle>Xem lại thông tin</CardTitle>
							<CardDescription>Vui lòng kiểm tra kỹ thông tin trước khi hoàn tất</CardDescription>
						</CardHeader>
						<CardContent>
							<div className='grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-4'>
								<div>
									<p className='text-sm font-semibold text-muted-foreground mb-2'>
										Thông tin tài khoản
									</p>
									<dl className='space-y-2'>
										<ReviewItem
											label='Người đại diện'
											value={vals.fullName}
										/>
										<ReviewItem
											label='Số điện thoại'
											value={vals.phone}
										/>
										<ReviewItem
											label='Email đăng nhập'
											value={vals.userEmail}
										/>
									</dl>
								</div>
								<div>
									<p className='text-sm font-semibold text-muted-foreground mb-2'>
										Thông tin công ty
									</p>
									<dl className='space-y-2'>
										<ReviewItem
											label='Tên công ty'
											value={vals.companyName}
										/>
										<ReviewItem
											label='Địa chỉ'
											value={vals.address}
										/>
										<ReviewItem
											label='Mã số thuế'
											value={vals.taxCode || "—"}
										/>
									</dl>
								</div>
							</div>

							<Separator className='my-6' />

							<label className='flex items-start gap-3 cursor-pointer'>
								<input
									type='checkbox'
									checked={confirmed}
									onChange={(e) => setConfirmed(e.target.checked)}
									className='mt-1 h-4 w-4 rounded border-border text-primary focus:ring-primary'
								/>
								<span className='text-sm text-muted-foreground'>
									Tôi xác nhận tất cả thông tin trên đều chính xác và chịu trách nhiệm về tính hợp lệ
									của các thông tin đã cung cấp.
								</span>
							</label>
						</CardContent>
					</Card>
				)}

				{/* Navigation Buttons */}
				<div className='flex items-center justify-between mt-8'>
					<button
						type='button'
						onClick={step > 1 ? handlePrev : onBack}
						className='text-sm text-muted-foreground hover:underline cursor-pointer'
					>
						{step > 1 ? "← Quay lại" : "Quay lại chọn vai trò"}
					</button>

					{step < 3 ? (
						<Button
							type='button'
							onClick={handleNext}
							className={"py-5 font-semibold"}
						>
							Tiếp theo →
						</Button>
					) : (
						<Button
							type='submit'
							disabled={isSubmitting || !confirmed}
						>
							{isSubmitting ? "Đang đăng ký..." : "Hoàn tất đăng ký"}
						</Button>
					)}
				</div>
			</form>
		</div>
	);
}

function ReviewItem({ label, value }: { label: string; value: string | undefined }) {
	return (
		<div className='flex flex-col'>
			<dt className='text-xs text-muted-foreground'>{label}</dt>
			<dd className='text-sm font-medium'>{value || "—"}</dd>
		</div>
	);
}
