package com.pokemonshowdown.activity;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.pokemonshowdown.R;

/**
 * Created by McBeengs on 19/10/2016.
 */

public class BaseActivity extends AppCompatActivity {

    private Toolbar toolbar;

    protected void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("");
            getSupportActionBar().setIcon(R.drawable.ic_logo);
        }
    }

    protected Toolbar getToolbar() {
        return toolbar;
    }

    protected Context getContext() {
        return this;
    }
}
