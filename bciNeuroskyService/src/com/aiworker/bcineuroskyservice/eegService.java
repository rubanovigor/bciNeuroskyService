package com.aiworker.bcineuroskyservice;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

import com.neurosky.thinkgear.TGDevice;
import com.neurosky.thinkgear.TGEegPower;

public class eegService extends Service{
	private static final String TAG = "eegService";
	
	// -- used for getting the handler from other class for sending messages
	public static Handler 		meegServiceHandler 			= null;
	// -- used for keep track on Android running status
	public static Boolean 		mIsServiceRunning 			= false;

	// -- BT and TG
	private BluetoothAdapter bluetoothAdapter;	TGDevice tgDevice;
	private String BTstatus;  private static final boolean RAW_ENABLED = false; // false by default	
	private int At=42; private int Med=42;
	private int delta = 0; private int high_alpha = 0; private int high_beta = 0; private int low_alpha = 0;
	private int low_beta = 0; private int low_gamma = 0; private int mid_gamma = 0; private int theta = 0;
	 	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {

		//Toast.makeText(this, "eegService Created", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onCreate");
	}

	@Override
	public void onStart(Intent intent, int startId) {
		//Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onStart");	
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	   processStartTG();
		 
	   MyThread myThread = new MyThread();
	   myThread.start();
		
		/*try 
		{
			Thread.sleep(1000);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}*/
		
		mIsServiceRunning = true; // set service running status = true
		
		//Toast.makeText(this, "Congrats! My Service Started", Toast.LENGTH_LONG).show();
		// We need to return if we want to handle this service explicitly. 
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		//Toast.makeText(this, "eegService Stopped", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onDestroy");
		
		
		tgDevice.close();
		Message msgToActivityDestroyed = new Message();
		msgToActivityDestroyed.what = 3; // -- disconnected 		    						
		MainActivity.mUiHandler.sendMessage(msgToActivityDestroyed);	
		
		mIsServiceRunning = false; // make it false, as the service is already destroyed.
		
		
	}
	
	//Your inner thread class is here to getting response from Activity and processing them
	class MyThread extends Thread
	{
		private static final String INNER_TAG = "MyThread";
		
    	public void run() 
    	{
    		this.setName(INNER_TAG);

    		// Prepare the looper before creating the handler.
			Looper.prepare();
			meegServiceHandler = new Handler()
			{
				//here we will receive messages from activity(using sendMessage() from activity)
    			public void handleMessage(Message msg)
    			{
    				Log.i("BackgroundThread","handleMessage(Message msg)" );
    				switch(msg.what)
    				{
    				case 0: // we sent message with what value =0 from the activity. here it is
    						//Reply to the activity from here using same process handle.sendMessage()
    						//So first get the Activity handler then send the message
    					if(null != MainActivity.mUiHandler)
    					{
    						//first build the message and send.
    						//put a integer value here and get it from the Activity handler
    						//For Example: lets use 0 (msg.what = 0;) 
    						//for receiving service running status in the activity
    						Message msgToActivity = new Message();
    						msgToActivity.what = 0; 
    						if(true ==mIsServiceRunning)
    							msgToActivity.obj  = "Request Received. Service is Running"; // you can put extra message here
    						else
    							msgToActivity.obj  = "Request Received. Service is not Running"; // you can put extra message here
    						
    						MainActivity.mUiHandler.sendMessage(msgToActivity);
    					}
    					
    				break;
    				
					default:
						break;
    				}
				}
    		};
    		Looper.loop();
    	}
	}


    void processStartTG() {
        // -- checking BT and connecting to the TG device
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {	// Alert user that Bluetooth is not available
        	Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_SHORT).show();
            //finish();
        	BTstatus = "bt turned off";
            return;
        } else { // create the TGDevice 
            tgDevice = new TGDevice(bluetoothAdapter, handler);
           // updateNotification("disconnected");
            StartEEG();
        	// Toast.makeText(this, "Bluetooth available", Toast.LENGTH_SHORT).show();
             
        }
	}

	public void StartEEG() {
		if (tgDevice.getState() != TGDevice.STATE_CONNECTING && tgDevice.getState() != TGDevice.STATE_CONNECTED) {
		    tgDevice.connect(RAW_ENABLED);
		}
	}
	
	// -- Handles messages from TGDevice 
	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
					  Message msgToActivity = new Message();
					   switch (msg.what) {
			                case TGDevice.MSG_STATE_CHANGE:
			                    /*display message according to state change type */
			                    switch (msg.arg1) {
			                    case TGDevice.STATE_IDLE:
			                        break;
			                    case TGDevice.STATE_CONNECTING:
			                    	//Toast.makeText(this, "connecting...", Toast.LENGTH_SHORT).show();
			                    	//BTstatus = "connecting...";
			                    	//mBroadcaster.broadcastIntentWithState(Constants.STATE_CONNECTING);  
		    						// -- send message to the MainActivity
		    						//Message msgToActivity = new Message();
		    						msgToActivity.what = 1; // -- connecting 
		    					    msgToActivity.obj  = "connecting . . ."; 		    						
		    						MainActivity.mUiHandler.sendMessage(msgToActivity);		    						
			                        break;
			                    case TGDevice.STATE_CONNECTED:
			                        tgDevice.start();
			                        
		    						msgToActivity.what = 1; // -- connecting 
		    					    msgToActivity.obj  = "connected"; 		    						
		    						MainActivity.mUiHandler.sendMessage(msgToActivity);
			                        break;
			                    case TGDevice.STATE_NOT_FOUND:
			                    	//Toast.makeText(this, "neurosky mindwave mobile was not found", Toast.LENGTH_SHORT).show();
		    						msgToActivity.what = 1; // -- connecting 
		    					    msgToActivity.obj  = "neurosky mindwave mobile\nwas not found"; 		    						
		    						MainActivity.mUiHandler.sendMessage(msgToActivity); 
			                        break;
			                    case TGDevice.STATE_NOT_PAIRED:
			                    	//Toast.makeText(this, "neurosky mindwave mobile not paired !", Toast.LENGTH_SHORT).show();
			    					msgToActivity.what = 1; // -- connecting 
		    					    msgToActivity.obj  = "neurosky mindwave mobile\nnot paired"; 		    						
		    						MainActivity.mUiHandler.sendMessage(msgToActivity); 
			                        break;
			                    case TGDevice.STATE_DISCONNECTED:
			                    	//Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
			    					msgToActivity.what = 1; // -- connecting 
		    					    msgToActivity.obj  = "neurosky mindwave mobile\ndisconnected"; 		    						
		    						MainActivity.mUiHandler.sendMessage(msgToActivity);  
			                    	break;
			                    }

			                    break;
			                    
			                case TGDevice.MSG_POOR_SIGNAL:
			                      //int TGState = msg.arg1;
			                    break;
			                case TGDevice.MSG_RAW_DATA:
			                	//raw1 = msg.arg1;
			                    //tv.append("Got raw: " + msg.arg1 + "\n");                  
			                    break;
			                case TGDevice.MSG_HEART_RATE:
			                    //tv.append("Heart rate: " + msg.arg1 + "\n");
			                    break;
			                case TGDevice.MSG_ATTENTION:
			                    // -- First send Attention data to the backend in async way
			                	//APIClient.collectAttention(null, msg.arg1);
			                	
			                		 	               
			                    At = msg.arg1;  
			                    
			                    msgToActivity.what = 2; // -- sending At/Med  
			                    msgToActivity.arg1 = At;
			                    msgToActivity.arg2 = Med;
	    					    msgToActivity.obj  = "connected"; 		    						
	    						MainActivity.mUiHandler.sendMessage(msgToActivity);	
			                    
			                    //mBroadcaster.broadcastIntentWithA(At);  
			                    //setUpAsForeground("Att: " + String.valueOf(At) + "||" + " Med: " + String.valueOf(Med) );
		                		
			                    //tv_Att.setText(String.valueOf(At));
			                    //mMusicPlayerThread.setAttention(At);
			                          
	    	                    // --saving data to file
	    	                    String filename; 
	    	                    String user_g = "denis";
	    	                    Time now = new Time();
	    	                    now.setToNow();
	    	                    String date_time = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));                    
	    	                    filename = "bciCamera_" + date_time + ".csv";
	    	                    
	    	                    writeToExternalStoragePublic(filename, user_g, now, At, Med);
	    	                    //writeToExternalStoragePublic(filename, gmail, now, At, Med);
			                    break;
			                    
			                case TGDevice.MSG_MEDITATION:
			                	//APIClient.collectMeditation(null, msg.arg1);

			                    Med = msg.arg1;
			                    msgToActivity.what = 2; // -- sending At/Med  
			                    msgToActivity.arg1 = At;
			                    msgToActivity.arg2 = Med;
	    					    msgToActivity.obj  = "connected"; 		    						
	    					    MainActivity.mUiHandler.sendMessage(msgToActivity);	
			                    
			                    //mBroadcaster.broadcastIntentWithM(Med);  
			                    //tv_Med.setText(String.valueOf(Med));
			                    //mMusicPlayerThread.setMeditation(Med);
			                    
			                    break;
			                case TGDevice.MSG_BLINK:
			                    //tv.append("Blink: " + msg.arg1 + "\n");
			                    break;
			                case TGDevice.MSG_RAW_COUNT:
			                    //tv.append("Raw Count: " + msg.arg1 + "\n");
			                    break;
			                case TGDevice.MSG_LOW_BATTERY:
			                    Toast.makeText(getApplicationContext(), "Low battery!", Toast.LENGTH_SHORT).show();
			                    break;
			                case TGDevice.MSG_RAW_MULTI:
			                    //TGRawMulti rawM = (TGRawMulti)msg.obj;
			                    //tv.append("Raw1: " + rawM.ch1 + "\nRaw2: " + rawM.ch2);
			                
			                case TGDevice.MSG_SLEEP_STAGE:
			                	//sleep_stage = msg.arg1;
			                	break;
			                case TGDevice.MSG_EEG_POWER:
			                    TGEegPower eegPower = (TGEegPower) msg.obj;
			                    //APIClient.collectEEGPower(null, eegPower);
			                    
			                    delta = eegPower.delta;
			                    high_alpha = eegPower.highAlpha;
			                    high_beta = eegPower.highBeta;
			                    low_alpha = eegPower.lowAlpha;
			                    low_beta = eegPower.lowBeta;
			                    low_gamma = eegPower.lowGamma;
			                    mid_gamma = eegPower.midGamma;
			                    theta = eegPower.theta;
			                    break;
			                default:
			                    break;
		                }

			}
		};

		public void writeToExternalStoragePublic(String filename,
		    		String user_g_l, Time now_l, int At_l, int Med_l) {
		    	
		        String packageName = this.getPackageName();
		        String path = Environment.getExternalStorageDirectory().getAbsolutePath()
		        		+ "/Android/data/" + packageName + "/files/";

		        String titles = "bciApp;username;time;att;med;"
		        		+ "delta;high_alpha;high_beta;low_alpha;low_beta;low_gamma;mid_gamma;theta;";
		        
		        try {
		               boolean exists = (new File(path)).exists();
		               if (!exists) {
		                    new File(path).mkdirs();
		               }
		                   // -- open output stream
		               File file = new File(path + filename);
		               if(file.exists()) {
		                    	FileOutputStream fOut = new FileOutputStream(path + filename,true);
		                    	// -- write Head and integers as separated ascii's
		                    	//fOut.write((titles.toString() + "\n").getBytes());
		                    	fOut.write(("bciCamera;").getBytes());
		                    	fOut.write((user_g_l.toString() + ";").getBytes());
		                    	fOut.write((now_l.toString() + ";").getBytes());
		                        fOut.write((Integer.valueOf(At_l).toString() + ";").getBytes());
		                        fOut.write((Integer.valueOf(Med_l).toString() + ";").getBytes());
		                        fOut.write((Double.valueOf(delta).toString() + ";").getBytes());                        
		                        fOut.write((Double.valueOf(high_alpha).toString() + ";").getBytes());
		                        fOut.write((Double.valueOf(high_beta).toString() + ";").getBytes());
		                        fOut.write((Double.valueOf(low_alpha).toString() + ";").getBytes());
		                        fOut.write((Double.valueOf(low_beta).toString() + ";").getBytes());
		                        fOut.write((Double.valueOf(low_gamma).toString() + ";").getBytes());
		                        fOut.write((Double.valueOf(mid_gamma).toString() + ";").getBytes());
		                        fOut.write((Double.valueOf(theta).toString() + ";").getBytes());
		                        fOut.flush();
		                        fOut.close();
		                    }    
		                else {
		                    	// -- write integers as separated ascii's
		                    	FileOutputStream fOut = new FileOutputStream(path + filename,true);
		                    	fOut.write((titles.toString() + "\n").getBytes());
		                        fOut.flush();
		                        fOut.close();
		                    }
		                    
		                } catch (IOException e) {
		                    e.printStackTrace();
		                }
		          
		    }

}