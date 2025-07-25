/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.mocks;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.RunRasAction;

public class MockIFrameworkRuns implements IFrameworkRuns{
    protected String groupName;
    private String mockSubmissionId = "submission1";

    List<IRun> runs ;


    public MockIFrameworkRuns(@NotNull String groupName, List<IRun> runs) {
        this.groupName = groupName;
        this.runs = runs;
    }

    public MockIFrameworkRuns(List<IRun> runs) {
        this.runs = runs;
    }

    @Override
    public @NotNull List<IRun> getActiveRuns() throws FrameworkException {
        throw new UnsupportedOperationException("Unimplemented method 'getActiveRuns'");
    }

    @Override
    public @NotNull List<IRun> getQueuedRuns() throws FrameworkException {
        throw new UnsupportedOperationException("Unimplemented method 'getQueuedRuns'");
    }

    @Override
    public @NotNull List<IRun> getAllRuns() throws FrameworkException {
        throw new UnsupportedOperationException("Unimplemented method 'getAllRuns'");
    }

    @Override
    public @NotNull List<IRun> getAllGroupedRuns(@NotNull String groupName) throws FrameworkException {
        if(groupName.equals("invalid")){
            throw new FrameworkException("exceptioninvalid group");
        }else if (groupName.equals("nullgroup")){
            return null;
        }
       return this.runs;
    }

    @Override
    public @NotNull Set<String> getActiveRunNames() throws FrameworkException {
        throw new UnsupportedOperationException("Unimplemented method 'getActiveRunNames'");
    }

    @Override
    public @NotNull IRun submitRun(String type, String requestor, String bundleName, String testName, String groupName,
            String mavenRepository, String obr, String stream, boolean local, boolean trace, Set<String> tags, Properties overrides,
            SharedEnvironmentPhase sharedEnvironmentPhase, String sharedEnvironmentRunName, String language, String submissionId)
            throws FrameworkException {
            if (stream.equals("null")){
                throw new FrameworkException(language);
            }
        return new MockIRun("runname", type, requestor, bundleName + "/" + testName, null, bundleName, testName, groupName, this.mockSubmissionId, tags);
    }

    @Override
    public boolean delete(String runname) throws DynamicStatusStoreException {
        return true;
    }

    @Override
    public IRun getRun(String runname) throws DynamicStatusStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'getRun'");
    }

    @Override
    public boolean reset(String runname) throws DynamicStatusStoreException {
        return true;
    }

    public void setMockSubmissionId(String mockSubmissionId) {
        this.mockSubmissionId = mockSubmissionId;
    }

    @Override
    public boolean markRunInterrupted(String runName, String interruptReason) throws DynamicStatusStoreException {
        return true;
    }

    @Override
    public void markRunFinished(String runName, String result) throws DynamicStatusStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'setRunStatus'");
    }

    @Override
    public void addRunRasAction(IRun run, RunRasAction rasActionToAdd) throws DynamicStatusStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'addRunRasAction'");
    }
}