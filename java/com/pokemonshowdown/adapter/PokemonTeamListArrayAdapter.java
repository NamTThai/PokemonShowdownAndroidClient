package com.pokemonshowdown.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pokemonshowdown.R;
import com.pokemonshowdown.data.Pokemon;
import com.pokemonshowdown.data.PokemonTeam;

import java.util.List;

/**
 * Class used to show the team list in the drawer (6 icons + nickname)
 */
public class PokemonTeamListArrayAdapter extends ArrayAdapter<PokemonTeam> {
    private Context mContext;
    private List<PokemonTeam> mPokemonTeamList;

    public PokemonTeamListArrayAdapter(Context getContext, List<PokemonTeam> userListData) {
        super(getContext, R.layout.listwidget_teampreview, R.id.team_nickname, userListData);
        this.mContext = getContext;
        mPokemonTeamList = userListData;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listwidget_teampreview, null);
        }
        PokemonTeam p = mPokemonTeamList.get(position);

        TextView teamName = (TextView) convertView.findViewById(R.id.team_nickname);
        if (p.getTier().isEmpty()) {
            teamName.setText(p.getNickname());
        } else {
            teamName.setText(p.getNickname() + " (" + p.getTier() + ")");
        }

        int c = 1;
        for (Pokemon pokemon : p.getPokemons()) {
            if (pokemon != null) {
                switch (c) {
                    case 1:
                        ((ImageView) convertView.findViewById(R.id.team_pokemon_icon_1)).setImageDrawable(mContext.getResources()
                                .getDrawable(pokemon.getIcon()));
                        break;
                    case 2:
                        ((ImageView) convertView.findViewById(R.id.team_pokemon_icon_2)).setImageDrawable(mContext.getResources()
                                .getDrawable(pokemon.getIcon()));
                        break;
                    case 3:
                        ((ImageView) convertView.findViewById(R.id.team_pokemon_icon_3)).setImageDrawable(mContext.getResources()
                                .getDrawable(pokemon.getIcon()));
                        break;
                    case 4:
                        ((ImageView) convertView.findViewById(R.id.team_pokemon_icon_4)).setImageDrawable(mContext.getResources()
                                .getDrawable(pokemon.getIcon()));
                        break;
                    case 5:
                        ((ImageView) convertView.findViewById(R.id.team_pokemon_icon_5)).setImageDrawable(mContext.getResources()
                                .getDrawable(pokemon.getIcon()));
                        break;
                    case 6:
                        ((ImageView) convertView.findViewById(R.id.team_pokemon_icon_6)).setImageDrawable(mContext.getResources()
                                .getDrawable(pokemon.getIcon()));
                        break;
                }
            }
            c++;
        }

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}