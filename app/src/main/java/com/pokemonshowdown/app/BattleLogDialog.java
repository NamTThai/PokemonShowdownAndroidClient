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


    private String getOutputPokemonSide(String split) {
        return getOutputPokemonSide(split, true);
    }

    private String getOutputPokemonSide(String split, boolean start) {
        StringBuilder sb = new StringBuilder();
        if (split.startsWith("p2")) {
            if (start) {
                sb.append("The opposing ");
            } else {
                sb.append("the opposing ");
            }
        }

        int separator = split.indexOf(':');
        sb.append(split.substring(separator + 1).trim());
        return sb.toString();
    }

    private String getId(String split) {
        int separator = split.indexOf(':');
        return split.substring(separator + 1).trim();
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
        boolean flag, eat, weaken;

        String fromEffect = null;
        String trimmedFromEffect = null;
        String ofSource = null;
        String trimmedOfEffect = null;

        String attacker, defender;
        String attackerOutputName;
        String defenderOutputName;

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
                attacker = getId(split[0]);
                attackerOutputName = getOutputPokemonSide(split[0]);

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
                    trimmedFromEffect = getId(trimmedFromEffect);
                    switch (trimmedFromEffect) {
                        case "stealthrock":
                            attackerOutputName = getOutputPokemonSide(split[0], false);
                            toAppendBuilder.append("Pointed stones dug into ").append(attackerOutputName).append("!");
                            break;
                        case "spikes":
                            toAppendBuilder.append(attackerOutputName).append(" is hurt by the spikes!");
                            break;
                        case "brn":
                            toAppendBuilder.append(attackerOutputName).append(" was hurt by its burn!");
                            break;
                        case "psn":
                            toAppendBuilder.append(attackerOutputName).append(" was hurt by poison!");
                            break;
                        case "lifeorb":
                            toAppendBuilder.append(attackerOutputName).append(" lost some of its HP!");
                            break;
                        case "recoil":
                            toAppendBuilder.append(attackerOutputName).append(" is damaged by recoil!");
                            break;
                        case "sandstorm":
                            toAppendBuilder.append(attackerOutputName).append(" is buffeted by the sandstorm!");
                            break;
                        case "hail":
                            toAppendBuilder.append(attackerOutputName).append(" is buffeted by the hail!");
                            break;
                        case "baddreams":
                            toAppendBuilder.append(attackerOutputName).append(" is tormented!");
                            break;
                        case "nightmare":
                            toAppendBuilder.append(attackerOutputName).append(" is locked in a nightmare!");
                            break;
                        case "confusion":
                            toAppendBuilder.append("It hurt itself in its confusion!");
                            break;
                        case "leechseed":
                            toAppendBuilder.append(attackerOutputName).append("'s health is sapped by Leech Seed!");
                            break;
                        case "flameburst":
                            attackerOutputName = getOutputPokemonSide(split[0], false);
                            toAppendBuilder.append("The bursting flame hit ").append(attackerOutputName).append("!");
                            break;
                        case "firepledge":
                            toAppendBuilder.append(attackerOutputName).append(" is hurt by the sea of fire!");
                            break;
                        case "jumpkick":
                        case "highjumpkick":
                            toAppendBuilder.append(attackerOutputName).append(" kept going and crashed!");
                            break;
                        default:
                            if (ofSource != null) {
                                ofSource = getId(ofSource);
                                fromEffect = getId(fromEffect);

                                toAppendBuilder.append(attackerOutputName).append(" is hurt by ").append(ofSource).append("'s ").append(fromEffect).append("!");
                            } else if (trimmedFromEffect.contains(":")) {
                                toAppendBuilder.append(attackerOutputName).append(" is hurt by its").append(fromEffect).append("!");
                            } else {
                                toAppendBuilder.append(attackerOutputName).append(" lost some HP because of ").append(fromEffect).append("!");
                            }
                            break;
                    }
                } else {
                    toAppendBuilder.append(attackerOutputName).append(" lost ");
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
                attacker = getId(split[0]);
                attackerOutputName = getOutputPokemonSide(split[0]);
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
                    trimmedFromEffect = getId(trimmedFromEffect);
                    switch (trimmedFromEffect) {
                        case "ingrain":
                            toAppendBuilder.append(attackerOutputName).append(" absorbed nutrients with its roots!");
                            break;
                        case "aquaring":
                            attackerOutputName = getOutputPokemonSide(split[0], false);
                            toAppendBuilder.append("Aqua Ring restored ").append(attackerOutputName).append("'s HP");
                            break;
                        case "raindish":
                        case "dryskin":
                        case "icebody":
                            toAppendBuilder.append(attackerOutputName).append("'s ").append(fromEffect).append(" heals it!");
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
                            if (trimmedOfEffect != null) {
                                if (trimmedOfEffect.contains(":")) {
                                    trimmedOfEffect = getOutputPokemonSide(ofSource);
                                    toAppendBuilder.append(trimmedOfEffect).append(" had its energy drained!");
                                    break;
                                }
                            }
                            // we should never enter here
                            toAppendBuilder.append(attackerOutputName).append(" drained health!");
                            break;

                        case "leftovers":
                        case "shellbell":
                            toAppendBuilder.append(attackerOutputName).append(" restored a little HP using its ").append(getId(fromEffect)).append("!");
                            break;
                        default:
                            fromEffect = getId(fromEffect);
                            toAppendBuilder.append(attackerOutputName).append(" restored HP using its ").append(fromEffect).append("!");
                            break;
                    }
                } else {
                    toAppendBuilder.append(attackerOutputName);
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
                trimmedFromEffect = getId(trimmedFromEffect);
                switch (trimmedFromEffect) {
                    case "painsplit":
                        toAppendBuilder.append("The battlers shared their pain!");
                        break;
                }
                // todo actually switch hps
                toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-boost":
                attackerOutputName = getOutputPokemonSide(split[0]);
                toAppendBuilder.append(attackerOutputName);
                toAppendBuilder.append("'s ");
                String stat = split[1];
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
                String amount = split[2];
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
                attackerOutputName = getOutputPokemonSide(split[0]);
                toAppendBuilder.append(attackerOutputName);
                toAppendBuilder.append("'s ");
                stat = split[1];
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
                amount = split[2];
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
                attackerOutputName = getOutputPokemonSide(split[0]);
                if (fromEffect != null) {
                    trimmedFromEffect = getId(trimmedFromEffect);
                    switch (trimmedFromEffect) {
                        case "bellydrum":
                            toAppendBuilder.append(attackerOutputName).append(" cut its own HP and maximized its Attack!");
                            break;

                        case "angerpoint":
                            toAppendBuilder.append(attackerOutputName).append(" maxed its Attack!");
                            break;
                    }
                }
                toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-swapboost":
                attackerOutputName = getOutputPokemonSide(split[0]);
                if (fromEffect != null) {
                    trimmedFromEffect = getId(trimmedFromEffect);
                    switch (trimmedFromEffect) {
                        case "guardswap":
                            toAppendBuilder.append(attackerOutputName).append(" switched all changes to its Defense and Sp. Def with the target!");
                            break;

                        case "heartswap":
                            toAppendBuilder.append(attackerOutputName).append(" switched stat changes with the target!");
                            break;

                        case "powerswap":
                            toAppendBuilder.append(attackerOutputName).append(" switched all changes to its Attack and Sp. Atk with the target!");
                            break;
                    }
                }
                toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-restoreboost":
                //nothign here
                toAppendSpannable = new SpannableStringBuilder("");
                break;

            case "-copyboost":
                attackerOutputName = getOutputPokemonSide(split[0]);
                defenderOutputName = getOutputPokemonSide(split[1], false);
                toAppendBuilder.append(attackerOutputName).append(" copied ").append(defenderOutputName).append("'s stat changes!");
                toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-clearboost":
                attackerOutputName = getOutputPokemonSide(split[0]);
                toAppendBuilder.append(attackerOutputName).append("'s stat changes were removed!");
                toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-invertboost":
                attackerOutputName = getOutputPokemonSide(split[0]);
                toAppendBuilder.append(attackerOutputName).append("'s stat changes were inverted!");
                toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-clearallboost":
                toAppendBuilder.append("All stat changes were eliminated!");
                toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
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
                attackerOutputName = getOutputPokemonSide(split[0], false);
                toAppendBuilder.append("It doesn't affect ");
                toAppendBuilder.append(attackerOutputName);
                toAppendBuilder.append(".");
                toAppendSpannable = new SpannableString(toAppendBuilder);
                break;

            case "-miss":
                if (split.length > 1) {
                    // there was a target
                    defenderOutputName = getOutputPokemonSide(split[1]);
                    toAppendBuilder.append(defenderOutputName).append(" avoided the attack!");
                } else {
                    attackerOutputName = getOutputPokemonSide(split[0]);
                    toAppendBuilder.append(attackerOutputName).append("'s attack missed!");
                }
                toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-fail":
                // todo
                attackerOutputName = getOutputPokemonSide(split[0]);
                if (split.length > 1) {
                    remaining = split[1];

                    switch (remaining) {
                        case "brn":
                            toAppendBuilder.append(attackerOutputName).append(" is already burned.");
                            break;
                        case "tox":
                        case "psn":
                            toAppendBuilder.append(attackerOutputName).append(" is already poisoned.");
                            break;
                        case "slp":
                            //todo try uproar
                            toAppendBuilder.append(attackerOutputName).append(" is already asleep.");
                            break;
                        case "par":
                            toAppendBuilder.append(attackerOutputName).append(" is already paralyzed.");
                            break;
                        case "frz":
                            toAppendBuilder.append(attackerOutputName).append(" is already frozen.");
                            break;
                        case "substitute":
                            // TODO try while having a sub up
                            toAppendBuilder.append(attackerOutputName).append(" cant create a substitute!");
                            break;
                        case "skydrop":
                            // TODO try
                            toAppendBuilder.append("But it failed!");
                            break;
                        case "unboost":
                            toAppendBuilder.append(attackerOutputName).append("'s stats were not lowered!");
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
                    String hitCountS = split[split.length - 1];
                    int hitCount = Integer.parseInt(hitCountS);
                    toAppendBuilder.append("Hit ").append(hitCount).append(" time");
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
                attackerOutputName = getOutputPokemonSide(split[0]);
                defenderOutputName = getOutputPokemonSide(split[1], false);
                toAppendBuilder.append(attackerOutputName).append(" is waiting for ").append(defenderOutputName).append("'s move...");
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
                attackerOutputName = getOutputPokemonSide(split[0]);
                toAppendBuilder.append(attackerOutputName);
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
                attackerOutputName = getOutputPokemonSide(split[0]);
                flag = false;
                if (trimmedFromEffect != null) {
                    trimmedFromEffect = getId(trimmedFromEffect);
                    switch (trimmedFromEffect) {
                        case "psychoshift":
                            //ofeffect should always be !null at that time
                            defenderOutputName = getOutputPokemonSide(ofSource, false);
                            toAppendBuilder.append(attackerOutputName).append(" moved its status onto ").append(defenderOutputName);
                            flag = true;
                            break;
                    }
                    if (trimmedFromEffect.contains("ability:")) {
                        toAppendBuilder.append(attackerOutputName).append("'s ").append(getId(fromEffect)).append(" heals its status!");
                        flag = true;
                    }
                }

                if (!flag) {
                    //split1 is cured status
                    switch (split[1]) {
                        case "brn":
                            if (trimmedFromEffect != null && trimmedFromEffect.contains("item:")) {
                                toAppendBuilder.append(attackerOutputName).append("'s ").append(getId(fromEffect)).append(" healed its burn!");
                                break;
                            }
                            if (split[0].startsWith("p2")) {
                                toAppendBuilder.append(attackerOutputName).append("'s burn was healed.");
                            } else {
                                toAppendBuilder.append(attackerOutputName).append(" healed its burn!.");
                            }
                            break;

                        case "tox":
                        case "psn":
                            if (trimmedFromEffect != null && trimmedFromEffect.contains("item:")) {
                                toAppendBuilder.append(attackerOutputName).append("'s ").append(getId(fromEffect)).append(" cured its poison!");
                                break;
                            }
                            toAppendBuilder.append(attackerOutputName).append(" was cured of its poisoning.");
                            break;

                        case "slp":
                            if (trimmedFromEffect != null && trimmedFromEffect.contains("item:")) {
                                toAppendBuilder.append(attackerOutputName).append("'s ").append(getId(fromEffect)).append(" woke it up!");
                                break;
                            }
                            toAppendBuilder.append(attackerOutputName).append(" woke up!");
                            break;

                        case "par":
                            if (trimmedFromEffect != null && trimmedFromEffect.contains("item:")) {
                                toAppendBuilder.append(attackerOutputName).append("'s ").append(getId(fromEffect)).append(" cured its paralysis!");
                                break;
                            }
                            toAppendBuilder.append(attackerOutputName).append(" was cured of paralysis.");

                            break;

                        case "frz":
                            if (trimmedFromEffect != null && trimmedFromEffect.contains("item:")) {
                                toAppendBuilder.append(attackerOutputName).append("'s ").append(getId(fromEffect)).append(" defrosted it!");
                                break;
                            }
                            toAppendBuilder.append(attackerOutputName).append(" thawed out!");
                            break;

                        default:
                            //confusion
                            toAppendBuilder.append(attackerOutputName).append("'s status cleared!");
                            break;
                    }
                }
                toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-cureteam":
                if (trimmedFromEffect != null) {
                    trimmedFromEffect = getId(trimmedFromEffect);
                    switch (trimmedFromEffect) {
                        case "aromatherapy":
                            toAppendBuilder.append("A soothing aroma wafted through the area!");
                            break;

                        case "healbell":
                            toAppendBuilder.append("A bell chimed!");
                            break;
                    }
                } else {
                    attackerOutputName = getOutputPokemonSide(split[0]);
                    toAppendBuilder.append(attackerOutputName);
                    toAppendBuilder.append(" 's team was cured");
                }
                toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-item":
                attackerOutputName = getOutputPokemonSide(split[0]);
                String item = getId(split[1]);
                if (fromEffect != null) {
                    // not to deal with item: or ability: or move:
                    trimmedFromEffect = getId(trimmedFromEffect);
                    switch (trimmedFromEffect) {
                        case "recycle":
                        case "pickup":
                            toAppendBuilder.append(attackerOutputName).append(" found one ").append(item).append("!");
                            break;

                        case "frisk":
                            toAppendBuilder.append(attackerOutputName).append(" frisked its target and found one ").append(item).append("!");
                            break;

                        case "thief":
                        case "covet":
                            defenderOutputName = getOutputPokemonSide(ofSource, false);
                            toAppendBuilder.append(attackerOutputName).append("  stole  ").append(defenderOutputName).append("'s ").append(item).append("!");
                            break;

                        case "harvest":
                            toAppendBuilder.append(attackerOutputName).append(" harvested one ").append(item).append("!");
                            break;

                        case "bestow":
                            defenderOutputName = getOutputPokemonSide(ofSource, false);
                            toAppendBuilder.append(attackerOutputName).append(" received ").append(item).append(" from ").append(defenderOutputName).append("!");
                            break;

                        default:
                            toAppendBuilder.append(attackerOutputName).append(" obtained one ").append(item).append(".");
                            break;
                    }
                } else {
                    switch (item) {
                        case "Air Balloon":
                            toAppendBuilder.append(attackerOutputName).append(" floats in the air with its Air Balloon!");
                            break;

                        default:
                            toAppendBuilder.append(attackerOutputName).append("has ").append(item).append("!");
                            break;
                    }
                }


                toAppendSpannable = new SpannableString(toAppendBuilder);
                break;

            case "-enditem":
                eat = messageDetails.contains("[eat]");
                weaken = messageDetails.contains("[weaken]");
                attacker = getId(split[0]);
                attackerOutputName = getOutputPokemonSide(split[0]);
                item = split[1];
                toAppend = attacker + " has lost its " + item;
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

            case "-formechange":
                //TODO
                toAppendSpannable = new SpannableString(command + ":" + messageDetails);
                break;

            case "-start":
                //todo
                toAppendSpannable = new SpannableString(command + ":" + messageDetails);
                break;

            case "-end":
                //todo
                toAppendSpannable = new SpannableString(command + ":" + messageDetails);
                break;

            case "-singleturn":
                //todo
                toAppendSpannable = new SpannableString(command + ":" + messageDetails);
                break;

            case "-singlemove":
                //todo
                toAppendSpannable = new SpannableString(command + ":" + messageDetails);
                break;

            case "-activate":
                //TODO
                toAppend = messageDetails + " has activated";
                toAppendSpannable = new SpannableString(toAppend);
                break;

            case "-sidestart":
                String side;
                if (messageDetails.startsWith("p2")) {
                    side = "the opposing team";
                } else {
                    side = "your team";
                }

                fromEffect = split[1];
                trimmedFromEffect = fromEffect;
                trimmedFromEffect = trimmedFromEffect.toLowerCase();
                trimmedFromEffect = trimmedFromEffect.replaceAll("\\s+", "");
                trimmedFromEffect = (trimmedFromEffect.contains(":") ? trimmedFromEffect.substring(trimmedFromEffect.indexOf(":") + 1) : trimmedFromEffect);
                switch (trimmedFromEffect) {
                    case "stealthrock":
                        toAppendBuilder.append("Pointed stones float in the air around ").append(side).append("!");
                        break;

                    case "spikes":
                        toAppendBuilder.append("Spikes were scattered all around the feet of ").append(side).append("!");
                        break;

                    case "toxicspikes":
                        toAppendBuilder.append("Toxic spikes were scattered all around the feet of ").append(side).append("!");
                        break;

                    case "stickyweb":
                        toAppendBuilder.append("A sticky web spreads out beneath ").append(side).append("'s feet!");
                        break;

                    case "tailwind":
                        toAppendBuilder.append("The tailwind blew from behind ").append(side).append("!");
                        break;

                    case "reflect":
                        toAppendBuilder.append("Reflect raised ").append(side).append("'s Defense!");
                        break;

                    case "lightscreen":
                        toAppendBuilder.append("Light Screen raised ").append(side).append("'s Special Defense!");
                        break;

                    case "safeguard":
                        toAppendBuilder.append(side).append(" became cloaked in a mystical veil!");
                        break;

                    case "mist":
                        toAppendBuilder.append(side).append(" became shrouded in mist!");
                        break;

                    case "luckychant":
                        toAppendBuilder.append("The Lucky Chant shielded ").append(side).append(" from critical hits!");
                        break;

                    case "firepledge":
                        toAppendBuilder.append("A sea of fire enveloped ").append(side).append("!");
                        break;

                    case "waterpledge":
                        toAppendBuilder.append("A rainbow appeared in the sky on ").append(side).append("'s side!");
                        break;

                    case "grasspledge":
                        toAppendBuilder.append("A swamp enveloped ").append(side).append("!");
                        break;

                    default:
                        toAppendBuilder.append(fromEffect).append(" started!");
                        break;
                }

                toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-sideend":
                if (messageDetails.startsWith("p2")) {
                    side = "the opposing team";
                } else {
                    side = "your team";
                }

                fromEffect = split[1];
                trimmedFromEffect = fromEffect;
                trimmedFromEffect = trimmedFromEffect.toLowerCase();
                trimmedFromEffect = trimmedFromEffect.replaceAll("\\s+", "");
                trimmedFromEffect = (trimmedFromEffect.contains(":") ? trimmedFromEffect.substring(trimmedFromEffect.indexOf(":") + 1) : trimmedFromEffect);

                switch (trimmedFromEffect) {
                    case "stealthrock":
                        toAppendBuilder.append("The pointed stones disappeared from around ").append(side).append("!");
                        break;

                    case "spikes":
                        toAppendBuilder.append("The spikes disappeared from around ").append(side).append("!");
                        break;

                    case "toxicspikes":
                        toAppendBuilder.append("The poison spikes disappeared from around ").append(side).append("!");
                        break;

                    case "stickyweb":
                        toAppendBuilder.append("The sticky web has disappeared from beneath ").append(side).append("'s feet!");
                        break;

                    case "tailwind":
                        side = Character.toUpperCase(side.charAt(0)) + side.substring(1);
                        toAppendBuilder.append(side).append("'s tailwind petered out!");
                        break;

                    case "reflect":
                        side = Character.toUpperCase(side.charAt(0)) + side.substring(1);
                        toAppendBuilder.append(side).append("'s Reflect wore off!");
                        break;

                    case "lightscreen":
                        side = Character.toUpperCase(side.charAt(0)) + side.substring(1);
                        toAppendBuilder.append(side).append("'s Reflect wore off!");
                        break;

                    case "safeguard":
                        side = Character.toUpperCase(side.charAt(0)) + side.substring(1);
                        toAppendBuilder.append(side).append(" is no longer protected by Safeguard!");
                        break;

                    case "mist":
                        side = Character.toUpperCase(side.charAt(0)) + side.substring(1);
                        toAppendBuilder.append(side).append(" is no longer protected by mist!");
                        break;

                    case "luckychant":
                        side = Character.toUpperCase(side.charAt(0)) + side.substring(1);
                        toAppendBuilder.append(side).append("'s Lucky Chant wore off!");
                        break;

                    case "firepledge":
                        toAppendBuilder.append("The sea of fire around ").append(side).append(" disappeared!");
                        break;

                    case "waterpledge":
                        toAppendBuilder.append("The rainbow on ").append(side).append("'s side disappeared!");
                        break;

                    case "grasspledge":
                        toAppendBuilder.append("The swamp around ").append(side).append(" disappeared!");
                        break;

                    default:
                        toAppendBuilder.append(fromEffect).append(" ended!");
                        break;
                }


                toAppendSpannable = new SpannableString(command + ":" + messageDetails);
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


            case "-fieldstart":
                //todo (trick room, maybe more)
                toAppendSpannable = new SpannableString(command + ":" + messageDetails);
                break;

            case "-fieldend":
                //todo (trick room, maybe more)
                toAppendSpannable = new SpannableString(command + ":" + messageDetails);
                break;

            case "-fieldactivate":
                //todo (trick room, maybe more)
                toAppendSpannable = new SpannableString(command + ":" + messageDetails);
                break;


            case "-message":
                toAppendSpannable = new SpannableString(messageDetails);
                break;

            case "-anim":
                toAppendSpannable = new SpannableString(command + ":" + messageDetails);
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