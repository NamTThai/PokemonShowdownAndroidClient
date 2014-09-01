package com.pokemonshowdown.app;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.pokemonshowdown.data.NodeConnection;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by thain on 7/15/14.
 */
public class BattleFieldActivity extends FragmentActivity {
    private final static String BTAG = "BattleFieldActivity";

    private int mPosition;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mLeftDrawerTitles;

    private ArrayList<String> mRoomList;

    private WebSocketClient mWebSocketClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle_field);

        mWebSocketClient = NodeConnection.getWithApplicationContext(getApplicationContext()).getWebSocketClient();
        if (mWebSocketClient == null) {
            mWebSocketClient = getWebSocketClient();
        }

        mTitle = mDrawerTitle = getTitle();
        mLeftDrawerTitles = getResources().getStringArray(R.array.bar_left_drawer);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.layout_battle_field_drawer);
        mDrawerList = (ListView) findViewById(R.id.layout_battle_field_left_drawer);

        // enable ActionBar application icon to behave as action to toggle navigation drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_battle_field, mLeftDrawerTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar application icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  // host Activity
                mDrawerLayout,         // DrawerLayout object
                R.drawable.ic_drawer,  // navigation drawer image to replace 'Up' caret
                R.string.drawer_open,  // "open drawer" description for accessibility
                R.string.drawer_close  // "close drawer" description for accessibility
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            mPosition = 0;
            selectItem(0);
            mRoomList = new ArrayList<>();
            mRoomList.add("lobby");
        } else {
            mPosition = savedInstanceState.getInt("Drawer Position");
            mRoomList = (ArrayList<String>) savedInstanceState.getSerializable("Room List");
            selectItem(mPosition);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("Drawer Position", mPosition);
        outState.putSerializable("Room List", mRoomList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.battle_field, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Called whenever we call invalidateOptionsMenu()
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the navigation drawer is open, hide action items related to the content view
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) return true;
        // Handle action buttons
        switch(item.getItemId()) {
            case R.id.menu_pokedex:
                startActivity(new Intent(this, PokedexActivity.class));
                return true;
            case R.id.menu_dmg_calc:
                startActivity(new Intent(this, DmgCalcActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(11)
    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setTitle(mTitle);
        }
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void selectItem(int position) {
        // update the main content by replacing fragments
        Fragment fragment;
        switch(position) {
            case 0:
                mPosition = 0;
                fragment = new FindBattleFragment();
                break;
            case 1:
                mPosition = 1;
                fragment = CommunityLoungeFragment.newInstance(mRoomList);
                break;
            case 5:
                mPosition = 5;
                fragment = new CreditsFragment();
                break;
            default:
                mPosition = 0;
                fragment = new FindBattleFragment();
        }

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.fragmentContainer, fragment, "Battle Field Drawer " + Integer.toString(position))
                .commit();

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mLeftDrawerTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void onBackPressed() {
        if (mPosition != 0) {
            selectItem(0);
            return;
        }
        if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
            mDrawerLayout.closeDrawer(mDrawerList);
            return;
        }
        super.onBackPressed();
    }

    private WebSocketClient getWebSocketClient() {
        ConnectivityManager connectivityManager = (ConnectivityManager) BattleFieldActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (mWebSocketClient != null && mWebSocketClient.getConnection().isOpen()) {
                return mWebSocketClient;
            } else {
                new ConnectToServer().execute();
                return mWebSocketClient;
            }
        } else {
            // TODO: get alert dialog for failed connection
            return null;
        }
    }

    private void closeActiveConnection() {
        if(mWebSocketClient != null && mWebSocketClient.getConnection().isOpen()) {
            mWebSocketClient.close();
        }
    }

    private void sendClientMessage(String message) {
        WebSocketClient webSocketClient = getWebSocketClient();
        if (webSocketClient != null) {
            webSocketClient.send(message);
        }
    }

    /**
     * Channel list:
     * -1: global or lobby
     * 0: battle
     * 1: chatroom
     */
    private void processMessage(String message) {
        // Break down message to see which channel it has to go through
        int channel;
        int roomId;
        if (message.charAt(0) != '>') {
            channel = -1;
            processGlobalMessage(message);
        } else {
            // TODO: deal with server messages that come with ROOMID
        }
    }

    private void processGlobalMessage(String message) {
        int channel;
        if (message.charAt(0) != '|') {
            channel = 1;
        } else {
            message = message.substring(1);
            String command = message.substring(0, message.indexOf('|'));
            switch (command) {
                case "popup":
                case "pm":
                case "usercount":
                case "nametaken":
                case "formats":
                case "updatesearch":
                case "updatechallenges":
                case "queryresponse":
                case "updateuser":
                case "challstr":
                    channel = -1;
                    break;
                default:
                    channel = 1;
            }
        }

        if (channel == mPosition) {
            CommunityLoungeFragment fragment = (CommunityLoungeFragment) getSupportFragmentManager().findFragmentByTag("Battle Field Drawer " + mPosition);
            fragment.processServerMessage("lobby", message);
        }
    }

    // The click listener for ListView in the navigation drawer
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private class ConnectToServer extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params){
            try {
                openNewConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private String openNewConnection() throws IOException {

            try {

                URI uri = new URI("ws://nthai.cs.trincoll.edu:8000/showdown/websocket");

                if (mWebSocketClient == null) {
                    mWebSocketClient = new WebSocketClient(uri) {
                        @Override
                        public void onOpen(ServerHandshake serverHandshake) {
                            // Log.d(BTAG, "Opened");
                            NodeConnection.getWithApplicationContext(getApplicationContext()).setWebSocketClient(mWebSocketClient);
                        }

                        @Override
                        public void onMessage(String s) {
                            Log.d("NodeConnection", s);
                            processMessage(s);
                        }

                        @Override
                        public void onClose(int code, String reason, boolean remote) {
                            mWebSocketClient = null;
                            Log.d(BTAG, "Closed: code " + code + " reason " + reason + " remote " + remote);
                        }

                        @Override
                        public void onError(Exception e) {
                            mWebSocketClient = null;
                            Log.d(BTAG, "Error: " + e.toString());
                        }
                    };
                }
                if (!mWebSocketClient.getConnection().isOpen()) {
                    mWebSocketClient.connect();
                }
            } catch (Exception e) {
                Log.d(BTAG, e.toString());
            }
            return null;
        }
    }

}
