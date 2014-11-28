package com.pokemonshowdown.app;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pokemonshowdown.data.Pokemon;
import com.pokemonshowdown.data.PokemonTeam;

import java.util.List;

/**
 * Class used to show the team list in the drawer (6 icons + nickname)
 */
public class PokemonTeamListArrayAdapter extends ArrayAdapter<PokemonTeam> {
    private Context ctx;

    public PokemonTeamListArrayAdapter(Context getContext, List<PokemonTeam> userListData) {
        super(getContext, R.layout.listwidget_teampreview, R.id.team_nickname, userListData);
        this.ctx = getContext;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        List<PokemonTeam> pokemonTeamList = PokemonTeam.getPokemonTeamList();
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listwidget_teampreview, null);
        }
        PokemonTeam p = pokemonTeamList.get(position);

        TextView teamName = (TextView) convertView.findViewById(R.id.team_nickname);
        if (p.getTier().equals("(None)")) {
            teamName.setText(p.getNickname());
        } else {
            teamName.setText(p.getNickname() + " (" + p.getTier() + ")");
        }

        LinearLayout layout = (LinearLayout) convertView.findViewById(R.id.pokemon_small_icon_list);
        layout.removeAllViews();

        for (Pokemon pokemon : p.getPokemons()) {
            if (pokemon != null) {
                ImageView image = new ImageView(getContext());
                int smallIconId = pokemon.getIconSmall();
                Drawable d = ctx.getResources().getDrawable(smallIconId);
                image.setImageDrawable(d);
                layout.addView(image);
            }
        }

        for (int i = 0; i < 6 - p.getPokemons().size(); i++) {
            ImageView image = new ImageView(getContext());
            Drawable d = ctx.getResources().getDrawable(R.drawable.smallicons_0);
            image.setImageDrawable(d);
            layout.addView(image);
        }


        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}