package com.aiworker.bcineuroskyservice;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

import com.neurosky.thinkgear.TGDevice;
import com.neurosky.thinkgear.TGEegPower;

public class eegService extends Service{
	private static final String TAG = "eegService";
	public String userName = "";
	public String appName;
	
	// -- used for getting the handler from other class for sending messages
	public static Handler 		meegServiceHandler 			= null;
	// -- used for keep track on Android running status
	public static Boolean 		mIsServiceRunning 			= false;
	private String NeuroskyCurrentStatus;
	// -- BT and TG
	private BluetoothAdapter bluetoothAdapter;	TGDevice tgDevice;
	private static final boolean RAW_ENABLED = false; // false by default	
	public static int At=0; public static int Med=0;
	public static int At_pl2=0; public static int Med_pl2=0;
	private int delta = 0; private int high_alpha = 0; private int high_beta = 0; private int low_alpha = 0;
	private int low_beta = 0; private int low_gamma = 0; private int mid_gamma = 0; private int theta = 0;
	private String CurrentActivity = "default";
	 	
	// -- notifications
	private NotificationManager mNotificationManager;
	private int notificationID = 100;
	private int numMessages = 0;
	   
	   
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
//	   userName = intent.getStringExtra("UserName");
//	   CurrentActivity = intent.getStringExtra("UserActivity");
	   userName = "UserName";
	   CurrentActivity = "UserActivity";
		
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
		//return Service.START_REDELIVER_INTENT;
	}
    
	@Override
	public void onDestroy() {
		//Toast.makeText(this, "eegService Stopped", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onDestroy");
		
		tgDevice.close();
		Message msgToActivityDestroyed = new Message();
		msgToActivityDestroyed.what = 3; // -- disconnected 		    						
		MainActivity.mUiHandler.sendMessage(msgToActivityDestroyed);	
		
		cancelNotification();
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
    					/*if(null != MainActivity.mUiHandler)
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
    					}*/
    					
    					CurrentActivity = msg.obj.toString();
    					
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
            return;
        } else {
            if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
            	Message msgToActivity = new Message();
            	msgToActivity.what = 4; // -- Bluetooth is OFF
			    msgToActivity.obj  = "Bluetooth is OFF\n turn Bluetooth ON first!"; 		    						
				MainActivity.mUiHandler.sendMessage(msgToActivity);  
				NeuroskyCurrentStatus = "Bluetooth is OFF, turn it ON to start!";
				displayNotification();
                return;
            }
            if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
            	// -- create the TGDevice 
                tgDevice = new TGDevice(bluetoothAdapter, handler);
            	StartEEG();
            }
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
			                    	//mBroadcaster.broadcastIntentWithState(Constants.STATE_CONNECTING);  
		    						// -- send message to the MainActivity
		    						//Message msgToActivity = new Message();
		    						msgToActivity.what = 1; // -- connecting 
		    					    msgToActivity.obj  = "connecting . . ."; 		    						
		    						MainActivity.mUiHandler.sendMessage(msgToActivity);	
		    						NeuroskyCurrentStatus = "Neurosky connecting . . .";
		    						displayNotification();
			                        break;
			                    case TGDevice.STATE_CONNECTED:
			                        tgDevice.start();
			                        
		    						msgToActivity.what = 1; // -- connecting 
		    					    msgToActivity.obj  = "connected"; 		    						
		    						MainActivity.mUiHandler.sendMessage(msgToActivity);
		    						NeuroskyCurrentStatus = "Neurosky connected";
		    						displayNotification();
			                        break;
			                    case TGDevice.STATE_DISCONNECTED:
				    				msgToActivity.what = 1; // -- connecting 
			    					msgToActivity.obj  = "neurosky mindwave mobile\ndisconnected"; 		    						
			    					MainActivity.mUiHandler.sendMessage(msgToActivity);  
			    					NeuroskyCurrentStatus = "Neurosky mindwave mobile disconnected";
			    					displayNotification();
			                    	break;
			                    case TGDevice.STATE_NOT_FOUND:
		    						msgToActivity.what = 1; // -- connecting 
		    					    msgToActivity.obj  = "neurosky mindwave mobile\nwas not found"; 		    						
		    						MainActivity.mUiHandler.sendMessage(msgToActivity); 
		    						NeuroskyCurrentStatus = "Neurosky mindwave mobile was not found";
		    						displayNotification();
			                        break;
			                    case TGDevice.STATE_NOT_PAIRED:
			                    	//Toast.makeText(this, "neurosky mindwave mobile not paired !", Toast.LENGTH_SHORT).show();
			    					msgToActivity.what = 1; // -- connecting 
		    					    msgToActivity.obj  = "neurosky mindwave mobile\nnot paired"; 		    						
		    						MainActivity.mUiHandler.sendMessage(msgToActivity); 
		    						NeuroskyCurrentStatus = "Neurosky mindwave mobile not paired";
		    						displayNotification();
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
			                    // -- send Attention data to the backend in async way
			                	if(MainActivity.backend){ APIClient.collectAttention(null, msg.arg1);}
			                	
			                	int[] iam={0, 0};
			                	if(MainActivity.backend){ iam = APIClient.getData();}
			                	At_pl2 = iam[0]; 
			                	Med_pl2 = iam[1];
			                	Log.e("ir_Response Att", String.valueOf(iam[0]));	
//			                	Log.e("ir_Response Med", String.valueOf(iam[1]));	
			                	
			                    At = msg.arg1;  
			                    
			                    msgToActivity.what = 2; // -- sending At/Med  
			                    msgToActivity.arg1 = At;
			                    msgToActivity.arg2 = Med;
	    					    msgToActivity.obj  = "connected"; 		    						
	    						MainActivity.mUiHandler.sendMessage(msgToActivity);	
	    						updateNotification();
	    						
			                    //mBroadcaster.broadcastIntentWithA(At);  
			                    //setUpAsForeground("Att: " + String.valueOf(At) + "||" + " Med: " + String.valueOf(Med) );
		                		
			                    //tv_Att.setText(String.valueOf(At));
			                    //mMusicPlayerThread.setAttention(At);
			                          
	    	                    // --saving data to file
	    	                    String filename; 
	    	                    //String userName = "ihar";
	    	                    Time now = new Time();
	    	                    now.setToNow();
	    	                    String date_time = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));                    
	    	                    filename = "bciNeuroskyService" + date_time + ".csv";
	    	                    
	    	                    writeToExternalStoragePublic(filename, userName, now, At, Med);
	    	                    //writeToExternalStoragePublic(filename, gmail, now, At, Med);
			                    break;
			                    
			                case TGDevice.MSG_MEDITATION:
			                	// -- First send Meditation data to the backend in async way
			                	if(MainActivity.backend){APIClient.collectMeditation(null, msg.arg1);}

			                    Med = msg.arg1;
			                    msgToActivity.what = 2; // -- sending At/Med  
			                    msgToActivity.arg1 = At;
			                    msgToActivity.arg2 = Med;
	    					    msgToActivity.obj  = "connected"; 		    						
	    					    MainActivity.mUiHandler.sendMessage(msgToActivity);	
	    					    updateNotification();
	    					    
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
			                    // -- First send eegPower data to the backend in async way
			                    if(MainActivity.backend){APIClient.collectEEGPower(null, eegPower);}
			                    
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
		   String userName_l, Time now_l, int At_l, int Med_l) {
		    	
		        String packageName = this.getPackageName();
		        String path = Environment.getExternalStorageDirectory().getAbsolutePath()
		        		+ "/Android/data/" + packageName + "/files/";

		        String titles = "bciApp;username;time;att;med;"
		        		+ "delta;high_alpha;high_beta;low_alpha;low_beta;low_gamma;mid_gamma;theta;"
		        		+ "CurrentActivity";
		        
		        appName = packageName;
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
		                    	//fOut.write(("bciNeuroskyService" + ";").getBytes());
		                    	fOut.write((appName + ";").getBytes()); 
		                    	fOut.write((userName_l.toString() + ";").getBytes());
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
		                        fOut.write((CurrentActivity + "\n").getBytes());
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

	@SuppressLint("NewApi")
	protected void displayNotification() {
	      //Log.i("Start", "notification");

	      /* Invoking the default notification service */
	      NotificationCompat.Builder  mBuilder = new NotificationCompat.Builder(this);	
	      //mBuilder.setOngoing(true); //this will make ongoing notification
	      
	      mBuilder.setContentTitle(NeuroskyCurrentStatus);
	      //mBuilder.setContentText("...");
	      mBuilder.setTicker(NeuroskyCurrentStatus);
	      mBuilder.setSmallIcon(R.drawable.ic_launcher);

	      /* Increase notification number every time a new notification arrives */
	      //mBuilder.setNumber(++numMessages);
	      
	      /* Creates an explicit intent for an Activity in your app */
	      //Intent resultIntent = new Intent(this, NotificationView.class);
	      Intent resultIntent = new Intent(this, MainActivity.class);

	      TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
	      //stackBuilder.addParentStack(NotificationView.class);
	      stackBuilder.addParentStack(MainActivity.class);

	      /* Adds the Intent that starts the Activity to the top of the stack */
	      stackBuilder.addNextIntent(resultIntent);
	      PendingIntent resultPendingIntent =
	         stackBuilder.getPendingIntent(
	            0,
	            PendingIntent.FLAG_UPDATE_CURRENT
	         );

	      mBuilder.setContentIntent(resultPendingIntent);

	      mNotificationManager =
	      (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

	      /* notificationID allows you to update the notification later on. */
	      mNotificationManager.notify(notificationID, mBuilder.build());
	   }

	protected void cancelNotification() {
	      Log.i("Cancel", "notification");
	      mNotificationManager.cancel(notificationID);
	}

    @SuppressLint("NewApi")
	protected void updateNotification() {
	      Log.i("Update", "notification");

	      /* Invoking the default notification service */
	      NotificationCompat.Builder  mBuilder =  new NotificationCompat.Builder(this);	

	      mBuilder.setContentTitle(NeuroskyCurrentStatus);
	      mBuilder.setContentText("A: " + At + " | M: " + Med);
	     // mBuilder.setTicker("New Message Alert!");
	      mBuilder.setSmallIcon(R.drawable.ic_launcher);
	      

	     /* Increase notification number every time a new notification arrives */
	      //mBuilder.setNumber(++numMessages);
	      
	      /* Creates an explicit intent for an Activity in your app */
	      //Intent resultIntent = new Intent(this, NotificationView.class);
	      Intent resultIntent = new Intent(this, MainActivity.class);
	      
	      TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
	      //stackBuilder.addParentStack(NotificationView.class);
	      stackBuilder.addParentStack(MainActivity.class);
	      
	      /* Adds the Intent that starts the Activity to the top of the stack */
	      stackBuilder.addNextIntent(resultIntent);
	      PendingIntent resultPendingIntent =
	         stackBuilder.getPendingIntent(
	            0,
	            PendingIntent.FLAG_UPDATE_CURRENT
	         );

	      mBuilder.setContentIntent(resultPendingIntent);

	      mNotificationManager =
	      (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

	      /* Update the existing notification using same notification ID */
	      mNotificationManager.notify(notificationID, mBuilder.build());
	   }
	
}