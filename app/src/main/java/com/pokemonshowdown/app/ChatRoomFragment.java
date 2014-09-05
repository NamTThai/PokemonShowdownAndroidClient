package com.pokemonshowdown.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.pokemonshowdown.data.NodeConnection;
import com.pokemonshowdown.data.Pokemon;

import org.java_websocket.client.WebSocketClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class ChatRoomFragment extends android.support.v4.app.Fragment {
    public final static String CTAG = "ChatRoomFragment";
    private final static String ROOM_NAME = "Room Name";
    private final static String ROOM_ID = "Room Id";
    private final static String[] COLOR_STRONG = {"#0099CC", "#9933CC", "#669900", "#FF8800", "#CC0000"};
    private final static String[] COLOR_WEAK = {"#33B5E5", "#AA66CC", "#99CC00", "#FFBB33", "#FF4444"};

    private String mRoomName;
    private String mRoomId;
    private ListView mUserList;
    private TextView mChatLog;

    private ArrayList<String> mUserListData;
    private UserAdapter mUserAdapter;
    private LayoutInflater mLayoutInflater;

    public static ChatRoomFragment newInstance(String roomId) {
        ChatRoomFragment fragment = new ChatRoomFragment();
        Bundle args = new Bundle();
        args.putString(ROOM_ID, roomId);
        fragment.setArguments(args);
        return fragment;
    }
    public ChatRoomFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mLayoutInflater = inflater;
        return inflater.inflate(R.layout.fragment_chat_room, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUserListData = new ArrayList<>();
        mUserListData.add("HudsonRain");
        mUserAdapter = new UserAdapter(getActivity(), mUserListData);
        ListView listView = (ListView) view.findViewById(R.id.user_list);
        listView.setAdapter(mUserAdapter);
        mUserListData.add("RainFountain");
        mUserAdapter.notifyDataSetChanged();
        if (getArguments() != null) {
            mRoomId = getArguments().getString(ROOM_ID);
            ((BattleFieldActivity) getActivity()).sendClientMessage("|/join " + mRoomId);
        }
        HashMap<String, String> roomLog = NodeConnection.getWithApplicationContext(getActivity().getApplicationContext()).getRoomLog();
        String log = roomLog.get(mRoomId);
        if (log != null) {
            roomLog.remove(mRoomId);
            processServerMessage(log);
        }
    }

    @Override
    public void onDestroy() {
        ((BattleFieldActivity) getActivity()).sendClientMessage("|/leave " + mRoomId);
        super.onDestroy();
    }

    public void processServerMessage(String message) {
        Log.d(CTAG, message);
        String command = message.substring(0, message.indexOf('|'));
        String messageDetails = message.substring(message.indexOf('|') + 1);
        switch (command) {
            case "users":
                int comma = messageDetails.indexOf(',');
                ArrayList<String> userListData;
                if (comma == -1) {
                    userListData = new ArrayList<>();
                } else {
                    int numUsers = Integer.parseInt(messageDetails.substring(0, comma));
                    String users = messageDetails.substring(comma + 2);
                    if (numUsers == 1) {
                        userListData = new ArrayList<>();
                        userListData.add(users);
                    } else {
                        userListData = (ArrayList<String>) Arrays.asList(users.split(", "));
                    }
                }
                mUserListData = userListData;
                mUserAdapter.notifyDataSetChanged();
                break;
        }
    }

    private void formatChatLog(String type, String data) {

    }

    private class UserAdapter extends ArrayAdapter<String> {

        public UserAdapter(Activity getContext, ArrayList<String> userListData) {
            super(getContext, 0, userListData);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.fragment_user_list, null);
            }

            String userName = getItem(position);
            TextView textView = (TextView) convertView.findViewById(R.id.userNameData);
            textView.setText(userName);
            textView.setTextColor(Color.parseColor(COLOR_STRONG[new Random().nextInt(COLOR_STRONG.length)]));
            return convertView;
        }
    }

}
