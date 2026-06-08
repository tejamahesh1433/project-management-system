import { Page, Locator, expect } from '@playwright/test';

export class RegisterPage {
  readonly page: Page;
  readonly heading: Locator;
  readonly submitButton: Locator;
  readonly form: Locator;
  readonly emailInput: Locator;
  readonly passwordInput: Locator;
  readonly navLinks: Locator;

  constructor(page: Page) {
    this.page = page;
    this.heading = page.getByRole('heading', { level: 1 });
    this.submitButton = page.getByRole('button', { name: /submit/i });
    this.form = page.locator('form');
    this.emailInput = page.getByLabel('Email');
    this.passwordInput = page.getByLabel('Password');
    this.navLinks = page.getByRole('navigation').getByRole('link');
  }

  async goto() {
    await this.page.goto('/register');
  }

  async waitForLoad() {
    await expect(this.heading).toBeVisible();
  }

  async submitForm() {
    await this.submitButton.click();
  }

  async login(email: string, password: string) {
    await this.emailInput.fill(email);
    await this.passwordInput.fill(password);
    await this.submitButton.click();
  }

}
