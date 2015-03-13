package com.pokemonshowdown.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
                new AlertDialog.Builder(this)
                        .setMessage(R.string.still_in_development)
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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                processBroadcastMessage(intent);
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(MyApplication.ACTION_FROM_MY_APPLICATION));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(DRAWER_POSITION, mPosition);
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
                return;
            case MyApplication.EXTRA_UNKNOWN_ERROR:
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

            case MyApplication.EXTRA_UPDATE_AVAILABLE:
                new AlertDialog.Builder(this)
                        .setMessage(R.string.update_available)
                        .setPositiveButton(R.string.dialog_ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        new DownloadUpdateTask().execute();
                                    }
                                })
                        .setNegativeButton(R.string.dialog_cancel, null)
                        .create()
                        .show();
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

    public void showErrorAlert(Exception e) {
        AlertDialog alertDialog = createErrorAlert(e);
        Log.e(BTAG, "App exception", e);
        alertDialog.show();
    }

    public AlertDialog createErrorAlert(Exception e) {
        return new AlertDialog.Builder(this)
                .setMessage(e.toString())
                .create();
    }


    public class DownloadUpdateTask extends AsyncTask<Void, Integer, Void> {
        private ProgressDialog waitingDialog;
        private final static String APK_LOCATION = "http://ns3367227.ip-37-187-3.eu/showdown/current.apk";
        private int status;

        public DownloadUpdateTask() {
            waitingDialog = new ProgressDialog(BattleFieldActivity.this);
            waitingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            waitingDialog.setMessage(getResources().getString(R.string.downloadingupdate));
            waitingDialog.setCancelable(false);
            waitingDialog.setMax(100);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            BattleFieldActivity.this.runOnUiThread(new java.lang.Runnable() {
                public void run() {
                    waitingDialog.show();
                }
            });
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            waitingDialog.setProgress(values[0]);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            final HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
            HttpClient client = new DefaultHttpClient(httpParams);
            HttpGet httpget = new HttpGet(APK_LOCATION);
            HttpResponse response = null;
            try {
                response = client.execute(httpget);
                status = response.getStatusLine().getStatusCode();
                if (status == HttpStatus.SC_OK) {
                    HttpEntity entity = response.getEntity();
                    InputStream fileStream = entity.getContent();
                    File file = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "temp.apk");
                    file.createNewFile();
                    FileOutputStream fos = new FileOutputStream(file);

                    long totalLength = entity.getContentLength();
                    int readLength = 0;
                    int count = 0;
                    byte data[] = new byte[1024];
                    while ((count = fileStream.read(data)) != -1) {
                        readLength += count;
                        publishProgress((int) ((readLength * 100) / totalLength));
                        fos.write(data, 0, count);
                    }

                    fos.flush();
                    fos.close();
                    fileStream.close();
                }
            } catch (IOException e) {
                return null;
            }
            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            BattleFieldActivity.this.runOnUiThread(new java.lang.Runnable() {
                public void run() {
                    waitingDialog.dismiss();
                }
            });

            if (status == HttpStatus.SC_OK) {
                File file = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "temp.apk");

                Intent promptInstall = new Intent(Intent.ACTION_VIEW)
                        .setDataAndType(Uri.fromFile(file),
                                "application/vnd.android.package-archive");
                startActivity(promptInstall);
            }

        }
    }
}
