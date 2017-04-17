package com.pokemonshowdown.data;

import android.content.Context;

import com.pokemonshowdown.application.MyApplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

public class PokemonInfo implements Serializable {
    public final static String STATUS_BURN = "brn";
    public final static String STATUS_FREEZE = "frz";
    public final static String STATUS_TOXIC = "tox";
    public final static String STATUS_POISON = "psn";
    public final static String STATUS_PARALYZE = "par";
    public final static String STATUS_SLEEP = "slp";

    private String mName;
    private String mNickname;
    private int[] mTypeIcon;
    private int mLevel;
    private String mGender;
    private boolean mShiny;
    private boolean mActive;
    private int mHp;
    private String mStatus;
    /**
     * Doesn't include HP
     */
    private int[] mStats;
    private HashMap<String, Integer> mMoves;

    private String mAbility;
    private String mNature;
    private String mItem;

    public PokemonInfo(Context activityContext, String pkm) {
        setName(pkm);
        setLevel(100);
        Pokemon defaultPkm = new Pokemon(activityContext, pkm);
        setNickname(defaultPkm.getNickName());
        setTypeIcon(defaultPkm.getTypeIcon());
        setGender(null);
        setShiny(false);
        setActive(false);
        setHp(100);
        setStatus(null);
        setStats(defaultPkm.getBaseStats());
        setMoves(new HashMap<String, Integer>());
        Collection<String> abilityList = defaultPkm.getAbilityList().values();
        setAbility("");
        for (String ability : abilityList) {
            mAbility += ability + "/";
        }
        mAbility = mAbility.substring(0, mAbility.length() - 1);
        setNature(null);
        setItem(null);
    }

    public void setAbility(String ability) {
        mAbility = MyApplication.toId(ability);
    }

    public int getIcon(Context appContext) {
        return Pokemon.getPokemonIcon(appContext, MyApplication.toId(mName));
    }

    public int getSprite(Context appContext, boolean back) {
        String gender = mGender;
        if (gender == null) {
            gender = "";
        }

        if (!back) {
            return Pokemon.getPokemonFrontSprite(appContext, MyApplication.toId(mName), false, (gender.equals("F")), mShiny);
        } else {
            return Pokemon.getPokemonBackSprite(appContext, MyApplication.toId(mName), false, (gender.equals("F")), mShiny);
        }
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getNickname() {
        return mNickname;
    }

    public void setNickname(String nickname) {
        mNickname = nickname;
    }

    public int getLevel() {
        return mLevel;
    }

    public void setLevel(int level) {
        mLevel = level;
    }

    public int[] getTypeIcon() {
        return mTypeIcon;
    }

    public void setTypeIcon(int[] typeIcon) {
        mTypeIcon = typeIcon;
    }

    public boolean isFemale() {
        return (getGender() != null && getGender().equals("F"));
    }

    public String getGender() {
        return mGender;
    }

    public void setGender(String gender) {
        mGender = gender;
    }

    public boolean isShiny() {
        return mShiny;
    }

    public void setShiny(boolean shiny) {
        mShiny = shiny;
    }

    public boolean isActive() {
        return mActive;
    }

    public void setActive(boolean active) {
        mActive = active;
    }

    public int getHp() {
        return mHp;
    }

    public void setHp(int hp) {
        mHp = hp;
    }

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String status) {
        mStatus = status;
    }

    public int[] getStats() {
        return mStats;
    }

    public void setStats(int[] stats) {
        if (stats != null && stats.length == 6) {
            if (mStats == null) {
                mStats = new int[5];
            }
            System.arraycopy(stats, 1, mStats, 0, 5);
        } else if (stats != null) {
            mStats = stats;
        }
    }

    public HashMap<String, Integer> getMoves() {
        return mMoves;
    }

    public void setMoves(HashMap<String, Integer> moves) {
        mMoves = moves;
    }

    public String getAbilityName(Context appContext) {
        String toReturn = AbilityDex.getAbilityName(appContext, mAbility);
        return (toReturn == null) ? mAbility : toReturn;
    }

    public String getNature() {
        return mNature;
    }

    public void setNature(String nature) {
        mNature = nature;
    }

    public String getItem() {
        return mItem;
    }

    public void setItem(String item) {
        mItem = MyApplication.toId(item);
    }

    public String getItemName(Context activityContext) {
        if (mItem != null) {
            return ItemDex.get(activityContext).getItemName(mItem);
        } else {
            return null;
        }
    }

    public boolean canMegaEvo() {
        String[] allPossibleMegas = new String[]{"Venusaur", "Charizard", "Blastoise", "Alakazam", "Gengar", "Kangaskhan",
                "Pinsir", "Gyarados", "Aerodactyl", "Mewtwo", "Ampharos", "Scizor", "Heracross", "Houndoom", "Tyranitar", "Blaziken", "Gardevoir",
                "Mawile", "Aggron", "Medicham", "Manectric", "Banette", "Absol", "Garchomp", "Lucario", "Abomasnow", "Beedrill",
                "Pidgeot", "Slowbro", "Steelix", "Sceptile", "Swampert", "Sableye", "Sharpedo", "Camerupt", "Altaria", "Glalie",
                "Salamence", "Latias", "Latios", "Metagross", "Lopunny", "Gallade", "Audino", "Diancie"};

        // Since all rules are meant to be broken, Rayquaza doesn't follow the normal rules of mega-evo.
        if (getName().equals("Rayquaza") && getMoves().containsKey("dragonascend")) {
            return true;
        }

        for (String s : allPossibleMegas) {
            String item = getItemName(MyApplication.getMyApplication());
            if (item != null && getName().contains(s) && item.contains(s.substring(0, 5))) {
                return true;
            }
        }

        return false;
    }

    public boolean canZMove(Context context) {
        // All exclusive Z-Moves for mons with specific moves
        if (getName().equals("Decidueye") && getMoves().containsKey("spiritshackle") && getItem().equals("decidiumz") ||
                getName().equals("Incineroar") && getMoves().containsKey("darkestlariat") && getItem().equals("inciniumz") ||
                getName().equals("Primarina") && getMoves().containsKey("sparklingaria") && getItem().equals("primariumz") ||
                getName().equals("Raichu-Alola") && getMoves().containsKey("thunderbolt") && getItem().equals("aloraichiumz") ||
                getName().equals("Eevee") && getMoves().containsKey("lastresort") && getItem().equals("eeviumz") ||
                getName().equals("Marshadow") && getMoves().containsKey("spectralthief") && getItem().equals("marshadiumz") ||
                getName().equals("Mew") && getMoves().containsKey("psychic") && getItem().equals("mewniumz") ||
                getName().equals("Pikachu") && getMoves().containsKey("volttackle") && getItem().equals("pikaniumz") ||
                getName().equals("Pikachu") && getMoves().containsKey("thunderbolt") && getItem().equals("pikashuniumz") ||
                getName().equals("Snorlax") && getMoves().containsKey("gigaimpact") && getItem().equals("snorliumz") ||
                getName().contains("Tapu") && getMoves().containsKey("naturesmadness") && getItem().equals("tapuniumz")) {
            return true;
        }

        for (String s : getMoves().keySet()) {
            try {
                JSONObject obj = MoveDex.get(context).getMoveJsonObject(s);
                switch (obj.getString("type")) {
                    case "Bug":
                        if (getItem().equals("buginiumz")) return true;
                    case "Dark":
                        if (getItem().equals("darkiniumz")) return true;
                    case "Dragon":
                        if (getItem().equals("dragoniumz")) return true;
                    case "Electric":
                        if (getItem().equals("electriumz")) return true;
                    case "Fairy":
                        if (getItem().equals("fairiumz")) return true;
                    case "Fighting":
                        if (getItem().equals("fightiniumz")) return true;
                    case "Fire":
                        if (getItem().equals("firiumz")) return true;
                    case "Flying":
                        if (getItem().equals("flyiniumz")) return true;
                    case "Ghost":
                        if (getItem().equals("ghostiumz")) return true;
                    case "Grass":
                        if (getItem().equals("grassiumz")) return true;
                    case "Ground":
                        if (getItem().equals("groundiumz")) return true;
                    case "Ice":
                        if (getItem().equals("iciumz")) return true;
                    case "Normal":
                        if (getItem().equals("normaliumz")) return true;
                    case "Poison":
                        if (getItem().equals("poisoniumz")) return true;
                    case "Psychic":
                        if (getItem().equals("psychiumz")) return true;
                    case "Rock":
                        if (getItem().equals("rockiumz")) return true;
                    case "Steel":
                        if (getItem().equals("steeliumz")) return true;
                    case "Water":
                        if (getItem().equals("wateriumz")) return true;
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }
}
