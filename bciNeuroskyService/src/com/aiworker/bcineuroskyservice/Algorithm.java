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

//		  int l = hData.length;
//		  for (int i = 0; i < l; i++)
//		  {
//		      if (hData[i] > 0)
//		      {
//		          total += hData[i];
//		          count++;
//		      } 
//		  }
		  
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
	 * @param histData - array with historical data
	 * @return histData with new values*/
	public static float[] saveIndexToArray(int index, float[] hData){
		// -- check if Att&Med are non-zero (neurosky sending Att&Med)
//		if( eegService.At != 0 && eegService.Med !=0){
			// -- save index to the end of the array
			hData[hData.length-1] = index; 			
//		}
		
		return hData;
	}
}
