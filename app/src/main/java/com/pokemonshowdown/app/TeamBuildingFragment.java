package com.pokemonshowdown.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pokemonshowdown.data.ItemDex;
import com.pokemonshowdown.data.MoveDex;
import com.pokemonshowdown.data.Pokemon;
import com.pokemonshowdown.data.PokemonTeam;
import com.pokemonshowdown.data.SearchableActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * This class is a Fragment loaded in the TeamBuildingActivity
 * This enables the user to add/remove/edit pokemons from the team
 */
public class TeamBuildingFragment extends Fragment {
    public static final String TAG = TeamBuildingFragment.class.getName();
    public final static String TEAMTAG = "team";
    private PokemonTeam pokemonTeam;
    private PokemonListAdapter pokemonListAdapter;
    private Button footerButton;

    /* Used for onactivityresult */
    private int selectedPos;
    private int selectedMove;

    public TeamBuildingFragment() {
        super();
    }

    public static final TeamBuildingFragment newInstance(PokemonTeam team) {
        TeamBuildingFragment fragment = new TeamBuildingFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(TEAMTAG, team);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        pokemonListAdapter.notifyDataSetChanged();
        // Notify parent activity that the pokemonTeam changed (so to reprint in the drawer)
        ((TeamBuildingActivity) getActivity()).updateList();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pokemonTeam = (PokemonTeam) getArguments().get(TEAMTAG);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teambuilding, parent, false);
        ListView lv = (ListView) view.findViewById(R.id.lisview_pokemonteamlist);

        // add pokemon button
        footerButton = new Button(getActivity().getApplicationContext());
        footerButton.setText("Add Pokemon");
        footerButton.setTextColor(Color.BLACK);
        footerButton.setBackgroundColor(Color.WHITE);
        footerButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_new, 0, 0, 0);
        footerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pokemonTeam.isFull()) {
                    return;
                } else {
                    selectedPos = -1;
                    Intent intent = new Intent(getActivity().getApplicationContext(), SearchableActivity.class);
                    intent.putExtra("Search Type", SearchableActivity.REQUEST_CODE_SEARCH_POKEMON);
                    startActivityForResult(intent, SearchableActivity.REQUEST_CODE_SEARCH_POKEMON);
                }
            }
        });
        lv.addFooterView(footerButton);

        registerForContextMenu(lv);

        // pokemon list adapter
        pokemonListAdapter = new PokemonListAdapter(getActivity().getApplicationContext(), pokemonTeam.getPokemons());
        lv.setAdapter(pokemonListAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Pokemon pkmn = pokemonTeam.getPokemon(position);
                selectedPos = position;
                PokemonFragment fragment = new PokemonFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("Pokemon", pkmn);
                bundle.putBoolean("Search", false);
                fragment.setArguments(bundle);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragment.show(fragmentManager, PokemonFragment.PTAG);
            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                return false;
            }
        });

        return view;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(Menu.NONE, 1, Menu.NONE, "Remove");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {
            case 1:
                pokemonTeam.removePokemon(info.position);
                pokemonListAdapter.notifyDataSetChanged();
                // Notify parent activity that the pokemonTeam changed (so to reprint in the drawer)
                ((TeamBuildingActivity) getActivity()).updateList();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setFocusableInTouchMode(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SearchableActivity.REQUEST_CODE_SEARCH_POKEMON) {
                Pokemon pokemon = new Pokemon(getActivity().getApplicationContext(), data.getExtras().getString("Search"), true);
                if (selectedPos != -1) {
                    pokemonTeam.replacePokemon(selectedPos, pokemon);
                } else {
                    pokemonTeam.addPokemon(pokemon);
                }

                if (pokemonTeam.isFull()) {
                    footerButton.setVisibility(View.GONE);
                } else {
                    footerButton.setVisibility(View.VISIBLE);
                }

                pokemonListAdapter.notifyDataSetChanged();
                ((TeamBuildingActivity) getActivity()).updateList();
            } else if (requestCode == SearchableActivity.REQUEST_CODE_SEARCH_ITEM) {
                String item = data.getExtras().getString("Search");

                Pokemon pkmn = pokemonTeam.getPokemon(selectedPos);
                pkmn.setItem(item);

                pokemonListAdapter.notifyDataSetChanged();
            } else if (requestCode == SearchableActivity.REQUEST_CODE_SEARCH_MOVES) {
                String move = data.getExtras().getString("Search");

                Pokemon pkmn = pokemonTeam.getPokemon(selectedPos);
                switch (selectedMove) {
                    case 1:
                        pkmn.setMove1(move);
                        break;
                    case 2:
                        pkmn.setMove2(move);
                        break;
                    case 3:
                        pkmn.setMove3(move);
                        break;
                    case 4:
                        pkmn.setMove4(move);
                        break;
                    default:
                        break;
                }
                pokemonListAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * Custom adapter for showing pokemons in a list including
     * moves, item...
     */
    private class PokemonListAdapter extends ArrayAdapter<Pokemon> {
        public PokemonListAdapter(Context getContext, List<Pokemon> userListData) {
            super(getContext, 0, userListData);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.listwidget_detailledpokemon, null);
            }

            final Pokemon pokemon = pokemonTeam.getPokemon(position);


            TextView teambuilder_pokemonNickName = (TextView) convertView.findViewById(R.id.teambuilder_pokemonNickName);
            teambuilder_pokemonNickName.setText(pokemon.getNickName());
            teambuilder_pokemonNickName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder renameDialog = new AlertDialog.Builder(TeamBuildingFragment.this.getActivity());
                    renameDialog.setTitle("Rename");
                    final EditText teamNameEditText = new EditText(TeamBuildingFragment.this.getActivity());
                    teamNameEditText.setText(pokemon.getNickName());
                    renameDialog.setView(teamNameEditText);

                    renameDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            pokemon.setNickName(teamNameEditText.getText().toString());
                            notifyDataSetChanged();
                            arg0.dismiss();
                        }
                    });

                    renameDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            arg0.dismiss();
                        }
                    });

                    renameDialog.show();
                }
            });

            ImageView teambuilder_pokemonIcon = (ImageView) convertView.findViewById(R.id.teambuilder_pokemonIcon);
            if (pokemon.isShiny()) {
                teambuilder_pokemonIcon.setImageDrawable(getResources().getDrawable(pokemon.getIconShiny()));
            } else {
                teambuilder_pokemonIcon.setImageDrawable(getResources().getDrawable(pokemon.getIcon()));
            }


            TextView teambuilder_itemName = (TextView) convertView.findViewById(R.id.teambuilder_item);
            if (pokemon.getItem().isEmpty()) {
                teambuilder_itemName.setText(R.string.pokemon_nohelditem);
                teambuilder_itemName.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            } else {
                teambuilder_itemName.setText(pokemon.getItem());
                int itemDrawable = ItemDex.getItemIcon(TeamBuildingFragment.this.getActivity(), pokemon.getItem());
                if (itemDrawable != 0) {
                    teambuilder_itemName.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(itemDrawable), null, null, null);
                }
            }
            teambuilder_itemName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity().getApplicationContext(), SearchableActivity.class);
                    intent.putExtra("Search Type", SearchableActivity.REQUEST_CODE_SEARCH_ITEM);
                    selectedPos = position;
                    startActivityForResult(intent, SearchableActivity.REQUEST_CODE_SEARCH_ITEM);
                }
            });


            TextView textView_move1_name = (TextView) convertView.findViewById(R.id.move1_name);
            textView_move1_name.setText(pokemon.getMove1());

            TextView textView_move2_name = (TextView) convertView.findViewById(R.id.move2_name);
            textView_move2_name.setText(pokemon.getMove2());

            TextView textView_move3_name = (TextView) convertView.findViewById(R.id.move3_name);
            textView_move3_name.setText(pokemon.getMove3());

            TextView textView_move4_name = (TextView) convertView.findViewById(R.id.move4_name);
            textView_move4_name.setText(pokemon.getMove4());

            if (!pokemon.getMove1().equals("--")) {
                ImageView move1_type = (ImageView) convertView.findViewById(R.id.move1_type);
                move1_type.setImageResource(MoveDex.getMoveTypeIcon(getActivity(), pokemon.getMove1(), false));
                JSONObject ppObject = MoveDex.get(getActivity()).getMoveJsonObject(pokemon.getMove1());
                if (ppObject != null) {
                    TextView move1_pp = (TextView) convertView.findViewById(R.id.move1_pp);
                    try {
                        move1_pp.setText(ppObject.getInt("pp") + "/" + ppObject.getInt("pp"));
                    } catch (JSONException e) {
                        Log.e(TAG, "", e);
                    }
                }
            }

            if (!pokemon.getMove2().equals("--")) {
                ImageView move2_type = (ImageView) convertView.findViewById(R.id.move2_type);
                move2_type.setImageResource(MoveDex.getMoveTypeIcon(getActivity(), pokemon.getMove2(), false));
                JSONObject ppObject = MoveDex.get(getActivity()).getMoveJsonObject(pokemon.getMove2());
                if (ppObject != null) {
                    TextView move2_pp = (TextView) convertView.findViewById(R.id.move2_pp);
                    try {
                        move2_pp.setText(ppObject.getInt("pp") + "/" + ppObject.getInt("pp"));
                    } catch (JSONException e) {
                        Log.e(TAG, "", e);
                    }
                }
            }


            if (!pokemon.getMove3().equals("--")) {
                ImageView move3_type = (ImageView) convertView.findViewById(R.id.move3_type);
                move3_type.setImageResource(MoveDex.getMoveTypeIcon(getActivity(), pokemon.getMove3(), false));
                JSONObject ppObject = MoveDex.get(getActivity()).getMoveJsonObject(pokemon.getMove3());
                if (ppObject != null) {
                    TextView move3_pp = (TextView) convertView.findViewById(R.id.move3_pp);
                    try {
                        move3_pp.setText(ppObject.getInt("pp") + "/" + ppObject.getInt("pp"));
                    } catch (JSONException e) {
                        Log.e(TAG, "", e);
                    }
                }
            }

            if (!pokemon.getMove4().equals("--")) {
                ImageView move4_type = (ImageView) convertView.findViewById(R.id.move4_type);
                move4_type.setImageResource(MoveDex.getMoveTypeIcon(getActivity(), pokemon.getMove4(), false));
                JSONObject ppObject = MoveDex.get(getActivity()).getMoveJsonObject(pokemon.getMove4());
                if (ppObject != null) {
                    TextView move4_pp = (TextView) convertView.findViewById(R.id.move4_pp);
                    try {
                        move4_pp.setText(ppObject.getInt("pp") + "/" + ppObject.getInt("pp"));
                    } catch (JSONException e) {
                        Log.e(TAG, "", e);
                    }
                }
            }


            RelativeLayout move1 = (RelativeLayout) convertView.findViewById(R.id.move1);
            move1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity().getApplicationContext(), SearchableActivity.class);
                    intent.putExtra("Search Type", SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
                    selectedPos = position;
                    selectedMove = 1;
                    startActivityForResult(intent, SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
                }
            });

            RelativeLayout move2 = (RelativeLayout) convertView.findViewById(R.id.move2);
            move2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity().getApplicationContext(), SearchableActivity.class);
                    intent.putExtra("Search Type", SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
                    selectedPos = position;
                    selectedMove = 2;
                    startActivityForResult(intent, SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
                }
            });


            RelativeLayout move3 = (RelativeLayout) convertView.findViewById(R.id.move3);
            move3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity().getApplicationContext(), SearchableActivity.class);
                    intent.putExtra("Search Type", SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
                    selectedPos = position;
                    selectedMove = 3;
                    startActivityForResult(intent, SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
                }
            });


            RelativeLayout move4 = (RelativeLayout) convertView.findViewById(R.id.move4);
            move4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity().getApplicationContext(), SearchableActivity.class);
                    intent.putExtra("Search Type", SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
                    selectedPos = position;
                    selectedMove = 4;
                    startActivityForResult(intent, SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
                }
            });

            return convertView;
        }
    }
}
