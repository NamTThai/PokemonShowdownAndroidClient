package com.pokemonshowdown.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pokemonshowdown.data.Pokemon;

import java.util.HashMap;
import java.util.Iterator;

public class LevelDialog extends DialogFragment {
    public static final String LTAG = "LEVEL_DIALOG";

    private int mLevel;

    public LevelDialog() {

    }

    @Override
    @SuppressWarnings("unchecked")
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.dialog_level, container);
        mLevel = getArguments().getInt("Level");

        final TextView textView = (TextView) view.findViewById(R.id.level);
        textView.setText(Integer.toString(mLevel));

        // Below are SeekBars
        SeekBar seekBar = (SeekBar) view.findViewById(R.id.bar_level);
        seekBar.setProgress(mLevel);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mLevel = progress;
                textView.setText(Integer.toString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        view.findViewById(R.id.save_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                PokemonFragment pokemonFragment = (PokemonFragment) fm.findFragmentByTag(PokemonFragment.PokemonTAG);
                pokemonFragment.getPokemon().setLevel(mLevel);
                pokemonFragment.getPokemon().setStats(pokemonFragment.getPokemon().calculateStats());
                pokemonFragment.resetStatsString();
                pokemonFragment.setLevelString(pokemonFragment.getPokemon().getLevel());
                getDialog().dismiss();
            }
        });

        return view;
    }
}