/**
 * The subset of JSON Schema draft-07 this package uses and validates against.
 *
 * @remarks
 * `additionalProperties: false` is deliberately absent from this type: an object property with
 * no declared schema is ignored rather than rejected, which is how the open-vocabulary rule
 * reaches the manifest. A record type declares `additionalProperties` as a schema, and every
 * value in it is then checked against it.
 */
export interface JsonSchema {
  /** Draft identifier, emitted into the published schema file and otherwise unused. */
  $schema?: string;
  /** Canonical URL of the schema, emitted into the published file and otherwise unused. */
  $id?: string;
  /** Human title, for editors. */
  title?: string;
  /** Human description, for editors and generated documentation. */
  description?: string;
  /** The one type a value may have here. */
  type?: "object" | "array" | "string" | "integer" | "number" | "boolean";
  /** Schemas for known properties of an object. */
  properties?: Record<string, JsonSchema>;
  /** Property names an object must carry. */
  required?: string[];
  /** Schema every element of an array is checked against. */
  items?: JsonSchema;
  /** Schema every otherwise-unknown property is checked against, which is how a record is declared. */
  additionalProperties?: JsonSchema;
  /** Regular expression a string must match. */
  pattern?: string;
  /** Lowest value a number may take. */
  minimum?: number;
  /** The complete set of values a string may take. */
  enum?: string[];
  /**
   * What the author should do when this part of the value is wrong.
   *
   * @remarks
   * Not a JSON Schema keyword. Validators ignore unknown keywords, so carrying the fix next to
   * the rule keeps "errors that teach" a property of the data rather than of the reporting code.
   */
  fix?: string;
}

/** One thing wrong with a value, located, explained, and paired with its remedy. */
export interface SchemaIssue {
  /** Dotted path to the offending value, for example `services.provides[0]`, or `(root)`. */
  path: string;
  /** What is wrong, naming the value that was actually found. */
  message: string;
  /** What to do about it. */
  fix: string;
}

/**
 * Checks a value against a {@link JsonSchema}, collecting every problem rather than stopping at
 * the first.
 *
 * @param value - the parsed value to check
 * @param schema - the schema to check it against
 * @param path - the path prefix issues are reported under, `"(root)"` by default
 * @returns every issue found, empty when the value is valid
 */
export function validateAgainstSchema(value: unknown, schema: JsonSchema, path = "(root)"): SchemaIssue[] {
  const issues: SchemaIssue[] = [];
  check(value, schema, path, issues);
  return issues;
}

function check(value: unknown, schema: JsonSchema, path: string, issues: SchemaIssue[]): void {
  if (schema.type === "object") checkObject(value, schema, path, issues);
  else if (schema.type === "array") checkArray(value, schema, path, issues);
  else if (schema.type === "string") checkString(value, schema, path, issues);
  else if (schema.type === "integer" || schema.type === "number") checkNumber(value, schema, path, issues);
  else if (schema.type === "boolean" && typeof value !== "boolean") issues.push(typeIssue(value, schema, path));
}

function checkObject(value: unknown, schema: JsonSchema, path: string, issues: SchemaIssue[]): void {
  if (typeof value !== "object" || value === null || Array.isArray(value)) {
    issues.push(typeIssue(value, schema, path));
    return;
  }
  const record = value as Record<string, unknown>;
  for (const key of schema.required ?? []) {
    if (record[key] !== undefined) continue;
    const child = schema.properties?.[key];
    issues.push({
      path: childPath(path, key),
      message: `required field "${key}" is missing`,
      fix: child?.fix ?? `add "${key}" to ${path}`,
    });
  }
  for (const [key, child] of Object.entries(record)) {
    if (child === undefined) continue;
    const childSchema = schema.properties?.[key] ?? schema.additionalProperties;
    if (!childSchema) continue;
    check(child, childSchema, childPath(path, key), issues);
  }
}

function checkArray(value: unknown, schema: JsonSchema, path: string, issues: SchemaIssue[]): void {
  if (!Array.isArray(value)) {
    issues.push(typeIssue(value, schema, path));
    return;
  }
  if (!schema.items) return;
  value.forEach((item, index) => check(item, schema.items as JsonSchema, `${path}[${index}]`, issues));
}

function checkString(value: unknown, schema: JsonSchema, path: string, issues: SchemaIssue[]): void {
  if (typeof value !== "string") {
    issues.push(typeIssue(value, schema, path));
    return;
  }
  if (schema.pattern && !new RegExp(schema.pattern).test(value)) {
    issues.push({ path, message: `"${value}" does not match ${schema.pattern}`, fix: schema.fix ?? `make ${path} match ${schema.pattern}` });
  }
  if (schema.enum && !schema.enum.includes(value)) {
    issues.push({ path, message: `"${value}" is not one of ${schema.enum.join(", ")}`, fix: schema.fix ?? `set ${path} to one of ${schema.enum.join(", ")}` });
  }
}

function checkNumber(value: unknown, schema: JsonSchema, path: string, issues: SchemaIssue[]): void {
  if (typeof value !== "number" || Number.isNaN(value) || (schema.type === "integer" && !Number.isInteger(value))) {
    issues.push(typeIssue(value, schema, path));
    return;
  }
  if (schema.minimum !== undefined && value < schema.minimum) {
    issues.push({ path, message: `expected a value >= ${schema.minimum}, got ${value}`, fix: schema.fix ?? `set ${path} to a value >= ${schema.minimum}` });
  }
}

function typeIssue(value: unknown, schema: JsonSchema, path: string): SchemaIssue {
  return { path, message: `expected ${schema.type}, got ${describe(value)}`, fix: schema.fix ?? `set ${path} to a ${schema.type}` };
}

function describe(value: unknown): string {
  if (value === null) return "null";
  if (Array.isArray(value)) return "array";
  return typeof value;
}

function childPath(path: string, key: string): string {
  return path === "(root)" ? key : `${path}.${key}`;
}
