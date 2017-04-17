package com.pokemonshowdown.data;

import android.content.Context;

import com.pokemonshowdown.application.MyApplication;

import java.util.ArrayList;
import java.util.HashMap;

public class CommunityLoungeData {
    private final static String CTAG = CommunityLoungeData.class.getName();
    private static CommunityLoungeData sCommunityLoungeData;
    private ArrayList<String> mRoomList;
    private HashMap<String, RoomData> mRoomDataHashMap;

    private CommunityLoungeData(Context appContext) {
        mRoomList = new ArrayList<>();
        mRoomDataHashMap = new HashMap<>();
    }

    public static CommunityLoungeData get(Context c) {
        if (sCommunityLoungeData == null) {
            sCommunityLoungeData = new CommunityLoungeData(c.getApplicationContext());
        }
        return sCommunityLoungeData;
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

    public void leaveAllRooms() {
        for (String roomId : mRoomList) {
            leaveRoom(roomId);
        }
    }

    public void leaveRoom(String roomId) {
        MyApplication.getMyApplication().sendClientMessage("|/leave " + roomId);
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
}
