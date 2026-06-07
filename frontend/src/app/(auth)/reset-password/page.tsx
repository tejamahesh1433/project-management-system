"use client";
import { useState, type FormEvent } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { useToast } from "@/components/ui/toast";
import { authApi } from "@/lib/api";

export default function ResetPasswordPage() {
  const router = useRouter();
  const params = useSearchParams();
  const token = params.get("token") ?? "";
  const { toast } = useToast();

  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    const errs: Record<string, string> = {};
    if (password.length < 8) errs.password = "At least 8 characters";
    if (password !== confirm) errs.confirm = "Passwords do not match";
    if (Object.keys(errs).length) { setErrors(errs); return; }

    setLoading(true);
    try {
      await authApi.resetPassword(token, password);
      toast("success", "Password reset!", "You can now sign in with your new password.");
      router.push("/login");
    } catch {
      toast("error", "Error", "Invalid or expired reset token.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Reset password</CardTitle>
        <CardDescription>Enter your new password below</CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <Input id="password" type="password" label="New password" placeholder="Min. 8 characters" value={password} onChange={(e) => setPassword(e.target.value)} error={errors.password} />
          <Input id="confirm" type="password" label="Confirm password" placeholder="••••••••" value={confirm} onChange={(e) => setConfirm(e.target.value)} error={errors.confirm} />
          <Button type="submit" loading={loading} className="w-full">Reset password</Button>
          <Link href="/login" className="text-center text-sm text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]">
            Back to sign in
          </Link>
        </form>
      </CardContent>
    </Card>
  );
}
