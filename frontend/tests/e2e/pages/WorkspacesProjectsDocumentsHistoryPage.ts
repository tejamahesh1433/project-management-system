import { Page, Locator, expect } from '@playwright/test';

export class WorkspacesProjectsDocumentsHistoryPage {
  readonly page: Page;
  readonly heading: Locator;
  readonly emailInput: Locator;
  readonly passwordInput: Locator;

  constructor(page: Page) {
    this.page = page;
    this.heading = page.getByRole('heading', { level: 1 });
    this.emailInput = page.getByLabel('Email');
    this.passwordInput = page.getByLabel('Password');
  }

  async goto(workspaceId: string, projectId: string, documentId: string) {
    await this.page.goto(`/workspaces/${workspaceId}`);
  }

  async waitForLoad() {
    await expect(this.heading).toBeVisible();
  }

  async login(email: string, password: string) {
    await this.emailInput.fill(email);
    await this.passwordInput.fill(password);
    await this.submitButton.click();
  }

}
