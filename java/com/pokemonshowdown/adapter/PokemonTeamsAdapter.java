package com.pokemonshowdown.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.pokemonshowdown.R;
import com.pokemonshowdown.activity.TeamBuilderActivity;
import com.pokemonshowdown.activity.TeamBuildingActivity;
import com.pokemonshowdown.application.MyApplication;
import com.pokemonshowdown.data.BattleFieldData;
import com.pokemonshowdown.data.Pokemon;
import com.pokemonshowdown.data.PokemonTeam;

import java.util.ArrayList;
import java.util.List;

import me.grantland.widget.AutofitTextView;

/**
 * Created by McBeengs on 29/10/2016.
 */

public class PokemonTeamsAdapter extends RecyclerView.Adapter<PokemonTeamsAdapter.ViewHolder> {

    private Context mContext;
    private List<PokemonTeam> mPokemonTeamsList;

    public PokemonTeamsAdapter(Context context, List<PokemonTeam> pokemonTeamsList) {
        mContext = context;
        mPokemonTeamsList = pokemonTeamsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View taskView = inflater.inflate(R.layout.fragment_team_builder_team_row, parent, false);
        return new ViewHolder(taskView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final PokemonTeam p = mPokemonTeamsList.get(position);

        if (p.getNickname().isEmpty()) {
            holder.teamNickname.setText("Team nº" + (position + 1));
        } else {
            holder.teamNickname.setText(p.getNickname());
        }

        int c = 0;
        for (Pokemon pokemon : p.getPokemons()) {
            if (pokemon != null) {
                int smallIconId = pokemon.getIcon();
                Drawable d = mContext.getResources().getDrawable(smallIconId);

                switch (c) {
                    case 0:
                        holder.icon1.setImageDrawable(d);
                        break;
                    case 1:
                        holder.icon2.setImageDrawable(d);
                        break;
                    case 2:
                        holder.icon3.setImageDrawable(d);
                        break;
                    case 3:
                        holder.icon4.setImageDrawable(d);
                        break;
                    case 4:
                        holder.icon5.setImageDrawable(d);
                        break;
                    case 5:
                        holder.icon6.setImageDrawable(d);
                        break;
                }
            }
            c++;
        }

        holder.tierLabel.setText(p.getTier());

        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new AlertDialog.Builder(mContext).setTitle("Delete Pokémon")
                        .setMessage("Are you sure you want to delete \"" + holder.teamNickname.getText() + "\"?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int uh) {
                                Toast.makeText(mContext, "\"" + holder.teamNickname.getText() + "\" removed", Toast.LENGTH_SHORT).show();
                                TeamBuilderActivity.ACCESSOR.deletePokemonTeam(p);
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
                return false;
            }
        });

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, TeamBuildingActivity.class);
                intent.putExtra("team", new Gson().toJson(p));
                mContext.startActivity(intent);
            }
        });

        holder.teamNickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText name = new EditText(mContext);
                name.setText(holder.teamNickname.getText());
                new AlertDialog.Builder(mContext).setTitle("Change name")
                        .setView(name)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                PokemonTeam temp = p;
                                p.setNickname(name.getText().toString());
                                int pos = TeamBuilderActivity.ACCESSOR.deletePokemonTeam(temp);
                                TeamBuilderActivity.ACCESSOR.savePokemonTeam(p, pos);
                                Toast.makeText(mContext, "Team renamed", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", null).show();
            }
        });

        holder.tierLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Spinner spinner = new Spinner(mContext);
                int dp = 15;
                int pixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, mContext.getResources().getDisplayMetrics());
                spinner.setPadding(pixels, pixels, pixels, pixels);
                ArrayList<String> formats = new ArrayList<>();

                for (BattleFieldData.FormatType type : BattleFieldData.get(mContext).getFormatTypes()) {
                    for (BattleFieldData.Format format : type.getFormatList()) {
                        if (!format.getName().contains("Random")) {
                            formats.add(format.getName());
                        }
                    }
                }

                ArrayAdapter<String> formatsAdapter = new ArrayAdapter<>(mContext, R.layout.fragment_user_list, formats);
                formatsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(formatsAdapter);

                new AlertDialog.Builder(mContext).setTitle("Set tier")
                        .setView(spinner)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                PokemonTeam temp = p;
                                p.setTier(MyApplication.toId((String) spinner.getSelectedItem()));
                                p.setTier((String) spinner.getSelectedItem());
                                holder.tierLabel.setText((String) spinner.getSelectedItem());
                                int pos = TeamBuilderActivity.ACCESSOR.deletePokemonTeam(temp);
                                TeamBuilderActivity.ACCESSOR.savePokemonTeam(p, pos);
                                Toast.makeText(mContext, "Tier updated", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", null).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPokemonTeamsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public View mView;
        public AutofitTextView teamNickname;
        public TextView tierLabel;
        public ImageView icon1;
        public ImageView icon2;
        public ImageView icon3;
        public ImageView icon4;
        public ImageView icon5;
        public ImageView icon6;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            itemView.setClickable(true);

            teamNickname = (AutofitTextView) itemView.findViewById(R.id.team_nickname);
            tierLabel = (TextView) itemView.findViewById(R.id.tier_label);
            icon1 = (ImageView) itemView.findViewById(R.id.team_pokemon_icon_1);
            icon2 = (ImageView) itemView.findViewById(R.id.team_pokemon_icon_2);
            icon3 = (ImageView) itemView.findViewById(R.id.team_pokemon_icon_3);
            icon4 = (ImageView) itemView.findViewById(R.id.team_pokemon_icon_4);
            icon5 = (ImageView) itemView.findViewById(R.id.team_pokemon_icon_5);
            icon6 = (ImageView) itemView.findViewById(R.id.team_pokemon_icon_6);
        }
    }
}
