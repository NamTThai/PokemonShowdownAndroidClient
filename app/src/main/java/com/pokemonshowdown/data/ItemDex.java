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

public class ItemDex {
    private final static String ITAG = ItemDex.class.getName();
    private HashMap<String, String> mItemDexEntries;

    private static ItemDex sItemDex;
    private Context mAppContext;

    private ItemDex(Context appContext) {
        mAppContext = appContext;
        mItemDexEntries = readFile(appContext);
    }

    public static ItemDex get(Context c) {
        if (sItemDex == null) {
            sItemDex = new ItemDex(c.getApplicationContext());
        }
        return sItemDex;
    }

    public static ItemDex getWithApplicationContext(Context appContext) {
        if (sItemDex == null) {
            sItemDex = new ItemDex(appContext);
        }
        return sItemDex;
    }

    public HashMap<String, String> getItemDexEntries() {
        return mItemDexEntries;
    }

    public String getItem(String name) {
        return mItemDexEntries.get(name);
    }

    public JSONObject getItemJsonObject(String name) {
        try {
            String item = mItemDexEntries.get(name);
            return new JSONObject(item);
        } catch (JSONException e) {
            Log.d(ITAG, e.toString());
            return null;
        }
    }

    public static int getItemIcon(Context appContext, String itemName) {
            return appContext.getResources().getIdentifier("item_" + itemName.toLowerCase().replaceAll("-", "_").replaceAll(" ", "").replaceAll("\'", "").replace(Character.toString('.'), ""), "drawable", appContext.getPackageName());
    }

    private HashMap<String, String> readFile(Context appContext) {
        HashMap<String, String> ItemDexEntries = new HashMap<>();
        String jsonString;
        try {
            InputStream inputStream = appContext.getResources().openRawResource(R.raw.item);
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
                    ItemDexEntries.put(key, entry.toString());
                }
            }
        } catch (JSONException e) {
            Log.d(ITAG, "JSON Exception");
        } catch (IOException e) {
            Log.d(ITAG, "Input Output problem");
        }

        return ItemDexEntries;
    }
}
