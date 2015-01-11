package com.pokemonshowdown.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pokemonshowdown.data.FieldFragment;
import com.pokemonshowdown.data.ItemDex;
import com.pokemonshowdown.data.MoveDex;
import com.pokemonshowdown.data.Pokedex;
import com.pokemonshowdown.data.Pokemon;
import com.pokemonshowdown.data.SearchableActivity;

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

public class DmgCalcActivity extends FragmentActivity implements FieldFragment.FieldConditionsListener {
    public final static String DTAG = DmgCalcActivity.class.getName();
    public final static int REQUEST_CODE_FIND_ATTACKER = 0;
    public final static int REQUEST_CODE_FIND_DEFENDER = 1;
    public final static int REQUEST_CODE_GET_MOVE_1 = 2;
    public final static int REQUEST_CODE_GET_MOVE_2 = 3;
    public final static int REQUEST_CODE_GET_MOVE_3 = 4;
    public final static int REQUEST_CODE_GET_MOVE_4 = 5;

    private final static DecimalFormat DAMAGE_FORMAT = new DecimalFormat("#0.0%");

    private Pokemon mAttacker;
    private Pokemon mDefender;

    private final static String PARAM_ATTACKER = "Attacker";
    private final static String PARAM_DEFENDER = "Defender";

    private Map<String, List<String>> mEffectivenessStrong = new HashMap<>();
    private Map<String, List<String>> mEffectivenessWeak = new HashMap<>();
    private Map<String, List<String>> mEffectivenessImmune = new HashMap<>();

    private boolean mIsSingles = true;
    private boolean mGravityActive = false;
    private boolean mStealthRocksActive = false;
    private boolean mReflectActive = false;
    private boolean mLightScreenActive = false;
    private boolean mForesightActive = false;
    private boolean mHelpingHandActive = false;
    private int mSpikesCount = 0;
    private Weather mActiveWeather = Weather.NO_WEATHER;

    private FieldFragment mFieldFragment;

    public enum FieldConditions {
        SINGLES, DOUBLES, STEALTH_ROCK, ZERO_SPIKES, ONE_SPIKES, TWO_SPIKES, THREE_SPIKES, REFLECT, LIGHT_SCREEN, FORESIGHT, HELPING_HAND, NO_WEATHER, SUN, RAIN, SAND, HAIL, GRAVITY;
    }

    private enum Weather {
        NO_WEATHER, SUN, RAIN, SAND, HAIL;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dmgcalc);

        DAMAGE_FORMAT.setRoundingMode(RoundingMode.FLOOR);
        createIndexOfTypeModifiers();

        getActionBar().setTitle(R.string.bar_dmg_calc);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        mFieldFragment = new DmgCalcFieldXYFragment();
        mFieldFragment.setFieldConditionsListener(this);
        Bundle fieldBundle = new Bundle();
        mFieldFragment.setArguments(fieldBundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.dmgcalc_field_container, mFieldFragment)
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

                calculateAllMoves();
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
                setAttacker("azumarill");
            }

            if (getDefender() == null) {
                setDefender("heatran");
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

        outState.putSerializable(PARAM_ATTACKER, getAttacker());
        outState.putSerializable(PARAM_DEFENDER, getDefender());
    }

    public Pokemon getAttacker() {
        return mAttacker;
    }

    public void setAttacker(Pokemon attacker) {
        mAttacker = attacker;

        TextView textView = (TextView) findViewById(R.id.dmgcalc_attacker);
        textView.setCompoundDrawablesWithIntrinsicBounds(attacker.getIcon(), 0, 0, 0);
        textView.setText(attacker.getName());

        setMove1(attacker.getMove1());
        setMove2(attacker.getMove2());
        setMove3(attacker.getMove3());
        setMove4(attacker.getMove4());
    }

    public void setAttacker(String attacker) {
        setAttacker(new Pokemon(this, attacker));
    }

    public Pokemon getDefender() {
        return mDefender;
    }

    public void setDefender(Pokemon defender) {
        mDefender = defender;
        TextView textView = (TextView) findViewById(R.id.dmgcalc_defender);
        textView.setCompoundDrawablesWithIntrinsicBounds(defender.getIcon(), 0, 0, 0);
        textView.setText(defender.getName());

        calculateAllMoves();
    }

    public void setDefender(String defender) {
        setDefender(new Pokemon(this, defender));
    }

    private void loadPokemon(Pokemon pokemon, int searchCode) {
        PokemonFragment fragment = new PokemonFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(PokemonFragment.POKEMON, pokemon);
        bundle.putBoolean(PokemonFragment.SEARCH, true);
        bundle.putInt(PokemonFragment.SEARCH_CODE, searchCode);
        bundle.putBoolean(PokemonFragment.STAGES, true);
        bundle.putBoolean(PokemonFragment.HEALTH, true);
        bundle.putBoolean(PokemonFragment.ITEMS, true);
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragment.show(fragmentManager, PokemonFragment.PTAG);
    }

    private void setMove1(String move) {
        mAttacker.setMove1(move);
        String moveName = (move.equals("")) ? "--" : MoveDex.getMoveName(getApplicationContext(), move);
        ((TextView) findViewById(R.id.move1)).setText(moveName);
        calculateDamage(1);
    }

    private void setMove2(String move) {
        mAttacker.setMove2(move);
        String moveName = (move.equals("")) ? "--" : MoveDex.getMoveName(getApplicationContext(), move);
        ((TextView) findViewById(R.id.move2)).setText(moveName);
        calculateDamage(2);
    }

    private void setMove3(String move) {
        mAttacker.setMove3(move);
        String moveName = (move.equals("")) ? "--" : MoveDex.getMoveName(getApplicationContext(), move);
        ((TextView) findViewById(R.id.move3)).setText(moveName);
        calculateDamage(3);
    }

    private void setMove4(String move) {
        mAttacker.setMove4(move);
        String moveName = (move.equals("")) ? "--" : MoveDex.getMoveName(getApplicationContext(), move);
        ((TextView) findViewById(R.id.move4)).setText(moveName);
        calculateDamage(4);
    }

    private void calculateDamage(int moveIndex) {
        // Fail-save
        if (getAttacker() == null || getDefender() == null) {
            return;
        }

        int minDamage = calculateDamageRoutine(moveIndex, 0.85, false);
        double minDamagePercent = (double) minDamage / getDefender().calculateHP();

        int maxDamage = calculateDamageRoutine(moveIndex, 1.0, false);
        double maxDamagePercent = (double) maxDamage / getDefender().calculateHP();

        String minDamageText = DAMAGE_FORMAT.format(minDamagePercent);
        String maxDamageText = DAMAGE_FORMAT.format(maxDamagePercent);

        int defenderHP = calculateDefendersInitialHP();
        int damagePerRound = calculateDefendersDamagePerRound();

        int maxHitsTilKo = minDamage == 0 ? 0 : (int) Math.ceil((double) defenderHP / (minDamage + damagePerRound));
        int minHitsTilKo = maxDamage == 0 ? 0 : (int) Math.ceil((double) defenderHP / (maxDamage + damagePerRound));

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

    private int calculateDefendersInitialHP() {
        int baseHP = "Shedinja".equals(getDefender().getName()) ? 1 : getDefender().calculateHP();
        int calculatedHP = getDefender().getHP();
        List<String> typing = Arrays.asList(getRealTyping(getDefender()));

        if (mStealthRocksActive && !getDefender().getAbility().equals("Magic Guard")) {
            calculatedHP -= baseHP * (0.125 * calculateWeaknessModifier("stealthrock", "Rock"));
        }

        if (mSpikesCount > 0 && !getDefender().getAbility().equals("Magic Guard") && !typing.contains("Flying") && !(getDefender().getAbility().equals("Levitate") && !isMoldBreakerActive())) {
            calculatedHP -= baseHP * (mSpikesCount == 1 ? 1.0 / 8 : mSpikesCount == 2 ? 1.0 / 6 : 1.0 / 4);
        }

        return calculatedHP;
    }

    private double getHPFraction(Pokemon pokemon) {
        return pokemon.getHP() / (double) pokemon.calculateHP();
    }

    private int calculateDefendersDamagePerRound() {
        int baseHP = getDefender().calculateHP();
        int damagePerRound = 0;
        String defenderAbility = getDefender().getAbility();
        List<String> defenderTypes = Arrays.asList(getRealTyping(getDefender()));

        // Weather damage
        if ((defenderAbility.equals("Solar Power") || defenderAbility.equals("Dry Skin")) && mActiveWeather == Weather.SUN) {
            damagePerRound += (baseHP * 0.125);
        }

        if (!defenderAbility.equals("Overcoat") && (!defenderAbility.equals("Magic Guard") || !getDefender().getItem().equals("safetygoggles"))) {
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
        if(getDefender().getItem().equals("Leftovers")) {
            damagePerRound -= (baseHP / 16);
        }

        if(getDefender().getItem().equals("stickybarb") && !getDefender().getAbility().equals("Magic Guard")) {
            damagePerRound += baseHP * (0.125);
        }

        return damagePerRound;
    }

    private void calculateAllMoves() {
        calculateDamage(1);
        calculateDamage(2);
        calculateDamage(3);
        calculateDamage(4);
    }

    private int calculateDamageRoutine(int moveIndex, double luck, boolean crit) {
        String move = null;
        String type = null;
        String basePower = null;
        String category = null;
        String targets = null;
        boolean hasSecondary = false;

        move = getAttacker().getMove(moveIndex);

        try {
            JSONObject moveJson = MoveDex.get(getApplicationContext()).getMoveJsonObject(move);
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
                    return calculateWeaknessModifier(move, type) == 0 ? 0 : 40;
                case "sonicboom":
                    return calculateWeaknessModifier(move, type) == 0 ? 0 : 20;
                case "seismictoss":
                case "nightshade":
                    return calculateWeaknessModifier(move, type) == 0 ? 0 : getAttacker().getLevel();
                default:
                    boolean usesAttack = "Physical".equals(category);
                    boolean usesDefense = "Physical".equals(category) || "psyshock".equals(move) || "psystrike".equals(move) || "secretsword".equals(move);

                    double attack = "foulplay".equals(move) ? Math.round(getDefender().calculateAtk() * getAttackMultiplier(getAttacker())) : usesAttack ? Math.round(getAttacker().calculateAtk() * getAttackMultiplier(getAttacker())) : getAttacker().calculateSpAtk() * getSpecialAttackMultiplier(getAttacker());
                    double defense = usesDefense ? getDefender().calculateDef() * getDefenseMultiplier(getDefender()) : getDefender().calculateSpDef() * getSpecialDefenseMultiplier(getDefender());
                    double base = calculateBasePower(move, type, Double.parseDouble(basePower));

                    String originalType = type;
                    type = modifyAttackType(move, type);

                    boolean isStab = Arrays.asList(getAttackerTypingAfterAbilities(type)).contains(type);

                    List<Double> modifiers = new ArrayList<>();
                    modifiers.add(luck);
                    modifiers.add(isStab ? 1.5 : 1.0);
                    modifiers.add(calculateWeaknessModifier(move, type));
                    modifiers.add(calculateCritMultiplier(move, crit));
                    modifiers.add(mHelpingHandActive ? 1.5 : 1.0);
                    modifiers.add(getSpreadMultiplicator(targets));
                    modifyDamageWithAbility(modifiers, move, type, originalType, category, usesDefense, hasSecondary);
                    modifyDamageWithItem(modifiers, move, type, originalType, category);
                    base = base == 0.0 ? 0 : Math.floor(((2 * getAttacker().getLevel() / 5 + 2) * attack * base / defense) / 50 + 2);
                    return applyDamageModifiers(base, modifiers);
            }
        }
    }

    private String modifyAttackType(String move, String type) {
        String attackerAbility = getAttacker().getAbility();
        if ("naturalgift".equals(move)) {
            JSONObject itemObject = ItemDex.get(this).getItemJsonObject(getAttacker().getItem());
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
            JSONObject itemObject = ItemDex.get(this).getItemJsonObject(getAttacker().getItem());
            type = itemObject == null ? null : itemObject.optString("onDrive", type);
        } else if ("judgement".equals(move)) {
            JSONObject itemObject = ItemDex.get(this).getItemJsonObject(getAttacker().getItem());
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
        if (pokemon.getAbility().equals("Huge Power") || pokemon.getAbility().equals("Pure Power")) {
            baseMultiplier *= 2.0;
        }

        if (pokemon.getAbility().equals("Hustle")) {
            baseMultiplier *= 1.5;
        }

        if (pokemon.getAbility().equals("Flower Gift") && mActiveWeather == Weather.SUN) {
            baseMultiplier *= 1.5;
        }

        if ("Defeatist".equals(pokemon.getAbility())) {
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

        if (mActiveWeather == Weather.SUN && pokemon.getAbility().equals("Solar Power")) {
            baseMultiplier *= 1.5;
        }

        if ("Defeatist".equals(pokemon.getAbility())) {
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

        if ("eviolite".equals(pokemon.getItem()) && Pokedex.get(this).getPokemonJSONObject(pokemon.getName()).has("evos")) {
            baseMultiplier *= 1.5;
        }

        return baseMultiplier;
    }

    private double getSpecialDefenseMultiplier(Pokemon pokemon) {
        double baseMultiplier = 1.0;

        if (pokemon.getAbility().equals("Flower Gift") && mActiveWeather == Weather.SUN) {
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

        if ("eviolite".equals(pokemon.getItem()) && Pokedex.get(this).getPokemonJSONObject(pokemon.getName()).has("evos")) {
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

    private void modifyDamageWithAbility(List<Double> modifiers, String move, String type, String originalType, String category, boolean usesDefense, boolean hasSecondary) {
        List<String> attackerTyping = Arrays.asList(getAttackerTypingAfterAbilities(type));
        String attackerAbility = getAttacker().getAbility();
        String defenderAbility = getDefender().getAbility();

        if (defenderAbility.equals("Wonder Guard") && calculateWeaknessModifier(move, type) > 1.0 && !isMoldBreakerActive()) {
            modifiers.add(0.0);
        }

        if (defenderAbility.equals("Heatproof") && type.equals("Fire") && !isMoldBreakerActive()) {
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
                "magnetbomb", "mistball", "mudbomb", "octazooka", "rockwrecker", "searingshot", "seedbomb", "shadowball", "sludgebomb", "weatherball", "zapcannon"}).contains(move) && !isMoldBreakerActive()) {
            modifiers.add(0.0);
        }

        if (defenderAbility.equals("Soundproof") && Arrays.asList(new String[]{"boomburst", "bugbuzz", "chatter", "confide", "disarmingvoice", "echoedvoice", "grasswhistle", "growl", "healbell", "hypervoice",
                "metalsound", "nobleroar", "relicsong", "round", "snarl", "snore", "uproar"}).contains(move) && !isMoldBreakerActive()) {
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

        if (defenderAbility.equals("Fur Coat") && usesDefense && !isMoldBreakerActive()) {
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

    private double getSpreadMultiplicator(String targets) {
        return ("allAdjacentFoes".equals(targets) || "allAdjacent".equals(targets) || "all".equals(targets)) && !mIsSingles ? 0.75 : 1;
    }

    private String[] getRealTyping(Pokemon pokemon) {
        String[] typing = pokemon.getType();
        if ("Forecast".equals(pokemon.getAbility())) {
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
        } else if ("Multitype".equals(pokemon.getAbility()) && pokemon.getName().contains("Arceus")) {
            JSONObject itemObject = ItemDex.get(this).getItemJsonObject(getAttacker().getItem());
            typing = itemObject == null ? new String[]{"Normal"} : new String[]{itemObject.optString("onPlate", "Normal")};
        }
        return typing;
    }

    private double calculateWeaknessModifier(String move, String type) {
        double modifier = 1.0;

        String[] defenderTyping = getRealTyping(getDefender());
        String attackerAbility = getAttacker().getAbility();
        String defenderAbility = getDefender().getAbility();

        for (String defType : defenderTyping) {
            if (mEffectivenessImmune.get(defType) != null && mEffectivenessImmune.get(defType).contains(type)) {
                if ((attackerAbility.equals("Scrappy") || mForesightActive) && (type.equals("Fighting") || type.equals("Normal"))) {
                    modifier = 1.0;
                } else if (defType.equals("Flying") && mGravityActive) {
                    modifier = 1.0;
                } else if ("ringtarget".equals(getDefender().getItem())) {
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
        if (defenderAbility.equals("Flash Fire") && type.equals("Fire") && !isMoldBreakerActive()) {
            modifier *= 0;
            // Gamefreak seems to hate electric typing..
        } else if ((defenderAbility.equals("Motor Drive") || defenderAbility.equals("Lightning Rod") || defenderAbility.equals("Volt Absorb")) && type.equals("Electric") && !isMoldBreakerActive()) {
            modifier *= 0;
        } else if (defenderAbility.equals("Sap Sipper") && type.equals("Grass") && !isMoldBreakerActive()) {
            modifier *= 0;
        } else if ((defenderAbility.equals("Water Absorb") || defenderAbility.equals("Storm Drain")) && type.equals("Water") && !isMoldBreakerActive()) {
            modifier *= 0;
        } else if (defenderAbility.equals("Dry Skin") && type.equals("Water") && !isMoldBreakerActive()) {
            modifier *= 0;
        } else if (defenderAbility.equals("Dry Skin") && type.equals("Fire") && !isMoldBreakerActive()) {
            modifier *= 1.25;
        } else if (defenderAbility.equals("Levitate") && type.equals("Ground") && !isMoldBreakerActive() && !mGravityActive) {
            modifier *= 0;
        } else if ((defenderAbility.equals("Filter") || defenderAbility.equals("Solid Rock")) && modifier > 1.0 && !isMoldBreakerActive()) {
            modifier *= 0.75;
        } else if (defenderAbility.equals("Thick Fat") && (type.equals("Ice") || type.equals("Fire")) && !isMoldBreakerActive()) {
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

    private double calculateBasePower(String move, String type, double bp) {
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
                bp = Math.min(200, 20 * countStatStages(getDefender(), true) + 60);
                break;
            case "storedpower":
                bp = 20 + (20 * countStatStages(getAttacker(), true));
                break;
            case "knockoff":
                bp = getDefender().getItem().isEmpty() || getDefender().getItem() == null ? 65 : 97.5;
                break;
            case "fling":
                JSONObject obj = ItemDex.get(this).getItemJsonObject(getAttacker().getItem());
                JSONObject flingObject = obj == null ? null : obj.optJSONObject("fling");
                if (flingObject != null) {
                    bp = flingObject.optInt("basePower", 0);
                } else {
                    bp = 0;
                }
                break;
            case "lowkick":
            case "grassknot":
                double weight = calcuateWeight(getDefender());
                bp = weight < 10.0 ? 20 : weight < 25.0 ? 40 : weight < 50.0 ? 60 : weight < 100.0 ? 80 : weight < 200.0 ? 100 : 120;
                break;
            case "acrobatics":
                bp = getAttacker().getItem() == null || getAttacker().getItem().isEmpty() || "flyinggem".equals(getAttacker().getItem()) ? 55 : 110;
                break;
            case "reversal":
            case "flail":
                int mod = (int) (48 * getHPFraction(getAttacker()));
                bp = mod <= 1 ? 200 : mod <= 4 ? 150 : mod <= 9 ? 100 : mod <= 16 ? 80 : mod <= 32 ? 40 : 20;
                break;
            case "heatcrash":
            case "heavyslam":
                ratio = calcuateWeight(getDefender()) / calcuateWeight(getAttacker());
                bp = ratio <= 0.2 ? 120 : ratio <= 0.25 ? 100 : ratio <= 1 / 3 ? 80 : ratio <= 0.5 ? 60 : 40;
                break;
            case "crushgrip":
            case "wringout":
                bp = 1 + (120 * getHPFraction(getDefender()));
                break;
            case "brine":
                bp = getHPFraction(getDefender()) <= 0.5 ? 130 : 65;
                break;
            case "eruption":
            case "waterspout":
                bp = Math.max(1, 150 * getHPFraction(getAttacker()));
                break;
            case "frustration":
            case "return":
                bp = 102;
                break;
            case "naturalgift":
                obj = ItemDex.get(this).getItemJsonObject(getAttacker().getItem());
                JSONObject naturalGiftObject = obj == null ? null : obj.optJSONObject("naturalGift");
                if (naturalGiftObject != null) {
                    bp = naturalGiftObject.optInt("basePower", 0);
                } else {
                    bp = 0;
                }
                break;
            case "gyroball":
                bp = 25.0 * getDefender().calculateSpd() / getAttacker().calculateSpd();
                break;
            case "electroball":
                ratio = getDefender().calculateSpd() / getAttacker().calculateSpd();
                bp = ratio < 0.25 ? 150 : ratio < 1 / 3 ? 120 : ratio < 0.5 ? 80 : 60;
                break;
            case "weatherball":
                if (mActiveWeather != Weather.NO_WEATHER) {
                    bp = 100;
                }
                break;
        }

        if (getAttacker().getAbility().equals("Technician") && bp <= 60) {
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
        if (getAttacker().getName().equals("Dialga") && getAttacker().getItem().equals("adamantorb") && (type.equals("Dragon") || type.equals("Steel"))) {
            bp *= 1.2;
        } else if (getAttacker().getName().equals("Palkia") && getAttacker().getItem().equals("lustrousorb") && (type.equals("Dragon") || type.equals("Water"))) {
            bp *= 1.2;
        } else if ((getAttacker().getName().equals("Giratina") || getAttacker().getName().equals("Giratina-Origin")) && getAttacker().getItem().equals("griseousorb") && (type.equals("Dragon") || type.equals("Ghost"))) {
            bp *= 1.2;
        }

        if (Arrays.asList(new String[]{"dracoplate", "dragonfang"}).contains(getAttacker().getItem()) && "Dragon".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"earthplate", "softsand"}).contains(getAttacker().getItem()) && "Ground".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"fistplate", "blackbelt"}).contains(getAttacker().getItem()) && "Fighting".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"flameplate", "charcoal"}).contains(getAttacker().getItem()) && "Fire".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"icicleplate", "nevermeltice"}).contains(getAttacker().getItem()) && "Ice".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"insectplate", "silverpowder"}).contains(getAttacker().getItem()) && "Bug".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"ironplate", "metalcoat"}).contains(getAttacker().getItem()) && "Steel".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"meadowplate", "miracleseed", "roseincense"}).contains(getAttacker().getItem()) && "Grass".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"mindplate", "twistedspoon", "oddincense"}).contains(getAttacker().getItem()) && "Psychic".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"pixieplate"}).contains(getAttacker().getItem()) && "Fairy".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"dreadplate", "blackglasses"}).contains(getAttacker().getItem()) && "Dark".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"skyplate", "sharpbeak"}).contains(getAttacker().getItem()) && "Flying".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"splashplate", "mysticwater", "seaincense", "waveincense"}).contains(getAttacker().getItem()) && "Water".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"spookyplate", "spelltag"}).contains(getAttacker().getItem()) && "Ghost".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"stoneplate", "hardstone", "rockincense"}).contains(getAttacker().getItem()) && "Rock".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"toxicplate", "poisonbarb"}).contains(getAttacker().getItem()) && "Poison".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"zapplate", "magnet"}).contains(getAttacker().getItem()) && "Electric".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"silkscarf"}).contains(getAttacker().getItem()) && "Normal".equals(type)) {
            bp *= 1.2;
        } else if (Arrays.asList(new String[]{"polkadotbow"}).contains(getAttacker().getItem()) && "Normal".equals(type)) {
            bp *= 1.125;
        } else if (Arrays.asList(new String[]{"pinkbow"}).contains(getAttacker().getItem()) && "Normal".equals(type)) {
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

        if ("Light Metal".equals(pokemon.getAbility())) {
            normalWeight *= 0.5;
        } else if ("Heavy Metal".equals(pokemon.getAbility())) {
            normalWeight *= 2.0;
        }

        return normalWeight;
    }

    private void createIndexOfTypeModifiers() {
        // Normal Type
        mEffectivenessStrong.put("Normal", Arrays.asList(new String[]{"Fighting"}));
        mEffectivenessImmune.put("Normal", Arrays.asList(new String[]{"Ghost"}));

        // Steel Type
        mEffectivenessStrong.put("Steel", Arrays.asList(new String[]{"Fighting", "Fire", "Ground"}));
        mEffectivenessWeak.put("Steel", Arrays.asList(new String[]{"Bug", "Dragon", "Flying", "Fairy", "Grass",
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

        calculateAllMoves();
    }

    private String[] getAttackerTypingAfterAbilities(String moveType) {
        return getAttacker().getAbility().equals("Protean") ? new String[]{moveType} : getRealTyping(getAttacker());
    }

    public void updateDamage() {
        calculateAllMoves();
    }


    @Override
    public void onFieldConditionChanged(FieldConditions conditions, boolean value) {
        setConditionStatus(conditions, value);
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mFieldFragment != null) {
            mFieldFragment.removeListener();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mFieldFragment != null) {
            mFieldFragment.setFieldConditionsListener(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mFieldFragment != null) {
            mFieldFragment.removeListener();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mFieldFragment != null) {
            mFieldFragment.setFieldConditionsListener(this);
        }
    }
}
