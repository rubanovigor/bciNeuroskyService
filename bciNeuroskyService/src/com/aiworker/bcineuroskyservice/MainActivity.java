package com.aiworker.bcineuroskyservice;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
	
	// -- processing variables 
	int pts = 40; 	float angle = 0;	float radius = 60.0f;
		// -- lathe segments
	int segments = 60;	float latheAngle = 0;	float latheRadius = 100.0f;
		// -- vertices
	PVector vertices[], vertices2[];
		// -- for shaded or wireframe rendering 
	boolean isWireFrame = false;
		// -- for optional helix
	boolean isHelix = false;	float helixOffset = 5.0f;
	// -- END processing variables 
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
        tv_Att = (TextView) findViewById(R.id.Att_label);
        tv_Med = (TextView) findViewById(R.id.Med_lable);       
        tv_NeuroskyStatus = (TextView) findViewById(R.id.NeuroskyStatus);
			
		
        // -- get the between instance stored values (status of music player)
        tv_NeuroskyStatus.setText(NeuroskyStatus);
        tv_Att.setText(String.valueOf(ServiceRunningFlag));
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        if (preferences.getString(Key_NeuroskyStatus, null) != null){
        	NeuroskyStatus =  preferences.getString(Key_NeuroskyStatus, null);
        	ServiceRunningFlag =  preferences.getString(Key_ServiceRunningFlag, "stoped");
        	tv_NeuroskyStatus.setText(NeuroskyStatus);
        	tv_Att.setText(String.valueOf(ServiceRunningFlag));
        }
        
        // -- manage correct appearance of the start/stop buttons
        //tv_Att.setText(String.valueOf(eegService.mIsServiceRunning)); 
        //tv_Att.setText(String.valueOf(ServiceRunningFlag));
        //if(eegService.mIsServiceRunning == true){
		if(ServiceRunningFlag.equals("running")){
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
		} 
		
		
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
	          				Button StopServiceButton=(Button)findViewById(R.id.stop_service);
		          			StopServiceButton.setVisibility(View.VISIBLE); 
		          			Button StartServiceButton=(Button)findViewById(R.id.start_service);
		          			StartServiceButton.setVisibility(View.INVISIBLE); 
	          			}
	          			if(NeuroskyStatus.equals("connecting . . .") ){
	          				Button StopServiceButton=(Button)findViewById(R.id.stop_service);
		          			StopServiceButton.setVisibility(View.INVISIBLE); 
		          			Button StartServiceButton=(Button)findViewById(R.id.start_service);
		          			StartServiceButton.setVisibility(View.INVISIBLE); 
	          			}
	          			if(NeuroskyStatus.equals("neurosky mindwave mobile\ndisconnected") || 
	          			   NeuroskyStatus.equals("neurosky mindwave mobile\nnot paired") ||
	          			   NeuroskyStatus.equals("neurosky mindwave mobile\nwas not found")    ){
	          				Button StopServiceButton=(Button)findViewById(R.id.stop_service);
		          			StopServiceButton.setVisibility(View.INVISIBLE); 
		          			Button StartServiceButton=(Button)findViewById(R.id.start_service);
		          			StartServiceButton.setVisibility(View.VISIBLE); 
	          				
	          			}
	          		
	          			break;
	          			
	          		case 2:
	          			At = msg.arg1; tv_Att.setText(String.valueOf(At));
	          			Med = msg.arg2; tv_Med.setText(String.valueOf(Med));
	          			NeuroskyStatus = msg.obj.toString();
	          			tv_NeuroskyStatus.setText(NeuroskyStatus);
	          			break;
	          		
	          		case 3:
	          			tv_Att.setText("-");
	          			tv_Med.setText("-");
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
	
	//s -- start toroid
	public void onClickStart_toroid (View v)
	{
		Intent intent = new Intent(this, ProcessingToroid.class);
		startActivity(intent);
	}
	
	//s -- start wave
	public void onClickStart_wave (View v)
	{
		Intent intent = new Intent(this, ProcessingWave.class);
		startActivity(intent);
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
        	// -- commit to storage 
        editor.commit(); 
           
        //finish();
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	        return super.onKeyDown(keyCode, event);
	}
		
	
}