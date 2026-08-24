import type { CapabilityType } from "../../generated/api";

interface Screens {
  screens(): string[];
}

interface SomethingElse {
  different: string;
}

declare const OTHER: CapabilityType<SomethingElse>;

export const mismatched: CapabilityType<Screens> = OTHER;
