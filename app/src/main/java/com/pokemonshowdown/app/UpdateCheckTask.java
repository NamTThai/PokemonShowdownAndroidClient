package com.pokemonshowdown.app;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.pokemonshowdown.data.MyApplication;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class UpdateCheckTask extends AsyncTask<Void, Void, Void> {
    private final static String TAG = UpdateCheckTask.class.getName();
    private final static String VERSION_URL = "http://ns3367227.ip-37-187-3.eu/showdown/version.txt";

    private int status;
    private String serverVersion = null;
    private MyApplication myApplication;
    public UpdateCheckTask(MyApplication application) {
        myApplication = application;
    }


    @Override
    protected Void doInBackground(Void... voids) {
        final HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 50000);
        HttpClient client = new DefaultHttpClient(httpParams);
        HttpGet httpget = new HttpGet(VERSION_URL);
        HttpResponse response = null;
        try {
            response = client.execute(httpget);
            status = response.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                serverVersion = EntityUtils.toString(entity);
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (status == HttpStatus.SC_OK) {
            try {
                String currentVersion = myApplication.getPackageManager().getPackageInfo(myApplication.getApplicationContext().getPackageName(), 0).versionName;
                if (isUpdateAvailable(currentVersion, serverVersion)) {
                    LocalBroadcastManager.getInstance(MyApplication.getMyApplication()).sendBroadcast(new Intent(MyApplication.ACTION_FROM_MY_APPLICATION).putExtra(MyApplication.EXTRA_DETAILS, MyApplication.EXTRA_UPDATE_AVAILABLE));
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    private boolean isUpdateAvailable(String currentVersion, String serverVersion) {
        serverVersion = serverVersion.trim();
        currentVersion = currentVersion.trim();

        String[] serverSplit = serverVersion.split("\\.");
        String[] currentSplit = currentVersion.split("\\.");

        Log.i(TAG, "Current version " + currentVersion);
        Log.i(TAG, "Server version " + serverVersion);

        int maxLength = serverSplit.length > currentSplit.length ? currentSplit.length : serverSplit.length;

        for (int i = 0; i < maxLength; i++) {
            int server = Integer.parseInt(serverSplit[i]);
            int current = Integer.parseInt(currentSplit[i]);

            if (server > current) {
                return true;
            }
        }
        // we only get here if we have something like 1.0.1 and 1.0.1.1, or the two version are equals
        return serverSplit.length > currentSplit.length;
    }
}
