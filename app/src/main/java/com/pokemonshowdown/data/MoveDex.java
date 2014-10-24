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
    private HashMap<String, Integer> mMoveAnimationEntries;

    public final static Integer CUSTOMIZED = -1;
    public final static Integer SHAKE = 0;
    public final static Integer DANCE = 1;

    private static MoveDex sMoveDex;
    private Context mAppContext;

    private MoveDex(Context appContext) {
        mAppContext = appContext;
        mMoveDexEntries = readFile(appContext);
        initializeAnimationEntries();
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

    public HashMap<String, Integer> getMoveAnimationEntries() {
        return mMoveAnimationEntries;
    }

    public Integer getMoveAnimationTag(String move) {
        Integer animation = getMoveAnimationEntries().get(move);
        return (animation == null) ? SHAKE : animation;
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

    public static String getMoveName(Context appContext, String name) {
        try {
            JSONObject moveJson = MoveDex.getWithApplicationContext(appContext).getMoveJsonObject(name);
            return moveJson.getString("name");
        } catch (JSONException e) {
            Log.d(MTAG, e.toString());
            return null;
        }
    }

    public static int getMoveType(Context appContext, String types) {
        return appContext.getResources().getIdentifier("types_" + types.toLowerCase(), "drawable", appContext.getPackageName());
    }

    public static int getMoveCategory(Context appContext, String category) {
        return appContext.getResources().getIdentifier("category_" + category.toLowerCase(), "drawable", appContext.getPackageName());
    }

    public static String getMaxPP(String pp) {
        int ppInt = Integer.parseInt(pp);
        ppInt *= 1.6;
        return Integer.toString(ppInt);
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

    private void initializeAnimationEntries() {
        mMoveAnimationEntries = new HashMap<>();
        String[] customizedEntries = {"dragonpulse"};
        String[] shakeEntries = {"taunt", "swagger", "swordsdance", "quiverdance", "dragondance", "agility",
                "doubleteam", "metronome", "teeterdance", "splash", "encore"};
        String[] danceEntries = {"attract", "raindance", "sunnyday",
                "hail", "sandstorm", "gravity", "trickroom", "magicroom", "wonderroom"};
        for (String customized : customizedEntries) {
            mMoveAnimationEntries.put(customized, CUSTOMIZED);
        }
        for (String shake : shakeEntries) {
            mMoveAnimationEntries.put(shake, SHAKE);
        }
        for (String dance : danceEntries) {
            mMoveAnimationEntries.put(dance, DANCE);
        }

    }
}
