import { Page, Locator, expect } from '@playwright/test';

export class WorkspacesSettingsPage {
  readonly page: Page;
  readonly heading: Locator;
  readonly emailInput: Locator;
  readonly passwordInput: Locator;
  readonly navLinks: Locator;
  readonly modal: Locator;

  constructor(page: Page) {
    this.page = page;
    this.heading = page.getByRole('heading', { level: 1 });
    this.emailInput = page.getByLabel('Email');
    this.passwordInput = page.getByLabel('Password');
    this.navLinks = page.getByRole('navigation').getByRole('link');
    this.modal = page.getByRole('dialog');
  }

  async goto(workspaceId: string) {
    await this.page.goto(``);
  }

  async waitForLoad() {
    await expect(this.heading).toBeVisible();
  }

  async login(email: string, password: string) {
    await this.emailInput.fill(email);
    await this.passwordInput.fill(password);
    await this.submitButton.click();
  }

  async waitForModal() {
    await expect(this.modal).toBeVisible();
  }

  async closeModal() {
    await this.page.keyboard.press('Escape');
    await expect(this.modal).not.toBeVisible();
  }

}
