/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.ras.directory;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.Test;

import dev.galasa.framework.mocks.MockFileSystem;
import dev.galasa.framework.spi.teststructure.TestStructure;
import dev.galasa.framework.spi.utils.GalasaGson;

public class DirectoryRASRunResultTest {

    private MockFileSystem createMockFileSystemWithStructure(TestStructure testStructure) throws IOException {
        MockFileSystem mockFileSystem = new MockFileSystem();
        Path runDir = mockFileSystem.getPath("/test-run");
        mockFileSystem.createDirectories(runDir);
        
        // Create structure.json file
        Path structureFile = runDir.resolve("structure.json");
        GalasaGson gson = new GalasaGson();
        String structureJson = gson.toJson(testStructure);
        mockFileSystem.write(structureFile, structureJson.getBytes(StandardCharsets.UTF_8));
        
        return mockFileSystem;
    }

    @Test
    public void testGetLogSizeReturnsMetadataSize() throws Exception {
        // Given...
        TestStructure testStructure = new TestStructure();
        testStructure.setLogSize(Long.valueOf(54321L));
        
        MockFileSystem mockFileSystem = createMockFileSystemWithStructure(testStructure);
        DirectoryRASFileSystemProvider fileSystemProvider = null;
        Path runDir = mockFileSystem.getPath("/test-run");
        
        GalasaGson gson = new GalasaGson();
        DirectoryRASRunResult runResult = new DirectoryRASRunResult(runDir, gson, "test-run-id", mockFileSystem, fileSystemProvider);

        // When...
        long logSize = runResult.getLogSize();

        // Then...
        assertThat(logSize).isEqualTo(54321L);
    }

    @Test
    public void testGetLogSizeFallsBackToFileSizeWhenNoMetadata() throws Exception {
        // Given...
        TestStructure testStructure = new TestStructure();
        // No log size metadata set
        
        MockFileSystem mockFileSystem = createMockFileSystemWithStructure(testStructure);
        DirectoryRASFileSystemProvider fileSystemProvider = null;
        Path runDir = mockFileSystem.getPath("/test-run");
        
        // Create a run.log file with known content
        Path runLogPath = runDir.resolve("run.log");
        String logContent = "Test log line 1\nTest log line 2\nTest log line 3\n";
        mockFileSystem.write(runLogPath, logContent.getBytes(StandardCharsets.UTF_8));
        long expectedSize = logContent.getBytes(StandardCharsets.UTF_8).length;

        GalasaGson gson = new GalasaGson();
        DirectoryRASRunResult runResult = new DirectoryRASRunResult(runDir, gson, "test-run-id", mockFileSystem, fileSystemProvider);

        // When...
        long logSize = runResult.getLogSize();

        // Then...
        assertThat(logSize).isEqualTo(expectedSize);
    }

    @Test
    public void testGetLogSizeReturnsZeroWhenNoMetadataAndNoFile() throws Exception {
        // Given...
        TestStructure testStructure = new TestStructure();
        // No log size metadata set and no run.log file exists
        
        MockFileSystem mockFileSystem = createMockFileSystemWithStructure(testStructure);
        DirectoryRASFileSystemProvider fileSystemProvider = null;
        Path runDir = mockFileSystem.getPath("/test-run");

        GalasaGson gson = new GalasaGson();
        DirectoryRASRunResult runResult = new DirectoryRASRunResult(runDir, gson, "test-run-id", mockFileSystem, fileSystemProvider);

        // When...
        long logSize = runResult.getLogSize();

        // Then...
        assertThat(logSize).isEqualTo(0L);
    }

    @Test
    public void testGetLogSizeReturnsZeroForEmptyLog() throws Exception {
        // Given...
        TestStructure testStructure = new TestStructure();
        testStructure.setLogSize(Long.valueOf(0L));
        
        MockFileSystem mockFileSystem = createMockFileSystemWithStructure(testStructure);
        DirectoryRASFileSystemProvider fileSystemProvider = null;
        Path runDir = mockFileSystem.getPath("/test-run");

        GalasaGson gson = new GalasaGson();
        DirectoryRASRunResult runResult = new DirectoryRASRunResult(runDir, gson, "test-run-id", mockFileSystem, fileSystemProvider);

        // When...
        long logSize = runResult.getLogSize();

        // Then...
        assertThat(logSize).isEqualTo(0L);
    }
}
