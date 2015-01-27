package com.aiworker.bcineuroskyservice;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
	
	TextView tv_Med;    TextView tv_Att;    TextView tv_NeuroskyStatus; 
	public static int At=42; public static int Med=42;
	public String ServiceRunningFlag = "stoped";  String Key_ServiceRunningFlag;
	public String NeuroskyStatus = ""; String Key_NeuroskyStatus;
	
	// -- toggle/switch buttons
	ToggleButton toggleActivity1, toggleActivity2, toggleActivity3;
	ToggleButton toggleUser1, toggleUser2, toggleUser3, toggleUser4;
	public static boolean State_toggleActivity1, State_toggleActivity2, State_toggleActivity3;
	public static boolean State_toggleUser1, State_toggleUser2, State_toggleUser3, State_toggleUser4;
	String Key_State_toggleActivity1, Key_State_toggleActivity2, Key_State_toggleActivity3;
	String Key_State_toggleUser1, Key_State_toggleUser2, Key_State_toggleUser3, Key_State_toggleUser4;
	
	Switch serviceOnOff, backendOnOff;
	public static boolean State_serviceOnOff, State_backendOnOff;
	String Key_State_serviceOnOff, Key_State_backendOnOff;
	public static boolean backend = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);
		setContentView(R.layout.service_main);
		
        tv_Att = (TextView) findViewById(R.id.Att_label);
        tv_Med = (TextView) findViewById(R.id.Med_lable);       
        tv_NeuroskyStatus = (TextView) findViewById(R.id.NeuroskyStatus);
		
        toggleActivity1 = (ToggleButton) findViewById(R.id.toggleActivity1);
        toggleActivity2 = (ToggleButton) findViewById(R.id.toggleActivity2);
        toggleActivity3 = (ToggleButton) findViewById(R.id.toggleActivity3);
        toggleUser1 = (ToggleButton) findViewById(R.id.toggleUser1);
        toggleUser2 = (ToggleButton) findViewById(R.id.toggleUser2);
        toggleUser3 = (ToggleButton) findViewById(R.id.toggleUser3);
        toggleUser4 = (ToggleButton) findViewById(R.id.toggleUser4);
        serviceOnOff = (Switch) findViewById(R.id.switch_service);
        backendOnOff = (Switch) findViewById(R.id.switch_backend);
		
        
        // -- get the between instance stored values (status of music player)
        tv_NeuroskyStatus.setText(NeuroskyStatus);
        //tv_Att.setText(String.valueOf(ServiceRunningFlag));
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        if (preferences.getString(Key_NeuroskyStatus, null) != null){
        	NeuroskyStatus =  preferences.getString(Key_NeuroskyStatus, null);
        	ServiceRunningFlag =  preferences.getString(Key_ServiceRunningFlag, "stoped");
//        	tv_NeuroskyStatus.setText(NeuroskyStatus);
        	
        	// -- restore toogle/switch buttons state
        	State_toggleActivity1 =  Boolean.valueOf(preferences.getString(Key_State_toggleActivity1, "false"));
        	toggleActivity1.setChecked(State_toggleActivity1);
//        	toggleActivity1.setChecked(true);
        	tv_NeuroskyStatus.setText(preferences.getString(Key_State_toggleActivity1, "aa"));
        	State_toggleActivity2 =  Boolean.valueOf(preferences.getString(Key_State_toggleActivity2, "false"));
        	toggleActivity2.setChecked(State_toggleActivity2);
        	State_toggleActivity3 =  Boolean.valueOf(preferences.getString(Key_State_toggleActivity3, "false"));
        	toggleActivity3.setChecked(State_toggleActivity3);
        	
        	State_toggleUser1 =  Boolean.valueOf(preferences.getString(Key_State_toggleUser1, "false"));
        	toggleUser1.setChecked(State_toggleUser1);
        	State_toggleUser2 =  Boolean.valueOf(preferences.getString(Key_State_toggleUser2, "false"));
        	toggleUser2.setChecked(State_toggleUser2);
        	State_toggleUser3 =  Boolean.valueOf(preferences.getString(Key_State_toggleUser3, "false"));
        	toggleUser3.setChecked(State_toggleUser3);
        	State_toggleUser4 = Boolean.valueOf(preferences.getString(Key_State_toggleUser4, "false"));
        	toggleUser4.setChecked(State_toggleUser4);
        	
        	State_serviceOnOff =  Boolean.valueOf(preferences.getString(Key_State_serviceOnOff, "false"));
        	serviceOnOff.setChecked(State_serviceOnOff);
        	State_backendOnOff =  Boolean.valueOf(preferences.getString(Key_State_backendOnOff, "false"));
        	backendOnOff.setChecked(State_backendOnOff);
        	
        	//tv_Att.setText(String.valueOf(ServiceRunningFlag));
        }
        
        // -- manage correct appearance of the start/stop buttons
//        //tv_Att.setText(String.valueOf(eegService.mIsServiceRunning)); 
//        //tv_Att.setText(String.valueOf(ServiceRunningFlag));
//        //if(eegService.mIsServiceRunning == true){
		/*if(ServiceRunningFlag.equals("running")){
			Button StartServiceButton=(Button)findViewById(R.id.start_service);
			StartServiceButton.setVisibility(View.INVISIBLE); 
			Button StopServiceButton=(Button)findViewById(R.id.stop_service);
			StopServiceButton.setVisibility(View.VISIBLE); 
		} else {
			Button StartServiceButton=(Button)findViewById(R.id.start_service);
			StartServiceButton.setVisibility(View.VISIBLE); 		
			Button StopServiceButton=(Button)findViewById(R.id.stop_service);
			StopServiceButton.setVisibility(View.INVISIBLE); 
		}
		
		if(NeuroskyStatus.equals("connected")){
			Button StartServiceButton=(Button)findViewById(R.id.start_service);
			StartServiceButton.setVisibility(View.INVISIBLE); 
			Button StopServiceButton=(Button)findViewById(R.id.stop_service);
			StopServiceButton.setVisibility(View.VISIBLE); 
		}
	    if(NeuroskyStatus.equals("connecting . . .")){
		    Button StopServiceButton=(Button)findViewById(R.id.stop_service);
		    StopServiceButton.setVisibility(View.INVISIBLE); 
		} */
        
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
//	          			tv_NeuroskyStatus.setText(NeuroskyStatus);

	          			if(NeuroskyStatus.equals("connected") ){
//	          				Button StopServiceButton=(Button)findViewById(R.id.stop_service);
//		          			StopServiceButton.setVisibility(View.VISIBLE); 
//		          			Button StartServiceButton=(Button)findViewById(R.id.start_service);
//		          			StartServiceButton.setVisibility(View.INVISIBLE); 
	          			}
	          			if(NeuroskyStatus.equals("connecting . . .") ){
//	          				Button StopServiceButton=(Button)findViewById(R.id.stop_service);
//		          			StopServiceButton.setVisibility(View.INVISIBLE); 
//		          			Button StartServiceButton=(Button)findViewById(R.id.start_service);
//		          			StartServiceButton.setVisibility(View.INVISIBLE); 
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
//	          			tv_NeuroskyStatus.setText(NeuroskyStatus);
	          			break;
	          		
	          		case 3:
	          			tv_Att.setText("-");
	          			tv_Med.setText("-");
	          			break;
	          		
	          		case 4:
	          			NeuroskyStatus = msg.obj.toString();
	          			tv_NeuroskyStatus.setText(NeuroskyStatus);
          				serviceOnOff.setChecked(false);
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
	public void onClickSendMessage (View v)
	{
		//only we need a handler to send message to any component.
		//here we will get the handler from the service first, then 
		//we will send a message to the service.

        EditText et_CurrentActivity = (EditText)findViewById(R.id.editTextCurrentActivity);
        
		if(null != eegService.meegServiceHandler)
		{
			//first build the message and send.
			//put a integer value here and get it from the service handler
			//For Example: lets use 0 (msg.what = 0;) for getting service running status from the service
			Message msg = new Message();
			msg.what = 0; 
			msg.obj  = et_CurrentActivity.getText(); // you can put extra message here
			eegService.meegServiceHandler.sendMessage(msg);
			//et_CurrentActivity.setText("");
		}
	}
	
//	// -- start toroid
//	public void onClickStart_toroid (View v)
//	{
//		Intent intent = new Intent(this, ProcessingToroid.class);
//		startActivity(intent);
//	}
	
	// -- start toroid
	public void onClickStart_activity_apps (View v)
	{
		Intent intent = new Intent(this, AppsActivity.class);
		startActivity(intent);
	}

	/** switch on/off the service*/
	public void onSwitchClickedService(View view) {
	    // Is the toggle on?
//	    boolean on = serviceOnOff.isChecked();
	    State_serviceOnOff = serviceOnOff.isChecked();
	    
	    if (State_serviceOnOff) {
			startService(new Intent(this, eegService.class));
			ServiceRunningFlag = "running";
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
	
//	//s -- start wave
//	public void onClickStart_wave (View v)
//	{
//		Intent intent = new Intent(this, ProcessingWave.class);
//		startActivity(intent);
//	}
		
	public void onClicktoggleActivity1(View view) {
	    // Is the toggle on?
		State_toggleActivity1 = toggleActivity1.isChecked();
	    
	    if (State_toggleActivity1) {
	    	toggleActivity2.setChecked(false); State_toggleActivity2 = false;
	    	toggleActivity3.setChecked(false); State_toggleActivity3 = false;
	    	if (State_toggleUser1 || State_toggleUser2 || State_toggleUser3 || State_toggleUser4)
	    		{serviceOnOff.setVisibility(View.VISIBLE); backendOnOff.setVisibility(View.VISIBLE);}
	    } else {
	    	if (!State_toggleActivity2 && !State_toggleActivity3) {
	    		toggleActivity1.setChecked(true);
	    		State_toggleActivity1 = true;
	    	}
	    }
	}
	
	public void onClicktoggleActivity2(View view) {
	    // Is the toggle on?
		State_toggleActivity2 = toggleActivity2.isChecked();
	    
	    if (State_toggleActivity2) {
	    	toggleActivity1.setChecked(false); State_toggleActivity1 = false;
	    	toggleActivity3.setChecked(false); State_toggleActivity3 = false;
	    	if (State_toggleUser1 || State_toggleUser2 || State_toggleUser3 || State_toggleUser4)
    			{serviceOnOff.setVisibility(View.VISIBLE); backendOnOff.setVisibility(View.VISIBLE);}
	    } else {
	    	if (!State_toggleActivity1 && !State_toggleActivity3) {
	    		toggleActivity2.setChecked(true);
	    		State_toggleActivity2 = true;
	    	}
	    }
	}
	
	public void onClicktoggleActivity3(View view) {
	    // Is the toggle on?
		State_toggleActivity3 = toggleActivity3.isChecked();
	    
	    if (State_toggleActivity3) {
	    	toggleActivity1.setChecked(false); State_toggleActivity1 = false;
	    	toggleActivity2.setChecked(false); State_toggleActivity2 = false;
	    	if (State_toggleUser1 || State_toggleUser2 || State_toggleUser3 || State_toggleUser4)
    			{serviceOnOff.setVisibility(View.VISIBLE); backendOnOff.setVisibility(View.VISIBLE);}
	    } else {
	        if (!State_toggleActivity1 && !State_toggleActivity2) {
	        	toggleActivity3.setChecked(true);
	        	State_toggleActivity3 = true;	
	        }
	    }
	}
	
	public void onClicktoggleUser1(View view) {
	    // Is the toggle on?
		State_toggleUser1 = toggleUser1.isChecked();
	    
	    if (State_toggleUser1) {
	    	toggleUser2.setChecked(false); State_toggleUser2 = false;
	    	toggleUser3.setChecked(false); State_toggleUser3 = false;
	    	toggleUser4.setChecked(false); State_toggleUser4 = false;
	    	if (State_toggleActivity1 || State_toggleActivity2 || State_toggleActivity3)
    			{serviceOnOff.setVisibility(View.VISIBLE); backendOnOff.setVisibility(View.VISIBLE);}
	    } else {
	    	if (!State_toggleUser2 && !State_toggleUser3 && !State_toggleUser4) {
	    		toggleUser1.setChecked(true);
	    		State_toggleUser1 = true;	
	    	}
	    }
	}
	
	public void onClicktoggleUser2(View view) {
	    // Is the toggle on?
		State_toggleUser2 = toggleUser2.isChecked();
	    
	    if (State_toggleUser2) {
	    	toggleUser1.setChecked(false); State_toggleUser1 = false;
	    	toggleUser3.setChecked(false); State_toggleUser3 = false;
	    	toggleUser4.setChecked(false); State_toggleUser4 = false;
	    	if (State_toggleActivity1 || State_toggleActivity2 || State_toggleActivity3)
				{serviceOnOff.setVisibility(View.VISIBLE); backendOnOff.setVisibility(View.VISIBLE);}
	    } else {
	    	if (!State_toggleUser1 && !State_toggleUser3 && !State_toggleUser4) {
	    		toggleUser2.setChecked(true);
	    		State_toggleUser2 = true;
	    	}
	    }
	}
	
	public void onClicktoggleUser3(View view) {
	    // Is the toggle on?
		State_toggleUser3 = toggleUser3.isChecked();
	    
	    if (State_toggleUser3) {
	    	toggleUser2.setChecked(false); State_toggleUser2 = false;
	    	toggleUser1.setChecked(false); State_toggleUser1 = false;
	    	toggleUser4.setChecked(false); State_toggleUser4 = false;
	    	if (State_toggleActivity1 || State_toggleActivity2 || State_toggleActivity3)
				{serviceOnOff.setVisibility(View.VISIBLE); backendOnOff.setVisibility(View.VISIBLE);}
	    } else {
	    	if (!State_toggleUser1 && !State_toggleUser2 && !State_toggleUser4) {
	    		toggleUser3.setChecked(true);
	    		State_toggleUser3 = true;	
	    	}
	    }
	}
	
	public void onClicktoggleUser4(View view) {
	    // Is the toggle on?
		State_toggleUser4 = toggleUser4.isChecked();
	    
	    if (State_toggleUser4) {
	    	toggleUser2.setChecked(false); State_toggleUser2 = false;
	    	toggleUser3.setChecked(false); State_toggleUser3 = false;
	    	toggleUser1.setChecked(false); State_toggleUser1 = false;
	    	if (State_toggleActivity1 || State_toggleActivity2 || State_toggleActivity3)
				{serviceOnOff.setVisibility(View.VISIBLE); backendOnOff.setVisibility(View.VISIBLE);}
	    } else {
	    	if (!State_toggleUser1 && !State_toggleUser2 && !State_toggleUser3) {
	    		toggleUser4.setChecked(true);
	    		State_toggleUser4 = true;	
	    	}
	    }
	}
	
   

	
	@Override
    public void onPause() {        
        super.onPause();
                       
        // -- store values between instances here  
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);  
        SharedPreferences.Editor editor = preferences.edit();  // Put the values from the UI 
        	//editor.putBoolean(KEY_play_flag, true); // value to store  
        editor.putString(Key_NeuroskyStatus, NeuroskyStatus);
        editor.putString(Key_ServiceRunningFlag, ServiceRunningFlag);
        // -- toggle/switch buttons state
//        editor.putString(Key_State_toggleActivity1, Boolean.toString(State_toggleActivity1));
//        editor.putString(Key_State_toggleActivity1, Boolean.toString(State_toggleActivity1));
        editor.putString(Key_State_toggleActivity1, "true");
        editor.putString(Key_State_toggleActivity2, Boolean.toString(State_toggleActivity2));
        editor.putString(Key_State_toggleActivity3, Boolean.toString(State_toggleActivity3));
        editor.putString(Key_State_toggleUser1, Boolean.toString(State_toggleUser1));
        editor.putString(Key_State_toggleUser2, Boolean.toString(State_toggleUser2));
        editor.putString(Key_State_toggleUser3, Boolean.toString(State_toggleUser3));
        editor.putString(Key_State_toggleUser4, Boolean.toString(State_toggleUser4)); 
        
        editor.putString(Key_State_serviceOnOff, Boolean.toString(State_serviceOnOff)); 
        editor.putString(Key_State_backendOnOff, Boolean.toString(State_backendOnOff)); 
        
        	// -- commit to storage 
        editor.commit(); 
           
        //finish();
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	        return super.onKeyDown(keyCode, event);
	}
		
	
}