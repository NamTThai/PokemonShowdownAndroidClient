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

public class MoveDex {
    private final static String MTAG = MoveDex.class.getName();
    private HashMap<String, String> mMoveDexEntries;

    private static MoveDex sMoveDex;
    private Context mAppContext;

    private MoveDex(Context appContext) {
        mAppContext = appContext;
        mMoveDexEntries = readFile(appContext);
    }

    public static MoveDex get(Context c) {
        if (sMoveDex == null) {
            sMoveDex = new MoveDex(c.getApplicationContext());
        }
        return sMoveDex;
    }

    public static MoveDex getWithApplicationContext(Context appContext) {
        if (sMoveDex == null) {
            sMoveDex = new MoveDex(appContext);
        }
        return sMoveDex;
    }

    public HashMap<String, String> getMoveDexEntries() {
        return mMoveDexEntries;
    }

    public String getMove(String name) {
        return mMoveDexEntries.get(name);
    }

    public JSONObject getMoveJsonObject(String name) {
        try {
            String move = mMoveDexEntries.get(name);
            return new JSONObject(move);
        } catch (JSONException e) {
            Log.d(MTAG, e.toString());
            return null;
        }
    }

    private HashMap<String, String> readFile(Context appContext) {
        HashMap<String, String> MoveDexEntries = new HashMap<>();
        String jsonString;
        try {
            InputStream inputStream = appContext.getResources().openRawResource(R.raw.moves);
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
                    MoveDexEntries.put(key, entry.toString());
                }
            }
        } catch (JSONException e) {
            Log.d(MTAG, "JSON Exception");
        } catch (IOException e) {
            Log.d(MTAG, "Input Output problem");
        }

        return MoveDexEntries;
    }
}
