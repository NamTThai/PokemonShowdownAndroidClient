package com.pokemonshowdown.data;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.pokemonshowdown.app.BattleFragment;
import com.pokemonshowdown.app.R;

import java.util.Random;

public class BattleAnimation {
    public final static String BTAG = BattleAnimation.class.getName();

    public static AnimatorSet processMove(String move, View view, BattleFragment battleFragment, String[] split) {
        move = MyApplication.toId(move);
        if (view == null) {
            return null;
        }
        RelativeLayout battleWrapper = (RelativeLayout) view.findViewById(R.id.animation_layout);
        RelativeLayout atkC = (RelativeLayout) view.findViewById(battleFragment.getPkmLayoutId(split[0]));
        ImageView atk = (ImageView) view.findViewById(battleFragment.getSpriteId(split[0]));
        RelativeLayout defC = (RelativeLayout) view.findViewById(battleFragment.getPkmLayoutId(split[2]));
        ImageView def = (ImageView) view.findViewById(battleFragment.getSpriteId(split[2]));
        int[] options = {R.drawable.battle_electroball, R.drawable.battle_energyball, R.drawable.battle_mistball, R.drawable.battle_shadowball, R.drawable.battle_poisonwisp, R.drawable.battle_wisp};
        return spread(battleFragment.getActivity(), battleWrapper, atkC, atk, options[Math.abs(new Random().nextInt() % 6)]);
    }

    public static AnimatorSet attack(Context context, RelativeLayout atkC, ImageView atk, RelativeLayout defC, ImageView def) {
        int[] locAtk = new int[2];
        int[] locDef = new int[2];
        atk.getLocationOnScreen(locAtk);
        def.getLocationOnScreen(locDef);
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator attackX = ObjectAnimator.ofFloat(atk, "x", 0f);
        attackX.setDuration(BattleFragment.ANIMATION_LONG);
        ObjectAnimator attackY = ObjectAnimator.ofFloat(atk, "y", 0f);
        attackY.setDuration(BattleFragment.ANIMATION_LONG);
        animatorSet.play(attackX);
        animatorSet.play(attackY).with(attackX);
        return animatorSet;
    }

    public static AnimatorSet ball(Context context, final RelativeLayout wrapper, final RelativeLayout atkC, final ImageView atk, final RelativeLayout defC, final ImageView def, int ballId) {
        AnimatorSet animatorSet = new AnimatorSet();
        final ImageView ball = new ImageView(context);
        ball.setImageResource(ballId);
        ball.setMaxHeight(atk.getHeight());
        ball.setMaxWidth(atk.getWidth());
        ball.setScaleType(ImageView.ScaleType.CENTER);
        ball.setAdjustViewBounds(true);
        ball.setX(atkC.getX() + atk.getX());
        ball.setY(atkC.getY() + atk.getY());
        ObjectAnimator ballX = ObjectAnimator.ofFloat(ball, "x", defC.getX() + def.getX());
        ballX.setDuration(BattleFragment.ANIMATION_LONG);
        ObjectAnimator ballY = ObjectAnimator.ofFloat(ball, "y", defC.getY() + def.getY());
        ballY.setDuration(BattleFragment.ANIMATION_LONG);
        ballX.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                wrapper.addView(ball);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                wrapper.removeView(ball);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.play(ballX).with(ballY);
        return animatorSet;
    }

    public static AnimatorSet bomb(Context context, final RelativeLayout defC, final ImageView def, int bombId) {
        AnimatorSet animatorSet = new AnimatorSet();
        int maxSize = 120;
        final ImageView bomb1 = new ImageView(context);
        bomb1.setImageResource(bombId);
        bomb1.setMaxHeight(maxSize);
        bomb1.setMaxWidth(maxSize);
        bomb1.setScaleType(ImageView.ScaleType.CENTER);
        bomb1.setAdjustViewBounds(true);
        final ImageView bomb2 = new ImageView(context);
        bomb2.setImageResource(bombId);
        bomb2.setMaxHeight(maxSize);
        bomb2.setMaxWidth(maxSize);
        bomb2.setScaleType(ImageView.ScaleType.CENTER);
        bomb2.setAdjustViewBounds(true);
        bomb2.setX(defC.getWidth() / 4);
        bomb2.setY(defC.getHeight() / 4);
        final ImageView bomb3 = new ImageView(context);
        bomb3.setImageResource(bombId);
        bomb3.setMaxHeight(maxSize);
        bomb3.setMaxWidth(maxSize);
        bomb3.setScaleType(ImageView.ScaleType.CENTER);
        bomb3.setAdjustViewBounds(true);
        bomb3.setX(defC.getWidth() / 2);
        ObjectAnimator bomb1Y = ObjectAnimator.ofFloat(bomb1, "y", defC.getHeight() / 4);
        bomb1Y.setDuration(BattleFragment.ANIMATION_LONG * 2 / 3);
        ObjectAnimator bomb2Y = ObjectAnimator.ofFloat(bomb2, "y", defC.getHeight() / 2);
        bomb2Y.setDuration(BattleFragment.ANIMATION_LONG * 2 / 3);
        ObjectAnimator bomb3Y = ObjectAnimator.ofFloat(bomb3, "y", defC.getHeight() / 4);
        bomb3Y.setDuration(BattleFragment.ANIMATION_LONG * 2 / 3);
        bomb1Y.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                defC.addView(bomb1);
                defC.addView(bomb2);
                defC.addView(bomb3);
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
        explode3Alpha.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                defC.removeView(bomb1);
                defC.removeView(bomb2);
                defC.removeView(bomb3);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

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

    public static AnimatorSet charge(Context context, RelativeLayout atkC, final ImageView atk, RelativeLayout defC, ImageView def, String[] split) {
        if (split.length >= 4 && split[3].equals("[still]")) {
            AnimatorSet animatorSet = new AnimatorSet();
            ObjectAnimator alpha = ObjectAnimator.ofFloat(atk, "alpha", 0.3f);
            alpha.setDuration(BattleFragment.ANIMATION_LONG);
            return animatorSet;
        } else {
            AnimatorSet animatorSet = flight(context, atkC, atk, defC);
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    atk.setAlpha(1f);
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
            return animatorSet;
        }
    }

    public static AnimatorSet contact(Context context, final RelativeLayout wrapper, final RelativeLayout atkC, final ImageView atk, final RelativeLayout defC, final ImageView def, int contactType) {
        AnimatorSet animatorSet = new AnimatorSet();
        final ImageView atkPkm = new ImageView(context);
        atkPkm.setImageDrawable(atk.getDrawable());
        atkPkm.setX(atkC.getX() + atk.getX());
        atkPkm.setY(atkC.getY() + atk.getY());
        final ViewGroup.LayoutParams imageParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ObjectAnimator attackX = ObjectAnimator.ofFloat(atkPkm, "x", (defC.getX() + def.getX()));
        attackX.setDuration(BattleFragment.ANIMATION_LONG / 3);
        attackX.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator attackY = ObjectAnimator.ofFloat(atkPkm, "y", (defC.getY() + def.getY()));
        attackY.setDuration(BattleFragment.ANIMATION_LONG / 3);
        attackY.setInterpolator(new AccelerateInterpolator());
        attackX.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                atk.setAlpha(0f);
                wrapper.addView(atkPkm, imageParams);
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
        final ImageView contact = new ImageView(context);
        contact.setMaxWidth(100);
        contact.setMaxHeight(100);
        contact.setImageResource(contactType);
        final float endX = defC.getX() + def.getX() + def.getWidth() * 0.5f - 50;
        final float endY = defC.getY() + def.getY() + def.getHeight() * 0.5f - 50;
        ObjectAnimator contactScaleX = ObjectAnimator.ofFloat(contact, "scaleX", 1.5f);
        contactScaleX.setDuration(BattleFragment.ANIMATION_LONG * 2 / 3);
        ObjectAnimator contactScaleY = ObjectAnimator.ofFloat(contact, "scaleY", 1.5f);
        contactScaleY.setDuration(BattleFragment.ANIMATION_LONG * 2 / 3);
        ObjectAnimator contactAlpha = ObjectAnimator.ofFloat(contact, "alpha", 0f);
        contactAlpha.setDuration(BattleFragment.ANIMATION_LONG * 2 / 3);
        contactAlpha.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator defendX = ObjectAnimator.ofFloat(atkPkm, "x", (atkC.getX() + atk.getX()));
        defendX.setDuration(BattleFragment.ANIMATION_LONG * 2 / 3);
        ObjectAnimator defendY = ObjectAnimator.ofFloat(atkPkm, "y", (atkC.getY() + atk.getY()));
        defendY.setDuration(BattleFragment.ANIMATION_LONG * 2 / 3);
        contactScaleX.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                defC.addView(contact, imageParams);
                contact.setX(endX);
                contact.setY(endY);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                defC.removeView(contact);
                wrapper.removeView(atkPkm);
                atk.setAlpha(1f);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

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

    public static AnimatorSet dance(Context context, RelativeLayout atkC, ImageView atk) {
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

    public static AnimatorSet drain(Context context, final RelativeLayout atkC, final ImageView atk, final RelativeLayout defC, final ImageView def) {
        AnimatorSet animatorSet = new AnimatorSet();
        final ImageView attack = new ImageView(context);
        attack.setImageResource(R.drawable.battle_energyball);
        attack.setMaxHeight(40);
        attack.setMaxWidth(40);
        attack.setScaleType(ImageView.ScaleType.CENTER);
        attack.setAdjustViewBounds(true);
        attack.setX(atk.getX() + atk.getWidth() * 0.5f - 20);
        attack.setY(atk.getY() + atk.getHeight() * 0.5f - 20);
        final ViewGroup.LayoutParams imageParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(attack, "scaleX", 3f);
        scaleX.setDuration(BattleFragment.ANIMATION_LONG / 2);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(attack, "scaleY", 3f);
        scaleY.setDuration(BattleFragment.ANIMATION_LONG / 2);
        scaleX.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                defC.addView(attack, imageParams);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                defC.removeView(attack);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        final ImageView drain = new ImageView(context);
        drain.setImageResource(R.drawable.battle_energyball);
        drain.setMaxHeight(atk.getHeight());
        drain.setMaxWidth(atk.getWidth());
        drain.setScaleType(ImageView.ScaleType.CENTER);
        drain.setAdjustViewBounds(true);
        drain.setX(atk.getX());
        drain.setY(atk.getY());
        ObjectAnimator drainX = ObjectAnimator.ofFloat(drain, "scaleX", 0.2f);
        drainX.setDuration(BattleFragment.ANIMATION_LONG / 2);
        ObjectAnimator drainY = ObjectAnimator.ofFloat(drain, "scaleY", 0.2f);
        drainY.setDuration(BattleFragment.ANIMATION_LONG / 2);
        drainX.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                atkC.addView(drain, imageParams);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                atkC.removeView(drain);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.play(scaleX).with(scaleY);
        animatorSet.play(drainX).after(scaleX);
        animatorSet.play(drainX).with(drainY);
        return animatorSet;
    }

    public static AnimatorSet earth(final RelativeLayout defC, final RelativeLayout def) {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator earth1 = ObjectAnimator.ofFloat(def, "x", 0f);
        earth1.setDuration(BattleFragment.ANIMATION_LONG / 6);
        ObjectAnimator earth2 = ObjectAnimator.ofFloat(def, "x", defC.getWidth() - def.getWidth());
        earth1.setDuration(BattleFragment.ANIMATION_LONG / 6);
        ObjectAnimator earth3 = ObjectAnimator.ofFloat(def, "x", 0f);
        earth1.setDuration(BattleFragment.ANIMATION_LONG / 6);
        ObjectAnimator earth4 = ObjectAnimator.ofFloat(def, "x", defC.getWidth() - def.getWidth());
        earth1.setDuration(BattleFragment.ANIMATION_LONG / 6);
        ObjectAnimator earth5 = ObjectAnimator.ofFloat(def, "x", 0f);
        earth1.setDuration(BattleFragment.ANIMATION_LONG / 6);
        ObjectAnimator earth6 = ObjectAnimator.ofFloat(def, "x", def.getX());
        earth1.setDuration(BattleFragment.ANIMATION_LONG / 6);
        animatorSet.play(earth1);
        animatorSet.play(earth2).after(earth1);
        animatorSet.play(earth3).after(earth2);
        animatorSet.play(earth4).after(earth3);
        animatorSet.play(earth5).after(earth4);
        animatorSet.play(earth6).after(earth5);
        return animatorSet;
    }

    public static AnimatorSet fast(Context context, final RelativeLayout wrapper, final RelativeLayout atkC, final ImageView atk, final RelativeLayout defC, final ImageView def) {
        AnimatorSet animatorSet = new AnimatorSet();
        final ImageView fast = new ImageView(context);
        fast.setImageDrawable(atk.getDrawable());
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
        attackX.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                wrapper.addView(fast, params);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                wrapper.removeView(fast);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        ObjectAnimator appear = ObjectAnimator.ofFloat(atk, "alpha", 1f);
        appear.setDuration(BattleFragment.ANIMATION_LONG / 3);
        animatorSet.play(disappear).before(attackX);
        animatorSet.play(attackX).with(attackY);
        animatorSet.play(appear).after(attackX);
        return animatorSet;
    }

    public static AnimatorSet flight(Context context, final RelativeLayout atkC, final ImageView atk, final RelativeLayout defC) {
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
        flightClawRight.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                defC.addView(leftClaw, imageParams);
                leftClaw.setX(0f);
                leftClaw.setY(0f);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                defC.removeView(leftClaw);
                atk.setX(atkC.getWidth() - atk.getWidth());
                atk.setY(atkC.getHeight() - atk.getHeight());
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        ObjectAnimator flightMiddleLeft = ObjectAnimator.ofFloat(atk, "x", atk.getX());
        flightMiddleLeft.setDuration(BattleFragment.ANIMATION_LONG / 4);
        ObjectAnimator flightMiddleTop = ObjectAnimator.ofFloat(atk, "y", atk.getY());
        flightMiddleTop.setDuration(BattleFragment.ANIMATION_LONG / 4);
        ObjectAnimator flightMiddleAlpha = ObjectAnimator.ofFloat(atk, "alpha", 1f);
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
    
    public static AnimatorSet phaze(final RelativeLayout def) {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator spin = ObjectAnimator.ofFloat(def, "rotation", 1080f);
        spin.setDuration(BattleFragment.ANIMATION_LONG);
        spin.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.play(spin);
        return animatorSet;
    }

    public static AnimatorSet shake(RelativeLayout atkC, ImageView atk) {
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

    public static AnimatorSet self(Context context, final RelativeLayout atkC, final ImageView atk) {
        AnimatorSet animatorSet = new AnimatorSet();
        final ImageView flash = new ImageView(context);
        flash.setImageResource(R.drawable.battle_wisp);
        flash.setMaxHeight(80);
        flash.setMaxWidth(80);
        flash.setScaleType(ImageView.ScaleType.CENTER);
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
        flash1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                atkC.addView(flash, imageParams);
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
        flash3.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                atkC.removeView(flash);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.play(flash1).before(flash2);
        animatorSet.play(flash2).before(flash3);
        return animatorSet;
    }

    public static AnimatorSet selfLight(Context context, final RelativeLayout atkC, final ImageView atk) {
        AnimatorSet animatorSet = new AnimatorSet();
        final ImageView flash = new ImageView(context);
        flash.setImageResource(R.drawable.battle_electroball);
        flash.setMaxHeight(atk.getHeight());
        flash.setMaxWidth(atk.getWidth());
        flash.setScaleType(ImageView.ScaleType.CENTER);
        flash.setAdjustViewBounds(true);
        flash.setX(atk.getX());
        flash.setY(atk.getY());
        final ViewGroup.LayoutParams imageParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(flash, "scaleX", 0.2f);
        scaleX.setDuration(BattleFragment.ANIMATION_LONG);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(flash, "scaleY", 0.2f);
        scaleY.setDuration(BattleFragment.ANIMATION_LONG);
        scaleX.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                atkC.addView(flash, imageParams);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                atkC.removeView(flash);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.play(scaleX).with(scaleY);
        return animatorSet;
    }

    public static AnimatorSet selfDark(Context context, final RelativeLayout atkC, final ImageView atk) {
        AnimatorSet animatorSet = new AnimatorSet();
        final ImageView flash = new ImageView(context);
        flash.setImageResource(R.drawable.battle_shadowball);
        flash.setMaxHeight(atk.getHeight());
        flash.setMaxWidth(atk.getWidth());
        flash.setScaleType(ImageView.ScaleType.CENTER);
        flash.setAdjustViewBounds(true);
        flash.setX(atk.getX());
        flash.setY(atk.getY());
        final ViewGroup.LayoutParams imageParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(flash, "scaleX", 0.2f);
        scaleX.setDuration(BattleFragment.ANIMATION_LONG);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(flash, "scaleY", 0.2f);
        scaleY.setDuration(BattleFragment.ANIMATION_LONG);
        scaleX.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                atkC.addView(flash, imageParams);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                atkC.removeView(flash);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.play(scaleX).with(scaleY);
        return animatorSet;
    }

    public static AnimatorSet slash(Context context, final RelativeLayout defC) {
        AnimatorSet animatorSet = new AnimatorSet();
        final ImageView slash = new ImageView(context);
        slash.setImageResource(R.drawable.battle_leftclaw);
        ObjectAnimator slashX = ObjectAnimator.ofFloat(slash, "x", defC.getWidth());
        slashX.setDuration(BattleFragment.ANIMATION_LONG);
        slashX.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator slashY = ObjectAnimator.ofFloat(slash, "y", defC.getHeight());
        slashY.setDuration(BattleFragment.ANIMATION_LONG);
        slashX.setInterpolator(new AccelerateInterpolator());
        slashX.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                defC.addView(slash);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                defC.removeView(slash);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.play(slashX).with(slashY);
        return animatorSet;
    }

    public static AnimatorSet spinAtk(Context context, final RelativeLayout atkC, final ImageView atk, final RelativeLayout defC) {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator flightLeft = ObjectAnimator.ofFloat(atk, "x", 0f);
        flightLeft.setDuration(BattleFragment.ANIMATION_LONG / 4);
        ObjectAnimator flightTop = ObjectAnimator.ofFloat(atk, "y", 0f);
        flightTop.setDuration(BattleFragment.ANIMATION_LONG / 4);
        ObjectAnimator flightAlpha = ObjectAnimator.ofFloat(atk, "alpha", 0f);
        flightAlpha.setDuration(BattleFragment.ANIMATION_LONG / 4);
        final ImageView spin = new ImageView(context);
        spin.setImageDrawable(atk.getDrawable());
        spin.setX(0);
        spin.setY(0);
        final ViewGroup.LayoutParams imageParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ObjectAnimator spinRight = ObjectAnimator.ofFloat(spin, "x", defC.getWidth());
        spinRight.setDuration(BattleFragment.ANIMATION_LONG / 2);
        ObjectAnimator spinBottom = ObjectAnimator.ofFloat(spin, "y", defC.getHeight());
        spinBottom.setDuration(BattleFragment.ANIMATION_LONG / 2);
        ObjectAnimator spinRotation = ObjectAnimator.ofFloat(spin, "rotation", 720);
        spinRotation.setDuration(BattleFragment.ANIMATION_LONG / 2);
        spinRight.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                defC.addView(spin, imageParams);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                defC.removeView(spin);
                atk.setX(atkC.getWidth() - atk.getWidth());
                atk.setY(atkC.getHeight() - atk.getHeight());
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        ObjectAnimator flightMiddleLeft = ObjectAnimator.ofFloat(atk, "x", atk.getX());
        flightMiddleLeft.setDuration(BattleFragment.ANIMATION_LONG / 4);
        ObjectAnimator flightMiddleTop = ObjectAnimator.ofFloat(atk, "y", atk.getY());
        flightMiddleTop.setDuration(BattleFragment.ANIMATION_LONG / 4);
        ObjectAnimator flightMiddleAlpha = ObjectAnimator.ofFloat(atk, "alpha", 1f);
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

    public static AnimatorSet spread(Context context, final RelativeLayout wrapper, final RelativeLayout atkC, final ImageView atk, int spreadId) {
        AnimatorSet animatorSet = new AnimatorSet();
        final ImageView flash1 = new ImageView(context);
        flash1.setImageResource(spreadId);
        flash1.setMaxHeight(100);
        flash1.setMaxWidth(100);
        flash1.setScaleType(ImageView.ScaleType.CENTER);
        flash1.setAdjustViewBounds(true);
        float startX = atkC.getX() + atk.getX() + atk.getWidth() * 0.5f - 50;
        float startY = atkC.getY() + atk.getY() + atk.getHeight() * 0.5f - 50;
        flash1.setX(startX);
        flash1.setY(startY);
        final ImageView flash2 = new ImageView(context);
        flash2.setImageResource(spreadId);
        flash2.setMaxHeight(100);
        flash2.setMaxWidth(100);
        flash2.setScaleType(ImageView.ScaleType.CENTER);
        flash2.setAdjustViewBounds(true);
        flash2.setX(startX);
        flash2.setY(startY);
        final ImageView flash3 = new ImageView(context);
        flash3.setImageResource(spreadId);
        flash3.setMaxHeight(100);
        flash3.setMaxWidth(100);
        flash3.setScaleType(ImageView.ScaleType.CENTER);
        flash3.setAdjustViewBounds(true);
        flash3.setX(startX);
        flash3.setY(startY);
        final ImageView flash4 = new ImageView(context);
        flash4.setImageResource(spreadId);
        flash4.setMaxHeight(100);
        flash4.setMaxWidth(100);
        flash4.setScaleType(ImageView.ScaleType.CENTER);
        flash4.setAdjustViewBounds(true);
        flash4.setX(startX);
        flash4.setY(startY);
        final ImageView flash5 = new ImageView(context);
        flash5.setImageResource(spreadId);
        flash5.setMaxHeight(100);
        flash5.setMaxWidth(100);
        flash5.setScaleType(ImageView.ScaleType.CENTER);
        flash5.setAdjustViewBounds(true);
        flash5.setX(startX);
        flash5.setY(startY);
        final ImageView flash6 = new ImageView(context);
        flash6.setImageResource(spreadId);
        flash6.setMaxHeight(100);
        flash6.setMaxWidth(100);
        flash6.setScaleType(ImageView.ScaleType.CENTER);
        flash6.setAdjustViewBounds(true);
        flash6.setX(startX);
        flash6.setY(startY);
        final ViewGroup.LayoutParams imageParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ObjectAnimator flash1FlyX = ObjectAnimator.ofFloat(flash1, "x", flash1.getX() - 500);
        flash1FlyX.setDuration(BattleFragment.ANIMATION_LONG);
        flash1FlyX.setInterpolator(new AccelerateDecelerateInterpolator());
        ObjectAnimator flash2FlyX = ObjectAnimator.ofFloat(flash2, "x", flash2.getX() - 300);
        flash2FlyX.setDuration(BattleFragment.ANIMATION_LONG);
        flash2FlyX.setInterpolator(new AccelerateDecelerateInterpolator());
        ObjectAnimator flash2FlyY = ObjectAnimator.ofFloat(flash2, "y", flash2.getY() - 100);
        flash2FlyY.setDuration(BattleFragment.ANIMATION_LONG);
        flash2FlyY.setInterpolator(new AccelerateDecelerateInterpolator());
        ObjectAnimator flash3FlyX = ObjectAnimator.ofFloat(flash3, "x", flash3.getX() + 400);
        flash3FlyX.setDuration(BattleFragment.ANIMATION_LONG);
        flash3FlyX.setInterpolator(new AccelerateDecelerateInterpolator());
        ObjectAnimator flash3FlyY = ObjectAnimator.ofFloat(flash3, "y", flash2.getY() - 100);
        flash3FlyY.setDuration(BattleFragment.ANIMATION_LONG);
        flash3FlyY.setInterpolator(new AccelerateDecelerateInterpolator());
        ObjectAnimator flash4FlyX = ObjectAnimator.ofFloat(flash4, "x", flash4.getX() + 500);
        flash4FlyX.setDuration(BattleFragment.ANIMATION_LONG);
        flash4FlyX.setInterpolator(new AccelerateDecelerateInterpolator());
        ObjectAnimator flash5FlyX = ObjectAnimator.ofFloat(flash5, "x", flash5.getX() + 400);
        flash5FlyX.setDuration(BattleFragment.ANIMATION_LONG);
        flash5FlyX.setInterpolator(new AccelerateDecelerateInterpolator());
        ObjectAnimator flash5FlyY = ObjectAnimator.ofFloat(flash5, "y", flash5.getY() + 100);
        flash5FlyY.setDuration(BattleFragment.ANIMATION_LONG);
        flash5FlyY.setInterpolator(new AccelerateDecelerateInterpolator());
        ObjectAnimator flash6FlyX = ObjectAnimator.ofFloat(flash6, "x", flash6.getX() - 400);
        flash6FlyX.setDuration(BattleFragment.ANIMATION_LONG);
        flash6FlyX.setInterpolator(new AccelerateDecelerateInterpolator());
        ObjectAnimator flash6FlyY = ObjectAnimator.ofFloat(flash6, "y", flash6.getY() + 100);
        flash6FlyY.setDuration(BattleFragment.ANIMATION_LONG);
        flash6FlyY.setInterpolator(new AccelerateDecelerateInterpolator());
        flash1FlyX.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                wrapper.addView(flash1, imageParams);
                wrapper.addView(flash2, imageParams);
                wrapper.addView(flash3, imageParams);
                wrapper.addView(flash4, imageParams);
                wrapper.addView(flash5, imageParams);
                wrapper.addView(flash6, imageParams);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                wrapper.removeView(flash1);
                wrapper.removeView(flash2);
                wrapper.removeView(flash3);
                wrapper.removeView(flash4);
                wrapper.removeView(flash5);
                wrapper.removeView(flash6);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

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

    public static AnimatorSet status(Context context, final RelativeLayout defC, final ImageView def, int statusId) {
        AnimatorSet animatorSet = new AnimatorSet();
        final ImageView status = new ImageView(context);
        status.setImageResource(statusId);
        status.setMaxHeight(100);
        status.setMaxWidth(100);
        status.setScaleType(ImageView.ScaleType.CENTER);
        status.setAdjustViewBounds(true);
        status.setX(def.getX() + def.getWidth() * 0.5f);
        status.setY(def.getY() + def.getHeight() * 0.5f);
        ObjectAnimator circlingX = ObjectAnimator.ofFloat(status, "x", status.getX(), 0f, status.getX(), defC.getWidth() - def.getWidth(), status.getX());
        circlingX.setDuration(BattleFragment.ANIMATION_LONG);
        ObjectAnimator circlingY = ObjectAnimator.ofFloat(status, "y", status.getY(), status.getY() - 30f, status.getY() - 60f, status.getY() - 30f, status.getY());
        circlingY.setDuration(BattleFragment.ANIMATION_LONG);
        circlingX.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                defC.addView(status);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                defC.removeView(status);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.play(circlingX).with(circlingY);
        return animatorSet;
    }

    public static AnimatorSet stream(Context context, final RelativeLayout wrapper, final RelativeLayout atkC, final ImageView atk, final RelativeLayout defC, final ImageView def, int spreadId) {
        AnimatorSet animatorSet = new AnimatorSet();
        int initialSize = 40;
        final ImageView flash1 = new ImageView(context);
        flash1.setImageResource(spreadId);
        flash1.setMaxHeight(initialSize);
        flash1.setMaxWidth(initialSize);
        flash1.setScaleType(ImageView.ScaleType.CENTER);
        flash1.setAdjustViewBounds(true);
        float startX = atkC.getX() + atk.getX() + atk.getWidth() * 0.5f;
        float startY = atkC.getY() + atk.getY() + atk.getHeight() * 0.5f;
        flash1.setX(startX);
        flash1.setY(startY);
        final ImageView flash2 = new ImageView(context);
        flash2.setImageResource(spreadId);
        flash2.setMaxHeight(initialSize);
        flash2.setMaxWidth(initialSize);
        flash2.setScaleType(ImageView.ScaleType.CENTER);
        flash2.setAdjustViewBounds(true);
        flash2.setX(startX);
        flash2.setY(startY);
        final ImageView flash3 = new ImageView(context);
        flash3.setImageResource(spreadId);
        flash3.setMaxHeight(initialSize);
        flash3.setMaxWidth(initialSize);
        flash3.setScaleType(ImageView.ScaleType.CENTER);
        flash3.setAdjustViewBounds(true);
        flash3.setX(startX);
        flash3.setY(startY);
        final ImageView flash4 = new ImageView(context);
        flash4.setImageResource(spreadId);
        flash4.setMaxHeight(initialSize);
        flash4.setMaxWidth(initialSize);
        flash4.setScaleType(ImageView.ScaleType.CENTER);
        flash4.setAdjustViewBounds(true);
        flash4.setX(startX);
        flash4.setY(startY);
        final ImageView flash5 = new ImageView(context);
        flash5.setImageResource(spreadId);
        flash5.setMaxHeight(initialSize);
        flash5.setMaxWidth(initialSize);
        flash5.setScaleType(ImageView.ScaleType.CENTER);
        flash5.setAdjustViewBounds(true);
        flash5.setX(startX);
        flash5.setY(startY);
        final ImageView flash6 = new ImageView(context);
        flash6.setImageResource(spreadId);
        flash6.setMaxHeight(initialSize);
        flash6.setMaxWidth(initialSize);
        flash6.setScaleType(ImageView.ScaleType.CENTER);
        flash6.setAdjustViewBounds(true);
        flash6.setX(startX);
        flash6.setY(startY);
        final ViewGroup.LayoutParams imageParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        float scale = 1.5f;
        ObjectAnimator flash1FlyX = ObjectAnimator.ofFloat(flash1, "x", (defC.getX() + def.getX()));
        flash1FlyX.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        flash1FlyX.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator flash1FlyY = ObjectAnimator.ofFloat(flash1, "y", (defC.getY() + def.getY()));
        flash1FlyY.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        flash1FlyY.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator flash1SizeX = ObjectAnimator.ofFloat(flash1, "scaleX", scale);
        flash1SizeX.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        ObjectAnimator flash1SizeY = ObjectAnimator.ofFloat(flash1, "scaleY", scale);
        flash1SizeY.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        ObjectAnimator flash2FlyX = ObjectAnimator.ofFloat(flash2, "x", (defC.getX() + def.getX()));
        flash2FlyX.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        flash2FlyX.setInterpolator(new AccelerateInterpolator());
        flash2FlyX.setStartDelay(BattleFragment.ANIMATION_LONG * (1 / 20));
        ObjectAnimator flash2FlyY = ObjectAnimator.ofFloat(flash2, "y", (defC.getY() + def.getY()));
        flash2FlyY.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        flash2FlyY.setInterpolator(new AccelerateInterpolator());
        flash2FlyY.setStartDelay(BattleFragment.ANIMATION_LONG * (1 / 20));
        ObjectAnimator flash2SizeX = ObjectAnimator.ofFloat(flash2, "scaleX", scale);
        flash2SizeX.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        flash2SizeX.setStartDelay(BattleFragment.ANIMATION_LONG * (1 / 20));
        ObjectAnimator flash2SizeY = ObjectAnimator.ofFloat(flash2, "scaleY", scale);
        flash2SizeY.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        flash2SizeY.setStartDelay(BattleFragment.ANIMATION_LONG * (1 / 20));
        ObjectAnimator flash3FlyX = ObjectAnimator.ofFloat(flash3, "x", (defC.getX() + def.getX()));
        flash3FlyX.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        flash3FlyX.setInterpolator(new AccelerateInterpolator());
        flash3FlyX.setStartDelay(BattleFragment.ANIMATION_LONG * (2 / 20));
        ObjectAnimator flash3FlyY = ObjectAnimator.ofFloat(flash3, "y", (defC.getY() + def.getY()));
        flash3FlyY.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        flash3FlyY.setInterpolator(new AccelerateInterpolator());
        flash3FlyY.setStartDelay(BattleFragment.ANIMATION_LONG * (2 / 20));
        ObjectAnimator flash3SizeX = ObjectAnimator.ofFloat(flash3, "scaleX", scale);
        flash3SizeX.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        flash3SizeX.setStartDelay(BattleFragment.ANIMATION_LONG * (2 / 20));
        ObjectAnimator flash3SizeY = ObjectAnimator.ofFloat(flash3, "scaleY", scale);
        flash3SizeY.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        flash3SizeY.setStartDelay(BattleFragment.ANIMATION_LONG * (2 / 20));
        ObjectAnimator flash4FlyX = ObjectAnimator.ofFloat(flash4, "x", (defC.getX() + def.getX()));
        flash4FlyX.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        flash4FlyX.setInterpolator(new AccelerateInterpolator());
        flash4FlyX.setStartDelay(BattleFragment.ANIMATION_LONG * (3 / 30));
        ObjectAnimator flash4FlyY = ObjectAnimator.ofFloat(flash4, "y", (defC.getY() + def.getY()));
        flash4FlyY.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        flash4FlyY.setInterpolator(new AccelerateInterpolator());
        flash4FlyY.setStartDelay(BattleFragment.ANIMATION_LONG * (3 / 30));
        ObjectAnimator flash4SizeX = ObjectAnimator.ofFloat(flash4, "scaleX", scale);
        flash4SizeX.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        flash4SizeX.setStartDelay(BattleFragment.ANIMATION_LONG * (3 / 30));
        ObjectAnimator flash4SizeY = ObjectAnimator.ofFloat(flash4, "scaleY", scale);
        flash4SizeY.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        flash4SizeY.setStartDelay(BattleFragment.ANIMATION_LONG * (3 / 30));
        ObjectAnimator flash5FlyX = ObjectAnimator.ofFloat(flash5, "x", (defC.getX() + def.getX()));
        flash5FlyX.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        flash5FlyX.setInterpolator(new AccelerateInterpolator());
        flash5FlyX.setStartDelay(BattleFragment.ANIMATION_LONG * (4 / 20));
        ObjectAnimator flash5FlyY = ObjectAnimator.ofFloat(flash5, "y", (defC.getY() + def.getY()));
        flash5FlyY.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        flash5FlyY.setInterpolator(new AccelerateInterpolator());
        flash5FlyY.setStartDelay(BattleFragment.ANIMATION_LONG * (4 / 20));
        ObjectAnimator flash5SizeX = ObjectAnimator.ofFloat(flash5, "scaleX", scale);
        flash5SizeX.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        flash5SizeX.setStartDelay(BattleFragment.ANIMATION_LONG * (4 / 20));
        ObjectAnimator flash5SizeY = ObjectAnimator.ofFloat(flash5, "scaleY", scale);
        flash5SizeY.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        flash5SizeY.setStartDelay(BattleFragment.ANIMATION_LONG * (4 / 20));
        ObjectAnimator flash6FlyX = ObjectAnimator.ofFloat(flash6, "x", (defC.getX() + def.getX()));
        flash6FlyX.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        flash6FlyX.setInterpolator(new AccelerateInterpolator());
        flash6FlyX.setStartDelay(BattleFragment.ANIMATION_LONG * (5 / 20));
        ObjectAnimator flash6FlyY = ObjectAnimator.ofFloat(flash6, "y", (defC.getY() + def.getY()));
        flash6FlyY.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        flash6FlyY.setInterpolator(new AccelerateInterpolator());
        flash6FlyY.setStartDelay(BattleFragment.ANIMATION_LONG * (5 / 20));
        ObjectAnimator flash6SizeX = ObjectAnimator.ofFloat(flash6, "scaleX", scale);
        flash6SizeX.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        flash6SizeX.setStartDelay(BattleFragment.ANIMATION_LONG * (5 / 20));
        ObjectAnimator flash6SizeY = ObjectAnimator.ofFloat(flash6, "scaleY", scale);
        flash6SizeY.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        flash6SizeY.setStartDelay(BattleFragment.ANIMATION_LONG * (5 / 20));
        flash1FlyX.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                wrapper.addView(flash1, imageParams);
                wrapper.addView(flash2, imageParams);
                wrapper.addView(flash3, imageParams);
                wrapper.addView(flash4, imageParams);
                wrapper.addView(flash5, imageParams);
                wrapper.addView(flash6, imageParams);
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
        flash6FlyX.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                wrapper.addView(flash1, imageParams);
                wrapper.addView(flash2, imageParams);
                wrapper.addView(flash3, imageParams);
                wrapper.addView(flash4, imageParams);
                wrapper.addView(flash5, imageParams);
                wrapper.addView(flash6, imageParams);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.play(flash1FlyX).with(flash1FlyY);
        animatorSet.play(flash1FlyX).with(flash1SizeX);
        animatorSet.play(flash1FlyX).with(flash1SizeY);
        animatorSet.play(flash1FlyX).with(flash2FlyX);
        animatorSet.play(flash1FlyX).with(flash2FlyY);
        animatorSet.play(flash1FlyX).with(flash2SizeX);
        animatorSet.play(flash1FlyX).with(flash2SizeY);
        animatorSet.play(flash1FlyX).with(flash3FlyX);
        animatorSet.play(flash1FlyX).with(flash3FlyY);
        animatorSet.play(flash1FlyX).with(flash3SizeX);
        animatorSet.play(flash1FlyX).with(flash3SizeY);
        animatorSet.play(flash1FlyX).with(flash4FlyX);
        animatorSet.play(flash1FlyX).with(flash4FlyY);
        animatorSet.play(flash1FlyX).with(flash4SizeX);
        animatorSet.play(flash1FlyX).with(flash4SizeY);
        animatorSet.play(flash1FlyX).with(flash5FlyX);
        animatorSet.play(flash1FlyX).with(flash5FlyY);
        animatorSet.play(flash1FlyX).with(flash5SizeX);
        animatorSet.play(flash1FlyX).with(flash5SizeY);
        animatorSet.play(flash1FlyX).with(flash6FlyX);
        animatorSet.play(flash1FlyX).with(flash6FlyY);
        animatorSet.play(flash1FlyX).with(flash6SizeX);
        animatorSet.play(flash1FlyX).with(flash6SizeY);
        return animatorSet;
    }
    
    public static AnimatorSet thunderNeutral(Context context, final RelativeLayout defC, final ImageView def){
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
        thunder2.setX(def.getX());
        final ImageView thunder3 = new ImageView(context);
        thunder3.setImageResource(R.drawable.battle_lightning);
        thunder3.setMaxHeight(def.getHeight());
        thunder3.setAdjustViewBounds(true);
        thunder3.setX(def.getX() + def.getWidth());
        ObjectAnimator drop1 = ObjectAnimator.ofFloat(thunder1, "y", defC.getY());
        drop1.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        ObjectAnimator drop2 = ObjectAnimator.ofFloat(thunder2, "y", defC.getY());
        drop2.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        drop2.setStartDelay(BattleFragment.ANIMATION_LONG / 8);
        ObjectAnimator drop3 = ObjectAnimator.ofFloat(thunder3, "y", defC.getY());
        drop3.setDuration(BattleFragment.ANIMATION_LONG * 3 / 4);
        drop3.setStartDelay(BattleFragment.ANIMATION_LONG / 4);
        drop1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                defC.addView(thunder1, params);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                defC.removeView(thunder1);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        drop2.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                defC.addView(thunder2, params);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                defC.removeView(thunder2);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        drop3.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                defC.addView(thunder3, params);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                defC.removeView(thunder3);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.play(drop1).with(drop2);
        animatorSet.play(drop1).with(drop3);
        return animatorSet;
    }

    public static AnimatorSet thunderStrong(Context context, final RelativeLayout defC, final ImageView def){
        AnimatorSet animatorSet = new AnimatorSet();
        final ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final ImageView thunder1 = new ImageView(context);
        thunder1.setImageResource(R.drawable.battle_lightning);
        thunder1.setMaxHeight(def.getHeight());
        thunder1.setAdjustViewBounds(true);
        thunder1.setX(def.getX() + def.getWidth() * 0.5f);
        ObjectAnimator flash1 = ObjectAnimator.ofFloat(thunder1, "alpha", 0f, 1f);
        flash1.setDuration(BattleFragment.ANIMATION_LONG / 3);
        ObjectAnimator flash2 = ObjectAnimator.ofFloat(thunder1, "alpha", 0f, 1f);
        flash2.setDuration(BattleFragment.ANIMATION_LONG / 3);
        ObjectAnimator flash3 = ObjectAnimator.ofFloat(thunder1, "alpha", 0f, 1f);
        flash3.setDuration(BattleFragment.ANIMATION_LONG / 3);
        flash1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                defC.addView(thunder1, params);
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
        flash3.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                defC.removeView(thunder1);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.play(flash1);
        animatorSet.play(flash2).after(flash1);
        animatorSet.play(flash3).after(flash2);
        return animatorSet;
    }

    public static AnimatorSet thunderWeak(Context context, final RelativeLayout atkC, final ImageView atk, final RelativeLayout defC, final ImageView def) {
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
        ObjectAnimator glowX = ObjectAnimator.ofFloat(glow, "scaleX", 4f);
        glowX.setDuration(BattleFragment.ANIMATION_LONG / 2);
        ObjectAnimator glowY = ObjectAnimator.ofFloat(glow, "scaleY", 4f);
        glowY.setDuration(BattleFragment.ANIMATION_LONG / 2);
        glowX.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                atkC.addView(glow, params);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                atkC.removeView(glow);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        ObjectAnimator drop = ObjectAnimator.ofFloat(thunder, "y", defC.getHeight());
        drop.setDuration(BattleFragment.ANIMATION_LONG / 2);
        drop.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                defC.addView(thunder, params);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                defC.removeView(thunder);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.play(glowX).with(glowY);
        animatorSet.play(drop).after(glowX);
        return animatorSet;
    }

    public static AnimatorSet trick(Context context, final RelativeLayout wrapper, final RelativeLayout atkC, final ImageView atk, final RelativeLayout defC, ImageView def) {
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
        goX.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                wrapper.addView(trick, layoutParams);
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
        ObjectAnimator backX = ObjectAnimator.ofFloat(trick, "x", startX);
        backX.setDuration(BattleFragment.ANIMATION_LONG / 2);
        ObjectAnimator backY = ObjectAnimator.ofFloat(trick, "y", startY);
        backY.setDuration(BattleFragment.ANIMATION_LONG / 2);
        backX.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                wrapper.removeView(trick);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.play(goX).with(goY);
        animatorSet.play(backX).after(goX);
        animatorSet.play(backX).with(backY);
        return animatorSet;
    }

    public static AnimatorSet wish(Context context, final RelativeLayout atkC, final ImageView atk) {
        AnimatorSet animatorSet = new AnimatorSet();
        final ImageView wish = new ImageView(context);
        wish.setImageResource(R.drawable.battle_wisp);
        wish.setMaxWidth(60);
        wish.setMaxHeight(60);
        wish.setScaleType(ImageView.ScaleType.CENTER);
        wish.setAdjustViewBounds(true);
        wish.setX(atk.getX() + atk.getWidth() * 0.5f);
        wish.setY(atk.getY() + atk.getHeight() * 0.5f);
        ObjectAnimator wishY = ObjectAnimator.ofFloat(wish, "y", 0f);
        wishY.setDuration(BattleFragment.ANIMATION_LONG);
        wishY.setInterpolator(new AccelerateInterpolator());
        wishY.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                atkC.addView(wish);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                atkC.removeView(wish);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.play(wishY);
        return animatorSet;
    }

    public static AnimatorSet xatk(Context context, final RelativeLayout atkC, final ImageView atk, final RelativeLayout defC) {
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
        flightClawRight.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                defC.addView(leftClaw, imageParams);
                leftClaw.setX(0f);
                leftClaw.setY(0f);
                defC.addView(rightClaw, imageParams);
                rightClaw.setX(defC.getWidth() - rightClaw.getWidth());
                rightClaw.setY(0f);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                defC.removeView(leftClaw);
                defC.removeView(rightClaw);
                atk.setX(atkC.getWidth() - atk.getWidth());
                atk.setY(atkC.getHeight() - atk.getHeight());
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        ObjectAnimator flightMiddleLeft = ObjectAnimator.ofFloat(atk, "x", atk.getX());
        flightMiddleLeft.setDuration(BattleFragment.ANIMATION_LONG / 4);
        ObjectAnimator flightMiddleTop = ObjectAnimator.ofFloat(atk, "y", atk.getY());
        flightMiddleTop.setDuration(BattleFragment.ANIMATION_LONG / 4);
        ObjectAnimator flightMiddleAlpha = ObjectAnimator.ofFloat(atk, "alpha", 1f);
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

    private static float[] getCenter(View view) {
        float[] toReturn = new float[2];
        toReturn[0] = view.getX() + view.getWidth() * 0.5f;
        toReturn[1] = view.getY() + view.getHeight() * 0.5f;
        return toReturn;
    }

}
