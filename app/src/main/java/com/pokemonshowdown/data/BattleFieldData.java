package com.pokemonshowdown.data;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.HashMap;

public class BattleFieldData {
    private final static String BTAG = BattleFieldData.class.getName();
    private ArrayList<FormatType> mFormatTypes;
    private ArrayList<String> mRoomList;
    private HashMap<String, RoomData> mRoomDataHashMap;

    private static BattleFieldData sBattleFieldData;
    private Context mAppContext;

    private BattleFieldData(Context appContext) {
        mAppContext = appContext;
        mFormatTypes = new ArrayList<>();
        mRoomList = new ArrayList<>();
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
                String formatName = (separator == -1) ? message : message.substring(separator);
                mFormatTypes.get(mFormatTypes.size() - 1).getFormatList().add(new Format(formatName));
                message = (separator == -1) ? "" : message.substring(separator + 1);
            }
        }
        LocalBroadcastManager.getInstance(mAppContext).sendBroadcast(new Intent(MyApplication.ACTION_FROM_MY_APPLICATION).putExtra(MyApplication.EXTRA_DETAILS, MyApplication.EXTRA_AVAILABLE_FORMATS));

    }

    public ArrayList<String> getRoomList() {
        return mRoomList;
    }

    public HashMap<String, RoomData> getRoomDataHashMap() {
        return mRoomDataHashMap;
    }

    public void saveRoomInstance(String roomId, ArrayList<String> userListData, CharSequence chatBox, boolean messageListener) {
        mRoomDataHashMap.put(roomId, new RoomData(roomId, userListData, chatBox, messageListener));
    }

    public RoomData getRoomInstance(String roomId) {
        return mRoomDataHashMap.get(roomId);
    }

    public void joinRoom(String roomId) {
        mRoomList.add(roomId);
        MyApplication.getMyApplication().sendClientMessage("|/join " + roomId);
    }

    public void leaveRoom(String roomId) {
        mRoomList.remove(roomId);
        MyApplication.getMyApplication().sendClientMessage("|/leave " + roomId);
    }

    public void leaveAllRooms() {
        for (String roomId : mRoomList) {
            leaveRoom(roomId);
        }
    }

    public static class RoomData {
        private String mRoomId;
        private ArrayList<String> mUserListData;
        private CharSequence mChatBox;
        private boolean mMessageListener;
        private ArrayList<String> mServerMessageOnHold;

        public RoomData(String roomId, ArrayList<String> userListData, CharSequence chatBox, boolean messageListener) {
            mRoomId = roomId;
            mUserListData = userListData;
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

        public ArrayList<String> getUserListData() {
            return mUserListData;
        }

        public void setUserListData(ArrayList<String> userListData) {
            mUserListData = userListData;
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

        public void addServerMessageOnHold(String serverMessageOnHold) {
            mServerMessageOnHold.add(serverMessageOnHold);
        }

        public boolean isMessageListener() {
            return mMessageListener;
        }

        public void setMessageListener(boolean messageListener) {
            mMessageListener = messageListener;
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
