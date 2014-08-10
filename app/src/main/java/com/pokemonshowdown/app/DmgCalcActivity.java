package com.pokemonshowdown.app;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by thain on 7/17/14.
 */
public class DmgCalcActivity extends FragmentActivity {
    private final static String DCTAG = "DMG_CALC_TAG";

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mLeftDrawerTitles;
    private int mCurrentPosition;

    private Pokemon mAttacker;
    private Pokemon mDefender;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dmgcalc);

        mTitle = mDrawerTitle = getTitle();
        mLeftDrawerTitles = getResources().getStringArray(R.array.bar_left_drawer_generation);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.layout_dmgcalc_drawer);
        mDrawerList = (ListView) findViewById(R.id.layout_dmgcalc_left_drawer);

        // enable ActionBar application icon to behave as action to toggle navigation drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_dmgcalc, mLeftDrawerTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar application icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  // host Activity
                mDrawerLayout,         // DrawerLayout object
                R.drawable.ic_drawer,  // navigation drawer image to replace 'Up' caret
                R.string.drawer_open,  // "open drawer" description for accessibility
                R.string.drawer_close  // "close drawer" description for accessibility
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        ImageView switchButton = (ImageView) findViewById(R.id.dmgcalc_switch);
        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Pokemon temp = getAttacker();
                setAttacker(getDefender());
                setDefender(temp);
            }
        });

        TextView attacker = (TextView) findViewById(R.id.dmgcalc_attacker);
        attacker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPokemon(getAttacker());
            }
        });

        TextView defender = (TextView) findViewById(R.id.dmgcalc_defender);
        defender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPokemon(getDefender());
            }
        });

        if (savedInstanceState == null) {
            mCurrentPosition = 5;
            selectItem(mCurrentPosition);
            setAttacker("azumarill");
            setDefender("heatran");
        } else {
            try {
                mCurrentPosition = savedInstanceState.getInt("position");
                selectItem(mCurrentPosition);
                setAttacker((Pokemon) savedInstanceState.getSerializable("Attacker"));
                setDefender((Pokemon) savedInstanceState.getSerializable("Defender"));
            } catch (NullPointerException e) {
                Log.e(DCTAG, e.toString());
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("Attacker", getAttacker());
        outState.putSerializable("Defender", getDefender());
        outState.putInt("position", mCurrentPosition);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.fragment_dmgcalc, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Called whenever we call invalidateOptionsMenu()
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the navigation drawer is open, hide action items related to the content view
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) return true;
        // Handle action buttons
        switch(item.getItemId()) {
            case R.id.menu_back:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public Pokemon getAttacker() {
        return mAttacker;
    }

    public void setAttacker(String attacker) {
        setAttacker(new Pokemon(getApplicationContext(), attacker, true));
    }

    public void setAttacker(Pokemon attacker) {
        mAttacker = attacker;

        TextView textView = (TextView) findViewById(R.id.dmgcalc_attacker);
        textView.setCompoundDrawablesWithIntrinsicBounds(attacker.getIconSmall(), 0, 0, 0);
        textView.setText(attacker.getName());
    }



    public Pokemon getDefender() {
        return mDefender;
    }

    public void setDefender(String defender) {
        setDefender(new Pokemon(getApplicationContext(), defender, true));
    }

    public void setDefender(Pokemon defender) {
        mDefender = defender;
        TextView textView = (TextView) findViewById(R.id.dmgcalc_defender);
        textView.setCompoundDrawablesWithIntrinsicBounds(defender.getIconSmall(), 0, 0, 0);
        textView.setText(defender.getName());
    }

    private void loadPokemon(Pokemon pokemon) {
        DialogFragment fragment = new PokemonFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("Pokemon", pokemon);
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragment.show(fragmentManager, PokemonFragment.PokemonTAG);
    }

    private void selectItem(int position) {
        mCurrentPosition = position;
        mDrawerList.setItemChecked(position, true);
        setTitle(mLeftDrawerTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    // The click listener for ListView in the navigation drawer
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

}

