package org.xwiki.contrib.mail;

public enum ConnectionErrors
{

    OK(0),
    AUTHENTICATION_FAILED(-1),
    CONNECTION_ERROR(-2),
    ILLEGAL_STATE(-3),
    UNEXPECTED_EXCEPTION(-4),
    FOLDER_NOT_FOUND(-5),
    UNKNOWN_HOST(-6),
    OTHER_ERROR(-7),
    INVALID_PREFERENCES(-8);

    private int code;

    private ConnectionErrors(final int code)
    {
        this.code = code;
    }

    public int getCode()
    {
        return this.code;
    }
}
