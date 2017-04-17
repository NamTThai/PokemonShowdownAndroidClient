package com.pokemonshowdown.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.pokemonshowdown.R;
import com.pokemonshowdown.adapter.PokemonAdapter;
import com.pokemonshowdown.adapter.SimpleStringAdapter;
import com.pokemonshowdown.data.ItemDex;
import com.pokemonshowdown.data.Pokemon;
import com.pokemonshowdown.data.PokemonTeam;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by McBeengs on 29/10/2016.
 */

public class TeamBuildingActivity extends BaseActivity implements View.OnClickListener {

    private final static int CLIPBOARD = 0;
    private final static int PASTEBIN = 1;
    private final static int QR = 2;
    public static TeamBuildingAccessor ACCESSOR;
    private PokemonTeam mTeam;
    private RecyclerView mPokemonsRecycler;
    private FloatingActionButton addFab;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_building);
        ACCESSOR = new TeamBuildingAccessor();
        setupToolbar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        String jsonMyObject = "";
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            jsonMyObject = extras.getString("team");
        }

        if (!jsonMyObject.isEmpty()) {
            mTeam = new Gson().fromJson(jsonMyObject, PokemonTeam.class);
        } else {
            mTeam = new PokemonTeam();
        }

        mPokemonsRecycler = (RecyclerView) findViewById(R.id.pokemons_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mPokemonsRecycler.setLayoutManager(layoutManager);
        if (mTeam.getPokemons().size() <= 0) {
            List<String> list = new ArrayList<>();
            list.add("There are no mons here...");
            mPokemonsRecycler.setAdapter(new SimpleStringAdapter(list));
        } else {
            mPokemonsRecycler.setAdapter(new PokemonAdapter(getContext(), mTeam.getPokemons()));
        }

        addFab = (FloatingActionButton) findViewById(R.id.add_fab);
        addFab.setOnClickListener(this);
        if (mTeam.isFull()) {
            addFab.setVisibility(View.GONE);
        } else {
            addFab.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.team_building, menu);

        menu.findItem(R.id.action_create_team).setVisible(false);
        menu.findItem(R.id.action_import_team).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_fab:
                Intent intent = new Intent(getApplicationContext(), SearchableActivity.class);
                intent.putExtra(SearchableActivity.SEARCH_TYPE, SearchableActivity.REQUEST_CODE_SEARCH_POKEMON);
                intent.putExtra(SearchableActivity.CURRENT_TIER, mTeam.getTier());
                startActivityForResult(intent, SearchableActivity.REQUEST_CODE_SEARCH_POKEMON);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SearchableActivity.REQUEST_CODE_SEARCH_POKEMON) {
                Pokemon pokemon = new Pokemon(getContext(), data.getExtras().getString("Search"));

                //Check if a tier separator was clicked
                if (pokemon.getName() == null) {
                    return;
                }

                // We need check if it's mega. If is, add mega stone
                if (pokemon.getName().contains("Mega") && !pokemon.getName().equals("Yanmega") && !pokemon.getName().equals("Meganium")) {
                    HashMap<String, String> itemDex = ItemDex.get(getApplicationContext()).getItemDexEntries();
                    for (String key : itemDex.keySet()) {
                        try {
                            JSONObject obj = ItemDex.get(getApplicationContext()).getItemJsonObject(key);
                            String item = obj.getString("name");

                            if (item.contains("ite") && !item.equals("Eviolite") && !item.contains("White")) {
                                // Check for Charizard / Mewtwo exclusive mega-evos
                                if (pokemon.getName().contains("Charizard")) {
                                    if (pokemon.getName().contains("X") && item.equals("Charizardite X")) {
                                        pokemon.setItem(item);
                                        break;
                                    } else if (pokemon.getName().contains("Y") && item.equals("Charizardite Y")) {
                                        pokemon.setItem(item);
                                        break;
                                    }
                                } else if (pokemon.getName().contains("Mewtwo")) {
                                    if (pokemon.getName().contains("X") && item.equals("Mewtwonite X")) {
                                        pokemon.setItem(item);
                                        break;
                                    } else if (pokemon.getName().contains("Y") && item.equals("Mewtwonite Y")) {
                                        pokemon.setItem(item);
                                        break;
                                    }
                                } else {
                                    if (item.contains(pokemon.getName().substring(0, 5))) {
                                        pokemon.setItem(item);
                                        break;
                                    }
                                }
                            }
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                    }
                }

                //Check item-exclusive pokemon. If true, add the item
                if (pokemon.getName().contains("Arceus-")) {
                    String type = pokemon.getName().substring(7, pokemon.getName().length());
                    switch (type) {
                        case "Bug":
                            pokemon.setItem("insectplate");
                            break;
                        case "Dark":
                            pokemon.setItem("dreadplate");
                            break;
                        case "Dragon":
                            pokemon.setItem("dracoplate");
                            break;
                        case "Electric":
                            pokemon.setItem("zapplate");
                            break;
                        case "Fairy":
                            pokemon.setItem("pixieplate");
                            break;
                        case "Fighting":
                            pokemon.setItem("fistplate");
                            break;
                        case "Fire":
                            pokemon.setItem("flameplate");
                            break;
                        case "Flying":
                            pokemon.setItem("skyplate");
                            break;
                        case "Ghost":
                            pokemon.setItem("spookyplate");
                            break;
                        case "Grass":
                            pokemon.setItem("meadowplate");
                            break;
                        case "Ground":
                            pokemon.setItem("earthplate");
                            break;
                        case "Ice":
                            pokemon.setItem("icicleplate");
                            break;
                        case "Poison":
                            pokemon.setItem("toxicplate");
                            break;
                        case "Psychic":
                            pokemon.setItem("mindplate");
                            break;
                        case "Rock":
                            pokemon.setItem("stoneplate");
                            break;
                        case "Steel":
                            pokemon.setItem("ironplate");
                            break;
                        case "Water":
                            pokemon.setItem("splashplate");
                            break;
                    }
                }

                if (pokemon.getName().contains("Genesect-")) {
                    String type = pokemon.getName().substring(9, pokemon.getName().length());
                    switch (type) {
                        case "Burn":
                            pokemon.setItem("burndrive");
                            break;
                        case "Chill":
                            pokemon.setItem("chilldrive");
                            break;
                        case "Douse":
                            pokemon.setItem("dousedrive");
                            break;
                        case "Shock":
                            pokemon.setItem("shockdrive");
                            break;
                    }
                }

                if (pokemon.getName().contains("Giratina-Origin")) {
                    pokemon.setItem("griseousorb");
                }

                if (pokemon.getName().contains("Groudon-Primal")) {
                    pokemon.setItem("redorb");
                }

                if (pokemon.getName().contains("Kyogre-Primal")) {
                    pokemon.setItem("blueorb");
                }

                if (pokemon.getName().contains("Silvally-")) {
                    String type = pokemon.getName().substring(9, pokemon.getName().length());
                    switch (type) {
                        case "Bug":
                            pokemon.setItem("bugmemory");
                            break;
                        case "Dark":
                            pokemon.setItem("darkmemory");
                            break;
                        case "Dragon":
                            pokemon.setItem("dragonmemory");
                            break;
                        case "Electric":
                            pokemon.setItem("electricmemory");
                            break;
                        case "Fairy":
                            pokemon.setItem("fairymemory");
                            break;
                        case "Fighting":
                            pokemon.setItem("fightingmemory");
                            break;
                        case "Fire":
                            pokemon.setItem("firememory");
                            break;
                        case "Flying":
                            pokemon.setItem("flyingmemory");
                            break;
                        case "Ghost":
                            pokemon.setItem("ghostmemory");
                            break;
                        case "Grass":
                            pokemon.setItem("grassmemory");
                            break;
                        case "Ground":
                            pokemon.setItem("groundmemory");
                            break;
                        case "Ice":
                            pokemon.setItem("icememory");
                            break;
                        case "Poison":
                            pokemon.setItem("poisonmemory");
                            break;
                        case "Psychic":
                            pokemon.setItem("psychicmemory");
                            break;
                        case "Rock":
                            pokemon.setItem("rockmemory");
                            break;
                        case "Steel":
                            pokemon.setItem("steelmemory");
                            break;
                        case "Water":
                            pokemon.setItem("watermemory");
                            break;
                    }
                }

                mTeam.addPokemon(pokemon);
                mPokemonsRecycler.setAdapter(new PokemonAdapter(getContext(), mTeam.getPokemons()));

                if (mTeam.isFull()) {
                    addFab.setVisibility(View.GONE);
                } else {
                    addFab.setVisibility(View.VISIBLE);
                }

                TeamBuilderActivity.ACCESSOR.saveOrUpdatePokemonTeam(mTeam);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int position;
        PokemonTeam pt;
        final PokemonTeam pt2;
        AlertDialog.Builder builder;
        AlertDialog alert;

        switch (item.getItemId()) {
            case R.id.action_export_team:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.export_title);
                builder.setItems(getResources().getStringArray(R.array.export_import_sources),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                if (item == CLIPBOARD) {
                                    ClipboardManager clipboard = (ClipboardManager)
                                            getSystemService(CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText(mTeam.getNickname(), mTeam.exportPokemonTeam(getApplicationContext()));
                                    clipboard.setPrimaryClip(clip);
                                    Toast.makeText(getApplicationContext(), R.string.team_exported, Toast.LENGTH_SHORT).show();
                                    Toast.makeText(getApplicationContext(), R.string.team_exported_none, Toast.LENGTH_SHORT).show();
                                } else if (item == PASTEBIN) {
                                    String exportData = mTeam.exportPokemonTeam(getApplicationContext());
                                    new PastebinTask(PastebinTaskId.EXPORT).execute(exportData);
                                } else if (item == QR) {
                                    String exportData = mTeam.exportPokemonTeam(getApplicationContext());
                                    PastebinTask pastebinTask = new PastebinTask(PastebinTaskId.EXPORT_FOR_QR);
                                    pastebinTask.execute(exportData);
                                }
                            }
                        }

                );
                alert = builder.create();
                alert.show();
                return true;
            case R.id.action_share_team:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, mTeam.exportPokemonTeam(getApplicationContext()));
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                return true;
            default:
                return false;
        }
    }

    private void fireSwapping(Pokemon pokemon, int position) {
        mTeam.removePokemon(position);
        mTeam.addPokemon(pokemon, position);
        mPokemonsRecycler.setAdapter(new PokemonAdapter(mPokemonsRecycler.getContext(), mTeam.getPokemons()));
        TeamBuilderActivity.ACCESSOR.saveOrUpdatePokemonTeam(mTeam);
    }

    private void fireButtonVisibility() {
        if (mTeam.isFull()) {
            addFab.setVisibility(View.GONE);
        } else {
            addFab.setVisibility(View.VISIBLE);
        }
    }

    private void fireTeamSaving(int position) {
        mTeam.removePokemon(position);
        mPokemonsRecycler.setAdapter(new PokemonAdapter(mPokemonsRecycler.getContext(), mTeam.getPokemons()));
        TeamBuilderActivity.ACCESSOR.saveOrUpdatePokemonTeam(mTeam);
    }

    private enum PastebinTaskId {
        EXPORT, EXPORT_FOR_QR // for QR
    }

    public class TeamBuildingAccessor {

        public void firePokemonSwapping(Pokemon pokemon, int position) {
            fireSwapping(pokemon, position);
        }

        public void fireAddButtonVisibility() {
            fireButtonVisibility();
        }

        public void firePokemonTeamSaving(int position) {
            fireTeamSaving(position);
        }
    }

    private class PastebinTask extends AsyncTask<String, Void, String> {
        private final static String PASTEBIN_API = "http://pastebin.com/api/api_post.php";
        private final static String API_DEV_KEY_KEY = "api_dev_key";
        private final static String API_DEV_KEY_VALUE = "027d7160b253fbcae3d91ff407ea82a6";
        private final static String API_OPTION_KEY = "api_option";
        private final static String API_OPTION_VALUE = "paste";
        private final static String PASTE_DATA = "api_paste_code";
        private final static String ENCODING = "UTF-8";

        private TeamBuildingActivity.PastebinTaskId mTask;
        // TODO use that exception one day
        private Exception mException;
        private boolean success;
        private ProgressDialog waitingDialog;

        public PastebinTask(TeamBuildingActivity.PastebinTaskId task) {
            mTask = task;
            waitingDialog = new ProgressDialog(TeamBuildingActivity.this);
            waitingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            waitingDialog.setCancelable(false);
            switch (mTask) {
                case EXPORT_FOR_QR:
                    waitingDialog.setMessage(getResources().getString(R.string.exportQR_inprogress));
                    break;

                case EXPORT:
                    waitingDialog.setMessage(getResources().getString(R.string.export_inprogress));
                    break;
            }
        }

        protected String doInBackground(String... strings) {
            String data = strings[0];
            String out = null;
            switch (mTask) {
                case EXPORT_FOR_QR:
                case EXPORT:
                    out = exportToPastebin(data);
                    break;
            }
            return out;
        }

        @Override
        protected void onPreExecute() {
            TeamBuildingActivity.this.runOnUiThread(new java.lang.Runnable() {
                public void run() {
                    waitingDialog.show();
                }
            });
        }

        @Override
        protected void onPostExecute(String aString) {
            if (success) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(TeamBuildingActivity.this);
                TeamBuildingActivity.this.runOnUiThread(new java.lang.Runnable() {
                    public void run() {
                        waitingDialog.dismiss();
                    }
                });
                switch (mTask) {
                    case EXPORT:
                        TextView link = new TextView(TeamBuildingActivity.this);
                        FrameLayout container = new FrameLayout(TeamBuildingActivity.this);
                        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        params.topMargin = 50;
                        link.setTextSize(18f);
                        link.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        link.setLayoutParams(params);
                        link.setText(aString);
                        container.addView(link);
                        link.setTextIsSelectable(true);
                        builder.setView(container);
                        builder.setTitle(R.string.export_success_dialog_title);
                        break;
                    case EXPORT_FOR_QR:
                        IntentIntegrator integrator = new IntentIntegrator(TeamBuildingActivity.this);
                        integrator.shareText(aString);
                        return;
                }
                builder.setIcon(android.R.drawable.ic_dialog_info);
                builder.setPositiveButton(R.string.dialog_ok, null);
                final android.app.AlertDialog alert = builder.create();
                TeamBuildingActivity.this.runOnUiThread(new java.lang.Runnable() {
                    public void run() {
                        alert.show();
                    }
                });
            } else {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(TeamBuildingActivity.this);
                builder.setTitle(R.string.error_dialog_title);
                builder.setIcon(android.R.drawable.ic_dialog_alert);
                switch (mTask) {
                    case EXPORT_FOR_QR:
                    case EXPORT:
                        if (aString == null) {
                            builder.setMessage(R.string.export_error_dialog_message);
                        } else {
                            builder.setMessage(aString);
                        }
                        break;
                }
                builder.setPositiveButton(R.string.dialog_ok, null);
                final android.app.AlertDialog alert = builder.create();
                TeamBuildingActivity.this.runOnUiThread(new java.lang.Runnable() {
                    public void run() {
                        waitingDialog.dismiss();
                        alert.show();
                    }
                });
            }
        }

        private String exportToPastebin(String data) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(PASTEBIN_API);
            String outputURL;
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair(API_DEV_KEY_KEY, API_DEV_KEY_VALUE));
                nameValuePairs.add(new BasicNameValuePair(API_OPTION_KEY, API_OPTION_VALUE));
                nameValuePairs.add(new BasicNameValuePair(PASTE_DATA, data));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                outputURL = EntityUtils.toString(entity, ENCODING);
                //export error (post limit reached)
                success = outputURL.startsWith("http://pastebin.com/");
            } catch (IOException e) {
                outputURL = null;
                mException = e;
                success = false;
            }
            return outputURL;
        }
    }
}
