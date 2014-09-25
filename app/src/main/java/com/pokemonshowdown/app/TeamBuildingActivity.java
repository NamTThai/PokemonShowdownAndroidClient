package com.pokemonshowdown.app;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.pokemonshowdown.data.Pokemon;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


public class TeamBuildingActivity extends FragmentActivity {
    private ListView mDrawerTeamList;
    private ArrayList<PokemonTeam> pokemonTeamList = null;
    private TeamListAdapter teamPreviewListAdapter;

    /**
     * Current selected team
     */
    private int selectedPos = -1;

    /**
     * Disk storage name
     */
    private String FILENAME = "pkmnStorage.dat";

    public void updateList() {
        teamPreviewListAdapter.notifyDataSetChanged();
    }


    /**
     * Saving roster to file when activity is destroyed
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("test", "ondestroy");
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);

            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(pokemonTeamList);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("test", "oncreate");

        setContentView(R.layout.activity_team_building);

        FileInputStream fos = null;
        try {
            fos = openFileInput(FILENAME);
            ObjectInputStream oos = new ObjectInputStream(fos);
            pokemonTeamList = (ArrayList<PokemonTeam>) oos.readObject();
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (pokemonTeamList == null) {
            pokemonTeamList = new ArrayList<>();
        }

        mDrawerTeamList = (ListView) findViewById(R.id.listview_teambuilding_teams);
        teamPreviewListAdapter = new TeamListAdapter(getApplicationContext(), pokemonTeamList);
        mDrawerTeamList.setAdapter(
                teamPreviewListAdapter
        );

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.layout_teambuilder_drawer);

        if (pokemonTeamList.size() > 0) {
            drawer.openDrawer(Gravity.START);
        }

        mDrawerTeamList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DrawerLayout d = (DrawerLayout) findViewById(R.id.layout_teambuilder_drawer);
                selectTeam(position);
                d.closeDrawer(Gravity.START);
            }
        });
    }

    private void selectTeam(int position) {
        PokemonTeam team = pokemonTeamList.get(position);
        TeamBuildingFragment fragment = TeamBuildingFragment.newInstance(team);

        selectedPos = position;
        //TODO implement checkable
        mDrawerTeamList.setItemChecked(position, true);

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.teambuilder_fragmentcontainer, fragment, "")
                .commit();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.team_building, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_create_team) {
            PokemonTeam pt = new PokemonTeam();
            pokemonTeamList.add(pt);
            teamPreviewListAdapter.notifyDataSetChanged();

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.layout_teambuilder_drawer);
            drawer.openDrawer(Gravity.START);

        } else if (id == R.id.action_remove_team) {
            if (selectedPos != -1) {
                pokemonTeamList.remove(selectedPos);
                selectedPos = -1;

                teamPreviewListAdapter.notifyDataSetChanged();

                getSupportFragmentManager().beginTransaction().
                        remove(getSupportFragmentManager().findFragmentById(R.id.teambuilder_fragmentcontainer)).commit();


                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.layout_teambuilder_drawer);
                drawer.openDrawer(Gravity.START);
            }
        } else if (id == R.id.action_export_team) {
            if (selectedPos != -1) {
                // TODO
                pokemonTeamList.get(selectedPos).export();
            }
        } else if (id == R.id.action_import_team) {
            // TODO
        } else if (id == R.id.action_open_team_drawer) {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.layout_teambuilder_drawer);
            drawer.openDrawer(Gravity.START);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Class used to show the team list in the drawer (6 icons + nickname)
     */
    private class TeamListAdapter extends ArrayAdapter<PokemonTeam> {
        public TeamListAdapter(Context getContext, ArrayList<PokemonTeam> userListData) {
            super(getContext, 0, userListData);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.listwidget_teampreview, null);
            }
            PokemonTeam p = pokemonTeamList.get(position);

            TextView teamName = (TextView) convertView.findViewById(R.id.team_nickname);
            teamName.setText(p.getNickname());

            LinearLayout layout = (LinearLayout) convertView.findViewById(R.id.pokemon_small_icon_list);
            layout.removeAllViews();

            for (Pokemon pokemon : p.getPokemons()) {
                ImageView i = new ImageView(getContext());
                int smallIconId = pokemon.getIconSmall();
                Drawable d = getResources().getDrawable(smallIconId);
                i.setImageDrawable(d);
                layout.addView(i);
            }

            return convertView;
        }
    }
}
