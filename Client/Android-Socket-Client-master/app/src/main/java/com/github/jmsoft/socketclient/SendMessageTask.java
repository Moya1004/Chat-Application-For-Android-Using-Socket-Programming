package com.github.jmsoft.socketclient;
import java.io.*;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.EditText;
import java.io.IOException;
import java.net.Socket;

import socketclient.lg.com.socketclient.R;

/**
 * Async task to send messages to server
 */
public class SendMessageTask extends AsyncTask<String, String, Void> {

    private ObjectOutputStream oos;
    private Socket sSocket;
    private Context context;
    private EditText etMessage;
    private String mIdentification;

    public SendMessageTask(ObjectOutputStream oos,Socket sSocket, EditText etMessage, String mIdentification, Context context){
        this.oos = oos;
        this.sSocket = sSocket;
        this.etMessage = etMessage;
        this.mIdentification = mIdentification;
        this.context = context;
    }

    @Override
    protected Void doInBackground(String... arg) {
        try {
            try {
                String msg = arg[0];
                if(msg.equalsIgnoreCase("LOGOUT")) {
                    oos.writeObject(new ChatMessage(ChatMessage.LOGOUT, ""));
                }
                // message to check who are present in chatroom
                else if(msg.equalsIgnoreCase("WHOISIN")) {
                    oos.writeObject(new ChatMessage(ChatMessage.WHOISIN, ""));
                }
                else if(msg.toLowerCase().startsWith("create group ")) {
                    oos.writeObject(new ChatMessage(ChatMessage.CREATE_GROUP, msg.toLowerCase().replace("create group ","")));
                }
                else if(msg.toLowerCase().startsWith("register for group ")) {
                    oos.writeObject(new ChatMessage(ChatMessage.REGISTER_FOR_GROUP, msg.toLowerCase().replace("register for group ","")));
                }
                else if(msg.toLowerCase().startsWith("message group ")) {
                    oos.writeObject(new ChatMessage(ChatMessage.MESSAGE_GROUP, msg.toLowerCase().replace("message group ","")));
                }
                // regular text message
                else {
                    oos.writeObject(new ChatMessage(ChatMessage.MESSAGE, msg));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e1) {
            e1.printStackTrace();
        }

        //Update UI
        publishProgress(arg[0]);
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        etMessage.setText("");
    }

    public void setsSocket(Socket socket){
        this.sSocket = socket;
    }
}
