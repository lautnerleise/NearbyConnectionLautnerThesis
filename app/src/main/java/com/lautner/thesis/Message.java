package com.lautner.thesis;

import java.io.Serializable;

public class Message implements Serializable {

    private String message;
    private String fromName;
    private String toEndpoint;
    private Role toRole;

    Message(String message, String fromName, String toEndpoint, Role toRole){
        this.message = message;
        this.fromName = fromName;
        this.toEndpoint = toEndpoint;
        this.toRole = toRole;
    }

    public String getMessage(){
        return message;
    }

    public String getFromName(){
        return fromName;
    }

    public String getToEndpoint(){
        return toEndpoint;
    }

    public Role getToRole(){
        return toRole;
    }
}
