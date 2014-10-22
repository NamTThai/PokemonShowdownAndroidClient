package com.pokemonshowdown.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Spannable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.pokemonshowdown.data.BattleFieldData;
import com.pokemonshowdown.data.MyApplication;

import java.util.ArrayList;

public class BattleLogDialog extends DialogFragment {
    public static final String BTAG = BattleLogDialog.class.getName();
    private String mRoomId;

    public static BattleLogDialog newInstance(String roomId) {
        BattleLogDialog fragment = new BattleLogDialog();
        Bundle args = new Bundle();
        args.putString(BattleFragment.ROOM_ID, roomId);
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
            mRoomId = getArguments().getString(BattleFragment.ROOM_ID);
        }

        final EditText chatBox = (EditText) view.findViewById(R.id.battle_chat_box);
        chatBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String message = chatBox.getText().toString();
                    message = mRoomId + "|" + message;
                    if (MyApplication.getMyApplication().verifySignedInBeforeSendingMessage()) {
                        MyApplication.getMyApplication().sendClientMessage(message);
                    }
                    chatBox.setText(null);
                    return false;
                }
                return false;
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        BattleFieldData.RoomData roomData = BattleFieldData.get(getActivity()).getRoomDataHashMap().get(mRoomId);
        if (roomData != null) {
            ((TextView) getView().findViewById(R.id.battlelog)).setText(roomData.getChatBox());
            final ScrollView scrollView = (ScrollView) getView().findViewById(R.id.battlelog_scrollview);
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.fullScroll(View.FOCUS_DOWN);
                }
            });

            ArrayList<Spannable> pendingMessages = roomData.getServerMessageOnHold();
            for (Spannable message : pendingMessages) {
                processServerMessage(message);
            }

            roomData.setMessageListener(false);
            roomData.setServerMessageOnHold(new ArrayList<Spannable>());
        }
    }

    @Override
    public void onPause() {
        BattleFieldData.RoomData roomData = BattleFieldData.get(getActivity()).getRoomInstance(mRoomId);
        if (roomData != null) {
            roomData.setMessageListener(true);
            CharSequence text = ((TextView) getView().findViewById(R.id.battlelog)).getText();
            roomData.setChatBox(text);
        }
        super.onPause();
    }

    public void processServerMessage(Spannable message) {
        appendServerMessage(message);
    }

    private void appendServerMessage(final Spannable message) {
        if (getView() != null) {
            final TextView chatlog = (TextView) getView().findViewById(R.id.battlelog);

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    chatlog.append(message);
                    chatlog.append("\n");

                    final ScrollView scrollView = (ScrollView) getView().findViewById(R.id.battlelog_scrollview);
                    scrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.fullScroll(View.FOCUS_DOWN);
                        }
                    });
                }
            });
        }
    }
}