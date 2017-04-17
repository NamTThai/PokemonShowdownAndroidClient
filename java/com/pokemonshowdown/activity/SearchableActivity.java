package com.pokemonshowdown.activity;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.pokemonshowdown.R;
import com.pokemonshowdown.application.MyApplication;
import com.pokemonshowdown.data.AbilityDex;
import com.pokemonshowdown.data.ItemDex;
import com.pokemonshowdown.data.Learnset;
import com.pokemonshowdown.data.MoveDex;
import com.pokemonshowdown.data.Pokedex;
import com.pokemonshowdown.data.Pokemon;
import com.pokemonshowdown.data.Tiering;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class SearchableActivity extends BaseActivity {

    public final static String STAG = SearchableActivity.class.getName();
    public final static int REQUEST_CODE_SEARCH_POKEMON = 0;
    public final static int REQUEST_CODE_SEARCH_ABILITY = 1;
    public final static int REQUEST_CODE_SEARCH_ITEM = 2;
    public final static int REQUEST_CODE_SEARCH_MOVES = 3;

    public final static String SEARCH_TYPE = "Search Type";
    public final static String CURRENT_TIER = "CURRENT_TIER";
    public final static String POKEMON_LEARNSET = "POKEMON_LEARNSET";

    public final static String SEARCH = "Search";

    private ArrayAdapter<String> mAdapter;
    private ArrayList<String> mAdapterList;
    private ListView mListView;
    private int mSearchType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setupToolbar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mSearchType = getIntent().getExtras().getInt(SEARCH_TYPE);
        mListView = (ListView) findViewById(R.id.list);

        switch (mSearchType) {
            case REQUEST_CODE_SEARCH_POKEMON:
                String tier = getIntent().getExtras().getString(CURRENT_TIER, null);

                HashMap<String, String> pokedex = Pokedex.get(getApplicationContext()).getPokedexEntries();
                mAdapterList = new ArrayList<>();
                // "" tier is only missingno lol
                if (tier == null || "".equals(tier)) {
                    mAdapterList.addAll(pokedex.keySet());
                    Collections.sort(mAdapterList);
                } else {
                    HashMap<String, ArrayList<String>> tiers = Tiering.get(getApplicationContext()).getTierList();
                    ArrayList<String> currentTier = tiers.get(tier);
                    if (currentTier != null) {
                        Collections.sort(currentTier);
                        mAdapterList.add(tier);
                        mAdapterList.addAll(currentTier);
                        int tierIndex = Tiering.TIER_ORDER.indexOf(tier);
                        for (int i = tierIndex + 1; i < Tiering.TIER_ORDER.size(); i++) {
                            mAdapterList.add(Tiering.TIER_ORDER.get(i));
                            ArrayList<String> nextTier = tiers.get(Tiering.TIER_ORDER.get(i));
                            Collections.sort(nextTier);
                            mAdapterList.addAll(nextTier);
                        }
                    } else {
                        mAdapterList.addAll(pokedex.keySet());
                        Collections.sort(mAdapterList);
                    }
                }
                mAdapter = new PokemonAdapter(this, mAdapterList);
                setListAdapter(mAdapter);
                break;
            case REQUEST_CODE_SEARCH_ABILITY:
                HashMap<String, String> abilityDex = AbilityDex.get(getApplicationContext()).getAbilityDexEntries();
                mAdapterList = new ArrayList<>(abilityDex.keySet());
                Collections.sort(mAdapterList);
                mAdapter = new AbilityAdapter(this, mAdapterList);
                setListAdapter(mAdapter);
                break;
            case REQUEST_CODE_SEARCH_ITEM:
                HashMap<String, String> itemDex = ItemDex.get(getApplicationContext()).getItemDexEntries();
                mAdapterList = new ArrayList<>(itemDex.keySet());
                Collections.sort(mAdapterList);
                mAdapter = new ItemAdapter(this, mAdapterList);
                setListAdapter(mAdapter);
                break;
            case REQUEST_CODE_SEARCH_MOVES:
                String pokemonId = getIntent().getExtras().getString(POKEMON_LEARNSET, null);
                if (pokemonId != null) {
                    mAdapterList = new ArrayList<>();
                    while (pokemonId != null) {
                        ArrayList<String> tempArray = Learnset.get(getApplicationContext()).getLearnetEntry(pokemonId);
                        if (tempArray != null) {
                            for (String move : tempArray) {
                                if (!mAdapterList.contains(move)) {
                                    mAdapterList.add(move);
                                }
                            }
                        }
                        try {
                            pokemonId = Pokedex.get(getApplicationContext()).getPokemonJSONObject(MyApplication.toId(pokemonId)).has("prevo") ?
                                    Pokedex.get(getApplicationContext()).getPokemonJSONObject(MyApplication.toId(pokemonId)).getString("prevo") : null;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    HashMap<String, String> moveDex = MoveDex.get(getApplicationContext()).getMoveDexEntries();
                    mAdapterList = new ArrayList<>(moveDex.keySet());
                }
                Collections.sort(mAdapterList);
                mAdapter = new MovesAdapter(this, mAdapterList);
                setListAdapter(mAdapter);
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = MyApplication.toId(intent.getStringExtra(SearchManager.QUERY));
            switch (mSearchType) {
                case REQUEST_CODE_SEARCH_POKEMON:
                    searchPokemon(query);
                    break;
                case REQUEST_CODE_SEARCH_ABILITY:
                    searchAbility(query);
                    break;
                case REQUEST_CODE_SEARCH_ITEM:
                    searchItem(query);
                    break;
                case REQUEST_CODE_SEARCH_MOVES:
                    searchMove(query);
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        getMenuInflater().inflate(R.menu.search, menu);

        MenuItem myActionMenuItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                switch (mSearchType) {
                    case REQUEST_CODE_SEARCH_POKEMON:
                        searchPokemon(newText);
                        break;
                    case REQUEST_CODE_SEARCH_ABILITY:
                        searchAbility(newText);
                        break;
                    case REQUEST_CODE_SEARCH_ITEM:
                        searchItem(newText);
                        break;
                    case REQUEST_CODE_SEARCH_MOVES:
                        searchMove(newText);
                        break;
                }
                return false;
            }
        });
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
        mAdapterList.clear();
        if (query.isEmpty()) {
            String tier = getIntent().getExtras().getString(CURRENT_TIER, "Uber");
            HashMap<String, ArrayList<String>> tiers = Tiering.get(getApplicationContext()).getTierList();
            ArrayList<String> currentTier = tiers.get(tier);
            if (currentTier != null) {
                Collections.sort(currentTier);
                mAdapterList.add(tier);
                mAdapterList.addAll(currentTier);
                int tierIndex = Tiering.TIER_ORDER.indexOf(tier);
                for (int i = tierIndex + 1; i < Tiering.TIER_ORDER.size(); i++) {
                    mAdapterList.add(Tiering.TIER_ORDER.get(i));
                    ArrayList<String> nextTier = tiers.get(Tiering.TIER_ORDER.get(i));
                    Collections.sort(nextTier);
                    mAdapterList.addAll(nextTier);
                }
            }
            mAdapter = new PokemonAdapter(this, mAdapterList);
            setListAdapter(mAdapter);
        } else {
            HashMap<String, String> pokedex = Pokedex.get(getApplicationContext()).getPokedexEntries();
            mAdapterList = new ArrayList<>();
            for (String pokemonName : pokedex.keySet()) {
                if (pokemonName.contains(query.toLowerCase())) {
                    mAdapterList.add(pokemonName);
                }
            }
            Collections.sort(mAdapterList);
            mAdapter = new PokemonAdapter(this, mAdapterList);
            setListAdapter(mAdapter);
        }
    }

    private void searchAbility(String query) {
        HashMap<String, String> abilityDex = AbilityDex.get(getApplicationContext()).getAbilityDexEntries();
        mAdapterList = new ArrayList<>();
        for (String abilityName : abilityDex.keySet()) {
            if (abilityName.contains(query.toLowerCase())) {
                mAdapterList.add(abilityName);
            }
        }
        mAdapter = new AbilityAdapter(this, mAdapterList);
        setListAdapter(mAdapter);
    }

    private void searchItem(String query) {
        HashMap<String, String> itemDex = ItemDex.get(getApplicationContext()).getItemDexEntries();
        mAdapterList = new ArrayList<>();
        for (String itemName : itemDex.keySet()) {
            if (itemName.contains(query.toLowerCase())) {
                mAdapterList.add(itemName);
            }
        }
        mAdapter = new ItemAdapter(this, mAdapterList);
        setListAdapter(mAdapter);
    }

    private void searchMove(String query) {
        HashMap<String, String> moveDex = MoveDex.get(getApplicationContext()).getMoveDexEntries();
        mAdapterList = new ArrayList<>();
        for (String moveName : moveDex.keySet()) {
            if (moveName.contains(query.toLowerCase())) {
                mAdapterList.add(moveName);
            }
        }
        mAdapter = new MovesAdapter(this, mAdapterList);
        setListAdapter(mAdapter);
    }

    private void setListAdapter(ArrayAdapter<String> adapter) {
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                switch (mSearchType) {
                    case REQUEST_CODE_SEARCH_POKEMON:
                        intent.putExtra(SEARCH, mAdapterList.get(i));
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                        break;
                    case REQUEST_CODE_SEARCH_ABILITY:
                        intent.putExtra(SEARCH, mAdapterList.get(i));
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                        break;
                    case REQUEST_CODE_SEARCH_ITEM:
                        intent.putExtra(SEARCH, mAdapterList.get(i));
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                        break;
                    case REQUEST_CODE_SEARCH_MOVES:
                        intent.putExtra(SEARCH, mAdapterList.get(i));
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                        break;
                }
            }
        });
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

            for (String s : Tiering.TIER_ORDER) {
                if (pokemonName.equals(s)) {
                    convertView = mContext.getLayoutInflater().inflate(R.layout.fragment_simple_list_row, null);
                    convertView.setBackgroundColor(Color.parseColor("#33B5E5"));
                    TextView text = (TextView) convertView.findViewById(R.id.text);
                    text.setText(s);
                    text.setTextColor(getResources().getColor(R.color.dark_blue));
                    return convertView;
                }
            }

            //if (convertView == null || convertView.findViewById(R.id.text) != null) {
            if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                convertView = mContext.getLayoutInflater().inflate(R.layout.fragment_pokemon_short_vertical, null);
            } else {
                String[] abilities = Pokemon.getPokemonAbilities(getApplicationContext(), getItem(position));
                if (abilities.length > 1) {
                    convertView = mContext.getLayoutInflater().inflate(R.layout.fragment_pokemon_short_horizontal, null);
                } else {
                    convertView = mContext.getLayoutInflater().inflate(R.layout.fragment_pokemon_short_horizontal_single, null);
                }
            }
            //}

            ImageView icon = (ImageView) convertView.findViewById(R.id.pokemon_icon);
            icon.setImageResource(Pokemon.getPokemonIcon(getApplicationContext(), pokemonName));
            TextView textView = (TextView) convertView.findViewById(R.id.short_pokemon_name);
            textView.setText(Pokemon.getPokemonName(getApplicationContext(), pokemonName));
            Integer[] typesIcon = Pokemon.getPokemonTypeIcon(getApplicationContext(), pokemonName);
            ImageView type1 = (ImageView) convertView.findViewById(R.id.type_1);
            type1.setImageResource(typesIcon[0]);
            ImageView type2 = (ImageView) convertView.findViewById(R.id.type_2);
            if (typesIcon.length == 2) {
                type2.setImageResource(typesIcon[1]);
            } else {
                type2.setImageResource(0);
            }

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                String[] abilities = Pokemon.getPokemonAbilities(getApplicationContext(), pokemonName);

                if (abilities.length > 1) {
                    Log.e(STAG, pokemonName);
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

            Integer[] baseStats = Pokemon.getPokemonBaseStats(getApplicationContext(), pokemonName);
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

    private class AbilityAdapter extends ArrayAdapter<String> {
        private Activity mContext;

        public AbilityAdapter(Activity getContext, ArrayList<String> pokemonList) {
            super(getContext, 0, pokemonList);
            mContext = getContext;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mContext.getLayoutInflater().inflate(R.layout.fragment_ability_list_short, null);
            }

            try {
                String abilityName = getItem(position);

                JSONObject abilityJson = AbilityDex.get(getApplicationContext()).getAbilityJsonObject(abilityName);
                TextView textView = (TextView) convertView.findViewById(R.id.short_ability_name);
                textView.setText(abilityJson.getString("name"));
                textView.setCompoundDrawablesWithIntrinsicBounds(Pokedex.getUnownIcon(getApplicationContext(), abilityName), 0, 0, 0);
                ((TextView) convertView.findViewById(R.id.short_ability_description)).setText(abilityJson.getString("shortDesc"));
            } catch (JSONException e) {
                Log.d(STAG, e.toString());
            }
            return convertView;
        }
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

                JSONObject moveJson = MoveDex.get(getApplicationContext()).getMoveJsonObject(move);
                TextView textView = (TextView) convertView.findViewById(R.id.short_move_name);
                textView.setText(moveJson.getString("name"));
                ImageView type = (ImageView) convertView.findViewById(R.id.type);
                type.setImageResource(MoveDex.getTypeIcon(getApplicationContext(), moveJson.getString("type")));
                ImageView category = (ImageView) convertView.findViewById(R.id.category);
                category.setImageResource(MoveDex.getCategoryIcon(getApplicationContext(), moveJson.getString("category")));

                TextView power = (TextView) convertView.findViewById(R.id.move_power);
                String pow = moveJson.getString("basePower");
                if (pow.equals("0")) {
                    power.setText("--");
                } else {
                    power.setText(pow);
                }
                TextView acc = (TextView) convertView.findViewById(R.id.move_acc);
                String accuracy = moveJson.getString("accuracy");
                if (accuracy.equals("true")) {
                    accuracy = "--";
                }
                acc.setText(accuracy);
                TextView pp = (TextView) convertView.findViewById(R.id.move_pp);
                pp.setText(MoveDex.getMaxPP(moveJson.getString("pp")));
            } catch (JSONException e) {
                Log.d(STAG, e.toString());
            }
            return convertView;
        }
    }

    private class ItemAdapter extends ArrayAdapter<String> {
        private Activity mContext;

        public ItemAdapter(Activity getContext, ArrayList<String> pokemonList) {
            super(getContext, 0, pokemonList);
            mContext = getContext;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mContext.getLayoutInflater().inflate(R.layout.fragment_item_list_row, null);
            }

            try {
                String itemTag = getItem(position);
                JSONObject itemJson = ItemDex.get(getApplicationContext()).getItemJsonObject(itemTag);
                ImageView icon = (ImageView) convertView.findViewById(R.id.short_item_icon);
                icon.setImageResource(ItemDex.getItemIcon(getApplicationContext(), itemTag));
                TextView textView = (TextView) convertView.findViewById(R.id.short_item_name);
                textView.setText(itemJson.getString("name"));
                ((TextView) convertView.findViewById(R.id.short_item_description)).setText(itemJson.getString("desc"));
            } catch (JSONException e) {
                Log.d(STAG, e.toString());
            }
            return convertView;
        }

    }
}
