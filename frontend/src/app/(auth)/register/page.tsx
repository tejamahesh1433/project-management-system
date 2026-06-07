"use client";
import { useState, type FormEvent } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { useToast } from "@/components/ui/toast";
import { useAuthStore } from "@/stores/auth";
import { authApi } from "@/lib/api";

export default function RegisterPage() {
  const router = useRouter();
  const { toast } = useToast();
  const setAuth = useAuthStore((s) => s.setAuth);

  const [form, setForm] = useState({ firstName: "", lastName: "", email: "", password: "", confirmPassword: "" });
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const set = (k: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm((f) => ({ ...f, [k]: e.target.value }));

  const validate = () => {
    const e: Record<string, string> = {};
    if (!form.firstName.trim()) e.firstName = "First name is required";
    if (!form.lastName.trim()) e.lastName = "Last name is required";
    if (!form.email) e.email = "Email is required";
    else if (!/\S+@\S+\.\S+/.test(form.email)) e.email = "Invalid email";
    if (!form.password) e.password = "Password is required";
    else if (form.password.length < 8) e.password = "At least 8 characters";
    if (form.password !== form.confirmPassword) e.confirmPassword = "Passwords do not match";
    return e;
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length) { setErrors(errs); return; }
    setErrors({});
    setLoading(true);
    try {
      const data = await authApi.register({
        email: form.email,
        password: form.password,
        firstName: form.firstName,
        lastName: form.lastName,
      });
      setAuth(data.user, data.accessToken, data.refreshToken);
      toast("success", "Account created!", "Welcome to ProjectFlow.");
      router.push("/dashboard");
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? "Registration failed";
      toast("error", "Error", msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Create account</CardTitle>
        <CardDescription>Get started with ProjectFlow for free</CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="flex flex-col gap-3">
          <div className="grid grid-cols-2 gap-3">
            <Input id="firstName" label="First name" placeholder="Jane" value={form.firstName} onChange={set("firstName")} error={errors.firstName} />
            <Input id="lastName" label="Last name" placeholder="Doe" value={form.lastName} onChange={set("lastName")} error={errors.lastName} />
          </div>
          <Input id="email" type="email" label="Email" placeholder="you@example.com" value={form.email} onChange={set("email")} error={errors.email} autoComplete="email" />
          <Input id="password" type="password" label="Password" placeholder="Min. 8 characters" value={form.password} onChange={set("password")} error={errors.password} autoComplete="new-password" />
          <Input id="confirmPassword" type="password" label="Confirm password" placeholder="••••••••" value={form.confirmPassword} onChange={set("confirmPassword")} error={errors.confirmPassword} />
          <Button type="submit" loading={loading} className="w-full mt-1">
            Create account
          </Button>
          <p className="text-center text-sm text-[var(--color-muted-foreground)]">
            Already have an account?{" "}
            <Link href="/login" className="text-[var(--color-primary)] hover:underline font-medium">
              Sign in
            </Link>
          </p>
        </form>
      </CardContent>
    </Card>
  );
}
