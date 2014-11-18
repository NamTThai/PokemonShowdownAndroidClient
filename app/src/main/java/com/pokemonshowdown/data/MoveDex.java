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
    public final static Integer SPREAD_LIGHT = -1;
    public final static Integer SPREAD_ENERGY = -2;
    public final static Integer SPREAD_MIST = -3;
    public final static Integer SPREAD_SHADOW = -4;
    public final static Integer SPREAD_POISON = -5;
    public final static Integer SPREAD_WAVE = -6;
    public final static Integer SPREAD_FIRE = -7;
    public final static Integer SPREAD_ROCK = -8;
    public final static Integer SPREAD_SPIKE = -9;
    public final static Integer SPREAD_TSPIKE = -10;
    public final static Integer SPREAD_WEB = -11;
    public final static Integer CONTACT_ENERGY = 101;
    public final static Integer CONTACT_CLAW = 102;
    public final static Integer CONTACT_KICK = 103;
    public final static Integer CONTACT_WAVE = 104;
    public final static Integer CONTACT_BITE = 105;
    public final static Integer CONTACT_POISON = 106;
    public final static Integer CONTACT_PUNCH = 107;
    public final static Integer CONTACT_SHADOW = 108;
    public final static Integer CONTACT_THUNDER = 109;
    public final static Integer CONTACT_FIRE = 110;
    public final static Integer CONTACT_NEUTRAL = 111;
    public final static Integer CONTACT_MIST = 112;
    public final static Integer CONTACT_LIGHT = 113;
    public final static Integer DRAIN = 25;
    public final static Integer FAST = 26;
    public final static Integer CONTACT_PUNCH_FIRE = 27;
    public final static Integer CONTACT_PUNCH_ICE = 28;
    public final static Integer CONTACT_PUNCH_THUNDER = 29;
    public final static Integer CONTACT_BITE_FIRE = 30;
    public final static Integer CONTACT_BITE_ICE = 31;
    public final static Integer CONTACT_BITE_THUNDER = 32;
    public final static Integer STREAM_NEUTRAL = 301;
    public final static Integer STREAM_LIGHT = 302;
    public final static Integer STREAM_ENERGY = 303;
    public final static Integer STREAM_MIST = 304;
    public final static Integer STREAM_POISON = 305;
    public final static Integer STREAM_SHADOW = 306;
    public final static Integer STREAM_WATER = 307;
    public final static Integer STREAM_FIRE = 308;
    public final static Integer STREAM_ICE = 309;
    public final static Integer STREAM_ROCK = 310;
    public final static Integer EARTH = 33;
    public final static Integer PHAZE = 34;
    public final static Integer THUNDER_STRONG = 35;
    public final static Integer THUNDER_NEUTRAL = 36;
    public final static Integer THUNDER_WEAK = 37;
    public final static Integer STATUS_PSN = 401;
    public final static Integer STATUS_PAR = 402;
    public final static Integer STATUS_SLP = 403;
    public final static Integer STATUS_BRN = 404;
    public final static Integer BALL_NEUTRAL = 501;
    public final static Integer BALL_LIGHT = 502;
    public final static Integer BALL_ENERGY = 503;
    public final static Integer BALL_MIST = 504;
    public final static Integer BALL_POISON = 505;
    public final static Integer BALL_SHADOW = 506;
    public final static Integer BALL_WATER = 507;
    public final static Integer BALL_FIRE = 508;
    public final static Integer BALL_ICE = 509;
    public final static Integer BALL_ROCK = 510;
    public final static Integer WISH = 38;
    public final static Integer SLASH = 39;
    public final static Integer BOMB_NEUTRAL = 601;
    public final static Integer BOMB_LIGHT = 602;
    public final static Integer BOMB_ENERGY = 603;
    public final static Integer BOMB_MIST = 604;
    public final static Integer BOMB_POISON = 605;
    public final static Integer BOMB_SHADOW = 606;
    public final static Integer BOMB_WATER = 607;
    public final static Integer BOMB_FIRE = 608;

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
                "curse", "shiftgear", "autotomize", "bulkup", "workup", "honeclaws", "shellsmash","stockpile",
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
                "perishsong", "echoedvoice", "relicsong", "partingshot", "nobleroar", "defog"};
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
        for (String spreadFire : spreadFireEntries) {
            mMoveAnimationEntries.put(spreadFire, SPREAD_FIRE);
        }
        for (String spreadRock : spreadRockEntries) {
            mMoveAnimationEntries.put(spreadRock, SPREAD_ROCK);
        }
        for (String spreadSpike : spreadSpikeEntries) {
            mMoveAnimationEntries.put(spreadSpike, SPREAD_SPIKE);
        }
        for (String spreadTspike : spreadTspikeEntries) {
            mMoveAnimationEntries.put(spreadTspike, SPREAD_TSPIKE);
        }
        for (String spreadWeb : spreadWebEntries) {
            mMoveAnimationEntries.put(spreadWeb, SPREAD_WEB);
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
        for (String contactShadow : contactShadowEntries) {
            mMoveAnimationEntries.put(contactShadow, CONTACT_SHADOW);
        }
        for (String contactThunder : contactThunderEntries) {
            mMoveAnimationEntries.put(contactThunder, CONTACT_THUNDER);
        }
        for (String contactFire : contactFireEntries) {
            mMoveAnimationEntries.put(contactFire, CONTACT_FIRE);
        }
        for (String contactNeutral : contactNeutralEntries) {
            mMoveAnimationEntries.put(contactNeutral, CONTACT_NEUTRAL);
        }
        for (String contactMist : contactMistEntries) {
            mMoveAnimationEntries.put(contactMist, CONTACT_MIST);
        }
        for (String contactLight : contactLightEntries) {
            mMoveAnimationEntries.put(contactLight, CONTACT_LIGHT);
        }
        for (String drain : drainEntries) {
            mMoveAnimationEntries.put(drain, DRAIN);
        }
        for (String fast : fastEntries) {
            mMoveAnimationEntries.put(fast, FAST);
        }
        for (String punchFire : punchFireEntries) {
            mMoveAnimationEntries.put(punchFire, CONTACT_PUNCH_FIRE);
        }
        for (String punchIce : punchIceEntries) {
            mMoveAnimationEntries.put(punchIce, CONTACT_PUNCH_ICE);
        }
        for (String punchThunder : punchThunderEntries) {
            mMoveAnimationEntries.put(punchThunder, CONTACT_PUNCH_THUNDER);
        }
        for (String biteFire : biteFireEntries) {
            mMoveAnimationEntries.put(biteFire, CONTACT_BITE_FIRE);
        }
        for (String biteIce : biteIceEntries) {
            mMoveAnimationEntries.put(biteIce, CONTACT_BITE_ICE);
        }
        for (String biteThunder : biteThunderEntries) {
            mMoveAnimationEntries.put(biteThunder, CONTACT_BITE_THUNDER);
        }
        for (String streamNeutral : streamNeutralEntries) {
            mMoveAnimationEntries.put(streamNeutral, STREAM_NEUTRAL);
        }
        for (String streamLight : streamLightEntries) {
            mMoveAnimationEntries.put(streamLight, STREAM_LIGHT);
        }
        for (String streamEnergy : streamEnergyEntries) {
            mMoveAnimationEntries.put(streamEnergy, STREAM_ENERGY);
        }
        for (String streamMist : streamMistEntries) {
            mMoveAnimationEntries.put(streamMist, STREAM_MIST);
        }
        for (String streamPoison : streamPoisonEntries) {
            mMoveAnimationEntries.put(streamPoison, STREAM_POISON);
        }
        for (String streamShadow : streamShadowEntries) {
            mMoveAnimationEntries.put(streamShadow, STREAM_SHADOW);
        }
        for (String streamWater : streamWaterEntries) {
            mMoveAnimationEntries.put(streamWater, STREAM_WATER);
        }
        for (String streamFire : streamFireEntries) {
            mMoveAnimationEntries.put(streamFire, STREAM_FIRE);
        }
        for (String streamIce : streamIceEntries) {
            mMoveAnimationEntries.put(streamIce, STREAM_ICE);
        }
        for (String streamRock : streamRockEntries) {
            mMoveAnimationEntries.put(streamRock, STREAM_ROCK);
        }
        for (String earth : earthEntries) {
            mMoveAnimationEntries.put(earth, EARTH);
        }
        for (String phaze : phazeEntries) {
            mMoveAnimationEntries.put(phaze, PHAZE);
        }
        for (String thunderStrong : thunderStrongEntries) {
            mMoveAnimationEntries.put(thunderStrong, THUNDER_STRONG);
        }
        for (String thunderNeutral : thunderNeutralEntries) {
            mMoveAnimationEntries.put(thunderNeutral, THUNDER_NEUTRAL);
        }
        for (String thunderWeak : thunderWeakEntries) {
            mMoveAnimationEntries.put(thunderWeak, THUNDER_WEAK);
        }
        for (String statusPsn : statusPsnEntries) {
            mMoveAnimationEntries.put(statusPsn, STATUS_PSN);
        }
        for (String statusPar : statusParEntries) {
            mMoveAnimationEntries.put(statusPar, STATUS_PAR);
        }
        for (String statusSlp : statusSlpEntries) {
            mMoveAnimationEntries.put(statusSlp, STATUS_SLP);
        }
        for (String statusBrn : statusBrnEntries) {
            mMoveAnimationEntries.put(statusBrn, STATUS_BRN);
        }
        for (String ballNeutral : ballNeutralEntries) {
            mMoveAnimationEntries.put(ballNeutral, BALL_NEUTRAL);
        }
        for (String ballLight : ballLightEntries) {
            mMoveAnimationEntries.put(ballLight, BALL_LIGHT);
        }
        for (String ballEnergy : ballEnergyEntries) {
            mMoveAnimationEntries.put(ballEnergy, BALL_ENERGY);
        }
        for (String ballMist : ballMistEntries) {
            mMoveAnimationEntries.put(ballMist, BALL_MIST);
        }
        for (String ballPoison : ballPoisonEntries) {
            mMoveAnimationEntries.put(ballPoison, BALL_POISON);
        }
        for (String ballShadow : ballShadowEntries) {
            mMoveAnimationEntries.put(ballShadow, BALL_SHADOW);
        }
        for (String ballWater : ballWaterEntries) {
            mMoveAnimationEntries.put(ballWater, BALL_WATER);
        }
        for (String ballFire : ballFireEntries) {
            mMoveAnimationEntries.put(ballFire, BALL_FIRE);
        }
        for (String ballIce : ballIceEntries) {
            mMoveAnimationEntries.put(ballIce, BALL_ICE);
        }
        for (String ballRock : ballRockEntries) {
            mMoveAnimationEntries.put(ballRock, BALL_ROCK);
        }
        for (String wish : wishEntries) {
            mMoveAnimationEntries.put(wish, WISH);
        }
        for (String slash : slashEntries) {
            mMoveAnimationEntries.put(slash, SLASH);
        }
        for (String bombNeutral : bombNeutralEntries) {
            mMoveAnimationEntries.put(bombNeutral, BOMB_NEUTRAL);
        }
        for (String bombLight : bombLightEntries) {
            mMoveAnimationEntries.put(bombLight, BOMB_LIGHT);
        }
        for (String bombEnergy : bombEnergyEntries) {
            mMoveAnimationEntries.put(bombEnergy, BOMB_ENERGY);
        }
        for (String bombMist : bombMistEntries) {
            mMoveAnimationEntries.put(bombMist, BOMB_MIST);
        }
        for (String bombPoison : bombPoisonEntries) {
            mMoveAnimationEntries.put(bombPoison, BOMB_POISON);
        }
        for (String bombShadow : bombShadowEntries) {
            mMoveAnimationEntries.put(bombShadow, BOMB_SHADOW);
        }
        for (String bombWater : bombWaterEntries) {
            mMoveAnimationEntries.put(bombWater, BOMB_WATER);
        }
        for (String bombFire : bombFireEntries) {
            mMoveAnimationEntries.put(bombFire, BOMB_FIRE);
        }
    }
}
