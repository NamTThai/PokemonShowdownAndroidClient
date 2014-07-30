package com.pokemonshowdown.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pokemonshowdown.data.InputFilterMinMax;

/**
 * Created by thain on 7/22/14.
*/
public class StatsDialog extends DialogFragment {
    public static final String STAG = "STATS_DIALOG";
    public static final int maxEV = 508;

    private int[] mStats;
    private int[] mBaseStats;
    private int[] mEVs;
    private int[] mIVs;
    private int mLevel;
    private float[] mNatureMultiplier;

    public StatsDialog() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStats = getArguments().getIntArray("Stats");
        mBaseStats = getArguments().getIntArray("BaseStats");
        mEVs = getArguments().getIntArray("EVs");
        mIVs = getArguments().getIntArray("IVs");
        mLevel = getArguments().getInt("Level");
        mNatureMultiplier = getArguments().getFloatArray("NatureMultiplier");
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
        setIVs(mIVs);

        getView().findViewById(R.id.save_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {FragmentManager fm = getActivity().getSupportFragmentManager();
                PokemonFragment pokemonFragment = (PokemonFragment) fm.findFragmentByTag(PokemonFragment.PokemonTAG);
                pokemonFragment.getPokemon().setStats(mStats);
                pokemonFragment.getPokemon().setEVs(mEVs);
                pokemonFragment.getPokemon().setIVs(mIVs);
                pokemonFragment.resetStatsString();
                getDialog().dismiss();
            }
        });

        // Below are SeekBars
        SeekBar seekBar;
        seekBar = (SeekBar) getView().findViewById(R.id.bar_HP);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setEVs(progress*4, -1, -1, -1, -1, -1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // Below are EVs
        EditText editText;

        // Below are IVs
        editText = (EditText) getView().findViewById(R.id.IV_HP);
        editText.setFilters(new InputFilter[]{new InputFilterMinMax(0, 31)});
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String newValue = s.toString();
                int intValue;
                if (newValue.matches("")) {
                    intValue = 0;
                } else {
                    intValue = Integer.parseInt(newValue);
                }
                mIVs[0] = intValue;
                mStats[0] = Pokemon.calculateHP(mBaseStats[0], mIVs[0], mEVs[0], mLevel);
                setStats(mStats[0], -1, -1, -1, -1, -1);
            }
        });

        editText = (EditText) getView().findViewById(R.id.IV_Atk);
        editText.setFilters(new InputFilter[]{new InputFilterMinMax(0, 31)});
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String newValue = s.toString();
                int intValue;
                if (newValue.matches("")) {
                    intValue = 0;
                } else {
                    intValue = Integer.parseInt(newValue);
                }
                mIVs[1] = intValue;
                mStats[1] = Pokemon.calculateAtk(mBaseStats[1], mIVs[1], mEVs[1], mLevel, mNatureMultiplier[1]);
                setStats(-1, mStats[1], -1, -1, -1, -1);
            }
        });

        editText = (EditText) getView().findViewById(R.id.IV_Def);
        editText.setFilters(new InputFilter[]{new InputFilterMinMax(0, 31)});
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String newValue = s.toString();
                int intValue;
                if (newValue.matches("")) {
                    intValue = 0;
                } else {
                    intValue = Integer.parseInt(newValue);
                }
                mIVs[2] = intValue;
                mStats[2] = Pokemon.calculateDef(mBaseStats[2], mIVs[2], mEVs[2], mLevel, mNatureMultiplier[2]);
                setStats(-1, -1, mStats[2], -1, -1, -1);
            }
        });

        editText = (EditText) getView().findViewById(R.id.IV_SpAtk);
        editText.setFilters(new InputFilter[]{new InputFilterMinMax(0, 31)});
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String newValue = s.toString();
                int intValue;
                if (newValue.matches("")) {
                    intValue = 0;
                } else {
                    intValue = Integer.parseInt(newValue);
                }
                mIVs[3] = intValue;
                mStats[3] = Pokemon.calculateSpAtk(mBaseStats[3], mIVs[3], mEVs[3], mLevel, mNatureMultiplier[3]);
                setStats(-1, -1, -1, mStats[3], -1, -1);
            }
        });

        editText = (EditText) getView().findViewById(R.id.IV_SpDef);
        editText.setFilters(new InputFilter[]{new InputFilterMinMax(0, 31)});
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String newValue = s.toString();
                int intValue;
                if (newValue.matches("")) {
                    intValue = 0;
                } else {
                    intValue = Integer.parseInt(newValue);
                }
                mIVs[4] = intValue;
                mStats[4] = Pokemon.calculateSpDef(mBaseStats[4], mIVs[4], mEVs[4], mLevel, mNatureMultiplier[4]);
                setStats(-1, -1, -1, -1, mStats[4], -1);
            }
        });

        editText = (EditText) getView().findViewById(R.id.IV_Spd);
        editText.setFilters(new InputFilter[]{new InputFilterMinMax(0, 31)});
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String newValue = s.toString();
                int intValue;
                if (newValue.matches("")) {
                    intValue = 0;
                } else {
                    intValue = Integer.parseInt(newValue);
                }
                mIVs[5] = intValue;
                mStats[5] = Pokemon.calculateSpd(mBaseStats[5], mIVs[5], mEVs[5], mLevel, mNatureMultiplier[5]);
                setStats(-1, -1, -1, -1, -1, mStats[5]);
            }
        });

    }

    private void setStats(int[] stats) {
        setStats(stats[0], stats[1], stats[2], stats[3], stats[4], stats[5]);
    }

    private void setStats(int HP, int Atk, int Def, int Spd, int SpAtk, int SpDef) {
        TextView textView;
        if (HP != -1) {
            mStats[0] = HP;
            textView = (TextView) getView().findViewById(R.id.final_HP);
            textView.setText(Integer.toString(HP), TextView.BufferType.EDITABLE);
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
        if (Spd != -1) {
            mStats[3] = Spd;
            textView = (TextView) getView().findViewById(R.id.final_Spd);
            textView.setText(Integer.toString(Spd), TextView.BufferType.EDITABLE);
        }
        if (SpAtk != -1) {
            mStats[4] = SpAtk;
            textView = (TextView) getView().findViewById(R.id.final_SpAtk);
            textView.setText(Integer.toString(SpAtk), TextView.BufferType.EDITABLE);
        }
        if (SpDef != -1) {
            mStats[5] = SpDef;
            textView = (TextView) getView().findViewById(R.id.final_SpDef);
            textView.setText(Integer.toString(SpDef), TextView.BufferType.EDITABLE);
        }
    }

    private int getRemainingEVs() {
        return maxEV - mEVs[0] - mEVs[1] - mEVs[2] - mEVs[3] - mEVs[4] - mEVs[5];
    }

    private void setEVs(int[] EVs) {
        setEVs(EVs[0]==0?-1:EVs[0], EVs[1]==0?-1:EVs[1], EVs[2]==0?-1:EVs[2], EVs[3]==0?-1:EVs[3], EVs[4]==0?-1:EVs[4], EVs[5]==0?-1:EVs[5]);
    }

    private void setEVs(int HP, int Atk, int Def, int Spd, int SpAtk, int SpDef) {
        EditText editText;
        SeekBar seekBar;
        if (HP != -1) {
            mEVs[0] = HP;
            editText = (EditText) getView().findViewById(R.id.EV_HP);
            editText.setText(Integer.toString(HP), TextView.BufferType.EDITABLE);
            seekBar = (SeekBar) getView().findViewById(R.id.bar_HP);
            seekBar.setProgress(HP/4);
        }
        if (Atk != -1) {
            mEVs[1] = Atk;
            editText = (EditText) getView().findViewById(R.id.EV_Atk);
            editText.setText(Integer.toString(Atk), TextView.BufferType.EDITABLE);
            seekBar = (SeekBar) getView().findViewById(R.id.bar_Atk);
            seekBar.setProgress(Atk/4);
        }
        if (Def != -1) {
            mEVs[2] = Def;
            editText = (EditText) getView().findViewById(R.id.EV_Def);
            editText.setText(Integer.toString(Def), TextView.BufferType.EDITABLE);
            seekBar = (SeekBar) getView().findViewById(R.id.bar_Def);
            seekBar.setProgress(Def/4);
        }
        if (Spd != -1) {
            mEVs[3] = Spd;
            editText = (EditText) getView().findViewById(R.id.EV_Spd);
            editText.setText(Integer.toString(Spd), TextView.BufferType.EDITABLE);
            seekBar = (SeekBar) getView().findViewById(R.id.bar_Spd);
            seekBar.setProgress(Spd/4);
        }
        if (SpAtk != -1) {
            mEVs[4] = SpAtk;
            editText = (EditText) getView().findViewById(R.id.EV_SpAtk);
            editText.setText(Integer.toString(SpAtk), TextView.BufferType.EDITABLE);
            seekBar = (SeekBar) getView().findViewById(R.id.bar_SpAtk);
            seekBar.setProgress(SpAtk/4);
        }
        if (SpDef != -1) {
            mEVs[5] = SpDef;
            editText = (EditText) getView().findViewById(R.id.EV_SpDef);
            editText.setText(Integer.toString(SpDef), TextView.BufferType.EDITABLE);
            seekBar = (SeekBar) getView().findViewById(R.id.bar_SpDef);
            seekBar.setProgress(SpDef/4);
        }
        editText = (EditText) getView().findViewById(R.id.EV_HP);
        int remainingEVs = getRemainingEVs();
        if (editText.getText().toString().matches("")) {
            editText.setHint(Integer.toString(remainingEVs));
        }
        editText = (EditText) getView().findViewById(R.id.EV_Atk);
        if (editText.getText().toString().matches("")) {
            editText.setHint(Integer.toString(remainingEVs));
        }
        editText = (EditText) getView().findViewById(R.id.EV_Def);
        if (editText.getText().toString().matches("")) {
            editText.setHint(Integer.toString(remainingEVs));
        }
        editText = (EditText) getView().findViewById(R.id.EV_Spd);
        if (editText.getText().toString().matches("")) {
            editText.setHint(Integer.toString(remainingEVs));
        }
        editText = (EditText) getView().findViewById(R.id.EV_SpAtk);
        if (editText.getText().toString().matches("")) {
            editText.setHint(Integer.toString(remainingEVs));
        }
        editText = (EditText) getView().findViewById(R.id.EV_SpDef);
        if (editText.getText().toString().matches("")) {
            editText.setHint(Integer.toString(remainingEVs));
        }
    }

    private void setIVs(int[] IVs) {
        setIVs(IVs[0], IVs[1], IVs[2], IVs[3], IVs[4], IVs[5]);
    }

    private void setIVs(int HP, int Atk, int Def, int Spd, int SpAtk, int SpDef) {
        EditText editText;
        if (HP != -1) {
            mIVs[0] = HP;
            editText = (EditText) getView().findViewById(R.id.IV_HP);
            editText.setText(Integer.toString(HP), TextView.BufferType.EDITABLE);
        }
        if (Atk != -1) {
            mIVs[1] = Atk;
            editText = (EditText) getView().findViewById(R.id.IV_Atk);
            editText.setText(Integer.toString(Atk), TextView.BufferType.EDITABLE);
        }
        if (Def != -1) {
            mIVs[2] = Def;
            editText = (EditText) getView().findViewById(R.id.IV_Def);
            editText.setText(Integer.toString(Def), TextView.BufferType.EDITABLE);
        }
        if (Spd != -1) {
            mIVs[3] = Spd;
            editText = (EditText) getView().findViewById(R.id.IV_Spd);
            editText.setText(Integer.toString(Spd), TextView.BufferType.EDITABLE);
        }
        if (SpAtk != -1) {
            mIVs[4] = SpAtk;
            editText = (EditText) getView().findViewById(R.id.IV_SpAtk);
            editText.setText(Integer.toString(SpAtk), TextView.BufferType.EDITABLE);
        }
        if (SpDef != -1) {
            mIVs[5] = SpDef;
            editText = (EditText) getView().findViewById(R.id.IV_SpDef);
            editText.setText(Integer.toString(SpDef), TextView.BufferType.EDITABLE);
        }
    }
}