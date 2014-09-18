package com.pokemonshowdown.app;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.pokemonshowdown.data.CommunityLoungeData;
import com.pokemonshowdown.data.MyApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class ChatRoomFragment extends android.support.v4.app.Fragment {
    public final static String CTAG = ChatRoomFragment.class.getName();
    private final static String ROOM_ID = "Room Id";
    public final static String[] COLOR_STRONG = {"#0099CC", "#9933CC", "#669900", "#FF8800", "#CC0000"};
    public final static String[] COLOR_WEAK = {"#33B5E5", "#AA66CC", "#99CC00", "#FFBB33", "#FF4444"};

    private String mRoomId;

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
        mUserAdapter = new UserAdapter(getActivity(), mUserListData);
        ListView listView = (ListView) view.findViewById(R.id.user_list);
        listView.setAdapter(mUserAdapter);

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
        roomDataHashMap.put(mRoomId, new CommunityLoungeData.RoomData(mRoomId, mUserListData, text));
    }

    @Override
    public void onResume() {
        super.onResume();
        HashMap<String, CommunityLoungeData.RoomData> roomDataHashMap = CommunityLoungeData.get(getActivity()).getRoomDataHashMap();
        CommunityLoungeData.RoomData roomData = roomDataHashMap.get(mRoomId);
        if (roomData != null) {
            mUserListData = roomData.getUserListData();
            mUserAdapter = new UserAdapter(getActivity(), mUserListData);
            ListView listView = (ListView) getView().findViewById(R.id.user_list);
            listView.setAdapter(mUserAdapter);

            ((TextView) getView().findViewById(R.id.community_chat_log)).setText(roomData.getChatBox());

            ArrayList<String> pendingMessages = roomData.getServerMessageOnHold();
            for (String message : pendingMessages) {
                processServerMessage(message);
            }

            roomDataHashMap.remove(mRoomId);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CommunityLoungeData.get(getActivity()).getRoomDataHashMap().remove(mRoomId);
    }

    public void processServerMessage(String message) {
        if (message.indexOf('|') == -1) {
            appendUserMessage("", message);
            return;
        }
        if (message.charAt(0) == '|' && message.charAt(1) == '|') {
            appendUserMessage("", message.substring(2));
            return;
        }
        String command = message.substring(0, message.indexOf('|'));
        final String messageDetails = message.substring(message.indexOf('|') + 1);
        int separator;
        switch (command) {
            case "init":
                break;
            case "title":
                break;
            case "users":
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mUserListData = new ArrayList<>();
                        int comma = messageDetails.indexOf(',');
                        if (comma != -1) {
                            String users = messageDetails.substring(comma + 1);
                            comma = users.indexOf(',');
                            while (comma != -1) {
                                mUserListData.add(users.substring(0, comma));
                                users = users.substring(comma + 1);
                                comma = users.indexOf(',');
                            }
                            mUserListData.add(users);
                        }
                        mUserAdapter = new UserAdapter(getActivity(), mUserListData);
                        View view = getView();
                        if (view != null) {
                            ((ListView) view.findViewById(R.id.user_list)).setAdapter(mUserAdapter);
                        }
                    }
                });
                break;
            case "join":
            case "j":
            case "J":
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        removeUserFromList(mUserListData, messageDetails);
                        mUserListData.add(messageDetails);
                        mUserAdapter.notifyDataSetChanged();
                    }
                });
                break;
            case "leave":
            case "l":
            case "L":
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        removeUserFromList(mUserListData, messageDetails);
                        mUserAdapter.notifyDataSetChanged();
                    }
                });
                break;
            case "name":
            case "n":
            case "N":
                separator = messageDetails.indexOf('|');
                final String oldName = messageDetails.substring(0, separator);
                final String newName = messageDetails.substring(separator + 1);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        removeUserFromList(mUserListData, oldName);
                        mUserListData.add(newName);
                        mUserAdapter.notifyDataSetChanged();
                    }
                });
                break;
            case "battle":
            case "b":
            case "B":
                break;
            case "chat":
            case "c":
                separator = messageDetails.indexOf('|');
                String user = messageDetails.substring(0, separator);
                String userMessage = messageDetails.substring(separator + 1);
                appendUserMessage(user, userMessage);
                break;
            case "tc":
            case "c:":
                separator = messageDetails.indexOf('|');
                // String timeStamp = messageDetails.substring(0, separator);
                String messageDetailsWithStamp = messageDetails.substring(separator + 1);
                separator = messageDetailsWithStamp.indexOf('|');
                String userStamp = messageDetailsWithStamp.substring(0, separator);
                String userMessageStamp = messageDetailsWithStamp.substring(separator + 1);
                appendUserMessage(userStamp, userMessageStamp);
                break;
            case "raw":
                appendUserMessage("YOUR BELOVED SERVER", Html.fromHtml(messageDetails).toString());
                break;
            default:
                Log.d(CTAG, message);
                appendUserMessage(command, messageDetails);
        }
    }

    private void appendUserMessage(String user, String message) {
        if (getView() != null) {
            final TextView chatlog = (TextView) getView().findViewById(R.id.community_chat_log);
            final Spannable userS = new SpannableString(user + ": ");
            userS.setSpan(new ForegroundColorSpan(Color.parseColor(COLOR_STRONG[new Random().nextInt(COLOR_STRONG.length)])),
                    0, userS.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            final String messageF = message;

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    chatlog.append(userS);
                    chatlog.append(messageF);
                    chatlog.append("\n");
                }
            });

            ScrollView scrollView = (ScrollView) getView().findViewById(R.id.chatroom_scrollview);
            scrollView.fullScroll(View.FOCUS_DOWN);
        }
    }

    private void removeUserFromList(ArrayList<String> userList, String username) {
        for(int i=0; i < userList.size(); i++) {
            String user = sanitizeUsername(userList.get(i));
            username = sanitizeUsername(username);
            if (user.equals(username)) {
                userList.remove(i);
                return;
            }
        }
    }

    /**
     * Trim everything except for letter and number
     */
    public static String sanitizeUsername(String user) {
        return user.toLowerCase().replaceAll("[^a-z0-9]", "");
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
