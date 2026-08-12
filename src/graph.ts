import type { PluginManifest } from "./manifest.js";

/** The order plugins activate in, and the dependency cycles that kept some of them out of it. */
export interface ActivationPlan {
  /** Plugin ids in activation order, providers before consumers. */
  order: string[];
  /** Each set of plugins that depend on one another, which is a load error naming the cycle. */
  cycles: string[][];
}

/**
 * Sorts plugins so a provider activates before its consumers.
 *
 * @remarks
 * A consumed service nobody provides draws no edge: the consumer activates anyway and its `want`
 * resolves later or times out, because at ecosystem scale providers appear and disappear at
 * runtime and a hard order fails the moment one is disabled. A cycle is reported instead of
 * hanging, and its members are left out of the order for the caller to quarantine.
 */
export function activationOrder(manifests: PluginManifest[]): ActivationPlan {
  const ids = manifests.map((manifest) => manifest.id);
  const providers = new Map<string, string[]>();
  for (const manifest of manifests) {
    for (const serviceId of manifest.services?.provides ?? []) {
      const list = providers.get(serviceId) ?? [];
      list.push(manifest.id);
      providers.set(serviceId, list);
    }
  }

  const dependencies = new Map<string, Set<string>>();
  for (const manifest of manifests) {
    const set = new Set<string>();
    for (const serviceId of manifest.services?.consumes ?? []) {
      for (const provider of providers.get(serviceId) ?? []) if (provider !== manifest.id) set.add(provider);
    }
    dependencies.set(manifest.id, set);
  }

  const cycles = findCycles(ids, dependencies);
  const blocked = new Set(cycles.flat());
  const order: string[] = [];
  const settled = new Set<string>(blocked);

  let progressed = true;
  while (progressed) {
    progressed = false;
    for (const id of ids) {
      if (settled.has(id)) continue;
      const pending = [...(dependencies.get(id) ?? [])].filter((dependency) => !settled.has(dependency));
      if (pending.length) continue;
      order.push(id);
      settled.add(id);
      progressed = true;
    }
  }

  return { order, cycles };
}

function findCycles(ids: string[], dependencies: Map<string, Set<string>>): string[][] {
  const index = new Map<string, number>();
  const lowLink = new Map<string, number>();
  const onStack = new Set<string>();
  const stack: string[] = [];
  const cycles: string[][] = [];
  let counter = 0;

  function visit(id: string): void {
    index.set(id, counter);
    lowLink.set(id, counter);
    counter++;
    stack.push(id);
    onStack.add(id);

    for (const dependency of dependencies.get(id) ?? []) {
      if (!index.has(dependency)) {
        visit(dependency);
        lowLink.set(id, Math.min(lowLink.get(id)!, lowLink.get(dependency)!));
      } else if (onStack.has(dependency)) {
        lowLink.set(id, Math.min(lowLink.get(id)!, index.get(dependency)!));
      }
    }

    if (lowLink.get(id) !== index.get(id)) return;
    const component: string[] = [];
    let member: string;
    do {
      member = stack.pop()!;
      onStack.delete(member);
      component.push(member);
    } while (member !== id);
    if (component.length > 1) cycles.push(component.reverse());
  }

  for (const id of ids) if (!index.has(id)) visit(id);
  return cycles;
}
