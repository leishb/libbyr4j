package com.github.wks.libbyr4j;

public class ByrException extends RuntimeException {

	private static final long serialVersionUID = -3768937489972361348L;

	public ByrException() {
	}

	public ByrException(String message) {
		super(message);
	}

	public ByrException(Throwable cause) {
		super(cause);
	}

	public ByrException(String message, Throwable cause) {
		super(message, cause);
	}

}
