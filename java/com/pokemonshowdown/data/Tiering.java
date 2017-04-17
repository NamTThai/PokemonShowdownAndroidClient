package com.pokemonshowdown.data;

import android.content.Context;
import android.util.Log;

import com.pokemonshowdown.R;
import com.pokemonshowdown.application.MyApplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Tiering {
    public final static List<String> TIER_ORDER = new ArrayList<>(
            Arrays.asList("Ubers", "OU", "BL", "UU", "BL2", "RU", "BL3", "NU", "BL4", "PU", "NFE", "LC Uber", "LC"));

    public final static String PTAG = Pokedex.class.getName();
    private static Tiering sTiering;

    // key is id of pokemon, value is array of moves id
    private HashMap<String, String> mPokemonTierMap = new HashMap<>();

    // key is tier, value is array of pokemons
    private HashMap<String, ArrayList<String>> mTierList = new HashMap<>();

    private Tiering(Context appContext) {
        readFile(appContext);
    }

    public static Tiering get(Context c) {
        if (sTiering == null) {
            sTiering = new Tiering(c.getApplicationContext());
        }
        return sTiering;
    }

    private void readFile(Context appContext) {
        String jsonString;
        try {
            InputStream inputStream = appContext.getResources().openRawResource(R.raw.formats_data);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }
            jsonString = stringBuilder.toString();
            inputStream.close();

            JSONObject jsonObject = new JSONObject(jsonString);

            Iterator<String> keys = jsonObject.keys();

            while (keys.hasNext()) {
                String key = keys.next(); // key is pokemon name

                // entry is learnet
                JSONObject entry = jsonObject.getJSONObject(key);
                // no tiers on some megas?
                String tier = entry.optString("tier", null);

                mPokemonTierMap.put(key, tier);
                if (tier != null) {
                    // servers tier name is Ubers but file name is Uber...
                    if (tier.equals("Uber")) {
                        tier = "Ubers";
                    }
                    if (mTierList.get(tier) != null) {
                        mTierList.get(tier).add(key);
                    } else {
                        mTierList.put(tier, new ArrayList<String>());
                        mTierList.get(tier).add(key);
                    }
                }
            }
        } catch (JSONException e) {
            Log.d(PTAG, "JSON Exception");
        } catch (IOException e) {
            Log.d(PTAG, "Input Output problem");
        }
    }

    public HashMap<String, String> getPokemonToTierList() {
        return mPokemonTierMap;
    }

    public String getPokemonTier(String pkmId) {
        return mPokemonTierMap.get(MyApplication.toId(pkmId));
    }

    public HashMap<String, ArrayList<String>> getTierList() {
        return mTierList;
    }
}