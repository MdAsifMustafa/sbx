package io.github.mdasifmustafa.sbx.error;

public class UserAbortException extends SbxException {
	
	private static final long serialVersionUID = 1L;

	public UserAbortException(String message) {
        super(message, 2);
    }
}