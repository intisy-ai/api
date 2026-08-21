package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;

/** One thing wrong with a manifest, located, explained, and paired with its remedy. */
@TsInterface(data = true)
public interface ValidationIssueShape {
    /** The manifest field the issue is about. */
    String path();

    /** What is wrong with it. */
    String message();

    /** How to put it right. */
    String fix();
}
