package com.pokemonshowdown.app;

import java.io.Serializable;

/**
 * Created by thain on 7/22/14.
 *
 * Array index (including nature array)
 * HP: 0
 * Atk: 1
 * Def: 2
 * SpAtk: 3
 * SpDef: 4
 * Spd: 5
 *
 * Example for Modest mNatureMultiplier array (+SpAtk, -Atk) [1.0, 0.9, 1.0, 1.1, 1.0, 1.0]
 */
public class Pokemon implements Serializable {
    private final static String PTAG = "POKEMON_OBJECT";

    private int mIcon;
    private int mIconSmall;
    private int mIconShiny;

    private int[] mStats;
    private int[] mBaseStats;
    private int[] mEVs;
    private int[] mIVs;
    private int mLevel;
    private int mGender;
    private int[] mNatureMultiplier;
    private String mNature;
    private boolean mShiny;
    private int mAbility;
    private String[] mAbilityList;
    private String[] mType;
    private String[] mMoves;
    private String[] mMoveList;
    private int mWeight;

    public Pokemon(int id) {

    }
    
    public int[] calculateStats() {
        int[] stats = new int[5];
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
        return ((getAtkIV() + 2 * getBaseAtk() + getAtkEV() / 4) * getLevel() / 100 + 5) * mNatureMultiplier[1];
    }

    public int calculateDef() {
        return ((getDefIV() + 2 * getBaseDef() + getDefEV() / 4) * getLevel() / 100 + 5) * mNatureMultiplier[2];
    }

    public int calculateSpAtk() {
        return ((getSpAtkIV() + 2 * getBaseSpAtk() + getSpAtkEV() / 4) * getLevel() / 100 + 5) * mNatureMultiplier[3];
    }

    public int calculateSpDef() {
        return ((getSpDefIV() + 2 * getBaseSpDef() + getSpDefEV() / 4) * getLevel() / 100 + 5) * mNatureMultiplier[4];
    }

    public int calculateSpd() {
        return ((getSpdIV() + 2 * getBaseSpd() + getSpdEV() / 4) * getLevel() / 100 + 5) * mNatureMultiplier[5];
    }

    public static int[] calculateStats(int[] baseStats, int[] IVs, int[] EVs, int level, int[] natureMultiplier) {
        int[] stats = new int[5];
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

    public static int calculateAtk(int baseAtk, int AtkIV, int AtkEV, int level, int natureMultiplier) {
        return ((AtkIV + 2 * baseAtk + AtkEV / 4) * level / 100 + 5) * natureMultiplier;
    }

    public static int calculateDef(int baseDef, int DefIV, int DefEV, int level, int natureMultiplier) {
        return ((DefIV + 2 * baseDef + DefEV / 4) * level / 100 + 5) * natureMultiplier;
    }

    public static int calculateSpAtk(int baseSpAtk, int SpAtkIV, int SpAtkEV, int level, int natureMultiplier) {
        return ((SpAtkIV + 2 * baseSpAtk + SpAtkEV / 4) * level / 100 + 5) * natureMultiplier;
    }

    public static int calculateSpDef(int baseSpDef, int SpDefIV, int SpDefEV, int level, int natureMultiplier) {
        return ((SpDefIV + 2 * baseSpDef + SpDefEV / 4) * level / 100 + 5) * natureMultiplier;
    }

    public static int calculateSpd(int baseSpd, int SpdIV, int SpdEV, int level, int natureMultiplier) {
        return ((SpdIV + 2 * baseSpd + SpdEV / 4) * level / 100 + 5) * natureMultiplier;
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

    public int getGender() {
        return mGender;
    }

    public void setGender(int gender) {
        mGender = gender;
    }

    public int[] getNatureMultiplier() {
        return mNatureMultiplier;
    }

    public void setNatureMultiplier(int[] natureMultiplier) {
        mNatureMultiplier = natureMultiplier;
    }

    public String getNature() {
        return mNature;
    }

    public void setNature(String nature) {
        mNature = nature;
    }

    public boolean isShiny() {
        return mShiny;
    }

    public void setShiny(boolean shiny) {
        mShiny = shiny;
    }

    public int getAbility() {
        return mAbility;
    }

    public void setAbility(int ability) {
        mAbility = ability;
    }

    public String[] getAbilityList() {
        return mAbilityList;
    }

    public void setAbilityList(String[] abilityList) {
        mAbilityList = abilityList;
    }

    public String[] getType() {
        return mType;
    }

    public void setType(String[] type) {
        mType = type;
    }

    public String[] getMoves() {
        return mMoves;
    }

    public void setMoves(String[] moves) {
        mMoves = moves;
    }

    public String[] getMoveList() {
        return mMoveList;
    }

    public void setMoveList(String[] moveList) {
        mMoveList = moveList;
    }

    public int getWeight() {
        return mWeight;
    }

    public void setWeight(int weight) {
        mWeight = weight;
    }
}
