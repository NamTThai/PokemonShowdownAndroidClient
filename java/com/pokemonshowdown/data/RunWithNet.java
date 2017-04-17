package com.pokemonshowdown.data;

import android.util.Log;

import com.pokemonshowdown.application.MyApplication;

public abstract class RunWithNet implements Runnable {
    public final static String RTAG = RunWithNet.class.getName();

    @Override
    public void run() {
        try {
            runWithNet();
        } catch (Exception e) {
            MyApplication.getMyApplication().addCaughtException(e);
            Log.e(RTAG, "Exception", e);
        }
    }

    public abstract void runWithNet() throws Exception;
}
