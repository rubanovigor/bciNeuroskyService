package com.aiworker.bcineuroskyservice;

import android.content.Context;
import android.util.Log;

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


import com.loopj.android.http.AsyncHttpClient;
import org.apache.http.Header;
import org.apache.http.HttpEntity;


public class APIClient {
    // NOTE: Fill 3 constants below with right values and build the application
	// for rubanovigor@gmail.com
		//    private static int profileId = 9;
		//    private static int exerciseId = 11;
		//    private static String token = "x_x55Xp1DgVW8jBXdGfk"; 
	
//    public static int profileId = MainActivity.profileId;
//    public static int exerciseId = MainActivity.exerciseId;
//    public static String token = MainActivity.token;
		// -- backend settings for local user
    private static int profileId, exerciseId;	private static String token;
    	// -- backend settings for network user
    private static int profileIdNetUser, exerciseIdNetUser; public static String tokenNetUser = "";
    
//    private static String host = "neuro-backend.herokuapp.com";
    private static String host = "neurolyzer.herokuapp.com";
    private static boolean backendEnabled = true;
    private static boolean backendConfigured = true;
    public static String msgFromBackend;
	public static int[] indexes = {0, 0};
	public static String lastTS1;
    
    
//    "http://" + "neuro-backend.herokuapp.com" + "/profiles/" + 11 + "/exercises_data.json?auth_token=" + "hY1C-Lrbi7wZSMW7os9x";

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

    // -- for network user
    public static void setProfileIdNetUser(int pId) {profileIdNetUser = pId;  checkConfiguration(); }
    public static void setExerciseIdNetUser(int eId){exerciseIdNetUser = eId; checkConfiguration(); }
    public static void setTokenNetUser(String tkn)  {tokenNetUser = tkn;      checkConfiguration(); }

      
    
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
    
    // ====================================
    public static int[] getData() {
//	     String ts1 = getCurrentTimeUTC();
//	     if (lastTS1 != null && !lastTS1.equals(ts1)) {
	//      AsyncHttpClient client = new AsyncHttpClient();
            client.get(null, getPartnerLatestDataURL(), null, null, new AsyncHttpResponseHandler(){
                @Override
                public void onSuccess(String response) {
                	msgFromBackend = response;
                	indexes = parseMsg(msgFromBackend);
                	Log.e("ir_Response", response);               
                }

               @Override
                 public void onFailure(Throwable e) {
            	   Log.e("error","ir_OnFailure!", e);
                 }
            });
//	      }
//	      lastTS1 = ts1;   
//        	Log.e("ir_Response Att:", String.valueOf(indexes[0]));
//        	Log.e("ir_Response Med:", String.valueOf(indexes[1]));
          return indexes;
    }

    public static String getPartnerLatestDataURL() {
//    	return "http://" + host + "/profiles/" + profileId + "/exercises/" + exerciseId +
//    			"/statistics/latest_stat.json?auth_token=" + token;
//    	return "http://" + host + "/profiles/" + profileId + "/exercises/" + exerciseId +
//    			"/statistics/latest.json?auth_token=" + token;
    	
    	return "http://" + host + "/profiles/" + profileIdNetUser + "/exercises/" + exerciseIdNetUser +
    			"/statistics/latest.json?auth_token=" + tokenNetUser;
    } 

    
    /*public static String getPartnerLatestDataURL() {
    	return "http://" + host + "/profiles/" + profileIdNetUser + "/exercises/" + exerciseIdNetUser +
    			"/statistics/latest_stat.json?auth_token=" + tokenNetUser;
    }*/
    // ====================================
    /*public static int[] getDataNetUser() {
          client.get(null, getPartnerLatestDataURLNetUser(), null, null,
        		  new AsyncHttpResponseHandler(){
		                @Override
		                public void onSuccess(String response) {
		                	msgFromBackend = response;
		                	indexes = parseMsg(msgFromBackend);
		                	Log.e("ir_Response", response);               
		                }
		
		               @Override
		                 public void onFailure(Throwable e) {
		            	   Log.e("error","ir_OnFailure!", e);
		                 }
          		  }
          );

          return indexes;
    }

    public static String getPartnerLatestDataURLNetUser() {
    	return "http://" + host + "/profiles/" + profileIdNetUser + "/exercises/" + exerciseIdNetUser +
    			"/statistics/latest_stat.json?auth_token=" + tokenNetUser;
    }
    */
    
    public static int[] parseMsg(String msg) {
    	String[] separatedMsg = msg.split(",");
    	int[] indexes_l = {0, 0};
    	String[] separatedMsgAtt = separatedMsg[4].split(":");
    	indexes_l[0] = Integer.valueOf(separatedMsgAtt[1]);
    	String[] separatedMsgMed = separatedMsg[5].split(":");
    	indexes_l[1] = Integer.valueOf(separatedMsgMed[1]);
    	
//    	Log.e("ir_Response Att:", String.valueOf(indexes_l[0]));
//    	Log.e("ir_Response Med:", String.valueOf(indexes_l[1]));
    	return indexes_l;
    }
    
    
//    01-20 15:27:36.675: E/ir_Response(31244): 
//    {"id":199254,"profile_id":11,"exercise_id":16,"timestamp":"2015-01-20T18:27:34.000Z",
//    	"attention":21,"meditation":64,
//    	"high_alpha":-32626.0,"low_alpha":-22455.0,"high_beta":-30498.0,"low_beta":18124.0,
//    	"mid_gamma":8504.0,"low_gamma":3788.0,"delta":7886.0,"theta":-28942.0}
 
    
}