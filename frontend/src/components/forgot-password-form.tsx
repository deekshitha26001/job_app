"use client";

import Link from "next/link";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { Loader2, AlertCircle, ArrowLeft, MailCheck } from "lucide-react";
import { toast } from "sonner";

const forgotPasswordSchema = z.object({
  email: z.string().email("Enter a valid email"),
});

type Values = z.infer<typeof forgotPasswordSchema>;

function FieldError({ message }: { message?: string }) {
  if (!message) return null;
  return (
    <p className="mt-1.5 flex items-center gap-1 text-xs text-rose-600">
      <AlertCircle size={11} />
      {message}
    </p>
  );
}

export function ForgotPasswordForm() {
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);

  const form = useForm<Values>({
    resolver: zodResolver(forgotPasswordSchema),
    mode: "onTouched",
  });

  const { register, handleSubmit, formState: { errors } } = form;

  const onSubmit = async (values: Values) => {
    setLoading(true);
    try {
      const res = await fetch("http://localhost:8080/api/auth/forgot-password", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: values.email }),
      });

      if (!res.ok) {
        throw new Error("Something went wrong");
      }

      setSuccess(true);
      toast.success("Reset link sent!");
    } catch (error: any) {
      toast.error("Failed to send reset link. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="flex min-h-screen items-center justify-center bg-[#fcfbff] px-5 py-12">
      <div className="w-full max-w-md">
        <Link href="/login" className="mb-8 inline-flex items-center gap-2 text-sm font-medium text-zinc-500 hover:text-zinc-800 transition">
          <ArrowLeft size={16} />
          Back to login
        </Link>

        {success ? (
          <div className="rounded-xl border border-zinc-200 bg-white p-8 text-center shadow-sm">
            <div className="mx-auto mb-5 grid h-12 w-12 place-items-center rounded-full bg-emerald-100 text-emerald-600">
              <MailCheck size={24} />
            </div>
            <h1 className="text-2xl font-semibold tracking-tight text-zinc-900">Check your email</h1>
            <p className="mt-3 text-sm text-zinc-500 leading-relaxed">
              We've sent a password reset link to your email address. It will expire in 15 minutes.
            </p>
          </div>
        ) : (
          <>
            <h1 className="text-3xl font-semibold tracking-tight">Forgot password?</h1>
            <p className="mt-2 text-sm text-zinc-500 mb-8">
              No worries, we'll send you reset instructions.
            </p>

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
              <label className="block" htmlFor="field-email">
                <span className="mb-1.5 block text-sm font-medium">Email address</span>
                <input
                  id="field-email"
                  type="email"
                  {...register("email")}
                  placeholder="Enter your email"
                  autoComplete="email"
                  className="h-11 w-full rounded-lg border border-zinc-200 bg-white px-3 text-sm outline-none transition focus:border-violet-500 focus:ring-4 focus:ring-violet-100"
                />
                <FieldError message={errors.email?.message} />
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
