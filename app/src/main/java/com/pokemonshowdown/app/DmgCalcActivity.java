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

import com.pokemonshowdown.data.MoveDex;
import com.pokemonshowdown.data.Pokemon;
import com.pokemonshowdown.data.SearchableActivity;

public class DmgCalcActivity extends FragmentActivity {
    public final static String DTAG = DmgCalcActivity.class.getName();
    public final static int REQUEST_CODE_FIND_ATTACKER = 0;
    public final static int REQUEST_CODE_FIND_DEFENDER = 1;
    public final static int REQUEST_CODE_GET_MOVE_1 = 2;
    public final static int REQUEST_CODE_GET_MOVE_2 = 3;
    public final static int REQUEST_CODE_GET_MOVE_3 = 4;
    public final static int REQUEST_CODE_GET_MOVE_4 = 5;

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
                setMove1(getAttacker().getMove1());
                setMove2(getAttacker().getMove2());
                setMove3(getAttacker().getMove3());
                setMove4(getAttacker().getMove4());
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

        TextView move1 = (TextView) findViewById(R.id.move1);
        setMove1(mAttacker.getMove1());
        move1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DmgCalcActivity.this, SearchableActivity.class);
                intent.putExtra("Search Type", SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
                DmgCalcActivity.this.startActivityForResult(intent, REQUEST_CODE_GET_MOVE_1);
            }
        });
        TextView move2 = (TextView) findViewById(R.id.move2);
        setMove2(mAttacker.getMove2());
        move2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DmgCalcActivity.this, SearchableActivity.class);
                intent.putExtra("Search Type", SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
                DmgCalcActivity.this.startActivityForResult(intent, REQUEST_CODE_GET_MOVE_2);
            }
        });
        TextView move3 = (TextView) findViewById(R.id.move3);
        setMove3(mAttacker.getMove3());
        move3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DmgCalcActivity.this, SearchableActivity.class);
                intent.putExtra("Search Type", SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
                DmgCalcActivity.this.startActivityForResult(intent, REQUEST_CODE_GET_MOVE_3);
            }
        });
        TextView move4 = (TextView) findViewById(R.id.move4);
        setMove4(mAttacker.getMove4());
        move4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DmgCalcActivity.this, SearchableActivity.class);
                intent.putExtra("Search Type", SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
                DmgCalcActivity.this.startActivityForResult(intent, REQUEST_CODE_GET_MOVE_4);
            }
        });
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
            switch (requestCode) {
                case REQUEST_CODE_FIND_ATTACKER:
                    setAttacker(data.getExtras().getString("Search"));
                    return;
                case REQUEST_CODE_FIND_DEFENDER:
                    setDefender(data.getExtras().getString("Search"));
                    return;
                case REQUEST_CODE_GET_MOVE_1:
                    setMove1(data.getExtras().getString("Search"));
                    return;
                case REQUEST_CODE_GET_MOVE_2:
                    setMove2(data.getExtras().getString("Search"));
                    return;
                case REQUEST_CODE_GET_MOVE_3:
                    setMove3(data.getExtras().getString("Search"));
                    return;
                case REQUEST_CODE_GET_MOVE_4:
                    setMove4(data.getExtras().getString("Search"));
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

    private void setMove1(String move) {
        mAttacker.setMove1(move);
        String moveName = (move.equals("--")) ? move : MoveDex.getMoveName(getApplicationContext(), move);
        ((TextView) findViewById(R.id.move1)).setText(moveName);
        calculateDamage(1);
    }

    private void setMove2(String move) {
        mAttacker.setMove2(move);
        String moveName = (move.equals("--")) ? move : MoveDex.getMoveName(getApplicationContext(), move);
        ((TextView) findViewById(R.id.move2)).setText(moveName);
        calculateDamage(2);
    }

    private void setMove3(String move) {
        mAttacker.setMove3(move);
        String moveName = (move.equals("--")) ? move : MoveDex.getMoveName(getApplicationContext(), move);
        ((TextView) findViewById(R.id.move3)).setText(moveName);
        calculateDamage(3);
    }

    private void setMove4(String move) {
        mAttacker.setMove4(move);
        String moveName = (move.equals("--")) ? move : MoveDex.getMoveName(getApplicationContext(), move);
        ((TextView) findViewById(R.id.move4)).setText(moveName);
        calculateDamage(4);
    }
    
    private void calculateDamage(int moveIndex) {
        String damage = "0.0%";
        switch (moveIndex) {
            case 1:
                ((TextView) findViewById(R.id.move1_result)).setText(damage);
                break;
            case 2:
                ((TextView) findViewById(R.id.move2_result)).setText(damage);
                break;
            case 3:
                ((TextView) findViewById(R.id.move3_result)).setText(damage);
                break;
            case 4:
                ((TextView) findViewById(R.id.move4_result)).setText(damage);
                break;
        }
    }

}

