package com.sab.littleh.util.sab_format;

public class SabParsingException extends RuntimeException {
    public SabParsingException() {
    }

    public SabParsingException(String message) {
        super(message);
    }

    public SabParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public SabParsingException(Throwable cause) {
        super(cause);
    }

    public SabParsingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
