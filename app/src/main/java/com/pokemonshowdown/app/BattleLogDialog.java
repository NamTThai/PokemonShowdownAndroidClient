package com.pokemonshowdown.app;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.*;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.*;
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
    private boolean weatherExist;
    private String currentWeather;
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
                String[] p2Team = mPlayer2Team.keySet().toArray(new String[mPlayer2Team.size()]);
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
            case "replace":
                //TODO need to handle roar & cie
                toAppendBuilder = new StringBuilder();
                attacker = messageDetails.substring(5, separator);
                remaining = messageDetails.substring(separator + 1);
                separator = remaining.indexOf(',');
                if (separator == -1) {
                    //for genderless
                    separator = remaining.indexOf('|');
                }
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
                // appendServerMessage(new SpannableString(message));
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

        String fromEffect = null;
        String trimmedFromEffect = null;
        String ofSource = null;
        String trimmedOfEffect = null;

        String attacker, defender;

        int from = messageDetails.indexOf("[from]");
        if (from != -1) {
            remaining = messageDetails.substring(from + 7);
            separator = remaining.indexOf('|');
            fromEffect = (separator == -1) ? remaining : remaining.substring(0, separator);
            //trim
            trimmedFromEffect = fromEffect;
            trimmedFromEffect = trimmedFromEffect.toLowerCase();
            trimmedFromEffect = trimmedFromEffect.replaceAll("\\s+", "");
        }
        int of = messageDetails.indexOf("[of]");
        if (of != -1) {
            remaining = messageDetails.substring(of + 5);
            separator = remaining.indexOf('|');
            ofSource = (separator == -1) ? remaining : remaining.substring(remaining.indexOf(':'), separator);

            trimmedOfEffect = ofSource;
            trimmedOfEffect = trimmedOfEffect.toLowerCase();
            trimmedOfEffect = trimmedOfEffect.replaceAll("\\s+", "");
        }

        separator = messageDetails.indexOf('|');
        String[] split = messageDetails.split("\\|");

        switch (command) {
            case "-damage":
                attacker = split[0].substring(5);

                if (messageDetails.startsWith("p2")) {
                    oldHP = mPlayer2Team.get(attacker);
                    if (oldHP == null) {
                        oldHP = 100;
                        mPlayer2Team.put(attacker, oldHP);
                    }
                } else {
                    oldHP = mPlayer1Team.get(attacker);
                    if (oldHP == null) {
                        oldHP = 100;
                        mPlayer1Team.put(attacker, oldHP);
                    }
                }

                remaining = split[1];
                separator = remaining.indexOf("/");
                if (separator == -1) { // fainted
                    intAmount = 0;
                } else {
                    String hp = remaining.substring(0, separator);
                    intAmount = Integer.parseInt(hp);
                }
                lostHP = oldHP - intAmount;

                if (trimmedFromEffect != null) {
                    switch (trimmedFromEffect) {
                        case "stealthrock":
                            toAppendBuilder.append("Pointed stones dug into ").append(attacker).append("!");
                            break;
                        case "spikes":
                            toAppendBuilder.append(attacker).append(" is hurt by the spikes!");
                            break;
                        case "brn":
                            toAppendBuilder.append(attacker).append(" was hurt by its burn!");
                            break;
                        case "psn":
                            toAppendBuilder.append(attacker).append(" was hurt by poison!");
                            break;
                        case "item:lifeorb":
                            toAppendBuilder.append(attacker).append(" lost some of its HP!");
                            break;
                        case "recoil":
                            toAppendBuilder.append(attacker).append(" is damaged by recoil!");
                            break;
                        case "sandstorm":
                            toAppendBuilder.append(attacker).append(" is buffeted by the sandstorm!");
                            break;
                        case "hail":
                            toAppendBuilder.append(attacker).append(" is buffeted by the hail!");
                            break;
                        case "baddreams":
                            toAppendBuilder.append(attacker).append(" is tormented!");
                            break;
                        case "nightmare":
                            toAppendBuilder.append(attacker).append(" is locked in a nightmare!");
                            break;
                        case "confusion":
                            toAppendBuilder.append("It hurt itself in its confusion!");
                            break;
                        case "leechseed":
                            toAppendBuilder.append(attacker).append("'s health is sapped by Leech Seed!");
                            break;
                        case "flameburst":
                            toAppendBuilder.append("The bursting flame hit ").append(attacker).append("!");
                            break;
                        case "firepledge":
                            toAppendBuilder.append(attacker).append(" is hurt by the sea of fire!");
                            break;
                        case "jumpkick":
                        case "highjumpkick":
                            toAppendBuilder.append(attacker).append(" kept going and crashed!");
                            break;
                        default:
                            if (ofSource != null) {
                                toAppendBuilder.append(attacker).append(" is hurt by ").append(ofSource).append("'s ").append(fromEffect).append("!");
                            } else if (fromEffect.contains("item:")) {
                                toAppendBuilder.append(attacker).append(" is hurt by its").append(fromEffect.substring(5)).append("!");
                            } else if (fromEffect.contains("ability:")) {
                                toAppendBuilder.append(attacker).append(" is hurt by its").append(fromEffect.substring(8)).append("!");
                            } else {
                                toAppendBuilder.append(attacker).append(" lost some HP because of ").append(fromEffect).append("!");
                            }
                            break;
                    }
                } else {
                    if (messageDetails.startsWith("p2")) {
                        toAppendBuilder.append("The opposing ");
                    }
                    toAppendBuilder.append(attacker).append(" lost ");
                    toAppendBuilder.append(lostHP).append("% of its health!");
                }

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
                remaining = messageDetails.substring(separator + 1);
                separator = remaining.indexOf("/");
                if (separator == -1) {
                    intAmount = 0; // shouldnt happen sicne we're healing
                } else {
                    String hp = remaining.substring(0, separator);
                    intAmount = Integer.parseInt(hp);
                }
                lostHP = intAmount - oldHP;

                if (trimmedFromEffect != null) {
                    switch (trimmedFromEffect) {
                        case "ingrain":
                            toAppendBuilder.append(attacker).append(" absorbed nutrients with its roots!");
                            break;
                        case "aquaring":
                            toAppendBuilder.append("Aqua Ring restored ").append(attacker).append("'s HP");
                            break;
                        case "raindish":
                        case "dryskin":
                        case "icebody":
                            toAppendBuilder.append(attacker).append("'s ").append(fromEffect).append(" heals it!");
                            break;
                        case "healingwish":
                            // TODO
                            break;
                        case "lunardance":
                            // TODO
                            break;
                        case "wish":
                            //TODO wish pass
                            break;
                        case "drain":
                            toAppendBuilder.append(attacker).append(" had its energy drained!");
                            break;
                        case "item:leftovers":
                        case "item:shellbell":
                            toAppendBuilder.append(attacker).append(" restored a little HP using its ").append(fromEffect.substring(5)).append("!");
                            break;
                        default:
                            if (fromEffect.contains("item:")) {
                                fromEffect = fromEffect.substring(5);
                            } else if (fromEffect.contains("ability:")) {
                                fromEffect = fromEffect.substring(8);
                            }
                            toAppendBuilder.append(attacker).append(" restored HP using its ").append(fromEffect).append("!");
                            break;
                    }
                } else {
                    if (messageDetails.startsWith("p2")) {
                        toAppendBuilder.append("The opposing ");
                    }

                    toAppendBuilder.append(attacker);
                    toAppendBuilder.append(" healed ").append(lostHP).append("% of it's health!");
                }
                if (messageDetails.startsWith("p2")) {
                    mPlayer2Team.put(attacker, intAmount);
                } else {
                    mPlayer1Team.put(attacker, intAmount);
                }

                toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                break;
            case "-sethp":
                switch (trimmedFromEffect) {
                    case "painsplit":
                        toAppendBuilder.append("The battlers shared their pain!");
                        break;
                }
                // todo actually switch hps
                toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
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
                        toAppendBuilder.append(stat).append(" ");
                        break;
                }
                String amount = remaining.substring(separator + 1);
                if (amount.contains("|")) {
                    amount = amount.substring(0, amount.indexOf("|"));
                }
                intAmount = Integer.parseInt(amount);
                if (intAmount == 2) {
                    toAppendBuilder.append("sharply ");
                } else if (intAmount > 2) {
                    toAppendBuilder.append("drastically ");
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
                        toAppendBuilder.append(stat).append(" ");
                        break;
                }
                amount = remaining.substring(separator + 1);
                if (amount.contains("|")) {
                    amount = amount.substring(0, amount.indexOf("|"));
                }
                toAppendBuilder.append("fell");
                intAmount = Integer.parseInt(amount);
                if (intAmount == 2) {
                    toAppendBuilder.append(" hashly");
                } else if (intAmount >= 3) {
                    toAppendBuilder.append(" severely");
                }
                toAppendBuilder.append("!");
                toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-setboost":
                attacker = split[0].substring(5);
                if (fromEffect != null) {
                    switch (trimmedFromEffect) {
                        case "bellydrum":
                            toAppendBuilder.append(attacker + " cut its own HP and maximized its Attack!");
                            break;

                        case "angerpoint":
                            toAppendBuilder.append(attacker + " maxed its Attack!");
                            break;
                    }
                }
                toAppendSpannable = new SpannableStringBuilder(command + ":" + messageDetails);
                break;

            case "-swapboost":
                attacker = split[0].substring(5);
                if (fromEffect != null) {
                    switch (trimmedFromEffect) {
                        case "guardswap":
                            toAppendBuilder.append(attacker + " switched all changes to its Defense and Sp. Def with the target!");
                            break;

                        case "heartswap":
                            toAppendBuilder.append(attacker + " switched stat changes with the target!");
                            break;

                        case "powerswap":
                            toAppendBuilder.append(attacker + " switched all changes to its Attack and Sp. Atk with the target!");
                            break;
                    }
                }
                toAppendSpannable = new SpannableStringBuilder(command + ":" + messageDetails);
                break;

            case "-restoreboost":
                //nothign here
                toAppendSpannable = new SpannableStringBuilder("");
                break;

            case "-copyboost":
                attacker = split[0].substring(5);
                defender = split[1].substring(5);
                toAppendBuilder.append(attacker).append(" copied ").append(defender).append("'s stat changes!");
                toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-clearboost":
                attacker = split[0].substring(5);
                toAppendBuilder.append(attacker).append("'s stat changes were removed!");
                toAppendSpannable = new SpannableStringBuilder(command + ":" + messageDetails);
                break;

            case "-invertboost":
                attacker = split[0].substring(5);
                toAppendBuilder.append(attacker).append("'s stat changes were inverted!");
                toAppendSpannable = new SpannableStringBuilder(command + ":" + messageDetails);
                break;

            case "-clearallboost":
                toAppendBuilder.append("All stat changes were eliminated!");
                toAppendSpannable = new SpannableStringBuilder(command + ":" + messageDetails);
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
                if (attacker.contains("|")) {
                    attacker = attacker.substring(0, attacker.indexOf("|"));
                }
                toAppendBuilder.append("It doesn't affect ");
                if (messageDetails.startsWith("p2")) {
                    toAppendBuilder.append("the opposing ");
                }
                toAppendBuilder.append(attacker);
                toAppendSpannable = new SpannableString(toAppendBuilder);
                break;

            case "-miss":
                if (split.length > 1) {
                    // there was a target
                    defender = split[1].substring(5);
                    toAppendBuilder.append(defender).append(" avoided the attack!");
                } else {
                    attacker = split[0].substring(5);
                    toAppendBuilder.append(attacker).append("'s attack missed!");
                }
                toAppendBuilder.append(" missed the target");
                toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-fail":
                // todo
                attacker = split[0].substring(5);
                if (split.length > 1) {
                    remaining = split[1];

                    switch (remaining) {
                        case "brn":
                            toAppendBuilder.append(attacker).append(" is already burned.");
                            break;
                        case "tox":
                        case "psn":
                            toAppendBuilder.append(attacker).append(" is already poisoned.");
                            break;
                        case "slp":
                            //todo try uproar
                            toAppendBuilder.append(attacker).append(" is already asleep.");
                            break;
                        case "par":
                            toAppendBuilder.append(attacker).append(" is already paralyzed.");
                            break;
                        case "frz":
                            toAppendBuilder.append(attacker).append(" is already frozen.");
                            break;
                        case "substitute":
                            // TODO try while having a sub up
                            toAppendBuilder.append(attacker).append(" cant create a substitute!");
                            break;
                        case "skydrop":
                            // TODO try
                            toAppendBuilder.append("But it failed!");
                            break;
                        case "unboost":
                            toAppendBuilder.append(attacker).append("'s stats were not lowered!");
                            break;

                        default:
                            toAppendBuilder.append("But it failed!");
                            break;
                    }
                } else {
                    toAppendBuilder.append("But it failed!");
                }


                toAppendSpannable = new SpannableString(toAppendBuilder);
                break;

            case "-notarget":
                toAppendSpannable = new SpannableString("But there was no target...");
                break;

            case "-ohko":
                toAppendSpannable = new SpannableString("It's a one-hit KO!");
                break;

            case "-hitcount":
                try {
                    String hitCountS = messageDetails.substring(messageDetails.lastIndexOf("|") + 1);
                    int hitCount = Integer.parseInt(hitCountS);
                    toAppendBuilder.append("Hit " + hitCount + " time");
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

            case "-nothing":
                toAppendSpannable = new SpannableString("But nothing happened! ");
                break;

            case "-waiting":
                attacker = split[0].substring(5);
                defender = split[1].substring(5);
                toAppendBuilder.append(attacker).append(" is waiting for ").append(defender).append("'s move...");
                toAppendSpannable = new SpannableString(toAppendBuilder);
                break;

            case "-combine":
                toAppendSpannable = new SpannableString("The two moves are joined! It's a combined move!");
                break;

            case "-prepare":
                // todo
                toAppendSpannable = new SpannableString(command + ":" + messageDetails);
                break;

            case "-status":
                attacker = split[0].substring(5, separator);
                if (messageDetails.startsWith("p2")) {
                    toAppendBuilder.append("The opposing ");
                }
                toAppendBuilder.append(attacker);
                remaining = split[1];
                switch (remaining) {
                    case "brn":
                        toAppendBuilder.append(" was burned");
                        if (fromEffect != null) {
                            toAppendBuilder.append(" by the ").append(fromEffect);
                        }
                        toAppendBuilder.append("!");
                        break;

                    case "tox":
                        toAppendBuilder.append(" was badly poisoned");
                        if (fromEffect != null) {
                            toAppendBuilder.append(" by the ").append(fromEffect);
                        }
                        toAppendBuilder.append("!");
                        break;

                    case "psn":
                        toAppendBuilder.append(" was poisoned!");
                        break;

                    case "slp":
                        if (fromEffect != null && trimmedFromEffect.equals("move:rest")) {
                            toAppendBuilder.append(" slept and became healthy!");
                        } else {
                            toAppendBuilder.append(" fell asleep!");
                        }
                        break;

                    case "par":
                        toAppendBuilder.append(" is paralyzed! It may be unable to move!");
                        break;

                    case "frz":
                        toAppendBuilder.append(" was frozen solid!");
                        break;
                }
                toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                break;
            case "-curestatus":
                // TODO effects
                attacker = messageDetails.substring(5, separator);
                if (messageDetails.startsWith("p2")) {
                    toAppendBuilder.append("The opposing ");
                }
                toAppendBuilder.append(attacker);
                remaining = messageDetails.substring(separator + 1);
                toAppendBuilder.append(" was cured from ").append(remaining);
                toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-cureteam":
                // TODO effects
                attacker = messageDetails.substring(5, separator);
                if (messageDetails.startsWith("p2")) {
                    toAppendBuilder.append("The opposing ");
                }
                toAppendBuilder.append(attacker);
                toAppendBuilder.append(" cured the whole team from bad status");
                toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                break;


            case "-weather":
                String weather = split[0];
                boolean upkeep = false;
                if (split.length > 1) {
                    upkeep = true;
                }
                switch (weather) {
                    case "RainDance":
                        if (upkeep) {
                            toAppendBuilder.append("Rain continues to fall!");
                        } else {
                            toAppendBuilder.append("It started to rain!");
                            weatherExist = true;
                        }
                        break;
                    case "Sandstorm":
                        if (upkeep) {
                            toAppendBuilder.append("The sandstorm rages.");
                        } else {
                            toAppendBuilder.append("A sandstorm kicked up!");
                            weatherExist = true;
                        }
                        break;
                    case "SunnyDay":
                        if (upkeep) {
                            toAppendBuilder.append("The sunlight is strong!");
                        } else {
                            toAppendBuilder.append("The sunlight turned harsh!");
                            weatherExist = true;
                        }
                        break;
                    case "Hail":
                        if (upkeep) {
                            toAppendBuilder.append("The hail crashes down.");
                        } else {
                            toAppendBuilder.append("It started to hail!");
                            weatherExist = true;
                        }
                        break;
                    case "none":
                        if (weatherExist) {
                            switch (currentWeather) {
                                case "RainDance":
                                    toAppendBuilder.append("The rain stopped.");
                                    break;
                                case "SunnyDay":
                                    toAppendBuilder.append("The sunlight faded.");
                                    break;
                                case "Sandstorm":
                                    toAppendBuilder.append("The sandstorm subsided.");
                                    break;
                                case "Hail":
                                    toAppendBuilder.append("The hail stopped.");
                                    break;
                            }
                        }
                        weatherExist = false;
                        break;
                }
                currentWeather = weather;
                toAppendSpannable = new SpannableString(toAppendBuilder);
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
                String side;
                if (messageDetails.startsWith("p2")) {
                    side = "the opposing team";
                } else {
                    side = "your team";
                }


                messageDetails = messageDetails.substring(messageDetails.indexOf('|'));
                if (messageDetails.contains("Stealth Rock")) {
                    toAppendBuilder.append("Pointed stones float in the air around ").append(side).append("!");
                } else if (messageDetails.contains("Toxic Spikes")) {
                    toAppendBuilder.append("Toxic spikes were scattered all around the feet of ").append(side).append("!");
                } else if (messageDetails.contains("Spikes")) {
                    toAppendBuilder.append("Spikes were scattered all around the feet of ").append(side).append("!");
                } else if (messageDetails.contains("Reflect")) {
                    toAppendBuilder.append("Reflect raised ").append(side).append("'s Defense!");
                } else if (messageDetails.contains("Light Screen")) {
                    toAppendBuilder.append("Light Screen raised ").append(side).append("'s Special Defense!");
                } else if (messageDetails.contains("Sticky Web")) {
                    toAppendBuilder.append("A sticky web spreads out beneath ").append(side).append("'s feet!");
                } else if (messageDetails.contains("Tailwind")) {
                    toAppendBuilder.append("The tailwind blew from behind ").append(side).append("!");
                } else if (messageDetails.contains("Safeguard")) {
                    toAppendBuilder.append(side).append(" became cloaked in a mystical veil!");
                } else if (messageDetails.contains("Mist")) {
                    toAppendBuilder.append(side).append(" became shrouded in mist!");
                } else if (messageDetails.contains("Lucky Chant")) {
                    toAppendBuilder.append("The Lucky Chant shielded ").append(side).append(" from critical hits!");
                } else if (messageDetails.contains("Fire Pledge")) {
                    toAppendBuilder.append("A sea of fire enveloped ").append(side).append("!");
                } else if (messageDetails.contains("Water Pledge")) {
                    toAppendBuilder.append("A rainbow appeared in the sky on ").append(side).append("'s side!");
                } else if (messageDetails.contains("Grass Pledge")) {
                    toAppendBuilder.append("A swamp enveloped ").append(side).append("!");
                } else {
                    toAppendBuilder.append(messageDetails).append(" started!");
                }


                toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-sideend":
                // todo
                toAppendSpannable = new SpannableString(command + ":" + messageDetails);
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