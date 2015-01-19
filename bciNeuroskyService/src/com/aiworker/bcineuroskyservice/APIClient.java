package com.aiworker.bcineuroskyservice;

import android.content.Context;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.text.DateFormat;

import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.message.BasicHeader;
import java.io.IOException;
import com.loopj.android.http.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Map;

import com.neurosky.thinkgear.TGEegPower;

public class APIClient {
    // NOTE: Fill 3 constants below with right values and build the application
    private static int profileId = 11;
    private static int exerciseId = 16;
    private static String token = "hY1C-Lrbi7wZSMW7os9x";
    private static String host = "neuro-backend.herokuapp.com";
    private static boolean backendEnabled = true;
    private static boolean backendConfigured = true;

    private static final AsyncHttpClient client = new AsyncHttpClient();

    private static final Gson gson = new GsonBuilder().create();

    private static String lastTS = null;
    private static StatsCollector statsCollector = new StatsCollector();

    public static void setProfileId(int pId) {
        profileId = pId;
        checkConfiguration();
    }

    public static void setExerciseId(int eId) {
        exerciseId = eId;
        checkConfiguration();
    }

    public static void setToken(String tkn) {
        token = tkn;
        checkConfiguration();
    }

    public static void setHost(String hst) {
        host = hst;
        checkConfiguration();
    }

    public static void setBackendEnabled(boolean enabled) {
        backendEnabled = enabled;
        checkConfiguration();
    }

    private static void checkConfiguration() {
        if (profileId > 0 && exerciseId > 0 && token != "" && host != "") {
            backendConfigured = true;
        } else {
            backendConfigured = false;
        }
    }

    public static void collectMeditation(Context context, int meditation) {
        if (backendEnabled && backendConfigured) {
            sendDataIfRequired();
            statsCollector.addMeditation(meditation);
        }
    }

    public static void collectAttention(Context context, int attention) {
        if (backendEnabled && backendConfigured) {
            sendDataIfRequired();
            statsCollector.addAttention(attention);
        }
    }

    public static void collectEEGPower(Context context, TGEegPower eegPower) {
        if (backendEnabled && backendConfigured) {
            sendDataIfRequired();
            statsCollector.addEEGPower(eegPower);
        }
    }

    public static void postData() {
        try {
            StringEntity entity = new StringEntity(generateJSONString());
            client.post(null, getAPIURL(), entity, "application/json", new AsyncHttpResponseHandler() {});
        } catch (IOException ioEx) {
            System.out.println(ioEx.toString());
        }
    }

    private static String generateJSONString() {
        NeuroStat neuro_stat = new NeuroStat(exerciseId, "NeuroSky", "active", lastTS, statsCollector);

        DeviceData device_data = new DeviceData();
        device_data.stats.add(neuro_stat);

        return gson.toJson(device_data);
    }

    private static long getCurrentTimeUTCMilliseconds() {
        Calendar now = Calendar.getInstance();
        // offset to add since we're not UTC
        long offset = now.get(Calendar.ZONE_OFFSET) + now.get(Calendar.DST_OFFSET);

        return (now.getTimeInMillis() + offset) % (24 * 60 * 60 * 1000);
    }

    private static String getCurrentTimeUTC() {
        DateFormat df = DateFormat.getDateTimeInstance();
        df.setTimeZone(TimeZone.getTimeZone("gmt"));
        return df.format(new Date());
    }

    private static String getAPIURL() {
        return "http://" + host + "/profiles/" +
            profileId + "/exercises_data.json?auth_token=" + token;
    }

    private static void sendDataIfRequired() {
        String ts = getCurrentTimeUTC();
        if (lastTS != null && !lastTS.equals(ts)) {
            postData();
            statsCollector = new StatsCollector();
        }
        lastTS = ts;
    }
    
    public static void getData() {
//        try {
//            StringEntity entity = new StringEntity(generateJSONString());
//            client.post(null, getAPIURL(), entity, "application/json", new AsyncHttpResponseHandler() {});
//        } catch (IOException ioEx) {
//            System.out.println(ioEx.toString());
//        }
        
//        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://www.google.com", new AsyncHttpResponseHandler() {  });
    }
}