package com.pokemonshowdown.data;

import android.util.Log;

import java.util.HashMap;

public class ServerOutput {
    private final static String STAG = "ServerOutput";

    public static String breakServerOutput(String output) {
        String command = output.substring(1, output.indexOf("|", output.indexOf("|") + 1));
        Log.d(STAG, command);
        switch (command) {
            case "init":
                return output.substring(output.indexOf("|", output.indexOf("|") + 1) + 1);
            default:
                return null;
        }
    }
}
