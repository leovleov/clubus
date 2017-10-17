package cmu.clubus.models;

import cmu.clubus.helpers.APPCrypt;

public class Token {

    String token = null;
    String userId = null;
    String userName = null;

    public Token(User user) throws Exception{
        this.userId = user.userId;
        this.token = APPCrypt.encrypt(user.userId);
        this.userName = user.userName;
    }
}
