import type { CapabilityType, PluginContext } from "../../generated/api";

interface Screens {
  screens(): string[];
}

declare const SCREENS: CapabilityType<Screens>;

export function activate(context: PluginContext): void {
  context.provide(SCREENS, { totallyWrong: true });
}
