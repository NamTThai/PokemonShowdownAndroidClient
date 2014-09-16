package com.pokemonshowdown.data;

import android.content.Context;

import java.util.ArrayList;

public class CommunityLoungeData {
    private final static String CTAG = CommunityLoungeData.class.getName();
    private ArrayList<String> mRoomList;

    private static CommunityLoungeData sCommunityLoungeData;
    private Context mAppContext;

    private CommunityLoungeData(Context appContext) {
        mAppContext = appContext;
        mRoomList = new ArrayList<>();
    }

    public static CommunityLoungeData get(Context c) {
        if (sCommunityLoungeData == null) {
            sCommunityLoungeData = new CommunityLoungeData(c.getApplicationContext());
        }
        return sCommunityLoungeData;
    }

    public static CommunityLoungeData getWithApplicationContext(Context appContext) {
        if (sCommunityLoungeData == null) {
            sCommunityLoungeData = new CommunityLoungeData(appContext);
        }
        return sCommunityLoungeData;
    }

    public ArrayList<String> getRoomList() {
        return mRoomList;
    }

    public void joinRoom(String roomId) {
        mRoomList.add(roomId);
        MyApplication.getMyApplication().sendClientMessage("|/join " + roomId);
    }

    public void leaveRoom(String roomId) {
        mRoomList.remove(roomId);
        MyApplication.getMyApplication().sendClientMessage("|/leave " + roomId);
    }

    public void joinAllRooms() {
        for (String roomId : mRoomList) {
            joinRoom(roomId);
        }
    }

    public void leaveAllRooms() {
        for (String roomId : mRoomList) {
            leaveRoom(roomId);
        }
    }
}
