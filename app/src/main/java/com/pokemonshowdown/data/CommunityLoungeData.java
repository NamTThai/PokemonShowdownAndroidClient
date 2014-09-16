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
        mRoomList.add("lobby");
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
}
