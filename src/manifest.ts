/**
 * The API major version this package implements.
 *
 * @remarks
 * A manifest's `api` field is a FLOOR, not a build tag: a host refuses to load only a plugin
 * whose declared floor exceeds the version the host implements, and loads everything else.
 * The number rises only when a host gains abilities a plugin can require; additions to this
 * package alone never change it.
 */
export const API_VERSION = 1;
