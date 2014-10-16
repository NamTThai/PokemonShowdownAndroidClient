package com.pokemonshowdown.app;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
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
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pokemonshowdown.data.BattleFieldData;
import com.pokemonshowdown.data.MyApplication;
import com.pokemonshowdown.data.Pokemon;

import org.w3c.dom.Text;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

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
        String[] split = messageDetails.split("\\|");

        final String position, attacker;
        int start;
        String remaining;
        final String toAppend;
        StringBuilder toAppendBuilder;
        Spannable toAppendSpannable;
        AnimatorSet toast;
        AnimatorSet animatorSet;
        Animator animator;
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
                toast = makeToast(Html.fromHtml(messageDetails).toString());
                startAnimation(toast);
                break;
            case "message":
                toast = makeToast(messageDetails);
                startAnimation(toast);
                break;
            case "gametype":
            case "gen":
                break;
            case "player":
                if (getView() == null) {
                    return;
                }

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
                        if (getView() == null) {
                            return;
                        }

                        ImageView icon = (ImageView) getView().findViewById(getIconId(playerType, iconId));
                        if (icon != null) {
                            icon.setImageResource(Pokemon.getPokemonIcon(getActivity(), MyApplication.toId(pokeName), false));
                        }
                    }
                });
                break;
            case "teampreview":
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (getView() == null) {
                            return;
                        }

                        FrameLayout frameLayout = (FrameLayout) getView().findViewById(R.id.battle_interface);
                        frameLayout.removeAllViews();
                        getActivity().getLayoutInflater().inflate(R.layout.fragment_battle_teampreview, frameLayout);
                        for(int i = 0; i < mPlayer1Team.size() ; i++) {
                            ImageView sprites = (ImageView) getView().findViewById(getTeamPreviewSpriteId("p1", i));
                            sprites.setImageResource(Pokemon.getPokemonSprite(getActivity(), MyApplication.toId(mPlayer1Team.get(i)), false));
                        }
                        for(int i = 0; i < mPlayer2Team.size() ; i++) {
                            ImageView sprites = (ImageView) getView().findViewById(getTeamPreviewSpriteId("p2", i));
                            sprites.setImageResource(Pokemon.getPokemonSprite(getActivity(), MyApplication.toId(mPlayer2Team.get(i)), false));
                        }
                    }
                });
                break;
            case "request":
                toast = makeToast(messageDetails);
                startAnimation(toast);
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
                        if (getView() == null) {
                            return;
                        }
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
                        if (getView() == null) {
                            return;
                        }
                        (getView().findViewById(R.id.inactive)).setVisibility(View.GONE);
                        (getView().findViewById(R.id.inactive_o)).setVisibility(View.GONE);
                    }
                });
                break;
            case "start":
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (getView() == null) {
                            return;
                        }

                        FrameLayout frameLayout = (FrameLayout) getView().findViewById(R.id.battle_interface);
                        frameLayout.removeAllViews();
                        getActivity().getLayoutInflater().inflate(R.layout.fragment_battle_animation, frameLayout);
                    }
                });
                break;
            case "move":
                attacker = messageDetails.substring(5, separator);
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
                toast = makeToast(toAppendSpannable);
                startAnimation(toast);
                break;
            case "switch":
            case "drag":
            case "replace":
                animatorSet = new AnimatorSet();
                final int toBeSwapped;
                final int spriteId;
                final int oldIconId;

                //TODO need to handle roar & cie
                toAppendBuilder = new StringBuilder();
                attacker = messageDetails.substring(5, separator);
                remaining = messageDetails.substring(separator + 1);
                String species, level, gender = "";
                separator = remaining.indexOf(',');
                if (separator == -1) {
                    level = "";
                    separator = remaining.indexOf('|');
                    species = remaining.substring(0, separator);
                } else {
                    species = remaining.substring(0, separator);
                    remaining = remaining.substring(separator + 2);
                    separator = remaining.indexOf(',');
                    if (separator == -1) {
                        level = remaining.substring(0, remaining.indexOf('|'));
                        if (level.length() == 1) {
                            gender = level;
                            level = "";
                        }
                    } else {
                        level = remaining.substring(0, separator);
                        gender = remaining.substring(separator + 2, separator + 3);
                    }
                }
                
                remaining = remaining.substring(remaining.indexOf('|') + 1);
                separator = remaining.indexOf(' ');
                final int hpInt;
                final String hpString;
                final String status;
                if (separator == -1) {
                    hpInt = processHpFraction(remaining);
                    status = "";
                } else {
                    hpInt = processHpFraction(remaining.substring(0, separator));
                    status = remaining.substring(separator + 1);
                }
                hpString = Integer.toString(hpInt);
                
                String speciesId = MyApplication.toId(species);

                spriteId = Pokemon.getPokemonSprite(getActivity(), speciesId, false);
                iconId = Pokemon.getPokemonIcon(getActivity(), speciesId, false);

                // Switching sprites and icons
                final String levelFinal = attacker + " " + level;
                final String genderFinal = gender;
                ArrayList<String> playerTeam = getTeam(messageDetails);
                if (playerTeam == null) {
                    playerTeam = new ArrayList<>();
                }

                if (findPokemonInTeam(playerTeam, species) == -1) {
                    playerTeam.add(species);
                    toBeSwapped = playerTeam.size() - 1;
                } else {
                    toBeSwapped = findPokemonInTeam(playerTeam, species);
                }
                Collections.swap(playerTeam, getTeamSlot(messageDetails), toBeSwapped);
                if (messageDetails.startsWith("p1")) {
                    toAppendBuilder.append("Go! ").append(attacker).append('!');
                } else {
                    toAppendBuilder.append(mPlayer2).append(" sent out ").append(attacker).append("!");
                }

                setTeam(messageDetails, playerTeam);

                toast = makeToast(new SpannableStringBuilder(toAppendBuilder));
                toast.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (getView() == null) {
                            return;
                        }

                        displayPokemon(messageDetails.substring(0, 3));

                        ImageView sprites = (ImageView) getView().findViewById(getSpriteId(messageDetails.substring(0, 3)));
                        if (sprites != null) {
                            sprites.setImageResource(spriteId);
                        }
                        ImageView iconLeader = (ImageView) getView().findViewById(getIconId(messageDetails, getTeamSlot(messageDetails)));
                        Drawable leader = iconLeader.getDrawable();
                        ImageView iconTrailer = (ImageView) getView().findViewById(getIconId(messageDetails, toBeSwapped));
                        iconTrailer.setImageDrawable(leader);
                        iconLeader.setImageResource(iconId);

                        TextView pkmName = (TextView) getView().findViewById(getSpriteNameid(messageDetails.substring(0, 3)));
                        if (pkmName != null) {
                            pkmName.setText(levelFinal);
                        }

                        ImageView gender = (ImageView) getView().findViewById(getGenderId(messageDetails.substring(0, 3)));
                        if (gender != null) {
                            if (genderFinal.equals("M")) {
                                gender.setImageResource(R.drawable.ic_gender_male);
                            } else {
                                if (genderFinal.equals("F")) {
                                    gender.setImageResource(R.drawable.ic_gender_female);
                                }
                            }
                        }

                        TextView hpText = (TextView) getView().findViewById(getHpId(messageDetails.substring(0, 3)));
                        ProgressBar hpBar = (ProgressBar) getView().findViewById(getHpBarId(messageDetails.substring(0, 3)));
                        if (hpText != null) {
                            hpText.setText(hpString);
                        }
                        if (hpBar != null) {
                            hpBar.setProgress(hpInt);
                        }

                        if (!status.equals("")) {
                            setStatus(messageDetails.substring(0, 3), status.toUpperCase());
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

                startAnimation(toast);
                break;
            case "detailschange":
                final String forme = (split[1].indexOf(',') == -1) ? split[1] : split[1].substring(0, split[1].indexOf(','));
                position = split[0].substring(0, 3);
                species = split[0].substring(5);

                replacePokemon(position, species, forme);

                toast = makeToast("Transforming", ANIMATION_SHORT);
                toast.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (getView() == null) {
                            return;
                        }

                        ImageView sprite = (ImageView) getView().findViewById(getSpriteId(position));
                        sprite.setImageResource(Pokemon.getPokemonSprite(getActivity(), MyApplication.toId(forme), false));
                        ImageView icon = (ImageView) getView().findViewById(getIconId(position));
                        icon.setImageResource(Pokemon.getPokemonIcon(getActivity(), MyApplication.toId(forme), false));
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

                startAnimation(toast);
                break;
            case "faint":
                position = split[0];
                attacker = split[0].substring(5);
                toAppendBuilder = new StringBuilder();
                if (messageDetails.startsWith("p2")) {
                    toAppendBuilder.append("The opposing ");
                }
                toAppendBuilder.append(attacker).append(" fainted!");
                toast = makeToast(new SpannableStringBuilder(toAppendBuilder));
                toast.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (getView() == null) {
                            return;
                        }

                        hidePokemon(position);
                        ImageView fainted = (ImageView) getView().findViewById(getIconId(position));
                        fainted.setImageResource(R.drawable.pokeball_unavailable);
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
                
                startAnimation(toast);
                break;
            case "turn":
                if (getView() == null) {
                    return;
                }
                TextView turn = (TextView) getView().findViewById(R.id.turn);
                animator = ObjectAnimator.ofFloat(turn, "alpha", 0f, 1f);
                toAppend = "TURN " + messageDetails;
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (getView() == null) {
                            return;
                        }
                        getView().findViewById(R.id.turn).setVisibility(View.VISIBLE);
                        ((TextView) getView().findViewById(R.id.turn)).setText(toAppend);
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
                animator.setDuration(ANIMATION_SHORT);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorSet = new AnimatorSet();
                animatorSet.play(animator);
                startAnimation(animatorSet);
                break;
            case "win":
                toAppend = messageDetails + " has won the battle!";
                toast = makeToast(new SpannableString(toAppend));
                startAnimation(toast);
                break;
            case "cant":
                //todo (cant attack bec frozen/para etc)
                break;
            default:
                toast = makeToast(message, ANIMATION_LONG);
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

                if (mAnimatorSetQueue == null) {
                    mAnimatorSetQueue = new ArrayDeque<>();
                }

                mAnimatorSetQueue.addLast(animator);

                if (mAnimatorSetQueue.size() == 1) {
                    animator.start();
                }
            }
        });
    }

    private AnimatorSet makeToast(final Spannable message, final int duration) {
        if (getView() == null) {
            return null;
        }
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
                if (getView() == null) {
                    return;
                }
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


    private int getSpriteId(String tag) {
        tag = tag.substring(0, 3);
        switch (tag) {
            case "p1a":
                return R.id.p1a_icon;
            case "p1b":
                return R.id.p1b_icon;
            case "p1c":
                return R.id.p1c_icon;
            case "p2a":
                return R.id.p2a_icon;
            case "p2b":
                return R.id.p2b_icon;
            case "p2c":
                return R.id.p2c_icon;
            default:
                return 0;
        }
    }
    
    private int getSpriteNameid(String tag) {
        tag = tag.substring(0, 3);
        switch (tag) {
            case "p1a":
                return R.id.p1a_pkm;
            case "p1b":
                return R.id.p1b_pkm;
            case "p1c":
                return R.id.p1c_pkm;
            case "p2a":
                return R.id.p2a_pkm;
            case "p2b":
                return R.id.p2b_pkm;
            case "p2c":
                return R.id.p2c_pkm;
            default:
                return 0;
        }
    }

    private int getIconId(String tag) {
        tag = tag.substring(0, 3);
        switch (tag) {
            case "p1a":
                return R.id.icon1;
            case "p1b":
                return R.id.icon2;
            case "p1c":
                return R.id.icon3;
            case "p2a":
                return R.id.icon1_o;
            case "p2b":
                return R.id.icon2_o;
            case "p2c":
                return R.id.icon3_o;
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
    
    private int getGenderId(String tag) {
        tag = tag.substring(0, 3);
        switch (tag) {
            case "p1a":
                return R.id.p1a_gender;
            case "p1b":
                return R.id.p1b_gender;
            case "p1c":
                return R.id.p1c_gender;
            case "p2a":
                return R.id.p2a_gender;
            case "p2b":
                return R.id.p2b_gender;
            case "p2c":
                return R.id.p2c_gender;
            default:
                return 0;
        }
    }
    
    private int getHpId(String tag) {
        tag = tag.substring(0, 3);
        switch (tag) {
            case "p1a":
                return R.id.p1a_hp;
            case "p1b":
                return R.id.p1b_hp;
            case "p1c":
                return R.id.p1c_hp;
            case "p2a":
                return R.id.p2a_hp;
            case "p2b":
                return R.id.p2b_hp;
            case "p2c":
                return R.id.p2c_hp;
            default:
                return 0;
        }
    }
    
    private int getHpBarId(String tag) {
        tag = tag.substring(0, 3);
        switch (tag) {
            case "p1a":
                return R.id.p1a_bar_hp;
            case "p1b":
                return R.id.p1b_bar_hp;
            case "p1c":
                return R.id.p1c_bar_hp;
            case "p2a":
                return R.id.p2a_bar_hp;
            case "p2b":
                return R.id.p2b_bar_hp;
            case "p2c":
                return R.id.p2c_bar_hp;
            default:
                return 0;
        }
    }
    
    private int getStatusId(String tag) {
        tag = tag.substring(0, 3);
        switch (tag) {
            case "p1a":
                return R.id.p1a_status;
            case "p1b":
                return R.id.p1b_status;
            case "p1c":
                return R.id.p1c_status;
            case "p2a":
                return R.id.p2a_status;
            case "p2b":
                return R.id.p2b_status;
            case "p2c":
                return R.id.p2c_status;
            default:
                return 0;
        }
    }

    private int getTeamSlot(String tag) {
        tag = Character.toString(tag.charAt(2));
        switch (tag) {
            case "a":
                return 0;
            case "b":
                return 1;
            case "c":
                return 2;
            default:
                return 0;
        }
    }

    private void setStatus(String tag, String status) {
        int id = getStatusId(tag);
        if (getView() == null) {
            return;
        }
        TextView stt = (TextView) getView().findViewById(id);
        if (stt != null) {
            stt.setText(status);
            switch (status) {
                case "slp":
                    stt.setBackgroundResource(R.drawable.editable_frame_blackwhite);
                    break;
                case "psn":
                case "tox":
                    stt.setBackgroundResource(R.drawable.editable_frame_light_purple);
                    break;
                case "brn":
                    stt.setBackgroundResource(R.drawable.editable_frame_light_red);
                    break;
                case "par":
                    stt.setBackgroundResource(R.drawable.editable_frame_light_orange);
                    break;
                case "frz":
                    stt.setBackgroundResource(R.drawable.editable_frame);
            }
        }
    }
    
    private void hidePokemon(String tag) {
        if (getView() == null) {
            return;
        }

        tag = tag.substring(0, 3);
        switch (tag) {
            case "p1a":
                getView().findViewById(R.id.p1a).setVisibility(View.GONE);
                return;
            case "p1b":
                getView().findViewById(R.id.p1b).setVisibility(View.GONE);
                return;
            case "p1c":
                getView().findViewById(R.id.p1c).setVisibility(View.GONE);
                return;
            case "p2a":
                getView().findViewById(R.id.p2a).setVisibility(View.GONE);
                return;
            case "p2b":
                getView().findViewById(R.id.p2b).setVisibility(View.GONE);
                return;
            case "p2c":
                getView().findViewById(R.id.p2c).setVisibility(View.GONE);
                return;
        }
    }

    private void displayPokemon(String tag) {
        if (getView() == null) {
            return;
        }

        tag = tag.substring(0, 3);
        switch (tag) {
            case "p1a":
                getView().findViewById(R.id.p1a).setVisibility(View.VISIBLE);
                return;
            case "p1b":
                getView().findViewById(R.id.p1b).setVisibility(View.VISIBLE);
                return;
            case "p1c":
                getView().findViewById(R.id.p1c).setVisibility(View.VISIBLE);
                return;
            case "p2a":
                getView().findViewById(R.id.p2a).setVisibility(View.VISIBLE);
                return;
            case "p2b":
                getView().findViewById(R.id.p2b).setVisibility(View.VISIBLE);
                return;
            case "p2c":
                getView().findViewById(R.id.p2c).setVisibility(View.VISIBLE);
                return;
        }
    }

    private int processHpFraction(String hpFraction) {
        int fraction = hpFraction.indexOf('/');
        if (fraction == -1) {
            return 0;
        } else {
            int remaining = Integer.parseInt(hpFraction.substring(0, fraction));
            int total = Integer.parseInt(hpFraction.substring(fraction + 1));
            return (int) (((float) remaining / (float) total) * 100);
        }
    }

    private void replacePokemon(String playerTag, String oldPkm, String newPkm) {
        if (playerTag.startsWith("p1")) {
            int index = findPokemonInTeam(mPlayer1Team, oldPkm);
            if (index != -1) {
                mPlayer1Team.set(index, newPkm);
            }
        } else {
            int index = findPokemonInTeam(mPlayer2Team, oldPkm);
            if (index != -1) {
                mPlayer2Team.set(index, newPkm);
            }
        }
    }
    
    private ArrayList<String> getTeam(String playerTag) {
        if (playerTag.startsWith("p1")) {
            return mPlayer1Team;
        } else {
            return mPlayer2Team;
        }
    }

    private void setTeam(String playerTag, ArrayList<String> playerTeam) {
        if (playerTag.startsWith("p1")) {
            mPlayer1Team = playerTeam;
        } else {
            mPlayer2Team = playerTeam;
        }
    }

    private int findPokemonInTeam(ArrayList<String> playerTeam, String pkm) {
        for (int i = 0; i < playerTeam.size(); i++) {
            if (playerTeam.get(i).contains(pkm)) {
                return i;
            }
        }
        return -1;
    }

}
