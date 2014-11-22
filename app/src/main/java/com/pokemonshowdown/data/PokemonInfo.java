package com.pokemonshowdown.data;

import android.content.Context;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

public class PokemonInfo implements Serializable {
    private String mName;
    private String mNickname;
    private String mNickName;
    private int[] mTypeIcon;
    private int mLevel;
    private String mGender;
    private boolean mShiny;
    private boolean mActive;
    private int mHp;
    private String mStatus;
    private int[] mStats;
    private HashMap<String, Integer> mMoves;
    private String mAbility;
    private String mNature;
    private String mItem;

    public PokemonInfo(Context activityContext, String pkm) {
        mName = pkm;
        mNickname = pkm;
        mLevel = 100;
        Pokemon defaultPkm = new Pokemon(activityContext, pkm, false);
        mTypeIcon = defaultPkm.getTypeIcon();
        mGender = null;
        mShiny = false;
        mActive = false;
        mHp = 100;
        mStatus = null;
        mStats = defaultPkm.getBaseStats();
        mMoves = new HashMap<>();
        Collection<String> abilityList = defaultPkm.getAbilityList().values();
        mAbility = "";
        for (String ability : abilityList) {
            mAbility += ability + "/";
        }
        mAbility = mAbility.substring(0, mAbility.length() - 1);
        mNature = null;
        mItem = null;
    }

    public int getIcon(Context appContext, boolean withAppContext) {
        return Pokemon.getPokemonIcon(appContext, MyApplication.toId(mName), withAppContext);
    }

    public int getSprite(Context appContext, boolean withAppContext) {
        String gender = mGender;
        if (gender == null) {
            gender = "";
        }
        return Pokemon.getPokemonSprite(appContext, MyApplication.toId(mName), withAppContext, false, (gender.equals("F")), mShiny);
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

    public String getNickName() {
        return mNickName;
    }

    public void setNickName(String nickName) {
        mNickName = nickName;
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
        mStats = stats;
    }

    public HashMap<String, Integer> getMoves() {
        return mMoves;
    }

    public void setMoves(HashMap<String, Integer> moves) {
        mMoves = moves;
    }

    public String getAbility() {
        return mAbility;
    }

    public void setAbility(String ability) {
        mAbility = ability;
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
        mItem = item;
    }

}
