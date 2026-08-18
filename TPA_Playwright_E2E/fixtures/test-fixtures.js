import { test as baseTest } from '@playwright/test';
import { HeaderPage } from '../pages/HeaderPage.js';
import { DashboardPage } from '../pages/DashboardPage.js';
import { ClaimDetailsPage } from '../pages/ClaimDetailsPage.js';
import { NewClaimModalPage } from '../pages/NewClaimModalPage.js';
import { ClearDataModalPage } from '../pages/ClearDataModalPage.js';
import { ApiClient } from '../utils/api.js';
import { DatabaseValidator } from '../utils/database.js';
import { initSamplePdfs } from './sample_pdfs.js';

export const test = baseTest.extend({
  headerPage: async ({ page }, use) => {
    await use(new HeaderPage(page));
  },
  dashboardPage: async ({ page }, use) => {
    await use(new DashboardPage(page));
  },
  claimDetailsPage: async ({ page }, use) => {
    await use(new ClaimDetailsPage(page));
  },
  newClaimModalPage: async ({ page }, use) => {
    await use(new NewClaimModalPage(page));
  },
  clearDataModalPage: async ({ page }, use) => {
    await use(new ClearDataModalPage(page));
  },
  apiClient: async ({ request }, use) => {
    await use(new ApiClient(request));
  },
  dbValidator: async ({ request }, use) => {
    await use(new DatabaseValidator(request));
  },
  testPdfs: async ({}, use) => {
    const pdfs = await initSamplePdfs();
    await use(pdfs);
  },
});

export { expect } from '@playwright/test';
