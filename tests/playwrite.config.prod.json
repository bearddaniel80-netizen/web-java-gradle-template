import { defineConfig } from "@playwright/test";

export default defineConfig({
  testDir: "./e2e",

  use: {
    baseURL: process.env.BASE_URL || "http://frontend:3000",

    trace: "on-first-retry",

    screenshot: "only-on-failure",

    video: "retain-on-failure",
  },

  reporter: [
    ["list"],
    ["html", {
      outputFolder: "playwright-report",
      open: "never",
    }],
  ],
});