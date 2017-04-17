package com.pokemonshowdown.dialog;

import android.app.Activity;
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
import com.pokemonshowdown.data.Learnset;
import com.pokemonshowdown.data.MoveDex;
import com.pokemonshowdown.data.Pokemon;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import me.grantland.widget.AutofitTextView;

public class PokemonInfoDialog extends DialogFragment {
    public final static String POKEMON_INFO = "PokemonInfo";

    private Pokemon mPokemon;

    public PokemonInfoDialog() {
        // Required empty public constructor
    }

    public static PokemonInfoDialog newInstance(Pokemon pkm) {
        PokemonInfoDialog fragment = new PokemonInfoDialog();
        Bundle args = new Bundle();
        args.putSerializable(POKEMON_INFO, pkm);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
        mPokemon = (Pokemon) getArguments().getSerializable(POKEMON_INFO);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_pokemon_info, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int[] typeIcon = mPokemon.getTypeIcon();

        TextView pokemonName = (TextView) view.findViewById(R.id.pokemon_name);
        pokemonName.setText(mPokemon.getName());
        pokemonName.setCompoundDrawablesWithIntrinsicBounds(mPokemon.getIcon(), 0, 0, 0);
        pokemonName.setCompoundDrawablePadding(8);

        ImageView type1 = (ImageView) view.findViewById(R.id.type_1);
        type1.setImageResource(typeIcon[0]);
        ImageView type2 = (ImageView) view.findViewById(R.id.type_2);
        if (typeIcon.length > 1) {
            type2.setImageResource(typeIcon[1]);
        } else {
            type2.setVisibility(View.INVISIBLE);
        }

        ImageView pokemonView = (ImageView) view.findViewById(R.id.pokemon_view);
        pokemonView.setImageResource(mPokemon.getFrontSprite());

        AutofitTextView pokemonStats = (AutofitTextView) view.findViewById(R.id.stats);
        pokemonStats.setText(getStatsString());

        TextView pokemonAbility = (TextView) view.findViewById(R.id.ability);
        pokemonAbility.setText(mPokemon.getAbility(getContext()));

        ArrayAdapter<String> adapter;
        ArrayList<String> adapterList = new ArrayList<>();

        ArrayList<String> tempArray = Learnset.get(getContext()).getLearnetEntry(mPokemon.getName());
        if (tempArray != null) {
            for (String move : tempArray) {
                if (!adapterList.contains(move)) {
                    adapterList.add(move);
                }
            }
        }

        Collections.sort(adapterList);
        adapter = new MovesAdapter(getActivity(), adapterList);
        ((ListView) view.findViewById(R.id.list)).setAdapter(adapter);
    }

    public String getStatsString() {
        int[] stats = mPokemon.getBaseStats();
        return ("HP " + stats[0] + " / Atk " + stats[1] + " / Def " + stats[2] + " / SpA " + stats[3] + " / SpD " + stats[4] + " / Spe " + stats[5]);
    }

    private class MovesAdapter extends ArrayAdapter<String> {
        private Activity mContext;

        public MovesAdapter(Activity getContext, ArrayList<String> pokemonList) {
            super(getContext, 0, pokemonList);
            mContext = getContext;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mContext.getLayoutInflater().inflate(R.layout.fragment_moves_list_row, null);
            }

            try {
                String move = getItem(position);

                JSONObject moveJson = MoveDex.get(getContext()).getMoveJsonObject(move);
                AutofitTextView textView = (AutofitTextView) convertView.findViewById(R.id.short_move_name);
                textView.setText(moveJson.getString("name"));
                ImageView type = (ImageView) convertView.findViewById(R.id.type);
                type.setImageResource(MoveDex.getTypeIcon(getContext(), moveJson.getString("type")));
                ImageView category = (ImageView) convertView.findViewById(R.id.category);
                category.setImageResource(MoveDex.getCategoryIcon(getContext(), moveJson.getString("category")));

                AutofitTextView power = (AutofitTextView) convertView.findViewById(R.id.move_power);
                String pow = moveJson.getString("basePower");
                if (pow.equals("0")) {
                    power.setText("--");
                } else {
                    power.setText(pow);
                }
                power.setLines(1);
                AutofitTextView acc = (AutofitTextView) convertView.findViewById(R.id.move_acc);
                String accuracy = moveJson.getString("accuracy");
                if (accuracy.equals("true")) {
                    accuracy = "--";
                }
                acc.setText(accuracy);
                acc.setLines(1);
                AutofitTextView pp = (AutofitTextView) convertView.findViewById(R.id.move_pp);
                pp.setText(MoveDex.getMaxPP(moveJson.getString("pp")));
                pp.setLines(1);
            } catch (JSONException e) {
                Log.d("TAG", e.toString());
            }
            return convertView;
        }
    }
}
