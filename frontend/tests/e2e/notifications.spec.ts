import { test, expect } from '@playwright/test';
import { NotificationsPage } from './pages/NotificationsPage';

test.describe('/notifications', () => {

  test('loads successfully', async ({ page }) => {
    await page.goto('/notifications');
    await expect(page).toHaveURL(//notifications/);
    // TODO: Add specific content assertions
  });

  test('has correct title', async ({ page }) => {
    await page.goto('/notifications');
    // TODO: Update expected title
    await expect(page).toHaveTitle(/.*/);
  });
});
