package org.cis120;

import java.util.*;

public class User {
    private final int userId;
    private String nickname;

    public User(int id, String nickname) {
        this.userId = id;
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public int getUserId() {
        return userId;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
