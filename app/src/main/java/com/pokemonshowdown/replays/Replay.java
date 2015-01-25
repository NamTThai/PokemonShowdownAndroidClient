package com.pokemonshowdown.replays;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.util.ArrayList;

/**
 * Replay instance
 * Returns events and such
 */
public class Replay {
    Fragment BattleFragment;

    //Each step is an seprate event in-game
    //unformated steps for BattleMessage.java
    private ArrayList<String> mFormatedSteps = new ArrayList<String>();
    //Speed in milliseconds at which each event of the replay will last
    //if Battle fragment does not handle messages fast enough, possibility of an exception being thrown
    private long mSpeed;

    private com.pokemonshowdown.app.BattleFragment mReplayBattleFragment;
    private int mCurrentStep;

    public ArrayList<String> getSteps(){return mFormatedSteps;}

    public static ArrayList<String> getSteps(String replayData){
        String [] unformatedSteps;
        ArrayList<String> formatedSteps = new ArrayList<String>();
        unformatedSteps = replayData.split(System.getProperty("line.separator"));
        for(String str : unformatedSteps){
            formatedSteps.add(str);
        }
        return formatedSteps;
    }


    public Replay(String replayData, com.pokemonshowdown.app.BattleFragment replayBattleFragment){
        mReplayBattleFragment = replayBattleFragment;
        mFormatedSteps.add("");
        String[] mReplayData = replayData.split("::");
        for(String str : mReplayData){
            mFormatedSteps.add(reformat(str).replaceFirst("\\W", ""));
        }
    }

    //Converts the script.log to everything the battle message can understand
    //Not fully stable
    public static String reformat(String mLine){
        String args[] = mLine.split("\\|");
        //Houses temporary splits when string is reformated
        String temp[];

        String command;
        if(args.length < 2){
            return "";
        }
            command = args[1];
    Log.i("Command", command);
        switch(command){
            case "player":
                break;
            case "poke":
                break;
            case "switch":
                mLine = mLine.replace(",", ", L100,");
                mLine = mLine.replace("(", "");
                mLine = mLine.replace(")", "");
                mLine = mLine.replace("\\", "");
                //mLine = mLine.replace(" 100/100", "100/100");
                mLine = fixp1p2(mLine);
                args = mLine.split("\\|");
                temp = args[4].split(" ");
                temp[0] = "";
                args[4] = temp[1];
                mLine = rebuildString(args);
                break;
            case "-damage":
                mLine = mLine.replace("(" , "");
                mLine = mLine.replace(")" , "");
                mLine = mLine.replace("\\" , "");
                mLine = fixp1p2(mLine);
                args = mLine.split("\\|");
                temp = args[3].split(" ");
                temp[0] = "";
                args[3] = temp[1];
                mLine = rebuildString(args);
                break;
            case "-heal":
                mLine = mLine.replace("(" , "");
                mLine = mLine.replace(")" , "");
                mLine = mLine.replace("\\" , "");
                mLine = fixp1p2(mLine);
                args = mLine.split("\\|");
                temp = args[3].split(" ");
                temp[0] = "";
                args[3] = temp[1];
                mLine = rebuildString(args);
                break;
            default:
                mLine = mLine.replace("(" , "");
                mLine = mLine.replace(")" , "");
                mLine = mLine.replace("\\" , "");
                mLine = fixp1p2(mLine);
                break;
        }
        return mLine;
    }

    static String fixp1p2(String in){
        in = in.replaceAll("p1", "p1a");
        in = in.replaceAll("p2", "p2a");
        return in;
    }

    private void runClock(){
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                getStep();
            }
        }, mSpeed);
        Log.i("Replay Next Step #", Integer.toString(mCurrentStep));
    }

    private void getStep(){
        if(mFormatedSteps.get(mCurrentStep).equalsIgnoreCase("callback|decision")){
            mSpeed = 1500;
            mCurrentStep++;
            mReplayBattleFragment.processServerMessage(mFormatedSteps.get(mCurrentStep));
            Log.i("Replay Next Step", mFormatedSteps.get(mCurrentStep));
            runClock();
            return;
        }
        mReplayBattleFragment.processServerMessage(mFormatedSteps.get(mCurrentStep));
        mCurrentStep++;
        Log.i("Replay Next Step", mFormatedSteps.get(mCurrentStep));
        runClock();
        return;
    }

      /* BattleFragment.

        new CountDownTimer(3000, 1000) {

                public void onTick(long millisUntilFinished) {
                    //Random thing needed
                }

                public void onFinish() {
                    Log.i("test", "reached");
                }
            }.start();*/

    //Pause the replay
    public void Pause(){

    }
    //Skip the turn
    public void Skip(){

    }
    //Play the replay
    public void Play() {
        /*if (p1Pokemon.isEmpty() || p2Pokemon.isEmpty() || turns.isEmpty()) {
            Log.e("Replay Error", "Not all replay values have been specified. Are you sure you calle" +
                    "d the methods in the right order?");
            return;
        }*/
        runClock();
        return;
    }
    public static String rebuildString(String[] s){
        String v = "";
        for (String string : s) {
            v += string;
            v += "|";
        }
        return v;
    }


}