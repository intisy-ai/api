---
title: Quickstart
---

# Writing a plugin

A plugin is a `plugin.json` and an entry module. Nothing else is required, and
`@intisy-ai/api` is the only import a minimal plugin needs.

## 1. Declare it

```json
{
  "$schema": "https://intisy-ai.github.io/api/schema/plugin.schema.json",
  "id": "wakatime-sync",
  "api": 1,
  "entry": "dist/index.js",
  "displayName": "WakaTime",
  "capabilities": ["settings"],
  "publish": { "scopedOnly": false },
  "repo": {
    "role": "Coding-activity reporter",
    "category": "plugin",
    "tech": "typescript"
  }
}
```

`id` is the plugin's permanent identity. `api` is the LOWEST API version the plugin needs, not
the one it was built against: a host loads anything whose floor it meets.

## 2. Write the entry

```ts
import { definePlugin } from "@intisy-ai/api";

export default definePlugin({
  async activate(ctx) {
    ctx.provide("settings", {
      schema: () => ({ fields: [{ key: "apiKey", type: "secret", label: "API key" }] }),
      run: async (actionId) => ({ ok: actionId === "sync", message: "synced" }),
    });
  },
  async deactivate() {},
});
```

Everything a plugin may touch arrives on `ctx`: its manifest, its config, its logger, its home
paths, the host descriptor, the service registry, and the event bus. A class implementing
`Plugin` and `definePlugin` with an object literal produce the same shape; pick either.

## 3. Check it before a host does

```bash
npx intisy-plugin validate
npx intisy-plugin doctor --home ~/.claude
```

`validate` checks one checkout's manifest. `doctor` checks a whole app home: manifest schema,
declared-versus-provided capabilities, unresolvable consumes, orphaned services, dependency
cycles, and api floors. Every problem names the field and the fix.

## 4. Turn the quiet failures loud while developing

```bash
INTISY_PLUGIN_STRICT=1 node your-host.js
```

Unknown ids are ignored in production so a plugin from a later API still loads. Strict mode keeps
the ignoring and prints each one, which is how a typo like `"screns"` finds you in seconds.
