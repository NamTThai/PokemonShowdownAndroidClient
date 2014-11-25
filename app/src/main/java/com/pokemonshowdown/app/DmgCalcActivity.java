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

import org.json.JSONException;
import org.json.JSONObject;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DmgCalcActivity extends FragmentActivity {
    public final static String DTAG = DmgCalcActivity.class.getName();
    public final static int REQUEST_CODE_FIND_ATTACKER = 0;
    public final static int REQUEST_CODE_FIND_DEFENDER = 1;
    public final static int REQUEST_CODE_GET_MOVE_1 = 2;
    public final static int REQUEST_CODE_GET_MOVE_2 = 3;
    public final static int REQUEST_CODE_GET_MOVE_3 = 4;
    public final static int REQUEST_CODE_GET_MOVE_4 = 5;

    private final DecimalFormat DAMAGE_FORMAT = new DecimalFormat("#0.0%");

    private Pokemon mAttacker;
    private Pokemon mDefender;

    private Map<String, List<String>> mEffectivenessStrong = new HashMap<>();
    private Map<String, List<String>> mEffectivenessWeak = new HashMap<>();
    private Map<String, List<String>> mEffectivenessImmune = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dmgcalc);

        DAMAGE_FORMAT.setRoundingMode(RoundingMode.FLOOR);
        createIndexOfTypeModifiers();

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

                calculateDamage(1);
                calculateDamage(2);
                calculateDamage(3);
                calculateDamage(4);
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

                calculateDamage(1);
                calculateDamage(2);
                calculateDamage(3);
                calculateDamage(4);
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
        switch (item.getItemId()) {
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
                    break;
                default:
                    break;
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

    public void setAttacker(Pokemon attacker) {
        mAttacker = attacker;

        TextView textView = (TextView) findViewById(R.id.dmgcalc_attacker);
        textView.setCompoundDrawablesWithIntrinsicBounds(attacker.getIconSmall(), 0, 0, 0);
        textView.setText(attacker.getName());
    }

    public void setAttacker(String attacker) {
        setAttacker(new Pokemon(getApplicationContext(), attacker, true));
    }

    public Pokemon getDefender() {
        return mDefender;
    }

    public void setDefender(Pokemon defender) {
        mDefender = defender;
        TextView textView = (TextView) findViewById(R.id.dmgcalc_defender);
        textView.setCompoundDrawablesWithIntrinsicBounds(defender.getIconSmall(), 0, 0, 0);
        textView.setText(defender.getName());
    }

    public void setDefender(String defender) {
        setDefender(new Pokemon(getApplicationContext(), defender, true));
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
        int minDamage = calculateDamageRoutine(moveIndex, 0.85, false);
        double minDamagePercent = (double) minDamage / getDefender().calculateHP();

        int maxDamage = calculateDamageRoutine(moveIndex, 1.0, false);
        double maxDamagePercent = (double) maxDamage / getDefender().calculateHP();

        String minDamageText = DAMAGE_FORMAT.format(minDamagePercent);
        String maxDamageText = DAMAGE_FORMAT.format(maxDamagePercent);

        int maxHitsTilKo = (int) Math.ceil(1 / minDamagePercent);
        int minHitsTilKo = (int) Math.ceil(1 / maxDamagePercent);

        String damageText;
        if (minDamage == maxDamage && minDamage == 0) {
            damageText = String.format("--");
        } else if (minDamageText.equals(maxDamageText)) {
            damageText = getResources().getString(R.string.dmg_is_same, minDamageText, minHitsTilKo);
        } else if (maxHitsTilKo == minHitsTilKo) {
            damageText = getResources().getString(R.string.dmg_not_same_same_ko, minDamageText, maxDamageText, minHitsTilKo);
        } else {
            damageText = getResources().getString(R.string.dmg_not_same_not_same_ko, minDamageText, maxDamageText, minHitsTilKo, maxHitsTilKo);
        }

        switch (moveIndex) {
            case 1:
                ((TextView) findViewById(R.id.move1_result)).setText(damageText);
                break;
            case 2:
                ((TextView) findViewById(R.id.move2_result)).setText(damageText);
                break;
            case 3:
                ((TextView) findViewById(R.id.move3_result)).setText(damageText);
                break;
            case 4:
                ((TextView) findViewById(R.id.move4_result)).setText(damageText);
                break;
        }
    }

    private int calculateDamageRoutine(int moveIndex, double luck, boolean crit) {
        String move = null;
        String type = null;
        String basePower = null;
        String category = null;
        String targets = null;
        boolean hasSecondary = false;

        switch (moveIndex) {
            case 1:
                move = getAttacker().getMove1();
                break;
            case 2:
                move = getAttacker().getMove2();
                break;
            case 3:
                move = getAttacker().getMove3();
                break;
            case 4:
                move = getAttacker().getMove4();
                break;
        }

        try {
            JSONObject moveJson = MoveDex.getWithApplicationContext(getApplicationContext()).getMoveJsonObject(move);
            type = moveJson.getString("type");
            basePower = moveJson.getString("basePower");
            category = moveJson.getString("category");
            targets = moveJson.getString("target");

            try {
                hasSecondary = moveJson.getBoolean("secondary");
            } catch (JSONException e) {
                // Ignore as it is already handled
            }
            Log.d("TEST", moveJson.toString());
        } catch (JSONException | NullPointerException e) {
            return 0;
        }

        if ("Status".equals(category)) {
            return 0;
        } else if ("dragonrage".equals(move)) {
            return "Fairy".equals(getDefender().getType()[0]) || "Fairy".equals(getDefender().getType()[1]) ? 0 : 40;
        } else if ("sonicboom".equals(move)) {
            return "Ghost".equals(getDefender().getType()[0]) || "Ghost".equals(getDefender().getType()[1]) ? 0 : 200;
        } else if ("seismictoss".equals(move)) {
            return "Ghost".equals(getDefender().getType()[0]) || "Ghost".equals(getDefender().getType()[1]) ? 0 : getAttacker().getLevel();
        } else if ("nightshade".equals(move)) {
            return "Normal".equals(getDefender().getType()[0]) || "Normal".equals(getDefender().getType()[1]) ? 0 : getAttacker().getLevel();
        } else {
            boolean usesAttack = "Physical".equals(category);
            boolean usesDefense = "Physical".equals(category) || "psyshock".equals(move) || "psystrike".equals(move) || "secredsword".equals(move);

            double attack = "foulplay".equals(move) ? Math.round(getDefender().calculateAtk() * getAtkMultiplier()) : usesAttack ? Math.round(getAttacker().calculateAtk() * getAtkMultiplier()) : getAttacker().calculateSpAtk();
            double defense = usesDefense ? getDefender().calculateDef() : getDefender().calculateSpDef();
            double base = calculateBasePower(move, Double.parseDouble(basePower));

            boolean isStab = getAttacker().getType()[0].equals(type) || getAttacker().getType()[1].equals(type);

            //  ((((2 * Level / 5 + 2) * AttackStat * AttackPower / DefenseStat) / 50) + 2) * STAB * Weakness/Resistance * RandomNumber / 100
            double modifier = luck * (isStab ? 1.5 : 1.0) * calculateWeaknessModifier(move, type) * calculateCritMultiplier(move, crit);
            return modifyDamageWithAbility(base == 0.0 ? 0 : (int) (Math.floor(((2 * getAttacker().getLevel() / 5 + 2) * attack * base / defense) / 50 + 2) * modifier), move, type, category, usesDefense, hasSecondary);
        }
    }

    private double getAtkMultiplier() {
        double baseMultiplier = 1.0;
        if (getAttacker().getAbility().equals("Huge Power") || getAttacker().getAbility().equals("Pure Power")) {
            baseMultiplier *= 2.0;
        }

        if (getAttacker().getAbility().equals("Hustle")) {
            baseMultiplier *= 1.5;
        }

        return baseMultiplier;
    }

    private int modifyDamageWithAbility(int damageNow, String move, String type, String category, boolean usesDefense, boolean hasSecondary) {
        if (getDefender().getAbility().equals("Wonder Guard") && calculateWeaknessModifier(move, type) > 1.0 && !isMoldBreakerActive()) {
            damageNow *= 0.0;
        }

        if (getDefender().getAbility().equals("Heatproof") && type.equals("Fire") && !isMoldBreakerActive()) {
            damageNow /= 2;
        }

        // Without the list thingy, this would be unreadable
        if (getAttacker().getAbility().equals("Mega Launcher") && Arrays.asList(new String[]{"aurasphere", "darkpulse", "dragonpulse", "waterpulse"}).contains(move)) {
            damageNow *= 1.5;
        }

        if (getAttacker().getAbility().equals("Iron Fist") && Arrays.asList(new String[]{"bulletpunch", "cometpunch", "dizzypunch", "drainpunch", "dynamicpunch", "firepunch", "focuspunch", "hammerarm", "icepunch",
                "machpunch", "megapunch", "meteormash", "poweruppunch", "shadowpunch", "skyuppercut", "thunderpunch"}).contains(move)) {
            damageNow *= 1.2;
        }

        if (getDefender().getAbility().equals("Bulletproof") && Arrays.asList(new String[]{"acidspray", "aurasphere", "barrage", "bulletseed", "eggbomb", "electroball", "energyball", "focusblast", "gyroball", "iceball",
                "magnetbomb", "mistball", "mudbomb", "octazooka", "rockwrecker", "searingshot", "seedbomb", "shadowball", "sludgebomb", "weatherball", "zapcannon"}).contains(move) && !isMoldBreakerActive()) {
            damageNow = 0;
        }

        if (getDefender().getAbility().equals("Soundproof") && Arrays.asList(new String[]{"boomburst", "bugbuzz", "chatter", "confide", "disarmingvoice", "echoedvoice", "grasswhistle", "growl", "healbell", "hypervoice",
                "metalsound", "nobleroar", "relicsong", "round", "snarl", "snore", "uproar"}).contains(move) && !isMoldBreakerActive()) {
            damageNow = 0;
        }

        if (getAttacker().getAbility().equals("Reckless") && Arrays.asList(new String[]{"bravebird", "doubleedge", "flareblitz", "headcharge", "headsmash", "highjumpkick", "jumpkick", "submission", "takedown",
                "volttackle", "woodhammer", "wildcharge"}).contains(move)) {
            damageNow *= 1.2;
        }

        if (getAttacker().getAbility().equals("Strong Jaw") && Arrays.asList(new String[]{"bite", "crunch", "firefang", "icefang", "poisonfang", "thunderfang"}).contains(move)) {
            damageNow *= 1.5;
        }

        if (getAttacker().getAbility().equals("Tough Claws") && (Arrays.asList(new String[]{"drainingkiss", "finalgambit", "grassknot", "infestation", "petaldance", "trumpcard", "wringout"}).contains(move)
                || ("Physical".equals(category) && !Arrays.asList("attackorder", "barrage", "beatup", "bonemerang", "boneclub", "bonerush", "bulldoze", "bulletseed", "earthquake", "eggbomb", "explosion", "feint", "fling",
                "freezeshock", "fusionbolt", "geargrind", "gunkshot", "iceshard", "iciclecrash", "iciclespear", "magnetbomb", "magnitude", "metalburst", "naturalgift", "payday", "poisonsting", "pinmissile", "present",
                "psychocut", "razorleaf", "rockblast", "rockslide", "rockthrow", "rocktomb", "rockwrecker", "sacredfire", "sandtomb", "secretpower", "seedbomb", "selfdestruct", "skyattack", "spikecannon", "smackdown",
                "stoneedge", "twineedle").contains(move)))) {
            damageNow = (damageNow * 4) / 3;
        }

        // Just a hack. This is probably not reliable with some moves. Need to reevaluate
        if (getAttacker().getAbility().equals("Parental Bond")) {
            damageNow *= 1.5;
        }

        if (getAttacker().getAbility().equals("Sheer Force") && hasSecondary) {
            damageNow *= 1.3;
        }

        if (((getAttacker().getAbility().equals("Fairy Aura") && getDefender().getAbility().equals("Aura Break")) || (getAttacker().getAbility().equals("Aura Break") && getDefender().getAbility().equals("Fairy Aura"))) &&
                type.equals("Fairy")) {
            damageNow = (damageNow * 2) / 3;
        } else if ((getAttacker().getAbility().equals("Fairy Aura") || getAttacker().getAbility().equals("Fairy Aura")) && type.equals("Fairy")) {
            damageNow = (damageNow * 4) / 3;
        }

        if (((getAttacker().getAbility().equals("Dark Aura") && getDefender().getAbility().equals("Aura Break")) || (getAttacker().getAbility().equals("Aura Break") && getDefender().getAbility().equals("Dark Aura"))) &&
                type.equals("Dark")) {
            damageNow = (damageNow * 2) / 3;
        } else if ((getAttacker().getAbility().equals("Dark Aura") || getAttacker().getAbility().equals("Dark Aura")) && type.equals("Dark")) {
            damageNow = (damageNow * 4) / 3;
        }

        if (getAttacker().getAbility().equals("Analytic")) {
            damageNow *= 1.3;
        }

        if (getAttacker().getAbility().equals("Aerilate") && type.equals("Normal")) {
            damageNow *= 1.3;
        }

        if (getAttacker().getAbility().equals("Refrigerate") && type.equals("Normal")) {
            damageNow *= 1.3;
        }

        if (getAttacker().getAbility().equals("Pixilate") && type.equals("Normal")) {
            damageNow *= 1.3;
        }

        if (getDefender().getAbility().equals("Fur Coat") && usesDefense && !isMoldBreakerActive()) {
            damageNow *= 0.5;
        }

        return damageNow;
    }

    private boolean isMoldBreakerActive() {
        return getAttacker().getAbility().equals("Mold Breaker") || getAttacker().getAbility().equals("Teravolt") || getAttacker().getAbility().equals("Turboblaze");
    }

    private double calculateCritMultiplier(String move, boolean crit) {
        double baseMultiplier = getAttacker().getAbility().equals("Sniper") ? 2.25 : 1.5;
        double modifier = crit ? baseMultiplier : 1.0;

        switch (move) {
            case "stormthrow":
            case "frostbreath":
                modifier = baseMultiplier;
        }

        switch (getDefender().getAbility()) {
            case "Battle Armor":
            case "Shell Armor":
                modifier = isMoldBreakerActive() ? baseMultiplier : 1.0;
        }

        return modifier;
    }

    private double calculateWeaknessModifier(String move, String type) {
        double modifier = 1.0;

        if (getAttacker().getAbility().equals("Aerilate") && type.equals("Normal")) {
            type = "Flying";
        } else if (getAttacker().getAbility().equals("Refrigerate") && type.equals("Normal")) {
            type = "Ice";
        } else if (getAttacker().getAbility().equals("Pixilate") && type.equals("Normal")) {
            type = "Fairy";
        } else if (getAttacker().getAbility().equals("Normalize")) {
            type = "Normal";
        }

        for (String defType : getDefender().getType()) {
            if (mEffectivenessImmune.get(defType) != null && mEffectivenessImmune.get(defType).contains(type)) {
                if (getAttacker().getAbility().equals("Scrappy") && (type.equals("Fighting") || type.equals("Normal"))) {
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
            for (String defType : getDefender().getType()) {
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

        if ("freezedry".equals(move) && (getDefender().getType()[0].equals("Water") || getDefender().getType()[1].equals("Water"))) {
            modifier *= 4; // as it should be resisted once
        }

        // Immunities by ability
        if (getDefender().getAbility().equals("Flash Fire") && type.equals("Fire") && !isMoldBreakerActive()) {
            modifier *= 0;
            // Gamefreak seems to hate electric typing..
        } else if ((getDefender().getAbility().equals("Motor Drive") || getDefender().getAbility().equals("Lightning Rod") || getDefender().getAbility().equals("Volt Absorb")) && type.equals("Electric") && !isMoldBreakerActive()) {
            modifier *= 0;
        } else if (getDefender().getAbility().equals("Sap Sipper") && type.equals("Grass") && !isMoldBreakerActive()) {
            modifier *= 0;
        } else if ((getDefender().getAbility().equals("Water Absorb") || getDefender().getAbility().equals("Storm Drain")) && type.equals("Water") && !isMoldBreakerActive()) {
            modifier *= 0;
        } else if (getDefender().getAbility().equals("Dry Skin") && type.equals("Water") && !isMoldBreakerActive()) {
            modifier *= 0;
        } else if (getDefender().getAbility().equals("Dry Skin") && type.equals("Fire") && !isMoldBreakerActive()) {
            modifier *= 1.25;
        } else if (getDefender().getAbility().equals("Levitate") && type.equals("Ground") && !isMoldBreakerActive()) {
            modifier *= 0;
        } else if ((getDefender().getAbility().equals("Filter") || getDefender().getAbility().equals("Solid Rock")) && modifier > 1.0 && !isMoldBreakerActive()) {
            modifier *= 0.75;
        } else if (getDefender().getAbility().equals("Thick Fat") && (type.equals("Ice") || type.equals("Fire")) && !isMoldBreakerActive()) {
            modifier *= 0.5;
        }

        if (getAttacker().getAbility().equals("Tinted Lens") && modifier < 1.0) {
            modifier *= 2.0;
        }

        return modifier;
    }

    private double calculateBasePower(String move, double bp) {
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
                bp = 60;
                break; // TODO: Reimplement if stat changes are supported
            case "fling":
                bp = 130;
                break; // TODO: Reimplement if items are supported
            case "lowkick":
            case "grassknot": // Currently bugged because getWeight is always 0
                int weight = getDefender().getWeight();
                bp = weight < 10 ? 20 : weight < 25 ? 40 : weight < 50 ? 60 : weight < 100 ? 80 : weight < 200 ? 100 : 120;
                break;
            case "reversal":
            case "flail":
                bp = 200; // TODO: Reimplement once health modifiable
                break;
            case "heatcrash":
            case "heavyslam":
                ratio = (double) getDefender().getWeight() / getAttacker().getWeight();
                bp = ratio <= 0.2 ? 120 : ratio <= 0.25 ? 100 : ratio <= 1 / 3 ? 80 : ratio <= 0.5 ? 60 : 40;
                break;
            case "crushgrip":
            case "wringout":
                bp = 121; // TODO: Reimplement once health modifiable
                break;
            case "frustration":
            case "return":
                bp = 102;
                break;
            case "naturalgift":
                bp = 100;// TODO: Reimplement if items are supported
                break;
            case "gyroball":
                bp = 25.0 * getDefender().calculateSpd() / getAttacker().calculateSpd();
                break;
            case "electroball":
                ratio = getDefender().calculateSpd() / getAttacker().calculateSpd();
                bp = ratio < 0.25 ? 150 : ratio < 1 / 3 ? 120 : ratio < 0.5 ? 80 : 60;
                // TODO: Many other moves...
        }

        if (getAttacker().getAbility().equals("Technician") && bp <= 60) {
            bp *= 1.5;
        }

        return bp;
    }


    private void createIndexOfTypeModifiers() {
        // Normal Type
        mEffectivenessStrong.put("Normal", Arrays.asList(new String[]{"Fighting"}));
        mEffectivenessImmune.put("Normal", Arrays.asList(new String[]{"Ghost"}));

        // Steel Type
        mEffectivenessStrong.put("Steel", Arrays.asList(new String[]{"Fighting", "Fire", "Ground"}));
        mEffectivenessWeak.put("Steel", Arrays.asList(new String[]{"Bug", "Dark", "Dragon", "Fairy", "Flying", "Grass",
                "Ice", "Normal", "Psychic", "Rock", "Steel"}));
        mEffectivenessImmune.put("Steel", Arrays.asList(new String[]{"Poison"}));

        // Fighting Type
        mEffectivenessStrong.put("Fighting", Arrays.asList(new String[]{"Fairy", "Flying", "Psychic"}));
        mEffectivenessWeak.put("Fighting", Arrays.asList(new String[]{"Bug", "Dark", "Rock"}));

        // Grass Type
        mEffectivenessStrong.put("Grass", Arrays.asList(new String[]{"Bug", "Fire", "Flying", "Ice", "Poison"}));
        mEffectivenessWeak.put("Grass", Arrays.asList(new String[]{"Electric", "Grass", "Ground", "Water"}));

        // Water Type
        mEffectivenessStrong.put("Water", Arrays.asList(new String[]{"Electric", "Grass"}));
        mEffectivenessWeak.put("Water", Arrays.asList(new String[]{"Fire", "Ice", "Steel", "Water"}));

        // Electric Type
        mEffectivenessStrong.put("Electric", Arrays.asList(new String[]{"Ground"}));
        mEffectivenessWeak.put("Electric", Arrays.asList(new String[]{"Electric", "Flying", "Steel"}));

        // Fairy Type
        mEffectivenessStrong.put("Fairy", Arrays.asList(new String[]{"Poison", "Steel"}));
        mEffectivenessWeak.put("Fairy", Arrays.asList(new String[]{"Bug", "Dark", "Fighting"}));
        mEffectivenessImmune.put("Fairy", Arrays.asList(new String[]{"Dragon"}));

        // Dragon Type
        mEffectivenessStrong.put("Dragon", Arrays.asList(new String[]{"Dragon", "Fairy", "Ice"}));
        mEffectivenessWeak.put("Dragon", Arrays.asList(new String[]{"Electric", "Fire", "Grass", "Water"}));

        // Dark Type
        mEffectivenessStrong.put("Dark", Arrays.asList(new String[]{"Bug", "Fairy", "Fighting"}));
        mEffectivenessWeak.put("Dark", Arrays.asList(new String[]{"Dark", "Ghost"}));
        mEffectivenessImmune.put("Dark", Arrays.asList(new String[]{"Psychic"}));

        // Bug Type
        mEffectivenessStrong.put("Bug", Arrays.asList(new String[]{"Fire", "Flying", "Rock"}));
        mEffectivenessWeak.put("Bug", Arrays.asList(new String[]{"Fighting", "Grass", "Ground"}));

        // Flying Type
        mEffectivenessStrong.put("Flying", Arrays.asList(new String[]{"Electric", "Ice", "Rock"}));
        mEffectivenessWeak.put("Flying", Arrays.asList(new String[]{"Bug", "Fighting", "Grass"}));
        mEffectivenessImmune.put("Flying", Arrays.asList(new String[]{"Ground"}));

        // Poison Type
        mEffectivenessStrong.put("Poison", Arrays.asList(new String[]{"Ground", "Psychic"}));
        mEffectivenessWeak.put("Poison", Arrays.asList(new String[]{"Bug", "Fairy", "Fighting", "Grass", "Poison"}));

        // Ice Type
        mEffectivenessStrong.put("Ice", Arrays.asList(new String[]{"Fighting", "Fire", "Rock", "Steel"}));
        mEffectivenessWeak.put("Ice", Arrays.asList(new String[]{"Ice"}));

        // Psychic Type
        mEffectivenessStrong.put("Psychic", Arrays.asList(new String[]{"Bug", "Dark", "Ghost"}));
        mEffectivenessWeak.put("Psychic", Arrays.asList(new String[]{"Fighting", "Psychic"}));

        // Ghost Type
        mEffectivenessStrong.put("Ghost", Arrays.asList(new String[]{"Dark", "Ghost"}));
        mEffectivenessWeak.put("Ghost", Arrays.asList(new String[]{"Bug", "Poison"}));
        mEffectivenessImmune.put("Ghost", Arrays.asList(new String[]{"Fighting", "Normal"}));

        // Fire Type
        mEffectivenessStrong.put("Fire", Arrays.asList(new String[]{"Ground", "Rock", "Water"}));
        mEffectivenessWeak.put("Fire", Arrays.asList(new String[]{"Bug", "Fairy", "Fire", "Grass", "Ice", "Steel"}));
    }
}

