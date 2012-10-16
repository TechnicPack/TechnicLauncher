package org.spoutcraft.launcher.exception;

public class MinecraftVerifyException extends Exception {
	private static final long	serialVersionUID	= 1L;

	public MinecraftVerifyException(String message) {
		super(message);
	}

	public MinecraftVerifyException(Throwable throwable, String message) {
		super(message, throwable);
	}

	public MinecraftVerifyException(Throwable throwable) {
		super(throwable);
	}

}