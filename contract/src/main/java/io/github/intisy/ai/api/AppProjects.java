package io.github.intisy.ai.api;

import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsOptional;
import java.util.List;

/**
 * Where an app records the projects a user has worked in.
 *
 * @implNote Absent on the descriptor means no project history, rather than a consumer guessing at a
 * location.
 */
@TsInterface(data = true)
public interface AppProjects {
    /**
     * A history file inside the app home.
     *
     * @return the history file path, or absent when this app keeps none
     */
    @TsOptional
    String historyFile();

    /**
     * Session databases to try in order, absolute or relative to the app home.
     *
     * @return the candidate database paths, or absent when this app keeps none
     */
    @TsOptional
    List<String> sessionDb();

    /**
     * The file the app writes inside a project's git directory to record the project id.
     *
     * @return the marker file name, or absent when this app writes none
     */
    @TsOptional
    String markerFile();
}
