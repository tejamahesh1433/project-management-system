import { test, expect } from '@playwright/test';
import { WorkspacesProjectsTasksOverviewPage } from './pages/WorkspacesProjectsTasksOverviewPage';

test.describe('/workspaces/[workspaceId]/projects/[projectId]/tasks/[taskId]/overview', () => {

  test('loads successfully', async ({ page }) => {
    await page.goto('/workspaces/test-id/projects/test-id/tasks/test-id/overview');
    await expect(page).toHaveURL(//workspaces/workspaceId\.\*/projects/projectId\.\*/tasks/taskId\.\*/overview/);
    // TODO: Add specific content assertions
  });

  test('has correct title', async ({ page }) => {
    await page.goto('/workspaces/test-id/projects/test-id/tasks/test-id/overview');
    // TODO: Update expected title
    await expect(page).toHaveTitle(/.*/);
  });

  test('handles dynamic parameters', async ({ page }) => {
    // TODO: Test with different parameter values
    await page.goto('/workspaces/test-id/projects/test-id/tasks/test-id/overview');
    await expect(page.locator('body')).toBeVisible();
  });
});
