package com.pokemonshowdown.data;

import android.util.Log;

public abstract class RunWithNet implements Runnable {
    public final static String ETAG = RunWithNet.class.getName();

    @Override
    public void run() {
        try {
            runWithNet();
        } catch (Exception e) {
            Log.e(ETAG, "Exception", e);
        }
    }

    public abstract void runWithNet();
}
