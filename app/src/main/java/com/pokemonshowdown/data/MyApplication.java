package com.pokemonshowdown.data;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by thain on 7/31/14.
 *
 * This class is to initialize all the data singletons
 */
public class MyApplication extends Application {
    public Pokedex mPokedex;
    public NodeConnection mNodeConnection;
    public Onboarding mOnboarding;

    @Override
    public void onCreate() {
        super.onCreate();
        
        Context appContext = getApplicationContext();

        mNodeConnection = NodeConnection.getWithApplicationContext(appContext);
        mPokedex = Pokedex.getWithApplicationContext(appContext);
        mOnboarding = Onboarding.getWithApplicationContext(appContext);
    }
}
