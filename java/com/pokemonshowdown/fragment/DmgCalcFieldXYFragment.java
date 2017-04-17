package com.pokemonshowdown.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pokemonshowdown.R;
import com.pokemonshowdown.activity.SearchableActivity;
import com.pokemonshowdown.data.ItemDex;
import com.pokemonshowdown.data.MoveDex;
import com.pokemonshowdown.data.Pokedex;
import com.pokemonshowdown.data.Pokemon;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DmgCalcFieldXYFragment extends Fragment {

    public final static String DTAG = DmgCalcFieldXYFragment.class.getName();
    public final static int REQUEST_CODE_FIND_ATTACKER = 0;
    public final static int REQUEST_CODE_FIND_DEFENDER = 1;
    public final static int REQUEST_CODE_FIND_ATTACKER_ABILITY = 11;
    public final static int REQUEST_CODE_FIND_ATTACKER_ITEM = 12;
    public final static int REQUEST_CODE_FIND_DEFENDER_ABILITY = 14;
    public final static int REQUEST_CODE_FIND_DEFENDER_ITEM = 15;
    public final static int REQUEST_CODE_GET_ATTACKER_MOVE_1 = 2;
    public final static int REQUEST_CODE_GET_ATTACKER_MOVE_2 = 3;
    public final static int REQUEST_CODE_GET_ATTACKER_MOVE_3 = 4;
    public final static int REQUEST_CODE_GET_ATTACKER_MOVE_4 = 5;
    public final static int REQUEST_CODE_GET_DEFENDER_MOVE_1 = 6;
    public final static int REQUEST_CODE_GET_DEFENDER_MOVE_2 = 7;
    public final static int REQUEST_CODE_GET_DEFENDER_MOVE_3 = 8;
    public final static int REQUEST_CODE_GET_DEFENDER_MOVE_4 = 9;

    private final static DecimalFormat DAMAGE_FORMAT = new DecimalFormat("#0.0%");
    private final static String PARAM_ATTACKER = "Attacker";
    private final static String PARAM_DEFENDER = "Defender";
    private Pokemon mAttacker;
    private Pokemon mDefender;
    private Map<String, List<String>> mEffectivenessStrong = new HashMap<>();
    private Map<String, List<String>> mEffectivenessWeak = new HashMap<>();
    private Map<String, List<String>> mEffectivenessImmune = new HashMap<>();
    private View mView;

    private boolean mIsSingles = true;
    private boolean mGravityActive = false;
    private boolean mStealthRocksActive = false;
    private boolean mReflectActive = false;
    private boolean mLightScreenActive = false;
    private boolean mForesightActive = false;
    private boolean mHelpingHandActive = false;
    private int mSpikesCount = 0;
    private Weather mActiveWeather = Weather.NO_WEATHER;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        setRetainInstance(true);
        mView = inflater.inflate(R.layout.fragment_damage_calc, parent, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DAMAGE_FORMAT.setRoundingMode(RoundingMode.FLOOR);
        createIndexOfTypeModifiers();

        TextView attacker = (TextView) mView.findViewById(R.id.dmgcalc_attacker_name);
        attacker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPokemon(REQUEST_CODE_FIND_ATTACKER);
            }
        });

        TextView defender = (TextView) mView.findViewById(R.id.dmgcalc_defender_name);
        defender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPokemon(REQUEST_CODE_FIND_DEFENDER);
            }
        });

        if (savedInstanceState == null) {
            setAttacker(new Pokemon(getContext(), "abomasnow"));
            setDefender(new Pokemon(getContext(), "abomasnow"));
            getAttacker().setMove1("blizzard");
            getAttacker().setMove2("giga drain");
            getAttacker().setMove3("earthquake");
            getAttacker().setMove4("ice shard");
            getAttacker().setSpAtkEV(252);
            getAttacker().setSpdEV(252);
            getAttacker().setAtkEV(4);
            getAttacker().setNature("Hasty");
            getAttacker().setItem("Life Orb");
            getDefender().setMove1("blizzard");
            getDefender().setMove2("giga drain");
            getDefender().setMove3("earthquake");
            getDefender().setMove4("ice shard");
            getDefender().setSpAtkEV(252);
            getDefender().setSpdEV(252);
            getDefender().setAtkEV(4);
            getDefender().setNature("Hasty");
            getDefender().setItem("Life Orb");
        } else {

            // Probably too pessimistic but I've seen things...
            if (savedInstanceState.containsKey(PARAM_ATTACKER)) {
                Serializable possibleAttacker = savedInstanceState.getSerializable(PARAM_ATTACKER);
                if (possibleAttacker instanceof Pokemon) {
                    setAttacker((Pokemon) possibleAttacker);
                }

            }

            if (savedInstanceState.containsKey(PARAM_DEFENDER)) {
                Serializable possibleDefender = savedInstanceState.getSerializable(PARAM_DEFENDER);
                if (possibleDefender instanceof Pokemon) {
                    setDefender((Pokemon) possibleDefender);
                }

            }

            // If it is not saved fall back to defaults
            if (getAttacker() == null) {
                setAttacker(new Pokemon(getContext(), "abomasnow"));
                getAttacker().setMove1("blizzard");
                getAttacker().setMove2("giga drain");
                getAttacker().setMove3("earthquake");
                getAttacker().setMove4("ice shard");
                getAttacker().setSpAtkEV(252);
                getAttacker().setSpdEV(252);
                getAttacker().setAtkEV(4);
                getAttacker().setNature("hasty");
                getAttacker().setAbilityTag("snowwarning");
                getAttacker().setItem("life orb");
            }

            if (getDefender() == null) {
                setDefender(new Pokemon(getContext(), "abomasnow"));
                getDefender().setMove1("blizzard");
                getDefender().setMove2("giga drain");
                getDefender().setMove3("earthquake");
                getDefender().setMove4("ice shard");
                getDefender().setSpAtkEV(252);
                getDefender().setSpdEV(252);
                getDefender().setAtkEV(4);
                getDefender().setNature("hasty");
                getDefender().setAbilityTag("snowwarning");
                getDefender().setItem("life orb");

            }
        }

        TextView atMove1 = (TextView) mView.findViewById(R.id.dmgcalc_attacker_move1);
        setAttackerMove1(mAttacker.getMove1());
        atMove1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateDamage(true, 1);
            }
        });
        TextView atMove2 = (TextView) mView.findViewById(R.id.dmgcalc_attacker_move2);
        setAttackerMove2(mAttacker.getMove2());
        atMove2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateDamage(true, 2);
            }
        });
        TextView atMove3 = (TextView) mView.findViewById(R.id.dmgcalc_attacker_move3);
        setAttackerMove3(mAttacker.getMove3());
        atMove3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateDamage(true, 3);
            }
        });
        TextView atMove4 = (TextView) mView.findViewById(R.id.dmgcalc_attacker_move4);
        setAttackerMove4(mAttacker.getMove4());
        atMove4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateDamage(true, 4);
            }
        });
        atMove1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(getContext(), SearchableActivity.class);
                intent.putExtra("Search Type", SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
                startActivityForResult(intent, REQUEST_CODE_GET_ATTACKER_MOVE_1);
                return false;
            }
        });
        atMove2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(getContext(), SearchableActivity.class);
                intent.putExtra("Search Type", SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
                startActivityForResult(intent, REQUEST_CODE_GET_ATTACKER_MOVE_2);
                return false;
            }
        });
        atMove3.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(getContext(), SearchableActivity.class);
                intent.putExtra("Search Type", SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
                startActivityForResult(intent, REQUEST_CODE_GET_ATTACKER_MOVE_3);
                return false;
            }
        });
        atMove4.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(getContext(), SearchableActivity.class);
                intent.putExtra("Search Type", SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
                startActivityForResult(intent, REQUEST_CODE_GET_ATTACKER_MOVE_4);
                return false;
            }
        });

        TextView defMove1 = (TextView) mView.findViewById(R.id.dmgcalc_defender_move1);
        setDefenderMove1(mDefender.getMove1());
        defMove1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateDamage(false, 1);
            }
        });
        TextView defMove2 = (TextView) mView.findViewById(R.id.dmgcalc_defender_move2);
        setDefenderMove2(mDefender.getMove2());
        defMove2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateDamage(false, 2);
            }
        });
        TextView defMove3 = (TextView) mView.findViewById(R.id.dmgcalc_defender_move3);
        setDefenderMove3(mDefender.getMove3());
        defMove3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateDamage(false, 3);
            }
        });
        TextView defMove4 = (TextView) mView.findViewById(R.id.dmgcalc_defender_move4);
        setDefenderMove4(mDefender.getMove4());
        defMove4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateDamage(false, 4);
            }
        });
        defMove1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent intent = new Intent(getContext(), SearchableActivity.class);
                intent.putExtra("Search Type", SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
                startActivityForResult(intent, REQUEST_CODE_GET_DEFENDER_MOVE_1);
                return false;
            }
        });
        defMove2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent intent = new Intent(getContext(), SearchableActivity.class);
                intent.putExtra("Search Type", SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
                startActivityForResult(intent, REQUEST_CODE_GET_DEFENDER_MOVE_2);
                return false;
            }
        });
        defMove3.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent intent = new Intent(getContext(), SearchableActivity.class);
                intent.putExtra("Search Type", SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
                startActivityForResult(intent, REQUEST_CODE_GET_DEFENDER_MOVE_3);
                return false;
            }
        });
        defMove4.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent intent = new Intent(getContext(), SearchableActivity.class);
                intent.putExtra("Search Type", SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
                startActivityForResult(intent, REQUEST_CODE_GET_DEFENDER_MOVE_4);
                return false;
            }
        });

        final TextView singles = (TextView) mView.findViewById(R.id.dmg_calc_field_singles);
        final TextView doubles = (TextView) mView.findViewById(R.id.dmg_calc_field_doubles);
        final TextView gravity = (TextView) mView.findViewById(R.id.dmg_calc_field_gravity);
        final TextView none = (TextView) mView.findViewById(R.id.dmg_calc_field_none);
        final TextView sun = (TextView) mView.findViewById(R.id.dmg_calc_field_sun);
        final TextView rain = (TextView) mView.findViewById(R.id.dmg_calc_field_rain);
        final TextView sand = (TextView) mView.findViewById(R.id.dmg_calc_field_sand);
        final TextView hail = (TextView) mView.findViewById(R.id.dmg_calc_field_hail);
        final TextView sr = (TextView) mView.findViewById(R.id.dmg_calc_field_sr);
        final TextView spike0 = (TextView) mView.findViewById(R.id.dmg_calc_field_0spike);
        final TextView spike1 = (TextView) mView.findViewById(R.id.dmg_calc_field_1spike);
        final TextView spike2 = (TextView) mView.findViewById(R.id.dmg_calc_field_2spike);
        final TextView spike3 = (TextView) mView.findViewById(R.id.dmg_calc_field_3spike);
        final TextView reflect = (TextView) mView.findViewById(R.id.dmg_calc_field_reflect);
        final TextView lightscreen = (TextView) mView.findViewById(R.id.dmg_calc_field_lightscreen);
        final TextView foresight = (TextView) mView.findViewById(R.id.dmg_calc_field_foresight);
        final TextView helpinghand = (TextView) mView.findViewById(R.id.dmg_calc_field_helpinghand);


        singles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                singles.setTypeface(null, Typeface.BOLD);
                doubles.setTypeface(null, Typeface.ITALIC);
                setConditionStatus(FieldConditions.SINGLES, true);
            }
        });

        doubles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doubles.setTypeface(null, Typeface.BOLD);
                singles.setTypeface(null, Typeface.ITALIC);
                setConditionStatus(FieldConditions.DOUBLES, true);
            }
        });

        gravity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Typeface typeface = gravity.getTypeface();
                if (typeface.isBold()) {
                    gravity.setTypeface(null, Typeface.ITALIC);
                    setConditionStatus(FieldConditions.GRAVITY, false);
                } else {
                    gravity.setTypeface(null, Typeface.BOLD);
                    setConditionStatus(FieldConditions.GRAVITY, true);
                }
            }
        });

        none.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                none.setTypeface(null, Typeface.BOLD);
                sun.setTypeface(null, Typeface.ITALIC);
                rain.setTypeface(null, Typeface.ITALIC);
                sand.setTypeface(null, Typeface.ITALIC);
                hail.setTypeface(null, Typeface.ITALIC);
                setConditionStatus(FieldConditions.NO_WEATHER, true);
            }
        });

        sun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                none.setTypeface(null, Typeface.ITALIC);
                sun.setTypeface(null, Typeface.BOLD);
                rain.setTypeface(null, Typeface.ITALIC);
                sand.setTypeface(null, Typeface.ITALIC);
                hail.setTypeface(null, Typeface.ITALIC);
                setConditionStatus(FieldConditions.SUN, true);
            }
        });

        rain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                none.setTypeface(null, Typeface.ITALIC);
                sun.setTypeface(null, Typeface.ITALIC);
                rain.setTypeface(null, Typeface.BOLD);
                sand.setTypeface(null, Typeface.ITALIC);
                hail.setTypeface(null, Typeface.ITALIC);
                setConditionStatus(FieldConditions.RAIN, true);
            }
        });

        sand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                none.setTypeface(null, Typeface.ITALIC);
                sun.setTypeface(null, Typeface.ITALIC);
                rain.setTypeface(null, Typeface.ITALIC);
                sand.setTypeface(null, Typeface.BOLD);
                hail.setTypeface(null, Typeface.ITALIC);
                setConditionStatus(FieldConditions.SAND, true);
            }
        });

        hail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                none.setTypeface(null, Typeface.ITALIC);
                sun.setTypeface(null, Typeface.ITALIC);
                rain.setTypeface(null, Typeface.ITALIC);
                sand.setTypeface(null, Typeface.ITALIC);
                hail.setTypeface(null, Typeface.BOLD);
                setConditionStatus(FieldConditions.HAIL, true);
            }
        });

        sr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Typeface typeface = sr.getTypeface();
                if (typeface.isBold()) {
                    sr.setTypeface(null, Typeface.ITALIC);
                    setConditionStatus(FieldConditions.STEALTH_ROCK, false);
                } else {
                    sr.setTypeface(null, Typeface.BOLD);
                    setConditionStatus(FieldConditions.STEALTH_ROCK, true);
                }
            }
        });

        spike0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spike0.setTypeface(null, Typeface.BOLD);
                spike1.setTypeface(null, Typeface.ITALIC);
                spike2.setTypeface(null, Typeface.ITALIC);
                spike3.setTypeface(null, Typeface.ITALIC);
                setConditionStatus(FieldConditions.ZERO_SPIKES, true);
            }
        });

        spike1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spike0.setTypeface(null, Typeface.ITALIC);
                spike1.setTypeface(null, Typeface.BOLD);
                spike2.setTypeface(null, Typeface.ITALIC);
                spike3.setTypeface(null, Typeface.ITALIC);
                setConditionStatus(FieldConditions.ONE_SPIKES, true);
            }
        });

        spike2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spike0.setTypeface(null, Typeface.ITALIC);
                spike1.setTypeface(null, Typeface.ITALIC);
                spike2.setTypeface(null, Typeface.BOLD);
                spike3.setTypeface(null, Typeface.ITALIC);
                setConditionStatus(FieldConditions.TWO_SPIKES, true);
            }
        });

        spike3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spike0.setTypeface(null, Typeface.ITALIC);
                spike1.setTypeface(null, Typeface.ITALIC);
                spike2.setTypeface(null, Typeface.ITALIC);
                spike3.setTypeface(null, Typeface.BOLD);
                setConditionStatus(FieldConditions.THREE_SPIKES, true);
            }
        });

        reflect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Typeface typeface = reflect.getTypeface();
                if (typeface.isBold()) {
                    reflect.setTypeface(null, Typeface.ITALIC);
                    setConditionStatus(FieldConditions.REFLECT, false);
                } else {
                    reflect.setTypeface(null, Typeface.BOLD);
                    setConditionStatus(FieldConditions.REFLECT, true);
                }
            }
        });

        lightscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Typeface typeface = lightscreen.getTypeface();
                if (typeface.isBold()) {
                    lightscreen.setTypeface(null, Typeface.ITALIC);
                    setConditionStatus(FieldConditions.LIGHT_SCREEN, false);
                } else {
                    lightscreen.setTypeface(null, Typeface.BOLD);
                    setConditionStatus(FieldConditions.LIGHT_SCREEN, true);
                }
            }
        });

        foresight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Typeface typeface = foresight.getTypeface();
                if (typeface.isBold()) {
                    foresight.setTypeface(null, Typeface.ITALIC);
                    setConditionStatus(FieldConditions.FORESIGHT, false);
                } else {
                    foresight.setTypeface(null, Typeface.BOLD);
                    setConditionStatus(FieldConditions.FORESIGHT, true);
                }
            }
        });

        helpinghand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Typeface typeface = helpinghand.getTypeface();
                if (typeface.isBold()) {
                    helpinghand.setTypeface(null, Typeface.ITALIC);
                    setConditionStatus(FieldConditions.HELPING_HAND, false);
                } else {
                    helpinghand.setTypeface(null, Typeface.BOLD);
                    setConditionStatus(FieldConditions.HELPING_HAND, true);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_FIND_ATTACKER:
                    setAttacker(new Pokemon(getContext(), data.getExtras().getString("Search")));
                    return;
                case REQUEST_CODE_FIND_DEFENDER:
                    setDefender(new Pokemon(getContext(), data.getExtras().getString("Search")));
                    return;
                case REQUEST_CODE_FIND_ATTACKER_ITEM:
                    Pokemon pokemon = getAttacker();
                    pokemon.setItem(data.getExtras().getString("Search"));
                    setAttacker(pokemon);
                    return;
                case REQUEST_CODE_FIND_ATTACKER_ABILITY:
                    pokemon = getAttacker();
                    pokemon.setAbilityTag(data.getExtras().getString("Search"));
                    setAttacker(pokemon);
                    return;
                case REQUEST_CODE_FIND_DEFENDER_ITEM:
                    pokemon = getDefender();
                    pokemon.setItem(data.getExtras().getString("Search"));
                    setDefender(pokemon);
                    return;
                case REQUEST_CODE_FIND_DEFENDER_ABILITY:
                    pokemon = getDefender();
                    pokemon.setAbilityTag(data.getExtras().getString("Search"));
                    setDefender(pokemon);
                    return;
                case REQUEST_CODE_GET_ATTACKER_MOVE_1:
                    setAttackerMove1(data.getExtras().getString("Search"));
                    return;
                case REQUEST_CODE_GET_ATTACKER_MOVE_2:
                    setAttackerMove2(data.getExtras().getString("Search"));
                    return;
                case REQUEST_CODE_GET_ATTACKER_MOVE_3:
                    setAttackerMove3(data.getExtras().getString("Search"));
                    return;
                case REQUEST_CODE_GET_ATTACKER_MOVE_4:
                    setAttackerMove4(data.getExtras().getString("Search"));
                    break;
                case REQUEST_CODE_GET_DEFENDER_MOVE_1:
                    setDefenderMove1(data.getExtras().getString("Search"));
                    return;
                case REQUEST_CODE_GET_DEFENDER_MOVE_2:
                    setDefenderMove2(data.getExtras().getString("Search"));
                    return;
                case REQUEST_CODE_GET_DEFENDER_MOVE_3:
                    setDefenderMove3(data.getExtras().getString("Search"));
                    return;
                case REQUEST_CODE_GET_DEFENDER_MOVE_4:
                    setDefenderMove4(data.getExtras().getString("Search"));
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(PARAM_ATTACKER, getAttacker());
        outState.putSerializable(PARAM_DEFENDER, getDefender());
    }

    public Pokemon getAttacker() {
        return mAttacker;
    }

    public void setAttacker(Pokemon attacker) {
        mAttacker = attacker;

        TextView textView = (TextView) mView.findViewById(R.id.dmgcalc_attacker_name);
        textView.setText(attacker.getName());
        ImageView imageView = (ImageView) mView.findViewById(R.id.dmgcalc_attacker_icon);
        imageView.setImageResource(attacker.getIcon());
        textView.invalidate();
        imageView.invalidate();

        setAttackerMove1(attacker.getMove1());
        setAttackerMove2(attacker.getMove2());
        setAttackerMove3(attacker.getMove3());
        setAttackerMove4(attacker.getMove4());
        setupMetricsDisplay(true);
        //calculateDamage(1);
    }

    public Pokemon getDefender() {
        return mDefender;
    }

    public void setDefender(Pokemon defender) {
        mDefender = defender;
        TextView textView = (TextView) mView.findViewById(R.id.dmgcalc_defender_name);
        textView.setText(defender.getName());
        ImageView imageView = (ImageView) mView.findViewById(R.id.dmgcalc_defender_icon);
        imageView.setImageResource(defender.getIcon());
        textView.invalidate();
        imageView.invalidate();

        setDefenderMove1(defender.getMove1());
        setDefenderMove2(defender.getMove2());
        setDefenderMove3(defender.getMove3());
        setDefenderMove4(defender.getMove4());
        setupMetricsDisplay(false);
        //calculateDamage(1);
    }

    private void loadPokemon(int searchCode) {
        Intent intent = new Intent(getActivity(), SearchableActivity.class);
        intent.putExtra("Search Type", SearchableActivity.REQUEST_CODE_SEARCH_POKEMON);
        startActivityForResult(intent, searchCode);
    }

    private void setAttackerMove1(String move) {
        mAttacker.setMove1(move);
        String moveName = (move.equals("")) ? "--" : MoveDex.getMoveName(getContext(), move);
        ((TextView) mView.findViewById(R.id.dmgcalc_attacker_move1)).setText(moveName);
        calculateDamage(true, 1);
    }

    private void setAttackerMove2(String move) {
        mAttacker.setMove2(move);
        String moveName = (move.equals("")) ? "--" : MoveDex.getMoveName(getContext(), move);
        ((TextView) mView.findViewById(R.id.dmgcalc_attacker_move2)).setText(moveName);
        calculateDamage(true, 2);
    }

    private void setAttackerMove3(String move) {
        mAttacker.setMove3(move);
        String moveName = (move.equals("")) ? "--" : MoveDex.getMoveName(getContext(), move);
        ((TextView) mView.findViewById(R.id.dmgcalc_attacker_move3)).setText(moveName);
        calculateDamage(true, 3);
    }

    private void setAttackerMove4(String move) {
        mAttacker.setMove4(move);
        String moveName = (move.equals("")) ? "--" : MoveDex.getMoveName(getContext(), move);
        ((TextView) mView.findViewById(R.id.dmgcalc_attacker_move4)).setText(moveName);
        calculateDamage(true, 4);
    }

    private void setDefenderMove1(String move) {
        mDefender.setMove1(move);
        String moveName = (move.equals("")) ? "--" : MoveDex.getMoveName(getContext(), move);
        ((TextView) mView.findViewById(R.id.dmgcalc_defender_move1)).setText(moveName);
        calculateDamage(false, 1);
    }

    private void setDefenderMove2(String move) {
        mDefender.setMove2(move);
        String moveName = (move.equals("")) ? "--" : MoveDex.getMoveName(getContext(), move);
        ((TextView) mView.findViewById(R.id.dmgcalc_defender_move2)).setText(moveName);
        calculateDamage(false, 2);
    }

    private void setDefenderMove3(String move) {
        mDefender.setMove3(move);
        String moveName = (move.equals("")) ? "--" : MoveDex.getMoveName(getContext(), move);
        ((TextView) mView.findViewById(R.id.dmgcalc_defender_move3)).setText(moveName);
        calculateDamage(false, 3);
    }

    private void setDefenderMove4(String move) {
        mDefender.setMove4(move);
        String moveName = (move.equals("")) ? "--" : MoveDex.getMoveName(getContext(), move);
        ((TextView) mView.findViewById(R.id.dmgcalc_defender_move4)).setText(moveName);
        calculateDamage(false, 4);
    }

    private void setupMetricsDisplay(final boolean bol) {
        final Pokemon attacker = bol ? getAttacker() : getDefender();

        if (bol) {
            ((TextView) mView.findViewById(R.id.misc_poke_name)).setText("(Attacker)");
        } else {
            ((TextView) mView.findViewById(R.id.misc_poke_name)).setText("(Defender)");
        }

        ((TextView) mView.findViewById(R.id.highlight_name)).setText(attacker.getName());
        final ImageView type1 = (ImageView) mView.findViewById(R.id.type_1);
        final ImageView type2 = (ImageView) mView.findViewById(R.id.type_2);
        final String[] types = new String[]{"Nothing", "Bug", "Dark", "Dragon", "Electric", "Fairy", "Fighting", "Fire", "Flying",
                "Ghost", "Grass", "Ground", "Ice", "Poison", "Psychic", "Rock", "Steel", "Water"};

        type1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getContext()).setTitle("Select a type")
                        .setItems(types, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    attacker.setType(new String[]{attacker.getType()[1]});
                                    attacker.setTypeIcon(new int[]{attacker.getTypeIcon()[1]});
                                    type1.setImageBitmap(null);
                                } else {
                                    attacker.setType(new String[]{types[i].toLowerCase(), attacker.getType()[1]});
                                    attacker.setTypeIcon(new int[]{getContext().getResources()
                                            .getIdentifier("types_" + types[i].toLowerCase(), "drawable", getContext().getPackageName()), attacker.getTypeIcon()[1]});
                                    type1.setImageResource(getContext().getResources()
                                            .getIdentifier("types_" + types[i].toLowerCase(), "drawable", getContext().getPackageName()));
                                }

                                Toast.makeText(getContext(), attacker.getName() + "'s first type was changed to \"" + types[i] + "\"", Toast.LENGTH_SHORT).show();
                                if (bol) {
                                    setAttacker(attacker);
                                } else {
                                    setDefender(attacker);
                                }
                            }
                        }).show();
            }
        });

        type2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getContext()).setTitle("Select a type")
                        .setItems(types, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    attacker.setType(new String[]{attacker.getType()[0]});
                                    attacker.setTypeIcon(new int[]{attacker.getTypeIcon()[0]});
                                    type2.setImageBitmap(null);
                                } else {
                                    attacker.setType(new String[]{attacker.getType()[0], types[i].toLowerCase()});
                                    attacker.setTypeIcon(new int[]{getContext().getResources()
                                            .getIdentifier("types_" + types[i].toLowerCase(), "drawable", getContext().getPackageName()), attacker.getTypeIcon()[0]});
                                    type2.setImageResource(getContext().getResources()
                                            .getIdentifier("types_" + types[i].toLowerCase(), "drawable", getContext().getPackageName()));
                                }

                                Toast.makeText(getContext(), attacker.getName() + "'s second type was changed to \"" + types[i] + "\"", Toast.LENGTH_SHORT).show();
                                if (bol) {
                                    setAttacker(attacker);
                                } else {
                                    setDefender(attacker);
                                }
                            }
                        }).show();
            }
        });

        if (attacker.getTypeIcon().length == 1) {
            type1.setImageResource(attacker.getTypeIcon()[0]);
            type2.setVisibility(View.INVISIBLE);
        } else {
            type1.setImageResource(attacker.getTypeIcon()[0]);
            type2.setVisibility(View.VISIBLE);
            type2.setImageResource(attacker.getTypeIcon()[1]);
        }

        final EditText level = (EditText) mView.findViewById(R.id.level_input);
        level.setText("" + attacker.getLevel());
        level.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (Integer.parseInt(textView.getText().toString()) > 100) {
                    level.setText("100");
                }
                return false;
            }
        });

        setupBaseEditText((EditText) mView.findViewById(R.id.base_hp), 0, bol, attacker);
        setupBaseEditText((EditText) mView.findViewById(R.id.base_atk), 1, bol, attacker);
        setupBaseEditText((EditText) mView.findViewById(R.id.base_def), 2, bol, attacker);
        setupBaseEditText((EditText) mView.findViewById(R.id.base_sp_atk), 3, bol, attacker);
        setupBaseEditText((EditText) mView.findViewById(R.id.base_sp_def), 4, bol, attacker);
        setupBaseEditText((EditText) mView.findViewById(R.id.base_spe), 5, bol, attacker);

        setupEVEditText((EditText) mView.findViewById(R.id.hp_evs_label), 0, bol, attacker);
        setupEVEditText((EditText) mView.findViewById(R.id.atk_evs_label), 1, bol, attacker);
        setupEVEditText((EditText) mView.findViewById(R.id.def_evs_label), 2, bol, attacker);
        setupEVEditText((EditText) mView.findViewById(R.id.sp_atk_evs_label), 3, bol, attacker);
        setupEVEditText((EditText) mView.findViewById(R.id.sp_def_evs_label), 4, bol, attacker);
        setupEVEditText((EditText) mView.findViewById(R.id.spe_evs_label), 5, bol, attacker);

        setupIVEditText((EditText) mView.findViewById(R.id.hp_ivs_label), 0, bol, attacker);
        setupIVEditText((EditText) mView.findViewById(R.id.atk_ivs_label), 1, bol, attacker);
        setupIVEditText((EditText) mView.findViewById(R.id.def_ivs_label), 2, bol, attacker);
        setupIVEditText((EditText) mView.findViewById(R.id.sp_atk_ivs_label), 3, bol, attacker);
        setupIVEditText((EditText) mView.findViewById(R.id.sp_def_ivs_label), 4, bol, attacker);
        setupIVEditText((EditText) mView.findViewById(R.id.spe_ivs_label), 5, bol, attacker);

        ((TextView) mView.findViewById(R.id.hp_stats)).setText("" + attacker.calculateHP());
        ((TextView) mView.findViewById(R.id.atk_stats)).setText("" + attacker.calculateAtk());
        ((TextView) mView.findViewById(R.id.def_stats)).setText("" + attacker.calculateDef());
        ((TextView) mView.findViewById(R.id.sp_atk_stats)).setText("" + attacker.calculateSpAtk());
        ((TextView) mView.findViewById(R.id.sp_def_stats)).setText("" + attacker.calculateSpDef());
        ((TextView) mView.findViewById(R.id.spe_stats)).setText("" + attacker.calculateSpd());

        final TextView nature = (TextView) mView.findViewById(R.id.nature);
        nature.setText(attacker.getNature());
        nature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedNature = Arrays.binarySearch(Pokemon.NATURES, attacker.getNature());
                Dialog dialog = new AlertDialog.Builder(getContext())
                        .setSingleChoiceItems(Pokemon.NATURES_DETAILS, selectedNature, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String newNature = Pokemon.NATURES[which];
                                nature.setText(newNature);
                                attacker.setNature(newNature);
                                attacker.setStats(attacker.calculateStats());
                                dialog.dismiss();
                                if (bol) {
                                    setAttacker(attacker);
                                } else {
                                    setDefender(attacker);
                                }
                            }
                        })
                        .create();
                dialog.show();
            }
        });

        final TextView ability = (TextView) mView.findViewById(R.id.ability);
        ability.setText(attacker.getAbility(getContext()));
        ability.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), SearchableActivity.class);
                intent.putExtra("Search Type", SearchableActivity.REQUEST_CODE_SEARCH_ABILITY);
                if (bol) {
                    startActivityForResult(intent, REQUEST_CODE_FIND_ATTACKER_ABILITY);
                } else {
                    startActivityForResult(intent, REQUEST_CODE_FIND_DEFENDER_ABILITY);
                }
            }
        });
    }

    private void setupBaseEditText(final EditText edit, final int stat, final boolean attacker, final Pokemon pokemon) {
        switch (stat) {
            case 0:
                edit.setText("" + pokemon.getBaseHP());
                break;
            case 1:
                edit.setText("" + pokemon.getBaseAtk());
                break;
            case 2:
                edit.setText("" + pokemon.getBaseDef());
                break;
            case 3:
                edit.setText("" + pokemon.getBaseSpAtk());
                break;
            case 4:
                edit.setText("" + pokemon.getBaseSpDef());
                break;
            case 5:
                edit.setText("" + pokemon.getBaseSpd());
                break;
        }

        edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (Integer.parseInt(edit.getText().toString()) > 255) {
                    edit.setText("255");
                }

                switch (stat) {
                    case 0:
                        pokemon.setBaseHP(Integer.parseInt(edit.getText().toString()));
                        break;
                    case 1:
                        pokemon.setBaseAtk(Integer.parseInt(edit.getText().toString()));
                        break;
                    case 2:
                        pokemon.setBaseDef(Integer.parseInt(edit.getText().toString()));
                        break;
                    case 3:
                        pokemon.setBaseSpAtk(Integer.parseInt(edit.getText().toString()));
                        break;
                    case 4:
                        pokemon.setBaseSpDef(Integer.parseInt(edit.getText().toString()));
                        break;
                    case 5:
                        pokemon.setBaseSpd(Integer.parseInt(edit.getText().toString()));
                        break;
                }

                if (attacker) {
                    setAttacker(pokemon);
                } else {
                    setDefender(pokemon);
                }

                calculateDamage(attacker, 1);
            }
        });
    }

    private void setupEVEditText(final EditText edit, final int stat, final boolean attacker, final Pokemon pokemon) {
        switch (stat) {
            case 0:
                edit.setText("" + pokemon.getHPEV());
                break;
            case 1:
                edit.setText("" + pokemon.getAtkEV());
                break;
            case 2:
                edit.setText("" + pokemon.getDefEV());
                break;
            case 3:
                edit.setText("" + pokemon.getSpAtkEV());
                break;
            case 4:
                edit.setText("" + pokemon.getSpDefEV());
                break;
            case 5:
                edit.setText("" + pokemon.getSpdEV());
                break;
        }

        edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (Integer.parseInt(edit.getText().toString()) > 252) {
                    edit.setText("252");
                }

                switch (stat) {
                    case 0:
                        pokemon.setHPEV(Integer.parseInt(edit.getText().toString()));
                        break;
                    case 1:
                        pokemon.setAtkEV(Integer.parseInt(edit.getText().toString()));
                        break;
                    case 2:
                        pokemon.setDefEV(Integer.parseInt(edit.getText().toString()));
                        break;
                    case 3:
                        pokemon.setSpAtkEV(Integer.parseInt(edit.getText().toString()));
                        break;
                    case 4:
                        pokemon.setSpDefEV(Integer.parseInt(edit.getText().toString()));
                        break;
                    case 5:
                        pokemon.setSpDefEV(Integer.parseInt(edit.getText().toString()));
                        break;
                }

                if (attacker) {
                    setAttacker(pokemon);
                } else {
                    setDefender(pokemon);
                }

                calculateDamage(attacker, 1);
            }
        });
    }

    private void setupIVEditText(final EditText edit, final int stat, final boolean attacker, final Pokemon pokemon) {
        switch (stat) {
            case 0:
                edit.setText("" + pokemon.getHPIV());
                break;
            case 1:
                edit.setText("" + pokemon.getAtkIV());
                break;
            case 2:
                edit.setText("" + pokemon.getDefIV());
                break;
            case 3:
                edit.setText("" + pokemon.getSpAtkIV());
                break;
            case 4:
                edit.setText("" + pokemon.getSpDefIV());
                break;
            case 5:
                edit.setText("" + pokemon.getSpdIV());
                break;
        }

        edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (Integer.parseInt(edit.getText().toString()) > 31) {
                    edit.setText("31");
                }

                switch (stat) {
                    case 0:
                        pokemon.setHPIV(Integer.parseInt(edit.getText().toString()));
                        break;
                    case 1:
                        pokemon.setAtkIV(Integer.parseInt(edit.getText().toString()));
                        break;
                    case 2:
                        pokemon.setDefIV(Integer.parseInt(edit.getText().toString()));
                        break;
                    case 3:
                        pokemon.setSpAtkIV(Integer.parseInt(edit.getText().toString()));
                        break;
                    case 4:
                        pokemon.setSpDefIV(Integer.parseInt(edit.getText().toString()));
                        break;
                    case 5:
                        pokemon.setSpDefIV(Integer.parseInt(edit.getText().toString()));
                        break;
                }

                if (attacker) {
                    setAttacker(pokemon);
                } else {
                    setDefender(pokemon);
                }

                calculateDamage(attacker, 1);
            }
        });
    }

    private void calculateDamage(boolean bol, int moveIndex) {
        // Fail-save
        if (getAttacker() == null || getDefender() == null) {
            return;
        }
        Pokemon attacker = bol ? getAttacker() : getDefender();
        Pokemon defender = bol ? getDefender() : getAttacker();

        setupMetricsDisplay(bol);
        int minDamage = calculateDamageRoutine(bol, moveIndex, 0.85, false);
        double minDamagePercent = (double) minDamage / defender.calculateHP();

        int maxDamage = calculateDamageRoutine(bol, moveIndex, 1.0, false);
        double maxDamagePercent = (double) maxDamage / defender.calculateHP();

        String minDamageText = DAMAGE_FORMAT.format(minDamagePercent);
        String maxDamageText = DAMAGE_FORMAT.format(maxDamagePercent);

        int defenderHP = calculateInitialHP(bol);
        int damagePerRound = calculateDamagePerRound(bol);

        int maxHitsTilKo = minDamage == 0 ? 0 : (int) Math.ceil((double) defenderHP / (minDamage + damagePerRound));
        int minHitsTilKo = maxDamage == 0 ? 0 : (int) Math.ceil((double) defenderHP / (maxDamage + damagePerRound));

        String percentage = minDamageText + " - " + maxDamageText;
        String message = "";
        if (minDamage == maxDamage && minDamage == 0) {
            message = String.format(" lol ");
        } else if (minDamageText.equals(maxDamageText) && minHitsTilKo > 0) {
            message = getResources().getString(R.string.dmg_is_same, "" + minHitsTilKo);
        } else if (minDamageText.equals(maxDamageText)) {
            message = getResources().getString(R.string.dmg_is_same_outhealed);
        } else if (maxHitsTilKo == minHitsTilKo && minHitsTilKo > 0) {
            message = getResources().getString(R.string.dmg_not_same_same_ko, "" + minHitsTilKo);
        } else if (minHitsTilKo > 0 && maxHitsTilKo <= 0) {
            message = getResources().getString(R.string.dmg_not_same_not_same__maybe_outhealed, "" + minHitsTilKo);
        } else if (minHitsTilKo <= 0 && maxHitsTilKo <= 0) {
            message = getResources().getString(R.string.dmg_not_same_not_same_outhealed);
        } else {
            message = getResources().getString(R.string.dmg_not_same_not_same_ko, "" + minHitsTilKo, "" + maxHitsTilKo);
        }

        //TODO the "to-do's" in this section just helps better visualize the chaos, so everything is ok
        String damageText = "&boost &EV&kNature &atkType &item &ability &attackerName &helping &attackName vs. &HPEV HP / &DefEV&DefNature &DefType &DefAbility &defenderName &condition (&percentage) -- &message";
        try {
            //TODO here we set the boosts on attacking stats, set the EV's and the type of attack (Special / Physical) both for attacker and defender
            if (MoveDex.get(getContext()).getMoveJsonObject(attacker.getMove(moveIndex).toLowerCase()).getString("category").equals("Special")) {
                //set boosts
                damageText = damageText.replace("&boost", "");

                //TODO offensive
                damageText = damageText.replace("&EV", "" + attacker.getSpAtkEV());
                damageText = damageText.replace("&atkType ", "SpA");
                if (attacker.getNature().equals("Adamant") || attacker.getNature().equals("Careful") || attacker.getNature().equals("Impish") || attacker.getNature().equals("Jolly")) {
                    damageText = damageText.replace("&kNature", "-");
                } else if (attacker.getNature().equals("Mild") || attacker.getNature().equals("Modest") || attacker.getNature().equals("Quiet") || attacker.getNature().equals("Rash")) {
                    damageText = damageText.replace("&kNature", "+");
                } else {
                    damageText = damageText.replace("&kNature", "");
                }

                //TODO defensive
                damageText = damageText.replace("&HPEV", "" + defender.getHPEV());
                damageText = damageText.replace("&DefEV", "" + defender.getDefEV());
                damageText = damageText.replace("&DefType ", "SpD");
                if (defender.getNature().equals("Lax") || defender.getNature().equals("Naive") || defender.getNature().equals("Naughty") || defender.getNature().equals("Rash")) {
                    damageText = damageText.replace("&DefNature", "-");
                } else if (defender.getNature().equals("Calm") || defender.getNature().equals("Careful") || defender.getNature().equals("Gentle") || defender.getNature().equals("Sassy")) {
                    damageText = damageText.replace("&DefNature", "+");
                } else {
                    damageText = damageText.replace("&DefNature", "");
                }
            } else if (MoveDex.get(getContext()).getMoveJsonObject(attacker.getMove(moveIndex).toLowerCase()).getString("category").equals("Physical")) {
                //set boosts
                damageText = damageText.replace("&boost", "");

                damageText = damageText.replace("&EV", "" + attacker.getAtkEV());
                damageText = damageText.replace("&atkType ", "Atk");
                if (attacker.getNature().equals("Bold") || attacker.getNature().equals("Calm") || attacker.getNature().equals("Modest") || attacker.getNature().equals("Timid")) {
                    damageText = damageText.replace("&kNature", "-");
                } else if (attacker.getNature().equals("Adamant") || attacker.getNature().equals("Brave") || attacker.getNature().equals("Lonely") || attacker.getNature().equals("Naughty")) {
                    damageText = damageText.replace("&kNature", "+");
                } else {
                    damageText = damageText.replace("&kNature", "");
                }

                //TODO defensive
                damageText = damageText.replace("&HPEV", "" + defender.getHPEV());
                damageText = damageText.replace("&DefEV", "" + defender.getDefEV());
                damageText = damageText.replace("&DefType ", "Def");
                if (defender.getNature().equals("Hasty") || defender.getNature().equals("Gentle") || defender.getNature().equals("Mild") || defender.getNature().equals("Lonely")) {
                    damageText = damageText.replace("&DefNature", "-");
                } else if (defender.getNature().equals("Bold") || defender.getNature().equals("Lax") || defender.getNature().equals("Relaxed") || defender.getNature().equals("Impish")) {
                    damageText = damageText.replace("&DefNature", "+");
                } else {
                    damageText = damageText.replace("&DefNature", "");
                }
            }

            if (!attacker.getItem().isEmpty()) {
                damageText = damageText.replace("&item", " " + ItemDex.get(getContext()).getItemName(attacker.getItem()));
            } else {
                damageText = damageText.replace("&item", "");
            }

            JSONObject move = MoveDex.get(getContext()).getMoveJsonObject(attacker.getMove(moveIndex).toLowerCase());
            Log.d(DTAG, move.toString());
            //TODO add special (offensive) effects abilities if necessary
            if (attacker.getAbility(getContext()).equals("Mold Breaker") || attacker.getAbility(getContext()).equals("Teravolt") || attacker.getAbility(getContext()).equals("Turboblaze")
                    || (move.getString("category").equals("Physical") && (attacker.getAbility(getContext()).equals("Pure Power") || attacker.getAbility(getContext()).equals("Huge Power")))
                    || attacker.getAbility(getContext()).equals("Pure Power") || (Integer.parseInt(move.getString("basePower")) <= 60 && attacker.getAbility(getContext()).equals("Technician"))
                    || (move.getString("category").equals("Physical") && attacker.getAbility(getContext()).equals("Tough Claws")) || attacker.getAbility(getContext()).contains("Pixilate")
                    || attacker.getAbility(getContext()).contains("Refrigerate") || attacker.getAbility(getContext()).contains("Aerilate") || attacker.getAbility(getContext()).contains("Analytic")
                    || (move.toString().contains("recoil") && attacker.getAbility(getContext()).contains("Reckless")) || (attacker.getAbility(getContext()).contains("Aura Break") &&
                    defender.getAbility(getContext()).equals("Fairy Aura") || defender.getAbility(getContext()).equals("Dark Aura")) || attacker.getAbility(getContext()).contains("Stance Change")
                    || attacker.getAbility(getContext()).equals("Parental Bond") || (!move.toString().substring(move.toString().indexOf("secondary") + 19).contains("false")
                    && attacker.getAbility(getContext()).equals("Sheer Force") || ((attacker.getType().length == 1 && move.getString("type").equals(attacker.getType()[0]))
                    || (attacker.getType().length == 2 && (attacker.getType()[0].equals(move.getString("type")) || attacker.getType()[1].equals(move.getString("type"))))
                    && attacker.getAbility(getContext()).equals("Adaptability")) || (move.getString("type").equals("fairy") && attacker.getAbility(getContext()).equals("Fairy Aura")
                    || defender.getAbility(getContext()).equals("Fairy Aura")) || (move.getString("type").equals("dark") && attacker.getAbility(getContext()).equals("Dark Aura")
                    || defender.getAbility(getContext()).equals("Dark Aura")) || (mActiveWeather == Weather.SAND && attacker.getAbility(getContext()).equals("Sand Force"))
                    || (move.getString("name").contains("Aura") || move.getString("name").contains("Pulse") && attacker.getAbility(getContext()).contains("Mega Launcher"))
                    || /*( guts condition) ||*/ (move.getString("name").contains("punch") && attacker.getAbility(getContext()).contains("Iron Fist"))
                    || (mActiveWeather == Weather.SUN && attacker.getAbility(getContext()).equals("Solar Power")) || (move.getString("name").contains("Bite") || move.getString("name")
                    .contains("Fang") || move.getString("name").contains("Crunch") && attacker.getAbility(getContext()).equals("Strong Jaw")) || attacker.getAbility(getContext()).equals("Tinted Lens")
                    || (attacker.getHP() < (attacker.getHP() / 2) && attacker.getAbility(getContext()).equals("Defeatist")))
                    ) {
                damageText = damageText.replace("&ability", attacker.getAbility(getContext()));
            } else {
                damageText = damageText.replace("&ability", "");
            }

            damageText = damageText.replace("&attackerName", attacker.getName());

            //TODO check Helping Hand boost
            if (mHelpingHandActive) {
                damageText = damageText.replace("&helping", "Helping Hand");
            } else {
                damageText = damageText.replace("&helping", "");
            }
            damageText = damageText.replace("&attackName", MoveDex.get(getContext()).getMoveJsonObject(attacker.getMove(moveIndex)).getString("name"));

            if ((defender.getAbility(getContext()).equals("Desolate Land") && move.getString("type").equals("Water")) || (defender.getAbility(getContext()).equals("Primordial Sea") &&
                    move.getString("type").equals("Fire")) || (defender.getHP() < defenderHP) && defender.getAbility(getContext()).equals("Multiscale") || (mActiveWeather != Weather.NO_WEATHER)
                    && defender.getAbility(getContext()).equals("Air Lock") || defender.getAbility(getContext()).equals("Thick Fat") || (move.getString("type").equals("Grass") && defender.getAbility(getContext())
                    .equals("Sap Sipper")) || defender.getAbility(getContext()).equals("Poison Heal") || defender.getAbility(getContext()).equals("Magic Guard") || defender.getAbility(getContext()).equals("Iron Barbs")
                    || defender.getAbility(getContext()).equals("Rough Skin") || (move.getString("type").equals("Ground") && defender.getAbility(getContext()).equals("Levitate"))
                    || (move.getString("type").equals("Water") && defender.getAbility(getContext()).equals("Water Absorb")) | (move.getString("type").equals("Electric") && defender.getAbility(getContext()).equals("Volt Absorb"))
                    || (move.getString("type").equals("Fire") && defender.getAbility(getContext()).equals("Flash Fire")) || (move.getString("name").contains("Ball") || move.getString("name")
                    .contains("Bullet") && defender.getAbility(getContext()).equals("Bulletproof")) || (mActiveWeather != Weather.NO_WEATHER && defender.getAbility(getContext()).equals("Overcoat"))
                    || (move.getString("type").equals("Water") || (move.getString("type").equals("Fire") && defender.getAbility(getContext()).equals("Dry Skin")))
                    || defender.getAbility(getContext()).equals("Solid Rock") || defender.getAbility(getContext()).equals("Fur Coat")) {
                damageText = damageText.replace("&DefAbility", " " + defender.getAbility(getContext()));
            } else {
                damageText = damageText.replace("&DefAbility", "");
            }

            damageText = damageText.replace("&defenderName", defender.getName());

            ArrayList<String> conditions = new ArrayList<>();
            String weather = "";
            if (mActiveWeather != Weather.NO_WEATHER) {
                if (mActiveWeather == Weather.SUN) {
                    weather = ("in Sun ");
                } else if (mActiveWeather == Weather.SAND) {
                    weather = ("in Sand ");
                } else if (mActiveWeather == Weather.HAIL) {
                    weather = ("in Hail ");
                } else if (mActiveWeather == Weather.RAIN) {
                    weather = ("in Rain ");
                } else if (mActiveWeather == Weather.HARSH_SUNSHINE) {
                    weather = ("in Harsh Sunshine ");
                } else if (mActiveWeather == Weather.HEAVY_RAIN) {
                    weather = ("in Heavy Rain ");
                } else if (mActiveWeather == Weather.STRONG_WINDS) {
                    weather = ("in Strong Winds ");
                }
            }

            if (mStealthRocksActive || mSpikesCount > 0 || mReflectActive || mLightScreenActive) {
                conditions.add("with ");
            }

            if (mStealthRocksActive) {
                conditions.add("S. Rock");
            }

            if (mSpikesCount > 0) {
                conditions.add(mSpikesCount + " layers of Spikes");
            }

//            if (mCritsActive) {
//                conditions.add("in a Crit.");
//            }

            if (move.getString("type").equals("Physical") && mReflectActive) {
                conditions.add("Reflect");
            }

            if (move.getString("type").equals("Special") && mLightScreenActive) {
                conditions.add("Light Screen");
            }

            String condMessage = weather;
            int i = 0;
            for (String s : conditions) {
                if (s.equals("with ")) {
                    condMessage += s;
                    i++;
                    continue;
                }

                condMessage += s;
                if (i < conditions.size() - 1) {
                    condMessage += ", ";
                } else if (i == conditions.size()) {
                    condMessage += " and ";
                }
                i++;
            }

            damageText = damageText.replace("&condition", condMessage);
            damageText = damageText.replace("&percentage", percentage);
            damageText = damageText.replace("&message", message);
        } catch (Exception ex) {
            Log.e(DTAG, "" + ex.getMessage());
        }
        ((TextView) mView.findViewById(R.id.dmgcalc_damage_label)).setText(damageText);
    }


    private int calculateInitialHP(boolean attacker) {
        Pokemon defender = attacker ? getDefender() : getAttacker();
        int baseHP = "Shedinja".equals(defender.getName()) ? 1 : defender.calculateHP();
        int calculatedHP = defender.getHP();
        List<String> typing = Arrays.asList(getRealTyping(defender));

        if (mStealthRocksActive && !defender.getAbility(getContext()).equals("Magic Guard")) {
            calculatedHP -= baseHP * (0.125 * calculateWeaknessModifier(attacker, "stealthrock", "Rock"));
        }

        if (mSpikesCount > 0 && !defender.getAbility(getContext()).equals("Magic Guard") && !typing.contains("Flying") && !(defender.getAbility(getContext()).equals("Levitate") && !isMoldBreakerActive(attacker))) {
            calculatedHP -= baseHP * (mSpikesCount == 1 ? 1.0 / 8 : mSpikesCount == 2 ? 1.0 / 6 : 1.0 / 4);
        }

        return calculatedHP;
    }

    private double getHPFraction(Pokemon pokemon) {
        return pokemon.getHP() / (double) pokemon.calculateHP();
    }

    private int calculateDamagePerRound(boolean attacker) {
        Pokemon pokemon = attacker ? getAttacker() : getDefender();
        int baseHP = pokemon.calculateHP();
        int damagePerRound = 0;
        String defenderAbility = pokemon.getAbility(getContext());
        List<String> defenderTypes = Arrays.asList(getRealTyping(getDefender()));

        // Weather damage
        if ((defenderAbility.equals("Solar Power") || defenderAbility.equals("Dry Skin")) && mActiveWeather == Weather.SUN) {
            damagePerRound += (baseHP * 0.125);
        }

        if (!defenderAbility.equals("Overcoat") && !defenderAbility.equals("Magic Guard") && !getDefender().getItem().equals("safetygoggles")) {
            if (mActiveWeather == Weather.SAND && !defenderAbility.equals("Sand Veil") && !defenderAbility.equals("Sand Rush") && !defenderAbility.equals("Sand Force") && !defenderTypes.contains("Rock") && !defenderTypes.contains("Steel") && !defenderTypes.contains("Ground")) {
                damagePerRound += (baseHP / 16);
            } else if (mActiveWeather == Weather.HAIL && !defenderAbility.equals("Ice Body") && !defenderAbility.equals("Snow Cloak") && !defenderTypes.contains("Ice")) {
                damagePerRound += (baseHP / 16);
            }
        }

        // Weather Healing
        if (defenderAbility.equals("Dry Skin") && mActiveWeather == Weather.RAIN) {
            damagePerRound -= (baseHP * 0.125);
        }

        if ((defenderAbility.equals("Rain Dish")) && mActiveWeather == Weather.RAIN) {
            damagePerRound -= (baseHP / 16);
        }

        if ((defenderAbility.equals("Ice Body")) && mActiveWeather == Weather.HAIL) {
            damagePerRound -= (baseHP / 16);
        }

        //Items
        if (getDefender().getItem().equals("leftovers")) {
            damagePerRound -= (baseHP / 16);
        }

        if (getDefender().getItem().equals("stickybarb") && !getDefender().getAbility(getContext()).equals("Magic Guard")) {
            damagePerRound += baseHP * (0.125);
        }

        return damagePerRound;
    }

    private int calculateDamageRoutine(boolean bol, int moveIndex, double luck, boolean crit) {
        String move = null;
        String type = null;
        String basePower = null;
        String category = null;
        String targets = null;
        boolean hasSecondary = false;
        Pokemon attacker = bol ? getAttacker() : getDefender();
        Pokemon defender = bol ? getDefender() : getAttacker();

        move = attacker.getMove(moveIndex);

        try {
            JSONObject moveJson = MoveDex.get(getContext()).getMoveJsonObject(move);
            type = moveJson.getString("type");
            basePower = moveJson.getString("basePower");
            category = moveJson.getString("category");
            targets = moveJson.getString("target");
            hasSecondary = moveJson.optBoolean("secondary", true);

        } catch (JSONException | NullPointerException e) {
            return 0;
        }

        if ("Status".equals(category)) {
            return 0;
        } else {
            switch (move) {
                case "dragonrage":
                    return calculateWeaknessModifier(bol, move, type) == 0 ? 0 : 40;
                case "sonicboom":
                    return calculateWeaknessModifier(bol, move, type) == 0 ? 0 : 20;
                case "seismictoss":
                case "nightshade":
                    return calculateWeaknessModifier(bol, move, type) == 0 ? 0 : getAttacker().getLevel();
                default:
                    boolean usesAttack = "Physical".equals(category);
                    boolean usesDefense = "Physical".equals(category) || "psyshock".equals(move) || "psystrike".equals(move) || "secretsword".equals(move);

                    double attack = "foulplay".equals(move) ? Math.round(defender.calculateAtk() * getAttackMultiplier(attacker)) : usesAttack ? Math.round(attacker.calculateAtk() * getAttackMultiplier(attacker)) : attacker.calculateSpAtk() * getSpecialAttackMultiplier(attacker);
                    double defense = usesDefense ? defender.calculateDef() * getDefenseMultiplier(defender) : defender.calculateSpDef() * getSpecialDefenseMultiplier(defender);
                    double base = calculateBasePower(bol, move, type, Double.parseDouble(basePower));

                    String originalType = type;
                    type = modifyAttackType(move, type);

                    boolean isStab = Arrays.asList(getTypingAfterAbilities(bol, type)).contains(type);

                    List<Double> modifiers = new ArrayList<>();
                    modifiers.add(luck);
                    modifiers.add(isStab ? 1.5 : 1.0);
                    modifiers.add(calculateWeaknessModifier(bol, move, type));
                    modifiers.add(calculateCritMultiplier(bol, move, crit));
                    modifiers.add(mHelpingHandActive ? 1.5 : 1.0);
                    modifiers.add(getSpreadMultiplicator(targets));
                    modifyDamageWithAbility(bol, modifiers, move, type, originalType, category, usesDefense, hasSecondary);
                    modifyDamageWithItem(modifiers, move, type, originalType, category);
                    base = base == 0.0 ? 0 : Math.floor(((2 * attacker.getLevel() / 5 + 2) * attack * base / defense) / 50 + 2);
                    return applyDamageModifiers(base, modifiers);
            }
        }
    }

    private String modifyAttackType(String move, String type) {
        String attackerAbility = getAttacker().getAbility(getContext());
        if ("naturalgift".equals(move)) {
            JSONObject itemObject = ItemDex.get(getContext()).getItemJsonObject(getAttacker().getItem());
            JSONObject naturalGiftObject = itemObject == null ? null : itemObject.optJSONObject("naturalGift");
            if (naturalGiftObject != null) {
                type = naturalGiftObject.optString("type", type);
            }
        } else if ("weatherball".equals(move)) {
            switch (mActiveWeather) {
                case HAIL:
                    type = "Ice";
                    break;
                case SAND:
                    type = "Rock";
                    break;
                case RAIN:
                    type = "Water";
                    break;
                case SUN:
                    type = "Fire";
                    break;
            }
        } else if (attackerAbility.equals("Aerilate") && type.equals("Normal")) {
            type = "Flying";
        } else if (attackerAbility.equals("Refrigerate") && type.equals("Normal")) {
            type = "Ice";
        } else if (attackerAbility.equals("Pixilate") && type.equals("Normal")) {
            type = "Fairy";
        } else if (attackerAbility.equals("Normalize")) {
            type = "Normal";
        } else if ("technoblast".equals(move)) {
            JSONObject itemObject = ItemDex.get(getContext()).getItemJsonObject(getAttacker().getItem());
            type = itemObject == null ? null : itemObject.optString("onDrive", type);
        } else if ("judgement".equals(move)) {
            JSONObject itemObject = ItemDex.get(getContext()).getItemJsonObject(getAttacker().getItem());
            type = itemObject == null ? "Normal" : itemObject.optString("onPlate", "Normal");
        }
        return type;
    }

    private int applyDamageModifiers(double base, List<Double> modifiers) {
        for (double modifier : modifiers) {
            base = base % 1 > 0.5 ? Math.ceil(base * modifier) : Math.floor(base * modifier);
        }
        return (int) base;
    }

    private double getAttackMultiplier(Pokemon pokemon) {
        double baseMultiplier = 1.0;
        if (pokemon.getAbility(getContext()).equals("Huge Power") || pokemon.getAbility(getContext()).equals("Pure Power")) {
            baseMultiplier *= 2.0;
        }

        if (pokemon.getAbility(getContext()).equals("Hustle")) {
            baseMultiplier *= 1.5;
        }

        if (pokemon.getAbility(getContext()).equals("Flower Gift") && mActiveWeather == Weather.SUN) {
            baseMultiplier *= 1.5;
        }

        if ("Defeatist".equals(pokemon.getAbility(getContext()))) {
            baseMultiplier *= 0.5;
        }

        if ("choiceband".equals(pokemon.getItem())) {
            baseMultiplier *= 1.5;
        }

        if ("Pikachu".equals(pokemon.getName()) && "lightball".equals(pokemon.getItem())) {
            baseMultiplier *= 2.0;
        }

        if (("Cubone".equals(pokemon.getName()) || "Marowak".equals(pokemon.getName())) && "thickclub".equals(pokemon.getItem())) {
            baseMultiplier *= 1.5;
        }

        return baseMultiplier;
    }

    private double getSpecialAttackMultiplier(Pokemon pokemon) {
        double baseMultiplier = 1.0;

        if (mActiveWeather == Weather.SUN && pokemon.getAbility(getContext()).equals("Solar Power")) {
            baseMultiplier *= 1.5;
        }

        if ("Defeatist".equals(pokemon.getAbility(getContext()))) {
            baseMultiplier *= 0.5;
        }

        if ("choicespecs".equals(pokemon.getItem())) {
            baseMultiplier *= 1.5;
        }

        if ("Clamperl".equals(pokemon.getName()) && "deepseatooth".equals(pokemon.getItem())) {
            baseMultiplier *= 2.0;
        }

        if ("Pikachu".equals(pokemon.getName()) && "lightball".equals(pokemon.getItem())) {
            baseMultiplier *= 2.0;
        }

        if (("Latios".equals(pokemon.getName()) || "Latias".equals(pokemon.getName())) && "souldew".equals(pokemon.getItem())) {
            baseMultiplier *= 1.5;
        }

        return baseMultiplier;
    }

    private double getDefenseMultiplier(Pokemon pokemon) {
        double baseMultiplier = 1.0;

        if ("eviolite".equals(pokemon.getItem()) && Pokedex.get(getContext()).getPokemonJSONObject(pokemon.getName()).has("evos")) {
            baseMultiplier *= 1.5;
        }

        return baseMultiplier;
    }

    private double getSpecialDefenseMultiplier(Pokemon pokemon) {
        double baseMultiplier = 1.0;

        if (pokemon.getAbility(getContext()).equals("Flower Gift") && mActiveWeather == Weather.SUN) {
            baseMultiplier *= 1.5;
        }

        if (Arrays.asList(pokemon.getType()).contains("Rock")) {
            baseMultiplier *= 1.5;
        }

        if ("assaultvest".equals(pokemon.getItem())) {
            baseMultiplier *= 1.5;
        }

        if ("Clamperl".equals(pokemon.getName()) && "deepseascale".equals(pokemon.getItem())) {
            baseMultiplier *= 2.0;
        }

        if (("Latios".equals(pokemon.getName()) || "Latias".equals(pokemon.getName())) && "souldew".equals(pokemon.getItem())) {
            baseMultiplier *= 1.5;
        }

        if ("eviolite".equals(pokemon.getItem()) && Pokedex.get(getContext()).getPokemonJSONObject(pokemon.getName()).has("evos")) {
            baseMultiplier *= 1.5;
        }

        return baseMultiplier;
    }

    private void modifyDamageWithItem(List<Double> modifiers, String move, String type, String originalType, String category) {
        String attackerItem = getAttacker().getItem();

        if ("lifeorb".equals(attackerItem)) {
            modifiers.add(1.3);
        }

        if ("muscleband".equals(attackerItem) && "Physical".equals(category)) {
            modifiers.add(1.1);
        }

        if ("wiseglasses".equals(attackerItem) && "Special".equals(category)) {
            modifiers.add(1.1);
        }
    }

    private void modifyDamageWithAbility(boolean bol, List<Double> modifiers, String move, String type, String originalType, String category, boolean usesDefense, boolean hasSecondary) {
        Pokemon attacker = bol ? getAttacker() : getDefender();
        Pokemon defender = bol ? getDefender() : getAttacker();
        List<String> attackerTyping = Arrays.asList(getTypingAfterAbilities(bol, type));
        String attackerAbility = attacker.getAbility(getContext());
        String defenderAbility = defender.getAbility(getContext());

        if (defenderAbility.equals("Wonder Guard") && calculateWeaknessModifier(bol, move, type) > 1.0 && !isMoldBreakerActive(bol)) {
            modifiers.add(0.0);
        }

        if (defenderAbility.equals("Heatproof") && type.equals("Fire") && !isMoldBreakerActive(bol)) {
            modifiers.add(0.5);
        }

        // Without the list thingy, this would be unreadable
        if (attackerAbility.equals("Mega Launcher") && Arrays.asList(new String[]{"aurasphere", "darkpulse", "dragonpulse", "waterpulse"}).contains(move)) {
            modifiers.add(1.5);
        }

        if (((attackerAbility.equals("Swarm") && type.equals("Bug")) || (attackerAbility.equals("Blaze") && type.equals("Fire")) || (attackerAbility.equals("Torrent") && type.equals("Water")) || (attackerAbility.equals("Overgrow") && type.equals("Grass"))) && getHPFraction(getAttacker()) <= 1 / 3.0) {
            modifiers.add(1.5);
        }

        if (attackerAbility.equals("Iron Fist") && Arrays.asList(new String[]{"bulletpunch", "cometpunch", "dizzypunch", "drainpunch", "dynamicpunch", "firepunch", "focuspunch", "hammerarm", "icepunch",
                "machpunch", "megapunch", "meteormash", "poweruppunch", "shadowpunch", "skyuppercut", "thunderpunch"}).contains(move)) {
            modifiers.add(1.2);
        }

        if (defenderAbility.equals("Bulletproof") && Arrays.asList(new String[]{"acidspray", "aurasphere", "barrage", "bulletseed", "eggbomb", "electroball", "energyball", "focusblast", "gyroball", "iceball",
                "magnetbomb", "mistball", "mudbomb", "octazooka", "rockwrecker", "searingshot", "seedbomb", "shadowball", "sludgebomb", "weatherball", "zapcannon"}).contains(move) && !isMoldBreakerActive(bol)) {
            modifiers.add(0.0);
        }

        if (defenderAbility.equals("Soundproof") && Arrays.asList(new String[]{"boomburst", "bugbuzz", "chatter", "confide", "disarmingvoice", "echoedvoice", "grasswhistle", "growl", "healbell", "hypervoice",
                "metalsound", "nobleroar", "relicsong", "round", "snarl", "snore", "uproar"}).contains(move) && !isMoldBreakerActive(bol)) {
            modifiers.add(0.0);
        }

        if (attackerAbility.equals("Reckless") && Arrays.asList(new String[]{"bravebird", "doubleedge", "flareblitz", "headcharge", "headsmash", "highjumpkick", "jumpkick", "submission", "takedown",
                "volttackle", "woodhammer", "wildcharge"}).contains(move)) {
            modifiers.add(1.2);
        }

        if (attackerAbility.equals("Strong Jaw") && Arrays.asList(new String[]{"bite", "crunch", "firefang", "icefang", "poisonfang", "thunderfang"}).contains(move)) {
            modifiers.add(1.5);
        }

        if (attackerAbility.equals("Tough Claws") && (Arrays.asList(new String[]{"drainingkiss", "finalgambit", "grassknot", "infestation", "petaldance", "trumpcard", "wringout"}).contains(move)
                || ("Physical".equals(category) && !Arrays.asList("attackorder", "barrage", "beatup", "bonemerang", "boneclub", "bonerush", "bulldoze", "bulletseed", "earthquake", "eggbomb", "explosion", "feint", "fling",
                "freezeshock", "fusionbolt", "geargrind", "gunkshot", "iceshard", "iciclecrash", "iciclespear", "magnetbomb", "magnitude", "metalburst", "naturalgift", "payday", "poisonsting", "pinmissile", "present",
                "psychocut", "razorleaf", "rockblast", "rockslide", "rockthrow", "rocktomb", "rockwrecker", "sacredfire", "sandtomb", "secretpower", "seedbomb", "selfdestruct", "skyattack", "spikecannon", "smackdown",
                "stoneedge", "twineedle").contains(move)))) {
            modifiers.add(4.0 / 3);
        }

        // Just a hack. This is probably not reliable with some moves. Need to reevaluate
        if (attackerAbility.equals("Parental Bond")) {
            modifiers.add(1.5);
        }

        if (attackerAbility.equals("Sheer Force") && hasSecondary) {
            modifiers.add(1.3);
        }

        if (((attackerAbility.equals("Fairy Aura") && defenderAbility.equals("Aura Break")) || (attackerAbility.equals("Aura Break") && defenderAbility.equals("Fairy Aura"))) &&
                type.equals("Fairy")) {
            modifiers.add(2.0 / 3);
        } else if ((attackerAbility.equals("Fairy Aura") || defenderAbility.equals("Fairy Aura")) && type.equals("Fairy")) {
            modifiers.add(4.0 / 3);
        }

        if (((attackerAbility.equals("Dark Aura") && defenderAbility.equals("Aura Break")) || (attackerAbility.equals("Aura Break") && defenderAbility.equals("Dark Aura"))) &&
                type.equals("Dark")) {
            modifiers.add(2.0 / 3);
        } else if ((attackerAbility.equals("Dark Aura") || defenderAbility.equals("Dark Aura")) && type.equals("Dark")) {
            modifiers.add(4.0 / 3);
        }

        if (attackerAbility.equals("Analytic")) {
            modifiers.add(1.3); //TODO: Reimplement
        }

        if (attackerAbility.equals("Aerilate") && originalType.equals("Normal")) {
            modifiers.add(1.3);
        }

        if (attackerAbility.equals("Refrigerate") && originalType.equals("Normal")) {
            modifiers.add(1.3);
        }

        if (attackerAbility.equals("Pixilate") && originalType.equals("Normal")) {
            modifiers.add(1.3);
        }

        if (defenderAbility.equals("Fur Coat") && usesDefense && !isMoldBreakerActive(bol)) {
            modifiers.add(0.5);
        }

        if (mActiveWeather == Weather.SAND && attackerAbility.equals("Sand Force") && Arrays.asList(new String[]{"Rock", "Ground", "Steel"}).contains(type)) {
            modifiers.add(1.3);
        }

        // Screens
        if ("Physical".equals(category) && mReflectActive && !attackerAbility.equals("Infiltrator")) {
            if (mIsSingles) {
                modifiers.add(0.5);
            } else {
                modifiers.add(2.0 / 3);
            }
        }

        if ("Special".equals(category) && mLightScreenActive && !attackerAbility.equals("Infiltrator")) {
            if (mIsSingles) {
                modifiers.add(0.5);
            } else {
                modifiers.add(2.0 / 3);
            }
        }
    }

    private boolean isMoldBreakerActive(boolean bol) {
        Pokemon attacker = bol ? getAttacker() : getDefender();
        return attacker.getAbility(getContext()).equals("Mold Breaker") || attacker.getAbility(getContext()).equals("Teravolt") || attacker.getAbility(getContext()).equals("Turboblaze");
    }

    private double calculateCritMultiplier(boolean bol, String move, boolean crit) {
        Pokemon attacker = bol ? getAttacker() : getDefender();
        Pokemon defender = bol ? getDefender() : getAttacker();
        double baseMultiplier = attacker.getAbility(getContext()).equals("Sniper") ? 2.25 : 1.5;
        double modifier = crit ? baseMultiplier : 1.0;

        switch (move) {
            case "stormthrow":
            case "frostbreath":
                modifier = baseMultiplier;
        }

        switch (defender.getAbility(getContext())) {
            case "Battle Armor":
            case "Shell Armor":
                modifier = isMoldBreakerActive(bol) ? baseMultiplier : 1.0;
        }

        return modifier;
    }

    private double getSpreadMultiplicator(String targets) {
        return ("allAdjacentFoes".equals(targets) || "allAdjacent".equals(targets) || "all".equals(targets)) && !mIsSingles ? 0.75 : 1;
    }

    private String[] getRealTyping(Pokemon pokemon) {
        String[] typing = pokemon.getType();
        if ("Forecast".equals(pokemon.getAbility(getContext()))) {
            switch (mActiveWeather) {
                case RAIN:
                    typing = new String[]{"Water"};
                    break;
                case SUN:
                    typing = new String[]{"Fire"};
                    break;
                case HAIL:
                    typing = new String[]{"Ice"};
                    break;
            }
        } else if ("Multitype".equals(pokemon.getAbility(getContext())) && pokemon.getName().contains("Arceus")) {
            JSONObject itemObject = ItemDex.get(getContext()).getItemJsonObject(getAttacker().getItem());
            typing = itemObject == null ? new String[]{"Normal"} : new String[]{itemObject.optString("onPlate", "Normal")};
        }
        return typing;
    }

    private double calculateWeaknessModifier(boolean bol, String move, String type) {
        double modifier = 1.0;
        Pokemon attacker = bol ? getAttacker() : getDefender();
        Pokemon defender = bol ? getDefender() : getAttacker();

        String[] defenderTyping = getRealTyping(defender);
        String attackerAbility = attacker.getAbility(getContext());
        String defenderAbility = attacker.getAbility(getContext());

        for (String defType : defenderTyping) {
            if (mEffectivenessImmune.get(defType) != null && mEffectivenessImmune.get(defType).contains(type)) {
                if ((attackerAbility.equals("Scrappy") || mForesightActive) && (type.equals("Fighting") || type.equals("Normal"))) {
                    modifier = 1.0;
                } else if (defType.equals("Flying") && mGravityActive) {
                    modifier = 1.0;
                } else if ("ringtarget".equals(defender.getItem())) {
                    modifier = 1.0;
                } else {
                    modifier = 0.0;
                }
            } else if (mEffectivenessWeak.get(defType) != null && mEffectivenessWeak.get(defType).contains(type)) {
                modifier *= 0.5;
            } else if (mEffectivenessStrong.get(defType) != null && mEffectivenessStrong.get(defType).contains(type)) {
                modifier *= 2.0;
            }
        }

        //Flying press. This should also work with normalize and electrify
        if ("flyingpress".equals(move)) {
            String backupType = type;
            type = "Flying";
            for (String defType : defenderTyping) {
                if (mEffectivenessImmune.get(defType) != null && mEffectivenessImmune.get(defType).contains(type)) {
                    modifier = 0.0;
                } else if (mEffectivenessWeak.get(defType) != null && mEffectivenessWeak.get(defType).contains(type)) {
                    modifier *= 0.5;
                } else if (mEffectivenessStrong.get(defType) != null && mEffectivenessStrong.get(defType).contains(type)) {
                    modifier *= 2.0;
                }
            }
            type = backupType;
        }

        if ("freezedry".equals(move) && Arrays.asList(defenderTyping).contains("Water")) {
            modifier *= 4; // as it should be resisted once
        }

        // Immunities by ability
        if (defenderAbility.equals("Flash Fire") && type.equals("Fire") && !isMoldBreakerActive(bol)) {
            modifier *= 0;
            // Gamefreak seems to hate electric typing..
        } else if ((defenderAbility.equals("Motor Drive") || defenderAbility.equals("Lightning Rod") || defenderAbility.equals("Volt Absorb")) && type.equals("Electric") && !isMoldBreakerActive(bol)) {
            modifier *= 0;
        } else if (defenderAbility.equals("Sap Sipper") && type.equals("Grass") && !isMoldBreakerActive(bol)) {
            modifier *= 0;
        } else if ((defenderAbility.equals("Water Absorb") || defenderAbility.equals("Storm Drain")) && type.equals("Water") && !isMoldBreakerActive(bol)) {
            modifier *= 0;
        } else if (defenderAbility.equals("Dry Skin") && type.equals("Water") && !isMoldBreakerActive(bol)) {
            modifier *= 0;
        } else if (defenderAbility.equals("Dry Skin") && type.equals("Fire") && !isMoldBreakerActive(bol)) {
            modifier *= 1.25;
        } else if (defenderAbility.equals("Levitate") && type.equals("Ground") && !isMoldBreakerActive(bol) && !mGravityActive) {
            modifier *= 0;
        } else if ((defenderAbility.equals("Filter") || defenderAbility.equals("Solid Rock")) && modifier > 1.0 && !isMoldBreakerActive(bol)) {
            modifier *= 0.75;
        } else if (defenderAbility.equals("Thick Fat") && (type.equals("Ice") || type.equals("Fire")) && !isMoldBreakerActive(bol)) {
            modifier *= 0.5;
        }

        if ("airballoon".equals(getDefender().getItem()) && "Ground".equals(type)) {
            modifier *= 0;
        }

        if (attackerAbility.equals("Tinted Lens") && modifier < 1.0) {
            modifier *= 2.0;
        } else if ("expertbelt".equals(getAttacker().getItem()) && modifier > 1.0) {
            modifier *= 1.2;
        }

        return modifier;
    }

    private double calculateBasePower(boolean bol, String move, String type, double bp) {
        Pokemon attacker = bol ? getAttacker() : getDefender();
        Pokemon defender = bol ? getDefender() : getAttacker();

        double ratio;
        switch (move) {
            case "avalanche":
            case "revenge":
                bp = 120;
                break;
            case "magnitude":
                bp = 150;
                break;
            case "punishment":
                bp = Math.min(200, 20 * countStatStages(attacker, true) + 60);
                break;
            case "storedpower":
                bp = 20 + (20 * countStatStages(attacker, true));
                break;
            case "knockoff":
                bp = defender.getItem().isEmpty() || defender.getItem() == null ? 65 : 97.5;
                break;
            case "fling":
                JSONObject obj = ItemDex.get(getContext()).getItemJsonObject(attacker.getItem());
                JSONObject flingObject = obj == null ? null : obj.optJSONObject("fling");
                if (flingObject != null) {
                    bp = flingObject.optInt("basePower", 0);
                } else {
                    bp = 0;
                }
                break;
            case "lowkick":
            case "grassknot":
                double weight = calcuateWeight(defender);
                bp = weight < 10.0 ? 20 : weight < 25.0 ? 40 : weight < 50.0 ? 60 : weight < 100.0 ? 80 : weight < 200.0 ? 100 : 120;
                break;
            case "acrobatics":
                bp = attacker.getItem() == null || attacker.getItem().isEmpty() || "flyinggem".equals(attacker.getItem()) ? 55 : 110;
                break;
            case "reversal":
            case "flail":
                int mod = (int) (48 * getHPFraction(attacker));
                bp = mod <= 1 ? 200 : mod <= 4 ? 150 : mod <= 9 ? 100 : mod <= 16 ? 80 : mod <= 32 ? 40 : 20;
                break;
            case "heatcrash":
            case "heavyslam":
                ratio = calcuateWeight(defender) / calcuateWeight(attacker);
                bp = ratio <= 0.2 ? 120 : ratio <= 0.25 ? 100 : ratio <= 1 / 3 ? 80 : ratio <= 0.5 ? 60 : 40;
                break;
            case "crushgrip":
            case "wringout":
                bp = 1 + (120 * getHPFraction(defender));
                break;
            case "brine":
                bp = getHPFraction(defender) <= 0.5 ? 130 : 65;
                break;
            case "eruption":
            case "waterspout":
                bp = Math.max(1, 150 * getHPFraction(attacker));
                break;
            case "frustration":
            case "return":
                bp = 102;
                break;
            case "naturalgift":
                obj = ItemDex.get(getContext()).getItemJsonObject(attacker.getItem());
                JSONObject naturalGiftObject = obj == null ? null : obj.optJSONObject("naturalGift");
                if (naturalGiftObject != null) {
                    bp = naturalGiftObject.optInt("basePower", 0);
                } else {
                    bp = 0;
                }
                break;
            case "gyroball":
                bp = 25.0 * defender.calculateSpd() / attacker.calculateSpd();
                break;
            case "electroball":
                ratio = defender.calculateSpd() / attacker.calculateSpd();
                bp = ratio < 0.25 ? 150 : ratio < 1 / 3 ? 120 : ratio < 0.5 ? 80 : 60;
                break;
            case "weatherball":
                if (mActiveWeather != Weather.NO_WEATHER) {
                    bp = 100;
                }
                break;
        }

        if (attacker.getAbility(getContext()).equals("Technician") && bp <= 60) {
            bp *= 1.5;
        }

        if ((mActiveWeather == Weather.SUN && type.equals("Fire")) || (mActiveWeather == Weather.RAIN && type.equals("Water"))) {
            bp *= 1.5;
        } else if ((mActiveWeather == Weather.SUN && type.equals("Water")) || (mActiveWeather == Weather.RAIN && type.equals("Fire"))) {
            bp *= 0.5;
        }

        if ("solarbeam".equals(move) && (mActiveWeather == Weather.SAND || mActiveWeather == Weather.RAIN || mActiveWeather == Weather.HAIL)) {
            bp *= 0.5;
        }

        // Items
        if (attacker.getName().equals("Dialga") && attacker.getItem().equals("adamantorb") && (type.equals("Dragon") || type.equals("Steel"))) {
            bp *= 1.2;
        } else if (attacker.getName().equals("Palkia") && attacker.getItem().equals("lustrousorb") && (type.equals("Dragon") || type.equals("Water"))) {
            bp *= 1.2;
        } else if ((attacker.getName().equals("Giratina") || attacker.getName().equals("Giratina-Origin")) && attacker.getItem().equals("griseousorb") && (type.equals("Dragon") || type.equals("Ghost"))) {
            bp *= 1.2;
        }

        if (Arrays.asList(new String[]{"dracoplate", "dragonfang"}).contains(attacker.getItem()) && "Dragon".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"earthplate", "softsand"}).contains(attacker.getItem()) && "Ground".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"fistplate", "blackbelt"}).contains(attacker.getItem()) && "Fighting".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"flameplate", "charcoal"}).contains(attacker.getItem()) && "Fire".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"icicleplate", "nevermeltice"}).contains(attacker.getItem()) && "Ice".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"insectplate", "silverpowder"}).contains(attacker.getItem()) && "Bug".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"ironplate", "metalcoat"}).contains(attacker.getItem()) && "Steel".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"meadowplate", "miracleseed", "roseincense"}).contains(attacker.getItem()) && "Grass".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"mindplate", "twistedspoon", "oddincense"}).contains(attacker.getItem()) && "Psychic".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"pixieplate"}).contains(attacker.getItem()) && "Fairy".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"dreadplate", "blackglasses"}).contains(attacker.getItem()) && "Dark".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"skyplate", "sharpbeak"}).contains(attacker.getItem()) && "Flying".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"splashplate", "mysticwater", "seaincense", "waveincense"}).contains(attacker.getItem()) && "Water".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"spookyplate", "spelltag"}).contains(attacker.getItem()) && "Ghost".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"stoneplate", "hardstone", "rockincense"}).contains(attacker.getItem()) && "Rock".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"toxicplate", "poisonbarb"}).contains(attacker.getItem()) && "Poison".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"zapplate", "magnet"}).contains(attacker.getItem()) && "Electric".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"silkscarf"}).contains(attacker.getItem()) && "Normal".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"polkadotbow"}).contains(attacker.getItem()) && "Normal".equals(type)) {
            bp *= 1.125;
        } else if (Arrays.asList(new String[]{"pinkbow"}).contains(attacker.getItem()) && "Normal".equals(type)) {
            bp *= 1.1;
        }

        return bp;
    }

    private int countStatStages(Pokemon pokemon, boolean positiveOnly) {
        int count = 0;
        for (int change : pokemon.getStages()) {
            if (change > 6 || !positiveOnly) {
                count += change - 6;
            }
        }
        return count;
    }

    private double calcuateWeight(Pokemon pokemon) {
        double normalWeight = pokemon.getWeight();

        if ("floatstone".equals(pokemon.getItem())) {
            normalWeight *= 0.5;
        }

        if ("Light Metal".equals(pokemon.getAbility(getContext()))) {
            normalWeight *= 0.5;
        } else if ("Heavy Metal".equals(pokemon.getAbility(getContext()))) {
            normalWeight *= 2.0;
        }

        return normalWeight;
    }

    private void createIndexOfTypeModifiers() {
        // Normal Type
        mEffectivenessStrong.put("Normal", Arrays.asList("Fighting"));
        mEffectivenessImmune.put("Normal", Arrays.asList("Ghost"));

        // Steel Type
        mEffectivenessStrong.put("Steel", Arrays.asList("Fighting", "Fire", "Ground"));
        mEffectivenessWeak.put("Steel", Arrays.asList("Bug", "Dragon", "Flying", "Fairy", "Grass",
                "Ice", "Normal", "Psychic", "Rock", "Steel"));
        mEffectivenessImmune.put("Steel", Arrays.asList("Poison"));

        // Fighting Type
        mEffectivenessStrong.put("Fighting", Arrays.asList("Fairy", "Flying", "Psychic"));
        mEffectivenessWeak.put("Fighting", Arrays.asList("Bug", "Dark", "Rock"));

        // Grass Type
        mEffectivenessStrong.put("Grass", Arrays.asList("Bug", "Fire", "Flying", "Ice", "Poison"));
        mEffectivenessWeak.put("Grass", Arrays.asList("Electric", "Grass", "Ground", "Water"));

        // Water Type
        mEffectivenessStrong.put("Water", Arrays.asList("Electric", "Grass"));
        mEffectivenessWeak.put("Water", Arrays.asList("Fire", "Ice", "Steel", "Water"));

        // Electric Type
        mEffectivenessStrong.put("Electric", Arrays.asList("Ground"));
        mEffectivenessWeak.put("Electric", Arrays.asList("Electric", "Flying", "Steel"));

        // Fairy Type
        mEffectivenessStrong.put("Fairy", Arrays.asList("Poison", "Steel"));
        mEffectivenessWeak.put("Fairy", Arrays.asList("Bug", "Dark", "Fighting"));
        mEffectivenessImmune.put("Fairy", Arrays.asList("Dragon"));

        // Dragon Type
        mEffectivenessStrong.put("Dragon", Arrays.asList("Dragon", "Fairy", "Ice"));
        mEffectivenessWeak.put("Dragon", Arrays.asList("Electric", "Fire", "Grass", "Water"));

        // Dark Type
        mEffectivenessStrong.put("Dark", Arrays.asList("Bug", "Fairy", "Fighting"));
        mEffectivenessWeak.put("Dark", Arrays.asList("Dark", "Ghost"));
        mEffectivenessImmune.put("Dark", Arrays.asList("Psychic"));

        // Bug Type
        mEffectivenessStrong.put("Bug", Arrays.asList("Fire", "Flying", "Rock"));
        mEffectivenessWeak.put("Bug", Arrays.asList("Fighting", "Grass", "Ground"));

        // Flying Type
        mEffectivenessStrong.put("Flying", Arrays.asList("Electric", "Ice", "Rock"));
        mEffectivenessWeak.put("Flying", Arrays.asList("Bug", "Fighting", "Grass"));
        mEffectivenessImmune.put("Flying", Arrays.asList("Ground"));

        // Poison Type
        mEffectivenessStrong.put("Poison", Arrays.asList("Ground", "Psychic"));
        mEffectivenessWeak.put("Poison", Arrays.asList("Bug", "Fairy", "Fighting", "Grass", "Poison"));

        // Ice Type
        mEffectivenessStrong.put("Ice", Arrays.asList("Fighting", "Fire", "Rock", "Steel"));
        mEffectivenessWeak.put("Ice", Arrays.asList("Ice"));

        // Psychic Type
        mEffectivenessStrong.put("Psychic", Arrays.asList("Bug", "Dark", "Ghost"));
        mEffectivenessWeak.put("Psychic", Arrays.asList("Fighting", "Psychic"));

        // Ghost Type
        mEffectivenessStrong.put("Ghost", Arrays.asList("Dark", "Ghost"));
        mEffectivenessWeak.put("Ghost", Arrays.asList("Bug", "Poison"));
        mEffectivenessImmune.put("Ghost", Arrays.asList("Fighting", "Normal"));

        // Fire Type
        mEffectivenessStrong.put("Fire", Arrays.asList("Ground", "Rock", "Water"));
        mEffectivenessWeak.put("Fire", Arrays.asList("Bug", "Fairy", "Fire", "Grass", "Ice", "Steel"));
    }

    private void setConditionStatus(FieldConditions conditions, boolean value) {
        switch (conditions) {
            case SINGLES:
                mIsSingles = value;
                break;
            case DOUBLES:
                mIsSingles = !value;
                break;
            case GRAVITY:
                mGravityActive = value;
                break;
            case FORESIGHT:
                mForesightActive = value;
                break;
            case HELPING_HAND:
                mHelpingHandActive = value;
                break;
            case LIGHT_SCREEN:
                mLightScreenActive = value;
                break;
            case REFLECT:
                mReflectActive = value;
                break;
            case ZERO_SPIKES:
                mSpikesCount = 0;
                break;
            case ONE_SPIKES:
                mSpikesCount = 1;
                break;
            case TWO_SPIKES:
                mSpikesCount = 2;
                break;
            case THREE_SPIKES:
                mSpikesCount = 3;
                break;
            case STEALTH_ROCK:
                mStealthRocksActive = value;
                break;
            case NO_WEATHER:
                mActiveWeather = Weather.NO_WEATHER;
                break;
            case SUN:
                mActiveWeather = Weather.SUN;
                break;
            case RAIN:
                mActiveWeather = Weather.RAIN;
                break;
            case SAND:
                mActiveWeather = Weather.SAND;
                break;
            case HAIL:
                mActiveWeather = Weather.HAIL;
                break;
        }
    }

    private String[] getTypingAfterAbilities(boolean bol, String moveType) {
        Pokemon attacker = bol ? getAttacker() : getDefender();
        return attacker.getAbility(getContext()).equals("Protean") ? new String[]{moveType} : getRealTyping(getAttacker());
    }

    public enum FieldConditions {
        SINGLES, DOUBLES, STEALTH_ROCK, ZERO_SPIKES, ONE_SPIKES, TWO_SPIKES, THREE_SPIKES, REFLECT, LIGHT_SCREEN, FORESIGHT, HELPING_HAND, NO_WEATHER, SUN, RAIN, SAND, HAIL, GRAVITY
    }

    private enum Weather {
        NO_WEATHER, SUN, RAIN, SAND, HAIL, HARSH_SUNSHINE, HEAVY_RAIN, STRONG_WINDS
    }
}
