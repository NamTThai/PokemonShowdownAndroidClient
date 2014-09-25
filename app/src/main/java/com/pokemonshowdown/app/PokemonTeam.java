package com.pokemonshowdown.app;

import com.pokemonshowdown.data.Pokemon;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class represents a pokemon team
 * Contains 0 to 6 Pokemon
 * Has a nickname
 */
public class PokemonTeam implements Serializable {

    /**
     * Nickname for team
     */
    private String nickname = "Test Team";

    /**
     * List of pokemons
     */
    private ArrayList<Pokemon> pokemons = new ArrayList<Pokemon>();

    /**
     * Exporting function to String
     */
    public String export() {
        StringBuilder sb = new StringBuilder();

        for (Pokemon pokemon : pokemons) {
            if (pokemon != null) {
                sb.append(pokemon.export());
                sb.append('\n');
            }
        }

        return sb.toString();
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

    public void remplacePokemon(int oldIndex, Pokemon p) {
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

}