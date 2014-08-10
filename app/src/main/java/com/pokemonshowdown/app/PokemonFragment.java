package com.pokemonshowdown.app;

import android.content.Intent;
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

import com.pokemonshowdown.data.Pokemon;
import com.pokemonshowdown.data.SearchableActivity;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by thain on 7/18/14.
 */
public class PokemonFragment extends DialogFragment {
    public final static String PokemonTAG = "POKEMON_FRAGMENT";

    private Pokemon mPokemon;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPokemon = (Pokemon) getArguments().getSerializable("Pokemon");
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
        pokemonIcon.setImageResource(getPokemon().getIconSmall());

        TextView pokemonName = (TextView) view.findViewById(R.id.pokemon_name);
        pokemonName.setText(getPokemon().getName());

        ImageView pokemonView = (ImageView) view.findViewById(R.id.pokemon_view);
        pokemonView.setImageResource(getPokemon().getIcon());

        if (getArguments().getBoolean("Search")) {
            addSearchWidget(view);
        }

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
        if (getPokemon().getAbilityTag() != null) {
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

    private void addSearchWidget(View view) {
        ImageButton imageButton = (ImageButton) view.findViewById(R.id.pokemon_fragment_functions);
        imageButton.setImageResource(R.drawable.ic_action_search);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeFragment();
                Intent intent = new Intent(getActivity(), SearchableActivity.class);
                intent.putExtra("Search Type", SearchableActivity.REQUEST_CODE_SEARCH_POKEMON);
                getActivity().startActivityForResult(intent, SearchableActivity.REQUEST_CODE_SEARCH_POKEMON);
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
        return ("HP " + Integer.toString(getPokemon().getHP()) + " / Atk " + Integer.toString(getPokemon().getAtk()) + " / Def " + Integer.toString(getPokemon().getDef()) + " / SpA " + Integer.toString(getPokemon().getSpAtk()) + " / SpD " + Integer.toString(getPokemon().getSpDef()) + " / Spe " + Integer.toString(getPokemon().getSpd()));
    }

    public void resetStatsString() {
        TextView pokemonStats= (TextView) getView().findViewById(R.id.stats);
        pokemonStats.setText(getStatsString());
    }

    private String getBaseStatsString() {
        return ("HP " + Integer.toString(getPokemon().getBaseHP()) + " / Atk " + Integer.toString(getPokemon().getBaseAtk()) + " / Def " + Integer.toString(getPokemon().getBaseDef()) + " / SpA " + Integer.toString(getPokemon().getBaseSpAtk()) + " / SpD " + Integer.toString(getPokemon().getBaseSpDef()) + " / Spe " + Integer.toString(getPokemon().getBaseSpd()));
    }

    private void resetBaseStatsString() {
        TextView pokemonStats= (TextView) getView().findViewById(R.id.stats);
        pokemonStats.setText(getBaseStatsString());
    }

    private void closeFragment() {
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

}
