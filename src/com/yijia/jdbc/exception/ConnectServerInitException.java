package com.yijia.jdbc.exception;

/**
 * ConnectServer初始化异常
 * 
 * @author zhqy
 *
 */
public class ConnectServerInitException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ConnectServerInitException() {
        super();
    }

    public ConnectServerInitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ConnectServerInitException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectServerInitException(String message) {
        super(message);
    }

    public ConnectServerInitException(Throwable cause) {
        super(cause);
    }
    
    

}
