package com.pokemonshowdown.fragment;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pokemonshowdown.R;
import com.pokemonshowdown.activity.ContainerActivity;
import com.pokemonshowdown.application.MyApplication;
import com.pokemonshowdown.data.AnimatorListenerWithNet;
import com.pokemonshowdown.data.AudioManager;
import com.pokemonshowdown.data.BattleFieldData;
import com.pokemonshowdown.data.BattleMessage;
import com.pokemonshowdown.data.MoveDex;
import com.pokemonshowdown.data.Onboarding;
import com.pokemonshowdown.data.Pokemon;
import com.pokemonshowdown.data.PokemonInfo;
import com.pokemonshowdown.data.RunWithNet;
import com.pokemonshowdown.dialog.BattleLogDialog;
import com.pokemonshowdown.dialog.MoveInfoDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import me.grantland.widget.AutofitTextView;

public class BattleFragment extends Fragment {
    public final static String BTAG = BattleFragment.class.getName();
    public final static String ROOM_ID = "Room Id";
    public final static int ANIMATION_SHORT = 500;
    public final static int ANIMATION_LONG = 1000;
    public final static int[] BACKGROUND_LIBRARY = {R.drawable.bg, R.drawable.bg_beach, R.drawable.bg_beachshore,
            R.drawable.bg_city, R.drawable.bg_desert, R.drawable.bg_earthycave, R.drawable.bg_forest,
            R.drawable.bg_icecave, R.drawable.bg_meadow, R.drawable.bg_river, R.drawable.bg_route};
    public final static String[] STATS = {"atk", "def", "spa", "spd", "spe", "accuracy", "evasion"};
    public final static String[] STTUS = {"psn", "tox", "frz", "par", "slp", "brn"};
    public final static String[][] TEAMMATES = {{"p1a", "p1b", "p1c"}, {"p2a", "p2b", "p2c"}};
    public final static String[] MORPHS = {"Arceus", "Gourgeist", "Genesect", "Pumpkaboo", "Wormadam"};
    public static Receiver RECEIVER;
    private ArrayDeque<AnimatorSet> mAnimatorSetQueue;
    private Animator mCurrentBattleAnimation;
    private String mRoomId;
    /**
     * 0 if it's a simple watch battle
     * 1 if player is p1
     * -1 if player is p2
     */
    private int mBattling;
    /**
     * false if battle not over
     */
    private boolean mBattleEnd;
    private boolean mMegaEvo = false;
    private boolean mZMove = false;
    private boolean mReOriented = false;
    private boolean mHasAdvertized = false;
    private int mBackground = 0;
    private boolean mTimer;
    private String mPlayer1;
    private String mPlayer2;
    private String mFormat;
    private String mGametype;
    private String mGen;
    private ArrayList<String> mReceivedMessages;
    private ArrayList<PokemonInfo> mPlayer1Team = new ArrayList<>();
    private ArrayList<PokemonInfo> mPlayer2Team = new ArrayList<>();
    private String mCurrentWeather;
    private boolean mWeatherExist;
    private int mRqid;
    private boolean mTeamPreview;
    private boolean mForceSwitch;
    private boolean mBatonPass;
    private boolean mWaiting;
    private int mCurrentActivePokemon = 0;
    private int mTotalActivePokemon = 0;
    private StringBuilder mChooseCommand = new StringBuilder();
    private JSONObject mRequestJson;
    private JSONObject mUndoMessage;
    private int mTeamSize;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_battle_players_log, container, false);
        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            mRoomId = savedInstanceState.getString(ROOM_ID);
            mMegaEvo = savedInstanceState.getBoolean("mega_evo");
            mZMove = savedInstanceState.getBoolean("z_move");
            mFormat = savedInstanceState.getString("format");
            mGametype = savedInstanceState.getString("gametype");
            mHasAdvertized = savedInstanceState.getBoolean("ad");
            mGen = savedInstanceState.getString("gen");
            mCurrentWeather = savedInstanceState.getString("weather");
            mBackground = savedInstanceState.getInt("background");

            String list = savedInstanceState.getString("messages");
            Type listOfTestObject = new TypeToken<ArrayList<String>>() {
            }.getType();
            mReceivedMessages = new Gson().fromJson(list, listOfTestObject);
        }

        if (mRoomId == null) {
            mRoomId = ContainerActivity.lastRoomIdCreated;
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBattling = 0;
        mBattleEnd = false;
        RECEIVER = new Receiver();

        view.findViewById(R.id.battlelog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogFragment = BattleLogDialog.newInstance(mRoomId);
                dialogFragment.show(getActivity().getSupportFragmentManager(), mRoomId);
            }
        });

        if (mBackground == 0) {
            mBackground = new Random().nextInt(BACKGROUND_LIBRARY.length);
        }
        ((ImageView) view.findViewById(R.id.battle_background)).setImageResource(BACKGROUND_LIBRARY[mBackground]);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        final boolean hasMusic = sharedPref.getBoolean("pref_key_music", false);
        if (hasMusic) {
            AudioManager.playBackgroundMusic(mRoomId, getContext());
        }

        view.findViewById(R.id.icon1).setOnClickListener(new PokemonInfoListener(true, 0));
        view.findViewById(R.id.icon2).setOnClickListener(new PokemonInfoListener(true, 1));
        view.findViewById(R.id.icon3).setOnClickListener(new PokemonInfoListener(true, 2));
        view.findViewById(R.id.icon4).setOnClickListener(new PokemonInfoListener(true, 3));
        view.findViewById(R.id.icon5).setOnClickListener(new PokemonInfoListener(true, 4));
        view.findViewById(R.id.icon6).setOnClickListener(new PokemonInfoListener(true, 5));
        view.findViewById(R.id.icon1_o).setOnClickListener(new PokemonInfoListener(false, 0));
        view.findViewById(R.id.icon2_o).setOnClickListener(new PokemonInfoListener(false, 1));
        view.findViewById(R.id.icon3_o).setOnClickListener(new PokemonInfoListener(false, 2));
        view.findViewById(R.id.icon4_o).setOnClickListener(new PokemonInfoListener(false, 3));
        view.findViewById(R.id.icon5_o).setOnClickListener(new PokemonInfoListener(false, 4));
        view.findViewById(R.id.icon6_o).setOnClickListener(new PokemonInfoListener(false, 5));

        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getView() != null) {
                    getView().findViewById(R.id.back).setVisibility(View.GONE);

                    if (isTeamPreview()) {
                        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                            ((AutofitTextView) getView().findViewById(R.id.action_label)).setText("How will you start the battle?");
                        }
                    } else {
                        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                            ((AutofitTextView) getView().findViewById(R.id.action_label)).setText("What will " + getCurrentActivePokemon().getName() + " do?");
                        }

                        if (!getCurrentActivePokemon().getName().contains("-Mega")
                                && getCurrentActivePokemon().canMegaEvo()) {
                            mMegaEvo = false;
                        }
                    }
                }
                MyApplication.getMyApplication().sendClientMessage(mRoomId + "|/undo");
                setRequestJson(getUndoMessage());
                startRequest();
            }
        });

        view.findViewById(R.id.skip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endAllAnimations();
            }
        });

        BattleFieldData.RoomData roomData = BattleFieldData.get(getContext()).getAnimationInstance(mRoomId);
        if (roomData != null) {
            ArrayList<String> serverMessageArchive = roomData.getServerMessageArchive();
            roomData.setServerMessageArchive(null);
            if (serverMessageArchive != null) {
                BattleFieldData.BattleLog battleLog = BattleFieldData.get(getContext()).getRoomDataHashMap().get(mRoomId);
                if (battleLog != null) {
                    battleLog.setChatBox(null);
                }

                mReOriented = true;
                for (String serverMessage : serverMessageArchive) {
                    processServerMessage(serverMessage);
                }
                endAllAnimations();
                mReOriented = false;
            }
        }

        if (view.findViewById(R.id.p1a) != null) {
            view.findViewById(R.id.p1a).setOnClickListener(new PokemonInfoListener(true, 0));
            view.findViewById(R.id.p2a).setOnClickListener(new PokemonInfoListener(false, 0));
        }

        if (!mHasAdvertized) {
            Toast.makeText(getContext(), R.string.loading, Toast.LENGTH_SHORT).show();
            if (Onboarding.get(MyApplication.getMyApplication()).isAdvertising()) {
                // sending advertisement message
                String advertisement = getRoomId() + "|" + MyApplication.getMyApplication().getString(R.string.advertise_message);
                MyApplication.getMyApplication().sendClientMessage(advertisement);
            }
            mHasAdvertized = true;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mBattleEnd = savedInstanceState.getBoolean("battle_end", false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.room_id:
                final String roomId = "http://play.pokemonshowdown.com/" + mRoomId;
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.bar_room_id)
                        .setMessage(roomId)
                        .setPositiveButton("Copy to Clipboard",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ClipboardManager clipboardManager = (ClipboardManager)
                                                getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                        ClipData clip = ClipData.newPlainText(BattleFragment.ROOM_ID, roomId);
                                        clipboardManager.setPrimaryClip(clip);
                                    }
                                }).create().show();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        BattleFieldData.RoomData roomData = BattleFieldData.get(getActivity()).getAnimationInstance(mRoomId);
        if (roomData != null) {
            roomData.setMessageListener(false);

            ArrayList<String> pendingMessages = roomData.getServerMessageOnHold();
            for (String message : pendingMessages) {
                processServerMessage(message);
            }

            roomData.clearServerMessageOnHold();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        BattleFieldData.RoomData roomData = BattleFieldData.get(getActivity()).getAnimationInstance(mRoomId);
        if (roomData != null) {
            roomData.setMessageListener(true);
            endAllAnimations();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ROOM_ID, mRoomId);
        outState.putBoolean("battle_end", mBattleEnd);
        outState.putBoolean("mega_evo", mMegaEvo);
        outState.putBoolean("z_move", mZMove);
        outState.putString("format", mFormat);
        outState.putString("gametype", mGametype);
        outState.putBoolean("ad", mHasAdvertized);
        outState.putString("gen", mGen);
        outState.putString("weather", mCurrentWeather);
        outState.putInt("background", mBackground);

        Type listOfTestObject = new TypeToken<ArrayList<String>>() {
        }.getType();
        String list = new Gson().toJson(mReceivedMessages, listOfTestObject);
        outState.putString("messages", list);
    }

    public void setUpTimer() {
        if (getView() == null) {
            return;
        }

        final TextView timer = (TextView) getView().findViewById(R.id.timer);
        timer.setVisibility(View.VISIBLE);
        mTimer = false;
        timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTimer = !mTimer;
                if (mTimer) {
                    timer.setBackgroundResource(R.drawable.editable_frame_light_red);
                    MyApplication.getMyApplication().sendClientMessage(mRoomId + "|/timer on");
                    Toast.makeText(getContext(), "Battle timer is now ON", Toast.LENGTH_SHORT).show();
                } else {
                    timer.setBackgroundResource(R.drawable.button_battle_action);
                    MyApplication.getMyApplication().sendClientMessage(mRoomId + "|/timer off");
                    Toast.makeText(getContext(), "Battle timer is now OFF", Toast.LENGTH_SHORT).show();
                }
                if (getAnimatorSetQueue().isEmpty() && getRequestJson() != null) {
                    startRequest();
                }
            }
        });
    }

    public void processServerMessage(final String message) {
        if (getView() != null && !isTeamPreview()) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                ((AutofitTextView) getView().findViewById(R.id.action_label)).setText(" ");
            }
        }

        BattleFieldData.RoomData roomData = BattleFieldData.get(getContext()).getAnimationInstance(mRoomId);
        if (roomData != null) {
            roomData.addServerMessageArchive(message);
        }
        new RunWithNet() {
            @Override
            public void runWithNet() throws Exception {
                String processedMessage = message;
                if (mBattling == -1) {
                    processedMessage = message.replace("p1", "p3").replace("p2", "p1").replace("p3", "p2");
                }
                BattleMessage.get().processMajorAction(BattleFragment.this, processedMessage);
            }
        }.run();
    }

    public Animator getCurrentBattleAnimation() {
        return mCurrentBattleAnimation;
    }

    public void setCurrentBattleAnimation(Animator currentBattleAnimation) {
        mCurrentBattleAnimation = currentBattleAnimation;
    }

    public String getPlayer1() {
        if (mPlayer1 == null) {
            mPlayer1 = BattleFieldData.get(getContext()).getAnimationInstance(getRoomId()).getPlayer1();
        }
        return mPlayer1;
    }

    public void setPlayer1(String player1) {
        mPlayer1 = player1;
    }

    public String getRoomId() {
        return mRoomId;
    }

    public String getPlayer2() {
        if (mPlayer2 == null) {
            mPlayer2 = BattleFieldData.get(getContext()).getAnimationInstance(getRoomId()).getPlayer2();
        }
        return mPlayer2;
    }

    public void setPlayer2(String player2) {
        mPlayer2 = player2;
    }

    public String getCurrentWeather() {
        return mCurrentWeather;
    }

    public void setCurrentWeather(String currentWeather) {
        mCurrentWeather = currentWeather;
    }

    public boolean isWeatherExist() {
        return mWeatherExist;
    }

    public void setWeatherExist(boolean weatherExist) {
        mWeatherExist = weatherExist;
    }

    public int getRqid() {
        return mRqid;
    }

    public void setRqid(int rqid) {
        mRqid = rqid;
    }

    public boolean isTeamPreview() {
        return mTeamPreview;
    }

    public void setTeamPreview(boolean teamPreview) {
        mTeamPreview = teamPreview;
    }

    public boolean isForceSwitch() {
        return mForceSwitch;
    }

    public void setForceSwitch(boolean forceSwitch) {
        mForceSwitch = forceSwitch;
    }

    public void setWaiting(boolean waiting) {
        mWaiting = waiting;
    }

    public int getBattling() {
        return mBattling;
    }

    public void setBattling(JSONObject object) throws JSONException {
        String side = object.getJSONObject("side").getString("id");
        if (side.equals("p1")) {
            mBattling = 1;
        } else {
            mBattling = -1;
            switchUpPlayer();
        }
        setUpTimer();
    }

    public void setBattling(int i) {
        mBattling = i;
    }

    public String getFormat() {
        return mFormat;
    }

    public void setFormat(String format) {
        mFormat = format;
    }

    public String getGametype() {
        return mGametype;
    }

    public void setGametype(String gametype) {
        mGametype = gametype;
    }

    public String getGen() {
        return mGen;
    }

    public void setGen(String gen) {
        mGen = gen;
    }

    public boolean getReOriented() {
        return mReOriented;
    }

    public boolean isBattleOver() {
        return mBattleEnd;
    }


    public void switchUpPlayer() {
        // Switch player name
        if (getView() == null) {
            return;
        }

        endAllAnimations();

        String holderString = mPlayer1;
        mPlayer1 = mPlayer2;
        mPlayer2 = holderString;
        ((TextView) getView().findViewById(R.id.username)).setText(mPlayer1);
        ((TextView) getView().findViewById(R.id.username_o)).setText(mPlayer2);

        ArrayList<PokemonInfo> holderTeam = mPlayer1Team;
        mPlayer1Team = null;
        mPlayer1Team = mPlayer2Team;
        mPlayer2Team = null;
        mPlayer2Team = holderTeam;

        // Switch player avatar
        Drawable holderDrawable = ((ImageView) getView().findViewById(R.id.avatar)).getDrawable();
        ((ImageView) getView().findViewById(R.id.avatar)).setImageDrawable(((ImageView) getView().findViewById(R.id.avatar_o)).getDrawable());
        ((ImageView) getView().findViewById(R.id.avatar_o)).setImageDrawable(holderDrawable);

        if (getView().findViewById(getTeamPreviewSpriteId("p1", 0)) != null) {
            for (int i = 0; i < 6; i++) {
                ImageView p1 = (ImageView) getView().findViewById(getTeamPreviewSpriteId("p1", i));
                ImageView p2 = (ImageView) getView().findViewById(getTeamPreviewSpriteId("p2", i));
                holderDrawable = p1.getDrawable();
                p1.setImageDrawable(p2.getDrawable());
                p2.setImageDrawable(holderDrawable);
            }
        }

        String[] team1;
        String[] team2;

        if (getFormat().contains("Doubles") || getFormat().contains("VGC")) {
            team1 = new String[]{"p1a", "p1b"};
            team2 = new String[]{"p2a", "p2b"};
        } else if (getFormat().contains("Triples")) {
            team1 = new String[]{"p1a", "p1b", "p1c"};
            team2 = new String[]{"p2a", "p2b", "p2c"};
        } else {
            team1 = new String[]{"p1a"};
            team2 = new String[]{"p2a"};
        }

        if (getView().findViewById(getPkmLayoutId("p1a")) != null) {
            for (int i = 0; i < team1.length; i++) {
                /**
                 * Our side
                 */
                View team1View = getView().findViewById(getPkmLayoutId(team1[i]));
                CharSequence team1Name = ((TextView) getView().findViewById(getSpriteNameid(team1[i]))).getText();
                Drawable team1Gender = ((ImageView) getView().findViewById(getGenderId(team1[i]))).getDrawable();
                int team1Hp = ((ProgressBar) getView().findViewById(getHpBarId(team1[i]))).getProgress();
                ArrayList<View> team1Statuses = new ArrayList<>();
                LinearLayout team1StatusesParent = (LinearLayout) getView().findViewById(getTempStatusId(team1[i]));
                for (int j = 0; j < team1StatusesParent.getChildCount(); j++) {
                    team1Statuses.add(team1StatusesParent.getChildAt(j));
                }
                team1StatusesParent.removeAllViews();

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
                final boolean isAnimated = sharedPref.getBoolean("pref_key_animated", false);

                final SimpleDraweeView team1Sprite = (SimpleDraweeView) getView().findViewById(getSpriteId(team1[i]));
                PokemonInfo poke1 = mPlayer1Team.get(i);

                String poke1Name = poke1.getName().toLowerCase();
                poke1Name = poke1Name.replace("mega-x", "megax");
                poke1Name = poke1Name.replace("mega-y", "megay");
                poke1Name = poke1Name.replace(" ", "");
                poke1Name = poke1Name.replace(":", "");
                if (poke1Name.contains("ho-oh")) {
                    poke1Name = poke1Name.replace("-", "");
                }
                if (poke1Name.contains("basculin-blue")) {
                    poke1Name = "basculin-bluestriped";
                }

                /**
                 * Opponent side
                 */
                View team2View = getView().findViewById(getPkmLayoutId(team2[i]));
                CharSequence team2Name = ((TextView) getView().findViewById(getSpriteNameid(team2[i]))).getText();
                Drawable team2Gender = ((ImageView) getView().findViewById(getGenderId(team2[i]))).getDrawable();
                int team2Hp = ((ProgressBar) getView().findViewById(getHpBarId(team2[i]))).getProgress();
                ArrayList<View> team2Statuses = new ArrayList<>();
                LinearLayout team2StatusesParent = (LinearLayout) getView().findViewById(getTempStatusId(team2[i]));
                for (int j = 0; j < team2StatusesParent.getChildCount(); j++) {
                    team2Statuses.add(team2StatusesParent.getChildAt(j));
                }
                team2StatusesParent.removeAllViews();

                final SimpleDraweeView team2Sprite = (SimpleDraweeView) getView().findViewById(getSpriteId(team2[i]));
                PokemonInfo poke2 = mPlayer2Team.get(i);
                String poke2Name = poke2.getName().toLowerCase();
                poke2Name = poke2Name.replace("mega-x", "megax");
                poke2Name = poke2Name.replace("mega-y", "megay");
                poke2Name = poke2Name.replace(" ", "");
                poke2Name = poke2Name.replace(":", "");
                if (poke2Name.contains("ho-oh")) {
                    poke2Name = poke2Name.replace("-", "");
                }
                if (poke2Name.contains("basculin-blue")) {
                    poke2Name = "basculin-bluestriped";
                }

                int visibility = team2View.getVisibility();
                String holderTag = "";

                if (team1View.getVisibility() == View.VISIBLE) {
                    team2View.setVisibility(View.VISIBLE);
                    ((TextView) getView().findViewById(getSpriteNameid(team2[i]))).setText(team1Name);
                    ((ImageView) getView().findViewById(getGenderId(team2[i]))).setImageDrawable(team1Gender);
                    ((TextView) getView().findViewById(getHpId(team2[i]))).setText(Integer.toString(team1Hp));
                    ((ProgressBar) getView().findViewById(getHpBarId(team2[i]))).setProgress(team1Hp);

                    if (isAnimated) {
                        String url = team2Sprite.getTag().toString();
                        if (url.contains("-back")) {
                            url = url.replace("-back", "");
                        } else {
                            String firstHalf = url.substring(0, url.lastIndexOf("/")) + "-back";
                            url = firstHalf + url.substring(url.lastIndexOf("/"));
                        }

                        Uri imageUri = Uri.parse(url);

                        DraweeController controller = Fresco.newDraweeControllerBuilder()
                                .setControllerListener(getController(team1Sprite))
                                .setUri(imageUri)
                                .setAutoPlayAnimations(true)
                                .build();

                        team1Sprite.setController(controller);
                        holderTag = team1Sprite.getTag().toString();
                        team1Sprite.setTag(url);
                    } else {
//                            Uri uri = new Uri.Builder()
//                                    .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
//                                    .path(String.valueOf(Pokemon.getPokemonBackSprite(MyApplication.getMyApplication(),
//                                            MyApplication.toId(poke1Name), true, poke1.isFemale(), poke1.isShiny())))
//                                    .build();
//                            team1Sprite.setImageURI(uri);
                        team1Sprite.setImageResource(Pokemon.getPokemonBackSprite(MyApplication.getMyApplication(),
                                MyApplication.toId(poke1Name), true, poke1.isFemale(), poke1.isShiny()));
                        team1Sprite.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    }

                    for (View v : team1Statuses) {
                        team2StatusesParent.addView(v);
                    }
                } else {
                    team2View.setVisibility(team1View.getVisibility());
                }

                if (visibility == View.VISIBLE) {
                    team1View.setVisibility(View.VISIBLE);
                    ((TextView) getView().findViewById(getSpriteNameid(team1[i]))).setText(team2Name);
                    ((ImageView) getView().findViewById(getGenderId(team1[i]))).setImageDrawable(team2Gender);
                    ((TextView) getView().findViewById(getHpId(team1[i]))).setText(Integer.toString(team2Hp));
                    ((ProgressBar) getView().findViewById(getHpBarId(team1[i]))).setProgress(team2Hp);

                    //here
                    if (isAnimated) {
                        String url = holderTag;
                        if (url.contains("-back")) {
                            url = url.replace("-back", "");
                        } else {
                            String firstHalf = url.substring(0, url.lastIndexOf("/")) + "-back";
                            url = firstHalf + url.substring(url.lastIndexOf("/"));
                        }

                        Uri imageUri = Uri.parse(url);

                        DraweeController controller = Fresco.newDraweeControllerBuilder()
                                .setControllerListener(getController(team2Sprite))
                                .setUri(imageUri)
                                .setAutoPlayAnimations(true)
                                .build();

                        team2Sprite.setController(controller);
                        team2Sprite.setTag(url);
                    } else {
//                           Uri uri = new Uri.Builder()
//                                   .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
//                                   .path(String.valueOf(Pokemon.getPokemonFrontSprite(MyApplication.getMyApplication(),
//                           MyApplication.toId(poke2Name), true, poke2.isFemale(), poke2.isShiny())))
//                                   .build();
//                           team2Sprite.setImageURI(uri);
                        team2Sprite.setImageResource(Pokemon.getPokemonFrontSprite(MyApplication.getMyApplication(),
                                MyApplication.toId(poke2Name), true, poke2.isFemale(), poke2.isShiny()));
                        team2Sprite.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    }

                    for (View v : team2Statuses) {
                        team1StatusesParent.addView(v);
                    }
                } else {
                    team1View.setVisibility(team2View.getVisibility());
                }

            }

            int[] p1Field = {R.id.field_lightscreen, R.id.field_reflect, R.id.field_rocks, R.id.field_spikes1,
                    R.id.field_spikes2, R.id.field_spikes3, R.id.field_tspikes1, R.id.field_tspikes2};
            int[] p2Field = {R.id.field_lightscreen_o, R.id.field_reflect_o, R.id.field_rocks_o, R.id.field_spikes1_o,
                    R.id.field_spikes2_o, R.id.field_spikes3_o, R.id.field_tspikes1_o, R.id.field_tspikes2_o};
            for (int i = 0; i < p1Field.length; i++) {
                int visibility;
                View p1 = getView().findViewById(p1Field[i]);
                View p2 = getView().findViewById(p2Field[i]);
                visibility = p1.getVisibility();
                p1.setVisibility(p2.getVisibility());
                p2.setVisibility(visibility);
            }
        }

        for (int i = 0; i < 6; i++) {
            ImageView p1 = (ImageView) getView().findViewById(getIconId("p1", i));
            ImageView p2 = (ImageView) getView().findViewById(getIconId("p2", i));
            holderDrawable = p1.getDrawable();
            float holderAlpha = p1.getAlpha();
            p1.setImageDrawable(p2.getDrawable());
            p1.setAlpha(p2.getAlpha());
            p2.setImageDrawable(holderDrawable);
            p2.setAlpha(holderAlpha);
        }
    }

    private BaseControllerListener<ImageInfo> getController(final SimpleDraweeView view) {
        return new BaseControllerListener<ImageInfo>() {
            @Override
            public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable anim) {
                if (imageInfo == null) {
                    return;
                }

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout
                        .LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

                if (imageInfo.getHeight() < 80) {
                    if (imageInfo.getHeight() <= 50) {
                        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                30, getResources().getDisplayMetrics());
                        params.setMargins(0, px, 0, px);
                        view.setLayoutParams(params);
                    } else if (imageInfo.getHeight() <= 60) {
                        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                25, getResources().getDisplayMetrics());
                        params.setMargins(0, px, 0, px);
                        view.setLayoutParams(params);
                    } else if (imageInfo.getHeight() <= 70) {
                        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                20, getResources().getDisplayMetrics());
                        params.setMargins(0, px, 0, px);
                        view.setLayoutParams(params);
                    } else if (imageInfo.getHeight() <= 80) {
                        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                15, getResources().getDisplayMetrics());
                        params.setMargins(0, px, 0, px);
                        view.setLayoutParams(params);
                    }
                } else {
                    params.setMargins(0, 0, 0, 0);
                    view.setLayoutParams(params);
                }
            }

            @Override
            public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {
            }

            @Override
            public void onFailure(String id, Throwable throwable) {
            }
        };
    }

    public JSONObject getRequestJson() {
        return mRequestJson;
    }

    public void setRequestJson(JSONObject getRequestJson) {
        mRequestJson = getRequestJson;
    }

    public JSONObject getUndoMessage() {
        return mUndoMessage;
    }

    public void setUndoMessage(JSONObject undoMessage) {
        mUndoMessage = undoMessage;
    }

    public AnimatorSet makeMinorToast(final Spannable message) {
        if (getView() == null) {
            return null;
        }
        String logMessage = message.toString();
        if (logMessage.equals("upkeep") || logMessage.contains("choice|")) {
            return new AnimatorSet();
        }
        TextView textView = (TextView) getView().findViewById(R.id.toast);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(textView, "alpha", 0f, 1f);
        fadeIn.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(textView, "alpha", 1f, 0f);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setStartDelay(ANIMATION_SHORT);

        AnimatorSet animation = new AnimatorSet();
        animation.play(fadeIn);
        animation.play(fadeOut).after(fadeIn);
        animation.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationStartWithNet(Animator animation) {
                if (getView() == null) {
                    return;
                }
                TextView toast = (TextView) getView().findViewById(R.id.toast);
                if (toast != null) {
                    toast.setText(message);
                }
            }
        });
        return animation;
    }

    public void makeChatToast(String user, String message) {
        if (mReceivedMessages == null) {
            mReceivedMessages = new ArrayList<>();
        }

        if (!mReceivedMessages.contains(message)) {
            mReceivedMessages.add(message);
            LayoutInflater inflater = getLayoutInflater(null);
            View layout = inflater.inflate(R.layout.dialog_custom_chat_toast,
                    (ViewGroup) getView().findViewById(R.id.custom_toast_container));

            TextView text = (TextView) layout.findViewById(R.id.user);
            text.setText("User \"" + user + "\" said: ");
            text.setTextColor(ChatRoomFragment.getColorStrong(user));

            TextView said = (TextView) layout.findViewById(R.id.message);
            said.setText(message);

            Toast toast = new Toast(getContext());
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();
        }
    }

    public AnimatorSet makeToast(final String message) {
        String logMessage = message.toString();
        if (logMessage.equals("upkeep") || logMessage.contains("choice|")) {
            return new AnimatorSet();
        }
        return makeToast(message, ANIMATION_LONG);
    }

    public AnimatorSet makeToast(final String message, final int duration) {
        String logMessage = message.toString();
        if (logMessage.equals("upkeep") || logMessage.contains("choice|")) {
            return new AnimatorSet();
        }
        return makeToast(new SpannableString(message), duration);
    }

    public AnimatorSet makeToast(final Spannable message, final int duration) {
        String logMessage = message.toString();
        if (logMessage.equals("upkeep") || logMessage.contains("choice|")) {
            return new AnimatorSet();
        }
        if (getView() == null) {
            return null;
        }
        TextView textView = (TextView) getView().findViewById(R.id.toast);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(textView, "alpha", 0f, 1f);
        fadeIn.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(textView, "alpha", 1f, 0f);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setStartDelay(duration);

        AnimatorSet animation = new AnimatorSet();
        animation.play(fadeIn);
        animation.play(fadeOut).after(fadeIn);
        animation.addListener(new AnimatorListenerWithNet() {
            @Override
            public void onAnimationStartWithNet(Animator animation) {
                if (getView() == null) {
                    return;
                }
                TextView toast = (TextView) getView().findViewById(R.id.toast);
                if (toast != null) {
                    toast.setText(message);
                }
            }
        });
        return animation;
    }

    public AnimatorSet makeToast(final Spannable message) {
        return makeToast(message, ANIMATION_LONG);
    }

    public void startAnimation(final AnimatorSet animator, final String serverMessage) {
        if (animator == null) {
            return;
        }

        getActivity().runOnUiThread(new RunWithNet() {
            @Override
            public void runWithNet() {
                animator.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationEndWithNet(Animator animation) {
                        if (getView() != null) {
                            getView().findViewById(R.id.skip).setVisibility(View.GONE);
                        }

                        getAnimatorSetQueue().pollFirst();
                        Animator nextOnQueue = getAnimatorSetQueue().peekFirst();
                        if (nextOnQueue != null) {
                            nextOnQueue.start();
                        } else {
                            startRequest();
                        }
                    }

                    @Override
                    public void onAnimationStartWithNet(final Animator animation) {
                        if (getView() != null) {
                            getView().findViewById(R.id.back).setVisibility(View.GONE);
                            getView().findViewById(R.id.skip).setVisibility(View.VISIBLE);
                        }

                        try {
                            triggerTeamPreview(false);
                        } catch (NullPointerException e) {
                            clearActionFrame();
                        }

                        if (!Onboarding.get(getContext()).isAnimation()) {
                            animation.end();
                        }
                    }


                });

                getAnimatorSetQueue().addLast(animator);

                if (mAnimatorSetQueue.size() == 1) {
                    try {
                        animator.start();
                    } catch (Exception e) {
                        Log.e(RunWithNet.RTAG, serverMessage, e);
                        endAllAnimations();
                    }
                }
            }
        });
    }

    public void addToLog(Spannable logMessage) {
        String message = logMessage.toString();
        if (message.equals("upkeep") || message.contains("choice|")) {
            return;
        }
        BattleFieldData.BattleLog battleLog = BattleFieldData.get(getContext()).getRoomInstance(mRoomId);
        if (battleLog != null && battleLog.isMessageListener()) {
            if (logMessage.length() > 0) {
                battleLog.addServerMessageOnHold(logMessage);
            }
        } else {
            BattleLogDialog battleLogDialog =
                    (BattleLogDialog) getActivity().getSupportFragmentManager().findFragmentByTag(mRoomId);
            if (battleLogDialog != null) {
                if (logMessage.length() > 0) {
                    battleLogDialog.appendToLog(logMessage);
                }
            }
        }
    }

    public ArrayDeque<AnimatorSet> getAnimatorSetQueue() {
        if (mAnimatorSetQueue == null) {
            mAnimatorSetQueue = new ArrayDeque<>();
        }
        return mAnimatorSetQueue;
    }

    /**
     * @param player can be p1 or p2
     */
    public int getTeamPreviewSpriteId(String player, int id) {
        String p = player.substring(0, 2);
        switch (p) {
            case "p1":
                switch (id) {
                    case 0:
                        return R.id.p1a_prev;
                    case 1:
                        return R.id.p1b_prev;
                    case 2:
                        return R.id.p1c_prev;
                    case 3:
                        return R.id.p1d_prev;
                    case 4:
                        return R.id.p1e_prev;
                    case 5:
                        return R.id.p1f_prev;
                    default:
                        return 0;
                }
            case "p2":
                switch (id) {
                    case 0:
                        return R.id.p2a_prev;
                    case 1:
                        return R.id.p2b_prev;
                    case 2:
                        return R.id.p2c_prev;
                    case 3:
                        return R.id.p2d_prev;
                    case 4:
                        return R.id.p2e_prev;
                    case 5:
                        return R.id.p2f_prev;
                    default:
                        return 0;
                }
            default:
                return 0;
        }
    }

    public int getSpriteNameid(String tag) {
        tag = tag.substring(0, 3);
        switch (tag) {
            case "p1a":
                return R.id.p1a_pkm;
            case "p1b":
                return R.id.p1b_pkm;
            case "p1c":
                return R.id.p1c_pkm;
            case "p2a":
                return R.id.p2a_pkm;
            case "p2b":
                return R.id.p2b_pkm;
            case "p2c":
                return R.id.p2c_pkm;
            default:
                return 0;
        }
    }

    public int getIconId(String tag) {
        tag = tag.substring(0, 3);
        switch (tag) {
            case "p1a":
                return R.id.icon1;
            case "p1b":
                return R.id.icon2;
            case "p1c":
                return R.id.icon3;
            case "p2a":
                return R.id.icon1_o;
            case "p2b":
                return R.id.icon2_o;
            case "p2c":
                return R.id.icon3_o;
            default:
                return 0;
        }
    }

    public int getIconId(String player, int id) {
        String p = player.substring(0, 2);
        switch (p) {
            case "p1":
                switch (id) {
                    case 0:
                        return R.id.icon1;
                    case 1:
                        return R.id.icon2;
                    case 2:
                        return R.id.icon3;
                    case 3:
                        return R.id.icon4;
                    case 4:
                        return R.id.icon5;
                    case 5:
                        return R.id.icon6;
                    default:
                        Log.d(BTAG, getTeamString(mPlayer1Team));
                        Log.d(BTAG, getTeamString(mPlayer2Team));
                        return R.id.icon1;
                }
            case "p2":
                switch (id) {
                    case 0:
                        return R.id.icon1_o;
                    case 1:
                        return R.id.icon2_o;
                    case 2:
                        return R.id.icon3_o;
                    case 3:
                        return R.id.icon4_o;
                    case 4:
                        return R.id.icon5_o;
                    case 5:
                        return R.id.icon6_o;
                    default:
                        Log.d(BTAG, getTeamString(mPlayer1Team));
                        Log.d(BTAG, getTeamString(mPlayer2Team));
                        return R.id.icon1_o;
                }
            default:
                Log.d(BTAG, getTeamString(mPlayer1Team));
                Log.d(BTAG, getTeamString(mPlayer2Team));
                return R.id.icon1;
        }
    }

    public int getGenderId(String tag) {
        tag = tag.substring(0, 3);
        switch (tag) {
            case "p1a":
                return R.id.p1a_gender;
            case "p1b":
                return R.id.p1b_gender;
            case "p1c":
                return R.id.p1c_gender;
            case "p2a":
                return R.id.p2a_gender;
            case "p2b":
                return R.id.p2b_gender;
            case "p2c":
                return R.id.p2c_gender;
            default:
                return 0;
        }
    }

    public int getHpId(String tag) {
        tag = tag.substring(0, 3);
        switch (tag) {
            case "p1a":
                return R.id.p1a_hp;
            case "p1b":
                return R.id.p1b_hp;
            case "p1c":
                return R.id.p1c_hp;
            case "p2a":
                return R.id.p2a_hp;
            case "p2b":
                return R.id.p2b_hp;
            case "p2c":
                return R.id.p2c_hp;
            default:
                return 0;
        }
    }

    public int getHpBarId(String tag) {
        tag = tag.substring(0, 3);
        switch (tag) {
            case "p1a":
                return R.id.p1a_bar_hp;
            case "p1b":
                return R.id.p1b_bar_hp;
            case "p1c":
                return R.id.p1c_bar_hp;
            case "p2a":
                return R.id.p2a_bar_hp;
            case "p2b":
                return R.id.p2b_bar_hp;
            case "p2c":
                return R.id.p2c_bar_hp;
            default:
                return 0;
        }
    }

    public PokemonInfo getPokemonInfo(String tag) {
        tag = tag.replaceFirst("\\[(.*?)\\] ", "").substring(0, 3);
        try {
            switch (tag) {
                case "p1a":
                    return mPlayer1Team.get(0);
                case "p1b":
                    return mPlayer1Team.get(1);
                case "p1c":
                    return mPlayer1Team.get(2);
                case "p2a":
                    return mPlayer2Team.get(0);
                case "p2b":
                    return mPlayer2Team.get(1);
                case "p2c":
                    return mPlayer2Team.get(2);
                default:
                    return null;
            }
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public int getTeamSlot(String tag) {
        tag = Character.toString(tag.charAt(2));
        switch (tag) {
            case "a":
                return 0;
            case "b":
                return 1;
            case "c":
                return 2;
            default:
                return 0;
        }
    }

    public void setAddonStatus(String tag, String status) {
        try {
            LinearLayout statusBar = (LinearLayout) getView().findViewById(getTempStatusId(tag));
            if (statusBar.findViewWithTag(status) != null) {
                return;
            }
            TextView stt = new TextView(getContext());
            stt.setTag(status);
            stt.setText(status.toUpperCase());
            stt.setTextSize(10);
            switch (status) {
                case "slp":
                    stt.setBackgroundResource(R.drawable.editable_frame_blackwhite);
                    break;
                case "psn":
                case "tox":
                    stt.setBackgroundResource(R.drawable.editable_frame_light_purple);
                    break;
                case "brn":
                    stt.setBackgroundResource(R.drawable.editable_frame_light_red);
                    break;
                case "par":
                    stt.setBackgroundResource(R.drawable.editable_frame_light_orange);
                    break;
                case "frz":
                    stt.setBackgroundResource(R.drawable.editable_frame);
                    break;
                default:
                    stt.setBackgroundResource(R.drawable.editable_frame);
            }
            stt.setPadding(2, 2, 2, 2);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            stt.setLayoutParams(layoutParams);

            statusBar.addView(stt, 0);
        } catch (NullPointerException e) {
        }
    }

    public int getTempStatusId(String tag) {
        tag = tag.substring(0, 3);
        switch (tag) {
            case "p1a":
                return R.id.p1a_temp_status;
            case "p1b":
                return R.id.p1b_temp_status;
            case "p1c":
                return R.id.p1c_temp_status;
            case "p2a":
                return R.id.p2a_temp_status;
            case "p2b":
                return R.id.p2b_temp_status;
            case "p2c":
                return R.id.p2c_temp_status;
            default:
                return 0;
        }
    }

    public void removeAddonStatus(String tag, String status) {
        try {
            LinearLayout statusBar = (LinearLayout) getView().findViewById(getTempStatusId(tag));

            TextView stt = (TextView) statusBar.findViewWithTag(status);
            if (stt != null) {
                statusBar.removeView(stt);
            }
        } catch (NullPointerException e) {

        }
    }

    public int getSubstitute(String tag) {
        tag = tag.substring(0, 2);
        switch (tag) {
            case "p1":
                return R.drawable.sprites_substitute_back;
            default:
                return R.drawable.sprites_substitute;
        }
    }

    public int getLastVisibleSpike(String tag, boolean nextInvisible) {
        try {
            tag = tag.substring(0, 2);
            switch (tag) {
                case "p1":
                    View layer1 = getView().findViewById(R.id.field_spikes1);
                    if (layer1.getVisibility() == View.INVISIBLE) {
                        return R.id.field_spikes1;
                    } else {
                        View layer2 = getView().findViewById(R.id.field_spikes2);
                        if (layer2.getVisibility() == View.INVISIBLE) {
                            if (nextInvisible) {
                                return R.id.field_spikes2;
                            } else {
                                return R.id.field_spikes1;
                            }
                        } else {
                            View layer3 = getView().findViewById(R.id.field_spikes3);
                            if (layer3.getVisibility() == View.INVISIBLE) {
                                if (nextInvisible) {
                                    return R.id.field_spikes3;
                                } else {
                                    return R.id.field_spikes2;
                                }
                            } else {
                                return R.id.field_spikes3;
                            }
                        }
                    }
                default:
                    layer1 = getView().findViewById(R.id.field_spikes1_o);
                    if (layer1.getVisibility() == View.INVISIBLE) {
                        return R.id.field_spikes1_o;
                    } else {
                        View layer2 = getView().findViewById(R.id.field_spikes2_o);
                        if (layer2.getVisibility() == View.INVISIBLE) {
                            if (nextInvisible) {
                                return R.id.field_spikes2_o;
                            } else {
                                return R.id.field_spikes1_o;
                            }
                        } else {
                            View layer3 = getView().findViewById(R.id.field_spikes3_o);
                            if (layer3.getVisibility() == View.INVISIBLE) {
                                if (nextInvisible) {
                                    return R.id.field_spikes3_o;
                                } else {
                                    return R.id.field_spikes2_o;
                                }
                            } else {
                                return R.id.field_spikes3_o;
                            }
                        }
                    }
            }
        } catch (NullPointerException e) {
            return R.id.field_spikes1;
        }
    }

    public int getLastVisibleTSpike(String tag, boolean nextInvisible) {
        try {
            tag = tag.substring(0, 2);
            switch (tag) {
                case "p1":
                    View layer1 = getView().findViewById(R.id.field_tspikes1);
                    if (layer1.getVisibility() == View.INVISIBLE) {
                        return R.id.field_tspikes1;
                    } else {
                        View layer2 = getView().findViewById(R.id.field_tspikes2);
                        if (layer2.getVisibility() == View.INVISIBLE) {
                            if (nextInvisible) {
                                return R.id.field_tspikes2;
                            } else {
                                return R.id.field_tspikes1;
                            }
                        } else {
                            return R.id.field_tspikes2;
                        }
                    }
                default:
                    layer1 = getView().findViewById(R.id.field_tspikes1_o);
                    if (layer1.getVisibility() == View.INVISIBLE) {
                        return R.id.field_tspikes1_o;
                    } else {
                        View layer2 = getView().findViewById(R.id.field_tspikes2_o);
                        if (layer2.getVisibility() == View.INVISIBLE) {
                            if (nextInvisible) {
                                return R.id.field_tspikes2_o;
                            } else {
                                return R.id.field_tspikes1_o;
                            }
                        } else {
                            return R.id.field_tspikes2_o;
                        }
                    }
            }
        } catch (NullPointerException e) {
            return R.id.field_tspikes1;
        }
    }

    public void hidePokemon(String tag) {
        try {
            RelativeLayout linearLayout;
            int layoutId;

            tag = tag.substring(0, 3);
            switch (tag) {
                case "p1a":
                    layoutId = R.id.p1a;
                    break;
                case "p1b":
                    layoutId = R.id.p1b;
                    break;
                case "p1c":
                    layoutId = R.id.p1c;
                    break;
                case "p2a":
                    layoutId = R.id.p2a;
                    break;
                case "p2b":
                    layoutId = R.id.p2b;
                    break;
                case "p2c":
                    layoutId = R.id.p2c;
                    break;
                default:
                    layoutId = R.id.p2c;
            }

            linearLayout = (RelativeLayout) getView().findViewById(layoutId);
            linearLayout.setVisibility(View.INVISIBLE);
            linearLayout.invalidate();
        } catch (NullPointerException e) {

        }
    }

    public String getTeamString(ArrayList<PokemonInfo> team) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        for (PokemonInfo pkm : team) {
            stringBuilder.append(pkm.getName()).append("|");
        }
        return stringBuilder.toString();
    }

    public void displayPokemon(String tag) {
        try {
            RelativeLayout linearLayout;
            int layoutId;

            tag = tag.substring(0, 3);
            switch (tag) {
                case "p1a":
                    layoutId = R.id.p1a;
                    break;
                case "p1b":
                    layoutId = R.id.p1b;
                    break;
                case "p1c":
                    layoutId = R.id.p1c;
                    break;
                case "p2a":
                    layoutId = R.id.p2a;
                    break;
                case "p2b":
                    layoutId = R.id.p2b;
                    break;
                default:
                    layoutId = R.id.p2c;
            }
            linearLayout = (RelativeLayout) getView().findViewById(layoutId);
            linearLayout.setVisibility(View.VISIBLE);
            getView().findViewById(getSpriteId(tag)).setAlpha(1f);
            if (!isBatonPass()) {
                ((LinearLayout) getView().findViewById(getTempStatusId(tag))).removeAllViews();
            } else {
                // baton pass removes staties (par,slp,frz,brn,tox,psn)
                removeAddonStatus(tag, "slp");
                removeAddonStatus(tag, "psn");
                removeAddonStatus(tag, "tox");
                removeAddonStatus(tag, "brn");
                removeAddonStatus(tag, "par");
                removeAddonStatus(tag, "frz");
            }
            ImageView sub = (ImageView) linearLayout.findViewWithTag("Substitute");
            if (sub != null) {
                if (!isBatonPass()) {
                    linearLayout.removeView(sub);
                } else {
                    getView().findViewById(getSpriteId(tag)).setAlpha(0.2f);
                }
            }
            setBatonPass(false);
        } catch (NullPointerException e) {

        }
    }

    public int getSpriteId(String tag) {
        tag = tag.substring(0, 3);
        switch (tag) {
            case "p1a":
                return R.id.p1a_icon;
            case "p1b":
                return R.id.p1b_icon;
            case "p1c":
                return R.id.p1c_icon;
            case "p2a":
                return R.id.p2a_icon;
            case "p2b":
                return R.id.p2b_icon;
            case "p2c":
                return R.id.p2c_icon;
            default:
                return 0;
        }
    }

    public boolean isBatonPass() {
        return mBatonPass;
    }

    public void setBatonPass(boolean batonPass) {
        mBatonPass = batonPass;
    }

    public void formChange(String playerTag, String newPkm) {
        PokemonInfo oldInfo, newInfo;
        ArrayList<PokemonInfo> team;
        if (playerTag.startsWith("p1")) {
            team = getPlayer1Team();
        } else {
            team = getPlayer2Team();
        }

        oldInfo = getPokemonInfo(playerTag);
        int index = findPokemonInTeam(team, oldInfo.getName());
        newInfo = new PokemonInfo(getContext(), newPkm);
        newInfo.setNickname(oldInfo.getNickname());
        newInfo.setLevel(oldInfo.getLevel());
        newInfo.setGender(oldInfo.getGender());
        newInfo.setShiny(oldInfo.isShiny());
        newInfo.setActive(oldInfo.isActive());
        newInfo.setHp(oldInfo.getHp());
        newInfo.setStatus(oldInfo.getStatus());
        newInfo.setMoves(oldInfo.getMoves());
        newInfo.setItem(oldInfo.getItem());
        team.set(index, newInfo);
    }

    public ArrayList<PokemonInfo> getPlayer1Team() {
        return mPlayer1Team;
    }

    public void setPlayer1Team(ArrayList<PokemonInfo> player1Team) {
        mPlayer1Team = player1Team;
    }

    public ArrayList<PokemonInfo> getPlayer2Team() {
        return mPlayer2Team;
    }

    public void setPlayer2Team(ArrayList<PokemonInfo> player2Team) {
        mPlayer2Team = player2Team;
    }

    public int findPokemonInTeam(ArrayList<PokemonInfo> playerTeam, String pkm) {
        boolean special = false;
        String species = "";
        for (String sp : MORPHS) {
            if (pkm.contains(sp)) {
                special = true;
                species = sp;
                break;
            }
        }

        ArrayList<String> teamName = getTeamNameArrayList(playerTeam);

        if (!special) {
            return teamName.indexOf(pkm);
        } else {
            for (int i = 0; i < teamName.size(); i++) {
                if (teamName.get(i).contains(species)) {
                    return i;
                }
            }
            return -1;
        }
    }

    public ArrayList<String> getTeamNameArrayList(ArrayList<PokemonInfo> playerTeam) {
        ArrayList<String> teamName = new ArrayList<>();
        for (PokemonInfo pkm : playerTeam) {
            teamName.add(pkm.getName());
        }
        return teamName;
    }

    public ArrayList<PokemonInfo> getTeam(String playerTag) {
        if (playerTag.startsWith("p1")) {
            return mPlayer1Team;
        } else {
            return mPlayer2Team;
        }
    }

    public void setTeam(String playerTag, ArrayList<PokemonInfo> playerTeam) {
        if (playerTag.startsWith("p1")) {
            mPlayer1Team = playerTeam;
        } else {
            mPlayer2Team = playerTeam;
        }
    }

    public String[] getTeamNameStringArray(ArrayList<PokemonInfo> teamMap) {
        String[] team = new String[teamMap.size()];
        for (Integer i = 0; i < teamMap.size(); i++) {
            PokemonInfo pkm = teamMap.get(i);
            team[i] = pkm.getName();
        }
        return team;
    }

    public String getPrintableOutputPokemonSide(String split) {
        return getPrintableOutputPokemonSide(split, true);
    }

    public String getPrintableOutputPokemonSide(String split, boolean start) {
        StringBuilder sb = new StringBuilder();
        if (split.startsWith("p2")) {
            if (start) {
                sb.append("The opposing ");
            } else {
                sb.append("the opposing ");
            }
        }

        int separator = split.indexOf(':');
        String toAppend = (separator == -1) ? split.trim() : split.substring(separator + 1).trim();
        sb.append(toAppend);
        return sb.toString();
    }

    public String trimOrigin(String fromEffectOfSource) {
        if (fromEffectOfSource == null) {
            return null;
        }
        return MyApplication.toId(getPrintable(fromEffectOfSource.replaceFirst("\\[(.*?)\\] ", "")));
    }

    public String getPrintable(String split) {
        int separator = split.indexOf(':');
        return (separator == -1) ? split.trim() : split.substring(separator + 1).trim();
    }

    public void processBoost(String playerTag, String stat, int boost) {
        try {
            LinearLayout tempStat = (LinearLayout) getView().findViewById(getTempStatusId(playerTag));
            TextView statBoost;
            int currentBoost;
            int index;
            if (tempStat.findViewWithTag(stat) == null) {
                statBoost = new TextView(getContext());
                statBoost.setTag(stat);
                statBoost.setTextSize(10);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                statBoost.setLayoutParams(layoutParams);
                currentBoost = boost;
                index = tempStat.getChildCount();
            } else {
                statBoost = (TextView) tempStat.findViewWithTag(stat);
                index = tempStat.indexOfChild(statBoost);
                tempStat.removeView(statBoost);
                String boostDetail = statBoost.getText().toString();
                currentBoost = Integer.parseInt(boostDetail.substring(0, boostDetail.indexOf(" "))) + boost;
            }
            if (currentBoost == 0) {
                return;
            } else {
                if (currentBoost > 0) {
                    statBoost.setBackgroundResource(R.drawable.editable_frame);
                } else {
                    statBoost.setBackgroundResource(R.drawable.editable_frame_light_orange);
                }
            }
            statBoost.setText(Integer.toString(currentBoost) + " " + stat.substring(0, 1).toUpperCase() + stat.substring(1));
            statBoost.setPadding(2, 2, 2, 2);
            tempStat.addView(statBoost, index);
        } catch (NullPointerException e) {

        }
    }

    public void invertBoost(String playerTag, String[] stats) {
        try {
            LinearLayout tempStat = (LinearLayout) getView().findViewById(getTempStatusId(playerTag));
            for (String stat : stats) {
                TextView statBoost = (TextView) tempStat.findViewWithTag(stat);
                if (statBoost != null) {
                    String boostDetail = statBoost.getText().toString();
                    int currentBoost = -1 * Integer.parseInt(boostDetail.substring(0, boostDetail.indexOf(" ")));
                    statBoost.setText(Integer.toString(currentBoost) + boostDetail.substring(boostDetail.indexOf(" ")));
                }
            }
        } catch (NullPointerException e) {

        }
    }

    public void restoreBoost(String playerTag) {
        try {
            LinearLayout tempStat = (LinearLayout) getView().findViewById(getTempStatusId(playerTag));
            for (String stat : STATS) {
                TextView statBoost = (TextView) tempStat.findViewWithTag(stat);
                if (statBoost != null) {
                    String boostDetail = statBoost.getText().toString();
                    int currentBoost = Integer.parseInt(boostDetail.substring(0, boostDetail.indexOf(" ")));
                    if (currentBoost < 0) {
                        tempStat.removeView(statBoost);
                    }
                }
            }
        } catch (NullPointerException e) {

        }
    }

    public void swapBoost(String org, String dest, String... stats) {
        org = org.substring(0, 3);
        dest = dest.substring(0, 3);

        try {

            LinearLayout orgTempStat = (LinearLayout) getView().findViewById(getTempStatusId(org));
            LinearLayout destTempStat = (LinearLayout) getView().findViewById(getTempStatusId(dest));

            for (String stat : stats) {
                TextView orgStat = (TextView) orgTempStat.findViewWithTag(stat);
                int orgIndex = orgTempStat.indexOfChild(orgStat);
                TextView destStat = (TextView) destTempStat.findViewWithTag(stat);
                int destIndex = destTempStat.indexOfChild(destStat);
                orgIndex = (orgIndex == -1) ? orgTempStat.getChildCount() : orgIndex;
                orgTempStat.removeView(orgStat);
                destIndex = (destIndex == -1) ? destTempStat.getChildCount() : destIndex;
                destTempStat.removeView(destStat);

                if (destStat != null) {
                    orgTempStat.addView(destStat, orgIndex);
                }
                if (orgStat != null) {
                    destTempStat.addView(orgStat, destIndex);
                }
            }
        } catch (NullPointerException e) {

        }
    }

    public void copyBoost(String org, String dest) {
        org = org.substring(0, 3);
        dest = dest.substring(0, 3);

        try {

            LinearLayout orgTempStat = (LinearLayout) getView().findViewById(getTempStatusId(org));
            LinearLayout destTempStat = (LinearLayout) getView().findViewById(getTempStatusId(dest));

            for (String stat : STATS) {
                TextView orgStat = (TextView) orgTempStat.findViewWithTag(stat);
                if (orgStat != null) {
                    TextView destStat = new TextView(getContext());
                    destStat.setTag(stat);
                    destStat.setPadding(2, 2, 2, 2);
                    destStat.setTextSize(10);
                    destStat.setText(orgStat.getText());
                    destStat.setBackground(orgStat.getBackground());
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    destStat.setLayoutParams(layoutParams);
                    destTempStat.addView(destStat);
                }
            }
        } catch (NullPointerException e) {

        }
    }

    public void addCommand(String command) {
        String chosen = mChooseCommand.toString();
        if (chosen.length() != 0) {
            mChooseCommand.append(",");
        }
        mChooseCommand.append(command);
        clearActionFrame();
    }

    private void sendCommands(StringBuilder command) {
        command.insert(0, getRoomId());
        command.append("|").append(getRqid());
        MyApplication.getMyApplication().sendClientMessage(command.toString());

        if (getView() != null) {
            getView().findViewById(R.id.back).setVisibility(View.VISIBLE);
        }

        setRequestJson(null);
    }

    private PokemonInfo getCurrentActivePokemon() {
        return getPlayer1Team().get(mCurrentActivePokemon);
    }

    public void processSwitch(int id) throws JSONException {
        if (isTeamPreview()) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                ((AutofitTextView) getView().findViewById(R.id.action_label)).setText(getPlayer1Team().get(id).getName()
                        + " will be sent out first.");
            }
            chooseLeadInTeamPreview(id);
        } else {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                ((AutofitTextView) getView().findViewById(R.id.action_label)).setText(getPlayer1Team().get(id).getName()
                        + " will replace " + getCurrentActivePokemon().getName());
            }
            chooseSwitch(id);
        }
    }

    public void chooseLeadInTeamPreview(int id) {
        String chosen = mChooseCommand.toString();
        if (chosen.contains(Integer.toString(id + 1))) {
            return;
        }

        if (getView() != null) {
            int chosenSprite = getTeamPreviewSpriteId("p1", id);
            getView().findViewById(chosenSprite).setAlpha(0.5f);
        }

        mChooseCommand.append(id + 1);
        mCurrentActivePokemon++;

        chosen = mChooseCommand.toString();
        int totalActive = (getTeamSize() > 0) ? getTeamSize() : mTotalActivePokemon;
        if (mCurrentActivePokemon == totalActive) {
            ArrayList<Integer> lineUp = new ArrayList<>();
            for (int i = 0; i < mPlayer1Team.size(); i++) {
                lineUp.add(i + 1); // 1 2 3 4 5 6
                // here we reset the active flags for all the pokemons
                // this is necessary for vgc as the pokemon 1 and 2 are active and if they are not selected they stay active until the end of the game since the next request item doenst have them in
                mPlayer1Team.get(i).setActive(false);
            }

            // starting with user selection
            for (int i = 0; i < chosen.length(); i++) {
                // between 1 and 6, we find the place in the arayè and switch them
                int newValue = Integer.parseInt(Character.toString(chosen.charAt(i)));
                int idxNewValue = lineUp.indexOf(newValue);

                int oldValue = lineUp.get(i);
                int idxOldValue = i;

                lineUp.set(idxOldValue, newValue);
                lineUp.set(idxNewValue, oldValue);
            }

            mChooseCommand = new StringBuilder();
            mChooseCommand.append("|/team ");

            for (int i = 0; i < mPlayer1Team.size(); i++) {
                mChooseCommand.append(lineUp.get(i));
            }

            triggerTeamPreview(false);
            sendCommands(mChooseCommand);
        }
    }

    public void chooseForceSwitch(JSONArray forceSwitch) throws JSONException {
        if (mCurrentActivePokemon == mTotalActivePokemon) {
            mChooseCommand.insert(0, "|/choose ");
            sendCommands(mChooseCommand);
            setForceSwitch(false);
            return;
        }

        if (forceSwitch.getBoolean(mCurrentActivePokemon)) {
            triggerSwitchOptions(true);
        } else {
            mCurrentActivePokemon++;
            addCommand("pass");
            chooseForceSwitch(forceSwitch);
        }
    }

    public void chooseSwitch(int id) throws JSONException {
        String chosen = mChooseCommand.toString();
        if (chosen.contains("switch " + (id + 1))) {
            return;
        }
        addCommand("switch " + Integer.toString(id + 1));
        mCurrentActivePokemon++;

        if (mCurrentActivePokemon == mTotalActivePokemon) {
            mChooseCommand.insert(0, "|/choose ");
            sendCommands(mChooseCommand);
        } else {
            if (isForceSwitch()) {
                try {
                    chooseForceSwitch(getRequestJson().getJSONArray("forceSwitch"));
                } catch (JSONException e) {
                    return;
                }
            } else {
                startAction(getRequestJson().getJSONArray("active"));
            }
        }
    }

    public void resetChooseCommand() {
        mChooseCommand = new StringBuilder();
        mCurrentActivePokemon = 0;
        mTotalActivePokemon = 0;
        for (PokemonInfo pokemonInfo : getPlayer1Team()) {
            if (pokemonInfo.isActive()) {
                mTotalActivePokemon++;
            }
        }
    }

    public void startRequest() {
        if (getRequestJson() == null) {
            if (getBattling() != 0 && !mBattleEnd) {
                new AlertDialog.Builder(getContext())
                        .setMessage("Oops, you skipped too quickly. Try tapping on Timer button :)")
                        .create().show();
            }
            return;
        }

        new RunWithNet() {
            @Override
            public void runWithNet() throws Exception {
                JSONObject requestJson = getRequestJson();

                if (requestJson.has(("side"))) {
                    String side = requestJson.getJSONObject("side").getString("id");
                    if (getBattling() == 1 && side.equals("p2")) {
                        setBattling(-1);
                        switchUpPlayer();
                    }
                }

                JSONArray teamJson = requestJson.getJSONObject("side").getJSONArray("pokemon");
                setPlayer1Team(new ArrayList<PokemonInfo>());

                for (int i = 0; i < teamJson.length(); i++) {
                    JSONObject info = teamJson.getJSONObject(i);
                    final PokemonInfo pkm = BattleMessage.get().parsePokemonInfo(BattleFragment.this, info);
                    getPlayer1Team().add(pkm);
                    final int pos = i;

                    if (getActivity() == null) {
                        return;
                    }

                    getActivity().runOnUiThread(new RunWithNet() {
                        @Override
                        public void runWithNet() {
                            if (getView() == null) {
                                return;
                            }

                            int pkmIcon = Pokemon.getPokemonIcon(getContext(),
                                    MyApplication.toId(pkm.getName()));
                            ImageView icon = (ImageView) getView().findViewById(getIconId("p1", pos));
                            icon.setImageResource(pkmIcon);
                            float alpha = pkm.getHp() == 0 ? 0.5f : 1f;
                            icon.setAlpha(alpha);
                        }
                    });
                }
                for (int i = teamJson.length(); i < 6; i++) {
                    // we set the rest to empty balls (for vgc)
                    final int finalI = i;
                    new RunWithNet() {
                        @Override
                        public void runWithNet() {
                            if (getView() == null) {
                                return;
                            }
                            ImageView icon = (ImageView) getView().findViewById(getIconId("p1", finalI));
                            icon.setImageResource(R.drawable.pokeball_none);
                        }
                    }.run();

                }

                setRqid(requestJson.optInt("rqid", 0));
                setTeamPreview(requestJson.optBoolean("teamPreview", false));
                setWaiting(requestJson.optBoolean("wait", false));

                if (isTeamPreview()) {
                    resetChooseCommand();
                    triggerTeamPreview(true);
                } else {
                    if (getRqid() != 0) {
                        resetChooseCommand();
                        if (requestJson.has("forceSwitch")) {
                            setForceSwitch(true);
                            JSONArray forceSwitchJsonArray = requestJson.getJSONArray("forceSwitch");
                            chooseForceSwitch(forceSwitchJsonArray);
                        } else {
                            startAction(requestJson.getJSONArray("active"));
                        }
                    }
                }
            }
        }.run();
    }

    public void startAction(final JSONArray active) {
        if (mWaiting) {
            return;
        }

        triggerSwitchOptions(true);
        triggerAttackOptions(active);
    }

    private void triggerAttackOptions(final JSONArray active) {
        if (getView() == null) {
            return;
        } else {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                ((AutofitTextView) getView().findViewById(R.id.action_label)).setText("What will " + getCurrentActivePokemon().getName() + " do?");
            }

            mMegaEvo = !(!getCurrentActivePokemon().getName().contains("-Mega")
                    && getCurrentActivePokemon().canMegaEvo());
        }

        FrameLayout frameLayout = (FrameLayout) getView().findViewById(R.id.action_interface);
        frameLayout.removeAllViews();

        getActivity().getLayoutInflater().inflate(R.layout.fragment_battle_action_moves, frameLayout);
        final LinearLayout[] moveViews = new LinearLayout[4];
        moveViews[0] = (LinearLayout) getView().findViewById(R.id.active_move1);
        moveViews[1] = (LinearLayout) getView().findViewById(R.id.active_move2);
        moveViews[2] = (LinearLayout) getView().findViewById(R.id.active_move3);
        moveViews[3] = (LinearLayout) getView().findViewById(R.id.active_move4);
        final AutofitTextView[] moveNames = new AutofitTextView[4];
        moveNames[0] = (AutofitTextView) getView().findViewById(R.id.active_move1_name);
        moveNames[1] = (AutofitTextView) getView().findViewById(R.id.active_move2_name);
        moveNames[2] = (AutofitTextView) getView().findViewById(R.id.active_move3_name);
        moveNames[3] = (AutofitTextView) getView().findViewById(R.id.active_move4_name);
        final TextView[] movePps = new TextView[4];
        movePps[0] = (TextView) getView().findViewById(R.id.active_move1_pp);
        movePps[1] = (TextView) getView().findViewById(R.id.active_move2_pp);
        movePps[2] = (TextView) getView().findViewById(R.id.active_move3_pp);
        movePps[3] = (TextView) getView().findViewById(R.id.active_move4_pp);
        final ImageView[] moveIcons = new ImageView[4];
        moveIcons[0] = (ImageView) getView().findViewById(R.id.active_move1_icon);
        moveIcons[1] = (ImageView) getView().findViewById(R.id.active_move2_icon);
        moveIcons[2] = (ImageView) getView().findViewById(R.id.active_move3_icon);
        moveIcons[3] = (ImageView) getView().findViewById(R.id.active_move4_icon);

        PokemonInfo currentPokemonInfo = getCurrentActivePokemon();
        CheckBox megaBox = (CheckBox) getView().findViewById(R.id.mega_evolution_checkbox);
        CheckBox zMoveBox = (CheckBox) getView().findViewById(R.id.zmove_checkbox);

//        if (currentPokemonInfo.canMegaEvo() && mMegaEvo && !currentPokemonInfo.getName().contains("-Mega")) {
//            mMegaEvo = false;
//        }

        if (currentPokemonInfo.canMegaEvo() && !mMegaEvo) {
            megaBox.setVisibility(View.VISIBLE);
            zMoveBox.setVisibility(View.GONE);
        } else if (currentPokemonInfo.canZMove(getContext()) && !mZMove) {
            megaBox.setVisibility(View.GONE);
            zMoveBox.setVisibility(View.VISIBLE);
        } else {
            megaBox.setVisibility(View.GONE);
            zMoveBox.setVisibility(View.GONE);
        }

        zMoveBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    String crystal = getCurrentActivePokemon().getItemName(getContext());
                    List<String> types = new ArrayList<>();
                    List<String> power = new ArrayList<>();
                    List<String> boost = new ArrayList<>();
                    String type;
                    String zPower = "null";
                    String zBoost = "null";
                    List<JSONObject> moves = new ArrayList<JSONObject>();

                    try {
                        JSONArray atk = active.getJSONObject(mCurrentActivePokemon).getJSONArray("moves");
                        for (int i = 0; i < atk.length(); i++) {
                            JSONObject obj = MoveDex.get(getContext()).getMoveJsonObject(atk.getJSONObject(i).getString("id"));
                            moves.add(obj);

                            type = obj.getString("type");
                            if (obj.has("zMovePower")) {
                                zPower = obj.getString("zMovePower");
                            }
                            if (obj.has("zMoveBoost")) {
                                zBoost = obj.getString("zMoveBoost");
                            }

                            types.add(type);
                            power.add(zPower);
                            boost.add(zBoost);
                        }

                        String zType = "";
                        String zName = "";
                        switch (crystal) {
                            // Exclusive crystals
                            case "Decidium Z":
                                for (int i = 0; i < types.size(); i++) {
                                    if (moves.get(i).getString("name").equals("Spirit Shackle")) {
                                        moveNames[i].setText("Sinister Arrow Raid");
                                        movePps[i].setText("1/1");
                                    } else {
                                        moveNames[i].setText("");
                                        movePps[i].setText("");
                                        moveIcons[i].setImageResource(0);
                                        moveViews[i].setBackgroundResource(0);
                                    }
                                }
                                return;
                            case "Incinium Z":
                                for (int i = 0; i < types.size(); i++) {
                                    if (moves.get(i).getString("name").equals("Darkest Lariat")) {
                                        moveNames[i].setText("Malicious Moonsault");
                                        movePps[i].setText("1/1");
                                    } else {
                                        moveNames[i].setText("");
                                        movePps[i].setText("");
                                        moveIcons[i].setImageResource(0);
                                        moveViews[i].setBackgroundResource(0);
                                    }
                                }
                                return;
                            case "Primarium Z":
                                for (int i = 0; i < types.size(); i++) {
                                    if (moves.get(i).getString("name").equals("Sparkling Aria")) {
                                        moveNames[i].setText("Oceanic Operetta");
                                        movePps[i].setText("1/1");
                                    } else {
                                        moveNames[i].setText("");
                                        movePps[i].setText("");
                                        moveIcons[i].setImageResource(0);
                                        moveViews[i].setBackgroundResource(0);
                                    }
                                }
                                return;
                            case "Aloraichium Z":
                                for (int i = 0; i < types.size(); i++) {
                                    if (moves.get(i).getString("name").equals("Thunderbolt")) {
                                        moveNames[i].setText("Stoked Sparksurfer");
                                        movePps[i].setText("1/1");
                                    } else {
                                        moveNames[i].setText("");
                                        movePps[i].setText("");
                                        moveIcons[i].setImageResource(0);
                                        moveViews[i].setBackgroundResource(0);
                                    }
                                }
                                return;
                            case "Eevium Z":
                                for (int i = 0; i < types.size(); i++) {
                                    if (moves.get(i).getString("name").equals("Last Resort")) {
                                        moveNames[i].setText("Extreme Evoboost");
                                        movePps[i].setText("1/1");
                                    } else {
                                        moveNames[i].setText("");
                                        movePps[i].setText("");
                                        moveIcons[i].setImageResource(0);
                                        moveViews[i].setBackgroundResource(0);
                                    }
                                }
                                return;
                            case "Marshadium Z":
                                for (int i = 0; i < types.size(); i++) {
                                    if (moves.get(i).getString("name").equals("Spectral Thief")) {
                                        moveNames[i].setText("Soul-Stealing 7-Star Strike");
                                        movePps[i].setText("1/1");
                                    } else {
                                        moveNames[i].setText("");
                                        movePps[i].setText("");
                                        moveIcons[i].setImageResource(0);
                                        moveViews[i].setBackgroundResource(0);
                                    }
                                }
                                return;
                            case "Mewnium Z":
                                for (int i = 0; i < types.size(); i++) {
                                    if (moves.get(i).getString("name").equals("Psychic")) {
                                        moveNames[i].setText("Genesis Supernova");
                                        movePps[i].setText("1/1");
                                    } else {
                                        moveNames[i].setText("");
                                        movePps[i].setText("");
                                        moveIcons[i].setImageResource(0);
                                        moveViews[i].setBackgroundResource(0);
                                    }
                                }
                                return;
                            case "Pikanium Z":
                                for (int i = 0; i < types.size(); i++) {
                                    if (moves.get(i).getString("name").equals("Volt Tackle")) {
                                        moveNames[i].setText("Catastropika");
                                        movePps[i].setText("1/1");
                                    } else {
                                        moveNames[i].setText("");
                                        movePps[i].setText("");
                                        moveIcons[i].setImageResource(0);
                                        moveViews[i].setBackgroundResource(0);
                                    }
                                }
                                return;
                            case "Pikashunium Z":
                                for (int i = 0; i < types.size(); i++) {
                                    if (moves.get(i).getString("name").equals("Thunderbolt")) {
                                        moveNames[i].setText("10,000,000 Volt Thunderbolt");
                                        movePps[i].setText("1/1");
                                    } else {
                                        moveNames[i].setText("");
                                        movePps[i].setText("");
                                        moveIcons[i].setImageResource(0);
                                        moveViews[i].setBackgroundResource(0);
                                    }
                                }
                                return;
                            case "Snorlium Z":
                                for (int i = 0; i < types.size(); i++) {
                                    if (moves.get(i).getString("name").equals("Giga Impact")) {
                                        moveNames[i].setText("Pulverizing Pancake");
                                        movePps[i].setText("1/1");
                                    } else {
                                        moveNames[i].setText("");
                                        movePps[i].setText("");
                                        moveIcons[i].setImageResource(0);
                                        moveViews[i].setBackgroundResource(0);
                                    }
                                }
                                return;
                            case "Tapunium Z":
                                for (int i = 0; i < types.size(); i++) {
                                    if (moves.get(i).getString("name").equals("Nature's Madness")) {
                                        moveNames[i].setText("Guardian of Alola");
                                        movePps[i].setText("1/1");
                                    } else {
                                        moveNames[i].setText("");
                                        movePps[i].setText("");
                                        moveIcons[i].setImageResource(0);
                                        moveViews[i].setBackgroundResource(0);
                                    }
                                }
                                return;
                            case "Buginium Z":
                                zType = "Bug";
                                zName = "Savage Spin-Out";
                                break;
                            case "Darkinium Z":
                                zType = "Dark";
                                zName = "Black Hole Eclipse";
                                break;
                            case "Dragonium Z":
                                zType = "Dragon";
                                zName = "Devastating Drake";
                                break;
                            case "Electrium Z":
                                zType = "Electric";
                                zName = "Gigavolt Havoc";
                                break;
                            case "Fairium Z":
                                zType = "Fairy";
                                zName = "Twinkle Tackle";
                                break;
                            case "Fightinium Z":
                                zType = "Fighting";
                                zName = "All-Out Pummeling";
                                break;
                            case "Firium Z":
                                zType = "Fire";
                                zName = "Inferno Overdrive";
                                break;
                            case "Flyinium Z":
                                zType = "Flying";
                                zName = "Supersonic Skystrike";
                                break;
                            case "Ghostium Z":
                                zType = "Ghost";
                                zName = "Never-Ending Nightmare";
                                break;
                            case "Grassium Z":
                                zType = "Grass";
                                zName = "Bloom Doom";
                                break;
                            case "Groundium Z":
                                zType = "Ground";
                                zName = "Tectonic Rage";
                                break;
                            case "Icium Z":
                                zType = "Ice";
                                zName = "Subzero Slammer";
                                break;
                            case "Normalium Z":
                                zType = "Normal";
                                zName = "Breakneck Blitz";
                                break;
                            case "Poisonium Z":
                                zType = "Poison";
                                zName = "Acid Downpour";
                                break;
                            case "Psychium Z":
                                zType = "Psychic";
                                zName = "Shattered Psyche";
                                break;
                            case "Rockium Z":
                                zType = "Rock";
                                zName = "Continental Crush";
                                break;
                            case "Steelium Z":
                                zType = "Steel";
                                zName = "Corkscrew Crash";
                                break;
                            case "Waterium Z":
                                zType = "Water";
                                zName = "Hydro Vortex";
                                break;
                        }

                        for (int i = 0; i < types.size(); i++) {
                            if (types.get(i).equals(zType)) {
                                if (moves.get(i).getString("category").equals("Status")) {
                                    moveNames[i].setText("Z-" + moves.get(i).getString("name"));
                                } else {
                                    moveNames[i].setText(zName);
                                }
                                movePps[i].setText("1/1");
                            } else {
                                moveNames[i].setText("");
                                movePps[i].setText("");
                                moveIcons[i].setImageResource(0);
                                moveViews[i].setBackgroundResource(0);
                            }
                        }
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    setupMovesView(active, moveViews, moveNames, movePps, moveIcons);
                }
            }
        });

        setupMovesView(active, moveViews, moveNames, movePps, moveIcons);
    }

    private void setupMovesView(final JSONArray active, LinearLayout[] moveViews, AutofitTextView[] moveNames,
                                TextView[] movePps, ImageView[] moveIcons) {
        try {
            JSONObject currentActive = active.getJSONObject(mCurrentActivePokemon);
            JSONArray moves = currentActive.getJSONArray("moves");

            if (moves.getJSONObject(0).getString("move").equals("Struggle")) {
                parseMoveCommandAndSend(active, 0, 0);
                return;
            }

            for (int i = 0; i < moves.length(); i++) {
                final JSONObject moveJson = moves.getJSONObject(i);
                String move = moveJson.getString("move");
                if (move.contains("Return") || move.contains("Frustration")) {
                    move = move.replaceAll("[^A-z]", "");
                }
                moveNames[i].setText(move);
                if (moveJson.optString("maxpp", "0").equals("0")) {
                    //sttruggle has noppinfo
                    movePps[i].setText("");
                } else {
                    String maxPP = "" + (Integer.parseInt(MoveDex.get(getContext()).getMoveJsonObject(moveJson.getString("id")).getString("pp"))
                            * 8 / 5);
                    movePps[i].setText(moveJson.optString("pp", "0") + "/" + maxPP);
                }

                String type = MoveDex.get(getContext()).getMoveJsonObject(moveJson.getString("id")).getString("type");

                int typeIcon = getMoveIcon(type);
                moveIcons[i].setImageResource(typeIcon);
                moveViews[i].setOnClickListener(parseMoveTarget(active, i));

                String ability = getCurrentActivePokemon().getAbilityName(getContext());

                // Account for all different move-type variations
                if (moveJson.getString("move").contains("Hidden Power")) {
                    moveViews[i].setBackgroundResource(getMoveBackground(moveJson.getString("move").substring(13)));
                    moveIcons[i].setImageResource(getMoveIcon(moveJson.getString("move").substring(13)));
                } else if (moveJson.getString("move").contains("Judgment") && getCurrentActivePokemon().getName()
                        .contains("Arceus") && getCurrentActivePokemon().getItemName(getContext()).contains("Plate")) {
                    String arceus = getCurrentActivePokemon().getName().substring(7, getCurrentActivePokemon().getName().length());
                    moveViews[i].setBackgroundResource(getMoveBackground(arceus));
                    moveIcons[i].setImageResource(getMoveIcon(arceus.toLowerCase()));
                } else if (type.equals("Normal") && ability.equals("Aerilate") || ability.equals("Pixilate") || ability.equals("Galvanize") ||
                        ability.equals("Refrigerate")) {
                    switch (ability) {
                        case "Aerilate":
                            moveViews[i].setBackgroundResource(getMoveBackground("flying"));
                            moveIcons[i].setImageResource(getMoveIcon("flying"));
                            break;
                        case "Pixilate":
                            moveViews[i].setBackgroundResource(getMoveBackground("fairy"));
                            moveIcons[i].setImageResource(getMoveIcon("fairy"));
                            break;
                        case "Galvanize":
                            moveViews[i].setBackgroundResource(getMoveBackground("electric"));
                            moveIcons[i].setImageResource(getMoveIcon("electric"));
                            break;
                        case "Refrigerate":
                            moveViews[i].setBackgroundResource(getMoveBackground("ice"));
                            moveIcons[i].setImageResource(getMoveIcon("ice"));
                            break;
                    }
                } else if (ability.equals("Normalize")) {
                    moveViews[i].setBackgroundResource(getMoveBackground("normal"));
                    moveIcons[i].setImageResource(getMoveIcon("normal"));
                } else {
                    moveViews[i].setBackgroundResource(getMoveBackground(type));
                }

                moveViews[i].setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        try {
                            MoveInfoDialog.newInstance(moveJson.getString("move"))
                                    .show(getActivity().getSupportFragmentManager(), BTAG);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return false;
                    }
                });

                if (moveJson.optBoolean("disabled", false)) {
                    moveViews[i].setOnClickListener(null);
                    moveViews[i].setBackgroundResource(R.drawable.uneditable_frame);
                }
            }

            boolean trapped = currentActive.optBoolean("trapped", false) ||
                    currentActive.optBoolean("maybeTrapped");
            if (trapped) {
                if (getCurrentActivePokemon().getItemName(getContext()) == null || !getCurrentActivePokemon().getItemName(getContext())
                        .equals("Shed Shell") || Arrays.asList(getCurrentActivePokemon().getTypeIcon()).contains(R.drawable.types_ghost)) {
                    triggerSwitchOptions(false);
                }
            }

        } catch (final JSONException e) {
            new RunWithNet() {
                @Override
                public void runWithNet() throws Exception {
                    throw e;
                }
            }.run();
        }
    }

    private int getMoveIcon(String type) {
        int res = 0;

        switch (type.toLowerCase()) {
            case "bug":
                res = R.drawable.types_bug;
                break;
            case "dark":
                res = R.drawable.types_dark;
                break;
            case "dragon":
                res = R.drawable.types_dragon;
                break;
            case "electric":
                res = R.drawable.types_electric;
                break;
            case "fairy":
                res = R.drawable.types_fairy;
                break;
            case "fighting":
                res = R.drawable.types_fighting;
                break;
            case "fire":
                res = R.drawable.types_fire;
                break;
            case "flying":
                res = R.drawable.types_flying;
                break;
            case "ghost":
                res = R.drawable.types_ghost;
                break;
            case "grass":
                res = R.drawable.types_grass;
                break;
            case "ground":
                res = R.drawable.types_ground;
                break;
            case "ice":
                res = R.drawable.types_ice;
                break;
            case "normal":
                res = R.drawable.types_normal;
                break;
            case "poison":
                res = R.drawable.types_poison;
                break;
            case "psychic":
                res = R.drawable.types_psychic;
                break;
            case "rock":
                res = R.drawable.types_rock;
                break;
            case "steel":
                res = R.drawable.types_steel;
                break;
            case "water":
                res = R.drawable.types_water;
                break;
        }

        return res;
    }

    private int getMoveBackground(String type) {
        int res = 0;

        switch (type.toLowerCase()) {
            case "bug":
                res = R.drawable.button_attack_bug;
                break;
            case "dark":
                res = R.drawable.button_attack_dark;
                break;
            case "dragon":
                res = R.drawable.button_attack_dragon;
                break;
            case "electric":
                res = R.drawable.button_attack_electric;
                break;
            case "fairy":
                res = R.drawable.button_attack_fairy;
                break;
            case "fighting":
                res = R.drawable.button_attack_fighting;
                break;
            case "fire":
                res = R.drawable.button_attack_fire;
                break;
            case "flying":
                res = R.drawable.button_attack_flying;
                break;
            case "ghost":
                res = R.drawable.button_attack_ghost;
                break;
            case "grass":
                res = R.drawable.button_attack_grass;
                break;
            case "ground":
                res = R.drawable.button_attack_ground;
                break;
            case "ice":
                res = R.drawable.button_attack_ice;
                break;
            case "normal":
                res = R.drawable.button_attack_normal;
                break;
            case "poison":
                res = R.drawable.button_attack_poison;
                break;
            case "psychic":
                res = R.drawable.button_attack_psychic;
                break;
            case "rock":
                res = R.drawable.button_attack_rock;
                break;
            case "steel":
                res = R.drawable.button_attack_steel;
                break;
            case "water":
                res = R.drawable.button_attack_water;
                break;
        }

        return res;
    }

    private View.OnClickListener parseMoveTarget(final JSONArray active, final int moveId) throws JSONException {
        if (getView() == null) {
            return null;
        }

        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new RunWithNet() {
                    @Override
                    public void runWithNet() throws Exception {

                        AlertDialog targetDialog = parseMoveTargetDialog(active, moveId);
                        if (targetDialog == null) {
                            parseMoveCommandAndSend(active, moveId, 0);
                        } else {
                            targetDialog.show();
                        }
                    }
                }.run();
            }
        };
    }

    private AlertDialog parseMoveTargetDialog(final JSONArray active, final int moveId) throws JSONException {
        final JSONObject moveJson = active.getJSONObject(mCurrentActivePokemon)
                .getJSONArray("moves")
                .getJSONObject(moveId);

        // null happens with struggle
        String target = moveJson.optString("target", null);
        if (target == null) {
            return null;
        }
        int maxAlly = 0;
        int maxFoe = 0;
        if (getView() != null) {
            if (getGametype().contains("doubles")) {
                maxAlly += (getView().findViewById(R.id.p1a).getVisibility() != View.GONE) ? 1 : 0;
                maxAlly += (getView().findViewById(R.id.p1b).getVisibility() != View.GONE) ? 1 : 0;
                maxFoe += (getView().findViewById(R.id.p2a).getVisibility() != View.GONE) ? 1 : 0;
                maxFoe += (getView().findViewById(R.id.p2b).getVisibility() != View.GONE) ? 1 : 0;
            } else if (getGametype().contains("triples")) {
                maxAlly += (getView().findViewById(R.id.p1a).getVisibility() != View.GONE) ? 1 : 0;
                maxAlly += (getView().findViewById(R.id.p1b).getVisibility() != View.GONE) ? 1 : 0;
                maxAlly += (getView().findViewById(R.id.p1c).getVisibility() != View.GONE) ? 1 : 0;
                maxFoe += (getView().findViewById(R.id.p2a).getVisibility() != View.GONE) ? 1 : 0;
                maxFoe += (getView().findViewById(R.id.p2b).getVisibility() != View.GONE) ? 1 : 0;
                maxFoe += (getView().findViewById(R.id.p2c).getVisibility() != View.GONE) ? 1 : 0;
            } else {
                maxAlly += (getView().findViewById(R.id.p1a).getVisibility() != View.GONE) ? 1 : 0;
                maxFoe += (getView().findViewById(R.id.p2a).getVisibility() != View.GONE) ? 1 : 0;
            }
        }

        int startFoe = Math.max(0, maxFoe - mCurrentActivePokemon - 2);
        int startAlly = Math.max(0, mCurrentActivePokemon - 1);
        int endFoe = Math.min(maxFoe - 1, maxFoe - mCurrentActivePokemon);
        int endAlly = Math.min(maxAlly - 1, mCurrentActivePokemon + 1);

        // counting foes
        final String[] foes = new String[maxFoe];
        int foeIndex = 0;
        for (int i = startFoe; i <= endFoe; i++) {
            PokemonInfo pkm = getPlayer2Team().get(i);
            if (checkSwitchedOut(false, i)) {
                foes[foeIndex] = pkm.getName();
                foeIndex++;
            }
        }

        // counting allies and self
        final String[] allyOrSelf = new String[maxAlly];
        int aosIndex = 0;
        for (int i = startAlly; i <= endAlly; i++) {
            PokemonInfo pkm = getPlayer1Team().get(i);
            if (checkSwitchedOut(true, i)) {
                allyOrSelf[aosIndex] = pkm.getName();
                aosIndex++;
            }
        }

        // counting allies but not self
        final String[] allies = new String[maxAlly - 1];
        int allyIndex = 0;
        for (int i = startAlly; i <= endAlly; i++) {
            PokemonInfo pkm = getPlayer1Team().get(i);
            if (i != mCurrentActivePokemon && checkSwitchedOut(true, i)) {
                allies[allyIndex] = pkm.getName();
                allyIndex++;
            }
        }

        String[] allTargets;
        final int numFoes = foeIndex;
        final int currentActive = mCurrentActivePokemon;
        final int foeOffset = startFoe;
        final int allyOffset = startAlly;
        switch (target) {
            case "any": //can hit anything on the BG, filling the list
                if ((foeIndex + allyIndex) < 2) {
                    return null;
                }
                ArrayList<String> anyTargets = new ArrayList<>();
                int anyFoes = 0;
                for (int i = 0; i < getPlayer2Team().size(); i++) {
                    PokemonInfo pkm = getPlayer2Team().get(i);
                    if (checkSwitchedOut(false, i)) {
                        anyTargets.add(pkm.getName());
                        anyFoes++;
                    }
                }
                for (int i = 0; i < getPlayer1Team().size(); i++) {
                    PokemonInfo pkm = getPlayer1Team().get(i);
                    if (i != mCurrentActivePokemon && checkSwitchedOut(true, i)) {
                        anyTargets.add(pkm.getName());
                    }
                }
                final int anyFoesNum = anyFoes;
                return new AlertDialog.Builder(getContext())
                        .setSingleChoiceItems(Arrays.copyOf(anyTargets.toArray(), anyTargets.toArray().length, String[].class), -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which < anyFoesNum) {
                                    parseMoveCommandAndSend(active, moveId, which + 1);
                                } else {
                                    parseMoveCommandAndSend(active, moveId, (which - anyFoesNum + 1) * -1);
                                }
                                dialog.dismiss();
                            }
                        }).create();
            case "normal": // can hit everyone close to mCurrentActivePokemon
                if ((foeIndex + allyIndex) < 2) {
                    return null;
                }

                allTargets = new String[foeIndex + allyIndex];
                System.arraycopy(foes, 0, allTargets, 0, foeIndex);
                System.arraycopy(allies, 0, allTargets, foeIndex, allyIndex);
                return new AlertDialog.Builder(getContext())
                        .setSingleChoiceItems(allTargets, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which < numFoes) {
                                    parseMoveCommandAndSend(active, moveId, which + 1 + foeOffset);
                                } else {
                                    parseMoveCommandAndSend(active, moveId, (which - numFoes + 1 + allyOffset) * -1);
                                }
                                dialog.dismiss();
                            }
                        }).create();
            case "adjacentFoe":
                if (foeIndex < 2) {
                    return null;
                }

                allTargets = new String[foeIndex];
                System.arraycopy(foes, 0, allTargets, 0, foeIndex);
                return new AlertDialog.Builder(getContext())
                        .setSingleChoiceItems(allTargets, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                parseMoveCommandAndSend(active, moveId, which + 1 + foeOffset);
                                dialog.dismiss();
                            }
                        }).create();
            case "adjacentAlly":
                if (allyIndex == 0) {
                    return null;
                }

                allTargets = new String[allyIndex];
                System.arraycopy(allies, 0, allTargets, 0, allyIndex);
                return new AlertDialog.Builder(getContext())
                        .setSingleChoiceItems(allTargets, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int pos = (which < currentActive) ? which : which + 1;
                                parseMoveCommandAndSend(active, moveId, (pos + 1 + allyOffset) * -1);
                                dialog.dismiss();
                            }
                        }).create();
            case "adjacentAllyOrSelf":
                if (aosIndex < 2) {
                    return null;
                }

                allTargets = new String[aosIndex];
                System.arraycopy(allyOrSelf, 0, allTargets, 0, aosIndex);
                return new AlertDialog.Builder(getContext())
                        .setSingleChoiceItems(allTargets, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                parseMoveCommandAndSend(active, moveId, (which + 1 + allyOffset) * -1);
                                dialog.dismiss();
                            }
                        }).create();
            default:
                return null;
        }
    }

    private boolean checkSwitchedOut(boolean player1, int pos) {
        if (getView() == null) {
            return false;
        }

        if (player1) {
            switch (pos) {
                case 0:
                    return getView().findViewById(R.id.p1a).getVisibility() == View.VISIBLE;
                case 1:
                    return getView().findViewById(R.id.p1b).getVisibility() == View.VISIBLE;
                case 2:
                    return getView().findViewById(R.id.p1c).getVisibility() == View.VISIBLE;
                default:
                    return false;
            }
        } else {
            switch (pos) {
                case 0:
                    return getView().findViewById(R.id.p2a).getVisibility() == View.VISIBLE;
                case 1:
                    return getView().findViewById(R.id.p2b).getVisibility() == View.VISIBLE;
                case 2:
                    return getView().findViewById(R.id.p2c).getVisibility() == View.VISIBLE;
                default:
                    return false;
            }
        }
    }

    private void parseMoveCommandAndSend(final JSONArray active, final int moveId, final int position) {
        if (getView() == null) {
            return;
        }

        new RunWithNet() {
            @Override
            public void runWithNet() throws Exception {
                JSONObject moveJson = active.getJSONObject(mCurrentActivePokemon)
                        .getJSONArray("moves")
                        .getJSONObject(moveId);

                String moveName = moveJson.getString("move");
                String command;

                if (moveName.startsWith("Hidden") || moveName.startsWith("Return") || moveName.startsWith("Frustration")
                        || moveName.startsWith("Gyro") || moveName.startsWith("Heavy")) {
                    moveName = moveName.replaceAll("[^A-z]", "");
                    //dirty fix to remove numbers from base-power variant attacks
                }

                /* Z-MOVE COMMANDS NORMAL / BACK / Z-MOVE
                *|/choose move 2|2
                *|/undo
                *|/choose move 2 zmove|2
                * */

                CheckBox megaBox = (CheckBox) getView().findViewById(R.id.mega_evolution_checkbox);
                CheckBox zMoveBox = (CheckBox) getView().findViewById(R.id.zmove_checkbox);
                if (megaBox.isChecked()) {
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        ((AutofitTextView) getView().findViewById(R.id.action_label)).setText(getCurrentActivePokemon().getName()
                                + " will mega evolve, then use " + moveName);
                    }
                    command = "move " + moveName + " mega";
                    mMegaEvo = true;
                } else if (zMoveBox.isChecked()) {
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        ((AutofitTextView) getView().findViewById(R.id.action_label)).setText(getCurrentActivePokemon().getName()
                                + " will use " + moveName);
                    }
                    command = "move " + moveName + " zmove";
                    mZMove = true;
                } else {
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        ((AutofitTextView) getView().findViewById(R.id.action_label)).setText(getCurrentActivePokemon().getName()
                                + " will use " + moveName);
                    }
                    command = "move " + moveName;
                }

                if (position != 0) {
                    command += " " + position;
                }

                addCommand(command);
                mCurrentActivePokemon++;

                if (mCurrentActivePokemon < mTotalActivePokemon) {
                    startAction(active);
                } else {
                    mChooseCommand.insert(0, "|/choose ");
                    sendCommands(mChooseCommand);
                }
            }
        }.run();
    }

    private void triggerSwitchOptions(boolean on) {
        if (getView() == null) {
            return;
        }

        for (int i = 0; i < getPlayer1Team().size(); i++) {
            PokemonInfo pkm = getPlayer1Team().get(i);
            ImageView icon = (ImageView) getView().findViewById(getIconId("p1", i));
            if (on) {
                if (!pkm.isActive() && pkm.getHp() > 0) {
                    icon.setBackgroundResource(R.drawable.editable_frame);
                    icon.setOnClickListener(new PokemonSwitchListener(true, i));
                }
            } else {
                icon.setBackgroundResource(0);
                icon.setOnClickListener(new PokemonInfoListener(true, i));
            }
        }
    }

    public void triggerTeamPreview(boolean on) {
        if (getView() == null) {
            return;
        }

        if (on) {
            getView().findViewById(R.id.p1a_prev)
                    .setOnClickListener(new PokemonSwitchListener(true, 0));
            getView().findViewById(R.id.p1b_prev)
                    .setOnClickListener(new PokemonSwitchListener(true, 1));
            getView().findViewById(R.id.p1c_prev)
                    .setOnClickListener(new PokemonSwitchListener(true, 2));
            getView().findViewById(R.id.p1d_prev)
                    .setOnClickListener(new PokemonSwitchListener(true, 3));
            getView().findViewById(R.id.p1e_prev)
                    .setOnClickListener(new PokemonSwitchListener(true, 4));
            getView().findViewById(R.id.p1f_prev)
                    .setOnClickListener(new PokemonSwitchListener(true, 5));
            getView().findViewById(R.id.p1a_prev)
                    .setAlpha(1f);
            getView().findViewById(R.id.p1b_prev)
                    .setAlpha(1f);
            getView().findViewById(R.id.p1c_prev)
                    .setAlpha(1f);
            getView().findViewById(R.id.p1d_prev)
                    .setAlpha(1f);
            getView().findViewById(R.id.p1e_prev)
                    .setAlpha(1f);
            getView().findViewById(R.id.p1f_prev)
                    .setAlpha(1f);
        } else {
            getView().findViewById(R.id.p1a_prev)
                    .setOnClickListener(new PokemonInfoListener(true, 0));
            getView().findViewById(R.id.p1b_prev)
                    .setOnClickListener(new PokemonInfoListener(true, 1));
            getView().findViewById(R.id.p1c_prev)
                    .setOnClickListener(new PokemonInfoListener(true, 2));
            getView().findViewById(R.id.p1d_prev)
                    .setOnClickListener(new PokemonInfoListener(true, 3));
            getView().findViewById(R.id.p1e_prev)
                    .setOnClickListener(new PokemonInfoListener(true, 4));
            getView().findViewById(R.id.p1f_prev)
                    .setOnClickListener(new PokemonInfoListener(true, 5));
        }
    }

    private void clearActionFrame() {
        if (getView() == null) {
            return;
        }
        FrameLayout frameLayout = (FrameLayout) getView().findViewById(R.id.action_interface);
        frameLayout.removeAllViews();
        triggerSwitchOptions(false);
    }

    public AnimatorSet createFlyingMessage(final String tag, AnimatorSet toast, final Spannable message) {
        try {
            message.setSpan(new RelativeSizeSpan(0.8f), 0, message.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            final TextView flyingMessage = new TextView(getContext());
            flyingMessage.setText(message);
            flyingMessage.setBackgroundResource(R.drawable.editable_frame);
            flyingMessage.setPadding(2, 2, 2, 2);
            flyingMessage.setAlpha(0f);
            toast.addListener(new AnimatorListenerWithNet() {
                @Override
                public void onAnimationStartWithNet(Animator animation) {
                    SimpleDraweeView imageView = (SimpleDraweeView) getView().findViewById(getSpriteId(tag));

                    RelativeLayout relativeLayout = (RelativeLayout) getView().findViewById(getPkmLayoutId(tag));
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.addRule(RelativeLayout.ALIGN_TOP, getSpriteId(tag));
                    layoutParams.addRule(RelativeLayout.ALIGN_LEFT, getSpriteId(tag));
                    layoutParams.setMargins((int) (imageView.getWidth() * 0.25f), (int) (imageView.getHeight() * 0.5f), 0, 0);
                    relativeLayout.addView(flyingMessage, layoutParams);
                }
            });

            ObjectAnimator flyingObject = ObjectAnimator.ofFloat(flyingMessage, "y", flyingMessage.getY());
            flyingObject.setDuration(ANIMATION_SHORT);
            flyingObject.setInterpolator(new AccelerateInterpolator());

            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(flyingMessage, "alpha", 0f, 1f);
            fadeIn.setInterpolator(new DecelerateInterpolator());
            fadeIn.setDuration(ANIMATION_SHORT / 4);

            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(flyingMessage, "alpha", 1f, 0f);
            fadeOut.setInterpolator(new AccelerateInterpolator());
            fadeOut.setStartDelay(ANIMATION_SHORT / 2);
            fadeOut.setDuration(ANIMATION_SHORT / 4);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(toast);
            animatorSet.play(fadeIn).with(toast);
            animatorSet.play(flyingObject).after(fadeIn);
            animatorSet.play(fadeOut).after(fadeIn);

            return animatorSet;
        } catch (NullPointerException e) {
            return new AnimatorSet();
        }
    }

    public int getPkmLayoutId(String tag) {
        tag = tag.substring(0, 3);
        switch (tag) {
            case "p1a":
                return R.id.p1a;
            case "p1b":
                return R.id.p1b;
            case "p1c":
                return R.id.p1c;
            case "p2a":
                return R.id.p2a;
            case "p2b":
                return R.id.p2b;
            case "p2c":
                return R.id.p2c;
            default:
                return 0;
        }
    }

    public void showEndBattleDialog(String battleStatusStatement) {
        if (!mBattleEnd) {
            mBattleEnd = true;
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
            final boolean hasMusic = sharedPref.getBoolean("pref_key_music", false);
            if (hasMusic) {
                AudioManager.stopBackgroundMusic(mRoomId);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getView().getContext());
            builder.setMessage(battleStatusStatement);

            builder.setPositiveButton(R.string.share_replay, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    MyApplication.getMyApplication().sendClientMessage(getRoomId() + "|/savereplay");
                }
            });
            builder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    BattleFieldData.get(getContext()).leaveRoom(mRoomId);
                    MainScreenFragment.TABS_HOLDER_ACCESSOR.removeTab(true);
                }
            });

            builder.show();
        }
    }

    public int getTeamSize() {
        return mTeamSize;
    }

    public void setTeamSize(int teamSize) {
        mTeamSize = teamSize;
    }

    private void endAllAnimations() {
        if (getView() == null || mAnimatorSetQueue == null) {
            return;
        }

        while (mAnimatorSetQueue.peekFirst() != null) {
            mAnimatorSetQueue.peekFirst().end();
            if (getCurrentBattleAnimation() != null) {
                getCurrentBattleAnimation().end();
                setCurrentBattleAnimation(null);
            }
        }
    }

    private void forfeitBattle() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        final boolean hasMusic = sharedPref.getBoolean("pref_key_music", false);
        if (hasMusic) {
            AudioManager.stopBackgroundMusic(mRoomId);
        }

        if (getBattling() != 0) {
            if (isBattleOver()) {
                MyApplication.getMyApplication().sendClientMessage(mRoomId + "|/leave");
            } else {
                MyApplication.getMyApplication().sendClientMessage(mRoomId + "|/forfeit");
            }
        }
        BattleFieldData.get(getActivity()).leaveRoom(mRoomId);
    }

    public class PokemonInfoListener implements View.OnClickListener {
        private boolean mPlayer1;
        private int mId;

        public PokemonInfoListener(boolean player1, int id) {
            mPlayer1 = player1;
            mId = id;
        }

        @Override
        public void onClick(View v) {
            if (v.getVisibility() != View.VISIBLE) {
                return;
            }

            PokemonInfo info = null;
            if (mId > -1 && mPlayer1) {
                if (mId < mPlayer1Team.size()) {
                    info = mPlayer1Team.get(mId);
                }
            } else {
                if (mId < mPlayer2Team.size()) {
                    info = mPlayer2Team.get(mId);
                }
            }

            if (info != null) {
                PokemonInfoFragment.newInstance(info, false, !mPlayer1)
                        .show(getActivity().getSupportFragmentManager(), BTAG);
            }
        }
    }

    public class PokemonSwitchListener implements View.OnClickListener {
        private boolean mPlayer1;
        private int mId;

        public PokemonSwitchListener(boolean player1, int id) {
            mPlayer1 = player1;
            mId = id;
        }

        @Override
        public void onClick(View v) {
            if (v.getVisibility() != View.VISIBLE) {
                return;
            }

            PokemonInfo info = null;
            if (mId > -1 && mPlayer1) {
                if (mId < mPlayer1Team.size()) {
                    info = mPlayer1Team.get(mId);
                }
            } else {
                if (mId < mPlayer2Team.size()) {
                    info = mPlayer2Team.get(mId);
                }
            }

            if (info != null) {
                PokemonInfoFragment.newInstance(info, true, !mPlayer1, BTAG, mId)
                        .show(getActivity().getSupportFragmentManager(), BTAG);
            }
        }
    }

    public class Receiver {

        public void processServerMessage(String roomId, String message) {
            if (mRoomId.equals(roomId)) {
                BattleFragment.this.processServerMessage(message);
            }
        }

        public void processSwitch(int id) throws JSONException {
            BattleFragment.this.processSwitch(id);
        }

        public void forfeitBattle(String roomId) {
            if (mRoomId.equals(roomId)) {
                BattleFragment.this.forfeitBattle();
            }
        }

        public boolean isBattleOver(String roomId) {
            if (mRoomId.equals(roomId)) {
                return mBattleEnd;
            } else {
                return false;
            }
        }

        public boolean isPlayerIn(String roomId) {
            if (mRoomId.equals(roomId)) {
                return getBattling() != 0;
            }
            return false;
        }
    }
}
