package com.pokemonshowdown.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;

public class NodeConnection {
    public final static String NTAG = "PKM_SERVER_CONNECTION";
    public final static int SUCCESS = 1;
    public final static int FAILURE = 0;
    private static NodeConnection sNodeConnection;

    private Context mAppContext;
    private WebSocketClient mWebSocketClient;

    private NodeConnection(Context appContext) {
        mAppContext = appContext;
        mWebSocketClient = null;
    }

    public static NodeConnection getWithApplicationContext(Context c) {
        if (sNodeConnection == null) {
            sNodeConnection = new NodeConnection(c);
        }
        return sNodeConnection;
    }

    public WebSocketClient getWebSocketClient() {
        return mWebSocketClient;
    }

    public void setWebSocketClient(WebSocketClient webSocketClient) {
        mWebSocketClient = webSocketClient;
    }
}
