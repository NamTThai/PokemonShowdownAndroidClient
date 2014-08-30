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

public class SpriteSheetDecomposer {

    public static void main(String[] args) {
        try {
            BufferedImage spriteSheet = ImageIO.read(new File("/Users/thain/Documents/Projects/Project0/PokemonShowdownAndroidClient/sprite-sheet-processor/data/bwicons-sheet-g6.png"));

            FileReader reader = new FileReader("/Users/thain/Documents/Projects/Project0/PokemonShowdownAndroidClient/sprite-sheet-processor/data/pokedex.json");
            BufferedReader br = new BufferedReader(reader);
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            line = stringBuilder.toString();
            JSONObject pokedex = new JSONObject(line);
            Iterator pokemonTags = pokedex.keys();

            String pre = "/Users/thain/Documents/Projects/Project0/PokemonShowdownAndroidClient/sprite-sheet-processor/img-processed/smallicons_";
            String pos = ".png";
            cutSpriteSheet(spriteSheet, 0, pre+"0"+pos);

            while (pokemonTags.hasNext()) {
                String pokemonTag = (String) pokemonTags.next();
                org.json.JSONObject pokemonData = (org.json.JSONObject) pokedex.get(pokemonTag);
                String pokemonName = pokemonData.getString("species").toLowerCase().replaceAll("-", "_").replaceAll(" ", "").replaceAll("\'", "").replace(Character.toString('.'), "");
                System.out.print(pokemonName);
                if (pokemonName.equals(pokemonTag)) {
                    int num = pokemonData.getInt("num");
                    if (num >= 0) {
                        cutSpriteSheet(spriteSheet, num, pre+pokemonName+pos);
                    }
                    String fileName = pre+pokemonName+pos;
                    switch (pokemonTag) {
                        case "unfezant": cutSpriteSheet(spriteSheet, 788, pre+pokemonName+"_f"+pos); break;
                        case "frillish": cutSpriteSheet(spriteSheet, 801, pre+pokemonName+"_f"+pos); break;
                        case "jellicent": cutSpriteSheet(spriteSheet, 802, pre+pokemonName+"_f"+pos); break;
                        case "meowstic": cutSpriteSheet(spriteSheet, 809, pre+pokemonName+"_f"+pos); break;
                        case "syclant": cutSpriteSheet(spriteSheet,832 + 0, fileName); break;
                        case "revenankh": cutSpriteSheet(spriteSheet,832 + 1, fileName); break;
                        case "pyroak": cutSpriteSheet(spriteSheet,832 + 2, fileName); break;
                        case "fidgit": cutSpriteSheet(spriteSheet,832 + 3, fileName); break;
                        case "stratagem": cutSpriteSheet(spriteSheet,832 + 4, fileName); break;
                        case "arghonaut": cutSpriteSheet(spriteSheet,832 + 5, fileName); break;
                        case "kitsunoh": cutSpriteSheet(spriteSheet,832 + 6, fileName); break;
                        case "cyclohm": cutSpriteSheet(spriteSheet,832 + 7, fileName); break;
                        case "colossoil": cutSpriteSheet(spriteSheet,832 + 8, fileName); break;
                        case "krilowatt": cutSpriteSheet(spriteSheet,832 + 9, fileName); break;
                        case "voodoom": cutSpriteSheet(spriteSheet,832 + 10, fileName); break;
                        case "tomohawk": cutSpriteSheet(spriteSheet,832 + 11, fileName); break;
                        case "necturna": cutSpriteSheet(spriteSheet,832 + 12, fileName); break;
                        case "mollux": cutSpriteSheet(spriteSheet,832 + 13, fileName); break;
                        case "aurumoth": cutSpriteSheet(spriteSheet,832 + 14, fileName); break;
                        case "malaconda": cutSpriteSheet(spriteSheet,832 + 15, fileName); break;
                        case "cawmodore": cutSpriteSheet(spriteSheet,832 + 16, fileName); break;
                        case "volkraken": cutSpriteSheet(spriteSheet,832 + 17, fileName); break;
                    }
                } else {
                    String fileName = pre+pokemonName+pos;
                    switch (pokemonTag) {
                        case "aegislashblade": cutSpriteSheet(spriteSheet, 681, fileName); break;
                        case "nidoranm": cutSpriteSheet(spriteSheet, 32, fileName); break;
                        case "nidoranf": cutSpriteSheet(spriteSheet, 29, fileName); break;
                        case "pichuspikyeared": cutSpriteSheet(spriteSheet, 172, fileName); break;
                        case "porygonz": cutSpriteSheet(spriteSheet, 474, fileName); break;
                        case "hooh": cutSpriteSheet(spriteSheet, 250, fileName); break;
                        case "genesectdouse": cutSpriteSheet(spriteSheet, 649, fileName); break;
                        case "genesectshock": cutSpriteSheet(spriteSheet, 649, fileName); break;
                        case "genesectburn": cutSpriteSheet(spriteSheet, 649, fileName); break;
                        case "genesectchill": cutSpriteSheet(spriteSheet, 649, fileName); break;
                        case "arceusbug": cutSpriteSheet(spriteSheet, 493, fileName); break;
                        case "arceusdark": cutSpriteSheet(spriteSheet, 493, fileName); break;
                        case "arceusdragon": cutSpriteSheet(spriteSheet, 493, fileName); break;
                        case "arceuselectric": cutSpriteSheet(spriteSheet, 493, fileName); break;
                        case "arceusfairy": cutSpriteSheet(spriteSheet, 493, fileName); break;
                        case "arceusfighting": cutSpriteSheet(spriteSheet, 493, fileName); break;
                        case "arceusfire": cutSpriteSheet(spriteSheet, 493, fileName); break;
                        case "arceusflying": cutSpriteSheet(spriteSheet, 493, fileName); break;
                        case "arceusghost": cutSpriteSheet(spriteSheet, 493, fileName); break;
                        case "arceusgrass": cutSpriteSheet(spriteSheet, 493, fileName); break;
                        case "arceusground": cutSpriteSheet(spriteSheet, 493, fileName); break;
                        case "arceusice": cutSpriteSheet(spriteSheet, 493, fileName); break;
                        case "arceuspoison": cutSpriteSheet(spriteSheet, 493, fileName); break;
                        case "arceuspsychic": cutSpriteSheet(spriteSheet, 493, fileName); break;
                        case "arceusrock": cutSpriteSheet(spriteSheet, 493, fileName); break;
                        case "arceussteel": cutSpriteSheet(spriteSheet, 493, fileName); break;
                        case "arceuswater": cutSpriteSheet(spriteSheet, 493, fileName); break;
                        case "pumpkaboosmall": cutSpriteSheet(spriteSheet, 710, fileName); break;
                        case "pumpkaboolarge": cutSpriteSheet(spriteSheet, 710, fileName); break;
                        case "pumpkaboosuper": cutSpriteSheet(spriteSheet, 710, fileName); break;
                        case "gourgeistsmall": cutSpriteSheet(spriteSheet, 711, fileName); break;
                        case "gourgeistlarge": cutSpriteSheet(spriteSheet, 711, fileName); break;
                        case "gourgeistsuper": cutSpriteSheet(spriteSheet, 711, fileName); break;
                        case "egg": cutSpriteSheet(spriteSheet, 731, fileName); break;
                        case "rotomfan": cutSpriteSheet(spriteSheet, 779, fileName); break;
                        case "rotomfrost": cutSpriteSheet(spriteSheet, 780, fileName); break;
                        case "rotomheat": cutSpriteSheet(spriteSheet,781, fileName); break;
                        case "rotommow": cutSpriteSheet(spriteSheet,782, fileName); break;
                        case "rotomwash": cutSpriteSheet(spriteSheet,783, fileName); break;
                        case "giratinaorigin": cutSpriteSheet(spriteSheet,785, fileName); break;
                        case "shayminsky": cutSpriteSheet(spriteSheet,787, fileName); break;
                        case "basculinbluestriped": cutSpriteSheet(spriteSheet,789, fileName); break;
                        case "darmanitanzen": cutSpriteSheet(spriteSheet,792, fileName); break;
                        case "deoxysattack": cutSpriteSheet(spriteSheet,763, fileName); break;
                        case "deoxysdefense": cutSpriteSheet(spriteSheet,764, fileName); break;
                        case "deoxysspeed": cutSpriteSheet(spriteSheet,766, fileName); break;
                        case "wormadamsandy": cutSpriteSheet(spriteSheet,771, fileName); break;
                        case "wormadamtrash": cutSpriteSheet(spriteSheet,772, fileName); break;
                        case "cherrimsunshine": cutSpriteSheet(spriteSheet,774, fileName); break;
                        case "castformrainy": cutSpriteSheet(spriteSheet,760, fileName); break;
                        case "castformsnowy": cutSpriteSheet(spriteSheet,761, fileName); break;
                        case "castformsunny": cutSpriteSheet(spriteSheet,762, fileName); break;
                        case "meloettapirouette": cutSpriteSheet(spriteSheet,804, fileName); break;
                        case "meowsticf": cutSpriteSheet(spriteSheet,809, fileName); break;
                        case "floetteeternalflower": cutSpriteSheet(spriteSheet,810, fileName); break;
                        case "tornadustherian": cutSpriteSheet(spriteSheet,816, fileName); break;
                        case "thundurustherian": cutSpriteSheet(spriteSheet,817, fileName); break;
                        case "landorustherian": cutSpriteSheet(spriteSheet,818, fileName); break;
                        case "kyuremblack": cutSpriteSheet(spriteSheet,819, fileName); break;
                        case "kyuremwhite": cutSpriteSheet(spriteSheet,820, fileName); break;
                        case "keldeoresolute": cutSpriteSheet(spriteSheet,821, fileName); break;
                        case "venusaurmega": cutSpriteSheet(spriteSheet,864, fileName); break;
                        case "charizardmegax": cutSpriteSheet(spriteSheet,865, fileName); break;
                        case "charizardmegay": cutSpriteSheet(spriteSheet,866, fileName); break;
                        case "blastoisemega": cutSpriteSheet(spriteSheet,867, fileName); break;
                        case "alakazammega": cutSpriteSheet(spriteSheet,868, fileName); break;
                        case "gengarmega": cutSpriteSheet(spriteSheet,869, fileName); break;
                        case "kangaskhanmega": cutSpriteSheet(spriteSheet,870, fileName); break;
                        case "pinsirmega": cutSpriteSheet(spriteSheet,871, fileName); break;
                        case "gyaradosmega": cutSpriteSheet(spriteSheet,872, fileName); break;
                        case "aerodactylmega": cutSpriteSheet(spriteSheet,873, fileName); break;
                        case "mewtwomegax": cutSpriteSheet(spriteSheet,874, fileName); break;
                        case "mewtwomegay": cutSpriteSheet(spriteSheet,875, fileName); break;
                        case "ampharosmega": cutSpriteSheet(spriteSheet,876, fileName); break;
                        case "scizormega": cutSpriteSheet(spriteSheet,877, fileName); break;
                        case "heracrossmega": cutSpriteSheet(spriteSheet,878, fileName); break;
                        case "houndoommega": cutSpriteSheet(spriteSheet,879, fileName); break;
                        case "tyranitarmega": cutSpriteSheet(spriteSheet,880, fileName); break;
                        case "blazikenmega": cutSpriteSheet(spriteSheet,881, fileName); break;
                        case "gardevoirmega": cutSpriteSheet(spriteSheet,882, fileName); break;
                        case "mawilemega": cutSpriteSheet(spriteSheet,883, fileName); break;
                        case "aggronmega": cutSpriteSheet(spriteSheet,884, fileName); break;
                        case "medichammega": cutSpriteSheet(spriteSheet,885, fileName); break;
                        case "manectricmega": cutSpriteSheet(spriteSheet,886, fileName); break;
                        case "banettemega": cutSpriteSheet(spriteSheet,887, fileName); break;
                        case "absolmega": cutSpriteSheet(spriteSheet,888, fileName); break;
                        case "garchompmega": cutSpriteSheet(spriteSheet,889, fileName); break;
                        case "lucariomega": cutSpriteSheet(spriteSheet,890, fileName); break;
                        case "abomasnowmega": cutSpriteSheet(spriteSheet,891, fileName); break;
                        case "latiasmega": cutSpriteSheet(spriteSheet,892, fileName); break;
                        case "latiosmega": cutSpriteSheet(spriteSheet,893, fileName); break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }

    }

    private static void cutSpriteSheet(BufferedImage sheet, int num, String filename) {
        int x = (num % 16) * 32;
        int y = (num / 16) * 32;
        BufferedImage icon = sheet.getSubimage(x, y, 32, 32);
        try {
            ImageIO.write(icon, "png", new File(filename));
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

}
