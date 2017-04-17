package com.pokemonshowdown.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.pokemonshowdown.R;
import com.pokemonshowdown.data.AbilityDex;
import com.pokemonshowdown.data.ItemDex;
import com.pokemonshowdown.data.MoveDex;
import com.pokemonshowdown.data.Pokemon;
import com.pokemonshowdown.dialog.AbilityInfoDialog;
import com.pokemonshowdown.dialog.ItemInfoDialog;
import com.pokemonshowdown.dialog.MoveInfoDialog;
import com.pokemonshowdown.dialog.PokemonInfoDialog;

import org.json.JSONException;
import org.json.JSONObject;

public class PokedexActivity extends BaseActivity {
    public final static String PTAG = PokedexActivity.class.getName();
    public final static int REQUEST_CODE_SEARCH_POKEMON = 0;
    public final static int REQUEST_CODE_SEARCH_ABILITY = 1;
    public final static int REQUEST_CODE_SEARCH_ITEM = 2;
    public final static int REQUEST_CODE_SEARCH_MOVES = 3;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == Activity.RESULT_OK) {
                switch (requestCode) {
                    case REQUEST_CODE_SEARCH_POKEMON:
                        Pokemon pokemon = new Pokemon(this, data.getExtras().getString(SearchableActivity.SEARCH));
                        PokemonInfoDialog.newInstance(pokemon).show(getSupportFragmentManager(), PTAG);
                        break;
                    case REQUEST_CODE_SEARCH_ABILITY:
                        String ability = data.getExtras().getString(SearchableActivity.SEARCH);
                        JSONObject abilityJson = AbilityDex.get(getApplicationContext()).getAbilityJsonObject(ability);

                        String desc = "";
                        try {
                            desc = abilityJson.getString("desc");
                        } catch (JSONException ex) {
                            desc = abilityJson.getString("shortDesc");
                        }

                        AbilityInfoDialog.newInstance(abilityJson.getString("name"), desc)
                                .show(getSupportFragmentManager(), PTAG);
                        break;
                    case REQUEST_CODE_SEARCH_ITEM:
                        String item = data.getExtras().getString(SearchableActivity.SEARCH);
                        JSONObject itemJson = ItemDex.get(getApplicationContext()).getItemJsonObject(item);
                        ItemInfoDialog.newInstance(itemJson.getString("name"), itemJson.getString("desc"))
                                .show(getSupportFragmentManager(), PTAG);
                        break;
                    case REQUEST_CODE_SEARCH_MOVES:
                        String move = data.getExtras().getString(SearchableActivity.SEARCH);
                        JSONObject moveJson = MoveDex.get(getApplicationContext()).getMoveJsonObject(move);
                        MoveInfoDialog.newInstance(moveJson.getString("name"))
                                .show(getSupportFragmentManager(), PTAG);
                        break;
                }
            }
        } catch (JSONException e) {
            Log.d(PTAG, e.toString());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokedex);
        setupToolbar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.pokedex).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PokedexActivity.this, SearchableActivity.class);
                intent.putExtra("Search Type", SearchableActivity.REQUEST_CODE_SEARCH_POKEMON);
                PokedexActivity.this.startActivityForResult(intent, REQUEST_CODE_SEARCH_POKEMON);
            }
        });

        findViewById(R.id.abilitydex).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PokedexActivity.this, SearchableActivity.class);
                intent.putExtra("Search Type", SearchableActivity.REQUEST_CODE_SEARCH_ABILITY);
                PokedexActivity.this.startActivityForResult(intent, REQUEST_CODE_SEARCH_ABILITY);
            }
        });

        findViewById(R.id.itemdex).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PokedexActivity.this, SearchableActivity.class);
                intent.putExtra("Search Type", SearchableActivity.REQUEST_CODE_SEARCH_ITEM);
                PokedexActivity.this.startActivityForResult(intent, REQUEST_CODE_SEARCH_ITEM);
            }
        });

        findViewById(R.id.movedex).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PokedexActivity.this, SearchableActivity.class);
                intent.putExtra("Search Type", SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
                PokedexActivity.this.startActivityForResult(intent, REQUEST_CODE_SEARCH_MOVES);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.clear();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(this) != null)
                    NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
