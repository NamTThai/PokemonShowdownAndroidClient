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

    @Override
    public void onCreate() {
        super.onCreate();

        connectToServer();

        initPokedex();
    }

    private void connectToServer() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new NodeConnection().execute();
        } else {
            Log.d(NodeConnection.CTAG, "Check network connection");
        }
    }

    private void initPokedex() {
        mPokedex = Pokedex.getWithApplicationContext(getApplicationContext());
    }
}
