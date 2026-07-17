"use client";

import { useEffect, useState, useRef } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { BriefcaseBusiness, Loader2, AlertCircle } from "lucide-react";

/**
 * Google OAuth callback page.
 *
 * Flow:
 *   Google → redirects to /auth/callback/google?code=xxx
 *   → This page POSTs the code to the backend
 *   → Backend exchanges it for user info, finds/creates a user, returns JWT
 *   → We store the JWT and redirect to /dashboard
 */
export default function GoogleCallback() {
  const router = useRouter();
  const { login } = useAuth();
  const [error, setError] = useState<string | null>(null);
  const called = useRef(false);

  useEffect(() => {
    if (called.current) return;
    called.current = true;

    const params = new URLSearchParams(window.location.search);
    const code = params.get("code");
    const oauthError = params.get("error");

    if (oauthError) {
      setError("Google sign-in was cancelled or denied.");
      return;
    }

    if (!code) {
      setError("No authorization code received from Google.");
      return;
    }

    (async () => {
      try {
        const redirectUri = `${window.location.origin}/auth/callback/google`;

        const res = await fetch("http://localhost:8080/api/auth/oauth/google", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ code, redirectUri }),
        });

        if (!res.ok) {
          const errData = await res.json().catch(() => null);
          throw new Error(errData?.message || "Google sign-in failed. Please try again.");
        }

        const data = await res.json();
        await login(data.token);
        router.replace("/dashboard");
      } catch (err: any) {
        setError(err.message ?? "Unexpected error during Google sign-in.");
      }
    })();
  }, [login, router]);

  if (error) {
    return (
      <main className="grid min-h-screen place-items-center bg-[#fcfbff] px-4">
        <div className="text-center">
          <span className="mx-auto mb-4 grid h-12 w-12 place-items-center rounded-full bg-rose-100 text-rose-600">
            <AlertCircle size={24} />
          </span>
          <h1 className="text-xl font-semibold text-zinc-900">Sign-in failed</h1>
          <p className="mt-2 text-sm text-zinc-500">{error}</p>
          <button
            onClick={() => router.push("/login")}
            className="mt-6 rounded-lg bg-violet-600 px-5 py-2.5 text-sm font-bold text-white transition hover:bg-violet-700"
          >
            Back to Login
          </button>
        </div>
      </main>
    );
  }

  return (
    <main className="grid min-h-screen place-items-center bg-[#fcfbff]">
      <div className="text-center">
        <span className="mx-auto mb-4 grid h-12 w-12 place-items-center rounded-full bg-violet-100">
          <BriefcaseBusiness size={24} className="text-violet-600" />
        </span>
        <Loader2 size={28} className="mx-auto animate-spin text-violet-600" />
        <p className="mt-4 text-sm font-medium text-zinc-600">
          Completing Google sign-in…
        </p>
      </div>
    </main>
  );
}
