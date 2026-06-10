import { Button } from "@/components/ui/button";
import { Field, FieldContent, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { loginSchema } from "@/lib/schemas/auth";
import { useToast } from "@/providers/ToastProvider";
import { UserRole } from "@/types/auth";
import RouterRoutes from "@/utils/RouterRoutes";
import getErrorMessage from "@/utils/getErrorMessage";
import { zodResolver } from "@hookform/resolvers/zod";
import { Eye, EyeOff, KeyRound, Mail } from "lucide-react";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { Link, Navigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";

type LoginFormData = {
	email: string;
	password: string;
};

export function LoginPage() {
	const { login, user, isAuthenticated } = useAuth();
	const {
		register,
		handleSubmit,
		formState: { errors, isSubmitting },
	} = useForm<LoginFormData>({
		resolver: zodResolver(loginSchema),
		defaultValues: { email: "", password: "" },
	});
	const [showPassword, setShowPassword] = useState(false);
	const toast = useToast();

	const onSubmit = async (data: LoginFormData) => {
		try {
			const userData = await login(data);

			if (!userData) {
				toast.error("Email hoặc mật khẩu không đúng", { position: "top-center" });
				return;
			}

			toast.success("Đăng nhập thành công", { position: "top-right" });
		} catch (error) {
			toast.error(getErrorMessage(error), { position: "top-center" });
			console.error("Login error:", error);
		}
	};

	if (isAuthenticated) {
		const redirectTo =
			user?.role === UserRole.ADMIN
				? RouterRoutes.ADMIN_DASHBOARD
				: user?.role === UserRole.EMPLOYER
					? RouterRoutes.EMPLOYER_DASHBOARD
					: RouterRoutes.HOME;
		return (
			<Navigate
				to={redirectTo}
				replace
			/>
		);
	}

	return (
		<form
			onSubmit={handleSubmit(onSubmit)}
			className='max-w-md mx-auto mt-10 p-6 border rounded'
		>
			<h2 className='text-2xl font-bold mb-6 text-center'>Đăng nhập</h2>
			<FieldGroup>
				<Field>
					<FieldLabel htmlFor='email'>Email</FieldLabel>
					<FieldContent className='flex flex-row gap-2'>
						<Input
							id='email'
							type='email'
							placeholder='Nhập email'
							autoComplete='email'
							{...register("email")}
							startIcon={<Mail size={25} />}
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
							placeholder='Nhập mật khẩu'
							autoComplete='current-password'
							{...register("password")}
							startIcon={<KeyRound size={25} />}
							endIcon={
								<Button
									onClick={() => setShowPassword(!showPassword)}
									className='flex grow-0 items-center justify-center p-0 text-muted-foreground hover:text-foreground cursor-pointer'
									tabIndex={-1}
								>
									{showPassword ? <EyeOff className='h-5! w-5!' /> : <Eye className='h-5! w-5!' />}
								</Button>
							}
						/>
					</FieldContent>
					<FieldError errors={errors.password ? [{ message: errors.password.message }] : []} />
				</Field>
			</FieldGroup>

			<div className='flex justify-end'>
				<Link
					to={RouterRoutes.FORGOT_PASSWORD}
					className='text-sm text-primary no-underline hover:no-underline mt-2 block'
				>
					Quên mật khẩu?
				</Link>
			</div>

			<Button
				type='submit'
				disabled={isSubmitting}
				variant={"primary"}
				className='w-full mt-6 font-semibold py-5 rounded-3xl'
			>
				{isSubmitting ? "Đang đăng nhập..." : "Đăng nhập"}
			</Button>

			<p className='mt-3 text-center'>
				Bạn chưa có tài khoản?{" "}
				<Link
					to={RouterRoutes.REGISTER}
					className='text-primary no-underline hover:no-underline'
				>
					Đăng ký
				</Link>
			</p>
		</form>
	);
}
