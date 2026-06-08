import { test, expect } from '@playwright/test';
import { ActivityPage } from './pages/ActivityPage';

test.describe('/activity', () => {

  test('loads successfully', async ({ page }) => {
    await page.goto('/activity');
    await expect(page).toHaveURL(//activity/);
    // TODO: Add specific content assertions
  });

  test('has correct title', async ({ page }) => {
    await page.goto('/activity');
    // TODO: Update expected title
    await expect(page).toHaveTitle(/.*/);
  });
});
