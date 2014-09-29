package com.pokemonshowdown.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.pokemonshowdown.data.CommunityLoungeData;
import com.pokemonshowdown.data.MyApplication;

import java.util.ArrayList;
import java.util.HashMap;

public class WatchBattleFragment extends android.support.v4.app.Fragment {
    public final static String WTAG = WatchBattleFragment.class.getName();
    private final static String ROOM_ID = "Room Id";

    private String mRoomId;

    public static WatchBattleFragment newInstance(String roomId) {
        WatchBattleFragment fragment = new WatchBattleFragment();
        Bundle args = new Bundle();
        args.putString(ROOM_ID, roomId);
        fragment.setArguments(args);
        return fragment;
    }
    public WatchBattleFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat_room, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            mRoomId = getArguments().getString(ROOM_ID);
        }

        final EditText chatBox = (EditText) view.findViewById(R.id.community_chat_box);
        chatBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String message = chatBox.getText().toString();
                    message = (mRoomId.equals("lobby")) ? ("|" + message) : (mRoomId + "|" + message);
                    if (MyApplication.getMyApplication().verifySignedInBeforeSendingMessage()) {
                        MyApplication.getMyApplication().sendClientMessage(message);
                    }
                    chatBox.setText(null);
                    return false;
                }
                return false;
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        HashMap<String, CommunityLoungeData.RoomData> roomDataHashMap = CommunityLoungeData.get(getActivity()).getRoomDataHashMap();
        CharSequence text = ((TextView) getView().findViewById(R.id.community_chat_log)).getText();
    }

    @Override
    public void onResume() {
        super.onResume();
        HashMap<String, CommunityLoungeData.RoomData> roomDataHashMap = CommunityLoungeData.get(getActivity()).getRoomDataHashMap();
        CommunityLoungeData.RoomData roomData = roomDataHashMap.get(mRoomId);
        if (roomData != null) {

            ((TextView) getView().findViewById(R.id.community_chat_log)).setText(roomData.getChatBox());

            ArrayList<String> pendingMessages = roomData.getServerMessageOnHold();
            for (String message : pendingMessages) {
                //processServerMessage(message);
            }

            roomDataHashMap.remove(mRoomId);
        }
    }

}
