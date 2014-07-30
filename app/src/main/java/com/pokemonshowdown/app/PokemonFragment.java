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

import java.util.HashMap;
import java.util.Iterator;

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

    private Pokemon mPokemon;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPokemon = new Pokemon(getActivity(), "azumarill");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pokemon, container, false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView pokemonIcon = (ImageView) view.findViewById(R.id.pokemon_icon);
        pokemonIcon.setImageResource(R.drawable.p184s);

        TextView pokemonName = (TextView) view.findViewById(R.id.pokemon_name);
        pokemonName.setText(getPokemon().getName());

        ImageButton imageButton = (ImageButton) view.findViewById(R.id.save_pokemon);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeFragment();
            }
        });

        ImageView pokemonView = (ImageView) view.findViewById(R.id.pokemon_view);
        pokemonView.setImageResource(R.drawable.p184);

        TextView pokemonStats= (TextView) view.findViewById(R.id.stats);
        resetStatsString();
        pokemonStats.setBackgroundResource(R.drawable.editable_frame);
        pokemonStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                StatsDialog statsDialog = new StatsDialog();
                Bundle bundle = new Bundle();
                bundle.putIntArray("Stats", getPokemon().getStats());
                bundle.putIntArray("BaseStats", getPokemon().getBaseStats());
                bundle.putIntArray("EVs", getPokemon().getEVs());
                bundle.putIntArray("IVs", getPokemon().getIVs());
                bundle.putInt("Level", getPokemon().getLevel());
                bundle.putFloatArray("NatureMultiplier", getPokemon().getNatureMultiplier());
                statsDialog.setArguments(bundle);
                statsDialog.show(fm, StatsDialog.STAG);
            }
        });

        TextView pokemonAbility = (TextView) view.findViewById(R.id.stats_abilities);
        pokemonAbility.setBackgroundResource(R.drawable.editable_frame);
        String abilities;
        if (getArguments() != null && getArguments().getString("Ability") != null) {
            getPokemon().setAbility(getArguments().getString("Ability"));
            abilities = getPokemon().getAbility();
        } else {
            HashMap<String, String> abilityList = getPokemon().getAbilityList();
            Iterator<String> abilityTags = abilityList.keySet().iterator();
            String abilityTag = abilityTags.next();
            abilities = abilityList.get(abilityTag);
            while (abilityTags.hasNext()) {
                abilityTag = abilityTags.next();
                abilities += "/" + abilityList.get(abilityTag);
            }
        }

        pokemonAbility.setText(abilities);
        pokemonAbility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                AbilityDialog abilityDialog = new AbilityDialog();
                Bundle bundle = new Bundle();
                bundle.putSerializable("AbilityList", getPokemon().getAbilityList());
                bundle.putString("SelectedAbility", getPokemon().getAbilityTag());
                abilityDialog.setArguments(bundle);
                abilityDialog.show(fm, AbilityDialog.ATAG);
            }
        });
    }

    public Pokemon getPokemon() {
        return mPokemon;
    }

    public void setAbilityString(String ability) {
        TextView pokemonAbility = (TextView) getView().findViewById(R.id.stats_abilities);
        pokemonAbility.setText(ability);
    }

    public String getStatsString() {
        return ("HP " + Integer.toString(getPokemon().getHP()) + " / Atk " + Integer.toString(getPokemon().getAtk()) + " / Def " + Integer.toString(getPokemon().getDef()) + " / SpAtk " + Integer.toString(getPokemon().getSpAtk()) + " / SpDef " + Integer.toString(getPokemon().getSpDef()) + " / Spd " + Integer.toString(getPokemon().getSpd()));
    }

    public void resetStatsString() {
        TextView pokemonStats= (TextView) getView().findViewById(R.id.stats);
        pokemonStats.setText(getStatsString());
    }

    private String getBaseStatsString() {
        return ("HP " + Integer.toString(getPokemon().getBaseHP()) + " / Atk " + Integer.toString(getPokemon().getBaseAtk()) + " / Def " + Integer.toString(getPokemon().getBaseDef()) + " / SpAtk " + Integer.toString(getPokemon().getBaseSpAtk()) + " / SpDef " + Integer.toString(getPokemon().getBaseSpDef()) + " / Spd " + Integer.toString(getPokemon().getBaseSpd()));
    }

    private void resetBaseStatsString() {
        TextView pokemonStats= (TextView) getView().findViewById(R.id.stats);
        pokemonStats.setText(getBaseStatsString());
    }

    private void closeFragment() {
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

}
