package com.pokemonshowdown.data;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;

import com.pokemonshowdown.app.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class BattleFieldData {
    private final static String BTAG = BattleFieldData.class.getName();
    private ArrayList<FormatType> mFormatTypes;
    private int mCurrentFormat;
    private JSONObject mAvailableBattle;
    private ArrayList<String> mRoomList;
    private HashMap<String, RoomData> mRoomDataHashMap;

    private static BattleFieldData sBattleFieldData;
    private Context mAppContext;

    private BattleFieldData(Context appContext) {
        mAppContext = appContext;
        mFormatTypes = new ArrayList<>();
        mRoomList = new ArrayList<>();
        mRoomList.add("global");
        mRoomDataHashMap = new HashMap<>();
    }

    public static BattleFieldData get(Context c) {
        if (sBattleFieldData == null) {
            sBattleFieldData = new BattleFieldData(c.getApplicationContext());
        }
        return sBattleFieldData;
    }

    public static BattleFieldData getWithApplicationContext(Context appContext) {
        if (sBattleFieldData == null) {
            sBattleFieldData = new BattleFieldData(appContext);
        }
        return sBattleFieldData;
    }

    public ArrayList<FormatType> getFormatTypes() {
        return mFormatTypes;
    }

    public void generateAvailableRoomList(String message) {
        while (message.length() != 0) {
            if (message.charAt(0) == ',') {
                message = message.substring(message.indexOf('|') + 1);
                String formatType = message.substring(0, message.indexOf('|'));
                mFormatTypes.add(new FormatType(formatType));
                message = message.substring(message.indexOf('|') + 1);
            } else {
                int separator = message.indexOf('|');
                String formatName = (separator == -1) ? message : message.substring(0, separator);
                mFormatTypes.get(mFormatTypes.size() - 1).getFormatList().add(processSpecialRoomTrait(formatName));
                message = (separator == -1) ? "" : message.substring(separator + 1);
            }
        }
        LocalBroadcastManager.getInstance(mAppContext).sendBroadcast(new Intent(MyApplication.ACTION_FROM_MY_APPLICATION).putExtra(MyApplication.EXTRA_DETAILS, MyApplication.EXTRA_AVAILABLE_FORMATS));

    }

    public String getCurrentFormatName() {
        int currentFormat = getCurrentFormat();
        int count = 0;
        do {
            int mask = mFormatTypes.get(count).getSearchableFormatList().size();
            if (mask > currentFormat) {
                return mFormatTypes.get(count).getSearchableFormatList().get(currentFormat);
            }
            count++;
            currentFormat -= mask;
        } while (currentFormat >= 0);
        return null;
    }

    public int getCurrentFormat() {
        return mCurrentFormat;
    }

    public void setCurrentFormat(int currentFormat) {
        mCurrentFormat = currentFormat;
    }

    public Format processSpecialRoomTrait(String query) {
        int separator = query.indexOf(',');
        Format format;
        if (separator == -1) {
            format = new Format(query);
        } else {
            format = new Format(query.substring(0, separator));
            String special = query.substring(separator);
            int specs = special.indexOf(",#");
            if (specs != -1) {
                format.getSpecialTrait().add(",#");
                special = special.substring(0, specs);
            }
            specs = special.indexOf(",,");
            if (specs != -1) {
                format.getSpecialTrait().add(",,");
                special = special.substring(0, specs);
            }
            specs = special.indexOf(",");
            if (specs != -1) {
                format.getSpecialTrait().add(",");
            }
        }
        return format;
    }

    public void parseAvailableWatchBattleList(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            mAvailableBattle = jsonObject.getJSONObject("rooms");
            LocalBroadcastManager.getInstance(mAppContext).sendBroadcast(new Intent(MyApplication.ACTION_FROM_MY_APPLICATION).putExtra(MyApplication.EXTRA_DETAILS, MyApplication.EXTRA_WATCH_BATTLE_LIST_READY));
        } catch (JSONException e) {
            Log.d(BTAG, e.toString());
        }
    }

    public HashMap<String, String> getAvailableWatchBattleList() {
        if (mAvailableBattle == null) {
            return null;
        }
        HashMap<String, String> toReturn = new HashMap<>();
        Iterator<String> rooms = mAvailableBattle.keys();
        String currentFormat = getCurrentFormatName();
        currentFormat = "-" + MyApplication.toId(currentFormat) + "-";
        while (rooms.hasNext()) {
            String roomId = rooms.next();
            if (roomId.contains(currentFormat)) {
                try {
                    JSONObject players = mAvailableBattle.getJSONObject(roomId);
                    String sb = players.getString("p1") + " vs. " + players.getString("p2");
                    toReturn.put(roomId, sb);
                } catch (JSONException e) {
                    Log.d(BTAG, e.toString());
                }
            }
        }
        return toReturn;
    }

    public static String getRoomFormat(String roomId) {
        return roomId.substring(roomId.indexOf("-") + 1, roomId.lastIndexOf("-"));
    }

    public ArrayList<String> getRoomList() {
        return mRoomList;
    }

    public HashMap<String, RoomData> getRoomDataHashMap() {
        return mRoomDataHashMap;
    }

    public void saveRoomInstance(String roomId, CharSequence chatBox, boolean messageListener) {
        mRoomDataHashMap.put(roomId, new RoomData(roomId, chatBox, messageListener));
    }

    public RoomData getRoomInstance(String roomId) {
        return mRoomDataHashMap.get(roomId);
    }

    public void joinRoom(String roomId) {
        mRoomList.add(roomId);
        HashMap<String, RoomData> roomDataHashMap = getRoomDataHashMap();
        roomDataHashMap.put(roomId, new RoomData(roomId, "", true));
        MyApplication.getMyApplication().sendClientMessage("|/join " + roomId);
    }

    public void leaveRoom(String roomId) {
        mRoomList.remove(roomId);
        getRoomDataHashMap().remove(roomId);
        MyApplication.getMyApplication().sendClientMessage("|/leave " + roomId);
    }

    public void leaveAllRooms() {
        for (String roomId : mRoomList) {
            leaveRoom(roomId);
        }
    }

    public static class RoomData {
        private String mRoomId;
        private CharSequence mChatBox;
        private boolean mMessageListener;
        private ArrayList<String> mServerMessageOnHold;

        private String mPlayer1;
        private String mPlayer2;

        public RoomData(String roomId, CharSequence chatBox, boolean messageListener) {
            mRoomId = roomId;
            mChatBox = chatBox;
            mServerMessageOnHold = new ArrayList<>();
            mMessageListener = messageListener;
        }

        public String getRoomId() {
            return mRoomId;
        }

        public void setRoomId(String roomId) {
            mRoomId = roomId;
        }

        public CharSequence getChatBox() {
            return mChatBox;
        }

        public void setChatBox(CharSequence chatBox) {
            mChatBox = chatBox;
        }

        public ArrayList<String> getServerMessageOnHold() {
            return mServerMessageOnHold;
        }

        public void setServerMessageOnHold(ArrayList<String> serverMessageOnHold) {
            mServerMessageOnHold = serverMessageOnHold;
        }

        public void addServerMessageOnHold(String serverMessageOnHold) {
            mServerMessageOnHold.add(serverMessageOnHold);
        }

        public boolean isMessageListener() {
            return mMessageListener;
        }

        public void setMessageListener(boolean messageListener) {
            mMessageListener = messageListener;
        }

        public String getPlayer1() {
            return mPlayer1;
        }

        public void setPlayer1(String player1) {
            mPlayer1 = player1;
        }

        public String getPlayer2() {
            return mPlayer2;
        }

        public void setPlayer2(String player2) {
            mPlayer2 = player2;
        }
    }

    public static class FormatType {
        private String mName;
        private ArrayList<Format> mFormatList;

        public FormatType(String name) {
            mName = name;
            mFormatList = new ArrayList<>();
        }

        public String getName() {
            return mName;
        }

        public void setName(String name) {
            mName = name;
        }

        public ArrayList<String> getSearchableFormatList() {
            ArrayList<String> formatList = new ArrayList<>();
            for (Format format : mFormatList) {
                if (!format.getSpecialTrait().contains(",")) {
                    formatList.add(format.getName());
                }
            }
            return formatList;
        }

        public ArrayList<Format> getFormatList() {
            return mFormatList;
        }

        public void setFormatList(ArrayList<Format> formatList) {
            mFormatList = formatList;
        }
    }

    public static class Format {
        private String mName;
        private ArrayList<String> mSpecialTrait;

        public Format(String name) {
            mName = name;
            mSpecialTrait = new ArrayList<>();
        }

        public String getName() {
            return mName;
        }

        public void setName(String name) {
            mName = name;
        }

        public ArrayList<String> getSpecialTrait() {
            return mSpecialTrait;
        }

        public void setSpecialTrait(ArrayList<String> specialTrait) {
            mSpecialTrait = specialTrait;
        }
    }
}
