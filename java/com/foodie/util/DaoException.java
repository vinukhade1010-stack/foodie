package com.foodie.util;

/** Wraps SQLException so servlets do not need try/catch around every DAO call. */
public class DaoException extends RuntimeException {
    public DaoException(Throwable cause) { super(cause); }
}
