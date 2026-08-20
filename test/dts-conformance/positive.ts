import type { CapabilityType, Plugin, PluginContext } from "../../generated/api";

interface Screens {
  screens(): string[];
}

const SCREENS = { id: "screens" } as CapabilityType<Screens>;

export const plugin: Plugin = {
  activate(context: PluginContext) {
    context.provide(SCREENS, { screens: () => ["overview"] });
    context.log.info(`api ${context.host.api}`);
    const token = context.config.get<string>("token");
    if (token !== undefined) context.log.warn(token);
  },
  deactivate() {},
};
