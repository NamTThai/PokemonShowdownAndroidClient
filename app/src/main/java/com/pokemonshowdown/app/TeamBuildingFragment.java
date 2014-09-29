package com.pokemonshowdown.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.pokemonshowdown.data.Pokemon;
import com.pokemonshowdown.data.SearchableActivity;

import java.util.List;

/**
 * This class is a Fragment loaded in the TeamBuildingActivity
 * This enables the user to add/remove/edit pokemons from the team
 */
public class TeamBuildingFragment extends Fragment {
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
        bundle.putSerializable("team", team);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        pokemonListAdapter.notifyDataSetChanged();
        /* Notify parent activity that the pokemonTeam changed (so to reprint in the drawer) */
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pokemonTeam = (PokemonTeam) getArguments().get("team");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teambuilding, parent, false);
        ListView lv = (ListView) view.findViewById(R.id.lisview_pokemonteamlist);

        /**
         * Add Pokemon button
         */
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

        /**
         * pokemon list adapter
         */
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

            Pokemon pokemon = pokemonTeam.getPokemon(position);

            ImageView imagepokemon = (ImageView) convertView.findViewById(R.id.pokemonsmallicon);
            if (pokemon.isShiny()) {
                imagepokemon.setImageDrawable(getResources().getDrawable(pokemon.getIconShiny()));
            } else {
                imagepokemon.setImageDrawable(getResources().getDrawable(pokemon.getIcon()));
            }

            {
                TextView tv = (TextView) convertView.findViewById(R.id.teambuilder_pokemonname);
                tv.setText(pokemon.getName());
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity().getApplicationContext(), SearchableActivity.class);
                        intent.putExtra("Search Type", SearchableActivity.REQUEST_CODE_SEARCH_POKEMON);
                        selectedPos = position;
                        startActivityForResult(intent, SearchableActivity.REQUEST_CODE_SEARCH_POKEMON);
                    }
                });
            }

            {
                TextView tv = (TextView) convertView.findViewById(R.id.teambuilder_pokemonhelditem);
                tv.setText(pokemon.getItem());
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity().getApplicationContext(), SearchableActivity.class);
                        intent.putExtra("Search Type", SearchableActivity.REQUEST_CODE_SEARCH_ITEM);
                        selectedPos = position;

                        startActivityForResult(intent, SearchableActivity.REQUEST_CODE_SEARCH_ITEM);
                    }
                });
            }

            {
                TextView tv = (TextView) convertView.findViewById(R.id.teambuilder_move1);
                tv.setText(pokemon.getMove1());

                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity().getApplicationContext(), SearchableActivity.class);
                        intent.putExtra("Search Type", SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
                        selectedPos = position;
                        selectedMove = 1;
                        startActivityForResult(intent, SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
                    }
                });

            }

            {
                TextView tv = (TextView) convertView.findViewById(R.id.teambuilder_move2);
                tv.setText(pokemon.getMove2());

                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity().getApplicationContext(), SearchableActivity.class);
                        intent.putExtra("Search Type", SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
                        selectedPos = position;
                        selectedMove = 2;
                        startActivityForResult(intent, SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
                    }
                });

            }
            {
                TextView tv = (TextView) convertView.findViewById(R.id.teambuilder_move3);
                tv.setText(pokemon.getMove3());

                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity().getApplicationContext(), SearchableActivity.class);
                        intent.putExtra("Search Type", SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
                        selectedPos = position;
                        selectedMove = 3;
                        startActivityForResult(intent, SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
                    }
                });

            }
            {
                TextView tv = (TextView) convertView.findViewById(R.id.teambuilder_move4);
                tv.setText(pokemon.getMove4());

                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity().getApplicationContext(), SearchableActivity.class);
                        intent.putExtra("Search Type", SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
                        selectedPos = position;
                        selectedMove = 4;
                        startActivityForResult(intent, SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
                    }
                });

            }

            return convertView;
        }
    }
}
