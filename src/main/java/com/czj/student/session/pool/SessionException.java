package com.czj.student.session.pool;

/**
 * 会话池异常
 */
public class SessionException extends RuntimeException {
    
    public SessionException(String message) {
        super(message);
    }
    
    public SessionException(String message, Throwable cause) {
        super(message, cause);
    }
} 