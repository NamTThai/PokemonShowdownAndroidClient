package com.pokemonshowdown.data;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.pokemonshowdown.app.R;
import com.pokemonshowdown.application.MyApplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ItemDex {
    private final static String ITAG = ItemDex.class.getName();
    private static ItemDex sItemDex;
    private HashMap<String, Integer> mItemDexEntries;
    private Context mAppContext;

    private ItemDex(Context appContext) {
        mAppContext = appContext;
        mItemDexEntries = initItemDex(appContext);
        //mItemDexEntries = readFile(appContext);
    }

    private HashMap<String, Integer> initItemDex(Context appContext) {
        Resources resources = appContext.getResources();
        String[] itemIds = resources.getStringArray(R.array.itemdex_ids);

        HashMap<String, Integer> itemDexEntries = new HashMap<>();
        for(String itemId : itemIds) {
            int res  = resources.getIdentifier(itemId, "string", appContext.getPackageName());
            itemDexEntries.put(itemId.replaceFirst("itemdex_", ""), res);
        }
        return itemDexEntries;
    }

    // No longer needed
    /*private HashMap<String, String> readFile(Context appContext) {
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
                JSONObject entry = jsonObject.getJSONObject(key);
                ItemDexEntries.put(key, entry.toString());
            }
        } catch (JSONException e) {
            Log.d(ITAG, "JSON Exception");
        } catch (IOException e) {
            Log.d(ITAG, "Input Output problem");
        }

        return ItemDexEntries;
    }*/

    public static ItemDex get(Context c) {
        if (sItemDex == null) {
            sItemDex = new ItemDex(c.getApplicationContext());
        }
        return sItemDex;
    }

    public static int getItemIcon(Context appContext, String itemName) {
        return appContext.getResources()
                .getIdentifier("item_" + MyApplication.toId(itemName), "drawable", appContext.getPackageName());
    }

    public HashMap<String, Integer> getItemDexEntries() {
        return mItemDexEntries;
    }

    public String getItemName(String name) {
        name = MyApplication.toId(name);
        String item;
        try {
            item = mAppContext.getResources().getString(mItemDexEntries.get(name));
            if (item != null) {
                JSONObject itemEntries = new JSONObject(item);
                item = itemEntries.getString("name");
            }
        } catch (JSONException e) {
            item = null;
        }
        return item;
    }

    public JSONObject getItemJsonObject(String name) {
        try {
            String item = mAppContext.getString(mItemDexEntries.get(MyApplication.toId(name)));
            return new JSONObject(item);
        } catch (NullPointerException | JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
