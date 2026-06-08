import { Page, Locator, expect } from '@playwright/test';

export class WorkspacesOverviewPage {
  readonly page: Page;
  readonly heading: Locator;
  readonly navLinks: Locator;

  constructor(page: Page) {
    this.page = page;
    this.heading = page.getByRole('heading', { level: 1 });
    this.navLinks = page.getByRole('navigation').getByRole('link');
  }

  async goto(workspaceId: string) {
    await this.page.goto(``);
  }

  async waitForLoad() {
    await expect(this.heading).toBeVisible();
  }

}
