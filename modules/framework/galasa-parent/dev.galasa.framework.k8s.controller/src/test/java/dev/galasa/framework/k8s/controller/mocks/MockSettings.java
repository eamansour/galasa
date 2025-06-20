/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller.mocks;

import dev.galasa.framework.k8s.controller.K8sController;
import dev.galasa.framework.k8s.controller.K8sControllerException;
import dev.galasa.framework.k8s.controller.Settings;
import dev.galasa.framework.k8s.controller.api.KubernetesEngineFacade;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;

public class MockSettings extends Settings {

    private V1ConfigMap mockConfigMap;

    public MockSettings(V1ConfigMap configMap, K8sController controller, KubernetesEngineFacade kube, String podName , String engineName) throws K8sControllerException {
        super(controller, kube, podName , engineName);
        this.mockConfigMap = configMap;
    }

}