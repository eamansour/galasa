/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.common;

import static org.assertj.core.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import dev.galasa.framework.mocks.MockRunResult;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.teststructure.TestStructure;

public class RunLogArtifactTest {

    @Test
    public void testGetPathNameReturnsRunLog() {
        // Given...
        RunLogArtifact artifact = new RunLogArtifact();

        // When...
        String pathName = artifact.getPathName();

        // Then...
        assertThat(pathName).isEqualTo("/run.log");
    }

    @Test
    public void testGetContentTypeReturnsTextPlain() {
        // Given...
        RunLogArtifact artifact = new RunLogArtifact();

        // When...
        String contentType = artifact.getContentType();

        // Then...
        assertThat(contentType).isEqualTo("text/plain");
    }

    @Test
    public void testGetContentReturnsLogAsBytes() throws Exception {
        // Given...
        String logContent = "Test log line 1\nTest log line 2\nTest log line 3";
        IRunResult mockRun = new MockRunResult("test-run-id", new TestStructure(), null, logContent);
        RunLogArtifact artifact = new RunLogArtifact();

        // When...
        byte[] content = artifact.getContent(mockRun);

        // Then...
        String result = new String(content, StandardCharsets.UTF_8);
        assertThat(result).isEqualTo(logContent);
    }

    @Test
    public void testGetContentWithNullLogReturnsEmptyBytes() throws Exception {
        // Given...
        IRunResult mockRun = new MockRunResult("test-run-id", new TestStructure(), null, null);
        RunLogArtifact artifact = new RunLogArtifact();

        // When...
        byte[] content = artifact.getContent(mockRun);

        // Then...
        String result = new String(content, StandardCharsets.UTF_8);
        assertThat(result).isEmpty();
    }

    @Test
    public void testStreamContentReturnsTrue() throws Exception {
        // Given...
        String logContent = "Test log content";
        IRunResult mockRun = new MockRunResult("test-run-id", new TestStructure(), null, logContent);
        RunLogArtifact artifact = new RunLogArtifact();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // When...
        boolean streamed = artifact.streamContent(mockRun, outputStream);

        // Then...
        assertThat(streamed).isTrue();
    }

    @Test
    public void testStreamContentWritesLogToOutputStream() throws Exception {
        // Given...
        String logContent = "Test log line 1\nTest log line 2\nTest log line 3";
        IRunResult mockRun = new MockRunResult("test-run-id", new TestStructure(), null, logContent);
        RunLogArtifact artifact = new RunLogArtifact();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // When...
        artifact.streamContent(mockRun, outputStream);

        // Then...
        String result = outputStream.toString(StandardCharsets.UTF_8);
        assertThat(result).isEqualTo(logContent);
    }

    @Test
    public void testStreamContentWithEmptyLogWritesEmptyStream() throws Exception {
        // Given...
        IRunResult mockRun = new MockRunResult("test-run-id", new TestStructure(), null, "");
        RunLogArtifact artifact = new RunLogArtifact();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // When...
        artifact.streamContent(mockRun, outputStream);

        // Then...
        String result = outputStream.toString(StandardCharsets.UTF_8);
        assertThat(result).isEmpty();
    }

    @Test
    public void testStreamContentWithNullLogWritesEmptyStream() throws Exception {
        // Given...
        IRunResult mockRun = new MockRunResult("test-run-id", new TestStructure(), null, null);
        RunLogArtifact artifact = new RunLogArtifact();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // When...
        artifact.streamContent(mockRun, outputStream);

        // Then...
        String result = outputStream.toString(StandardCharsets.UTF_8);
        assertThat(result).isEmpty();
    }

    @Test
    public void testStreamContentHandlesSpecialCharacters() throws Exception {
        // Given...
        String logContent = "Special chars: !@#$%^&*()\nUnicode: 你好世界\nEmoji: 🚀🎉";
        IRunResult mockRun = new MockRunResult("test-run-id", new TestStructure(), null, logContent);
        RunLogArtifact artifact = new RunLogArtifact();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // When...
        artifact.streamContent(mockRun, outputStream);

        // Then...
        String result = outputStream.toString(StandardCharsets.UTF_8);
        assertThat(result).isEqualTo(logContent);
        assertThat(result).contains("!@#$%^&*()");
        assertThat(result).contains("你好世界");
        assertThat(result).contains("🚀🎉");
    }

    @Test
    public void testStreamContentPropagatesResultArchiveStoreException() throws Exception {
        // Given...
        IRunResult mockRun = new IRunResult() {
            @Override
            public String getRunId() { return "test-run-id"; }
            
            @Override
            public TestStructure getTestStructure() { return new TestStructure(); }
            
            @Override
            public java.nio.file.Path getArtifactsRoot() { return null; }
            
            @Override
            public String getLog() throws ResultArchiveStoreException {
                throw new ResultArchiveStoreException("Test exception");
            }
            
            @Override
            public void streamLog(OutputStream outputStream) throws ResultArchiveStoreException {
                throw new ResultArchiveStoreException("Test streaming exception");
            }
            
            @Override
            public void discard() {}
            
            @Override
            public void loadArtifacts() {}
            
            @Override
            public void loadArtifact(String artifactPath) {}
        };
        
        RunLogArtifact artifact = new RunLogArtifact();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // When...
        Throwable thrown = catchThrowable(() -> {
            artifact.streamContent(mockRun, outputStream);
        });

        // Then...
        assertThat(thrown)
            .isInstanceOf(ResultArchiveStoreException.class)
            .hasMessageContaining("Test streaming exception");
    }

}
