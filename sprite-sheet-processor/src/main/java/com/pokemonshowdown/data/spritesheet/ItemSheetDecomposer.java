package com.pokemonshowdown.data.spritesheet;

import org.json.JSONObject;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

public class ItemSheetDecomposer {

    public static void main(String[] args) {
        try {
            BufferedImage spriteSheet = ImageIO.read(new File("/Users/thain/Documents/Projects/Project0/PokemonShowdownAndroidClient/sprite-sheet-processor/data/itemicons-sheet.png"));

            FileReader reader = new FileReader("/Users/thain/Documents/Projects/Project0/PokemonShowdownAndroidClient/sprite-sheet-processor/data/item.json");
            BufferedReader br = new BufferedReader(reader);
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            line = stringBuilder.toString();
            JSONObject itemDex = new JSONObject(line);
            Iterator itemTags = itemDex.keys();

            String pre = "/Users/thain/Documents/Projects/Project0/PokemonShowdownAndroidClient/sprite-sheet-processor/img-processed/item_";
            String pos = ".png";

            while (itemTags.hasNext()) {
                String itemTag = (String) itemTags.next();
                JSONObject itemData = (JSONObject) itemDex.get(itemTag);
                int spritenum = itemData.getInt("spritenum");
                if (spritenum > 0)
                cutSpriteSheet(spriteSheet, spritenum, pre+itemTag+pos);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }

    }

    private static void cutSpriteSheet(BufferedImage sheet, int num, String filename) {
        int x = (num % 16) * 24;
        int y = (num / 16) * 24;
        BufferedImage icon = sheet.getSubimage(x, y, 24, 24);
        try {
            ImageIO.write(icon, "png", new File(filename));
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

}
