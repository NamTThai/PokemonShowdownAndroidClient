package com.pokemonshowdown.app;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import com.pokemonshowdown.application.MyApplication;

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

    // version.txt contains versionCode versionName
    private final static String VERSION_URL = "http://ns3367227.ip-37-187-3.eu/showdown/version.txt";

    private int status;
    private String serverVersionName = null;
    private int serverVersionCode = 0;

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
        HttpResponse response;
        try {
            response = client.execute(httpget);
            status = response.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                String[] serverOutput = EntityUtils.toString(entity).split(" ");
                serverVersionCode = Integer.parseInt(serverOutput[0]);
                serverVersionName = serverOutput[1];
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
                int currentVersionCode = myApplication.getPackageManager()
                        .getPackageInfo(myApplication.getApplicationContext().getPackageName(), 0)
                        .versionCode;
                if (currentVersionCode < serverVersionCode) {
                    LocalBroadcastManager.getInstance(MyApplication.getMyApplication())
                            .sendBroadcast(new Intent(MyApplication.ACTION_FROM_MY_APPLICATION)
                                    .putExtra(MyApplication.EXTRA_DETAILS, MyApplication.EXTRA_UPDATE_AVAILABLE)
                                    .putExtra(MyApplication.EXTRA_SERVER_VERSION, serverVersionName));
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

}
