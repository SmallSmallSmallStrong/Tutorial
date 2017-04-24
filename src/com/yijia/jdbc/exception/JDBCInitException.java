package com.yijia.jdbc.exception;

/**
 * 数据库链接初始化异常
 * 
 * @author zhqy
 *
 */
public class JDBCInitException extends Exception {

    private static final long serialVersionUID = 1L;

    public JDBCInitException() {
        super();
    }

    public JDBCInitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public JDBCInitException(String message, Throwable cause) {
        super(message, cause);
    }

    public JDBCInitException(String message) {
        super(message);
    }

    public JDBCInitException(Throwable cause) {
        super(cause);
    }
    
    

}
