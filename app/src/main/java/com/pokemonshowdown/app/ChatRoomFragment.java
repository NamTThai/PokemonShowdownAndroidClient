package com.pokemonshowdown.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.pokemonshowdown.data.NodeConnection;

public class ChatRoomFragment extends android.support.v4.app.Fragment {
    private final static String CTAG = "ChatRoomFragment";
    private final static String ROOM_NAME = "Room Name";
    private final static String ROOM_ID = "Room Id";

    private String mRoomName;
    private String mRoomId;
    private ListView mUserList;
    private TextView mChatLog;

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
        if (getArguments() != null) {
            mRoomId = getArguments().getString(ROOM_ID);
            NodeConnection.getWithApplicationContext(getActivity().getApplicationContext()).getWebSocketClient().send("|/join " + mRoomId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat_room, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void processServerMessage(String message) {

    }

    private void formatChatLog(String type, String data) {

    }

    private void formatUserList(String type, String data) {

    }

}
