package com.vatichub.obd2.exception;

public class OBD2ClientException extends Exception {
    public OBD2ClientException(String msg) {
        super(msg);
    }

    public OBD2ClientException(String msg, Throwable e) {
        super(msg, e);
    }
}
