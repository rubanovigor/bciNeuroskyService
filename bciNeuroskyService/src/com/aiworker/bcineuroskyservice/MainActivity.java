package com.aiworker.bcineuroskyservice;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

public class MainActivity extends Activity{
//public class MainActivity extends PApplet{
	public static Handler mUiHandler = null;
	public final static String EXTRA_MESSAGE = "com.aiworker.bcineuroskyservice.MESSAGE";
	public static String userName="";
	public static String userActivity="";
	private static final String TAG = "MainActivity";
	public static int layer = 1;
	public static final String tService = "toggleButtonService";
	public static final String tBackEnd = "toggleButtonBackEnd";
	private static final int RESULT_SETTINGS = 666;
	
	TextView tv_Med;    TextView tv_Att;    TextView tv_NeuroskyStatus; 
	public static int At=42; public static int Med=42;
	public String ServiceRunningFlag = "stoped";  String Key_ServiceRunningFlag;
	public String NeuroskyStatus = ""; String Key_NeuroskyStatus;
		
//	Switch serviceOnOff, backendOnOff;
	ToggleButton serviceOnOff, backendOnOff;
	public static boolean State_serviceOnOff, State_backendOnOff;
	String Key_State_serviceOnOff, Key_State_backendOnOff;
	public static boolean backend = false;
	
	ImageButton ibAtt, ibMed, ibS, ibInfo, ibmOS, ibToroid, ibBack, imOneToroid, ibTwoToroids;
	public static String UserControl="att";
	public static String toroidGameType="you";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_ui_main);
		 Log.e("onCreate", "MainActivity"); 
		
        tv_Att = (TextView) findViewById(R.id.Att_label);
        tv_Med = (TextView) findViewById(R.id.Med_lable);       
        tv_NeuroskyStatus = (TextView) findViewById(R.id.NeuroskyStatus);
		
                     
        serviceOnOff = (ToggleButton) findViewById(R.id.switch_service);
        backendOnOff = (ToggleButton) findViewById(R.id.switch_backend);       
//        serviceOnOff = (Switch) findViewById(R.id.switch_service);
//        backendOnOff = (Switch) findViewById(R.id.switch_backend);

        ibAtt = (ImageButton) findViewById(R.id.ib_att); 
        ibMed = (ImageButton) findViewById(R.id.ib_med); 
        ibS = (ImageButton) findViewById(R.id.ib_s);     
        ibInfo = (ImageButton) findViewById(R.id.ib_info); 
        ibmOS = (ImageButton) findViewById(R.id.ib_mOS);        
        ibToroid = (ImageButton) findViewById(R.id.ib_toroid); 
        ibBack = (ImageButton) findViewById(R.id.ib_back); 
        imOneToroid = (ImageButton) findViewById(R.id.ib_onetoroid); 
        ibTwoToroids = (ImageButton) findViewById(R.id.ib_twotoroids); 

        
	    // -- for glass testing only
	   /* Button StopServiceButton=(Button)findViewById(R.id.stop_service);
	    StopServiceButton.setVisibility(View.INVISIBLE); 
	    Button StartServiceButton=(Button)findViewById(R.id.start_service);
		StartServiceButton.setVisibility(View.INVISIBLE); 
	    Button mActivity=(Button)findViewById(R.id.send_message);
	    mActivity.setVisibility(View.INVISIBLE); 
		Button temp=(Button)findViewById(R.id.start_wave);
		temp.setVisibility(View.INVISIBLE); 
			
		Intent intent = new Intent(this, ProcessingToroid.class);
		startActivity(intent);*/
		// - end testing for glass
		
		mUiHandler = new Handler() // Receive messages from service class 
        {
			public void handleMessage(Message msg)
        	{
          		switch(msg.what)
        		{
	          		case 0:
	          			// add the status which came from service and show on GUI
	          			Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
	          			break;
	          			
	          		case 1:
	          			NeuroskyStatus = msg.obj.toString();
	          			tv_NeuroskyStatus.setText(NeuroskyStatus);

	          			if(NeuroskyStatus.equals("connected") ){
	          				State_serviceOnOff = true; 
	          				serviceOnOff.setChecked(State_serviceOnOff); 
	          				tv_NeuroskyStatus.setText(NeuroskyStatus);
	          			}
	          			if(NeuroskyStatus.equals("connecting . . .") ){
	          				State_serviceOnOff = true; 
	          				serviceOnOff.setChecked(State_serviceOnOff); 
	          				tv_NeuroskyStatus.setText(NeuroskyStatus);
	          			}
	          			if(NeuroskyStatus.equals("neurosky mindwave mobile\ndisconnected") || 
	          			   NeuroskyStatus.equals("neurosky mindwave mobile\nnot paired") ||
	          			   NeuroskyStatus.equals("neurosky mindwave mobile\nwas not found")    ){
//	          				Button StopServiceButton=(Button)findViewById(R.id.stop_service);
//		          			StopServiceButton.setVisibility(View.INVISIBLE); 
//		          			Button StartServiceButton=(Button)findViewById(R.id.start_service);
//		          			StartServiceButton.setVisibility(View.VISIBLE); 
	          				State_serviceOnOff = false; serviceOnOff.setChecked(State_serviceOnOff);
	          				ServiceOff();
	          			}
	          		
	          			break;
	          			
	          		case 2:
	          			At = msg.arg1; tv_Att.setText(String.valueOf(At));
	          			Med = msg.arg2; tv_Med.setText(String.valueOf(Med));
	          			NeuroskyStatus = msg.obj.toString();
	          			tv_NeuroskyStatus.setText("connected");
	          			break;
	          		
	          		case 3: // service Destroyed
	          			tv_Att.setText("-");
	          			tv_Med.setText("-");
	          			break;
	          		
	          		case 4: // BT is OFF
	          			NeuroskyStatus = msg.obj.toString();
	          			tv_NeuroskyStatus.setText(NeuroskyStatus);
	          			State_serviceOnOff = false; serviceOnOff.setChecked(State_serviceOnOff);
          				//ServiceOff();
	          			break;
	          			         		
	        		default:
        			break;
        		}
        	}
        };
	}
	
	//start the service
	public void onClickStartServie(View V)
	{
		//start the service from here //eegService is your service class name
		startService(new Intent(this, eegService.class));
		//if(eegService.mIsServiceRunning == true){
			Button StartServiceButton=(Button)findViewById(R.id.start_service);
			StartServiceButton.setVisibility(View.INVISIBLE); 
		//}
		ServiceRunningFlag = "running";
		
	}
	//Stop the started service
	public void onClickStopService(View V)
	{
		//Stop the running service from here//eegService is your service class name
		//Service will only stop if it is already running.
		stopService(new Intent(this, eegService.class));
		
		Button StartServiceButton=(Button)findViewById(R.id.start_service);
		StartServiceButton.setVisibility(View.VISIBLE); 	
		Button StopServiceButton=(Button)findViewById(R.id.stop_service);
		StopServiceButton.setVisibility(View.INVISIBLE); 
		//NeuroskyStatus = "neurosky mindwave mobile\ndisconnected";
	    //tv_NeuroskyStatus.setText(NeuroskyStatus);
		ServiceRunningFlag = "stoped";
	}
	// -- submit current user activity 
//	public void onClickSendMessage (View v)
//	{
//		//only we need a handler to send message to any component.
//		//here we will get the handler from the service first, then 
//		//we will send a message to the service.
//
//        EditText et_CurrentActivity = (EditText)findViewById(R.id.editTextCurrentActivity);
//        
//		if(null != eegService.meegServiceHandler)
//		{
//			//first build the message and send.
//			//put a integer value here and get it from the service handler
//			//For Example: lets use 0 (msg.what = 0;) for getting service running status from the service
//			Message msg = new Message();
//			msg.what = 0; 
//			msg.obj  = et_CurrentActivity.getText(); // you can put extra message here
//			eegService.meegServiceHandler.sendMessage(msg);
//			//et_CurrentActivity.setText("");
//		}
//	}
	
//	// -- start toroid
//	public void onClickStart_toroid (View v)
//	{
//		Intent intent = new Intent(this, ProcessingToroid.class);
//		startActivity(intent);
//	}
	
//	// -- start actiwity with app button
//	public void onClickStart_activity_apps (View v)
//	{
//		Intent intent = new Intent(this, AppsActivity.class);
//		startActivity(intent);
//	}

	/** switch on/off the service*/
	public void onSwitchClickedService(View view) {
	    // Is the toggle on?
//	    boolean on = serviceOnOff.isChecked();
	    State_serviceOnOff = serviceOnOff.isChecked();
	    
	    if (State_serviceOnOff) {
//			startService(new Intent(this, eegService.class));
			ServiceRunningFlag = "running";
			
			Intent serviceIntent = new Intent(this, eegService.class);
			serviceIntent.putExtra("UserName", userName);
			serviceIntent.putExtra("UserActivity", userActivity);
			startService(serviceIntent);
			  
	    } else { //Service will only stop if it is already running
			stopService(new Intent(this, eegService.class));
			ServiceRunningFlag = "stoped";
	    }    
	}
	public void ServiceOff() {
		// -- Service will only stop if it is already running
			stopService(new Intent(this, eegService.class));
			ServiceRunningFlag = "stoped";
	}
	
	/** switch on/off the backend service*/
	public void onSwitchClickedBackEnd(View view) {
	    // Is the toggle on?
//	    boolean on = ((Switch) view).isChecked();
		State_backendOnOff = backendOnOff.isChecked();
	    
	    if (State_backendOnOff) {
	    	backend = true;
	    } else {
	        backend = false;
	    }
	}
	
	
//	public void onClicktoggleUser4(View view) {
//	    // Is the toggle on?
//		State_toggleUser4 = toggleUser4.isChecked();
//	    
//	    if (State_toggleUser4) {
//	    	userName = "AntonioManno";
//	    	toggleUser2.setChecked(false); State_toggleUser2 = false;
//	    	toggleUser3.setChecked(false); State_toggleUser3 = false;
//	    	toggleUser1.setChecked(false); State_toggleUser1 = false;
//	    	if (State_toggleActivity1 || State_toggleActivity2 || State_toggleActivity3)
//				{serviceOnOff.setVisibility(View.VISIBLE); backendOnOff.setVisibility(View.VISIBLE);}
//	    } else {
//	    	if (!State_toggleUser1 && !State_toggleUser2 && !State_toggleUser3) {
//	    		toggleUser4.setChecked(true);
//	    		State_toggleUser4 = true;	
//	    	}
//	    }
//	}
	
   
	@Override
    public void onStop() {        
        super.onStop();
        Log.e("onStop", "MainActivity"); 
	}
	
	@Override
    public void onPause() {        
        super.onPause();
        Log.e("onPause", "MainActivity"); ;    
       
        // -- service
        SharedPreferences ss1 = getSharedPreferences(tService, 0);
        SharedPreferences.Editor ee1 = ss1.edit(); 
        ee1.putBoolean(Key_State_serviceOnOff, State_serviceOnOff); ee1.commit();
        
        // -- backend
        SharedPreferences ss2 = getSharedPreferences(tBackEnd, 0);
        SharedPreferences.Editor ee2 = ss2.edit(); 
        ee2.putBoolean(Key_State_backendOnOff, State_backendOnOff); ee2.commit();
        
        
        
        //finish();
    }

	@Override
    public void onStart() {        
        super.onStart();
        Log.e("onStart", "MainActivity"); 
                	
	}    
	   
	@Override
    public void onResume() {        
        super.onResume();
        Log.e("onResume", "MainActivity"); 
        
        // load backend settings
        updateBackendSettings();   
        
	    // -- service
		SharedPreferences ss1 = getSharedPreferences(tService, 0);
		State_serviceOnOff =  ss1.getBoolean(Key_State_serviceOnOff, false);
			serviceOnOff.setChecked(State_serviceOnOff);
			
		// -- backend
		SharedPreferences ss2 = getSharedPreferences(tBackEnd, 0);
		State_backendOnOff =  ss2.getBoolean(Key_State_backendOnOff, false);
        	backendOnOff.setChecked(State_backendOnOff);			
    }   

	
	/** -- set S (A-M) flag for further use */
	public void onImageButtonS_Clicked (View v)
	{
		switchToLayer2();
		UserControl = "S";
	}
	
	/** -- set Attention flag for further use */
	public void onImageButtonA_Clicked (View v)
	{
		switchToLayer2();
		UserControl = "att";
	}
	
	/** -- set Meditation flag for further use */
	public void onImageButtonM_Clicked (View v)
	{
		switchToLayer2();
		UserControl = "med";
	}
	
	/** -- proceed with back button, returning to layer 1 */
	public void onImageButtonBack_Clicked (View v)
	{
		if(layer==2){switchToLayer1();}
		if(layer==3){switchToLayer2from3();}
	}
	
	/** -- proceed with info button */
	public void onImageButtonInfo_Clicked (View v)
	{
		Intent intent = new Intent(this, appInfo.class);
		startActivity(intent);
	}
	
	/** -- proceed with toroid button */
	public void onImageButtonToroid_Clicked (View v)
	{
		switchToLayer3from2();
	}
	
	// -- start mindOS
	public void onImageButtonMindOS_Clicked (View v)
	{
		Intent intent = new Intent(this, ProcessingWave.class);
		startActivity(intent);
	}
	
	/** -- proceed with onetoroid button */
	public void onImageButtonOneToroid_Clicked (View v)
	{
		toroidGameType="you";
//		Intent intent = new Intent(this, ProcessingToroid.class);
		Intent intent = new Intent(this, ProcessingRNDgame.class);
		startActivity(intent);
	}
	
	/** -- proceed with twotoroids button */
	public void onImageButtonTwoToroids_Clicked (View v)
	{
		toroidGameType="rnd vs you";
		Intent intent = new Intent(this, ProcessingRNDgame.class);
		startActivity(intent);
	}

	/** -- proceed with backend settings button */
	public void onImageButtonBackendSettings_Clicked (View v)
	{
		Intent intent = new Intent(this, backendSettingsActivity.class);
        startActivityForResult(intent, RESULT_SETTINGS);
	}
	
	//=====================================
	/** -- switch to layer 2 from layer 1 */
	public void switchToLayer2 ()
	{
		layer = 2;
		ibS.setVisibility(View.INVISIBLE); ibAtt.setVisibility(View.INVISIBLE); ibMed.setVisibility(View.INVISIBLE);
		ibmOS.setVisibility(View.VISIBLE); ibToroid.setVisibility(View.VISIBLE); ibBack.setVisibility(View.VISIBLE);
	}
	
	/** -- switch to layer 1 from layer 2*/
	public void switchToLayer1 ()
	{
		layer = 1;
		ibS.setVisibility(View.VISIBLE); ibAtt.setVisibility(View.VISIBLE); ibMed.setVisibility(View.VISIBLE);
		ibmOS.setVisibility(View.INVISIBLE); ibToroid.setVisibility(View.INVISIBLE); ibBack.setVisibility(View.INVISIBLE);
	}
	
	/** -- switch to layer 2 from layer 3 */
	public void switchToLayer2from3 ()
	{
		layer = 2;
		ibmOS.setVisibility(View.VISIBLE); ibToroid.setVisibility(View.VISIBLE); ibBack.setVisibility(View.VISIBLE);
		imOneToroid.setVisibility(View.INVISIBLE); ibTwoToroids.setVisibility(View.INVISIBLE); 
	}
	
	/** -- switch to layer 3 from layer 2 (toroid selection) */
	public void switchToLayer3from2 ()
	{
		layer = 3;
		imOneToroid.setVisibility(View.VISIBLE); ibTwoToroids.setVisibility(View.VISIBLE);  ibBack.setVisibility(View.VISIBLE);
		ibmOS.setVisibility(View.INVISIBLE); ibToroid.setVisibility(View.INVISIBLE);
	}
	
	 private void updateBackendSettings() {
	        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	        // -- backend_profile_id
	        if (sharedPrefs.getString("pref_backend_profile_id", "0").isEmpty()){
	    	    APIClient.setProfileId(0);	 
//		        tv_NeuroskyStatus.setText(sharedPrefs.getString("pref_backend_profile_id", "0"));
		    }else{
		    	APIClient.setProfileId(Integer.parseInt(sharedPrefs.getString("pref_backend_profile_id", "0")));
//		    	tv_NeuroskyStatus.setText(sharedPrefs.getString("pref_backend_profile_id", "0"));
		    }

	        
	        // -- backend_profile_id
	        if (sharedPrefs.getString("pref_backend_profile_id", "0").isEmpty()){
	        	APIClient.setProfileId(0);;	        
		    }else{
		    	APIClient.setProfileId(Integer.parseInt(sharedPrefs.getString("pref_backend_profile_id", "0")));;
		    }	        
	        
	        // -- rest string type settings
	        APIClient.setToken(sharedPrefs.getString("pref_backend_token", ""));
	        tv_NeuroskyStatus.setText(sharedPrefs.getString("pref_backend_token", ""));
	        
	        APIClient.setHost(sharedPrefs.getString("pref_backend_host", "neuro-backend.herokuapp.com"));
	        APIClient.setBackendEnabled(sharedPrefs.getBoolean("pref_use_backend", false));
	 }
	 
	/*@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	        return super.onKeyDown(keyCode, event);
	}*/
		
	
}