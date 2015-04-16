package com.pokemonshowdown.data;

import android.content.Context;
import android.util.Log;

import com.paypal.android.sdk.D;
import com.pokemonshowdown.app.R;
import com.pokemonshowdown.application.MyApplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemDex {
    private final static String ITAG = ItemDex.class.getName();
    public final static JSONObject DUMMY_OBJECT;

    static {
        try {
            DUMMY_OBJECT = new JSONObject("{\"gen\":1,\"spritenum\":0,\"num\":0,\"name\":\"\",\"id\":\"dummy\",\"desc\":\"\"}");
        } catch (JSONException e) {
            // Will not happen, but is needed because it's final.
            throw new Error();
        }
    }

    public static int getItemIcon(Context appContext, String itemName) {
        itemName = itemName.replaceFirst("itemdex_", "");
        return appContext.getResources()
                .getIdentifier("item_" + MyApplication.toId(itemName), "drawable", appContext.getPackageName());
    }

    public static List<String> getItemDexEntries(Context context) {
        return Arrays.asList(context.getResources().getStringArray(R.array.itemdex_ids));
    }

    public static String getItemName(Context context, String name) {
        name = MyApplication.toId(name);
        String item;
        try {
            name = name.startsWith("itemDex_") ? name : "itemdex_" + name;
            int stringId = context.getResources().getIdentifier(name, "string", context.getPackageName());
                    item = context.getResources().getString(stringId);
            if (item != null) {
                JSONObject itemEntries = new JSONObject(item);
                item = itemEntries.getString("name");
            }
        } catch (JSONException e) {
            item = null;
        }
        return item;
    }

    public static JSONObject getItemJsonObject(Context context, String name) {
        try {
            int stringId = context.getResources().getIdentifier(name, "string", context.getPackageName());
            String item = context.getString(stringId);
            return new JSONObject(item);
        } catch (NullPointerException | JSONException e) {
            e.printStackTrace();
            return DUMMY_OBJECT;
        }
    }
}
