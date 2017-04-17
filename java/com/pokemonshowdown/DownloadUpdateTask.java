package com.pokemonshowdown;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import com.pokemonshowdown.activity.ContainerActivity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DownloadUpdateTask extends AsyncTask<Void, Integer, Void> {
    private final static String APK_LOCATION = "http://ns3367227.ip-37-187-3.eu/showdown/current.apk";
    private ProgressDialog waitingDialog;
    private int status;

    private ContainerActivity battleFieldActivity;

    public DownloadUpdateTask(ContainerActivity battleFieldActivity) {
        waitingDialog = new ProgressDialog(battleFieldActivity);
        waitingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        waitingDialog.setMessage(battleFieldActivity.getResources().getString(R.string.downloadingupdate));
        waitingDialog.setCancelable(false);
        waitingDialog.setMax(100);

        this.battleFieldActivity = battleFieldActivity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        battleFieldActivity.runOnUiThread(new java.lang.Runnable() {
            public void run() {
                waitingDialog.show();
            }
        });
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        waitingDialog.setProgress(values[0]);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        final HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
        HttpClient client = new DefaultHttpClient(httpParams);
        HttpGet httpget = new HttpGet(APK_LOCATION);
        HttpResponse response;
        try {
            response = client.execute(httpget);
            status = response.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                InputStream fileStream = entity.getContent();
                File file = new File(battleFieldActivity.getApplicationContext()
                        .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "temp.apk");
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);

                long totalLength = entity.getContentLength();
                int readLength = 0;
                int count;
                byte data[] = new byte[1024];
                while ((count = fileStream.read(data)) != -1) {
                    readLength += count;
                    publishProgress((int) ((readLength * 100) / totalLength));
                    fos.write(data, 0, count);
                }

                fos.flush();
                fos.close();
                fileStream.close();
            }
        } catch (IOException e) {
            return null;
        }
        return null;

    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        battleFieldActivity.runOnUiThread(new java.lang.Runnable() {
            public void run() {
                waitingDialog.dismiss();
            }
        });

        if (status == HttpStatus.SC_OK) {
            File file = new File(battleFieldActivity.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "temp.apk");

            Intent promptInstall = new Intent(Intent.ACTION_VIEW)
                    .setDataAndType(Uri.fromFile(file),
                            "application/vnd.android.package-archive");
            battleFieldActivity.startActivity(promptInstall);
        }

    }
}
