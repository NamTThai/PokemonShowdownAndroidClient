package com.pokemonshowdown.data;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Initialize all the data singletons
 * Manage NodeConnection
 */
public class MyApplication extends Application {
    public final static String MTAG = MyApplication.class.getName();
    public final static String ACTION_FROM_MY_APPLICATION = "From My Application";
    public final static String EXTRA_DETAILS = "Details";
    public final static String EXTRA_NO_INTERNET_CONNECTION = "No Internet Connection";
    public final static String EXTRA_WATCH_BATTLE_LIST_READY = "Watch Battle List Ready";
    public final static String EXTRA_AVAILABLE_FORMATS = "Available Formats";
    public final static String EXTRA_SERVER_MESSAGE = "New Server Message";
    public final static String EXTRA_REQUIRE_SIGN_IN = "Require Sign In";
    public final static String EXTRA_ERROR_MESSAGE = "Error Message";
    public final static String EXTRA_CHANNEL = "Channel";
    public final static String EXTRA_ROOMID = "RoomId";
    
    private static MyApplication sMyApplication;
    
    private Pokedex mPokedex;
    private MoveDex mMoveDex;
    private AbilityDex mAbilityDex;
    private ItemDex mItemDex;
    private WebSocketClient mWebSocketClient;
    private Onboarding mOnboarding;
    private CommunityLoungeData mCommunityLoungeData;
    private BattleFieldData mBattleFieldData;
    private JSONObject mClientInitiationJson;
    private int mUserCount;
    private int mBattleCount;
    private HashMap<String, JSONArray> mRoomCategoryList;

    @Override
    public void onCreate() {
        super.onCreate();

        sMyApplication = this;
        Context appContext = getApplicationContext();
        
        mWebSocketClient = getWebSocketClient();
        mPokedex = Pokedex.getWithApplicationContext(appContext);
        mMoveDex = MoveDex.getWithApplicationContext(appContext);
        mAbilityDex = AbilityDex.getWithApplicationContext(appContext);
        mItemDex = ItemDex.getWithApplicationContext(appContext);
        mOnboarding = Onboarding.getWithApplicationContext(appContext);
        mBattleFieldData = BattleFieldData.getWithApplicationContext(appContext);
        mCommunityLoungeData = CommunityLoungeData.getWithApplicationContext(appContext);
        mRoomCategoryList = new HashMap<>();
        initiateChatRoomList();
    }

    @Override
    public void onTerminate() {
        mOnboarding.signingOut();
        mBattleFieldData.leaveAllRooms();
        mCommunityLoungeData.leaveAllRooms();
        closeActiveConnection();
        super.onTerminate();
    }

    public static MyApplication getMyApplication() {
        return sMyApplication;
    }

    public void setWebSocketClient(WebSocketClient webSocketClient) {
        mWebSocketClient = webSocketClient;
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
            Intent intent = new Intent(ACTION_FROM_MY_APPLICATION).putExtra(EXTRA_DETAILS, EXTRA_NO_INTERNET_CONNECTION);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            return null;
        }
    }

    public WebSocketClient openNewConnection() {
        try {
            URI uri = new URI("ws://sim.smogon.com:8000/showdown/websocket");
            // URI uri = new URI("ws://nthai.cs.trincoll.edu:8000/showdown/websocket");

            WebSocketClient webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    Log.d(MTAG, "Opened connection");
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
        if(mWebSocketClient != null) {
            mWebSocketClient.close();
        }
    }

    public void sendClientMessage(String message) {
        WebSocketClient webSocketClient = getWebSocketClient();
        if (webSocketClient != null) {
            try {
                webSocketClient.send(message);
            } catch (WebsocketNotConnectedException e) {
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_FROM_MY_APPLICATION).putExtra(EXTRA_DETAILS, EXTRA_NO_INTERNET_CONNECTION));
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
            Log.d("Text", message);
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
            String command = (endOfCommand == -1)? message : message.substring(0, endOfCommand);
            String messageDetail = (endOfCommand == -1)? "" : message.substring(endOfCommand + 1);

            Onboarding onboarding;
            switch (command) {
                case "challstr":
                    channel = -1;
                    onboarding = Onboarding.getWithApplicationContext(getApplicationContext());
                    onboarding.setKeyId(messageDetail.substring(0, messageDetail.indexOf('|')));
                    onboarding.setChallenge(messageDetail.substring(messageDetail.indexOf('|') + 1));
                    String result = Onboarding.getWithApplicationContext(getApplicationContext()).attemptSignIn();
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
                    onboarding = Onboarding.getWithApplicationContext(getApplicationContext());
                    if (guestStatus.equals("0")) {
                        onboarding.setUsername(username);
                        onboarding.setSignedIn(false);
                        onboarding.setAvatar(avatar);
                    } else {
                        onboarding.setUsername(username);
                        onboarding.setSignedIn(true);
                        onboarding.setAvatar(avatar);
                    }
                    break;
                case "nametaken":
                    channel = -1;
                    final String errorMessage = messageDetail.substring(messageDetail.indexOf('|') + 1);
                    LocalBroadcastManager.getInstance(MyApplication.this).sendBroadcast(new Intent(ACTION_FROM_MY_APPLICATION).putExtra(EXTRA_DETAILS, EXTRA_ERROR_MESSAGE).putExtra(EXTRA_ERROR_MESSAGE, errorMessage));
                    break;
                case "queryresponse":
                    channel = -1;
                    final String query = messageDetail.substring(0, messageDetail.indexOf('|'));
                    switch (query) {
                        case "rooms":
                            final String rooms = messageDetail.substring(messageDetail.indexOf('|') + 1);
                            if (!rooms.equals("null")) {
                                try {
                                    setClientInitiationJson(new JSONObject(rooms));
                                } catch (JSONException e) {
                                    Log.d(MTAG, e.toString());
                                }
                            }
                            break;
                        case "roomlist":
                            String roomlist = messageDetail.substring(messageDetail.indexOf('|') + 1);
                            BattleFieldData.getWithApplicationContext(getApplicationContext()).parseAvailableWatchBattleList(roomlist);
                            break;
                        default:
                            Log.d(MTAG, message);
                    }
                    break;
                case "formats":
                    channel = -1;
                    BattleFieldData.getWithApplicationContext(getApplicationContext()).generateAvailableRoomList(messageDetail);
                    break;
                case "popup":
                case "pm":
                case "usercount":
                case "updatesearch":
                case "updatechallenges":
                case "deinit":
                    channel = -1;
                    Log.d(MTAG, message);
                    break;
                default:
                    channel = 1;
            }
        }

        if (channel == 1) {
            LocalBroadcastManager.getInstance(MyApplication.this).sendBroadcast(new Intent(ACTION_FROM_MY_APPLICATION).putExtra(EXTRA_DETAILS, EXTRA_SERVER_MESSAGE).putExtra(EXTRA_SERVER_MESSAGE, message).putExtra(EXTRA_CHANNEL, channel).putExtra(EXTRA_ROOMID, "lobby"));
        }

    }

    public void processRoomMessage(String roomId, String message) {
        int channel;
        if (roomId.startsWith("battle")) {
            channel = 0;
        } else {
            channel = 1;
        }

        if (message.length() == 0 ) {
            return;
        }

        if (message.charAt(0) == '|') {
            message = message.substring(1);
        }

        LocalBroadcastManager.getInstance(MyApplication.this).sendBroadcast(new Intent(ACTION_FROM_MY_APPLICATION).putExtra(EXTRA_DETAILS, EXTRA_SERVER_MESSAGE).putExtra(EXTRA_SERVER_MESSAGE, message).putExtra(EXTRA_CHANNEL, channel).putExtra(EXTRA_ROOMID, roomId));
    }

    public boolean verifySignedInBeforeSendingMessage() {
        Onboarding onboarding = Onboarding.getWithApplicationContext(getApplicationContext());
        if (!onboarding.isSignedIn()) {
            if (onboarding.getKeyId() == null || onboarding.getChallenge() == null) {
                getWebSocketClient();
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_FROM_MY_APPLICATION).putExtra(EXTRA_DETAILS, EXTRA_NO_INTERNET_CONNECTION));
                return false;
            }
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_FROM_MY_APPLICATION).putExtra(EXTRA_DETAILS, EXTRA_REQUIRE_SIGN_IN));
            return false;
        }
        return true;
    }

    private void initiateChatRoomList() {
        sendClientMessage("|/cmd rooms");
    }

    public JSONObject getClientInitiationJson() {
        return mClientInitiationJson;
    }

    public void setClientInitiationJson(JSONObject clientInitiationJson) {
        mClientInitiationJson = clientInitiationJson;
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

    public HashMap<String, JSONArray> getRoomCategoryList() {
        return mRoomCategoryList;
    }

    public void setRoomCategoryList(HashMap<String, JSONArray> roomCategoryList) {
        mRoomCategoryList = roomCategoryList;
    }

    public static String toId(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}
