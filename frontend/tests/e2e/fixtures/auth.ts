import { test as base, Page } from '@playwright/test';

interface AuthFixtures {
  authenticatedPage: Page;
}

export const test = base.extend<AuthFixtures>({
  authenticatedPage: async ({ page }, use) => {
    // Option 1: Login via UI
    // await page.goto('/login');
    // await page.getByLabel('Email').fill(process.env.TEST_EMAIL || 'test@example.com');
    // await page.getByLabel('Password').fill(process.env.TEST_PASSWORD || 'password');
    // await page.getByRole('button', { name: 'Sign in' }).click();
    // await page.waitForURL('/dashboard');

    // Option 2: Login via API
    // const response = await page.request.post('/api/auth/login', {
    //   data: {
    //     email: process.env.TEST_EMAIL,
    //     password: process.env.TEST_PASSWORD,
    //   },
    // });
    // const { token } = await response.json();
    // await page.context().addCookies([
    //   { name: 'auth-token', value: token, domain: 'localhost', path: '/' }
    // ]);

    await use(page);
  },
});

export { expect } from '@playwright/test';
