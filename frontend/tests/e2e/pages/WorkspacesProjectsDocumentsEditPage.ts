import { Page, Locator, expect } from '@playwright/test';

export class WorkspacesProjectsDocumentsEditPage {
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

  async goto(workspaceId: string, projectId: string, documentId: string) {
    await this.page.goto(`/workspaces/${workspaceId}`);
  }

  async waitForLoad() {
    await expect(this.heading).toBeVisible();
  }

  async submitForm() {
    await this.submitButton.click();
  }

}
