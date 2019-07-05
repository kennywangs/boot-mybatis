package com.xxb.base;

import java.sql.SQLException;

public class OptimisticLockingFailureException extends SQLException {

	private static final long serialVersionUID = 1L;

	public OptimisticLockingFailureException(String msg) {
		super(msg);
	}
	
	public OptimisticLockingFailureException(String msg, Throwable ex) {
		super(msg, ex);
	}
}
