/**
 * Every capability id this API version mints.
 *
 * @remarks
 * Capability ids are bare names owned solely by this package, because a capability is the HOST
 * contract and central ownership is what keeps it one contract. A host that meets an id absent
 * from this list ignores it rather than failing, so a plugin built against a later version
 * still loads.
 */
export const CAPABILITY_IDS = [
  "provider",
  "front-door",
  "screens",
  "settings",
  "commands",
  "plugin-management",
  "cross-app-sync",
  "custom-endpoints",
  "config-history",
  "marketplace-source",
] as const;

/** One of the capability ids this API version mints. */
export type CapabilityId = (typeof CAPABILITY_IDS)[number];

/**
 * Service ids that are bare names rather than namespaced ones, because they name a CONTRACT any
 * plugin may implement rather than one plugin's own offering. Asking for a well-known id is how
 * a consumer stays swappable; asking for a namespaced id is how it demands one implementation.
 */
export const WELL_KNOWN_SERVICES = ["accounts", "routing", "activity"] as const;

/** One of the well-known bare service ids. */
export type WellKnownServiceId = (typeof WELL_KNOWN_SERVICES)[number];

/** Whether an id is a capability this API version knows. */
export function isKnownCapability(id: string): boolean {
  return (CAPABILITY_IDS as readonly string[]).includes(id);
}

/** Whether an id is one of the well-known bare service ids. */
export function isWellKnownService(id: string): boolean {
  return (WELL_KNOWN_SERVICES as readonly string[]).includes(id);
}

/**
 * The plugin id a namespaced service id belongs to, or `null` for a bare id or a malformed one
 * whose name part is empty.
 */
export function serviceOwner(id: string): string | null {
  const separator = id.indexOf(":");
  if (separator <= 0) return null;
  if (separator === id.length - 1) return null;
  return id.slice(0, separator);
}

/**
 * Whether a plugin is allowed to register a service id: its own namespace, or a well-known bare
 * id. Everything else is squatting, and the registry rejects it.
 */
export function mayRegister(pluginId: string, serviceId: string): boolean {
  if (isWellKnownService(serviceId)) return true;
  return serviceOwner(serviceId) === pluginId;
}
