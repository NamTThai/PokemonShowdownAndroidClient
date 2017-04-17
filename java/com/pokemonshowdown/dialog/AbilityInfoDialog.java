package com.pokemonshowdown.dialog;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.pokemonshowdown.R;
import com.pokemonshowdown.data.Pokedex;
import com.pokemonshowdown.data.Pokemon;

import java.util.ArrayList;
import java.util.Collections;

public class AbilityInfoDialog extends DialogFragment {
    public final static String ABILITY = "Ability";
    public final static String DESC = "Description";

    private String mAbility;
    private String mDescription;

    public AbilityInfoDialog() {
        // Required empty public constructor
    }

    public static AbilityInfoDialog newInstance(String ability, String description) {
        AbilityInfoDialog fragment = new AbilityInfoDialog();
        Bundle args = new Bundle();
        args.putSerializable(ABILITY, ability);
        args.putSerializable(DESC, description);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
        mAbility = (String) getArguments().getSerializable(ABILITY);
        mDescription = (String) getArguments().getSerializable(DESC);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_ability_info, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView abilityName = (TextView) view.findViewById(R.id.ability_name);
        abilityName.setText(mAbility);

        TextView abilityText = (TextView) view.findViewById(R.id.text);
        abilityText.setText(mDescription);

        ArrayAdapter<String> adapter;
        ArrayList<String> adapterList = new ArrayList<>();

        for (String s : Pokedex.get(getContext()).getPokedexEntries().keySet()) {
            if (Pokedex.get(getContext()).getPokedexEntries().get(s).contains(mAbility)) {
                adapterList.add(s);
            }
        }

        Collections.sort(adapterList);
        adapter = new PokemonAdapter(getActivity(), adapterList);
        ((ListView) view.findViewById(R.id.list)).setAdapter(adapter);
    }

    private class PokemonAdapter extends ArrayAdapter<String> {
        private Activity mContext;

        public PokemonAdapter(Activity getContext, ArrayList<String> pokemonList) {
            super(getContext, 0, pokemonList);
            mContext = getContext;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String pokemonName = getItem(position);

            //if (convertView == null || convertView.findViewById(R.id.text) != null) {
            if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                convertView = mContext.getLayoutInflater().inflate(R.layout.fragment_pokemon_short_vertical, null);
            } else {
                String[] abilities = Pokemon.getPokemonAbilities(getContext(), getItem(position));
                if (abilities.length > 1) {
                    convertView = mContext.getLayoutInflater().inflate(R.layout.fragment_pokemon_short_horizontal, null);
                } else {
                    convertView = mContext.getLayoutInflater().inflate(R.layout.fragment_pokemon_short_horizontal_single, null);
                }
            }
            //}

            ImageView icon = (ImageView) convertView.findViewById(R.id.pokemon_icon);
            icon.setImageResource(Pokemon.getPokemonIcon(getContext(), pokemonName));
            TextView textView = (TextView) convertView.findViewById(R.id.short_pokemon_name);
            textView.setText(Pokemon.getPokemonName(getContext(), pokemonName));
            Integer[] typesIcon = Pokemon.getPokemonTypeIcon(getContext(), pokemonName);
            ImageView type1 = (ImageView) convertView.findViewById(R.id.type_1);
            type1.setImageResource(typesIcon[0]);
            ImageView type2 = (ImageView) convertView.findViewById(R.id.type_2);
            if (typesIcon.length == 2) {
                type2.setImageResource(typesIcon[1]);
            } else {
                type2.setImageResource(0);
            }

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                String[] abilities = Pokemon.getPokemonAbilities(getContext(), pokemonName);

                if (abilities.length > 1) {
                    Log.e("TAG", pokemonName);
                    TextView ability1 = (TextView) convertView.findViewById(R.id.ability_1);
                    TextView ability2 = (TextView) convertView.findViewById(R.id.ability_2);
                    TextView ability3 = (TextView) convertView.findViewById(R.id.ability_3);

                    if (abilities.length == 2) {
                        ability1.setText(abilities[0]);
                        ability2.setText(abilities[1]);
                        ability3.setText("");
                    } else if (abilities.length == 3) {
                        ability1.setText(abilities[0]);
                        ability2.setText(abilities[1]);
                        ability3.setText(abilities[2]);
                    }
                } else {
                    TextView ability1 = (TextView) convertView.findViewById(R.id.ability_1);
                    ability1.setText(abilities[0]);
                }
            }

            Integer[] baseStats = Pokemon.getPokemonBaseStats(getContext(), pokemonName);
            TextView hp = (TextView) convertView.findViewById(R.id.pokemon_short_hp);
            hp.setText(baseStats[0].toString());
            TextView atk = (TextView) convertView.findViewById(R.id.pokemon_short_Atk);
            atk.setText(baseStats[1].toString());
            TextView def = (TextView) convertView.findViewById(R.id.pokemon_short_Def);
            def.setText(baseStats[2].toString());
            TextView spa = (TextView) convertView.findViewById(R.id.pokemon_short_SpAtk);
            spa.setText(baseStats[3].toString());
            TextView spd = (TextView) convertView.findViewById(R.id.pokemon_short_SpDef);
            spd.setText(baseStats[4].toString());
            TextView spe = (TextView) convertView.findViewById(R.id.pokemon_short_Spd);
            spe.setText(baseStats[5].toString());
            int BST = baseStats[0] + baseStats[1] + baseStats[2] + baseStats[3] + baseStats[4] + baseStats[5];
            TextView bst = (TextView) convertView.findViewById(R.id.pokemon_short_BST);
            bst.setText(Integer.toString(BST));
            return convertView;
        }
    }
}
