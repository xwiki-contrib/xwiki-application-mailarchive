package org.xwiki.contrib.mail;

public enum SourceConnectionErrors
{

    OK(0),
    AUTHENTICATION_FAILED(-1),
    CONNECTION_ERROR(-2),
    ILLEGAL_STATE(-3),
    UNEXPECTED_EXCEPTION(-4),
    FOLDER_NOT_FOUND(-5),
    UNKNOWN_HOST(-6),
    OTHER_ERROR(-7),
    INVALID_PREFERENCES(-8),
    INVALID_STORE_FILE(-9),
    INVALID_STORE_FILE_FORMAT(-10),
    UNKNOWN_SOURCE_TYPE(-11);

    private int code;

    private SourceConnectionErrors(final int code)
    {
        this.code = code;
    }

    public int getCode()
    {
        return this.code;
    }
}
