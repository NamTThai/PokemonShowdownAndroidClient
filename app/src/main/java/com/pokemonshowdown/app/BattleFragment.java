package com.pokemonshowdown.app;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.pokemonshowdown.data.BattleFieldData;

import java.util.ArrayList;

public class BattleFragment extends android.support.v4.app.Fragment {
    public final static String BTAG = BattleFragment.class.getName();
    private final static String ROOM_ID = "Room Id";

    private String mRoomId;

    public static BattleFragment newInstance(String roomId) {
        BattleFragment fragment = new BattleFragment();
        Bundle args = new Bundle();
        args.putString(ROOM_ID, roomId);
        fragment.setArguments(args);
        return fragment;
    }
    public BattleFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_battle, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            mRoomId = getArguments().getString(ROOM_ID);
        }

        view.findViewById(R.id.battlelog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogFragment = BattleLogDialog.newInstance(mRoomId);
                dialogFragment.show(getActivity().getSupportFragmentManager(), mRoomId);
            }
        });
    }

    public static class BattleLogDialog extends DialogFragment {
        public static final String BTAG = BattleLogDialog.class.getName();
        private String mRoomId;

        public static BattleLogDialog newInstance(String roomId) {
            BattleLogDialog fragment = new BattleLogDialog();
            Bundle args = new Bundle();
            args.putString(ROOM_ID, roomId);
            fragment.setArguments(args);
            return fragment;
        }
        public BattleLogDialog() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            View view = inflater.inflate(R.layout.dialog_battlelog, container);

            if (getArguments() != null) {
                mRoomId = getArguments().getString(ROOM_ID);
            }

            BattleFieldData.RoomData roomData = BattleFieldData.get(getActivity()).getRoomDataHashMap().get(mRoomId);
            if (roomData != null) {
                ((TextView) view.findViewById(R.id.battlelog)).setText(roomData.getChatBox());

                ArrayList<String> pendingMessages = roomData.getServerMessageOnHold();
                for (String message : pendingMessages) {
                    processServerMessage(message);
                }

                roomData.setMessageListener(false);
            }

            return view;
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            BattleFieldData.RoomData roomData = BattleFieldData.get(getActivity()).getRoomDataHashMap().get(mRoomId);
            roomData.setMessageListener(true);
            CharSequence text = ((TextView) getView().findViewById(R.id.battlelog)).getText();
            roomData.setChatBox(text);
            super.onDismiss(dialog);
        }

        public void processServerMessage(String message) {
            Log.d(BTAG, message);
        }
    }

}
