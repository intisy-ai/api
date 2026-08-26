package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;

/**
 * The plugin that connects an app to the local API.
 *
 * @implNote Data, not code: a host reads this to install and track the app's loader, so an app whose
 * loader is renamed or rehosted needs no consumer change.
 */
@TsInterface(data = true)
public interface AppLoader {
    /** The loader plugin's id. */
    String id();

    /** Where the loader is cloned from, as {@code owner/repo} or a full URL. */
    String url();
}
