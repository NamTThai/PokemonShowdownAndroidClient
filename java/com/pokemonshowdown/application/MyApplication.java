package com.pokemonshowdown.application;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.pokemonshowdown.R;
import com.pokemonshowdown.data.BattleFieldData;
import com.pokemonshowdown.data.Onboarding;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Initialize all the data singletons
 * Manage NodeConnection
 */
public class MyApplication extends Application {
    public final static String MTAG = MyApplication.class.getName();

    private static MyApplication sMyApplication;

    private String mServerAddress;
    private WebSocketClient mWebSocketClient;
    private int mUserCount;
    private int mBattleCount;
    private HashMap<String, JSONArray> mRoomCategoryList;
    private List<Exception> mCaughtExceptions = new ArrayList<>();

    public static MyApplication getMyApplication() {
        return sMyApplication;
    }

    public static String toId(String name) {
        if (name == null) {
            return null;
        }
        return name.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sMyApplication = this;
        Fresco.initialize(this);

        mRoomCategoryList = getRoomCategoryList();
    }

    public WebSocketClient getWebSocketClient() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (mWebSocketClient != null && mWebSocketClient.getConnection().isOpen()) {
                return mWebSocketClient;
            } else {
                return mWebSocketClient = openNewConnection();
            }
        } else {
            BroadcastSender.get(this).sendBroadcastFromMyApplication(
                    BroadcastSender.EXTRA_NO_INTERNET_CONNECTION);
            return null;
        }
    }

    public WebSocketClient openNewConnection() {
        if (getServerAddress() == null) {
            return null;
        }

        try {
            URI uri = new URI(getServerAddress());

            WebSocketClient webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    Log.d(MTAG, "Opened connection");
                    initiateChatRoomList();
                }

                @Override
                public void onMessage(String s) {
                    Log.d(MTAG, s);
                    processMessage(s);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(MTAG, "Closed: code " + code + " reason " + reason + " remote " + remote);
                }

                @Override
                public void onError(Exception e) {
                    Log.d(MTAG, "Problem with websocket connection: " + e);
                    e.printStackTrace();
                }
            };
            webSocketClient.connect();
            return webSocketClient;
        } catch (URISyntaxException e) {
            Log.d(MTAG, "Wrong URI");
            return null;
        }
    }

    public void closeActiveConnection() {
        if (mWebSocketClient != null) {
            mWebSocketClient.close();
        }
    }

    public void sendClientMessage(String message) {
        WebSocketClient webSocketClient = getWebSocketClient();
        Log.d(MTAG, message);
        if (webSocketClient != null) {
            try {
                webSocketClient.send(message);
            } catch (WebsocketNotConnectedException e) {
                BroadcastSender.get(this).sendBroadcastFromMyApplication(
                        BroadcastSender.EXTRA_NO_INTERNET_CONNECTION);
            }
        }
    }

    /**
     * Channel list:
     * -1: global or lobby
     * 0: battle
     * 1: chatroom
     */
    private void processMessage(String message) {
        if (message.length() == 0) {
            return;
        }

        if (message.charAt(0) != '>') {
            int newLine = message.indexOf('\n');
            while (newLine != -1) {
                processGlobalMessage(message.substring(0, newLine));
                message = message.substring(newLine + 1);
                newLine = message.indexOf('\n');
            }
            processGlobalMessage(message);
        } else {
            int newLine = message.indexOf('\n');
            String roomId = message.substring(1, newLine);
            message = message.substring(newLine + 1);
            newLine = message.indexOf('\n');
            while (newLine != -1) {
                processRoomMessage(roomId, message.substring(0, newLine));
                message = message.substring(newLine + 1);
                newLine = message.indexOf('\n');
            }
            processRoomMessage(roomId, message);
        }
    }

    public void processGlobalMessage(String message) {
        int channel;
        if (message.charAt(0) != '|') {
            channel = 1;
        } else {
            message = message.substring(1);
            int endOfCommand = message.indexOf('|');
            String command = (endOfCommand == -1) ? message : message.substring(0, endOfCommand);
            String messageDetail = (endOfCommand == -1) ? "" : message.substring(endOfCommand + 1);

            Onboarding onboarding;
            switch (command) {
                case "challstr":
                    channel = -1;
                    onboarding = Onboarding.get(getApplicationContext());
                    onboarding.setKeyId(messageDetail.substring(0, messageDetail.indexOf('|')));
                    onboarding.setChallenge(messageDetail.substring(messageDetail.indexOf('|') + 1));
                    String result = Onboarding.get(getApplicationContext()).attemptSignIn();
                    if (result != null) {
                        sendClientMessage("|/trn " + result);
                    }
                    break;
                case "assertion":
                    channel = -1;
                    String name = messageDetail.substring(0, messageDetail.indexOf('|'));
                    String assertion = messageDetail.substring(messageDetail.indexOf('|') + 1);
                    sendClientMessage("|/trn " + name + ",0," + assertion);
                    break;
                case "updateuser":
                    channel = -1;
                    String username = messageDetail.substring(0, messageDetail.indexOf('|'));
                    String guestStatus = messageDetail.substring(messageDetail.indexOf('|') + 1, messageDetail.lastIndexOf('|'));
                    String avatar = messageDetail.substring(messageDetail.lastIndexOf('|') + 1);
                    if (avatar.length() == 1) {
                        avatar = "00" + avatar;
                    } else {
                        if (avatar.length() == 2) {
                            avatar = "0" + avatar;
                        }
                    }
                    onboarding = Onboarding.get(getApplicationContext());
                    if (guestStatus.equals("0")) {
                        onboarding.setUsername(username);
                        onboarding.setSignedIn(false);
                        onboarding.setAvatar(avatar);
                    } else {
                        onboarding.setUsername(username);
                        onboarding.setSignedIn(true);
                        onboarding.setAvatar(avatar);

                        BroadcastSender.get(this).sendBroadcastFromMyApplication(
                                BroadcastSender.EXTRA_LOGIN_SUCCESSFUL, username);
                    }
                    break;
                case "nametaken":
                    channel = -1;
                    final String errorMessage = messageDetail.substring(messageDetail.indexOf('|') + 1);
                    BroadcastSender.get(this).sendBroadcastFromMyApplication(
                            BroadcastSender.EXTRA_ERROR_MESSAGE, errorMessage);
                    break;
                case "queryresponse":
                    channel = -1;
                    final String query = messageDetail.substring(0, messageDetail.indexOf('|'));
                    switch (query) {
                        case "rooms":
                            final String rooms = messageDetail.substring(messageDetail.indexOf('|') + 1);
                            if (!rooms.equals("null")) {
                                try {
                                    processClientInitiationJson(new JSONObject(rooms));
                                } catch (JSONException e) {
                                    Log.d(MTAG, e.toString());
                                }
                            }
                            break;
                        case "roomlist":
                            String roomList = messageDetail.substring(messageDetail.indexOf('|') + 1);
                            BattleFieldData.get(getApplicationContext()).parseAvailableWatchBattleList(roomList);
                            break;
                        case "savereplay":
                            final String replayData = messageDetail.substring(messageDetail.indexOf('|') + 1);
                            BroadcastSender.get(this).sendBroadcastFromMyApplication(
                                    BroadcastSender.EXTRA_REPLAY_DATA, replayData);
                            break;
                        case "userdetails":
                            final String details = messageDetail.substring(messageDetail.indexOf('|') + 1);
                            try {
                                JSONObject userDetailsJSON = new JSONObject(details);
                                String userIDDetails = userDetailsJSON.optString("userid", null);
                                if (userIDDetails != null && userIDDetails.equals(Onboarding.get(this).getUsername())) {
                                    //we update our avatar for now
                                    int avatarId = userDetailsJSON.optInt("avatar", 0);
                                    if (avatarId > 0) {
                                        String avatarString = String.valueOf(avatarId);
                                        while (avatarString.length() < 3) {
                                            avatarString = "0" + avatarString;
                                        }
                                        Onboarding.get(this).setAvatar(avatarString);
                                    }
                                }

                            } catch (JSONException e) {
                                Log.d(MTAG, e.toString());
                            }

                            break;
                        default:
                            Log.d(MTAG, message);
                    }
                    break;
                case "formats":
                    channel = -1;
                    BattleFieldData.get(getApplicationContext()).generateAvailableRoomList(messageDetail);
                    break;
                case "popup":
                    channel = -1;
                    final String popupMessage = messageDetail.substring(messageDetail.indexOf('|') + 1);
                    BroadcastSender.get(this).sendBroadcastFromMyApplication(
                            BroadcastSender.EXTRA_ERROR_MESSAGE, popupMessage);
                    break;
                case "updatesearch":
                    channel = -1;
                    final String searchStatus = messageDetail.substring(messageDetail.indexOf('|') + 1);
                    BroadcastSender.get(this).sendBroadcastFromMyApplication(
                            BroadcastSender.EXTRA_UPDATE_SEARCH, searchStatus);
                    break;
                case "pm":
                    channel = -1;
                    String user = messageDetail.substring(5, messageDetail.indexOf("|", 5));
                    String log = messageDetail.substring(messageDetail.lastIndexOf("|"));

                    Toast.makeText(sMyApplication, "User \"" + user + "\" said: " + log, Toast.LENGTH_LONG).show();
                    break;
                case "usercount":
                case "updatechallenges":
                    channel = -1;
                    final String challengesStatus = messageDetail.substring(messageDetail.indexOf('|') + 1);
                    BroadcastSender.get(this).sendBroadcastFromMyApplication(
                            BroadcastSender.EXTRA_UPDATE_CHALLENGE, challengesStatus);
                    break;
                case "deinit":
                    channel = -1;
                    Log.d(MTAG, message);
                    break;
                default:
                    channel = 1;
                    break;
            }
        }

        if (channel == 1) {
            BroadcastSender.get(this).sendBroadcastFromMyApplication(
                    BroadcastSender.EXTRA_SERVER_MESSAGE, message,
                    BroadcastSender.EXTRA_CHANNEL, Integer.toString(channel),
                    BroadcastSender.EXTRA_ROOMID, "lobby");
        }

    }

    public void processRoomMessage(String roomId, String message) {
        int channel;
        if (roomId.startsWith("battle")) {
            channel = 0;
        } else {
            channel = 1;
        }

        if (message.length() == 0) {
            return;
        }

        if (message.charAt(0) == '|') {
            message = message.substring(1);
        }

        if (message.startsWith("init") && channel == 0) {
            BroadcastSender.get(this).sendBroadcastFromMyApplication(
                    BroadcastSender.EXTRA_NEW_BATTLE_ROOM, null,
                    BroadcastSender.EXTRA_ROOMID, roomId);
        } else {
            BroadcastSender.get(this).sendBroadcastFromMyApplication(
                    BroadcastSender.EXTRA_SERVER_MESSAGE, message,
                    BroadcastSender.EXTRA_CHANNEL, Integer.toString(channel),
                    BroadcastSender.EXTRA_ROOMID, roomId);
        }
    }

    public boolean verifySignedInBeforeSendingMessage() {
        Onboarding onboarding = Onboarding.get(getApplicationContext());
        if (!onboarding.isSignedIn()) {
            if (onboarding.getKeyId() == null || onboarding.getChallenge() == null) {
                getWebSocketClient();
                BroadcastSender.get(this).sendBroadcastFromMyApplication(
                        BroadcastSender.EXTRA_NO_INTERNET_CONNECTION);
                return false;
            }
            BroadcastSender.get(this).sendBroadcastFromMyApplication(
                    BroadcastSender.EXTRA_REQUIRE_SIGN_IN);
            return false;
        }
        return true;
    }

    private void initiateChatRoomList() {
        sendClientMessage("|/cmd rooms");
    }

    public void processClientInitiationJson(JSONObject clientInitiationJson) {
        try {
            Iterator<String> keySet = clientInitiationJson.keys();
            while (keySet.hasNext()) {
                String key = keySet.next();
                switch (key) {
                    case "userCount":
                        setUserCount(clientInitiationJson.getInt("userCount"));
                        break;
                    case "battleCount":
                        setBattleCount(clientInitiationJson.getInt("battleCount"));
                        break;
                    default:
                        JSONArray rooms = clientInitiationJson.getJSONArray(key);
                        getRoomCategoryList().put(key, rooms);
                }
            }
        } catch (JSONException e) {
            Log.d(MTAG, e.toString());
        }
    }

    public int getUserCount() {
        return mUserCount;
    }

    public void setUserCount(int userCount) {
        mUserCount = userCount;
    }

    public int getBattleCount() {
        return mBattleCount;
    }

    public void setBattleCount(int battleCount) {
        mBattleCount = battleCount;
    }

    public String getServerAddress() {
        return mServerAddress;
    }

    public void setServerAddress(String serverAddress) {
        mServerAddress = serverAddress;
    }

    public HashMap<String, JSONArray> getRoomCategoryList() {
        if (mRoomCategoryList == null) {
            mRoomCategoryList = new HashMap<>();
        }
        return mRoomCategoryList;
    }

    public List<Exception> getCaughtExceptions() {
        return mCaughtExceptions;
    }

    public void addCaughtException(Exception e) {
        mCaughtExceptions.add(e);
        if (Onboarding.get(getApplicationContext()).isBugReporting()) {
            Toast.makeText(this, getText(R.string.bug_captured), Toast.LENGTH_SHORT).show();
        }
    }

    public void clearCaughtExceptions() {
        mCaughtExceptions.clear();
    }
}
