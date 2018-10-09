package com.github.jmsoft.socketclient;

import android.content.Context;
import android.os.AsyncTask;

import com.github.jmsoft.socketclient.adapter.ChatAdapter;

import java.io.DataInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.io.*;


import socketclient.lg.com.socketclient.R;

/**
 * Async task to connect to server
 */
public class ConnectionTask extends AsyncTask<Void, String, Void> {

    //Socket for connecting the client to server
    public ObjectOutputStream oos;
    private ObjectInputStream ois;
    private Socket sSocket;
    private InetAddress ia;
    private int mPort;
    private Context context;
    private List<String> messages;
    private ChatAdapter mChatAdapter;
    private String identity;

    public ConnectionTask(InetAddress ia, int mPort, Context context, List<String> messages, ChatAdapter mChatAdapter,String identity){
        this.ia = ia;
        this.mPort = mPort;
        this.context = context;
        this.messages = messages;
        this.mChatAdapter = mChatAdapter;
        this.identity = identity;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            sSocket = new Socket("192.168.43.180", 1500);
            oos = new ObjectOutputStream(sSocket.getOutputStream());
            ois = new ObjectInputStream(sSocket.getInputStream());
            //Add connected to server message to UI
            publishProgress(context.getResources().getString(R.string.connected_to_server));
            oos.writeObject(identity);
            //Listen to messages
            while (true) {
                final String string = ois.readObject().toString();

                publishProgress(string);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        messages.add(values[0]);
        mChatAdapter.notifyDataSetChanged();
    }

    public Socket getsSocket() {
        return sSocket;
    }
}
