package com.aiworker.bcineuroskyservice;

import java.util.Random;

public class Algorithm {	
	
	/** create dynamic time-series from rapid changing time-series 
	 * @return DynamicTS */
	public static float CreateDynamic(int TS, float DynamicTS,
			float tsMin, float tsMax, float graviton,
			float ClusterX1, float ClusterX2, float ClusterDeltaX){
		 // -- limit time-series
	     if (DynamicTS>=tsMax)	{DynamicTS = tsMax; }          
	     if (DynamicTS<=tsMin )	{DynamicTS = tsMin; }
	     
	     // -- update DynamicTS depending on TS value
	     if(TS >= ClusterX1-ClusterDeltaX &&	TS <= ClusterX2+ClusterDeltaX) 
														   { DynamicTS = DynamicTS ; }
		 	else {
			 	if (TS > ClusterX2+ClusterDeltaX )		   { DynamicTS = DynamicTS + TS*graviton/100; } 
				else {
						if (TS < ClusterX1-ClusterDeltaX ) { DynamicTS = DynamicTS - TS*graviton/100;  }
					}                    
				}
	     
		 // -- limit time-series
	     if (DynamicTS>=tsMax)	{DynamicTS = tsMax; }          
	     if (DynamicTS<=tsMin )	{DynamicTS = tsMin; }
	     
	     return DynamicTS;
	}
	
	/** convert S to dynamic movement*/
	public static float StoDynamicMovement(int TS, float DynamicTS,
			float tsMin, float tsMax, float graviton,
			float ClusterX1, float ClusterX2, float ClusterDeltaX){
		 // -- limit time-series
	     if (DynamicTS>=tsMax)	{DynamicTS = tsMax; }          
	     if (DynamicTS<=tsMin )	{DynamicTS = tsMin; }
	     
	     // -- update DynamicTS depending on TS value
	     if(TS >= ClusterX1-ClusterDeltaX &&	TS <= ClusterX2+ClusterDeltaX)
	     												   { DynamicTS = DynamicTS - graviton; }
		 	else {
			 	if (TS > ClusterX2+ClusterDeltaX )		   { DynamicTS = DynamicTS + graviton; } 
				else {
						if (TS < ClusterX1-ClusterDeltaX ) { DynamicTS = DynamicTS + graviton; }
					}                    
				}
	     

	     if (DynamicTS>=tsMax)	{DynamicTS = tsMax; }          
	     if (DynamicTS<=tsMin )	{DynamicTS = tsMin; }
	     
	     return DynamicTS;
	}
	
	/** convert Index (Att or Med) to dynamic movement/rotational acceleration*/
	public static float AMtoDynamicMovement(int TS, float DynamicTS,
			float tsMin, float tsMax, float graviton,
			float ClusterX1, float ClusterDeltaX){
		 // -- limit time-series
	     if (DynamicTS>=tsMax)	{DynamicTS = tsMax; }          
	     if (DynamicTS<=tsMin )	{DynamicTS = tsMin; }
	     
	     // -- update DynamicTS depending on TS value
	     if(TS >= ClusterX1+ClusterDeltaX )				   { DynamicTS = DynamicTS + graviton; }
		 	else {
			 	if (TS < ClusterX1+ClusterDeltaX )		   { DynamicTS = DynamicTS - graviton; }              
				}
	     

	     if (DynamicTS>=tsMax)	{DynamicTS = tsMax; }          
	     if (DynamicTS<=tsMin )	{DynamicTS = tsMin; }
	     
	     return DynamicTS;
	}
	
	/** 
	 * convert acceleration to angular dynamic movement
	 * <p>
	 * accelAlpha = accelAlpha + (float) Math.toDegrees(acceleration) 
	 * @param alpha - rotational angle
	 * @param acceleration - rotational acceleration
	 * @return alpha*/
	public static float CircularMovement(float alpha, float acceleration){	
		alpha = alpha + (float) Math.toDegrees(acceleration);
		if (alpha>=360){alpha = alpha - 360;}
		return alpha;
	}
	
	/** 
	 * calculate array (non-zero values) average
	 * <p>
	 * @param histData - array with historical data
	 * @param l - length of the history to be used in calculating moving average
	 * @return average*/
	public static float MovingAverage(float[] hData, int l){	
		  int count = 0;
		  float total = 0;
		  float average;
		  
		  for (int i = hData.length - l; i < hData.length; i++)
		  {
		      if (hData[i] > 0)
		      {
		          total += hData[i];
		          count++;
		      } 
		  }

		  average = total / count;
		
		  return average;
	}

	public static int nonZeroLength(float[] histData){	
		  int count = 0;

		  int l = histData.length;
		  //Are you sure you want this to start at index 1?
		  for (int i = 0; i < l; i++)
		  {
		      if (histData[i] > 0)
		      {
		          count++;
		      } 
		  }
		
		  return count;
	}

	/** 
	 * save EEG index to the end of the histData array
	 * <p>
	 * @param index - EEG index
	 * @param hData - array with historical data
	 * @return histData with new values*/
	public static float[] saveIndexToArray(int index, float[] hData){
		// -- save index to the end of the array		
		switch(MainActivity.UserControl){
		  	 // -- store current index value
		  	 case "A": {				  		 
				hData[hData.length-1] = index;				  		 
		  		break;
		  	 }
		  	 case "M": {
		  		hData[hData.length-1] = index;
		  		break;
		  	 }
		  	 case "S": {
		  		hData[hData.length-1] = (index+100)/2;
		  		break;
		  	 }
	  	 
		  }
			
		return hData;
	}
	
	/** 
	 * shift 1D array to the left
	 * <p>
	 * @param hData - 1D array
	 * @return hData shifted array*/
	public static float[] shiftToLeft1DArray(float[] hData){	
		for (int j = 0; j < hData.length-1; j++) {
			hData[j] = hData[j+1]; 
		}
			
		return hData;
	}
	
	/** 
	 * shift 2D array to the left
	 * <p>
	 * @param hData - 2D array
	 * @return hData shifted array*/
	public static float[][] shiftToLeft2DArray(float[][] hData, int d1, int d2){			
		for (int j = 0; j < d1-1; j++) {
			  for (int i = 0; i < d2; i++) {
				  hData[i][j] = hData[i][j+1];
			  }
		  }
			
		return hData;
	}
	
	/** 
	 * get specific EEG index based on user preferrence
	 * <p>
	 * @return index (A, M or S)*/
	public static int getSpecificEEGIndex(){			
		switch(MainActivity.UserControl){
	 	 case "A":
	 		return eegService.At;
	 	 case "M":
	 		return eegService.Med;
	 	 case "S":
	 		return (eegService.At - eegService.Med);
	 	 
	 	 default:
	 	     return 0;
	 	 }			
	}
	
	
}
