package com.pokemonshowdown.data;

import android.content.Context;
import android.util.Log;

import com.pokemonshowdown.app.R;
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

public class Tiering {

    public final static String PTAG = Pokedex.class.getName();
    private static Tiering sTiering;

    // key is id of pokemon, valeu is array of moves id
    private HashMap<String, String> mTierList;

    private Tiering(Context appContext) {
        mTierList = readFile(appContext);
    }

    private HashMap<String, String> readFile(Context appContext) {
        HashMap<String, String> learnsetEntries = new HashMap<>();
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
                String tier = entry.getString("tier");

                learnsetEntries.put(key, tier);
            }
        } catch (JSONException e) {
            Log.d(PTAG, "JSON Exception");
        } catch (IOException e) {
            Log.d(PTAG, "Input Output problem");
        }

        return learnsetEntries;
    }


    public static Tiering get(Context c) {
        if (sTiering == null) {
            sTiering = new Tiering(c.getApplicationContext());
        }
        return sTiering;
    }

    public HashMap<String,String> getTierList() {
        return mTierList;
    }

    public String getTier(String pkmId) {
        return mTierList.get(MyApplication.toId(pkmId));
    }

}
