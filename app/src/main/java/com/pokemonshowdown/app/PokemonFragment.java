package com.pokemonshowdown.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;

/**
 * Created by thain on 7/18/14.
 *
 * Array indices:
 * 0 HP
 * 1 Atk
 * 2 Def
 * 3 Spd
 * 4 SpAtk
 * 5 SpDef
 */
public class PokemonFragment extends DialogFragment {
    public final static String PokemonTAG = "POKEMON_FRAGMENT";

    private int[] mStats;
    private int[] mBaseStats;
    private int[] mEV;
    private int[] mIV;
    private int mAbility;
    private String[] mAbilityList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStats = new int[6];
        mBaseStats = new int[6];
        mEV = new int[6];
        mIV = new int[6];
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pokemon, container, false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        ImageView pokemonIcon = (ImageView) view.findViewById(R.id.pokemon_icon);
        pokemonIcon.setImageResource(R.drawable.p184s);

        TextView pokemonName = (TextView) view.findViewById(R.id.pokemon_name);
        pokemonName.setText(R.string.p184);

        ImageButton imageButton = (ImageButton) view.findViewById(R.id.save_pokemon);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeFragment();
            }
        });

        ImageView pokemonView = (ImageView) view.findViewById(R.id.pokemon_view);
        pokemonView.setImageResource(R.drawable.p184);

        mStats[0] = Integer.parseInt(getResources().getString(R.string.p184HP));
        mStats[1] = Integer.parseInt(getResources().getString(R.string.p184Atk));
        mStats[2] = Integer.parseInt(getResources().getString(R.string.p184Def));
        mStats[3] = Integer.parseInt(getResources().getString(R.string.p184Spd));
        mStats[4] = Integer.parseInt(getResources().getString(R.string.p184SpAtk));
        mStats[5] = Integer.parseInt(getResources().getString(R.string.p184SpDef));

        Arrays.fill(mIV, 31);

        TextView pokemonStats= (TextView) view.findViewById(R.id.stats);
        setStatsString(pokemonStats);
        pokemonStats.setBackgroundResource(R.drawable.editable_frame);
        pokemonStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                StatsDialog statsDialog = new StatsDialog();
                Bundle bundle = new Bundle();
                bundle.putIntArray("Stats", mStats);
                bundle.putIntArray("EVs", mEV);
                bundle.putIntArray("IVs", mIV);
                statsDialog.setArguments(bundle);
                statsDialog.show(fm, StatsDialog.STAG);
            }
        });

        TextView pokemonAbility = (TextView) view.findViewById(R.id.stats_abilities);
        pokemonAbility.setBackgroundResource(R.drawable.editable_frame);
        mAbilityList = getResources().getStringArray(R.array.p184Ability);
        String abilities = mAbilityList[0];
        for (int i=1; i < mAbilityList.length; i++) {
            abilities += " / " + mAbilityList[i];
        }
        if (getArguments() != null && getArguments().getInt("Ability") != 0) {
            setAbility(getArguments().getInt("Ability") - 1);
        } else {
            mAbility = -1;
        }

        pokemonAbility.setText(abilities);
        pokemonAbility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                AbilityDialog abilityDialog = new AbilityDialog();
                Bundle bundle = new Bundle();
                bundle.putStringArray("AbilityList", mAbilityList);
                bundle.putInt("SelectedAbility", mAbility);
                abilityDialog.setArguments(bundle);
                abilityDialog.show(fm, AbilityDialog.ATAG);
            }
        });

        return view;
    }

    public int[] getEV() {
        return mEV;
    }

    public void setEV(int[] EV) {
        mEV = EV;
    }

    public int[] getIV() {
        return mIV;
    }

    public void setIV(int[] IV) {
        mIV = IV;
    }

    public int getAbility() {
        return mAbility;
    }

    public void setAbility(int ability) {
        mAbility = ability;
        if (mAbility != -1) {
            TextView pokemonAbility = (TextView) getView().findViewById(R.id.stats_abilities);
            pokemonAbility.setText(mAbilityList[ability]);
        }
    }

    public String[] getAbilityList() {
        return mAbilityList;
    }

    public void setAbilityList(String[] abilityList) {
        mAbilityList = abilityList;
    }

    private String getStatsString() {
        return ("HP " + Integer.toString(mStats[0]) + " / Atk " + Integer.toString(mStats[1]) + " / Def " + Integer.toString(mStats[2]) + " / Spd " + Integer.toString(mStats[3]) + " / SpAtk " + Integer.toString(mStats[4]) + " / SpDef " + Integer.toString(mStats[5]));
    }

    private void setStatsString(TextView textView) {
        textView.setText(getStatsString());
    }

    private String getBaseStatsString() {
        return ("HP " + Integer.toString(mBaseStats[0]) + " / Atk " + Integer.toString(mBaseStats[1]) + " / Def " + Integer.toString(mBaseStats[2]) + " / Spd " + Integer.toString(mBaseStats[3]) + " / SpAtk " + Integer.toString(mBaseStats[4]) + " / SpDef " + Integer.toString(mBaseStats[5]));
    }

    private void setBaseStatsString(TextView textView) {
        textView.setText(getBaseStatsString());
    }

    private void closeFragment() {
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

}
