package org.spoutcraft.launcher.exception;

import java.io.IOException;

public class AccountMigratedException extends IOException {
	private static final long	serialVersionUID	= 1L;
	private final Throwable		cause;
	private final String			message;

	public AccountMigratedException(String message) {
		this(null, message);
	}

	public AccountMigratedException(Throwable throwable, String message) {
		this.cause = throwable;
		this.message = message;
	}

	public AccountMigratedException() {
		this(null, "Account migrated, use e-mail as username.");
	}

	@Override
	public Throwable getCause() {
		return this.cause;
	}

	@Override
	public String getMessage() {
		return this.message;
	}
}
