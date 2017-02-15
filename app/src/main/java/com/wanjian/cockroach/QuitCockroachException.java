package com.wanjian.cockroach;

/**
 * Created by wanjian on 2017/2/15.
 */

public class QuitCockroachException extends RuntimeException {
    public QuitCockroachException(String message) {
        super(message);
    }
}
