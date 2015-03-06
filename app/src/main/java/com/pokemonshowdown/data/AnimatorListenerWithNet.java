package com.pokemonshowdown.data;

import android.animation.Animator;

public class AnimatorListenerWithNet implements Animator.AnimatorListener {

    @Override
    public void onAnimationStart(final Animator animation) {
        new RunWithNet() {
            @Override
            public void runWithNet() {
                onAnimationStartWithNet(animation);
            }
        }.run();
    }

    @Override
    public void onAnimationEnd(final Animator animation) {
        new RunWithNet() {
            @Override
            public void runWithNet() {
                onAnimationEndWithNet(animation);
            }
        }.run();
    }

    @Override
    public void onAnimationCancel(final Animator animation) {
        new RunWithNet() {
            @Override
            public void runWithNet() {
                onAnimationCancelWithNet(animation);
            }
        }.run();
    }

    @Override
    public void onAnimationRepeat(final Animator animation) {
        new RunWithNet() {
            @Override
            public void runWithNet() {
                onAnimationRepeatWithNet(animation);
            }
        }.run();
    }

    public void onAnimationRepeatWithNet(Animator animation) {

    }

    public void onAnimationCancelWithNet(Animator animation) {

    }

    public void onAnimationEndWithNet(Animator animation) {

    }

    public void onAnimationStartWithNet(Animator animation) {

    }
}
