package com.pokemonshowdown.data;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pokemonshowdown.app.R;

import org.apache.http.HttpResponse;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.util.Random;

public class NodeConnection {
    public final static String NTAG = "PKM_SERVER_CONNECTION";
    public final static int SUCCESS = 1;
    public final static int FAILURE = 0;
    private static NodeConnection sNodeConnection;

    private Context mAppContext;
    private WebSocketClient mWebSocketClient;

    private NodeConnection(Context appContext) {
        mAppContext = appContext;
        mWebSocketClient = getWebSocketClient();
    }

    public static NodeConnection getWithApplicationContext(Context c) {
        if (sNodeConnection == null) {
            sNodeConnection = new NodeConnection(c.getApplicationContext());
        }
        return sNodeConnection;
    }

    public WebSocketClient getWebSocketClient() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mAppContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (mWebSocketClient != null && mWebSocketClient.getConnection().isOpen()) {
                return mWebSocketClient;
            } else {
                new ConnectToServer().execute();
                return mWebSocketClient;
            }
        } else {
            Log.d(NodeConnection.NTAG, "Check network connection");
            return null;
        }
    }

    public void closeActiveConnection() {
        if(mWebSocketClient != null && mWebSocketClient.getConnection().isOpen()) {
            mWebSocketClient.close();
        }
    }

    public int sendClientMessage(String message) {
        WebSocketClient webSocketClient = getWebSocketClient();
        if (webSocketClient != null) {
            webSocketClient.send(message);
            return SUCCESS;
        } else {
            return FAILURE;
        }
    }

    private class ConnectToServer extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params){
            try {
                openNewConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private String openNewConnection() throws IOException {

            try {
                Log.d(NTAG, "Initiating connection");

                URI uri = new URI("ws://nthai.cs.trincoll.edu:8000/showdown/websocket");

                if (mWebSocketClient == null) {
                    mWebSocketClient = new WebSocketClient(uri) {
                        @Override
                        public void onOpen(ServerHandshake serverHandshake) {
                            Log.d(NTAG, "Opened");
                        }

                        @Override
                        public void onMessage(String s) {
                            Log.d(NTAG, s);
                        }

                        @Override
                        public void onClose(int code, String reason, boolean remote) {
                            Log.d(NTAG, "Closed: code " + code + " reason " + reason + " remote " + remote);
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.d(NTAG, "Error: " + e.toString());
                        }
                    };
                }
                if (!mWebSocketClient.getConnection().isOpen()) {
                    mWebSocketClient.connect();
                }
            } catch (Exception e) {
                Log.d(NTAG, e.toString());
            }
            return null;
        }
    }
}
