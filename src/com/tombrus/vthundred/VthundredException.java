package com.tombrus.vthundred;

import java.io.*;

public class VthundredException extends RuntimeException {
    private Exception cause;

    public VthundredException (String msg) {
        this(new Exception(msg));
    }

    public VthundredException (Exception cause) {
        super(cause.getMessage(), cause);
        this.cause = cause;
    }

    @Override
    public Throwable getCause () {
        return cause;
    }

    @Override
    public String getLocalizedMessage () {
        return cause.getLocalizedMessage();
    }

    @Override
    public String getMessage () {
        return cause.getMessage();
    }

    @Override
    public StackTraceElement[] getStackTrace () {
        return cause.getStackTrace();
    }

    @Override
    public void printStackTrace () {
        cause.printStackTrace();
    }

    @Override
    public void printStackTrace (PrintStream s) {
        cause.printStackTrace(s);
    }

    @Override
    public void printStackTrace (PrintWriter s) {
        cause.printStackTrace(s);
    }

    @Override
    public String toString () {
        return cause.toString();
    }
}
