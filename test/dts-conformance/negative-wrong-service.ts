import type { PluginContext, ServiceType } from "../../generated/api";

interface Store {
  open(): void;
}

const STORE = { id: "other-plugin:store" } as ServiceType<Store>;

export function activate(context: PluginContext): void {
  context.services.register(STORE, { close: () => {} });
}
