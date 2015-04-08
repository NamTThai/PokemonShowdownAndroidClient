package com.pokemonshowdown.app;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pokemonshowdown.application.MyApplication;
import com.pokemonshowdown.data.AnimatorListenerWithNet;
import com.pokemonshowdown.data.BattleFieldData;
import com.pokemonshowdown.data.BattleMessage;
import com.pokemonshowdown.data.MoveDex;
import com.pokemonshowdown.data.Onboarding;
import com.pokemonshowdown.data.PokemonInfo;
import com.pokemonshowdown.data.RunWithNet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

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
    public final static String SERVER_MESSAGE_ARCHIVE = "Server Message Archive";
    private ArrayList<String> mServerMessageArchive;
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
    private boolean mTimer;
    private String mPlayer1;
    private String mPlayer2;
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

    public BattleFragment() {

    }

    public static BattleFragment newInstance(String roomId) {
        BattleFragment fragment = new BattleFragment();
        Bundle args = new Bundle();
        args.putString(ROOM_ID, roomId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_battle, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toast.makeText(getActivity(), R.string.loading, Toast.LENGTH_SHORT).show();

        if (getArguments() != null) {
            mRoomId = getArguments().getString(ROOM_ID);
        }

        mBattling = 0;
        mBattleEnd = false;

        int id = new Random().nextInt(BACKGROUND_LIBRARY.length);
        ((ImageView) view.findViewById(R.id.battle_background)).setImageResource(BACKGROUND_LIBRARY[id]);

        view.findViewById(R.id.battlelog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogFragment = BattleLogDialog.newInstance(mRoomId);
                dialogFragment.show(getActivity().getSupportFragmentManager(), mRoomId);
            }
        });

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

        if (savedInstanceState != null) {
            mServerMessageArchive = savedInstanceState.getStringArrayList(SERVER_MESSAGE_ARCHIVE);
            if (mServerMessageArchive != null) {
                for (String serverMessage : mServerMessageArchive) {
                    processServerMessage(serverMessage);
                }
                endAllAnimations();
            }
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
        outState.putStringArrayList(SERVER_MESSAGE_ARCHIVE, mServerMessageArchive);
    }

    private void setUpTimer() {
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
                } else {
                    timer.setBackgroundResource(R.drawable.uneditable_frame_red);
                    MyApplication.getMyApplication().sendClientMessage(mRoomId + "|/timer off");
                }
            }
        });
    }

    public void processServerMessage(final String message) {
        new RunWithNet() {
            @Override
            public void runWithNet() throws Exception {
                String processedMessage = message;
                if (mBattling == -1) {
                    processedMessage = message.replace("p1", "p3").replace("p2", "p1").replace("p3", "p2");
                }
                BattleMessage.processMajorAction(BattleFragment.this, processedMessage);
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
            mPlayer1 = BattleFieldData.get(getActivity()).getAnimationInstance(getRoomId()).getPlayer1();
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
            mPlayer2 = BattleFieldData.get(getActivity()).getAnimationInstance(getRoomId()).getPlayer2();
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

    public boolean isBattleOver() {
        return mBattleEnd;
    }


    private void switchUpPlayer() {
        // Switch player name
        if (getView() == null) {
            return;
        }

        String holderString = mPlayer1;
        mPlayer1 = mPlayer2;
        mPlayer2 = holderString;
        ((TextView) getView().findViewById(R.id.username)).setText(mPlayer1);
        ((TextView) getView().findViewById(R.id.username_o)).setText(mPlayer2);

        ArrayList<PokemonInfo> holderTeam = mPlayer1Team;
        mPlayer1Team = mPlayer2Team;
        mPlayer2Team = holderTeam;

        // Switch player avatar
        Drawable holderDrawable = ((ImageView) getView().findViewById(R.id.avatar)).getDrawable();
        ((ImageView) getView().findViewById(R.id.avatar)).setImageDrawable(((ImageView) getView().findViewById(R.id.avatar_o)).getDrawable());
        ((ImageView) getView().findViewById(R.id.avatar_o)).setImageDrawable(holderDrawable);
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

    public AnimatorSet makeToast(final String message) {
        return makeToast(message, ANIMATION_LONG);
    }

    public AnimatorSet makeToast(final String message, final int duration) {
        return makeToast(new SpannableString(message), duration);
    }

    public AnimatorSet makeToast(final Spannable message, final int duration) {
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
        getActivity().runOnUiThread(new RunWithNet() {
            @Override
            public void runWithNet() {
                animator.addListener(new AnimatorListenerWithNet() {
                    @Override
                    public void onAnimationEndWithNet(Animator animation) {
                        if (getView() != null) {
                            getView().findViewById(R.id.skip).setVisibility(View.GONE);
                        }

                        mAnimatorSetQueue.pollFirst();
                        Animator nextOnQueue = mAnimatorSetQueue.peekFirst();
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
                            getView().findViewById(R.id.skip).setVisibility(View.GONE);
                        }

                        try {
                            triggerTeamPreview(false);
                        } catch (NullPointerException e) {
                            clearActionFrame();
                        }

                        if (!Onboarding.get(getActivity()).isAnimation()) {
                            animation.end();
                        }
                    }


                });

                if (mAnimatorSetQueue == null) {
                    mAnimatorSetQueue = new ArrayDeque<>();
                }

                mAnimatorSetQueue.addLast(animator);

                if (mAnimatorSetQueue.size() == 1) {
                    animator.start();
                }
            }
        });
    }

    public void addToLog(Spannable logMessage) {
        BattleFieldData.BattleLog battleLog = BattleFieldData.get(getActivity()).getRoomInstance(mRoomId);
        if (battleLog != null && battleLog.isMessageListener()) {
            if (logMessage.length() > 0) {
                battleLog.addServerMessageOnHold(logMessage);
            }
        } else {
            BattleLogDialog battleLogDialog =
                    (BattleLogDialog) getActivity().getSupportFragmentManager().findFragmentByTag(mRoomId);
            if (battleLogDialog != null) {
                if (logMessage.length() > 0) {
                    battleLogDialog.processServerMessage(logMessage);
                }
            }
        }
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
            TextView stt = new TextView(getActivity());
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
            RelativeLayout relativeLayout;
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

            relativeLayout = (RelativeLayout) getView().findViewById(layoutId);
            relativeLayout.setVisibility(View.INVISIBLE);
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
            RelativeLayout relativeLayout;
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
            relativeLayout = (RelativeLayout) getView().findViewById(layoutId);
            relativeLayout.setVisibility(View.VISIBLE);
            getView().findViewById(getSpriteId(tag)).setAlpha(1f);
            if (!isBatonPass()) {
                ((LinearLayout) getView().findViewById(getTempStatusId(tag))).removeAllViews();
            }
            ImageView sub = (ImageView) relativeLayout.findViewWithTag("Substitute");
            if (sub != null) {
                if (!isBatonPass()) {
                    relativeLayout.removeView(sub);
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
        newInfo = new PokemonInfo(getActivity(), newPkm);
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
                statBoost = new TextView(getActivity());
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
                    TextView destStat = new TextView(getActivity());
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
        Log.d(BTAG, command.toString());
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
            chooseLeadInTeamPreview(id);
        } else {
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
            // starting with user selection
            for (int i = 0; i < chosen.length(); i++) {
                lineUp.add(Integer.parseInt(Character.toString(chosen.charAt(i))));
            }
            // filling with rest of ids
            for (int i = 1; i <= mPlayer1Team.size(); i++) {
                if (lineUp.indexOf(i) == -1) {
                    lineUp.add(i);
                }
            }

            mChooseCommand = new StringBuilder();
            mChooseCommand.append("|/team ");

            // line up size should be 6
            for (int i = 0; i < lineUp.size(); i++) {
                mChooseCommand.append(lineUp.get(i));
            }

            setTeamPreview(false);
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
            return;
        }

        new RunWithNet() {
            @Override
            public void runWithNet() throws Exception {
                JSONObject requestJson = getRequestJson();

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
        }

        FrameLayout frameLayout = (FrameLayout) getView().findViewById(R.id.action_interface);
        frameLayout.removeAllViews();

        getActivity().getLayoutInflater().inflate(R.layout.fragment_battle_action_moves, frameLayout);
        RelativeLayout[] moveViews = new RelativeLayout[4];
        moveViews[0] = (RelativeLayout) getView().findViewById(R.id.active_move1);
        moveViews[1] = (RelativeLayout) getView().findViewById(R.id.active_move2);
        moveViews[2] = (RelativeLayout) getView().findViewById(R.id.active_move3);
        moveViews[3] = (RelativeLayout) getView().findViewById(R.id.active_move4);
        TextView[] moveNames = new TextView[4];
        moveNames[0] = (TextView) getView().findViewById(R.id.active_move1_name);
        moveNames[1] = (TextView) getView().findViewById(R.id.active_move2_name);
        moveNames[2] = (TextView) getView().findViewById(R.id.active_move3_name);
        moveNames[3] = (TextView) getView().findViewById(R.id.active_move4_name);
        TextView[] movePps = new TextView[4];
        movePps[0] = (TextView) getView().findViewById(R.id.active_move1_pp);
        movePps[1] = (TextView) getView().findViewById(R.id.active_move2_pp);
        movePps[2] = (TextView) getView().findViewById(R.id.active_move3_pp);
        movePps[3] = (TextView) getView().findViewById(R.id.active_move4_pp);
        ImageView[] moveIcons = new ImageView[4];
        moveIcons[0] = (ImageView) getView().findViewById(R.id.active_move1_icon);
        moveIcons[1] = (ImageView) getView().findViewById(R.id.active_move2_icon);
        moveIcons[2] = (ImageView) getView().findViewById(R.id.active_move3_icon);
        moveIcons[3] = (ImageView) getView().findViewById(R.id.active_move4_icon);

        PokemonInfo currentPokemonInfo = getCurrentActivePokemon();
        CheckBox checkBox = (CheckBox) getView().findViewById(R.id.mega_evolution_checkbox);

        if (currentPokemonInfo.canMegaEvo()) {
            checkBox.setVisibility(View.VISIBLE);
        } else {
            checkBox.setVisibility(View.GONE);
        }

        try {
            JSONObject currentActive = active.getJSONObject(mCurrentActivePokemon);
            JSONArray moves = currentActive.getJSONArray("moves");
            boolean trapped = currentActive.optBoolean("trapped", false) ||
                    currentActive.optBoolean("maybeTrapped");
            if (!trapped || moves.length() != 1) {
                for (int i = 0; i < moves.length(); i++) {
                    JSONObject moveJson = moves.getJSONObject(i);
                    moveNames[i].setText(moveJson.getString("move"));
                    movePps[i].setText(moveJson.optString("pp", "0"));
                    int typeIcon = MoveDex.getMoveTypeIcon(getActivity(), moveJson.getString("id"));
                    moveIcons[i].setImageResource(typeIcon);
                    moveViews[i].setOnClickListener(parseMoveTarget(active, i));
                    if (moveJson.optBoolean("disabled", false)) {
                        moveViews[i].setOnClickListener(null);
                        moveViews[i].setBackgroundResource(R.drawable.uneditable_frame);
                    }
                }

                if (trapped) {
                    triggerSwitchOptions(false);
                }
            } else {
                parseMoveCommandAndSend(active, 0, 0);
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
        String target = moveJson.getString("target");

        int start = (mCurrentActivePokemon == 0) ? 0 : mCurrentActivePokemon - 1;
        int endFoe = (mCurrentActivePokemon + 1 >= getPlayer2Team().size()) ?
                getPlayer2Team().size() - 1 : mCurrentActivePokemon + 1;
        int endAlly = (mCurrentActivePokemon + 1 >= getPlayer1Team().size()) ?
                getPlayer1Team().size() - 1 : mCurrentActivePokemon + 1;

        // counting foes
        final String[] foes = new String[3];
        final int[] foeIcons = new int[3];
        int foeIndex = 0;
        for (int i = start; i <= endFoe; i++) {
            PokemonInfo pkm = getPlayer2Team().get(i);
            if (checkSwitchedOut(false, i)) {
                foes[foeIndex] = pkm.getName();
                foeIcons[foeIndex] = pkm.getIcon(getActivity());
                foeIndex++;
            }
        }

        // counting allies and self
        final String[] allyOrSelf = new String[3];
        final int[] aosIcons = new int[3];
        int aosIndex = 0;
        for (int i = start; i <= endAlly; i++) {
            PokemonInfo pkm = getPlayer1Team().get(i);
            if (checkSwitchedOut(true, i)) {
                allyOrSelf[aosIndex] = pkm.getName();
                aosIcons[aosIndex] = pkm.getIcon(getActivity());
                aosIndex++;
            }
        }

        // counting allies but not self
        final String[] allies = new String[2];
        final int[] allyIcons = new int[2];
        int allyIndex = 0;
        for (int i = start; i <= endAlly; i++) {
            PokemonInfo pkm = getPlayer1Team().get(i);
            if (i != mCurrentActivePokemon && checkSwitchedOut(true, i)) {
                allies[allyIndex] = pkm.getName();
                allyIcons[allyIndex] = pkm.getIcon(getActivity());
                allyIndex++;
            }
        }

        String[] allTargets;
        final int numFoes = foeIndex;
        final int currentActive = mCurrentActivePokemon;
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
                return new AlertDialog.Builder(getActivity())
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
                return new AlertDialog.Builder(getActivity())
                        .setSingleChoiceItems(allTargets, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which < numFoes) {
                                    parseMoveCommandAndSend(active, moveId, which + 1);
                                } else {
                                    parseMoveCommandAndSend(active, moveId, (which - numFoes + 1) * -1);
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
                return new AlertDialog.Builder(getActivity())
                        .setSingleChoiceItems(allTargets, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                parseMoveCommandAndSend(active, moveId, which + 1);
                                dialog.dismiss();
                            }
                        }).create();
            case "adjacentAlly":
                if (allyIndex < 2) {
                    return null;
                }

                allTargets = new String[allyIndex];
                System.arraycopy(allies, 0, allTargets, 0, allyIndex);
                return new AlertDialog.Builder(getActivity())
                        .setSingleChoiceItems(allTargets, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int pos = (which < currentActive) ? which : which + 1;
                                parseMoveCommandAndSend(active, moveId, (pos + 1) * -1);
                                dialog.dismiss();
                            }
                        }).create();
            case "adjacentAllyOrSelf":
                if (aosIndex < 2) {
                    return null;
                }

                allTargets = new String[aosIndex];
                System.arraycopy(allyOrSelf, 0, allTargets, 0, aosIndex);
                return new AlertDialog.Builder(getActivity())
                        .setSingleChoiceItems(allTargets, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                parseMoveCommandAndSend(active, moveId, (which + 1) * -1);
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

                CheckBox checkBox = (CheckBox) getView().findViewById(R.id.mega_evolution_checkbox);
                if (checkBox.isChecked()) {
                    command = "move " + moveName + " mega";
                } else {
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
            final TextView flyingMessage = new TextView(getActivity());
            flyingMessage.setText(message);
            flyingMessage.setBackgroundResource(R.drawable.editable_frame);
            flyingMessage.setPadding(2, 2, 2, 2);
            flyingMessage.setAlpha(0f);
            toast.addListener(new AnimatorListenerWithNet() {
                @Override
                public void onAnimationStartWithNet(Animator animation) {
                    ImageView imageView = (ImageView) getView().findViewById(getSpriteId(tag));

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
        mBattleEnd = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(battleStatusStatement);

        builder.setPositiveButton(R.string.share_replay, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                MyApplication.getMyApplication().sendClientMessage(getRoomId() + "|/savereplay");
            }
        });
        builder.setNegativeButton(R.string.back_to_battle, null);

        builder.show();
    }

    public void setTeamSize(int teamSize) {
        mTeamSize = teamSize;
        int[] switchIcons = {R.id.icon1, R.id.icon2, R.id.icon3, R.id.icon4, R.id.icon5, R.id.icon6};
        if (teamSize != 0) {
            for (int i = teamSize; i < getPlayer1Team().size(); i++) {
                getView().findViewById(switchIcons[i]).setVisibility(View.GONE);
                getView().findViewById(switchIcons[i]).setOnClickListener(null);
            }
        }
    }

    public int getTeamSize() {
        return mTeamSize;
    }

    private void endAllAnimations() {
        if (getView() == null) {
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
                PokemonInfoFragment.newInstance(info, false)
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
                PokemonInfoFragment.newInstance(info, true, BattleFragment.this.getTag(), mId)
                        .show(getActivity().getSupportFragmentManager(), BTAG);
            }
        }
    }

}
