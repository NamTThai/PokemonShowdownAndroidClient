package com.pokemonshowdown.application;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.pokemonshowdown.activity.ContainerActivity;

import java.util.ArrayList;

public class BroadcastListener extends BroadcastReceiver {

    private static BroadcastListener sBroadcastListener;
    private Context mContext;
    private ContainerActivity mBattleFieldActivity;
    private ArrayList<Intent> mPendingIntents;
    private boolean mListening;

    private BroadcastListener(Context context) {
        mContext = context;
    }

    public static BroadcastListener get(Context context) {
        if (sBroadcastListener == null) {
            sBroadcastListener = new BroadcastListener(context.getApplicationContext());
        }
        return sBroadcastListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mBattleFieldActivity == null) {
            return;
        }

        if (mListening) {
            mBattleFieldActivity.processBroadcastMessage(intent);
        }
    }

    public void addPendingIntent(Intent intent) {
        if (mPendingIntents == null) {
            mPendingIntents = new ArrayList<>();
        }
        mPendingIntents.add(intent);
    }

    public boolean isListening() {
        return mListening;
    }

    public void register(ContainerActivity battleFieldActivity) {
        mBattleFieldActivity = battleFieldActivity;
        mListening = true;
        LocalBroadcastManager.getInstance(mBattleFieldActivity).registerReceiver(this,
                new IntentFilter(BroadcastSender.ACTION_FROM_MY_APPLICATION));
        if (mPendingIntents != null && !mPendingIntents.isEmpty()) {
            for (Intent intent : mPendingIntents) {
                mBattleFieldActivity.processBroadcastMessage(intent);
            }
            mPendingIntents = null;
        }
    }

    public void unregister() {
        mListening = false;
        LocalBroadcastManager.getInstance(mBattleFieldActivity).unregisterReceiver(this);
        mBattleFieldActivity = null;
    }
}
