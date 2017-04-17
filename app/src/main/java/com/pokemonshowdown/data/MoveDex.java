package com.pokemonshowdown.data;

import android.content.Context;
import android.util.Log;

import com.pokemonshowdown.R;
import com.pokemonshowdown.application.MyApplication;

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
    private static MoveDex sMoveDex;
    private HashMap<String, String> mMoveDexEntries;
    private HashMap<String, Moves> mMoveAnimationEntries;

    private MoveDex(Context appContext) {
        mMoveDexEntries = readFile(appContext);
        initializeAnimationEntries();
    }

    public static String getMoveMaxPP(Context appContext, String name) {
        try {
            name = MyApplication.toId(name);
            JSONObject moveJson = MoveDex.get(appContext).getMoveJsonObject(name);
            return getMaxPP(moveJson.getString("pp"));
        } catch (JSONException | NullPointerException e) {
            return "0";
        }
    }

    public static MoveDex get(Context c) {
        if (sMoveDex == null) {
            sMoveDex = new MoveDex(c.getApplicationContext());
        }
        return sMoveDex;
    }

    public static String getMaxPP(String pp) {
        int ppInt = Integer.parseInt(pp);
        ppInt *= 1.6;
        return Integer.toString(ppInt);
    }

    public static String getMoveName(Context appContext, String name) {
        try {
            name = MyApplication.toId(name);
            JSONObject moveJson = MoveDex.get(appContext).getMoveJsonObject(name);
            return moveJson.getString("name");
        } catch (JSONException | NullPointerException e) {
            return null;
        }
    }

    public static int getMoveTypeIcon(Context appContext, String move) {
        try {
            move = MyApplication.toId(move);
            MoveDex moveDex;
            moveDex = MoveDex.get(appContext);
            String types = moveDex.getMoveJsonObject(move).getString("type");
            return appContext.getResources()
                    .getIdentifier("types_" + MyApplication.toId(types), "drawable", appContext.getPackageName());
        } catch (JSONException | NullPointerException e) {
            return 0;
        }
    }

    public static int getTypeIcon(Context appContext, String types) {
        return appContext.getResources()
                .getIdentifier("types_" + MyApplication.toId(types), "drawable", appContext.getPackageName());
    }

    public static int getCategoryIcon(Context appContext, String category) {
        return appContext.getResources()
                .getIdentifier("category_" + MyApplication.toId(category), "drawable", appContext.getPackageName());
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
                Object entry = jsonObject.get(key);
                MoveDexEntries.put(key, entry.toString());
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
        String[] shakeEntries = {"taunt", "swagger", "swordsdance", "quiverdance", "dragondance", "agility",
                "doubleteam", "metronome", "teeterdance", "splash", "encore"};
        String[] danceEntries = {"attract", "raindance", "sunnyday",
                "hail", "sandstorm", "gravity", "trickroom", "magicroom", "wonderroom", "captivate", "charm"};
        String[] flightEntries = {"aerialace", "bravebird", "acrobatics", "flyingpress", "drillpeck"};
        String[] xatkEntries = {"flail", "xscissor", "crosschop", "facade", "guillotine", "return", "frustration",
                "leafblade", "crosspoison"};
        String[] spinatkEntries = {"uturn", "rapidspin", "gyroball"};
        String[] selfEntries = {"reflect", "safeguard", "lightscreen", "mist", "transform", "bellydrum",
                "aromatherapy", "healbell", "magiccoat", "protect", "detect", "kingshield", "spikyshield",
                "endure", "bide", "rockpolish", "harden", "irondefense", "rest", "howl", "acupressure",
                "curse", "shiftgear", "autotomize", "bulkup", "workup", "honeclaws", "shellsmash", "stockpile",
                "ingrain", "aquaring", "coil", "refresh", "minimize", "doomdesire", "futuresight", "cottonguard",
                "roost", "softboiled", "milkdrink", "slackoff", "acidarmor", "substitute", "batonpass", "growth",
                "painsplit", "assist", "naturepower", "copycat", "sleeptalk", "meanlook", "destinybond",
                "reflecttype"};
        String[] selfLightEntries = {"barrier", "amnesia", "synthesis", "moonlight", "morningsun", "cosmicpower",
                "charge", "geomancy", "calmmind", "recover"};
        String[] selfDarkEntries = {"nastyplot", "tailglow", "camouflage"};
        String[] trickEntries = {"trick", "switcheroo"};
        String[] chargeEntries = {"shadowforce", "bounce", "dig", "dive", "fly", "skydrop", "skullbash", "skyattack",
                "phantomforce"};
        String[] spreadLightEntries = {"hiddenpower"};
        String[] spreadEnergyEntries = {"bugbuzz", "seedflare"};
        String[] spreadMistEntries = {"aurasphere"};
        String[] spreadShadowEntries = {"dragonpulse", "dragonbreath", "acidspray"};
        String[] spreadPoisonEntries = {"storedpower"};
        String[] spreadWaveEntries = {"waterspout"};
        String[] spreadFireEntries = {"eruption", "magmastorm", "inferno"};
        String[] spreadRockEntries = {"stealthrock"};
        String[] spreadSpikeEntries = {"spikes"};
        String[] spreadTspikeEntries = {"toxicspikes"};
        String[] spreadWebEntries = {"stickyweb", "electroweb"};
        String[] contactEnergyEntries = {"powerwhip", "woodhammer"};
        String[] contactClawEntries = {"dragonclaw", "nightslash", "sacredsword", "knockdown", "tailslap",
                "furyswipes", "furyattack", "shadowclaw", "shadowstrike", "scratch", "slash", "boneclub",
                "bonerush"};
        String[] contactKickEntries = {"highjumpkick", "jumpkick", "lowkick", "megakick", "blazekick", "lowsweep"};
        String[] contactWaveEntries = {"waterfall", "aquatail", "crabhammer"};
        String[] contactBiteEntries = {"bite", "superfang", "bugbite", "crunch", "pursuit"};
        String[] contactPoisonEntries = {"ironhead", "doubleedge", "bodyslam", "dragontail", "reversal",
                "punishment", "circlethrow", "knockoff", "endeavor", "strength", "counter", "payback", "revenge",
                "rockclimb", "retaliate", "tackle", "beatup"};
        String[] contactPunchEntries = {"closecombat", "hammerarm", "brickbreak", "poisonjab", "shadowpunch", "drainpunch",
                "focuspunch", "dynamicpunch", "cometpunch", "megapunch", "meteormash", "skyuppercut", "superpower",
                "playrough", "armthrust"};
        String[] contactShadowEntries = {"megahorn"};
        String[] contactThunderEntries = {"wildcharge", "volttackle", "boltstrike"};
        String[] contactFireEntries = {"flareblitz", "vcreate", "flamewheel", "flamecharge"};
        String[] contactNeutralEntries = {"gigaimpact", "outrage", "dragonascent", "magikarpsrevenge", "headsmash",
                "headcharge"};
        String[] contactMistEntries = {"zenheadbutt"};
        String[] contactLightEntries = {"seismictoss", "struggle"};
        String[] drainEntries = {"hornleech", "absorb", "megadrain", "gigadrain", "drainingkiss"};
        String[] fastEntries = {"extremespeed", "quickattack", "suckerpunch", "bulletpunch", "machpunch", "fakeout",
                "shadowsneak", "faintattack", "mefirst", "aquajet", "iceshard"};
        String[] punchFireEntries = {"firepunch", "precipiceblades"};
        String[] punchIceEntries = {"icepunch"};
        String[] punchThunderEntries = {"thunderpunch"};
        String[] biteFireEntries = {"firefang"};
        String[] biteIceEntries = {"icefang"};
        String[] biteThunderEntries = {"thunderfang"};
        String[] streamNeutralEntries = {"hyperbeam", "geargrind", "triattack"};
        String[] streamLightEntries = {"judgment", "signalbeam"};
        String[] streamEnergyEntries = {"bulletseed", "seedbomb", "grassknot", "petaldance", "magicalleaf",
                "pinmissile", "twinneedle", "attackorder"};
        String[] streamMistEntries = {"psychic", "psyshock", "psychoboost", "psystrike", "lightofruin"};
        String[] streamPoisonEntries = {"sludgebomb", "acid", "sludgewave"};
        String[] streamShadowEntries = {"nightshade", "foulplay", "darkvoid"};
        String[] streamWaterEntries = {"surf"};
        String[] streamFireEntries = {"flamethrower", "heatwave", "lavaplume", "searingshot", "fierydance", "ember",
                "incinerate"};
        String[] streamIceEntries = {"iciclespear", "icebeam", "avalanche", "freezeshock", "iceburn", "icywind",
                "glaciate", "freezedry", "iciclecrash"};
        String[] streamRockEntries = {"rockblast", "powergem", "rockslide", "diamondstorm", "paleowave", "ancientpower",
                "rocktomb", "thousandarrows", "smackdown"};
        String[] earthEntries = {"earthquake", "earthpower", "magnitude", "fissure", "bulldoze", "drillrun", "thousandwaves"};
        String[] phazeEntries = {"whirlwind", "hurricane", "oblivionwing", "gust", "twister", "aeroblast"};
        String[] thunderStrongEntries = {"thunder", "fushionbolt", "zapcannon"};
        String[] thunderNeutralEntries = {"thunderbolt", "shockwave", "discharge"};
        String[] thunderWeakEntries = {"voltswitch", "chargebeam"};
        String[] statusPsnEntries = {"toxic"};
        String[] statusParEntries = {"thunderwave"};
        String[] statusSlpEntries = {"spore", "leechseed", "sleeppowder", "poisonpowder", "stunspore", "powder"};
        String[] statusBrnEntries = {"willowisp"};
        String[] ballNeutralEntries = {"roar", "hypervoice", "chatter", "round", "yawn", "sing",
                "perishsong", "echoedvoice", "relicsong", "partingshot", "nobleroar", "defog", "clearsmog"};
        String[] ballLightEntries = {"electroball", "weatherball", "flashcannon", "lusterpurge"};
        String[] ballEnergyEntries = {"energyball"};
        String[] ballMistEntries = {"mistball", "dazzlinggleam", "moonblast", "hyperspacehole"};
        String[] ballPoisonEntries = {};
        String[] ballShadowEntries = {"shadowball", "hex", "darkpulse", "nightdaze", "snarl"};
        String[] ballWaterEntries = {"scald", "waterpulse", "bubblebeam", "brine", "bubble", "watersport"};
        String[] ballFireEntries = {"sacredfire", "blueflare", "fusionflare"};
        String[] ballIceEntries = {"blizzard", "sheercold"};
        String[] ballRockEntries = {"stoneedge"};
        String[] wishEntries = {"wish", "healingwish", "heartswap", "lunardance", "tailwind"};
        String[] slashEntries = {"airslash", "aircutter", "razorwind", "psychocut", "secretsword"};
        String[] bombNeutralEntries = {"explosion", "selfdestruct"};
        String[] bombLightEntries = {"boomburst"};
        String[] bombEnergyEntries = {"solarbeam", "leafstorm", "frenzyplant"};
        String[] bombMistEntries = {"focusblast"};
        String[] bombPoisonEntries = {"gunkshot"};
        String[] bombShadowEntries = {"dracometeor", "roaroftime", "spacialrend", "hyperspacefury"};
        String[] bombWaterEntries = {"hydropump", "originpulse", "hydrocannon"};
        String[] bombFireEntries = {"fireblast", "overheat", "blastburn"};
        for (String shake : shakeEntries) {
            mMoveAnimationEntries.put(shake, Moves.SHAKE);
        }
        for (String dance : danceEntries) {
            mMoveAnimationEntries.put(dance, Moves.DANCE);
        }
        for (String flight : flightEntries) {
            mMoveAnimationEntries.put(flight, Moves.FLIGHT);
        }
        for (String xatk : xatkEntries) {
            mMoveAnimationEntries.put(xatk, Moves.XATK);
        }
        for (String spinatk : spinatkEntries) {
            mMoveAnimationEntries.put(spinatk, Moves.SPINATK);
        }
        for (String self : selfEntries) {
            mMoveAnimationEntries.put(self, Moves.SELF);
        }
        for (String selfLight : selfLightEntries) {
            mMoveAnimationEntries.put(selfLight, Moves.SELF_LIGHT);
        }
        for (String selfDark : selfDarkEntries) {
            mMoveAnimationEntries.put(selfDark, Moves.SELF_DARK);
        }
        for (String trick : trickEntries) {
            mMoveAnimationEntries.put(trick, Moves.TRICK);
        }
        for (String charge : chargeEntries) {
            mMoveAnimationEntries.put(charge, Moves.CHARGE);
        }
        for (String spreadLight : spreadLightEntries) {
            mMoveAnimationEntries.put(spreadLight, Moves.SPREAD_LIGHT);
        }
        for (String spreadEnergy : spreadEnergyEntries) {
            mMoveAnimationEntries.put(spreadEnergy, Moves.SPREAD_ENERGY);
        }
        for (String spreadMist : spreadMistEntries) {
            mMoveAnimationEntries.put(spreadMist, Moves.SPREAD_MIST);
        }
        for (String spreadShadow : spreadShadowEntries) {
            mMoveAnimationEntries.put(spreadShadow, Moves.SPREAD_SHADOW);
        }
        for (String spreadPoison : spreadPoisonEntries) {
            mMoveAnimationEntries.put(spreadPoison, Moves.SPREAD_POISON);
        }
        for (String spreadWave : spreadWaveEntries) {
            mMoveAnimationEntries.put(spreadWave, Moves.SPREAD_WAVE);
        }
        for (String spreadFire : spreadFireEntries) {
            mMoveAnimationEntries.put(spreadFire, Moves.SPREAD_FIRE);
        }
        for (String spreadRock : spreadRockEntries) {
            mMoveAnimationEntries.put(spreadRock, Moves.SPREAD_ROCK);
        }
        for (String spreadSpike : spreadSpikeEntries) {
            mMoveAnimationEntries.put(spreadSpike, Moves.SPREAD_SPIKE);
        }
        for (String spreadTspike : spreadTspikeEntries) {
            mMoveAnimationEntries.put(spreadTspike, Moves.SPREAD_TSPIKE);
        }
        for (String spreadWeb : spreadWebEntries) {
            mMoveAnimationEntries.put(spreadWeb, Moves.SPREAD_WEB);
        }
        for (String contactEnergy : contactEnergyEntries) {
            mMoveAnimationEntries.put(contactEnergy, Moves.CONTACT_ENERGY);
        }
        for (String contactClaw : contactClawEntries) {
            mMoveAnimationEntries.put(contactClaw, Moves.CONTACT_CLAW);
        }
        for (String contactKick : contactKickEntries) {
            mMoveAnimationEntries.put(contactKick, Moves.CONTACT_KICK);
        }
        for (String contactWave : contactWaveEntries) {
            mMoveAnimationEntries.put(contactWave, Moves.CONTACT_WAVE);
        }
        for (String contactBite : contactBiteEntries) {
            mMoveAnimationEntries.put(contactBite, Moves.CONTACT_BITE);
        }
        for (String contactPoison : contactPoisonEntries) {
            mMoveAnimationEntries.put(contactPoison, Moves.CONTACT_POISON);
        }
        for (String contactPunch : contactPunchEntries) {
            mMoveAnimationEntries.put(contactPunch, Moves.CONTACT_PUNCH);
        }
        for (String contactShadow : contactShadowEntries) {
            mMoveAnimationEntries.put(contactShadow, Moves.CONTACT_SHADOW);
        }
        for (String contactThunder : contactThunderEntries) {
            mMoveAnimationEntries.put(contactThunder, Moves.CONTACT_THUNDER);
        }
        for (String contactFire : contactFireEntries) {
            mMoveAnimationEntries.put(contactFire, Moves.CONTACT_FIRE);
        }
        for (String contactNeutral : contactNeutralEntries) {
            mMoveAnimationEntries.put(contactNeutral, Moves.CONTACT_NEUTRAL);
        }
        for (String contactMist : contactMistEntries) {
            mMoveAnimationEntries.put(contactMist, Moves.CONTACT_MIST);
        }
        for (String contactLight : contactLightEntries) {
            mMoveAnimationEntries.put(contactLight, Moves.CONTACT_LIGHT);
        }
        for (String drain : drainEntries) {
            mMoveAnimationEntries.put(drain, Moves.DRAIN);
        }
        for (String fast : fastEntries) {
            mMoveAnimationEntries.put(fast, Moves.FAST);
        }
        for (String punchFire : punchFireEntries) {
            mMoveAnimationEntries.put(punchFire, Moves.CONTACT_PUNCH_FIRE);
        }
        for (String punchIce : punchIceEntries) {
            mMoveAnimationEntries.put(punchIce, Moves.CONTACT_PUNCH_ICE);
        }
        for (String punchThunder : punchThunderEntries) {
            mMoveAnimationEntries.put(punchThunder, Moves.CONTACT_PUNCH_THUNDER);
        }
        for (String biteFire : biteFireEntries) {
            mMoveAnimationEntries.put(biteFire, Moves.CONTACT_BITE_FIRE);
        }
        for (String biteIce : biteIceEntries) {
            mMoveAnimationEntries.put(biteIce, Moves.CONTACT_BITE_ICE);
        }
        for (String biteThunder : biteThunderEntries) {
            mMoveAnimationEntries.put(biteThunder, Moves.CONTACT_BITE_THUNDER);
        }
        for (String streamNeutral : streamNeutralEntries) {
            mMoveAnimationEntries.put(streamNeutral, Moves.STREAM_NEUTRAL);
        }
        for (String streamLight : streamLightEntries) {
            mMoveAnimationEntries.put(streamLight, Moves.STREAM_LIGHT);
        }
        for (String streamEnergy : streamEnergyEntries) {
            mMoveAnimationEntries.put(streamEnergy, Moves.STREAM_ENERGY);
        }
        for (String streamMist : streamMistEntries) {
            mMoveAnimationEntries.put(streamMist, Moves.STREAM_MIST);
        }
        for (String streamPoison : streamPoisonEntries) {
            mMoveAnimationEntries.put(streamPoison, Moves.STREAM_POISON);
        }
        for (String streamShadow : streamShadowEntries) {
            mMoveAnimationEntries.put(streamShadow, Moves.STREAM_SHADOW);
        }
        for (String streamWater : streamWaterEntries) {
            mMoveAnimationEntries.put(streamWater, Moves.STREAM_WATER);
        }
        for (String streamFire : streamFireEntries) {
            mMoveAnimationEntries.put(streamFire, Moves.STREAM_FIRE);
        }
        for (String streamIce : streamIceEntries) {
            mMoveAnimationEntries.put(streamIce, Moves.STREAM_ICE);
        }
        for (String streamRock : streamRockEntries) {
            mMoveAnimationEntries.put(streamRock, Moves.STREAM_ROCK);
        }
        for (String earth : earthEntries) {
            mMoveAnimationEntries.put(earth, Moves.EARTH);
        }
        for (String phaze : phazeEntries) {
            mMoveAnimationEntries.put(phaze, Moves.PHAZE);
        }
        for (String thunderStrong : thunderStrongEntries) {
            mMoveAnimationEntries.put(thunderStrong, Moves.THUNDER_STRONG);
        }
        for (String thunderNeutral : thunderNeutralEntries) {
            mMoveAnimationEntries.put(thunderNeutral, Moves.THUNDER_NEUTRAL);
        }
        for (String thunderWeak : thunderWeakEntries) {
            mMoveAnimationEntries.put(thunderWeak, Moves.THUNDER_WEAK);
        }
        for (String statusPsn : statusPsnEntries) {
            mMoveAnimationEntries.put(statusPsn, Moves.STATUS_PSN);
        }
        for (String statusPar : statusParEntries) {
            mMoveAnimationEntries.put(statusPar, Moves.STATUS_PAR);
        }
        for (String statusSlp : statusSlpEntries) {
            mMoveAnimationEntries.put(statusSlp, Moves.STATUS_SLP);
        }
        for (String statusBrn : statusBrnEntries) {
            mMoveAnimationEntries.put(statusBrn, Moves.STATUS_BRN);
        }
        for (String ballNeutral : ballNeutralEntries) {
            mMoveAnimationEntries.put(ballNeutral, Moves.BALL_NEUTRAL);
        }
        for (String ballLight : ballLightEntries) {
            mMoveAnimationEntries.put(ballLight, Moves.BALL_LIGHT);
        }
        for (String ballEnergy : ballEnergyEntries) {
            mMoveAnimationEntries.put(ballEnergy, Moves.BALL_ENERGY);
        }
        for (String ballMist : ballMistEntries) {
            mMoveAnimationEntries.put(ballMist, Moves.BALL_MIST);
        }
        for (String ballPoison : ballPoisonEntries) {
            mMoveAnimationEntries.put(ballPoison, Moves.BALL_POISON);
        }
        for (String ballShadow : ballShadowEntries) {
            mMoveAnimationEntries.put(ballShadow, Moves.BALL_SHADOW);
        }
        for (String ballWater : ballWaterEntries) {
            mMoveAnimationEntries.put(ballWater, Moves.BALL_WATER);
        }
        for (String ballFire : ballFireEntries) {
            mMoveAnimationEntries.put(ballFire, Moves.BALL_FIRE);
        }
        for (String ballIce : ballIceEntries) {
            mMoveAnimationEntries.put(ballIce, Moves.BALL_ICE);
        }
        for (String ballRock : ballRockEntries) {
            mMoveAnimationEntries.put(ballRock, Moves.BALL_ROCK);
        }
        for (String wish : wishEntries) {
            mMoveAnimationEntries.put(wish, Moves.WISH);
        }
        for (String slash : slashEntries) {
            mMoveAnimationEntries.put(slash, Moves.SLASH);
        }
        for (String bombNeutral : bombNeutralEntries) {
            mMoveAnimationEntries.put(bombNeutral, Moves.BOMB_NEUTRAL);
        }
        for (String bombLight : bombLightEntries) {
            mMoveAnimationEntries.put(bombLight, Moves.BOMB_LIGHT);
        }
        for (String bombEnergy : bombEnergyEntries) {
            mMoveAnimationEntries.put(bombEnergy, Moves.BOMB_ENERGY);
        }
        for (String bombMist : bombMistEntries) {
            mMoveAnimationEntries.put(bombMist, Moves.BOMB_MIST);
        }
        for (String bombPoison : bombPoisonEntries) {
            mMoveAnimationEntries.put(bombPoison, Moves.BOMB_POISON);
        }
        for (String bombShadow : bombShadowEntries) {
            mMoveAnimationEntries.put(bombShadow, Moves.BOMB_SHADOW);
        }
        for (String bombWater : bombWaterEntries) {
            mMoveAnimationEntries.put(bombWater, Moves.BOMB_WATER);
        }
        for (String bombFire : bombFireEntries) {
            mMoveAnimationEntries.put(bombFire, Moves.BOMB_FIRE);
        }
    }

    public JSONObject getMoveJsonObject(String name) {
        name = MyApplication.toId(name);
        try {
            String move = mMoveDexEntries.get(name);
            return new JSONObject(move);
        } catch (JSONException | NullPointerException e) {
            return null;
        }
    }

    public HashMap<String, String> getMoveDexEntries() {
        return mMoveDexEntries;
    }

    public Moves getMoveAnimationEntry(String move) {
        return mMoveAnimationEntries.get(move);
    }

    public enum Moves {
        SHAKE, DANCE, FLIGHT, SPINATK, XATK, SELF, SELF_LIGHT, SELF_DARK, TRICK, CHARGE,
        SPREAD_LIGHT, SPREAD_ENERGY, SPREAD_MIST, SPREAD_SHADOW, SPREAD_POISON, SPREAD_WAVE, SPREAD_FIRE, SPREAD_ROCK, SPREAD_SPIKE, SPREAD_TSPIKE, SPREAD_WEB,
        CONTACT_ENERGY, CONTACT_CLAW, CONTACT_KICK, CONTACT_WAVE, CONTACT_BITE, CONTACT_POISON, CONTACT_PUNCH, CONTACT_SHADOW, CONTACT_THUNDER, CONTACT_FIRE, CONTACT_NEUTRAL, CONTACT_MIST, CONTACT_LIGHT,
        DRAIN, FAST,
        CONTACT_PUNCH_FIRE, CONTACT_PUNCH_ICE, CONTACT_PUNCH_THUNDER, CONTACT_BITE_FIRE, CONTACT_BITE_ICE, CONTACT_BITE_THUNDER,
        STREAM_NEUTRAL, STREAM_LIGHT, STREAM_ENERGY, STREAM_MIST, STREAM_POISON, STREAM_SHADOW, STREAM_WATER, STREAM_FIRE, STREAM_ICE, STREAM_ROCK,
        EARTH, PHAZE, THUNDER_STRONG, THUNDER_NEUTRAL, THUNDER_WEAK, STATUS_PSN, STATUS_PAR, STATUS_SLP, STATUS_BRN,
        BALL_NEUTRAL, BALL_LIGHT, BALL_ENERGY, BALL_MIST, BALL_POISON, BALL_SHADOW, BALL_WATER, BALL_FIRE, BALL_ICE, BALL_ROCK,
        WISH, SLASH,
        BOMB_NEUTRAL, BOMB_LIGHT, BOMB_ENERGY, BOMB_MIST, BOMB_POISON, BOMB_SHADOW, BOMB_WATER, BOMB_FIRE
    }
}
