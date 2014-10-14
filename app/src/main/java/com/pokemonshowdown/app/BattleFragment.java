package com.pokemonshowdown.app;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pokemonshowdown.data.BattleFieldData;
import com.pokemonshowdown.data.MyApplication;
import com.pokemonshowdown.data.Pokemon;

import org.w3c.dom.Text;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.PriorityQueue;

public class BattleFragment extends android.support.v4.app.Fragment {
    public final static String BTAG = BattleFragment.class.getName();
    public final static String ROOM_ID = "Room Id";
    public final static int ANIMATION_SHORT = 500;
    public final static int ANIMATION_LONG = 1000;

    private ArrayDeque<AnimatorSet> mAnimatorSetQueue;

    private String mRoomId;
    private String mPlayer1;
    private String mPlayer2;
    private ArrayList<String> mPlayer1Team;
    private ArrayList<String> mPlayer2Team;

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

    @Override
    public void onResume() {
        super.onResume();
        BattleFieldData.AnimationData animationData = BattleFieldData.get(getActivity()).getAnimationInstance(mRoomId);
        if (animationData != null) {
            animationData.setMessageListener(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        BattleFieldData.AnimationData animationData = BattleFieldData.get(getActivity()).getAnimationInstance(mRoomId);
        if (animationData != null) {
            animationData.setMessageListener(true);
        }
    }

    public void processServerMessage(String message) {
        BattleFieldData.AnimationData animationData = BattleFieldData.get(getActivity()).getAnimationInstance(mRoomId);
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
            case "chat":
            case "c":
            case "tc":
            case "c:":
                break;
            case "raw":
                makeToast(Html.fromHtml(messageDetails).toString());
                break;
            case "message":
                makeToast(messageDetails);
                break;
            case "gametype":
            case "gen":
                break;
            case "player":
                final String playerType;
                final String playerName;
                final String avatar;
                if (separator == -1) {
                    playerType = messageDetails;
                    playerName = "";
                    avatar = null;
                } else {
                    playerType = messageDetails.substring(0, separator);
                    String playerDetails = messageDetails.substring(separator + 1);
                    separator = playerDetails.indexOf('|');
                    playerName = playerDetails.substring(0, separator);
                    avatar = playerDetails.substring(separator + 1);
                }
                if (playerType.equals("p1")) {
                    animationData.setPlayer1(playerName);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView) getView().findViewById(R.id.username)).setText(playerName);
                            if (avatar != null) {
                                int avatarResource = getActivity().getApplicationContext()
                                        .getResources().getIdentifier("avatar_" + avatar, "drawable", getActivity().getApplicationContext().getPackageName());
                                ((ImageView) getView().findViewById(R.id.avatar)).setImageResource(avatarResource);
                            }
                        }
                    });
                    mPlayer1 = playerName;
                } else {
                    animationData.setPlayer2(playerName);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView) getView().findViewById(R.id.username_o)).setText(playerName);
                            if (avatar != null) {
                                int avatarResource = getActivity().getApplicationContext()
                                        .getResources().getIdentifier("avatar_" + avatar, "drawable", getActivity().getApplicationContext().getPackageName());
                                ((ImageView) getView().findViewById(R.id.avatar_o)).setImageResource(avatarResource);
                            }
                        }
                    });
                    mPlayer2 = playerName;
                }
                break;
            case "tier":
                break;
            case "rated":
                break;
            case "rule":
                break;
            case "":
                break;
            case "clearpoke":
                mPlayer1Team = new ArrayList<>();
                mPlayer2Team = new ArrayList<>();
                break;
            case "poke":
                playerType = messageDetails.substring(0, separator);
                int comma = messageDetails.indexOf(',');
                final String pokeName = (comma == -1) ? messageDetails.substring(separator + 1) :
                        messageDetails.substring(separator + 1, comma);
                final int iconId;
                if (playerType.equals("p1")) {
                    iconId = mPlayer1Team.size();
                    mPlayer1Team.add(pokeName);
                } else {
                    iconId = mPlayer2Team.size();
                    mPlayer2Team.add(pokeName);
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ImageView icon = (ImageView) getView().findViewById(getIconId(playerType, iconId));
                        icon.setImageResource(Pokemon.getPokemonIconSmall(getActivity(), MyApplication.toId(pokeName), false));
                    }
                });
                break;
            case "teampreview":
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        FrameLayout frameLayout = (FrameLayout) getView().findViewById(R.id.battle_interface);
                        frameLayout.removeAllViews();
                        getActivity().getLayoutInflater().inflate(R.layout.fragment_battle_teampreview, frameLayout);
                        for(int i = 0; i < mPlayer1Team.size() ; i++) {
                            ImageView sprites = (ImageView) getView().findViewById(getTeamPreviewSpriteId("p1", i));
                            sprites.setImageResource(Pokemon.getPokemonIcon(getActivity(), MyApplication.toId(mPlayer1Team.get(i)), false));
                        }
                        for(int i = 0; i < mPlayer2Team.size() ; i++) {
                            ImageView sprites = (ImageView) getView().findViewById(getTeamPreviewSpriteId("p2", i));
                            sprites.setImageResource(Pokemon.getPokemonIcon(getActivity(), MyApplication.toId(mPlayer2Team.get(i)), false));
                        }
                    }
                });
                break;
            case "request":
                makeToast(messageDetails);
                break;
            case "inactive":
                final String inactive;
                final String player;
                if ((messageDetails.startsWith(mPlayer1)) || (messageDetails.startsWith("Player 1"))) {
                    player = "p1";
                } else {
                    if ((messageDetails.startsWith(mPlayer2)) || (messageDetails.startsWith("Player 2"))) {
                        player = "p2";
                    } else {
                        break;
                    }
                }
                if (messageDetails.contains(" seconds left")) {
                    remaining = messageDetails.substring(0, messageDetails.indexOf(" seconds left"));
                    inactive = remaining.substring(remaining.lastIndexOf(' ')) + "s";
                } else {
                    break;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (player.equals("p1")) {
                            TextView textView = (TextView) getView().findViewById(R.id.inactive);
                            textView.setVisibility(View.VISIBLE);
                            textView.setText(inactive);
                        } else {
                            TextView textView = (TextView) getView().findViewById(R.id.inactive_o);
                            textView.setVisibility(View.VISIBLE);
                            textView.setText(inactive);
                        }
                    }
                });
                break;
            case "inactiveoff":
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        (getView().findViewById(R.id.inactive)).setVisibility(View.GONE);
                        (getView().findViewById(R.id.inactive_o)).setVisibility(View.GONE);
                    }
                });
                break;
            case "start":
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        FrameLayout frameLayout = (FrameLayout) getView().findViewById(R.id.battle_interface);
                        frameLayout.removeAllViews();
                        getActivity().getLayoutInflater().inflate(R.layout.fragment_battle_animation, frameLayout);
                    }
                });
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
                makeToast(toAppendSpannable);
                break;
            case "switch":
            case "drag":
                toAppendBuilder = new StringBuilder();
                attacker = messageDetails.substring(5, separator);
                remaining = messageDetails.substring(separator + 1);
                separator = remaining.indexOf(',');
                if(separator == -1) {
                    separator = remaining.indexOf('|');
                }
                String species = remaining.substring(0, separator);
                attacker = (!attacker.equals(species)) ? attacker + " (" + species + ")" : attacker;
                if (messageDetails.startsWith("p1")) {
                    toAppendBuilder.append("Go! ").append(attacker).append('!');
                } else {
                    toAppendBuilder.append(mPlayer2).append(" sent out ").append(attacker).append("!");
                }
                makeToast(new SpannableStringBuilder(toAppendBuilder));
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
                AnimatorSet toast = makeToast(message, ANIMATION_LONG);
                startAnimation(toast);
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
        Spannable toAppendSpannable = new SpannableString("");
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
                /*attacker = messageDetails.substring(5, separator);
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

                toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);*/
                break;
            case "-heal":
                /*attacker = messageDetails.substring(5, separator);
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

                toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);*/
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
                if (messageDetails.contains("Stealth Rock")) {
                    toAppendBuilder.append("Pointed stones float in the air around ");
                } else if (messageDetails.contains("Toxic Spikes")) {
                    toAppendBuilder.append("Toxic spikes were scattered all around the feet of ");
                } else if (messageDetails.contains("Spikes")) {
                    toAppendBuilder.append("Spikes were scattered all around the feet of ");
                } else if (messageDetails.contains("Reflect")) {
                    toAppendBuilder.append("A protective veil augments the Defense of ");
                } else if (messageDetails.contains("Light Screen")) {
                    toAppendBuilder.append("A protective veil augments the Special Defense of ");
                } else if (messageDetails.contains("Sticky Web")) {
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
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mAnimatorSetQueue == null) {
                    mAnimatorSetQueue = new ArrayDeque<>();
                }

                AnimatorSet toast = makeToast(message);

                toast.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mAnimatorSetQueue.pollFirst();
                        Animator nextOnQueue = mAnimatorSetQueue.peekFirst();
                        if (nextOnQueue != null) {
                            nextOnQueue.start();
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });

                mAnimatorSetQueue.addLast(toast);

                if (mAnimatorSetQueue.size() == 1) {
                    toast.start();
                }
            }
        });
    }

    private void startAnimation(final AnimatorSet animator) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mAnimatorSetQueue.pollFirst();
                        Animator nextOnQueue = mAnimatorSetQueue.peekFirst();
                        if (nextOnQueue != null) {
                            nextOnQueue.start();
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });

                mAnimatorSetQueue.addLast(animator);

                if (mAnimatorSetQueue.size() == 1) {
                    animator.start();
                }
            }
        });
    }

    private AnimatorSet makeToast(final Spannable message, final int duration) {
        TextView textView = (TextView) getView().findViewById(R.id.toast);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(textView, "alpha", 0f, 1f);
        fadeIn.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(textView, "alpha", 1f, 0f);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setStartDelay(duration);

        AnimatorSet animation = new AnimatorSet();
        animation.play(fadeIn);
        animation.play(fadeOut).after(fadeIn);
        animation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                TextView toast = (TextView) getView().findViewById(R.id.toast);
                if (toast != null) {
                    toast.setText(message);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        return animation;
    }

    private AnimatorSet makeToast(final String message) {
        return makeToast(message, ANIMATION_LONG);
    }

    private AnimatorSet makeToast(final String message, final int duration) {
        return makeToast(new SpannableString(message), duration);
    }

    private AnimatorSet makeToast(final Spannable message) {
        return makeToast(message, ANIMATION_LONG);
    }

    /**
     * @param player can be p1 or p2
     */
    private int getTeamPreviewSpriteId(String player, int id) {
        String p = player.substring(0, 2);
        switch (p) {
            case "p1":
                switch (id) {
                    case 0:
                        return R.id.p1a_prev;
                    case 1:
                        return R.id.p1b_prev;
                    case 2:
                        return R.id.p1c_prev;
                    case 3:
                        return R.id.p1d_prev;
                    case 4:
                        return R.id.p1e_prev;
                    case 5:
                        return R.id.p1f_prev;
                    default:
                        return 0;
                }
            case "p2":
                switch (id) {
                    case 0:
                        return R.id.p2a_prev;
                    case 1:
                        return R.id.p2b_prev;
                    case 2:
                        return R.id.p2c_prev;
                    case 3:
                        return R.id.p2d_prev;
                    case 4:
                        return R.id.p2e_prev;
                    case 5:
                        return R.id.p2f_prev;
                    default:
                        return 0;
                }
            default:
                return 0;
        }
    }

    private int getIconId(String player, int id) {
        String p = player.substring(0, 2);
        switch (p) {
            case "p1":
                switch (id) {
                    case 0:
                        return R.id.icon1;
                    case 1:
                        return R.id.icon2;
                    case 2:
                        return R.id.icon3;
                    case 3:
                        return R.id.icon4;
                    case 4:
                        return R.id.icon5;
                    case 5:
                        return R.id.icon6;
                    default:
                        return 0;
                }
            case "p2":
                switch (id) {
                    case 0:
                        return R.id.icon1_o;
                    case 1:
                        return R.id.icon2_o;
                    case 2:
                        return R.id.icon3_o;
                    case 3:
                        return R.id.icon4_o;
                    case 4:
                        return R.id.icon5_o;
                    case 5:
                        return R.id.icon6_o;
                    default:
                        return 0;
                }
            default:
                return 0;
        }
    }

}
