package com.pokemonshowdown.replays;

import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.pokemonshowdown.app.BattleFragment;

import java.util.ArrayList;

/**
 * Replay instance
 * Returns events and such
 */
public class Replay {
    Fragment BattleFragment;

    //Each step is an seperate event in-game
    String [] steps;
    //Speed in milliseconds at which each event of the replay will last
    public float speed;

    public String player1;
    public String player1rank;
    public String player2;
    public String player2rank;
    public String gameType;
    public String tier;
    public boolean rated;

    public ArrayList<String> rules = new ArrayList<String>();
    public ArrayList<String> p1Pokemon = new ArrayList<String>();
    public ArrayList<String> p2Pokemon = new ArrayList<String>();
    public ArrayList<ArrayList> turns = new ArrayList<ArrayList>();

    //Currently only takes non-cached copies of replays
    public Replay(String replayData){
        //Thanks StackOverflow!
        steps = replayData.split(System.getProperty("line.separator"));
        //Both player join events
        player1 = steps[0].replace("|join|", "");
        player2 = steps[1].replace("|join|", "");
        //Player data display event
        player1rank = steps[2].replace("|player|p1|" + player1 + "|", "");
        player2rank = steps[3].replace("|player|p2|" + player2 + "|", "");
        gameType = steps[4].replace("|gametype|", "");
        tier = steps[5].replace("|tier|", "");
        rated = (steps[6].contains("|rated")) ? true : false;
        getElse();
    }
    //Get stuff like rules and pokemon
    void getElse(){
        //Record turns into arraylists
        boolean recordingTurn = false;
        ArrayList turnData = new ArrayList<String>();

        for(String s : steps){
            if(s.contains("|rule|")) {
                rules.add(s.replace("|rule|", ""));
            }
            //Pokemon can be M/F/shiny
            else if(s.contains("|poke|p1|")){
                p1Pokemon.add(s.replace("|poke|p1|",""));
            }
            else if(s.contains("|poke|p2|")){
                p2Pokemon.add(s.replace("|poke|p2|",""));
            }
            else if(s.contains("|turn|") && !recordingTurn) {
                recordingTurn = true;
            }
            else if(s.contains("|turn|") && recordingTurn){
                turns.add(turnData);
                turnData.clear();
            }
            else if(recordingTurn){
                turnData.add(s);
            }
        }
    }
    //Play the replay
    public void Play(){
        if(p1Pokemon.isEmpty() || p2Pokemon.isEmpty() || turns.isEmpty()) {
            Log.e("Replay Error", "Not all replay values have been specified. Are you sure you calle" +
                    "d the methods in the right order?");
            return;
        }

        BattleFragment frag = new com.pokemonshowdown.app.BattleFragment().newInstance("0");

        new CountDownTimer(3000, 1000) {

                public void onTick(long millisUntilFinished) {
                    //mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
                }

                public void onFinish() {
                    Log.i("test", "reached");
                }
            }.start();
        }

    //Pause the replay
    public void Pause(){

    }
    //Skip the turn
    public void Skip(){

    }
}
