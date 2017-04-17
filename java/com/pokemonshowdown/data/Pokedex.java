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
import java.util.HashMap;
import java.util.Iterator;

public class Pokedex {
    public final static String PTAG = Pokedex.class.getName();
    private static Pokedex sPokedex;
    private HashMap<String, String> mPokedexEntries;

    private Pokedex(Context appContext) {
        mPokedexEntries = readFile(appContext);
    }

    public static Pokedex get(Context c) {
        if (sPokedex == null) {
            sPokedex = new Pokedex(c.getApplicationContext());
        }
        return sPokedex;
    }

    public static int getUnownIcon(Context appContext, String name) {
        return appContext.getResources()
                .getIdentifier("unown_" + name.toLowerCase().charAt(0), "drawable", appContext.getPackageName());
    }

    private HashMap<String, String> readFile(Context appContext) {
        HashMap<String, String> pokedexEntries = new HashMap<>();
        String jsonString;
        try {
            InputStream inputStream = appContext.getResources().openRawResource(R.raw.pokedex);
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
                String key = keys.next();
                JSONObject entry = jsonObject.getJSONObject(key);
                pokedexEntries.put(key, entry.toString());
            }
        } catch (JSONException e) {
            Log.d(PTAG, "JSON Exception");
        } catch (IOException e) {
            Log.d(PTAG, "Input Output problem");
        }

        return pokedexEntries;
    }

    public HashMap<String, String> getPokedexEntries() {
        return mPokedexEntries;
    }

    public JSONObject getPokemonJSONObject(String name) {
        try {
            return new JSONObject(getPokemonJSONString(name));
        } catch (JSONException e) {
            return null;
        }
    }

    public String getPokemonJSONString(String name) {
        return mPokedexEntries.get(MyApplication.toId(name));
    }
}
