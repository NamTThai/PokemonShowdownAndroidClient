package com.pokemonshowdown.data;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * This class is to initialize all the data singletons
 */
public class MyApplication extends Application {
    public Pokedex mPokedex;
    public MoveDex mMoveDex;
    public AbilityDex mAbilityDex;
    public ItemDex mItemDex;
    public NodeConnection mNodeConnection;
    public Onboarding mOnboarding;

    @Override
    public void onCreate() {
        super.onCreate();
        
        Context appContext = getApplicationContext();

        mNodeConnection = NodeConnection.getWithApplicationContext(appContext);
        mPokedex = Pokedex.getWithApplicationContext(appContext);
        mMoveDex = MoveDex.getWithApplicationContext(appContext);
        mAbilityDex = AbilityDex.getWithApplicationContext(appContext);
        mItemDex = ItemDex.getWithApplicationContext(appContext);
        mOnboarding = Onboarding.getWithApplicationContext(appContext);
    }
}
