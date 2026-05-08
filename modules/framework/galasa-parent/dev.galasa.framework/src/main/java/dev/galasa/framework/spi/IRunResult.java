/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import java.io.OutputStream;
import java.nio.file.Path;

import dev.galasa.framework.spi.teststructure.TestStructure;

public interface IRunResult {
    
    String getRunId();

    TestStructure getTestStructure() throws ResultArchiveStoreException;

    Path getArtifactsRoot() throws ResultArchiveStoreException;

    String getLog() throws ResultArchiveStoreException;

    /**
     * Stream the run log content directly to an OutputStream.
     * This method is preferred for large logs to avoid memory issues.
     *
     * @param outputStream The stream to write log content to
     * @throws ResultArchiveStoreException if there's an error accessing the log
     */
    void streamLog(OutputStream outputStream) throws ResultArchiveStoreException;

    /**
     * Get the size of the run log in bytes without loading it into memory.
     * This is useful for setting Content-Length headers without reading the entire log.
     *
     * @return The log size in bytes, or -1 if size is unknown or cannot be determined efficiently
     * @throws ResultArchiveStoreException if there's an error accessing the log metadata
     */
    default long getLogSize() throws ResultArchiveStoreException {
        // Default: size unknown
        return -1;
    }

    void discard() throws ResultArchiveStoreException;

    void loadArtifacts() throws ResultArchiveStoreException;

    void loadArtifact(String artifactPath) throws ResultArchiveStoreException;

}
