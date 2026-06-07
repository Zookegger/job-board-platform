import { Button } from "@/components/ui/button";
import { Field, FieldContent, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { loginSchema } from "@/lib/schemas/auth";
import { useToast } from "@/providers/ToastProvider";
import RouterRoutes from "@/utils/RouterRoutes";
import { zodResolver } from "@hookform/resolvers/zod";
import { Eye, EyeOff, KeyRound, Mail } from "lucide-react";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { Link, useNavigate } from "react-router-dom";
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
	const navigate = useNavigate();
	const toast = useToast();

	const onSubmit = async (data: LoginFormData) => {
		try {
			await login(data);
			navigate("/");
		} catch {
			toast.error("Email hoặc mật khẩu không đúng", { position: "top-center"});
		}
	};

	if (isAuthenticated) return <div>Chào, {user?.fullName}</div>;

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
								<button
									type='button'
									onClick={() => setShowPassword(!showPassword)}
									className='flex items-center justify-center text-muted-foreground hover:text-foreground cursor-pointer'
									tabIndex={-1}
								>
									{showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
								</button>
							}
						/>
					</FieldContent>
					<FieldError errors={errors.password ? [{ message: errors.password.message }] : []} />
				</Field>
			</FieldGroup>

			<div className='flex justify-end'>
				<Link
					to={RouterRoutes.FORGOT_PASSWORD}
					className='text-sm text-blue-600 no-underline hover:no-underline mt-2 block'
				>
					Quên mật khẩu?
				</Link>
			</div>

			<Button
				type='submit'
				disabled={isSubmitting}
				className=' w-full mt-6 bg-blue-600 hover:bg-blue-800 text-white font-semibold py-5 rounded-3xl'
			>
				{isSubmitting ? "Đang đăng nhập..." : "Đăng nhập"}
			</Button>

			<p className='mt-3 text-center'>
				Bạn chưa có tài khoản?{" "}
				<Link
					to={RouterRoutes.REGISTER}
					className='text-blue-600 no-underline hover:no-underline'
				>
					Đăng ký
				</Link>
			</p>
		</form>
	);
}
