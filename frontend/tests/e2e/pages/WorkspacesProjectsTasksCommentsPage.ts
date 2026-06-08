import { Page, Locator, expect } from '@playwright/test';

export class WorkspacesProjectsTasksCommentsPage {
  readonly page: Page;
  readonly heading: Locator;

  constructor(page: Page) {
    this.page = page;
    this.heading = page.getByRole('heading', { level: 1 });
  }

  async goto(workspaceId: string, projectId: string, taskId: string) {
    await this.page.goto(`/workspaces/${workspaceId}`);
  }

  async waitForLoad() {
    await expect(this.heading).toBeVisible();
  }

}
