package com.pokemonshowdown.data;

import android.os.AsyncTask;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;

/**
 * Created by thain on 8/9/14.
 */
public class NodeConnection extends AsyncTask<String, Void, String> {
    public final static String CTAG = "PKM_SERVER_CONNECTION";

    @Override
    protected String doInBackground(String... params){
        try {
            downloadFromServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String downloadFromServer() throws IOException {

        try {
            Log.d(CTAG, "Initiating connection");

            URI uri = new URI("ws://nthai.cs.trincoll.edu:8000/showdown/websocket");

            WebSocketClient webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    Log.d(CTAG, "Opened");
                    send("|/join lobby");
                }

                @Override
                public void onMessage(String s) {
                    Log.d(CTAG, s);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(CTAG, "Closed: code " + code + " reason " + reason + " remote " + remote);
                }

                @Override
                public void onError(Exception e) {
                    Log.d(CTAG, "Error: " + e.toString());
                }
            };
            webSocketClient.connect();
            webSocketClient.getConnection().send("|/ranking RainFountain");
        } catch (Exception e) {
            Log.d(CTAG, e.toString());
        }
        return null;
    }
}
