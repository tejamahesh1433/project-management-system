import { Page, Locator, expect } from '@playwright/test';

export class WorkspacesNewPage {
  readonly page: Page;
  readonly heading: Locator;
  readonly submitButton: Locator;
  readonly form: Locator;
  readonly navLinks: Locator;

  constructor(page: Page) {
    this.page = page;
    this.heading = page.getByRole('heading', { level: 1 });
    this.submitButton = page.getByRole('button', { name: /submit/i });
    this.form = page.locator('form');
    this.navLinks = page.getByRole('navigation').getByRole('link');
  }

  async goto() {
    await this.page.goto('/workspaces/new');
  }

  async waitForLoad() {
    await expect(this.heading).toBeVisible();
  }

  async submitForm() {
    await this.submitButton.click();
  }

}
