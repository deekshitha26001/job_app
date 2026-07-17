"use client";

import Link from "next/link";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import {
  BriefcaseBusiness,
  Eye,
  EyeOff,
  Loader2,
  AlertCircle,
} from "lucide-react";
import { toast } from "sonner";

// ─── Schemas ─────────────────────────────────────────────────────────────────

const loginSchema = z.object({
  email: z.string().email("Enter a valid email"),
  password: z.string().min(8, "Use at least 8 characters"),
});

const signupSchema = z
  .object({
    name: z.string().min(2, "Tell us your name"),
    email: z.string().email("Enter a valid email"),
    password: z.string().min(8, "Use at least 8 characters"),
    confirm: z.string().min(8, "Please confirm your password"),
  })
  .refine((v) => v.password === v.confirm, {
    message: "Passwords don't match",
    path: ["confirm"],
  });

type LoginValues = z.infer<typeof loginSchema>;
type SignupValues = z.infer<typeof signupSchema>;
type Values = SignupValues;

// ─── OAuth helpers ────────────────────────────────────────────────────────────

const GOOGLE_CLIENT_ID = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID;
const GITHUB_CLIENT_ID = process.env.NEXT_PUBLIC_GITHUB_CLIENT_ID;

function buildGoogleOAuthUrl(redirectUri: string) {
  const params = new URLSearchParams({
    client_id: GOOGLE_CLIENT_ID!,
    redirect_uri: redirectUri,
    response_type: "code",
    scope: "openid email profile",
    access_type: "offline",
    prompt: "select_account",
  });
  return `https://accounts.google.com/o/oauth2/v2/auth?${params}`;
}

function buildGithubOAuthUrl(redirectUri: string) {
  const params = new URLSearchParams({
    client_id: GITHUB_CLIENT_ID!,
    redirect_uri: redirectUri,
    scope: "user:email",
  });
  return `https://github.com/login/oauth/authorize?${params}`;
}

// ─── Sub-components ───────────────────────────────────────────────────────────

function FieldError({ message }: { message?: string }) {
  if (!message) return null;
  return (
    <p className="mt-1.5 flex items-center gap-1 text-xs text-rose-600">
      <AlertCircle size={11} />
      {message}
    </p>
  );
}

// ─── Main Component ───────────────────────────────────────────────────────────

export function AuthForm({ mode }: { mode: "login" | "signup" }) {
  const router = useRouter();
  const { login } = useAuth();

  // Password visibility states — separate for each field
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  // Loading states — one per action
  const [loadingForm, setLoadingForm] = useState(false);
  const [loadingGoogle, setLoadingGoogle] = useState(false);
  const [loadingGithub, setLoadingGithub] = useState(false);

  const isAnyLoading = loadingForm || loadingGoogle || loadingGithub;

  const form = useForm<Values>({
    resolver: zodResolver(mode === "login" ? (loginSchema as any) : signupSchema),
    mode: "onTouched",
  });

  const { register, handleSubmit, formState: { errors } } = form;

  // ── Form submit ─────────────────────────────────────────────────────────────
  const onSubmit = async (values: Values) => {
    setLoadingForm(true);
    try {
      if (mode === "login") {
        const res = await fetch("http://localhost:8080/api/auth/login", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ email: values.email, password: values.password }),
        });

        if (!res.ok) {
          const err = await res.json().catch(() => null);
          throw new Error(err?.message || "Invalid email or password");
        }

        const data = await res.json();
        await login(data.token);
        toast.success("Welcome back!");
        router.push("/dashboard");
      } else {
        const res = await fetch("http://localhost:8080/api/auth/signup", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            name: values.name,
            email: values.email,
            password: values.password,
          }),
        });

        if (!res.ok) {
          const err = await res.json().catch(() => null);
          throw new Error(err?.message || "Sign up failed. Please try again.");
        }

        toast.success("Account created! Please log in.");
        router.push("/login");
      }
    } catch (error: any) {
      toast.error(error.message ?? "Something went wrong. Please try again.");
    } finally {
      setLoadingForm(false);
    }
  };

  // ── Google OAuth ────────────────────────────────────────────────────────────
  const handleGoogleSignIn = () => {
    if (!GOOGLE_CLIENT_ID) {
      toast.error(
        "Google Sign-In is not configured yet. Add NEXT_PUBLIC_GOOGLE_CLIENT_ID to your .env file.",
        { duration: 5000 }
      );
      return;
    }
    setLoadingGoogle(true);
    const redirectUri = `${window.location.origin}/auth/callback/google`;
    window.location.href = buildGoogleOAuthUrl(redirectUri);
  };

  // ── GitHub OAuth ────────────────────────────────────────────────────────────
  const handleGithubSignIn = () => {
    if (!GITHUB_CLIENT_ID) {
      toast.error(
        "GitHub Sign-In is not configured yet. Add NEXT_PUBLIC_GITHUB_CLIENT_ID to your .env file.",
        { duration: 5000 }
      );
      return;
    }
    setLoadingGithub(true);
    const redirectUri = `${window.location.origin}/auth/callback/github`;
    window.location.href = buildGithubOAuthUrl(redirectUri);
  };

  // ─── Render ─────────────────────────────────────────────────────────────────
  return (
    <main className="grid min-h-screen lg:grid-cols-2">
      {/* ── Left panel ── */}
      <section className="flex items-center justify-center bg-[#fcfbff] px-5 py-12">
        <div className="w-full max-w-md">
          {/* Logo */}
          <Link href="/" className="flex items-center gap-2 text-lg font-bold">
            <span className="grid h-8 w-8 place-items-center rounded-lg bg-violet-600 text-white">
              <BriefcaseBusiness size={17} />
            </span>
            Hirely
          </Link>

          {/* Heading */}
          <h1 className="mt-12 text-3xl font-semibold tracking-tight">
            {mode === "login" ? "Welcome back" : "Create your profile"}
          </h1>
          <p className="mt-2 text-sm text-zinc-500">
            {mode === "login"
              ? "Pick up right where you left off."
              : "Your next great opportunity starts here."}
          </p>

          {/* OAuth buttons */}
          <div className="mt-7 grid grid-cols-2 gap-3">
            <button
              type="button"
              id="btn-google-oauth"
              onClick={handleGoogleSignIn}
              disabled={isAnyLoading}
              className="flex h-11 items-center justify-center gap-2 rounded-lg border border-zinc-200 text-sm font-semibold transition hover:bg-zinc-50 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {loadingGoogle ? (
                <Loader2 size={16} className="animate-spin" />
              ) : (
                <svg
                  width="17"
                  height="17"
                  viewBox="0 0 48 48"
                  aria-hidden="true"
                >
                  <path
                    fill="#EA4335"
                    d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"
                  />
                  <path
                    fill="#4285F4"
                    d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"
                  />
                  <path
                    fill="#FBBC05"
                    d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"
                  />
                  <path
                    fill="#34A853"
                    d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"
                  />
                </svg>
              )}
              Google
            </button>

            <button
              type="button"
              id="btn-github-oauth"
              onClick={handleGithubSignIn}
              disabled={isAnyLoading}
              className="flex h-11 items-center justify-center gap-2 rounded-lg border border-zinc-200 text-sm font-semibold transition hover:bg-zinc-50 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {loadingGithub ? (
                <Loader2 size={16} className="animate-spin" />
              ) : (
                <svg
                  width="17"
                  height="17"
                  viewBox="0 0 24 24"
                  fill="currentColor"
                  aria-hidden="true"
                >
                  <path d="M12 .297c-6.63 0-12 5.373-12 12 0 5.303 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286 0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12" />
                </svg>
              )}
              GitHub
            </button>
          </div>

          {/* Divider */}
          <div className="my-6 flex items-center gap-3 text-xs text-zinc-400">
            <span className="h-px flex-1 bg-zinc-200" />
            or continue with email
            <span className="h-px flex-1 bg-zinc-200" />
          </div>

          {/* Email / password form */}
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
            {/* Name (signup only) */}
            {mode === "signup" && (
              <label className="block" htmlFor="field-name">
                <span className="mb-1.5 block text-sm font-medium">Full name</span>
                <input
                  id="field-name"
                  type="text"
                  {...register("name")}
                  placeholder="Full name"
                  className="h-11 w-full rounded-lg border border-zinc-200 bg-white px-3 text-sm outline-none transition focus:border-violet-500 focus:ring-4 focus:ring-violet-100"
                />
                <FieldError message={errors.name?.message} />
              </label>
            )}

            {/* Email */}
            <label className="block" htmlFor="field-email">
              <span className="mb-1.5 block text-sm font-medium">Email address</span>
              <input
                id="field-email"
                type="email"
                {...register("email")}
                placeholder="Email address"
                autoComplete="email"
                className="h-11 w-full rounded-lg border border-zinc-200 bg-white px-3 text-sm outline-none transition focus:border-violet-500 focus:ring-4 focus:ring-violet-100"
              />
              <FieldError message={errors.email?.message} />
            </label>

            {/* Password */}
            <label className="block" htmlFor="field-password">
              <span className="mb-1.5 block text-sm font-medium">Password</span>
              <div className="relative">
                <input
                  id="field-password"
                  type={showPassword ? "text" : "password"}
                  {...register("password")}
                  placeholder="Password"
                  autoComplete={mode === "login" ? "current-password" : "new-password"}
                  className="h-11 w-full rounded-lg border border-zinc-200 bg-white px-3 pr-10 text-sm outline-none transition focus:border-violet-500 focus:ring-4 focus:ring-violet-100"
                />
                <button
                  type="button"
                  aria-label={showPassword ? "Hide password" : "Show password"}
                  onClick={() => setShowPassword((s) => !s)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-zinc-400 hover:text-zinc-600 transition"
                >
                  {showPassword ? <EyeOff size={17} /> : <Eye size={17} />}
                </button>
              </div>
              <FieldError message={errors.password?.message} />
            </label>

            {/* Confirm password (signup only) */}
            {mode === "signup" && (
              <label className="block" htmlFor="field-confirm">
                <span className="mb-1.5 block text-sm font-medium">Confirm password</span>
                <div className="relative">
                  <input
                    id="field-confirm"
                    type={showConfirm ? "text" : "password"}
                    {...register("confirm")}
                    placeholder="Confirm password"
                    autoComplete="new-password"
                    className="h-11 w-full rounded-lg border border-zinc-200 bg-white px-3 pr-10 text-sm outline-none transition focus:border-violet-500 focus:ring-4 focus:ring-violet-100"
                  />
                  <button
                    type="button"
                    aria-label={showConfirm ? "Hide confirm password" : "Show confirm password"}
                    onClick={() => setShowConfirm((s) => !s)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-zinc-400 hover:text-zinc-600 transition"
                  >
                    {showConfirm ? <EyeOff size={17} /> : <Eye size={17} />}
                  </button>
                </div>
                <FieldError message={errors.confirm?.message} />
              </label>
            )}

            {/* Remember / forgot (login only) */}
            {mode === "login" && (
              <div className="flex justify-between text-sm">
                <label className="flex items-center gap-2 text-zinc-500 cursor-pointer">
                  <input
                    type="checkbox"
                    id="remember-me"
                    className="accent-violet-600"
                  />
                  Remember me
                </label>
                <a
                  href="#"
                  className="font-medium text-violet-600 hover:underline"
                  onClick={(e) => {
                    e.preventDefault();
                    toast.info("Password reset coming soon!");
                  }}
                >
                  Forgot password?
                </a>
              </div>
            )}

            {/* Submit */}
            <button
              type="submit"
              id="btn-submit-auth"
              disabled={isAnyLoading}
              className="flex h-11 w-full items-center justify-center gap-2 rounded-lg bg-violet-600 text-sm font-bold text-white transition hover:bg-violet-700 disabled:cursor-not-allowed disabled:opacity-70"
            >
              {loadingForm && <Loader2 className="animate-spin" size={17} />}
              {mode === "login" ? "Log in" : "Create account"}
            </button>
          </form>

          {/* Switch mode */}
          <p className="mt-6 text-center text-sm text-zinc-500">
            {mode === "login" ? "New to Hirely? " : "Already have an account? "}
            <Link
              className="font-semibold text-violet-600 hover:underline"
              href={mode === "login" ? "/signup" : "/login"}
            >
              {mode === "login" ? "Create an account" : "Log in"}
            </Link>
          </p>
        </div>
      </section>

      {/* ── Right panel (decorative) ── */}
      <aside className="relative hidden overflow-hidden bg-zinc-950 p-14 text-white lg:block">
        <div className="absolute -right-24 -top-20 h-96 w-96 rounded-full bg-violet-600 blur-3xl opacity-60" />
        <div className="relative flex h-full flex-col justify-between">
          <p className="max-w-md text-4xl font-semibold tracking-tight">
            "Hirely made job hunting feel intentional again."
          </p>
          <div>
            <div className="flex -space-x-2">
              {["AM", "KS", "JL", "RT"].map((x) => (
                <span
                  key={x}
                  className="grid h-9 w-9 place-items-center rounded-full border-2 border-zinc-950 bg-violet-400 text-xs font-bold"
                >
                  {x}
                </span>
              ))}
            </div>
            <p className="mt-4 font-semibold">Join 50,000+ ambitious people</p>
            <p className="mt-1 text-sm text-zinc-400">finding work they care about.</p>
          </div>
        </div>
      </aside>
    </main>
  );
}
