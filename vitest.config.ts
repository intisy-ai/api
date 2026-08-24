import { defineConfig } from "vitest/config";

/**
 * @remarks
 * The long timeout is load-bearing: the drift gates shell out to Gradle, and a cold TeaVM
 * `generateJavaScript` takes several times vitest's 5s default. Without it those tests time out on
 * every clean checkout and report a failure that reads as drift, which is the flapping gate that
 * teaches a reader to ignore red.
 */
export default defineConfig({
  test: {
    include: ["src/**/*.test.ts", "test/**/*.test.ts"],
    testTimeout: 600000,
  },
});
