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
import android.widget.ScrollView;
import android.widget.TextView;

import com.pokemonshowdown.data.BattleFieldData;
import com.pokemonshowdown.data.MyApplication;

import java.util.ArrayList;
import java.util.HashMap;

public class BattleLogDialog extends DialogFragment {
    public static final String BTAG = BattleLogDialog.class.getName();
    private String mRoomId;
    private String mPlayer1;
    private String mPlayer2;
    private HashMap<String, Integer> mPlayer1Team = new HashMap<>();
    private HashMap<String, Integer> mPlayer2Team = new HashMap<>();

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
                mPlayer1Team = new HashMap<>();
                mPlayer2Team = new HashMap<>();
                break;
            case "poke":
                playerType = messageDetails.substring(0, separator);
                String pokeName = messageDetails.substring(separator + 1);
                if (playerType.equals("p1")) {
                    mPlayer1Team.put(pokeName, 100);
                } else {
                    mPlayer2Team.put(pokeName, 100);
                }
                break;
            case "teampreview":
                toAppendBuilder = new StringBuilder();
                toAppendBuilder.append(mPlayer1).append("'s Team: ");
                String[] p1Team = mPlayer1Team.keySet().toArray(new String[mPlayer1Team.size()]);
                for (int i = 0; i < p1Team.length - 1; i++) {
                    toAppendBuilder.append(p1Team[i]).append("/");
                }
                toAppendBuilder.append(p1Team[p1Team.length - 1]);

                toAppendBuilder.append("\n").append(mPlayer2).append("'s Team: ");
                String[] p2Team = mPlayer1Team.keySet().toArray(new String[mPlayer2Team.size()]);
                for (int i = 0; i < p2Team.length - 1; i++) {
                    toAppendBuilder.append(p2Team[i]).append("/");
                }
                toAppendBuilder.append(p2Team[p2Team.length - 1]);
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
                //TODO need to handle roar & cie
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
            case "cant":
                //todo (cant attack bec frozen/para etc)
                break;
            default:
                appendServerMessage(new SpannableString(message));
                break;
        }
    }

    private void processMinorAction(String command, String messageDetails) {
        if (messageDetails.contains("[silent]")) {
            return;
        }

        int separator;
        int start;
        Integer oldHP;
        int lostHP;
        int intAmount;
        String remaining;
        String toAppend;
        StringBuilder toAppendBuilder = new StringBuilder();
        Spannable toAppendSpannable;
        String move;

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
                toAppend = "But it failed!";
                toAppendSpannable = new SpannableString(toAppend);
                break;
            case "-damage":
                attacker = messageDetails.substring(5, separator);
                if (messageDetails.startsWith("p2")) {
                    toAppendBuilder.append("The opposing ");
                    oldHP = mPlayer2Team.get(attacker);
                    if (oldHP == null) {
                        mPlayer2Team.put(attacker, 100);
                        oldHP = mPlayer2Team.get(attacker);
                    }
                } else {
                    oldHP = mPlayer1Team.get(attacker);
                    if (oldHP == null) {
                        mPlayer1Team.put(attacker, 100);
                        oldHP = mPlayer1Team.get(attacker);
                    }

                }
                toAppendBuilder.append(attacker + " lost ");
                remaining = messageDetails.substring(separator + 1);
                separator = remaining.indexOf("/");
                if (separator == -1) { // fainted
                    intAmount = 0;
                } else {
                    String hp = remaining.substring(0, separator);
                    intAmount = Integer.parseInt(hp);
                }
                lostHP = oldHP - intAmount;
                toAppendBuilder.append(lostHP + "% of its health!");
                if (messageDetails.startsWith("p2")) {
                    mPlayer2Team.put(attacker, intAmount);
                } else {
                    mPlayer1Team.put(attacker, intAmount);
                }

                toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                break;
            case "-heal":
                attacker = messageDetails.substring(5, separator);
                if (messageDetails.startsWith("p2")) {
                    toAppendBuilder.append("The opposing ");
                    oldHP = mPlayer2Team.get(attacker);
                    if (oldHP == null) {
                        // in randbats , we dont get the pokemon list
                        mPlayer2Team.put(attacker, 100);
                        oldHP = mPlayer2Team.get(attacker);
                    }
                } else {
                    oldHP = mPlayer1Team.get(attacker);
                    if (oldHP == null) {
                        // in randbats , we dont get the pokemon list
                        mPlayer1Team.put(attacker, 100);
                        oldHP = mPlayer1Team.get(attacker);
                    }
                }
                toAppendBuilder.append(attacker);
                remaining = messageDetails.substring(separator + 1);
                separator = remaining.indexOf("/");
                if (separator == -1) {
                    intAmount = 0; // shouldnt happen sicne we're healing
                } else {
                    String hp = remaining.substring(0, separator);
                    intAmount = Integer.parseInt(hp);
                }
                lostHP = intAmount - oldHP;
                toAppendBuilder.append(" healed " + lostHP + "% of it's health!");
                if (messageDetails.startsWith("p2")) {
                    mPlayer2Team.put(attacker, intAmount);
                } else {
                    mPlayer1Team.put(attacker, intAmount);
                }

                toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
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
                toAppendBuilder.append("'s ");
                separator = remaining.indexOf('|');
                String stat = remaining.substring(0, separator);
                switch (stat) {
                    case "atk":
                        toAppendBuilder.append("Attack ");
                        break;
                    case "def":
                        toAppendBuilder.append("Defense ");
                        break;
                    case "spa":
                        toAppendBuilder.append("Special Attack ");
                        break;
                    case "spd":
                        toAppendBuilder.append("Special Defense ");
                        break;
                    case "spe":
                        toAppendBuilder.append("Speed ");
                        break;
                    default:
                        toAppendBuilder.append(stat + " ");
                        break;
                }
                String amount = remaining.substring(separator + 1);
                if (amount.indexOf("|") != -1) {
                    amount = amount.substring(0, amount.indexOf("|"));
                }
                intAmount = Integer.parseInt(amount);
                if (intAmount >= 2) {
                    toAppendBuilder.append("sharply ");
                }
                toAppendBuilder.append("rose!");
                toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                break;
            case "-unboost":
                attacker = messageDetails.substring(5, separator);
                if (messageDetails.startsWith("p2")) {
                    toAppendBuilder.append("The opposing ");
                }
                toAppendBuilder.append(attacker);
                remaining = messageDetails.substring(separator + 1);
                toAppendBuilder.append("'s ");
                separator = remaining.indexOf('|');
                stat = remaining.substring(0, separator);
                switch (stat) {
                    case "atk":
                        toAppendBuilder.append("Attack ");
                        break;
                    case "def":
                        toAppendBuilder.append("Defense ");
                        break;
                    case "spa":
                        toAppendBuilder.append("Special Attack ");
                        break;
                    case "spd":
                        toAppendBuilder.append("Special Defense ");
                        break;
                    case "spe":
                        toAppendBuilder.append("Speed ");
                        break;
                    default:
                        toAppendBuilder.append(stat + " ");
                        break;
                }
                amount = remaining.substring(separator + 1);
                if (amount.indexOf("|") != -1) {
                    amount = amount.substring(0, amount.indexOf("|"));
                }
                toAppendBuilder.append("fell");
                intAmount = Integer.parseInt(amount);
                if (intAmount >= 2) {
                    toAppendBuilder.append(" hashly");
                }
                toAppendBuilder.append("!");
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
                toAppendSpannable = new SpannableString("It's not very effective...");
                break;
            case "-immune":
                attacker = messageDetails.substring(5);
                if (attacker.indexOf("|") != -1) {
                    attacker = attacker.substring(0, attacker.indexOf("|"));
                }
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
            case "-sidestart":
                //reflect, rocks, spikes, light screen, toxic spikes
                // TODO check leech seed maybe?
                messageDetails = messageDetails.substring(messageDetails.indexOf('|'));
                if (messageDetails.indexOf("Stealth Rock") != -1) {
                    toAppendBuilder.append("Pointed stones float in the air around ");
                } else if (messageDetails.indexOf("Toxic Spikes") != -1) {
                    toAppendBuilder.append("Toxic spikes were scattered all around the feet of ");
                } else if (messageDetails.indexOf("Spikes") != -1) {
                    toAppendBuilder.append("Spikes were scattered all around the feet of ");
                } else if (messageDetails.indexOf("Reflect") != -1) {
                    toAppendBuilder.append("A protective veil augments the Defense of ");
                } else if (messageDetails.indexOf("Light Screen") != -1) {
                    toAppendBuilder.append("A protective veil augments the Special Defense of ");
                } else if (messageDetails.indexOf("Sticky Web") != -1) {
                    toAppendBuilder.append("A sticky web spreads out beneath ");
                }

                if (messageDetails.startsWith("p2")) {
                    toAppendBuilder.append("the opposing team!");
                } else {
                    toAppendBuilder.append("your team!");
                }
                toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-sideend":
                // todo
                toAppendSpannable = new SpannableString(command + ":" + messageDetails);
                break;

            case "-hitcount":
                try {
                    String hitCountS = messageDetails.substring(messageDetails.lastIndexOf(separator) + 1);
                    int hitCount = Integer.parseInt(hitCountS);
                    toAppendBuilder.append("Hit " + hitCount + "time");
                    if (hitCount > 1) {
                        toAppendBuilder.append("s");
                    }
                    toAppendBuilder.append("!");
                    toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                } catch (NumberFormatException e) {
                    // todo handle
                    toAppendSpannable = new SpannableString(command + ":" + messageDetails);
                }
                break;

            case "-singleturn":
                //todo proctect apparently
                toAppendSpannable = new SpannableString(command + ":" + messageDetails);
                break;

            case "-fieldstart":
                //todo (trick room, maybe more)
                toAppendSpannable = new SpannableString(command + ":" + messageDetails);
                break;

            case "-fieldend":
                //todo (trick room, maybe more)
                toAppendSpannable = new SpannableString(command + ":" + messageDetails);
                break;

            case "-start":
                //todo substitute,yawn,taunt,flashfire
                toAppendSpannable = new SpannableString(command + ":" + messageDetails);
                break;

            case "-end":
                //todo substitute,yawn,taunt
                toAppendSpannable = new SpannableString(command + ":" + messageDetails);
                break;

            case "-message":
                toAppendSpannable = new SpannableString(messageDetails);
                break;

            default:
                toAppendSpannable = new SpannableString(command + ":" + messageDetails);
                break;
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