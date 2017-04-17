package com.pokemonshowdown;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.pokemonshowdown.fragment.MainScreenFragment;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by c.bouvet on 4/7/2015.
 */
public class ExportReplayTask extends AsyncTask<String, String, Boolean> {
    private final static String REPLAY_EXPORT_URL = "https://play.pokemonshowdown.app.com/~~showdown/action.php?act=uploadreplay";

    private ProgressDialog mWaitingDialog;
    private Context context;
    private Exception e;
    private String roomId;

    public ExportReplayTask(Context context) {
        this.context = context;
        this.e = null;
    }

    @Override
    protected void onPreExecute() {
        mWaitingDialog = new ProgressDialog(context);
        mWaitingDialog.setIndeterminate(true);
        mWaitingDialog.setCancelable(false);
        mWaitingDialog.setMessage(context.getResources().getString(R.string.exporting_replay));
        mWaitingDialog.show();
    }

    @Override
    protected void onPostExecute(Boolean success) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        if (success) {
            String replayURL = "http://replay.pokemonshowdown.app.com/" + roomId;
            builder.setMessage(String.format(context.getResources().getString(R.string.replay_exported), replayURL));
        } else {
            builder.setMessage(R.string.replay_exporting_failure);
        }

        builder.setPositiveButton(R.string.dialog_ok, null);
        final AlertDialog alert = builder.create();
        mWaitingDialog.dismiss();
        MainScreenFragment.TABS_HOLDER_ACCESSOR.removeTab(true);
        alert.show();
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        String replayData = strings[0];
        try {
            JSONObject jsonObject = new JSONObject(replayData);
            roomId = jsonObject.getString("id");
            String postData = "log=" + URLEncoder.encode(jsonObject.getString("log"), "UTF-8") + "&id=" + URLEncoder.encode(jsonObject.getString("id"), "UTF-8");
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
            return output.equals("success");
        } catch (Exception e) {
            this.e = e;
            return false;
        }
    }
}
