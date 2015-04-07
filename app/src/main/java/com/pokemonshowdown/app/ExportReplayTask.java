package com.pokemonshowdown.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by c.bouvet on 4/7/2015.
 */
public class ExportReplayTask extends AsyncTask<String, String, Boolean> {
    private final static String REPLAY_EXPORT_URL = "https://play.pokemonshowdown.com/~~showdown/action.php?act=uploadreplay";

    private ProgressDialog waitingDialog;
    private Context context;
    private Exception e;
    private String roomId;

    public ExportReplayTask(Context context) {
        this.context = context;
        this.e = null;
    }

    @Override
    protected void onPreExecute() {
        waitingDialog = new ProgressDialog(context);
        waitingDialog.setIndeterminate(true);
        waitingDialog.setCancelable(false);
        waitingDialog.setMessage(context.getResources().getString(R.string.exporting_replay));
        waitingDialog.show();
    }

    @Override
    protected void onPostExecute(Boolean success) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        if (success) {
            String replayURL = "http://replay.pokemonshowdown.com/" + roomId;
            builder.setMessage(String.format(context.getResources().getString(R.string.replay_exported), replayURL));
        } else {
            builder.setMessage(R.string.replay_exporting_failure);
        }

        builder.setPositiveButton(R.string.dialog_ok, null);
        final AlertDialog alert = builder.create();
        waitingDialog.dismiss();
        alert.show();
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        String replayData = strings[0];
        try {
            JSONObject jsonObject = new JSONObject(replayData);
            roomId = jsonObject.getString("id");
            String postData = "log=" + jsonObject.getString("log") + "&id=" + jsonObject.getString("id");
            URL url = new URL(REPLAY_EXPORT_URL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());

            // Send request
            outStream.writeBytes(postData);
            outStream.flush();
            outStream.close();

            InputStream inputStream = conn.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader((inputStream)));
            String output = bufferedReader.readLine();
            inputStream.close();
            if (output.equals("success")) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            this.e = e;
            return false;
        }
    }
}
