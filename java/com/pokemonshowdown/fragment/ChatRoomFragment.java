package com.pokemonshowdown.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.pokemonshowdown.R;
import com.pokemonshowdown.application.MyApplication;
import com.pokemonshowdown.data.CommunityLoungeData;
import com.pokemonshowdown.data.Onboarding;
import com.pokemonshowdown.dialog.ChallengeDialog;
import com.pokemonshowdown.dialog.OnboardingDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class ChatRoomFragment extends android.support.v4.app.Fragment {
    public final static String CTAG = ChatRoomFragment.class.getName();
    public final static String[] COLOR_STRONG = {"#0099CC", "#9933CC", "#669900", "#FF8800", "#CC0000"};
    public final static String[] COLOR_WEAK = {"#33B5E5", "#AA66CC", "#99CC00", "#FFBB33", "#FF4444"};
    public final static String USER_PRIORITY = "~#@%+ ";
    private final static String ROOM_ID = "Room Id";
    private String mRoomId;
    private boolean isFABVisible = true;

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

    public static int getColorStrong(String name) {
        if (name.length() < 2) {
            return Color.parseColor(COLOR_STRONG[0]);
        }
        int value = (int) name.charAt(1) + (int) name.charAt(name.length() - 1);
        return Color.parseColor(COLOR_STRONG[value % COLOR_STRONG.length]);
    }

    /**
     * Trim everything except for letter and number
     */
    public static String sanitizeUsername(String user) {
        return user.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mLayoutInflater = inflater;
        return inflater.inflate(R.layout.fragment_chat_room, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUserListData = new ArrayList<>();
        mUserAdapter = new UserAdapter(getActivity(), mUserListData);
        final ListView listView = (ListView) view.findViewById(R.id.user_list);
        listView.setAdapter(mUserAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String userName = (String) listView.getItemAtPosition(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatRoomFragment.this.getActivity());
                builder.setTitle(userName);
                builder.setItems(getResources().getStringArray(R.array.actions_user_chatroom),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                if (item == 0) { // challenge
                                    //first need to check if the user is logged in
                                    Onboarding onboarding = Onboarding.get(getActivity().getApplicationContext());
                                    if (!onboarding.isSignedIn()) {
                                        FragmentManager fm = getActivity().getSupportFragmentManager();
                                        OnboardingDialog fragment = new OnboardingDialog();
                                        fragment.show(fm, OnboardingDialog.OTAG);
                                        return;
                                    }

                                    ChallengeDialog cd = ChallengeDialog.newInstance(userName, null);
                                    cd.show(ChatRoomFragment.this.getActivity().getSupportFragmentManager(), userName);
                                }
                            }
                        }

                );
                builder.show();
            }
        });

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            final FloatingActionButton usersFab = (FloatingActionButton) view.findViewById(R.id.users_button);
            usersFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listView.getVisibility() == View.VISIBLE) {
                        listView.setVisibility(View.GONE);

                        usersFab.animate().translationX(0f).start();
                    } else {
                        listView.setVisibility(View.VISIBLE);

                        usersFab.animate().translationX(520f).start();
                    }
                }
            });
        }

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
    public void onPause() {
        super.onPause();
        HashMap<String, CommunityLoungeData.RoomData> roomDataHashMap = CommunityLoungeData.get(getActivity()).getRoomDataHashMap();
        CharSequence text = ((TextView) getView().findViewById(R.id.community_chat_log)).getText();
        roomDataHashMap.put(mRoomId, new CommunityLoungeData.RoomData(mRoomId, mUserListData, text, true));
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
                        Collections.sort(mUserListData, new Comparator<String>() {
                            @Override
                            public int compare(String lhs, String rhs) {
                                return compareUser(lhs, rhs);
                            }
                        });
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
                        addUserToList(mUserListData, messageDetails);
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
                        addUserToList(mUserListData, newName);
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
            userS.setSpan(new ForegroundColorSpan(getColorStrong(user)),
                    0, userS.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            final String messageF = message;

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    chatlog.append(userS);
                    chatlog.append(messageF);
                    chatlog.append("\n");

                    final ScrollView scrollView = (ScrollView) getView().findViewById(R.id.chatroom_scrollview);
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

    /**
     * @return 1 if a should appear after b
     */
    private int compareUser(String a, String b) {
        char a1 = a.charAt(0);
        int a1pos = USER_PRIORITY.indexOf(a1);
        a1pos = (a1pos == -1) ? USER_PRIORITY.length() : a1pos;
        char b1 = b.charAt(0);
        int b1pos = USER_PRIORITY.indexOf(b1);
        b1pos = (b1pos == -1) ? USER_PRIORITY.length() : b1pos;
        if (a1pos != b1pos) {
            return (a1pos > b1pos) ? 1 : -1;
        } else {
            return sanitizeUsername(a).compareTo(sanitizeUsername(b));
        }
    }

    private void removeUserFromList(ArrayList<String> userList, String username) {
        username = sanitizeUsername(username);
        for (int i = 0; i < userList.size(); i++) {
            String user = sanitizeUsername(userList.get(i));
            if (user.equals(username)) {
                userList.remove(i);
                return;
            }
        }
    }

    private void addUserToList(ArrayList<String> userList, String username) {
        for (int i = 0; i < userList.size(); i++) {
            int compare = compareUser(username, userList.get(i));
            if (compare == 0) {
                return;
            }
            if (compare < 0) {
                userList.add(i, username);
                return;
            }
        }
        userList.add(username);
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
            textView.setTextColor(getColorStrong(userName));
            return convertView;
        }
    }

}
