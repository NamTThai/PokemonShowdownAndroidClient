package com.pokemonshowdown.data;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.pokemonshowdown.R;
import com.pokemonshowdown.application.MyApplication;
import com.pokemonshowdown.fragment.BattleFragment;

public class BattleAnimation {
    public final static String BTAG = BattleAnimation.class.getName();

    public static AnimatorSet processMove(String move, BattleFragment battleFragment, String[] split) {
        move = MyApplication.toId(move);
        MoveDex.Moves animationId = MoveDex.get(battleFragment.getActivity()).getMoveAnimationEntry(move);
        if (battleFragment.getView() == null) {
            return null;
        }

        RelativeLayout wrapper = (RelativeLayout) battleFragment.getView().findViewById(R.id.animation_layout);
        RelativeLayout atkC = (RelativeLayout) battleFragment.getView().findViewById(battleFragment.getPkmLayoutId(split[0]));
        SimpleDraweeView atk = (SimpleDraweeView) battleFragment.getView().findViewById(battleFragment.getSpriteId(split[0]));
        RelativeLayout defC = (RelativeLayout) battleFragment.getView().findViewById(battleFragment.getPkmLayoutId(split[2]));
        SimpleDraweeView def = (SimpleDraweeView) battleFragment.getView().findViewById(battleFragment.getSpriteId(split[2]));
        try {
            if (animationId == null) {
                return fast(battleFragment, wrapper, atkC, atk, defC, def);
            } else {
                switch (animationId) {
                    case SHAKE:
                        return shake(atkC, atk);
                    case DANCE:
                        return dance(atkC, atk);
                    case FLIGHT:
                        return flight(battleFragment, atkC, atk, defC);
                    case SPINATK:
                        return spinAtk(battleFragment, atkC, atk, defC);
                    case XATK:
                        return xatk(battleFragment, atkC, atk, defC);
                    case SELF:
                        return self(battleFragment, atkC, atk);
                    case SELF_LIGHT:
                        return selfLight(battleFragment, atkC, atk);
                    case SELF_DARK:
                        return selfDark(battleFragment, atkC, atk);
                    case TRICK:
                        return trick(battleFragment, wrapper, atkC, atk, defC, def);
                    case CHARGE:
                        return charge(battleFragment, atkC, atk, defC, def, split);
                    case SPREAD_LIGHT:
                        return spread(battleFragment, wrapper, atkC, atk, R.drawable.battle_electroball);
                    case SPREAD_ENERGY:
                        return spread(battleFragment, wrapper, atkC, atk, R.drawable.battle_energyball);
                    case SPREAD_MIST:
                        return spread(battleFragment, wrapper, atkC, atk, R.drawable.battle_mistball);
                    case SPREAD_SHADOW:
                        return spread(battleFragment, wrapper, atkC, atk, R.drawable.battle_shadowball);
                    case SPREAD_POISON:
                        return spread(battleFragment, wrapper, atkC, atk, R.drawable.battle_poisonwisp);
                    case SPREAD_WAVE:
                        return spread(battleFragment, wrapper, atkC, atk, R.drawable.battle_waterwisp);
                    case SPREAD_FIRE:
                        return spread(battleFragment, wrapper, atkC, atk, R.drawable.battle_fireball);
                    case SPREAD_ROCK:
                        return spread(battleFragment, wrapper, atkC, atk, R.drawable.field_rocks);
                    case SPREAD_SPIKE:
                        return spread(battleFragment, wrapper, atkC, atk, R.drawable.field_spikes);
                    case SPREAD_TSPIKE:
                        return spread(battleFragment, wrapper, atkC, atk, R.drawable.field_tspikes);
                    case SPREAD_WEB:
                        return spread(battleFragment, wrapper, atkC, atk, R.drawable.battle_web);
                    case CONTACT_ENERGY:
                        return contact(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_energyball);
                    case CONTACT_CLAW:
                        return contact(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_leftclaw);
                    case CONTACT_KICK:
                        return contact(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_foot);
                    case CONTACT_WAVE:
                        return contact(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_waterwisp);
                    case CONTACT_BITE:
                        return contact(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_bite);
                    case CONTACT_POISON:
                        return contact(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_poisonwisp);
                    case CONTACT_PUNCH:
                        return contact(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_fist);
                    case CONTACT_SHADOW:
                        return contact(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_shadowball);
                    case CONTACT_THUNDER:
                        return contact(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_lightning);
                    case CONTACT_FIRE:
                        return contact(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_fireball);
                    case CONTACT_NEUTRAL:
                        return contact(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_wisp);
                    case CONTACT_MIST:
                        return contact(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_mistball);
                    case CONTACT_LIGHT:
                        return contact(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_electroball);
                    case DRAIN:
                        return drain(battleFragment, atkC, atk, defC);
                    case FAST:
                        return fast(battleFragment, wrapper, atkC, atk, defC, def);
                    case CONTACT_PUNCH_FIRE:
                        return contactTwice(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_fist, R.drawable.battle_fireball);
                    case CONTACT_PUNCH_ICE:
                        return contactTwice(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_fist, R.drawable.battle_icicle);
                    case CONTACT_PUNCH_THUNDER:
                        return contactTwice(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_fist, R.drawable.battle_lightning);
                    case CONTACT_BITE_FIRE:
                        return contactTwice(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_bite, R.drawable.battle_fireball);
                    case CONTACT_BITE_ICE:
                        return contactTwice(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_bite, R.drawable.battle_icicle);
                    case CONTACT_BITE_THUNDER:
                        return contactTwice(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_bite, R.drawable.battle_lightning);
                    case STREAM_NEUTRAL:
                        return stream(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_wisp);
                    case STREAM_LIGHT:
                        return stream(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_electroball);
                    case STREAM_ENERGY:
                        return stream(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_energyball);
                    case STREAM_MIST:
                        return stream(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_mistball);
                    case STREAM_POISON:
                        return stream(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_poisonwisp);
                    case STREAM_SHADOW:
                        return stream(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_shadowball);
                    case STREAM_WATER:
                        return stream(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_waterwisp);
                    case STREAM_FIRE:
                        return stream(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_fireball);
                    case STREAM_ICE:
                        return stream(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_icicle);
                    case STREAM_ROCK:
                        return stream(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.field_rocks);
                    case EARTH:
                        return earth(defC, def);
                    case PHAZE:
                        return phaze(def);
                    case THUNDER_STRONG:
                        return thunderStrong(battleFragment, defC, def);
                    case THUNDER_NEUTRAL:
                        return thunderNeutral(battleFragment, defC, def);
                    case THUNDER_WEAK:
                        return thunderWeak(battleFragment, atkC, atk, defC, def);
                    case STATUS_PSN:
                        return status(battleFragment, defC, def, R.drawable.battle_poisonwisp);
                    case STATUS_PAR:
                        return status(battleFragment, defC, def, R.drawable.battle_electroball);
                    case STATUS_SLP:
                        return status(battleFragment, defC, def, R.drawable.battle_energyball);
                    case STATUS_BRN:
                        return status(battleFragment, defC, def, R.drawable.battle_fireball);
                    case BALL_NEUTRAL:
                        return ball(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_wisp);
                    case BALL_LIGHT:
                        return ball(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_electroball);
                    case BALL_ENERGY:
                        return ball(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_energyball);
                    case BALL_MIST:
                        return ball(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_mistball);
                    case BALL_POISON:
                        return ball(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_poisonwisp);
                    case BALL_SHADOW:
                        return ball(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_shadowball);
                    case BALL_WATER:
                        return ball(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_waterwisp);
                    case BALL_FIRE:
                        return ball(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_fireball);
                    case BALL_ICE:
                        return ball(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.battle_icicle);
                    case BALL_ROCK:
                        return ball(battleFragment, wrapper, atkC, atk, defC, def, R.drawable.field_rocks);
                    case WISH:
                        return wish(battleFragment, atkC, atk);
                    case SLASH:
                        return slash(battleFragment, defC);
                    case BOMB_NEUTRAL:
                        return bomb(battleFragment, defC, def, R.drawable.battle_wisp);
                    case BOMB_LIGHT:
                        return bomb(battleFragment, defC, def, R.drawable.battle_electroball);
                    case BOMB_ENERGY:
                        return bomb(battleFragment, defC, def, R.drawable.battle_energyball);
                    case BOMB_MIST:
                        return bomb(battleFragment, defC, def, R.drawable.battle_mistball);
                    case BOMB_POISON:
                        return bomb(battleFragment, defC, def, R.drawable.battle_poisonwisp);
                    case BOMB_SHADOW:
                        return bomb(battleFragment, defC, def, R.drawable.battle_shadowball);
                    case BOMB_WATER:
                        return bomb(battleFragment, defC, def, R.drawable.battle_waterwisp);
                    case BOMB_FIRE:
                        return bomb(battleFragment, defC, def, R.drawable.battle_fireball);
                    default:
                        return self(battleFragment, atkC, atk);
                }
            }
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static AnimatorSet fast(BattleFragment battleFragment, final RelativeLayout wrapper, final RelativeLayout atkC, final SimpleDraweeView atk, final RelativeLayout defC, final SimpleDraweeView def) {
        Context context = battleFragment.getActivity();
        final float initialAlpha = atk.getAlpha();
        AnimatorSet animatorSet = new AnimatorSet();
        final SimpleDraweeView fast = new SimpleDraweeView(context);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setControllerListener(getController(context, fast))
                .setUri(Uri.parse(atk.getTag().toString()))
                .setAutoPlayAnimations(true)
                .build();
        fast.setController(controller);
        fast.setX(atkC.getX() + atk.getX());
        fast.setY(atkC.getY() + atk.getY());
        final ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ObjectAnimator disappear = ObjectAnimator.ofFloat(atk, "alpha", 0f);
        disappear.setDuration(BattleFragment.ANIMATION_LONG / 3);
        ObjectAnimator attackX = ObjectAnimator.ofFloat(fast, "x", (defC.getX() + def.getX()));
        attackX.setDuration(BattleFragment.ANIMATION_LONG / 3);
        attackX.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator attackY = ObjectAnimator.ofFloat(fast, "y", (defC.getY() + def.getY()));
        attackY.setDuration(BattleFragment.ANIMATION_LONG / 3);
        attackY.setInterpolator(new AccelerateInterpolator());
        attackX.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationStartWithNet(Animator animation) {
                wrapper.addView(fast, params);
            }

            @Override
            public void onAnimationEndWithNet(Animator animation) {
                wrapper.removeView(fast);
            }
        });
        ObjectAnimator appear = ObjectAnimator.ofFloat(atk, "alpha", initialAlpha);
        appear.setDuration(BattleFragment.ANIMATION_LONG / 3);
        animatorSet.play(disappear).before(attackX);
        animatorSet.play(attackX).with(attackY);
        animatorSet.play(appear).after(attackX);
        return animatorSet;
    }

    public static AnimatorSet shake(RelativeLayout atkC, SimpleDraweeView atk) {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator shakeLeft = ObjectAnimator.ofFloat(atk, "x", 0f);
        shakeLeft.setDuration(BattleFragment.ANIMATION_LONG / 6);
        ObjectAnimator shakeRightAll = ObjectAnimator.ofFloat(atk, "x", atkC.getWidth() - atk.getWidth());
        shakeRightAll.setDuration(BattleFragment.ANIMATION_LONG / 3);
        ObjectAnimator shakeLeftAll = ObjectAnimator.ofFloat(atk, "x", 0f);
        shakeLeftAll.setDuration(BattleFragment.ANIMATION_LONG / 3);
        ObjectAnimator shakeMiddle = ObjectAnimator.ofFloat(atk, "x", atk.getX());
        shakeMiddle.setDuration(BattleFragment.ANIMATION_LONG / 6);
        animatorSet.play(shakeLeft);
        animatorSet.play(shakeRightAll).after(shakeLeft);
        animatorSet.play(shakeLeftAll).after(shakeRightAll);
        animatorSet.play(shakeMiddle).after(shakeLeftAll);
        return animatorSet;
    }

    public static AnimatorSet dance(RelativeLayout atkC, SimpleDraweeView atk) {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator shakeLeft = ObjectAnimator.ofFloat(atk, "x", 0f);
        shakeLeft.setDuration(BattleFragment.ANIMATION_LONG / 4);
        ObjectAnimator shakeRight = ObjectAnimator.ofFloat(atk, "x", atkC.getWidth() - atk.getWidth());
        shakeRight.setDuration(BattleFragment.ANIMATION_LONG / 2);
        ObjectAnimator shakeMiddle = ObjectAnimator.ofFloat(atk, "x", atk.getX());
        shakeMiddle.setDuration(BattleFragment.ANIMATION_LONG / 4);
        animatorSet.play(shakeLeft);
        animatorSet.play(shakeRight).after(shakeLeft);
        animatorSet.play(shakeMiddle).after(shakeRight);
        return animatorSet;
    }

    public static AnimatorSet flight(BattleFragment battleFragment, final RelativeLayout atkC, final SimpleDraweeView atk, final RelativeLayout defC) {
        Context context = battleFragment.getActivity();
        final float initialAlpha = atk.getAlpha();
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator flightLeft = ObjectAnimator.ofFloat(atk, "x", 0f);
        flightLeft.setDuration(BattleFragment.ANIMATION_LONG / 4);
        ObjectAnimator flightTop = ObjectAnimator.ofFloat(atk, "y", 0f);
        flightTop.setDuration(BattleFragment.ANIMATION_LONG / 4);
        ObjectAnimator flightAlpha = ObjectAnimator.ofFloat(atk, "alpha", 0f);
        flightAlpha.setDuration(BattleFragment.ANIMATION_LONG / 4);
        final ImageView leftClaw = new ImageView(context);
        leftClaw.setImageResource(R.drawable.battle_leftclaw);
        final ViewGroup.LayoutParams imageParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ObjectAnimator flightClawRight = ObjectAnimator.ofFloat(leftClaw, "x", defC.getWidth());
        flightClawRight.setDuration(BattleFragment.ANIMATION_LONG / 2);
        ObjectAnimator flightClawBottom = ObjectAnimator.ofFloat(leftClaw, "y", defC.getHeight());
        flightClawBottom.setDuration(BattleFragment.ANIMATION_LONG / 2);
        flightClawRight.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationStartWithNet(Animator animation) {
                defC.addView(leftClaw, imageParams);
                leftClaw.setX(0f);
                leftClaw.setY(0f);
            }

            @Override
            public void onAnimationEndWithNet(Animator animation) {
                defC.removeView(leftClaw);
                atk.setX(atkC.getWidth() - atk.getWidth());
                atk.setY(atkC.getHeight() - atk.getHeight());
            }
        });
        ObjectAnimator flightMiddleLeft = ObjectAnimator.ofFloat(atk, "x", atk.getX());
        flightMiddleLeft.setDuration(BattleFragment.ANIMATION_LONG / 4);
        ObjectAnimator flightMiddleTop = ObjectAnimator.ofFloat(atk, "y", atk.getY());
        flightMiddleTop.setDuration(BattleFragment.ANIMATION_LONG / 4);
        ObjectAnimator flightMiddleAlpha = ObjectAnimator.ofFloat(atk, "alpha", initialAlpha);
        flightMiddleAlpha.setDuration(BattleFragment.ANIMATION_LONG / 4);
        animatorSet.play(flightLeft).with(flightTop);
        animatorSet.play(flightLeft).with(flightAlpha);
        animatorSet.play(flightLeft).before(flightClawRight);
        animatorSet.play(flightClawRight).with(flightClawBottom);
        animatorSet.play(flightMiddleLeft).after(flightClawRight);
        animatorSet.play(flightMiddleLeft).with(flightMiddleTop);
        animatorSet.play(flightMiddleLeft).with(flightMiddleAlpha);
        return animatorSet;
    }

    public static AnimatorSet spinAtk(BattleFragment battleFragment, final RelativeLayout atkC, final SimpleDraweeView atk, final RelativeLayout defC) {
        Context context = battleFragment.getActivity();
        final float initialAlpha = atk.getAlpha();
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator flightLeft = ObjectAnimator.ofFloat(atk, "x", 0f);
        flightLeft.setDuration(BattleFragment.ANIMATION_LONG / 4);
        ObjectAnimator flightTop = ObjectAnimator.ofFloat(atk, "y", 0f);
        flightTop.setDuration(BattleFragment.ANIMATION_LONG / 4);
        ObjectAnimator flightAlpha = ObjectAnimator.ofFloat(atk, "alpha", 0f);
        flightAlpha.setDuration(BattleFragment.ANIMATION_LONG / 4);
        final SimpleDraweeView spin = new SimpleDraweeView(context);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setControllerListener(getController(context, spin))
                .setUri(Uri.parse(atk.getTag().toString()))
                .setAutoPlayAnimations(true)
                .build();
        spin.setController(controller);
        spin.setX(0);
        spin.setY(0);
        final ViewGroup.LayoutParams imageParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ObjectAnimator spinRight = ObjectAnimator.ofFloat(spin, "x", defC.getWidth());
        spinRight.setDuration(BattleFragment.ANIMATION_LONG / 2);
        ObjectAnimator spinBottom = ObjectAnimator.ofFloat(spin, "y", defC.getHeight());
        spinBottom.setDuration(BattleFragment.ANIMATION_LONG / 2);
        ObjectAnimator spinRotation = ObjectAnimator.ofFloat(spin, "rotation", 720);
        spinRotation.setDuration(BattleFragment.ANIMATION_LONG / 2);
        spinRight.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationStartWithNet(Animator animation) {
                defC.addView(spin, imageParams);
            }

            @Override
            public void onAnimationEndWithNet(Animator animation) {
                defC.removeView(spin);
                atk.setX(atkC.getWidth() - atk.getWidth());
                atk.setY(atkC.getHeight() - atk.getHeight());
            }
        });
        ObjectAnimator flightMiddleLeft = ObjectAnimator.ofFloat(atk, "x", atk.getX());
        flightMiddleLeft.setDuration(BattleFragment.ANIMATION_LONG / 4);
        ObjectAnimator flightMiddleTop = ObjectAnimator.ofFloat(atk, "y", atk.getY());
        flightMiddleTop.setDuration(BattleFragment.ANIMATION_LONG / 4);
        ObjectAnimator flightMiddleAlpha = ObjectAnimator.ofFloat(atk, "alpha", initialAlpha);
        flightMiddleAlpha.setDuration(BattleFragment.ANIMATION_LONG / 4);
        animatorSet.play(flightLeft).with(flightTop);
        animatorSet.play(flightLeft).with(flightAlpha);
        animatorSet.play(flightLeft).before(spinRight);
        animatorSet.play(spinRight).with(spinBottom);
        animatorSet.play(spinRight).with(spinRotation);
        animatorSet.play(flightMiddleLeft).after(spinRight);
        animatorSet.play(flightMiddleLeft).with(flightMiddleTop);
        animatorSet.play(flightMiddleLeft).with(flightMiddleAlpha);
        return animatorSet;
    }

    public static AnimatorSet xatk(BattleFragment battleFragment, final RelativeLayout atkC, final SimpleDraweeView atk, final RelativeLayout defC) {
        Context context = battleFragment.getActivity();
        final float initialAlpha = atk.getAlpha();
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator flightLeft = ObjectAnimator.ofFloat(atk, "x", 0f);
        flightLeft.setDuration(BattleFragment.ANIMATION_LONG / 4);
        ObjectAnimator flightTop = ObjectAnimator.ofFloat(atk, "y", 0f);
        flightTop.setDuration(BattleFragment.ANIMATION_LONG / 4);
        ObjectAnimator flightAlpha = ObjectAnimator.ofFloat(atk, "alpha", 0f);
        flightAlpha.setDuration(BattleFragment.ANIMATION_LONG / 4);
        final ImageView leftClaw = new ImageView(context);
        leftClaw.setImageResource(R.drawable.battle_leftclaw);
        final ImageView rightClaw = new ImageView(context);
        rightClaw.setImageResource(R.drawable.battle_rightclaw);
        final ViewGroup.LayoutParams imageParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ObjectAnimator flightClawRight = ObjectAnimator.ofFloat(leftClaw, "x", defC.getWidth());
        flightClawRight.setDuration(BattleFragment.ANIMATION_LONG / 2);
        ObjectAnimator flightClawBottom = ObjectAnimator.ofFloat(leftClaw, "y", defC.getHeight());
        flightClawBottom.setDuration(BattleFragment.ANIMATION_LONG / 2);
        ObjectAnimator flightClawLeft = ObjectAnimator.ofFloat(rightClaw, "x", 0f);
        flightClawLeft.setDuration(BattleFragment.ANIMATION_LONG / 2);
        ObjectAnimator flightClawBottomL = ObjectAnimator.ofFloat(rightClaw, "y", defC.getHeight());
        flightClawBottomL.setDuration(BattleFragment.ANIMATION_LONG / 2);
        flightClawRight.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationStartWithNet(Animator animation) {
                defC.addView(leftClaw, imageParams);
                leftClaw.setX(0f);
                leftClaw.setY(0f);
                defC.addView(rightClaw, imageParams);
                rightClaw.setX(defC.getWidth() - rightClaw.getWidth());
                rightClaw.setY(0f);
            }

            @Override
            public void onAnimationEndWithNet(Animator animation) {
                defC.removeView(leftClaw);
                defC.removeView(rightClaw);
                atk.setX(atkC.getWidth() - atk.getWidth());
                atk.setY(atkC.getHeight() - atk.getHeight());
            }
        });
        ObjectAnimator flightMiddleLeft = ObjectAnimator.ofFloat(atk, "x", atk.getX());
        flightMiddleLeft.setDuration(BattleFragment.ANIMATION_LONG / 4);
        ObjectAnimator flightMiddleTop = ObjectAnimator.ofFloat(atk, "y", atk.getY());
        flightMiddleTop.setDuration(BattleFragment.ANIMATION_LONG / 4);
        ObjectAnimator flightMiddleAlpha = ObjectAnimator.ofFloat(atk, "alpha", initialAlpha);
        flightMiddleAlpha.setDuration(BattleFragment.ANIMATION_LONG / 4);
        animatorSet.play(flightLeft).with(flightTop);
        animatorSet.play(flightLeft).with(flightAlpha);
        animatorSet.play(flightLeft).before(flightClawRight);
        animatorSet.play(flightClawRight).with(flightClawBottom);
        animatorSet.play(flightClawRight).with(flightClawLeft);
        animatorSet.play(flightClawRight).with(flightClawBottomL);
        animatorSet.play(flightMiddleLeft).after(flightClawRight);
        animatorSet.play(flightMiddleLeft).with(flightMiddleTop);
        animatorSet.play(flightMiddleLeft).with(flightMiddleAlpha);
        return animatorSet;
    }

    public static AnimatorSet self(BattleFragment battleFragment, final RelativeLayout atkC, final SimpleDraweeView atk) {
        Context context = battleFragment.getActivity();
        AnimatorSet animatorSet = new AnimatorSet();
        final ImageView flash = new ImageView(context);
        flash.setImageResource(R.drawable.battle_wisp);
        flash.setMaxHeight(80);
        flash.setMaxWidth(80);
        flash.setAdjustViewBounds(true);
        float[] atkCenter = getCenter(atk);
        flash.setX(atkCenter[0] - flash.getWidth() * 0.5f);
        flash.setY(atkCenter[1] - flash.getHeight() * 0.5f);
        final ViewGroup.LayoutParams imageParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ObjectAnimator flash1 = ObjectAnimator.ofFloat(flash, "alpha", 1f);
        flash1.setDuration(BattleFragment.ANIMATION_LONG / 3);
        ObjectAnimator flash2 = ObjectAnimator.ofFloat(flash, "alpha", 0f, 1f);
        flash2.setDuration(BattleFragment.ANIMATION_LONG / 3);
        ObjectAnimator flash3 = ObjectAnimator.ofFloat(flash, "alpha", 0f, 1f);
        flash3.setDuration(BattleFragment.ANIMATION_LONG / 3);
        flash1.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationStartWithNet(Animator animation) {
                atkC.addView(flash, imageParams);
            }
        });
        flash3.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationEndWithNet(Animator animation) {
                atkC.removeView(flash);
            }
        });
        animatorSet.play(flash1).before(flash2);
        animatorSet.play(flash2).before(flash3);
        return animatorSet;
    }

    public static AnimatorSet selfLight(BattleFragment battleFragment, final RelativeLayout atkC, final SimpleDraweeView atk) {
        Context context = battleFragment.getActivity();
        AnimatorSet animatorSet = new AnimatorSet();
        final ImageView flash = new ImageView(context);
        flash.setImageResource(R.drawable.battle_electroball);
        flash.setMaxHeight(atk.getHeight());
        flash.setMaxWidth(atk.getWidth());
        flash.setAdjustViewBounds(true);
        flash.setX(atk.getX());
        flash.setY(atk.getY());
        final ViewGroup.LayoutParams imageParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(flash, "scaleX", 0.2f);
        scaleX.setDuration(BattleFragment.ANIMATION_LONG);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(flash, "scaleY", 0.2f);
        scaleY.setDuration(BattleFragment.ANIMATION_LONG);
        scaleX.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationStartWithNet(Animator animation) {
                atkC.addView(flash, imageParams);
            }

            @Override
            public void onAnimationEndWithNet(Animator animation) {
                atkC.removeView(flash);
            }
        });
        animatorSet.play(scaleX).with(scaleY);
        return animatorSet;
    }

    public static AnimatorSet selfDark(BattleFragment battleFragment, final RelativeLayout atkC, final SimpleDraweeView atk) {
        Context context = battleFragment.getActivity();
        AnimatorSet animatorSet = new AnimatorSet();
        final ImageView flash = new ImageView(context);
        flash.setImageResource(R.drawable.battle_shadowball);
        flash.setMaxHeight(atk.getHeight());
        flash.setMaxWidth(atk.getWidth());
        flash.setAdjustViewBounds(true);
        flash.setX(atk.getX());
        flash.setY(atk.getY());
        final ViewGroup.LayoutParams imageParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(flash, "scaleX", 0.2f);
        scaleX.setDuration(BattleFragment.ANIMATION_LONG);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(flash, "scaleY", 0.2f);
        scaleY.setDuration(BattleFragment.ANIMATION_LONG);
        scaleX.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationStartWithNet(Animator animation) {
                atkC.addView(flash, imageParams);
            }

            @Override
            public void onAnimationEndWithNet(Animator animation) {
                atkC.removeView(flash);
            }
        });
        animatorSet.play(scaleX).with(scaleY);
        return animatorSet;
    }

    public static AnimatorSet trick(BattleFragment battleFragment, final RelativeLayout wrapper, final RelativeLayout atkC, final SimpleDraweeView atk, final RelativeLayout defC, SimpleDraweeView def) {
        Context context = battleFragment.getActivity();
        AnimatorSet animatorSet = new AnimatorSet();
        final ImageView trick = new ImageView(context);
        trick.setImageResource(R.drawable.pokeball_available);
        final ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        float startX = atkC.getX() + atk.getX() + atk.getWidth() * 0.5f;
        float startY = atkC.getY() + atk.getY() + atk.getHeight() * 0.5f;
        float endX = defC.getX() + def.getX() + def.getWidth() * 0.5f;
        float endY = defC.getY() + def.getY() + def.getHeight() * 0.5f;
        ObjectAnimator goX = ObjectAnimator.ofFloat(trick, "x", startX, endX);
        goX.setDuration(BattleFragment.ANIMATION_LONG / 2);
        ObjectAnimator goY = ObjectAnimator.ofFloat(trick, "y", startY, endY);
        goY.setDuration(BattleFragment.ANIMATION_LONG / 2);
        goX.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationStartWithNet(Animator animation) {
                wrapper.addView(trick, layoutParams);
            }
        });
        ObjectAnimator backX = ObjectAnimator.ofFloat(trick, "x", startX);
        backX.setDuration(BattleFragment.ANIMATION_LONG / 2);
        ObjectAnimator backY = ObjectAnimator.ofFloat(trick, "y", startY);
        backY.setDuration(BattleFragment.ANIMATION_LONG / 2);
        backX.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationEndWithNet(Animator animation) {
                wrapper.removeView(trick);
            }
        });
        animatorSet.play(goX).with(goY);
        animatorSet.play(backX).after(goX);
        animatorSet.play(backX).with(backY);
        return animatorSet;
    }

    public static AnimatorSet charge(BattleFragment battleFragment, RelativeLayout atkC, final SimpleDraweeView atk, RelativeLayout defC, SimpleDraweeView def, String[] split) {
        final float initialAlpha = atk.getAlpha();
        if (split.length >= 4 && split[3].equals("[still]")) {
            AnimatorSet animatorSet = new AnimatorSet();
            ObjectAnimator alpha = ObjectAnimator.ofFloat(atk, "alpha", 0.3f);
            alpha.setDuration(BattleFragment.ANIMATION_LONG);
            return animatorSet;
        } else {
            Context context = battleFragment.getActivity();
            AnimatorSet animatorSet = flight(battleFragment, atkC, atk, defC);
            animatorSet.addListener(new AnimatorListenerWithNet() {
                @Override
                public void onAnimationStartWithNet(Animator animation) {
                    atk.setAlpha(initialAlpha);
                }
            });
            return animatorSet;
        }
    }

    public static AnimatorSet spread(BattleFragment battleFragment, final RelativeLayout wrapper, final RelativeLayout atkC, final SimpleDraweeView atk, int spreadId) {
        Context context = battleFragment.getActivity();
        AnimatorSet animatorSet = new AnimatorSet();
        final ImageView flash1 = new ImageView(context);
        flash1.setImageResource(spreadId);
        flash1.setMaxHeight(100);
        flash1.setMaxWidth(100);
        flash1.setAdjustViewBounds(true);
        float startX = atkC.getX() + atk.getX() + atk.getWidth() * 0.5f - 50;
        float startY = atkC.getY() + atk.getY() + atk.getHeight() * 0.5f - 50;
        flash1.setX(startX);
        flash1.setY(startY);
        final ImageView flash2 = new ImageView(context);
        flash2.setImageResource(spreadId);
        flash2.setMaxHeight(100);
        flash2.setMaxWidth(100);
        flash2.setAdjustViewBounds(true);
        flash2.setX(startX);
        flash2.setY(startY);
        final ImageView flash3 = new ImageView(context);
        flash3.setImageResource(spreadId);
        flash3.setMaxHeight(100);
        flash3.setMaxWidth(100);
        flash3.setAdjustViewBounds(true);
        flash3.setX(startX);
        flash3.setY(startY);
        final ImageView flash4 = new ImageView(context);
        flash4.setImageResource(spreadId);
        flash4.setMaxHeight(100);
        flash4.setMaxWidth(100);
        flash4.setAdjustViewBounds(true);
        flash4.setX(startX);
        flash4.setY(startY);
        final ImageView flash5 = new ImageView(context);
        flash5.setImageResource(spreadId);
        flash5.setMaxHeight(100);
        flash5.setMaxWidth(100);
        flash5.setAdjustViewBounds(true);
        flash5.setX(startX);
        flash5.setY(startY);
        final ImageView flash6 = new ImageView(context);
        flash6.setImageResource(spreadId);
        flash6.setMaxHeight(100);
        flash6.setMaxWidth(100);
        flash6.setAdjustViewBounds(true);
        flash6.setX(startX);
        flash6.setY(startY);
        final ViewGroup.LayoutParams imageParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ObjectAnimator flash1FlyX = ObjectAnimator.ofFloat(flash1, "x", flash1.getX() - 500);
        flash1FlyX.setDuration(BattleFragment.ANIMATION_LONG);
        flash1FlyX.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator flash2FlyX = ObjectAnimator.ofFloat(flash2, "x", flash2.getX() - 300);
        flash2FlyX.setDuration(BattleFragment.ANIMATION_LONG);
        flash2FlyX.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator flash2FlyY = ObjectAnimator.ofFloat(flash2, "y", flash2.getY() - 100);
        flash2FlyY.setDuration(BattleFragment.ANIMATION_LONG);
        flash2FlyY.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator flash3FlyX = ObjectAnimator.ofFloat(flash3, "x", flash3.getX() + 400);
        flash3FlyX.setDuration(BattleFragment.ANIMATION_LONG);
        flash3FlyX.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator flash3FlyY = ObjectAnimator.ofFloat(flash3, "y", flash2.getY() - 100);
        flash3FlyY.setDuration(BattleFragment.ANIMATION_LONG);
        flash3FlyY.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator flash4FlyX = ObjectAnimator.ofFloat(flash4, "x", flash4.getX() + 500);
        flash4FlyX.setDuration(BattleFragment.ANIMATION_LONG);
        flash4FlyX.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator flash5FlyX = ObjectAnimator.ofFloat(flash5, "x", flash5.getX() + 400);
        flash5FlyX.setDuration(BattleFragment.ANIMATION_LONG);
        flash5FlyX.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator flash5FlyY = ObjectAnimator.ofFloat(flash5, "y", flash5.getY() + 100);
        flash5FlyY.setDuration(BattleFragment.ANIMATION_LONG);
        flash5FlyY.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator flash6FlyX = ObjectAnimator.ofFloat(flash6, "x", flash6.getX() - 400);
        flash6FlyX.setDuration(BattleFragment.ANIMATION_LONG);
        flash6FlyX.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator flash6FlyY = ObjectAnimator.ofFloat(flash6, "y", flash6.getY() + 100);
        flash6FlyY.setDuration(BattleFragment.ANIMATION_LONG);
        flash6FlyY.setInterpolator(new AccelerateInterpolator());
        flash1FlyX.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationStartWithNet(Animator animation) {
                wrapper.addView(flash1, imageParams);
                wrapper.addView(flash2, imageParams);
                wrapper.addView(flash3, imageParams);
                wrapper.addView(flash4, imageParams);
                wrapper.addView(flash5, imageParams);
                wrapper.addView(flash6, imageParams);
            }

            @Override
            public void onAnimationEndWithNet(Animator animation) {
                wrapper.removeView(flash1);
                wrapper.removeView(flash2);
                wrapper.removeView(flash3);
                wrapper.removeView(flash4);
                wrapper.removeView(flash5);
                wrapper.removeView(flash6);
            }
        });
        animatorSet.play(flash1FlyX).with(flash2FlyX);
        animatorSet.play(flash1FlyX).with(flash2FlyY);
        animatorSet.play(flash1FlyX).with(flash3FlyX);
        animatorSet.play(flash1FlyX).with(flash3FlyY);
        animatorSet.play(flash1FlyX).with(flash4FlyX);
        animatorSet.play(flash1FlyX).with(flash5FlyX);
        animatorSet.play(flash1FlyX).with(flash5FlyY);
        animatorSet.play(flash1FlyX).with(flash6FlyX);
        animatorSet.play(flash1FlyX).with(flash6FlyY);
        return animatorSet;
    }

    public static AnimatorSet contact(BattleFragment battleFragment, final RelativeLayout wrapper, final RelativeLayout atkC, final SimpleDraweeView atk, final RelativeLayout defC, final SimpleDraweeView def, int contactId) {
        Context context = battleFragment.getActivity();
        final float initialAlpha = atk.getAlpha();
        AnimatorSet animatorSet = new AnimatorSet();
        final SimpleDraweeView atkPkm = new SimpleDraweeView(context);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setControllerListener(getController(context, atkPkm))
                .setUri(Uri.parse(atk.getTag().toString()))
                .setAutoPlayAnimations(true)
                .build();
        atkPkm.setController(controller);
        atkPkm.setX(atkC.getX() + atk.getX());
        atkPkm.setY(atkC.getY() + atk.getY());
        final ViewGroup.LayoutParams imageParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ObjectAnimator attackX = ObjectAnimator.ofFloat(atkPkm, "x", (defC.getX() + def.getX()));
        attackX.setDuration(BattleFragment.ANIMATION_LONG / 3);
        attackX.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator attackY = ObjectAnimator.ofFloat(atkPkm, "y", (defC.getY() + def.getY()));
        attackY.setDuration(BattleFragment.ANIMATION_LONG / 3);
        attackY.setInterpolator(new AccelerateInterpolator());
        attackX.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationStartWithNet(Animator animation) {
                atk.setAlpha(0f);
                wrapper.addView(atkPkm, imageParams);
            }
        });
        final ImageView contact = new ImageView(context);
        contact.setMaxWidth(100);
        contact.setMaxHeight(100);
        contact.setAdjustViewBounds(true);
        contact.setImageResource(contactId);
        contact.setX(def.getX() + def.getWidth() * 0.5f - 50);
        contact.setY(def.getY() + def.getHeight() * 0.5f - 50);
        ObjectAnimator contactScaleX = ObjectAnimator.ofFloat(contact, "scaleX", 3.5f);
        contactScaleX.setDuration(BattleFragment.ANIMATION_LONG * 2 / 3);
        ObjectAnimator contactScaleY = ObjectAnimator.ofFloat(contact, "scaleY", 3.5f);
        contactScaleY.setDuration(BattleFragment.ANIMATION_LONG * 2 / 3);
        ObjectAnimator contactAlpha = ObjectAnimator.ofFloat(contact, "alpha", 0f);
        contactAlpha.setDuration(BattleFragment.ANIMATION_LONG * 2 / 3);
        contactAlpha.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator defendX = ObjectAnimator.ofFloat(atkPkm, "x", (atkC.getX() + atk.getX()));
        defendX.setDuration(BattleFragment.ANIMATION_LONG * 2 / 3);
        ObjectAnimator defendY = ObjectAnimator.ofFloat(atkPkm, "y", (atkC.getY() + atk.getY()));
        defendY.setDuration(BattleFragment.ANIMATION_LONG * 2 / 3);
        contactScaleX.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationStartWithNet(Animator animation) {
                defC.addView(contact, imageParams);
            }

            @Override
            public void onAnimationEndWithNet(Animator animation) {
                defC.removeView(contact);
                wrapper.removeView(atkPkm);
                atk.setAlpha(initialAlpha);
            }
        });
        animatorSet.play(attackX).with(attackY);
        animatorSet.play(attackX).before(contactScaleX);
        animatorSet.play(contactScaleX).with(contactScaleY);
        animatorSet.play(contactScaleX).with(contactAlpha);
        animatorSet.play(contactScaleX).with(defendX);
        animatorSet.play(contactScaleX).with(defendY);
        return animatorSet;
    }

    public static AnimatorSet drain(BattleFragment battleFragment, final RelativeLayout atkC, final SimpleDraweeView atk, final RelativeLayout defC) {
        Context context = battleFragment.getActivity();
        AnimatorSet animatorSet = new AnimatorSet();
        final ImageView attack = new ImageView(context);
        attack.setImageResource(R.drawable.battle_energyball);
        attack.setMaxHeight(40);
        attack.setMaxWidth(40);
        attack.setAdjustViewBounds(true);
        attack.setX(atk.getX() + atk.getWidth() * 0.5f - 20);
        attack.setY(atk.getY() + atk.getHeight() * 0.5f - 20);
        final ViewGroup.LayoutParams imageParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(attack, "scaleX", 5f);
        scaleX.setDuration(BattleFragment.ANIMATION_LONG / 2);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(attack, "scaleY", 5f);
        scaleY.setDuration(BattleFragment.ANIMATION_LONG / 2);
        scaleX.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationStartWithNet(Animator animation) {
                defC.addView(attack, imageParams);
            }

            @Override
            public void onAnimationEndWithNet(Animator animation) {
                defC.removeView(attack);
            }
        });
        final ImageView drain = new ImageView(context);
        drain.setImageResource(R.drawable.battle_energyball);
        drain.setMaxHeight(atk.getHeight());
        drain.setMaxWidth(atk.getWidth());
        drain.setAdjustViewBounds(true);
        drain.setX(atk.getX());
        drain.setY(atk.getY());
        ObjectAnimator drainX = ObjectAnimator.ofFloat(drain, "scaleX", 0.2f);
        drainX.setDuration(BattleFragment.ANIMATION_LONG / 2);
        ObjectAnimator drainY = ObjectAnimator.ofFloat(drain, "scaleY", 0.2f);
        drainY.setDuration(BattleFragment.ANIMATION_LONG / 2);
        drainX.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationStartWithNet(Animator animation) {
                atkC.addView(drain, imageParams);
            }

            @Override
            public void onAnimationEndWithNet(Animator animation) {
                atkC.removeView(drain);
            }
        });
        animatorSet.play(scaleX).with(scaleY);
        animatorSet.play(drainX).after(scaleX);
        animatorSet.play(drainX).with(drainY);
        return animatorSet;
    }

    public static AnimatorSet contactTwice(BattleFragment battleFragment, final RelativeLayout wrapper, final RelativeLayout atkC, final SimpleDraweeView atk, final RelativeLayout defC, final SimpleDraweeView def, int contact1Id, int contact2Id) {
        Context context = battleFragment.getActivity();
        final float initialAlpha = atk.getAlpha();
        AnimatorSet animatorSet = new AnimatorSet();
        final SimpleDraweeView atkPkm = new SimpleDraweeView(context);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setControllerListener(getController(context, atkPkm))
                .setUri(Uri.parse(atk.getTag().toString()))
                .setAutoPlayAnimations(true)
                .build();
        atkPkm.setController(controller);
        atkPkm.setX(atkC.getX() + atk.getX());
        atkPkm.setY(atkC.getY() + atk.getY());
        final ViewGroup.LayoutParams imageParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ObjectAnimator attackX = ObjectAnimator.ofFloat(atkPkm, "x", (defC.getX() + def.getX()));
        attackX.setDuration(BattleFragment.ANIMATION_LONG / 3);
        attackX.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator attackY = ObjectAnimator.ofFloat(atkPkm, "y", (defC.getY() + def.getY()));
        attackY.setDuration(BattleFragment.ANIMATION_LONG / 3);
        attackY.setInterpolator(new AccelerateInterpolator());
        attackX.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationStartWithNet(Animator animation) {
                atk.setAlpha(0f);
                wrapper.addView(atkPkm, imageParams);
            }
        });
        final ImageView contact1 = new ImageView(context);
        contact1.setMaxWidth(100);
        contact1.setMaxHeight(100);
        contact1.setAdjustViewBounds(true);
        contact1.setImageResource(contact1Id);
        contact1.setX(def.getX() + def.getWidth() * 0.5f - 50);
        contact1.setY(def.getY() + def.getHeight() * 0.5f - 50);
        ObjectAnimator contactScaleX = ObjectAnimator.ofFloat(contact1, "scaleX", 3.5f);
        contactScaleX.setDuration(BattleFragment.ANIMATION_LONG / 3);
        ObjectAnimator contactScaleY = ObjectAnimator.ofFloat(contact1, "scaleY", 3.5f);
        contactScaleY.setDuration(BattleFragment.ANIMATION_LONG / 3);
        ObjectAnimator contactAlpha = ObjectAnimator.ofFloat(contact1, "alpha", 0f);
        contactAlpha.setDuration(BattleFragment.ANIMATION_LONG / 3);
        contactAlpha.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator defendX = ObjectAnimator.ofFloat(atkPkm, "x", (atkC.getX() + atk.getX()));
        defendX.setDuration(BattleFragment.ANIMATION_LONG / 3);
        ObjectAnimator defendY = ObjectAnimator.ofFloat(atkPkm, "y", (atkC.getY() + atk.getY()));
        defendY.setDuration(BattleFragment.ANIMATION_LONG / 3);
        final ImageView contact2 = new ImageView(context);
        contact2.setMaxWidth(100);
        contact2.setMaxHeight(100);
        contact2.setAdjustViewBounds(true);
        contact2.setImageResource(contact2Id);
        contact2.setX(def.getX() + def.getWidth() * 0.5f - 50);
        contact2.setY(def.getY() + def.getHeight() * 0.5f - 50);
        ObjectAnimator contact2ScaleX = ObjectAnimator.ofFloat(contact2, "scaleX", 3.5f);
        contact2ScaleX.setDuration(BattleFragment.ANIMATION_LONG / 3);
        ObjectAnimator contact2ScaleY = ObjectAnimator.ofFloat(contact2, "scaleY", 3.5f);
        contact2ScaleY.setDuration(BattleFragment.ANIMATION_LONG / 3);
        ObjectAnimator contact2Alpha = ObjectAnimator.ofFloat(contact2, "alpha", 0f);
        contact2Alpha.setDuration(BattleFragment.ANIMATION_LONG / 3);
        contact2Alpha.setInterpolator(new AccelerateInterpolator());
        contactScaleX.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationStartWithNet(Animator animation) {
                defC.addView(contact1, imageParams);
            }

            @Override
            public void onAnimationEndWithNet(Animator animation) {
                defC.removeView(contact1);
            }
        });
        contact2ScaleX.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationStartWithNet(Animator animation) {
                defC.addView(contact2, imageParams);
            }

            @Override
            public void onAnimationEndWithNet(Animator animation) {
                defC.removeView(contact2);
                wrapper.removeView(atkPkm);
                atk.setAlpha(initialAlpha);
            }
        });
        animatorSet.play(attackX).with(attackY);
        animatorSet.play(attackX).before(contactScaleX);
        animatorSet.play(contactScaleX).with(contactScaleY);
        animatorSet.play(contactScaleX).with(contactAlpha);
        animatorSet.play(contactScaleX).with(defendX);
        animatorSet.play(contactScaleX).with(defendY);
        animatorSet.play(contact2ScaleX).after(contactScaleX);
        animatorSet.play(contact2ScaleX).with(contact2ScaleY);
        animatorSet.play(contact2ScaleX).with(contact2Alpha);
        return animatorSet;
    }

    public static AnimatorSet stream(BattleFragment battleFragment, final RelativeLayout wrapper, final RelativeLayout atkC, final SimpleDraweeView atk, final RelativeLayout defC, final SimpleDraweeView def, int spreadId) {
        Context context = battleFragment.getActivity();
        AnimatorSet animatorSet = new AnimatorSet();
        int initialSize = 40;
        final ImageView flash1 = new ImageView(context);
        flash1.setImageResource(spreadId);
        flash1.setMaxHeight(initialSize);
        flash1.setMaxWidth(initialSize);
        flash1.setAdjustViewBounds(true);
        float startX = atkC.getX() + atk.getX() + atk.getWidth() * 0.5f - 20;
        float startY = atkC.getY() + atk.getY() + atk.getHeight() * 0.5f - 20;
        flash1.setX(startX);
        flash1.setY(startY);
        final ImageView flash2 = new ImageView(context);
        flash2.setImageResource(spreadId);
        flash2.setMaxHeight(initialSize);
        flash2.setMaxWidth(initialSize);
        flash2.setAdjustViewBounds(true);
        flash2.setX(startX);
        flash2.setY(startY);
        final ImageView flash3 = new ImageView(context);
        flash3.setImageResource(spreadId);
        flash3.setMaxHeight(initialSize);
        flash3.setMaxWidth(initialSize);
        flash3.setAdjustViewBounds(true);
        flash3.setX(startX);
        flash3.setY(startY);
        final ImageView flash4 = new ImageView(context);
        flash4.setImageResource(spreadId);
        flash4.setMaxHeight(initialSize);
        flash4.setMaxWidth(initialSize);
        flash4.setAdjustViewBounds(true);
        flash4.setX(startX);
        flash4.setY(startY);
        final ImageView flash5 = new ImageView(context);
        flash5.setImageResource(spreadId);
        flash5.setMaxHeight(initialSize);
        flash5.setMaxWidth(initialSize);
        flash5.setAdjustViewBounds(true);
        flash5.setX(startX);
        flash5.setY(startY);
        final ImageView flash6 = new ImageView(context);
        flash6.setImageResource(spreadId);
        flash6.setMaxHeight(initialSize);
        flash6.setMaxWidth(initialSize);
        flash6.setAdjustViewBounds(true);
        flash6.setX(startX);
        flash6.setY(startY);
        final ViewGroup.LayoutParams imageParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        float scale = 5f;
        float endX = defC.getX() + def.getX() + def.getWidth() * 0.5f - 50;
        float endY = defC.getY() + def.getY() + def.getHeight() * 0.5f - 50;
        ObjectAnimator flash1FlyX = ObjectAnimator.ofFloat(flash1, "x", endX);
        flash1FlyX.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        flash1FlyX.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator flash1FlyY = ObjectAnimator.ofFloat(flash1, "y", endY);
        flash1FlyY.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        flash1FlyY.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator flash1SizeX = ObjectAnimator.ofFloat(flash1, "scaleX", scale);
        flash1SizeX.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        ObjectAnimator flash1SizeY = ObjectAnimator.ofFloat(flash1, "scaleY", scale);
        flash1SizeY.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        ObjectAnimator flash2FlyX = ObjectAnimator.ofFloat(flash2, "x", endX);
        flash2FlyX.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        flash2FlyX.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator flash2FlyY = ObjectAnimator.ofFloat(flash2, "y", endY);
        flash2FlyY.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        flash2FlyY.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator flash2SizeX = ObjectAnimator.ofFloat(flash2, "scaleX", scale);
        flash2SizeX.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        ObjectAnimator flash2SizeY = ObjectAnimator.ofFloat(flash2, "scaleY", scale);
        flash2SizeY.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        ObjectAnimator flash3FlyX = ObjectAnimator.ofFloat(flash3, "x", endX);
        flash3FlyX.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        flash3FlyX.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator flash3FlyY = ObjectAnimator.ofFloat(flash3, "y", endY);
        flash3FlyY.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        flash3FlyY.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator flash3SizeX = ObjectAnimator.ofFloat(flash3, "scaleX", scale);
        flash3SizeX.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        ObjectAnimator flash3SizeY = ObjectAnimator.ofFloat(flash3, "scaleY", scale);
        flash3SizeY.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        ObjectAnimator flash4FlyX = ObjectAnimator.ofFloat(flash4, "x", endX);
        flash4FlyX.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        flash4FlyX.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator flash4FlyY = ObjectAnimator.ofFloat(flash4, "y", endY);
        flash4FlyY.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        flash4FlyY.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator flash4SizeX = ObjectAnimator.ofFloat(flash4, "scaleX", scale);
        flash4SizeX.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        ObjectAnimator flash4SizeY = ObjectAnimator.ofFloat(flash4, "scaleY", scale);
        flash4SizeY.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        ObjectAnimator flash5FlyX = ObjectAnimator.ofFloat(flash5, "x", endX);
        flash5FlyX.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        flash5FlyX.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator flash5FlyY = ObjectAnimator.ofFloat(flash5, "y", endY);
        flash5FlyY.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        flash5FlyY.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator flash5SizeX = ObjectAnimator.ofFloat(flash5, "scaleX", scale);
        flash5SizeX.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        ObjectAnimator flash5SizeY = ObjectAnimator.ofFloat(flash5, "scaleY", scale);
        flash5SizeY.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        ObjectAnimator flash6FlyX = ObjectAnimator.ofFloat(flash6, "x", endX);
        flash6FlyX.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        flash6FlyX.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator flash6FlyY = ObjectAnimator.ofFloat(flash6, "y", endY);
        flash6FlyY.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        flash6FlyY.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator flash6SizeX = ObjectAnimator.ofFloat(flash6, "scaleX", scale);
        flash6SizeX.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        ObjectAnimator flash6SizeY = ObjectAnimator.ofFloat(flash6, "scaleY", scale);
        flash6SizeY.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        flash1FlyX.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationStartWithNet(Animator animation) {
                wrapper.addView(flash1, imageParams);
                wrapper.addView(flash2, imageParams);
                wrapper.addView(flash3, imageParams);
                wrapper.addView(flash4, imageParams);
                wrapper.addView(flash5, imageParams);
                wrapper.addView(flash6, imageParams);
            }
        });
        flash6FlyX.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationEndWithNet(Animator animation) {
                wrapper.removeView(flash1);
                wrapper.removeView(flash2);
                wrapper.removeView(flash3);
                wrapper.removeView(flash4);
                wrapper.removeView(flash5);
                wrapper.removeView(flash6);
            }
        });
        ValueAnimator delay2 = ValueAnimator.ofFloat(1f);
        delay2.setDuration(BattleFragment.ANIMATION_LONG / 20);
        ValueAnimator delay3 = ValueAnimator.ofFloat(1f);
        delay3.setDuration(BattleFragment.ANIMATION_LONG * 2 / 20);
        ValueAnimator delay4 = ValueAnimator.ofFloat(1f);
        delay4.setDuration(BattleFragment.ANIMATION_LONG * 3 / 20);
        ValueAnimator delay5 = ValueAnimator.ofFloat(1f);
        delay5.setDuration(BattleFragment.ANIMATION_LONG * 4 / 20);
        ValueAnimator delay6 = ValueAnimator.ofFloat(1f);
        delay6.setDuration(BattleFragment.ANIMATION_LONG * 5 / 20);
        animatorSet.play(flash1FlyX).with(flash1FlyY);
        animatorSet.play(flash1FlyX).with(flash1SizeX);
        animatorSet.play(flash1FlyX).with(flash1SizeY);
        animatorSet.play(flash1FlyX).with(delay2);
        animatorSet.play(flash1FlyX).with(delay3);
        animatorSet.play(flash1FlyX).with(delay4);
        animatorSet.play(flash1FlyX).with(delay5);
        animatorSet.play(flash1FlyX).with(delay6);
        animatorSet.play(flash2FlyX).after(delay2);
        animatorSet.play(flash2FlyX).with(flash2FlyY);
        animatorSet.play(flash2FlyX).with(flash2SizeX);
        animatorSet.play(flash2FlyX).with(flash2SizeY);
        animatorSet.play(flash3FlyX).after(delay3);
        animatorSet.play(flash3FlyX).with(flash3FlyY);
        animatorSet.play(flash3FlyX).with(flash3SizeX);
        animatorSet.play(flash3FlyX).with(flash3SizeY);
        animatorSet.play(flash4FlyX).after(delay4);
        animatorSet.play(flash4FlyX).with(flash4FlyY);
        animatorSet.play(flash4FlyX).with(flash4SizeX);
        animatorSet.play(flash4FlyX).with(flash4SizeY);
        animatorSet.play(flash5FlyX).after(delay5);
        animatorSet.play(flash5FlyX).with(flash5FlyY);
        animatorSet.play(flash5FlyX).with(flash5SizeX);
        animatorSet.play(flash5FlyX).with(flash5SizeY);
        animatorSet.play(flash6FlyX).after(delay6);
        animatorSet.play(flash6FlyX).with(flash6FlyY);
        animatorSet.play(flash6FlyX).with(flash6SizeX);
        animatorSet.play(flash6FlyX).with(flash6SizeY);
        return animatorSet;
    }

    public static AnimatorSet earth(final RelativeLayout defC, final SimpleDraweeView def) {
        AnimatorSet animatorSet = new AnimatorSet();
        float left = 0f;
        float right = defC.getWidth() - def.getWidth();
        ObjectAnimator earth1 = ObjectAnimator.ofFloat(def, "x", def.getX(), left, right, left, right, left, right, left, right, def.getX());
        earth1.setDuration(BattleFragment.ANIMATION_LONG);
        animatorSet.play(earth1);
        return animatorSet;
    }

    public static AnimatorSet phaze(final SimpleDraweeView def) {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator spin = ObjectAnimator.ofFloat(def, "rotation", 1080f);
        spin.setDuration(BattleFragment.ANIMATION_LONG);
        spin.setInterpolator(new AccelerateDecelerateInterpolator());
        spin.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationEndWithNet(Animator animation) {
                def.setRotation(0f);
            }
        });
        animatorSet.play(spin);
        return animatorSet;
    }

    public static AnimatorSet thunderStrong(BattleFragment battleFragment, final RelativeLayout defC, final SimpleDraweeView def) {
        Context context = battleFragment.getActivity();
        AnimatorSet animatorSet = new AnimatorSet();
        final ImageView thunder1 = new ImageView(context);
        thunder1.setImageResource(R.drawable.battle_lightning);
        thunder1.setMaxHeight(defC.getHeight());
        thunder1.setScaleType(ImageView.ScaleType.FIT_END);
        thunder1.setAdjustViewBounds(true);
        thunder1.setX(def.getX() + def.getWidth() * 0.5f);
        ObjectAnimator flash1 = ObjectAnimator.ofFloat(thunder1, "alpha", 0f, 1f);
        flash1.setDuration(BattleFragment.ANIMATION_LONG / 3);
        ObjectAnimator flash2 = ObjectAnimator.ofFloat(thunder1, "alpha", 0f, 1f);
        flash2.setDuration(BattleFragment.ANIMATION_LONG / 3);
        ObjectAnimator flash3 = ObjectAnimator.ofFloat(thunder1, "alpha", 0f, 1f);
        flash3.setDuration(BattleFragment.ANIMATION_LONG / 3);
        flash1.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationStartWithNet(Animator animation) {
                defC.addView(thunder1);
            }
        });
        flash3.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationEndWithNet(Animator animation) {
                defC.removeView(thunder1);
            }
        });
        animatorSet.play(flash1);
        animatorSet.play(flash2).after(flash1);
        animatorSet.play(flash3).after(flash2);
        return animatorSet;
    }

    public static AnimatorSet thunderNeutral(BattleFragment battleFragment, final RelativeLayout defC, final SimpleDraweeView def) {
        Context context = battleFragment.getActivity();
        AnimatorSet animatorSet = new AnimatorSet();
        final ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final ImageView thunder1 = new ImageView(context);
        thunder1.setImageResource(R.drawable.battle_lightning);
        thunder1.setMaxHeight(def.getHeight());
        thunder1.setAdjustViewBounds(true);
        thunder1.setX(def.getX() + def.getWidth() * 0.5f);
        final ImageView thunder2 = new ImageView(context);
        thunder2.setImageResource(R.drawable.battle_lightning);
        thunder2.setMaxHeight(def.getHeight());
        thunder2.setAdjustViewBounds(true);
        thunder2.setX(def.getX() + def.getWidth() * 0.5f - 20);
        final ImageView thunder3 = new ImageView(context);
        thunder3.setImageResource(R.drawable.battle_lightning);
        thunder3.setMaxHeight(def.getHeight());
        thunder3.setAdjustViewBounds(true);
        thunder3.setX(def.getX() + def.getWidth() * 0.5f + 20);
        ObjectAnimator drop1 = ObjectAnimator.ofFloat(thunder1, "y", defC.getY());
        drop1.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        ObjectAnimator drop2 = ObjectAnimator.ofFloat(thunder2, "y", defC.getY());
        drop2.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        drop2.setStartDelay(BattleFragment.ANIMATION_LONG / 8);
        ObjectAnimator drop3 = ObjectAnimator.ofFloat(thunder3, "y", defC.getY());
        drop3.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        drop3.setStartDelay(BattleFragment.ANIMATION_LONG / 4);
        drop1.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationStartWithNet(Animator animation) {
                defC.addView(thunder1, params);
            }

            @Override
            public void onAnimationEndWithNet(Animator animation) {
                defC.removeView(thunder1);
            }
        });
        drop2.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationStartWithNet(Animator animation) {
                defC.addView(thunder2, params);
            }

            @Override
            public void onAnimationEndWithNet(Animator animation) {
                defC.removeView(thunder2);
            }
        });
        drop3.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationStartWithNet(Animator animation) {
                defC.addView(thunder3, params);
            }

            @Override
            public void onAnimationEndWithNet(Animator animation) {
                defC.removeView(thunder3);
            }
        });
        animatorSet.play(drop1).with(drop2);
        animatorSet.play(drop1).with(drop3);
        return animatorSet;
    }

    public static AnimatorSet thunderWeak(BattleFragment battleFragment, final RelativeLayout atkC, final SimpleDraweeView atk, final RelativeLayout defC, final SimpleDraweeView def) {
        Context context = battleFragment.getActivity();
        AnimatorSet animatorSet = new AnimatorSet();
        final ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final ImageView glow = new ImageView(context);
        glow.setImageResource(R.drawable.battle_electroball);
        glow.setAlpha(0.5f);
        glow.setMaxWidth(40);
        glow.setMaxHeight(40);
        glow.setX(atk.getX() + atk.getWidth() * 0.5f);
        glow.setY(atk.getY() + atk.getHeight() * 0.5f);
        glow.setAdjustViewBounds(true);
        final ImageView thunder = new ImageView(context);
        thunder.setImageResource(R.drawable.battle_lightning);
        thunder.setMaxHeight(defC.getHeight());
        thunder.setAdjustViewBounds(true);
        thunder.setX(def.getX() + def.getWidth() * 0.5f);
        ObjectAnimator glowX = ObjectAnimator.ofFloat(glow, "scaleX", 8f);
        glowX.setDuration(BattleFragment.ANIMATION_LONG / 2);
        ObjectAnimator glowY = ObjectAnimator.ofFloat(glow, "scaleY", 8f);
        glowY.setDuration(BattleFragment.ANIMATION_LONG / 2);
        glowX.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationStartWithNet(Animator animation) {
                atkC.addView(glow, params);
            }

            @Override
            public void onAnimationEndWithNet(Animator animation) {
                atkC.removeView(glow);
            }
        });
        ObjectAnimator drop = ObjectAnimator.ofFloat(thunder, "y", defC.getHeight());
        drop.setDuration(BattleFragment.ANIMATION_LONG / 2);
        drop.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationStartWithNet(Animator animation) {
                defC.addView(thunder, params);
            }

            @Override
            public void onAnimationEndWithNet(Animator animation) {
                defC.removeView(thunder);
            }
        });
        animatorSet.play(glowX).with(glowY);
        animatorSet.play(drop).after(glowX);
        return animatorSet;
    }

    public static AnimatorSet status(BattleFragment battleFragment, final RelativeLayout defC, final SimpleDraweeView def, int statusId) {
        Context context = battleFragment.getActivity();
        AnimatorSet animatorSet = new AnimatorSet();
        final ImageView status = new ImageView(context);
        status.setImageResource(statusId);
        status.setMaxHeight(100);
        status.setMaxWidth(100);
        status.setAdjustViewBounds(true);
        status.setX(def.getX() + def.getWidth() * 0.5f);
        status.setY(def.getY() + def.getHeight() * 0.5f);
        ObjectAnimator circlingX = ObjectAnimator.ofFloat(status, "x", status.getX(), 0f, status.getX(), defC.getWidth() - def.getWidth(), status.getX());
        circlingX.setDuration(BattleFragment.ANIMATION_LONG);
        ObjectAnimator circlingY = ObjectAnimator.ofFloat(status, "y", status.getY(), status.getY() - 30f, status.getY() - 60f, status.getY() - 30f, status.getY());
        circlingY.setDuration(BattleFragment.ANIMATION_LONG);
        circlingX.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationStartWithNet(Animator animation) {
                defC.addView(status);
            }

            @Override
            public void onAnimationEndWithNet(Animator animation) {
                defC.removeView(status);
            }
        });
        animatorSet.play(circlingX).with(circlingY);
        return animatorSet;
    }

    public static AnimatorSet ball(BattleFragment battleFragment, final RelativeLayout wrapper, final RelativeLayout atkC, final SimpleDraweeView atk, final RelativeLayout defC, final SimpleDraweeView def, int ballId) {
        Context context = battleFragment.getActivity();
        AnimatorSet animatorSet = new AnimatorSet();
        final ImageView ball = new ImageView(context);
        ball.setImageResource(ballId);
        ball.setMaxHeight(atk.getHeight());
        ball.setMaxWidth(atk.getWidth());
        ball.setAdjustViewBounds(true);
        ball.setX(atkC.getX() + atk.getX());
        ball.setY(atkC.getY() + atk.getY());
        ObjectAnimator ballX = ObjectAnimator.ofFloat(ball, "x", defC.getX() + def.getX());
        ballX.setDuration(BattleFragment.ANIMATION_LONG);
        ObjectAnimator ballY = ObjectAnimator.ofFloat(ball, "y", defC.getY() + def.getY());
        ballY.setDuration(BattleFragment.ANIMATION_LONG);
        ballX.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationEndWithNet(Animator animation) {
                wrapper.removeView(ball);
            }

            @Override
            public void onAnimationStartWithNet(Animator animation) {
                wrapper.addView(ball);
            }


        });
        animatorSet.play(ballX).with(ballY);
        return animatorSet;
    }

    public static AnimatorSet wish(BattleFragment battleFragment, final RelativeLayout atkC, final SimpleDraweeView atk) {
        Context context = battleFragment.getActivity();
        AnimatorSet animatorSet = new AnimatorSet();
        final ImageView wish = new ImageView(context);
        wish.setImageResource(R.drawable.battle_wisp);
        wish.setMaxWidth(60);
        wish.setMaxHeight(60);
        wish.setAdjustViewBounds(true);
        wish.setX(atk.getX() + atk.getWidth() * 0.5f);
        wish.setY(atk.getY() + atk.getHeight() * 0.5f);
        ObjectAnimator wishY = ObjectAnimator.ofFloat(wish, "y", -20f);
        wishY.setDuration(BattleFragment.ANIMATION_LONG);
        wishY.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationStartWithNet(Animator animation) {
                atkC.addView(wish);
            }

            @Override
            public void onAnimationEndWithNet(Animator animation) {
                atkC.removeView(wish);
            }
        });
        animatorSet.play(wishY);
        return animatorSet;
    }

    public static AnimatorSet slash(BattleFragment battleFragment, final RelativeLayout defC) {
        Context context = battleFragment.getActivity();
        AnimatorSet animatorSet = new AnimatorSet();
        final ImageView slash = new ImageView(context);
        slash.setImageResource(R.drawable.battle_leftclaw);
        ObjectAnimator slashX = ObjectAnimator.ofFloat(slash, "x", defC.getWidth());
        slashX.setDuration(BattleFragment.ANIMATION_LONG);
        slashX.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator slashY = ObjectAnimator.ofFloat(slash, "y", defC.getHeight());
        slashY.setDuration(BattleFragment.ANIMATION_LONG);
        slashX.setInterpolator(new AccelerateInterpolator());
        slashX.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationStartWithNet(Animator animation) {
                defC.addView(slash);
            }

            @Override
            public void onAnimationEndWithNet(Animator animation) {
                defC.removeView(slash);
            }
        });
        animatorSet.play(slashX).with(slashY);
        return animatorSet;
    }

    public static AnimatorSet bomb(BattleFragment battleFragment, final RelativeLayout defC, final SimpleDraweeView def, int bombId) {
        Context context = battleFragment.getActivity();
        AnimatorSet animatorSet = new AnimatorSet();
        int maxSize = 240;
        final ImageView bomb1 = new ImageView(context);
        bomb1.setImageResource(bombId);
        bomb1.setMaxHeight(maxSize);
        bomb1.setMaxWidth(maxSize);
        bomb1.setAdjustViewBounds(true);
        final ImageView bomb2 = new ImageView(context);
        bomb2.setImageResource(bombId);
        bomb2.setMaxHeight(maxSize);
        bomb2.setMaxWidth(maxSize);
        bomb2.setAdjustViewBounds(true);
        bomb2.setX(defC.getWidth() / 4);
        bomb2.setY(defC.getHeight() / 4);
        final ImageView bomb3 = new ImageView(context);
        bomb3.setImageResource(bombId);
        bomb3.setMaxHeight(maxSize);
        bomb3.setMaxWidth(maxSize);
        bomb3.setAdjustViewBounds(true);
        bomb3.setX(defC.getWidth() / 2);
        ObjectAnimator bomb1Y = ObjectAnimator.ofFloat(bomb1, "y", defC.getHeight() / 4);
        bomb1Y.setDuration(BattleFragment.ANIMATION_LONG * 2 / 3);
        ObjectAnimator bomb2Y = ObjectAnimator.ofFloat(bomb2, "y", defC.getHeight() / 2);
        bomb2Y.setDuration(BattleFragment.ANIMATION_LONG * 2 / 3);
        ObjectAnimator bomb3Y = ObjectAnimator.ofFloat(bomb3, "y", defC.getHeight() / 4);
        bomb3Y.setDuration(BattleFragment.ANIMATION_LONG * 2 / 3);
        bomb1Y.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationStartWithNet(Animator animation) {
                defC.addView(bomb1);
                defC.addView(bomb2);
                defC.addView(bomb3);
            }
        });
        ObjectAnimator explode1X = ObjectAnimator.ofFloat(bomb1, "scaleX", 2f);
        explode1X.setDuration(BattleFragment.ANIMATION_LONG / 3);
        explode1X.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator explode1Y = ObjectAnimator.ofFloat(bomb1, "scaleY", 2f);
        explode1Y.setDuration(BattleFragment.ANIMATION_LONG / 3);
        explode1Y.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator explode1Alpha = ObjectAnimator.ofFloat(bomb1, "alpha", 0f);
        explode1Alpha.setDuration(BattleFragment.ANIMATION_LONG / 3);
        explode1Alpha.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator explode2X = ObjectAnimator.ofFloat(bomb2, "scaleX", 2f);
        explode2X.setDuration(BattleFragment.ANIMATION_LONG / 3);
        explode2X.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator explode2Y = ObjectAnimator.ofFloat(bomb2, "scaleY", 2f);
        explode2Y.setDuration(BattleFragment.ANIMATION_LONG / 3);
        explode2Y.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator explode2Alpha = ObjectAnimator.ofFloat(bomb2, "alpha", 0f);
        explode2Alpha.setDuration(BattleFragment.ANIMATION_LONG / 3);
        explode2Alpha.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator explode3X = ObjectAnimator.ofFloat(bomb3, "scaleX", 2f);
        explode3X.setDuration(BattleFragment.ANIMATION_LONG / 3);
        explode3X.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator explode3Y = ObjectAnimator.ofFloat(bomb3, "scaleY", 2f);
        explode3Y.setDuration(BattleFragment.ANIMATION_LONG / 3);
        explode3Y.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator explode3Alpha = ObjectAnimator.ofFloat(bomb3, "alpha", 0f);
        explode3Alpha.setDuration(BattleFragment.ANIMATION_LONG / 3);
        explode3Alpha.setInterpolator(new AccelerateInterpolator());
        explode3Alpha.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationEndWithNet(Animator animation) {
                defC.removeView(bomb1);
                defC.removeView(bomb2);
                defC.removeView(bomb3);
            }
        });
        animatorSet.play(bomb1Y).with(bomb2Y);
        animatorSet.play(bomb1Y).with(bomb3Y);
        animatorSet.play(explode1X).after(bomb1Y);
        animatorSet.play(explode1X).with(explode1Y);
        animatorSet.play(explode1X).with(explode1Alpha);
        animatorSet.play(explode1X).with(explode2X);
        animatorSet.play(explode1X).with(explode2Y);
        animatorSet.play(explode1X).with(explode2Alpha);
        animatorSet.play(explode1X).with(explode3X);
        animatorSet.play(explode1X).with(explode3Y);
        animatorSet.play(explode1X).with(explode3Alpha);
        return animatorSet;
    }

    private static float[] getCenter(View view) {
        float[] toReturn = new float[2];
        toReturn[0] = view.getX() + view.getWidth() * 0.5f;
        toReturn[1] = view.getY() + view.getHeight() * 0.5f;
        return toReturn;
    }

    private static BaseControllerListener<ImageInfo> getController(final Context context, final SimpleDraweeView view) {
        return new BaseControllerListener<ImageInfo>() {
            @Override
            public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable anim) {
                if (imageInfo == null) {
                    return;
                }

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout
                        .LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

                if (imageInfo.getHeight() < 80) {
                    if (imageInfo.getHeight() <= 50) {
                        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                30, context.getResources().getDisplayMetrics());
                        params.setMargins(0, px, 0, px);
                        view.setLayoutParams(params);
                    } else if (imageInfo.getHeight() <= 60) {
                        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                25, context.getResources().getDisplayMetrics());
                        params.setMargins(0, px, 0, px);
                        view.setLayoutParams(params);
                    } else if (imageInfo.getHeight() <= 70) {
                        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                20, context.getResources().getDisplayMetrics());
                        params.setMargins(0, px, 0, px);
                        view.setLayoutParams(params);
                    } else if (imageInfo.getHeight() <= 80) {
                        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                15, context.getResources().getDisplayMetrics());
                        params.setMargins(0, px, 0, px);
                        view.setLayoutParams(params);
                    }
                } else {
                    params.setMargins(0, 0, 0, 0);
                    view.setLayoutParams(params);
                }
            }

            @Override
            public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {
            }

            @Override
            public void onFailure(String id, Throwable throwable) {
            }
        };
    }
}
