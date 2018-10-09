package com.github.jmsoft.socketclient;
import java.io.*;
/*
 * This class defines the different type of messages that will be exchanged between the
 * Clients and the Server. 
 * When talking from a Java Client to a Java Server a lot easier to pass Java objects, no 
 * need to count bytes or to wait for a line feed at the end of the frame
 */

public class ChatMessage implements Serializable {

	// The different types of message sent by the Client
	// WHOISIN to receive the list of the users connected
	// MESSAGE an ordinary text message
	// LOGOUT to disconnect from the Server
        private static final long serialVersionUID = 1L;
	public static final int WHOISIN = 0, MESSAGE = 1, LOGOUT = 2  , CREATE_GROUP = 3 , REGISTER_FOR_GROUP = 4 
                , MESSAGE_GROUP = 5, EVERYONE = 6;
	private int type;
	private String message;
	
	// constructor
	public ChatMessage(int type, String message) {
		this.type = type;
		this.message = message;
	}
	
	public int getType() {
		return type;
	}

	public String getMessage() {
		return message;
	}
}
