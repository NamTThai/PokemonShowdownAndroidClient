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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


public class TeamBuildingActivity extends FragmentActivity {
    public static final String TAG = TeamBuildingActivity.class.getName();
    private Spinner pkmn_spinner;
    private List<PokemonTeam> pokemonTeamList;
    private PokemonTeamListArrayAdapter pokemonTeamListArrayAdapter;
    private final static int CLIPBOARD = 0;
    private final static int PASTEBIN = 1;
    private final static int QR = 2;
    private ProgressDialog waitingDialog;

    public void updateList() {
        pokemonTeamListArrayAdapter.notifyDataSetChanged();
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
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // TODO ?
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
        final String[] items = {"Clipboard", "Pastebin", "QR Code"};

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
                builder.setItems(items, new DialogInterface.OnClickListener() {
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
                                waitingDialog = new ProgressDialog(TeamBuildingActivity.this);
                                waitingDialog.setMessage("Exporting to pastebin");
                                waitingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                waitingDialog.setCancelable(false);

                                new PastebinPasteTask().execute(exportData);

                                TeamBuildingActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        waitingDialog.show();
                                    }
                                });

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
                builder.setItems(items, new DialogInterface.OnClickListener() {
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
                    renameDialog.setTitle("Rename");
                    final EditText teamNameEditText = new EditText(TeamBuildingActivity.this);
                    teamNameEditText.setText(pt2.getNickname());
                    renameDialog.setView(teamNameEditText);

                    renameDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            pt2.setNickname(teamNameEditText.getText().toString());
                            pokemonTeamListArrayAdapter.notifyDataSetChanged();
                            arg0.dismiss();
                        }
                    });

                    renameDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            arg0.dismiss();
                        }
                    });

                    renameDialog.show();
                    Toast.makeText(getApplicationContext(), R.string.team_renamed, Toast.LENGTH_SHORT).show();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    class PastebinPasteTask extends AsyncTask<String, Void, String> {
        private Exception exception;

        @Override
        protected String doInBackground(String... strings) {
            String pastebinOut = null;
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://pastebin.com/api/api_post.php");
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("api_dev_key", "027d7160b253fbcae3d91ff407ea82a6"));
                nameValuePairs.add(new BasicNameValuePair("api_option", "paste"));
                nameValuePairs.add(new BasicNameValuePair("api_paste_code", strings[0]));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                pastebinOut = EntityUtils.toString(entity, "UTF-8");
                return pastebinOut;
            } catch (ClientProtocolException e) {
                exception = e;
            } catch (UnsupportedEncodingException e) {
                exception = e;
            } catch (IOException e) {
                exception = e;
            } finally {
                return pastebinOut;
            }
        }

        protected void onPostExecute(String returnData) {
            waitingDialog.dismiss();
            if (exception != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TeamBuildingActivity.this);
                builder.setTitle("Error");
                builder.setIcon(android.R.drawable.ic_dialog_alert);
                builder.setMessage("Exporting data failed!");
                builder.setPositiveButton("Ok", null);
                final AlertDialog alert = builder.create();
                TeamBuildingActivity.this.runOnUiThread(new java.lang.Runnable() {
                    public void run() {
                        alert.show();
                    }
                });
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(TeamBuildingActivity.this);
                builder.setTitle("Success");
                builder.setIcon(android.R.drawable.ic_dialog_info);
                builder.setMessage(returnData);
                builder.setPositiveButton("Ok", null);
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
