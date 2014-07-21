package com.pokemonshowdown.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

/**
 * Created by thain on 7/18/14.
 */
public class PokemonFragment extends DialogFragment {
    private int mHP;
    private int mAtk;
    private int mDef;
    private int mSpd;
    private int mSpAtk;
    private int mSpDef;
    private int[] mEV;
    private int[] mIV;
    private String mAbility;
    private String[] mAbilityList;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pokemon, container, false);

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

        TextView pokemonHP = (TextView) view.findViewById(R.id.stats_HP);
        pokemonHP.setText("HP "+getResources().getString(R.string.p184HP));
        mHP = Integer.parseInt(getResources().getString(R.string.p184HP));
        TextView pokemonAtk = (TextView) view.findViewById(R.id.stats_Atk);
        pokemonAtk.setText("Atk "+getResources().getString(R.string.p184Atk));
        mAtk = Integer.parseInt(getResources().getString(R.string.p184Atk));
        TextView pokemonDef = (TextView) view.findViewById(R.id.stats_Def);
        pokemonDef.setText("Def "+getResources().getString(R.string.p184Def));
        mDef = Integer.parseInt(getResources().getString(R.string.p184Def));
        TextView pokemonSpd = (TextView) view.findViewById(R.id.stats_Spd);
        pokemonSpd.setText("Spd "+getResources().getString(R.string.p184Spd));
        mSpd = Integer.parseInt(getResources().getString(R.string.p184Spd));
        TextView pokemonSpAtk = (TextView) view.findViewById(R.id.stats_SpAtk);
        pokemonSpAtk.setText("SpAtk "+getResources().getString(R.string.p184SpAtk));
        mSpAtk = Integer.parseInt(getResources().getString(R.string.p184SpAtk));
        TextView pokemonSpDef = (TextView) view.findViewById(R.id.stats_SpDef);
        pokemonSpDef.setText("SpDef "+getResources().getString(R.string.p184SpDef));
        mSpDef = Integer.parseInt(getResources().getString(R.string.p184SpDef));

        TextView pokemonAbility = (TextView) view.findViewById(R.id.stats_abilities);
        mAbilityList = getResources().getStringArray(R.array.p184Ability);
        String abilities = mAbilityList[0];
        for (int i=1; i < mAbilityList.length; i++) {
            abilities += " / " + mAbilityList[i];
        }
        pokemonAbility.setText(abilities);
        pokemonAbility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager().
            }
        });

        return view;
    }

    public int getHP() {
        return mHP;
    }

    public void setHP(int HP) {
        mHP = HP;
    }

    public int getAtk() {
        return mAtk;
    }

    public void setAtk(int atk) {
        mAtk = atk;
    }

    public int getDef() {
        return mDef;
    }

    public void setDef(int def) {
        mDef = def;
    }

    public int getSpd() {
        return mSpd;
    }

    public void setSpd(int spd) {
        mSpd = spd;
    }

    public int getSpAtk() {
        return mSpAtk;
    }

    public void setSpAtk(int spAtk) {
        mSpAtk = spAtk;
    }

    public int getSpDef() {
        return mSpDef;
    }

    public void setSpDef(int spDef) {
        mSpDef = spDef;
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

    public String getAbility() {
        return mAbility;
    }

    public void setAbility(String ability) {
        mAbility = ability;
    }

    public String[] getAbilityList() {
        return mAbilityList;
    }

    public void setAbilityList(String[] abilityList) {
        mAbilityList = abilityList;
    }

    //TODO: get stats power and color code them accordingly
    private void resetStatsColor() {

    }

    private void closeFragment() {
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

    private class AbilityDialog extends DialogFragment {

        public AbilityDialog() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.dialog_ability, container);
            RadioGroup abilityDialog = (RadioGroup) view.findViewById(R.id.pokemon_ability_list);


            return view;
        }
    }
}
