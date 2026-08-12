---
title: Permanence
---

# Why this API never needs a redesign

Four mechanisms, all of them hard rules rather than guidance. They are why the ecosystem can add
plugins, hosts, capabilities, and surfaces indefinitely without another plugin-system rewrite.

## 1. Append-only

A published capability interface, service contract, manifest field, or event shape is never
removed and never changes meaning. New needs get new ids (`screens.v2`), never edits to old ones.
Deprecation is a documentation state, not a code change. The package version signals additions.

Practical consequence: think before adding a field, because it is forever. A field nothing
renders yet should not ship; adding it later is legal and cheap.

## 2. Open vocabularies

Unknown capability id, unknown service id, unknown event topic, unknown manifest field, unknown
screen-node kind: always ignored with a debug log, never an error. A plugin built against next
year's API loads on today's host, minus what the host does not know.

## 3. The `api` floor

`"api": 1` in a manifest is the LOWEST version the plugin needs. A host refuses only what exceeds
what it implements, and says so precisely: "needs api 3, this host has api 2, update the app".

## 4. No branching on a plugin id

Hosts and core libraries never compare against a specific plugin's id. Capability ids and service
ids are the only dispatch keys, which is grep-enforceable in CI. A PLUGIN asking for another
plugin's namespaced service is not a violation: that is dispatch by service id, and declaring the
dependency is the point. The rule binds HOSTS, which must stay ignorant of every specific plugin.

## What this does not buy

Hard security isolation of untrusted plugins. Permissions are declarative: they are an
install-time consent surface, not a sandbox. That limit is accepted knowingly, and the `ctx` seam
is what would let a future host run a plugin out of process without changing plugin code.
