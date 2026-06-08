/**
 * Auth setup — runs once before authenticated tests.
 * Saves browser storage state so subsequent tests don't re-login.
 */
import { test as setup, expect } from "@playwright/test";
import { mockAllApis, injectAuthState, FAKE_WORKSPACE } from "./fixtures/api-mocks";
import path from "path";

const AUTH_FILE = path.join(__dirname, ".auth/user.json");

setup("authenticate", async ({ page }) => {
  await mockAllApis(page);
  await injectAuthState(page);

  // Navigate to dashboard — with mocked APIs it will redirect to the workspace
  await page.goto("/dashboard");
  await page.waitForTimeout(2000);

  // Verify we're on a workspace page (authenticated)
  const url = page.url();
  const isWorkspacePage = url.includes("/workspaces/") || url.includes("/dashboard");
  expect(isWorkspacePage).toBeTruthy();

  // Save auth state for downstream tests
  await page.context().storageState({ path: AUTH_FILE });
});
