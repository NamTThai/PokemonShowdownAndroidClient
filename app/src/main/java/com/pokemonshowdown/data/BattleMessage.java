package com.pokemonshowdown.data;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pokemonshowdown.app.BattleFragment;
import com.pokemonshowdown.app.ChatRoomFragment;
import com.pokemonshowdown.app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class BattleMessage {

    public static void processMajorAction(final BattleFragment battleFragment, final String message) {
        BattleFieldData.AnimationData animationData = BattleFieldData.get(battleFragment.getActivity()).getAnimationInstance(battleFragment.getRoomId());
        final BattleFieldData.ViewData viewData = BattleFieldData.get(battleFragment.getActivity()).getViewData(battleFragment.getRoomId());
        String command = (message.indexOf('|') == -1) ? message : message.substring(0, message.indexOf('|'));
        final String messageDetails = message.substring(message.indexOf('|') + 1);
        if (command.startsWith("-")) {
            processMinorAction(battleFragment, command, messageDetails);
            return;
        }

        int separator = messageDetails.indexOf('|');
        final String[] split = messageDetails.split("\\|");
        final ArrayList<PokemonInfo> team1 = battleFragment.getPlayer1Team();
        final ArrayList<PokemonInfo> team2 = battleFragment.getPlayer2Team();
        final View view = battleFragment.getView();

        final ArrayList<PokemonInfo> team;
        final String position, attacker;
        final int iconId;
        final PokemonInfo pokemonInfo;
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
                String user = split[0];
                String userMessage = split[1];
                toAppend = user + ": " + userMessage;
                toAppendSpannable = new SpannableString(toAppend);
                toAppendSpannable.setSpan(new ForegroundColorSpan(ChatRoomFragment.getColorStrong(user)),
                        0, user.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                logMessage = new SpannableString(toAppendSpannable);
                break;
            case "raw":
                toast = battleFragment.makeToast(Html.fromHtml(messageDetails).toString());
                battleFragment.startAnimation(toast);
                logMessage = new SpannableString(Html.fromHtml(messageDetails).toString());
                break;
            case "message":
                toast = battleFragment.makeToast(messageDetails);
                battleFragment.startAnimation(toast);
                logMessage = new SpannableString(messageDetails);
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
                    playerType = split[0];
                    playerName = split[1];
                    avatar = split[2];
                }
                final int avatarResource;
                if (avatar != null) {
                    avatarResource = battleFragment.getActivity().getApplicationContext()
                            .getResources().getIdentifier("avatar_" + avatar, "drawable",
                                    battleFragment.getActivity().getApplicationContext().getPackageName());

                } else {
                    avatarResource = 0;
                }
                if (playerType.equals("p1")) {
                    animationData.setPlayer1(playerName);
                    battleFragment.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (view == null) {
                                viewData.addViewSetterOnHold(R.id.username, playerName,
                                        BattleFieldData.ViewData.SetterType.TEXTVIEW_SETTEXT);
                                viewData.addViewSetterOnHold(R.id.avatar, avatarResource,
                                        BattleFieldData.ViewData.SetterType.IMAGEVIEW_SETIMAGERESOURCE);
                            } else {
                                ((TextView) view.findViewById(R.id.username)).setText(playerName);
                                ((ImageView) view.findViewById(R.id.avatar)).setImageResource(avatarResource);
                            }
                        }
                    });
                    battleFragment.setPlayer1(playerName);
                } else {
                    animationData.setPlayer2(playerName);
                    battleFragment.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (view == null) {
                                viewData.addViewSetterOnHold(R.id.username_o, playerName,
                                        BattleFieldData.ViewData.SetterType.TEXTVIEW_SETTEXT);
                                viewData.addViewSetterOnHold(R.id.avatar_o, avatarResource,
                                        BattleFieldData.ViewData.SetterType.IMAGEVIEW_SETIMAGERESOURCE);
                            } else {
                                ((TextView) view.findViewById(R.id.username_o)).setText(playerName);
                                ((ImageView) view.findViewById(R.id.avatar_o)).setImageResource(avatarResource);
                            }
                        }
                    });
                    battleFragment.setPlayer2(playerName);
                }
                break;
            case "tier":
                toAppend = "Format:" + "\n" + messageDetails;
                toAppendSpannable = new SpannableString(toAppend);
                toAppendSpannable.setSpan(new StyleSpan(Typeface.BOLD),
                        toAppend.indexOf('\n') + 1, toAppend.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                logMessage = new SpannableString(toAppendSpannable);
                break;

            case "rated":
                toAppend = command.toUpperCase();
                toAppendSpannable = new SpannableString(toAppend);
                toAppendSpannable.setSpan(new ForegroundColorSpan(R.color.dark_blue),
                        0, toAppend.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                logMessage = new SpannableString(toAppendSpannable);
                break;

            case "rule":
                toAppendSpannable = new SpannableString(messageDetails);
                toAppendSpannable.setSpan(new StyleSpan(Typeface.ITALIC),
                        0, messageDetails.indexOf(':') + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                logMessage = new SpannableString(toAppendSpannable);
                break;

            case "":
                logMessage = new SpannableString(" ");
                break;

            case "clearpoke":
                battleFragment.setPlayer1Team(new ArrayList<PokemonInfo>());
                battleFragment.setPlayer2Team(new ArrayList<PokemonInfo>());
                break;

            case "poke":
                playerType = split[0];
                int comma = split[1].indexOf(',');
                final String pokeName = (comma == -1) ? split[1] : split[1].substring(0, comma);
                team = (playerType.equals("p1")) ? team1 : team2;
                iconId = battleFragment.getIconId(playerType, team.size());
                pokemonInfo = new PokemonInfo(battleFragment.getActivity(), processSpecialName(pokeName));
                team.add(pokemonInfo);

                battleFragment.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int imageResource = Pokemon.getPokemonIcon(battleFragment.getActivity(),
                                MyApplication.toId(pokeName));
                        if (view == null) {
                            viewData.addViewSetterOnHold(iconId, imageResource,
                                    BattleFieldData.ViewData.SetterType.IMAGEVIEW_SETIMAGERESOURCE);
                        } else {
                            ImageView icon = (ImageView) view.findViewById(iconId);
                            if (icon != null) {
                                icon.setImageResource(imageResource);
                            }
                        }
                    }
                });
                break;
            case "teampreview":
                battleFragment.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (view == null) {
                            return;
                        }

                        FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.battle_interface);
                        frameLayout.removeAllViews();
                        battleFragment.getActivity().getLayoutInflater().inflate(R.layout.fragment_battle_teampreview, frameLayout);
                        for (int i = 0; i < team1.size(); i++) {
                            ImageView sprites = (ImageView) view.findViewById(battleFragment.getTeamPreviewSpriteId("p1", i));
                            PokemonInfo pkm = team1.get(i);
                            sprites.setImageResource(Pokemon.getPokemonSprite(battleFragment.getActivity(),
                                    MyApplication.toId(pkm.getName()), true, pkm.isFemale(), pkm.isShiny()));
                        }
                        for (int i = 0; i < team2.size(); i++) {
                            ImageView sprites = (ImageView) view.findViewById(battleFragment.getTeamPreviewSpriteId("p2", i));
                            PokemonInfo pkm = team2.get(i);
                            sprites.setImageResource(Pokemon.getPokemonSprite(battleFragment.getActivity(),
                                    MyApplication.toId(pkm.getName()), false, pkm.isFemale(), pkm.isShiny()));
                        }
                    }
                });
                toAppendBuilder = new StringBuilder();
                toAppendBuilder.append(battleFragment.getPlayer1()).append("'s Team: ");
                String[] p1Team = battleFragment.getTeamNameStringArray(team1);
                for (int i = 0; i < p1Team.length - 1; i++) {
                    toAppendBuilder.append(p1Team[i]).append("/");
                }
                toAppendBuilder.append(p1Team[p1Team.length - 1]);

                toAppendBuilder.append("\n").append(battleFragment.getPlayer2()).append("'s Team: ");
                String[] p2Team = battleFragment.getTeamNameStringArray(team2);
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
                        battleFragment.setBattling(requestJson);
                        setDisplayTeam(battleFragment, requestJson);
                    }
                } catch (JSONException e) {
                    new AlertDialog.Builder(battleFragment.getActivity())
                            .setMessage(R.string.request_error)
                            .create()
                            .show();
                    return;
                }
                break;

            case "inactive":
                final String inactive;
                final String player;
                if ((messageDetails.startsWith(battleFragment.getPlayer1())) || (messageDetails.startsWith("Player 1"))) {
                    player = "p1";
                } else {
                    if ((messageDetails.startsWith(battleFragment.getPlayer2())) || (messageDetails.startsWith("Player 2"))) {
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
                battleFragment.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (player.equals("p1")) {
                            if (view == null) {
                                viewData.addViewSetterOnHold(R.id.inactive, inactive,
                                        BattleFieldData.ViewData.SetterType.TEXTVIEW_SETTEXT);
                                viewData.addViewSetterOnHold(R.id.inactive, null,
                                        BattleFieldData.ViewData.SetterType.VIEW_VISIBLE);
                            } else {
                                TextView textView = (TextView) view.findViewById(R.id.inactive);
                                textView.setVisibility(View.VISIBLE);
                                textView.setText(inactive);
                            }
                        } else {
                            if (view == null) {
                                viewData.addViewSetterOnHold(R.id.inactive_o, inactive,
                                        BattleFieldData.ViewData.SetterType.TEXTVIEW_SETTEXT);
                                viewData.addViewSetterOnHold(R.id.inactive_o, null,
                                        BattleFieldData.ViewData.SetterType.VIEW_VISIBLE);
                            } else {
                                TextView textView = (TextView) view.findViewById(R.id.inactive_o);
                                textView.setVisibility(View.VISIBLE);
                                textView.setText(inactive);
                            }
                        }
                    }
                });
                toAppendSpannable = new SpannableString(messageDetails);
                toAppendSpannable.setSpan(new ForegroundColorSpan(R.color.dark_red),
                        0, messageDetails.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                logMessage = new SpannableString(toAppendSpannable);
                break;

            case "inactiveoff":
                battleFragment.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (view == null) {
                            viewData.addViewSetterOnHold(R.id.inactive, null,
                                    BattleFieldData.ViewData.SetterType.VIEW_GONE);
                            viewData.addViewSetterOnHold(R.id.inactive_o, null,
                                    BattleFieldData.ViewData.SetterType.VIEW_GONE);
                        } else {
                            view.findViewById(R.id.inactive).setVisibility(View.GONE);
                            view.findViewById(R.id.inactive_o).setVisibility(View.GONE);
                        }
                    }
                });
                toAppendSpannable = new SpannableString(messageDetails);
                toAppendSpannable.setSpan(new ForegroundColorSpan(R.color.dark_red),
                        0, messageDetails.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                logMessage = new SpannableString(toAppendSpannable);
                break;

            case "start":
                battleFragment.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (view == null) {
                            viewData.addViewSetterOnHold(R.id.battle_interface, null,
                                    BattleFieldData.ViewData.SetterType.BATTLE_START);
                        } else {
                            FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.battle_interface);
                            frameLayout.removeAllViews();
                            battleFragment.getActivity().getLayoutInflater()
                                    .inflate(R.layout.fragment_battle_animation, frameLayout);
                        }
                    }
                });
                toAppend = battleFragment.getPlayer1() + " vs. " + battleFragment.getPlayer2();
                toAppendSpannable = new SpannableString(toAppend);
                toAppendSpannable.setSpan(new StyleSpan(Typeface.BOLD),
                        0, toAppend.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
                toAppendSpannable.setSpan(new StyleSpan(Typeface.BOLD),
                        start, start + move.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                logMessage = toAppendSpannable;
                toast = battleFragment.makeToast(logMessage);

                toast.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (view == null) {
                            return;
                        }
                        AnimatorSet animatorSet = BattleAnimation.processMove(move, view, battleFragment, split);
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
                battleFragment.startAnimation(toast);
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
                battleFragment.setOldHp(messageDetails, hpInt);
                hpString = Integer.toString(hpInt);

                String speciesId = MyApplication.toId(species);

                spriteId = Pokemon.getPokemonSprite(battleFragment.getActivity(), speciesId, back, female, shiny);
                iconId = Pokemon.getPokemonIcon(battleFragment.getActivity(), speciesId);

                // Switching sprites and icons
                final String levelFinal = attacker + " " + level;
                final String genderFinal = gender;
                ArrayList<PokemonInfo> playerTeam = battleFragment.getTeam(messageDetails);
                if (playerTeam == null) {
                    playerTeam = new ArrayList<>();
                }
                if (battleFragment.findPokemonInTeam(playerTeam, species) == -1) {
                    playerTeam.add(playerTeam.size(), new PokemonInfo(battleFragment.getActivity(), species));
                    toBeSwapped = playerTeam.size() - 1;
                } else {
                    toBeSwapped = battleFragment.findPokemonInTeam(playerTeam, species);
                }
                int j = battleFragment.getTeamSlot(messageDetails);
                PokemonInfo holder = playerTeam.get(j);
                playerTeam.set(j, playerTeam.get(toBeSwapped));
                playerTeam.set(toBeSwapped, holder);

                battleFragment.setTeam(messageDetails, playerTeam);

                if (command.equals("switch")) {
                    //TODO need to buffer batonpass/uturn/voltswitch for switching out message
                    //then we switch in
                    if (messageDetails.startsWith("p2")) {
                        toAppendBuilder.append(battleFragment.getPlayer2()).append(" sent out ").append(species).append("!");
                    } else {
                        toAppendBuilder.append("Go! ").append(species).append("!");
                    }
                } else {
                    if (command.equals("drag")) {
                        toAppendBuilder.append(species).append(" was dragged out!");
                    } else { //replace, no text here (illusion mons)
                    }
                }


                toast = battleFragment.makeToast(new SpannableStringBuilder(toAppendBuilder));
                toast.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (view == null) {
                            return;
                        }

                        battleFragment.displayPokemon(messageDetails.substring(0, 3));

                        ImageView sprites = (ImageView) view.findViewById(battleFragment.getSpriteId(messageDetails.substring(0, 3)));
                        if (sprites != null) {
                            sprites.setImageResource(spriteId);
                        }
                        ImageView iconLeader = (ImageView) view.findViewById(battleFragment.getIconId(messageDetails, battleFragment.getTeamSlot(messageDetails)));
                        Drawable leader = iconLeader.getDrawable();
                        ImageView iconTrailer = (ImageView) view.findViewById(battleFragment.getIconId(messageDetails, toBeSwapped));
                        iconTrailer.setImageDrawable(leader);
                        iconLeader.setImageResource(iconId);

                        TextView pkmName = (TextView) view.findViewById(battleFragment.getSpriteNameid(messageDetails.substring(0, 3)));
                        if (pkmName != null) {
                            pkmName.setText(levelFinal);
                        }

                        ImageView gender = (ImageView) view.findViewById(battleFragment.getGenderId(messageDetails.substring(0, 3)));
                        if (gender != null) {
                            if (genderFinal.equals("M")) {
                                gender.setImageResource(R.drawable.ic_gender_male);
                            } else {
                                if (genderFinal.equals("F")) {
                                    gender.setImageResource(R.drawable.ic_gender_female);
                                }
                            }
                        }

                        TextView hpText = (TextView) view.findViewById(battleFragment.getHpId(messageDetails.substring(0, 3)));
                        ProgressBar hpBar = (ProgressBar) view.findViewById(battleFragment.getHpBarId(messageDetails.substring(0, 3)));
                        if (hpText != null) {
                            hpText.setText(hpString);
                        }
                        if (hpBar != null) {
                            hpBar.setProgress(hpInt);
                        }

                        if (!status.equals("")) {
                            battleFragment.setAddonStatus(messageDetails.substring(0, 3), status.toLowerCase());
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
                battleFragment.startAnimation(toast);
                break;

            case "detailschange":
                final String forme = (split[1].contains(",")) ? split[1].substring(0, split[1].indexOf(',')) : split[1];

                position = split[0].substring(0, 3);
                species = split[0].substring(5);

                battleFragment.formChange(position, species, forme);

                toast = battleFragment.makeToast("Transforming", BattleFragment.ANIMATION_SHORT);
                toast.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (view == null) {
                            return;
                        }

                        boolean back = split[0].startsWith("p1");
                        ImageView sprite = (ImageView) view.findViewById(battleFragment.getSpriteId(position));
                        sprite.setImageResource(Pokemon.getPokemonSprite(battleFragment.getActivity(),
                                MyApplication.toId(forme), back, false, false));
                        ImageView icon = (ImageView) view.findViewById(battleFragment.getIconId(position));
                        icon.setImageResource(Pokemon.getPokemonIcon(battleFragment.getActivity(),
                                MyApplication.toId(forme)));
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
                battleFragment.startAnimation(toast);
                break;

            case "faint":
                position = split[0];
                attacker = split[0].substring(5);
                toAppendBuilder = new StringBuilder();
                if (messageDetails.startsWith("p2")) {
                    toAppendBuilder.append("The opposing ");
                }
                toAppendBuilder.append(attacker).append(" fainted!");
                toast = battleFragment.makeToast(new SpannableStringBuilder(toAppendBuilder));
                toast.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (view == null) {
                            return;
                        }

                        battleFragment.hidePokemon(position);
                        ImageView fainted = (ImageView) view.findViewById(battleFragment.getIconId(position));
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

                battleFragment.startAnimation(toast);
                logMessage = new SpannableString(toAppendBuilder);
                break;

            case "turn":
                if (view == null) {
                    return;
                }
                TextView turn = (TextView) view.findViewById(R.id.turn);
                animator = ObjectAnimator.ofFloat(turn, "alpha", 0f, 1f);
                toAppend = "TURN " + messageDetails;
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (view == null) {
                            return;
                        }
                        view.findViewById(R.id.turn).setVisibility(View.VISIBLE);
                        ((TextView) view.findViewById(R.id.turn)).setText(toAppend);
                        (view.findViewById(R.id.inactive)).setVisibility(View.GONE);
                        (view.findViewById(R.id.inactive_o)).setVisibility(View.GONE);
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
                animator.setDuration(BattleFragment.ANIMATION_SHORT);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorSet = new AnimatorSet();
                animatorSet.play(animator);
                battleFragment.startAnimation(animatorSet);
                toAppendSpannable = new SpannableString(toAppend.toUpperCase());
                toAppendSpannable.setSpan(new UnderlineSpan(), 0, toAppend.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                toAppendSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, toAppend.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                toAppendSpannable.setSpan(new RelativeSizeSpan(1.25f), 0, toAppend.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                toAppendSpannable.setSpan(new ForegroundColorSpan(R.color.dark_blue), 0, toAppend.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                logMessage = new SpannableString(toAppendSpannable);
                break;

            case "win":
                toAppend = messageDetails + " has won the battle!";
                toast = battleFragment.makeToast(new SpannableString(toAppend));
                battleFragment.startAnimation(toast);
                logMessage = new SpannableString(toAppend);
                break;

            case "cant":
                String attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                toAppendBuilder = new StringBuilder();
                switch (battleFragment.getPrintable(MyApplication.toId(split[1]))) {
                    case "taunt":
                        toAppendBuilder.append(attackerOutputName).append(" can't use ").append(battleFragment.getPrintable(split[2])).append(" after the taunt!");
                        break;

                    case "gravity":
                        toAppendBuilder.append(attackerOutputName).append(" can't use ").append(battleFragment.getPrintable(split[2])).append(" because of gravity!");
                        break;

                    case "healblock":
                        toAppendBuilder.append(attackerOutputName).append(" can't use ").append(battleFragment.getPrintable(split[2])).append(" because of Heal Block!");
                        break;

                    case "imprison":
                        toAppendBuilder.append(attackerOutputName).append(" can't use the sealed ").append(battleFragment.getPrintable(split[2])).append("!");
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
                        attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
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
                        toAppendBuilder.append(attackerOutputName).append(" used ").append(battleFragment.getPrintable(split[2]));
                        toAppendBuilder.append("\nBut there was no PP left for the move!");
                        break;

                    default:
                        toAppendBuilder.append(attackerOutputName);
                        if (split.length > 2) {
                            toAppendBuilder.append(" can't use ").append(battleFragment.getPrintable(split[2]));
                        } else {
                            toAppendBuilder.append(" can't move");
                        }
                        toAppendBuilder.append("!");
                        break;
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString("Failed!"));
                battleFragment.startAnimation(animatorSet);
                break;

            default:
                toast = battleFragment.makeToast(message, BattleFragment.ANIMATION_LONG);
                battleFragment.startAnimation(toast);
                logMessage = new SpannableString(message);
                break;
        }

        battleFragment.addToLog(logMessage);
    }


    public static void processMinorAction(final BattleFragment battleFragment, String command, final String messageDetails) {
        final View view = battleFragment.getView();

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
            fromEffectId = MyApplication.toId(fromEffect);
        }
        int of = messageDetails.indexOf("[of]");
        if (of != -1) {
            remaining = messageDetails.substring(of + 5);
            separator = remaining.indexOf('|');
            ofSource = (separator == -1) ? remaining : remaining.substring(remaining.indexOf(':'), separator);

            trimmedOfEffect = MyApplication.toId(ofSource);
        }

        final String[] split = messageDetails.split("\\|");

        AnimatorSet toast;
        AnimatorSet animatorSet;

        if (view == null) {
            return;
        }

        switch (command) {
            case "-damage":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                oldHP = battleFragment.getOldHp(messageDetails);
                remaining = (split[1].indexOf(' ') == -1) ? split[1] : split[1].substring(0, split[1].indexOf(' '));
                intAmount = processHpFraction(remaining);
                battleFragment.setOldHp(messageDetails, intAmount);
                lostHP = intAmount - oldHP;

                if (fromEffectId != null) {
                    switch (battleFragment.getPrintable(fromEffectId)) {
                        case "stealthrock":
                            attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
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
                            attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
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
                                toAppendBuilder.append(attackerOutputName).append(" is hurt by ").append(battleFragment.getPrintable(ofSource)).append("'s ").append(battleFragment.getPrintable(fromEffect)).append("!");
                            } else if (fromEffectId.contains(":")) {
                                toAppendBuilder.append(attackerOutputName).append(" is hurt by its").append(battleFragment.getPrintable(fromEffect)).append("!");
                            } else {
                                toAppendBuilder.append(attackerOutputName).append(" lost some HP because of ").append(battleFragment.getPrintable(fromEffect)).append("!");
                            }
                            break;
                    }
                } else {
                    toAppendBuilder.append(attackerOutputName).append(" lost ");
                    toAppendBuilder.append(-lostHP).append("% of its health!");
                }

                toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));

                final TextView damage = new TextView(battleFragment.getActivity());
                damage.setText(lostHP + "%");
                damage.setBackgroundResource(R.drawable.editable_frame_light_red);
                damage.setPadding(2, 2, 2, 2);
                damage.setAlpha(0f);

                toast.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (view == null) {
                            return;
                        }
                        ((TextView) view.findViewById(battleFragment.getHpId(messageDetails))).setText(Integer.toString(intAmount));

                        ImageView imageView = (ImageView) view.findViewById(battleFragment.getSpriteId(messageDetails));

                        RelativeLayout relativeLayout = (RelativeLayout) view.findViewById(battleFragment.getPkmLayoutId(messageDetails));
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        layoutParams.addRule(RelativeLayout.ALIGN_TOP, battleFragment.getSpriteId(messageDetails));
                        layoutParams.addRule(RelativeLayout.ALIGN_LEFT, battleFragment.getSpriteId(messageDetails));
                        layoutParams.setMargins((int) (imageView.getWidth() * 0.5f), (int) (imageView.getHeight() * 0.5f), 0, 0);
                        relativeLayout.addView(damage, layoutParams);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (view == null) {
                            return;
                        }

                        RelativeLayout relativeLayout = (RelativeLayout) view.findViewById(battleFragment.getPkmLayoutId(messageDetails));
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
                fadeIn.setDuration(BattleFragment.ANIMATION_SHORT / 4);

                ObjectAnimator fadeOut = ObjectAnimator.ofFloat(damage, "alpha", 1f, 0f);
                fadeOut.setInterpolator(new AccelerateInterpolator());
                fadeOut.setStartDelay(BattleFragment.ANIMATION_SHORT / 2);
                fadeOut.setDuration(BattleFragment.ANIMATION_SHORT / 4);

                ProgressBar hpBar = (ProgressBar) view.findViewById(battleFragment.getHpBarId(messageDetails));
                ObjectAnimator hpCountDownBar = ObjectAnimator.ofInt(hpBar, "progress", intAmount);
                hpCountDownBar.setDuration(BattleFragment.ANIMATION_SHORT);
                hpCountDownBar.setInterpolator(new AccelerateDecelerateInterpolator());

                animatorSet = new AnimatorSet();
                animatorSet.play(toast);
                animatorSet.play(hpCountDownBar).with(toast);
                animatorSet.play(fadeIn).with(toast);
                animatorSet.play(fadeOut).after(fadeIn);

                battleFragment.startAnimation(animatorSet);

                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-heal":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);

                oldHP = battleFragment.getOldHp(messageDetails);

                remaining = (split[1].indexOf(' ') == -1) ? split[1] : split[1].substring(0, split[1].indexOf(' '));
                intAmount = processHpFraction(remaining);
                battleFragment.setOldHp(messageDetails, intAmount);
                lostHP = intAmount - oldHP;

                if (fromEffectId != null) {
                    switch (battleFragment.getPrintable(fromEffectId)) {
                        case "ingrain":
                            toAppendBuilder.append(attackerOutputName).append(" absorbed nutrients with its roots!");
                            break;
                        case "aquaring":
                            attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                            toAppendBuilder.append("Aqua Ring restored ").append(attackerOutputName).append("'s HP!");
                            break;
                        case "raindish":
                        case "dryskin":
                        case "icebody":
                            toAppendBuilder.append(attackerOutputName).append("'s ").append(battleFragment.getPrintable(fromEffect)).append(" heals it!");
                            break;
                        case "healingwish":
                            attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
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
                                toAppendBuilder.append(battleFragment.getPrintableOutputPokemonSide(wisher)).append("'s wish came true!");
                            }
                            break;
                        case "drain":
                            if (trimmedOfEffect != null) {
                                toAppendBuilder.append(battleFragment.getPrintableOutputPokemonSide(ofSource)).append(" had its energy drained!");
                                break;
                            }
                            // we should never enter here
                            toAppendBuilder.append(attackerOutputName).append(" drained health!");
                            break;

                        case "leftovers":
                        case "shellbell":
                            toAppendBuilder.append(attackerOutputName).append(" restored a little HP using its ").append(battleFragment.getPrintable(fromEffect)).append("!");
                            break;
                        default:
                            toAppendBuilder.append(attackerOutputName).append(" restored HP using its ").append(battleFragment.getPrintable(fromEffect)).append("!");
                            break;
                    }
                } else {
                    toAppendBuilder.append(attackerOutputName);
                    toAppendBuilder.append(" healed ").append(lostHP).append("% of it's health!");
                }

                toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));

                final TextView heal = new TextView(battleFragment.getActivity());
                heal.setText(lostHP + "%");
                heal.setBackgroundResource(R.drawable.editable_frame_light_green);
                heal.setPadding(2, 2, 2, 2);
                heal.setAlpha(0f);

                toast.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (view == null) {
                            return;
                        }
                        ((TextView) view.findViewById(battleFragment.getHpId(messageDetails))).setText(Integer.toString(intAmount));

                        ImageView imageView = (ImageView) view.findViewById(battleFragment.getSpriteId(messageDetails));

                        RelativeLayout relativeLayout = (RelativeLayout) view.findViewById(battleFragment.getPkmLayoutId(messageDetails));
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        layoutParams.addRule(RelativeLayout.ALIGN_TOP, battleFragment.getSpriteId(messageDetails));
                        layoutParams.addRule(RelativeLayout.ALIGN_LEFT, battleFragment.getSpriteId(messageDetails));
                        layoutParams.setMargins((int) (imageView.getWidth() * 0.5f), (int) (imageView.getHeight() * 0.5f), 0, 0);
                        relativeLayout.addView(heal, layoutParams);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (view == null) {
                            return;
                        }

                        RelativeLayout relativeLayout = (RelativeLayout) view.findViewById(battleFragment.getPkmLayoutId(messageDetails));
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
                fadeIn.setDuration(BattleFragment.ANIMATION_SHORT / 4);

                fadeOut = ObjectAnimator.ofFloat(heal, "alpha", 1f, 0f);
                fadeOut.setInterpolator(new AccelerateInterpolator());
                fadeOut.setStartDelay(BattleFragment.ANIMATION_SHORT / 2);
                fadeOut.setDuration(BattleFragment.ANIMATION_SHORT / 4);

                hpBar = (ProgressBar) view.findViewById(battleFragment.getHpBarId(messageDetails));
                hpCountDownBar = ObjectAnimator.ofInt(hpBar, "progress", intAmount);
                hpCountDownBar.setDuration(BattleFragment.ANIMATION_SHORT);
                hpCountDownBar.setInterpolator(new AccelerateDecelerateInterpolator());

                animatorSet = new AnimatorSet();
                animatorSet.play(toast);
                animatorSet.play(hpCountDownBar).with(toast);
                animatorSet.play(fadeIn).with(toast);
                animatorSet.play(fadeOut).after(fadeIn);

                battleFragment.startAnimation(animatorSet);

                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;
            case "-sethp":
                switch (battleFragment.getPrintable(fromEffectId)) {
                    case "painsplit":
                        toAppendBuilder.append("The battlers shared their pain!");
                        toast = battleFragment.makeMinorToast(new SpannableString(toAppendBuilder));

                        toast.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                if (view == null) {
                                    return;
                                }
                                int pkmAHp = processHpFraction(split[1]);
                                int pkmBHp = processHpFraction(split[3]);

                                ((TextView) view.findViewById(battleFragment.getHpId(split[0]))).setText(Integer.toString(pkmAHp));
                                ((TextView) view.findViewById(battleFragment.getHpId(split[2]))).setText(Integer.toString(pkmBHp));

                                ProgressBar pkmAHpBar = (ProgressBar) view.findViewById(battleFragment.getHpBarId(split[0]));
                                ObjectAnimator pkmACountDown = ObjectAnimator.ofInt(pkmAHpBar, "progress", pkmAHp);
                                pkmACountDown.setDuration(BattleFragment.ANIMATION_SHORT);
                                pkmACountDown.setInterpolator(new AccelerateDecelerateInterpolator());
                                ProgressBar pkmBHpBar = (ProgressBar) view.findViewById(battleFragment.getHpBarId(split[2]));
                                ObjectAnimator pkmBCountDown = ObjectAnimator.ofInt(pkmBHpBar, "progress", pkmBHp);
                                pkmBCountDown.setDuration(BattleFragment.ANIMATION_SHORT);
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
                        battleFragment.startAnimation(toast);
                        break;
                }
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;
            case "-boost":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
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
                        attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append("The ").append(battleFragment.getPrintable(fromEffect)).append(statAmount).append(" raised ").append(attackerOutputName).append("'s ").append(stat).append("!");
                    } else {
                        toAppendBuilder.append(attackerOutputName).append("'s ").append(battleFragment.getPrintable(fromEffect)).append(statAmount).append(" raised its ").append(stat).append("!");
                    }
                } else {
                    toAppendBuilder.append(attackerOutputName).append("'s ").append(stat).append(statAmount).append(" rose!");
                }

                toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));

                toast.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        battleFragment.processBoost(messageDetails, increasedStat, intAmount);
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
                battleFragment.startAnimation(toast);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;
            case "-unboost":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
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
                        attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append("The ").append(battleFragment.getPrintable(fromEffect)).append(statAmount).append(" lowered ").append(attackerOutputName).append("'s ").append(stat).append("!");
                    } else {
                        toAppendBuilder.append(attackerOutputName).append("'s ").append(battleFragment.getPrintable(fromEffect)).append(statAmount).append(" lowered its ").append(stat).append("!");
                    }
                } else {
                    toAppendBuilder.append(attackerOutputName).append("'s ").append(stat).append(statAmount).append(" fell!");
                }

                toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                toast.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        battleFragment.processBoost(messageDetails, increasedStat, intAmount);
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
                battleFragment.startAnimation(toast);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;
            case "-setboost":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                if (fromEffect != null) {
                    switch (battleFragment.getPrintable(fromEffectId)) {
                        case "bellydrum":
                            toAppendBuilder.append(attackerOutputName).append(" cut its own HP and maximized its Attack!");
                            toast = battleFragment.makeMinorToast(new SpannableString(toAppendBuilder));
                            toast.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    battleFragment.processBoost(split[0], "atk", 6);
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
                            battleFragment.startAnimation(toast);
                            break;

                        case "angerpoint":
                            toAppendBuilder.append(attackerOutputName).append(" maxed its Attack!");
                            toast = battleFragment.makeMinorToast(new SpannableString(toAppendBuilder));
                            toast.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    battleFragment.processBoost(split[0], "atk", 6);
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
                            battleFragment.startAnimation(toast);
                            break;
                    }
                }
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;
            case "-swapboost":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                if (fromEffect != null) {
                    switch (battleFragment.getPrintable(fromEffectId)) {
                        case "guardswap":
                            toAppendBuilder.append(attackerOutputName).append(" switched all changes to its Defense and Sp. Def with the target!");
                            toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                            toast.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    battleFragment.swapBoost(split[0], split[1], "def", "spd");
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
                            battleFragment.startAnimation(toast);
                            break;

                        case "heartswap":
                            toAppendBuilder.append(attackerOutputName).append(" switched stat changes with the target!");
                            toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                            toast.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    battleFragment.swapBoost(split[0], split[1], BattleFragment.STATS);
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
                            battleFragment.startAnimation(toast);
                            break;

                        case "powerswap":
                            toAppendBuilder.append(attackerOutputName).append(" switched all changes to its Attack and Sp. Atk with the target!");
                            toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                            toast.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    battleFragment.swapBoost(split[0], split[1], "atk", "spa");
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
                            battleFragment.startAnimation(toast);
                            break;
                    }
                    logMessage = new SpannableStringBuilder(toAppendBuilder);
                }
                break;
            case "-copyboost":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                defenderOutputName = battleFragment.getPrintableOutputPokemonSide(split[1], false);
                toAppendBuilder.append(attackerOutputName).append(" copied ").append(defenderOutputName).append("'s stat changes!");
                toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                toast.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        battleFragment.copyBoost(split[0], split[1]);
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
                battleFragment.startAnimation(toast);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;
            case "-clearboost":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                toAppendBuilder.append(attackerOutputName).append("'s stat changes were removed!");
                toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                toast.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (view == null) {
                            return;
                        }
                        LinearLayout linearLayout = (LinearLayout) view.findViewById(battleFragment.getTempStatusId(split[0]));
                        for (String stat : BattleFragment.STATS) {
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
                battleFragment.startAnimation(toast);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;
            case "-invertboost":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                toAppendBuilder.append(attackerOutputName).append("'s stat changes were inverted!");
                toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                toast.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        battleFragment.invertBoost(split[0], BattleFragment.STATS);
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
                battleFragment.startAnimation(toast);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;
            case "-clearallboost":
                toAppendBuilder.append("All stat changes were eliminated!");
                toast = battleFragment.makeMinorToast(new SpannableStringBuilder(toAppendBuilder));
                toast.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (view == null) {
                            return;
                        }
                        String[] layouts = {"p1a", "p1b", "p1c", "p2a", "p2b", "p2c"};
                        for (String layout : layouts) {
                            LinearLayout linearLayout = (LinearLayout) view.findViewById(battleFragment.getTempStatusId(layout));
                            for (String stat : BattleFragment.STATS) {
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
                battleFragment.startAnimation(toast);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;
            case "-crit":
                toAppendSpannable = new SpannableString("It's a critical hit!");
                toast = battleFragment.makeMinorToast(toAppendSpannable);
                animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString("Critical!"));
                battleFragment.startAnimation(animatorSet);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;
            case "-supereffective":
                toAppendSpannable = new SpannableString("It's super effective!");
                toast = battleFragment.makeMinorToast(toAppendSpannable);
                animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString("Booya!"));
                battleFragment.startAnimation(animatorSet);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;
            case "-resisted":
                toAppendSpannable = new SpannableString("It's not very effective...");
                toast = battleFragment.makeMinorToast(toAppendSpannable);
                animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString("Resisted!"));
                battleFragment.startAnimation(animatorSet);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;
            case "-immune":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                toAppendBuilder.append("It doesn't affect ");
                toAppendBuilder.append(attackerOutputName);
                toAppendBuilder.append(".");
                toAppendSpannable = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(toAppendSpannable);
                animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString("Immuned!"));
                battleFragment.startAnimation(animatorSet);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;
            case "-miss":
                if (split.length > 1) {
                    // there was a target
                    defenderOutputName = battleFragment.getPrintableOutputPokemonSide(split[1]);
                    toAppendBuilder.append(defenderOutputName).append(" avoided the attack!");
                } else {
                    attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                    toAppendBuilder.append(attackerOutputName).append("'s attack missed!");
                }
                toAppendSpannable = new SpannableStringBuilder(toAppendBuilder);
                toast = battleFragment.makeMinorToast(toAppendSpannable);
                animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString("Missed!"));
                battleFragment.startAnimation(animatorSet);
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                break;

            case "-fail":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
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
                            if (fromEffect != null && battleFragment.getPrintable(fromEffectId).equals("uproar")) {
                                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
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
                            toAppendBuilder.append(attackerOutputName).append("'s BattleFragment.stats were not lowered!");
                            break;

                        default:
                            toAppendBuilder.append("But it failed!");
                            break;
                    }
                } else {
                    toAppendBuilder.append("But it failed!");
                }
                toast = battleFragment.makeMinorToast(new SpannableString(toAppendBuilder));
                animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString("But it failed!"));
                battleFragment.startAnimation(animatorSet);

                logMessage = new SpannableString(toAppendBuilder);
                break;

            case "-notarget":
                logMessage = new SpannableString("But there was no target...");
                toast = battleFragment.makeMinorToast(logMessage);
                battleFragment.startAnimation(toast);
                break;

            case "-ohko":
                logMessage = new SpannableString("It's a one-hit KO!");
                toast = battleFragment.makeMinorToast(logMessage);
                battleFragment.startAnimation(toast);
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
                toast = battleFragment.makeMinorToast(logMessage);
                battleFragment.startAnimation(toast);
                break;

            case "-nothing":
                logMessage = new SpannableString("But nothing happened! ");
                toast = battleFragment.makeMinorToast(logMessage);
                battleFragment.startAnimation(toast);
                break;

            case "-waiting":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                defenderOutputName = battleFragment.getPrintableOutputPokemonSide(split[1], false);
                toAppendBuilder.append(attackerOutputName).append(" is waiting for ").append(defenderOutputName).append("'s move...");
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                battleFragment.startAnimation(toast);
                break;

            case "-combine":
                logMessage = new SpannableString("The two moves are joined! It's a combined move!");
                toast = battleFragment.makeMinorToast(logMessage);
                battleFragment.startAnimation(toast);
                break;

            case "-prepare":
                // todo
                logMessage = new SpannableString(command + ":" + messageDetails);
                break;

            case "-status":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                toAppendBuilder.append(attackerOutputName);
                remaining = split[1];
                switch (remaining) {
                    case "brn":
                        toAppendBuilder.append(" was burned");
                        if (fromEffect != null) {
                            toAppendBuilder.append(" by the ").append(battleFragment.getPrintable(fromEffect));
                        }
                        toAppendBuilder.append("!");
                        break;

                    case "tox":
                        toAppendBuilder.append(" was badly poisoned");
                        if (fromEffect != null) {
                            toAppendBuilder.append(" by the ").append(battleFragment.getPrintable(fromEffect));
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
                toast = battleFragment.makeMinorToast(logMessage);
                final String status;
                status = remaining;
                toast.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        battleFragment.setAddonStatus(split[0], status);
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
                battleFragment.startAnimation(toast);
                break;

            case "-curestatus":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                flag = false;
                if (fromEffectId != null) {
                    fromEffectId = battleFragment.getPrintable(fromEffectId);
                    switch (battleFragment.getPrintable(fromEffectId)) {
                        case "psychoshift":
                            defenderOutputName = battleFragment.getPrintableOutputPokemonSide(ofSource, false);
                            toAppendBuilder.append(attackerOutputName).append(" moved its status onto ").append(defenderOutputName);
                            flag = true;
                            break;
                    }
                    if (fromEffectId.contains("ability:")) {
                        toAppendBuilder.append(attackerOutputName).append("'s ").append(battleFragment.getPrintable(fromEffect)).append(" heals its status!");
                        flag = true;
                    }
                }

                if (!flag) {
                    //split1 is cured status
                    switch (split[1]) {
                        case "brn":
                            if (fromEffectId != null && fromEffectId.contains("item:")) {
                                toAppendBuilder.append(attackerOutputName).append("'s ").append(battleFragment.getPrintable(fromEffect)).append(" healed its burn!");
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
                                toAppendBuilder.append(attackerOutputName).append("'s ").append(battleFragment.getPrintable(fromEffect)).append(" cured its poison!");
                                break;
                            }
                            toAppendBuilder.append(attackerOutputName).append(" was cured of its poisoning.");
                            break;

                        case "slp":
                            if (fromEffectId != null && fromEffectId.contains("item:")) {
                                toAppendBuilder.append(attackerOutputName).append("'s ").append(battleFragment.getPrintable(fromEffect)).append(" woke it up!");
                                break;
                            }
                            toAppendBuilder.append(attackerOutputName).append(" woke up!");
                            break;

                        case "par":
                            if (fromEffectId != null && fromEffectId.contains("item:")) {
                                toAppendBuilder.append(attackerOutputName).append("'s ").append(battleFragment.getPrintable(fromEffect)).append(" cured its paralysis!");
                                break;
                            }
                            toAppendBuilder.append(attackerOutputName).append(" was cured of paralysis.");

                            break;

                        case "frz":
                            if (fromEffectId != null && fromEffectId.contains("item:")) {
                                toAppendBuilder.append(attackerOutputName).append("'s ").append(battleFragment.getPrintable(fromEffect)).append(" defrosted it!");
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
                toast = battleFragment.makeMinorToast(logMessage);
                if (!flag) {
                    toast.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            battleFragment.removeAddonStatus(split[0], split[1]);
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
                battleFragment.startAnimation(toast);
                break;

            case "-cureteam":
                if (fromEffectId != null) {
                    switch (battleFragment.getPrintable(fromEffectId)) {
                        case "aromatherapy":
                            toAppendBuilder.append("A soothing aroma wafted through the area!");
                            break;

                        case "healbell":
                            toAppendBuilder.append("A bell chimed!");
                            break;
                    }
                } else {
                    attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                    toAppendBuilder.append(attackerOutputName);
                    toAppendBuilder.append(" 's team was cured");
                }
                logMessage = new SpannableStringBuilder(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                toast.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        String[] teammate;
                        if (split[0].startsWith("p1")) {
                            teammate = battleFragment.TEAMMATES[0];
                        } else {
                            teammate = battleFragment.TEAMMATES[1];
                        }
                        for (String mate : teammate) {
                            for (String stt : battleFragment.STTUS) {
                                battleFragment.removeAddonStatus(mate, stt);
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
                battleFragment.startAnimation(toast);
                break;

            case "-item":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                final String item;
                item = battleFragment.getPrintable(split[1]);
                if (fromEffect != null) {
                    // not to deal with item: or ability: or move:
                    switch (battleFragment.getPrintable(fromEffectId)) {
                        case "recycle":
                        case "pickup":
                            toAppendBuilder.append(attackerOutputName).append(" found one ").append(item).append("!");
                            break;

                        case "frisk":
                            toAppendBuilder.append(attackerOutputName).append(" frisked its target and found one ").append(item).append("!");
                            break;

                        case "thief":
                        case "covet":
                            defenderOutputName = battleFragment.getPrintableOutputPokemonSide(ofSource, false);
                            toAppendBuilder.append(attackerOutputName).append("  stole  ").append(defenderOutputName).append("'s ").append(item).append("!");
                            break;

                        case "harvest":
                            toAppendBuilder.append(attackerOutputName).append(" harvested one ").append(item).append("!");
                            break;

                        case "bestow":
                            defenderOutputName = battleFragment.getPrintableOutputPokemonSide(ofSource, false);
                            toAppendBuilder.append(attackerOutputName).append(" received ").append(item).append(" from ").append(defenderOutputName).append("!");
                            break;

                        default:
                            toAppendBuilder.append(attackerOutputName).append(" obtained one ").append(item).append(".");
                            break;
                    }
                    logMessage = new SpannableString(toAppendBuilder);
                    toast = battleFragment.makeMinorToast(logMessage);
                    animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString(item));
                    battleFragment.startAnimation(animatorSet);
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
                    toast = battleFragment.makeMinorToast(logMessage);
                    toast.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            battleFragment.setAddonStatus(split[0], item);
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
                    battleFragment.startAnimation(toast);
                }
                break;

            case "-enditem":
                eat = messageDetails.contains("[eat]");
                weaken = messageDetails.contains("[weaken]");
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                item = split[1].trim();

                if (eat) {
                    toAppendBuilder.append(attackerOutputName).append(" ate its ").append(item).append("!");
                } else if (weaken) {
                    toAppendBuilder.append(attackerOutputName).append(" weakened the damage to ").append(item).append("!");
                } else if (fromEffect != null) {
                    switch (battleFragment.getPrintable(fromEffectId)) {
                        case "fling":
                            toAppendBuilder.append(attackerOutputName).append(" flung its ").append(item).append("!");
                            break;

                        case "knockoff":
                            defenderOutputName = battleFragment.getPrintableOutputPokemonSide(ofSource);
                            attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);

                            toAppendBuilder.append(defenderOutputName).append(" knocked off ").append(attackerOutputName).append("'s ").append(item).append("!");
                            break;

                        case "stealeat":
                            defenderOutputName = battleFragment.getPrintableOutputPokemonSide(ofSource);
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
                    String itemId = MyApplication.toId(item);
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
                            defenderOutputName = battleFragment.getPrintableOutputPokemonSide(ofSource, false);
                            toAppendBuilder.append(attackerOutputName).append(" held up its Red Card against ").append(defenderOutputName).append("!");
                            break;

                        default:
                            toAppendBuilder.append(attackerOutputName).append("'s ").append(item).append(" activated!");
                            break;
                    }
                }

                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString(item));
                animatorSet.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        battleFragment.removeAddonStatus(split[0], item);
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
                battleFragment.startAnimation(animatorSet);
                break;

            case "-ability":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                ability = split[1];

                if (fromEffect != null) {
                    switch (battleFragment.getPrintable(fromEffectId)) {
                        case "trace":
                            defenderOutputName = battleFragment.getPrintableOutputPokemonSide(ofSource, false);
                            toAppendBuilder.append(attackerOutputName).append(" traced ").append(defenderOutputName).append("'s ").append(battleFragment.getPrintable(ability)).append("!");
                            break;

                        case "roleplay":
                            defenderOutputName = battleFragment.getPrintableOutputPokemonSide(ofSource, false);
                            toAppendBuilder.append(attackerOutputName).append(" copied ").append(defenderOutputName).append("'s ").append(battleFragment.getPrintable(ability)).append("!");
                            break;

                        case "mummy":
                            toAppendBuilder.append(attackerOutputName).append("'s Ability became Mummy!");
                            break;
                    }
                } else {
                    switch (MyApplication.toId(ability)) {
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
                            toAppendBuilder.append(attackerOutputName).append(" intimidates ").append(battleFragment.getPrintable(ofSource)).append("!");
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
                            toAppendBuilder.append(attackerOutputName).append(" has ").append(battleFragment.getPrintable(ability)).append("!");
                            break;
                    }
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeToast(logMessage);
                battleFragment.startAnimation(toast);
                break;

            case "-endability":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                ability = split[1];

                if (fromEffect != null) {
                    switch (battleFragment.getPrintable(fromEffectId)) {
                        case "mummy":
                            attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                            toAppendBuilder.append("(").append(attackerOutputName).append("'s Ability was previously ").append(battleFragment.getPrintable(ability)).append(")");
                            break;

                        default:
                            toAppendBuilder.append(attackerOutputName).append("\\'s Ability was suppressed!");
                            break;
                    }
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeToast(logMessage);
                battleFragment.startAnimation(toast);
                break;

            case "-transform":
                attacker = battleFragment.getPrintableOutputPokemonSide(split[0]);
                defender = battleFragment.getPrintable(split[1]);
                toAppend = attacker + " transformed into " + defender + "!";
                logMessage = new SpannableString(toAppend);
                toast = battleFragment.makeMinorToast(logMessage);
                toast.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (view == null) {
                            return;
                        }
                        ImageView orgn = (ImageView) view.findViewById(battleFragment.getSpriteId(split[0]));
                        ImageView dest = (ImageView) view.findViewById(battleFragment.getSpriteId(split[1]));
                        orgn.setImageDrawable(dest.getDrawable());
                        battleFragment.copyBoost(split[1], split[0]);
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
                battleFragment.startAnimation(toast);
                break;

            case "-formechange":
                // nothing here
                logMessage = new SpannableString("");
                break;

            case "-start":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                animatorSet = new AnimatorSet();
                final String newEffect;
                newEffect = battleFragment.getPrintable(split[1]);
                switch (battleFragment.getPrintable(MyApplication.toId(split[1]))) {
                    case "typechange":
                        if (fromEffect != null) {
                            if (battleFragment.getPrintable(fromEffectId).equals("reflecttype")) {
                                toAppendBuilder.append(attackerOutputName).append("'s type changed to match ").append(battleFragment.getPrintable(ofSource)).append("'s!");
                            } else {
                                toAppendBuilder.append(attackerOutputName).append("'s ").append(battleFragment.getPrintable(fromEffect)).append(" made it the ").append(battleFragment.getPrintable(split[2])).append(" type!");
                            }
                        } else {
                            toAppendBuilder.append(attackerOutputName).append(" transformed into the ").append(battleFragment.getPrintable(split[2])).append(" type!");
                        }
                        break;

                    case "typeadd":
                        attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append(battleFragment.getPrintable(split[2])).append(" type was added to ").append(attackerOutputName).append(" type!");
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
                                    battleFragment.setAddonStatus(split[0], newEffect);
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
                                battleFragment.setAddonStatus(split[0], newEffect);
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
                                battleFragment.setAddonStatus(split[0], newEffect);
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
                        attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append("The power of ").append(attackerOutputName).append("'s Fire-type moves rose!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
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
                                battleFragment.setAddonStatus(split[0], newEffect);
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
                                battleFragment.setAddonStatus(split[0], newEffect);
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
                        toAppendBuilder.append(attackerOutputName).append("'s").append(battleFragment.getPrintable(split[2])).append(" was disabled!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
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
                                battleFragment.setAddonStatus(split[0], newEffect);
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
                                battleFragment.setAddonStatus(split[0], newEffect);
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
                                battleFragment.setAddonStatus(split[0], newEffect);
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
                                battleFragment.setAddonStatus(split[0], newEffect);
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
                            toAppendBuilder.append(attackerOutputName).append(" fell in love from the ").append(battleFragment.getPrintable(fromEffect)).append("!");
                        } else {
                            toAppendBuilder.append(attackerOutputName).append(" fell in love!");
                        }
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
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
                                battleFragment.setAddonStatus(split[0], newEffect);
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
                        attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append(battleFragment.getPrintableOutputPokemonSide(ofSource)).append(" cut its own HP and laid a curse on ").append(attackerOutputName).append("!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                battleFragment.setAddonStatus(split[0], newEffect);
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
                                battleFragment.setAddonStatus(split[0], newEffect);
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
                                battleFragment.setAddonStatus(split[0], newEffect);
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
                                battleFragment.setAddonStatus(split[0], newEffect);
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
                            attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
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
                                    if (view == null) {
                                        return;
                                    }
                                    ImageView imageView = (ImageView) view.findViewById(battleFragment.getSpriteId(split[0]));
                                    imageView.setAlpha(0.2f);
                                    ImageView substitute = new ImageView(battleFragment.getActivity());
                                    substitute.setImageResource(battleFragment.getSubstitute(split[0]));
                                    substitute.setTag("Substitute");

                                    RelativeLayout relativeLayout = (RelativeLayout) view.findViewById(battleFragment.getPkmLayoutId(split[0]));
                                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                                    layoutParams.addRule(RelativeLayout.ALIGN_TOP, battleFragment.getSpriteId(split[0]));
                                    layoutParams.addRule(RelativeLayout.ALIGN_LEFT, battleFragment.getSpriteId(split[0]));
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
                                battleFragment.setAddonStatus(split[0], newEffect);
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
                                battleFragment.setAddonStatus(split[0], newEffect);
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
                        toAppendBuilder.append(attackerOutputName).append(" learned ").append(battleFragment.getPrintable(split[2])).append("!");
                        break;

                    case "followme":
                    case "ragepowder":
                        toAppendBuilder.append(attackerOutputName).append(" became the center of attention!");
                        break;

                    case "powder":
                        toAppendBuilder.append(attackerOutputName).append(" is covered in powder!");
                        break;

                    default:
                        toAppendBuilder.append(attackerOutputName).append("'s ").append(battleFragment.getPrintable(split[1])).append(" started!");
                        break;
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet.play(toast);
                battleFragment.startAnimation(animatorSet);
                break;

            case "-end":
                attacker = split[0];
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                animatorSet = new AnimatorSet();
                newEffect = battleFragment.getPrintable(split[1]);
                animatorSet.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        battleFragment.removeAddonStatus(split[0], newEffect);
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
                switch (battleFragment.getPrintable(MyApplication.toId(split[1]))) {
                    case "powertrick":
                        toAppendBuilder.append(attackerOutputName).append(" switched its Attack and Defense!");
                        break;

                    case "telekinesis":
                        toAppendBuilder.append(attackerOutputName).append(" was freed from the telekinesis!");
                        break;

                    case "confusion":
                        if (fromEffect.contains("item:")) {
                            toAppendBuilder.append(attackerOutputName).append("'s ").append(battleFragment.getPrintable(fromEffect)).append(" snapped out of its confusion!");
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
                                if (view == null) {
                                    return;
                                }
                                RelativeLayout relativeLayout = (RelativeLayout) view.findViewById(battleFragment.getPkmLayoutId(split[0]));
                                View v = relativeLayout.findViewWithTag("Substitute");
                                if (v != null) {
                                    relativeLayout.removeView(v);
                                }
                                ImageView imageView = (ImageView) view.findViewById(battleFragment.getSpriteId(split[0]));
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
                            toAppendBuilder.append(attackerOutputName).append(" took the ").append(battleFragment.getPrintable(split[1])).append(" attack!");
                        } else {
                            toAppendBuilder.append(attackerOutputName).append("'s ").append(battleFragment.getPrintable(split[1])).append(" ended!");
                        }
                        break;
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet.play(toast);
                battleFragment.startAnimation(animatorSet);
                break;

            case "-singleturn":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                switch (battleFragment.getPrintable(MyApplication.toId(split[1]))) {
                    case "roost":
                        toAppendBuilder.append(attackerOutputName).append(" landed on the ground!");
                        break;

                    case "quickguard":
                        attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append("Quick Guard protected ").append(attackerOutputName).append(" landed on the ground!");
                        break;

                    case "wideguard":
                        attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append("Wide Guard protected ").append(attackerOutputName).append(" landed on the ground!");
                        break;

                    case "protect":
                        toAppendBuilder.append(attackerOutputName).append(" protected itself!");
                        break;

                    case "endure":
                        toAppendBuilder.append(attackerOutputName).append(" braced itself!");
                        break;

                    case "helpinghand":
                        attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append(battleFragment.getPrintableOutputPokemonSide(ofSource)).append(" is ready to help ").append(attackerOutputName).append("!");
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
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString(battleFragment.getPrintable(split[1])));
                battleFragment.startAnimation(animatorSet);
                break;

            case "-singlemove":
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                switch (battleFragment.getPrintable(MyApplication.toId(split[1]))) {
                    case "grudge":
                        toAppendBuilder.append(attackerOutputName).append(" wants its target to bear a grudge!");
                        break;
                    case "destinybond":
                        toAppendBuilder.append(attackerOutputName).append(" is trying to take its foe down with it!");
                        break;
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString(battleFragment.getPrintable(split[1])));
                battleFragment.startAnimation(animatorSet);
                break;

            case "-activate":
                attacker = split[0];
                attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0]);
                switch (battleFragment.getPrintable(MyApplication.toId(split[1]))) {
                    case "confusion":
                        toAppendBuilder.append(attackerOutputName).append(" is confused!");
                        break;

                    case "destinybond":
                        toAppendBuilder.append(attackerOutputName).append(" took its attacker down with it!");
                        break;

                    case "snatch":
                        toAppendBuilder.append(attackerOutputName).append(" snatched ").append(battleFragment.getPrintable(ofSource)).append("'s move!");
                        break;

                    case "grudge":
                        toAppendBuilder.append(attackerOutputName).append("'s").append(battleFragment.getPrintable(split[2])).append(" lost all its PP due to the grudge!");
                        break;

                    case "quickguard":
                        attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append("Quick Guard protected ").append(attackerOutputName).append("!");
                        break;

                    case "wideguard":
                        attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append("Wide Guard protected ").append(attackerOutputName).append("!");
                        break;

                    case "protect":
                        toAppendBuilder.append(attackerOutputName).append(" protected itself!");
                        break;

                    case "substitute":
                        if (messageDetails.contains("[damage]")) {
                            attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                            toAppendBuilder.append("The substitute took damage for ").append(attackerOutputName).append(" protected itself!");
                        } else if (messageDetails.contains("[block]")) {
                            toAppendBuilder.append(attackerOutputName).append("'s Substitute blocked").append(battleFragment.getPrintable(split[2])).append("!");
                        }
                        break;

                    case "attract":
                        toAppendBuilder.append(attackerOutputName).append(" is in love with ").append(battleFragment.getPrintable(ofSource)).append("!");
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
                            side = battleFragment.getPlayer2();
                        } else {
                            side = battleFragment.getPlayer1();
                        }
                        toAppendBuilder.append("Congratulations, ").append(side).append("!");
                        break;

                    case "trick":
                    case "switcheroo":
                        toAppendBuilder.append(attackerOutputName).append(" switched items with its target!");
                        break;

                    case "brickbreak":
                        if (MyApplication.toId(ofSource).startsWith("p2")) {
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
                        toAppendBuilder.append("It reduced the PP of ").append(attackerOutputName).append("'s ").append(battleFragment.getPrintable(split[2])).append(" by ").append(battleFragment.getPrintable(split[3])).append("!");
                        break;

                    case "gravity":
                        toAppendBuilder.append(attackerOutputName).append(" couldn't stay airborne because of gravity!");
                        break;

                    case "magnitude":
                        toAppendBuilder.append("Magnitude ").append(battleFragment.getPrintable(split[2])).append("!");
                        break;

                    case "sketch":
                        toAppendBuilder.append(attackerOutputName).append(" sketched ").append(battleFragment.getPrintable(split[2])).append("!");
                        break;

                    case "skillswap":
                        toAppendBuilder.append(attackerOutputName).append(" swapped Abilities with its target!");
                        if (ofSource != null) {
                            toAppendBuilder.append("\n").append(attackerOutputName).append(" acquired ").append(battleFragment.getPrintable(split[2])).append("!");
                            toAppendBuilder.append("\n").append(battleFragment.getPrintable(ofSource)).append(" acquired ").append(battleFragment.getPrintable(split[3])).append("!");
                        }
                        break;

                    case "charge":
                        toAppendBuilder.append(attackerOutputName).append(" began charging power!");
                        break;

                    case "struggle":
                        toAppendBuilder.append(attackerOutputName).append(" has no moves left!");
                        break;

                    case "bind":
                        toAppendBuilder.append(attackerOutputName).append(" was squeezed by ").append(battleFragment.getPrintable(ofSource)).append("!");
                        break;

                    case "wrap":
                        toAppendBuilder.append(attackerOutputName).append(" was wrapped by ").append(battleFragment.getPrintable(ofSource)).append("!");
                        break;

                    case "clamp":
                        toAppendBuilder.append(battleFragment.getPrintable(ofSource)).append(" clamped ").append(attackerOutputName).append("!");
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
                        toAppendBuilder.append(attackerOutputName).append(" has been afflicted with an infestation by ").append(battleFragment.getPrintable(ofSource)).append("!");
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
                        toAppendBuilder.append(battleFragment.getPrintable(split[2])).append(" was blocked by the kicked-up mat!");
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
                        toAppendBuilder.append(attackerOutputName).append("'s Forewarn alerted it to ").append(battleFragment.getPrintable(split[2])).append("!");
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
                        attackerOutputName = battleFragment.getPrintableOutputPokemonSide(split[0], false);
                        toAppendBuilder.append(battleFragment.getPrintable(ofSource)).append(" shared its ").append(battleFragment.getPrintable(split[2])).append(" with ").append(attackerOutputName);
                        break;

                    //items
                    case "custapberry":
                    case "quickclaw":
                        toAppendBuilder.append(attackerOutputName).append("'s ").append(battleFragment.getPrintable(split[1])).append(" let it move first!");
                        break;

                    case "leppaberry":
                        toAppendBuilder.append(attackerOutputName).append(" restored ").append(battleFragment.getPrintable(split[2])).append("'s PP using its Leppa Berry!");
                        break;

                    default:
                        toAppendBuilder.append(attackerOutputName).append("'s ").append(" activated!");
                        break;
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet = battleFragment.createFlyingMessage(split[0], toast, new SpannableString(battleFragment.getPrintable(split[1])));
                battleFragment.startAnimation(animatorSet);
                break;

            case "-sidestart":
                if (messageDetails.startsWith("p2")) {
                    side = "the opposing team";
                } else {
                    side = "your team";
                }

                fromEffect = split[1];
                fromEffectId = battleFragment.getPrintable(MyApplication.toId(fromEffect));
                animatorSet = new AnimatorSet();
                switch (fromEffectId) {
                    case "stealthrock":
                        toAppendBuilder.append("Pointed stones float in the air around ").append(side).append("!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                if (view == null) {
                                    return;
                                }
                                int id = (messageDetails.startsWith("p1")) ? R.id.field_rocks : R.id.field_rocks_o;
                                view.findViewById(id).setVisibility(View.VISIBLE);
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
                                if (view == null) {
                                    return;
                                }
                                view.findViewById(battleFragment.getLastVisibleSpike(messageDetails, true)).setVisibility(View.VISIBLE);
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
                                if (view == null) {
                                    return;
                                }
                                view.findViewById(battleFragment.getLastVisibleTSpike(messageDetails, true)).setVisibility(View.VISIBLE);
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
                                if (view == null) {
                                    return;
                                }
                                int id = (messageDetails.startsWith("p1")) ? R.id.field_reflect : R.id.field_reflect_o;
                                view.findViewById(id).setVisibility(View.VISIBLE);
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
                                if (view == null) {
                                    return;
                                }
                                int id = (messageDetails.startsWith("p1")) ? R.id.field_lightscreen : R.id.field_lightscreen_o;
                                view.findViewById(id).setVisibility(View.VISIBLE);
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
                        toAppendBuilder.append(battleFragment.getPrintable(fromEffect)).append(" started!");
                        break;
                }

                logMessage = new SpannableStringBuilder(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet.play(toast);
                battleFragment.startAnimation(animatorSet);
                break;

            case "-sideend":
                if (messageDetails.startsWith("p2")) {
                    side = "the opposing team";
                } else {
                    side = "your team";
                }

                fromEffect = split[1];
                fromEffectId = battleFragment.getPrintable(MyApplication.toId(fromEffect));

                animatorSet = new AnimatorSet();
                switch (fromEffectId) {
                    case "stealthrock":
                        toAppendBuilder.append("The pointed stones disappeared from around ").append(side).append("!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                if (view == null) {
                                    return;
                                }
                                int id = (messageDetails.startsWith("p1")) ? R.id.field_rocks : R.id.field_rocks_o;
                                view.findViewById(id).setVisibility(View.INVISIBLE);
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
                                if (view == null) {
                                    return;
                                }
                                view.findViewById(battleFragment.getLastVisibleSpike(messageDetails, false)).setVisibility(View.INVISIBLE);
                                view.findViewById(battleFragment.getLastVisibleSpike(messageDetails, false)).setVisibility(View.INVISIBLE);
                                view.findViewById(battleFragment.getLastVisibleSpike(messageDetails, false)).setVisibility(View.INVISIBLE);
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
                                if (view == null) {
                                    return;
                                }
                                view.findViewById(battleFragment.getLastVisibleTSpike(messageDetails, false)).setVisibility(View.INVISIBLE);
                                view.findViewById(battleFragment.getLastVisibleTSpike(messageDetails, false)).setVisibility(View.INVISIBLE);
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
                                if (view == null) {
                                    return;
                                }
                                int id = (messageDetails.startsWith("p1")) ? R.id.field_reflect : R.id.field_reflect_o;
                                view.findViewById(id).setVisibility(View.INVISIBLE);
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
                                if (view == null) {
                                    return;
                                }
                                int id = (messageDetails.startsWith("p1")) ? R.id.field_lightscreen : R.id.field_lightscreen_o;
                                view.findViewById(id).setVisibility(View.INVISIBLE);
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
                        toAppendBuilder.append(battleFragment.getPrintable(fromEffect)).append(" ended!");
                        break;
                }

                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet.play(toast);
                battleFragment.startAnimation(animatorSet);
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
                            battleFragment.setWeatherExist(true);
                            animatorSet.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    if (view == null) {
                                        return;
                                    }
                                    ((ImageView) view.findViewById(R.id.weather_background)).setImageResource(R.drawable.weather_raindance);
                                    ((TextView) view.findViewById(R.id.weather)).setText(weather);
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
                            battleFragment.setWeatherExist(true);
                            animatorSet.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    if (view == null) {
                                        return;
                                    }
                                    ((ImageView) view.findViewById(R.id.weather_background)).setImageResource(R.drawable.weather_sandstorm);
                                    ((TextView) view.findViewById(R.id.weather)).setText(weather);
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
                            battleFragment.setWeatherExist(true);
                            animatorSet.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    if (view == null) {
                                        return;
                                    }
                                    ((ImageView) view.findViewById(R.id.weather_background)).setImageResource(R.drawable.weather_sunnyday);
                                    ((TextView) view.findViewById(R.id.weather)).setText(weather);
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
                            battleFragment.setWeatherExist(true);
                            animatorSet.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    if (view == null) {
                                        return;
                                    }
                                    ((ImageView) view.findViewById(R.id.weather_background)).setImageResource(R.drawable.weather_hail);
                                    ((TextView) view.findViewById(R.id.weather)).setText(weather);
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
                        if (battleFragment.isWeatherExist()) {
                            switch (battleFragment.getCurrentWeather()) {
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
                                    if (view == null) {
                                        return;
                                    }
                                    ((ImageView) view.findViewById(R.id.weather_background)).setImageResource(0);
                                    ((TextView) view.findViewById(R.id.weather)).setText(null);
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
                        battleFragment.setWeatherExist(false);
                        break;
                }
                battleFragment.setCurrentWeather(weather);
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet.play(toast);
                battleFragment.startAnimation(animatorSet);
                break;


            case "-fieldstart":
                attackerOutputName = ofSource;
                animatorSet = new AnimatorSet();
                switch (battleFragment.getPrintable(MyApplication.toId(split[0]))) {
                    case "trickroom":
                        toAppendBuilder.append(attackerOutputName).append(" twisted the dimensions!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                if (view == null) {
                                    return;
                                }
                                ((ImageView) view.findViewById(R.id.battle_background)).setImageResource(R.drawable.weather_trickroom);
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
                        toAppendBuilder.append("It created a bizarre area in which the Defense and Sp. Def BattleFragment.stats are swapped!");
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
                        toAppendBuilder.append(battleFragment.getPrintable(split[1])).append(" started!");
                        break;
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet.play(toast);
                battleFragment.startAnimation(animatorSet);
                break;

            case "-fieldend":
                animatorSet = new AnimatorSet();
                switch (battleFragment.getPrintable(MyApplication.toId(split[0]))) {
                    case "trickroom":
                        toAppendBuilder.append("The twisted dimensions returned to normal!");
                        animatorSet.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                if (view == null) {
                                    return;
                                }
                                int id = new Random().nextInt(BattleFragment.BACKGROUND_LIBRARY.length);
                                ((ImageView) view.findViewById(R.id.battle_background)).setImageResource(BattleFragment.BACKGROUND_LIBRARY[id]);
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
                        toAppendBuilder.append("'Wonder Room wore off, and the Defense and Sp. Def BattleFragment.stats returned to normal!");
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
                        toAppendBuilder.append(battleFragment.getPrintable(split[1])).append(" ended!");
                        break;
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                animatorSet.play(toast);
                battleFragment.startAnimation(animatorSet);
                break;

            case "-fieldactivate":
                switch (battleFragment.getPrintable(MyApplication.toId(split[0]))) {
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
                        toAppendBuilder.append(battleFragment.getPrintable(split[1])).append(" hit!");
                        break;
                }
                logMessage = new SpannableString(toAppendBuilder);
                toast = battleFragment.makeMinorToast(logMessage);
                battleFragment.startAnimation(toast);
                break;

            case "-message":
                logMessage = new SpannableString(messageDetails);
                toast = battleFragment.makeMinorToast(logMessage);
                battleFragment.startAnimation(toast);
                break;

            case "-anim":
                logMessage = new SpannableString(command + ":" + messageDetails);
                toast = battleFragment.makeMinorToast(logMessage);
                battleFragment.startAnimation(toast);
                break;

            default:
                toAppendSpannable = new SpannableString(command + ":" + messageDetails);
                toast = battleFragment.makeMinorToast(toAppendSpannable);
                battleFragment.startAnimation(toast);
                logMessage = new SpannableString(command + ":" + messageDetails);
                break;
        }

        if (messageDetails.contains("[silent]")) {
            return;
        }

        logMessage.setSpan(new RelativeSizeSpan(0.8f), 0, logMessage.toString().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        battleFragment.addToLog(logMessage);
    }

    private static void setDisplayTeam(BattleFragment battleFragment, JSONObject object) throws JSONException {
        object = object.getJSONObject("side");
        JSONArray team = object.getJSONArray("pokemon");
        for (int i = 0; i < team.length(); i++) {
            JSONObject info = team.getJSONObject(i);
            PokemonInfo pkm = parsePokemonInfo(battleFragment, info);
            battleFragment.getPlayer1Team().add(i, pkm);
        }
    }

    private static PokemonInfo parsePokemonInfo(BattleFragment battleFragment, JSONObject info) throws JSONException {
        String details = info.getString("details");
        String name = !details.contains(",") ? details : details.substring(0, details.indexOf(","));
        PokemonInfo pkm = new PokemonInfo(battleFragment.getActivity(), name);
        processPokemonDetailString(pkm, details);
        String nickname = info.getString("ident").substring(4);
        pkm.setNickname(nickname);
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
            if (move.startsWith("hiddenpower")) {
                move = move.toLowerCase().replaceAll("[^a-z]", "");
                //dirty fix to remvoe that 60 from hiddenpower...
            }
            JSONObject ppObject = MoveDex.get(battleFragment.getActivity()).getMoveJsonObject(move);
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

    private static int processHpFraction(String hpFraction) {
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

    private static String processSpecialName(String name) {
        for (String sp : BattleFragment.MORPHS) {
            if (name.contains(sp)) {
                return sp;
            }
        }
        return name;
    }

    private static void processPokemonDetailString(PokemonInfo pkm, String details) {
        String[] split = details.split(" ,");
        String name = split[0];
        pkm.setName(name);
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
    }

}
