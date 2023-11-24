package com.coding.maintenancehouse.network;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@SuppressWarnings("deprecation")
public class NetworkUtil extends AsyncTask<Void, Void, Boolean> {


    private final Consumer mConsumer;

    public interface Consumer {
        void accept(Boolean internet);
    }

    public NetworkUtil(Consumer consumer) {
        mConsumer = consumer;
        execute();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            URL url = new URL("https://www.google.com");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(3000);
            httpURLConnection.connect();
            if (httpURLConnection.getResponseCode() == 200) {
                return true;
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean isConnectedToInternet) {
        mConsumer.accept(isConnectedToInternet);
    }



}
