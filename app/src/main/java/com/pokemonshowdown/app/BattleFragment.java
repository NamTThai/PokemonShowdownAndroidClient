package com.pokemonshowdown.app;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.support.v4.app.Fragment;
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
import android.text.style.TextAppearanceSpan;
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

import com.pokemonshowdown.data.BattleFieldData;
import com.pokemonshowdown.data.MyApplication;
import com.pokemonshowdown.data.Pokemon;

import org.w3c.dom.Text;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class BattleFragment extends Fragment {
    public final static String BTAG = BattleFragment.class.getName();
    public final static String ROOM_ID = "Room Id";
    public final static int ANIMATION_SHORT = 500;
    public final static int ANIMATION_LONG = 1000;
    private final static String[] stats = {"atk", "def", "spa", "spd", "spe", "accuracy", "evasion"};

    private ArrayDeque<AnimatorSet> mAnimatorSetQueue;
    private int[] progressBarHolder = new int[6];

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
        try {
            processMajorAction(message);
        } catch (Exception e) {
            Log.d(BTAG, "error is in " + message);
        }
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
                if (messageDetails.startsWith("p2")) {
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
                final int toBeSwapped;
                final int spriteId;

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
                setOldHp(messageDetails, hpInt);
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
                            setStatus(messageDetails.substring(0, 3), status.toLowerCase());
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


    private void processMinorAction(String command, final String messageDetails) {
        if (messageDetails.contains("[silent]")) {
            return;
        }

        int separator;
        int start;
        Integer oldHP;
        final int lostHP;
        final int intAmount;
        String remaining;
        String toAppend;
        StringBuilder toAppendBuilder = new StringBuilder();
        Spannable toAppendSpannable;
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

        separator = messageDetails.indexOf('|');
        final String[] split = messageDetails.split("\\|");

        AnimatorSet toast;
        AnimatorSet animatorSet;
        Animator animator;

        if (getView() == null) {
            return;
        }

        switch (command) {
            case "-damage":
                attacker = getPrintable(split[0]);
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
                    toAppendBuilder.append(lostHP).append("% of its health!");
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
                break;

            case "-heal":
                attacker = getPrintable(split[0]);
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
                                trimmedOfEffect = getPrintableOutputPokemonSide(ofSource);
                                toAppendBuilder.append(trimmedOfEffect).append(" had its energy drained!");
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
                break;
            case "-sethp":
                switch (getPrintable(fromEffectId)) {
                    case "painsplit":
                        toast = makeMinorToast(new SpannableString("The battlers shared their pain!"));
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
                if (intAmount == 2) {
                    statAmount = " harshly";
                } else if (intAmount >= 3) {
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
                break;
            case "-setboost":
                attackerOutputName = getPrintableOutputPokemonSide(split[0]);
                if (fromEffect != null) {
                    switch (getPrintable(fromEffectId)) {
                        case "bellydrum":
                            toast = makeMinorToast(new SpannableString(attackerOutputName + " cut its own HP and maximized its Attack!"));
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
                            toast = makeMinorToast(new SpannableString(attackerOutputName + " maxed its Attack!"));
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
                break;
            case "-invertboost":
                attackerOutputName = getPrintableOutputPokemonSide(split[0]);
                toAppendBuilder.append(attackerOutputName).append("'s stat changes were inverted!");
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
                break;
            case "-crit":
                toAppendSpannable = new SpannableString("It's a critical hit!");
                toast = makeMinorToast(toAppendSpannable);
                animatorSet = createFlyingMessage(split[0], toast, toAppendSpannable);
                startAnimation(animatorSet);
                break;
            case "-supereffective":
                toAppendSpannable = new SpannableString("It's super effective!");
                toast = makeMinorToast(toAppendSpannable);
                animatorSet = createFlyingMessage(split[0], toast, toAppendSpannable);
                startAnimation(animatorSet);
                break;
            case "-resisted":
                toAppendSpannable = new SpannableString("It's not very effective...");
                toast = makeMinorToast(toAppendSpannable);
                animatorSet = createFlyingMessage(split[0], toast, toAppendSpannable);
                startAnimation(animatorSet);
                break;
            case "-immune":
                attackerOutputName = getPrintableOutputPokemonSide(split[0], false);
                toAppendBuilder.append("It doesn't affect ");
                toAppendBuilder.append(attackerOutputName);
                toAppendBuilder.append(".");
                toAppendSpannable = new SpannableString(toAppendBuilder);
                toast = makeMinorToast(toAppendSpannable);
                animatorSet = createFlyingMessage(split[0], toast, toAppendSpannable);
                startAnimation(animatorSet);
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
                animatorSet = createFlyingMessage(split[0], toast, toAppendSpannable);
                startAnimation(animatorSet);
                break;
            default:
                toAppendSpannable = new SpannableString(command + ":" + messageDetails);
                toast = makeMinorToast(toAppendSpannable);
                startAnimation(toast);
                break;
        }

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

    private AnimatorSet makeMinorToast(final Spannable message) {
        message.setSpan(new RelativeSizeSpan(0.8f), 0, message.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

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
    
    private int getPkmLayoutId(String tag) {
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
    
    private int getTempStatusId(String tag) {
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
    
    private int getOldHp(String tag) {
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
    
    private void setOldHp(String tag, int hp) {
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
        int id = getTempStatusId(tag);
        if (getView() == null) {
            return;
        }
        LinearLayout statusBar = (LinearLayout) getView().findViewById(id);

        TextView stt = new TextView(getActivity());
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
        stt.setPadding(2, 2, 2, 2);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        stt.setLayoutParams(layoutParams);

        statusBar.addView(stt, 0);

    }
    
    private void hidePokemon(String tag) {
        if (getView() == null) {
            return;
        }

        tag = tag.substring(0, 3);
        switch (tag) {
            case "p1a":
                getView().findViewById(R.id.p1a).setVisibility(View.INVISIBLE);
                return;
            case "p1b":
                getView().findViewById(R.id.p1b).setVisibility(View.INVISIBLE);
                return;
            case "p1c":
                getView().findViewById(R.id.p1c).setVisibility(View.INVISIBLE);
                return;
            case "p2a":
                getView().findViewById(R.id.p2a).setVisibility(View.INVISIBLE);
                return;
            case "p2b":
                getView().findViewById(R.id.p2b).setVisibility(View.INVISIBLE);
                return;
            case "p2c":
                getView().findViewById(R.id.p2c).setVisibility(View.INVISIBLE);
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
                ((LinearLayout) getView().findViewById(R.id.p1a_temp_status)).removeAllViews();
                return;
            case "p1b":
                getView().findViewById(R.id.p1b).setVisibility(View.VISIBLE);
                ((LinearLayout) getView().findViewById(R.id.p1b_temp_status)).removeAllViews();
                return;
            case "p1c":
                getView().findViewById(R.id.p1c).setVisibility(View.VISIBLE);
                ((LinearLayout) getView().findViewById(R.id.p1c_temp_status)).removeAllViews();
                return;
            case "p2a":
                getView().findViewById(R.id.p2a).setVisibility(View.VISIBLE);
                ((LinearLayout) getView().findViewById(R.id.p2a_temp_status)).removeAllViews();
                return;
            case "p2b":
                getView().findViewById(R.id.p2b).setVisibility(View.VISIBLE);
                ((LinearLayout) getView().findViewById(R.id.p2b_temp_status)).removeAllViews();
                return;
            case "p2c":
                getView().findViewById(R.id.p2c).setVisibility(View.VISIBLE);
                ((LinearLayout) getView().findViewById(R.id.p2c_temp_status)).removeAllViews();
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
        String[] specialPkm = {"Arceus", "Gourgeist"};
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

    private String getPrintableOutputPokemonSide(String split) {
        return getPrintableOutputPokemonSide(split, true);
    }

    private String getPrintableOutputPokemonSide(String split, boolean start) {
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

    private String getPrintable(String split) {
        int separator = split.indexOf(':');
        return split.substring(separator + 1).trim();
    }

    private String toId(String str) {
        return str.toLowerCase().replaceAll("\\s+", "");
    }

    private void processBoost(String playerTag, String stat, int boost) {
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
            LinearLayout.LayoutParams layoutParams= new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
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

    private void swapBoost(String org, String dest, String... stats) {
        org = org.substring(0, 3);
        dest = dest.substring(0, 3);
        if (getView() == null) {
            return;
        }

        LinearLayout orgTempStat = (LinearLayout) getView().findViewById(getTempStatusId(org));
        LinearLayout destTempStat = (LinearLayout) getView().findViewById(getTempStatusId(dest));

        for(String stat : stats) {
            TextView orgStat = (TextView) orgTempStat.findViewWithTag(stat);
            int orgIndex = orgTempStat.indexOfChild(orgStat);
            TextView destStat = (TextView) destTempStat.findViewWithTag(stat);
            int destIndex = destTempStat.indexOfChild(destStat);
            orgIndex = (orgIndex == -1) ? orgTempStat.getChildCount(): orgIndex;
            orgTempStat.removeView(orgStat);
            destIndex = (destIndex == -1) ? destTempStat.getChildCount(): destIndex;
            destTempStat.removeView(destStat);

            if (destStat != null) {
                orgTempStat.addView(destStat, orgIndex);
            }
            if (orgStat != null) {
                destTempStat.addView(orgStat, destIndex);
            }
        }
    }

    private void copyBoost(String org, String dest) {
        org = org.substring(0, 3);
        dest = dest.substring(0, 3);
        if (getView() == null) {
            return;
        }

        LinearLayout orgTempStat = (LinearLayout) getView().findViewById(getTempStatusId(org));
        LinearLayout destTempStat = (LinearLayout) getView().findViewById(getTempStatusId(dest));

        for(String stat : stats) {
            TextView orgStat = (TextView) orgTempStat.findViewWithTag(stat);
            if (orgStat != null) {
                destTempStat.addView(orgStat);
            }
        }
    }

    private AnimatorSet createFlyingMessage(final String tag, AnimatorSet toast, final Spannable message) {
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
                layoutParams.setMargins(0, (int) (imageView.getHeight() * 0.5f), 0, 0);
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

        ObjectAnimator flyingObject = ObjectAnimator.ofFloat(flyingMessage, "y", 0.5f);
        flyingObject.setDuration(ANIMATION_SHORT);
        flyingObject.setInterpolator(new AccelerateDecelerateInterpolator());

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
