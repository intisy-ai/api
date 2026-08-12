export { API_VERSION } from "./manifest.js";
export { PluginError, isPluginError } from "./errors.js";
export { ignoreUnknown, isStrict, setDiagnosticSink, setStrict, STRICT_ENV } from "./strict.js";
export type { DiagnosticSink } from "./strict.js";
export { validateAgainstSchema } from "./schema.js";
export type { JsonSchema, SchemaIssue } from "./schema.js";
export { MANIFEST_SCHEMA, SCHEMA_ID } from "./manifest-schema.js";
