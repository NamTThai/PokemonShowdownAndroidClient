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
    public final static Integer FLIGHT = 2;
    public final static Integer SPINATK = 3;
    public final static Integer XATK = 4;
    public final static Integer SELF = 5;
    public final static Integer SELF_LIGHT = 6;
    public final static Integer SELF_DARK = 7;
    public final static Integer TRICK = 8;
    public final static Integer CHARGE = 9;
    public final static Integer SPREAD_LIGHT = 10;
    public final static Integer SPREAD_ENERGY = 11;
    public final static Integer SPREAD_MIST = 12;
    public final static Integer SPREAD_SHADOW = 13;
    public final static Integer SPREAD_POISON = 14;
    public final static Integer SPREAD_WAVE = 15;
    public final static Integer CONTACT_ENERGY = 16;
    public final static Integer CONTACT_CLAW = 17;
    public final static Integer CONTACT_KICK = 18;
    public final static Integer CONTACT_WAVE = 19;
    public final static Integer CONTACT_BITE = 20;
    public final static Integer CONTACT_POISON = 21;
    public final static Integer CONTACT_PUNCH = 22;
    public final static Integer DRAIN = 23;

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
        String[] customizedEntries = {"voltswitch", "thunderwave", "explosion"};
        String[] shakeEntries = {"taunt", "swagger", "swordsdance", "quiverdance", "dragondance", "agility",
                "doubleteam", "metronome", "teeterdance", "splash", "encore"};
        String[] danceEntries = {"attract", "raindance", "sunnyday",
                "hail", "sandstorm", "gravity", "trickroom", "magicroom", "wonderroom"};
        String[] flightEntries = {"aerialace", "bravebird", "acrobatics", "flyingpress", "drillpeck"};
        String[] xatkEntries = {"flail", "xscissor", "crosschop", "facade", "guillotine", "return", "frustration",
                "leafblade"};
        String[] spinatkEntries = {"uturn", "rapidspin", "gyroball"};
        String[] selfEntries = {"reflect", "safeguard", "lightscreen", "mist", "transform", "bellydrum",
                "aromatherapy", "healbell", "magiccoat", "protect", "detect", "kingshield", "spikyshield",
                "endure", "bide", "rockpolish", "harden", "irondefense", "rest", "howl", "acupressure",
                "curse", "shiftgear", "autotomize", "bulkup", "workup", "honeclaws", "shellsmash","stockpile",
                "ingrain", "aquaring", "coil", "refresh", "minimize", "doomdesire", "futuresight", "cottonguard",
                "roost", "softboiled", "milkdrink", "slackoff", "acidarmor", "substitute", "batonpass", "growth",
                "painsplit"};
        String[] selfLightEntries = {"barrier", "amnesia", "synthesis", "moonlight", "morningsun", "cosmicpower",
                "charge", "geomancy", "calmmind", "recover"};
        String[] selfDarkEntries = {"nastyplot", "tailglow"};
        String[] trickEntries = {"trick", "switcheroo"};
        String[] chargeEntries = {"shadowforce", "bounce", "dig", "dive", "fly", "skydrop", "skullbash", "skyattack"};
        String[] spreadLightEntries = {"hiddenpower"};
        String[] spreadEnergyEntries = {"bugbuzz", "seedflare"};
        String[] spreadMistEntries = {"focusblast"};
        String[] spreadShadowEntries = {"dragonpulse"};
        String[] spreadPoisonEntries = {"storedpower"};
        String[] spreadWaveEntries = {};
        String[] contactEnergyEntries = {"powerwhip", "woodhammer"};
        String[] contactClawEntries = {"dragonclaw", "nightslash", "sacredsword", "knockdown"};
        String[] contactKickEntries = {"highjumpkick"};
        String[] contactWaveEntries = {"seismictoss"};
        String[] contactBiteEntries = {"bite", "superfang", "bugbite", "crunch", "pursuit"};
        String[] contactPoisonEntries = {"ironhead", "doubleedge", "bodyslam", "dragontail", "reversal",
                "punishment", "circlethrow", "knockoff", "endeavor", "strength"};
        String[] contactPunchEntries = {"closecombat", "hammerarm", "brickbreak", "poisonjab", "shadowpunch", "drainpunch",
                "focuspunch", "dynamicpunch", "cometpunch", "megapunch"};
        String[] drainEntries = {"hornleech", "absorb", "megadrain", "gigadrain"};
        for (String customized : customizedEntries) {
            mMoveAnimationEntries.put(customized, CUSTOMIZED);
        }
        for (String shake : shakeEntries) {
            mMoveAnimationEntries.put(shake, SHAKE);
        }
        for (String dance : danceEntries) {
            mMoveAnimationEntries.put(dance, DANCE);
        }
        for (String flight : flightEntries) {
            mMoveAnimationEntries.put(flight, FLIGHT);
        }
        for (String xatk : xatkEntries) {
            mMoveAnimationEntries.put(xatk, XATK);
        }
        for (String spinatk : spinatkEntries) {
            mMoveAnimationEntries.put(spinatk, SPINATK);
        }
        for (String self : selfEntries) {
            mMoveAnimationEntries.put(self, SELF);
        }
        for (String selfLight : selfLightEntries) {
            mMoveAnimationEntries.put(selfLight, SELF_LIGHT);
        }
        for (String selfDark : selfDarkEntries) {
            mMoveAnimationEntries.put(selfDark, SELF_DARK);
        }
        for (String trick : trickEntries) {
            mMoveAnimationEntries.put(trick, TRICK);
        }
        for (String charge : chargeEntries) {
            mMoveAnimationEntries.put(charge, CHARGE);
        }
        for (String spreadLight : spreadLightEntries) {
            mMoveAnimationEntries.put(spreadLight, SPREAD_LIGHT);
        }
        for (String spreadEnergy : spreadEnergyEntries) {
            mMoveAnimationEntries.put(spreadEnergy, SPREAD_ENERGY);
        }
        for (String spreadMist : spreadMistEntries) {
            mMoveAnimationEntries.put(spreadMist, SPREAD_MIST);
        }
        for (String spreadShadow : spreadShadowEntries) {
            mMoveAnimationEntries.put(spreadShadow, SPREAD_SHADOW);
        }
        for (String spreadPoison : spreadPoisonEntries) {
            mMoveAnimationEntries.put(spreadPoison, SPREAD_POISON);
        }
        for (String spreadWave : spreadWaveEntries) {
            mMoveAnimationEntries.put(spreadWave, SPREAD_WAVE);
        }
        for (String contactEnergy : contactEnergyEntries) {
            mMoveAnimationEntries.put(contactEnergy, CONTACT_ENERGY);
        }
        for (String contactClaw : contactClawEntries) {
            mMoveAnimationEntries.put(contactClaw, CONTACT_CLAW);
        }
        for (String contactKick : contactKickEntries) {
            mMoveAnimationEntries.put(contactKick, CONTACT_KICK);
        }
        for (String contactWave : contactWaveEntries) {
            mMoveAnimationEntries.put(contactWave, CONTACT_WAVE);
        }
        for (String contactBite : contactBiteEntries) {
            mMoveAnimationEntries.put(contactBite, CONTACT_BITE);
        }
        for (String contactPoison : contactPoisonEntries) {
            mMoveAnimationEntries.put(contactPoison, CONTACT_POISON);
        }
        for (String contactPunch : contactPunchEntries) {
            mMoveAnimationEntries.put(contactPunch, CONTACT_PUNCH);
        }
        for (String drain : drainEntries) {
            mMoveAnimationEntries.put(drain, DRAIN);
        }
    }
}
