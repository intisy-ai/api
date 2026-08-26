package io.github.intisy.ai.engine;

/** One thing wrong with a value, located, explained, and paired with its remedy. */
public final class SchemaIssue {

    private final String path;
    private final String message;
    private final String fix;

    /**
     * @param path dotted path to the offending value, for example services.provides[0], or (root)
     * @param message what is wrong, naming the value that was actually found
     * @param fix what to do about it
     */
    public SchemaIssue(String path, String message, String fix) {
        this.path = path;
        this.message = message;
        this.fix = fix;
    }

    /**
     * Dotted path to the offending value, for example services.provides[0], or (root).
     *
     * @return the dotted path
     */
    public String getPath() {
        return path;
    }

    /**
     * What is wrong, naming the value that was actually found.
     *
     * @return the issue's message
     */
    public String getMessage() {
        return message;
    }

    /**
     * What to do about it.
     *
     * @return the fix instruction
     */
    public String getFix() {
        return fix;
    }

    @Override
    public String toString() {
        return path + ": " + message + " (fix: " + fix + ")";
    }
}
