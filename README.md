# api

[![npm version](https://img.shields.io/npm/v/@intisy-ai/api)](https://www.npmjs.com/package/@intisy-ai/api)
[![npm downloads](https://img.shields.io/npm/dm/@intisy-ai/api)](https://www.npmjs.com/package/@intisy-ai/api)
[![CI](https://img.shields.io/github/actions/workflow/status/intisy-ai/api/publish.yml)](https://github.com/intisy-ai/api/actions)

The plugin contract for the intisy-ai ecosystem, published as `@intisy-ai/api`. It holds the
`plugin.json` manifest schema, the plugin context and lifecycle, the typed keys a capability or a
service is reached by, the declaration engine a host runs, and the `validate` command. The contract
is written in Java and the TypeScript surface is generated from it. It has no dependencies and
imports nothing from node outside its CLI, so a host, a plugin, and a dashboard renderer can all
import the same package.

## Under-the-Hood Architecture

```mermaid
flowchart TD
    MANIFEST[plugin.json] -->|validate| VALIDATE[validateManifest]
    MANIFEST -->|declares| CAPS[capabilities]
    MANIFEST -->|declares| SVC[services.provides / consumes]

    subgraph Host [host, one per app]
        HOST[createPluginHost]
        LEDGER[(introspection ledger)]
        HUB[(service registry)]
    end

    HOST -->|contextFor| CTX[PluginContext]
    CTX -->|provide| HOST
    CTX -->|register / get / want / watch| HUB
    CTX -->|subscribe| BUS[events]
    CTX --> LEDGER

    PLUGIN[plugin entry module] -->|activate ctx| CTX
    HOST -->|verifyActivation| CHECK{declared == provided?}
    CHECK -->|no| QUARANTINE[markBroken: capabilities and services dropped, host stays up]
    CHECK -->|yes| ACTIVE[capability id is the only dispatch key a host has]

    SVC --> ORDER[activationOrder: providers before consumers, cycles named]
```

## The permanence rules

The API is append-only from its first release. These four rules are what make that possible, and
they are enforced in review:

1. **The API package is append-only.** A published capability interface, service contract,
   manifest field, or event shape is never removed and never changes meaning. New needs get new
   ids, never edits to old ones. Deprecation is a documentation state, not a code change.
2. **Every vocabulary is open.** An unknown capability id, service id, event topic, manifest
   field, or screen-node kind is ignored with a debug log, never an error. A plugin built against
   next year's API loads on today's host, minus what the host does not know.
3. **`"api": 1` is a floor.** A host refuses to load only a plugin whose declared floor exceeds
   what the host implements, and says exactly that. Everything else loads.
4. **Nothing may branch on a plugin id.** Hosts and core libraries never compare against a
   specific plugin's id; capability ids and service ids are the only dispatch keys. A plugin
   asking for another plugin's namespaced service is not a violation: that is dispatch by service
   id, and declaring the dependency is the point.

## Structure

- one directory per java module (the contract and the engine, the source of truth)
  - `annotations/`, `processor/`: the emission annotations and the emitter that renders TypeScript
  - `contract/`: the manifest, the plugin, the context, and the typed keys
  - `engine/`: validation, activation order, the service hub, the ledger and the diagnostics channel
  - `teavm/`: the engine's JavaScript module surface, compiled to an ES2015 module
- `generated/` (emitted from the Java, committed)
  - `api.keys.js` and `api.keys.d.ts`: the package root, types and constants
  - `api.d.ts`: the contract declarations, also served as `./contract`
  - `engine.js` and `engine.d.ts`: the engine, served as `./engine`
- `src/cli/`: the `intisy-plugin` command, the only TypeScript here and the only node importer
- `dist/cli/main.js` (compiled output, not committed)
- `schema/plugin.schema.json`: the manifest schema, emitted from the Java `ManifestSchema`

## Installation

Through the plugin manager, as a dependency of the host or plugin that needs it:

```bash
npm install @intisy-ai/api
```

Inside this repo's own sibling packages, as a file dependency on the submodule checkout:

```json
{ "dependencies": { "api": "file:../api" } }
```

## Configuration

This package reads no configuration file. Its one environment switch is
`INTISY_PLUGIN_STRICT=1`, which makes every ignored unknown id loud instead of quiet:

```bash
INTISY_PLUGIN_STRICT=1 npx intisy-plugin validate
```

A host can route the same diagnostics into its own logger with `setDiagnosticSink`, and force
the mode with `setStrict(true)`.

## Logging

This package writes no log files. Every plugin-facing message goes to the `Logger` the host puts
on the context, and the package's own diagnostics go to the sink a host installs, or to the
console when it installs none.

## License

MIT
