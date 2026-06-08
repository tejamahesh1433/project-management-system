import { Page, Locator, expect } from '@playwright/test';

export class WorkspacesProjectsAnalyticsPage {
  readonly page: Page;
  readonly heading: Locator;

  constructor(page: Page) {
    this.page = page;
    this.heading = page.getByRole('heading', { level: 1 });
  }

  async goto(workspaceId: string, projectId: string) {
    await this.page.goto(`/workspaces`);
  }

  async waitForLoad() {
    await expect(this.heading).toBeVisible();
  }

}
