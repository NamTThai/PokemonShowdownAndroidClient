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

    @Override
    public void onCreate() {
        super.onCreate();

        connectToServer();
        initPokedex();
    }

    private void connectToServer() {
        mNodeConnection = NodeConnection.getWithApplicationContext(getApplicationContext());
    }

    private void initPokedex() {
        mPokedex = Pokedex.getWithApplicationContext(getApplicationContext());
    }
}
