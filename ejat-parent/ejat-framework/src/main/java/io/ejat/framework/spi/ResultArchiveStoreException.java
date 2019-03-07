package io.ejat.framework.spi;

public class ResultArchiveStoreException extends FrameworkException {
	private static final long serialVersionUID = 1L;

	public ResultArchiveStoreException() {
	}

	public ResultArchiveStoreException(String message) {
		super(message);
	}

	public ResultArchiveStoreException(Throwable cause) {
		super(cause);
	}

	public ResultArchiveStoreException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResultArchiveStoreException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
