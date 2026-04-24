/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.teststructure.TestStructure;

public class MockRunResult implements IRunResult {

    private String runId ;
    private TestStructure testStructure;
    private Path artifactRoot;
    private String log;
    private boolean isDiscarded = false;
    private boolean isLoadingArtifactsEnabled = false;

    public MockRunResult(
        String runId,
        TestStructure testStructure,
        Path artifactRoot,
        String log
    ) {
        this.runId = runId ;
        this.testStructure = testStructure ;
        this.artifactRoot = artifactRoot;
        this.log = log;
    }

    @Override
    public String getRunId() {
        return this.runId;
    }

    @Override
    public TestStructure getTestStructure() throws ResultArchiveStoreException {
        return this.testStructure;
    }

    @Override
    public Path getArtifactsRoot() throws ResultArchiveStoreException {
        return this.artifactRoot;
    }

    @Override
    public String getLog() throws ResultArchiveStoreException {
        return this.log;
    }

    @Override
    public void streamLog(OutputStream outputStream) throws ResultArchiveStoreException {
        try {
            if (this.log != null) {
                outputStream.write(this.log.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            }
        } catch (IOException e) {
            throw new ResultArchiveStoreException("Error streaming log content", e);
        }
    }

    @Override
    public long getLogSize() throws ResultArchiveStoreException {
        long size = 0;
        
        // First try to get size from TestStructure metadata
        if (testStructure != null) {
            Long logSize = testStructure.getLogSize();
            if (logSize != null) {
                size = logSize.longValue();
            } else if (this.log != null) {
                // Fall back to calculating size from log string
                size = this.log.getBytes(StandardCharsets.UTF_8).length;
            }
        } else if (this.log != null) {
            // No test structure, calculate from log string
            size = this.log.getBytes(StandardCharsets.UTF_8).length;
        }
        
        return size;
    }

    @Override
    public void discard() throws ResultArchiveStoreException {
        isDiscarded = true;
    }

    @Override
    public void loadArtifacts() throws ResultArchiveStoreException {
        isLoadingArtifactsEnabled = true;
    }

    @Override
    public void loadArtifact(String artifactPath) throws ResultArchiveStoreException {
        isLoadingArtifactsEnabled = true;
    }

    public boolean isDiscarded() {
        return this.isDiscarded;
    }

    public boolean isLoadingArtifactsEnabled() {
        return isLoadingArtifactsEnabled;
    }
}