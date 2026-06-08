import { Page, Locator, expect } from '@playwright/test';

export class WorkspacesProjectsBoardsPage {
  readonly page: Page;
  readonly heading: Locator;
  readonly modal: Locator;

  constructor(page: Page) {
    this.page = page;
    this.heading = page.getByRole('heading', { level: 1 });
    this.modal = page.getByRole('dialog');
  }

  async goto(workspaceId: string, projectId: string) {
    await this.page.goto(`/workspaces`);
  }

  async waitForLoad() {
    await expect(this.heading).toBeVisible();
  }

  async waitForModal() {
    await expect(this.modal).toBeVisible();
  }

  async closeModal() {
    await this.page.keyboard.press('Escape');
    await expect(this.modal).not.toBeVisible();
  }

}
