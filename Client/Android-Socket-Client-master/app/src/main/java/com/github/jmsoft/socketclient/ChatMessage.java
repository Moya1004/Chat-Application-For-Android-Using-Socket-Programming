package com.github.jmsoft.socketclient;
/**
 * Created by Yusuf on 4/18/2018.
 */
import java.io.*;

public class ChatMessage implements Serializable {

    // The different types of message sent by the Client
    // WHOISIN to receive the list of the users connected
    // MESSAGE an ordinary text message
    // LOGOUT to disconnect from the Server
    private static final long serialVersionUID = 1L;
    static final int WHOISIN = 0, MESSAGE = 1, LOGOUT = 2 , CREATE_GROUP = 3 , REGISTER_FOR_GROUP = 4 , MESSAGE_GROUP = 5;
    private int type;
    private String message;

    // constructor
    ChatMessage(int type, String message) {
        this.type = type;
        this.message = message;
    }

    int getType() {
        return type;
    }

    String getMessage() {
        return message;
    }
}
