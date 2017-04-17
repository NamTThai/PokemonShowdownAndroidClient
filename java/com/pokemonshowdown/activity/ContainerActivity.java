package com.pokemonshowdown.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.pokemonshowdown.DownloadUpdateTask;
import com.pokemonshowdown.ExportReplayTask;
import com.pokemonshowdown.R;
import com.pokemonshowdown.UpdateCheckTask;
import com.pokemonshowdown.application.BroadcastListener;
import com.pokemonshowdown.application.BroadcastSender;
import com.pokemonshowdown.application.MyApplication;
import com.pokemonshowdown.data.BattleFieldData;
import com.pokemonshowdown.data.CommunityLoungeData;
import com.pokemonshowdown.data.Onboarding;
import com.pokemonshowdown.dialog.ChallengeDialog;
import com.pokemonshowdown.dialog.OnboardingDialog;
import com.pokemonshowdown.dialog.UserDialog;
import com.pokemonshowdown.fragment.BattleFragment;
import com.pokemonshowdown.fragment.BattleLobbyFragment;
import com.pokemonshowdown.fragment.CommunityLoungeFragment;
import com.pokemonshowdown.fragment.CreditsFragment;
import com.pokemonshowdown.fragment.HomeFragment;
import com.pokemonshowdown.fragment.MainScreenFragment;
import com.pokemonshowdown.fragment.PlaceHolderFragment;
import com.pokemonshowdown.fragment.WatchBattleFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by McBeengs on 19/10/2016.
 */

public class ContainerActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    public final static String BTAG = ContainerActivity.class.getName();
    public final static int REQUEST_CODE_DONATION = 100;
    public final static int REQUEST_CODE_BUG_REPORT = 200;
    public final static String BATTLE_FIELD_FRAGMENT_TAG = "Battle Field Drawer 0";
    public final static String DRAWER_POSITION = "Drawer Position";
    private static final String CHALLENGE_DIALOG_TAG = "CHALLENGE_DIALOG_TAG";
    public static String lastRoomIdCreated = "";
    private int mPosition;
    private int numOfRooms = 0;
    private DrawerLayout mDrawerLayout;
    private AlertDialog mDialog;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mLeftDrawerTitles;
    private BroadcastListener mBroadcastListener;
    private boolean updateMessageShow = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        setupToolbar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mTitle = mDrawerTitle = getTitle();
        mLeftDrawerTitles = getResources().getStringArray(R.array.bar_left_drawer);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.layout_battle_field_drawer);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.layout_battle_field_drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, getToolbar(), R.string.EVs, R.string.EVs);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            mPosition = 0;
            updateMessageShow = false;
            selectItem(0);
        } else {
            mPosition = savedInstanceState.getInt(DRAWER_POSITION);
            updateMessageShow = savedInstanceState.getBoolean("update");
            selectItem(mPosition);
        }

        if (!updateMessageShow) {
            new UpdateCheckTask((MyApplication) getApplicationContext()).execute();
            updateMessageShow = true;
        }

        MyApplication.getMyApplication().getWebSocketClient();
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

        if (!Onboarding.get(getApplicationContext()).propertyExists(Onboarding.WARNING_HEADER)) {
            mDialog = new AlertDialog.Builder(ContainerActivity.this)
                    .setMessage(R.string.warning_dialog)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Onboarding.get(getApplicationContext()).setWarned();
                        }
                    })
                    .create();
            mDialog.show();
        }

        if (!Onboarding.get(getApplicationContext()).propertyExists(Onboarding.ADV_HEADER)) {
            mDialog = new AlertDialog.Builder(ContainerActivity.this)
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
        outState.putBoolean("update", updateMessageShow);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.layout_battle_field_drawer);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.battle_field, menu);
        menu.findItem(R.id.community_lounge).setVisible(false);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_BUG_REPORT) {
            // seems like gmail doenst tell us if the email hsa been sent or not
            MyApplication.getMyApplication().clearCaughtExceptions();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.cancel:
                MainScreenFragment.TABS_HOLDER_ACCESSOR.removeTab(false);
                return true;
            case R.id.team_building:
                startActivity(new Intent(this, TeamBuilderActivity.class));
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
                            mDialog = new AlertDialog.Builder(ContainerActivity.this)
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
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.menu_bug_report:
                if (MyApplication.getMyApplication().getCaughtExceptions().size() > 0) {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto", "psandroidteam@gmail.com", null));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Bug report");
                    StringBuilder bodyStringBuilder = new StringBuilder();
                    int idx = 1;
                    bodyStringBuilder.append("Version ");
                    try {
                        bodyStringBuilder.append(MyApplication.getMyApplication().getPackageManager()
                                .getPackageInfo(MyApplication.getMyApplication().getApplicationContext().getPackageName(), 0)
                                .versionCode);
                    } catch (PackageManager.NameNotFoundException e) {
                        bodyStringBuilder.append("unknown");
                    }
                    bodyStringBuilder.append(System.getProperty("line.separator"));
                    for (Exception e : MyApplication.getMyApplication().getCaughtExceptions()) {
                        bodyStringBuilder.append("Bug ").append(idx++).append(System.getProperty("line.separator"));
                        bodyStringBuilder.append(e.getMessage()).append(System.getProperty("line.separator"));
                        for (StackTraceElement element : e.getStackTrace()) {
                            bodyStringBuilder.append(element.toString());
                            bodyStringBuilder.append(System.getProperty("line.separator"));
                        }
                    }
                    emailIntent.putExtra(Intent.EXTRA_TEXT, bodyStringBuilder.toString());

                    startActivityForResult(Intent.createChooser(emailIntent, "Email your bug report"), REQUEST_CODE_BUG_REPORT);
                } else {
                    Toast.makeText(getContext(), "There are no bugs to report.", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // update the main content by replacing fragments

        int pos;
        switch (item.getItemId()) {
            case R.id.nav_battle_field:
                pos = 0;
                break;
            case R.id.nav_community_lounge:
                pos = 1;
                break;
            case R.id.nav_replays:
                pos = 2;
                break;
            case R.id.nav_credits:
                pos = 3;
                break;
            default:
                pos = 0;
        }

        selectItem(pos);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.layout_battle_field_drawer);
        drawer.closeDrawer(GravityCompat.START);

        return true;
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
                                if (mDialog != null && mDialog.isShowing()) {
                                    mDialog.dismiss();
                                }
                                mDialog = new AlertDialog.Builder(ContainerActivity.this)
                                        .setMessage("Searching for other trainers...")
                                        .create();
                                mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialogInterface) {
                                        MyApplication.getMyApplication().sendClientMessage("|/cancelsearch");
                                    }
                                });
                                mDialog.setCancelable(true);
                                mDialog.show();
                            }
                        });
                    }

                    if (BattleFieldData.sRooms == null) {
                        BattleFieldData.sRooms = new ArrayList<>();
                    }
                    BattleFieldData.sRooms.clear();

                    JSONObject gamesObject = updateSearchJSon.getJSONObject("games");
                    if (gamesObject.length() != 0) {
                        Iterator<String> iterator = gamesObject.keys();
                        while (iterator.hasNext()) {
                            String currentKey = iterator.next();
                            BattleFieldData.sRooms.add(currentKey);
                        }
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
                        mDialog = new AlertDialog.Builder(ContainerActivity.this)
                                .setMessage(R.string.no_connection)
                                .create();
                        mDialog.show();
                    }
                });
                return;
            case BroadcastSender.EXTRA_AVAILABLE_FORMATS:
//                BattleFieldFragment battleFieldFragment = (BattleFieldFragment) getSupportFragmentManager().findFragmentByTag("Battle Field Drawer 0");
//                if (battleFieldFragment != null) {
//                    battleFieldFragment.setAvailableFormat();
//                }
                return;
            case BroadcastSender.EXTRA_WATCH_BATTLE_LIST_READY:
                WatchBattleFragment.ACCESSOR.fireBattlesListViewUpdate();
                return;
            case BroadcastSender.EXTRA_NEW_BATTLE_ROOM:
                String roomId = intent.getExtras().getString(BroadcastSender.EXTRA_ROOMID);
                lastRoomIdCreated = roomId;

                // We need to check what tab the battle is going to occur. A challenge, for instance, will be hold on the
                // last available tab index (tabCount + 1), but a battle (or spectator, who knows) coming from the lobby screen
                // will be played on the tab index that requested it (E.g.: Tab 2 of 3, so index = 1), overriding the View prior
                // to that.

                BattleFieldData.get(getContext()).joinRoom(roomId, false);
                Bundle args = new Bundle();
                args.putString(BattleFragment.ROOM_ID, roomId);

                if (BattleLobbyFragment.requestingRoomIndex > 0 || WatchBattleFragment.requestingRoomIndex > 0) {
                    MainScreenFragment.TABS_HOLDER_ACCESSOR.removeTab(true);
                    MainScreenFragment.TABS_HOLDER_ACCESSOR.addTab(BattleFragment.class.getName(), args);
                } else {
                    MainScreenFragment.TABS_HOLDER_ACCESSOR.addTab(BattleFragment.class.getName(), args);
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

                        // Account for incompatible teams message. Just gives it a better treatment.
                        if (errorMessage.contains("can't learn") || errorMessage.contains("has no moves") || errorMessage.contains("is unreleased")
                                || errorMessage.contains("is banned") || errorMessage.contains("does not exist") || errorMessage.contains("is only obtainable")) {
                            String message = "The following errors were found on the selected team. If you want to use it anyway, please " +
                                    "go to the team builder section and fix them.\n\n";

                            String error = errorMessage.substring(1, errorMessage.length()).replace("||-", "nextError");
                            for (String s : error.split("nextError")) {
                                if (!s.isEmpty()) {
                                    message += "* " + s + "\n\n";
                                }
                            }

                            mDialog = new AlertDialog.Builder(ContainerActivity.this)
                                    .setTitle("Team authentication error")
                                    .setMessage(message)
                                    .create();
                            mDialog.show();

                            return;
                        }

                        mDialog = new AlertDialog.Builder(ContainerActivity.this)
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
                        mDialog = new AlertDialog.Builder(ContainerActivity.this)
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
                        mDialog = new AlertDialog.Builder(ContainerActivity.this)
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
                                    new DownloadUpdateTask(ContainerActivity.this).execute();
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

                //Set username
                HomeFragment.USERNAME_LOGGED.setUsername(userName);
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
                BattleFragment.RECEIVER.processServerMessage(roomId, message);
            }
        }
    }

    private void selectItem(int position) {
        // update the main content by replacing fragments
        mPosition = position;
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(String.valueOf(position));
        if (fragment == null) {
            switch (position) {
                case 0:
                    //fragment = BattleFieldFragment.newInstance(null);
                    fragment = new MainScreenFragment();
                    break;
                case 1:
                    //fragment = CommunityLoungeFragment.newInstance();
                    fragment = new CommunityLoungeFragment();
                    break;
                case 3:
                    fragment = new CreditsFragment();
                    break;
                default:
                    fragment = new PlaceHolderFragment();
            }
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment,
                String.valueOf(position)).commitAllowingStateLoss();
    }
}
