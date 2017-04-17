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

public class AbilityDex {
    public final static String ATAG = AbilityDex.class.getName();
    private static AbilityDex sAbilityDex;
    private HashMap<String, String> mAbilityDexEntries;

    private AbilityDex(Context appContext) {
        mAbilityDexEntries = readFile(appContext);
    }

    public static String getAbilityName(Context appContext, String name) {
        try {
            name = MyApplication.toId(name);
            JSONObject abilityJson = AbilityDex.get(appContext).getAbilityJsonObject(name);
            return abilityJson.getString("name");
        } catch (JSONException | NullPointerException e) {
            return null;
        }
    }

    public static AbilityDex get(Context c) {
        if (sAbilityDex == null) {
            sAbilityDex = new AbilityDex(c.getApplicationContext());
        }
        return sAbilityDex;
    }

    private HashMap<String, String> readFile(Context appContext) {
        HashMap<String, String> AbilityDexEntries = new HashMap<>();
        String jsonString;
        try {
            InputStream inputStream = appContext.getResources().openRawResource(R.raw.ability);
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
                AbilityDexEntries.put(key, entry.toString());
            }
        } catch (JSONException e) {
            Log.d(ATAG, "JSON Exception");
        } catch (IOException e) {
            Log.d(ATAG, "Input Output problem");
        }

        return AbilityDexEntries;
    }

    public JSONObject getAbilityJsonObject(String name) {
        try {
            String ability = mAbilityDexEntries.get(MyApplication.toId(name));
            return new JSONObject(ability);
        } catch (JSONException e) {
            return null;
        }
    }

    public HashMap<String, String> getAbilityDexEntries() {
        return mAbilityDexEntries;
    }
}
