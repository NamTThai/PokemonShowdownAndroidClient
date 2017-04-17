package com.pokemonshowdown.data;

import android.content.Context;
import android.preference.PreferenceManager;

import com.pokemonshowdown.application.MyApplication;

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

    public static void loadPokemonTeams(Context appContext) {
        mPokemonTeamList = new ArrayList<>();
        String fileContentString = PreferenceManager.getDefaultSharedPreferences(appContext).getString("teams", "null");

        // team builder first use
        if (fileContentString.equals("null")) {
            return;
        }
        fileContentString = fileContentString.replace("\r\n", "\n");
        String fileContentStringArray[] = fileContentString.split("\n");

        StringBuffer pokemonTeamBuffer = new StringBuffer("");
        String currentNickname = null;
        String currentTierId = "";

        for (String s : fileContentStringArray) {
            // Every line starting with === is a new team
            if (s.startsWith("===") && s.endsWith("===")) {
                // If other team was already been buffered, we save it
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

                // Before starting (either the first or others), we get the name and tier (if exists) and store them
                pokemonTeamBuffer.setLength(0);
                if (s.contains("[") && s.contains("]")) {
                    currentNickname = s.substring(s.indexOf("]") + 2, s.indexOf("=", s.indexOf("]") + 2) - 1);
                    currentTierId = s.substring(s.indexOf("[") + 1, s.indexOf("]"));
                } else {
                    currentTierId = "";
                    currentNickname = s.substring(4, s.indexOf("=", 5) - 1);
                }

            } else {
                pokemonTeamBuffer.append(s).append("\n");
            }
        }

        // Every last team will be ignored using the loop above, so we take the already loaded buffer and add him separately
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
    }

    public static PokemonTeam importPokemonTeamFromPastebin(String importString, Context c) {
        PokemonTeam team = new PokemonTeam();
        importString = importString.replace("\r\n", "\n");
        String fileContentStringArray[] = importString.split("\n");

        StringBuffer pokemonTeamBuffer = new StringBuffer("");
        String currentNickname = null;
        String currentTierId = "";

        for (String s : fileContentStringArray) {
            // Every line starting with === is a new team
            if (s.startsWith("===") && s.endsWith("===")) {
                pokemonTeamBuffer.setLength(0);
                if (s.contains("[") && s.contains("]")) {
                    currentNickname = s.substring(s.indexOf("]") + 2, s.indexOf("=", s.indexOf("]") + 2) - 1);
                    currentTierId = s.substring(s.indexOf("[") + 1, s.indexOf("]"));
                } else {
                    currentTierId = "";
                    currentNickname = s.substring(4, s.indexOf("=", 5) - 1);
                }

            } else {
                pokemonTeamBuffer.append(s).append("\n");
            }
        }

        if (pokemonTeamBuffer.length() > 0 && currentNickname != null) {
            PokemonTeam pt = PokemonTeam.importPokemonTeam(pokemonTeamBuffer.toString(), c, true);
            pt.setNickname(currentNickname);
            if (!currentTierId.isEmpty()) {
                BattleFieldData.Format currentFormat = BattleFieldData.get(c).getFormatUsingId(currentTierId);
                if (currentFormat != null) {
                    pt.setTier(currentFormat.getName());
                }
            }
            team = pt;
        }

        return team;
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
            if (pokemonString.trim().isEmpty() && sb.length() > 0) {
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

    public static List<PokemonTeam> getPokemonTeamList(Context c) {
        if (mPokemonTeamList == null) {
            mPokemonTeamList = new ArrayList<>();
        }

        if (mPokemonTeamList.isEmpty()) {
            loadPokemonTeams(c);
        }

//        for (PokemonTeam team : mPokemonTeamList) {
//            for (Pokemon mon : team.getPokemons()) {
//                Log.d("gssgfs", "" + mon.getName());
//            }
//        }
        return mPokemonTeamList;
    }

    public static void savePokemonTeams(Context c, List<PokemonTeam> teams) {
        mPokemonTeamList = teams;
        StringBuilder sb = new StringBuilder();

        for (PokemonTeam pokemonTeam : mPokemonTeamList) {
            sb.append("=== ");
            if (!pokemonTeam.getTier().isEmpty()) {
                sb.append("[").append(MyApplication.toId(pokemonTeam.getTier())).append("] ");
            }
            sb.append(pokemonTeam.getNickname()).append(" ===\n");
            sb.append(pokemonTeam.exportPokemonTeam(c));
            sb.append("\n");
        }

        PreferenceManager.getDefaultSharedPreferences(c).edit().putString("teams", sb.toString()).commit();
    }

    public void addPokemon(Pokemon p) {
        mPokemons.add(p);
    }

    public void addPokemon(Pokemon p, int position) {
        mPokemons.add(position, p);
    }

    public String getTier() {
        return mTier;
    }

    public void setTier(String tier) {
        this.mTier = tier;
    }

    /**
     * Accessors
     */
    public String getNickname() {
        return mNickname;
    }

    public void setNickname(String nickname) {
        this.mNickname = nickname;
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