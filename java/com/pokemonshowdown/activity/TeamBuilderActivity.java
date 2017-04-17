package com.pokemonshowdown.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.pokemonshowdown.R;
import com.pokemonshowdown.adapter.PokemonTeamsAdapter;
import com.pokemonshowdown.adapter.SimpleStringAdapter;
import com.pokemonshowdown.application.MyApplication;
import com.pokemonshowdown.data.BattleFieldData;
import com.pokemonshowdown.data.PokemonTeam;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class TeamBuilderActivity extends BaseActivity implements View.OnClickListener {

    private final static String TAG = TeamBuilderActivity.class.getName();
    private final static int CLIPBOARD = 0;
    private final static int PASTEBIN = 1;
    private final static int QR = 2;
    public static PokemonTeamAccessor ACCESSOR;
    private List<PokemonTeam> mPokemonTeamList;
    private RecyclerView mRecyclerTeams;
    private UpdateRecyclerView UPDATE;
    private FloatingActionMenu searchMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ACCESSOR = new PokemonTeamAccessor();
        UPDATE = new UpdateRecyclerView();
        setContentView(R.layout.fragment_team_builder);
        setupToolbar();

        searchMenu = (FloatingActionMenu) findViewById(R.id.search_menu);
        findViewById(R.id.by_name).setOnClickListener(this);
        findViewById(R.id.by_tier).setOnClickListener(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        setupRecyclerTeams();
    }

    @Override
    public void onClick(View v) {
        searchMenu.close(true);
        switch (v.getId()) {
            case R.id.by_name:
                Collections.sort(mPokemonTeamList, new Comparator<PokemonTeam>() {
                    @Override
                    public int compare(PokemonTeam t1, PokemonTeam t2) {
                        return t1.getNickname().compareTo(t2.getNickname());
                    }
                });
                mRecyclerTeams.setAdapter(new PokemonTeamsAdapter(getContext(), mPokemonTeamList));
                Toast.makeText(this, "Teams organized alphabetically by name.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.by_tier:
                Collections.sort(mPokemonTeamList, new Comparator<PokemonTeam>() {
                    @Override
                    public int compare(PokemonTeam t1, PokemonTeam t2) {
                        return t1.getTier().compareTo(t2.getTier());
                    }
                });
                mRecyclerTeams.setAdapter(new PokemonTeamsAdapter(getContext(), mPokemonTeamList));
                Toast.makeText(this, "Teams organized alphabetically by tiers.", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.team_building, menu);


        menu.findItem(R.id.action_export_team).setVisible(false);
        menu.findItem(R.id.action_share_team).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int position;
        PokemonTeam pt;
        final PokemonTeam pt2;
        AlertDialog.Builder builder;
        AlertDialog alert;

        switch (item.getItemId()) {
            case R.id.action_create_team:
                pt = new PokemonTeam();
                pt.setNickname("Team nº" + (mPokemonTeamList.size() + 1));
                pt.setTier(BattleFieldData.get(getContext()).getFormatTypes().get(0).getFormatList().get(2).getName());
                saveTeam(pt, mPokemonTeamList.size());
                mRecyclerTeams.setAdapter(new PokemonTeamsAdapter(getContext(), mPokemonTeamList));
                Toast.makeText(getApplicationContext(), R.string.team_created, Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_import_team:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.import_title);
                builder.setItems(getResources().getStringArray(R.array.export_import_sources),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                if (item == CLIPBOARD) {
                                    ClipboardManager clipboard = (ClipboardManager)
                                            getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData importClip = clipboard.getPrimaryClip();
                                    if (importClip != null) {
                                        ClipData.Item clipItem = importClip.getItemAt(0);
                                        // Gets the clipboard as text.
                                        String pasteData = clipItem.getText().toString();
                                        PokemonTeam pt = PokemonTeam.importPokemonTeam(pasteData, getApplicationContext(), true);
                                        if (pt != null && pt.getTeamSize() > 0) {
                                            pt.setTier(BattleFieldData.get(getContext()).getFormatTypes().get(0).getFormatList().get(2).getName());
                                            pt.setNickname("Imported Team");
                                            mPokemonTeamList.add(pt);
                                            mRecyclerTeams.setAdapter(new PokemonTeamsAdapter(getContext(), mPokemonTeamList));
                                            Toast.makeText(getApplicationContext(), R.string.team_imported, Toast.LENGTH_SHORT).show();
                                            saveOrUpdateTeam(pt);
                                        } else {
                                            Toast.makeText(getApplicationContext(), R.string.team_imported_invalid_data, Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(getApplicationContext(), R.string.team_imported_empty, Toast.LENGTH_SHORT).show();
                                    }
                                } else if (item == PASTEBIN) {
                                    final AlertDialog.Builder urlDialog = new AlertDialog.Builder(TeamBuilderActivity.this);
                                    urlDialog.setTitle(R.string.url_dialog_title);
                                    final EditText urlEditText = new EditText(TeamBuilderActivity.this);
                                    urlEditText.setText(R.string.pastebin_url);
                                    urlEditText.setSelection(urlEditText.getText().length());
                                    urlDialog.setView(urlEditText);

                                    urlDialog.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            new PastebinTask(PastebinTaskId.IMPORT).execute(urlEditText.getText().toString());
                                        }
                                    });

                                    urlDialog.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            arg0.dismiss();
                                        }
                                    });

                                    urlDialog.show();
                                } else if (item == QR) {
                                    IntentIntegrator integrator = new IntentIntegrator(TeamBuilderActivity.this);
                                    integrator.initiateScan();
                                }
                            }
                        }

                );
                alert = builder.create();
                alert.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupRecyclerTeams() {
        mPokemonTeamList = PokemonTeam.getPokemonTeamList(getContext());

        mRecyclerTeams = (RecyclerView) findViewById(R.id.recycler_teams);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerTeams.setLayoutManager(layoutManager);
        if (mPokemonTeamList.size() <= 0) {
            List<String> list = new ArrayList<>();
            list.add("There are no teams here...");
            mRecyclerTeams.setAdapter(new SimpleStringAdapter(list));
        } else {
            mRecyclerTeams.setAdapter(new PokemonTeamsAdapter(getContext(), mPokemonTeamList));
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        PokemonTeam.savePokemonTeams(MyApplication.getMyApplication(), mPokemonTeamList);
        setupRecyclerTeams();
    }

    @Override
    protected void onDestroy() {
        PokemonTeam.savePokemonTeams(MyApplication.getMyApplication(), mPokemonTeamList);
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            //  here weh ave the url read from the barcode
            String url = scanResult.getContents();
            if (url == null) {
                return;
            }
            new PastebinTask(PastebinTaskId.IMPORT).execute(url);
        } else {
            //passing to the fragment below (for searchable pokemons/moves ...  etc etc)
            super.onActivityResult(requestCode, resultCode, data);

        }
    }

    private void saveOrUpdateTeam(PokemonTeam team) {
        PokemonTeam temp = new PokemonTeam();
        for (PokemonTeam t : mPokemonTeamList) {
            if (t.getNickname().equals(team.getNickname())) {
                temp = t;
                break;
            }
        }
        mPokemonTeamList.remove(temp);
        mPokemonTeamList.add(team);
        mRecyclerTeams.setAdapter(new PokemonTeamsAdapter(getContext(), mPokemonTeamList));
        PokemonTeam.savePokemonTeams(MyApplication.getMyApplication(), mPokemonTeamList);
    }

    private void saveTeam(PokemonTeam team, int position) {
        mPokemonTeamList.add(position, team);
        mRecyclerTeams.setAdapter(new PokemonTeamsAdapter(getContext(), mPokemonTeamList));
        PokemonTeam.savePokemonTeams(MyApplication.getMyApplication(), mPokemonTeamList);
    }

    private int deleteTeam(PokemonTeam team) {
        int i = 0;
        for (PokemonTeam t : mPokemonTeamList) {
            if (t.getNickname().equals(team.getNickname())) {
                break;
            }
            i++;
        }
        mPokemonTeamList.remove(team);
        mRecyclerTeams.setAdapter(new PokemonTeamsAdapter(getContext(), mPokemonTeamList));
        PokemonTeam.savePokemonTeams(MyApplication.getMyApplication(), mPokemonTeamList);
        UPDATE.setupView();
        return i;
    }

    private enum PastebinTaskId {
        IMPORT
    }

    private class UpdateRecyclerView {

        public void setupView() {
            setupRecyclerTeams();
        }
    }

    public class PokemonTeamAccessor {

        public void savePokemonTeam(PokemonTeam team, int position) {
            saveTeam(team, position);
        }

        public int deletePokemonTeam(PokemonTeam team) {
            return deleteTeam(team);
        }

        public void saveOrUpdatePokemonTeam(PokemonTeam team) {
            saveOrUpdateTeam(team);
        }
    }

    private class PastebinTask extends AsyncTask<String, Void, String> {
        private final static String PASTEBIN_RAW = "http://pastebin.com/raw.php?i=";
        private final static String ENCODING = "UTF-8";

        private PastebinTaskId mTask;
        // TODO use that exception one day
        private Exception mException;
        private boolean success;
        private ProgressDialog waitingDialog;
        private String message;

        public PastebinTask(PastebinTaskId task) {
            mTask = task;
            switch (mTask) {
                case IMPORT:
                    message = getResources().getString(R.string.import_inprogress);
                    break;
            }
        }

        protected String doInBackground(String... strings) {
            String data = strings[0];
            String out = null;
            switch (mTask) {
                case IMPORT:
                    importFromPastebin(data);
                    break;
            }

            return out;
        }

        @Override
        protected void onPreExecute() {
            waitingDialog = new ProgressDialog(TeamBuilderActivity.this);
            waitingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            waitingDialog.setCancelable(false);
            waitingDialog.setMessage(message);
            waitingDialog.show();
        }

        @Override
        protected void onPostExecute(String aString) {
            if (success) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TeamBuilderActivity.this);
                waitingDialog.dismiss();
                switch (mTask) {
                    case IMPORT:
                        builder.setTitle(R.string.import_success_dialog_title);
                        break;
                }
                builder.setIcon(android.R.drawable.ic_dialog_info);
                builder.setPositiveButton(R.string.dialog_ok, null);
                final AlertDialog alert = builder.create();
                TeamBuilderActivity.this.runOnUiThread(new java.lang.Runnable() {
                    public void run() {
                        alert.show();
                    }
                });
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(TeamBuilderActivity.this);
                builder.setTitle(R.string.error_dialog_title);
                builder.setIcon(android.R.drawable.ic_dialog_alert);
                switch (mTask) {
                    case IMPORT:
                        builder.setMessage(R.string.import_error_dialog_message);
                        break;
                }
                builder.setPositiveButton(R.string.dialog_ok, null);
                final AlertDialog alert = builder.create();
                TeamBuilderActivity.this.runOnUiThread(new java.lang.Runnable() {
                    public void run() {
                        waitingDialog.dismiss();
                        alert.show();
                    }
                });
            }
        }

        private void importFromPastebin(String url) {
            if (url.startsWith("http://")) {
                url = url.substring("http://".length());
            }
            if (!url.startsWith("pastebin.com/")) {
                success = false;
                return;
            }
            url = url.trim();
            String[] split = url.split("/");
            String pastebinId = split[1];

            String finalUrl = PASTEBIN_RAW + pastebinId;
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(finalUrl);
            HttpResponse response = null;
            String pastebinOut = null;
            try {
                response = httpclient.execute(httpget);
                HttpEntity entity = response.getEntity();
                pastebinOut = EntityUtils.toString(entity, ENCODING);
            } catch (IOException e) {
                mException = e;
                success = false;
                return;
            }

            if (pastebinOut == null || pastebinOut.isEmpty() || pastebinOut.contains("<!DOCTYPE HTML>")) {
                success = false;
                return;
            }

            // Not all teams imported from pastebin has their names and tiers, so he account for both
            PokemonTeam pokemonTeam = null;
            if (pastebinOut.contains("===")) {
                pokemonTeam = PokemonTeam.importPokemonTeamFromPastebin(pastebinOut, TeamBuilderActivity.this);
            } else {
                pokemonTeam = PokemonTeam.importPokemonTeam(pastebinOut, TeamBuilderActivity.this, false);
            }

            if (pokemonTeam != null && pokemonTeam.getTeamSize() > 0) {
                if (pokemonTeam.getNickname() == null || pokemonTeam.getNickname().isEmpty()) {
                    pokemonTeam.setNickname("Imported Team");
                }
                mPokemonTeamList.add(pokemonTeam);
                PokemonTeam.savePokemonTeams(TeamBuilderActivity.this, mPokemonTeamList);
                success = true;
                return;
            } else {
                success = false;
                return;
            }
        }
    }
}
