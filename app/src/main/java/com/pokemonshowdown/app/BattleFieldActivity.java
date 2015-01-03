package com.pokemonshowdown.app;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
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

import com.pokemonshowdown.data.BattleFieldData;
import com.pokemonshowdown.data.CommunityLoungeData;
import com.pokemonshowdown.data.MyApplication;
import com.pokemonshowdown.data.Onboarding;

import org.json.JSONException;
import org.json.JSONObject;

public class BattleFieldActivity extends FragmentActivity {
    public final static String BTAG = BattleFieldActivity.class.getName();
    public final static String BATTLE_FIELD_FRAGMENT_TAG = "Battle Field Drawer 0";
    public final static String DRAWER_POSITION = "Drawer Position";

    private int mPosition;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private AlertDialog mDialog;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mLeftDrawerTitles;

    private BroadcastReceiver mBroadcastReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle_field);

        mTitle = mDrawerTitle = getTitle();
        mLeftDrawerTitles = getResources().getStringArray(R.array.bar_left_drawer);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.layout_battle_field_drawer);
        mDrawerList = (ListView) findViewById(R.id.layout_battle_field_left_drawer);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_battle_field, mLeftDrawerTitles));
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.drawable.ic_drawer,
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }

        if (savedInstanceState == null) {
            mPosition = 0;
            selectItem(0);
        } else {
            mPosition = savedInstanceState.getInt(DRAWER_POSITION);
            selectItem(mPosition);
        }
    }

    @Override
    protected void onResume() {
        super.onPause();
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                processBroadcastMessage(intent);
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(MyApplication.ACTION_FROM_MY_APPLICATION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(DRAWER_POSITION, mPosition);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.battle_field, menu);
        menu.findItem(R.id.community_lounge).setVisible(false);
        menu.findItem(R.id.room_id).setVisible(false);
        menu.findItem(R.id.cancel).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) return true;

        switch (item.getItemId()) {
            case R.id.team_building:
                startActivity(new Intent(this, TeamBuildingActivity.class));
                return true;
            case R.id.menu_pokedex:
                startActivity(new Intent(this, PokedexActivity.class));
                return true;
            case R.id.menu_dmg_calc:
                startActivity(new Intent(this, DmgCalcActivity.class));
                return true;
            case R.id.menu_login:
                Onboarding onboarding = Onboarding.get(getApplicationContext());
                if (onboarding.getKeyId() == null || onboarding.getChallenge() == null) {
                    MyApplication.getMyApplication().getWebSocketClient();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mDialog != null && mDialog.isShowing()) {
                                mDialog.dismiss();
                            }
                            mDialog = new AlertDialog.Builder(BattleFieldActivity.this)
                                    .setMessage(R.string.weak_connection)
                                    .create();
                            mDialog.show();
                        }
                    });
                    return true;
                }
                if (onboarding.isSignedIn()) {
                    FragmentManager fm = getSupportFragmentManager();
                    UserDialog userDialog = new UserDialog();
                    userDialog.show(fm, UserDialog.UTAG);
                    return true;
                }
                FragmentManager fm = getSupportFragmentManager();
                OnboardingDialog fragment = new OnboardingDialog();
                fragment.show(fm, OnboardingDialog.OTAG);
                return true;
            case R.id.menu_settings:
                new AlertDialog.Builder(this)
                        .setMessage(R.string.teaser_alert)
                        .create()
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

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
        switch (position) {
            case 0:
                mPosition = 0;
                fragment = BattleFieldFragment.newInstance();
                break;
            case 1:
                mPosition = 1;
                fragment = CommunityLoungeFragment.newInstance();
                break;
            case 3:
                mPosition = 3;
                fragment = CreditsFragment.newInstance();
                break;
            default:
                mPosition = 2;
                fragment = new PlaceHolderFragment();
        }

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.fragmentContainer, fragment, "Battle Field Drawer " + Integer.toString(position))
                .commit();

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

    private void processBroadcastMessage(Intent intent) {
        String details = intent.getExtras().getString(MyApplication.EXTRA_DETAILS);
        switch (details) {
            case MyApplication.EXTRA_UPDATE_SEARCH:
                String updateSearchStatus = intent.getExtras().getString(MyApplication.EXTRA_UPDATE_SEARCH);
                try {
                    JSONObject updateSearchJSon = new JSONObject(updateSearchStatus);
                    Object updateStatusObject = updateSearchJSon.get("searching");
                    // is only boolean when search is done or maybe canceled
                    if (updateStatusObject instanceof Boolean) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mDialog != null && mDialog.isShowing()) {
                                    mDialog.dismiss();
                                }
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mDialog = new AlertDialog.Builder(BattleFieldActivity.this)
                                        .setMessage(R.string.searching_battle)
                                        .create();
                                mDialog.show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case MyApplication.EXTRA_NO_INTERNET_CONNECTION:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mDialog != null && mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                        mDialog = new AlertDialog.Builder(BattleFieldActivity.this)
                                .setMessage(R.string.no_connection)
                                .create();
                        mDialog.show();
                    }
                });
                return;
            case MyApplication.EXTRA_AVAILABLE_FORMATS:
                BattleFieldFragment battleFieldFragment = (BattleFieldFragment) getSupportFragmentManager().findFragmentByTag("Battle Field Drawer 0");
                if (battleFieldFragment != null) {
                    battleFieldFragment.setAvailableFormat();
                }
                return;
            case MyApplication.EXTRA_WATCH_BATTLE_LIST_READY:
                battleFieldFragment = (BattleFieldFragment) getSupportFragmentManager().findFragmentByTag("Battle Field Drawer 0");
                if (battleFieldFragment != null) {
                    battleFieldFragment.generateAvailableWatchBattleDialog();
                }
                return;
            case MyApplication.EXTRA_NEW_BATTLE_ROOM:
                String roomId = intent.getExtras().getString(MyApplication.EXTRA_ROOMID);
                BattleFieldFragment fragment = (BattleFieldFragment) getSupportFragmentManager().findFragmentByTag("Battle Field Drawer 0");
                if (fragment != null) {
                    fragment.processNewRoomRequest(roomId);
                }
                return;
            case MyApplication.EXTRA_SERVER_MESSAGE:
                String serverMessage = intent.getExtras().getString(MyApplication.EXTRA_SERVER_MESSAGE);
                int channel = intent.getExtras().getInt(MyApplication.EXTRA_CHANNEL);
                roomId = intent.getExtras().getString(MyApplication.EXTRA_ROOMID);
                processMessage(channel, roomId, serverMessage);
                return;
            case MyApplication.EXTRA_REQUIRE_SIGN_IN:
                FragmentManager fm = getSupportFragmentManager();
                OnboardingDialog dialog = new OnboardingDialog();
                dialog.show(fm, OnboardingDialog.OTAG);
                return;
            case MyApplication.EXTRA_ERROR_MESSAGE:
                final String errorMessage = intent.getExtras().getString(MyApplication.EXTRA_ERROR_MESSAGE);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mDialog != null && mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                        mDialog = new AlertDialog.Builder(BattleFieldActivity.this)
                                .setMessage(errorMessage)
                                .create();
                        mDialog.show();
                    }
                });
        }
    }

    /**
     * Channel list:
     * -1: global or lobby
     * 0: battle
     * 1: chatroom
     */
    public void processMessage(int channel, String roomId, String message) {
        // Break down message to see which channel it has to go through
        if (channel == 1) {
            CommunityLoungeData.RoomData roomData = CommunityLoungeData.get(getApplicationContext()).getRoomInstance(roomId);
            if (roomData != null && roomData.isMessageListener()) {
                roomData.addServerMessageOnHold(message);
            } else {
                CommunityLoungeFragment fragment = (CommunityLoungeFragment) getSupportFragmentManager().findFragmentByTag("Battle Field Drawer 1");
                if (fragment != null) {
                    fragment.processServerMessage(roomId, message);
                }
            }
        } else { // channel == 0
            BattleFieldData.RoomData roomData = BattleFieldData.get(this).getAnimationInstance(roomId);
            if (roomData != null && roomData.isMessageListener()) {
                roomData.addServerMessageOnHold(message);
            } else {
                BattleFieldFragment fragment = (BattleFieldFragment) getSupportFragmentManager().findFragmentByTag("Battle Field Drawer 0");
                if (fragment != null) {
                    fragment.processServerMessage(roomId, message);
                }
            }
        }
    }

    public AlertDialog createErrorAlert(Exception e) {
        return new AlertDialog.Builder(this)
                .setMessage(e.toString())
                .create();
    }

    public void showErrorAlert(Exception e) {
        AlertDialog alertDialog = createErrorAlert(e);
        Log.e(BTAG, "App exception", e);
        alertDialog.show();
    }

}
