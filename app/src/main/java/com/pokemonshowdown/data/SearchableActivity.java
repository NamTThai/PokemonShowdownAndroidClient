package com.pokemonshowdown.data;

import android.app.Activity;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.pokemonshowdown.app.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by thain on 8/10/14.
 */
public class SearchableActivity extends ListActivity {
    public final static String SearchTAG = "SEARCH_DIALOG_POKEMON";
    public final static int REQUEST_CODE_SEARCH_POKEMON = 0;

    private ArrayAdapter<String> mAdapter;
    private ArrayList<String> mAdapterList;
    private int mSearchType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getActionBar().setTitle(R.string.search_title);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        mSearchType = getIntent().getExtras().getInt("Search Type");

        switch (mSearchType) {
            case REQUEST_CODE_SEARCH_POKEMON:
                HashMap<String, String> pokedex = Pokedex.getWithApplicationContext(getApplicationContext()).getPokedexEntries();
                mAdapterList = new ArrayList<>(pokedex.keySet());
                mAdapter = new PokemonAdapter(this, mAdapterList);
                setListAdapter(mAdapter);
                getActionBar().setTitle(R.string.search_label_pokemon);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent();
        intent.putExtra("Search", mAdapterList.get(position));
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            switch (mSearchType) {
                case REQUEST_CODE_SEARCH_POKEMON:
                    searchPokemon(query);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(Activity.RESULT_CANCELED);
                finish();
                return true;
            case R.id.menu_search:
                onSearchRequested();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void searchPokemon(String query) {
        HashMap<String, String> pokedex = Pokedex.getWithApplicationContext(getApplicationContext()).getPokedexEntries();
        mAdapterList = new ArrayList<String>();
        for (String pokemonName : pokedex.keySet()) {
            if (pokemonName.contains(query.toLowerCase())) {
                mAdapterList.add(pokemonName);
            }
        }
        mAdapter = new PokemonAdapter(this, mAdapterList);
        setListAdapter(mAdapter);
    }

    private class PokemonAdapter extends ArrayAdapter<String> {
        private Activity mContext;

        public PokemonAdapter(Activity getContext, ArrayList<String> pokemonList) {
            super(getContext, 0, pokemonList);
            mContext = getContext;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mContext.getLayoutInflater().inflate(R.layout.fragment_pokemon_short, null);
            }

            String pokemonName = getItem(position);
            TextView textView = (TextView) convertView.findViewById(R.id.short_pokemon_name);
            textView.setText(Pokemon.getPokemonName(getApplicationContext(), pokemonName, true));
            textView.setCompoundDrawablesWithIntrinsicBounds(Pokemon.getPokemonIconSmall(getApplicationContext(), pokemonName, true), 0, 0, 0);
            Integer[] typesIcon = Pokemon.getPokemonTypeIcon(getApplicationContext(), pokemonName, true);
            ImageView type1 = (ImageView) convertView.findViewById(R.id.type_1);
            type1.setImageResource(typesIcon[0]);
            ImageView type2 = (ImageView) convertView.findViewById(R.id.type_2);
            if (typesIcon.length == 2) {
                type2.setImageResource(typesIcon[1]);
            } else {
                type2.setImageResource(0);
            }
            Integer[] baseStats = Pokemon.getPokemonBaseStats(getApplicationContext(), pokemonName, true);
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
