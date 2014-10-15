package com.pokemonshowdown.data;

import android.content.Context;
import android.util.Log;

import com.pokemonshowdown.app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by thain on 7/22/14.
 * <p/>
 * Array index (including nature array)
 * HP: 0
 * Atk: 1
 * Def: 2
 * SpAtk: 3
 * SpDef: 4
 * Spd: 5
 * <p/>
 * Example for Modest mNatureMultiplier array (+SpAtk, -Atk) [1.0, 0.9, 1.0, 1.1, 1.0, 1.0]
 * <p/>
 * mAbility holds ability tag
 * <p/>
 * Default:
 * Gender: male unless no option (M, F, N)
 * Level: 100
 */
public class Pokemon implements Serializable {
    public final static String PTAG = "POKEMON_OBJECT";
    public final static String[] NATURES = {"Adamant", "Bashful", "Bold", "Brave", "Calm", "Careful", "Docile", "Gentle", "Hardy", "Hasty", "Impish", "Jolly", "Lax", "Lonely", "Mild", "Modest", "Naive", "Naughty", "Quiet", "Quirky", "Rash", "Relaxed", "Sassy", "Serious", "Timid"};
    public final static String[] NATURES_DETAILS = {"Adamant (+Atk -SpA)", "Bashful", "Bold (+Def -Atk)", "Brave (+Atk -Spe)", "Calm (+SpD -Atk)", "Careful (+SpD -SpA)", "Docile", "Gentle (+SpD -Def)", "Hardy", "Hasty (+Spe -Def)", "Impish (+Def -SpA)", "Jolly (+Spe -SpA)", "Lax (+Def -SpD)", "Lonely (+Atk -Def)", "Mild (+SpA -Def)", "Modest (+SpA -Atk)", "Naive (+Spe -SpD)", "Naughty (+Atk -SpD)", "Quiet (+SpA -Spe)", "Quirky", "Rash (+SpA -SpD)", "Relaxed (+Def -Spe)", "Sassy (+SpD -Spe)", "Serious", "Timid (+Spe -Atk)"};

    private int mHappiness;
    private int mIcon;
    private int mIconM;
    private int mIconF;
    private int mIconSmall;
    private int mIconShiny;
    private int mIconShinyM;
    private int mIconShinyF;
    private String mTagName;
    private String mNameWithUnderScore;
    private String mName;
    private String mNickName;
    private int[] mStats;
    private int[] mBaseStats;
    private int[] mEVs;
    private int[] mIVs;
    private int mLevel;
    private String mGender;
    private boolean mGenderAvailable;
    private float[] mNatureMultiplier;
    private String mNature;
    private boolean mShiny;
    private String mAbility;
    private HashMap<String, String> mAbilityList;
    private String[] mType;
    private int[] mTypeIcon;
    private int mWeight;
    private String mItem;
    private String mMove1;
    private String mMove2;
    private String mMove3;
    private String mMove4;

    /**
     * Exporting function
     * @return A string with the pokemon using Showdown! format
     */
    public String export() {
        StringBuilder sb = new StringBuilder();
        if (getName().length() > 0) {
            sb.append(getName());
            sb.append(" (" + getGender() + ")");
            if (getItem().length() > 0) {
                sb.append(" @ " + getItem());
            }
            sb.append('\n');
        }
        if (getAbility().length() > 0) {
            sb.append("Ability: " + getAbility() + '\n');
        }

        if (getLevel() != 100) {
            sb.append("Level: " + getLevel() + '\n');
        }

        if (isShiny()) {
            sb.append("Shiny: Yes\n");
        }

        if (getHappiness() != 255) {
            sb.append("Happiness: " + getHappiness() + "\n");
        }

        boolean difZero = false;
        for (int i : getEVs()) {
            if (i != 0) {
                difZero = true;
                break;
            }
        }
        if (difZero) {
            boolean first = true;

            sb.append("EVs: ");
            if (getHPEV() != 0) {
                if (first) {
                    sb.append(getHPEV() + " HP ");
                    first = false;
                }
            }
            if (getAtkEV() != 0) {
                if (first) {
                    sb.append(getAtkEV() + " Atk ");
                    first = false;

                } else {
                    sb.append("/ " + getAtkEV() + " Atk ");
                }
            }
            if (getDefEV() != 0) {
                if (first) {
                    sb.append(getDefEV() + " Def ");
                    first = false;

                } else {
                    sb.append("/ " + getDefEV() + " Def ");
                }
            }
            if (getSpAtkEV() != 0) {
                if (first) {
                    sb.append(getSpAtkEV() + " SpA ");
                    first = false;

                } else {
                    sb.append("/ " + getSpAtkEV() + " SpA ");
                }
            }
            if (getSpDefEV() != 0) {
                if (first) {
                    sb.append(getSpDefEV() + " SpD ");
                    first = false;

                } else {
                    sb.append("/ " + getSpDefEV() + " SpD ");
                }
            }
            if (getSpdEV() != 0) {
                if (first) {
                    sb.append(getSpdEV() + " Spe ");
                    first = false;
                } else {
                    sb.append("/ " + getSpdEV() + " Spe ");
                }
            }
            sb.append('\n');
        }
        boolean noMoves = true;
        if (!getMove1().equals("--")) {
            noMoves = false;
        }
        if (!getMove2().equals("--")) {
            noMoves = false;
        }
        if (!getMove3().equals("--")) {
            noMoves = false;
        }
        if (!getMove4().equals("--")) {
            noMoves = false;
        }
        if (!noMoves) {
            if (getMove1().equals("--")) {
                sb.append("- \n");
            } else {
                sb.append("- " + getMove1() + "\n");
            }

            if (getMove2().equals("--")) {
                sb.append("- \n");
            } else {
                sb.append("- " + getMove2() + "\n");
            }

            if (getMove3().equals("--")) {
                sb.append("- \n");
            } else {
                sb.append("- " + getMove3() + "\n");
            }

            if (getMove4().equals("--")) {
                sb.append("- \n");
            } else {
                sb.append("- " + getMove4() + "\n");
            }
        }

        if (getNature().length() > 0) {
            sb.append(getNature() + " Nature" + '\n');
        }

        return sb.toString();
    }

    public Pokemon(Context appContext, String name, boolean withAppContext) {
        try {
            mTagName = name;
            JSONObject jsonObject;
            if (withAppContext) {
                jsonObject = new JSONObject(Pokedex.getWithApplicationContext(appContext).getPokemon(name));
            } else {
                jsonObject = new JSONObject(Pokedex.get(appContext).getPokemon(name));
            }
            initializePokemon(appContext, jsonObject);
        } catch (JSONException e) {
            Log.d(PTAG, e.toString());
        }
    }

    private void initializePokemon(Context appContext, JSONObject jsonObject) {
        try {
            mName = jsonObject.getString("species");
            mNameWithUnderScore = mName.replaceAll("-", "_").replaceAll(" ", "").replaceAll("\'", "").replace(Character.toString('.'), "").toLowerCase();

            mIcon = appContext.getResources().getIdentifier("sprites_" + mNameWithUnderScore, "drawable", appContext.getPackageName());
            mIconM = appContext.getResources().getIdentifier("sprites_" + mNameWithUnderScore, "drawable", appContext.getPackageName());
            mIconF = appContext.getResources().getIdentifier("sprites_" + mNameWithUnderScore + "_f", "drawable", appContext.getPackageName());
            mIconShiny = appContext.getResources().getIdentifier("sprshiny_" + mNameWithUnderScore, "drawable", appContext.getPackageName());
            mIconShinyM = appContext.getResources().getIdentifier("sprshiny_" + mNameWithUnderScore, "drawable", appContext.getPackageName());
            mIconShinyF = appContext.getResources().getIdentifier("sprshiny_" + mNameWithUnderScore + "_f", "drawable", appContext.getPackageName());
            mIconSmall = appContext.getResources().getIdentifier("smallicons_" + mNameWithUnderScore, "drawable", appContext.getPackageName());

            setNickName(mName);
            setStats(new int[6]);
            setBaseStats(new int[6]);
            JSONObject baseStats = (JSONObject) jsonObject.get("baseStats");
            mBaseStats[0] = baseStats.getInt("hp");
            mBaseStats[1] = baseStats.getInt("atk");
            mBaseStats[2] = baseStats.getInt("def");
            mBaseStats[3] = baseStats.getInt("spa");
            mBaseStats[4] = baseStats.getInt("spd");
            mBaseStats[5] = baseStats.getInt("spe");
            setEVs(new int[6]);
            setIVs(new int[6]);
            Arrays.fill(mIVs, 31);
            setLevel(100);
            try {
                setGender(jsonObject.getString("gender"));
                mGenderAvailable = false;
            } catch (JSONException e) {
                mGenderAvailable = true;
                setGender("M");
            }
            setHappiness(255);
            setNature("Adamant");
            setStats(calculateStats());
            setShiny(false);
            JSONArray types = jsonObject.getJSONArray("types");
            setType(new String[types.length()]);
            setTypeIcon(new int[types.length()]);
            for (int i = 0; i < types.length(); i++) {
                mType[i] = types.get(i).toString();
                mTypeIcon[i] = appContext.getResources().getIdentifier("types_" + mType[i].toLowerCase(), "drawable", appContext.getPackageName());
            }
            JSONObject abilityList = (JSONObject) jsonObject.get("abilities");
            Iterator<String> keys = abilityList.keys();
            mAbilityList = new HashMap<>();
            while (keys.hasNext()) {
                String key = keys.next();
                mAbilityList.put(key, abilityList.getString(key));
            }
            setAbilityTag("0");
            setItem("leftovers");

            setMove1("--");
            setMove2("--");
            setMove3("--");
            setMove4("--");
        } catch (JSONException e) {
            Log.d(PTAG, e.toString());
        } catch (java.lang.NullPointerException e) {
            Log.e(PTAG, e.toString());
        }
    }

    public static String getPokemonName(Context appContext, String name, boolean withAppContext) {
        try {
            JSONObject jsonObject;
            if (withAppContext) {
                jsonObject = new JSONObject(Pokedex.getWithApplicationContext(appContext).getPokemon(name));
            } else {
                jsonObject = new JSONObject(Pokedex.get(appContext).getPokemon(name));
            }
            return jsonObject.getString("species");
        } catch (JSONException e) {
            Log.d(PTAG, e.toString());
        }
        return null;
    }

    public static int getPokemonSprite(Context appContext, String name, boolean withAppContext) {
        try {
            JSONObject jsonObject;
            if (withAppContext) {
                jsonObject = new JSONObject(Pokedex.getWithApplicationContext(appContext).getPokemon(name));
            } else {
                jsonObject = new JSONObject(Pokedex.get(appContext).getPokemon(name));
            }
            return appContext.getResources().getIdentifier("sprites_" + jsonObject.getString("species").toLowerCase().replaceAll("-", "_").replaceAll(" ", "").replaceAll("\'", "").replace(Character.toString('.'), ""), "drawable", appContext.getPackageName());
        } catch (JSONException e) {
            Log.d(PTAG, e.toString());
        }
        return 0;
    }

    public static int getPokemonIcon(Context appContext, String name, boolean withAppContext) {
        try {
            JSONObject jsonObject;
            if (withAppContext) {
                jsonObject = new JSONObject(Pokedex.getWithApplicationContext(appContext).getPokemon(name));
            } else {
                jsonObject = new JSONObject(Pokedex.get(appContext).getPokemon(name));
            }
            return appContext.getResources().getIdentifier("smallicons_" + jsonObject.getString("species").toLowerCase().replaceAll("-", "_").replaceAll(" ", "").replaceAll("\'", "").replace(Character.toString('.'), ""), "drawable", appContext.getPackageName());
        } catch (JSONException e) {
            Log.d(PTAG, e.toString());
        }
        return 0;
    }

    public static Integer[] getPokemonBaseStats(Context appContext, String name, boolean withAppContext) {
        try {
            JSONObject jsonObject;
            if (withAppContext) {
                jsonObject = new JSONObject(Pokedex.getWithApplicationContext(appContext).getPokemon(name));
            } else {
                jsonObject = new JSONObject(Pokedex.get(appContext).getPokemon(name));
            }
            JSONObject baseStats = (JSONObject) jsonObject.get("baseStats");
            Integer[] baseStatsInteger = new Integer[6];
            baseStatsInteger[0] = baseStats.getInt("hp");
            baseStatsInteger[1] = baseStats.getInt("atk");
            baseStatsInteger[2] = baseStats.getInt("def");
            baseStatsInteger[3] = baseStats.getInt("spa");
            baseStatsInteger[4] = baseStats.getInt("spd");
            baseStatsInteger[5] = baseStats.getInt("spe");
            return baseStatsInteger;
        } catch (JSONException e) {
            Log.d(PTAG, e.toString());
        }
        return null;
    }

    public static Integer[] getPokemonTypeIcon(Context appContext, String name, boolean withAppContext) {
        try {
            JSONObject jsonObject;
            if (withAppContext) {
                jsonObject = new JSONObject(Pokedex.getWithApplicationContext(appContext).getPokemon(name));
            } else {
                jsonObject = new JSONObject(Pokedex.get(appContext).getPokemon(name));
            }
            JSONArray types = jsonObject.getJSONArray("types");
            String[] typesString = new String[types.length()];
            Integer[] typesIcon = new Integer[types.length()];
            for (int i = 0; i < types.length(); i++) {
                typesString[i] = types.get(i).toString();
                typesIcon[i] = appContext.getResources().getIdentifier("types_" + typesString[i].toLowerCase(), "drawable", appContext.getPackageName());
            }
            return typesIcon;
        } catch (JSONException e) {
            Log.d(PTAG, e.toString());
        }
        return null;
    }

    public int[] calculateStats() {
        int[] stats = new int[6];
        stats[0] = calculateHP();
        stats[1] = calculateAtk();
        stats[2] = calculateDef();
        stats[3] = calculateSpAtk();
        stats[4] = calculateSpDef();
        stats[5] = calculateSpd();
        return stats;
    }

    public int calculateHP() {
        return ((getHPIV() + 2 * getBaseHP() + getHPEV() / 4 + 100) * getLevel() / 100 + 10);
    }

    public int calculateAtk() {
        return (int) (((getAtkIV() + 2 * getBaseAtk() + getAtkEV() / 4) * getLevel() / 100 + 5) * mNatureMultiplier[1]);
    }

    public int calculateDef() {
        return (int) (((getDefIV() + 2 * getBaseDef() + getDefEV() / 4) * getLevel() / 100 + 5) * mNatureMultiplier[2]);
    }

    public int calculateSpAtk() {
        return (int) (((getSpAtkIV() + 2 * getBaseSpAtk() + getSpAtkEV() / 4) * getLevel() / 100 + 5) * mNatureMultiplier[3]);
    }

    public int calculateSpDef() {
        return (int) (((getSpDefIV() + 2 * getBaseSpDef() + getSpDefEV() / 4) * getLevel() / 100 + 5) * mNatureMultiplier[4]);
    }

    public int calculateSpd() {
        return (int) (((getSpdIV() + 2 * getBaseSpd() + getSpdEV() / 4) * getLevel() / 100 + 5) * mNatureMultiplier[5]);
    }

    public static int[] calculateStats(int[] baseStats, int[] IVs, int[] EVs, int level, int[] natureMultiplier) {
        int[] stats = new int[6];
        stats[0] = calculateHP(baseStats[0], IVs[0], EVs[0], level);
        stats[1] = calculateAtk(baseStats[1], IVs[1], EVs[1], level, natureMultiplier[1]);
        stats[2] = calculateDef(baseStats[2], IVs[2], EVs[2], level, natureMultiplier[2]);
        stats[3] = calculateSpAtk(baseStats[3], IVs[3], EVs[3], level, natureMultiplier[3]);
        stats[4] = calculateSpDef(baseStats[4], IVs[4], EVs[4], level, natureMultiplier[4]);
        stats[5] = calculateSpd(baseStats[5], IVs[5], EVs[5], level, natureMultiplier[5]);
        return stats;
    }

    public static int calculateHP(int baseHP, int HPIV, int HPEV, int level) {
        return ((HPIV + 2 * baseHP + HPEV / 4 + 100) * level / 100 + 10);
    }

    public static int calculateAtk(int baseAtk, int AtkIV, int AtkEV, int level, float natureMultiplier) {
        return (int) (((AtkIV + 2 * baseAtk + AtkEV / 4) * level / 100 + 5) * natureMultiplier);
    }

    public static int calculateDef(int baseDef, int DefIV, int DefEV, int level, float natureMultiplier) {
        return (int) (((DefIV + 2 * baseDef + DefEV / 4) * level / 100 + 5) * natureMultiplier);
    }

    public static int calculateSpAtk(int baseSpAtk, int SpAtkIV, int SpAtkEV, int level, float natureMultiplier) {
        return (int) (((SpAtkIV + 2 * baseSpAtk + SpAtkEV / 4) * level / 100 + 5) * natureMultiplier);
    }

    public static int calculateSpDef(int baseSpDef, int SpDefIV, int SpDefEV, int level, float natureMultiplier) {
        return (int) (((SpDefIV + 2 * baseSpDef + SpDefEV / 4) * level / 100 + 5) * natureMultiplier);
    }

    public static int calculateSpd(int baseSpd, int SpdIV, int SpdEV, int level, float natureMultiplier) {
        return (int) (((SpdIV + 2 * baseSpd + SpdEV / 4) * level / 100 + 5) * natureMultiplier);
    }

    public String getName() {
        return mName;
    }

    public String getNickName() {
        return mNickName;
    }

    public void setNickName(String nickName) {
        mNickName = nickName;
    }

    public int getIcon() {
        return mIcon;
    }

    public void setIcon(int icon) {
        mIcon = icon;
    }

    public int getIconSmall() {
        return mIconSmall;
    }

    public void setIconSmall(int iconSmall) {
        mIconSmall = iconSmall;
    }

    public int getIconShiny() {
        if (mIconShiny == 0) {
            return mIcon;
        }
        return mIconShiny;
    }

    public void setIconShiny(int iconShiny) {
        mIconShiny = iconShiny;
    }

    public int[] getStats() {
        return mStats;
    }

    public void setStats(int[] stats) {
        mStats = stats;
    }

    public int getHP() {
        return mStats[0];
    }

    public void setHP(int HP) {
        mStats[0] = HP;
    }

    public int getAtk() {
        return mStats[1];
    }

    public void setAtk(int Atk) {
        mStats[1] = Atk;
    }

    public int getDef() {
        return mStats[2];
    }

    public void setDef(int Def) {
        mStats[2] = Def;
    }

    public int getSpAtk() {
        return mStats[3];
    }

    public void setSpAtk(int SpAtk) {
        mStats[3] = SpAtk;
    }

    public int getSpDef() {
        return mStats[4];
    }

    public void setSpDef(int SpDef) {
        mStats[4] = SpDef;
    }

    public int getSpd() {
        return mStats[5];
    }

    public void setSpd(int Spd) {
        mStats[5] = Spd;
    }

    public int[] getBaseStats() {
        return mBaseStats;
    }

    public void setBaseStats(int[] baseStats) {
        mBaseStats = baseStats;
    }

    public int getBaseHP() {
        return mBaseStats[0];
    }

    public int getBaseAtk() {
        return mBaseStats[1];
    }

    public int getBaseDef() {
        return mBaseStats[2];
    }

    public int getBaseSpAtk() {
        return mBaseStats[3];
    }

    public int getBaseSpDef() {
        return mBaseStats[4];
    }

    public int getBaseSpd() {
        return mBaseStats[5];
    }

    public int[] getEVs() {
        return mEVs;
    }

    public void setEVs(int[] EVs) {
        mEVs = EVs;
    }

    public int getHPEV() {
        return mEVs[0];
    }

    public void setHPEV(int HP) {
        mEVs[0] = HP;
    }

    public int getAtkEV() {
        return mEVs[1];
    }

    public void setAtkEV(int Atk) {
        mEVs[1] = Atk;
    }

    public int getDefEV() {
        return mEVs[2];
    }

    public void setDefEV(int Def) {
        mEVs[2] = Def;
    }

    public int getSpAtkEV() {
        return mEVs[3];
    }

    public void setSpAtkEV(int SpAtk) {
        mEVs[3] = SpAtk;
    }

    public int getSpDefEV() {
        return mEVs[4];
    }

    public void setSpDefEV(int SpDef) {
        mEVs[4] = SpDef;
    }

    public int getSpdEV() {
        return mEVs[5];
    }

    public void setSpdEV(int Spd) {
        mEVs[5] = Spd;
    }

    public int[] getIVs() {
        return mIVs;
    }

    public void setIVs(int[] IVs) {
        mIVs = IVs;
    }

    public int getHPIV() {
        return mIVs[0];
    }

    public void setHPIV(int HP) {
        mIVs[0] = HP;
    }

    public int getAtkIV() {
        return mIVs[1];
    }

    public void setAtkIV(int Atk) {
        mIVs[1] = Atk;
    }

    public int getDefIV() {
        return mIVs[2];
    }

    public void setDefIV(int Def) {
        mIVs[2] = Def;
    }

    public int getSpAtkIV() {
        return mIVs[3];
    }

    public void setSpAtkIV(int SpAtk) {
        mIVs[3] = SpAtk;
    }

    public int getSpDefIV() {
        return mIVs[4];
    }

    public void setSpDefIV(int SpDef) {
        mIVs[4] = SpDef;
    }

    public int getSpdIV() {
        return mIVs[5];
    }

    public void setSpdIV(int Spd) {
        mIVs[5] = Spd;
    }

    public int getLevel() {
        return mLevel;
    }

    public void setLevel(int level) {
        mLevel = level;
    }

    public String getGender() {
        return mGender;
    }

    public void setGender(String gender) {
        mGender = gender;
    }

    public boolean isGenderAvailable() {
        return mGenderAvailable;
    }

    public int getGenderIcon() {
        switch (mGender) {
            case "M":
                return R.drawable.ic_gender_male;
            case "F":
                return R.drawable.ic_gender_female;
            default:
                return 0;
        }
    }

    public void switchGender() {
        if (!isGenderAvailable()) {
            return;
        }
        switch (mGender) {
            case "M":
                setGender("F");
                if (mIconF != 0) {
                    setIcon(mIconF);
                }
                if (mIconShinyF != 0) {
                    setIconShiny(mIconShinyF);
                }
                return;
            case "F":
                setGender("M");
                if (mIconM != 0) {
                    setIcon(mIconM);
                }
                if (mIconShinyM != 0) {
                    setIconShiny(mIconShinyM);
                }
        }
    }

    public float[] getNatureMultiplier() {
        return mNatureMultiplier;
    }

    public void setNatureMultiplier(String nature) {
        mNatureMultiplier = new float[6];
        Arrays.fill(mNatureMultiplier, 1.0f);
        if (nature.equals("Adamant")) {
            mNatureMultiplier[1] = 1.1f;
            mNatureMultiplier[3] = 0.9f;
        }
        if (nature.equals("Bold")) {
            mNatureMultiplier[2] = 1.1f;
            mNatureMultiplier[1] = 0.9f;
        }
        if (nature.equals("Brave")) {
            mNatureMultiplier[1] = 1.1f;
            mNatureMultiplier[5] = 0.9f;
        }
        if (nature.equals("Calm")) {
            mNatureMultiplier[4] = 1.1f;
            mNatureMultiplier[1] = 0.9f;
        }
        if (nature.equals("Careful")) {
            mNatureMultiplier[5] = 1.1f;
            mNatureMultiplier[3] = 0.9f;
        }
        if (nature.equals("Gentle")) {
            mNatureMultiplier[5] = 1.1f;
            mNatureMultiplier[2] = 0.9f;
        }
        if (nature.equals("Hasty")) {
            mNatureMultiplier[5] = 1.1f;
            mNatureMultiplier[2] = 0.9f;
        }
        if (nature.equals("Impish")) {
            mNatureMultiplier[2] = 1.1f;
            mNatureMultiplier[3] = 0.9f;
        }
        if (nature.equals("Jolly")) {
            mNatureMultiplier[5] = 1.1f;
            mNatureMultiplier[3] = 0.9f;
        }
        if (nature.equals("Lax")) {
            mNatureMultiplier[2] = 1.1f;
            mNatureMultiplier[4] = 0.9f;
        }
        if (nature.equals("Lonely")) {
            mNatureMultiplier[1] = 1.1f;
            mNatureMultiplier[2] = 0.9f;
        }
        if (nature.equals("Mild")) {
            mNatureMultiplier[3] = 1.1f;
            mNatureMultiplier[2] = 0.9f;
        }
        if (nature.equals("Modest")) {
            mNatureMultiplier[3] = 1.1f;
            mNatureMultiplier[1] = 0.9f;
        }
        if (nature.equals("Naive")) {
            mNatureMultiplier[5] = 1.1f;
            mNatureMultiplier[4] = 0.9f;
        }
        if (nature.equals("Naughty")) {
            mNatureMultiplier[1] = 1.1f;
            mNatureMultiplier[5] = 0.9f;
        }
        if (nature.equals("Quiet")) {
            mNatureMultiplier[3] = 1.1f;
            mNatureMultiplier[5] = 0.9f;
        }
        if (nature.equals("Rash")) {
            mNatureMultiplier[3] = 1.1f;
            mNatureMultiplier[5] = 0.9f;
        }
        if (nature.equals("Relaxed")) {
            mNatureMultiplier[2] = 1.1f;
            mNatureMultiplier[5] = 0.9f;
        }
        if (nature.equals("Sassy")) {
            mNatureMultiplier[4] = 1.1f;
            mNatureMultiplier[5] = 0.9f;
        }
        if (nature.equals("Timid")) {
            mNatureMultiplier[5] = 1.1f;
            mNatureMultiplier[1] = 0.9f;
        }
    }

    public String getNature() {
        return mNature;
    }

    public void setNature(String nature) {
        mNature = nature;
        setNatureMultiplier(nature);
    }

    public boolean isShiny() {
        return mShiny;
    }

    public void setShiny(boolean shiny) {
        mShiny = shiny;
    }

    public void switchShiny() {
        setShiny(!isShiny());
    }

    public String getAbility() {
        return getAbilityList().get(mAbility);
    }

    public String getAbilityTag() {
        return mAbility;
    }

    public void setAbilityTag(String abilityTag) {
        mAbility = abilityTag;
    }

    public HashMap<String, String> getAbilityList() {
        return mAbilityList;
    }

    public void setAbilityList(HashMap<String, String> abilityList) {
        mAbilityList = abilityList;
    }

    public String[] getType() {
        return mType;
    }

    public void setType(String[] type) {
        mType = type;
    }

    public int[] getTypeIcon() {
        return mTypeIcon;
    }

    public void setTypeIcon(int[] typeIcon) {
        mTypeIcon = typeIcon;
    }

    public int getWeight() {
        return mWeight;
    }

    public void setWeight(int weight) {
        mWeight = weight;
    }

    public String getItem() {
        return mItem;
    }

    public void setItem(String item) {
        mItem = item;
    }

    public String getMove1() {
        return mMove1;
    }

    public void setMove1(String move1) {
        mMove1 = move1;
    }

    public String getMove2() {
        return mMove2;
    }

    public void setMove2(String move2) {
        mMove2 = move2;
    }

    public String getMove3() {
        return mMove3;
    }

    public void setMove3(String move3) {
        mMove3 = move3;
    }

    public String getMove4() {
        return mMove4;
    }

    public void setMove4(String move4) {
        mMove4 = move4;
    }

    public void setHappiness(int happiness) {
        this.mHappiness = happiness;
    }

    public int getHappiness() {
        return mHappiness;
    }
}
