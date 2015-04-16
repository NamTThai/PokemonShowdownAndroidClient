package com.pokemonshowdown.data;

import android.content.Context;
import android.util.Log;

import com.pokemonshowdown.application.MyApplication;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a pokemon team
 * Contains 0 to 6 Pokemon
 * Has a nickname
 */
public class PokemonTeam implements Serializable {
    public static final String TAG = PokemonTeam.class.getName();
    private static final String pokemonTeamStorageName = "pkmnStorage.dat";
    private static List<PokemonTeam> mPokemonTeamList;
    private String mTier = "";
    /**
     * Nickname for team
     */
    private String mNickname = "";
    /**
     * List of pokemons
     */
    private ArrayList<Pokemon> mPokemons = new ArrayList<Pokemon>();

    public PokemonTeam() {
    }

    public PokemonTeam(String nickname) {
        this.mNickname = nickname;
    }


    public static List<PokemonTeam> getPokemonTeamList() {
        return mPokemonTeamList;
    }

    public static void loadPokemonTeams(Context appContext) {
        FileInputStream fos;
        try {
            mPokemonTeamList = new ArrayList<>();
            fos = appContext.openFileInput(pokemonTeamStorageName);
            StringBuffer fileContent = new StringBuffer("");
            byte[] buffer = new byte[1024];
            int n;
            while ((n = fos.read(buffer)) != -1) {
                fileContent.append(new String(buffer, 0, n));
            }
            fos.close();

            String fileContentString = fileContent.toString();
            fileContentString = fileContentString.replace("\r\n", "\n");
            String fileContentStringArray[] = fileContentString.split("\n");

            StringBuffer pokemonTeamBuffer = new StringBuffer("");
            String currentNickname = null;
            String currentTierId = "";

            for (String s : fileContentStringArray) {
                if (s.startsWith("===") && s.endsWith("===")) {
                    if (pokemonTeamBuffer.length() > 0) {
                        PokemonTeam pt = PokemonTeam.importPokemonTeam(pokemonTeamBuffer.toString(), appContext, true);
                        pt.setNickname(currentNickname);
                        if (!currentTierId.isEmpty()) {
                            BattleFieldData.Format currentFormat = BattleFieldData.get(appContext).getFormatUsingId(currentTierId);
                            if (currentFormat != null) {
                                pt.setTier(currentFormat.getName());
                            }
                        }
                        mPokemonTeamList.add(pt);
                    }
                    pokemonTeamBuffer.setLength(0);
                    if (s.contains("[") && s.contains("]")) {
                        currentTierId = s.substring(s.indexOf("[") + 1, s.indexOf("]"));
                        currentNickname = s.substring(s.indexOf("]") + 1).replace("=", "").trim();
                    } else {
                        currentTierId = "";
                        currentNickname = s.replace("=", "").trim();
                    }
                } else {
                    pokemonTeamBuffer.append(s).append("\n");
                }
            }

            if (pokemonTeamBuffer.length() > 0 && currentNickname != null) {
                PokemonTeam pt = PokemonTeam.importPokemonTeam(pokemonTeamBuffer.toString(), appContext, true);
                pt.setNickname(currentNickname);
                if (!currentTierId.isEmpty()) {
                    BattleFieldData.Format currentFormat = BattleFieldData.get(appContext).getFormatUsingId(currentTierId);
                    if (currentFormat != null) {
                        pt.setTier(currentFormat.getName());
                    }
                }
                mPokemonTeamList.add(pt);
            }

        } catch (IOException e) {
            mPokemonTeamList = new ArrayList<>();
        }
    }

    public static PokemonTeam importPokemonTeam(String importString, Context c, boolean withAppContest) {
        PokemonTeam pt = new PokemonTeam();
        if (importString.isEmpty()) {
            return null;
        }
        importString = importString.replace("\r\n", "\n");
        String[] pokemonImportStrings = importString.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String pokemonString : pokemonImportStrings) {
            if (pokemonString.isEmpty() && sb.length() > 0) {
                Pokemon p = Pokemon.importPokemon(sb.toString(), c, withAppContest);
                if (p != null) {
                    pt.addPokemon(p);
                }
                sb.setLength(0);
            } else {
                sb.append(pokemonString).append("\n");
            }
        }

        if (sb.length() > 0) {
            Pokemon p = Pokemon.importPokemon(sb.toString(), c, withAppContest);
            if (p != null) {
                pt.addPokemon(p);
            }
        }

        return pt;
    }

    public void addPokemon(Pokemon p) {
        mPokemons.add(p);
    }

    public static void savePokemonTeams(Context c) {
        FileOutputStream fos = null;
        try {
            fos = c.openFileOutput(pokemonTeamStorageName, Context.MODE_PRIVATE);
            StringBuilder sb = new StringBuilder();

            for (PokemonTeam pokemonTeam : mPokemonTeamList) {
                sb.append("=== ");
                if (!pokemonTeam.getTier().isEmpty()) {
                    sb.append("[").append(MyApplication.toId(pokemonTeam.getTier())).append("] ");
                }
                sb.append(pokemonTeam.getNickname()).append(" ===\n");
                sb.append(pokemonTeam.exportPokemonTeam(c));
            }

            fos.write(sb.toString().getBytes());
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }

    public String getTier() {
        return mTier;
    }

    /**
     * Accessors
     */
    public String getNickname() {
        return mNickname;
    }

    public String exportPokemonTeam(Context appContext) {
        StringBuilder sb = new StringBuilder();

        for (Pokemon pokemon : mPokemons) {
            if (pokemon != null) {
                sb.append(pokemon.exportPokemon(appContext));
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    public void setNickname(String nickname) {
        this.mNickname = nickname;
    }

    public void setTier(String tier) {
        this.mTier = tier;
    }

    public String exportForVerification() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Pokemon pokemon : mPokemons) {
            if (pokemon != null) {
                if (first == false) {
                    sb.append("]");
                }
                sb.append(pokemon.exportForVerification());
                first = false;
            }
        }
        return sb.toString();
    }

    public ArrayList<Pokemon> getPokemons() {
        return mPokemons;
    }

    public void replacePokemon(int oldIndex, Pokemon p) {
        mPokemons.set(oldIndex, p);
    }

    public Pokemon getPokemon(int index) {
        return mPokemons.get(index);
    }

    public int getTeamSize() {
        return mPokemons.size();
    }

    public boolean isFull() {
        return (mPokemons.size() == 6);
    }

    public void removePokemon(int index) {
        mPokemons.remove(index);
    }
}