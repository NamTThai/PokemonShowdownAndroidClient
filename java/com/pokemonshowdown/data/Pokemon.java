package com.pokemonshowdown.data;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.pokemonshowdown.R;
import com.pokemonshowdown.application.MyApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

/**
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
    public final static String PTAG = Pokemon.class.getName();
    public final static String[] NATURES = {"Adamant", "Bashful", "Bold", "Brave", "Calm", "Careful", "Docile", "Gentle", "Hardy", "Hasty", "Impish", "Jolly", "Lax", "Lonely", "Mild", "Modest", "Naive", "Naughty", "Quiet", "Quirky", "Rash", "Relaxed", "Sassy", "Serious", "Timid"};
    public final static String[] NATURES_DETAILS = {"Adamant (+Atk -SpA)", "Bashful", "Bold (+Def -Atk)", "Brave (+Atk -Spe)", "Calm (+SpD -Atk)", "Careful (+SpD -SpA)", "Docile", "Gentle (+SpD -Def)", "Hardy", "Hasty (+Spe -Def)", "Impish (+Def -SpA)", "Jolly (+Spe -SpA)", "Lax (+Def -SpD)", "Lonely (+Atk -Def)", "Mild (+SpA -Def)", "Modest (+SpA -Atk)", "Naive (+Spe -SpD)", "Naughty (+Atk -SpD)", "Quiet (+SpA -Spe)", "Quirky", "Rash (+SpA -SpD)", "Relaxed (+Def -Spe)", "Sassy (+SpD -Spe)", "Serious", "Timid (+Spe -Atk)"};

    private final static double[] STAGES_MAIN_STATS = {0.25, 0.285, 0.33, 0.4, 0.5, 0.66, 1, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0};

    private int mHappiness;
    private int mIcon;
    private int mFrontSprite;
    private int mBackSprite;
    private String mName;
    private String mNickName;
    private String mBaseName;
    private int[] mStats;
    private int[] mBaseStats;
    private int[] mEVs;
    private int[] mIVs;
    private int[] mStages;
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
    private double mWeight;
    private String mItem;
    private String mMove1;
    private String mMove2;
    private String mMove3;
    private String mMove4;

    public Pokemon(Context appContext, String name) {
        try {
            name = MyApplication.toId(name);
            String unaltered = name;
            String[] specialForms = new String[]{"burmysandy", "burmytrash", "shelloseast", "gastrodoneast", "deerlingsummer",
                    "deerlingautumn", "deerlingwinter", "sawsbucksummer", "sawsbuckautumn", "sawsbuckwinter", "vivillonarchipelago",
                    "vivilloncontinental", "vivillonelegant", "vivillongarden", "vivillonhighplains", "vivillonicysnow",
                    "vivillonjungle", "vivillonmarine", "vivillonmodern", "vivillonmonsoon", "vivillonocean", "vivillonpolar",
                    "vivillonriver", "vivillonsandstorm", "vivillonsavanna", "vivillonsun", "vivillontundra", "flabebeblue",
                    "flabebeorange", "flabebewhite", "flabebeyellow", "floetteblue", "floetteorange", "floettewhite", "floetteyellow",
                    "florgesblue", "florgesorange", "florgeswhite", "florgesyellow", "miniororange", "minioryellow", "miniorgreen",
                    "miniorblue", "miniorindigo", "miniorviolet", "magearnaoriginal"};
            for (String s : specialForms) {
                if (name.equals(s)) {
                    // We aren't using switch because of redundancy in names (E.g.: Vivillon)
                    if (s.contains("burmy")) {
                        name = "burmy";
                    } else if (s.contains("shellos")) {
                        name = "shellos";
                    } else if (s.contains("gastrodon")) {
                        name = "gastrodon";
                    } else if (s.contains("deerling")) {
                        name = "deerling";
                    } else if (s.contains("sawsbuck")) {
                        name = "sawsbuck";
                    } else if (s.contains("vivillon")) {
                        name = "vivillon";
                    } else if (s.contains("flabebe")) {
                        name = "flabebe";
                    } else if (s.contains("floette")) {
                        name = "floette";
                    } else if (s.contains("florges")) {
                        name = "florges";
                    } else if (s.contains("minior")) {
                        name = "minior";
                    } else if (s.contains("magearna")) {
                        name = "magearna";
                    }
                    break;
                }
            }

            JSONObject jsonObject = Pokedex.get(appContext).getPokemonJSONObject(name);
            if (name.equals(unaltered)) {
                initializePokemon(appContext, jsonObject, false, unaltered);
            } else {
                initializePokemon(appContext, jsonObject, true, unaltered);
            }
        } catch (NullPointerException e) {
            Log.e(PTAG, "Can't find pokemon " + name + " with error log " + e.toString());
        }
    }

    public static int getPokemonFrontSprite(Context appContext, String name, boolean back, boolean female, boolean shiny) {
        try {
            name = MyApplication.toId(name);
            String prefix = (shiny) ? "sprshiny_front_" : "sprites_front_";
            int toReturn;
            if (female) {
                String drawableName = prefix + name + "f";
                toReturn = appContext.getResources().getIdentifier(drawableName, "drawable", appContext.getPackageName());
                if (toReturn == 0) {
                    drawableName = prefix + name;
                    toReturn = appContext.getResources().getIdentifier(drawableName, "drawable", appContext.getPackageName());
                }
            } else {
                String drawableName = prefix + name;
                toReturn = appContext.getResources().getIdentifier(drawableName, "drawable", appContext.getPackageName());
            }
            //return (toReturn == 0) ? R.drawable.sprites_0 : toReturn;
            return toReturn;
        } catch (NullPointerException e) {
            //return R.drawable.sprites_0;
        }

        return 0;
    }

    public static int getPokemonBackSprite(Context appContext, String name, boolean back, boolean female, boolean shiny) {
        try {
            name = MyApplication.toId(name);
            String prefix = (shiny) ? "sprshiny_back_" : "sprites_back_";
            int toReturn;
            if (female) {
                //String drawableName = prefix + name + "f";
                String drawableName = prefix + name;
                toReturn = appContext.getResources().getIdentifier(drawableName, "drawable", appContext.getPackageName());
                if (toReturn == 0) {
                    drawableName = prefix + name;
                    toReturn = appContext.getResources().getIdentifier(drawableName, "drawable", appContext.getPackageName());
                }
            } else {
                String drawableName = prefix + name;
                toReturn = appContext.getResources().getIdentifier(drawableName, "drawable", appContext.getPackageName());
            }
            //return (toReturn == 0) ? R.drawable.sprites_0 : toReturn;
            return toReturn;
        } catch (NullPointerException e) {
            //return R.drawable.sprites_0;
        }

        return 0;
    }

    public static int getPokemonIcon(Context appContext, String name) {
        try {
            name = name.toLowerCase().replace("-", "").trim();
            name = MyApplication.toId(name);

            int toReturn = appContext.getResources()
                    .getIdentifier("smallicons_" + name, "drawable", appContext.getPackageName());
            return (toReturn == 0) ? R.drawable.smallicons_0 : toReturn;
        } catch (NullPointerException e) {
            return R.drawable.smallicons_0;
        }
    }

    public static Pokemon importPokemon(String importString, Context appContext, boolean withAppContext) {
        String[] pokemonStrings = importString.split("\n");
        if (pokemonStrings.length == 0) {
            return null;
        }

        String pokemonMainData = "";
        // We need to bypass g between mons on the import, causing split[0] to be != null
        for (String s : pokemonStrings) {
            if (!s.trim().isEmpty()) {
                pokemonMainData = s; // is Name @ Item or Name or nickname (Name) or  nickname (Name) @ Item
                break;
            }
        }
        String pokemonName = "", pokemonNickname = null, pokemonItem = null, pokemonGender = null;
        Pokemon p = null;
        boolean isGender = false; // no nickname, but gender

        if (pokemonMainData.contains("@")) {
            String[] nameItem = pokemonMainData.split("@");
            pokemonItem = nameItem[1];
            pokemonMainData = nameItem[0];
        }

        if (pokemonMainData.contains("(") && pokemonMainData.contains(")")) {
            int countOpen = pokemonMainData.length() - pokemonMainData.replace("(", "").length();
            int countClosed = pokemonMainData.length() - pokemonMainData.replace(")", "").length();

            if (countOpen == 1 && countClosed == 1) {
                // either name or gender
                String genderOrName = pokemonMainData.substring(pokemonMainData.lastIndexOf("(") + 1, pokemonMainData.lastIndexOf(")"));

                if (genderOrName.equals("M") || genderOrName.equals("F") || genderOrName.equals("N")) {
                    pokemonGender = genderOrName;
                    pokemonName = pokemonMainData.substring(0, pokemonMainData.lastIndexOf("("));
                } else {
                    pokemonName = genderOrName;
                    pokemonNickname = pokemonMainData.substring(0, pokemonMainData.lastIndexOf("("));
                }
            } else {
                // both name + gender
                String genderOrName = pokemonMainData.substring(pokemonMainData.lastIndexOf("(") + 1, pokemonMainData.lastIndexOf(")"));

                if (genderOrName.equals("M") || genderOrName.equals("F") || genderOrName.equals("N")) {
                    pokemonGender = genderOrName;
                    pokemonMainData = pokemonMainData.substring(0, pokemonMainData.lastIndexOf("("));
                    pokemonName = pokemonMainData.substring(pokemonMainData.lastIndexOf("(") + 1, pokemonMainData.lastIndexOf(")"));
                    pokemonNickname = pokemonMainData.substring(0, pokemonMainData.lastIndexOf("("));
                } else {
                    // is nickname with ()()() and (name)
                    pokemonName = genderOrName;
                    pokemonNickname = pokemonMainData.substring(0, pokemonMainData.lastIndexOf("("));
                }
            }
        } else {
            pokemonName = pokemonMainData;
        }

        //Toast.makeText(appContext, pokemonName, Toast.LENGTH_SHORT).show();
        // replace for different formes

        pokemonName = MyApplication.toId(pokemonName);
        try {
            p = new Pokemon(appContext, pokemonName);
        } catch (NullPointerException e) {
            return null;
        }

        if (pokemonNickname != null) {
            p.setNickName(pokemonNickname.trim());
        }

        if (pokemonItem != null) {
            p.setItem(MyApplication.toId(pokemonItem));
        }

        if (pokemonGender != null) {
            if (pokemonGender.equals("M") || pokemonGender.equals("F") || pokemonGender.equals("N")) {
                p.setGender(pokemonGender);
            }
        }

        int currentMoveId = 1;
        for (int i = 1; i < pokemonStrings.length; i++) {
            String currentString = pokemonStrings[i];
            if (currentString.contains("-")) {
                // its a move!
                // same as items, it's a real name , we need an id. Lowercasing and removing spaces  + - should do the trick
                String move = currentString.substring(currentString.indexOf("-") + 1);
                move = MyApplication.toId(move);
                switch (currentMoveId) {
                    case 1:
                        p.setMove1(move);
                        break;
                    case 2:
                        p.setMove2(move);
                        break;
                    case 3:
                        p.setMove3(move);
                        break;
                    case 4:
                        p.setMove4(move);
                        break;
                }
                currentMoveId++;
            } else if (currentString.contains("IVs:")) {
                String ivs = currentString.substring(currentString.indexOf(":") + 1);
                String[] ivsSplit = ivs.split("/");

                for (String iv : ivsSplit) {
                    iv = iv.trim();
                    String value, stat;
                    String[] valueStat = iv.split(" ");
                    stat = valueStat[1];
                    value = valueStat[0];
                    int ivValue;
                    try {
                        ivValue = Integer.parseInt(value.trim());
                    } catch (NumberFormatException e) {
                        continue;
                    }
                    switch (stat) {
                        case "HP":
                            p.setHPIV(ivValue);
                            break;
                        case "Atk":
                            p.setAtkIV(ivValue);
                            break;
                        case "Def":
                            p.setDefIV(ivValue);
                            break;
                        case "SpA":
                            p.setSpAtkIV(ivValue);
                            break;
                        case "SpD":
                            p.setSpDefIV(ivValue);
                            break;
                        case "Spe":
                            p.setSpdIV(ivValue);
                            break;
                    }
                }
            } else if (currentString.contains("EVs:")) {
                String evs = currentString.substring(currentString.indexOf(":") + 1);
                String[] evssplit = evs.split("/");

                for (String ev : evssplit) {
                    ev = ev.trim();
                    String value, stat;
                    String[] valueStat = ev.split(" ");
                    stat = valueStat[1];
                    value = valueStat[0];
                    int ivValue;
                    try {
                        ivValue = Integer.parseInt(value.trim());
                    } catch (NumberFormatException e) {
                        continue;
                    }
                    switch (stat) {
                        case "HP":
                            p.setHPEV(ivValue);
                            break;
                        case "Atk":
                            p.setAtkEV(ivValue);
                            break;
                        case "Def":
                            p.setDefEV(ivValue);
                            break;
                        case "SpA":
                            p.setSpAtkEV(ivValue);
                            break;
                        case "SpD":
                            p.setSpDefEV(ivValue);
                            break;
                        case "Spe":
                            p.setSpdEV(ivValue);
                            break;
                    }
                }
            } else if (currentString.contains("Nature")) {
                String nature = currentString.substring(0, currentString.indexOf("Nature")).trim();
                p.setNature(nature);
            } else if (currentString.contains("Ability:")) {
                String abilityName = currentString.substring(currentString.indexOf(":") + 1).trim();

                for (String s : p.getAbilityList().keySet()) {
                    if (p.getAbilityList().get(s).equals(abilityName)) {
                        p.setAbilityTag(s);
                        break;
                    }
                }
            } else if (currentString.contains("Level:")) {
                String level = currentString.substring(currentString.indexOf(":") + 1).trim();
                try {
                    p.setLevel(Integer.parseInt(level));
                } catch (NumberFormatException e) {
                    break;
                }
            } else if (currentString.contains("Shiny")) {
                if (!p.isShiny()) {
                    p.switchFrontShiny(appContext, true);
                    p.switchBackShiny(appContext, true);
                }
            }
        }

        Log.d("dfjd", p.exportPokemon(appContext));
        return p;
    }

    public static String getPokemonName(Context appContext, String name) {
        try {
            name = MyApplication.toId(name);
            JSONObject jsonObject = Pokedex.get(appContext).getPokemonJSONObject(name);
            return jsonObject.getString("species");
        } catch (JSONException e) {
            Log.d(PTAG, e.toString());
        } catch (NullPointerException e) {
            return "???";
        }
        return "???";
    }

    public static Integer[] getPokemonBaseStats(Context appContext, String name) {
        try {
            JSONObject jsonObject = Pokedex.get(appContext).getPokemonJSONObject(name);
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
        } catch (NullPointerException e) {
            return null;
        }
        return null;
    }

    public static String[] getPokemonAbilities(Context appContext, String name) {
        try {
            JSONObject jsonObject = Pokedex.get(appContext).getPokemonJSONObject(name);
            jsonObject = (JSONObject) jsonObject.get("abilities");
            Iterator<String> keys = jsonObject.keys();
            String[] abilities = new String[jsonObject.length()];
            int i = 0;
            while (keys.hasNext()) {
                String key = keys.next();
                abilities[i] = jsonObject.getString(key);
                i++;
            }
            return abilities;
        } catch (JSONException ex) {
            Log.d(PTAG, ex.toString());
        }

        return null;
    }

    public static Integer[] getPokemonTypeIcon(Context appContext, String name) {
        try {
            JSONObject jsonObject = Pokedex.get(appContext).getPokemonJSONObject(name);
            JSONArray types = jsonObject.getJSONArray("types");
            String[] typesString = new String[types.length()];
            Integer[] typesIcon = new Integer[types.length()];
            for (int i = 0; i < types.length(); i++) {
                typesString[i] = types.get(i).toString();
                typesIcon[i] = appContext.getResources()
                        .getIdentifier("types_" + typesString[i].toLowerCase(), "drawable", appContext.getPackageName());
            }
            return typesIcon;
        } catch (JSONException e) {
            Log.d(PTAG, e.toString());
        } catch (NullPointerException e) {
            return null;
        }
        return null;
    }

    public static int getGenderIcon(String gender) {
        if (gender == null) {
            return 0;
        }

        switch (gender) {
            case "M":
                return R.drawable.ic_gender_male;
            case "F":
                return R.drawable.ic_gender_female;
            default:
                return 0;
        }
    }

    public static int[] calculateStats(int[] baseStats, int[] IVs, int[] EVs, int[] stages, int level, int[] natureMultiplier) {
        int[] stats = new int[6];
        stats[0] = calculateHP(baseStats[0], IVs[0], EVs[0], level);
        stats[1] = calculateAtk(baseStats[1], IVs[1], EVs[1], stages[1], level, natureMultiplier[1]);
        stats[2] = calculateDef(baseStats[2], IVs[2], EVs[2], stages[1], level, natureMultiplier[2]);
        stats[3] = calculateSpAtk(baseStats[3], IVs[3], EVs[3], stages[1], level, natureMultiplier[3]);
        stats[4] = calculateSpDef(baseStats[4], IVs[4], EVs[4], stages[1], level, natureMultiplier[4]);
        stats[5] = calculateSpd(baseStats[5], IVs[5], EVs[5], stages[1], level, natureMultiplier[5]);
        return stats;
    }

    public static int calculateHP(int baseHP, int HPIV, int HPEV, int level) {
        return ((HPIV + 2 * baseHP + HPEV / 4 + 100) * level / 100 + 10);
    }

    public static int calculateAtk(int baseAtk, int AtkIV, int AtkEV, int atkStage, int level, float natureMultiplier) {
        return (int) (((AtkIV + 2 * baseAtk + AtkEV / 4) * level / 100 + 5) * natureMultiplier * STAGES_MAIN_STATS[atkStage]);
    }

    public static int calculateDef(int baseDef, int DefIV, int DefEV, int defStages, int level, float natureMultiplier) {
        return (int) (((DefIV + 2 * baseDef + DefEV / 4) * level / 100 + 5) * natureMultiplier * STAGES_MAIN_STATS[defStages]);
    }

    public static int calculateSpAtk(int baseSpAtk, int SpAtkIV, int SpAtkEV, int spAtkStages, int level, float natureMultiplier) {
        return (int) (((SpAtkIV + 2 * baseSpAtk + SpAtkEV / 4) * level / 100 + 5) * natureMultiplier * STAGES_MAIN_STATS[spAtkStages]);
    }

    public static int calculateSpDef(int baseSpDef, int SpDefIV, int SpDefEV, int spDefStages, int level, float natureMultiplier) {
        return (int) (((SpDefIV + 2 * baseSpDef + SpDefEV / 4) * level / 100 + 5) * natureMultiplier * STAGES_MAIN_STATS[spDefStages]);
    }

    public static int calculateSpd(int baseSpd, int SpdIV, int SpdEV, int spdStages, int level, float natureMultiplier) {
        return (int) (((SpdIV + 2 * baseSpd + SpdEV / 4) * level / 100 + 5) * natureMultiplier * STAGES_MAIN_STATS[spdStages]);
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

    private void initializePokemon(Context appContext, JSONObject jsonObject, boolean isForm, String form) {
        try {
            if (!isForm) {
                mName = jsonObject.getString("species");
            } else {
                int size = jsonObject.getString("species").length();
                form = form.substring(size);
                mName = jsonObject.getString("species");
                mBaseName = jsonObject.getString("species") + "-" + form.substring(0, 1).toUpperCase() + form.substring(1, form.length());
            }

            mFrontSprite = getPokemonFrontSprite(appContext, mBaseName == null ? mName : mBaseName, false, false, false);
            mBackSprite = getPokemonBackSprite(appContext, mBaseName == null ? mName : mBaseName, false, false, false);
            mIcon = getPokemonIcon(appContext, mBaseName == null ? mName : mBaseName);

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

            mStages = new int[6];
            Arrays.fill(mStages, 6); // Neutral Stage

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
                mType[i] = types.getString(i);
                mTypeIcon[i] = appContext.getResources()
                        .getIdentifier("types_" + mType[i].toLowerCase(), "drawable", appContext.getPackageName());
            }

            JSONObject abilityList = (JSONObject) jsonObject.get("abilities");
            Iterator<String> keys = abilityList.keys();
            mAbilityList = new HashMap<>();
            while (keys.hasNext()) {
                String key = keys.next();
                mAbilityList.put(key, abilityList.getString(key));
            }
            setAbilityTag("0");

            setWeight(Double.parseDouble(jsonObject.getString("weightkg")));

            setItem("");

            setMove1("");
            setMove2("");
            setMove3("");
            setMove4("");
        } catch (JSONException e) {
            Log.d(PTAG, e.toString());
        }
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
        if (mName.equals("Shedinja")) {
            return 1;
        }
        return ((getHPIV() + 2 * getBaseHP() + getHPEV() / 4 + 100) * getLevel() / 100 + 10);
    }

    public int calculateAtk() {
        return (int) (((getAtkIV() + 2 * getBaseAtk() + getAtkEV() / 4) * getLevel() / 100 + 5) * mNatureMultiplier[1] * STAGES_MAIN_STATS[mStages[1]]);
    }

    public int calculateDef() {
        return (int) (((getDefIV() + 2 * getBaseDef() + getDefEV() / 4) * getLevel() / 100 + 5) * mNatureMultiplier[2] * STAGES_MAIN_STATS[mStages[2]]);
    }

    public int calculateSpAtk() {
        return (int) (((getSpAtkIV() + 2 * getBaseSpAtk() + getSpAtkEV() / 4) * getLevel() / 100 + 5) * mNatureMultiplier[3] * STAGES_MAIN_STATS[mStages[3]]);
    }

    public int calculateSpDef() {
        return (int) (((getSpDefIV() + 2 * getBaseSpDef() + getSpDefEV() / 4) * getLevel() / 100 + 5) * mNatureMultiplier[4] * STAGES_MAIN_STATS[mStages[4]]);
    }

    public int calculateSpd() {
        return (int) (((getSpdIV() + 2 * getBaseSpd() + getSpdEV() / 4) * getLevel() / 100 + 5) * mNatureMultiplier[5] * STAGES_MAIN_STATS[mStages[5]]);
    }

    public int getHPIV() {
        return mIVs[0];
    }

    public void setHPIV(int HP) {
        mIVs[0] = HP;
    }

    public int getBaseHP() {
        return mBaseStats[0];
    }

    public void setBaseHP(int i) {
        mBaseStats[0] = i;
    }

    public int getHPEV() {
        return mEVs[0];
    }

    public void setHPEV(int HP) {
        mEVs[0] = HP;
    }

    public int getLevel() {
        return mLevel;
    }

    public void setLevel(int level) {
        mLevel = level;
    }

    public int getAtkIV() {
        return mIVs[1];
    }

    public void setAtkIV(int Atk) {
        mIVs[1] = Atk;
    }

    public int getBaseAtk() {
        return mBaseStats[1];
    }

    public void setBaseAtk(int i) {
        mBaseStats[1] = i;
    }

    public int getAtkEV() {
        return mEVs[1];
    }

    public void setAtkEV(int Atk) {
        mEVs[1] = Atk;
    }

    public int getDefIV() {
        return mIVs[2];
    }

    public void setDefIV(int Def) {
        mIVs[2] = Def;
    }

    public int getBaseDef() {
        return mBaseStats[2];
    }

    public void setBaseDef(int i) {
        mBaseStats[2] = i;
    }

    public int getDefEV() {
        return mEVs[2];
    }

    public void setDefEV(int Def) {
        mEVs[2] = Def;
    }

    public int getSpAtkIV() {
        return mIVs[3];
    }

    public void setSpAtkIV(int SpAtk) {
        mIVs[3] = SpAtk;
    }

    public int getBaseSpAtk() {
        return mBaseStats[3];
    }

    public void setBaseSpAtk(int i) {
        mBaseStats[3] = i;
    }

    public int getSpAtkEV() {
        return mEVs[3];
    }

    public void setSpAtkEV(int SpAtk) {
        mEVs[3] = SpAtk;
    }

    public int getSpDefIV() {
        return mIVs[4];
    }

    public void setSpDefIV(int SpDef) {
        mIVs[4] = SpDef;
    }

    public int getBaseSpDef() {
        return mBaseStats[4];
    }

    public void setBaseSpDef(int i) {
        mBaseStats[4] = i;
    }

    public int getSpDefEV() {
        return mEVs[4];
    }

    public void setSpDefEV(int SpDef) {
        mEVs[4] = SpDef;
    }

    public int getSpdIV() {
        return mIVs[5];
    }

    public void setSpdIV(int Spd) {
        mIVs[5] = Spd;
    }

    public int getBaseSpd() {
        return mBaseStats[5];
    }

    public void setBaseSpd(int i) {
        mBaseStats[5] = i;
    }

    public int getSpdEV() {
        return mEVs[5];
    }

    public void setSpdEV(int Spd) {
        mEVs[5] = Spd;
    }

    public HashMap<String, String> getAbilityList() {
        return mAbilityList;
    }

    public boolean isShiny() {
        return mShiny;
    }

    public void setShiny(boolean shiny) {
        mShiny = shiny;
    }

    public void switchFrontShiny(Context c, boolean shiny) {
        setShiny(shiny);
        setFrontSprite(getPokemonFrontSprite(c, mBaseName == null ? mName : mBaseName, false, getGender().equals("F"), isShiny()));
    }

    public void switchBackShiny(Context c, boolean shiny) {
        setShiny(shiny);
        setBackSprite(getPokemonBackSprite(c, mBaseName == null ? mName : mBaseName, false, getGender().equals("F"), isShiny()));
    }

    public String getGender() {
        return mGender;
    }

    public void setGender(String gender) {
        mGender = gender;
    }

    public String exportForVerification() {
        StringBuilder sb = new StringBuilder();
        if (!getNickName().equals(getName())) {
            sb.append(getNickName()).append("|").append(getName());
        } else {
            sb.append(getName()).append("|");
        }
        sb.append("|");
        sb.append(getItem()).append("|");
        sb.append(getAbilityTag()).append("|");
        if (!getMove1().equals("")) {
            sb.append(getMove1()).append(",");
        }
        if (!getMove2().equals("")) {
            sb.append(getMove2()).append(",");
        }
        if (!getMove3().equals("")) {
            sb.append(getMove3()).append(",");
        }
        if (!getMove4().equals("")) {
            sb.append(getMove4());
        }
        sb.append("|");
        sb.append(getNature()).append("|");
        //evs
        sb.append(getHPEV()).append(",");
        sb.append(getAtkEV()).append(",");
        sb.append(getDefEV()).append(",");
        sb.append(getSpAtkEV()).append(",");
        sb.append(getSpDefEV()).append(",");
        sb.append(getSpdEV()).append("|");
        //gender
        sb.append(getGender()).append("|");
        //ivs
        sb.append(getHPIV()).append(",");
        sb.append(getAtkIV()).append(",");
        sb.append(getDefIV()).append(",");
        sb.append(getSpAtkIV()).append(",");
        sb.append(getSpDefIV()).append(",");
        sb.append(getSpdIV()).append("|");

        // shiny
        if (isShiny()) {
            sb.append("S");
        }
        sb.append("|");

        //level
        sb.append(getLevel()).append("|");
        sb.append(getHappiness());
        return sb.toString();
    }

    public String getNickName() {
        return mNickName;
    }

    public void setNickName(String nickName) {
        mNickName = nickName;
    }

    public String getName() {
        return mBaseName == null ? mName : mBaseName;
    }

    // Necessary for getting the unchanged form of the pokemon when selecting another form on PokemonActivity
    public String getRealName() {
        return mName;
    }

    public String getItem() {
        return mItem;
    }

    public void setItem(String item) {
        mItem = item;
    }

    public String getAbilityTag() {
        return mAbility;
    }

    public void setAbilityTag(String abilityTag) {
        mAbility = abilityTag;
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

    public String getNature() {
        return mNature;
    }

    public void setNature(String nature) {
        mNature = nature;
        setNatureMultiplier(nature);
    }

    public int getHappiness() {
        return mHappiness;
    }

    public void setHappiness(int happiness) {
        mHappiness = happiness;
    }

    /**
     * Exporting function
     *
     * @return A string with the pokemon using Showdown! format
     */
    public String exportPokemon(Context appContext) {
        StringBuilder sb = new StringBuilder();
        if (getName() == null) {
            return "";
        }

        if (getName().length() > 0) {
            if (!getNickName().equals(getName())) {
                sb.append(getNickName()).append(" (").append(getName()).append(")");
            } else {
                sb.append(getName());
            }
            if (!getGender().toUpperCase().equals("N")) {
                sb.append(" (").append(getGender().toUpperCase()).append(")");
            }
            if (getItem().length() > 0) {
                JSONObject itemJSon = ItemDex.get(appContext).getItemJsonObject(getItem());
                if (itemJSon != null) {
                    try {
                        String itemName = itemJSon.getString("name");
                        sb.append(" @ ").append(itemName);
                    } catch (JSONException e) {
                        Log.e(PTAG, e.toString());
                    }
                }

            }
            sb.append("\n");
        }
        if (getAbility(appContext).length() > 0) {
            sb.append("Ability: ").append(getAbility(appContext)).append("\n");
        }

        if (getLevel() != 100) {
            sb.append("Level: ").append(getLevel()).append("\n");
        }

        if (isShiny()) {
            sb.append("Shiny: Yes\n");
        }

        if (getHappiness() != 255) {
            sb.append("Happiness: ").append(getHappiness()).append("\n");
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
                    sb.append(getHPEV()).append(" HP ");
                    first = false;
                }
            }
            if (getAtkEV() != 0) {
                if (first) {
                    sb.append(getAtkEV()).append(" Atk ");
                    first = false;

                } else {
                    sb.append("/ ").append(getAtkEV()).append(" Atk ");
                }
            }
            if (getDefEV() != 0) {
                if (first) {
                    sb.append(getDefEV()).append(" Def ");
                    first = false;

                } else {
                    sb.append("/ ").append(getDefEV()).append(" Def ");
                }
            }
            if (getSpAtkEV() != 0) {
                if (first) {
                    sb.append(getSpAtkEV()).append(" SpA ");
                    first = false;

                } else {
                    sb.append("/ ").append(getSpAtkEV()).append(" SpA ");
                }
            }
            if (getSpDefEV() != 0) {
                if (first) {
                    sb.append(getSpDefEV()).append(" SpD ");
                    first = false;

                } else {
                    sb.append("/ ").append(getSpDefEV()).append(" SpD ");
                }
            }
            if (getSpdEV() != 0) {
                if (first) {
                    sb.append(getSpdEV()).append(" Spe ");
                    first = false;
                } else {
                    sb.append("/ ").append(getSpdEV()).append(" Spe ");
                }
            }
            sb.append("\n");
        }

        // IVS
        difZero = false;
        for (int i : getIVs()) {
            if (i != 31) {
                difZero = true;
                break;
            }
        }
        if (difZero) {
            boolean first = true;

            sb.append("IVs: ");
            if (getHPIV() != 31) {
                if (first) {
                    sb.append(getHPIV()).append(" HP ");
                    first = false;
                }
            }
            if (getAtkIV() != 31) {
                if (first) {
                    sb.append(getAtkIV()).append(" Atk ");
                    first = false;
                } else {
                    sb.append("/ ").append(getAtkIV()).append(" Atk ");
                }
            }
            if (getDefIV() != 31) {
                if (first) {
                    sb.append(getDefIV()).append(" Def ");
                    first = false;

                } else {
                    sb.append("/ ").append(getDefIV()).append(" Def ");
                }
            }
            if (getSpAtkIV() != 31) {
                if (first) {
                    sb.append(getSpAtkIV()).append(" SpA ");
                    first = false;

                } else {
                    sb.append("/ ").append(getSpAtkIV()).append(" SpA ");
                }
            }
            if (getSpDefIV() != 31) {
                if (first) {
                    sb.append(getSpDefIV()).append(" SpD ");
                    first = false;

                } else {
                    sb.append("/ ").append(getSpDefIV()).append(" SpD ");
                }
            }
            if (getSpdIV() != 31) {
                if (first) {
                    sb.append(getSpdIV()).append(" Spe ");
                    first = false;
                } else {
                    sb.append("/ ").append(getSpdIV()).append(" Spe ");
                }
            }
            sb.append("\n");
        }

        if (getNature().length() > 0) {
            sb.append(getNature()).append(" Nature").append("\n");
        }

        // moves
        boolean noMoves = true;
        if (!getMove1().equals("")) {
            noMoves = false;
        }
        if (!getMove2().equals("")) {
            noMoves = false;
        }
        if (!getMove3().equals("")) {
            noMoves = false;
        }
        if (!getMove4().equals("")) {
            noMoves = false;
        }
        if (!noMoves) {
            if (!getMove1().equals("")) {
                JSONObject move1Object = MoveDex.get(appContext).getMoveJsonObject(getMove1());
                if (move1Object != null) {
                    try {
                        String realMove = move1Object.getString("name");
                        sb.append("- ").append(realMove).append("\n");
                    } catch (JSONException e) {
                        Log.e(PTAG, e.toString());
                    }
                }
            }

            if (!getMove2().equals("")) {
                JSONObject move2Object = MoveDex.get(appContext).getMoveJsonObject(getMove2());
                if (move2Object != null) {
                    try {
                        String realMove = move2Object.getString("name");
                        sb.append("- ").append(realMove).append("\n");
                    } catch (JSONException e) {
                        Log.e(PTAG, e.toString());
                    }
                }
            }

            if (!getMove3().equals("")) {
                JSONObject move3Object = MoveDex.get(appContext).getMoveJsonObject(getMove3());
                if (move3Object != null) {
                    try {
                        String realMove = move3Object.getString("name");
                        sb.append("- ").append(realMove).append("\n");
                    } catch (JSONException e) {
                        Log.e(PTAG, e.toString());
                    }
                }
            }

            if (!getMove4().equals("")) {
                JSONObject move4Object = MoveDex.get(appContext).getMoveJsonObject(getMove4());
                if (move4Object != null) {
                    try {
                        String realMove = move4Object.getString("name");
                        sb.append("- ").append(realMove).append("\n");
                    } catch (JSONException e) {
                        Log.e(PTAG, e.toString());
                    }
                }
            }
        }

        return sb.toString();
    }

    public String getAbility(Context context) {
        // Necessary maneuvers to counter Null Exceptions when giving up from choosing a custom ability
        // When having a custom ability already.
        try {
            String ability = getAbilityList().get(mAbility);
            if (ability == null || ability.equals("null") || ability.isEmpty()) {
                return AbilityDex.get(context).getAbilityJsonObject(mAbility).getString("name");
            }
            return getAbilityList().get(mAbility);
        } catch (Exception ex) {
            try {
                JSONObject jsonObject = Pokedex.get(context).getPokemonJSONObject(getName());
                JSONObject abilityList = (JSONObject) jsonObject.get("abilities");
                Iterator<String> keys = abilityList.keys();
                mAbilityList = new HashMap<>();

                while (keys.hasNext()) {
                    String key = keys.next();
                    mAbilityList.put(key, abilityList.getString(key));
                }

                setAbilityTag("0");

                Toast.makeText(context, "Failed to set a custom ability. Setting the first available instead", Toast.LENGTH_SHORT).show();
                return getAbilityList().get("0");
            } catch (JSONException ex2) {
                return null;
            }
        }
    }

    public int[] getEVs() {
        return mEVs;
    }

    public void setEVs(int[] EVs) {
        mEVs = EVs;
    }

    public int[] getIVs() {
        return mIVs;
    }

    public void setIVs(int[] IVs) {
        mIVs = IVs;
    }

    public int getFrontSprite() {
        return mFrontSprite;
    }

    public void setFrontSprite(int sprite) {
        mFrontSprite = sprite;
    }

    public int getBackSprite() {
        return mBackSprite;
    }

    public void setBackSprite(int sprite) {
        mBackSprite = sprite;
    }

    public int getIcon() {
        return mIcon;
    }

    public void setIcon(int icon) {
        mIcon = icon;
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

    public int[] getStages() {
        return mStages;
    }

    public void setStages(int[] stages) {
        mStages = stages;
    }

    public int getAtkStages() {
        return mStages[1];
    }

    public void setAtkStages(int atk) {
        mStages[1] = atk;
    }

    public int getDefStages() {
        return mStages[2];
    }

    public void setDefStages(int def) {
        mStages[2] = def;
    }

    public int getSpAtkStages() {
        return mStages[3];
    }

    public void setSpAtkStages(int spAtk) {
        mStages[4] = spAtk;
    }

    public int getSpDefStages() {
        return mStages[4];
    }

    public void setSpDefStages(int spDef) {
        mStages[4] = spDef;
    }

    public int getSpdStages() {
        return mStages[5];
    }

    public void setSpdStages(int spd) {
        mStages[5] = spd;
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

    public void switchGender(Context c) {
        if (!isGenderAvailable()) {
            return;
        }
        switch (mGender) {
            case "M":
                setGender("F");
                setFrontSprite(getPokemonFrontSprite(c, mName, false, true, isShiny()));
                setIcon(getPokemonIcon(c, mName));
                return;
            case "F":
                setGender("M");
                setFrontSprite(getPokemonFrontSprite(c, mName, false, false, isShiny()));
                setIcon(getPokemonIcon(c, mName));
        }
    }

    public boolean isGenderAvailable() {
        return mGenderAvailable;
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
            mNatureMultiplier[4] = 1.1f;
            mNatureMultiplier[3] = 0.9f;
        }
        if (nature.equals("Gentle")) {
            mNatureMultiplier[4] = 1.1f;
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
            mNatureMultiplier[4] = 0.9f;
        }
        if (nature.equals("Quiet")) {
            mNatureMultiplier[3] = 1.1f;
            mNatureMultiplier[5] = 0.9f;
        }
        if (nature.equals("Rash")) {
            mNatureMultiplier[3] = 1.1f;
            mNatureMultiplier[4] = 0.9f;
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

    public double getWeight() {
        return mWeight;
    }

    public void setWeight(double weight) {
        mWeight = weight;
    }

    public String getMove(int moveId) {
        switch (moveId) {
            case 1:
                return mMove1;
            case 2:
                return mMove2;
            case 3:
                return mMove3;
            case 4:
                return mMove4;
            default:
                return null;
        }
    }

    public void setMove(int moveId, String move) {
        switch (moveId) {
            case 1:
                mMove1 = move;
                break;
            case 2:
                mMove2 = move;
                break;
            case 3:
                mMove3 = move;
                break;
            case 4:
                mMove4 = move;
                break;
        }
    }
}
