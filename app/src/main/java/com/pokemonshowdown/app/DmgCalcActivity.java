package com.pokemonshowdown.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pokemonshowdown.data.Pokemon;

public class DmgCalcActivity extends FragmentActivity {
    public final static String DTAG = DmgCalcActivity.class.getName();
    public final static int REQUEST_CODE_FIND_ATTACKER = 0;
    public final static int REQUEST_CODE_FIND_DEFENDER = 1;

    private Pokemon mAttacker;
    private Pokemon mDefender;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dmgcalc);

        getActionBar().setTitle(R.string.bar_dmg_calc);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        Fragment fieldFragment = new DmgCalcFieldXYFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.dmgcalc_field_container, fieldFragment)
                .commit();

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
                loadPokemon(getAttacker(), REQUEST_CODE_FIND_ATTACKER);
            }
        });

        TextView defender = (TextView) findViewById(R.id.dmgcalc_defender);
        defender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPokemon(getDefender(), REQUEST_CODE_FIND_DEFENDER);
            }
        });

        if (savedInstanceState == null) {
            setAttacker("azumarill");
            setDefender("heatran");
        } else {
            try {
                setAttacker((Pokemon) savedInstanceState.getSerializable("Attacker"));
                setDefender((Pokemon) savedInstanceState.getSerializable("Defender"));
            } catch (NullPointerException e) {
                Log.e(DTAG, e.toString());
            }
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_FIND_ATTACKER) {
                setAttacker(data.getExtras().getString("Search"));
            } else {
                setDefender(data.getExtras().getString("Search"));
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("Attacker", getAttacker());
        outState.putSerializable("Defender", getDefender());
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

    private void loadPokemon(Pokemon pokemon, int searchCode) {
        DialogFragment fragment = new PokemonFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("Pokemon", pokemon);
        bundle.putBoolean("Search", true);
        bundle.putInt("Search Code", searchCode);
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragment.show(fragmentManager, PokemonFragment.PTAG);
    }

}

