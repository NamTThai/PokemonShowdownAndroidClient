package com.pokemonshowdown.app;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.pokemonshowdown.data.BattleFieldData;
import com.pokemonshowdown.data.MyApplication;

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

        FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.battle_interface);
        getLayoutInflater(savedInstanceState).inflate(R.layout.fragment_battle_animation, frameLayout);

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
        private String mPlayer1;
        private String mPlayer2;
        private ArrayList<String> mPlayer1Team;
        private ArrayList<String> mPlayer2Team;

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
                mPlayer1 = roomData.getPlayer1();
                mPlayer2 = roomData.getPlayer2();

                ((TextView) getView().findViewById(R.id.battlelog)).setText(roomData.getChatBox());
                final ScrollView scrollView = (ScrollView) getView().findViewById(R.id.battlelog_scrollview);
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });

                ArrayList<String> pendingMessages = roomData.getServerMessageOnHold();
                for (String message : pendingMessages) {
                    processServerMessage(message);
                }

                roomData.setMessageListener(false);
                roomData.setServerMessageOnHold(new ArrayList<String>());
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

        public void processServerMessage(String message) {
            BattleFieldData.RoomData roomData = BattleFieldData.get(getActivity()).getRoomInstance(mRoomId);
            String command = (message.indexOf('|') == -1) ? message : message.substring(0, message.indexOf('|'));
            final String messageDetails = message.substring(message.indexOf('|') + 1);
            if (command.startsWith("-")) {
                processMinorAction(command, messageDetails);
                return;
            }

            int separator = messageDetails.indexOf('|');
            int start;
            String remaining;
            String toAppend;
            StringBuilder toAppendBuilder;
            Spannable toAppendSpannable;
            switch (command) {
                case "init":
                case "title":
                case "join":
                case "j":
                case "J":
                case "leave":
                case "l":
                case "L":
                    break;
                case "chat":
                case "c":
                    String user = messageDetails.substring(0, separator);
                    String userMessage = messageDetails.substring(separator + 1);
                    toAppend = user + ": " + userMessage;
                    toAppendSpannable = new SpannableString(toAppend);
                    toAppendSpannable.setSpan(new ForegroundColorSpan(ChatRoomFragment.getColorStrong(user)), 0, user.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    appendServerMessage(toAppendSpannable);
                    break;
                case "tc":
                case "c:":
                    // String timeStamp = messageDetails.substring(0, separator);
                    String messageDetailsWithStamp = messageDetails.substring(separator + 1);
                    separator = messageDetailsWithStamp.indexOf('|');
                    String userStamp = messageDetailsWithStamp.substring(0, separator);
                    String userMessageStamp = messageDetailsWithStamp.substring(separator + 1);
                    toAppend = userStamp + ": " + userMessageStamp;
                    toAppendSpannable = new SpannableString(toAppend);
                    toAppendSpannable.setSpan(new ForegroundColorSpan(ChatRoomFragment.getColorStrong(userStamp)), 0, userStamp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    appendServerMessage(toAppendSpannable);
                    break;
                case "raw":
                    appendServerMessage(new SpannableString(Html.fromHtml(messageDetails).toString()));
                    break;
                case "message":
                    appendServerMessage(new SpannableString(messageDetails));
                    break;
                case "gametype":
                case "gen":
                    break;
                case "player":
                    String playerType;
                    String playerName;
                    if (separator == -1) {
                        playerType = messageDetails;
                        playerName = "";
                    } else {
                        playerType = messageDetails.substring(0, separator);
                        String playerDetails = messageDetails.substring(separator + 1);
                        separator = playerDetails.indexOf('|');
                        playerName = playerDetails.substring(0, separator);
                    }
                    if (playerType.equals("p1")) {
                        roomData.setPlayer1(playerName);
                        mPlayer1 = playerName;
                    } else {
                        roomData.setPlayer2(playerName);
                        mPlayer2 = playerName;
                    }
                    break;
                case "tier":
                    toAppend = "Format:" + "\n" + messageDetails;
                    toAppendSpannable = new SpannableString(toAppend);
                    toAppendSpannable.setSpan(new StyleSpan(Typeface.BOLD), toAppend.indexOf('\n') + 1, toAppend.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    appendServerMessage(toAppendSpannable);
                    break;
                case "rated":
                    toAppend = command.toUpperCase();
                    toAppendSpannable = new SpannableString(toAppend);
                    toAppendSpannable.setSpan(new ForegroundColorSpan(R.color.dark_blue), 0, toAppend.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    appendServerMessage(toAppendSpannable);
                    break;
                case "rule":
                    toAppendSpannable = new SpannableString(messageDetails);
                    toAppendSpannable.setSpan(new StyleSpan(Typeface.ITALIC), 0, messageDetails.indexOf(':') + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    appendServerMessage(toAppendSpannable);
                    break;
                case "":
                    toAppendSpannable = new SpannableString("");
                    appendServerMessage(toAppendSpannable);
                    break;
                case "clearpoke":
                    mPlayer1Team = new ArrayList<>();
                    mPlayer2Team = new ArrayList<>();
                    break;
                case "poke":
                    playerType = messageDetails.substring(0, separator);
                    String pokeName = messageDetails.substring(separator + 1);
                    if (playerType.equals("p1")) {
                        mPlayer1Team.add(pokeName);
                    } else {
                        mPlayer2Team.add(pokeName);
                    }
                    break;
                case "teampreview":
                    toAppendBuilder = new StringBuilder();
                    toAppendBuilder.append(mPlayer1).append("'s Team: ");
                    for (int i = 0; i < mPlayer1Team.size() - 1; i++) {
                        toAppendBuilder.append(mPlayer1Team.get(i)).append("/");
                    }
                    toAppendBuilder.append(mPlayer1Team.get(mPlayer1Team.size() - 1));
                    toAppendBuilder.append("\n").append(mPlayer2).append("'s Team: ");
                    for (int i = 0; i < mPlayer2Team.size() - 1; i++) {
                        toAppendBuilder.append(mPlayer2Team.get(i)).append("/");
                    }
                    toAppendBuilder.append(mPlayer2Team.get(mPlayer2Team.size() - 1));
                    toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                    appendServerMessage(toAppendSpannable);
                    break;
                case "request":
                    appendServerMessage(new SpannableString(messageDetails));
                    break;
                case "inactive":
                case "inactiveoff":
                    toAppendSpannable = new SpannableString(messageDetails);
                    toAppendSpannable.setSpan(new ForegroundColorSpan(R.color.dark_red), 0, messageDetails.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    appendServerMessage(toAppendSpannable);
                    break;
                case "start":
                    toAppend = roomData.getPlayer1() + " vs. " + roomData.getPlayer2();
                    toAppendSpannable = new SpannableString(toAppend);
                    toAppendSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, toAppend.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    appendServerMessage(toAppendSpannable);
                    break;
                case "move":
                    String attacker = messageDetails.substring(5, separator);
                    remaining = messageDetails.substring(separator + 1);
                    toAppendBuilder = new StringBuilder();
                    if (remaining.startsWith("p2")) {
                        toAppendBuilder.append("The opposing's ");
                    }
                    toAppendBuilder.append(attacker).append(" used ");
                    String move = remaining.substring(0, remaining.indexOf('|'));
                    toAppendBuilder.append(move).append("!");
                    toAppend = toAppendBuilder.toString();
                    start = toAppend.indexOf(move);
                    toAppendSpannable = new SpannableString(toAppend);
                    toAppendSpannable.setSpan(new StyleSpan(Typeface.BOLD), start, start + move.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    appendServerMessage(toAppendSpannable);
                    break;
                case "switch":
                case "drag":
                    toAppendBuilder = new StringBuilder();
                    attacker = messageDetails.substring(5, separator);
                    remaining = messageDetails.substring(separator + 1);
                    separator = remaining.indexOf(',');
                    String species = remaining.substring(0, separator);
                    attacker = (!attacker.equals(species)) ? attacker + " (" + species + ")" : attacker;
                    if (messageDetails.startsWith("p1")) {
                        toAppendBuilder.append("Go! ").append(attacker).append('!');
                    } else {
                        toAppendBuilder.append(mPlayer2).append(" sent out ").append(attacker).append("!");
                    }
                    appendServerMessage(new SpannableStringBuilder(toAppendBuilder));
                    break;
                case "detailschange":
                    break;
                case "faint":
                    attacker = messageDetails.substring(5);
                    toAppendBuilder = new StringBuilder();
                    if (messageDetails.startsWith("p2")) {
                        toAppendBuilder.append("The opposing ");
                    }
                    toAppendBuilder.append(attacker).append(" fainted!");
                    appendServerMessage(new SpannableStringBuilder(toAppendBuilder));
                    break;
                case "turn":
                    toAppend = "TURN " + messageDetails;
                    toAppendSpannable = new SpannableString(toAppend.toUpperCase());
                    toAppendSpannable.setSpan(new UnderlineSpan(), 0, toAppend.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    toAppendSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, toAppend.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    toAppendSpannable.setSpan(new RelativeSizeSpan(1.25f), 0, toAppend.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    toAppendSpannable.setSpan(new ForegroundColorSpan(R.color.dark_blue), 0, toAppend.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    appendServerMessage(toAppendSpannable);
                    break;
                case "win":
                    toAppend = messageDetails + " has won the battle!";
                    appendServerMessage(new SpannableString(toAppend));
                    break;
                default:
                    appendServerMessage(new SpannableString(message));
            }
        }

        private void processMinorAction(String command, String messageDetails) {
            if (messageDetails.contains("[silent]")) {
                return;
            }

            int separator;
            int start;
            String remaining;
            String toAppend;
            StringBuilder toAppendBuilder = new StringBuilder();
            Spannable toAppendSpannable;

            String fromEffect;
            String ofSource;
            int from = messageDetails.indexOf("[from]");
            if (from != -1) {
                remaining = messageDetails.substring(from + 7);
                separator = remaining.indexOf('|');
                fromEffect = (separator == -1) ? remaining : remaining.substring(0, separator);
            }
            int of = messageDetails.indexOf("[of]");
            if (of != -1) {
                remaining = messageDetails.substring(of + 5);
                separator = remaining.indexOf('|');
                ofSource = (separator == -1) ? remaining : remaining.substring(0, separator);
            }

            separator = messageDetails.indexOf('|');
            switch (command) {
                case "message":
                    toAppendSpannable = new SpannableString(messageDetails);
                    break;
                case "-miss":
                    String attacker = messageDetails.substring(5, separator);
                    if (messageDetails.startsWith("p2")) {
                        toAppendBuilder.append("The opposing ");
                    }
                    toAppendBuilder.append(attacker);
                    toAppendBuilder.append(" missed the target");
                    toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                    break;
                case "-fail":
                    attacker = messageDetails.substring(5, separator);
                    String action = messageDetails.substring(separator + 1);
                    toAppend = action + " failed against " + attacker;
                    toAppendSpannable = new SpannableString(toAppend);
                    break;
                case "-damage":
                    attacker = messageDetails.substring(5, separator);
                    if (messageDetails.startsWith("p2")) {
                        toAppendBuilder.append("The opposing ");
                    }
                    toAppendBuilder.append(attacker);
                    remaining = messageDetails.substring(separator + 1);
                    toAppendBuilder.append(" has lost ").append(remaining);
                    toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                    /*
                    separator = remaining.indexOf(" ");
                    String hp = remaining.substring(0, separator);
                    if (hp.equals("0")) {
                        toAppendBuilder.append("has fainted!");
                    }*/
                    break;
                case "-heal":
                    attacker = messageDetails.substring(5, separator);
                    if (messageDetails.startsWith("p2")) {
                        toAppendBuilder.append("The opposing ");
                    }
                    toAppendBuilder.append(attacker);
                    remaining = messageDetails.substring(separator + 1);
                    toAppendBuilder.append(" healed ").append(remaining);
                    toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                    /*
                    separator = remaining.indexOf(" ");
                    String hp = remaining.substring(0, separator);
                    if (hp.equals("0")) {
                        toAppendBuilder.append("has fainted!");
                    }*/
                    break;
                case "-status":
                    attacker = messageDetails.substring(5, separator);
                    if (messageDetails.startsWith("p2")) {
                        toAppendBuilder.append("The opposing ");
                    }
                    toAppendBuilder.append(attacker);
                    remaining = messageDetails.substring(separator + 1);
                    toAppendBuilder.append(" was inflicted with ").append(remaining);
                    toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                    /*
                    separator = remaining.indexOf(" ");
                    String hp = remaining.substring(0, separator);
                    if (hp.equals("0")) {
                        toAppendBuilder.append("has fainted!");
                    }*/
                    break;
                case "-curestatus":
                    attacker = messageDetails.substring(5, separator);
                    if (messageDetails.startsWith("p2")) {
                        toAppendBuilder.append("The opposing ");
                    }
                    toAppendBuilder.append(attacker);
                    remaining = messageDetails.substring(separator + 1);
                    toAppendBuilder.append(" was cured from ").append(remaining);
                    toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                    /*
                    separator = remaining.indexOf(" ");
                    String hp = remaining.substring(0, separator);
                    if (hp.equals("0")) {
                        toAppendBuilder.append("has fainted!");
                    }*/
                    break;
                case "-cureteam":
                    attacker = messageDetails.substring(5, separator);
                    if (messageDetails.startsWith("p2")) {
                        toAppendBuilder.append("The opposing ");
                    }
                    toAppendBuilder.append(attacker);
                    toAppendBuilder.append(" cured the whole team from bad status");
                    toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                    /*
                    separator = remaining.indexOf(" ");
                    String hp = remaining.substring(0, separator);
                    if (hp.equals("0")) {
                        toAppendBuilder.append("has fainted!");
                    }*/
                    break;
                case "-boost":
                    attacker = messageDetails.substring(5, separator);
                    if (messageDetails.startsWith("p2")) {
                        toAppendBuilder.append("The opposing ");
                    }
                    toAppendBuilder.append(attacker);
                    remaining = messageDetails.substring(separator + 1);
                    toAppendBuilder.append(" has boosted ");
                    separator = remaining.indexOf('|');
                    String stat = remaining.substring(0, separator);
                    String amount = remaining.substring(separator + 1);
                    toAppendBuilder.append(stat).append(" by ").append(amount);
                    toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                    break;
                case "-unboost":
                    attacker = messageDetails.substring(5, separator);
                    if (messageDetails.startsWith("p2")) {
                        toAppendBuilder.append("The opposing ");
                    }
                    toAppendBuilder.append(attacker);
                    remaining = messageDetails.substring(separator + 1);
                    toAppendBuilder.append(" has reduced ");
                    separator = remaining.indexOf('|');
                    stat = remaining.substring(0, separator);
                    amount = remaining.substring(separator + 1);
                    toAppendBuilder.append(stat).append(" by ").append(amount);
                    toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                    break;
                case "-weather":
                    toAppend = "Weather changed to " + messageDetails;
                    toAppendSpannable = new SpannableString(toAppend);
                    break;
                case "-crit":
                    toAppendSpannable = new SpannableString("It's a critical hit!");
                    break;
                case "-supereffective":
                    toAppendSpannable = new SpannableString("It's super effective!");
                    break;
                case "-resisted":
                    toAppendSpannable = new SpannableString("It's not very effective");
                    break;
                case "-immune":
                    attacker = messageDetails.substring(5);
                    toAppend = attacker + " is immuned";
                    toAppendSpannable = new SpannableString(toAppend);
                    break;
                case "-item":
                    attacker = messageDetails.substring(5, separator);
                    remaining = messageDetails.substring(separator + 1);
                    toAppend = attacker + " revealed its " + remaining;
                    toAppendSpannable = new SpannableString(toAppend);
                    break;
                case "-enditem":
                    attacker = messageDetails.substring(5, separator);
                    remaining = messageDetails.substring(separator + 1);
                    toAppend = attacker + " has lost its " + remaining;
                    toAppendSpannable = new SpannableString(toAppend);
                    break;
                case "-ability":
                    attacker = messageDetails.substring(5, separator);
                    remaining = messageDetails.substring(separator + 1);
                    toAppend = attacker + " revealed its ability " + remaining;
                    toAppendSpannable = new SpannableString(toAppend);
                    break;
                case "-endability":
                    attacker = messageDetails.substring(5);
                    toAppend = attacker + " lost its ability";
                    toAppendSpannable = new SpannableString(toAppend);
                    break;
                case "-transform":
                    attacker = messageDetails.substring(5, separator);
                    remaining = messageDetails.substring(separator + 1);
                    toAppend = attacker + " transformed into " + remaining;
                    toAppendSpannable = new SpannableString(toAppend);
                    break;
                case "-activate":
                    toAppend = messageDetails + " has activated";
                    toAppendSpannable = new SpannableString(toAppend);
                    break;
                default:
                    toAppendSpannable = new SpannableString(command + ":" + messageDetails);
            }
            toAppendSpannable.setSpan(new RelativeSizeSpan(0.8f), 0, toAppendSpannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            appendServerMessage(toAppendSpannable);
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

}
