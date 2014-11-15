package com.pokemonshowdown.data;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.pokemonshowdown.app.BattleFragment;

public class BattleAnimation {
    public final static String BTAG = BattleAnimation.class.getName();

    public static AnimatorSet processMove(String move, View view, BattleFragment battleFragment, String[] split) {
        move = MyApplication.toId(move);
        if (view == null) {
            return null;
        }
        RelativeLayout atkContainer = (RelativeLayout) view.findViewById(battleFragment.getPkmLayoutId(split[0]));
        ImageView atk = (ImageView) view.findViewById(battleFragment.getSpriteId(split[0]));
        return shake(atkContainer, atk);
    }

    public static AnimatorSet attack(ImageView atk, ImageView def) {
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

    public static AnimatorSet dance(RelativeLayout container, ImageView atk) {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator shakeLeft = ObjectAnimator.ofFloat(atk, "x", 0f);
        shakeLeft.setDuration(BattleFragment.ANIMATION_LONG / 4);
        ObjectAnimator shakeRight = ObjectAnimator.ofFloat(atk, "x", container.getWidth() - atk.getWidth());
        shakeRight.setDuration(BattleFragment.ANIMATION_LONG / 2);
        ObjectAnimator shakeMiddle = ObjectAnimator.ofFloat(atk, "x", atk.getX());
        shakeMiddle.setDuration(BattleFragment.ANIMATION_LONG / 4);
        animatorSet.play(shakeLeft);
        animatorSet.play(shakeRight).after(shakeLeft);
        animatorSet.play(shakeMiddle).after(shakeRight);
        return animatorSet;
    }

    public static AnimatorSet shake(RelativeLayout container, ImageView atk) {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator shakeLeft = ObjectAnimator.ofFloat(atk, "x", 0f);
        shakeLeft.setDuration(BattleFragment.ANIMATION_LONG / 6);
        ObjectAnimator shakeRightAll = ObjectAnimator.ofFloat(atk, "x", container.getWidth() - atk.getWidth());
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

    public static float[] getCenter(ImageView view) {
        float[] toReturn = new float[2];
        toReturn[0] = view.getX() + view.getWidth() * 0.5f;
        toReturn[1] = view.getY() + view.getHeight() * 0.5f;
        return toReturn;
    }

    public static ImageView getImageView(Context activityContext, int resource) {
        ImageView toReturn = new ImageView(activityContext);
        toReturn.setImageResource(resource);

        ViewGroup.LayoutParams layoutParams = toReturn.getLayoutParams();
        layoutParams.width = 32;
        layoutParams.height = 32;

        return toReturn;
    }

}
