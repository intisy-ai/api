package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;

/** One thing wrong with a manifest, located, explained, and paired with its remedy. */
@TsInterface(data = true)
public interface ValidationIssueShape {
    String path();

    String message();

    String fix();
}
