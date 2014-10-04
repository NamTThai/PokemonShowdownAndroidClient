package com.pokemonshowdown.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;

import com.pokemonshowdown.data.Pokemon;
import com.pokemonshowdown.data.PokemonTeam;

import java.util.List;


public class TeamBuildingActivity extends FragmentActivity {
    public static final String TAG = TeamBuildingActivity.class.getName();
    private Spinner pkmn_spinner;
    private List<PokemonTeam> pokemonTeamList = PokemonTeam.getPokemonTeamList();
    private PokemonTeamListArrayAdapter pokemonTeamListArrayAdapter;

    public void updateList() {
        pokemonTeamListArrayAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_building);

        // spinner
        pkmn_spinner = (Spinner) findViewById(R.id.pokemonteamlist_spinner);
        pokemonTeamListArrayAdapter = new PokemonTeamListArrayAdapter(getApplicationContext(), pokemonTeamList);
        pkmn_spinner.setAdapter(pokemonTeamListArrayAdapter);

        pkmn_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                PokemonTeam pt = (PokemonTeam) adapterView.getItemAtPosition(i);

                TeamBuildingFragment fragment = TeamBuildingFragment.newInstance(pt);

                FragmentManager fm = getSupportFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.teambuilding_fragmentcontainer, fragment, "")
                        .commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // TODO ?
            }
        });


        //buttons
        ImageButton button_new = (ImageButton) findViewById(R.id.pokemonteamlist_add);
        button_new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PokemonTeam pt = new PokemonTeam();
                pokemonTeamList.add(pt);
                pokemonTeamListArrayAdapter.notifyDataSetChanged();
                pkmn_spinner.setSelection(pokemonTeamList.size() - 1);
            }
        });


        ImageButton button_remove = (ImageButton) findViewById(R.id.pokemonteamlist_remove);
        button_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = pkmn_spinner.getSelectedItemPosition();
                if (position != AdapterView.INVALID_POSITION) {
                    pokemonTeamList.remove(position);
                    pokemonTeamListArrayAdapter.notifyDataSetChanged();

                    getSupportFragmentManager().beginTransaction().
                            remove(getSupportFragmentManager().findFragmentById(R.id.teambuilding_fragmentcontainer)).commit();

                    if (pokemonTeamList.size() > 0) {
                        pkmn_spinner.setSelection(pokemonTeamList.size() - 1);
                    }

                }
            }
        });

        final ImageButton button_settings = (ImageButton) findViewById(R.id.pokemonteamlist_settings);
        button_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(TeamBuildingActivity.this, button_settings);
                popup.getMenuInflater().inflate(R.menu.team_building, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_export_team:
                                //export to clipboard TODO export to other places
                                int position = pkmn_spinner.getSelectedItemPosition();
                                if (position != AdapterView.INVALID_POSITION) {
                                    PokemonTeam pt = pokemonTeamList.get(position);
                                    ClipboardManager clipboard = (ClipboardManager)
                                            getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText(pt.getNickname(), pt.export());
                                    clipboard.setPrimaryClip(clip);
                                }
                                break;

                            case R.id.action_import_team:
                                // TODO
                                break;

                            case R.id.action_rename_team:
                                // TODO (create renaming dialog)
                                break;
                        }
                        return true;
                    }
                });

                popup.show();
            }
        });
    }

    /**
     * Class used to show the team list in the drawer (6 icons + nickname)
     */
    private class PokemonTeamListArrayAdapter extends ArrayAdapter<PokemonTeam> {
        public PokemonTeamListArrayAdapter(Context getContext, List<PokemonTeam> userListData) {
            super(getContext, R.layout.listwidget_teampreview, R.id.team_nickname, userListData);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.listwidget_teampreview, null);
            }
            PokemonTeam p = pokemonTeamList.get(position);

            TextView teamName = (TextView) convertView.findViewById(R.id.team_nickname);
            teamName.setText(p.getNickname());

            LinearLayout layout = (LinearLayout) convertView.findViewById(R.id.pokemon_small_icon_list);
            layout.removeAllViews();

            for (Pokemon pokemon : p.getPokemons()) {
                if (pokemon != null) {
                    ImageView image = new ImageView(getContext());
                    int smallIconId = pokemon.getIconSmall();
                    Drawable d = getResources().getDrawable(smallIconId);
                    image.setImageDrawable(d);
                    layout.addView(image);
                }
            }

            for (int i = 0; i < 6 - p.getPokemons().size(); i++) {
                ImageView image = new ImageView(getContext());
                Drawable d = getResources().getDrawable(R.drawable.smallicons_0);
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
}
