package com.pokemonshowdown.data;

import android.content.Context;

import com.pokemonshowdown.app.R;
import com.pokemonshowdown.application.MyApplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class ItemDex {
    private final static String ITAG = ItemDex.class.getName();
    public final static JSONObject DUMMY_JSON_ITEM;
    public final static String DUMMY_ITEM = "Item not loadable.";

    static {
        try {
            DUMMY_JSON_ITEM = new JSONObject("{\"gen\":1,\"spritenum\":0,\"num\":0,\"name\":\"Broken Item ID\",\"id\":\"dummy\",\"desc\":\"This item should not be displayed here. Please report this bug.\"}");
        } catch (JSONException e) {
            // Will not happen, but is needed because it's final.
            throw new Error();
        }
    }

    public static int getItemIcon(Context appContext, String itemName) {
        if (itemName != null) {
            return appContext.getResources()
                    .getIdentifier("item_" + MyApplication.toId(itemName), "drawable", appContext.getPackageName());
        } else {
            return R.drawable.sprites_0;
        }
    }

    public static List<String> getItemDexEntries(Context context) {
        return Arrays.asList(context.getResources().getStringArray(R.array.itemdex_ids));
    }

    public static String getItemName(Context context, String name) {
        if (name != null) {
            name = MyApplication.toId(name);
            String itemName;
            try {
                int stringId = context.getResources().getIdentifier(name, "string", context.getPackageName());
                itemName = context.getResources().getString(stringId);
                JSONObject itemEntries = new JSONObject(itemName);
                return itemEntries.getString("name");
            } catch (JSONException e) {
                //Handled in return statement the same way as an null name.
            }
        }
        return DUMMY_ITEM;
    }

    public static JSONObject getItemJsonObject(Context context, String name) {
        try {
            name = MyApplication.toId(name);
            if(name != null) {
                int stringId = context.getResources().getIdentifier(name, "string", context.getPackageName());
                if (stringId > 0) {
                    String item = context.getString(stringId);
                    return new JSONObject(item);
                }
            }
        } catch (JSONException e) {
            //Handled in return statement
        }
        return DUMMY_JSON_ITEM;
    }
}
