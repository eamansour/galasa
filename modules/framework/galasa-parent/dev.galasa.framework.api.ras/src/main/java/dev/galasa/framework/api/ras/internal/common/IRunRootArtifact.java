/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.common;

import java.io.IOException;
import java.io.OutputStream;

import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;

public interface IRunRootArtifact {
    String getPathName();
    
    /**
     * Get content as byte array. Use for small artifacts only.
     * For large artifacts, use streamContent() instead.
     *
     * @param run The run result to get content from
     * @return The artifact content as a byte array
     * @throws ResultArchiveStoreException if there's an error accessing the artifact
     * @throws IOException if there's an error reading the artifact
     */
    byte[] getContent(IRunResult run) throws ResultArchiveStoreException, IOException;
    
    /**
     * Stream content directly to output stream. Preferred for large artifacts.
     *
     * @param run The run result to get content from
     * @param outputStream The stream to write content to
     * @return true if streaming is supported and was used, false if getContent() should be used instead
     * @throws ResultArchiveStoreException if there's an error accessing the artifact
     */
    default boolean streamContent(IRunResult run, OutputStream outputStream)
        throws ResultArchiveStoreException {
        // Default: streaming not supported
        return false;
    }
    
    /**
     * Get the size of the artifact content in bytes without loading it into memory.
     * This is useful for setting Content-Length headers without reading the entire artifact.
     *
     * @param run The run result to get content size from
     * @return The size in bytes, or -1 if size is unknown or cannot be determined efficiently
     * @throws ResultArchiveStoreException if there's an error accessing the artifact metadata
     */
    default long getContentSize(IRunResult run) throws ResultArchiveStoreException {
        // Default: size unknown
        return -1;
    }
    
    String getContentType();
}
