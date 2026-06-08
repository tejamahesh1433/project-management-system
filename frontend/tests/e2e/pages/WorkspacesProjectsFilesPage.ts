import { Page, Locator, expect } from '@playwright/test';

export class WorkspacesProjectsFilesPage {
  readonly page: Page;
  readonly heading: Locator;
  readonly submitButton: Locator;
  readonly form: Locator;

  constructor(page: Page) {
    this.page = page;
    this.heading = page.getByRole('heading', { level: 1 });
    this.submitButton = page.getByRole('button', { name: /submit/i });
    this.form = page.locator('form');
  }

  async goto(workspaceId: string, projectId: string) {
    await this.page.goto(`/workspaces`);
  }

  async waitForLoad() {
    await expect(this.heading).toBeVisible();
  }

  async submitForm() {
    await this.submitButton.click();
  }

}
