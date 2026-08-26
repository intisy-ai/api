package io.github.intisy.ai.js.surface;

import io.github.intisy.ai.tsemit.TsInterface;

/** One thing wrong with a manifest, located, explained, and paired with its remedy. */
@TsInterface(data = true)
public interface ValidationIssueShape {
    /**
     * The manifest field the issue is about.
     *
     * @return the field path
     */
    String path();

    /**
     * What is wrong with it.
     *
     * @return the problem description
     */
    String message();

    /**
     * How to put it right.
     *
     * @return the fix instructions
     */
    String fix();
}
