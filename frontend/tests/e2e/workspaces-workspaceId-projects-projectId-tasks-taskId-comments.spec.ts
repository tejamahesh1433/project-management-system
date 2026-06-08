import { test, expect } from '@playwright/test';
import { WorkspacesProjectsTasksCommentsPage } from './pages/WorkspacesProjectsTasksCommentsPage';

test.describe('/workspaces/[workspaceId]/projects/[projectId]/tasks/[taskId]/comments', () => {

  test('loads successfully', async ({ page }) => {
    await page.goto('/workspaces/test-id/projects/test-id/tasks/test-id/comments');
    await expect(page).toHaveURL(//workspaces/workspaceId\.\*/projects/projectId\.\*/tasks/taskId\.\*/comments/);
    // TODO: Add specific content assertions
  });

  test('has correct title', async ({ page }) => {
    await page.goto('/workspaces/test-id/projects/test-id/tasks/test-id/comments');
    // TODO: Update expected title
    await expect(page).toHaveTitle(/.*/);
  });

  test('handles dynamic parameters', async ({ page }) => {
    // TODO: Test with different parameter values
    await page.goto('/workspaces/test-id/projects/test-id/tasks/test-id/comments');
    await expect(page.locator('body')).toBeVisible();
  });
});
