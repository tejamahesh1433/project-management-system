import { test, expect } from '@playwright/test';
import { WorkspacesProjectsSprintsTasksPage } from './pages/WorkspacesProjectsSprintsTasksPage';

test.describe('/workspaces/[workspaceId]/projects/[projectId]/sprints/[sprintId]/tasks', () => {

  test('loads successfully', async ({ page }) => {
    await page.goto('/workspaces/test-id/projects/test-id/sprints/test-id/tasks');
    await expect(page).toHaveURL(//workspaces/workspaceId\.\*/projects/projectId\.\*/sprints/sprintId\.\*/tasks/);
    // TODO: Add specific content assertions
  });

  test('has correct title', async ({ page }) => {
    await page.goto('/workspaces/test-id/projects/test-id/sprints/test-id/tasks');
    // TODO: Update expected title
    await expect(page).toHaveTitle(/.*/);
  });

  test('handles dynamic parameters', async ({ page }) => {
    // TODO: Test with different parameter values
    await page.goto('/workspaces/test-id/projects/test-id/sprints/test-id/tasks');
    await expect(page.locator('body')).toBeVisible();
  });
});
