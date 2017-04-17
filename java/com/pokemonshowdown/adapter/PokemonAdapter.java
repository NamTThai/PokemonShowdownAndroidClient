package com.pokemonshowdown.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.pokemonshowdown.R;
import com.pokemonshowdown.activity.PokemonActivity;
import com.pokemonshowdown.activity.TeamBuildingActivity;
import com.pokemonshowdown.data.ItemDex;
import com.pokemonshowdown.data.MoveDex;
import com.pokemonshowdown.data.Pokemon;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import me.grantland.widget.AutofitTextView;

/**
 * Created by McBeengs on 29/10/2016.
 */

public class PokemonAdapter extends RecyclerView.Adapter<PokemonAdapter.ViewHolder> {

    private Context mContext;
    private List<Pokemon> mPokemons;

    public PokemonAdapter(Context context, List<Pokemon> pokemons) {
        mContext = context;
        mPokemons = pokemons;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View taskView = inflater.inflate(R.layout.fragment_team_building_pokemon_row, parent, false);
        return new ViewHolder(taskView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Pokemon poke = mPokemons.get(position);

        holder.pokemonIcon.setImageResource(poke.getFrontSprite());
        holder.pokemonName.setText(poke.getNickName());
        holder.ability.setText(poke.getAbility(mContext));

        if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            holder.level.setText("" + poke.getLevel());
            if (poke.getGender().equals("M")) {
                holder.gender.setText("Male");
            } else if (poke.getGender().equals("F")) {
                holder.gender.setText("Female");
            } else {
                holder.gender.setText("Genderless");
            }
        }

        String itemString = poke.getItem();
        JSONObject itemJSon = ItemDex.get(mContext).getItemJsonObject(itemString);
        if (itemJSon != null) {
            try {
                String itemName = itemJSon.getString("name");
                holder.itemName.setText(itemName);
                int itemDrawable = ItemDex.getItemIcon(mContext, poke.getItem());
                if (itemDrawable != 0) {
                    holder.itemIcon.setImageResource(itemDrawable);
                }
            } catch (JSONException e) {
                holder.itemName.setText(R.string.pokemon_nohelditem);
            }
        } else {
            //wrong item data
            holder.itemName.setText(R.string.pokemon_nohelditem);
            poke.setItem("");
        }

        String m1 = poke.getMove1().isEmpty() ? "Empty" : poke.getMove1();
        String m2 = poke.getMove2().isEmpty() ? "Empty" : poke.getMove2();
        String m3 = poke.getMove3().isEmpty() ? "Empty" : poke.getMove3();
        String m4 = poke.getMove4().isEmpty() ? "Empty" : poke.getMove4();

        if (!m1.equals("Empty")) {
            m1 = m1.toLowerCase().replace("-", "").trim();
            JSONObject m1Json = MoveDex.get(mContext).getMoveJsonObject(m1);
            try {
                Log.d("gukgbjh", m1Json.toString());
                m1 = m1Json.getString("name");
                holder.move1.setText(m1);
            } catch (JSONException e) {
                holder.move1.setText("Empty");
            }
        } else {
            holder.move1.setText(m1);
        }

        if (!m2.equals("Empty")) {
            m2 = m2.toLowerCase().replace("-", "").trim();
            JSONObject m2Json = MoveDex.get(mContext).getMoveJsonObject(m2);
            try {
                m2 = m2Json.getString("name");
                holder.move2.setText(m2);
            } catch (JSONException e) {
                holder.move2.setText("Empty");
            }
        } else {
            holder.move2.setText(m2);
        }

        if (!m3.equals("Empty")) {
            m3 = m3.toLowerCase().replace("-", "").trim();
            JSONObject m3Json = MoveDex.get(mContext).getMoveJsonObject(m3);
            try {
                m3 = m3Json.getString("name");
                holder.move3.setText(m3);
            } catch (JSONException e) {
                holder.move3.setText("Empty");
            }
        } else {
            holder.move3.setText(m3);
        }

        if (!m4.equals("Empty")) {
            m4 = m4.toLowerCase().replace("-", "").trim();
            JSONObject m4Json = MoveDex.get(mContext).getMoveJsonObject(m4);
            try {
                m4 = m4Json.getString("name");
                holder.move4.setText(m4);
            } catch (JSONException e) {
                holder.move4.setText("Empty");
            }
        } else {
            holder.move4.setText(m4);
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, PokemonActivity.class);
                intent.putExtra("pokemon", new Gson().toJson(poke));
                intent.putExtra("position", position);
                mContext.startActivity(intent);
            }
        });

        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new AlertDialog.Builder(mContext).setTitle("Delete Pokémon")
                        .setMessage("Are you sure you want to delete\"" + holder.pokemonName.getText() + "\"?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int uh) {
                                Toast.makeText(mContext, "\"" + holder.pokemonName.getText() + "\" removed", Toast.LENGTH_SHORT).show();
                                TeamBuildingActivity.ACCESSOR.fireAddButtonVisibility();
                                TeamBuildingActivity.ACCESSOR.firePokemonTeamSaving(position);
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPokemons.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public View mView;
        public ImageView pokemonIcon;
        public AutofitTextView pokemonName;
        public AutofitTextView ability;
        public ImageView itemIcon;
        public AutofitTextView level;
        public AutofitTextView gender;
        public AutofitTextView itemName;
        public AutofitTextView move1;
        public AutofitTextView move2;
        public AutofitTextView move3;
        public AutofitTextView move4;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            pokemonIcon = (ImageView) itemView.findViewById(R.id.pokemon_icon);
            pokemonName = (AutofitTextView) itemView.findViewById(R.id.pokemon_name);
            ability = (AutofitTextView) itemView.findViewById(R.id.ability);
            itemIcon = (ImageView) itemView.findViewById(R.id.item_icon);
            itemName = (AutofitTextView) itemView.findViewById(R.id.item_name);
            move1 = (AutofitTextView) itemView.findViewById(R.id.move_1);
            move2 = (AutofitTextView) itemView.findViewById(R.id.move_2);
            move3 = (AutofitTextView) itemView.findViewById(R.id.move_3);
            move4 = (AutofitTextView) itemView.findViewById(R.id.move_4);

            if (mView.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                level = (AutofitTextView) itemView.findViewById(R.id.level);
                gender = (AutofitTextView) itemView.findViewById(R.id.gender);
            }
        }
    }
}
