package com.lautner.thesis;

import java.io.Serializable;

public class User implements Serializable {
    private String userName;
    private Role role;

    User(String userName, Role role) {
        this.userName = userName;
        this.role = role;
    }

    public String getName(){
        return userName;
    }

    public Role getRole(){
        return role;
    }


}
