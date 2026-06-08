import type { Page, Route } from "@playwright/test";

export const FAKE_USER = {
  id: "00000000-0000-0000-0000-000000000001",
  email: "test@example.com",
  displayName: "Test User",
};

export const FAKE_WORKSPACE = {
  id: "00000000-0000-0000-0000-000000000010",
  name: "Test Workspace",
  slug: "test-workspace",
  ownerId: FAKE_USER.id,
  createdAt: "2024-01-01T00:00:00Z",
  updatedAt: "2024-01-01T00:00:00Z",
};

export const FAKE_PROJECT = {
  id: "00000000-0000-0000-0000-000000000020",
  workspaceId: FAKE_WORKSPACE.id,
  name: "Test Project",
  status: "ACTIVE",
  color: "#6366f1",
  createdAt: "2024-01-01T00:00:00Z",
  updatedAt: "2024-01-01T00:00:00Z",
};

export const AUTH_RESPONSE = {
  accessToken: "fake-access-token",
  refreshToken: "fake-refresh-token",
  tokenType: "Bearer",
  expiresInSeconds: 900,
  user: FAKE_USER,
};

/** Intercept all /api/v1/* requests and return sensible defaults. */
export async function mockAllApis(page: Page) {
  await page.route("**/api/v1/**", async (route: Route) => {
    const url = route.request().url();
    const method = route.request().method();

    // Auth endpoints
    if (url.includes("/auth/login") && method === "POST") {
      return route.fulfill({ status: 200, json: AUTH_RESPONSE });
    }
    if (url.includes("/auth/register") && method === "POST") {
      return route.fulfill({ status: 200, json: AUTH_RESPONSE });
    }
    if (url.includes("/auth/logout")) {
      return route.fulfill({ status: 200, json: { message: "Logged out" } });
    }
    if (url.includes("/auth/forgot-password")) {
      return route.fulfill({ status: 200, json: { resetToken: "fake-token" } });
    }
    if (url.includes("/auth/refresh")) {
      return route.fulfill({ status: 200, json: AUTH_RESPONSE });
    }

    // Workspaces
    if (url.match(/\/workspaces$/) || url.match(/\/workspaces\?/)) {
      return route.fulfill({ status: 200, json: [FAKE_WORKSPACE] });
    }
    if (url.includes("/workspaces/") && url.includes("/members")) {
      return route.fulfill({ status: 200, json: [] });
    }
    if (url.includes("/workspaces/") && url.includes("/invitations")) {
      return route.fulfill({ status: 200, json: [] });
    }
    if (url.match(/\/workspaces\/[^/]+$/)) {
      return route.fulfill({ status: 200, json: FAKE_WORKSPACE });
    }

    // Projects
    if (url.match(/\/projects$/) || url.match(/\/projects\?/)) {
      return route.fulfill({ status: 200, json: [] });
    }
    if (url.includes("/projects/") && url.includes("/members")) {
      return route.fulfill({ status: 200, json: [] });
    }
    if (url.match(/\/projects\/[^/]+$/)) {
      return route.fulfill({ status: 200, json: FAKE_PROJECT });
    }

    // Tasks, boards, sprints, documents
    if (url.match(/\/(tasks|boards|sprints|documents|folders|labels|files)\b/)) {
      return route.fulfill({ status: 200, json: [] });
    }

    // Notifications
    if (url.includes("/notifications/unread")) {
      return route.fulfill({ status: 200, json: { count: 0 } });
    }
    if (url.includes("/notifications")) {
      return route.fulfill({ status: 200, json: [] });
    }

    // Analytics
    if (url.includes("/analytics")) {
      return route.fulfill({
        status: 200,
        json: {
          totalTasks: 0, completedTasks: 0, inProgressTasks: 0,
          todoTasks: 0, completionRate: 0,
          tasksByStatus: {}, tasksByPriority: {}, tasksByType: {},
        },
      });
    }

    // Reports, integrations, AI
    if (url.includes("/reports") || url.includes("/integrations") || url.includes("/ai")) {
      return route.fulfill({ status: 200, json: [] });
    }

    // Activity
    if (url.includes("/activities")) {
      return route.fulfill({ status: 200, json: [] });
    }

    // Default passthrough — don't mock unknown endpoints
    return route.continue();
  });
}

/** Inject auth tokens into localStorage before navigation. */
export async function injectAuthState(page: Page) {
  await page.addInitScript(() => {
    localStorage.setItem("accessToken", "fake-access-token");
    localStorage.setItem("refreshToken", "fake-refresh-token");
    localStorage.setItem(
      "auth-storage",
      JSON.stringify({
        state: {
          user: { id: "00000000-0000-0000-0000-000000000001", email: "test@example.com", displayName: "Test User" },
          isAuthenticated: true,
          _hasHydrated: true,
          accessToken: "fake-access-token",
          refreshToken: "fake-refresh-token",
        },
        version: 0,
      })
    );
  });
}
