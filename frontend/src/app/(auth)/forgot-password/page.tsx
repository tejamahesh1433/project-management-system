"use client";
import { useState, type FormEvent } from "react";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { useToast } from "@/components/ui/toast";
import { authApi } from "@/lib/api";
import { CheckCircle } from "lucide-react";

export default function ForgotPasswordPage() {
  const { toast } = useToast();
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [sent, setSent] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!email) return;
    setLoading(true);
    try {
      await authApi.forgotPassword(email);
      setSent(true);
    } catch {
      toast("error", "Error", "Could not send reset email. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  if (sent) {
    return (
      <Card>
        <CardContent className="pt-6 text-center">
          <div className="flex justify-center mb-4">
            <div className="rounded-full bg-green-100 p-3">
              <CheckCircle className="h-8 w-8 text-green-600" />
            </div>
          </div>
          <h2 className="text-lg font-semibold mb-2">Check your email</h2>
          <p className="text-sm text-[var(--color-muted-foreground)] mb-4">
            We sent a password reset link to <strong>{email}</strong>
          </p>
          <Link href="/login" className="text-sm text-[var(--color-primary)] hover:underline">
            Back to sign in
          </Link>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Forgot password</CardTitle>
        <CardDescription>Enter your email to receive a reset link</CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <Input
            id="email"
            type="email"
            label="Email"
            placeholder="you@example.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            autoComplete="email"
          />
          <Button type="submit" loading={loading} className="w-full">
            Send reset link
          </Button>
          <Link href="/login" className="text-center text-sm text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)] transition-colors">
            Back to sign in
          </Link>
        </form>
      </CardContent>
    </Card>
  );
}
