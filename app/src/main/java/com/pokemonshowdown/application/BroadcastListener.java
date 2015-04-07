package com.pokemonshowdown.application;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.pokemonshowdown.app.BattleFieldActivity;

import java.util.ArrayList;

public class BroadcastListener extends BroadcastReceiver {
    private static BroadcastListener sBroadcastListener;
    private Context mContext;

    private BattleFieldActivity mBattleFieldActivity;
    private ArrayList<Intent> mPendingIntents;
    private boolean mListening;

    private BroadcastListener(BattleFieldActivity battleFieldActivity) {
        mContext = battleFieldActivity.getApplicationContext();
        mBattleFieldActivity = battleFieldActivity;
    }

    public static BroadcastListener get(BattleFieldActivity battleFieldActivity) {
        if (sBroadcastListener == null) {
            sBroadcastListener = new BroadcastListener(battleFieldActivity);
        }
        return sBroadcastListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mListening) {
            mBattleFieldActivity.processBroadcastMessage(intent);
        } else {
            if (mPendingIntents == null) {
                mPendingIntents = new ArrayList<>();
            }
            mPendingIntents.add(intent);
        }
    }

    public void register() {
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
        LocalBroadcastManager.getInstance(mBattleFieldActivity).registerReceiver(this,
                new IntentFilter(BroadcastSender.ACTION_FROM_MY_APPLICATION));
    }
}
