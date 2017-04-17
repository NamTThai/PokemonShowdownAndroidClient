package com.pokemonshowdown;

import android.content.pm.PackageManager;
import android.os.AsyncTask;

import com.pokemonshowdown.application.BroadcastSender;
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
    private final static String CHANGELOG_URL = "http://ns3367227.ip-37-187-3.eu/showdown/changelog.txt";

    private int status;
    private String serverVersionName = null;
    private String changelogData = null;

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
        HttpGet httpGetChangelog = new HttpGet(CHANGELOG_URL);

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

            response = client.execute(httpGetChangelog);
            int changelogStatus = response.getStatusLine().getStatusCode();
            if (changelogStatus == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                changelogData = EntityUtils.toString(entity);
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
                    BroadcastSender.get(MyApplication.getMyApplication()).sendBroadcastFromMyApplication(
                            BroadcastSender.EXTRA_UPDATE_AVAILABLE, null,
                            BroadcastSender.EXTRA_SERVER_VERSION, serverVersionName,
                            BroadcastSender.EXTRA_CHANGELOG, changelogData);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

}
