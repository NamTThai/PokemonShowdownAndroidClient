package com.pokemonshowdown.data;

import android.content.Context;

import com.pokemonshowdown.application.MyApplication;

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
    private boolean mCanMegaEvo;

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
        setCanMegaEvo(false);
    }

    public void setAbility(String ability) {
        mAbility = MyApplication.toId(ability);
    }

    public void setCanMegaEvo(boolean canMegaEvo) {
        mCanMegaEvo = canMegaEvo;
    }

    public int getIcon(Context appContext) {
        return Pokemon.getPokemonIcon(appContext, MyApplication.toId(mName));
    }

    public int getSprite(Context appContext) {
        String gender = mGender;
        if (gender == null) {
            gender = "";
        }
        return Pokemon.getPokemonSprite(appContext, MyApplication.toId(mName), false, (gender.equals("F")), mShiny);
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
        return mCanMegaEvo;
    }
}
