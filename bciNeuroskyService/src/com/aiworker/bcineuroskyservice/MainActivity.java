package com.aiworker.bcineuroskyservice;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
public static Handler mUiHandler = null;

TextView tv_Med;    TextView tv_Att;    TextView tv_NeuroskyStatus; 
private int At=42; private int Med=42;

String NeuroskyStatus; String Key_NeuroskyStatus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
        tv_Att = (TextView) findViewById(R.id.Att_label);
        tv_Med = (TextView) findViewById(R.id.Med_lable);       
        tv_NeuroskyStatus = (TextView) findViewById(R.id.NeuroskyStatus);
		//Button resetButton=(Button)findViewById(R.id.my_button_del);
		//resetButton.setVisibility(0); //To set visible
		
        // -- get the between instance stored values (status of music player)
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        if (preferences.getString(Key_NeuroskyStatus, null) != null){
        	NeuroskyStatus =  preferences.getString(Key_NeuroskyStatus, null);
        	tv_NeuroskyStatus.setText(NeuroskyStatus);
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
	          			break;
	          			
	          		case 2:
	          			tv_Att.setText(String.valueOf(msg.arg1));
	          			tv_Med.setText(String.valueOf(msg.arg2));
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
	}
	//Stop the started service
	public void onClickStopService(View V)
	{
		//Stop the running service from here//eegService is your service class name
		//Service will only stop if it is already running.
		stopService(new Intent(this, eegService.class));
	}
	//send message to service
	public void onClickSendMessage (View v)
	{
		//only we need a handler to send message to any component.
		//here we will get the handler from the service first, then 
		//we will send a message to the service.

		if(null != eegService.meegServiceHandler)
		{
			//first build the message and send.
			//put a integer value here and get it from the service handler
			//For Example: lets use 0 (msg.what = 0;) for getting service running status from the service
			Message msg = new Message();
			msg.what = 0; 
			msg.obj  = "Add your Extra Meaage Here"; // you can put extra message here
			eegService.meegServiceHandler.sendMessage(msg);
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
        	// -- commit to storage 
        editor.commit(); 
           
       // finish();
    }

}