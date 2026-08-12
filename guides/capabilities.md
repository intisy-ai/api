---
title: Capabilities
---

# Capabilities: what a host consumes

A capability is something the HOST consumes ("I am a provider", "I contribute screens"). A
service is something ANOTHER PLUGIN consumes. Keeping the two apart is what lets one plugin be
several things at once.

Capability ids are bare names minted only by this package, because they are the host contract.
The manifest declares which ones a plugin has; `activate` supplies the implementations; the host
checks the two against each other and quarantines the plugin when they disagree.

| id | interface | what it is |
| --- | --- | --- |
| `provider` | `ProviderCapability` | talks to one upstream vendor, in canonical IR only |
| `front-door` | `FrontDoorCapability` | owns one app's wire format, both directions |
| `screens` | `ScreensCapability` | contributes navigation entries it lays out and fills |
| `settings` | `SettingsCapability` | declares fields, actions, and sections |
| `commands` | `CommandsCapability` | contributes slash commands |
| `plugin-management` | `PluginManagementCapability` | installs, updates, and removes plugins |
| `cross-app-sync` | `CrossAppSyncCapability` | reconciles state across app homes |
| `custom-endpoints` | `CustomEndpointsCapability` | serves user-defined upstream endpoints |
| `config-history` | `ConfigHistoryCapability` | keeps configuration history and restores it |
| `marketplace-source` | `MarketplaceSourceCapability` | contributes installable entries |

## Providing one

```ts
ctx.provide("screens", {
  screens: () => [{ id: "history", label: "History", layout: { kind: "stack", children: [] } }],
  read: async ({ screenId, home }) => ({ sources: { rows: await rowsFor(home) } }),
  invoke: async ({ actionId }) => ({ ok: true, refresh: true }),
});
```

`ctx.provide` is typed through `CapabilityMap`, so the implementation is checked against the
interface at compile time. An id this API version does not mint is still accepted and still
stored, because vocabularies are open; strict mode reports it so a typo does not pass silently.

## Consuming them

A host asks by capability id and gets every implementation with the plugin it came from:

```ts
for (const { pluginId, implementation } of host.capability("screens")) {
  render(pluginId, await implementation.screens());
}
```

A host never asks for a plugin by name. That rule is what keeps the number of plugins unbounded.
