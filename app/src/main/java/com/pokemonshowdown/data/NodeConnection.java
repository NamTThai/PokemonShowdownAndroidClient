package com.pokemonshowdown.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.pokemonshowdown.app.BattleFieldActivity;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

public class NodeConnection {
    public final static String NTAG = NodeConnection.class.getName();
    private static NodeConnection sNodeConnection;

    private Context mAppContext;
    private WebSocketClient mWebSocketClient;
    private HashMap<String, String> mRoomLog;

    private NodeConnection(Context appContext) {
        mAppContext = appContext;
        mRoomLog = new HashMap<>();
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

    public HashMap<String, String> getRoomLog() {
        return mRoomLog;
    }

    public void setRoomLog(HashMap<String, String> roomLog) {
        mRoomLog = roomLog;
    }
}
