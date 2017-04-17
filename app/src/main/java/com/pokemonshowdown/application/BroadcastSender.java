package com.pokemonshowdown.application;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public class BroadcastSender {
    public final static String ACTION_FROM_MY_APPLICATION = "From My Application";
    public final static String EXTRA_DETAILS = "Details";
    public final static String EXTRA_NO_INTERNET_CONNECTION = "No Internet Connection";
    public final static String EXTRA_WATCH_BATTLE_LIST_READY = "Watch Battle List Ready";
    public final static String EXTRA_AVAILABLE_FORMATS = "Available Formats";
    public final static String EXTRA_NEW_BATTLE_ROOM = "New Room";
    public final static String EXTRA_SERVER_MESSAGE = "New Server Message";
    public final static String EXTRA_REQUIRE_SIGN_IN = "Require Sign In";
    public final static String EXTRA_ERROR_MESSAGE = "Error Message";
    public final static String EXTRA_UNKNOWN_ERROR = "Unknown Error";
    public final static String EXTRA_UPDATE_SEARCH = "Search Update";
    public final static String EXTRA_CHANNEL = "Channel";
    public final static String EXTRA_ROOMID = "RoomId";
    public final static String EXTRA_UPDATE_AVAILABLE = "Update Available";
    public final static String EXTRA_SERVER_VERSION = "Server Version";
    public final static String EXTRA_LOGIN_SUCCESSFUL = "Login Successful";
    public final static String EXTRA_REPLAY_DATA = "Replay Data";
    public final static String EXTRA_UPDATE_CHALLENGE = "EXTRA_UPDATE_CHALLENGE";
    public static final String EXTRA_CHANGELOG = "Changelog";

    private static BroadcastSender sBroadcastSender;
    private Context mContext;

    private BroadcastSender(Context context) {
        mContext = context;
    }

    public static BroadcastSender get(Context c) {
        if (sBroadcastSender == null) {
            sBroadcastSender = new BroadcastSender(c.getApplicationContext());
        }
        return sBroadcastSender;
    }

    public void sendBroadcastFromMyApplication(String details) {
        Intent intent = new Intent(ACTION_FROM_MY_APPLICATION)
                .putExtra(EXTRA_DETAILS, details);
        sendBroadcast(intent);
    }

    public void sendBroadcastFromMyApplication(String key, String value) {
        Intent intent = new Intent(ACTION_FROM_MY_APPLICATION)
                .putExtra(EXTRA_DETAILS, key)
                .putExtra(key, value);
        sendBroadcast(intent);
    }

    public void sendBroadcastFromMyApplication(String... s) {
        if (s.length % 2 != 0) {
            return;
        }
        Intent intent = new Intent(ACTION_FROM_MY_APPLICATION)
                .putExtra(EXTRA_DETAILS, s[0]);
        for (int i = 0; i < s.length - 1; i += 2) {
            intent.putExtra(s[i], s[i + 1]);
        }
        sendBroadcast(intent);
    }

    private void sendBroadcast(Intent intent) {
        if (BroadcastListener.get(mContext).isListening()) {
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        } else {
            BroadcastListener.get(mContext).addPendingIntent(intent);
        }
    }

}
