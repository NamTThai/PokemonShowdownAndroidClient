package com.pokemonshowdown.app;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.pokemonshowdown.application.BroadcastListener;
import com.pokemonshowdown.application.BroadcastSender;
import com.pokemonshowdown.application.MyApplication;
import com.pokemonshowdown.data.BattleFieldData;
import com.pokemonshowdown.data.CommunityLoungeData;
import com.pokemonshowdown.data.Onboarding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.text.DecimalFormat;
import java.util.Arrays;

public class BattleFieldActivity extends FragmentActivity {
    public final static String BTAG = BattleFieldActivity.class.getName();
    public final static int REQUEST_CODE_DONATION = 100;
    public final static String BATTLE_FIELD_FRAGMENT_TAG = "Battle Field Drawer 0";
    public final static String DRAWER_POSITION = "Drawer Position";
    private static final String CHALLENGE_DIALOG_TAG = "CHALLENGE_DIALOG_TAG";
    private float mDonationAmount;
    private int mPosition;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private AlertDialog mDialog;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mLeftDrawerTitles;

    private BroadcastListener mBroadcastListener;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
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
                new SettingsDialog().show(getSupportFragmentManager(), SettingsDialog.STAG);
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
    public void onBackPressed() {
        if (mPosition != 0) {
            selectItem(0);
            return;
        }
        if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle_field);

        new UpdateCheckTask((MyApplication) getApplicationContext()).execute();

        MyApplication.getMyApplication().getWebSocketClient();

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
            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu();
            }

            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
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
    protected void onDestroy() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBroadcastListener.unregister();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mBroadcastListener = BroadcastListener.get(this);
        mBroadcastListener.register(this);

        if(!Onboarding.get(getApplicationContext()).propertyExists(Onboarding.ADV_HEADER)) {
            mDialog = new AlertDialog.Builder(BattleFieldActivity.this)
                    .setMessage(R.string.advertise_dialog)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Onboarding.get(getApplicationContext()).setAdvertising(true);
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Onboarding.get(getApplicationContext()).setAdvertising(false);
                        }
                    })
                    .create();
            mDialog.show();

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(DRAWER_POSITION, mPosition);
    }

    public void processBroadcastMessage(Intent intent) {
        String details = intent.getExtras().getString(BroadcastSender.EXTRA_DETAILS);
        switch (details) {
            case BroadcastSender.EXTRA_UPDATE_SEARCH:
                String updateSearchStatus = intent.getExtras().getString(BroadcastSender.EXTRA_UPDATE_SEARCH);
                try {
                    JSONObject updateSearchJSon = new JSONObject(updateSearchStatus);
                    JSONArray updateStatusObject = updateSearchJSon.getJSONArray("searching");
                    // is only boolean when search is done or maybe canceled
                    if (updateStatusObject.length() == 0) {
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
            case BroadcastSender.EXTRA_NO_INTERNET_CONNECTION:
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
            case BroadcastSender.EXTRA_AVAILABLE_FORMATS:
                BattleFieldFragment battleFieldFragment = (BattleFieldFragment) getSupportFragmentManager().findFragmentByTag("Battle Field Drawer 0");
                if (battleFieldFragment != null) {
                    battleFieldFragment.setAvailableFormat();
                }
                return;
            case BroadcastSender.EXTRA_WATCH_BATTLE_LIST_READY:
                battleFieldFragment = (BattleFieldFragment) getSupportFragmentManager().findFragmentByTag("Battle Field Drawer 0");
                if (battleFieldFragment != null) {
                    battleFieldFragment.generateAvailableWatchBattleDialog();
                }
                return;
            case BroadcastSender.EXTRA_NEW_BATTLE_ROOM:
                String roomId = intent.getExtras().getString(BroadcastSender.EXTRA_ROOMID);
                BattleFieldFragment fragment = (BattleFieldFragment) getSupportFragmentManager().findFragmentByTag("Battle Field Drawer 0");
                if (fragment != null) {
                    fragment.processNewRoomRequest(roomId);
                } else {
                    fragment = BattleFieldFragment.newInstance(roomId);
                    FragmentManager fm = getSupportFragmentManager();
                    fm.beginTransaction()
                            .replace(R.id.fragmentContainer, fragment, "Battle Field Drawer " + Integer.toString(0))
                            .commit();

                    mDrawerList.setItemChecked(0, true);
                    setTitle(mLeftDrawerTitles[0]);
                    mDrawerLayout.closeDrawer(mDrawerList);

                }
                return;
            case BroadcastSender.EXTRA_SERVER_MESSAGE:
                String serverMessage = intent.getExtras().getString(BroadcastSender.EXTRA_SERVER_MESSAGE);
                int channel = Integer.parseInt(intent.getExtras().getString(BroadcastSender.EXTRA_CHANNEL));
                roomId = intent.getExtras().getString(BroadcastSender.EXTRA_ROOMID);
                processMessage(channel, roomId, serverMessage);
                return;
            case BroadcastSender.EXTRA_REQUIRE_SIGN_IN:
                FragmentManager fm = getSupportFragmentManager();
                OnboardingDialog dialog = new OnboardingDialog();
                dialog.show(fm, OnboardingDialog.OTAG);
                return;
            case BroadcastSender.EXTRA_ERROR_MESSAGE:
                final String errorMessage = intent.getExtras().getString(BroadcastSender.EXTRA_ERROR_MESSAGE);
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
                return;

            case BroadcastSender.EXTRA_UPDATE_CHALLENGE:
                Fragment challengeDialogExistingFragment = getSupportFragmentManager().findFragmentByTag(CHALLENGE_DIALOG_TAG);
                if (challengeDialogExistingFragment != null) {
                    //already a challenge dialog showing
                    break;
                }
                String updateChallengeStatus = intent.getExtras().getString(BroadcastSender.EXTRA_UPDATE_CHALLENGE);
                try {
                    JSONObject updateChallengeJSon = new JSONObject(updateChallengeStatus);
                    //seems like we can receive multiple challenges, but only send one
                    JSONObject from = (JSONObject) updateChallengeJSon.get("challengesFrom");
                    Iterator<?> fromKeys = from.keys();
                    while (fromKeys.hasNext()) {
                        String userName = (String) fromKeys.next();
                        String format = from.getString(userName);
                        Log.d(BTAG, "Challenge from " + userName + ", format:" + format);
                        ChallengeDialog cd = ChallengeDialog.newInstance(userName, format);
                        cd.show(getSupportFragmentManager(), CHALLENGE_DIALOG_TAG);
                        // we pop challenges 1 by 1
                        break;
                    }

                    if (updateChallengeJSon.getString("challengeTo").equals("null")) {
                        if (mDialog != null && mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                    } else {
                        JSONObject to = (JSONObject) updateChallengeJSon.get("challengeTo");
                        //"challengeTo":{"to":"tetonator","format":"randombattle"}
                        final String userName = to.getString("to");
                        String format = to.getString("format");
                        Log.d(BTAG, "Challenge to " + userName + ", format:" + format);

                        if (mDialog != null && mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                        mDialog = new AlertDialog.Builder(BattleFieldActivity.this)
                                .setMessage(String.format(getResources().getString(R.string.waiting_challenge_dialog), userName, format))
                                .create();
                        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                MyApplication.getMyApplication().sendClientMessage("|/cancelchallenge " + userName);
                            }
                        });
                        mDialog.setCancelable(true);
                        mDialog.show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            case BroadcastSender.EXTRA_UNKNOWN_ERROR:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mDialog != null && mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                        mDialog = new AlertDialog.Builder(BattleFieldActivity.this)
                                .setMessage("An unknown error was caught. " +
                                        "You can copy current roomId to clipboard; if anything funny happens," +
                                        " finish the battle in your device's browser.")
                                .create();
                        mDialog.show();
                    }
                });
                break;

            case BroadcastSender.EXTRA_UPDATE_AVAILABLE:
                final String serverVersion = intent.getExtras().getString(BroadcastSender.EXTRA_SERVER_VERSION);
                if (serverVersion != null) {
                    final String changelog = intent.getExtras().getString(BroadcastSender.EXTRA_CHANGELOG);

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    String serverVersionPrintable = String.format(
                            getResources().getString(R.string.update_available), serverVersion.trim());

                    builder.setTitle(serverVersionPrintable);
                    if (changelog != null) {
                        TextView message = new TextView(this);
                        message.setMovementMethod(new ScrollingMovementMethod());
                        message.setText(Html.fromHtml(changelog));
                        builder.setView(message);
                    }
                    builder.setPositiveButton(R.string.dialog_ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new DownloadUpdateTask(BattleFieldActivity.this).execute();
                                }
                            })
                            .setNegativeButton(R.string.dialog_cancel, null)
                            .create()
                            .show();
                }

                break;

            case BroadcastSender.EXTRA_LOGIN_SUCCESSFUL:
                final String userName = intent.getExtras().getString(BroadcastSender.EXTRA_LOGIN_SUCCESSFUL);
                Toast.makeText(this, String.format(getResources().getString(R.string.login_successful), userName), Toast.LENGTH_SHORT).show();
                break;

            case BroadcastSender.EXTRA_REPLAY_DATA:
                final String replayData = intent.getExtras().getString(BroadcastSender.EXTRA_REPLAY_DATA);
                new ExportReplayTask(this).execute(replayData);
                break;
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

    private void selectItem(int position) {
        // update the main content by replacing fragments
        Fragment fragment;
        switch (position) {
            case 0:
                mPosition = 0;
                fragment = BattleFieldFragment.newInstance(null);
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
}
