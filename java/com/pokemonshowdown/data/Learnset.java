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
import java.util.HashMap;
import java.util.Iterator;

public class Learnset {

    public final static String PTAG = Pokedex.class.getName();
    private static Learnset sLearnset;

    // key is id of pokemon, valeu is array of moves id
    private HashMap<String, ArrayList<String>> mLearnsetEntries;

    private Learnset(Context appContext) {
        mLearnsetEntries = readFile(appContext);
    }

    public static Learnset get(Context c) {
        if (sLearnset == null) {
            sLearnset = new Learnset(c.getApplicationContext());
        }
        return sLearnset;
    }

    private HashMap<String, ArrayList<String>> readFile(Context appContext) {
        HashMap<String, ArrayList<String>> learnsetEntries = new HashMap<>();
        String jsonString;
        try {
            InputStream inputStream = appContext.getResources().openRawResource(R.raw.learnsets);
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
                ArrayList<String> learnsetData = new ArrayList<>();

                // entry is learnet
                JSONObject entry = jsonObject.getJSONObject(key);
                JSONObject learnset = entry.getJSONObject("learnset");

                Iterator<String> learnsetIterator = learnset.keys();
                while (learnsetIterator.hasNext()) {
                    learnsetData.add(learnsetIterator.next());
                }
                learnsetEntries.put(key, learnsetData);
            }
        } catch (JSONException e) {
            Log.d(PTAG, "JSON Exception");
        } catch (IOException e) {
            Log.d(PTAG, "Input Output problem");
        }

        return learnsetEntries;
    }

    public HashMap<String, ArrayList<String>> getLearnsetEntries() {
        return mLearnsetEntries;
    }

    public ArrayList<String> getLearnetEntry(String pkmId) {
        ArrayList<String> learnset = mLearnsetEntries.get(MyApplication.toId(pkmId));
        if (learnset == null) {
            // happens with megas or therian forms -> should use the base form entry
            if (pkmId.contains("-")) {
                learnset = mLearnsetEntries.get(MyApplication.toId(pkmId.substring(0, pkmId.indexOf("-"))));
            }
        }

        return learnset;
    }

}
