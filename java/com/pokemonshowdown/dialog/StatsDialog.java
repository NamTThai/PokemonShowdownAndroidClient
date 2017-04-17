package com.pokemonshowdown.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pokemonshowdown.R;
import com.pokemonshowdown.activity.PokemonActivity;
import com.pokemonshowdown.data.Pokemon;

import java.util.Arrays;
import java.util.List;

/**
 * Created by thain on 7/22/14.
 * <p/>
 * Array indices:
 * HP 0
 * Atk 1
 * Def 2
 * SpA 3 SpAtk
 * SpD 4 SpDef
 * Spe 5 Spd
 */
public class StatsDialog extends DialogFragment {
    public static final String STAG = "STATS_DIALOG";

    public final static String ARGUMENT_POKEMON = "Pokemon";
    public final static String ARGUMENT_STAGES = "Stages";
    public final static String ARGUMENT_SHOW_STAGES = "ShowStages";
    public final static String ARGUMENT_STATS = "Stats";
    public final static String ARGUMENT_BASE_STATS = "BaseStats";
    public final static String ARGUMENT_EV = "EVs";
    public final static String ARGUMENT_IV = "IVs";
    public final static String ARGUMENT_LEVEL = "Level";
    public final static String ARGUMENT_NATURE_MULTIPLIER = "NatureMultiplier";
    public static final int maxEV = 508;
    private final static List<String> SPINNER_STAGES = Arrays.asList("-6", "-5", "-4", "-3", "-2", "-1", "0", "+1", "+2", "+3", "+4", "+5", "+6");
    private String mPokemonName;
    private int[] mStats;
    private int[] mBaseStats;
    private int[] mEVs;
    private int[] mIVs;
    private int mLevel;
    private float[] mNatureMultiplier;
    private int[] mStages;
    private boolean mShowStages = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPokemonName = getArguments().getString(ARGUMENT_POKEMON);
        mStats = getArguments().getIntArray(ARGUMENT_STATS);
        mBaseStats = getArguments().getIntArray(ARGUMENT_BASE_STATS);
        mEVs = getArguments().getIntArray(ARGUMENT_EV);
        mIVs = getArguments().getIntArray(ARGUMENT_IV);
        mLevel = getArguments().getInt(ARGUMENT_LEVEL);
        mNatureMultiplier = getArguments().getFloatArray(ARGUMENT_NATURE_MULTIPLIER);
        mStages = getArguments().getIntArray(ARGUMENT_STAGES);
        mShowStages = getArguments().getBoolean(ARGUMENT_SHOW_STAGES);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_stats, container);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setStats(mStats);
        setEVs(mEVs);

        SeekBar seekBar;
        EditText editText;

        seekBar = (SeekBar) getView().findViewById(R.id.bar_HP);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int storedProgress = mEVs[0];
                int changes = progress * 4 - storedProgress;
                int remainingEVs = getRemainingEVs();
                if (changes > 0 && changes > remainingEVs) {
                    mEVs[0] = storedProgress + remainingEVs;
                } else {
                    mEVs[0] = progress * 4;
                }
                seekBar.setProgress(mEVs[0] / 4);
                TextView textView = (TextView) getView().findViewById(R.id.EV_HP);
                textView.setText(Integer.toString(mEVs[0]));
                mStats[0] = Pokemon.calculateHP(mBaseStats[0], mIVs[0], mEVs[0], mLevel);
                setStats(mStats[0], -1, -1, -1, -1, -1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBar = (SeekBar) getView().findViewById(R.id.bar_Atk);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int storedProgress = mEVs[1];
                int changes = progress * 4 - storedProgress;
                int remainingEVs = getRemainingEVs();
                if (changes > 0 && changes > remainingEVs) {
                    mEVs[1] = storedProgress + remainingEVs;
                } else {
                    mEVs[1] = progress * 4;
                }
                seekBar.setProgress(mEVs[1] / 4);
                TextView textView = (TextView) getView().findViewById(R.id.EV_Atk);
                textView.setText(Integer.toString(mEVs[1]));
                mStats[1] = Pokemon.calculateAtk(mBaseStats[1], mIVs[1], mEVs[1], mStages[1], mLevel, mNatureMultiplier[1]);
                setStats(-1, mStats[1], -1, -1, -1, -1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBar = (SeekBar) getView().findViewById(R.id.bar_Def);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int storedProgress = mEVs[2];
                int changes = progress * 4 - storedProgress;
                int remainingEVs = getRemainingEVs();
                if (changes > 0 && changes > remainingEVs) {
                    mEVs[2] = storedProgress + remainingEVs;
                } else {
                    mEVs[2] = progress * 4;
                }
                seekBar.setProgress(mEVs[2] / 4);
                TextView textView = (TextView) getView().findViewById(R.id.EV_Def);
                textView.setText(Integer.toString(mEVs[2]));
                mStats[2] = Pokemon.calculateDef(mBaseStats[2], mIVs[2], mEVs[2], mStages[1], mLevel, mNatureMultiplier[2]);
                setStats(-1, -1, mStats[2], -1, -1, -1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBar = (SeekBar) getView().findViewById(R.id.bar_SpAtk);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int storedProgress = mEVs[3];
                int changes = progress * 4 - storedProgress;
                int remainingEVs = getRemainingEVs();
                if (changes > 0 && changes > remainingEVs) {
                    mEVs[3] = storedProgress + remainingEVs;
                } else {
                    mEVs[3] = progress * 4;
                }
                seekBar.setProgress(mEVs[3] / 4);
                TextView textView = (TextView) getView().findViewById(R.id.EV_SpAtk);
                textView.setText(Integer.toString(mEVs[3]));
                mStats[3] = Pokemon.calculateSpAtk(mBaseStats[3], mIVs[3], mEVs[3], mStages[1], mLevel, mNatureMultiplier[3]);
                setStats(-1, -1, -1, mStats[3], -1, -1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBar = (SeekBar) getView().findViewById(R.id.bar_SpDef);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int storedProgress = mEVs[4];
                int changes = progress * 4 - storedProgress;
                int remainingEVs = getRemainingEVs();
                if (changes > 0 && changes > remainingEVs) {
                    mEVs[4] = storedProgress + remainingEVs;
                } else {
                    mEVs[4] = progress * 4;
                }
                seekBar.setProgress(mEVs[4] / 4);
                TextView textView = (TextView) getView().findViewById(R.id.EV_SpDef);
                textView.setText(Integer.toString(mEVs[4]));
                mStats[4] = Pokemon.calculateSpDef(mBaseStats[4], mIVs[4], mEVs[4], mStages[1], mLevel, mNatureMultiplier[4]);
                setStats(-1, -1, -1, -1, mStats[4], -1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBar = (SeekBar) getView().findViewById(R.id.bar_Spd);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int storedProgress = mEVs[5];
                int changes = progress * 4 - storedProgress;
                int remainingEVs = getRemainingEVs();
                if (changes > 0 && changes > remainingEVs) {
                    mEVs[5] = storedProgress + remainingEVs;
                } else {
                    mEVs[5] = progress * 4;
                }
                seekBar.setProgress(mEVs[5] / 4);
                TextView textView = (TextView) getView().findViewById(R.id.EV_Spd);
                textView.setText(Integer.toString(mEVs[5]));
                mStats[5] = Pokemon.calculateSpd(mBaseStats[5], mIVs[5], mEVs[5], mStages[1], mLevel, mNatureMultiplier[5]);
                setStats(-1, -1, -1, -1, -1, mStats[5]);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        getView().findViewById(R.id.save_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PokemonActivity.POKEMON_STATS.setPokemonStats(mStats);
                PokemonActivity.POKEMON_STATS.setPokemonEVs(mEVs);
                PokemonActivity.POKEMON_STATS.setupBars();
                Toast.makeText(getContext(), "EV's saved", Toast.LENGTH_SHORT).show();
                getDialog().dismiss();
            }
        });
        highlightNature();

    }

    private void setStats(int[] stats) {
        setStats(stats[0], stats[1], stats[2], stats[3], stats[4], stats[5]);
    }

    private void setEVs(int[] EVs) {
        setEVs(EVs[0], EVs[1], EVs[2], EVs[3], EVs[4], EVs[5]);
    }

    private void highlightNature() {
        TextView textViewPositive;
        TextView textViewNegative;

        if (mNatureMultiplier[1] == 1.1f) {
            textViewPositive = (TextView) getView().findViewById(R.id.final_Atk);
            textViewPositive.setBackgroundResource(R.drawable.nature_positive);
        } else if (mNatureMultiplier[1] == 0.9f) {
            textViewNegative = (TextView) getView().findViewById(R.id.final_Atk);
            textViewNegative.setBackgroundResource(R.drawable.nature_negative);
        }

        if (mNatureMultiplier[2] == 1.1f) {
            textViewPositive = (TextView) getView().findViewById(R.id.final_Def);
            textViewPositive.setBackgroundResource(R.drawable.nature_positive);
        } else if (mNatureMultiplier[2] == 0.9f) {
            textViewNegative = (TextView) getView().findViewById(R.id.final_Def);
            textViewNegative.setBackgroundResource(R.drawable.nature_negative);
        }

        if (mNatureMultiplier[3] == 1.1f) {
            textViewPositive = (TextView) getView().findViewById(R.id.final_SpAtk);
            textViewPositive.setBackgroundResource(R.drawable.nature_positive);
        } else if (mNatureMultiplier[3] == 0.9f) {
            textViewNegative = (TextView) getView().findViewById(R.id.final_SpAtk);
            textViewNegative.setBackgroundResource(R.drawable.nature_negative);
        }

        if (mNatureMultiplier[4] == 1.1f) {
            textViewPositive = (TextView) getView().findViewById(R.id.final_SpDef);
            textViewPositive.setBackgroundResource(R.drawable.nature_positive);
        } else if (mNatureMultiplier[4] == 0.9f) {
            textViewNegative = (TextView) getView().findViewById(R.id.final_SpDef);
            textViewNegative.setBackgroundResource(R.drawable.nature_negative);
        }

        if (mNatureMultiplier[5] == 1.1f) {
            textViewPositive = (TextView) getView().findViewById(R.id.final_Spd);
            textViewPositive.setBackgroundResource(R.drawable.nature_positive);
        } else if (mNatureMultiplier[5] == 0.9f) {
            textViewNegative = (TextView) getView().findViewById(R.id.final_Spd);
            textViewNegative.setBackgroundResource(R.drawable.nature_negative);
        }
    }

    private int getRemainingEVs() {
        return maxEV - mEVs[0] - mEVs[1] - mEVs[2] - mEVs[3] - mEVs[4] - mEVs[5];
    }

    private void setStats(int HP, int Atk, int Def, int SpAtk, int SpDef, int Spd) {
        TextView textView;
        if (HP != -1) {
            if (mPokemonName.equals("Shedinja")) {
                mStats[0] = 1;
                textView = (TextView) getView().findViewById(R.id.final_HP);
                textView.setText("1", TextView.BufferType.EDITABLE);
            } else {
                mStats[0] = HP;
                textView = (TextView) getView().findViewById(R.id.final_HP);
                textView.setText(Integer.toString(HP), TextView.BufferType.EDITABLE);
            }
        }
        if (Atk != -1) {
            mStats[1] = Atk;
            textView = (TextView) getView().findViewById(R.id.final_Atk);
            textView.setText(Integer.toString(Atk), TextView.BufferType.EDITABLE);
        }
        if (Def != -1) {
            mStats[2] = Def;
            textView = (TextView) getView().findViewById(R.id.final_Def);
            textView.setText(Integer.toString(Def), TextView.BufferType.EDITABLE);
        }
        if (SpAtk != -1) {
            mStats[3] = SpAtk;
            textView = (TextView) getView().findViewById(R.id.final_SpAtk);
            textView.setText(Integer.toString(SpAtk), TextView.BufferType.EDITABLE);
        }
        if (SpDef != -1) {
            mStats[4] = SpDef;
            textView = (TextView) getView().findViewById(R.id.final_SpDef);
            textView.setText(Integer.toString(SpDef), TextView.BufferType.EDITABLE);
        }
        if (Spd != -1) {
            mStats[5] = Spd;
            textView = (TextView) getView().findViewById(R.id.final_Spd);
            textView.setText(Integer.toString(Spd), TextView.BufferType.EDITABLE);
        }
    }

    private void setEVs(int HP, int Atk, int Def, int SpAtk, int SpDef, int Spd) {
        if (HP != -1) {
            SeekBar seekBar = (SeekBar) getView().findViewById(R.id.bar_HP);
            seekBar.setProgress(HP / 4);
            TextView textView = (TextView) getView().findViewById(R.id.EV_HP);
            textView.setText(Integer.toString(HP));
        }
        if (Atk != -1) {
            SeekBar seekBar = (SeekBar) getView().findViewById(R.id.bar_Atk);
            seekBar.setProgress(Atk / 4);
            TextView textView = (TextView) getView().findViewById(R.id.EV_Atk);
            textView.setText(Integer.toString(Atk));
        }
        if (Def != -1) {
            SeekBar seekBar = (SeekBar) getView().findViewById(R.id.bar_Def);
            seekBar.setProgress(Def / 4);
            TextView textView = (TextView) getView().findViewById(R.id.EV_Def);
            textView.setText(Integer.toString(Def));
        }
        if (SpAtk != -1) {
            SeekBar seekBar = (SeekBar) getView().findViewById(R.id.bar_SpAtk);
            seekBar.setProgress(SpAtk / 4);
            TextView textView = (TextView) getView().findViewById(R.id.EV_SpAtk);
            textView.setText(Integer.toString(SpAtk));
        }
        if (SpDef != -1) {
            SeekBar seekBar = (SeekBar) getView().findViewById(R.id.bar_SpDef);
            seekBar.setProgress(SpDef / 4);
            TextView textView = (TextView) getView().findViewById(R.id.EV_SpDef);
            textView.setText(Integer.toString(SpDef));
        }
        if (Spd != -1) {
            SeekBar seekBar = (SeekBar) getView().findViewById(R.id.bar_Spd);
            seekBar.setProgress(Spd / 4);
            TextView textView = (TextView) getView().findViewById(R.id.EV_Spd);
            textView.setText(Integer.toString(Spd));
        }
    }

    public void setStages(int[] stages) {
        setStages(stages[1], stages[2], stages[3], stages[4], stages[5]);
    }

    public void setStages(int atk, int def, int spAtk, int spDef, int spd) {
        if (atk != -1) {
            mStages[1] = atk;
        }

        if (def != -1) {
            mStages[2] = def;
        }

        if (spAtk != -1) {
            mStages[3] = spAtk;
        }

        if (spDef != -1) {
            mStages[4] = spDef;
        }

        if (spd != -1) {
            mStages[5] = spd;
        }
    }

    private int getRemainingEVsForFilter(int avoid) {
        switch (avoid) {
            case 0:
                return maxEV - mEVs[1] - mEVs[2] - mEVs[3] - mEVs[4] - mEVs[5];
            case 1:
                return maxEV - mEVs[0] - mEVs[2] - mEVs[3] - mEVs[4] - mEVs[5];
            case 2:
                return maxEV - mEVs[0] - mEVs[1] - mEVs[3] - mEVs[4] - mEVs[5];
            case 3:
                return maxEV - mEVs[0] - mEVs[1] - mEVs[2] - mEVs[4] - mEVs[5];
            case 4:
                return maxEV - mEVs[0] - mEVs[1] - mEVs[2] - mEVs[3] - mEVs[5];
            case 5:
                return maxEV - mEVs[0] - mEVs[1] - mEVs[2] - mEVs[3] - mEVs[4];
            default:
                return getRemainingEVs();
        }
    }
}