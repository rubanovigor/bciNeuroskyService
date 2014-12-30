package com.aiworker.bcineuroskyservice;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AppsActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_apps);		
	}
	
	// -- start AttSierpF
	public void onClickStart_AttSierpF (View v)
	{
		Intent intent = new Intent(this, ProcessingToroid.class);
		startActivity(intent);
	}
	
	// -- start AttVividF
	public void onClickStart_AttVividF (View v)
	{
		Intent intent = new Intent(this, ProcessingAttVividF.class);
		startActivity(intent);
	}
	
	
	// -- start mindOS
	public void onClickStart_MindOS (View v)
	{
		Intent intent = new Intent(this, ProcessingWave.class);
		startActivity(intent);
	}
	
	// -- start network game sample
	public void onClickStart_NetworkGame (View v)
	{
		Intent intent = new Intent(this, ProcessingNewtorkGame.class);
		startActivity(intent);
	}
	
	// -- start network game sample
	public void onClickStart_rndNGame (View v)
	{
		Intent intent = new Intent(this, ProcessingRNDgame.class);
		startActivity(intent);
	}
	
	
}
