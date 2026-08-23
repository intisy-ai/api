import type { PluginContext, TopicType } from "../../generated/api";

const INSTALLED = { id: "plugin.installed" } as TopicType<{ id: string }>;

export function activate(context: PluginContext): void {
  context.events.publish(INSTALLED, { totallyWrong: true });
}
