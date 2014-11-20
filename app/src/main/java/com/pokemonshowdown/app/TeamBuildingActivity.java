package com.pokemonshowdown.app;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
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

import com.pokemonshowdown.data.PokemonTeam;

import java.util.List;


public class TeamBuildingActivity extends FragmentActivity {
    public static final String TAG = TeamBuildingActivity.class.getName();
    private Spinner pkmn_spinner;
    private List<PokemonTeam> pokemonTeamList;
    private PokemonTeamListArrayAdapter pokemonTeamListArrayAdapter;

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
        switch (item.getItemId()) {
            case R.id.action_create_team:
                pt = new PokemonTeam();
                pokemonTeamList.add(pt);
                pokemonTeamListArrayAdapter.notifyDataSetChanged();
                pkmn_spinner.setSelection(pokemonTeamList.size() - 1);
                return true;
            case R.id.action_remove_team:
                position = pkmn_spinner.getSelectedItemPosition();
                if (position != AdapterView.INVALID_POSITION) {
                    pokemonTeamList.remove(position);
                    pokemonTeamListArrayAdapter.notifyDataSetChanged();

                    getSupportFragmentManager().beginTransaction().
                            remove(getSupportFragmentManager().findFragmentById(R.id.teambuilding_fragmentcontainer)).commit();

                    if (pokemonTeamList.size() > 0) {
                        pkmn_spinner.setSelection(pokemonTeamList.size() - 1);
                    }
                }
                return true;
            case R.id.action_export_team:
                position = pkmn_spinner.getSelectedItemPosition();
                if (position != AdapterView.INVALID_POSITION) {
                    pt = pokemonTeamList.get(position);
                    ClipboardManager clipboard = (ClipboardManager)
                            getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(pt.getNickname(), pt.exportPokemonTeam());
                    clipboard.setPrimaryClip(clip);
                }
                return true;
            case R.id.action_import_team:
                ClipboardManager clipboard = (ClipboardManager)
                        getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData importClip = clipboard.getPrimaryClip();
                if (importClip != null) {
                    ClipData.Item clipItem = importClip.getItemAt(0);
                    // Gets the clipboard as text.
                    String pasteData = clipItem.getText().toString();
                    pt = PokemonTeam.importPokemonTeam(pasteData, getApplicationContext(), true);
                    if (pt != null) {
                        pokemonTeamList.add(pt);
                        pokemonTeamListArrayAdapter.notifyDataSetChanged();
                        pkmn_spinner.setSelection(pokemonTeamList.size() - 1);
                    } else {
                        //todo handle bad data
                    }
                } else {
                    // todo handle no clipboard
                }
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
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
