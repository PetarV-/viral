package com.hackbridge.viral;

/**
 * Created by PetarV on 30/01/2016.
 */

/*
 * Phone -> Server. When typing in a code. (id, code)
 */
public class CodeMessage extends Message
{
    private long   id;
    private String code;

    public CodeMessage(long id, String code)
    {
        this.id = id;
        this.code = code;
    }

    public long getId()
    {
        return id;
    }

    public String getCode()
    {
        return code;
    }
}
