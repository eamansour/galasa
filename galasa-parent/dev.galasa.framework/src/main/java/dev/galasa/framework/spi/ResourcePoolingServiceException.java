/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

public class ResourcePoolingServiceException extends FrameworkException {
    private static final long serialVersionUID = 1L;

    public ResourcePoolingServiceException() {
    }

    public ResourcePoolingServiceException(String message) {
        super(message);
    }

    public ResourcePoolingServiceException(Throwable cause) {
        super(cause);
    }

    public ResourcePoolingServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourcePoolingServiceException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
