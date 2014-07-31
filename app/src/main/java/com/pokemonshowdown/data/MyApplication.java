package com.pokemonshowdown.data;

import android.app.Application;

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

        initPokedex();
    }

    private void initPokedex() {
        mPokedex = Pokedex.getWithApplicationContext(getApplicationContext());
    }
}
