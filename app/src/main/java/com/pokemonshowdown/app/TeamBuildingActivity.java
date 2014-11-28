package com.pokemonshowdown.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.pokemonshowdown.data.PokemonTeam;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


public class TeamBuildingActivity extends FragmentActivity {
    private final static String TAG = TeamBuildingActivity.class.getName();
    private Spinner pkmn_spinner;

    private List<PokemonTeam> pokemonTeamList;
    private PokemonTeamListArrayAdapter pokemonTeamListArrayAdapter;
    private final static int CLIPBOARD = 0;
    private final static int PASTEBIN = 1;
    private final static int QR = 2;
    private ProgressDialog waitingDialog;

    public void updateList() {
        pokemonTeamListArrayAdapter.notifyDataSetChanged();
        TeamBuildingFragment teamBuildingFragment = (TeamBuildingFragment) getSupportFragmentManager().findFragmentById(R.id.teambuilding_fragmentcontainer);
        if (teamBuildingFragment != null) {
            teamBuildingFragment.updateList();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_building);

        PokemonTeam.loadPokemonTeams(getApplicationContext());
        pokemonTeamList = PokemonTeam.getPokemonTeamList();

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

                Spinner tier_spinner = (Spinner) findViewById(R.id.tier_spinner);
                tier_spinner.setSelection(((ArrayAdapter) tier_spinner.getAdapter()).getPosition(pt.getTier()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // TODO ?
            }
        });

        Spinner tier_spinner = (Spinner) findViewById(R.id.tier_spinner);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.tier_list));
        tier_spinner.setAdapter(adapter);

        tier_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String tier = (String) adapterView.getItemAtPosition(i);
                PokemonTeam pt = (PokemonTeam) pkmn_spinner.getSelectedItem();
                if (pt != null) {
                    pt.setTier(tier);
                    pokemonTeamListArrayAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();
        PokemonTeam.savePokemonTeams(getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.team_building, menu);
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
                pokemonTeamList.add(pt);
                pt.setNickname("Team #" + pokemonTeamList.size());
                pokemonTeamListArrayAdapter.notifyDataSetChanged();
                pkmn_spinner.setSelection(pokemonTeamList.size() - 1);
                Toast.makeText(getApplicationContext(), R.string.team_created, Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_remove_team:
                position = pkmn_spinner.getSelectedItemPosition();
                if (position != AdapterView.INVALID_POSITION) {
                    pokemonTeamList.remove(position);
                    pokemonTeamListArrayAdapter.notifyDataSetChanged();

                    if (pokemonTeamList.size() > 0) {
                        pkmn_spinner.setSelection(0, false);
                        PokemonTeam pt3 = pokemonTeamList.get(0);
                        TeamBuildingFragment fragment = TeamBuildingFragment.newInstance(pt3);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.teambuilding_fragmentcontainer, fragment, "")
                                .commit();
                    } else {
                        getSupportFragmentManager().beginTransaction().
                                remove(getSupportFragmentManager().findFragmentById(R.id.teambuilding_fragmentcontainer)).commit();
                    }
                    Toast.makeText(getApplicationContext(), R.string.team_removed, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.team_removed_none, Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_export_team:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.export_title);
                builder.setItems(getResources().getStringArray(R.array.export_import_sources), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        if (item == CLIPBOARD) {
                            int position = pkmn_spinner.getSelectedItemPosition();
                            if (position != AdapterView.INVALID_POSITION) {
                                PokemonTeam pt = pokemonTeamList.get(position);
                                ClipboardManager clipboard = (ClipboardManager)
                                        getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText(pt.getNickname(), pt.exportPokemonTeam(getApplicationContext()));
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(getApplicationContext(), R.string.team_exported, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.team_exported_none, Toast.LENGTH_SHORT).show();
                            }
                        } else if (item == PASTEBIN) {
                            int position = pkmn_spinner.getSelectedItemPosition();
                            if (position != AdapterView.INVALID_POSITION) {
                                PokemonTeam pt = pokemonTeamList.get(position);
                                String exportData = pt.exportPokemonTeam(getApplicationContext());
                                new PastebinPasteTask().execute(exportData);
                            }
                        } else {
                            new AlertDialog.Builder(TeamBuildingActivity.this)
                                    .setMessage(R.string.still_in_development)
                                    .create().show();
                        }
                    }
                });
                alert = builder.create();
                alert.show();
                return true;

            case R.id.action_import_team:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.import_title);
                builder.setItems(getResources().getStringArray(R.array.export_import_sources), new DialogInterface.OnClickListener() {
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
                                    pokemonTeamList.add(pt);
                                    pt.setNickname("Imported Team");
                                    pokemonTeamListArrayAdapter.notifyDataSetChanged();
                                    pkmn_spinner.setSelection(pokemonTeamList.size() - 1);
                                    Toast.makeText(getApplicationContext(), R.string.team_imported, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), R.string.team_imported_invalid_data, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.team_imported_empty, Toast.LENGTH_SHORT).show();
                            }
                        } else if (item == PASTEBIN) {
                            final AlertDialog.Builder urlDialog = new AlertDialog.Builder(TeamBuildingActivity.this);
                            urlDialog.setTitle(R.string.url_dialog_title);
                            final EditText urlEditText = new EditText(TeamBuildingActivity.this);
                            urlEditText.setText(R.string.pastebin_url);
                            urlEditText.setSelection(urlEditText.getText().length());
                            urlDialog.setView(urlEditText);

                            urlDialog.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface arg0, int arg1) {
                                    new PastebinCopyTask().execute(urlEditText.getText().toString());
                                }
                            });

                            urlDialog.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface arg0, int arg1) {
                                    arg0.dismiss();
                                }
                            });

                            urlDialog.show();
                        } else {
                            new AlertDialog.Builder(TeamBuildingActivity.this)
                                    .setMessage(R.string.still_in_development)
                                    .create().show();
                        }
                    }
                });
                alert = builder.create();
                alert.show();
                return true;

            case R.id.action_rename_team:
                position = pkmn_spinner.getSelectedItemPosition();
                if (position != AdapterView.INVALID_POSITION) {
                    pt2 = pokemonTeamList.get(position);
                    AlertDialog.Builder renameDialog = new AlertDialog.Builder(TeamBuildingActivity.this);
                    renameDialog.setTitle(R.string.rename_pokemon);
                    final EditText teamNameEditText = new EditText(TeamBuildingActivity.this);
                    teamNameEditText.setText(pt2.getNickname());
                    renameDialog.setView(teamNameEditText);

                    renameDialog.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            pt2.setNickname(teamNameEditText.getText().toString());
                            pokemonTeamListArrayAdapter.notifyDataSetChanged();
                            Toast.makeText(getApplicationContext(), R.string.team_renamed, Toast.LENGTH_SHORT).show();
                            arg0.dismiss();
                        }
                    });

                    renameDialog.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            arg0.dismiss();
                        }
                    });

                    renameDialog.show();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class PastebinCopyTask extends AsyncTask<String, Void, PokemonTeam> {
        private final static String PASTEBIN_RAW = "http://pastebin.com/raw.php?i=";
        private final static String ENCODING = "UTF-8";

        private Exception mException;

        @Override
        protected PokemonTeam doInBackground(String... strings) {
            TeamBuildingActivity.this.runOnUiThread(new java.lang.Runnable() {
                public void run() {
                    waitingDialog = new ProgressDialog(TeamBuildingActivity.this);
                    waitingDialog.setMessage(getResources().getString(R.string.import_inprogress));
                    waitingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    waitingDialog.setCancelable(false);
                    waitingDialog.show();
                }
            });

            String url = strings[0];
            if (url.startsWith("http://")) {
                url = url.substring("http://".length());
            }
            if (!url.startsWith("pastebin.com/")) {
                return null;
            }
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
                return null;
            }

            PokemonTeam pokemonTeam = PokemonTeam.importPokemonTeam(pastebinOut, TeamBuildingActivity.this, false);
            return pokemonTeam;
        }

        @Override
        protected void onPostExecute(PokemonTeam pokemonTeam) {
            waitingDialog.dismiss();
            if (pokemonTeam == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TeamBuildingActivity.this);
                builder.setTitle(R.string.error_dialog_title);
                builder.setIcon(android.R.drawable.ic_dialog_alert);
                builder.setMessage(R.string.import_error_dialog_message);
                builder.setPositiveButton(R.string.dialog_ok, null);
                final AlertDialog alert = builder.create();
                TeamBuildingActivity.this.runOnUiThread(new java.lang.Runnable() {
                    public void run() {
                        alert.show();
                    }
                });
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(TeamBuildingActivity.this);
                builder.setTitle(R.string.import_success_dialog_title);
                builder.setIcon(android.R.drawable.ic_dialog_info);
                builder.setPositiveButton(R.string.dialog_ok, null);
                final AlertDialog alert = builder.create();
                TeamBuildingActivity.this.runOnUiThread(new java.lang.Runnable() {
                    public void run() {
                        alert.show();
                    }
                });
                pokemonTeam.setNickname("Imported Team");
                pokemonTeamList.add(pokemonTeam);
                pokemonTeamListArrayAdapter.notifyDataSetChanged();
                pkmn_spinner.setSelection(pokemonTeamList.size() - 1);
            }
        }
    }

    private class PastebinPasteTask extends AsyncTask<String, Void, String> {
        private final static String PASTEBIN_API = "http://pastebin.com/api/api_post.php";
        private final static String API_DEV_KEY_KEY = "api_dev_key";
        private final static String API_DEV_KEY_VALUE = "027d7160b253fbcae3d91ff407ea82a6";
        private final static String API_OPTION_KEY = "api_option";
        private final static String API_OPTION_VALUE = "paste";
        private final static String PASTE_DATA = "api_paste_code";
        private final static String ENCODING = "UTF-8";

        private Exception mException;

        @Override
        protected String doInBackground(String... strings) {
            TeamBuildingActivity.this.runOnUiThread(new java.lang.Runnable() {
                public void run() {
                    waitingDialog = new ProgressDialog(TeamBuildingActivity.this);
                    waitingDialog.setMessage(getResources().getString(R.string.export_inprogress));
                    waitingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    waitingDialog.setCancelable(false);
                    waitingDialog.show();
                }
            });
            String pastebinOut = null;
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(PASTEBIN_API);
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair(API_DEV_KEY_KEY, API_DEV_KEY_VALUE));
                nameValuePairs.add(new BasicNameValuePair(API_OPTION_KEY, API_OPTION_VALUE));
                nameValuePairs.add(new BasicNameValuePair(PASTE_DATA, strings[0]));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                pastebinOut = EntityUtils.toString(entity, ENCODING);
                return pastebinOut;
            } catch (ClientProtocolException e) {
                mException = e;
            } catch (UnsupportedEncodingException e) {
                mException = e;
            } catch (IOException e) {
                mException = e;
            } finally {
                return pastebinOut;
            }
        }

        protected void onPostExecute(String returnData) {
            waitingDialog.dismiss();
            if (mException != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TeamBuildingActivity.this);
                builder.setTitle(R.string.error_dialog_title);
                builder.setIcon(android.R.drawable.ic_dialog_alert);
                builder.setMessage(R.string.export_error_dialog_message);
                builder.setPositiveButton(R.string.dialog_ok, null);
                final AlertDialog alert = builder.create();
                TeamBuildingActivity.this.runOnUiThread(new java.lang.Runnable() {
                    public void run() {
                        alert.show();
                    }
                });
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(TeamBuildingActivity.this);
                builder.setTitle(R.string.export_success_dialog_title);
                builder.setIcon(android.R.drawable.ic_dialog_info);
                builder.setMessage(returnData);
                builder.setPositiveButton(R.string.dialog_ok, null);
                final AlertDialog alert = builder.create();
                TeamBuildingActivity.this.runOnUiThread(new java.lang.Runnable() {
                    public void run() {
                        alert.show();
                    }
                });
            }
        }


    }
}
