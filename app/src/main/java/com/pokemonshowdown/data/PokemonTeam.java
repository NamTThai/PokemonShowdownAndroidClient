package com.pokemonshowdown.data;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
    private static List<PokemonTeam> pokemonTeamList;
    private String tier = "(None)";
    /**
     * Nickname for team
     */
    private String nickname = "";

    /**
     * List of pokemons
     */
    private ArrayList<Pokemon> pokemons = new ArrayList<Pokemon>();

    /**
     * Exporting function to String
     */

    public static List<PokemonTeam> getPokemonTeamList() {
        return pokemonTeamList;
    }

    public static void loadPokemonTeams(Context c) {
        FileInputStream fos;
        try {
            fos = c.openFileInput(pokemonTeamStorageName);
            ObjectInputStream oos = new ObjectInputStream(fos);
            pokemonTeamList = (ArrayList<PokemonTeam>) oos.readObject();
            oos.close();
        } catch (IOException e) {
            pokemonTeamList = new ArrayList<>();
        } catch (ClassNotFoundException e) {
            pokemonTeamList = new ArrayList<>();
        }
    }

    public static void savePokemonTeams(Context c) {
        FileOutputStream fos = null;
        try {
            fos = c.openFileOutput(pokemonTeamStorageName, Context.MODE_PRIVATE);

            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(pokemonTeamList);
            oos.close();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }


    public String exportPokemonTeam(Context appContext) {
        StringBuilder sb = new StringBuilder();

        for (Pokemon pokemon : pokemons) {
            if (pokemon != null) {
                sb.append(pokemon.exportPokemon(appContext));
                sb.append("\n");
            }
        }

        return sb.toString();
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

    /**
     * Accessors
     */
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public ArrayList<Pokemon> getPokemons() {
        return pokemons;
    }

    public void addPokemon(Pokemon p) {
        pokemons.add(p);
    }

    public void replacePokemon(int oldIndex, Pokemon p) {
        pokemons.set(oldIndex, p);
    }

    public Pokemon getPokemon(int index) {
        return pokemons.get(index);
    }

    public int getTeamSize() {
        return pokemons.size();
    }

    public boolean isFull() {
        return (pokemons.size() == 6);
    }

    public void removePokemon(int index) {
        pokemons.remove(index);
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getTier() {
        return tier;
    }
}