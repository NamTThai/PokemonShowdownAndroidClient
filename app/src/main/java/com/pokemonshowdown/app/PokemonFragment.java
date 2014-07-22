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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

/**
 * Created by thain on 7/18/14.
 */
public class PokemonFragment extends DialogFragment {
    public final static String PokemonTAG = "POKEMON_FRAGMENT";

    private int mHP;
    private int mAtk;
    private int mDef;
    private int mSpd;
    private int mSpAtk;
    private int mSpDef;
    private int[] mEV;
    private int[] mIV;
    private int mAbility;
    private String[] mAbilityList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        mHP = Integer.parseInt(getResources().getString(R.string.p184HP));
        mAtk = Integer.parseInt(getResources().getString(R.string.p184Atk));
        mDef = Integer.parseInt(getResources().getString(R.string.p184Def));
        mSpd = Integer.parseInt(getResources().getString(R.string.p184Spd));
        mSpAtk = Integer.parseInt(getResources().getString(R.string.p184SpAtk));
        mSpDef = Integer.parseInt(getResources().getString(R.string.p184SpDef));

        TextView pokemonStats= (TextView) view.findViewById(R.id.stats);
        setStatsString(pokemonStats);
        pokemonStats.setBackgroundResource(R.drawable.editable_frame);

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

    //TODO: get stats power and color code them accordingly
    private void resetStatsColor() {

    }

    private String getStatsString() {
        return ("HP " + Integer.toString(mHP) + " / Atk " + Integer.toString(mAtk) + " / Def " + Integer.toString(mDef) + " / Spd " + Integer.toString(mSpd) + " / SpAtk " + Integer.toString(mSpAtk) + " / SpDef " + Integer.toString(mSpDef));
    }

    private void setStatsString(TextView textView) {
        textView.setText(getStatsString());
    }

    private void closeFragment() {
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

    private class AbilityDialog extends DialogFragment {
        public static final String ATAG = "ABILITY_DIALOG";

        private String[] mAbilityList;

        public AbilityDialog() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            View view = inflater.inflate(R.layout.dialog_ability, container);

            RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.pokemon_ability_list);
            radioGroup.removeAllViews();
            mAbilityList = getArguments().getStringArray("AbilityList");
            int selectedAbility = getArguments().getInt("SelectedAbility");
            for (int i=0; i < mAbilityList.length; i++) {
                RadioButton radioButton = new RadioButton(getActivity());
                radioButton.setText(mAbilityList[i]);
                radioButton.setPadding(0, 0, 48, 0);
                if (i == selectedAbility) {
                    radioButton.setChecked(true);
                }
                radioGroup.addView(radioButton);
            }
            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    PokemonFragment pokemonFragment = (PokemonFragment) fm.findFragmentByTag(PokemonFragment.PokemonTAG);
                    pokemonFragment.setAbility(group.indexOfChild(group.findViewById(checkedId)));
                    getDialog().dismiss();
                }
            });

            return view;
        }
    }

    private class StatsDialog extends DialogFragment {
        public static final String STAG = "STATS_DIALOG";

        public StatsDialog() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            View view = inflater.inflate(R.layout.dialog_stats, container);
        }
    }
}
