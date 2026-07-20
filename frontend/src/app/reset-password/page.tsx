import { ResetPasswordForm } from "@/components/reset-password-form";
import { Suspense } from "react";

export default function ResetPasswordPage() {
  return (
    <Suspense fallback={<div className="min-h-screen grid place-items-center">Loading...</div>}>
      <ResetPasswordForm />
    </Suspense>
  );
}
