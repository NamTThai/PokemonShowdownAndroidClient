package com.pokemonshowdown.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.pokemonshowdown.data.MyApplication;
import com.pokemonshowdown.data.Pokemon;
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
                    ClipData clip = ClipData.newPlainText(pt.getNickname(), pt.export());
                    clipboard.setPrimaryClip(clip);
                }
                return true;
            case R.id.action_import_team:
                //todo
                return true;
            case R.id.action_rename_team:
                // todo renaming dialog
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


}
