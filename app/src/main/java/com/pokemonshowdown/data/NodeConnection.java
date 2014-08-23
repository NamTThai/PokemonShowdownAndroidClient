package com.pokemonshowdown.data;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by thain on 8/9/14.
 */
public class NodeConnection extends AsyncTask<String, Void, String> {
    public final static String CTAG = "PKM_SERVER_CONNECTION";

    @Override
    protected String doInBackground(String... params){
        try {
            downloadFromServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String downloadFromServer() throws IOException {
        InputStream inputStream = null;

        try {
            Log.d(CTAG, "Initiating connection");

            URL url = new URL("ws://nthai.cs.trincoll.edu:8000/showdown/websocket");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            int response = conn.getResponseCode();
            Log.d(CTAG, "The response code is : " + response);

            inputStream = conn.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader((inputStream)));

            String output;
            Log.d(CTAG, "Output from server...");
            while ((output = bufferedReader.readLine()) != null) {
                Log.d(CTAG, output);

                if (isCancelled()) break;
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return null;
    }
}
