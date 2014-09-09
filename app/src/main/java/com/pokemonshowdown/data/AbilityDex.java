package com.pokemonshowdown.data;

import android.content.Context;
import android.util.Log;

import com.pokemonshowdown.app.R;

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
    private HashMap<String, String> mAbilityDexEntries;

    private static AbilityDex sAbilityDex;
    private Context mAppContext;

    private AbilityDex(Context appContext) {
        mAppContext = appContext;
        mAbilityDexEntries = readFile(appContext);
    }

    public static AbilityDex get(Context c) {
        if (sAbilityDex == null) {
            sAbilityDex = new AbilityDex(c.getApplicationContext());
        }
        return sAbilityDex;
    }

    public static AbilityDex getWithApplicationContext(Context appContext) {
        if (sAbilityDex == null) {
            sAbilityDex = new AbilityDex(appContext);
        }
        return sAbilityDex;
    }

    public HashMap<String, String> getAbilityDexEntries() {
        return mAbilityDexEntries;
    }

    public String getAbility(String name) {
        return mAbilityDexEntries.get(name);
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
                Object value = jsonObject.get(key);
                if (jsonObject.get(key) instanceof JSONObject) {
                    JSONObject entry = (JSONObject) value;
                    AbilityDexEntries.put(key, entry.toString());
                }
            }
        } catch (JSONException e) {
            Log.d(ATAG, "JSON Exception");
        } catch (IOException e) {
            Log.d(ATAG, "Input Output problem");
        }

        return AbilityDexEntries;
    }
}
