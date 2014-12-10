package com.pokemonshowdown.replays;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.logging.Handler;

/**
 * Battle log of all events are stored in
 * TODO : Allow cached copies of replays
 */
public abstract class RetrieveReplayTask extends AsyncTask<String, Void, Element> implements ResponseHandler{
    Document doc;

    @Override
    protected Element doInBackground(String... url) {
        Element BattleLog;
        Log.i("Getting Replay", url[0]);
        try {
            doc = Jsoup.connect(url[0]).get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        BattleLog = doc.select("script.log").first();
        return BattleLog;
    }

    @Override
    protected void onPostExecute(Element element) {
        OnResponseRecived(element);
        return;
    }

    @Override
    public abstract void OnResponseRecived(Object result);

}
