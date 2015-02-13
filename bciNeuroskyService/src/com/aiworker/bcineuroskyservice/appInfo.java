package com.aiworker.bcineuroskyservice;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class appInfo extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.tutorial);
		
	}
	
	
}
