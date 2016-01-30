package com.hackbridge.viral;

/**
 * Created by PetarV on 30/01/2016.
 */
public class IdHandlerPair {
    private long id;
    private ClientHandler handler;

    public IdHandlerPair(long id, ClientHandler handler) {
        this.id = id;
        this.handler = handler;
    }

    public long getId() {
        return id;
    }

    public ClientHandler getHandler() {
        return handler;
    }
}
