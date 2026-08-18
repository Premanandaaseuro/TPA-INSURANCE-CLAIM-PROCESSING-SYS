import { defineConfig, devices } from '@playwright/test';

/**
 * Playwright Configuration for TPA Insurance Claim Processing System E2E Suite
 */
export default defineConfig({
  testDir: './tests',
  timeout: 60000,
  expect: {
    timeout: 10000,
  },
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: 1,
  outputDir: './reports/test-results',
  reporter: [
    ['html', { outputFolder: 'reports/playwright-report', open: 'never' }],
    ['list'],
    ['json', { outputFile: 'reports/test-results/results.json' }]
  ],
  use: {
    baseURL: process.env.BASE_URL || 'http://localhost:7001',
    apiBaseURL: process.env.API_URL || 'http://localhost:7002/api',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'on-first-retry',
    actionTimeout: 15000,
    navigationTimeout: 30000,
    ignoreHTTPSErrors: true,
  },

  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] },
    },
    {
      name: 'webkit',
      use: { ...devices['Desktop Safari'] },
    },
  ],
});


