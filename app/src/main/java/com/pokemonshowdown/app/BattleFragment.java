package com.pokemonshowdown.app;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pokemonshowdown.data.BattleAnimation;
import com.pokemonshowdown.data.BattleFieldData;
import com.pokemonshowdown.data.MoveDex;
import com.pokemonshowdown.data.MyApplication;
import com.pokemonshowdown.data.Pokemon;
import com.pokemonshowdown.data.PokemonInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class BattleFragment extends Fragment {
    public final static String BTAG = BattleFragment.class.getName();
    public final static String ROOM_ID = "Room Id";
    public final static int ANIMATION_SHORT = 500;
    public final static int ANIMATION_LONG = 1000;
    public final static int[] BACKGROUND_LIBRARY = {R.drawable.bg, R.drawable.bg_beach, R.drawable.bg_beachshore, R.drawable.bg_city, R.drawable.bg_desert, R.drawable.bg_earthycave, R.drawable.bg_forest, R.drawable.bg_icecave, R.drawable.bg_meadow, R.drawable.bg_river, R.drawable.bg_route};
    public final static String[] stats = {"atk", "def", "spa", "spd", "spe", "accuracy", "evasion"};
    public final static String[] sttus = {"psn", "tox", "frz", "par", "slp", "brn"};
    public final static String[][] teammates = {{"p1a", "p1b", "p1c"}, {"p2a", "p2b", "p2c"}};

    public ArrayDeque<AnimatorSet> mAnimatorSetQueue;
    public int[] progressBarHolder = new int[6];

    public String mRoomId;
    /**
     * 0 if it's a simple watch battle
     * 1 if player is p1
     * -1 if player is p2
     */
    public int mBattling;
    public String mPlayer1;
    public String mPlayer2;
    public HashMap<Integer, PokemonInfo> mPlayer1Team;
    public HashMap<Integer, PokemonInfo> mPlayer2Team;

    public String currentWeather;
    public boolean weatherExist;
    public int turnNumber;
    public boolean myTurn;

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

        mBattling = 0;

        int id = new Random().nextInt(BACKGROUND_LIBRARY.length);
        ((ImageView) view.findViewById(R.id.battle_background)).setImageResource(BACKGROUND_LIBRARY[id]);

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

    private void setBattling(JSONObject object) throws JSONException {
        String side = object.getJSONObject("side").getString("id");
        if (side.equals("p1")) {
            mBattling = 1;
        } else {
            mBattling = -1;
            switchUpPlayer();
        }
    }

    private void setDisplayTeam(JSONObject object) throws JSONException {
        object = object.getJSONObject("side");
        JSONArray team = object.getJSONArray("pokemon");
        for (int i = 0; i < team.length(); i++) {
            JSONObject info = team.getJSONObject(i);
            PokemonInfo pkm = parsePokemonInfo(info);
            mPlayer1Team.put(i, pkm);
        }
    }

    private PokemonInfo parsePokemonInfo(JSONObject info) throws JSONException {
        String details = info.getString("details");
        String name = !details.contains(",") ? details : details.substring(0, details.indexOf(","));
        PokemonInfo pkm = new PokemonInfo(getActivity(), name);
        String nickname = info.getString("ident").substring(4);
        pkm.setNickname(nickname);
        if (details.contains(", L")) {
            String level = details.substring(details.indexOf(", L") + 3);
            level = !level.contains(",") ? level : level.substring(0, level.indexOf(","));
            pkm.setLevel(Integer.parseInt(level));
        }
        if (details.contains(", M")) {
            pkm.setGender("M");
        } else {
            if (details.contains(", F")) {
                pkm.setGender("F");
            }
        }
        if (details.contains("shiny")) {
            pkm.setShiny(true);
        }
        String hp = info.getString("condition");
        pkm.setHp(processHpFraction(hp));
        pkm.setActive(info.getBoolean("active"));
        JSONObject statsArray = info.getJSONObject("stats");
        int[] stats = new int[5];
        stats[0] = statsArray.getInt("atk");
        stats[1] = statsArray.getInt("def");
        stats[2] = statsArray.getInt("spa");
        stats[3] = statsArray.getInt("spd");
        stats[4] = statsArray.getInt("spe");
        pkm.setStats(stats);
        JSONArray movesArray = info.getJSONArray("moves");
        HashMap<String, Integer> moves = new HashMap<>();
        for (int i = 0; i < movesArray.length(); i++) {
            String move = movesArray.getString(i);
            JSONObject ppObject = MoveDex.get(getActivity()).getMoveJsonObject(move);
            if (ppObject == null) {
                moves.put(move, 0);
            } else {
                moves.put(move, ppObject.getInt("pp"));
            }
        }
        pkm.setMoves(moves);
        pkm.setAbility("baseAbility");
        pkm.setItem("item");
        try {
            pkm.setCanMegaEvo(info.getBoolean("canMegaEvo"));
        } catch (JSONException e) {
            pkm.setCanMegaEvo(false);
        }
        return pkm;
    }

    private void switchUpPlayer() {
        // Switch player name
        if (getView() == null) {
            return;
        }

        String holderString = mPlayer1;
        mPlayer1 = mPlayer2;
        mPlayer2 = holderString;

        HashMap<Integer, PokemonInfo> holderTeam = mPlayer1Team;
        mPlayer1Team = mPlayer2Team;
        mPlayer2Team = holderTeam;

        // Switch player avatar
        Drawable holderDrawable = ((ImageView) getView().findViewById(R.id.avatar)).getDrawable();
        ((ImageView) getView().findViewById(R.id.avatar)).setImageDrawable(((ImageView) getView().findViewById(R.id.avatar_o)).getDrawable());
        ((ImageView) getView().findViewById(R.id.avatar_o)).setImageDrawable(holderDrawable);
    }

    public void processServerMessage(String message) {
        if (mBattling == -1) {
            message = message.replace("p1", "p3").replace("p2", "p1").replace("p3", "p2");
        }
        processMajorAction(message);
    }

    public void processMajorAction(final String message) {
        BattleFieldData.AnimationData animationData = BattleFieldData.get(getActivity()).getAnimationInstance(mRoomId);
        String command = (message.indexOf('|') == -1) ? message : message.substring(0, message.indexOf('|'));
        final String messageDetails = message.substring(message.indexOf('|') + 1);
        if (command.startsWith("-")) {
            processMinorAction(command, messageDetails);
            return;
        }

        int separator = messageDetails.indexOf('|');
        final String[] split = messageDetails.split("\\|");

        final String position, attacker;
        int start;
        String remaining;
        final String toAppend;
        StringBuilder toAppendBuilder;
        Spannable toAppendSpannable;
        AnimatorSet toast;
        AnimatorSet animatorSet;
        Animator animator;

        Spannable logMessage = new SpannableString("");
        switch (command) {
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
            case "tc":
            case "c:":
                String user = messageDetails.substring(0, separator);
                String userMessage = messageDetails.substring(separator + 1);
                toAppend = user + ": " + userMessage;
                toAppendSpannable = new SpannableString(toAppend);
                toAppendSpannable.setSpan(new ForegroundColorSpan(ChatRoomFragment.getColorStrong(user)), 0, user.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                logMessage = new SpannableString(toAppendSpannable);
                break;
            case "raw":
                toast = makeToast(Html.fromHtml(messageDetails).toString());
                startAnimation(toast);
                logMessage = new SpannableString(Html.fromHtml(messageDetails).toString());
                break;
            case "message":
                toast = makeToast(messageDetails);
                startAnimation(toast);
                logMessage = new SpannableString(messageDetails);
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
                toAppend = "Format:" + "\n" + messageDetails;
                toAppendSpannable = new SpannableString(toAppend);
                toAppendSpannable.setSpan(new StyleSpan(Typeface.BOLD), toAppend.indexOf('\n') + 1, toAppend.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                logMessage = new SpannableString(toAppendSpannable);
                break;

            case "rated":
                toAppend = command.toUpperCase();
                toAppendSpannable = new SpannableString(toAppend);
                toAppendSpannable.setSpan(new ForegroundColorSpan(R.color.dark_blue), 0, toAppend.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                logMessage = new SpannableString(toAppendSpannable);
                break;

            case "rule":
                toAppendSpannable = new SpannableString(messageDetails);
                toAppendSpannable.setSpan(new StyleSpan(Typeface.ITALIC), 0, messageDetails.indexOf(':') + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                logMessage = new SpannableString(toAppendSpannable);
                break;

            case "":
                logMessage = new SpannableString(" ");
                break;

            case "clearpoke":
                mPlayer1Team = new HashMap<>();
                mPlayer2Team = new HashMap<>();
                break;

            case "poke":
                playerType = messageDetails.substring(0, separator);
                int comma = messageDetails.indexOf(',');
                final String pokeName = (comma == -1) ? messageDetails.substring(separator + 1) :
                        messageDetails.substring(separator + 1, comma);
                final int iconId;
                if (playerType.equals("p1")) {
                    iconId = mPlayer1Team.size();
                    mPlayer1Team.put(iconId, new PokemonInfo(getActivity(), pokeName));
                } else {
                    iconId = mPlayer2Team.size();
                    mPlayer2Team.put(iconId, new PokemonInfo(getActivity(), pokeName));
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
                        for (int i = 0; i < mPlayer1Team.size(); i++) {
                            ImageView sprites = (ImageView) getView().findViewById(getTeamPreviewSpriteId("p1", i));
                            sprites.setImageResource(Pokemon.getPokemonSprite(getActivity(), MyApplication.toId(mPlayer1Team.get(i).getName()), false, true, false, false));
                        }
                        for (int i = 0; i < mPlayer2Team.size(); i++) {
                            ImageView sprites = (ImageView) getView().findViewById(getTeamPreviewSpriteId("p2", i));
                            sprites.setImageResource(Pokemon.getPokemonSprite(getActivity(), MyApplication.toId(mPlayer2Team.get(i).getName()), false, false, false, false));
                        }
                    }
                });
                toAppendBuilder = new StringBuilder();
                toAppendBuilder.append(mPlayer1).append("'s Team: ");
                String[] p1Team = getTeamName(mPlayer1Team);
                for (int i = 0; i < p1Team.length - 1; i++) {
                    toAppendBuilder.append(p1Team[i]).append("/");
                }
                toAppendBuilder.append(p1Team[p1Team.length - 1]);

                toAppendBuilder.append("\n").append(mPlayer2).append("'s Team: ");
                String[] p2Team = getTeamName(mPlayer2Team);
                for (int i = 0; i < p2Team.length - 1; i++) {
                    toAppendBuilder.append(p2Team[i]).append("/");
                }
                toAppendBuilder.append(p2Team[p2Team.length - 1]);
                toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                logMessage = new SpannableString(toAppendSpannable);
                break;

            case "request":
                try {
                    JSONObject requestJson = new JSONObject(messageDetails);
                    if (requestJson.length() == 1 && requestJson.keys().next().equals("side")) {
                        setBattling(requestJson);
                        setDisplayTeam(requestJson);
                    }
                } catch (JSONException e) {
                    new AlertDialog.Builder(getActivity())
                            .setMessage(R.string.request_error)
                            .create()
                            .show();
                    return;
                }
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
                toAppendSpannable = new SpannableString(messageDetails);
                toAppendSpannable.setSpan(new ForegroundColorSpan(R.color.dark_red), 0, messageDetails.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                logMessage = new SpannableString(toAppendSpannable);
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
                toAppendSpannable = new SpannableString(messageDetails);
                toAppendSpannable.setSpan(new ForegroundColorSpan(R.color.dark_red), 0, messageDetails.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                logMessage = new SpannableString(toAppendSpannable);
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
                toAppend = mPlayer1 + " vs. " + mPlayer2;
                toAppendSpannable = new SpannableString(toAppend);
                toAppendSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, toAppend.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                logMessage = new SpannableString(toAppendSpannable);
                break;
            case "move":
                // todo useMove line 2747 battle.js
                attacker = messageDetails.substring(5, separator);
                toAppendBuilder = new StringBuilder();
                if (messageDetails.startsWith("p2")) {
                    toAppendBuilder.append("The opposing's ");
                }
                toAppendBuilder.append(attacker).append(" used ");
                final String move = split[1];
                toAppendBuilder.append(move).append("!");
                toAppend = toAppendBuilder.toString();
                start = toAppend.indexOf(move);
                toAppendSpannable = new SpannableString(toAppend);
                toAppendSpannable.setSpan(new StyleSpan(Typeface.BOLD), start, start + move.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                logMessage = toAppendSpannable;
                toast = makeToast(logMessage);

                toast.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (getView() == null) {
                            return;
                        }
                        AnimatorSet animatorSet = BattleAnimation.processMove(move, getView(), BattleFragment.this, split);
                        animatorSet.start();
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

            case "switch":
            case "drag":
            case "replace":
                final int toBeSwapped;
                final int spriteId;

                //TODO need to handle roar & cie
                toAppendBuilder = new StringBuilder();
                attacker = messageDetails.substring(5, separator);
                remaining = messageDetails.substring(separator + 1);
                final boolean shiny;
                final boolean female;
                final boolean back = messageDetails.startsWith("p1");
                String species, level, gender = "";
                separator = remaining.indexOf(',');
                if (separator == -1) {
                    level = "";
                    shiny = false;
                    female = false;
                    separator = remaining.indexOf('|');
                    species = remaining.substring(0, separator);
                } else {
                    species = remaining.substring(0, separator);
                    remaining = remaining.substring(separator + 2);
                    if (remaining.contains("M")) {
                        gender = "M";
                    }
                    if (remaining.contains("F")) {
                        gender = "F";
                    }
                    shiny = remaining.contains("shiny");
                    female = remaining.contains("F");
                    if (remaining.contains(", L")) {
                        level = remaining.substring(remaining.indexOf(", L") + 2);
                        separator = level.indexOf(",");
                        level = (separator == -1) ? level.substring(0, level.indexOf('|')) : level.substring(0, separator);
                    } else {
                        level = "";
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
                setOldHp(messageDetails, hpInt);
                hpString = Integer.toString(hpInt);

                String speciesId = MyApplication.toId(species);

                spriteId = Pokemon.getPokemonSprite(getActivity(), speciesId, false, back, female, shiny);
                iconId = Pokemon.getPokemonIcon(getActivity(), speciesId, false);

                // Switching sprites and icons
                final String levelFinal = attacker + " " + level;
                final String genderFinal = gender;
                HashMap<Integer, PokemonInfo> playerTeam = getTeam(messageDetails);
                if (playerTeam == null) {
                    playerTeam = new HashMap<>();
                }
                ArrayList<String> teamName = getTeamNameArrayList(playerTeam);
                if (findPokemonInTeam(teamName, species) == -1) {
                    playerTeam.put(playerTeam.size(), new PokemonInfo(getActivity(), species));
                    toBeSwapped = playerTeam.size() - 1;
                } else {
                    toBeSwapped = findPokemonInTeam(teamName, species);
                }
                int j = getTeamSlot(messageDetails);
                playerTeam.put(7, playerTeam.get(j));
                playerTeam.put(j, playerTeam.get(toBeSwapped));
                playerTeam.put(toBeSwapped, playerTeam.get(7));
                playerTeam.remove(7);

                setTeam(messageDetails, playerTeam);

                if(command.equals("switch")) {
                    //TODO need to buffer batonpass/uturn/voltswitch for switching out message
                    //then we switch in
                    if(messageDetails.startsWith("p2")) {
                        toAppendBuilder.append(mPlayer2).append(" sent out ").append(species).append("!");
                    } else {
                        toAppendBuilder.append("Go! ").append(species).append("!");
                    }
                } else {
                    if (command.equals("drag")) {
                        toAppendBuilder.append(species).append(" was dragged out!");
                    } else { //replace, no text here (illusion mons)
                    }
                }


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
                            setAddonStatus(messageDetails.substring(0, 3), status.toLowerCase());
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
                logMessage = new SpannableString(toAppendBuilder);
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

                        boolean back = split[0].startsWith("p1");
                        ImageView sprite = (ImageView) getView().findViewById(getSpriteId(position));
                        sprite.setImageResource(Pokemon.getPokemonSprite(getActivity(), MyApplication.toId(forme), false, back, false, false));
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
                logMessage = new SpannableString(toAppendBuilder);
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
                        (getView().findViewById(R.id.inactive)).setVisibility(View.GONE);
                        (getView().findViewById(R.id.inactive_o)).setVisibility(View.GONE);
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
                toAppendSpannable = new SpannableString(toAppend.toUpperCase());
                toAppendSpannable.setSpan(new UnderlineSpan(), 0, toAppend.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                toAppendSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, toAppend.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                toAppendSpannable.setSpan(new RelativeSizeSpan(1.25f), 0, toAppend.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                toAppendSpannable.setSpan(new ForegroundColorSpan(R.color.dark_blue), 0, toAppend.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                logMessage = new SpannableString(toAppendSpannable);
                break;

            case "win":
                toAppend = messageDetails + " has won the battle!";
                toast = makeToast(new SpannableString(toAppend));
                startAnimation(toast);
                logMessage = new SpannableString(toAppend);
                break;

            case "cant":
                String attackerOutputName = getPrintableOutputPokemonSide(split[0]);
                toAppendBuilder = new StringBuilder();
                switch (getPrintable(toId(split[1]))) {
                    case "taunt":
                        toAppendBuilder.append(attackerOutputName).append(" can't use ").append(getPrintable(split[2])).append(" after the taunt!");
                        break;

                    case "gravity":
                        toAppendBuilder.append(attackerOutputName).append(" can't use ").append(getPrintable(split[2])).append(" because of gravity!");
                        break;

                    case "healblock":
                        toAppendBuilder.append(attackerOutputName).append(" can't use ").append(getPrintable(split[2])).append(" because of Heal Block!");
                        break;

                    case "imprison":
                        toAppendBuilder.append(attackerOutputName).append(" can't use the sealed ").append(getPrintable(split[2])).append("!");
                        break;

                    case "par":
                        toAppendBuilder.append(attackerOutputName).append(" is paralyzed! It can't move!");
                        break;

                    case "frz":
                        toAppendBuilder.append(attackerOutputName).append(" is frozen solid!'");
                        break;

                    case "slp":
                        toAppendBuilder.append(attackerOutputName).append(" is fast asleep.");
                        break;

                    case "skydrop":
                        attackerOutputName = getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append("Sky Drop won't let ").append(attackerOutputName).append(" is paralyzed! It can't move!");
                        break;

                    case "truant":
                        toAppendBuilder.append(attackerOutputName).append(" is loafing around!");
                        break;

                    case "recharge":
                        toAppendBuilder.append(attackerOutputName).append(" must recharge!");
                        break;

                    case "focuspunch":
                        toAppendBuilder.append(attackerOutputName).append(" lost its focus and couldn't move!");
                        break;

                    case "flinch":
                        toAppendBuilder.append(attackerOutputName).append(" flinched and couldn't move!");
                        break;

                    case "attract":
                        toAppendBuilder.append(attackerOutputName).append(" is immobilized by love!");
                        break;

                    case "nopp":
                        toAppendBuilder.append(attackerOutputName).append(" used ").append(getPrintable(split[2]));
                        toAppendBuilder.append("\nBut there was no PP left for the move!");
                        break;

                    default:
                        toAppendBuilder.append(attackerOutputName);
                        if(split.length > 2) {
                            toAppendBuilder.append(" can't use ").append(getPrintable(split[2]));
                        } else {
                            toAppendBuilder.append(" can't move");
                        }
                        toAppendBuilder.append("!");
                        break;
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = makeMinorToast(logMessage);
                animatorSet = createFlyingMessage(split[0], toast, new SpannableString("Failed!"));
                startAnimation(animatorSet);
                break;

            default:
                toast = makeToast(message, ANIMATION_LONG);
                startAnimation(toast);
                logMessage = new SpannableString(message);
                break;
        }

        addToLog(logMessage);
    }


    public void processMinorAction(String command, final String messageDetails) {
        int separator;
        Integer oldHP;
        final int lostHP;
        final int intAmount;
        String remaining;
        String toAppend;
        StringBuilder toAppendBuilder = new StringBuilder();
        Spannable toAppendSpannable;
        Spannable logMessage = new SpannableString("");
        String move, ability;
        boolean flag, eat, weaken;

        String fromEffect = null;
        String fromEffectId = null;
        String ofSource = null;
        String trimmedOfEffect = null;

        String attacker, defender, side, stat, statAmount;
        String attackerOutputName;
        String defenderOutputName;

        int from = messageDetails.indexOf("[from]");
        if (from != -1) {
            remaining = messageDetails.substring(from + 7);
            separator = remaining.indexOf('|');
            fromEffect = (separator == -1) ? remaining : remaining.substring(0, separator);
            //trim
            fromEffectId = toId(fromEffect);
        }
        int of = messageDetails.indexOf("[of]");
        if (of != -1) {
            remaining = messageDetails.substring(of + 5);
            separator = remaining.indexOf('|');
            ofSource = (separator == -1) ? remaining : remaining.substring(remaining.indexOf(':'), separator);

            trimmedOfEffect = toId(ofSource);
        }

        final String[] split = messageDetails.split("\\|");

        AnimatorSet toast;
        AnimatorSet animatorSet;

        if (getView() == null) {
            return;
        }

        switch (command) {
            case "-damage":
                attackerOutputName = getPrintableOutputPokemonSide(split[0]);
                oldHP = getOldHp(messageDetails);
                remaining = (split[1].indexOf(' ') == -1) ? split[1] : split[1].substring(0, split[1].indexOf(' '));
                intAmount = processHpFraction(remaining);
                setOldHp(messageDetails, intAmount);
                lostHP = intAmount - oldHP;

                if (fromEffectId != null) {
                    switch (getPrintable(fromEffectId)) {
                        case "stealthrock":
                            attackerOutputName = getPrintableOutputPokemonSide(split[0], false);
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
                            attackerOutputName = getPrintableOutputPokemonSide(split[0], false);
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
                                toAppendBuilder.append(attackerOutputName).append(" is hurt by ").append(getPrintable(ofSource)).append("'s ").append(getPrintable(fromEffect)).append("!");
                            } else if (fromEffectId.contains(":")) {
                                toAppendBuilder.append(attackerOutputName).append(" is hurt by its").append(getPrintable(fromEffect)).append("!");
                            } else {
                                toAppendBuilder.append(attackerOutputName).append(" lost some HP because of ").append(getPrintable(fromEffect)).append("!");
                            }
                            break;
                    }
                } else {
                    toAppendBuilder.append(attackerOutputName).append(" lost ");
                    toAppendBuilder.append(- lostHP).append("% of its health!");
                }

                toast = makeMinorToast(new SpannableStringBuilder(toAppendBuilder));

                final TextView damage = new TextView(getActivity());
                damage.setText(lostHP + "%");
                damage.setBackgroundResource(R.drawable.editable_frame_light_red);
                damage.setPadding(2, 2, 2, 2);
                damage.setAlpha(0f);

                toast.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (getView() == null) {
                            return;
                        }
                        ((TextView) getView().findViewById(getHpId(messageDetails))).setText(Integer.toString(intAmount));

                        ImageView imageView = (ImageView) getView().findViewById(getSpriteId(messageDetails));

                        RelativeLayout relativeLayout = (RelativeLayout) getView().findViewById(getPkmLayoutId(messageDetails));
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        layoutParams.addRule(RelativeLayout.ALIGN_TOP, getSpriteId(messageDetails));
                        layoutParams.addRule(RelativeLayout.ALIGN_LEFT, getSpriteId(messageDetails));
                        layoutParams.setMargins((int) (imageView.getWidth() * 0.5f), (int) (imageView.getHeight() * 0.5f), 0, 0);
                        relativeLayout.addView(damage, layoutParams);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (getView() == null) {
                            return;
                        }

                        RelativeLayout relativeLayout = (RelativeLayout) getView().findViewById(getPkmLayoutId(messageDetails));
                        relativeLayout.removeView(damage);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });

                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(damage, "alpha", 0f, 1f);
                fadeIn.setInterpolator(new DecelerateInterpolator());
                fadeIn.setDuration(ANIMATION_SHORT / 4);

                ObjectAnimator fadeOut = ObjectAnimator.ofFloat(damage, "alpha", 1f, 0f);
                fadeOut.setInterpolator(new AccelerateInterpolator());
                fadeOut.setStartDelay(ANIMATION_SHORT / 2);
                fadeOut.setDuration(ANIMATION_SHORT / 4);

                ProgressBar hpBar = (ProgressBar) getView().findViewById(getHpBarId(messageDetails));
                ObjectAnimator hpCountDownBar = ObjectAnimator.ofInt(hpBar, "progress", intAmount);
                hpCountDownBar.setDuration(ANIMATION_SHORT);
                hpCountDownBar.setInterpolator(new AccelerateDecelerateInterpolator());

                animatorSet = new AnimatorSet();
                animatorSet.play(toast);
                animatorSet.play(hpCountDownBar).with(toast);
                animatorSet.play(fadeIn).with(toast);
                animatorSet.play(fadeOut).after(fadeIn);

                startAnimation(animatorSet);

                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-heal":
                attackerOutputName = getPrintableOutputPokemonSide(split[0]);

                oldHP = getOldHp(messageDetails);

                remaining = (split[1].indexOf(' ') == -1) ? split[1] : split[1].substring(0, split[1].indexOf(' '));
                intAmount = processHpFraction(remaining);
                setOldHp(messageDetails, intAmount);
                lostHP = intAmount - oldHP;

                if (fromEffectId != null) {
                    switch (getPrintable(fromEffectId)) {
                        case "ingrain":
                            toAppendBuilder.append(attackerOutputName).append(" absorbed nutrients with its roots!");
                            break;
                        case "aquaring":
                            attackerOutputName = getPrintableOutputPokemonSide(split[0], false);
                            toAppendBuilder.append("Aqua Ring restored ").append(attackerOutputName).append("'s HP!");
                            break;
                        case "raindish":
                        case "dryskin":
                        case "icebody":
                            toAppendBuilder.append(attackerOutputName).append("'s ").append(getPrintable(fromEffect)).append(" heals it!");
                            break;
                        case "healingwish":
                            attackerOutputName = getPrintableOutputPokemonSide(split[0], false);
                            toAppendBuilder.append("The healing wish came true for ").append(attackerOutputName);
                            break;
                        case "lunardance":
                            toAppendBuilder.append(attackerOutputName).append(" became cloaked in mystical moonlight!");
                            break;
                        case "wish":
                            //TODO TRY
                            String wisher;
                            if (messageDetails.contains("[wisher]")) {
                                separator = messageDetails.substring(messageDetails.indexOf("[wisher]")).indexOf("|");
                                if (separator != -1) {
                                    wisher = messageDetails.substring(messageDetails.indexOf("[wisher]") + 8, separator);
                                } else {
                                    wisher = messageDetails.substring(messageDetails.indexOf("[wisher]") + 8);
                                }
                                toAppendBuilder.append(getPrintableOutputPokemonSide(wisher)).append("'s wish came true!");
                            }
                            break;
                        case "drain":
                            if (trimmedOfEffect != null) {
                                toAppendBuilder.append(getPrintableOutputPokemonSide(ofSource)).append(" had its energy drained!");
                                break;
                            }
                            // we should never enter here
                            toAppendBuilder.append(attackerOutputName).append(" drained health!");
                            break;

                        case "leftovers":
                        case "shellbell":
                            toAppendBuilder.append(attackerOutputName).append(" restored a little HP using its ").append(getPrintable(fromEffect)).append("!");
                            break;
                        default:
                            toAppendBuilder.append(attackerOutputName).append(" restored HP using its ").append(getPrintable(fromEffect)).append("!");
                            break;
                    }
                } else {
                    toAppendBuilder.append(attackerOutputName);
                    toAppendBuilder.append(" healed ").append(lostHP).append("% of it's health!");
                }

                toast = makeMinorToast(new SpannableStringBuilder(toAppendBuilder));

                final TextView heal = new TextView(getActivity());
                heal.setText(lostHP + "%");
                heal.setBackgroundResource(R.drawable.editable_frame_light_green);
                heal.setPadding(2, 2, 2, 2);
                heal.setAlpha(0f);

                toast.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (getView() == null) {
                            return;
                        }
                        ((TextView) getView().findViewById(getHpId(messageDetails))).setText(Integer.toString(intAmount));

                        ImageView imageView = (ImageView) getView().findViewById(getSpriteId(messageDetails));

                        RelativeLayout relativeLayout = (RelativeLayout) getView().findViewById(getPkmLayoutId(messageDetails));
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        layoutParams.addRule(RelativeLayout.ALIGN_TOP, getSpriteId(messageDetails));
                        layoutParams.addRule(RelativeLayout.ALIGN_LEFT, getSpriteId(messageDetails));
                        layoutParams.setMargins((int) (imageView.getWidth() * 0.5f), (int) (imageView.getHeight() * 0.5f), 0, 0);
                        relativeLayout.addView(heal, layoutParams);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (getView() == null) {
                            return;
                        }

                        RelativeLayout relativeLayout = (RelativeLayout) getView().findViewById(getPkmLayoutId(messageDetails));
                        relativeLayout.removeView(heal);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });

                fadeIn = ObjectAnimator.ofFloat(heal, "alpha", 0f, 1f);
                fadeIn.setInterpolator(new DecelerateInterpolator());
                fadeIn.setDuration(ANIMATION_SHORT / 4);

                fadeOut = ObjectAnimator.ofFloat(heal, "alpha", 1f, 0f);
                fadeOut.setInterpolator(new AccelerateInterpolator());
                fadeOut.setStartDelay(ANIMATION_SHORT / 2);
                fadeOut.setDuration(ANIMATION_SHORT / 4);

                hpBar = (ProgressBar) getView().findViewById(getHpBarId(messageDetails));
                hpCountDownBar = ObjectAnimator.ofInt(hpBar, "progress", intAmount);
                hpCountDownBar.setDuration(ANIMATION_SHORT);
                hpCountDownBar.setInterpolator(new AccelerateDecelerateInterpolator());

                animatorSet = new AnimatorSet();
                animatorSet.play(toast);
                animatorSet.play(hpCountDownBar).with(toast);
                animatorSet.play(fadeIn).with(toast);
                animatorSet.play(fadeOut).after(fadeIn);

                startAnimation(animatorSet);

                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;
            case "-sethp":
                switch (getPrintable(fromEffectId)) {
                    case "painsplit":
                        toAppendBuilder.append("The battlers shared their pain!");
                        toast = makeMinorToast(new SpannableString(toAppendBuilder));

                        toast.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                if (getView() == null) {
                                    return;
                                }
                                int pkmAHp = processHpFraction(split[1]);
                                int pkmBHp = processHpFraction(split[3]);

                                ((TextView) getView().findViewById(getHpId(split[0]))).setText(Integer.toString(pkmAHp));
                                ((TextView) getView().findViewById(getHpId(split[2]))).setText(Integer.toString(pkmBHp));

                                ProgressBar pkmAHpBar = (ProgressBar) getView().findViewById(getHpBarId(split[0]));
                                ObjectAnimator pkmACountDown = ObjectAnimator.ofInt(pkmAHpBar, "progress", pkmAHp);
                                pkmACountDown.setDuration(ANIMATION_SHORT);
                                pkmACountDown.setInterpolator(new AccelerateDecelerateInterpolator());
                                ProgressBar pkmBHpBar = (ProgressBar) getView().findViewById(getHpBarId(split[2]));
                                ObjectAnimator pkmBCountDown = ObjectAnimator.ofInt(pkmBHpBar, "progress", pkmBHp);
                                pkmBCountDown.setDuration(ANIMATION_SHORT);
                                pkmBCountDown.setInterpolator(new AccelerateDecelerateInterpolator());
                                pkmACountDown.start();
                                pkmBCountDown.start();
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
                }
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;
            case "-boost":
                attackerOutputName = getPrintableOutputPokemonSide(split[0]);
                stat = split[1];
                final String increasedStat;
                increasedStat = stat;
                statAmount = "";
                switch (stat) {
                    case "atk":
                        stat = "Attack";
                        break;
                    case "def":
                        stat = "Defense";
                        break;
                    case "spa":
                        stat = "Special Attack";
                        break;
                    case "spd":
                        stat = "Special Defense";
                        break;
                    case "spe":
                        stat = "Speed";
                        break;
                    default:
                        break;
                }
                String amount = split[2];
                intAmount = Integer.parseInt(amount);
                if (intAmount == 2) {
                    statAmount = " sharply";
                } else if (intAmount > 2) {
                    statAmount = " drastically";
                }

                if (fromEffect != null) {
                    if (fromEffect.contains("item:")) {
                        attackerOutputName = getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append("The ").append(getPrintable(fromEffect)).append(statAmount).append(" raised ").append(attackerOutputName).append("'s ").append(stat).append("!");
                    } else {
                        toAppendBuilder.append(attackerOutputName).append("'s ").append(getPrintable(fromEffect)).append(statAmount).append(" raised its ").append(stat).append("!");
                    }
                } else {
                    toAppendBuilder.append(attackerOutputName).append("'s ").append(stat).append(statAmount).append(" rose!");
                }

                toast = makeMinorToast(new SpannableStringBuilder(toAppendBuilder));

                toast.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        processBoost(messageDetails, increasedStat, intAmount);
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
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;
            case "-unboost":
                attackerOutputName = getPrintableOutputPokemonSide(split[0]);
                stat = split[1];
                increasedStat = stat;
                statAmount = "";

                switch (stat) {
                    case "atk":
                        stat = "Attack";
                        break;
                    case "def":
                        stat = "Defense";
                        break;
                    case "spa":
                        stat = "Special Attack";
                        break;
                    case "spd":
                        stat = "Special Defense";
                        break;
                    case "spe":
                        stat = "Speed";
                        break;
                    default:
                        break;
                }
                amount = split[2];
                intAmount = -1 * Integer.parseInt(amount);
                if (intAmount == -2) {
                    statAmount = " harshly";
                } else if (intAmount <= -3) {
                    statAmount = " severely";
                }

                if (fromEffect != null) {
                    if (fromEffect.contains("item:")) {
                        attackerOutputName = getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append("The ").append(getPrintable(fromEffect)).append(statAmount).append(" lowered ").append(attackerOutputName).append("'s ").append(stat).append("!");
                    } else {
                        toAppendBuilder.append(attackerOutputName).append("'s ").append(getPrintable(fromEffect)).append(statAmount).append(" lowered its ").append(stat).append("!");
                    }
                } else {
                    toAppendBuilder.append(attackerOutputName).append("'s ").append(stat).append(statAmount).append(" fell!");
                }

                toast = makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                toast.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        processBoost(messageDetails, increasedStat, intAmount);
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
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;
            case "-setboost":
                attackerOutputName = getPrintableOutputPokemonSide(split[0]);
                if (fromEffect != null) {
                    switch (getPrintable(fromEffectId)) {
                        case "bellydrum":
                            toAppendBuilder.append(attackerOutputName).append(" cut its own HP and maximized its Attack!");
                            toast = makeMinorToast(new SpannableString(toAppendBuilder));
                            toast.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    processBoost(split[0], "atk", 6);
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

                        case "angerpoint":
                            toAppendBuilder.append(attackerOutputName).append(" maxed its Attack!");
                            toast = makeMinorToast(new SpannableString(toAppendBuilder));
                            toast.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    processBoost(split[0], "atk", 6);
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
                    }
                }
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;
            case "-swapboost":
                attackerOutputName = getPrintableOutputPokemonSide(split[0]);
                if (fromEffect != null) {
                    switch (getPrintable(fromEffectId)) {
                        case "guardswap":
                            toAppendBuilder.append(attackerOutputName).append(" switched all changes to its Defense and Sp. Def with the target!");
                            toast = makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                            toast.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    swapBoost(split[0], split[1], "def", "spd");
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

                        case "heartswap":
                            toAppendBuilder.append(attackerOutputName).append(" switched stat changes with the target!");
                            toast = makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                            toast.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    swapBoost(split[0], split[1], stats);
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

                        case "powerswap":
                            toAppendBuilder.append(attackerOutputName).append(" switched all changes to its Attack and Sp. Atk with the target!");
                            toast = makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                            toast.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    swapBoost(split[0], split[1], "atk", "spa");
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
                    }
                    logMessage = new SpannableStringBuilder(toAppendBuilder);
                }
                break;
            case "-copyboost":
                attackerOutputName = getPrintableOutputPokemonSide(split[0]);
                defenderOutputName = getPrintableOutputPokemonSide(split[1], false);
                toAppendBuilder.append(attackerOutputName).append(" copied ").append(defenderOutputName).append("'s stat changes!");
                toast = makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                toast.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        copyBoost(split[0], split[1]);
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
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;
            case "-clearboost":
                attackerOutputName = getPrintableOutputPokemonSide(split[0]);
                toAppendBuilder.append(attackerOutputName).append("'s stat changes were removed!");
                toast = makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                toast.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (getView() == null) {
                            return;
                        }
                        LinearLayout linearLayout = (LinearLayout) getView().findViewById(getTempStatusId(split[0]));
                        for (String stat : stats) {
                            TextView v = (TextView) linearLayout.findViewWithTag(stat);
                            linearLayout.removeView(v);
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
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;
            case "-invertboost":
                attackerOutputName = getPrintableOutputPokemonSide(split[0]);
                toAppendBuilder.append(attackerOutputName).append("'s stat changes were inverted!");
                toast = makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                toast.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        invertBoost(split[0], stats);
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
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;
            case "-clearallboost":
                toAppendBuilder.append("All stat changes were eliminated!");
                toast = makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                toast.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (getView() == null) {
                            return;
                        }
                        String[] layouts = {"p1a", "p1b", "p1c", "p2a", "p2b", "p2c"};
                        for (String layout : layouts) {
                            LinearLayout linearLayout = (LinearLayout) getView().findViewById(getTempStatusId(layout));
                            for (String stat : stats) {
                                TextView v = (TextView) linearLayout.findViewWithTag(stat);
                                linearLayout.removeView(v);
                            }
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
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;
            case "-crit":
                toAppendSpannable = new SpannableString("It's a critical hit!");
                toast = makeMinorToast(toAppendSpannable);
                animatorSet = createFlyingMessage(split[0], toast, new SpannableString("Critical!"));
                startAnimation(animatorSet);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;
            case "-supereffective":
                toAppendSpannable = new SpannableString("It's super effective!");
                toast = makeMinorToast(toAppendSpannable);
                animatorSet = createFlyingMessage(split[0], toast, new SpannableString("Booya!"));
                startAnimation(animatorSet);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;
            case "-resisted":
                toAppendSpannable = new SpannableString("It's not very effective...");
                toast = makeMinorToast(toAppendSpannable);
                animatorSet = createFlyingMessage(split[0], toast, new SpannableString("Resisted!"));
                startAnimation(animatorSet);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;
            case "-immune":
                attackerOutputName = getPrintableOutputPokemonSide(split[0], false);
                toAppendBuilder.append("It doesn't affect ");
                toAppendBuilder.append(attackerOutputName);
                toAppendBuilder.append(".");
                toAppendSpannable = new SpannableString(toAppendBuilder);
                toast = makeMinorToast(toAppendSpannable);
                animatorSet = createFlyingMessage(split[0], toast, new SpannableString("Immuned!"));
                startAnimation(animatorSet);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;
            case "-miss":
                if (split.length > 1) {
                    // there was a target
                    defenderOutputName = getPrintableOutputPokemonSide(split[1]);
                    toAppendBuilder.append(defenderOutputName).append(" avoided the attack!");
                } else {
                    attackerOutputName = getPrintableOutputPokemonSide(split[0]);
                    toAppendBuilder.append(attackerOutputName).append("'s attack missed!");
                }
                toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                toast = makeMinorToast(toAppendSpannable);
                animatorSet = createFlyingMessage(split[0], toast, new SpannableString("Missed!"));
                startAnimation(animatorSet);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-fail":
                attackerOutputName = getPrintableOutputPokemonSide(split[0]);
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
                            if (fromEffect != null && getPrintable(fromEffectId).equals("uproar")) {
                                attackerOutputName = getPrintableOutputPokemonSide(split[0], false);
                                toAppendBuilder.append("But the uproar kept ").append(attackerOutputName).append(" awake!");
                            } else {
                                toAppendBuilder.append(attackerOutputName).append(" is already asleep.");
                            }
                            break;
                        case "par":
                            toAppendBuilder.append(attackerOutputName).append(" is already paralyzed.");
                            break;
                        case "frz":
                            toAppendBuilder.append(attackerOutputName).append(" is already frozen.");
                            break;
                        case "substitute":
                            if (messageDetails.contains("[weak]")) {
                                toAppendBuilder.append(attackerOutputName).append("It was too weak to make a substitute!");
                            } else {
                                toAppendBuilder.append(attackerOutputName).append(" already has a substitute!");
                            }
                            break;
                        case "skydrop":
                            if (messageDetails.contains("[heavy]")) {
                                toAppendBuilder.append(attackerOutputName).append(" is too heavy to be lifted!");
                            } else {
                                toAppendBuilder.append("But it failed!");
                            }
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
                toast = makeMinorToast(new SpannableString(toAppendBuilder));
                animatorSet = createFlyingMessage(split[0], toast, new SpannableString("But it failed!"));
                startAnimation(animatorSet);

                logMessage = new SpannableString(toAppendBuilder);
                break;

            case "-notarget":
                logMessage = new SpannableString("But there was no target...");
                toast = makeMinorToast(logMessage);
                startAnimation(toast);
                break;

            case "-ohko":
                logMessage = new SpannableString("It's a one-hit KO!");
                toast = makeMinorToast(logMessage);
                startAnimation(toast);
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
                    logMessage = new SpannableStringBuilder(toAppendBuilder);
                } catch (NumberFormatException e) {
                    logMessage = new SpannableString(command + ":" + messageDetails);
                }
                toast = makeMinorToast(logMessage);
                startAnimation(toast);
                break;

            case "-nothing":
                logMessage = new SpannableString("But nothing happened! ");
                toast = makeMinorToast(logMessage);
                startAnimation(toast);
                break;

            case "-waiting":
                attackerOutputName = getPrintableOutputPokemonSide(split[0]);
                defenderOutputName = getPrintableOutputPokemonSide(split[1], false);
                toAppendBuilder.append(attackerOutputName).append(" is waiting for ").append(defenderOutputName).append("'s move...");
                logMessage = new SpannableString(toAppendBuilder);
                toast = makeMinorToast(logMessage);
                startAnimation(toast);
                break;

            case "-combine":
                logMessage = new SpannableString("The two moves are joined! It's a combined move!");
                toast = makeMinorToast(logMessage);
                startAnimation(toast);
                break;

            case "-prepare":
                // todo
                logMessage = new SpannableString(command + ":" + messageDetails);
                break;

            case "-status":
                attackerOutputName = getPrintableOutputPokemonSide(split[0]);
                toAppendBuilder.append(attackerOutputName);
                remaining = split[1];
                switch (remaining) {
                    case "brn":
                        toAppendBuilder.append(" was burned");
                        if (fromEffect != null) {
                            toAppendBuilder.append(" by the ").append(getPrintable(fromEffect));
                        }
                        toAppendBuilder.append("!");
                        break;

                    case "tox":
                        toAppendBuilder.append(" was badly poisoned");
                        if (fromEffect != null) {
                            toAppendBuilder.append(" by the ").append(getPrintable(fromEffect));
                        }
                        toAppendBuilder.append("!");
                        break;

                    case "psn":
                        toAppendBuilder.append(" was poisoned!");
                        break;

                    case "slp":
                        if (fromEffect != null && fromEffectId.equals("move:rest")) {
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
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                toast = makeMinorToast(logMessage);
                final String status;
                status = remaining;
                toast.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        setAddonStatus(split[0], status);
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

            case "-curestatus":
                attackerOutputName = getPrintableOutputPokemonSide(split[0]);
                flag = false;
                if (fromEffectId != null) {
                    fromEffectId = getPrintable(fromEffectId);
                    switch (getPrintable(fromEffectId)) {
                        case "psychoshift":
                            defenderOutputName = getPrintableOutputPokemonSide(ofSource, false);
                            toAppendBuilder.append(attackerOutputName).append(" moved its status onto ").append(defenderOutputName);
                            flag = true;
                            break;
                    }
                    if (fromEffectId.contains("ability:")) {
                        toAppendBuilder.append(attackerOutputName).append("'s ").append(getPrintable(fromEffect)).append(" heals its status!");
                        flag = true;
                    }
                }

                if (!flag) {
                    //split1 is cured status
                    switch (split[1]) {
                        case "brn":
                            if (fromEffectId != null && fromEffectId.contains("item:")) {
                                toAppendBuilder.append(attackerOutputName).append("'s ").append(getPrintable(fromEffect)).append(" healed its burn!");
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
                            if (fromEffectId != null && fromEffectId.contains("item:")) {
                                toAppendBuilder.append(attackerOutputName).append("'s ").append(getPrintable(fromEffect)).append(" cured its poison!");
                                break;
                            }
                            toAppendBuilder.append(attackerOutputName).append(" was cured of its poisoning.");
                            break;

                        case "slp":
                            if (fromEffectId != null && fromEffectId.contains("item:")) {
                                toAppendBuilder.append(attackerOutputName).append("'s ").append(getPrintable(fromEffect)).append(" woke it up!");
                                break;
                            }
                            toAppendBuilder.append(attackerOutputName).append(" woke up!");
                            break;

                        case "par":
                            if (fromEffectId != null && fromEffectId.contains("item:")) {
                                toAppendBuilder.append(attackerOutputName).append("'s ").append(getPrintable(fromEffect)).append(" cured its paralysis!");
                                break;
                            }
                            toAppendBuilder.append(attackerOutputName).append(" was cured of paralysis.");

                            break;

                        case "frz":
                            if (fromEffectId != null && fromEffectId.contains("item:")) {
                                toAppendBuilder.append(attackerOutputName).append("'s ").append(getPrintable(fromEffect)).append(" defrosted it!");
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
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                toast = makeMinorToast(logMessage);
                if (!flag) {
                    toast.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            removeAddonStatus(split[0], split[1]);
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
                }
                startAnimation(toast);
                break;

            case "-cureteam":
                if (fromEffectId != null) {
                    switch (getPrintable(fromEffectId)) {
                        case "aromatherapy":
                            toAppendBuilder.append("A soothing aroma wafted through the area!");
                            break;

                        case "healbell":
                            toAppendBuilder.append("A bell chimed!");
                            break;
                    }
                } else {
                    attackerOutputName = getPrintableOutputPokemonSide(split[0]);
                    toAppendBuilder.append(attackerOutputName);
                    toAppendBuilder.append(" 's team was cured");
                }
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                toast = makeMinorToast(logMessage);
                toast.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        String[] teammate;
                        if (split[0].startsWith("p1")) {
                            teammate = teammates[0];
                        } else {
                            teammate = teammates[1];
                        }
                        for (String mate : teammate) {
                            for (String stt : sttus) {
                                removeAddonStatus(mate, stt);
                            }
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

            case "-item":
                attackerOutputName = getPrintableOutputPokemonSide(split[0]);
                final String item;
                item = getPrintable(split[1]);
                if (fromEffect != null) {
                    // not to deal with item: or ability: or move:
                    switch (getPrintable(fromEffectId)) {
                        case "recycle":
                        case "pickup":
                            toAppendBuilder.append(attackerOutputName).append(" found one ").append(item).append("!");
                            break;

                        case "frisk":
                            toAppendBuilder.append(attackerOutputName).append(" frisked its target and found one ").append(item).append("!");
                            break;

                        case "thief":
                        case "covet":
                            defenderOutputName = getPrintableOutputPokemonSide(ofSource, false);
                            toAppendBuilder.append(attackerOutputName).append("  stole  ").append(defenderOutputName).append("'s ").append(item).append("!");
                            break;

                        case "harvest":
                            toAppendBuilder.append(attackerOutputName).append(" harvested one ").append(item).append("!");
                            break;

                        case "bestow":
                            defenderOutputName = getPrintableOutputPokemonSide(ofSource, false);
                            toAppendBuilder.append(attackerOutputName).append(" received ").append(item).append(" from ").append(defenderOutputName).append("!");
                            break;

                        default:
                            toAppendBuilder.append(attackerOutputName).append(" obtained one ").append(item).append(".");
                            break;
                    }
                    logMessage = new SpannableString(toAppendBuilder);
                    toast = makeMinorToast(logMessage);
                    animatorSet = createFlyingMessage(split[0], toast, new SpannableString(item));
                    startAnimation(animatorSet);
                } else {
                    switch (item) {
                        case "Air Balloon":
                            toAppendBuilder.append(attackerOutputName).append(" floats in the air with its Air Balloon!");
                            break;

                        default:
                            toAppendBuilder.append(attackerOutputName).append("has ").append(item).append("!");
                            break;
                    }
                    logMessage = new SpannableString(toAppendBuilder);
                    toast = makeMinorToast(logMessage);
                    toast.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            setAddonStatus(split[0], item);
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
                }
                break;

            case "-enditem":
                eat = messageDetails.contains("[eat]");
                weaken = messageDetails.contains("[weaken]");
                attackerOutputName = getPrintableOutputPokemonSide(split[0]);
                item = split[1].trim();

                if (eat) {
                    toAppendBuilder.append(attackerOutputName).append(" ate its ").append(item).append("!");
                } else if (weaken) {
                    toAppendBuilder.append(attackerOutputName).append(" weakened the damage to ").append(item).append("!");
                } else if (fromEffect != null) {
                    switch (getPrintable(fromEffectId)) {
                        case "fling":
                            toAppendBuilder.append(attackerOutputName).append(" flung its ").append(item).append("!");
                            break;

                        case "knockoff":
                            defenderOutputName = getPrintableOutputPokemonSide(ofSource);
                            attackerOutputName = getPrintableOutputPokemonSide(split[0], false);

                            toAppendBuilder.append(defenderOutputName).append(" knocked off ").append(attackerOutputName).append("'s ").append(item).append("!");
                            break;

                        case "stealeat":
                            defenderOutputName = getPrintableOutputPokemonSide(ofSource);
                            toAppendBuilder.append(defenderOutputName).append(" stole and ate its target's ").append(item).append("!");
                            break;

                        case "gem":
                            separator = messageDetails.indexOf("[move]");
                            move = "";
                            if (separator != -1) {
                                move = messageDetails.substring(separator + 6);
                                if (move.contains("|")) {
                                    move = move.substring(0, move.indexOf("|"));
                                }
                            }
                            toAppendBuilder.append("The ").append(item).append(" strengthened ").append(move).append("'s power!");
                            break;

                        case "incinerate":
                            toAppendBuilder.append(attackerOutputName).append("'s ").append(item).append(" was burnt up!");
                            break;

                        default:
                            toAppendBuilder.append(attackerOutputName).append(" lost its").append(item).append("!");
                            break;
                    }
                } else {
                    String itemId = toId(item);
                    switch (itemId) {
                        case "airballoon":
                            toAppendBuilder.append(attackerOutputName).append("'s Air Balloon popped!");
                            break;

                        case "focussash":
                            toAppendBuilder.append(attackerOutputName).append(" hung on using its Focus Sash!");
                            break;

                        case "focusband":
                            toAppendBuilder.append(attackerOutputName).append(" hung on using its Focus Band!");
                            break;

                        case "mentalherb":
                            toAppendBuilder.append(attackerOutputName).append(" used its Mental Herb to come back to its senses!");
                            break;

                        case "whiteherb":
                            toAppendBuilder.append(attackerOutputName).append(" restored its status using its White Herb!");
                            break;

                        case "ejectbutton":
                            toAppendBuilder.append(attackerOutputName).append(" is switched out with the Eject Button!");
                            break;

                        case "redcard":
                            defenderOutputName = getPrintableOutputPokemonSide(ofSource, false);
                            toAppendBuilder.append(attackerOutputName).append(" held up its Red Card against ").append(defenderOutputName).append("!");
                            break;

                        default:
                            toAppendBuilder.append(attackerOutputName).append("'s ").append(item).append(" activated!");
                            break;
                    }
                }

                logMessage = new SpannableString(toAppendBuilder);
                toast = makeMinorToast(logMessage);
                animatorSet = createFlyingMessage(split[0], toast, new SpannableString(item));
                animatorSet.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        removeAddonStatus(split[0], item);
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
                startAnimation(animatorSet);
                break;

            case "-ability":
                attackerOutputName = getPrintableOutputPokemonSide(split[0]);
                ability = split[1];

                if (fromEffect != null) {
                    switch (getPrintable(fromEffectId)) {
                        case "trace":
                            defenderOutputName = getPrintableOutputPokemonSide(ofSource, false);
                            toAppendBuilder.append(attackerOutputName).append(" traced ").append(defenderOutputName).append("'s ").append(getPrintable(ability)).append("!");
                            break;

                        case "roleplay":
                            defenderOutputName = getPrintableOutputPokemonSide(ofSource, false);
                            toAppendBuilder.append(attackerOutputName).append(" copied ").append(defenderOutputName).append("'s ").append(getPrintable(ability)).append("!");
                            break;

                        case "mummy":
                            toAppendBuilder.append(attackerOutputName).append("'s Ability became Mummy!");
                            break;
                    }
                } else {
                    switch (toId(ability)) {
                        case "pressure":
                            toAppendBuilder.append(attackerOutputName).append(" is exerting its pressure!");
                            break;

                        case "moldbreaker":
                            toAppendBuilder.append(attackerOutputName).append(" breaks the mold!");
                            break;

                        case "turboblaze":
                            toAppendBuilder.append(attackerOutputName).append(" is radiating a blazing aura!");
                            break;

                        case "teravolt":
                            toAppendBuilder.append(attackerOutputName).append(" is radiating a bursting aura!");
                            break;

                        case "intimidate":
                            toAppendBuilder.append(attackerOutputName).append(" intimidates ").append(getPrintable(ofSource)).append("!");
                            break;

                        case "unnerve":
                            if (split[0].startsWith("p2")) {
                                side = "your team";
                            } else {
                                side = "the opposing team";
                            }
                            toAppendBuilder.append(attackerOutputName).append(" 's Unnerve makes ").append(side).append(" too nervous to eat Berries!");
                            break;

                        case "aurabreak":
                            toAppendBuilder.append(attackerOutputName).append(" reversed all other Pokémon's auras!");
                            break;

                        case "fairyaura":
                            toAppendBuilder.append(attackerOutputName).append(" is radiating a fairy aura!");
                            break;

                        case "darkaura":
                            toAppendBuilder.append(attackerOutputName).append(" is radiating a dark aura!");
                            break;

                        case "airlock":
                        case "cloudnine":
                            toAppendBuilder.append("The effects of weather disappeared.");
                            break;

                        default:
                            toAppendBuilder.append(attackerOutputName).append(" has ").append(getPrintable(ability)).append("!");
                            break;
                    }
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = makeToast(logMessage);
                startAnimation(toast);
                break;

            case "-endability":
                attackerOutputName = getPrintableOutputPokemonSide(split[0]);
                ability = split[1];

                if (fromEffect != null) {
                    switch (getPrintable(fromEffectId)) {
                        case "mummy":
                            attackerOutputName = getPrintableOutputPokemonSide(split[0], false);
                            toAppendBuilder.append("(").append(attackerOutputName).append("'s Ability was previously ").append(getPrintable(ability)).append(")");
                            break;

                        default:
                            toAppendBuilder.append(attackerOutputName).append("\\'s Ability was suppressed!");
                            break;
                    }
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = makeToast(logMessage);
                startAnimation(toast);
                break;

            case "-transform":
                attacker = getPrintableOutputPokemonSide(split[0]);
                defender = getPrintable(split[1]);
                toAppend = attacker + " transformed into " + defender + "!";
                logMessage = new SpannableString(toAppend);
                toast = makeMinorToast(logMessage);
                toast.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (getView() == null) {
                            return;
                        }
                        ImageView orgn = (ImageView) getView().findViewById(getSpriteId(split[0]));
                        ImageView dest = (ImageView) getView().findViewById(getSpriteId(split[1]));
                        orgn.setImageDrawable(dest.getDrawable());
                        copyBoost(split[1], split[0]);
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

            case "-formechange":
                // nothing here
                logMessage = new SpannableString("");
                break;

            case "-start":
                attackerOutputName = getPrintableOutputPokemonSide(split[0]);
                animatorSet = new AnimatorSet();
                final String newEffect;
                newEffect = getPrintable(split[1]);
                switch (getPrintable(toId(split[1]))) {
                    case "typechange":
                        if (fromEffect != null) {
                            if (getPrintable(fromEffectId).equals("reflecttype")) {
                                toAppendBuilder.append(attackerOutputName).append("'s type changed to match ").append(getPrintable(ofSource)).append("'s!");
                            } else {
                                toAppendBuilder.append(attackerOutputName).append("'s ").append(getPrintable(fromEffect)).append(" made it the ").append(getPrintable(split[2])).append(" type!");
                            }
                        } else {
                            toAppendBuilder.append(attackerOutputName).append(" transformed into the ").append(getPrintable(split[2])).append(" type!");
                        }
                        break;

                    case "typeadd":
                        attackerOutputName = getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append(getPrintable(split[2])).append(" type was added to ").append(attackerOutputName).append(" type!");
                        break;

                    case "powertrick":
                        toAppendBuilder.append(attackerOutputName).append(" switched its Attack and Defense!");
                        break;

                    case "foresight":
                    case "miracleeye":
                        toAppendBuilder.append(attackerOutputName).append(" was identified!");
                        break;

                    case "telekinesis":
                        toAppendBuilder.append(attackerOutputName).append(" was hurled into the air!");
                        break;

                    case "confusion":
                        if (messageDetails.contains("[already]")) {
                            toAppendBuilder.append(attackerOutputName).append(" is already confused!");
                        } else {
                            toAppendBuilder.append(attackerOutputName).append(" became confused!");
                            animatorSet.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    setAddonStatus(split[0], newEffect);
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
                        }
                        break;

                    case "leechseed":
                        toAppendBuilder.append(attackerOutputName).append(" was seeded!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                setAddonStatus(split[0], newEffect);
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
                        break;

                    case "mudsport":
                        toAppendBuilder.append("Electricity's power was weakened!");
                        break;

                    case "watersport":
                        toAppendBuilder.append("Fire's power was weakened!");
                        break;

                    case "yawn":
                        toAppendBuilder.append(attackerOutputName).append(" grew drowsy!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                setAddonStatus(split[0], newEffect);
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
                        break;

                    case "flashfire":
                        attackerOutputName = getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append("The power of ").append(attackerOutputName).append("'s Fire-type moves rose!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                setAddonStatus(split[0], newEffect);
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
                        break;

                    case "taunt":
                        toAppendBuilder.append(attackerOutputName).append(" fell for the taunt!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                setAddonStatus(split[0], newEffect);
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
                        break;

                    case "imprison":
                        toAppendBuilder.append(attackerOutputName).append(" sealed the opponent's move(s)!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                setAddonStatus(split[0], newEffect);
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
                        break;

                    case "disable":
                        toAppendBuilder.append(attackerOutputName).append("'s").append(getPrintable(split[2])).append(" was disabled!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                setAddonStatus(split[0], newEffect);
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
                        break;

                    case "embargo":
                        toAppendBuilder.append(attackerOutputName).append(" can't use items anymore!");
                        break;

                    case "ingrain":
                        toAppendBuilder.append(attackerOutputName).append(" planted its roots!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                setAddonStatus(split[0], newEffect);
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
                        break;

                    case "aquaring":
                        toAppendBuilder.append(attackerOutputName).append(" surrounded itself with a veil of water!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                setAddonStatus(split[0], newEffect);
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
                        break;

                    case "stockpile1":
                        toAppendBuilder.append(attackerOutputName).append(" stockpiled 1!");
                        break;

                    case "stockpile2":
                        toAppendBuilder.append(attackerOutputName).append(" stockpiled 2!");
                        break;

                    case "stockpile3":
                        toAppendBuilder.append(attackerOutputName).append(" stockpiled 3!");
                        break;

                    case "perish0":
                        toAppendBuilder.append(attackerOutputName).append("'s perish count fell to 0.");
                        break;

                    case "perish1":
                        toAppendBuilder.append(attackerOutputName).append("'s perish count fell to 1.");
                        break;

                    case "perish2":
                        toAppendBuilder.append(attackerOutputName).append("'s perish count fell to 2.");
                        break;

                    case "perish3":
                        toAppendBuilder.append(attackerOutputName).append("'s perish count fell to 3.");
                        break;

                    case "encore":
                        toAppendBuilder.append(attackerOutputName).append(" received an encore!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                setAddonStatus(split[0], newEffect);
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
                        break;

                    case "bide":
                        toAppendBuilder.append(attackerOutputName).append(" is storing energy!");
                        break;

                    case "slowstart":
                        toAppendBuilder.append(attackerOutputName).append(" can't get it going because of its Slow Start!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                setAddonStatus(split[0], newEffect);
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
                        break;

                    case "attract":
                        if (fromEffect != null) {
                            toAppendBuilder.append(attackerOutputName).append(" fell in love from the ").append(getPrintable(fromEffect)).append("!");
                        } else {
                            toAppendBuilder.append(attackerOutputName).append(" fell in love!");
                        }
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                setAddonStatus(split[0], newEffect);
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
                        break;

                    case "autotomize":
                        toAppendBuilder.append(attackerOutputName).append(" became nimble!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                setAddonStatus(split[0], newEffect);
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
                        break;

                    case "focusenergy":
                        toAppendBuilder.append(attackerOutputName).append(" is getting pumped!");
                        break;

                    case "curse":
                        attackerOutputName = getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append(getPrintableOutputPokemonSide(ofSource)).append(" cut its own HP and laid a curse on ").append(attackerOutputName).append("!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                setAddonStatus(split[0], newEffect);
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
                        break;

                    case "nightmare":
                        toAppendBuilder.append(attackerOutputName).append(" began having a nightmare!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                setAddonStatus(split[0], newEffect);
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
                        break;

                    case "magnetrise":
                        toAppendBuilder.append(attackerOutputName).append(" levitated with electromagnetism!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                setAddonStatus(split[0], newEffect);
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
                        break;

                    case "smackdown":
                        toAppendBuilder.append(attackerOutputName).append(" fell straight down!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                setAddonStatus(split[0], newEffect);
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
                        break;

                    case "substitute":
                        if (messageDetails.contains("[damage]")) {
                            attackerOutputName = getPrintableOutputPokemonSide(split[0], false);
                            toAppendBuilder.append("The substitute took damage for ").append(attackerOutputName).append("!");
                        } else if (messageDetails.contains("[block]")) {
                            toAppendBuilder.append("But it failed!");
                        } else if (messageDetails.contains("[already]")) {
                            toAppendBuilder.append(attackerOutputName).append(" already has a substitute!");
                        } else {
                            toAppendBuilder.append(attackerOutputName).append(" put in a substitute!");
                            animatorSet.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    if (getView() == null) {
                                        return;
                                    }
                                    ImageView imageView = (ImageView) getView().findViewById(getSpriteId(split[0]));
                                    imageView.setAlpha(0.2f);
                                    ImageView substitute = new ImageView(getActivity());
                                    substitute.setImageResource(getSubstitute(split[0]));
                                    substitute.setTag("Substitute");

                                    RelativeLayout relativeLayout = (RelativeLayout) getView().findViewById(getPkmLayoutId(split[0]));
                                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                                    layoutParams.addRule(RelativeLayout.ALIGN_TOP, getSpriteId(split[0]));
                                    layoutParams.addRule(RelativeLayout.ALIGN_LEFT, getSpriteId(split[0]));
                                    relativeLayout.addView(substitute, layoutParams);
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
                        }
                        break;

                    case "uproar":
                        if (messageDetails.contains("[upkeep]")) {
                            toAppendBuilder.append(attackerOutputName).append(" is making an uproar!");
                        } else {
                            toAppendBuilder.append(attackerOutputName).append(" caused an uproar!");
                        }
                        break;

                    case "doomdesire":
                        toAppendBuilder.append(attackerOutputName).append(" chose Doom Desire as its destiny!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                setAddonStatus(split[0], newEffect);
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
                        break;

                    case "futuresight":
                        toAppendBuilder.append(attackerOutputName).append(" foresaw an attack!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                setAddonStatus(split[0], newEffect);
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
                        break;

                    case "mimic":
                        toAppendBuilder.append(attackerOutputName).append(" learned ").append(getPrintable(split[2])).append("!");
                        break;

                    case "followme":
                    case "ragepowder":
                        toAppendBuilder.append(attackerOutputName).append(" became the center of attention!");
                        break;

                    case "powder":
                        toAppendBuilder.append(attackerOutputName).append(" is covered in powder!");
                        break;

                    default:
                        toAppendBuilder.append(attackerOutputName).append("'s ").append(getPrintable(split[1])).append(" started!");
                        break;
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = makeMinorToast(logMessage);
                animatorSet.play(toast);
                startAnimation(animatorSet);
                break;

            case "-end":
                attacker = split[0];
                attackerOutputName = getPrintableOutputPokemonSide(split[0]);
                animatorSet = new AnimatorSet();
                newEffect = getPrintable(split[1]);
                animatorSet.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        removeAddonStatus(split[0], newEffect);
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
                switch (getPrintable(toId(split[1]))) {
                    case "powertrick":
                        toAppendBuilder.append(attackerOutputName).append(" switched its Attack and Defense!");
                        break;

                    case "telekinesis":
                        toAppendBuilder.append(attackerOutputName).append(" was freed from the telekinesis!");
                        break;

                    case "confusion":
                        if (fromEffect.contains("item:")) {
                            toAppendBuilder.append(attackerOutputName).append("'s ").append(getPrintable(fromEffect)).append(" snapped out of its confusion!");
                        } else {
                            if (attacker.startsWith("p2")) {
                                toAppendBuilder.append(attackerOutputName).append(" snapped out of confusion!");
                            } else {
                                toAppendBuilder.append(attackerOutputName).append(" snapped out of its confusion.");
                            }
                        }
                        break;

                    case "leechseed":
                        if (fromEffect != null && fromEffectId.equals("rapidspin")) {
                            toAppendBuilder.append(attackerOutputName).append(" was freed from Leech Seed!");
                        }
                        break;

                    case "healblock":
                        toAppendBuilder.append(attackerOutputName).append("'s Heal Block wore off!");
                        break;

                    case "taunt":
                        toAppendBuilder.append(attackerOutputName).append("'s taunt wore off!");
                        break;

                    case "disable":
                        toAppendBuilder.append(attackerOutputName).append(" is no longer disabled!");
                        break;

                    case "embargo":
                        toAppendBuilder.append(attackerOutputName).append(" can use items again!");
                        break;

                    case "torment":
                        toAppendBuilder.append(attackerOutputName).append("'s torment wore off!");
                        break;

                    case "encore":
                        toAppendBuilder.append(attackerOutputName).append("'s encore ended!");
                        break;

                    case "bide":
                        toAppendBuilder.append(attackerOutputName).append(" unleashed energy!");
                        break;

                    case "magnetrise":
                        if (attacker.startsWith("p2")) {
                            toAppendBuilder.append("The electromagnetism of ").append(attackerOutputName).append(" wore off!");
                        } else {
                            toAppendBuilder.append(attackerOutputName).append("s electromagnetism wore off!");
                        }
                        break;

                    case "perishsong":
                        break;

                    case "substitute":
                        toAppendBuilder.append(attackerOutputName).append("'s substitute faded!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                if (getView() == null) {
                                    return;
                                }
                                RelativeLayout relativeLayout = (RelativeLayout) getView().findViewById(getPkmLayoutId(split[0]));
                                View v = relativeLayout.findViewWithTag("Substitute");
                                if (v != null) {
                                    relativeLayout.removeView(v);
                                }
                                ImageView imageView = (ImageView) getView().findViewById(getSpriteId(split[0]));
                                imageView.setAlpha(1f);
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
                        break;

                    case "uproar":
                        toAppendBuilder.append(attackerOutputName).append(" calmed down.");
                        break;

                    case "stockpile":
                        toAppendBuilder.append(attackerOutputName).append("'s stockpiled effect wore off!");
                        break;

                    case "infestation":
                        toAppendBuilder.append(attackerOutputName).append(" was freed from Infestation!");
                        break;

                    default:
                        if (split[1].contains("move:")) {
                            toAppendBuilder.append(attackerOutputName).append(" took the ").append(getPrintable(split[1])).append(" attack!");
                        } else {
                            toAppendBuilder.append(attackerOutputName).append("'s ").append(getPrintable(split[1])).append(" ended!");
                        }
                        break;
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = makeMinorToast(logMessage);
                animatorSet.play(toast);
                startAnimation(animatorSet);
                break;

            case "-singleturn":
                attackerOutputName = getPrintableOutputPokemonSide(split[0]);
                switch (getPrintable(toId(split[1]))) {
                    case "roost":
                        toAppendBuilder.append(attackerOutputName).append(" landed on the ground!");
                        break;

                    case "quickguard":
                        attackerOutputName = getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append("Quick Guard protected ").append(attackerOutputName).append(" landed on the ground!");
                        break;

                    case "wideguard":
                        attackerOutputName = getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append("Wide Guard protected ").append(attackerOutputName).append(" landed on the ground!");
                        break;

                    case "protect":
                        toAppendBuilder.append(attackerOutputName).append(" protected itself!");
                        break;

                    case "endure":
                        toAppendBuilder.append(attackerOutputName).append(" braced itself!");
                        break;

                    case "helpinghand":
                        attackerOutputName = getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append(getPrintableOutputPokemonSide(ofSource)).append(" is ready to help ").append(attackerOutputName).append("!");
                        break;

                    case "focuspunch":
                        toAppendBuilder.append(attackerOutputName).append(" is tightening its focus!");
                        break;

                    case "snatch":
                        toAppendBuilder.append(attackerOutputName).append("  waits for a target to make a move!");
                        break;

                    case "magiccoat":
                        toAppendBuilder.append(attackerOutputName).append(" shrouded itself with Magic Coat!'");
                        break;

                    case "matblock":
                        toAppendBuilder.append(attackerOutputName).append(" intends to flip up a mat and block incoming attacks!");
                        break;

                    case "electrify":
                        toAppendBuilder.append(attackerOutputName).append("'s moves have been electrified!");
                        break;
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = makeMinorToast(logMessage);
                animatorSet = createFlyingMessage(split[0], toast, new SpannableString(getPrintable(split[1])));
                startAnimation(animatorSet);
                break;

            case "-singlemove":
                attackerOutputName = getPrintableOutputPokemonSide(split[0]);
                switch (getPrintable(toId(split[1]))) {
                    case "grudge":
                        toAppendBuilder.append(attackerOutputName).append(" wants its target to bear a grudge!");
                        break;
                    case "destinybond":
                        toAppendBuilder.append(attackerOutputName).append(" is trying to take its foe down with it!");
                        break;
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = makeMinorToast(logMessage);
                animatorSet = createFlyingMessage(split[0], toast, new SpannableString(getPrintable(split[1])));
                startAnimation(animatorSet);
                break;

            case "-activate":
                attacker = split[0];
                attackerOutputName = getPrintableOutputPokemonSide(split[0]);
                switch (getPrintable(toId(split[1]))) {
                    case "confusion":
                        toAppendBuilder.append(attackerOutputName).append(" is confused!");
                        break;

                    case "destinybond":
                        toAppendBuilder.append(attackerOutputName).append(" took its attacker down with it!");
                        break;

                    case "snatch":
                        toAppendBuilder.append(attackerOutputName).append(" snatched ").append(getPrintable(ofSource)).append("'s move!");
                        break;

                    case "grudge":
                        toAppendBuilder.append(attackerOutputName).append("'s").append(getPrintable(split[2])).append(" lost all its PP due to the grudge!");
                        break;

                    case "quickguard":
                        attackerOutputName = getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append("Quick Guard protected ").append(attackerOutputName).append("!");
                        break;

                    case "wideguard":
                        attackerOutputName = getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append("Wide Guard protected ").append(attackerOutputName).append("!");
                        break;

                    case "protect":
                        toAppendBuilder.append(attackerOutputName).append(" protected itself!");
                        break;

                    case "substitute":
                        if (messageDetails.contains("[damage]")) {
                            attackerOutputName = getPrintableOutputPokemonSide(split[0], false);
                            toAppendBuilder.append("The substitute took damage for ").append(attackerOutputName).append(" protected itself!");
                        } else if (messageDetails.contains("[block]")) {
                            toAppendBuilder.append(attackerOutputName).append("'s Substitute blocked").append(getPrintable(split[2])).append("!");
                        }
                        break;

                    case "attract":
                        toAppendBuilder.append(attackerOutputName).append(" is in love with ").append(getPrintable(ofSource)).append("!");
                        break;

                    case "bide":
                        toAppendBuilder.append(attackerOutputName).append(" is storing energy!");
                        break;

                    case "mist":
                        toAppendBuilder.append(attackerOutputName).append(" is protected by the mist!");
                        break;

                    case "trapped":
                        toAppendBuilder.append(attackerOutputName).append(" can no longer escape!");
                        break;

                    case "stickyweb":
                        toAppendBuilder.append(attackerOutputName).append(" was caught in a sticky web!");
                        break;

                    case "happyhour":
                        toAppendBuilder.append("Everyone is caught up in the happy atmosphere!");
                        break;

                    case "celebrate":
                        if (attacker.startsWith("p2")) {
                            side = mPlayer2;
                        } else {
                            side = mPlayer1;
                        }
                        toAppendBuilder.append("Congratulations, ").append(side).append("!");

                        break;

                    case "trick":
                    case "switcheroo":
                        toAppendBuilder.append(attackerOutputName).append(" switched items with its target!");
                        break;

                    case "brickbreak":
                        if (toId(ofSource).startsWith("p2")) {
                            side = "the opposing team";
                        } else {
                            side = "your team";
                        }
                        toAppendBuilder.append(attackerOutputName).append(" shattered ").append(side).append(" protections!");
                        break;

                    case "pursuit":
                        toAppendBuilder.append(attackerOutputName).append(" is being sent back!");
                        break;

                    case "feint":
                        toAppendBuilder.append(attackerOutputName).append(" fell for the feint!");
                        break;

                    case "spite":
                        toAppendBuilder.append("It reduced the PP of ").append(attackerOutputName).append("'s ").append(getPrintable(split[2])).append(" by ").append(getPrintable(split[3])).append("!");
                        break;

                    case "gravity":
                        toAppendBuilder.append(attackerOutputName).append(" couldn't stay airborne because of gravity!");
                        break;

                    case "magnitude":
                        toAppendBuilder.append("Magnitude ").append(getPrintable(split[2])).append("!");
                        break;

                    case "sketch":
                        toAppendBuilder.append(attackerOutputName).append(" sketched ").append(getPrintable(split[2])).append("!");
                        break;

                    case "skillswap":
                        toAppendBuilder.append(attackerOutputName).append(" swapped Abilities with its target!");
                        if (ofSource != null) {
                            toAppendBuilder.append("\n").append(attackerOutputName).append(" acquired ").append(getPrintable(split[2])).append("!");
                            toAppendBuilder.append("\n").append(getPrintable(ofSource)).append(" acquired ").append(getPrintable(split[3])).append("!");
                        }
                        break;

                    case "charge":
                        toAppendBuilder.append(attackerOutputName).append(" began charging power!");
                        break;

                    case "struggle":
                        toAppendBuilder.append(attackerOutputName).append(" has no moves left!");
                        break;

                    case "bind":
                        toAppendBuilder.append(attackerOutputName).append(" was squeezed by ").append(getPrintable(ofSource)).append("!");
                        break;

                    case "wrap":
                        toAppendBuilder.append(attackerOutputName).append(" was wrapped by ").append(getPrintable(ofSource)).append("!");
                        break;

                    case "clamp":
                        toAppendBuilder.append(getPrintable(ofSource)).append(" clamped ").append(attackerOutputName).append("!");
                        break;

                    case "whirlpool":
                        toAppendBuilder.append(attackerOutputName).append(" became trapped in the vortex!");
                        break;

                    case "firespin":
                        toAppendBuilder.append(attackerOutputName).append(" became trapped in the fiery vortex!");
                        break;

                    case "magmastorm":
                        toAppendBuilder.append(attackerOutputName).append(" became trapped by swirling magma!");
                        break;

                    case "sandtomb":
                        toAppendBuilder.append(attackerOutputName).append(" became trapped by Sand Tomb!");
                        break;

                    case "infestation":
                        toAppendBuilder.append(attackerOutputName).append(" has been afflicted with an infestation by ").append(getPrintable(ofSource)).append("!");
                        break;

                    case "afteryou":
                        toAppendBuilder.append(attackerOutputName).append(" took the kind offer!");
                        break;

                    case "quash":
                        toAppendBuilder.append(attackerOutputName).append("'s move was postponed!");
                        break;

                    case "powersplit":
                        toAppendBuilder.append(attackerOutputName).append(" shared its power with the target!");
                        break;

                    case "guardsplit":
                        toAppendBuilder.append(attackerOutputName).append(" shared its guard with the target!");
                        break;

                    case "ingrain":
                        toAppendBuilder.append(attackerOutputName).append(" anchored itself with its roots!");
                        break;

                    case "matblock":
                        toAppendBuilder.append(getPrintable(split[2])).append(" was blocked by the kicked-up mat!");
                        break;

                    case "powder":
                        toAppendBuilder.append("When the flame touched the powder on the Pokémon, it exploded!");
                        break;

                    case "fairylock":
                        toAppendBuilder.append("No one will be able to run away during the next turn!");
                        break;

                    //abilities
                    case "sturdy":
                        toAppendBuilder.append(attackerOutputName).append(" held on thanks to Sturdy!");
                        break;

                    case "magicbounce":
                    case "magiccoat":
                    case "rebound":
                        break;

                    case "wonderguard":
                        toAppendBuilder.append(attackerOutputName).append("'s Wonder Guard evades the attack!");
                        break;

                    case "speedboost":
                        toAppendBuilder.append(attackerOutputName).append("'s' Speed Boost increases its speed!");
                        break;

                    case "forewarn":
                        toAppendBuilder.append(attackerOutputName).append("'s Forewarn alerted it to ").append(getPrintable(split[2])).append("!");
                        break;

                    case "anticipation":
                        toAppendBuilder.append(attackerOutputName).append(" shuddered!");
                        break;

                    case "telepathy":
                        toAppendBuilder.append(attackerOutputName).append(" avoids attacks by its ally Pok&#xE9;mon!");
                        break;

                    case "suctioncups":
                        toAppendBuilder.append(attackerOutputName).append(" anchors itself!");
                        break;

                    case "symbiosis":
                        attackerOutputName = getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append(getPrintable(ofSource)).append(" shared its ").append(getPrintable(split[2])).append(" with ").append(attackerOutputName);
                        break;

                    //items
                    case "custapberry":
                    case "quickclaw":
                        toAppendBuilder.append(attackerOutputName).append("'s ").append(getPrintable(split[1])).append(" let it move first!");
                        break;

                    case "leppaberry":
                        toAppendBuilder.append(attackerOutputName).append(" restored ").append(getPrintable(split[2])).append("'s PP using its Leppa Berry!");
                        break;

                    default:
                        toAppendBuilder.append(attackerOutputName).append("'s ").append(" activated!");
                        break;
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = makeMinorToast(logMessage);
                animatorSet = createFlyingMessage(split[0], toast, new SpannableString(getPrintable(split[1])));
                startAnimation(animatorSet);
                break;

            case "-sidestart":
                if (messageDetails.startsWith("p2")) {
                    side = "the opposing team";
                } else {
                    side = "your team";
                }

                fromEffect = split[1];
                fromEffectId = getPrintable(toId(fromEffect));
                animatorSet = new AnimatorSet();
                switch (fromEffectId) {
                    case "stealthrock":
                        toAppendBuilder.append("Pointed stones float in the air around ").append(side).append("!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                if (getView() == null) {
                                    return;
                                }
                                int id = (messageDetails.startsWith("p1")) ? R.id.field_rocks : R.id.field_rocks_o;
                                getView().findViewById(id).setVisibility(View.VISIBLE);
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
                        break;

                    case "spikes":
                        toAppendBuilder.append("Spikes were scattered all around the feet of ").append(side).append("!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                if (getView() == null) {
                                    return;
                                }
                                getView().findViewById(getLastVisibleSpike(messageDetails, true)).setVisibility(View.VISIBLE);
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
                        break;

                    case "toxicspikes":
                        toAppendBuilder.append("Toxic spikes were scattered all around the feet of ").append(side).append("!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                if (getView() == null) {
                                    return;
                                }
                                getView().findViewById(getLastVisibleTSpike(messageDetails, true)).setVisibility(View.VISIBLE);
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
                        break;

                    case "stickyweb":
                        toAppendBuilder.append("A sticky web spreads out beneath ").append(side).append("'s feet!");
                        break;

                    case "tailwind":
                        toAppendBuilder.append("The tailwind blew from behind ").append(side).append("!");
                        break;

                    case "reflect":
                        toAppendBuilder.append("Reflect raised ").append(side).append("'s Defense!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                if (getView() == null) {
                                    return;
                                }
                                int id = (messageDetails.startsWith("p1")) ? R.id.field_reflect : R.id.field_reflect_o;
                                getView().findViewById(id).setVisibility(View.VISIBLE);
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
                        break;

                    case "lightscreen":
                        toAppendBuilder.append("Light Screen raised ").append(side).append("'s Special Defense!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                if (getView() == null) {
                                    return;
                                }
                                int id = (messageDetails.startsWith("p1")) ? R.id.field_lightscreen : R.id.field_lightscreen_o;
                                getView().findViewById(id).setVisibility(View.VISIBLE);
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
                        break;

                    case "safeguard":
                        side = Character.toUpperCase(side.charAt(0)) + side.substring(1);
                        toAppendBuilder.append(side).append(" became cloaked in a mystical veil!");
                        break;

                    case "mist":
                        side = Character.toUpperCase(side.charAt(0)) + side.substring(1);
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
                        toAppendBuilder.append(getPrintable(fromEffect)).append(" started!");
                        break;
                }

                logMessage = new SpannableStringBuilder(toAppendBuilder);
                toast = makeMinorToast(logMessage);
                animatorSet.play(toast);
                startAnimation(animatorSet);
                break;

            case "-sideend":
                if (messageDetails.startsWith("p2")) {
                    side = "the opposing team";
                } else {
                    side = "your team";
                }

                fromEffect = split[1];
                fromEffectId = getPrintable(toId(fromEffect));

                animatorSet = new AnimatorSet();
                switch (fromEffectId) {
                    case "stealthrock":
                        toAppendBuilder.append("The pointed stones disappeared from around ").append(side).append("!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                if (getView() == null) {
                                    return;
                                }
                                int id = (messageDetails.startsWith("p1")) ? R.id.field_rocks : R.id.field_rocks_o;
                                getView().findViewById(id).setVisibility(View.INVISIBLE);
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
                        break;

                    case "spikes":
                        toAppendBuilder.append("The spikes disappeared from around ").append(side).append("!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                if (getView() == null) {
                                    return;
                                }
                                getView().findViewById(getLastVisibleSpike(messageDetails, false)).setVisibility(View.INVISIBLE);
                                getView().findViewById(getLastVisibleSpike(messageDetails, false)).setVisibility(View.INVISIBLE);
                                getView().findViewById(getLastVisibleSpike(messageDetails, false)).setVisibility(View.INVISIBLE);
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
                        break;

                    case "toxicspikes":
                        toAppendBuilder.append("The poison spikes disappeared from around ").append(side).append("!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                if (getView() == null) {
                                    return;
                                }
                                getView().findViewById(getLastVisibleTSpike(messageDetails, false)).setVisibility(View.INVISIBLE);
                                getView().findViewById(getLastVisibleTSpike(messageDetails, false)).setVisibility(View.INVISIBLE);
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
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                if (getView() == null) {
                                    return;
                                }
                                int id = (messageDetails.startsWith("p1")) ? R.id.field_reflect : R.id.field_reflect_o;
                                getView().findViewById(id).setVisibility(View.INVISIBLE);
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
                        break;

                    case "lightscreen":
                        side = Character.toUpperCase(side.charAt(0)) + side.substring(1);
                        toAppendBuilder.append(side).append("'s Reflect wore off!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                if (getView() == null) {
                                    return;
                                }
                                int id = (messageDetails.startsWith("p1")) ? R.id.field_lightscreen : R.id.field_lightscreen_o;
                                getView().findViewById(id).setVisibility(View.INVISIBLE);
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
                        toAppendBuilder.append(getPrintable(fromEffect)).append(" ended!");
                        break;
                }

                logMessage = new SpannableString(toAppendBuilder);
                toast = makeMinorToast(logMessage);
                animatorSet.play(toast);
                startAnimation(animatorSet);
                break;

            case "-weather":
                final String weather = split[0];
                boolean upkeep = false;
                if (split.length > 1) {
                    upkeep = true;
                }
                animatorSet = new AnimatorSet();
                switch (weather) {
                    case "RainDance":
                        if (upkeep) {
                            toAppendBuilder.append("Rain continues to fall!");
                        } else {
                            toAppendBuilder.append("It started to rain!");
                            weatherExist = true;
                            animatorSet.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    if (getView() == null) {
                                        return;
                                    }
                                    ((ImageView) getView().findViewById(R.id.weather_background)).setImageResource(R.drawable.weather_raindance);
                                    ((TextView) getView().findViewById(R.id.weather)).setText(weather);
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
                        }
                        break;
                    case "Sandstorm":
                        if (upkeep) {
                            toAppendBuilder.append("The sandstorm rages.");
                        } else {
                            toAppendBuilder.append("A sandstorm kicked up!");
                            weatherExist = true;
                            animatorSet.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    if (getView() == null) {
                                        return;
                                    }
                                    ((ImageView) getView().findViewById(R.id.weather_background)).setImageResource(R.drawable.weather_sandstorm);
                                    ((TextView) getView().findViewById(R.id.weather)).setText(weather);
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
                        }
                        break;
                    case "SunnyDay":
                        if (upkeep) {
                            toAppendBuilder.append("The sunlight is strong!");
                        } else {
                            toAppendBuilder.append("The sunlight turned harsh!");
                            weatherExist = true;
                            animatorSet.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    if (getView() == null) {
                                        return;
                                    }
                                    ((ImageView) getView().findViewById(R.id.weather_background)).setImageResource(R.drawable.weather_sunnyday);
                                    ((TextView) getView().findViewById(R.id.weather)).setText(weather);
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
                        }
                        break;
                    case "Hail":
                        if (upkeep) {
                            toAppendBuilder.append("The hail crashes down.");
                        } else {
                            toAppendBuilder.append("It started to hail!");
                            weatherExist = true;
                            animatorSet.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    if (getView() == null) {
                                        return;
                                    }
                                    ((ImageView) getView().findViewById(R.id.weather_background)).setImageResource(R.drawable.weather_hail);
                                    ((TextView) getView().findViewById(R.id.weather)).setText(weather);
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
                            animatorSet.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    if (getView() == null) {
                                        return;
                                    }
                                    ((ImageView) getView().findViewById(R.id.weather_background)).setImageResource(0);
                                    ((TextView) getView().findViewById(R.id.weather)).setText(null);
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
                        }
                        weatherExist = false;
                        break;
                }
                currentWeather = weather;
                logMessage = new SpannableString(toAppendBuilder);
                toast = makeMinorToast(logMessage);
                animatorSet.play(toast);
                startAnimation(animatorSet);
                break;


            case "-fieldstart":
                attackerOutputName = ofSource;
                animatorSet = new AnimatorSet();
                switch (getPrintable(toId(split[0]))) {
                    case "trickroom":
                        toAppendBuilder.append(attackerOutputName).append(" twisted the dimensions!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                if (getView() == null) {
                                    return;
                                }
                                ((ImageView) getView().findViewById(R.id.battle_background)).setImageResource(R.drawable.weather_trickroom);
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
                        break;

                    case "wonderroom":
                        toAppendBuilder.append("It created a bizarre area in which the Defense and Sp. Def stats are swapped!");
                        break;

                    case "magicroom":
                        toAppendBuilder.append("It created a bizarre area in which Pok&#xE9;mon's held items lose their effects!");
                        break;

                    case "gravity":
                        toAppendBuilder.append("Gravity intensified!");
                        break;

                    case "mudsport":
                        toAppendBuilder.append("Electric's power was weakened!");
                        break;

                    case "watersport":
                        toAppendBuilder.append("Fire's power was weakened!");
                        break;

                    default:
                        toAppendBuilder.append(getPrintable(split[1])).append(" started!");
                        break;
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = makeMinorToast(logMessage);
                animatorSet.play(toast);
                startAnimation(animatorSet);
                break;

            case "-fieldend":
                animatorSet = new AnimatorSet();
                switch (getPrintable(toId(split[0]))) {
                    case "trickroom":
                        toAppendBuilder.append("The twisted dimensions returned to normal!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                if (getView() == null) {
                                    return;
                                }
                                int id = new Random().nextInt(BACKGROUND_LIBRARY.length);
                                ((ImageView) getView().findViewById(R.id.battle_background)).setImageResource(BACKGROUND_LIBRARY[id]);
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
                        break;

                    case "wonderroom":
                        toAppendBuilder.append("'Wonder Room wore off, and the Defense and Sp. Def stats returned to normal!");
                        break;

                    case "magicroom":
                        toAppendBuilder.append("Magic Room wore off, and the held items' effects returned to normal!");
                        break;

                    case "gravity":
                        toAppendBuilder.append("Gravity returned to normal!");
                        break;

                    case "mudsport":
                        toAppendBuilder.append("The effects of Mud Sport have faded.");
                        break;

                    case "watersport":
                        toAppendBuilder.append("The effects of Water Sport have faded.");
                        break;

                    default:
                        toAppendBuilder.append(getPrintable(split[1])).append(" ended!");
                        break;
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = makeMinorToast(logMessage);
                animatorSet.play(toast);
                startAnimation(animatorSet);
                break;

            case "-fieldactivate":
                switch (getPrintable(toId(split[0]))) {
                    case "perishsong":
                        toAppendBuilder.append("All Pok&#xE9;mon hearing the song will faint in three turns!");
                        break;

                    case "payday":
                        toAppendBuilder.append("Coins were scattered everywhere!");
                        break;

                    case "iondeluge":
                        toAppendBuilder.append("A deluge of ions showers the battlefield!");
                        break;

                    default:
                        toAppendBuilder.append(getPrintable(split[1])).append(" hit!");
                        break;
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = makeMinorToast(logMessage);
                startAnimation(toast);
                break;

            case "-message":
                logMessage = new SpannableString(messageDetails);
                toast = makeMinorToast(logMessage);
                startAnimation(toast);
                break;

            case "-anim":
                logMessage = new SpannableString(command + ":" + messageDetails);
                toast = makeMinorToast(logMessage);
                startAnimation(toast);
                break;

            default:
                toAppendSpannable = new SpannableString(command + ":" + messageDetails);
                toast = makeMinorToast(toAppendSpannable);
                startAnimation(toast);
                logMessage = new SpannableString(command + ":" + messageDetails);
                break;
        }

        if (messageDetails.contains("[silent]")) {
            return;
        }

        logMessage.setSpan(new RelativeSizeSpan(0.8f), 0, logMessage.toString().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        addToLog(logMessage);
    }

    public void startAnimation(final AnimatorSet animator) {
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

    public AnimatorSet makeMinorToast(final Spannable message) {
        if (getView() == null) {
            return null;
        }
        TextView textView = (TextView) getView().findViewById(R.id.toast);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(textView, "alpha", 0f, 1f);
        fadeIn.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(textView, "alpha", 1f, 0f);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setStartDelay(ANIMATION_SHORT);

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

    public AnimatorSet makeToast(final Spannable message, final int duration) {
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

    public AnimatorSet makeToast(final String message) {
        return makeToast(message, ANIMATION_LONG);
    }

    public AnimatorSet makeToast(final String message, final int duration) {
        return makeToast(new SpannableString(message), duration);
    }

    public AnimatorSet makeToast(final Spannable message) {
        return makeToast(message, ANIMATION_LONG);
    }

    public void addToLog(Spannable logMessage) {
        BattleFieldData.RoomData roomData = BattleFieldData.get(getActivity()).getRoomInstance(mRoomId);
        if (roomData != null && roomData.isMessageListener()) {
            if (logMessage.length() > 0) {
                roomData.addServerMessageOnHold(logMessage);
            }
        } else {
            BattleLogDialog battleLogDialog =
                    (BattleLogDialog) getActivity().getSupportFragmentManager().findFragmentByTag(mRoomId);
            if (battleLogDialog != null) {
                if (logMessage.length() > 0) {
                    battleLogDialog.processServerMessage(logMessage);
                }
            }
        }
    }

    /**
     * @param player can be p1 or p2
     */
    public int getTeamPreviewSpriteId(String player, int id) {
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

    public int getPkmLayoutId(String tag) {
        tag = tag.substring(0, 3);
        switch (tag) {
            case "p1a":
                return R.id.p1a;
            case "p1b":
                return R.id.p1b;
            case "p1c":
                return R.id.p1c;
            case "p2a":
                return R.id.p2a;
            case "p2b":
                return R.id.p2b;
            case "p2c":
                return R.id.p2c;
            default:
                return 0;
        }
    }

    public int getSpriteId(String tag) {
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

    public int getSpriteNameid(String tag) {
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

    public int getIconId(String tag) {
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

    public int getIconId(String player, int id) {
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
                        Log.d(BTAG, mPlayer1Team.toString());
                        Log.d(BTAG, mPlayer2Team.toString());
                        return R.id.icon1;
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
                        Log.d(BTAG, mPlayer1Team.toString());
                        Log.d(BTAG, mPlayer2Team.toString());
                        return R.id.icon1_o;
                }
            default:
                Log.d(BTAG, mPlayer1Team.toString());
                Log.d(BTAG, mPlayer2Team.toString());
                return R.id.icon1;
        }
    }

    public int getGenderId(String tag) {
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

    public int getHpId(String tag) {
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

    public int getHpBarId(String tag) {
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

    public int getTempStatusId(String tag) {
        tag = tag.substring(0, 3);
        switch (tag) {
            case "p1a":
                return R.id.p1a_temp_status;
            case "p1b":
                return R.id.p1b_temp_status;
            case "p1c":
                return R.id.p1c_temp_status;
            case "p2a":
                return R.id.p2a_temp_status;
            case "p2b":
                return R.id.p2b_temp_status;
            case "p2c":
                return R.id.p2c_temp_status;
            default:
                return 0;
        }
    }

    public int getOldHp(String tag) {
        tag = tag.substring(0, 3);
        switch (tag) {
            case "p1a":
                return progressBarHolder[0];
            case "p1b":
                return progressBarHolder[1];
            case "p1c":
                return progressBarHolder[2];
            case "p2a":
                return progressBarHolder[3];
            case "p2b":
                return progressBarHolder[4];
            case "p2c":
                return progressBarHolder[5];
            default:
                return 0;
        }
    }

    public void setOldHp(String tag, int hp) {
        tag = tag.substring(0, 3);
        switch (tag) {
            case "p1a":
                progressBarHolder[0] = hp;
                break;
            case "p1b":
                progressBarHolder[1] = hp;
                break;
            case "p1c":
                progressBarHolder[2] = hp;
                break;
            case "p2a":
                progressBarHolder[3] = hp;
                break;
            case "p2b":
                progressBarHolder[4] = hp;
                break;
            case "p2c":
                progressBarHolder[5] = hp;
                break;
        }
    }

    public int getTeamSlot(String tag) {
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

    public void setAddonStatus(String tag, String status) {
        if (getView() == null) {
            return;
        }
        LinearLayout statusBar = (LinearLayout) getView().findViewById(getTempStatusId(tag));

        TextView stt = new TextView(getActivity());
        stt.setTag(status);
        stt.setText(status.toUpperCase());
        stt.setTextSize(10);
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
                break;
            default:
                stt.setBackgroundResource(R.drawable.editable_frame);
        }
        stt.setPadding(2, 2, 2, 2);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        stt.setLayoutParams(layoutParams);

        statusBar.addView(stt, 0);

    }

    public void removeAddonStatus(String tag, String status) {
        if (getView() == null) {
            return;
        }
        LinearLayout statusBar = (LinearLayout) getView().findViewById(getTempStatusId(tag));

        TextView stt = (TextView) statusBar.findViewWithTag(status);
        if (stt != null) {
            statusBar.removeView(stt);
        }
    }

    public int getSubstitute(String tag) {
        if (getView() == null) {
            return R.drawable.sprites_substitute;
        }

        tag = tag.substring(0, 2);
        switch (tag) {
            case "p1":
                return R.drawable.sprites_substitute_back;
            default:
                return R.drawable.sprites_substitute;
        }
    }

    public int getLastVisibleSpike(String tag, boolean nextInvisible) {
        if (getView() == null) {
            return R.id.field_spikes1;
        }

        tag = tag.substring(0, 2);
        switch (tag) {
            case "p1":
                View layer1 = getView().findViewById(R.id.field_spikes1);
                if (layer1.getVisibility() == View.INVISIBLE) {
                    return R.id.field_spikes1;
                } else {
                    View layer2 = getView().findViewById(R.id.field_spikes2);
                    if (layer2.getVisibility() == View.INVISIBLE) {
                        if (nextInvisible) {
                            return R.id.field_spikes2;
                        } else {
                            return R.id.field_spikes1;
                        }
                    } else {
                        View layer3 = getView().findViewById(R.id.field_spikes3);
                        if (layer3.getVisibility() == View.INVISIBLE) {
                            if (nextInvisible) {
                                return R.id.field_spikes3;
                            } else {
                                return R.id.field_spikes2;
                            }
                        } else {
                            return R.id.field_spikes3;
                        }
                    }
                }
            default:
                layer1 = getView().findViewById(R.id.field_spikes1_o);
                if (layer1.getVisibility() == View.INVISIBLE) {
                    return R.id.field_spikes1_o;
                } else {
                    View layer2 = getView().findViewById(R.id.field_spikes2_o);
                    if (layer2.getVisibility() == View.INVISIBLE) {
                        if (nextInvisible) {
                            return R.id.field_spikes2_o;
                        } else {
                            return R.id.field_spikes1_o;
                        }
                    } else {
                        View layer3 = getView().findViewById(R.id.field_spikes3_o);
                        if (layer3.getVisibility() == View.INVISIBLE) {
                            if (nextInvisible) {
                                return R.id.field_spikes3_o;
                            } else {
                                return R.id.field_spikes2_o;
                            }
                        } else {
                            return R.id.field_spikes3_o;
                        }
                    }
                }
        }
    }

    public int getLastVisibleTSpike(String tag, boolean nextInvisible) {
        if (getView() == null) {
            return R.id.field_tspikes1;
        }

        tag = tag.substring(0, 2);
        switch (tag) {
            case "p1":
                View layer1 = getView().findViewById(R.id.field_tspikes1);
                if (layer1.getVisibility() == View.INVISIBLE) {
                    return R.id.field_tspikes1;
                } else {
                    View layer2 = getView().findViewById(R.id.field_tspikes2);
                    if (layer2.getVisibility() == View.INVISIBLE) {
                        if (nextInvisible) {
                            return R.id.field_tspikes2;
                        } else {
                            return R.id.field_tspikes1;
                        }
                    } else {
                        return R.id.field_tspikes2;
                    }
                }
            default:
                layer1 = getView().findViewById(R.id.field_tspikes1_o);
                if (layer1.getVisibility() == View.INVISIBLE) {
                    return R.id.field_tspikes1_o;
                } else {
                    View layer2 = getView().findViewById(R.id.field_tspikes2_o);
                    if (layer2.getVisibility() == View.INVISIBLE) {
                        if (nextInvisible) {
                            return R.id.field_tspikes2_o;
                        } else {
                            return R.id.field_tspikes1_o;
                        }
                    } else {
                        return R.id.field_tspikes2_o;
                    }
                }
        }
    }

    public void hidePokemon(String tag) {
        if (getView() == null) {
            return;
        }

        RelativeLayout relativeLayout;
        int layoutId;

        tag = tag.substring(0, 3);
        switch (tag) {
            case "p1a":
                layoutId = R.id.p1a;
                break;
            case "p1b":
                layoutId = R.id.p1b;
                break;
            case "p1c":
                layoutId = R.id.p1c;
                break;
            case "p2a":
                layoutId = R.id.p2a;
                break;
            case "p2b":
                layoutId = R.id.p2b;
                break;
            case "p2c":
                layoutId = R.id.p2c;
                break;
            default:
                layoutId = R.id.p2c;
        }

        relativeLayout = (RelativeLayout) getView().findViewById(layoutId);
        relativeLayout.setVisibility(View.INVISIBLE);
    }

    public void displayPokemon(String tag) {
        if (getView() == null) {
            return;
        }

        RelativeLayout relativeLayout;
        int layoutId;

        tag = tag.substring(0, 3);
        switch (tag) {
            case "p1a":
                layoutId = R.id.p1a;
                ((LinearLayout) getView().findViewById(R.id.p1a_temp_status)).removeAllViews();
                break;
            case "p1b":
                layoutId = R.id.p1b;
                ((LinearLayout) getView().findViewById(R.id.p1b_temp_status)).removeAllViews();
                break;
            case "p1c":
                layoutId = R.id.p1c;
                ((LinearLayout) getView().findViewById(R.id.p1c_temp_status)).removeAllViews();
                break;
            case "p2a":
                layoutId = R.id.p2a;
                ((LinearLayout) getView().findViewById(R.id.p2a_temp_status)).removeAllViews();
                break;
            case "p2b":
                layoutId = R.id.p2b;
                ((LinearLayout) getView().findViewById(R.id.p2b_temp_status)).removeAllViews();
                break;
            default:
                layoutId = R.id.p2c;
                ((LinearLayout) getView().findViewById(R.id.p2c_temp_status)).removeAllViews();
        }
        relativeLayout = (RelativeLayout) getView().findViewById(layoutId);
        relativeLayout.setVisibility(View.VISIBLE);
        getView().findViewById(getSpriteId(tag)).setAlpha(1f);
        ImageView sub = (ImageView) relativeLayout.findViewWithTag("Substitute");
        if (sub != null) {
            relativeLayout.removeView(sub);
        }
    }

    public int processHpFraction(String hpFraction) {
        int status = hpFraction.indexOf(' ');
        hpFraction = (status == -1) ? hpFraction : hpFraction.substring(status);
        int fraction = hpFraction.indexOf('/');
        if (fraction == -1) {
            return 0;
        } else {
            int remaining = Integer.parseInt(hpFraction.substring(0, fraction));
            int total = Integer.parseInt(hpFraction.substring(fraction + 1));
            return (int) (((float) remaining / (float) total) * 100);
        }
    }

    public void replacePokemon(String playerTag, String oldPkm, String newPkm) {
        if (playerTag.startsWith("p1")) {
            int index = findPokemonInTeam(getTeamNameArrayList(mPlayer1Team), oldPkm);
            if (index != -1) {
                mPlayer1Team.put(index, new PokemonInfo(getActivity(), newPkm));
            }
        } else {
            int index = findPokemonInTeam(getTeamNameArrayList(mPlayer2Team), oldPkm);
            if (index != -1) {
                mPlayer2Team.put(index, new PokemonInfo(getActivity(), newPkm));
            }
        }
    }

    public HashMap<Integer, PokemonInfo> getTeam(String playerTag) {
        if (playerTag.startsWith("p1")) {
            return mPlayer1Team;
        } else {
            return mPlayer2Team;
        }
    }

    public void setTeam(String playerTag, HashMap<Integer, PokemonInfo> playerTeam) {
        if (playerTag.startsWith("p1")) {
            mPlayer1Team = playerTeam;
        } else {
            mPlayer2Team = playerTeam;
        }
    }

    public String[] getTeamName(HashMap<Integer, PokemonInfo> teamMap) {
        String[] team = new String[teamMap.size()];
        for (Integer i = 0; i < teamMap.size(); i++) {
            PokemonInfo pkm = teamMap.get(i);
            team[i] = pkm.getName();
        }
        return team;
    }

    public ArrayList<String> getTeamNameArrayList(HashMap<Integer, PokemonInfo> teamMap) {
        ArrayList<String> team = new ArrayList<>();
        for (Integer i = 0; i < teamMap.size(); i++) {
            PokemonInfo pkm = teamMap.get(i);
            team.add(pkm.getName());
        }
        return team;
    }

    public int findPokemonInTeam(ArrayList<String> playerTeam, String pkm) {
        String[] specialPkm = {"Arceus", "Gourgeist", "Genesect", "Pumpkaboo"};
        boolean special = false;
        String species = "";
        for (String sp : specialPkm) {
            if (pkm.contains(sp)) {
                special = true;
                species = sp;
                break;
            }
        }
        if (!special) {
            return playerTeam.indexOf(pkm);
        } else {
            for (int i = 0; i < playerTeam.size(); i++) {
                if (playerTeam.get(i).contains(species)) {
                    return i;
                }
            }
            return -1;
        }
    }

    public String getPrintableOutputPokemonSide(String split) {
        return getPrintableOutputPokemonSide(split, true);
    }

    public String getPrintableOutputPokemonSide(String split, boolean start) {
        StringBuilder sb = new StringBuilder();
        if (split.startsWith("p2")) {
            if (start) {
                sb.append("The opposing ");
            } else {
                sb.append("the opposing ");
            }
        }

        int separator = split.indexOf(':');
        String toAppend = (separator == -1) ? split.trim() : split.substring(separator + 1).trim();
        sb.append(toAppend);
        return sb.toString();
    }

    public String getPrintable(String split) {
        int separator = split.indexOf(':');
        return (separator == -1) ? split.trim() : split.substring(separator + 1).trim();
    }

    public String toId(String str) {
        return str.toLowerCase().replaceAll("\\s+", "");
    }

    public void processBoost(String playerTag, String stat, int boost) {
        if (getView() == null) {
            return;
        }
        LinearLayout tempStat = (LinearLayout) getView().findViewById(getTempStatusId(playerTag));
        TextView statBoost;
        int currentBoost;
        int index;
        if (tempStat.findViewWithTag(stat) == null) {
            statBoost = new TextView(getActivity());
            statBoost.setTag(stat);
            statBoost.setTextSize(10);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            statBoost.setLayoutParams(layoutParams);
            currentBoost = boost;
            index = tempStat.getChildCount();
        } else {
            statBoost = (TextView) tempStat.findViewWithTag(stat);
            index = tempStat.indexOfChild(statBoost);
            tempStat.removeView(statBoost);
            String boostDetail = statBoost.getText().toString();
            currentBoost = Integer.parseInt(boostDetail.substring(0, boostDetail.indexOf(" "))) + boost;
        }
        if (currentBoost == 0) {
            return;
        } else {
            if (currentBoost > 0) {
                statBoost.setBackgroundResource(R.drawable.editable_frame);
            } else {
                statBoost.setBackgroundResource(R.drawable.editable_frame_light_orange);
            }
        }
        statBoost.setText(Integer.toString(currentBoost) + " " + stat.substring(0, 1).toUpperCase() + stat.substring(1));
        statBoost.setPadding(2, 2, 2, 2);
        tempStat.addView(statBoost, index);
    }

    public void invertBoost(String playerTag, String[] stats) {
        if (getView() == null) {
            return;
        }
        LinearLayout tempStat = (LinearLayout) getView().findViewById(getTempStatusId(playerTag));
        for (String stat : stats) {
            TextView statBoost = (TextView) tempStat.findViewWithTag(stat);
            if (statBoost != null) {
                String boostDetail = statBoost.getText().toString();
                int currentBoost = -1 * Integer.parseInt(boostDetail.substring(0, boostDetail.indexOf(" ")));
                statBoost.setText(Integer.toString(currentBoost) + boostDetail.substring(boostDetail.indexOf(" ")));
            }
        }
    }

    public void swapBoost(String org, String dest, String... stats) {
        org = org.substring(0, 3);
        dest = dest.substring(0, 3);
        if (getView() == null) {
            return;
        }

        LinearLayout orgTempStat = (LinearLayout) getView().findViewById(getTempStatusId(org));
        LinearLayout destTempStat = (LinearLayout) getView().findViewById(getTempStatusId(dest));

        for (String stat : stats) {
            TextView orgStat = (TextView) orgTempStat.findViewWithTag(stat);
            int orgIndex = orgTempStat.indexOfChild(orgStat);
            TextView destStat = (TextView) destTempStat.findViewWithTag(stat);
            int destIndex = destTempStat.indexOfChild(destStat);
            orgIndex = (orgIndex == -1) ? orgTempStat.getChildCount() : orgIndex;
            orgTempStat.removeView(orgStat);
            destIndex = (destIndex == -1) ? destTempStat.getChildCount() : destIndex;
            destTempStat.removeView(destStat);

            if (destStat != null) {
                orgTempStat.addView(destStat, orgIndex);
            }
            if (orgStat != null) {
                destTempStat.addView(orgStat, destIndex);
            }
        }
    }

    public void copyBoost(String org, String dest) {
        org = org.substring(0, 3);
        dest = dest.substring(0, 3);
        if (getView() == null) {
            return;
        }

        LinearLayout orgTempStat = (LinearLayout) getView().findViewById(getTempStatusId(org));
        LinearLayout destTempStat = (LinearLayout) getView().findViewById(getTempStatusId(dest));

        for (String stat : stats) {
            TextView orgStat = (TextView) orgTempStat.findViewWithTag(stat);
            if (orgStat != null) {
                TextView destStat = new TextView(getActivity());
                destStat.setTag(stat);
                destStat.setPadding(2, 2, 2, 2);
                destStat.setTextSize(10);
                destStat.setText(orgStat.getText());
                destStat.setBackground(orgStat.getBackground());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                destStat.setLayoutParams(layoutParams);
                destTempStat.addView(destStat);
            }
        }
    }

    public AnimatorSet createFlyingMessage(final String tag, AnimatorSet toast, final Spannable message) {
        if (getView() == null) {
            return null;
        }
        message.setSpan(new RelativeSizeSpan(0.8f), 0, message.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        final TextView flyingMessage = new TextView(getActivity());
        flyingMessage.setText(message);
        flyingMessage.setBackgroundResource(R.drawable.editable_frame);
        flyingMessage.setPadding(2, 2, 2, 2);
        flyingMessage.setAlpha(0f);

        toast.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (getView() == null) {
                    return;
                }
                ImageView imageView = (ImageView) getView().findViewById(getSpriteId(tag));

                RelativeLayout relativeLayout = (RelativeLayout) getView().findViewById(getPkmLayoutId(tag));
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.addRule(RelativeLayout.ALIGN_TOP, getSpriteId(tag));
                layoutParams.addRule(RelativeLayout.ALIGN_LEFT, getSpriteId(tag));
                layoutParams.setMargins((int) (imageView.getWidth() * 0.25f), (int) (imageView.getHeight() * 0.5f), 0, 0);
                relativeLayout.addView(flyingMessage, layoutParams);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (getView() == null) {
                    return;
                }

                RelativeLayout relativeLayout = (RelativeLayout) getView().findViewById(getPkmLayoutId(tag));
                relativeLayout.removeView(flyingMessage);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        ObjectAnimator flyingObject = ObjectAnimator.ofFloat(flyingMessage, "y", flyingMessage.getY());
        flyingObject.setDuration(ANIMATION_SHORT);
        flyingObject.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(flyingMessage, "alpha", 0f, 1f);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(ANIMATION_SHORT / 4);

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(flyingMessage, "alpha", 1f, 0f);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setStartDelay(ANIMATION_SHORT / 2);
        fadeOut.setDuration(ANIMATION_SHORT / 4);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(toast);
        animatorSet.play(fadeIn).with(toast);
        animatorSet.play(flyingObject).after(fadeIn);
        animatorSet.play(fadeOut).after(fadeIn);

        return animatorSet;
    }

}
