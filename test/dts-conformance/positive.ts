import type { CapabilityType, Plugin, PluginContext, ServiceType, TopicType } from "../../generated/api";

interface Screens {
  screens(): string[];
}

interface Store {
  open(): void;
}

const SCREENS = { id: "screens" } as CapabilityType<Screens>;
const STORE = { id: "other-plugin:store" } as ServiceType<Store>;
const INSTALLED = { id: "plugin.installed" } as TopicType<{ id: string }>;

export const plugin: Plugin = {
  async activate(context: PluginContext) {
    context.provide(SCREENS, { screens: () => ["overview"] });
    context.log.info(`api ${context.host.api}`);
    const token = context.config.get<string>("token");
    if (token !== undefined) context.log.warn(token);

    // A service reached by its typed handle, never by importing the plugin behind it.
    const present = context.services.get(STORE);
    if (present !== undefined) present.open();
    context.services.register(STORE, { open: () => {} });
    const awaited = await context.services.want(STORE, { timeoutMs: 500 });
    awaited.open();
    context.services.watch(STORE, (service, event) => context.log.info(`${event} ${typeof service}`));

    context.events.publish(INSTALLED, { id: "widget" });
    const stop = context.events.subscribe(INSTALLED, (payload) => context.log.info(payload.id));
    stop();
  },
  deactivate() {},
};
