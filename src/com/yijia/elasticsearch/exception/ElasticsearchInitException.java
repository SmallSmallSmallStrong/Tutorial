package com.yijia.elasticsearch.exception;

/***
 * Elasticsearch初始化失败
 * @author zhqy
 *
 */
public class ElasticsearchInitException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ElasticsearchInitException() {
        super();
    }

    public ElasticsearchInitException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ElasticsearchInitException(String message, Throwable cause) {
        super(message, cause);
    }

    public ElasticsearchInitException(String message) {
        super(message);
    }

    public ElasticsearchInitException(Throwable cause) {
        super(cause);
    }

}
