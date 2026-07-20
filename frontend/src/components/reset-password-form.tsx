"use client";

import Link from "next/link";
import { useState } from "react";
import { useSearchParams } from "next/navigation";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { Loader2, AlertCircle, Eye, EyeOff, CheckCircle2 } from "lucide-react";
import { toast } from "sonner";

const resetPasswordSchema = z
  .object({
    password: z.string().min(8, "Use at least 8 characters"),
    confirm: z.string().min(8, "Please confirm your password"),
  })
  .refine((v) => v.password === v.confirm, {
    message: "Passwords don't match",
    path: ["confirm"],
  });

type Values = z.infer<typeof resetPasswordSchema>;

function FieldError({ message }: { message?: string }) {
  if (!message) return null;
  return (
    <p className="mt-1.5 flex items-center gap-1 text-xs text-rose-600">
      <AlertCircle size={11} />
      {message}
    </p>
  );
}

export function ResetPasswordForm() {
  const searchParams = useSearchParams();
  const token = searchParams.get("token");

  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  const form = useForm<Values>({
    resolver: zodResolver(resetPasswordSchema),
    mode: "onTouched",
  });

  const { register, handleSubmit, formState: { errors } } = form;

  if (!token) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-[#fcfbff] px-5 py-12">
        <div className="w-full max-w-md text-center">
          <AlertCircle size={48} className="mx-auto text-rose-500 mb-4" />
          <h1 className="text-2xl font-semibold tracking-tight text-zinc-900">Invalid Link</h1>
          <p className="mt-2 text-sm text-zinc-500">
            This password reset link is invalid or missing the token.
          </p>
          <Link href="/forgot-password" className="mt-6 inline-block rounded-lg bg-violet-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-violet-700">
            Request a new link
          </Link>
        </div>
      </main>
    );
  }

  const onSubmit = async (values: Values) => {
    setLoading(true);
    try {
      const res = await fetch("http://localhost:8080/api/auth/reset-password", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ token, newPassword: values.password }),
      });

      const data = await res.json().catch(() => null);

      if (!res.ok) {
        throw new Error(data?.message || "Failed to reset password");
      }

      setSuccess(true);
      toast.success("Password reset successfully!");
    } catch (error: any) {
      toast.error(error.message ?? "Something went wrong. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="flex min-h-screen items-center justify-center bg-[#fcfbff] px-5 py-12">
      <div className="w-full max-w-md">
        {success ? (
          <div className="rounded-xl border border-zinc-200 bg-white p-8 text-center shadow-sm">
            <div className="mx-auto mb-5 grid h-12 w-12 place-items-center rounded-full bg-emerald-100 text-emerald-600">
              <CheckCircle2 size={24} />
            </div>
            <h1 className="text-2xl font-semibold tracking-tight text-zinc-900">All set!</h1>
            <p className="mt-3 text-sm text-zinc-500 leading-relaxed">
              Your password has been successfully reset. You can now log in with your new password.
            </p>
            <Link href="/login" className="mt-8 flex h-11 w-full items-center justify-center rounded-lg bg-violet-600 text-sm font-bold text-white transition hover:bg-violet-700">
              Go to login
            </Link>
          </div>
        ) : (
          <>
            <h1 className="text-3xl font-semibold tracking-tight">Set new password</h1>
            <p className="mt-2 text-sm text-zinc-500 mb-8">
              Your new password must be different from previously used passwords.
            </p>

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
              <label className="block" htmlFor="field-password">
                <span className="mb-1.5 block text-sm font-medium">New password</span>
                <div className="relative">
                  <input
                    id="field-password"
                    type={showPassword ? "text" : "password"}
                    {...register("password")}
                    placeholder="Enter new password"
                    autoComplete="new-password"
                    className="h-11 w-full rounded-lg border border-zinc-200 bg-white px-3 pr-10 text-sm outline-none transition focus:border-violet-500 focus:ring-4 focus:ring-violet-100"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword((s) => !s)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-zinc-400 hover:text-zinc-600 transition"
                  >
                    {showPassword ? <EyeOff size={17} /> : <Eye size={17} />}
                  </button>
                </div>
                <FieldError message={errors.password?.message} />
              </label>

              <label className="block" htmlFor="field-confirm">
                <span className="mb-1.5 block text-sm font-medium">Confirm new password</span>
                <div className="relative">
                  <input
                    id="field-confirm"
                    type={showConfirm ? "text" : "password"}
                    {...register("confirm")}
                    placeholder="Confirm new password"
                    autoComplete="new-password"
                    className="h-11 w-full rounded-lg border border-zinc-200 bg-white px-3 pr-10 text-sm outline-none transition focus:border-violet-500 focus:ring-4 focus:ring-violet-100"
                  />
                  <button
                    type="button"
                    onClick={() => setShowConfirm((s) => !s)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-zinc-400 hover:text-zinc-600 transition"
                  >
                    {showConfirm ? <EyeOff size={17} /> : <Eye size={17} />}
                  </button>
                </div>
                <FieldError message={errors.confirm?.message} />
              </label>

              <button
                type="submit"
                disabled={loading}
                className="mt-6 flex h-11 w-full items-center justify-center gap-2 rounded-lg bg-violet-600 text-sm font-bold text-white transition hover:bg-violet-700 disabled:cursor-not-allowed disabled:opacity-70"
              >
                {loading && <Loader2 className="animate-spin" size={17} />}
                Reset password
              </button>
            </form>
          </>
        )}
      </div>
    </main>
  );
}
