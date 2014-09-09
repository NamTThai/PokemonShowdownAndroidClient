package com.pokemonshowdown.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.pokemonshowdown.data.AbilityDex;
import com.pokemonshowdown.data.ItemDex;
import com.pokemonshowdown.data.MoveDex;
import com.pokemonshowdown.data.Pokemon;
import com.pokemonshowdown.data.SearchableActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class PokedexActivity extends FragmentActivity {
    public final static String PTAG = PokedexActivity.class.getName();
    public final static int REQUEST_CODE_SEARCH_POKEMON = 0;
    public final static int REQUEST_CODE_SEARCH_ABILITY = 1;
    public final static int REQUEST_CODE_SEARCH_ITEM = 2;
    public final static int REQUEST_CODE_SEARCH_MOVES = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokedex);
        getActionBar().setTitle(R.string.bar_pokedex);
        getActionBar().setDisplayHomeAsUpEnabled(true);

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == Activity.RESULT_OK) {
                DialogFragment fragment;
                Dialog dialog;
                switch (requestCode) {
                    case REQUEST_CODE_SEARCH_POKEMON:
                        Pokemon pokemon = new Pokemon(getApplicationContext(), data.getExtras().getString("Search"), true);
                        fragment = new PokemonFragment();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("Pokemon", pokemon);
                        bundle.putBoolean("Search", true);
                        bundle.putInt("Search Code", REQUEST_CODE_SEARCH_POKEMON);
                        fragment.setArguments(bundle);
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        fragment.show(fragmentManager, PokemonFragment.PTAG);
                        break;
                    case REQUEST_CODE_SEARCH_ABILITY:
                        String ability = data.getExtras().getString("Search");
                        JSONObject abilityJson = AbilityDex.getWithApplicationContext(getApplicationContext()).getAbilityJsonObject(ability);
                        dialog = new AlertDialog.Builder(this)
                                .setTitle(abilityJson.getString("name"))
                                .setMessage(abilityJson.getString("desc"))
                                .create();
                        dialog.show();
                        break;
                    case REQUEST_CODE_SEARCH_ITEM:
                        String item = data.getExtras().getString("Search");
                        JSONObject itemJson = ItemDex.getWithApplicationContext(getApplicationContext()).getItemJsonObject(item);
                        dialog = new AlertDialog.Builder(this)
                                .setTitle(itemJson.getString("name"))
                                .setMessage(itemJson.getString("desc"))
                                .setIcon(ItemDex.getItemIcon(getApplicationContext(), item))
                                .create();
                        dialog.show();
                        break;
                    case REQUEST_CODE_SEARCH_MOVES:
                        String move = data.getExtras().getString("Search");
                        JSONObject moveJson = MoveDex.getWithApplicationContext(getApplicationContext()).getMoveJsonObject(move);
                        dialog = new AlertDialog.Builder(this)
                                .setTitle(moveJson.getString("name"))
                                .setMessage(moveJson.getString("desc"))
                                .setIcon(MoveDex.getMoveType(getApplicationContext(), moveJson.getString("type")))
                                .create();
                        dialog.show();
                        break;
                }
            }
        } catch (JSONException e) {
            Log.d(PTAG, e.toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.clear();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                if (NavUtils.getParentActivityName(this) != null)
                    NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
